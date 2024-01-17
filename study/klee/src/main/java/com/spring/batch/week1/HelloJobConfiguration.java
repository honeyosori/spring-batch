package com.spring.batch.week1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HelloJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("helloJob")
                .start(this.helloStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step helloStep() {
        return stepBuilderFactory.get("helloStep")
                .tasklet((contribution, chunkContext) -> {
                    ExecutionContext jobExecutionContext = chunkContext.getStepContext()
                                                                        .getStepExecution()
                                                                        .getJobExecution()
                                                                        .getExecutionContext();
                    jobExecutionContext.put("jobItem", "JOB!");

                    ExecutionContext stepExecutionContext = chunkContext.getStepContext()
                                                                        .getStepExecution()
                                                                        .getExecutionContext();
                    stepExecutionContext.put("stepItem", "STEP!");

                    log.info("From jobExecutionContext: {}", jobExecutionContext.get("jobItem"));
                    log.info("From stepExecutionContext: {}", stepExecutionContext.get("stepItem"));

                    // return unmodifiableMap
                    Map<String, Object> getJobExecutionContext = chunkContext.getStepContext().getJobExecutionContext();
                    log.info("From getJobExecutionContext:");
                    getJobExecutionContext.keySet().forEach(key -> log.info("key: {}, value: {}", key, getJobExecutionContext.get(key)));

                    return RepeatStatus.FINISHED;
                })
                .listener(promotionListener())
                .build();
    }


//    @Bean
//    public Step helloStep() {
//        return stepBuilderFactory.get("helloStep")
//                .tasklet(new HelloTasklet())
//                .listener(promotionListener())
//                .build();
//    }

    @Bean
    public StepExecutionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"stepItem"});
        return listener;
    }

}
