# Execution Context

## 개념
- JobExecution과 StepExecution은 ExecutionContext를 가지고 있다.
- ExecutionContext는 JobExecution과 StepExecution이 실행되는 동안 데이터를 공유할 수 있는 공간이다.
- 데이터는 Key-Value 형태로 저장된다.
- ExecutionContext는 JobRepository에 의해 관리된다.

## 사용법
```java
 public Step helloStep() {
    return stepBuilderFactory.get("helloStep")
            .tasklet((contribution, chunkContext) -> {
                ExecutionContext jobExecutionContext = chunkContext.getStepContext()
                                                                    .getStepExecution()
                                                                    .getJobExecution()
                                                                    .getExecutionContext();
                jobExecutionContext.put("jobItem", "JOB!");

                ExecutionContext stepExecutionContext = chunkContext.getStepContext()
                                                                    .getStepExecution()
                                                                    .getExecutionContext();
                stepExecutionContext.put("stepItem", "STEP!");

                log.info("From jobExecutionContext: {}", jobExecutionContext.get("jobItem"));
                log.info("From stepExecutionContext: {}", stepExecutionContext.get("stepItem"));

                // StepContext.getJobExecutionContext로 접근 시 unmodifiableMap 반환
                Map<String, Object> getJobExecutionContext = chunkContext.getStepContext().getJobExecutionContext();
                log.info("From getJobExecutionContext:");
                getJobExecutionContext.keySet().forEach(key -> log.info("key: {}, value: {}", key, getJobExecutionContext.get(key)));

                return RepeatStatus.FINISHED;
            }).build();
}
```


## 응용
- ExecutionPromotionListener를 사용하여 Step에서 처리한 데이터를 Job에서 사용할 수 있게 승격할 수 있다.
- 스텝의 ExecutionContext에 저장된 키를 못 찾아도 기본적으로 예외는 발생하지 않는다.

```java
@Bean
public Step helloStep() {
    return stepBuilderFactory.get("helloStep")
            .tasklet(new HelloTasklet())
            .listener(promotionListener())
            .build();
}

@Bean
public StepExecutionListener promotionListener() {
    ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
    listener.setKeys(new String[]{"stepItem"});
    return listener;
}
```

## 메타데이터
- Job(Step)ExecutionContext는 BATCH_JOB(STEP)_EXECUTION_CONTEXT 테이블에 저장된다.

| JOB_EXECUTION_ID | SHORT_CONTEXT | SREIALIZED_CONTEXT |
| --- | --- | --- |
| 6	| {"@class":"java.util.HashMap","jobItem":"JOB!","stepItem":"STEP!"} | NULL |


## 참고
- [Exit status, Batch status 개념](https://velog.io/@mu1616/spring-batch-BatchSatus-ExitStatus)