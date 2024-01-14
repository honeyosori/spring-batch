# Step 도메인 이해

## Step

Job의 구성 요소로 구체적인 처리 프로세스를 Task 기반으로 정의한 도메인 객체이다.

### 구현체

- TaskletStep: 가장 기본적인 Step 구현체
- PartitionStep: 멀티 스레드 방식으로 하나의 Step을 여러 개로 분리해서 실행 (멀티 스레드 프로세싱 참고)
- JobStep: Step 내에서 Job을 실행하도록 함
- FlowStep: Step 내에서 Flow를 실행하도록 함

실행 단위에 따라 단일 Task 기반 Tasklet과 Chunk 기반 Tasklet으로 나뉜다. (Chunk 프로세싱 활용 참고)

## StepExecution

Step 실행 시 생성되며 Step 실행 중 발생한 정보들을 저장하고 있는 객체이다.

Job을 재시작했을 때 완료된 Step은 재실행하지 않고 실패한 Step만 재실행하여 StepExecution을 새로 생성한다.

### SimpleStepHandler.class

```java
protected boolean shouldStart(StepExecution lastStepExecution, JobExecution jobExecution, Step step) throws JobRestartException, StartLimitExceededException {
    BatchStatus stepStatus;
    if (lastStepExecution == null) {
        stepStatus = BatchStatus.STARTING;
    } else {
        stepStatus = lastStepExecution.getStatus();
    }

    if (stepStatus == BatchStatus.UNKNOWN) {
        throw new JobRestartException("Cannot restart step from UNKNOWN status. The last execution ended with a failure that could not be rolled back, so it may be dangerous to proceed. Manual intervention is probably necessary.");
    } else if ((stepStatus != BatchStatus.COMPLETED || step.isAllowStartIfComplete()) && stepStatus != BatchStatus.ABANDONED) {
        if (this.jobRepository.getStepExecutionCount(jobExecution.getJobInstance(), step.getName()) < step.getStartLimit()) {
            return true;
        } else {
            throw new StartLimitExceededException("Maximum start limit exceeded for step: " + step.getName() + "StartMax: " + step.getStartLimit());
        }
    } else {
        if (logger.isInfoEnabled()) {
            logger.info("Step already complete or not restartable, so no action to execute: " + lastStepExecution);
        }

        return false;
    }
}
```

### JobExecution과의 관계

- 1:M 관계
- StepExecution이 완료되어야 JobExecution이 완료되며, StepExecution이 하나라도 실패하면 JobExecution도 실패로 처리된다.

```jsx
create table if not exists spring_batch.BATCH_STEP_EXECUTION
(
    STEP_EXECUTION_ID  bigint        not null primary key,
    VERSION            bigint        not null,
    STEP_NAME          varchar(100)  not null,
    JOB_EXECUTION_ID   bigint        not null,
    START_TIME         datetime(6)   not null,
    END_TIME           datetime(6)   null,
    STATUS             varchar(10)   null,
    COMMIT_COUNT       bigint        null,
    READ_COUNT         bigint        null,
    FILTER_COUNT       bigint        null,
    WRITE_COUNT        bigint        null,
    READ_SKIP_COUNT    bigint        null,
    WRITE_SKIP_COUNT   bigint        null,
    PROCESS_SKIP_COUNT bigint        null,
    ROLLBACK_COUNT     bigint        null,
    EXIT_CODE          varchar(2500) null,
    EXIT_MESSAGE       varchar(2500) null,
    LAST_UPDATED       datetime(6)   null,
    constraint JOB_EXEC_STEP_FK
        foreign key (JOB_EXECUTION_ID) references spring_batch.BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
);
```

## StepContribution

Chunk 프로세스의 변경사항을 가지고 있는 도메인 객체이다.

청크 프로세스가 완료되기 직전에 StepExecution의 apply(StepContribution contribution) 메서드를 호출하여 StepContribution의 내용을 StepExecution에 반영한다.

StepExecution의 ExitStatus를 사용자가 원하는 값으로 설정할 수 있게 setExitStatus(ExitStatus status) 메서드를 제공한다.

### ExitStatus

- UNKNOWN: 알 수 없음
- EXECUTING: 진행중 (비동기 실행 시 사용됨)
- COMPLETED: 정상적으로 완료됨
- NOOP: 처리되지 않음
- FAILED: 실패함
- STOPPED: 중단됨