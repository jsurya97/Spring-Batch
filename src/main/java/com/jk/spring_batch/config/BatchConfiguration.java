package com.jk.spring_batch.config;

import com.jk.spring_batch.listener.HwJobExecutionListener;
import com.jk.spring_batch.listener.HwStepExecutionListener;
import com.jk.spring_batch.processor.InMemProcessor;
import com.jk.spring_batch.reader.InMemoryReader;
import com.jk.spring_batch.writer.InMemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Configuration
public class BatchConfiguration {

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Autowired
    private HwJobExecutionListener hwJobExecutionListener;

    @Autowired
    private HwStepExecutionListener hwStepExecutionListener;

    @Autowired
    InMemProcessor inMemProcessor;

    @Bean
    public Step step1() {
        return steps.get("step1")
                .listener(hwStepExecutionListener)
                .tasklet(helloWorldTakslet())
                .build();
    }

    @Bean
    public Step step2() {
        return steps.get("step2").<Integer, Integer>chunk(3)
                .reader(reader())
                .processor(inMemProcessor)
                .writer(new InMemWriter())
                .build();
    }

    private Tasklet helloWorldTakslet() {
        return (new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Hellowworld");
                return RepeatStatus.FINISHED;
            }
        });
    }

    @Bean
    public Job helowworldJob(){
        return jobs.get("hellowwordjob")
                .listener(hwJobExecutionListener)
                .start(step1())
                .next(step2())
                .build();
    }


    @Bean
    public ItemReader reader(){
        return new InMemoryReader();
    }

    @Bean
    public ItemWriter writer(){
        return new InMemWriter();
    }


}
