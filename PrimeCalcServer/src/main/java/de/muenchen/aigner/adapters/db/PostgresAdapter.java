package de.muenchen.aigner.adapters.db;

import de.muenchen.aigner.domain.model.PrimeBlock;
import de.muenchen.aigner.ports.PrimeRepository;

import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;

public class PostgresAdapter implements PrimeRepository {

    private final String url = "jdbc:postgresql://localhost:5432/bitsieve";
    private final String user = "user";
    private final String password = "password";

    public PostgresAdapter() {
        // Initialisiere die Tabelle, falls sie nicht existiert
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS prime_blocks (
                    block_index BIGINT PRIMARY KEY,
                    start_number NUMERIC NOT NULL,
                    end_number NUMERIC NOT NULL,
                    bits BYTEA NOT NULL
                )
            """);
        } catch (SQLException e) {
            throw new RuntimeException("DB Initialization failed", e);
        }
    }

    @Override
    public void saveBlock(PrimeBlock block) {
        String sql = "INSERT INTO prime_blocks (block_index, start_number, end_number, bits) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (block_index) DO NOTHING";

        BigInteger index = block.getStartBlock().divide(BigInteger.valueOf(10000));

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, index.longValue());
            pstmt.setBigDecimal(2, new java.math.BigDecimal(block.getStartBlock()));
            pstmt.setBigDecimal(3, new java.math.BigDecimal(block.getEndBlock()));
            pstmt.setBytes(4, block.getBitSet().toByteArray()); // Hier ist der Zauber!

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<PrimeBlock> getBlock(BigInteger blockIndex) {
        String sql = "SELECT start_number, bits FROM prime_blocks WHERE block_index = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, blockIndex.longValue());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                BigInteger start = rs.getBigDecimal("start_number").toBigInteger();
                byte[] bytes = rs.getBytes("bits");

                PrimeBlock block = new PrimeBlock(start);
                block.setBitSet(BitSet.valueOf(bytes));
                return Optional.of(block);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public BigInteger getMaxCalculatedNumber() {
        String sql = "SELECT MAX(end_number) FROM prime_blocks";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next() && rs.getBigDecimal(1) != null) {
                return rs.getBigDecimal(1).toBigInteger();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigInteger.ZERO;
    }

    @Override
    public List<BigInteger> getPrimesUpTo(BigInteger limit) {
        List<BigInteger> primes = new ArrayList<>();
        // Wir laden alle Blöcke, die Primzahlen unter dem Limit enthalten könnten
        String sql = "SELECT start_number, bits FROM prime_blocks WHERE start_number <= ? ORDER BY block_index";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, new java.math.BigDecimal(limit));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                BigInteger start = rs.getBigDecimal("start_number").toBigInteger();
                BitSet bits = BitSet.valueOf(rs.getBytes("bits"));

                for (int i = 0; i < 10000; i++) {
                    if (bits.get(i)) {
                        BigInteger p = start.add(BigInteger.valueOf(i));
                        if (p.compareTo(limit) <= 0) {
                            primes.add(p);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return primes;
    }
}