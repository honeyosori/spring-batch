package com.spring.batch.week3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FlowJob {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	@Bean
	public Job conditionalStepLogicJob() {
		return this.jobBuilderFactory.get("conditionalStepLogicJob")
//				.start(initializeBatch())
                .start(preProcessingFlow())
				.next(runBatch())
                .end()
                .incrementer(new RunIdIncrementer())
				.build();
	}


    /**
     * flow step
     */
	@Bean
	public Step initializeBatch() {
		return this.stepBuilderFactory.get("initializeBatch")
				.flow(preProcessingFlow())
				.build();
	}

    @Bean
    public Flow preProcessingFlow() {
        return new FlowBuilder<Flow>("preProcessingFlow")
                .start(loadFileStep())
                .next(loadCustomerStep())
                .next(updateStartStep())
                .build();
    }

	@Bean
	public Step loadFileStep() {
		return this.stepBuilderFactory.get("loadFileStep")
				.tasklet(loadStockFile())
				.build();
	}

	@Bean
	public Step loadCustomerStep() {
		return this.stepBuilderFactory.get("loadCustomerStep")
				.tasklet(loadCustomerFile())
				.build();
	}

	@Bean
	public Step updateStartStep() {
		return this.stepBuilderFactory.get("updateStartStep")
				.tasklet(updateStart())
				.build();
	}

	@Bean
	public Step runBatch() {
		return this.stepBuilderFactory.get("runBatch")
				.tasklet(runBatchTasklet())
				.build();
	}

    @Bean
    public Tasklet loadStockFile() {
        return (contribution, chunkContext) -> {
            log.info("The stock file has been loaded");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet loadCustomerFile() {
        return (contribution, chunkContext) -> {
            log.info("The customer file has been loaded");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet updateStart() {
        return (contribution, chunkContext) -> {
            log.info("The start has been updated");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet runBatchTasklet() {
        return (contribution, chunkContext) -> {
            log.info("The batch has been run");
            return RepeatStatus.FINISHED;
        };
    }
}
