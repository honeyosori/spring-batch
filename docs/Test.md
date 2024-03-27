## 1. Spring Batch Test

### 1.1. @SpringBatchTest

- Spring Batch 4.1 버전부터 제공
- 테스트에 필요한 다양한 유틸 클래스들을 빈으로 등록해준다.

### 1.2. Util Class

- **JobLauncherTestUtils**: launchJob(), launchStep() 등 테스트할 Job 또는 Step 실행 지원
- **JobRepositoryTestUtils**: JobRepository를 사용해 데이터베이스에 저장된 JobExecution 생성/삭제 지원
- **StepScopeTestExecutionListener**: 단위 테스트 시 @StepScope 컨텍스트 생성. 해당 컨텍스트를 통해 JobParameter 등을 단위 테스트에서 DI 받을 수 있다.
- **JobScopeTestExecutionListener**: 단위 테스트 시 @JobScope 컨텍스트 생성. 해당 컨텍스트를 통해 JobParameter 등을 단위 테스트에서 DI 받을 수 있다.

### 1.2. 예제

Test 하고자 하는 Job
```java
@Configuration
@RequiredArgsConstructor
public class TestJobConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final JobRepositoryListener listener;

	@Bean
	public Job job() {
		return jobBuilderFactory.get("testJob")
				.start(step())
				// validator가 있으면 테스트 실패
				// .validator(new DefaultJobParametersValidator(new String[]{"name"}, new String[]{"run.id"}))
				.preventRestart()
				.incrementer(new RunIdIncrementer())
				.listener(listener)
				.build();
	}

	@Bean
	public Step step() {
		return stepBuilderFactory.get("testStep")
				.tasklet((StepContribution stepContribution, ChunkContext chunkContext) -> {
					System.out.println("step was executed");
					return RepeatStatus.FINISHED;
				})
				.build();
	}
}
```

테스트 Configuration
```java
@Configuration
@EnableAutoConfiguration
@EnableBatchProcessing
public class BatchTestConfig {
}
```

테스트
```java
@SpringBatchTest
@SpringBootTest(classes = { TestJobConfiguration.class, JobRepositoryListener.class, BatchTestConfig.class })
public class JobTest {

	@Autowired
	JobLauncherTestUtils jobLauncherTestUtils;

	@Test
	void jobTest() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
				.addString("name", "soob1")
				.toJobParameters();

		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

		assertEquals(jobExecution.getStatus(), BatchStatus.COMPLETED);
	}
}
```

## 2. 운영

### 2.1. JobExplorer

* JobRepository에서 조회 기능만 제공 (Read-only)

```java
public interface JobExplorer {
    List<JobInstance> getJobInstances(String var1, int var2, int var3);

    @Nullable
    default JobInstance getLastJobInstance(String jobName) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    JobExecution getJobExecution(@Nullable Long var1);

    @Nullable
    StepExecution getStepExecution(@Nullable Long var1, @Nullable Long var2);

    @Nullable
    JobInstance getJobInstance(@Nullable Long var1);

    List<JobExecution> getJobExecutions(JobInstance var1);

    @Nullable
    default JobExecution getLastJobExecution(JobInstance jobInstance) {
        throw new UnsupportedOperationException();
    }

    Set<JobExecution> findRunningJobExecutions(@Nullable String var1);

    List<String> getJobNames();

    List<JobInstance> findJobInstancesByJobName(String var1, int var2, int var3);

    int getJobInstanceCount(@Nullable String var1) throws NoSuchJobException;
}
```

### 2.2. JobRegistry


### 2.3. JobOperator


---
### 참고 자료

- https://www.baeldung.com/spring-batch-testing-job
- https://jojoldu.tistory.com/455
