package de.muenchen.aigner.adapters.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import de.muenchen.aigner.domain.model.PrimeBlock;
import de.muenchen.aigner.ports.PrimeRepository;

import javax.sql.DataSource;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;

public class PostgresAdapter implements PrimeRepository {

    private final DataSource dataSource;

    public PostgresAdapter() {
        // 1. Konfiguration aus Datei laden
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if(is == null) {
                props.load(is);
            }
        } catch (Exception e) {
           // throw new RuntimeException("Konnte application.properties nicht laden", e);
        }
        String dbUrl = System.getenv("DB_URL");
        if(dbUrl == null || dbUrl.isEmpty()) {
            dbUrl = props.getProperty("db.url", "jdbc:postgresql://localhost:5432/bitsieve");
        }

        String dbUser = props.getProperty("db.user", "user");
        String dbPass = props.getProperty("db.password", "password");

        // 2. Hikari Connection Pool konfigurieren
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUser);
        config.setPassword(dbPass);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(10);

        // Optimierungen für Postgres
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");

        this.dataSource = new HikariDataSource(config);

        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = dataSource.getConnection();
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
            throw new RuntimeException("DB Init fehlgeschlagen", e);
        }
    }

    @Override
    public void saveBlock(PrimeBlock block) {
        String sql = "INSERT INTO prime_blocks (block_index, start_number, end_number, bits) VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = dataSource.getConnection(); // Holt Verbindung aus dem Pool
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, block.getStartBlock().divide(BigInteger.valueOf(10000)).longValue());
            pstmt.setBigDecimal(2, new java.math.BigDecimal(block.getStartBlock()));
            pstmt.setBigDecimal(3, new java.math.BigDecimal(block.getEndBlock()));
            pstmt.setBytes(4, block.getBitSet().toByteArray());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<PrimeBlock> getBlock(BigInteger blockIndex) {
        String sql = "SELECT start_number, bits FROM prime_blocks WHERE block_index = ?";

        // Geändert: dataSource.getConnection() statt DriverManager
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, blockIndex.longValue());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BigInteger start = rs.getBigDecimal("start_number").toBigInteger();
                    byte[] bytes = rs.getBytes("bits");

                    PrimeBlock block = new PrimeBlock(start);
                    block.setBitSet(BitSet.valueOf(bytes));
                    return Optional.of(block);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public BigInteger getMaxCalculatedNumber() {
        String sql = "SELECT MAX(end_number) FROM prime_blocks";
        try (Connection conn = dataSource.getConnection();
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
        String sql = "SELECT start_number, bits FROM prime_blocks WHERE start_number <= ? ORDER BY block_index";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, new java.math.BigDecimal(limit));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BigInteger start = rs.getBigDecimal("start_number").toBigInteger();
                    BitSet bits = BitSet.valueOf(rs.getBytes("bits"));

                    for (int i = 0; i < PrimeBlock.BLOCK_SIZE; i++) {
                        if (bits.get(i)) {
                            BigInteger p = start.add(BigInteger.valueOf(i));
                            if (p.compareTo(limit) <= 0) {
                                primes.add(p);
                            }
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