package com.spring.batch.extension;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MultithreadedJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;


    @Bean
    public Job multithreadedJob() {
        return jobBuilderFactory.get("multithreadedJob")
                .start(multithreadedStep())
                .incrementer(new RunIdIncrementer())
                .preventRestart()
                .build();
    }

    @Bean
    public Step multithreadedStep() {
        return stepBuilderFactory.get("multithreadedStep")
                .<String, String>chunk(10)
                .reader(multithreadedItemReader())
                .writer(multithreadedItemWriter())
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public ListItemReader<String> multithreadedItemReader() {
        List<String> items = new ArrayList<>(100);

        for (int i = 1; i <= 100; i++) {
            items.add("item_" + i);
        }

        return new ListItemReader<>(items);
    }

    @Bean
    public ItemWriter<String> multithreadedItemWriter() {

        return items -> {
            for (String item : items) {
                log.info(">> " + item);
            }
        };
    }
}
