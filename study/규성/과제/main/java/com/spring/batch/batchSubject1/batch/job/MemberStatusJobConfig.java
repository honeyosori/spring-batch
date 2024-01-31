package com.spring.batch.batchSubject1.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Flow;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MemberStatusJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final MemberExecutionDecider memberExecutionDecider;

    @Bean
    public Job memberJob(){
//        return jobBuilderFactory.get("memberJob");
        return null;
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("step")
                .tasklet((stepContribution, chunkContext) -> {
                    log.info("시작");

                    return RepeatStatus.FINISHED;
                })
                .build();
    }


    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet((stepContribution, chunkContext) -> {
                    log.info("회원 등급 및 최종 로그인 날짜를 통한 회원 상태 변경");
                    /**
                     * 1년 미접속 = 휴먼계정 처리 및 알림 (관리자 제외)
                     * 2년 미접속 = 계정 삭제 (VIP 및 관리자 제외)
                     */
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet((stepContribution, chunkContext) -> {
                    log.info("회원 이름 필터링 및 회원 상태 변경");

                    /**
                     * 이름에 부적절한 단어가 들어간 회원 필터링 및 알림
                     */

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

}
