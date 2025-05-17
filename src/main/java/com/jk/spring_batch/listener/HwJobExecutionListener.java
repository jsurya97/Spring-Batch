package com.jk.spring_batch.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class HwJobExecutionListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("BEFORE jobExecution.getJobInstance().getJobName() "+jobExecution.getJobInstance().getJobName());
        System.out.println("BEFORE jobExecution.getExecutionContext().toString() "+jobExecution.getExecutionContext().toString());
        jobExecution.getExecutionContext().put("name","jayasurya");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println("AFTER jobExecution.getExecutionContext().toString() "+jobExecution.getExecutionContext().toString());

    }
}
