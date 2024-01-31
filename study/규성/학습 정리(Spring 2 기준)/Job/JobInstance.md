# JobInstance

## 개념

- Job 이 실행될 때 생성되는 Job 의 논리적 실행 단위 객체로서 고유하게 식별 가능한 작업 실행을 나타냄
- Job 의 설정과 구성은 동일하지만 Job 이 실행되는 시점에 처리하는 내용은 다르기 때문에 Job 의 실행을 구분해야 함
  - 하루에 한 번 씩 배치 Job 이 실행이 되는 시점에 처리하는 내용은 다르기 때문에 Job 의 실행을 구분해야함
- JobInstance 생성 및 실행
  - 처음 시작 하는 Job + JobParameter 조합 : 새로운 JobInstance 생성
  - 이전과 동일한 Job + JobParameter 조합 : 이미 존재하는 JobInstance 반환
    - 내부적으로 JobName + jobKey(JobParameter 의 해시값) 를 가지고 JobInstance 객체를 얻음
- Job 과는 1:M 관계

## BATCH_JOB_INSTACNE 테이블

JOB_NAME(Job) 과 JOB_KEY(JobParameter 해시값) 가 동일한 데이터는 중복해서 저장할 수 없음

## 예제

```java
@Configuration
@RequiredArgsConstructor
public class JobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job helloHob(){
        return jobBuilderFactory.get("helloJob")
                .start(helloStep())
                .next(helloStep2())
                .next(helloStep3())
                .build();
    }

    @Bean
    public Step helloStep(){
        return stepBuilderFactory.get("helloStep")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("hello");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step helloStep2(){
        return stepBuilderFactory.get("helloStep2")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("hello2");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step helloStep3(){
        return stepBuilderFactory.get("helloStep3")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("hello3");
                    return RepeatStatus.FINISHED;
                }).build();
    }
}
```

```java
@Component
@RequiredArgsConstructor
public class JobRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final Job job;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("name2", "user2")
                .toJobParameters();

        jobLauncher.run(job, jobParameters);
    }
}

```

```yaml
spring:
  batch:
    job:
      enabled: false
```

#### BATCH_JOB_EXECUTION_PARAMS
![image](https://github.com/Sal-Mal/salmal-be/assets/53935439/c7e3ff21-2206-4a27-a647-1ea52513e3df)
#### BATCH_JOB_INSTANCE
![image](https://github.com/Sal-Mal/salmal-be/assets/53935439/6c9703bf-f645-4921-aa17-6aa03a75c0ef)

#### 설명
JobInstance 의 경우 Job 과 JobParameter 의 조합이 달라지면 새로 생성이 된다. 관련 테이블인 BATCH_JOB_INSTANCE 와 BATCH_JOB_EXECUTION_PARAMS 를 살펴보면
새로운 조홥의 JobParameter 일 경우, BATCH_JOB_EXECUTION_PARAMS 에 새로운 row 가 추가되며 BATCH_JOB_INSTANCE 에도 새롭게 추가된 JobInstance 에 대한 정보(JOB_NAME + JOB_KEY) 가
추가되는 것을 확인할 수 있다.

만약 이미 존재하는 Job + JobParmeter 의 조합을 가진 JobInstance 가 반환이되면(이전에 실행했던 동일한 작업) App 단에서는 RuntimeException 이 발생한다.