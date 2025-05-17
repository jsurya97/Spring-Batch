package com.jk.spring_batch.listener;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class HwStepExecutionListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("BEFORE HwStepExecutionListener"+stepExecution.getJobExecution().getExecutionContext());

        System.out.println("BEFORE HwStepExecutionListener getJobParameters "+stepExecution.getJobExecution().getJobParameters());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("After HwStepExecutionListener"+stepExecution.getJobExecution().getExecutionContext());
        return null;
    }
}
