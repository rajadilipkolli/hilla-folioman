package com.app.folioman.config;

import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.filters.RetryFilter;

public class SchemeSyncRetryFilter extends RetryFilter {

    @Override
    protected long getSecondsToAdd(Job job) {
        if ("Update MF Schemes".equals(job.getJobName())) {
            return 3600L;
        }
        return super.getSecondsToAdd(job);
    }
}
