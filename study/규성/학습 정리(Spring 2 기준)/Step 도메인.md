# Step 

## 1. Step

### 1.1 배치 실행 흐름

![image](https://github.com/honeyosori/spring-batch/assets/53935439/d377ffb1-7fd3-4f41-adf7-3c3fc9b63808)

### 1.2 Step 기본 개념(역할)

- 모든 Job 은 하나 이상의 Step 으로 구성됨
- Job 의 세부 작업을 Task 기반으로 설정하고 명세해놓은 객체
- Batch Job 을 구성하는 독립적인 하나의 단계
- 배치 처리를 정의하고 컨트롤하는데 필요한 모든 정보를 가지고 있는 도메인 객체

### 1.3 기본 구현체 

- TaskletStep
  - 가장 기본이 되는 클래스로 Tasklet타입의 구현체들을 제어한다.
- PartitionStep
  - 멀티 스레드 방식으로 Step 을 여러 개로 분리해서 실행한다.
- JobStep
  - Step 내에서 Job 을 실행하도록 한다.
- FlowStep
  - Step 내에서 Flow 를 실행하도록 한다.

### 1.4 직접 코드 기반으로 구조 살펴보기

#### 구조도 
![image](https://github.com/honeyosori/spring-batch/assets/53935439/2f7e9da3-cc17-4a7d-bcd5-831eaae817cf)

#### Step
![image](https://github.com/honeyosori/spring-batch/assets/53935439/f03785ef-a433-4af2-a6e5-9807e48db7ea)

#### AbstractStep
![image](https://github.com/honeyosori/spring-batch/assets/53935439/746349f5-9f16-4cf9-a3e5-6892ed8b0abb)
![image](https://github.com/honeyosori/spring-batch/assets/53935439/c2bb05aa-50ef-476c-961b-bdb1c6c79546)

#### JobStep
![image](https://github.com/honeyosori/spring-batch/assets/53935439/5efe3963-6c8e-4395-9e1d-974582844d25)

### 1.5 더 나아가보기

## 2. StepExecution

### 2.1 기본 개념

- Step 에 대한 한번의 시도를 의미하는 객체
- Step 실행 중에 발생한 정보들을 저장하고 있는 객체
  - 속성 : 시작시간, 종료시간, 상태(시작/완료/실패), commit count, rollback count
- Step 이 시도될 때마다 생성되며 각 Step 별로 생성
- Job 이 재시작을 하더라도 이미 성공적으로 완료된 Step 은 재실행 되지 않고 실패한 Step 만 실행
  - Step 이 실패해서 현재 Step 을 실행하지 않는다면 StepExecution 을 생성하지 않는다.
  - 단, 일단 실행된다면 생성됨
  - Step 의 StepExecution 이 모두 정상 완료되어야 JobExecution 이 정상 완료됨
  - Step 의 StepExecution 중 하나라도 실패하면 JobExecution 을 실패함


### 2.2 직접 코드 기반으로 살펴보기

#### StepExecution
![image](https://github.com/honeyosori/spring-batch/assets/53935439/8c06f968-87f4-46eb-9a8b-0c05ebbe3696)

### 더 나아가보기

## 3. StepContribution

### 3.1 기본 개념

- 청크 프로세스의 변경 사항을 버퍼링 한 후 StepExecution 상태를 업데이트하는 도메인 객체
- 청크 커밋 직전에 StepExecution 의 apply 메서드를 호출하여 상태를 업데이트함
- ExitStatus 의 기본 종료코드 외 사용자 정의 종료코드를 생성해서 적용할 수 있음

### 3.2 실행 흐름

![image](https://github.com/honeyosori/spring-batch/assets/53935439/c00e5189-de8c-40ff-a948-688aae4fee18)

> StepExecution 이 완료되는 시점에 apply 메서드를 호출하여 속성들의 상태를 최종 업데이트한다.

### 3.3 직접 코드 기반으로 살펴보기

### StepContribution
![image](https://github.com/honeyosori/spring-batch/assets/53935439/4eb75dcf-8c0b-406e-93e3-512c47ed6025)

## 4. ExecutionContext

### 4.1 기본 개념 

- 프레임워크에서 유지 및 관리하는 키/값으로 된 컬렉션으로 StepExecution 또는 JobExecution 객체의 상태를 저장하는 공유 객체
- DB 에 직렬화 한 값으로 저장됨 (JSON 형태 / {"key" : "value"})
- 공유 범위
  - 
- Job 재 시작시 이미 처리한 row 데이터는 건너뛰고 이후로 수행하도록 할 떄 상태 정보를 활용한다.

### 4.2 직접 코드 기반으로 살펴보기