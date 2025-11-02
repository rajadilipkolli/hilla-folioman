package com.app.folioman.portfolio.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for portfolio cache settings.
 *
 * Example configuration:
 * ```
 * app.portfolio.cache:
 *   eviction:
 *     batch-size: 200
 *     transaction-cron: "0 45 18 * * *"
 * ```
 */
@Validated
@ConfigurationProperties(prefix = "app.portfolio.cache")
public class PortfolioCacheProperties {

    /**
     * Configuration properties for cache eviction.
     */
    @Valid
    private Eviction eviction = new Eviction();

    public Eviction getEviction() {
        return eviction;
    }

    public void setEviction(Eviction eviction) {
        this.eviction = eviction;
    }

    public static class Eviction {
        /**
         * Batch size for cache eviction operations.
         * Default is 100 entries per batch to prevent Redis blocking for too long.
         */
        private int batchSize = 100;

        /**
         * Cron expression for transaction cache eviction job.
         * Default is 6:45 PM UTC daily (00:15 IST).
         */
        @NotBlank(message = "transactionCron cant be blank")
        private String transactionCron = "0 45 18 * * *";

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public String getTransactionCron() {
            return transactionCron;
        }

        public void setTransactionCron(String transactionCron) {
            this.transactionCron = transactionCron;
        }
    }
}
