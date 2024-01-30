# Step Flow
스텝을 일렬로 실행하는 것이 아니라, 특정 조건에 따라 분기를 타거나, 동시에 실행하거나, 반복해서 실행하는 등의 흐름을 만들 수 있다. 이러한 흐름을 만드는 것을 스텝 플로우라고 한다.
스텝 플로우를 커스터마이징할 수 있는 방법에 대해 알아본다.

## Transition
스프링 배치는 전이(transition)라는 개념으로 스텝 플로우를 제어한다.
전이는 스텝의 상태값을 보고 다음에 실행할 스텝을 결정하거나 잡을 종료하는 등의 제어를 한다.

```java
public Job stepNextConditionalJob() {
    return jobBuilderFactory.get("stepNextConditionalJob")
        .start(conditionalJobStep1())
            .on("FAILED") // FAILED 일 경우
            .to(conditionalJobStep3()) // step3으로 이동한다.
            .on("*") // step3의 결과 관계 없이
            .end() // step3으로 이동하면 Flow가 종료한다.
        .from(conditionalJobStep1()) // step1로부터
            .on("*") // FAILED 외에 모든 경우
            .to(conditionalJobStep2()) // step2로 이동한다.
            .next(conditionalJobStep3()) // step2가 정상 종료되면 step3으로 이동한다.
            .on("*") // step3의 결과 관계 없이
            .end() // step3으로 이동하면 Flow가 종료한다.
        .end() // Job 종료
        .build();
}
```

- next(): 다음 스텝으로 이동한다.
- on(): 캐치할 ExitStatus를 지정한다.
  - 와일드 카드
  - *: 0개 이상의 문자 일치 (모든 ExitStatus)
  - ?: 한 글자 일치
- to(): 다음으로 이동할 스텝 지정한다.
- from(): 일종의 이벤트 리스너 역할을 한다. 상태값을 보고 일치하는 상태라면 to()에 포함된 스텝을 호출한다.
- end(): FlowBuilder를 반환하는 end()와 FlowBuilder를 종료하는 end() 2개가 있다. 
on("*") 뒤에 있는 end()는 FlowBuilder를 반환하는 end()이고, build() 앞에 있는 end()는 FlowBuilder를 종료하는 end()이다. 
FlowBuilder를 반환하는 end()를 사용할 경우, 계속해서 from()을 이어갈 수 있다.

스프링 배치는 자동으로 가장 제한적인 전이부터 덜 제한적인 전이 순으로 정력한 뒤 각 전이를 순서대로 적용한다.

## Decider
- Decider는 Step의 상태값을 보고 분기를 타는 역할을 한다.
- JobExecutionDecider 인터페이스를 구현한 클래스를 만들어서 사용한다.
- 해당 인터페이스는 StepExecution과 JobExecution을 인자로 받는 decide() 메소드를 구현해야 한다.
- FlowExecutionStatus를 반환하는데, 이 값은 BatchStatus와 ExitStatus를 래핑한 래퍼 객체이다.
- decider() 메소드를 통해 Decider를 등록한다.

## 종료
| transition element | 종료 상태 (Batch status) | 재시작 가능 여부 | 설명                                       |
|--------------------|------------|----------|------------------------------------------|
| end()              | Completed  | 불가능      | 성공적 종료. 인자로 status를 넘기면 exitStatus 조작 가능 |
| fail()             | Failed     | 가능       | 실패한 스텝부터 재시작                             |
| stopAndRestart()   | Stopped    | 가능       | 지정한 스텝부터 재시작                             |

```java
@Bean
public Job job() {
    return this.jobBuilderFactory.get("conditionalJob")
            .start(firstStep())
            .on("FAILED").end()
//          .on("FAILED").fail()
//          .on("FAILED").stopAndRestart(successStep())
            .from(firstStep())
                .on("*").to(successStep())
            .end()
            .build();
}
```

## 플로우 외부화
```java
public Flow stepFlow() {
    return new FlowBuilder<Flow>("stepFlow")
        .start(step1())
        .next(step2());
}
```
- FlowBuilder 
```jobBuilderFactory.get("job1").start(stepFlow()).build()```
- Flow step 
```stepBuilderFactory.get("step1").flow(stepFlow()).build()```
  - 해당 플로우가 담긴 스텝을 하나의 스텝처럼 기록
  - 개별 스텝 집계가 필요하지 않고 전체 흐름을 모니터링할 때 사용
- Job step
  - 잡 내에서 다른 잡 호출
