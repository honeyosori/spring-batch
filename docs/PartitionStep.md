## 1. PartitionStep

### 1.1. 개요

> - MasterStep이 SlaveStep을 여러 개 실행시키는 방식
> - MasterStep은 PartitionStep으로 구현한다.

### 1.2. 구성요소

#### 1.2.1. PartitionStep
- 파티셔닝 기능을 수행하는 Step
- PartitionHandler를 사용해 Worker step을 실행시키고, StepExecutionAggregator를 사용해 Worker step의 결과를 집계한다.

#### 1.2.2. PartitionHandler
- Worker step을 실행시키는 역할을 수행한다.
- 대표적인 구현체로 TaskExecutorPartitionHandler, MessageChannelPartitionHandler 제공
  - [TaskExecutorPartitionHandler](https://github.com/spring-projects/spring-batch/blob/main/spring-batch-core/src/main/java/org/springframework/batch/core/partition/support/TaskExecutorPartitionHandler.java): 각 StepExecution을 로컬 JVM에서 멀티스레드로 실행시키는 방식
  - MessageChannelPartitionHandler: 각 StepExecution을 원격 JVM으로 실행시키는 방식

#### 1.2.3. StepExecutionSplitter
- 파티셔닝된 StepExecution을 생성하는 역할을 수행하며 gridSize(=worker 개수)만큼 StepExecution을 생성한다.
- Partitioner에서 생성된 ExecutionContext를 StepExecution에 매핑한다.

#### 1.2.4. Partitioner
- StepExecution에서 사용할 ExecutionContext를 생성한다.
- 기본 구현체로 SimplePartitioner 제공

### 1.3. 코드
```java
public class PartitionStep extends AbstractStep {
    
    @Override
    protected void doExecute(StepExecution stepExecution) throws Exception {
        stepExecution.getExecutionContext().put(STEP_TYPE_KEY, this.getClass().getName());

        // Wait for task completion and then aggregate the results
        Collection<StepExecution> executions = partitionHandler.handle(stepExecutionSplitter, stepExecution);
        stepExecution.upgradeStatus(BatchStatus.COMPLETED);
        stepExecutionAggregator.aggregate(stepExecution, executions);

        // If anything failed or had a problem we need to crap out
        if (stepExecution.getStatus().isUnsuccessful()) {
            throw new JobExecutionException("Partition handler returned an unsuccessful step");
        }
    }
}
```

## 2. SynchronizedItemStreamReader

### 2.1. 개요

> - Thread-safe 하지 않은 ItemReader를 Thread-safe하게 만들어주는 래퍼 클래스
> - Spring Batch 4.0부터 추가됨
> - SynchronizedItemStreamReader와 SynchronizedItemStreamWriter 제공

### 2.2. 코드
```java
public class SynchronizedItemStreamReader<T> implements ItemStreamReader<T>, InitializingBean {
    private ItemStreamReader<T> delegate;

    @Nullable
    public synchronized T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return this.delegate.read();
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.delegate, "A delegate item reader is required");
    }
}
```

### 참고 자료
* https://docs.spring.io/spring-batch/reference/scalability.html
* https://godekdls.github.io/Spring%20Batch/springbatchintegration/
* https://jojoldu.tistory.com/493
