# Skip

## Skip이란?

* 데이터 처리 중 지정한 Exception이 발생했을 때 해당 데이터에 대한 처리를 건너뛰는 기능이다.
* ItemReader, ItemProcessor, ItemWriter에서 발생한 예외를 처리할 수 있다.

## 적용 방법
```java
public Step step() {
    return stepBuilderFactory.get("step")
        .<I, O>chunk(10)
        .reader(reader())
        .processor(processor())
        .writer(writer())
        .faultTolerant()
        .skip(SkippableException.class) // skip할 Exception 지정
        .noSkip(NonskippableException.class) // skip하지 않을 Exception 지정
        .skipLimit(3) // skip 횟수 제한
        .skipPolicy(skipPolicy()) // skip 여부를 결정하는 정책
        .build();
}
```

## SkipPolicy
skip 여부를 결정하는 기능을 하는 인터페이스이다.

```java
public interface SkipPolicy {
    boolean shouldSkip(Throwable var1, int var2) throws SkipLimitExceededException;
}
```

### 구현체
* LimitCheckingSkipPolicy: SkipPolicy를 지정하지 않았을 때 기본값으로 사용되는 구현체. skip 횟수와 Exception 타입을 확인해 skip 여부를 결정한다.
* AlwaysSkipPolicy: 항상 skip 처리를 하는 구현체
* NeverSkipPolicy: 항상 skip 처리를 하지 않는 구현체
* ExceptionClassifierSkipPolicy: Exception 타입에 따라 skip 여부를 결정하는 구현체
* CompositeSkipPolicy: 여러 SkipPolicy를 조합하여 사용할 수 있는 구현체. 여러 개의 SkipPolicy는 OR 조건으로 결합된다.

## skip 처리 흐름

* ChunkProvider(ItemReader)에서 예외가 발생한 경우: 해당 아이템을 skip하고 다음 아이템을 읽어온다.
* ChunkProcessor(ItemProcessor, ItemWriter)에서 예외가 발생한 경우: 다시 해당 chunk의 처음으로 돌아가 Reader로부터 아이템을 다시 읽어올 때, 예외가 발생한 아이템을 skip하고 읽어온다.

```java
public class FaultTolerantChunkProvider<I> extends SimpleChunkProvider<I> {

    protected I read(StepContribution contribution, Chunk<I> chunk) throws Exception {
        while(true) {
            try {
                return this.doRead();
            } catch (Exception var4) {
                if (this.shouldSkip(this.skipPolicy, var4, contribution.getStepSkipCount())) {
                    contribution.incrementReadSkipCount();
                    chunk.skip(var4);
                    ...
                } else {
                    ...
                }
            }
        }
    }

    private boolean shouldSkip(SkipPolicy policy, Throwable e, int skipCount) {
        try {
            return policy.shouldSkip(e, skipCount);
        } catch (SkipException var5) {
            throw var5;
        } catch (RuntimeException var6) {
            throw new SkipPolicyFailedException("Fatal exception in SkipPolicy.", var6, e);
        }
    }
}
```
