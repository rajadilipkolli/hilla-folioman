package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;
import org.jobrunr.jobs.lambdas.IocJobLambda;
import org.jobrunr.scheduling.JobScheduler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class CasPostUploadQueueTest {

    @Test
    void retriesCommittedRequestWithSameJobIdAfterEnqueueFailure() throws Exception {
        var jdbcTemplate = mock(JdbcTemplate.class);
        var scheduler = mock(JobScheduler.class);
        var queue = new CasPostUploadQueue(jdbcTemplate, scheduler);
        var jobId = ArgumentCaptor.forClass(UUID.class);
        doThrow(new IllegalStateException("Job storage unavailable"))
                .doReturn(null)
                .when(scheduler)
                .enqueue(any(UUID.class), ArgumentMatchers.<IocJobLambda<PortfolioPostUploadService>>any());

        TransactionSynchronizationManager.initSynchronization();
        try {
            queue.request(12L);
            verify(jdbcTemplate).update(anyString(), eq(12L), jobId.capture());
            verifyNoInteractions(scheduler);
            TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        assertThat(Mockito.mockingDetails(jdbcTemplate).getInvocations()).hasSize(1);
        var row = mock(ResultSet.class);
        when(row.getLong("cas_id")).thenReturn(12L);
        when(row.getObject("job_id", UUID.class)).thenReturn(jobId.getValue());
        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<Object>>any()))
                .thenAnswer(invocation ->
                        List.of(invocation.<RowMapper<Object>>getArgument(1).mapRow(row, 0)));

        queue.dispatchPending();

        verify(scheduler, Mockito.times(2))
                .enqueue(eq(jobId.getValue()), ArgumentMatchers.<IocJobLambda<PortfolioPostUploadService>>any());
        verify(jdbcTemplate)
                .update(
                        ArgumentMatchers.startsWith("DELETE FROM portfolio.cas_post_upload_request"),
                        eq(12L),
                        eq(jobId.getValue()));
    }
}
