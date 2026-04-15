package de.muenchen.aigner.domain.service;

import de.muenchen.aigner.domain.model.PrimeBlock;
import de.muenchen.aigner.ports.PrimeRepository;

import java.math.BigInteger;
import java.util.List;

public class PrimeService {
    private final PrimeRepository repository;
    private final BigInteger blockSize = BigInteger.valueOf(10_000);

    public PrimeService(PrimeRepository repository) {
        this.repository = repository;
    }
    public boolean isPrime(BigInteger number) {
        BigInteger maxCalculated = repository.getMaxCalculatedNumber();

        if(number.compareTo(maxCalculated) > 0) {
            calculateUpTo(number);
        }

        BigInteger blockIndex = number.divide(blockSize);
        return repository.getBlock(blockIndex)
                .map(block->block.getBitSet().get(number.remainder(blockSize).intValue()))
                .orElse(false);
    }

    private void calculateUpTo(BigInteger number) {
        BigInteger currentMax = repository.getMaxCalculatedNumber();

        while(currentMax.compareTo(number) < 0) {
            BigInteger nextStart = currentMax.equals(BigInteger.ZERO) ? BigInteger.ZERO : currentMax.add(BigInteger.ONE);
            PrimeBlock block = new PrimeBlock(nextStart);

            applySieve(block);

            repository.saveBlock(block);
            currentMax = block.getEndBlock();
        }

    }

    private void applySieve(PrimeBlock block) {
        block.getBitSet().set(0, PrimeBlock.BLOCK_SIZE);

        if(block.getStartBlock().equals(BigInteger.ZERO)) {
            block.getBitSet().clear(0);
            block.getBitSet().clear(1);


            for (int p = 2; p * p < PrimeBlock.BLOCK_SIZE; p++) {

                if (block.getBitSet().get(p)) {
                    for (int q = p * p; q < PrimeBlock.BLOCK_SIZE; q += p) {
                        block.getBitSet().clear(q);
                    }
                }
            }
        }else{
            BigInteger limit = block.getEndBlock().sqrt();
            List<BigInteger> basePrime = repository.getPrimesUpTo(limit);

            for(BigInteger p : basePrime) {
                BigInteger firstMultiple = block.getStartBlock()
                        .add(p).subtract(BigInteger.ONE)
                                .divide(p).multiply(p);
                if(firstMultiple.equals(p)){
                    firstMultiple = firstMultiple.add(p);
                }
                for (BigInteger j = firstMultiple; j.compareTo(block.getEndBlock()) <= 0; j = j.add(p)) {

                    int indexInBitSet = j.subtract(block.getStartBlock()).intValue();
                    block.getBitSet().clear(indexInBitSet);
                }
            }
        }
    }
}
