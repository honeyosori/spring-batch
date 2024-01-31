package io.spring.batch.helloworld.job;

import io.spring.batch.helloworld.step.StepConfiguration;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepConfiguration stepConfiguration;


    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("job")
                .start(stepConfiguration.step1())
                .next(stepConfiguration.step2())
                .build();
    }


}
