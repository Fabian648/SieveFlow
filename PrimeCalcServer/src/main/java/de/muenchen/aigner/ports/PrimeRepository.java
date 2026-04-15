package de.muenchen.aigner.ports;

import de.muenchen.aigner.domain.model.PrimeBlock;

import java.math.BigInteger;
import java.util.Optional;

public interface PrimeRepository {
    void saveBlock(PrimeBlock block);
    Optional<PrimeBlock> getBlock(BigInteger blockIndex);
    BigInteger getMaxCalculatedNumber();

}
