package com.spring.batch.week2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ConditionalJob {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	@Bean
	public Tasklet passTasklet() {
		return (contribution, chunkContext) -> {
			return RepeatStatus.FINISHED;
//			throw new RuntimeException("Causing a failure");
		};
	}

	@Bean
	public Tasklet successTasklet() {
		return (contribution, context) -> {
			log.info("Success!");
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public Tasklet failTasklet() {
		return (contribution, context) -> {
			log.info("Failure!");
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public Job job() {
		return this.jobBuilderFactory.get("conditionalJob")
				.start(firstStep())
				    .on("FAILED")
                    .to(failureStep())
//                    .end()
//                    .fail()
//                    .stopAndRestart(successStep())
				.from(firstStep())
					.on("*")
                    .to(successStep())
				.end()
                .incrementer(new RunIdIncrementer())
				.build();
	}

	@Bean
	public Step firstStep() {
		return this.stepBuilderFactory.get("firstStep")
				.tasklet(passTasklet())
				.build();
	}

	@Bean
	public Step successStep() {
		return this.stepBuilderFactory.get("successStep")
				.tasklet(successTasklet())
				.build();
	}

	@Bean
	public Step failureStep() {
		return this.stepBuilderFactory.get("failureStep")
				.tasklet(failTasklet())
				.build();
	}

	@Bean
	public JobExecutionDecider decider() {
		return new RandomDecider();
	}
}
