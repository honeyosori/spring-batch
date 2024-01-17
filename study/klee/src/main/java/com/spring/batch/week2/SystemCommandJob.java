package com.spring.batch.week2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.step.tasklet.SimpleSystemProcessExitCodeMapper;
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SystemCommandJob {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

//    @Bean
//    public Job systemCommandJob() {
//        return jobBuilderFactory.get("systemCommandJob")
//                .start(this.systemCommandStep())
//                .incrementer(new RunIdIncrementer())
//                .build();
//    }

//    @Bean
//    public Step systemCommandStep() {
//        return stepBuilderFactory.get("systemCommandStep")
//                .tasklet(systemCommandTasklet())
//                .build();
//    }
//
//    @Bean
//	public SystemCommandTasklet systemCommandTasklet() {
//		SystemCommandTasklet tasklet = new SystemCommandTasklet();
//
//		tasklet.setCommand("touch test.txt");
//		tasklet.setTimeout(5000);
//		tasklet.setInterruptOnCancel(true); // 비정상적 종료 시 스레드 강제 종료 여부
//
//        // 명령을 실행할 디렉토리
//		tasklet.setWorkingDirectory("/Users/workspace/spring-batch");
//        // 시스템 반환 코드를 배치 상태 값으로 매핑하는 구현체
//		tasklet.setSystemProcessExitCodeMapper(touchCodeMapper());
//		// 명령 완료 여부 확인 주기
//        tasklet.setTerminationCheckInterval(5000);
//        // 커스텀 taskExecutor 설정
//		tasklet.setTaskExecutor(new SimpleAsyncTaskExecutor());
//        // 환경 변수 설정
//		tasklet.setEnvironmentParams(new String[] {
//				"JAVA_HOME=/java",
//				"BATCH_HOME=/Users/batch"});
//
//		return tasklet;
//	}
//
//    @Bean
//	public SimpleSystemProcessExitCodeMapper touchCodeMapper() {
//		return new SimpleSystemProcessExitCodeMapper();
//	}

}
