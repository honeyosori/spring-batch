package com.spring.batch.order.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

import java.util.Random;

@Slf4j
public class NotificationDecider implements JobExecutionDecider {

    private Random random = new Random();

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        if (random.nextBoolean()) {
            if(random.nextBoolean()) {
                log.info("SMS 발송 요청");
                return new FlowExecutionStatus("SMS");
            }
            log.info("EMAIL 발송 요청");
            return new FlowExecutionStatus("EMAIL");
        } else {
            log.info("알림 발송 안함");
            return new FlowExecutionStatus("NOT_SEND");
        }
    }
}
