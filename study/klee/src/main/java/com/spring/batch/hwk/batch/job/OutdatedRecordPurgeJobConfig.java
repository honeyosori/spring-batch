package com.spring.batch.hwk.batch.job;

import com.spring.batch.hwk.batch.step.GetDomainListTasklet;
import com.spring.batch.hwk.batch.step.GetSchemaListTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.SimplePartitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OutdatedRecordPurgeJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final GetSchemaListTasklet schemaValidatorTasklet;
    private final GetDomainListTasklet getDomainListTasklet;

    @Qualifier("memberDataSource")
    private final DataSource memberDataSource;

    @Bean
    public Job outdatedRecordPurgeJob() throws Exception {
        return jobBuilderFactory.get("outdatedRecordPurgeJob")
                .start(getSchemaStep())
                .next(getDomainListStep())
                .next(step1())
                .incrementer(new RunIdIncrementer())
                .preventRestart()
                .build();
    }

    @Bean
    public Step getDomainListStep(){
        return stepBuilderFactory.get("getDomainListStep")
                .tasklet(getDomainListTasklet)
                .build();
    }

    @Bean
    public Step getSchemaStep(){
        return stepBuilderFactory.get("getSchemaStep")
                .tasklet(schemaValidatorTasklet)
                .build();
    }

    @Bean
    public Step partitionStep() throws Exception {
        return stepBuilderFactory.get("partitionStep")
                .partitioner("partitionStep", partitioner())
                .step(splitStep())
                .partitionHandler(partitionHandler())
                .build();
    }

    @Bean
    public Partitioner partitioner() {
        return new SimplePartitioner();
    }

    @Bean
    public TaskExecutorPartitionHandler partitionHandler() throws Exception {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(splitStep());
        partitionHandler.setTaskExecutor(executor());
        partitionHandler.setGridSize(5);
        return partitionHandler;
    }

    @Bean
    public TaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix("partition-thread");
        executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
        executor.initialize();
        return executor;
    }

    @Bean
    public Step splitStep() throws Exception {
        return stepBuilderFactory.get("splitStep")
                .flow(splitFlow())
                .build();
    }

    @Bean
    public Flow splitFlow() throws Exception {
        return new FlowBuilder<SimpleFlow>("splitFlow")
                .split(new SimpleAsyncTaskExecutor())
                .add(flow1(), flow2())
                .build();
    }

    @Bean
    public Flow flow1() throws Exception {
        return new FlowBuilder<SimpleFlow>("flow1")
                .start(step1())
                .build();
    }

    @Bean
    public Flow flow2() {
        return new FlowBuilder<SimpleFlow>("flow2")
                .start(step2())
                .build();
    }

    @Bean
    public Step step1() throws Exception {
        return stepBuilderFactory.get("step1")
                .<Long, Long>chunk(100)
                .reader(jdbcPagingItemReader())
                .writer(list -> log.info("writer size: {}", list.size()))
                .build();
    }

    @Bean
    public JdbcPagingItemReader<Long> jdbcPagingItemReader() throws Exception {
        return new JdbcPagingItemReaderBuilder<Long>()
                .pageSize(100)
                .fetchSize(100)
                .dataSource(memberDataSource)
                .rowMapper((resultSet, rowNum) -> resultSet.getLong("vh_id"))
                .queryProvider(createQueryProvider())
                .name("jdbcPagingItemReader")
                .build();
    }

    @Bean
    public PagingQueryProvider createQueryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(memberDataSource);
        queryProvider.setSelectClause("vh_id");
        queryProvider.setFromClause("from domain");

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("vh_id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);

        return queryProvider.getObject();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet((contribution, chunkContext) -> {
                    log.info("step2");
                    return null;
                })
                .build();
    }
}
