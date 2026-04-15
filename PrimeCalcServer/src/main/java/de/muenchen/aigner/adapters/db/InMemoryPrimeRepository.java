package de.muenchen.aigner.adapters.db;

import de.muenchen.aigner.domain.model.PrimeBlock;
import de.muenchen.aigner.ports.PrimeRepository;

import java.math.BigInteger;
import java.util.*;

public class InMemoryPrimeRepository implements PrimeRepository {
    private final Map<BigInteger, PrimeBlock> storage = new HashMap<>();
    private BigInteger maxCalculated = BigInteger.ZERO;

    @Override
    public void saveBlock(PrimeBlock block) {
        // Wir berechnen den Index: Start / 10.000
        BigInteger index = block.getStartBlock().divide(BigInteger.valueOf(PrimeBlock.BLOCK_SIZE));
        storage.put(index, block);

        if (block.getEndBlock().compareTo(maxCalculated) > 0) {
            maxCalculated = block.getEndBlock();
        }
        System.out.println("Speichere Block " + index + " (bis " + maxCalculated + ")");
    }

    @Override
    public Optional<PrimeBlock> getBlock(BigInteger blockIndex) {
        return Optional.ofNullable(storage.get(blockIndex));
    }

    @Override
    public BigInteger getMaxCalculatedNumber() {
        return maxCalculated;
    }

    @Override
    public List<BigInteger> getPrimesUpTo(BigInteger limit) {
        // Diese Methode braucht der Service für das Sieve-Verfahren
        List<BigInteger> primes = new ArrayList<>();
        // Wir gehen durch alle gespeicherten Blöcke und sammeln Primzahlen ein
        for (PrimeBlock block : storage.values()) {
            for (int i = 0; i < PrimeBlock.BLOCK_SIZE; i++) {
                if (block.getBitSet().get(i)) {
                    BigInteger p = block.getStartBlock().add(BigInteger.valueOf(i));
                    if (p.compareTo(limit) <= 0) primes.add(p);
                }
            }
        }
        return primes;
    }
}