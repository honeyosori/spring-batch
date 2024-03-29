# Job

## JobBuilderFactory

SimpleBatchConfiguration 설정 클래스에서 빈으로 등록되어, Job을 생성하고자 할 때 JobBuilderFactory를 의존성 주입받아 편리하게 Job을 생성할 수 있다.

## SimpleJob

Job 구현체 중 하나로 여러 단계의 Step으로 구성된다.

## SimpleJobBuilder(JobBuilderHelper) API

```java
public Job job() {
    return jobBuilderFactory.get("job")
        .start(Step)
        .next(Step)
        .validator(JobParametersValidator)
        .preventRestart()
        .incrementer(JobParametersIncrementer)
        .listener(JobExecutionListener)
        .build();
}
```

### start(), next()

start 메서드를 통해 처음 실행할 Step을 설정하고, next 메서드를 통해 이후 순차적으로 실행하고자 하는 Step을 정의한다.

### validator()

* JobParametersValidator를 설정한다.
  * JobParametersValidator: Job 실행 시 JobParameters를 검증하는 역할을 수행하는 인터페이스
  * 스프링 배치에서는 [DefaultJobParametersValidator](https://github.com/spring-projects/spring-batch/blob/main/spring-batch-core/src/main/java/org/springframework/batch/core/job/DefaultJobParametersValidator.java) 구현체를 제공한다.
* Job을 실행시키면 총 두 번 validate 메서드를 호출한다.
  1. [SimpleJobLauncher](https://github.com/spring-projects/spring-batch/blob/main/spring-batch-core/src/main/java/org/springframework/batch/core/launch/support/SimpleJobLauncher.java) run 메소드 내에서 JobExecution 객체를 생성하기 전
  2. [AbastractJob](https://github.com/spring-projects/spring-batch/blob/main/spring-batch-core/src/main/java/org/springframework/batch/core/job/AbstractJob.java) execute 메서드에서 doExecute 메서드를 실행하기 전

### preventRestart()

Job의 성공/실패 여부와 관계 없이 동일한 파라미터로 여러번 재실행되는 것을 막는다.

#### JobBuilderHelper
```java
public B preventRestart() {
    this.properties.restartable = false;
    return this;
}
```

#### SimpleJobLauncher

JobLauncher 인터페이스의 구현클래스

```java
public JobExecution run(final Job job, final JobParameters jobParameters) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
    Assert.notNull(job, "The Job must not be null.");
    Assert.notNull(jobParameters, "The JobParameters must not be null.");
    JobExecution lastExecution = this.jobRepository.getLastJobExecution(job.getName(), jobParameters);
    if (lastExecution != null){
		if(!job.isRestartable()){
		    throw new JobRestartException("JobInstance already exists and is not restartable");
		}
		...
```

### incrementer()

* JobParametersIncrementer를 설정한다.
  * JobParametersIncrementer: Job 실행 전 JobParameters에 임의의 id key, value를 추가하여 같은 JobParameters로 여러번 Job을 실행할 수 있도록 한다.
  * 스프링 배치에서는 [RunIdIncrementer](https://github.com/spring-projects/spring-batch/blob/main/spring-batch-core/src/main/java/org/springframework/batch/core/launch/support/RunIdIncrementer.java) 구현체를 제공한다.
* 만일 validator 설정이 있다면, 순수하게 클라이언트로부터 넘어온 JobParameters에 대해 한번 검증하고, Incrementer에 의해 변경된 JobParameters에 대해서도 다시 한번 검증한다.

#### job 실행 시 설정 적용 순서 (스프링 배치 4 기준)

1. validator를 통해 JobParameters 유효성 검증
2. 기존에 실행된 JobExecution이 있는지 확인 및 재실행 가능 여부 확인
3. incrementer로 JobParameters 변경
4. validator를 통해 JobParameters 유효성 검증

### listener()

[JobExecutionListener](https://github.com/spring-projects/spring-batch/blob/main/spring-batch-core/src/main/java/org/springframework/batch/core/JobExecutionListener.java)를 통해 Job 실행 전/후로 수행해야 하는 로직을 설정한다.

## Job 실행하는 여러가지 방법

1. 스프링 부트 기동시 Job이 실행되지 않게 구성

```yaml
## application.yml
spring:
  batch:
    job:
      enable: false # default: true
```

2. 특정 Job만 실행하고 싶을 때
```yaml
## application.yml
  spring:
    batch:
      job:
        name: jobName1,JobName2...
```

3. REST API 방식으로 Job 실행하기

```java

@RestController
public static class JobLaunchingController {
    
  @Autowired
  private JobLauncher jobLauncher;
  
  @Autowired
  private ApplicationContext context;
  
  @PostMapping("/run")
  public ExitStatus runJob(@RequestBody JobLaunchRequest request) {
      
    Job job = this.context.getBean(request.getName(), Job.class);
    return this.jobLauncher.run(job, request.getJobParameters());
    getExitStatus();
      
  }

}

@AllArgsConstructor
@Getter
public static class JobLaunchRequest {

    private String name;
    private Properties jobParameters;

    public getJobParameters() {
        Properties properties = new Properties();
        properties.putAll(this.jobParameters);
        return new JobParametersBuilder(properties)
            .toJobParameters();
    }
}
```

## Job 중지하기

- 자연스러운 종료
  - 모든 Step이 COMPLETED을 반환하면, JOB 자신도 COMPLETED 종료 코드 반환
- 인위적인 개입이 필요한 순간
  - 로직에 오류가 있어 중단해야 하는 경우
  - How?
    - StepExecution.setTerminateOnly()
      - ExitStatus.STOPPED
    - throw Exception을 발생 
      - ExitStatus.FAILED
    - STOPPED vs FAILED
      - STOPPED는 그대로 종료?
      - FAILED는 청크 단위로 rollback과 완료된 청크는 Skip
        - Example
          - chunk1 ◼︎◼︎◼︎◼︎◼︎
          - chunk2 ◼︎◼︎◼︎☒◻
          - chunk3 ◻︎◻︎◻︎◻︎◻︎
        - 예를들어 chunk2의 4번째 Item에서 Exception이 발생하는 경우
        - chunk2의 1~4는 Rollback되며 재 실행시, chunk1은 skip, chunk2의 첫 번째 item부터 실행
