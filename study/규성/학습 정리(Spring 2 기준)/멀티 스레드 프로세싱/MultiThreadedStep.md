# Multi-threaded Step

## 기본 개념
- Step 내에서 멀티 스레드로 Chunk 기반 처리가 이루어지는 구조
- TaskExecutorRepeatTemplate 이 반복 사용되며 설정한 개수 만큼의 스레드를 생성하여 수행한다.

![image](https://github.com/honeyosori/spring-batch/assets/53935439/996c0fa5-5a23-4768-8abe-11891a3488c4)

## 실행 흐름
![image](https://github.com/honeyosori/spring-batch/assets/53935439/b4e3c2c5-c95c-49ab-9e18-ba03cf577117)

## API

![image](https://github.com/honeyosori/spring-batch/assets/53935439/04bb7e73-8cec-4fa7-8771-b946cf49365c)

- Thread-safe 한 ItemReader 설정
- 스레드 생성 및 실행을 위한 taskExecutor 설정

## 예제 코드

```java
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MultiThreadStepConfiguration {

    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilderFactory;
    private final DataSource dataSource;

    @Bean
    public Job job() throws Exception {
        return jobBuilderFactory.get("batchJob")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .listener(new StopWatchJobListener())
                .build();
    }

    @Bean
    public Step step1() throws Exception {
        return stepBuilderFactory.get("step1")
                .chunk(100)
                .reader(pagingItemReader())
                .listener(new CustomItemReadListener())
                .processor((ItemProcessor<? super Object, ?>) item -> item)
                .listener(new CustomItemProcessListener())
                .writer(customItemWriter())
                .listener(new CustomItemWriterListener())
                .taskExecutor(taskExecutor()) // 멀티 스레드
                .build();
    }

    @Bean
    public JdbcPagingItemReader pagingItemReader() {

        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.dataSource);
        reader.setPageSize(100);
        reader.setRowMapper(new CustomerRowMapper());

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, firstName, lastName, birthdate");
        queryProvider.setFromClause("from customer");

        Map<String, Order> sortKeys = new HashMap<>();

        sortKeys.put("id", Order.ASCENDING);

        queryProvider.setSortKeys(sortKeys);

        reader.setQueryProvider(queryProvider);

        return reader;
    }

    private JdbcBatchItemWriter customItemWriter() {
        JdbcBatchItemWriter<Customer> itemWriter = new JdbcBatchItemWriter<>();

        itemWriter.setDataSource(this.dataSource);
        itemWriter.setSql("insert into customer2 values (:id, :firstName, :lastName, :birthdate)");
        itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        itemWriter.afterPropertiesSet();

        return itemWriter;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(4); // 4 개의 쓰레드
        taskExecutor.setMaxPoolSize(8);
        taskExecutor.setThreadNamePrefix("async-thread");

        return taskExecutor;
    }
}

```

- 병렬로 실행되는 각 Thread 는 독립적으로 실행되며 Chunk 를 공유하지 않는다.
- 내부에서 동시성 문제가 발생하지 않도록 동기화 처리(synchronized)를 해주고 있다.

### 멀티 스레드 실행 결과 (1000)
![image](https://github.com/honeyosori/spring-batch/assets/53935439/caa8ddc5-b91f-48d9-91eb-5b93b5a9ffa5)
=================
![image](https://github.com/honeyosori/spring-batch/assets/53935439/7596e2ba-cbb7-439a-9e50-3f8ee4985dce)
=================
![image](https://github.com/honeyosori/spring-batch/assets/53935439/32f2d6fe-6a2a-4f2f-a620-a5211c44b7b8)
=================
![image](https://github.com/honeyosori/spring-batch/assets/53935439/c6049322-2d34-4d4b-a759-6ba6cfb53713)

> 4개의 스레드를 통해 각각 100개의 Item 을 가진 청크로 나뉘어 병렬로 처리되며 실행 속도가 빠르며, 동시성 이슈는 발생하지 않는다. 

### 싱글 스레드 실행 결과(1000)

![image](https://github.com/honeyosori/spring-batch/assets/53935439/e517ea04-b160-4583-b2c7-ad1329c1b3ad)

> 하나의 main 스레드를 통해 실행되며 실행속도가 비교적 느리다.

## XXXpagingItemReader 동기화 처리

### AbstractItemCountingItemStreamItemReader
![image](https://github.com/honeyosori/spring-batch/assets/53935439/1240364c-71d9-4f52-8ce4-b9f25afbe8ad)
### AbstractPagingItemReader
![image](https://github.com/honeyosori/spring-batch/assets/53935439/f4c9d7c5-ea53-49b5-a5d0-0ebeb7fde672)

> JDBC, JPA, Hibernate 등등