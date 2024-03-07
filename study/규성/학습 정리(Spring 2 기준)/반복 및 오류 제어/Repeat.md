# 반복 및 오류 제어

## 개념

- 작업을 얼마나 반복해야 하는지 알려줄 수 있는 기능
- 특정 조건이 충족 될 때까지(or 특정 조건이 아직 충족되지 않을 때까지) Job 또는 Step 을 반복하도록 배치 어플리케이션을 구성할 수 있다.
- Step 의 반복과 Chunk 의 반복을 RepeatOperation 을 사용해서 처리하고 있다.
- 기본 구현체로 RepeatTemplate 를 제공한다.

## 구조

![image](https://github.com/honeyosori/spring-batch/assets/53935439/f26ffade-b0cf-4d5e-a1a9-d6a1725873f0)

> RepeatTemplate 에서 반복 여부를 결정하게 되는데 내부에 존재하는 while 문을 통해 반복을 실행한다.
> 스프링 배치에서 반복 기회는 총 두 번 존재하며, Tasklet 작업 또는 Chunk 작업을 반복 실행할 수 있다.
 
## 반복 종료 여부를 결정하는 3 가지 항목

![image](https://github.com/honeyosori/spring-batch/assets/53935439/40a0eab8-d055-49c5-9f87-42d6d0201c85)

> 예외가 발생하지 않고 & 종료 정책에 해당하지 않으며 & 반환되는 상태값이 CONTINUABLE 일 경우에만 반복한다.
> 반대로 예외가 발생하거나 종료 정책에 해당하거나 반환되는 상태값이 FINISHED 일 경우에는 반복문을 종료한다.

### RepeatStatus

- 스프링 배치의 처리가 끝났는지 판별하기 위한 열거형
  - CONTINUABLE (작업 남음)
  - FINISHED (작업 끝남)

### CompletionPolicy

- RepeatTemplate 의 iterate 메소드 안에서 반복을 중단할지를 결정한다.
- 실행 횟수 또는 완료 시기, 오류 발생 시 수행할 작업에 대한 반복 여부를 결정한다.
- 정상 종료를 알리는데 사용된다.

![image](https://github.com/honeyosori/spring-batch/assets/53935439/70bad44a-c040-4507-88a6-825a3e038d8f)

```java
package com.spring.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.*;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.RepeatCallback;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.repeat.exception.ExceptionHandler;
import org.springframework.batch.repeat.exception.SimpleLimitExceptionHandler;
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RepeatConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job helloHob() throws Exception {
        return jobBuilderFactory.get("batchJob")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

    @Bean
    public Step step1(){
        return stepBuilderFactory.get("step1")
                .chunk(5)
                .reader(new ItemReader<>() {
                    int i = 0;
                    @Override
                    public Object read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        i++;
                        return i > 3 ? null : "item" + i;
                    }
                })
                .processor(new ItemProcessor<Object, Object>() {

                    RepeatTemplate repeatTemplate = new RepeatTemplate();

                    @Override
                    public Object process(Object item) throws Exception {

                        // 3 번의 반복
                        repeatTemplate.setCompletionPolicy(new SimpleCompletionPolicy(3));
//                        // 3초 동안 반복
//                        repeatTemplate.setCompletionPolicy(new TimeoutTerminationPolicy(3000));
//
//                        // 여러개 정책 동시 사용(or 조건으로 실행)
//                        CompositeCompletionPolicy compositeCompletionPolicy = new CompositeCompletionPolicy();
//                        CompletionPolicy[] completionPolicies = new CompletionPolicy[]{
//                                new TimeoutTerminationPolicy(3000),
//                                new SimpleCompletionPolicy(3)
//                        };
                        
                        repeatTemplate.iterate(new RepeatCallback() {
                            @Override
                            public RepeatStatus doInIteration(RepeatContext context) throws Exception {
                                System.out.println("repeatTemplate is testing");
                                return RepeatStatus.CONTINUABLE;
                            }
                        });

                        return item;
                    }
                })
                .writer(items -> System.out.println(items))
                .build();
    }
}

```

### ExceptionHandler

- RepeatCallBack 안에서 예외가 발생하면 RepeatTemplate 이 ExceptionHandler 를 참조해서 예외를 다시 던질지 여부를 결정한다.
- 예외를 받아서 다시 던지게 되면 반복을 종료한다.
- 비정상 종료를 알리는데 사용된다.

![image](https://github.com/honeyosori/spring-batch/assets/53935439/964002d1-b1d6-43e3-9598-9fe2be8e2c6a)

```java
@Configuration
@RequiredArgsConstructor
public class RepeatConfiguration {

    @Bean
    public Step step1(){
        return stepBuilderFactory.get("step1")
                .chunk(5)
                .reader(new ItemReader<>() {
                    int i = 0;
                    @Override
                    public Object read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        i++;
                        return i > 3 ? null : "item" + i;
                    }
                })
                .processor(new ItemProcessor<Object, Object>() {

                    RepeatTemplate repeatTemplate = new RepeatTemplate();

                    @Override
                    public Object process(Object item) throws Exception {

                        repeatTemplate.setExceptionHandler(simpleLimitExceptionHandler());

                        repeatTemplate.iterate(new RepeatCallback() {
                            @Override
                            public RepeatStatus doInIteration(RepeatContext context) throws Exception {
                                // 총 4번 실행되며, 4번째 throw 된 Exception 은 정상적으로 처리되지 못함
                                System.out.println("repeatTemplate is testing");
                                throw new RuntimeException("Exception is occurred");
                            }
                        });

                        return item;
                    }
                })
                .writer(items -> System.out.println(items))
                .build();
    }

    @Bean
    public ExceptionHandler simpleLimitExceptionHandler(){
        return new SimpleLimitExceptionHandler(3);
    }
}

```