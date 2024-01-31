//package com.spring.batch.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobParameters;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
//import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
//import org.springframework.batch.repeat.RepeatStatus;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@RequiredArgsConstructor
//public class JobConfiguration {
//
//    private final JobBuilderFactory jobBuilderFactory;
//    private final StepBuilderFactory stepBuilderFactory;
//
//    @Bean
//    public Job helloHob(){
//        return jobBuilderFactory.get("helloJob")
//                .start(helloStep())
//                .next(helloStep2())
//                .next(helloStep3())
//                .build();
//    }
//
//    @Bean
//    public Step helloStep(){
//        return stepBuilderFactory.get("helloStep")
//                .tasklet((contribution, chunkContext) -> {
//
//                    JobParameters jobParameters = contribution.getStepExecution().getJobExecution().getJobParameters();
//                    jobParameters.getString("name");
//                    jobParameters.getLong("seq");
//                    jobParameters.getDate("date");
//                    jobParameters.getDouble("age");
//
//                    System.out.println("hello");
//                    return RepeatStatus.FINISHED;
//                }).build();
//    }
//
//    @Bean
//    public Step helloStep2(){
//        return stepBuilderFactory.get("helloStep2")
//                .tasklet((contribution, chunkContext) -> {
//                    System.out.println("hello2");
////                    throw new RuntimeException("step2 has failed");
//                    return RepeatStatus.FINISHED;
//                }).build();
//    }
//
//    @Bean
//    public Step helloStep3(){
//        return stepBuilderFactory.get("helloStep3")
//                .tasklet((contribution, chunkContext) -> {
//                    System.out.println("hello3");
//                    return RepeatStatus.FINISHED;
//                }).build();
//    }
//}