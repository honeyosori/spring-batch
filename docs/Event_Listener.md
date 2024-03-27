# EventListener

## 목차
<!-- TOC -->
  * [1. 개념](#1-개념)
  * [2. 종류](#2-종류)
  * [3. 구현 방법](#3-구현-방법-)
    * [3.1. Annotation 방식](#31-annotation-방식-)
    * [3.2. Interface implements 방식](#32-interface-implements-방식)
  * [4. 예제](#4-예제)
<!-- TOC -->

## 1. 개념
- Job, Step, Chunk 단계의 실행 전후에 발생하는 이벤트를 받아 용도에 맞게 활용할 수 있도록 제공하는 인터셉터 개념의 클래스
- 각 단계별 로그기록을 남기거나 소요된 시간을 계산하거나 실행상태 정보들을 참조 및 조회 할 수 있다.
 

## 2. 종류
| 분류   | 리스너(Interface)        | Method                                                          | 시점                               | 비고                  |
|-------|-----------------------|-----------------------------------------------------------------|----------------------------------|---------------------|
| Job   | JobExecutionListener  | void beforeJob(JobExecution jobExecution)                       | Job 실행 전                         | Job의 성공여부와 상관없이 호출  |
| Job   | JobExecutionListener  | ExitStatus afterJob(JobExecution jobExecution)                  | Job 실행 후                         | Job의 성공여부와 상관없이 호출  |
| Step  | StepExecutionListener | void beforeStep(StepExecution stepExecution)                    | Step 실행 전                        | Step의 성공여부와 상관없이 호출 |
| Step  | StepExecutionListener | ExitStatus afterStep(StepExecution stepExecution)               | Step 실행 후                        | Step의 성공여부와 상관없이 호출 |
| Step  | ChunkListener         | void beforeChunk(ChunkContext context)                          | 트랜잭션이 시작되기 전 호출                  |                     |
| Step  | ChunkListener         | void afterChunk(ChunkContext context)                           | Chunk 가 커밋된 후 호출                 | 롤백되었다면 호출되지 않는다     |
| Step  | ChunkListener         | void afterChunkError(ChunkContext context)                      | 오류 발생 및 롤백이 되면 호출                |                     |
| Step  | ItemReadListener      | void beforeRead()                                               | read() 메소드 호출 전 매번 호출            |                     |
| Step  | ItemReadListener      | void afterRead(T item)                                          | read() 메소드 호출이 성공할 때마다           |                     |
| Step  | ItemReadListener      | void onReadError(Exception ex)                                  | 읽는 도중 오류가 발생하면 호출                |                     |
| Step  | ItemProcessListener   | void beforeProcess(T item)                                      | process() 메소드 호출 전 호출            |                     |
| Step  | ItemProcessListener   | void afterProcess(T item, @Nullable S result)                   | process() 메소드 호출이 성공할 때 호출       |                     |
| Step  | ItemProcessListener   | void onProcessError(T item, Exception e)                        | 처리 도중 오류가 발생하면 호출                |                     |
| Step  | ItemWriteListener     | void beforeWrite(List<? extends S> items)                       | write() 메소드를 호출하기 전 호출           |                     |
| Step  | ItemWriteListener     | void afterWrite(List<? extends S> items)                        | write() 메소드 호출이 성공할 때 호출         |                     |
| Step  | ItemWriteListener     | void onWriteError(Exception exception, List<? extends S> items) | 쓰기 도중 오류가 발생하면 호출                |                     |
| Skip  | SkipListener          | void onSkipInRead(Throwable t)                                  | read 수행중 Skip 이 발생할 경우 호출        |                     |
| Skip  | SkipListener          | void onSkipInWrite(S item, Throwable t)                         | write 수행중 Skip 이 발생할 경우 호출       |                     |
| Skip  | SkipListener          | void onSkipInProcess(T item, throwable t)                       | process 수행중 Skip 이 발생할 경우 호출  |                     |
| Retry | RetryListener         |                                                                 |                                  |                     |

## 3. 구현 방법 
### 3.1. Annotation 방식 
- 인터페이스를 구현할 필요가 없다
- 클래스 및 메서드명을 자유롭게 작성 할 수 있다
```java
public class BatchStepExecutionListener {
    
    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("beforeStep!!");
    }
}
```
### 3.2. Interface implements 방식
```java
public class BatchStepExecutionListener implements StepExecutionListener {
    
    @Override
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("beforeStep!!");
    }

}
```

## 4. 예제

![ex01.png](imgs%2Fex01.png)

![ex02.png](imgs%2Fex02.png)

![ex03.png](imgs%2Fex03.png)