package de.muenchen.aigner.domain.model;

import java.util.BitSet;
import java.math.BigInteger;

public class PrimeBlock {

    public static final int BLOCK_SIZE = 10_000;

    private BitSet bitSet;
    private BigInteger start_block;
    private BigInteger end_block;

    public PrimeBlock(BigInteger start_at) {
        this.bitSet = new BitSet(BLOCK_SIZE);
        this.start_block = start_at;
        this.end_block = start_at.add(BigInteger.valueOf(BLOCK_SIZE-1));
    }

    public BitSet getBitSet() {
        return bitSet;
    }

    public BigInteger getStartBlock() {
        return start_block;
    }

    public BigInteger getEndBlock() {
        return end_block;
    }

    public String getString() {
        return "Start: %s, Ende: %s, Gesetzte Bits: %d"
                .formatted(start_block, end_block, bitSet.cardinality());
    }

}
