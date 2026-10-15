package dev.ferrox.core.result;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Functional Result (Either) container for type-safe error handling without throwing exceptions.
 * Implemented using Java 21 Sealed Interfaces and Records.
 *
 * @param <T> Success value type (Right)
 * @param <E> Error value type (Left)
 */
public sealed interface Result<T, E> permits Result.Success, Result.Failure {

    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    boolean isSuccess();

    default boolean isFailure() {
        return !isSuccess();
    }

    T getOrNull();

    E getErrorOrNull();

    default T getOrElse(T defaultValue) {
        return isSuccess() ? getOrNull() : defaultValue;
    }

    default Optional<T> toOptional() {
        return isSuccess() ? Optional.ofNullable(getOrNull()) : Optional.empty();
    }

    <U> Result<U, E> map(Function<? super T, ? extends U> mapper);

    <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> mapper);

    default Result<T, E> onSuccess(Consumer<? super T> action) {
        if (isSuccess()) {
            action.accept(getOrNull());
        }
        return this;
    }

    default Result<T, E> onFailure(Consumer<? super E> action) {
        if (isFailure()) {
            action.accept(getErrorOrNull());
        }
        return this;
    }

    record Success<T, E>(T value) implements Result<T, E> {
        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public T getOrNull() {
            return value;
        }

        @Override
        public E getErrorOrNull() {
            return null;
        }

        @Override
        public <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
            Objects.requireNonNull(mapper, "mapper must not be null");
            return Result.success(mapper.apply(value));
        }

        @Override
        public <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> mapper) {
            Objects.requireNonNull(mapper, "mapper must not be null");
            return mapper.apply(value);
        }
    }

    record Failure<T, E>(E error) implements Result<T, E> {
        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public T getOrNull() {
            return null;
        }

        @Override
        public E getErrorOrNull() {
            return error;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
            return (Result<U, E>) this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> mapper) {
            return (Result<U, E>) this;
        }
    }
}
