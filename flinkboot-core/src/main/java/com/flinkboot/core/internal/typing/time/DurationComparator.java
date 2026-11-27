package com.flinkboot.core.internal.typing.time;

import org.apache.flink.api.common.typeutils.TypeComparator;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;
import org.apache.flink.core.memory.MemorySegment;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

/**
 * Internal Flink {@link TypeComparator} for {@link Duration}, aligned with
 * {@link com.flinkboot.core.api.typing.time.DurationSerializer}'s nullable binary layout.
 */
public final class DurationComparator extends TypeComparator<Duration> {

    private static final long serialVersionUID = 1L;

    private static final int NORMALIZED_KEY_LEN = 1 + Long.BYTES + Integer.BYTES;

    private final boolean ascendingComparison;

    private final TypeComparator<?>[] comparators = new TypeComparator<?>[]{this};

    private transient Duration reference;
    /**
     * Creates a new {@link DurationComparator}.
     *
     * @param ascending {@code true} for ascending order, {@code false} for descending order
     */
    public DurationComparator(boolean ascending) {
        this.ascendingComparison = ascending;
    }

    @Override
    public int hash(Duration record) {
        return Objects.hashCode(record);
    }

    @Override
    public void setReference(Duration toCompare) {
        this.reference = toCompare;
    }

    @Override
    public boolean equalToReference(Duration candidate) {
        return Objects.equals(candidate, this.reference);
    }

    @Override
    public int compareToReference(TypeComparator<Duration> referencedComparator) {
        var otherRef = ((DurationComparator) referencedComparator).reference;
        return compare(otherRef, this.reference);
    }

    @Override
    public int compare(Duration first, Duration second) {
        if (first == null && second == null) {
            return 0;
        }
        int cmp;
        if (first == null) {
            cmp = -1;
        } else if (second == null) {
            cmp = 1;
        } else {
            cmp = first.compareTo(second);
        }
        return ascendingComparison ? cmp : -cmp;
    }

    @Override
    public int compareSerialized(DataInputView firstSource, DataInputView secondSource) throws IOException {
        boolean firstNull = firstSource.readBoolean();
        boolean secondNull = secondSource.readBoolean();
        if (firstNull && secondNull) {
            return 0;
        }
        int cmp;
        if (firstNull) {
            cmp = -1;
        } else if (secondNull) {
            cmp = 1;
        } else {
            long firstSeconds = firstSource.readLong();
            long secondSeconds = secondSource.readLong();
            cmp = Long.compare(firstSeconds, secondSeconds);
            if (cmp == 0) {
                int firstNanos = firstSource.readInt();
                int secondNanos = secondSource.readInt();
                cmp = Integer.compare(firstNanos, secondNanos);
            }
        }
        return ascendingComparison ? cmp : -cmp;
    }

    @Override
    public boolean supportsNormalizedKey() {
        return true;
    }

    @Override
    public boolean supportsSerializationWithKeyNormalization() {
        return false;
    }

    @Override
    public int getNormalizeKeyLen() {
        return NORMALIZED_KEY_LEN;
    }

    @Override
    public boolean isNormalizedKeyPrefixOnly(int keyBytes) {
        return keyBytes < NORMALIZED_KEY_LEN;
    }

    private static void putPrefixBigEndian(MemorySegment target, int offset, long value, int totalBytes, int len) {
        for (int i = 0; i < len; i++) {
            target.put(offset + i, (byte) (value >>> ((totalBytes - 1 - i) << 3)));
        }
    }

    private static void fillZero(MemorySegment target, int offset, int len) {
        for (int i = 0; i < len; i++) {
            target.put(offset + i, (byte) 0);
        }
    }

    @Override
    public void putNormalizedKey(Duration record, MemorySegment target, int offset, int numBytes) {
        if (numBytes <= 0) {
            return;
        }
        if (record == null) {
            fillZero(target, offset, numBytes);
            return;
        }
        target.put(offset, (byte) 1);
        int pos = offset + 1;
        int remaining = numBytes - 1;

        long unsignedSeconds = record.getSeconds() ^ Long.MIN_VALUE;
        if (remaining < Long.BYTES) {
            putPrefixBigEndian(target, pos, unsignedSeconds, Long.BYTES, remaining);
            return;
        }
        target.putLongBigEndian(pos, unsignedSeconds);
        pos += Long.BYTES;
        remaining -= Long.BYTES;

        int nanos = record.getNano();
        if (remaining < Integer.BYTES) {
            putPrefixBigEndian(target, pos, nanos, Integer.BYTES, remaining);
            return;
        }
        target.putIntBigEndian(pos, nanos);
        fillZero(target, pos + Integer.BYTES, remaining - Integer.BYTES);
    }

    @Override
    public void writeWithKeyNormalization(Duration record, DataOutputView target) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Duration readWithKeyDenormalization(Duration reuse, DataInputView source) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean invertNormalizedKey() {
        return !ascendingComparison;
    }

    @Override
    public TypeComparator<Duration> duplicate() {
        return new DurationComparator(ascendingComparison);
    }

    @Override
    public int extractKeys(Object record, Object[] target, int index) {
        target[index] = record;
        return 1;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public TypeComparator[] getFlatComparators() {
        return comparators;
    }

}
