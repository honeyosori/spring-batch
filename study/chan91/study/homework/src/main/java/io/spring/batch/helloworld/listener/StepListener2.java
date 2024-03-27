package io.spring.batch.helloworld.listener;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.context.annotation.Configuration;

/**
 * Inteface 방식 리스너 정의
 */

@Configuration
public class StepListener2 implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("beforeStep impl");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("After Step impl");
        return ExitStatus.COMPLETED;
    }

}