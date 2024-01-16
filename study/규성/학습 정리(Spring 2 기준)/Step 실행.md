# Step 실행

## 1. StepBuilderFactory

### 1.1 개념

- StepBuilder 를 생성하는 팩토리 클래스로서 get(String name) 메서드를 제공
- StepBuilderFactory.get("Step이름")

### 1.2 코드로 살펴보기

#### StepBuilderFactory
![image](https://github.com/honeyosori/spring-batch/assets/53935439/1d9071e8-6fd1-4b3d-a62e-09fe901c7ca8)

> 필드 변수로 설정된 jobRepository 의 경우, 위와 같이 Builder 클래스를 통해 생성되고 Step 에 전달되어 메타데이터를 기록하는데 사용됨

#### StepBuilder
![image](https://github.com/honeyosori/spring-batch/assets/53935439/0c74e74e-90e5-4749-b56a-0393c7dde4aa)

> StepBuilder 의 경우 위와 같이 정의된 설정 조건에 따라서 다섯 개의 하위 빌더 클래스를 생성하고 실제 Step 생성을 위임한다.

- TaskletStepBuilder(TaskletStep = 기본)
- SimpleStepBuilder(TaskletStep = 청크 기반 작업 수행 클래스 생성)
- PartitionStepBuilder(PartitionStep = 멀티 스레드 방식으로 Job 실행)
- JobStepBuilder(JobStep = Step 안에서 Job 실행)
- FlowStepBuilder(FlowStep = Step 안에서 Flow 실행)

![image](https://github.com/honeyosori/spring-batch/assets/53935439/00fd9667-68fc-4d95-8598-024b04bc9e4f)

정리해보자면 아래와 같은 구조로 API 의 파라미터 타입과 구분에 따라 적절한 하위 빌더가 생성이 되고, 
빌더를 통해 사용 목적에 따라 Step 의 구현체를 만들어 사용한다.

예시는 아래와 같다.
> Step -> AbstractStep -> TaskletStep

#### AbstractStep
![image](https://github.com/honeyosori/spring-batch/assets/53935439/e6fb9f23-5848-43bc-87ed-2fb2656d05a0)

#### TaskletStep
![image](https://github.com/honeyosori/spring-batch/assets/53935439/7334e2e3-7341-492a-b64e-675eee88abb0)

### 1.3 구조도

![image](https://github.com/honeyosori/spring-batch/assets/53935439/89e44d29-99a0-4c2b-bac2-852dd54458c0)

## 2. TaskletStep

### 2.1 개념

- 스프링 배치에서 제공하는 Step 의 구현체로 Tasklet 을 실행시키는 도메인 객체
- Task 기반과 Chunk 기반으로 나뉘어서 Tasklet 을 실행한다.
  - TaskletStepBuilder(기본/Task 기반)
    - 대량 처리의 경우 청크 기반에 비해 더 복잡한 구현이 필요
    - 단일 작업 기반으로 처리되는 것이 더 효율적일 경우 사용
  - SimpleStepBuilder(Chunk 기반)
    - 하나의 큰 덩어리를 여러개로 나눠 실행한다는 의미로 대량 처리를 하는 경우 효과적

### 2.2 Task 기반과 Chunk 기반 비교

![image](https://github.com/honeyosori/spring-batch/assets/53935439/89ccfb64-cc72-4ee2-bf3a-47a1f8abaae8)

#### 코드로 살펴보기

## 3. TaskletStep - tasklet()

### 3.1 개념

- Tasklet 타입의 클래스를 설정한다.
  - Tasklet
    - Step 내에서 구성되고 실행되는 도메인 객체로서, 주로 단일 테스트를 수행하기 위한 것
    - TaskletStep 에 의해 반복적으로 수행되며, 반환값에 따라 계속 수행 혹은 종료한다.
    - RepeatStatus - Tasklet 의 반복 여부 상태값
      - RepeatStatus.FINISHED (종료)
      - RepeatStatus.CONTINUABLE (반복)
      - 종료가 리턴되거나 실패 예외가 던져지기 전까지 TaskletStep 에 의해 while 문 안에서 반복적으로 호출됨(무한 루프에 주의)
- Step 에 오직 하나의 Tasklet 설정이 가능하며 두 개 이상을 설정했을 경우 마지막 객체가 실행된다.


### 3.2 구조도

![image](https://github.com/honeyosori/spring-batch/assets/53935439/73d478fa-24a5-4155-ac06-f548e8a91d78)
![image](https://github.com/honeyosori/spring-batch/assets/53935439/ef84df7f-1c5c-4e73-908a-79875cbb2da6)

## 4. JobStep 

### 4.1 개념

- Job 에 속하는 Step 중 외부의 Job 을 포함하고 있는 Step
- 외부의 Job 이 실패하면 해당 Step 이 실패하므로 결국 최종 Job 도 실패함
- 모든 메타데이터는 기본 Job 과 외부 Job 별로 각각 저장된다.
- 커다란 시스템을 작은 모듈로 쪼개고 Job 의 흐름을 관리하고자할 때 사용할 수 있다.

### 4.2 관계도


![image](https://github.com/honeyosori/spring-batch/assets/53935439/fb5af5b0-50a9-40ce-8191-e70ff049381d)
![image](https://github.com/honeyosori/spring-batch/assets/53935439/929d26e6-2f8c-4ad1-b847-41f6628fc27d)