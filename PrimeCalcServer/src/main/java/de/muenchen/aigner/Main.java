package de.muenchen.aigner;

import de.muenchen.aigner.adapters.db.InMemoryPrimeRepository;
import de.muenchen.aigner.domain.service.PrimeService;
import de.muenchen.aigner.ports.PrimeRepository;

import java.math.BigInteger;

public class Main {
    public static void main(String[] args) {
        System.out.println("BitSieve Server startet...");

        // 1. Adapter wählen (später tauschst du das gegen PostgresAdapter)
        PrimeRepository repository = new InMemoryPrimeRepository();

        // 2. Service mit Adapter initialisieren
        PrimeService primeService = new PrimeService(repository);

        // 3. Test-Abfragen
        check(primeService, "7");
        check(primeService, "10");
        check(primeService, "1756983"); // Das wird das Sieve triggern!
    }

    private static void check(PrimeService service, String num) {
        BigInteger n = new BigInteger(num);
        boolean result = service.isPrime(n);
        System.out.println("Ist " + n + " eine Primzahl? " + (result ? "JA" : "NEIN"));
    }
}