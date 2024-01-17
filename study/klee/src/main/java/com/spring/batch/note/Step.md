# Step
- Step은 독립적이며 자체적으로 배치 처리를 수행하여, 개발자는 필요에 따라 자유롭게 step을 조합하여 배치를 구성할 수 있다. 

## Step 유형
### Tasklet
- 단일로 수행될 커스텀한 기능을 개발할 때 사용

### Chunk
- 대량의 데이터를 처리할 때 사용
- ItemReader, ItemProcessor, ItemWriter를 사용
- ItemReader는 청크 단위로 데이터를 읽어 오고, ItemProcessor는 데이터를 가공하고, ItemWriter는 데이터를 출력한다.

## Step 구성
### Tasklet 기반 구성
- Tasklet 인터페이스 구현
  - RepeatStatus 객체 반환
    - RepeatStatus.FINISHED : 태스크릿 완료
    - RepeatStatus.CONTINUABLE : 해당 태스크릿 반복 실행
- CallableTaskletAdapter
  - Callable 구현체를 Tasklet으로 사용할 수 있도록 해주는 어댑터
  - 어댑터는 Callbable 객체의 call() 메서드를 호출하고, 반환된 결과를 반환한다.
  - Step의 특정 로직은 해당 step이 실해오디는 스레드가 아닌 다른 스레드에서 실행되어야 할 때 사용한다.
- MethodInvokingTaskletAdapter
  - 다른 클래스의 메서드를 Tasklet으로 사용할 수 있도록 해주는 어댑터
  - 메서드가 ExitStatus를 반환하지 않으면, ExitStatus.COMPLETED로 반환한다.
- SystemCommandTasklet
  - 시스템 명령어를 실행할 수 있도록 해주는 Tasklet
  - 비동기로 실행

### Chunk 기반 구성
#### 청크 크기 구성
- 하드 코딩
- CompletionPolicy
  - Chunk의 완료 여부를 결정할 수 있는 결정 로직 구현
    - SimpleCompletionPolicy
      - 완료할 아이템 수를 지정
    - TimeoutTerminationPolicy
      - 지정된 시간이 지나면 완료
    - CompositeCompletionPolicy
      - 여러 CompletionPolicy를 조합하여 사용 (하나라도 완료되면 청크 완료)


## Step Listener
- Step의 실행 전, 후 특정 로직 실행
- StepExecutionListener 인터페이스
  - beforeStep() : Step 실행 전
  - afterStep() : Step 실행 후
  - @BeforeStep, @AfterStep 어노테이션 사용
- ChunkListener 인터페이스
  - beforeChunk() : Chunk 실행 전
  - afterChunk() : Chunk 실행 후
  - @BeforeChunk, @AfterChunk 어노테이션 사용





  
