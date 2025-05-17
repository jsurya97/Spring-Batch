package com.jk.spring_batch.config;

import com.jk.spring_batch.listener.HwJobExecutionListener;
import com.jk.spring_batch.listener.HwStepExecutionListener;
import com.jk.spring_batch.model.Product;
import com.jk.spring_batch.processor.InMemProcessor;
import com.jk.spring_batch.reader.InMemoryReader;
import com.jk.spring_batch.writer.InMemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

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
//                .reader(flatFileItemReader(null))
                .reader(flatFixedFileItemReader(null))
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
                .incrementer(new RunIdIncrementer())
                .listener(hwJobExecutionListener)
                .start(step1())
                .next(step2())
                .build();
    }


    @Bean
    public ItemReader reader(){
        return new InMemoryReader();
    }

    @StepScope
    @Bean
    public FlatFileItemReader flatFileItemReader(@Value("#{jobParameters['fileInput']}") String inputFile){

        FlatFileItemReader reader = new FlatFileItemReader();
        // step 1 let reader known where is the file
        reader.setResource(new FileSystemResource(inputFile));

        //create the line mapper
        reader.setLineMapper(
                new DefaultLineMapper<Product>(){
                    {
                        setLineTokenizer( new DelimitedLineTokenizer(){
                            {
                                setNames(new String[]{"productID","productName","ProductDesc","price","unit"});

                            }
                        });
                        setFieldSetMapper(new BeanWrapperFieldSetMapper(){
                            {
                                setTargetType(Product.class);
                            }
                        });
                    }
                }
        );

        // step 3 reader to skip the header
        reader.setLinesToSkip(1);
        return reader;
    }


    //Red fixed file (txt)
    @StepScope
    @Bean
    public FlatFileItemReader flatFixedFileItemReader(@Value("#{jobParameters['fileInput']}") String inputFile){

        FlatFileItemReader reader = new FlatFileItemReader();
        // step 1 let reader known where is the file
        reader.setResource(new FileSystemResource(inputFile));

        //create the line mapper
        reader.setLineMapper(
                new DefaultLineMapper<Product>(){
                    {
                        setLineTokenizer( new FixedLengthTokenizer(){
                            {
                                setNames(new String[]{"productID","productName","ProductDesc","price","unit"});
                                setColumns(
                                        new Range(1,16),
                                        new Range(17,41),
                                        new Range(42,65),
                                        new Range(66,73),
                                        new Range(74,80)
                                );
                            }
                        });
                        setFieldSetMapper(new BeanWrapperFieldSetMapper(){
                            {
                                setTargetType(Product.class);
                            }
                        });
                    }
                }
        );

        // step 3 reader to skip the header
        reader.setLinesToSkip(1);
        return reader;
    }


}
