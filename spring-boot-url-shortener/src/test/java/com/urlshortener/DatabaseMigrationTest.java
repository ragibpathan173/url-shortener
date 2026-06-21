package com.urlshortener;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseMigrationTest {

    @Test
    void createsTheSchemaWithoutSampleAccounts() throws Exception {
        String databaseUrl = "jdbc:h2:mem:flyway-migration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Flyway flyway = Flyway.configure()
                .dataSource(databaseUrl, "sa", "")
                .locations("classpath:db/migration")
                .load();

        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(1);

        try (var connection = DriverManager.getConnection(databaseUrl, "sa", "");
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery("select count(*) from users")) {
            resultSet.next();
            assertThat(resultSet.getInt(1)).isZero();
        }
    }
}
