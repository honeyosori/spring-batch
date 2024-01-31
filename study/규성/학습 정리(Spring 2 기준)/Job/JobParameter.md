# JobParameter

## 개념

- Job 을 실행할 때 함께 포함되어 사용되는 파라미터를 가진 도메인 객체
- 하나의 Job 에 존재할 수 있는 여러개의 JobInstance 를 구분하기 위한 용도
- 따라서 Jobparameters 와 JobInstance 는 1:1 관계
- JOB_EXECUTION 과는 1:M 의 관계 (여러번 실행한다면)

## 생성 및 바인딩

- 어플리케이션 실행 시 주입
  - Java -jar LogBatch.jar.requestDate=20210101
- 코드로 생성
  - JobParameterBuilder, DefaultJobParametersConverter
- SpEL 이용
  - @Value("#{jobParameter[requestDate]}"), @JobScope, @StepScope 등의 선언이 필수

## 간단하게 도식화해서 알아보기

![image](https://github.com/Sal-Mal/salmal-be/assets/53935439/44e6c7e4-d1c9-4c48-8ad4-84c26af1b8f6)

위와 같이 JobParameter의 경우 여러가지 타입으로 정의할 수 있다.


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
                
               // Step 안에서 설정한 jobParameters 를 참조할 수 있음 
              JobParameters jobParameters = contribution.getStepExecution().getJobExecution().getJobParameters();
              jobParameters.getString("name");
              jobParameters.getLong("seq");
              jobParameters.getDate("date");
              jobParameters.getDouble("age");
              
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

@Component
@RequiredArgsConstructor
public class JobRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final Job job;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("name", "user")
                .addLong("seq", 2L)
                .addDate("date", new Date())
                .addDouble("age", 16.5)
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

### BATCH_JOB_EXECUTION (1)
![image](https://github.com/Sal-Mal/salmal-be/assets/53935439/931c211f-384b-4090-94d9-63e5a865e64c)

### BATCH_JOB_EXECUTION_PARAMS (M)
![image](https://github.com/Sal-Mal/salmal-be/assets/53935439/ff49cfd0-8b77-4f60-a85f-df86236ae82f)

