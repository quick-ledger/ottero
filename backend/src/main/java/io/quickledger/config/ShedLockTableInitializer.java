package io.quickledger.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class ShedLockTableInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ShedLockTableInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public ShedLockTableInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void createShedLockTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS shedlock (
                name VARCHAR(64) NOT NULL,
                lock_until TIMESTAMP(3) NOT NULL,
                locked_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                locked_by VARCHAR(255) NOT NULL,
                PRIMARY KEY (name)
            )
            """;

        try {
            jdbcTemplate.execute(sql);
            logger.info("ShedLock table verified/created successfully");
        } catch (Exception e) {
            logger.error("Failed to create shedlock table: {}", e.getMessage());
        }
    }
}
