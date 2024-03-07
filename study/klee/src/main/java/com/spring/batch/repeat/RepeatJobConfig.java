package com.spring.batch.repeat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatCallback;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.repeat.exception.SimpleLimitExceptionHandler;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RepeatJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job repeatJob(Step repeatStep) {
        return jobBuilderFactory.get("repeatJob")
                .start(repeatStep)
                .incrementer(new RunIdIncrementer())
                .preventRestart()
                .build();
    }

    @Bean
    public Step repeatStep() {
        return stepBuilderFactory.get("repeatStep")
                .tasklet(new RepeatTemplateTasklet())
                .build();
    }


    private static class RepeatTemplateTasklet implements Tasklet {

        private RepeatTemplate repeatTemplate;

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
            // Perform your task logic here
            log.info("Executing repeatStep");

            // Use RepeatTemplate for more control over repetitions
            repeatTemplate = new RepeatTemplate();

            // Set the desired repeat count
            repeatTemplate.setCompletionPolicy(new SimpleCompletionPolicy(3));

            // Set the exception handler
            repeatTemplate.setExceptionHandler(new SimpleLimitExceptionHandler(2));

            repeatTemplate.iterate(new RepeatCallback() {
                @Override
                public RepeatStatus doInIteration(RepeatContext context) {
                    // Perform iteration logic here
                    log.info("Iteration " + context.getStartedCount());
                    return RepeatStatus.CONTINUABLE;
                }
            });

            return RepeatStatus.FINISHED;
        }
    }

}
