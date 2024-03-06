# FaultTolerant

## 기본 개념

- Job 실행 중에 오류가 발생할 경우 장애를 처리하기 위한 기능을 제공하며 이를 통해 복원력을 향상 시킬 수 있다.
- 오류가 발생해도 Step 이 즉시 종료되지 않고 Retry 또는 Skip 기능을 활성화 함으로써 내결함성 서비스가 가능하다.
- 프로그램의 내결함성을 위해 Skip 과 Retry 기능을 제공한다.
  - Skip : ItemReader / ItemProcessor / ItemWriter 에 적용이 가능하다.
  - Retry : ItemProcessor / ItemWriter 에 적용이 가능하다. (ItemReader 는 적용 불가)
    - FaultTolerantChunkProcessor.doWithRetry(RetryContext context) 안에서 실행이 됨
- FaultTolerant 구조는 청크 기반의 프로세스 기반위에 Skip 과 Retry 기능이 추가되어 재정의 되어 있다.

## 구조

## API 

> StepBuilderFactory > StepBuilder > FaultTolerantStepBuilder > TaskletStep

## 전체 처리 과정

![image](https://github.com/honeyosori/spring-batch/assets/53935439/7c5d0f96-7eb0-442b-8a06-9ea8d7dbaf17)

## 코드

### case 1. 정해진 skip 개수를 넘는 경우
```java
@Configuration
@RequiredArgsConstructor
public class FaultTolerantConfiguration {

  @Bean
  public Step step1() throws Exception{
    return stepBuilderFactory.get("step1")
            .chunk(5)
            .reader(new ItemReader<String>() {
              int i = 0;
              @Override
              public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                i++;
                if(i == 1 || i == 2 || i == 3){
                   // 이쪽에서 제한 횟수(2) 넘게 다 걸림
                  throw new IllegalArgumentException("this exception is skipped");
                }
                System.out.println(i);
                return i > 3 ? null : "item" + i;
              }
            })
            .processor(new ItemProcessor<Object, String>() {

              @Override
              public String process(Object item) throws Exception {
                System.out.println((String)item);
                return (String)item;
              }
            })
            .writer(items -> System.out.println(items))
            .faultTolerant()
            .skip(IllegalArgumentException.class) // 해당 예외가 발생할 경우 skip
            .skipLimit(2)
            .retry(IllegalStateException.class) // 해당 예외가 발생할 경우 retry
            .retryLimit(2)
            .build();
  }
}
```
### 실행 결과 
![image](https://github.com/honeyosori/spring-batch/assets/53935439/64befe69-3754-423f-ba5e-dab733bc4a6c)
![image](https://github.com/honeyosori/spring-batch/assets/53935439/db04a135-175c-4a40-b049-b5d52c8c3bcd)

### case 2. 정해진 retry 횟수를 넘는 경우

```java
package com.spring.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FaultTolerantConfiguration {

    @Bean
    public Step step1() throws Exception{
        return stepBuilderFactory.get("step1")
                .chunk(5)
                .reader(new ItemReader<String>() {
                    int i = 0;
                    @Override
                    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        i++;
                        if(i == 1 || i == 2){
                            throw new IllegalArgumentException("this exception is skipped");
                        }
                        System.out.println(i);
                        return i > 3 ? null : "item" + i;
                    }
                })
                .processor(new ItemProcessor<Object, String>() {
                    int i = 0;
                    @Override
                    public String process(Object item) throws Exception {
                        i++;
                        System.out.println(i + " 번째 시도, item : " + item);
                        if(i == 1 || i == 2) {
                            System.out.println("실패");
                            throw new IllegalStateException("this exception is retired");
                        }
                        System.out.println((String)item);
                        return (String)item;
                    }
                })
                .writer(items -> System.out.println(items))
                .faultTolerant()
                .skip(IllegalArgumentException.class) // 해당 예외가 발생할 경우 skip
                .skipLimit(2)
                .retry(IllegalStateException.class) // 해당 예외가 발생할 경우 retry
                .retryLimit(2)
                .build();
    }
}


```

## 실행 결과

![image](https://github.com/onjsdnjs/spring-batch-lecture/assets/53935439/d397a2d2-5acf-40fb-b672-cb5b55df2285)
![image](https://github.com/onjsdnjs/spring-batch-lecture/assets/53935439/1feb64d7-9836-4793-b10a-7e9e467b9d94)

> 최대 2번의 재실행 기회를 모두 소진하고도 exception 이 발생한다면 해당 예외를 skip 이 처리해야한다. 즉, 위 로직이 
> 문제없이 실행되려면 최대 3번의 재실행 기회(retryLimit) 를 가져야한다.
