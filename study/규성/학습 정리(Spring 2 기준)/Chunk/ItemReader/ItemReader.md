# ItemReader

## 기본 개념

- 다양한 입력(플랫 파일, DB, XML, JSON, RabbitMQ, Custom Reader)으로부터 데이터를 읽어서 제공하는 인터페이스
- ChunkOrientedTasklet 실행 시 필수적 요소로 설정해야한다.

## 구조

![image](https://github.com/honeyosori/spring-batch/assets/53935439/da96d032-2942-4bc0-bea5-29c1b97ff1f4)

- 다수의 구현체들이 ItemReader 와 ItemStream 을 동시에 구현하고 있음
  - 파일의 스트림을 열거나 DB 커넥션을 열거나 종료, 입력 장치 초기화 등의 작업을 수행
  - ExecutionContext 이 read 와 관련된 여러가지 상태 정보를 저장해서 재식작 시 다시 참조하도록 지원

> 위와 같이 다양한 입력을 읽어들이기 위한 다양한 구현체들이 존재한다. (but QueryDSL 미지원)

