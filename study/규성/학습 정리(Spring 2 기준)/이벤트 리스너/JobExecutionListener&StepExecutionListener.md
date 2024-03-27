# JobExecutionListener & StepExecutionListener

## JobExecutionListener

- Job 의 설공여부와 상관없이 호출된다.
- 성공/실패 여부는 JobExecution 을 통해 알 수 있다.

![image](https://github.com/honeyosori/spring-batch/assets/53935439/da3121c1-b4d8-4d73-99e3-0d8805939426)

## StepExecutionListener

- Step 의 성공여부와 상관없이 호출된다.
- 성공/실패 여부는 StepExecution 을 통해 알 수 있다.

![image](https://github.com/honeyosori/spring-batch/assets/53935439/77754f17-d9fe-4c88-9168-3528652cb0e4)

## 예제 코드

```java

@Configuration
@RequiredArgsConstructor
@Slf4j
public class JobAndStepListenerConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final CustomJobExecutionListener customJobExecutionListener;
    private final CustomAnnotationJobExecutionListener customAnnotationJobExecutionListener;

    private final CustomStepExecutionListener customStepExecutionListener;
    private final CustomAnnotationStepExecutionListener customAnnotationStepExecutionListener;

    @Bean
    public Job job() throws Exception {

        return jobBuilderFactory.get("batchJob")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .next(step2())
                .listener(customJobExecutionListener) 
//               .listener(customAnnotationJobExecutionListener)
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet(((contribution, chunkContext) -> RepeatStatus.FINISHED))
                .listener(customAnnotationStepExecutionListener)
                .build();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet(((contribution, chunkContext) -> RepeatStatus.FINISHED))
                .listener(customAnnotationStepExecutionListener)
                .build();
    }
}


```

```java
@Slf4j
@Component
public class CustomJobExecutionListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("jobName : {}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long startTime = jobExecution.getStartTime().getTime();
        long endTime = jobExecution.getEndTime().getTime();
        log.info("총 소요시간 : {}", endTime - startTime);
    }
}

```

```java
@Slf4j
@Component
public class CustomAnnotationJobExecutionListener {

    @BeforeJob
    public void beforeTheJob(JobExecution jobExecution){
        log.info("jobName : {}", jobExecution.getJobInstance().getJobName());
    }

    @AfterJob
    public void afterTheJob(JobExecution jobExecution){
        long startTime = jobExecution.getStartTime().getTime();
        long endTime = jobExecution.getEndTime().getTime();
        log.info("총 소요시간 : {}", endTime - startTime);
    }
}

```

```java
@Slf4j
@Component
public class CustomStepExecutionListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        String stepName = stepExecution.getStepName();
        stepExecution.getExecutionContext().put("name", "user1");
        log.info("stepName : {}", stepName);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        ExitStatus exitStatus = stepExecution.getExitStatus();
        log.info("exitStatus = {}", exitStatus);
        BatchStatus status = stepExecution.getStatus();
        log.info("status : {}", status);
        String name = (String) stepExecution.getExecutionContext().get("name");
        log.info("name = {}", name);

        return ExitStatus.COMPLETED;
    }
}

```

```java
@Slf4j
@Component
public class CustomAnnotationStepExecutionListener {

    @BeforeStep
    public void beforeTheStep(StepExecution stepExecution){
        String stepName = stepExecution.getStepName();
        stepExecution.getExecutionContext().put("name", "user1");
        log.info("stepName : {}", stepName);
    }

    @AfterStep
    public ExitStatus afterTheStep(StepExecution stepExecution){
        ExitStatus exitStatus = stepExecution.getExitStatus();
        log.info("exitStatus = {}", exitStatus);
        BatchStatus status = stepExecution.getStatus();
        log.info("status : {}", status);
        String name = (String) stepExecution.getExecutionContext().get("name");
        log.info("name = {}", name);
        return ExitStatus.COMPLETED;
    }
}
```

## 실행 결과

### 1. 정상 실행
![image](https://github.com/honeyosori/spring-batch/assets/53935439/904f5d90-f85f-47c1-a1a5-6a9020f73b69)

### 2. @BeforeJob & @AfterJob 없이 Object 타입의 객체 전달
![image](https://github.com/honeyosori/spring-batch/assets/53935439/47d3ad88-1433-49d6-a8cc-cff4a9a41bf0)

### 3. listener 다수 설정
case 1) 서로 같은 타입의 리스너를 설정했을 때
```java
class Test {
    
    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet(((contribution, chunkContext) -> RepeatStatus.FINISHED))
                .listener(customAnnotationStepExecutionListener)
                .listener(customAnnotationStepExecutionListener)
                .build();
    }
}
```
![image](https://github.com/honeyosori/spring-batch/assets/53935439/5585d670-3bee-477d-bcdc-bdc50a0ec486)
case 2) 서로 다른 타입의 리스너를 설정했을 때
```java
class Test{

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet(((contribution, chunkContext) -> RepeatStatus.FINISHED))
                .listener(customStepExecutionListener)
                .listener(customAnnotationStepExecutionListener)
                .build();
    }

}

```
![image](https://github.com/honeyosori/spring-batch/assets/53935439/82f1b00d-fea7-4866-8394-baea5cc4d260)

### 4. Bean 을 통해 공유 객체가 아닌, 리스너들을 개별 인스턴스로 생성했을 때

```java
class Test{

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet(((contribution, chunkContext) -> RepeatStatus.FINISHED))
                .listener(new CustomStepExecutionListener())
                .listener(new CustomStepExecutionListener())
                .build();
    }

}


```

![image](https://github.com/honeyosori/spring-batch/assets/53935439/e2720d18-1cc2-42fd-b666-718a88a8b6c2)