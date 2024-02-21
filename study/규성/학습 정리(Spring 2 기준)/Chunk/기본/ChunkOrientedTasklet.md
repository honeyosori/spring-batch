# ChunkOrientedTasklet

## 기본 개념

- ChunkOrientedTasklet 은 스프링 배치에서 제공하는 Chunk 지향 프로세싱을 담당하는 도메인 객체
- ItemReader, ItemWriter, ItemProcessor 를 사용해 Chunk 기반의 데이터 입출력 처리를 담당한다.
- TaskletStep 에 의해서 반복적으로 실행되며 ChunkOrientedTasklet 이 실행될 때마다 매번 새로운 트랜잭션이 생성되어 처리가 이루어진다.
- 예외 발생 시, 해당 Chunk 는 롤백 되며 이전에 커밋한 Chunk 의 경우 완료된 상태가 유지된다.
- 내부적으로 itemReader 를 핸들링하는 ChunkProvider 와 ItemProcessor, ItemWriter 를 핸들링하는 ChunkProcessor 타입의 구현체를 가진다.


![image](https://github.com/honeyosori/spring-batch/assets/53935439/839ab7f7-344e-46fe-b709-183b1c305697)

## 코드로 살펴보기

```java

public class ChunkOrientedTasklet<I> implements Tasklet {
    
    
    
    @Nullable
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        @SuppressWarnings("unchecked")
        // 1. 처리 중 예외가 발생하여 재 시도할 경우, 다시 데이터를 읽지 않고 버퍼에 담아 놓았던 데이터를 가지고 옴
        Chunk<I> inputs = (Chunk<I>) chunkContext.getAttribute(INPUTS_KEY); 
        
        if (inputs == null) {
            // 2. Item 을 Chunk size 만큼 박복해서 읽은 다음 Chunk<I> 에 저장하고 반환
            inputs = chunkProvider.provide(contribution);
            if (buffering) {
                // 3. Chunk 캐싱 -> ChunkContext 버퍼에 담음
                chunkContext.setAttribute(INPUTS_KEY, inputs);
            }
        }

        // 4. ChunkProvider 로 부터 받은 Chunk<I> 의 아이템 개수만큼 데이터를 가공 및 저장
        chunkProcessor.process(contribution, inputs); 
        chunkProvider.postProcess(contribution, inputs);

        if (inputs.isBusy()) {
            logger.debug("Inputs still busy");
            return RepeatStatus.CONTINUABLE;
        }

        // 5. 작업이 완료되면 버퍼에 저장한 Chunk 데이터삭제
        chunkContext.removeAttribute(INPUTS_KEY);
        chunkContext.setComplete();

        if (logger.isDebugEnabled()) {
            logger.debug("Inputs not busy, ended: " + inputs.isEnd());
        }
        
        // 6. 읽을 Item 이 더 존재하는지 확인하고 존재하면 프로세스를 반복하지만, null 일경우 FINISHED 를 반환하고 프로세스 종료
        return RepeatStatus.continueIf(!inputs.isEnd());

    }
}
    

```