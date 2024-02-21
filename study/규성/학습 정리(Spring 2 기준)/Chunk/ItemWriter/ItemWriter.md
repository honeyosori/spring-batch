# ItemWriter

## 기본 개념

- Chunk 단위로 데이터를 받아 일괄 출력 작업을 위한 인터페이스
- 아이템 하나가 아닌 아이템 전체 리스트를 전달받는다.
- ChunkOrientedTasklet 실행 시 필수적으로 설정해야한다.

## 구조

![image](https://github.com/honeyosori/spring-batch/assets/53935439/deb9dbf8-4c33-45bd-bab6-89284b7f9d6c)

- 다수의 구현체들이 ItemWriter 와 ItemStream 을 동시에 구현하고 있다.
  - 파일의 스트림을 열거나 종료, DB 커넥션을 열거나 종료, 출력 장치 초기화 등의 작업
- 보통 ItemReader 구현체와 1:1 대응 관계인 구현체들로 구성되어 있다.


