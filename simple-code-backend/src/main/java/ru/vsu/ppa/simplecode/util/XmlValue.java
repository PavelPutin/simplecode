package ru.vsu.ppa.simplecode.util;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class XmlValue<T> {

    private static final XmlValue<?> EMPTY = new XmlValue<>(null, null, null);

    private final T value;
    private final String nodeName;
    private final String attributeName;

    public static <T> XmlValue<T> empty() {
        @SuppressWarnings("unchecked")
        XmlValue<T> t = (XmlValue<T>) EMPTY;
        return t;
    }

    private <U> XmlValue<U> emptyChain() {
        return new XmlValue<>(null, nodeName, attributeName);
    }

    private XmlValue(T value, String nodeName, String attributeName) {
        this.value = value;
        this.nodeName = nodeName;
        this.attributeName = attributeName;
    }

    private static <T> XmlValue<T> of(T value, String nodeName, String attributeName) {
        return ofAttribute(value, nodeName, attributeName);
    }

    public static <T> XmlValue<T> ofNode(T value, String nodeName) {
        return value == null ? new XmlValue<>(null, nodeName, null)
                             : new XmlValue<>(value, nodeName, null);
    }

    public static <T> XmlValue<T> ofAttribute(T value, String nodeName, String attributeName) {
        return value == null ? new XmlValue<>(null, nodeName, attributeName)
                             : new XmlValue<>(value, nodeName, attributeName);
    }

    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }

    public boolean isEmpty() {
        return value == null;
    }

    public XmlValue<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (isEmpty()) {
            return this;
        } else {
            return predicate.test(value) ? this : emptyChain();
        }
    }

    public <U> XmlValue<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (isEmpty()) {
            return emptyChain();
        } else {
            return XmlValue.of(mapper.apply(value), nodeName, attributeName);
        }
    }

    public <U> XmlValue<U> flatMap(Function<? super T, ? extends XmlValue<? extends U>> mapper) {
        Objects.requireNonNull(mapper);
        if (isEmpty()) {
            return emptyChain();
        } else {
            @SuppressWarnings("unchecked")
            XmlValue<U> r = (XmlValue<U>) mapper.apply(value);
            return Objects.requireNonNull(r);
        }
    }

    public XmlValue<T> or(Supplier<? extends XmlValue<? extends T>> supplier) {
        Objects.requireNonNull(supplier);
        if (isPresent()) {
            return this;
        } else {
            @SuppressWarnings("unchecked")
            XmlValue<T> r = (XmlValue<T>) supplier.get();
            return Objects.requireNonNull(r);
        }
    }

    public Stream<T> stream() {
        if (isEmpty()) {
            return Stream.empty();
        } else {
            return Stream.of(value);
        }
    }

    public T orElse(T other) {
        return value != null ? value : other;
    }

    public T orElseGet(Supplier<? extends T> supplier) {
        return value != null ? value : supplier.get();
    }

    public T orElseThrow() {
        if (value == null) {
            if (attributeName == null) {
                throw PolygonProblemXMLIncomplete.tagNotFound(nodeName);
            } else {
                throw PolygonProblemXMLIncomplete.tagWithAttributeNotFound(nodeName, attributeName);
            }
        }
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        XmlValue<?> xmlValue = (XmlValue<?>) o;
        return Objects.equals(value, xmlValue.value) && Objects.equals(nodeName, xmlValue.nodeName)
                && Objects.equals(attributeName, xmlValue.attributeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, nodeName, attributeName);
    }

    @Override
    public String toString() {
        return value != null
               ? ("XmlValue[" + value + "]")
               : "XmlValue.empty";
    }

}
