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