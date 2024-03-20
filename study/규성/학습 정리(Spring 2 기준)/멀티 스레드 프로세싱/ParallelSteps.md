# Parallel Steps

## 기본 개념
- SplitState 를 사용해서 여러 개의 Flow 들을 병렬적으로 실행하는 구조
- 실행이 다 완료된 후 FlowExecutionStatus 결과들을 취합해서 다음 단계 결정을 한다.

![image](https://github.com/honeyosori/spring-batch/assets/53935439/f03ab64c-2b40-4c0c-959d-69219c82131f)

## SplitState
![image](https://github.com/honeyosori/spring-batch/assets/53935439/632ef479-ab73-419d-845b-1aad70992c58)
![image](https://github.com/honeyosori/spring-batch/assets/53935439/c76eb494-d9d1-411e-81b6-58ff5caaef9e)
> 추가한 Flow 개수 만큼 Flow 에 설정된 (Step, Decider, Flow) 객체들을 실행한다. 이 떄 
> taskExecutor.excute()를 통해 Thread 들이 개별적으로 작업을 맡아 실행하게 된다.

## 흐름 살펴보기

![image](https://github.com/honeyosori/spring-batch/assets/53935439/b9edfacb-2b5e-4a9a-84a2-903d4105df9c)

> TaskExecutor 를 통해 실행되는 Thread 들은 병렬,독립적으로 실행된다. 각각의 Thread 들이 실행된 결과는 FlowExecution 객체에 담겨서
> 반환이 되고 FlowExecutionAggregator 가 최종 상태값(COMPLETE, STOPPED, FAILED, UNKNOWN) 을 반환하여 다음 Step 을 결정하도록 한다.

### FlowExecution & FlowExecutionStatus

```java
public class FlowExecution implements Comparable<FlowExecution> {

	private final String name;
	private final FlowExecutionStatus status;

	/**
	 * @param name the flow name to be associated with the FlowExecution.
	 * @param status the {@link FlowExecutionStatus} to be associated with the FlowExecution.
	 */
	public FlowExecution(String name, FlowExecutionStatus status) {
		this.name = name;
		this.status = status;
	}

	/**
	 * @return the name of the end state reached
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the FlowExecutionStatus
	 */
	public FlowExecutionStatus getStatus() {
		return status;
	}

	/**
	 * Create an ordering on {@link FlowExecution} instances by comparing their
	 * statuses.
	 *
	 * @see Comparable#compareTo(Object)
	 *
	 * @param other the {@link FlowExecution} instance to compare with this instance.
	 * @return negative, zero or positive as per the contract
	 */
	@Override
	public int compareTo(FlowExecution other) {
		return this.status.compareTo(other.getStatus());
	}

	@Override
	public String toString() {
		return String.format("FlowExecution: name=%s, status=%s", name, status);
	}

}
```

```java
public class FlowExecutionStatus implements Comparable<FlowExecutionStatus> {

    /**
     * Special well-known status value.
     */
    public static final FlowExecutionStatus COMPLETED = new FlowExecutionStatus(Status.COMPLETED.toString());

    /**
     * Special well-known status value.
     */
    public static final FlowExecutionStatus STOPPED = new FlowExecutionStatus(Status.STOPPED.toString());

    /**
     * Special well-known status value.
     */
    public static final FlowExecutionStatus FAILED = new FlowExecutionStatus(Status.FAILED.toString());

    /**
     * Special well-known status value.
     */
    public static final FlowExecutionStatus UNKNOWN = new FlowExecutionStatus(Status.UNKNOWN.toString());

}
```

## API

![image](https://github.com/honeyosori/spring-batch/assets/53935439/b97261c1-8f04-4d91-bd7a-071eadd96419)

1. Flow 생성
2. Flow 2 와 Flow 3 를 생성하고 총 3 개의 Flow 를 합친다.
3. Flow 4 은 split 처리가 완료된 후 실행된다. (main Thread)

> Flow 1,2,3 모두 병렬 실행되며, 4는 위의 모든 Flow 에 담긴 작업이 끝난 뒤 main Thread 를 통해 실행된다.

## 예제 코드

```java

@RequiredArgsConstructor
@Configuration
public class ParallelStepConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    
    @Bean
    public Job job() {
        return jobBuilderFactory.get("batchJob")
                .incrementer(new RunIdIncrementer())
                .start(flow1())
//                .next(flow2())
                .split(taskExecutor()).add(flow2()) // 여러 개의 flow 추가 가능, 병렬 실행
                .end() // job 마무리
                .listener(new StopWatchJobListener())
                .build();
    }
    
    @Bean
    public Flow flow1() {

        TaskletStep step = stepBuilderFactory.get("step1")
                .tasklet(tasklet()).build();

        return new FlowBuilder<Flow>("flow1")
                .start(step)
                .build();
    }

    @Bean
    public Flow flow2() {

        TaskletStep step2 = stepBuilderFactory.get("step2")
                .tasklet(tasklet()).build();

        TaskletStep step3 = stepBuilderFactory.get("step3")
                .tasklet(tasklet()).build();

        return new FlowBuilder<Flow>("flow2")
                .start(step2)
                .next(step3)
                .build();
    }

    @Bean
    public Tasklet tasklet() {
        return new CustomTasklet();
    }

    @Bean
    public TaskExecutor taskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setThreadNamePrefix("async-thread-");
        return executor;
    }
}

```

```java
@Slf4j
@Component
public class CustomTasklet implements Tasklet {

    private long sum;
    private Object lock = new Object();

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // synchronized (lock){ // 동기화
        //
        // }
        for(int i = 0; i<1000000000; i++){
            sum++;
        }

        log.info("{} has been executed on thread {}", chunkContext.getStepContext().getStepName(), Thread.currentThread().getName());
        log.info("sum : {}", sum);
        return RepeatStatus.FINISHED;
    }
}

```

- 총 2개의 Thread 가 추가로 생성되어 flow1,2를 병렬로 실행시키며 각 Flow 안에 담긴 Step 들을 실행하게 된다.
- Flow 1 의 경우 Step 1, Flow 2 의 경우 Step 2, 3 가 각각 실행된다.
- 이 때, 실행할 작업(Tasklet)이 Bean 으로 생성되어 싱글톤으로 관리된다.
- 공유 자원으로서 별도의 처리가 없다면 모든 Thread 가 동시에 접근할 수 있다.
- 즉, 동시성 문제가 발생할 수 있다.

### 병렬 처리 X
![image](https://github.com/honeyosori/spring-batch/assets/53935439/082fbcc3-fcc7-45e8-942c-dd8ec77096af)
### 병렬 처리 O & Thread 점유 시간 김
![image](https://github.com/honeyosori/spring-batch/assets/53935439/e492dc84-f3ff-46c5-a5f1-93915746aaba)
### 병렬 처리 O & Thread 점유 시간 짦음
![image](https://github.com/honeyosori/spring-batch/assets/53935439/1d7846ae-3d09-4bf8-8083-82a9ca42745c)

> 해결할 수 있는 방법들 중 가장 간단하게 생각해볼 수 있는 방법은 syncronized 키워드를 통해 동기화 처리를 통해 해주는 것이다.
> 하지만 이럴 경우 정합성은 보장되지만 성능상의 손해를 보게된다.