package io.spring.batch.helloworld.listener;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.context.annotation.Configuration;


/**
 * Annotation 방식 리스너 정의
 */

@Configuration
public class StepListener1 {

    @BeforeStep
    public void beforeStep(StepExecution stepExecution){
        System.out.println("스텝이 실행되기 전 실행");
    }

    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution){
        System.out.println("스텝이 다 끝난 후");
        return ExitStatus.COMPLETED;
    }

}
