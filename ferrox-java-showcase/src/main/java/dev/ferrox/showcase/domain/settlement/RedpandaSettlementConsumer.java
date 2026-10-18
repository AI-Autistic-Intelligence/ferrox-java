package dev.ferrox.showcase.domain.settlement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Background consumer running on a Project Loom Virtual Thread.
 * Consumes 'orders.matched.v1' from Redpanda and triggers async settlement.
 */
@Component
public class RedpandaSettlementConsumer {

    private static final Logger log = LoggerFactory.getLogger(RedpandaSettlementConsumer.class);
    private static final String TOPIC_ORDERS_MATCHED = "orders.matched.v1";
    private static final String REDPANDA_BOOTSTRAP = "localhost:19092";

    private final LoomSettlementService settlementService;
    private final ObjectMapper objectMapper;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread consumerThread;

    public RedpandaSettlementConsumer(LoomSettlementService settlementService, ObjectMapper objectMapper) {
        this.settlementService = settlementService;
        this.objectMapper = objectMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        running.set(true);
        // Start listener on a native Project Loom Virtual Thread
        consumerThread = Thread.ofVirtual().name("redpanda-settlement-consumer").start(this::consumeLoop);
        log.info("🚀 Redpanda Settlement Listener active on Virtual Thread (topic: {})", TOPIC_ORDERS_MATCHED);
    }

    private void consumeLoop() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, REDPANDA_BOOTSTRAP);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ferrox-java-settlement-ledger");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "3000");

        while (running.get()) {
            try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
                consumer.subscribe(Collections.singletonList(TOPIC_ORDERS_MATCHED));
                log.info("✅ Connected to Redpanda at {} for topic '{}'", REDPANDA_BOOTSTRAP, TOPIC_ORDERS_MATCHED);

                while (running.get()) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(250));
                    for (ConsumerRecord<String, String> record : records) {
                        handleRecord(record.value());
                    }
                }
            } catch (Exception e) {
                if (running.get()) {
                    log.debug("Redpanda listener awaiting broker at {}: {}", REDPANDA_BOOTSTRAP, e.getMessage());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    public void handleRecord(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode dataNode = root.has("data") ? root.get("data") : root;

            String tradeId = dataNode.has("trade_id") ? dataNode.get("trade_id").asText() : "t-" + System.currentTimeMillis();
            String symbol = dataNode.has("symbol") ? dataNode.get("symbol").asText() : "BTC/USD";
            String buyerId = dataNode.has("buyer_id") ? dataNode.get("buyer_id").asText() : "buyer-alpha";
            String sellerId = dataNode.has("seller_id") ? dataNode.get("seller_id").asText() : "seller-beta";
            double price = dataNode.has("price") ? dataNode.get("price").asDouble() : 67450.0;
            double qty = dataNode.has("quantity") ? dataNode.get("quantity").asDouble() : 0.5;

            MatchedTradeEvent trade = new MatchedTradeEvent(
                    tradeId, symbol, buyerId, sellerId, price, qty, System.currentTimeMillis()
            );

            settlementService.processTradeAsync(trade);
        } catch (Exception e) {
            log.warn("Failed to parse matched trade record: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
    }
}
