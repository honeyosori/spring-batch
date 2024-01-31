package com.spring.batch.order.batch;

import com.spring.batch.order.module.application.OrderFindService;
import com.spring.batch.order.module.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.batch.api.Decider;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * 1. 주문 조회
 * 2. 재고 확인
 * 2-1. 재고 있으면 Shipping 처리
 * 2-2. 없으면 Pending
 * 3. 재고 차감
 * 4. 알림 대상 조회
 * 5. 알림 설정 확인
 * 5-1. 메일 알림
 * 5-2. SMS 알림
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class OrderJob {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job orderProcessingJob() {
        return jobBuilderFactory.get("orderProcessingJob")
//                .start(findOrderStep(null))
                .start(findOrdersStep())
                .next(stockDecider())
                    .on("OUT_OF_STOCK")
                    .to(changeStatusToPendingStep())
                .from(stockDecider())
                    .on("AVAILABLE")
                    .to(changeStatusToShippingStep())
                    .next(notificationDecider())
                        .on("SMS")
                        .to(sendSMSStep())
                    .from(notificationDecider())
                        .on("EMAIL")
                        .to(sendMailStep())
                    .from(notificationDecider())
                        .on("*")
                        .end()
                .end()
                .listener(new JobLoggingListener())
                .incrementer(new RunIdIncrementer())
                .preventRestart()
                .build();
    }

    @Bean
    @JobScope
    public Step findOrderStep(@Value("#{jobParameters[date]}") Integer date) {
        return stepBuilderFactory.get("readOrderStep")
                .tasklet((contribution, chunkContext) -> {
                    log.info("{} 주문 조회", date);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public StockDecider stockDecider() {
        return new StockDecider();
    }

    @Bean
    public Step changeStatusToShippingStep() {
        return stepBuilderFactory.get("changeStatusToShippingStep")
                .tasklet((contribution, chunkContext) -> {
                    log.info("상품 상태 변경 (Shipping)");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step changeStatusToPendingStep() {
        return stepBuilderFactory.get("changeStatusToPendingStep")
                .tasklet((contribution, chunkContext) -> {
                    log.info("상품 상태 변경 (Pending)");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public NotificationDecider notificationDecider() {
        return new NotificationDecider();
    }

    @Bean
    public Step sendMailStep() {
        return stepBuilderFactory.get("sendMailStep")
                .tasklet((contribution, chunkContext) -> {
                    log.info("메일 발송");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step sendSMSStep() {
        return stepBuilderFactory.get("sendSMSStep")
                .tasklet((contribution, chunkContext) -> {
                    log.info("SMS 발송");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }


    private final OrderFindService orderFindService;

    @Bean
    public Step findOrdersStep() {
        return stepBuilderFactory.get("findOrdersStep")
                .tasklet(findOrdersTasklet(null))
                .build();
    }

    @Bean
    @StepScope
    public MethodInvokingTaskletAdapter findOrdersTasklet(@Value("#{jobParameters[date]}") Integer date) {
        MethodInvokingTaskletAdapter methodInvokingTaskletAdapter = new MethodInvokingTaskletAdapter();
        methodInvokingTaskletAdapter.setTargetObject(orderFindService);
        methodInvokingTaskletAdapter.setTargetMethod("findOrders");
        methodInvokingTaskletAdapter.setArguments(new Object[]{date});

        return methodInvokingTaskletAdapter;
    }
}
