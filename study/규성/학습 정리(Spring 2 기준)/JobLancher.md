# JobLauncher 

## 1. 기본 개념

- 배치 Job 을 실행시키는 역할을 수행한다.
- Job 과 Job Parameters 를 인자로 받으며, 요청된 배치 작업을 수행한 후 최종 Client 에게 JobExecution 을 반환한다.
- 스프링 부트 배치가 구동이 되면 JobLauncher 빈이 자동 생성된다.
- Job 실행
- JobLaucher 의 run(Job, JobParameters) 로 동작
  - JobLauncherApplicationRunner 가 JobLauncher 를 실행시킴
  - 동기적 수행
  - 비동기적 수행

## 2. 동작 

![image](https://github.com/honeyosori/spring-batch/assets/53935439/a5363bb4-48f9-4395-ba19-2acb646fa77d)

- 동기적 실행
  - taskExecutor 를 SyncTaskExecutor 로 설정할 경우(Default)
  - JobExecutor 를 획득하고 배치 처리를 최종 완료한 이후 Client 에게 JobExecution 을 반환
  - 스케줄러에 의한 배치 처리에 적합(배치 처리 시간이 길어도 상관 없을 경우)
- 비동기적 실행
  - taskExecutor 가 SimpleAsyncTaskExecutor 로 설정할 경우
  - JobExecution 을 획득한 후 Client 에게 바로 JobExecution(상태 : UNKNOWN) 반환
  - HTTP 요청에 의한 배치 처리에 적합함(배치 처리 시간이 길 경우 문제가 생기는 경우)

## 3. 직접 코드로 살펴보기

### JobLauncher
![image](https://github.com/honeyosori/spring-batch/assets/53935439/7eafe0ef-85c4-4d07-83c1-6d1a015ef770)

### JobLauncherApplicationRunner
![image](https://github.com/honeyosori/spring-batch/assets/53935439/fbc98426-4180-41da-8865-ac4d84ccab87)
![image](https://github.com/honeyosori/spring-batch/assets/53935439/1bca4e0e-5928-457f-bdb8-7ff0b3f6595e)

위와 같이 execute() 메서드 내에서 이벤트 기반으로 실행되는 것을 확인할 수 있다.

JobLauncherApplicationRunner 의 경우 스프링 부트의 초기 Configuration 클래스 중 하나인
BatchAutoConfiguration 에서 다음과 같이 초기화되어 빈으로 등록된다.

### BatchAutoConfiguration
![image](https://github.com/honeyosori/spring-batch/assets/53935439/7afbf85a-b349-4f7b-ba32-2e9cd3776658)