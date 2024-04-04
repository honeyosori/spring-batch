package com.spring.batch.hwk.batch.job;

import com.spring.batch.hwk.batch.step.DisposalLogReader;
import com.spring.batch.hwk.batch.step.GetDomainListTasklet;
import com.spring.batch.hwk.batch.step.GetSchemaListTasklet;
import com.spring.batch.hwk.module.DomainDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OutdatedRecordPurgeJobConfig2 {

    public final String JOB_NAME = "outdatedRecordPurgeJob2";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final GetDomainListTasklet getDomainListTasklet;
    private final GetSchemaListTasklet getSchemaListTasklet;

    @Bean(JOB_NAME)
    public Job outdatedRecordPurgeJob() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(getSchemaStep())
                .next(getDomainStep())
                .next(disposalLogStep())
                .incrementer(new RunIdIncrementer())
                .preventRestart()
                .build();
    }

    @Bean(JOB_NAME+"_getSchemaStep")
    public Step getSchemaStep(){
        return stepBuilderFactory.get(JOB_NAME + "_getSchemaStep")
                .tasklet(getSchemaListTasklet)
                .build();
    }

    @Bean(JOB_NAME+"_getDomainStep")
    public Step getDomainStep(){
        return stepBuilderFactory.get(JOB_NAME + "_getDomainListStep")
                .tasklet(getDomainListTasklet)
                .build();
    }

    @Bean(JOB_NAME+"_disposalLogStep")
    public Step disposalLogStep() {
        return stepBuilderFactory.get(JOB_NAME + "_disposalLogStep")
                .<DomainDto, DomainDto>chunk(100)
                .reader(disposalLogReader())
                .writer(list -> {
                    log.info("writer size: {}", list.size());
                    for (DomainDto domainDto : list) {
                        log.info("domainDto: {}", domainDto.toString());
                    }

                })
                .build();
    }

    @Bean(JOB_NAME+"_disposalLogReader")
    public DisposalLogReader<DomainDto> disposalLogReader() {
        return new DisposalLogReader<>();
    }
}
