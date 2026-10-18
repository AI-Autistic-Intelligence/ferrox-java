package dev.ferrox.showcase.domain.settlement;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe In-Memory Double-Entry Ledger enforcing financial accounting invariants:
 * 1. Collateral verification before settlement (no unbacked margin).
 * 2. Strict conservation of assets: Sum of Debits == Sum of Credits.
 * 3. Atomic multi-leg journal entries for Buyer, Seller, and Treasury Fee.
 */
@Component
public class DoubleEntryLedger {

    public record JournalEntry(
            long entryId,
            String settlementId,
            String debitAccount,
            String creditAccount,
            String asset,
            double amount,
            long timestamp
    ) {}

    public static class Account {
        private final String accountId;
        private double usdBalance;
        private double btcBalance;

        public Account(String accountId, double initialUsd, double initialBtc) {
            this.accountId = accountId;
            this.usdBalance = initialUsd;
            this.btcBalance = initialBtc;
        }

        public synchronized double getUsdBalance() {
            return usdBalance;
        }

        public synchronized double getBtcBalance() {
            return btcBalance;
        }

        public synchronized void debitUsd(double amount) {
            if (usdBalance < amount) {
                throw new IllegalStateException("Insufficient USD collateral for account " + accountId + ": balance=" + usdBalance + ", required=" + amount);
            }
            usdBalance -= amount;
        }

        public synchronized void creditUsd(double amount) {
            usdBalance += amount;
        }

        public synchronized void debitBtc(double amount) {
            if (btcBalance < amount) {
                throw new IllegalStateException("Insufficient BTC collateral for account " + accountId + ": balance=" + btcBalance + ", required=" + amount);
            }
            btcBalance -= amount;
        }

        public synchronized void creditBtc(double amount) {
            btcBalance += amount;
        }
    }

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private final List<JournalEntry> journal = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong entrySequence = new AtomicLong(1);

    public DoubleEntryLedger() {
        // Seed default institutional market participants
        accounts.put("buyer-alpha", new Account("buyer-alpha", 5_000_000.0, 0.0));
        accounts.put("seller-beta", new Account("seller-beta", 0.0, 500.0));
        accounts.put("exchange-treasury", new Account("exchange-treasury", 0.0, 0.0));
    }

    public Account getOrCreateAccount(String accountId, double defaultUsd, double defaultBtc) {
        return accounts.computeIfAbsent(accountId, id -> new Account(id, defaultUsd, defaultBtc));
    }

    /**
     * Executes an atomic settlement between Buyer and Seller.
     * Leg 1: Buyer USD -> Seller USD (Notional)
     * Leg 2: Seller BTC -> Buyer BTC (Quantity)
     * Leg 3: Buyer USD -> Exchange Treasury (Maker/Taker fee: 0.05%)
     *
     * @return SettledTradeEvent containing settlement result and execution status
     */
    public SettledTradeEvent settleTrade(MatchedTradeEvent trade, long latencyMicros, String threadName) {
        String settlementId = "stl-" + UUID.randomUUID().toString().substring(0, 8);
        double notionalUsd = trade.price() * trade.quantity();
        double feeUsd = notionalUsd * 0.0005; // 5 bps fee
        long now = System.currentTimeMillis();

        Account buyer = getOrCreateAccount(trade.buyerId(), 1_000_000.0, 0.0);
        Account seller = getOrCreateAccount(trade.sellerId(), 0.0, 100.0);
        Account treasury = getOrCreateAccount("exchange-treasury", 0.0, 0.0);

        // Verification of collateral margins
        if (buyer.getUsdBalance() < (notionalUsd + feeUsd)) {
            return new SettledTradeEvent(
                    settlementId, trade.tradeId(), trade.symbol(), trade.buyerId(), trade.sellerId(),
                    trade.price(), trade.quantity(), notionalUsd, feeUsd,
                    "REJECTED_COLLATERAL",
                    "Buyer USD balance (" + buyer.getUsdBalance() + ") is insufficient for required notional + fee (" + (notionalUsd + feeUsd) + ")",
                    latencyMicros, threadName, now
            );
        }

        if (seller.getBtcBalance() < trade.quantity()) {
            return new SettledTradeEvent(
                    settlementId, trade.tradeId(), trade.symbol(), trade.buyerId(), trade.sellerId(),
                    trade.price(), trade.quantity(), notionalUsd, feeUsd,
                    "REJECTED_COLLATERAL",
                    "Seller BTC balance (" + seller.getBtcBalance() + ") is insufficient for required quantity (" + trade.quantity() + ")",
                    latencyMicros, threadName, now
            );
        }

        // Atomic multi-account execution lock ordering by accountId to prevent deadlocks
        Account firstLock = buyer.accountId.compareTo(seller.accountId) < 0 ? buyer : seller;
        Account secondLock = firstLock == buyer ? seller : buyer;

        synchronized (firstLock) {
            synchronized (secondLock) {
                // Double-entry execution
                buyer.debitUsd(notionalUsd + feeUsd);
                seller.creditUsd(notionalUsd);
                treasury.creditUsd(feeUsd);

                seller.debitBtc(trade.quantity());
                buyer.creditBtc(trade.quantity());

                // Journal records
                journal.add(new JournalEntry(entrySequence.getAndIncrement(), settlementId, buyer.accountId, seller.accountId, "USD", notionalUsd, now));
                journal.add(new JournalEntry(entrySequence.getAndIncrement(), settlementId, buyer.accountId, treasury.accountId, "USD", feeUsd, now));
                journal.add(new JournalEntry(entrySequence.getAndIncrement(), settlementId, seller.accountId, buyer.accountId, "BTC", trade.quantity(), now));
            }
        }

        return new SettledTradeEvent(
                settlementId, trade.tradeId(), trade.symbol(), trade.buyerId(), trade.sellerId(),
                trade.price(), trade.quantity(), notionalUsd, feeUsd,
                "SETTLED", null,
                latencyMicros, threadName, now
        );
    }

    public Map<String, Map<String, Double>> getAccountBalances() {
        Map<String, Map<String, Double>> result = new LinkedHashMap<>();
        for (Map.Entry<String, Account> entry : accounts.entrySet()) {
            result.put(entry.getKey(), Map.of(
                    "USD", entry.getValue().getUsdBalance(),
                    "BTC", entry.getValue().getBtcBalance()
            ));
        }
        return result;
    }

    public List<JournalEntry> getRecentJournal(int limit) {
        synchronized (journal) {
            int size = journal.size();
            int from = Math.max(0, size - limit);
            return new ArrayList<>(journal.subList(from, size));
        }
    }

    public int getJournalSize() {
        return journal.size();
    }
}
