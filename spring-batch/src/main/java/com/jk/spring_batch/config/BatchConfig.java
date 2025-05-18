package com.jk.spring_batch.config;

import com.jk.spring_batch.ConsoleTasklet;
import com.jk.spring_batch.model.Product;
import com.jk.spring_batch.processor.ProdutProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.*;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.ResourceAccessException;


import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;

@EnableBatchProcessing
@Configuration
public class BatchConfig {

    @Autowired
    private StepBuilderFactory steps;

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private DataSource dataSource;

    @Bean
    @StepScope
    public FlatFileItemReader flatFileItemReader(
            @Value( "#{jobParameters['fileInput']}"  ) FileSystemResource inputFile
    ){

        FlatFileItemReader reader = new FlatFileItemReader();
        reader.setResource(inputFile);
        reader.setLinesToSkip(1);
        reader.setLineMapper(new DefaultLineMapper(){
            {
                setFieldSetMapper(new BeanWrapperFieldSetMapper(){
                    {
                        setTargetType(Product.class);
                    }
                });

                setLineTokenizer(new DelimitedLineTokenizer(){
                    {
                        setNames(new String[]{"productId","prodName","productDesc"  ,"price","unit"});
                        setDelimiter(",");
                    }
                });
            }
        });

        return reader;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter flatFileItemWriter(
            @Value("#{jobParameters['fileOutput']}" )FileSystemResource outputFile
    ){
        FlatFileItemWriter writer = new FlatFileItemWriter();

        writer.setResource(outputFile);
        writer.setLineAggregator( new DelimitedLineAggregator(){
            {
                setDelimiter("|");
                setFieldExtractor(new BeanWrapperFieldExtractor(){
                    {
                        setNames(new String[]{"productId","prodName","productDesc","price","unit" });
                    }
                });
            }
        });

        // how to write the header
        writer.setHeaderCallback(new FlatFileHeaderCallback() {
            @Override
            public void writeHeader(Writer writer) throws IOException {
                writer.write("productID,productName,ProductDesc,price,unit");
            }
        });

        writer.setAppendAllowed(true);
//        writer.setFooterCallback(new FlatFileFooterCallback() {
//            @Override
//            public void writeFooter(Writer writer) throws IOException {
//                writer.write("This file is created at "+ LocalDate.now().toString());
//            }
//        });

        return writer;
    }


    @Bean
    public JdbcBatchItemWriter dbWriter(){
        JdbcBatchItemWriter writer = new JdbcBatchItemWriter ();
        writer.setDataSource(this.dataSource);
        writer.setSql("insert into products (product_id, prod_name, prod_desc, price, unit )" +
                " values (?, ?, ?, ? , ? ) ");
        writer.setItemPreparedStatementSetter(new ItemPreparedStatementSetter<Product>() {
            @Override
            public void setValues(Product item, PreparedStatement ps) throws SQLException {
                ps.setInt(1,item.getProductId());
                ps.setString(2,item.getProdName());
                ps.setString(3, item.getProductDesc());
                ps.setBigDecimal(4, item.getPrice());
                ps.setInt(5, item.getUnit());
            }
        });

        return writer;

    }

    @Bean
    public JdbcBatchItemWriter jdbcBatchItemWriter2(){
        return new JdbcBatchItemWriterBuilder<Product>()
                .dataSource(dataSource)
                .sql("insert into products (product_id, prod_name, prod_desc, price, unit )" +
                        " values ( :productId, :prodName, :productDesc, :price, :unit ) ")
                .beanMapped()
                .build();
    }

    @Bean
    public Step asyncStep() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.afterPropertiesSet();
        return steps.get("async")
                .<Product,Product>chunk(5)
                .reader(flatFileItemReader(null))
                .processor(asyncItemProcessor())
                .writer(asyncItemWriter())
                .build();
    }

    @Bean
    public AsyncItemProcessor asyncItemProcessor(){
        AsyncItemProcessor processor = new AsyncItemProcessor();
        processor.setDelegate(new ProdutProcessor());
        processor.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return processor;
    }


    @Bean
    public AsyncItemWriter asyncItemWriter(){
        AsyncItemWriter writer = new AsyncItemWriter();
        writer.setDelegate(flatFileItemWriter(null));
        return writer;
    }

    @Bean
    public Step step0() {
        return steps.get("step0")
                .tasklet(new ConsoleTasklet())
                .build();
    }

    @Bean
    public Job job1() throws InterruptedException {
        return jobs.get("joobn")
               .incrementer(new RunIdIncrementer())
                .start(step0())
                .next(asyncStep())
                .build();
    }

}
