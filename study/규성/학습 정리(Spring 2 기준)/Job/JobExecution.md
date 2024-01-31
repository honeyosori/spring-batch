# JobExecution

## 개념

- JobInstance 에 대한 한번의 시도를 의미하는 객체로서 Job 실행 중에 발생한 정보들을 저장하고 있는 객체
  - 시작시간, 종료시간, 상태(시작, 완료, 실패, 종료) 등의 속성을 가짐
- JobInstance 와의 관계
  - JobExecution 은 FAILED 또는 COMPLETED 등의 Job 의 실행 결과 상태를 가지고 있음
  - JobExecution 의 실행 상태 결과가 COMPLETED 이면 JobInstance 실행이 완료된 것으로 간주하기 때문에 재실행이 불가능함
  - JobExecution 의 실행 상태 결과가 FAILED 이면 재실행이 가능함
    - JobParameter 가 동일한 값으로 Job 을 실행할지라도 실패했으므로 실행할 수 있음
  - 정리하자면, JobExecution 의 실행 상태 결과가 COMPLETED 될 때까지 하나의 JobInstance 내에서 여러 번의 시도가 생길 수 있다.

## BATCH_JOB_EXECUTION 테이블

JobInstance 와 JobExecution 은 1:M 관계(Job실행 상태 결과에 따라 여러번 실행할 수 있음)로서 
JobExecution 은 JobInstance 에 대한 성공 및 실패 내역을 가지고 있다.

## JobExecution 도식도

![image](https://github.com/Sal-Mal/salmal-be/assets/53935439/02f71010-370f-4d99-97d8-50c1b669f559)

JobLauncher 가 실행 한 뒤, JobRepository 를 통해 DB 에 저장되어 있는 메타데이터 중 동일한 Job과 JobParameter 조합이 있는지 여부를
검사한 뒤, 없다면 새로운 JobInstance 및 JobExecution 을 생성(테이블 row 추가)하게 되지만, 만약 이미 동일한 조합이 과거에 실행 결과(BatchStatus)가 Completed 
로 마무리된 작업이라면 예외를 발생시킨다. 물론 이미 실행된 적이 있지만 Completed 가 아닌 다른 상태, 즉 재실행이 가능한 상태라면 예외가 발생하지 않으며 
정상적으로 실행이 된다.

## 흐름도

![image](https://github.com/Sal-Mal/salmal-be/assets/53935439/8b9ddaa4-b159-4163-99de-5e6bf5cefc3e)

### DB 메타데이터를 통해 알 수 있는 사실

위 흐름대로 실행이 됐다는 것을 가정하고, BATCH_JOB_EXECUTION 테이블을 들여다보면 다음과 같은 사실을 알 수 있다.

1. JobInstance A 의 경우 JobParameter 를 통해 21/01/01 에 실행된 배치 작업인데 단번에 성공했다.
2. JobInstance B 의 경우 21/01/02 에 실행된 배치 작업인데 한번 실패했고, 그 다음 작업 실행을 통해 성공했다.
