package com.app.folioman.portfolio.domain;

import java.util.UUID;
import org.jobrunr.jobs.annotations.Recurring;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@ConditionalOnProperty(prefix = "jobrunr.job-scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CasPostUploadQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasPostUploadQueue.class);

    private final JdbcTemplate jdbcTemplate;
    private final JobScheduler scheduler;

    CasPostUploadQueue(JdbcTemplate jdbcTemplate, JobScheduler scheduler) {
        this.jdbcTemplate = jdbcTemplate;
        this.scheduler = scheduler;
    }

    void request(Long casId) {
        UUID jobId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO portfolio.cas_post_upload_request (cas_id, job_id)
                VALUES (?, ?)
                ON CONFLICT (cas_id) DO UPDATE SET job_id = EXCLUDED.job_id
                """, casId, jobId);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    enqueue(casId, jobId);
                } catch (RuntimeException e) {
                    LOGGER.warn("Could not queue post-upload processing for CAS {}; will retry", casId, e);
                }
                // The recurring job acknowledges the request using its own database connection.
            }
        });
    }

    @Recurring(id = "dispatch-pending-cas-post-upload", interval = "PT1M")
    public void dispatchPending() {
        var requests = jdbcTemplate.query(
                """
                SELECT cas_id, job_id FROM portfolio.cas_post_upload_request
                """, (rs, row) -> new Request(rs.getLong("cas_id"), rs.getObject("job_id", UUID.class)));
        for (Request request : requests) {
            try {
                enqueue(request.casId(), request.jobId());
                jdbcTemplate.update("""
                        DELETE FROM portfolio.cas_post_upload_request WHERE cas_id = ? AND job_id = ?
                        """, request.casId(), request.jobId());
            } catch (RuntimeException e) {
                LOGGER.warn("Could not dispatch post-upload processing for CAS {}", request.casId(), e);
            }
        }
    }

    private void enqueue(Long casId, UUID jobId) {
        scheduler.<PortfolioPostUploadService>enqueue(jobId, service -> service.processCasPostUpload(casId));
    }

    private record Request(Long casId, UUID jobId) {}
}
