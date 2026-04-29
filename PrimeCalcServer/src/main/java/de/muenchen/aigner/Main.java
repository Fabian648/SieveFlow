package de.muenchen.aigner;

import de.muenchen.aigner.adapters.db.InMemoryPrimeRepository;
import de.muenchen.aigner.adapters.db.PostgresAdapter;
import de.muenchen.aigner.adapters.network.TCPServerAdapter;
import de.muenchen.aigner.domain.service.PrimeService;
import de.muenchen.aigner.ports.PrimeRepository;

import java.math.BigInteger;

public class Main {
    public static void main(String[] args) {
        System.out.println("BitSieve Server startet...");

        // 1. Adapter wählen (später tauschst du das gegen PostgresAdapter)
        //PrimeRepository repository = new InMemoryPrimeRepository();
        PostgresAdapter repository = new PostgresAdapter();

        // 2. Service mit Adapter initialisieren
        PrimeService primeService = new PrimeService(repository);

        TCPServerAdapter server = new  TCPServerAdapter(primeService);

        server.start();

    }

    private static void check(PrimeService service, String num) {
        BigInteger n = new BigInteger(num);
        boolean result = service.isPrime(n);
        System.out.println("Ist " + n + " eine Primzahl? " + (result ? "JA" : "NEIN"));
    }
}