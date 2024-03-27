# SkipListener & RetryListener

## SkipListener

![image](https://github.com/honeyosori/spring-batch/assets/53935439/339402db-a07b-40a6-abaf-a473596c25fb)

> 위의 Listener 들은 실제 Skip 이 될 때 호출되는 것이 아니라 모든 과정이 끝나고 호출이 된다.

## 예제 코드

```java
@Configuration
@Slf4j
@RequiredArgsConstructor
public class SkipListenerConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job() throws Exception {
        return jobBuilderFactory.get("batchJob")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() throws Exception {
        return stepBuilderFactory.get("step1")
                .chunk(10)
                .reader(listItemReader())
                .processor((ItemProcessor<? super Object, String>) item -> {
                    log.info("process item : {}", item.toString());
                    int processingItem = (Integer) item;
                    if(item.equals(4)) {
                        log.info("item 이 4일 때 예외 발생");
                        throw new CustomSkipException("");
                    }
                    return processingItem+"";
                })
                .writer((ItemWriter<? super Object>) items -> {
                    log.info("write items : {}", items);
                })
                .faultTolerant()
                .skip(CustomSkipException.class)
                .skipLimit(2)
                .listener(new CustomSkipListener())
                .build();
    }

    @Bean
    public ItemReader<Integer> listItemReader(){
        List<Integer> list = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
        return new LinkedListItemReader<>(list);
    }
}

```
```java
@Slf4j
public class LinkedListItemReader<T> implements ItemReader<T> {
    private List<T> list;

    public LinkedListItemReader(List<T> list) {
        if(AopUtils.isAopProxy(list)) this.list = list;
        else this.list = new LinkedList<>(list);
    }

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if(!list.isEmpty()){
            T remove = (T)list.remove(0);
            log.info("read : {}", remove);
            if((Integer)remove == 3) {
                log.info("item 이 3일 때 예외 발생");
                throw new CustomSkipException();
            }
            return remove;
        }
        return null;
    }
}

```
```java
@Slf4j
@Component
public class CustomSkipListener implements SkipListener<Integer, String> {

    @Override
    public void onSkipInRead(Throwable t) {
        log.info("on skip in read : {}", t.getMessage());
    }

    @Override
    public void onSkipInWrite(String item, Throwable t) {
        log.info("on skip in write : {}, {}", item, t.getMessage());
    }

    @Override
    public void onSkipInProcess(Integer item, Throwable t) {
        log.info("on skip in process : {}, {}", item, t.getMessage());
    }
}

```

## 실행 결과
```
2024-03-28 01:38:37.377  INFO 20716 --- [           main] o.s.b.c.l.support.SimpleJobLauncher      : Job: [SimpleJob: [name=batchJob]] launched with the following parameters: [{name=user, seq=2, date=1711557517327, age=16.5}]
2024-03-28 01:38:37.399  INFO 20716 --- [           main] o.s.batch.core.job.SimpleStepHandler     : Executing step: [step1]
2024-03-28 01:38:37.408  INFO 20716 --- [           main] c.s.b.l.skip.LinkedListItemReader        : read : 1
2024-03-28 01:38:37.409  INFO 20716 --- [           main] c.s.b.l.skip.LinkedListItemReader        : read : 2
2024-03-28 01:38:37.409  INFO 20716 --- [           main] c.s.b.l.skip.LinkedListItemReader        : read : 3
2024-03-28 01:38:37.409  INFO 20716 --- [           main] c.s.b.l.skip.LinkedListItemReader        : item 이 3일 때 예외 발생
2024-03-28 01:38:37.410  INFO 20716 --- [           main] c.s.b.l.skip.LinkedListItemReader        : read : 4
2024-03-28 01:38:37.410  INFO 20716 --- [           main] c.s.b.l.skip.LinkedListItemReader        : read : 5
2024-03-28 01:38:37.410  INFO 20716 --- [           main] c.s.b.l.skip.LinkedListItemReader        : read : 6
2024-03-28 01:38:37.410  INFO 20716 --- [           main] c.s.b.l.skip.LinkedListItemReader        : read : 7
2024-03-28 01:38:37.410  INFO 20716 --- [           main] c.s.b.l.skip.LinkedListItemReader        : read : 8
2024-03-28 01:38:37.410  INFO 20716 --- [           main] c.s.b.l.skip.LinkedListItemReader        : read : 9
2024-03-28 01:38:37.410  INFO 20716 --- [           main] c.s.b.l.skip.LinkedListItemReader        : read : 10
2024-03-28 01:38:37.412  INFO 20716 --- [           main] c.s.b.l.skip.SkipListenerConfiguration   : process item : 1
2024-03-28 01:38:37.414  INFO 20716 --- [           main] c.s.b.l.skip.SkipListenerConfiguration   : process item : 2
2024-03-28 01:38:37.414  INFO 20716 --- [           main] c.s.b.l.skip.SkipListenerConfiguration   : process item : 4
2024-03-28 01:38:37.414  INFO 20716 --- [           main] c.s.b.l.skip.SkipListenerConfiguration   : item 이 4일 때 예외 발생
2024-03-28 01:38:37.416  INFO 20716 --- [           main] c.s.b.l.skip.SkipListenerConfiguration   : process item : 1
2024-03-28 01:38:37.416  INFO 20716 --- [           main] c.s.b.l.skip.SkipListenerConfiguration   : process item : 2
2024-03-28 01:38:37.416  INFO 20716 --- [           main] c.s.b.l.skip.SkipListenerConfiguration   : process item : 5
2024-03-28 01:38:37.416  INFO 20716 --- [           main] c.s.b.l.skip.SkipListenerConfiguration   : process item : 6
2024-03-28 01:38:37.416  INFO 20716 --- [           main] c.s.b.l.skip.SkipListenerConfiguration   : process item : 7
2024-03-28 01:38:37.416  INFO 20716 --- [           main] c.s.b.l.skip.SkipListenerConfiguration   : process item : 8
2024-03-28 01:38:37.416  INFO 20716 --- [           main] c.s.b.l.skip.SkipListenerConfiguration   : process item : 9
2024-03-28 01:38:37.416  INFO 20716 --- [           main] c.s.b.l.skip.SkipListenerConfiguration   : process item : 10
2024-03-28 01:38:37.417  INFO 20716 --- [           main] c.s.b.l.skip.SkipListenerConfiguration   : write items : [1, 2, 5, 6, 7, 8, 9, 10]
2024-03-28 01:38:37.421  INFO 20716 --- [           main] o.s.batch.core.step.AbstractStep         : Step: [step1] executed in 22ms
2024-03-28 01:38:37.426  INFO 20716 --- [           main] o.s.b.c.l.support.SimpleJobLauncher      : Job: [SimpleJob: [name=batchJob]] completed with the following parameters: [{name=user, seq=2, date=1711557517327, age=16.5}] and the following status: [COMPLETED] in 37ms
```


## RetryListener

![image](https://github.com/honeyosori/spring-batch/assets/53935439/cd612e63-08d5-4069-b35c-91d6290a9e48)


### 예제 코드

```java
@Configuration
@RequiredArgsConstructor
@Slf4j
public class RetryListenerConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job() throws Exception {
        return jobBuilderFactory.get("batchJob")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() throws Exception {
        return stepBuilderFactory.get("step1")
                .chunk(10)
                .reader(listItemReader())
                .processor(new CustomItemProcessor())
                .writer(new CustomItemWriter())
                .faultTolerant()
                .retry(CustomRetryException.class)
                .retryLimit(2)
                .listener(new CustomRetryListener())
                .build();
    }

    @Bean
    public ItemReader<Integer> listItemReader(){
        List<Integer> list = Arrays.asList(1,2,3,4);
        return new LinkedListItemReader<>(list);
    }
}
```
```java
@Slf4j
public class CustomRetryListener implements RetryListener {

    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
        log.info("open");
        return true;
    }

    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        log.info("close");
    }

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        log.info("on error, retry count : {}", context.getRetryCount());
    }
}

```
```java
@Slf4j
public class CustomItemProcessor implements ItemProcessor<Object, String> {
    int count = 0;

    @Override
    public String process(Object item) throws Exception {

        log.info("item : {}, count : {}", item, count);
        if(count < 2) {
            if(count % 2 == 0) count++;
            else if(count % 2 == 1) {
                count++;
                log.info("예외 발생");
                throw new CustomRetryException("failed");
            }

        }

        return String.valueOf(item);
    }
}

```
```java
@Slf4j
public class CustomItemWriter implements ItemWriter<Object> {

    int count = 0;

    @Override
    public void write(List<? extends Object> items) throws Exception {

        log.info("items : {}", items);

        for(Object item : items) {
            log.info("item : {}, count : {}", item, count);
            if (count < 2) {
                if (count % 2 == 0) count++;
                else if (count % 2 == 1) {
                    count++;
                    log.info("예외 발생");
                    throw new CustomRetryException("failed");
                }

            }
            log.info("write : {}", item);
        }

    }
}

```


### 실행 결과

> processor 와 writer 는 각각 1번씩(count 가 1일 때) 예외가 발생하고 retry 됨

```
2024-03-28 02:37:59.684  INFO 18428 --- [           main] o.s.b.c.l.support.SimpleJobLauncher      : Job: [SimpleJob: [name=batchJob]] launched with the following parameters: [{name=user, seq=2, date=1711561079632, age=16.5}]
2024-03-28 02:37:59.707  INFO 18428 --- [           main] o.s.batch.core.job.SimpleStepHandler     : Executing step: [step1]
2024-03-28 02:37:59.716  INFO 18428 --- [           main] c.s.b.l.skip.LinkedListItemReader        : read : 1
2024-03-28 02:37:59.717  INFO 18428 --- [           main] c.s.b.l.skip.LinkedListItemReader        : read : 2
2024-03-28 02:37:59.717  INFO 18428 --- [           main] c.s.b.l.skip.LinkedListItemReader        : read : 3
2024-03-28 02:37:59.717  INFO 18428 --- [           main] c.s.b.l.skip.LinkedListItemReader        : read : 4
> 읽고 Chunk 로 넘김
2024-03-28 02:37:59.719  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : open
2024-03-28 02:37:59.719  INFO 18428 --- [           main] c.s.b.l.retry.CustomItemProcessor        : item : 1, count : 0
2024-03-28 02:37:59.719  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : close
2024-03-28 02:37:59.719  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : open
2024-03-28 02:37:59.720  INFO 18428 --- [           main] c.s.b.l.retry.CustomItemProcessor        : item : 2, count : 1
2024-03-28 02:37:59.720  INFO 18428 --- [           main] c.s.b.l.retry.CustomItemProcessor        : 예외 발생
> processor 에서 예외가 발생
2024-03-28 02:37:59.720  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : on error, retry count : 1
2024-03-28 02:37:59.720  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : close
> retry
2024-03-28 02:37:59.722  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : open
2024-03-28 02:37:59.722  INFO 18428 --- [           main] c.s.b.l.retry.CustomItemProcessor        : item : 1, count : 2
2024-03-28 02:37:59.722  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : close
2024-03-28 02:37:59.722  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : open
2024-03-28 02:37:59.722  INFO 18428 --- [           main] c.s.b.l.retry.CustomItemProcessor        : item : 2, count : 2
2024-03-28 02:37:59.722  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : close
2024-03-28 02:37:59.722  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : open
2024-03-28 02:37:59.722  INFO 18428 --- [           main] c.s.b.l.retry.CustomItemProcessor        : item : 3, count : 2
2024-03-28 02:37:59.722  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : close
2024-03-28 02:37:59.722  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : open
2024-03-28 02:37:59.722  INFO 18428 --- [           main] c.s.b.l.retry.CustomItemProcessor        : item : 4, count : 2
2024-03-28 02:37:59.722  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : close
2024-03-28 02:37:59.722  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : open
> 성공적으로 item 들을 writer 에 넘김
2024-03-28 02:37:59.723  INFO 18428 --- [           main] c.s.b.listener.retry.CustomItemWriter    : items : [1, 2, 3, 4]
2024-03-28 02:37:59.723  INFO 18428 --- [           main] c.s.b.listener.retry.CustomItemWriter    : item : 1, count : 0
2024-03-28 02:37:59.723  INFO 18428 --- [           main] c.s.b.listener.retry.CustomItemWriter    : write : 1
2024-03-28 02:37:59.723  INFO 18428 --- [           main] c.s.b.listener.retry.CustomItemWriter    : item : 2, count : 1
2024-03-28 02:37:59.723  INFO 18428 --- [           main] c.s.b.listener.retry.CustomItemWriter    : 예외 발생
> writer 에서 예외가 발생
2024-03-28 02:37:59.723  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : on error, retry count : 1
2024-03-28 02:37:59.723  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : close
> retry
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : open
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.l.retry.CustomItemProcessor        : item : 1, count : 2
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : close
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : open
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.l.retry.CustomItemProcessor        : item : 2, count : 2
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : close
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : open
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.l.retry.CustomItemProcessor        : item : 3, count : 2
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : close
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : open
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.l.retry.CustomItemProcessor        : item : 4, count : 2
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : close
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : open
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.listener.retry.CustomItemWriter    : items : [1, 2, 3, 4]
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.listener.retry.CustomItemWriter    : item : 1, count : 2
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.listener.retry.CustomItemWriter    : write : 1
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.listener.retry.CustomItemWriter    : item : 2, count : 2
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.listener.retry.CustomItemWriter    : write : 2
2024-03-28 02:37:59.724  INFO 18428 --- [           main] c.s.b.listener.retry.CustomItemWriter    : item : 3, count : 2
2024-03-28 02:37:59.725  INFO 18428 --- [           main] c.s.b.listener.retry.CustomItemWriter    : write : 3
2024-03-28 02:37:59.725  INFO 18428 --- [           main] c.s.b.listener.retry.CustomItemWriter    : item : 4, count : 2
2024-03-28 02:37:59.725  INFO 18428 --- [           main] c.s.b.listener.retry.CustomItemWriter    : write : 4
2024-03-28 02:37:59.725  INFO 18428 --- [           main] c.s.b.l.retry.CustomRetryListener        : close
2024-03-28 02:37:59.729  INFO 18428 --- [           main] o.s.batch.core.step.AbstractStep         : Step: [step1] executed in 22ms
2024-03-28 02:37:59.735  INFO 18428 --- [           main] o.s.b.c.l.support.SimpleJobLauncher      : Job: [SimpleJob: [name=batchJob]] completed with the following parameters: [{name=user, seq=2, date=1711561079632, age=16.5}] and the following status: [COMPLETED] in 39ms
```