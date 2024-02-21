# ChunkProcessor 

## 기본 개념

- ItemProcessor 를 사용해서 Item 을 변형, 가공, 필터링하고 ItemWriter 를 사용해서 Chunk 데이터를 저장 및 출력한다.
- Chunk<O> 를 만들고 앞에서 넘어온 Chunk<I> 의 item 을 한건씩 처리후 Chunk<O> 에 저장한다.
- 외부로 부터 호출될때마다 항상 새로운 Chunk 가 생성된다.

## SimpleChunkProcessor

```java

public class SimpleChunkProcessor<I, O> implements ChunkProcessor<I>, InitializingBean {

    private ItemProcessor<? super I, ? extends O> itemProcessor;

    private ItemWriter<? super O> itemWriter;

    private final MulticasterBatchListener<I, O> listener = new MulticasterBatchListener<>();

    @Override
    public final void process(StepContribution contribution, Chunk<I> inputs) throws Exception {

        initializeUserData(inputs);

        if (isComplete(inputs)) {
            return;
        }
        Chunk<O> outputs = transform(contribution, inputs); // ItemProcessor 에서 가공처리 된 아이템을 담은 Chunk<O> 를 반환

        contribution.incrementFilterCount(getFilterCount(inputs, outputs)); // ItemProcessor 필터링 도니 아이템 개수 저장

        write(contribution, inputs, getAdjustedOutputs(inputs, outputs)); // 가공 처리도니 Chunk<O> 의 List<Item> 를 ItemWriter 에게 전달

    }

}

```