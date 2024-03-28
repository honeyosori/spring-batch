# Spring Batch Test

### 0. 구성
``` java
@SpringBatchTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes=SimpleJobConfig.class)
public class SimpleJobConfigTestss { ... }
```

- `@Runwith(SpringRunner.class)` : 스프링 JUnit 기능을 사용하겠다는 표시 (JUnit4)
  - `@ExtendWith(SpringExtension.class)` (JUnit5)
- `@contextConfiguration` : ApplicationContext에 설정할 리소스를 명시
- `@SpringBatchTest`: JobLauncherTestUtils , JobRepositoryTestUtils를 포함한 스프링 배치 테스트 유틸리티를 주입할 수 있다.
  - JobLauncherTestUtils: 잡이나 스텝을 실행하는 인스턴스
  - JobRepositoryTestUtils: JobRepository에서 JobExecutions를 생성하는 데 사용 
  - JobScopeTextExecutionListener/StepScopeTestExecutionListener: 스텝 스코프와 잡 스코프 빈을 테스트하는 데 사용


### 1. 잡 테스트
```java
@SpringBatchTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes=SkipSampleConfiguration.class)
public class SkipSampleFunctionalTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    private SimpleJdbcTemplate simpleJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    @Test
    public void testJob() throws Exception {
        simpleJdbcTemplate.update("delete from CUSTOMER");
        for (int i = 1; i <= 10; i++) {
            simpleJdbcTemplate.update("insert into CUSTOMER values (?, 0, ?, 100000)",
                                      i, "customer" + i);
        }

        JobExecution jobExecution = jobLauncherTestUtils.launchJob();


        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }
}
```
- 데이터베이스에서 데이터를 조회하고 플랫(flat) 파일에 쓰는 배치 job의 테스트 예시
- `JobLauncherTestUtils` 클래스가 제공하는 `launchJob()` 메서드로 Job 을 실행한다.
- JobParameters를 인자로 전달할수도 있다.
- `launchJob()` 메서드가 리턴하는 JobExecution 객체로 Job의 상태 를 검증할 수 있다. 여기선 Job이 "COMPLETED" 상태로 끝나는 걸 검증한다

### 2. 스텝 테스트
```java
JobExecution jobExecution = jobLauncherTestUtils.launchStep("loadFileStep");
```
- 배치 잡이 복잡한 경우, 각 스텝을 개별적으로 테스트하는 것이 좋다.
- `launchStep()`: `JobLauncherTestUtils` 클래스가 제공하는 스텝 이름을 받아 실행하는 메서드

### 3. 잡과 스텝 스코프 빈 테스트
- step 스코프로 선언되어 step이나 job 컨텍스트에 나중에 바인딩(late binding)되는 컴포넌트의 테스트는 실제로 step이 실행 중인 것처럼 컨텍스트를 생성하지 않으면 독립적으로 테스트하기 까다롭다.
- 스프링 배치에는 이를 위한 두 가지 컴포넌트를 제공한다.
  - StepScopeTestExecutionListener 와 StepScopeTestUtils
- 아래처럼 이 리스너를 클래스 레벨에 선언하면 각 테스트 메소드마다 step execution 컨텍스트를 만든다:

```java
@ContextConfiguration
@TestExecutionListeners( { 
        DependencyInjectionTestExecutionListener.class, // Dependency Injection
        StepScopeTestExecutionListener.class // Step Scope
})
@RunWith(SpringRunner.class)
public class StepScopeTestExecutionListenerIntegrationTests {

    // This component is defined step-scoped, so it cannot be injected unless
    // a step is active...
    @Autowired
    private ItemReader<String> reader;

  /**
   * Create a step execution and bind it to the reader
   * 해당 메서드가 정의 되어있지 않으면 디폴트 StepExecution을 반환.
   * @return a step execution
   */
  public StepExecution getStepExecution() {
        StepExecution execution = MetaDataInstanceFactory.createStepExecution();
        execution.getExecutionContext().putString("input.data", "foo,bar,spam");
        return execution;
    }

    @Test
    public void testReader() {
        // The reader is initialized and bound to the input data
        assertNotNull(reader.read());
    }
}
```

- 4.1.0 버전부터는 `@SpringBatchTest` 어노테이션을 사용하면 `StepScopeTestExecutionListener` 와 `JobScopeTestExecutionListener` 가 자동으로 등록된다.
```java
@SpringBatchTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class StepScopeTestExecutionListenerIntegrationTests {...}
```

- 리스너를 쓰는 게 편리하지만 StepScopeTestUtils 클래스를 사용해서도 StepExecution을 생성하고 바인딩할 수 있다.
```java
int count = StepScopeTestUtils.doInStepScope(stepExecution,
    new Callable<Integer>() {
      public Integer call() throws Exception {

        int count = 0;

        while (reader.read() != null) {
           count++;
        }
        return count;
    }
});
```

### 4. 결과 검증


### 5. 배치 도메인 모킹


참고 자료:
https://godekdls.github.io/Spring%20Batch/unittesting/