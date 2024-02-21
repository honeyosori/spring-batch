# 청크 프로세스 이해

## 1. Chunk

Chunk란? 여러 개의 아이템 덩어리를 의미한다.

한번에 전체 아이템을 처리하는게 아니라, 아이템을 특정 개수로 나눠 처리하고 싶을 때 청크 프로세스를 사용한다.

## 2. ChunkOrientedTasklet

### 2-1. 개념
* Tasklet 구현체 중 하나로 Chunk 기반 프로세싱 기능 제공
* Chunk 단위로 트랜잭션 관리 (특정 Chunk에서 에러가 발생했을 때 이전 Chunk까지는 커밋이 완료된 상태로 롤백되지 않음)
* ChunkProvider와 ChunkProcessor로 구성

```java
public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    Chunk<I> inputs = (Chunk)chunkContext.getAttribute("INPUTS");
    if (inputs == null) {
        inputs = this.chunkProvider.provide(contribution);
        if (this.buffering) {
            chunkContext.setAttribute("INPUTS", inputs);
        }
    }

    this.chunkProcessor.process(contribution, inputs);
    this.chunkProvider.postProcess(contribution, inputs);
    if (inputs.isBusy()) {
        logger.debug("Inputs still busy");
        return RepeatStatus.CONTINUABLE;
    } else {
        chunkContext.removeAttribute("INPUTS");
        chunkContext.setComplete();
        if (logger.isDebugEnabled()) {
            logger.debug("Inputs not busy, ended: " + inputs.isEnd());
        }

        return RepeatStatus.continueIf(!inputs.isEnd());
    }
}
```
#### ChunkOrientedTasklet이 실행될 때
* "INPUTS" key로 Chunk 데이터를 chunkContext에 캐싱해두고 재시도 시 캐싱해 둔 데이터를 사용한다. 해당 Chunk에 대한 작업이 완료되면 캐싱해 둔 데이터는 삭제한다.

### 2-2. ChunkProvider

ItemReader를 사용해서 소스로부터 아이템을 청크 사이즈만큼 읽어와 Chunk 객체로 반환하는 인터페이스

#### 구현체
* SimpleChunkProvider
* FaultTolerantChunkProvider

### 2-3. ChunkProcessor

ItemProcessor를 사용해서 아이템을 변경, 필터링하고 ItemWriter를 사용해서 Chunk 객체를 출력, 저장한다.

### 3. ItemStream

* Chunk 프로세스의 상태를 저장하고 오류 발생 시 저장한 상태를 참조해 실패한 Chunk부터 재시작하도록 지원하는 인터페이스
* 대부분의 ItemReader, ItemWriter 구현체들이 ItemStream도 같이 구현하고 있음
* ItemProcessor는 ItemStream을 구현하지 않음
* 상태를 저장하기 위해 ExecutionContext 사용

```java
public interface ItemStream {
    // step 시작 시 호출, 저장된 상태를 읽어오거나 자원을 얻어올 때 사용
    void open(ExecutionContext var1) throws ItemStreamException;
	
    // 상태 저장
    void update(ExecutionContext var1) throws ItemStreamException;
	
    // step 종료 시 호출, 자원 반환 시 사용
    void close() throws ItemStreamException;
}
```

---
참고 자료

[Spring Batch 실패를 다루는 기술 - ItemStream](https://jgrammer.tistory.com/entry/Spring-Batch-%EC%8B%A4%ED%8C%A8%EB%A5%BC-%EB%8B%A4%EB%A3%A8%EB%8A%94-%EA%B8%B0%EC%88%A0-ItemStream)

