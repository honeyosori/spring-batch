package com.spring.batch.week2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChunkJob {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
	public Job chunkBasedJob() {
		return this.jobBuilderFactory.get("chunkBasedJob")
				.start(chunkStep())
				.incrementer(new RunIdIncrementer())
				.build();
	}

	@Bean
	public Step chunkStep() {
		return this.stepBuilderFactory.get("chunkStep")
//				.<String, String>chunk(10)
				.<String, String>chunk(completionPolicy())
//				.<String, String>chunk(randomCompletionPolicy())
				.reader(itemReader())
				.writer(itemWriter())
				.listener(new LoggingStepStartStopListener())
				.build();
	}

	@Bean
	public ListItemReader<String> itemReader() {
		List<String> items = new ArrayList<>(100);

		for (int i = 0; i < 100; i++) {
			items.add(UUID.randomUUID().toString());
		}

		return new ListItemReader<>(items);
	}

	@Bean
	public ItemWriter<String> itemWriter() {
		return items -> {
			for (String item : items) {
				System.out.println(">> current item = " + item);
			}
		};
	}

	@Bean
	public CompletionPolicy completionPolicy() {
		CompositeCompletionPolicy policy =
				new CompositeCompletionPolicy();

		policy.setPolicies(
				new CompletionPolicy[] {
						new TimeoutTerminationPolicy(3),
						new SimpleCompletionPolicy(10)});

		return policy;
	}

	@Bean
	public CompletionPolicy randomCompletionPolicy() {
		return new RandomChunkSizePolicy();
	}
}
