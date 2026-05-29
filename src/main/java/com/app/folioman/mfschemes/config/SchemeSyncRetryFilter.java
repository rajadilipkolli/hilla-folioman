package com.app.folioman.mfschemes.config;

import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.filters.RetryFilter;

public class SchemeSyncRetryFilter extends RetryFilter {

    @Override
    protected long getSecondsToAdd(Job job) {
        if ("update-mf-schemes".equals(job.getJobName())) {
            return 3600L;
        }
        return super.getSecondsToAdd(job);
    }
}
