# JobRepository 

## 1. 개념

- 배치 작업 중의 정보를 저장하는 저장소
- 모든 metadata 를 저장(JOB 수행 시각/종료 시각/수행 횟수 등)
  - JobLauncher, Job, Step 의 구현체 내부에서 CRUD 기능을 처리함

![image](https://github.com/honeyosori/spring-batch/assets/53935439/c44786f5-eb74-4c51-b043-36cd7e372f3d)


## 2. 살펴보기

### 2.1 JobRepository
![image](https://github.com/honeyosori/spring-batch/assets/53935439/e8bb17f1-8530-4dc0-8f22-3263b28a9b01)

스프링 배치가 처음 실행될 때, 총 4개의 설정 클래스를 실행시킨다.

그 중, `SimpleBatchConfiguration` 에서 Bean 으로 등록이 되게 된다.

### 2.2 SimpleBatchConfiguration
![image](https://github.com/honeyosori/spring-batch/assets/53935439/eb293387-9d19-492c-816b-ae1eed9f3437)
![image](https://github.com/Sal-Mal/salmal-be/assets/53935439/36d6a29c-c162-419d-8e85-63a8044b5432)

## 3. 설정

- `@EnableBatchProcessing` 어노테이션만 선언하면 JobRepository 가 자동으로 빈으로 등록된다.
- BatchConfigurer 인터페이스를 구현하거나, BasicBatchConfigurer 를 상속해서 Jobrepository 의 설정을 커스터마이징 할 수도 있다.
- JDBC 혹은 In Memory 방식으로 설정할 수 있다.



