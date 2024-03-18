
<!-- TOC -->
  * [1. AsyncItemProcessor](#1-asyncitemprocessor-)
  * [2. AsyncItemWriter](#2-asyncitemwriter)
  * [3. 다중 스레드 스텝](#3-다중-스레드-스텝)
  * [4. 병렬 스텝](#4-병렬-스텝)
  * [5. 병렬 스텝 구성하기](#5-병렬-스텝-구성하기)
<!-- TOC -->

## 1. AsyncItemProcessor와 AsyncItemWriter
### 1.1. 개요
 - 비동기 처리가 필요한 ItemProcessor와 ItemWriter에 비동기 처리 하는것(데코레이터)
 - **AsyncItemProcessor와 AsyncItemWriter를 함께 사용해야 한다**
 - **spring-batch-integration** 의존성 필요

### 1.2. 예제

```java

import java.beans.BeanProperty;
import javax.sql.rowset.spi.TransactionalWriter;

@Bean
public ASyncItemProcessor<Transaction, Transaction> asyncItemProcessor() {

}

@Bean
public ItemProcessor<Transaction, Transaction> processor() {
    return (transaction) -> {
        Thread.sleep(5);
        return transaction;
    }
}



```








## 2. AsyncItemWriter
## 3. 다중 스레드 스텝
## 4. 병렬 스텝
## 5. 병렬 스텝 구성하기

