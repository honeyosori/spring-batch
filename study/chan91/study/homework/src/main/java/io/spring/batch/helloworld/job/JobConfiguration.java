package io.spring.batch.helloworld.job;

import io.spring.batch.helloworld.step.StepConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class JobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepConfiguration stepConfiguration;

    @Bean
    public Job job() {

        String randomJobName = String.valueOf(UUID.randomUUID());

        return this.jobBuilderFactory.get(randomJobName)
                .start(stepConfiguration.step1())
                .next(stepConfiguration.step2())
                .build();
    }


}
