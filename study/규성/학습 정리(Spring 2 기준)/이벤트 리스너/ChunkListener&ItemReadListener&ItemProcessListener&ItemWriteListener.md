# Listener In ChunkProcess

## ChunkListener / ItemReadListener

![image](https://github.com/honeyosori/spring-batch/assets/53935439/da004b64-d499-4ff3-aaa4-7d12b68988ad)

> 둘 다 Object 타입을 지원한다 (Annotation 방식)

## ItemProcessListener / ItemWriteListener

![image](https://github.com/honeyosori/spring-batch/assets/53935439/93d9b58e-f7d7-461c-804b-c0f5578b7c05)

## 예제 코드

```java

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChunkListenerConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final CustomItemProcessListener itemProcessListener;
    private final CustomChunkListener chunkListener;
    private final CustomItemReadListener itemReadListener;
    private final CustomItemWriteListener itemWriteListener;


    @Bean
    public Job job() throws Exception {
        return jobBuilderFactory.get("batchJob")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .chunk(10)
                .listener(chunkListener)
                .listener(itemReadListener)
                .listener(itemProcessListener)
                .listener(itemWriteListener)
                .reader(listItemReader())
                .processor((ItemProcessor<? super Object, String>) item -> {
                    Integer processingItem = (Integer) item;
                    processingItem+=10;
                    return processingItem.toString();
                })
                .writer((ItemWriter<? super Object>) items -> {
                    log.info("items : {}", items);
                })
                .build();
    }

    @Bean
    public ItemReader<Integer> listItemReader() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        return new ListItemReader<>(list);
    }

}


```

```java
@Slf4j
@Component
public class CustomChunkListener {

    @BeforeChunk
    public void beforeChunk (){
        log.info("before chunk");
    }

    @AfterChunk
    public void afterChunk() {
        log.info("after chunk");
    }

    @AfterChunkError
    public void afterChunkError(){
        log.info("after chunk error");
    }
}

```

```java
@Slf4j
@Component
public class CustomItemReadListener implements ItemReadListener {

    @Override
    public void beforeRead() {
        log.info("before read");
    }

    @Override
    public void afterRead(Object item) {
        log.info("after read : {}", item);
    }

    @Override
    public void onReadError(Exception ex) {
        log.info("on read error");
    }
}

```

```java
@Slf4j
@Component
public class CustomItemProcessListener implements ItemProcessListener<Integer, String> {

    @Override
    public void beforeProcess(Integer item) {
        log.info("before process : {}", item);
    }

    @Override
    public void afterProcess(Integer item, String result) {
        log.info("after process : {}, result : {}", item, result);
    }

    @Override
    public void onProcessError(Integer item, Exception e) {
        log.info("on process error");
    }
}

```

```java
@Slf4j
@Component
public class CustomItemWriteListener implements ItemWriteListener {

    @Override
    public void beforeWrite(List items) {
        log.info("before write");
    }

    @Override
    public void afterWrite(List items) {
        log.info("after write");
    }

    @Override
    public void onWriteError(Exception exception, List items) {
        log.info("on write error");
    }
}

```

## 실행 결과

```json
2024-03-28 00:32:07.108  INFO 15540 --- [           main] o.s.batch.core.job.SimpleStepHandler     : Executing step: [step1]
2024-03-28 00:32:07.116  INFO 15540 --- [           main] c.s.b.l.chunk.CustomChunkListener        : before chunk
2024-03-28 00:32:07.117  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : before read
2024-03-28 00:32:07.117  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : after read : 1
2024-03-28 00:32:07.118  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : before read
2024-03-28 00:32:07.118  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : after read : 2
2024-03-28 00:32:07.118  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : before read
2024-03-28 00:32:07.118  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : after read : 3
2024-03-28 00:32:07.119  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : before read
2024-03-28 00:32:07.119  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : after read : 4
2024-03-28 00:32:07.119  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : before read
2024-03-28 00:32:07.119  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : after read : 5
2024-03-28 00:32:07.119  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : before read
2024-03-28 00:32:07.119  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : after read : 6
2024-03-28 00:32:07.119  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : before read
2024-03-28 00:32:07.119  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : after read : 7
2024-03-28 00:32:07.119  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : before read
2024-03-28 00:32:07.119  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : after read : 8
2024-03-28 00:32:07.119  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : before read
2024-03-28 00:32:07.119  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : after read : 9
2024-03-28 00:32:07.119  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : before read
2024-03-28 00:32:07.119  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : after read : 10
2024-03-28 00:32:07.119  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : before process : 1
2024-03-28 00:32:07.119  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : after process : 1, result : 11
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : before process : 2
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : after process : 2, result : 12
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : before process : 3
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : after process : 3, result : 13
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : before process : 4
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : after process : 4, result : 14
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : before process : 5
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : after process : 5, result : 15
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : before process : 6
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : after process : 6, result : 16
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : before process : 7
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : after process : 7, result : 17
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : before process : 8
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : after process : 8, result : 18
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : before process : 9
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : after process : 9, result : 19
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : before process : 10
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemProcessListener  : after process : 10, result : 20
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemWriteListener    : before write
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.c.ChunkListenerConfiguration     : items : [11, 12, 13, 14, 15, 16, 17, 18, 19, 20]
2024-03-28 00:32:07.120  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemWriteListener    : after write
2024-03-28 00:32:07.124  INFO 15540 --- [           main] c.s.b.l.chunk.CustomChunkListener        : after chunk
2024-03-28 00:32:07.124  INFO 15540 --- [           main] c.s.b.l.chunk.CustomChunkListener        : before chunk
2024-03-28 00:32:07.124  INFO 15540 --- [           main] c.s.b.l.chunk.CustomItemReadListener     : before read
2024-03-28 00:32:07.127  INFO 15540 --- [           main] c.s.b.l.chunk.CustomChunkListener        : after chunk
2024-03-28 00:32:07.128  INFO 15540 --- [           main] o.s.batch.core.step.AbstractStep         : Step: [step1] executed in 20ms
2024-03-28 00:32:07.134  INFO 15540 --- [           main] o.s.b.c.l.support.SimpleJobLauncher      : Job: [SimpleJob: [name=batchJob]] completed with the following parameters: [{name=user, seq=2, date=1711553527032, age=16.5}] and the following status: [COMPLETED] in 34ms

```