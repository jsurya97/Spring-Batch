package com.jk.spring_batch.writer;

import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.batch.item.support.AbstractItemStreamItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InMemWriter extends AbstractItemStreamItemWriter {
    @Override
    public void write(List list) throws Exception {
        list.stream().forEach(System.out::println);
        System.out.println("------------------------------");
    }
}
