# Listener In ChunkProcess

## ChunkListener / ItemReadListener

> 둘 다 Object 타입을 지원한다 (Annotation 방식)

## ItemProcessListener / ItemWriteListener


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