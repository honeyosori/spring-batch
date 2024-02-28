package com.spring.batch.order.batch;

import com.spring.batch.order.module.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.ClassifierCompositeItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.Collections;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class OrderChunkJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private Random random = new Random();

    @Bean
    public Job orderChunkJob() {
        return jobBuilderFactory.get("orderChunkJob")
                .start(orderChunkStep())
                .incrementer(new RunIdIncrementer())
                .preventRestart()
                .build();
    }

    @Bean
    public Step orderChunkStep() {
        return stepBuilderFactory.get("orderChunkJob")
                .<Order, Order>chunk(5)
                .reader(orderItemReader(null, null))
                .processor(orderItemProcessor())
                .writer(orderItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Order> orderItemReader(EntityManagerFactory entityManagerFactory,
                                             @Value("#{jobParameters[date]}") Integer date) {

        QueryProvider queryProvider = new QueryProvider();
        queryProvider.setDate(date);

        return new JpaPagingItemReaderBuilder<Order>()
                .name("orderReader")
                .entityManagerFactory(entityManagerFactory)
                .queryProvider(queryProvider)
                .parameterValues(Collections.singletonMap("date", date))
                .build();
    }

    @Bean
    public ItemWriter<Order> orderItemWriter() {
        return orders -> {
            for (Order order : orders) {
                log.info("Writing item: {}", order);
            }
        };
    }

    @Bean
    public ClassifierCompositeItemProcessor<Order, Order> orderItemProcessor() {
        ClassifierCompositeItemProcessor<Order, Order> processor = new ClassifierCompositeItemProcessor<>();
        processor.setClassifier(orderStatusClassifier());
        return processor;
    }

    @Bean
    public Classifier orderStatusClassifier() {
        return new OrderStatusClassifier(orderedStatusItemProcessor(), cancelStatusItemProcessor());
    }

    @Bean
    public ItemProcessor<Order, Order> orderedStatusItemProcessor() {
        return new ItemProcessor<Order, Order>() {
            @Override
            public Order process(Order order) throws Exception {
                if (isStockAvailable(order)) {
                    return order;
                }
                log.info("Item is out of stock: {}", order);
                return null;
            }

            private boolean isStockAvailable(Order order) {
                return random.nextBoolean();
            }
        };
    }

    @Bean
    public ItemProcessor<Order, Order> cancelStatusItemProcessor() {
        return order -> {
            log.info("Canceling item: {}", order);
            return null;
        };
    }
}
