# ItemStream 

## 기본 개념
- ItemReader 와 ItemWriter 처리 과정 중 상태를 저장하고 오류가 발생하면 해당 상태를 참조하여 실패한 곳에서 재시작하도록 지원
- 리소스를 열고 닫아야 하며 입출력 장치 초기화 등의 작업을 해야하는 경우
- ExecutionContext 를 매개변수로 받아서 상태 정보를 업데이트한다.
- ItemReader, ItemWriter 는 ItemStream 을 함께 구현해야한다.

## 구조

![image](https://github.com/honeyosori/spring-batch/assets/53935439/ef76988d-5771-4181-8cc9-ad3101be8356)

- open() : read, writer 메서드 호출 전에 파일이나 커넥션에 필요한 리소스에 접근하도록 하는 초기화 작업
- update() : 현재까지 진행된 모든 상태를 저장
  - 데이터 : 100  / Chunk 크기 : 10 = 총 10 번 호출
- close() : 열려 있는 모든 리소스를 안전하게 해제하고 닫음