
<!-- TOC -->
  * [1. AsyncItemProcessor와 AsyncItemWriter](#1-asyncitemprocessor와-asyncitemwriter)
    * [1.1. 개요](#11-개요)
    * [1.2. 예제](#12-예제)
  * [2. 다중 스레드 스텝(Multi Thread Step)](#2-다중-스레드-스텝multi-thread-step)
    * [2.1. 개요](#21-개요)
  * [3. 병렬 스텝(Parallel Steps)](#3-병렬-스텝parallel-steps)
<!-- TOC -->

## 1. AsyncItemProcessor와 AsyncItemWriter
### 1.1. 개요

> - 비동기 처리가 필요한 ItemProcessor와 ItemWriter를 비동기 처리 하는것(데코레이터)
> - **AsyncItemProcessor와 AsyncItemWriter를 함께 사용해야 한다**
> - **spring-batch-integration** 의존성 필요

### 1.2. 예제

```java 

import java.beans.BeanProperty;
import javax.sql.rowset.spi.TransactionalWriter;


// ItemProcessor부
@Bean
public AsyncItemProcessor<Transaction, Transaction> asyncItemProcessor() {
    AsyncItemProcessor<Transaction, Transaction> processor = new AsyncItemProcessor<>();
    
    processor.setDelegate(processor()); // ItemProcessor 주입
    processor.setTaskExecutor(new SimpleAsyncTaskExecutor()); //사용할 Executor 주입
    // SimpleAsyncTaskExecutor 한 개의 요청당 한 개의 Thread 생성(테스트용 적합, 운영용 X)
    // 운영 -> ThreadPoolTaskExecutor 사용 권장(쓰레드 풀 개수만큼 Thread 생성)
    
    return processor;
}

@Bean
public ItemProcessor<Transaction, Transaction> processor() {
    return (transaction) -> {
        Thread.sleep(5);
        return transaction;
    };
}

//ItemWriter부
@Bean
public JdbcBatchItemWriter<Transaction> writer(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<Transaction>()
        .dataSource(dataSource)
        .beanMapped()
        .sql("INSERT INTO TRANSACTION (ACCOUNT, AMOUNT, TIMESTAMP) VALUES (:account, :amount, :timestamp")
        .build();
}

@Bean
public AsyncItemWriter<Transaction> asyncItemWriter() {
    AsyncItemWriter<Transaction> writer = new AsyncItemWriter<>();

    writer.setDelegate(writer(null)); //ItemWriter 주입
    return writer;
}

//Step & job
@Bean
public Step step1async() {
    return this.stepBuilderFactory.get("step1async")
        .<Transaction, Future<Transaction>>chunk(100) 
        .reader(fileTransactionReader(null))
        .processor(asyncItemProcessor())
        .writer(asyncItemWriter())
        .build();
}

// asyncItemProcessor를 사용하기 위해서는
// chunk 두번째 제네릭 타입 Future
// Future는 Javascript의 Promise 또는 Callback 함수 같은 느낌으로 이해되었음

@Bean Job asyncJob(){
    return this.jobBuilderFactory.get("asyncJob")
        .start(step1async())
        .build();
}

```


## 2. 다중 스레드 스텝(Multi Thread Step)
### 2.1. 개요
- 스텝은 기본적으로 단일 스레드로 동작
- Multi Thread Step == 잡의 실행을 병렬화 하는 가장 쉬운 방법
- ItemReader는 상태 저장(Job 재시작시 중단 위치를 찾기 위해)
- Multi Thread 환경에서는 saveState(false)를 통해 상태 저장 X

#### 2.1.1. 다중 스레드 스텝 
![](https://www.javacodebook.com/wp-content/uploads/2013/09/fig23-24.jpg)

```java

@Bean
public Step step1() {
    return this.stepBuilderFacotyr.get("step1")
        .<Transaction,Transaction>chunk(100)
        .reader(fileTransactionReader(null))
        .writer(writer(null))
        .taskExecutor(new SimpleAsyncTaskExecutor()) //taskExecutor로 SimpleAsyncTaskExecutor를 주입한다
        .build();
}

```


## 3. 병렬 스텝(Parallel Steps)
