package com.flinkboot.core.internal.time;

import org.apache.flink.api.common.typeutils.TypeComparator;
import org.apache.flink.api.common.typeutils.base.BasicTypeComparator;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.MemorySegment;

import java.io.IOException;
import java.time.Duration;

/**
 * Internal Flink {@link TypeComparator} for {@link Duration}, aligned with
 * {@link com.flinkboot.core.api.typing.time.DurationSerializer}'s nullable binary layout.
 */
public final class DurationComparator extends BasicTypeComparator<Duration> {

    private static final long serialVersionUID = 1L;
    private static final int NORMALIZED_KEY_LEN = 1 + Long.BYTES + Integer.BYTES;

    /**
     * Creates a new {@link DurationComparator}.
     *
     * @param ascending {@code true} for ascending order, {@code false} for descending order
     */
    public DurationComparator(boolean ascending) {
        super(ascending);
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
    public int getNormalizeKeyLen() {
        return NORMALIZED_KEY_LEN;
    }

    @Override
    public boolean isNormalizedKeyPrefixOnly(int keyBytes) {
        return keyBytes < NORMALIZED_KEY_LEN;
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
        if (remaining >= Long.BYTES) {
            target.putLongBigEndian(pos, unsignedSeconds);
            pos += Long.BYTES;
            remaining -= Long.BYTES;

            int nanos = record.getNano();
            if (remaining >= Integer.BYTES) {
                target.putIntBigEndian(pos, nanos);
                pos += Integer.BYTES;
                remaining -= Integer.BYTES;
                fillZero(target, pos, remaining);
            } else {
                putIntPrefixBigEndian(target, pos, nanos, remaining);
            }
        } else {
            putLongPrefixBigEndian(target, pos, unsignedSeconds, remaining);
        }
    }

    @Override
    public TypeComparator<Duration> duplicate() {
        return new DurationComparator(ascendingComparison);
    }

    private static void putLongPrefixBigEndian(MemorySegment target, int offset, long value, int len) {
        for (int i = 0; i < len; i++) {
            target.put(offset + i, (byte) (value >>> ((Long.BYTES - 1 - i) << 3)));
        }
    }

    private static void putIntPrefixBigEndian(MemorySegment target, int offset, int value, int len) {
        for (int i = 0; i < len; i++) {
            target.put(offset + i, (byte) (value >>> ((Integer.BYTES - 1 - i) << 3)));
        }
    }

    private static void fillZero(MemorySegment target, int offset, int len) {
        for (int i = 0; i < len; i++) {
            target.put(offset + i, (byte) 0);
        }
    }
}
