# ChunkProvider

## 기본 개념

- ItemReader 를 사용해서 소스로부터 아이템을 Chunk size 만큼 읽어 Chunk 단위로 만들어 제공하는 도메인 객체
- Chunk\<I> 를 만들고 내부적으로 반복문을 사용해 ItemReader.read() 를 계속 호출하면서 Item 을 Chunk 에 쌓는다.
- 외부로 부터 ChunkProvider 가 호출될 때마다 항상 새로운 Chunk 가 생성된다.
- 반복문 종료 시점
  - Chunk size 만큼 Item 을 읽으면 반복문 종료되고 CHunkProcessor 로 넘어간다.
  - ItemReader 가 읽은 Item 이 null 일 경우 반복문을 종료하고 해당 Step 반복문까지 종료
- 기본 구현체로서 SimpleChunkProvider 와 FaultTolerantChunkProvider 가 있음

## ChunkProvider(SimpleChunkProvider)

> ItemReader 를 이용해서 Item을 읽고 Chunk 에 저장하는 역할 수행

```java

public class SimpleChunkProvider<I> implements ChunkProvider<I> {
    
  
  @Override
  public Chunk<I> provide(final StepContribution contribution) throws Exception {

    final Chunk<I> inputs = new Chunk<>(); // Item 을 담을 Chunk 생성, provider() 호출마다 새롭게 생성
    repeatOperations.iterate(new RepeatCallback() { // Chunk 크기 만큼 반복문을 실행하면서 read() 호출

      @Override
      public RepeatStatus doInIteration(final RepeatContext context) throws Exception {
        I item = null;
        Timer.Sample sample = Timer.start(Metrics.globalRegistry);
        String status = BatchMetrics.STATUS_SUCCESS;
        try {
          item = read(contribution, inputs); // ItemReader 가 Item 한 개씩 읽어서 반환
        }
        catch (SkipOverflowException e) {
          status = BatchMetrics.STATUS_FAILURE;
          return RepeatStatus.FINISHED;
        }
        finally {
          stopTimer(sample, contribution.getStepExecution(), status);
        }
        if (item == null) { // 더 이상 읽은 Item 이 없는 null 일 경우 반복문 종료 및 전체 Chunk 프로세스 종료
          inputs.setEnd();
          return RepeatStatus.FINISHED;
        }
        inputs.add(item);
        contribution.incrementReadCount();
        return RepeatStatus.CONTINUABLE;
      }

    });

    return inputs;

  }
  
}
```