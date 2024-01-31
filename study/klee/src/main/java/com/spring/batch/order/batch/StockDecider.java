package com.spring.batch.order.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

import java.util.Random;

@Slf4j
public class StockDecider implements JobExecutionDecider {

    private Random random = new Random();
    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        if (random.nextBoolean()) {
            log.info("재고 있음");
            return new FlowExecutionStatus("AVAILABLE");
        } else {
            log.info("재고 없음");
            return new FlowExecutionStatus("OUT_OF_STOCK");
        }
    }
}
