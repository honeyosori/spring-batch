# AsyncItemProcessor & AsyncItemWriter

## 기본 개념

- Step 안에서 ItemProcessor 가 비동기적으로 동작하는 구조
- AsyncItemProcessor 와 AsyncItemWriter 가 함께 구성이 되어야한다.
- AsyncItemProcessor 로부터 AsyncItemWriter 가 받는 최종 결과값은 List<Future<T>> 타입이며 비동기 실행이 완료될 때까지 대기한다.
- spring-batch-integration 의존성이 필요하다.

![image](https://github.com/honeyosori/spring-batch/assets/53935439/b326ad81-a259-49f2-80e6-f6fe4d383bee)

> 내부적으로 가지고 있는 ItemProcessor 를 통해 비동기적으로 실행한다. 
> 이 때 Main Thread 가 아닌 다른 Thread 안에서 별개로 실행된다.
> 비동기적으로 모든 작업을 마무리하면 결과값을 Future(내부에 Item 존재) 에 담아 전달하고 동일하게 위임을 통해 처리한다.

## 실행 흐름

![image](https://github.com/honeyosori/spring-batch/assets/53935439/fcb3c235-5a32-43af-a4d2-cac2d9b1d582)

> block 이 걸리고

## API

![image](https://github.com/honeyosori/spring-batch/assets/53935439/8794f0d4-45f0-4a60-bcee-fad65f1f5d56)

1. Step 기존 설정
2. Chunk 개수 설정
3. ItemReader 설정
4. (비동기 실행을 위한) AsyncItemProcessor 설정
- 청크 개수 혹은 스레드 풀 개수 만큼 스레드가 생성되어 비동기로 실행
- 내부적으로 실제 ItemProcessor 에게 실행을 위임하고 결과를 Future 에 저장한다.
5. AsyncItemWriter 설정
- 비동기 실행 결과 값들을 모두 받아오기까지 대기함
- 내부적으로 실제 ItemWriter 에게 최종 결과값을 넘겨주고 실행을 위임한다.
6. TaskletStep 생성

## 예제 코드

```java

@RequiredArgsConstructor
@Configuration
public class AsyncConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    @Bean
    public Job job() throws Exception {
        return jobBuilderFactory.get("batchJob")
                .incrementer(new RunIdIncrementer())
//                .start(step1()) // 동기적으로 실행
                .start(asyncStep1()) // 비동기적으로 실행
                .listener(new StopWatchJobListener())
                .build();
    }

    @Bean
    public Step step1() throws Exception {
        return stepBuilderFactory.get("step1")
                .chunk(100)
                .reader(pagingItemReader())
                .processor(customItemProcessor())
                .writer(customItemWriter())
                .build();
    }

    @Bean
    public Step asyncStep1() throws Exception {
        return stepBuilderFactory.get("asyncStep1")
                .<Customer, Customer>chunk(100)
                .reader(pagingItemReader())
                .processor(asyncItemProcessor())
                .writer(asyncItemWriter())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public JdbcPagingItemReader<Customer> pagingItemReader() {
        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.dataSource);
        reader.setPageSize(100);
        reader.setRowMapper(new CustomerRowMapper());

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, firstName, lastName, birthdate");
        queryProvider.setFromClause("from customer");

        Map<String, Order> sortKeys = new HashMap<>(1);

        sortKeys.put("id", Order.ASCENDING);

        queryProvider.setSortKeys(sortKeys);

        reader.setQueryProvider(queryProvider);

        return reader;
    }

    @Bean
    public ItemProcessor customItemProcessor() {
        return new ItemProcessor<Customer, Customer>() {
            @Override
            public Customer process(Customer item) throws Exception {

                Thread.sleep(1000);

                return new Customer(item.getId(),
                        item.getFirstName().toUpperCase(),
                        item.getLastName().toUpperCase(),
                        item.getBirthdate());
            }
        };
    }



    @Bean
    public AsyncItemProcessor asyncItemProcessor() throws Exception {
        AsyncItemProcessor<Customer, Customer> asyncItemProcessor = new AsyncItemProcessor();

        asyncItemProcessor.setDelegate(customItemProcessor());
        asyncItemProcessor.setTaskExecutor(new SimpleAsyncTaskExecutor());
//        asyncItemProcessor.setTaskExecutor(taskExecutor());
        asyncItemProcessor.afterPropertiesSet();

        return asyncItemProcessor;
    }

    @Bean
    public AsyncItemWriter asyncItemWriter() throws Exception {
        AsyncItemWriter<Customer> asyncItemWriter = new AsyncItemWriter<>();

        asyncItemWriter.setDelegate(customItemWriter());
        asyncItemWriter.afterPropertiesSet();

        return asyncItemWriter;
    }

    @Bean
    public JdbcBatchItemWriter customItemWriter() {
        JdbcBatchItemWriter<Customer> itemWriter = new JdbcBatchItemWriter<>();

        itemWriter.setDataSource(this.dataSource);
        itemWriter.setSql("insert into customer2 values (:id, :firstName, :lastName, :birthdate)");
        itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider());
        itemWriter.afterPropertiesSet();

        return itemWriter;
    }



    @Bean
    public TaskExecutor taskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setThreadNamePrefix("async-thread-");
        return executor;
    }
}
```

![image](https://github.com/honeyosori/spring-batch/assets/53935439/779b33f9-25e1-4575-a525-1409381b0256)
===
![image](https://github.com/honeyosori/spring-batch/assets/53935439/b8a0da4d-a5a2-4c87-a181-e21f0f334708)