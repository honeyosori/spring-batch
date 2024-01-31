# StepExecution

## 개념

- Step 에 대한 한번의 시도를 의미하는 객체
- Step 실행 중에 발생한 정보들을 저장하고 있는 객체
    - 속성 : 시작시간, 종료시간, 상태(시작/완료/실패), commit count, rollback count
- Step 이 시도될 때마다 생성되며 각 Step 별로 생성
- Job 이 재시작을 하더라도 이미 성공적으로 완료된 Step 은 재실행 되지 않고 실패한 Step 만 실행
    - Step 이 실패해서 현재 Step 을 실행하지 않는다면 StepExecution 을 생성하지 않는다.
    - 단, 일단 실행된다면 생성됨
    - Step 의 StepExecution 이 모두 정상 완료되어야 JobExecution 이 정상 완료됨
    - Step 의 StepExecution 중 하나라도 실패하면 JobExecution 을 실패함

## 흐름 살펴보기

![image](https://github.com/Sal-Mal/salmal-be/assets/53935439/1d11b16a-2205-46c9-bdb4-fcc61be2eab8)

위 흐름도에서 보이듯이, StepExecution 의 BatchStatus 에 따라 JobExecution 의 상태가 결정된다. 즉, Job 을 구성하고 있는 Step 중
하나라도 실패할 경우, JobExecution 의 상태는 FAILED 로 기록되며 COMPLETED 가 될 때까지 재실행이 가능하다.

## StepExecution 코드로 살펴보기
![image](https://github.com/honeyosori/spring-batch/assets/53935439/8c06f968-87f4-46eb-9a8b-0c05ebbe3696)

## 그림으로 살펴보는 예시

![image](https://github.com/Sal-Mal/salmal-be/assets/53935439/00bd2d8e-0c04-439c-9d67-02e113997019)

정산관련 배치작업을 수행하며, JobParameter 에 날짜를 넣어 구분하고 있다. 먼저 배치작업을 시작함에 따라 성공 여부와 관계 없이 JobInstance 는
생성이 된다. 이후 JobExecution 의 경우 하위 StepExecution 의 상태가 모두 Completed 일 경우에만 Completed 로 설정되며 하나라도 완료되지 못한
작업이 있을 경우 실행 결과 상태가 FAILED 가 된다.