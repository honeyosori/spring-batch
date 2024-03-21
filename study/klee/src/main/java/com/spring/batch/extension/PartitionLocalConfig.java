package com.spring.batch.extension;

import com.spring.batch.order.module.dao.OrderRepository;
import com.spring.batch.order.module.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class PartitionLocalConfig {
    public static final String JOB_NAME = "partitionLocalBatch";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final OrderRepository orderRepository;

    private int chunkSize = 10;
    private int poolSize = 5;

    @Bean(name = JOB_NAME+"_taskPool")
    public TaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setThreadNamePrefix("partition-thread");
        executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
        executor.initialize();
        return executor;
    }

    @Bean(name = JOB_NAME+"_partitionHandler")
    public TaskExecutorPartitionHandler partitionHandler() {
        // SimpleAsyncTaskExecutor를 사용할 경우 쓰레드를 계속해서 생성할 수 있기 때문에 실제 운영 환경에서는 대형 장애를 발생시킬 수 있음.
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler(); // 쓰레드풀 내에서 지정된 갯수만큼의 쓰레드만 생성할 수 있도록 ThreadPoolTaskExecutor 사용
        partitionHandler.setStep(step1()); // Worker로 실행할 Step 지정
        partitionHandler.setTaskExecutor(executor()); // 멀티쓰레드로 실행할 수 있도록 TaskExecutor 지정
        partitionHandler.setGridSize(poolSize); // 파티션 수 지정
        return partitionHandler;
    }

    @Bean(name = JOB_NAME)
    public Job job() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(step1Manager())
                .incrementer(new RunIdIncrementer())
                .preventRestart()
                .build();
    }

    // 마스터 Step은 어떤 Step을 Worker로 지정하여 파티셔닝을 할 것인지를 결정하고, 이때 사용할 PartitionHandler 를 등록한다.
    @Bean(name = JOB_NAME +"_step1Manager")
    public Step step1Manager() {
        return stepBuilderFactory.get("step1.manager")
                .partitioner("step1", partitioner(null, null))
                .step(step1())
                .partitionHandler(partitionHandler())
                .build();
    }

    @Bean(name = JOB_NAME +"_partitioner")
    @StepScope
    public OrderIdRangePartitioner partitioner(
            @Value("#{jobParameters['startDate']}") Integer startDate,
            @Value("#{jobParameters['endDate']}") Integer endDate) {

        return new OrderIdRangePartitioner(orderRepository, startDate, endDate);
    }

    @Bean(name = JOB_NAME +"_step")
    public Step step1() {
        return stepBuilderFactory.get(JOB_NAME +"_step")
                .<Order, Order>chunk(chunkSize)
                .reader(reader(null, null))
                .writer(writer(null, null))
                .build();
    }

    @Bean(name = JOB_NAME +"_reader")
    @StepScope
    public JpaPagingItemReader<Order> reader(
            @Value("#{stepExecutionContext[minId]}") Long minId,
            @Value("#{stepExecutionContext[maxId]}") Long maxId) {
        // stepExecutionContext는 partitioner에서 ExecutionContext에 저장한 minId, maxId를 읽어올 수 있음

        Map<String, Object> params = new HashMap<>();
        params.put("minId", minId);
        params.put("maxId", maxId);

        log.info("reader minId={}, maxId={}", minId, maxId);

//        Random random = new Random();
//        if(random.nextBoolean()) {
//            log.error("reader exception");
//            throw new RuntimeException("reader exception");
//        }

        return new JpaPagingItemReaderBuilder<Order>()
                .name(JOB_NAME +"_reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString(
                        "SELECT o " +
                                "FROM Order o " +
                                "WHERE o.id BETWEEN :minId AND :maxId")
                .parameterValues(params)
                .build();
    }

    @Bean(name = JOB_NAME +"_writer")
    @StepScope
    public ItemWriter<Order> writer(
            @Value("#{stepExecutionContext[minId]}") Long minId,
            @Value("#{stepExecutionContext[maxId]}") Long maxId) {

        return items -> {
            for (Order item : items) {
                log.info("writing item id={}", item.getId());
            }
        };
    }
}
