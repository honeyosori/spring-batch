# ItemProcessor

![image](https://github.com/honeyosori/spring-batch/assets/53935439/aa99a091-14d4-4c63-b290-64aae53f4d56)
![image](https://github.com/honeyosori/spring-batch/assets/53935439/dd688c17-5883-43f3-8b2a-9372a38f02fe)

## 기본 개념

- 데이터를 출력하기 전에 데이터를 가공, 변형, 필터링 하는 역할
- ItemReader 및 ItemWriter 와 분리되어 비즈니스 로직을 구현할 수 있다.
- ItemReader 로 부터 받은 아이템을 특정 타입으로 변환해서 ItemWriter 에 넘겨줄 수 있다.
- ChunkOrientedTasklet 실행 시 선택적 요소(필수 요소가 아님) 이다.

> 요약하자면 ItemReader 를 통해 Chunk size 만큼의 Item 을 읽어 ItemProcessor 에 Chunk 로 전달해주면, ItemProcessor 는 Chunk 에 담겨있는
> Item 들을 적절하게 가공 및 처리해서 List 타입으로 ItemWriter 에게 넘겨주는 역할을 수행한다. 쉽게 말해 reader 와 writer 사이의 비즈니스 로직을 처리하는 구간

## 구조

![image](https://github.com/honeyosori/spring-batch/assets/53935439/4daa9596-f5ff-4c81-b124-25c2ec1e2b59)

- O process
  - \<I> 제네릭은 ItemReader 에서 받을 데이터 타입 지정
  - \<O> 제네릭은 ItemWriter 에 보낼 데이터 타입 지정
  - 아이템 하나씩 가공 처리하며 null 을 리턴할 경우 해당 아이템은 Chunk\<O> 에 저장되지 않음

## 예시

```java
class Example{
  
  @Bean
  public Step helloStep(){
    return stepBuilderFactory.get("helloStep")
            .<String, String>chunk(5)
            .reader(new ListItemReader<>(Arrays.asList("item1", "item2", "item3")))
            .processor(new ItemProcessor<String, String>() {
              @Override
              public String process(String item) throws Exception {
                Thread.sleep(300);
                System.out.println("item = " + item);
                return "my" + item;
              }
            })
            .writer(new ItemWriter<String>() {
              @Override
              public void write(List<? extends String> items) throws Exception {
                Thread.sleep(300);
                System.out.println("items = " + items);
              }
            })
            .build();
  }
}

```

![image](https://github.com/honeyosori/spring-batch/assets/53935439/1382fe54-ff30-462a-a6c1-cb5c39a64578)


## 활용

Spring Batch 에서는 여러가지 Processor 클래스(구현체)를 제공하고 있다.

- ItemProcessorAdapter
- CompositeItemProcessor
- ClassifierCompositeItemProcessor
- ..

직접 구현 클래스를 만들어 사용할 수도 있지만, 필요에 따라서 Batch 가 제공해주는 클래스를 활용하면 편리하다.

### CompositeItemProcessor 

![image](https://github.com/honeyosori/spring-batch/assets/53935439/e814cdb9-e996-481a-8672-c5472809aa5e)

- ItemProcessor 들을 연결(Chaining) 해서 위임하면 각 ItemProcessor 를 실행시킨다.
- 이전 ItemProcessor 의 반환 값은 다음 Processor 값으로 연결된다.

> 프로세서의 역할은 reader 에서 writer 로 옮겨 갈 동안 Chunk 에 담긴 Item 을 대상으로한 변환기이다. Item 을 대상으로 하나 이상의 변환이 필요할 때,
> 모든 역할을 하나의 프로세서에 몰아두기보단 각 프로세서 별로 역할을 분담해서 이를 연결해줄 수 있도록 한다면 훨씬 관리하기 편하다. 

#### 예시

```java
class Example {
    
    @Bean(BEAN_PREFIX + "step")
    @JobScope
    public Step step(){
        return stepBuilderFactory.get(BEAN_PREFIX+"step")
            .<Teacher, String>chunk(chunkSize)
            .reader(reader())
            .processor(compositeProcessor())
            .writer(writer())
            .build();
        }

    @Bean
    public CompositeItemProcessor compositeProcessor(){
            List<ItemProcessor> delegates=new ArrayList<>(2);
            delegates.add(processor1());
            delegates.add(processor2());

            CompositeItemProcessor processor=new CompositeItemProcessor<>();

            processor.setDelegates(delegates);

            return processor;
        }

        public ItemProcessor<Teacher, String> processor1(){
            return Teacher::getName;
        }

        public ItemProcessor<String, String> processor2(){
            return name->"안녕하세요. "+name+"입니다.";
        }

}
```

### ClassifierCompositeItemProcessor

![image](https://github.com/honeyosori/spring-batch/assets/53935439/e66ae354-c659-4b4d-88ad-951249e2ed5a)

- Classifier 로 라우팅 패턴을 구현해서 ItemProcessor 구현체 중에서 하나를 호출하는 역할을 한다.

> 상황에 따라 프로세서를 다르게 적용하고 싶을 때, Classifier 에 따라 적용할 프로세서를 분류하여 사용할 수 있다. 즉, 위임자의 역할을 수행하며 

```java

@Data
@Builder
public class ProcessorInfo {

  private int id;
}


class Example{
  @Bean
  public Step helloStep2(){
    return stepBuilderFactory.get("helloStep2")
            .<ProcessorInfo, ProcessorInfo>chunk(10)
            .reader(new ItemReader<ProcessorInfo>() {
              int i = 0;
              @Override
              public ProcessorInfo read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                i++;
                ProcessorInfo processorInfo = ProcessorInfo.builder().id(i).build();

                return i > 3 ? null : processorInfo;
              }
            })
            .processor(customItemProcessor())
            .writer(items -> System.out.println(items))
            .build();
  }

  @Bean
  public ItemProcessor<? super ProcessorInfo, ? extends ProcessorInfo> customItemProcessor(){
    
    // 반환 : ClassifierCompositeItemProcessor<ProcessorInfo, ProcessorInfo>
    ClassifierCompositeItemProcessor<ProcessorInfo, ProcessorInfo> processor = new ClassifierCompositeItemProcessor<>();
    ProcessorClassifier<ProcessorInfo, ItemProcessor<?, ? extends ProcessorInfo>> classifier = new ProcessorClassifier<>();

    Map<Integer, ItemProcessor<ProcessorInfo, ProcessorInfo>> processorMap = new HashMap<>();
    processorMap.put(1, new CustomItemProcessor1());
    processorMap.put(2, new CustomItemProcessor2());
    processorMap.put(3, new CustomItemProcessor3());

    classifier.setProcessorMap(processorMap);
    processor.setClassifier(classifier);

    return processor;
  }
}

public class ProcessorClassifier<C, T> implements Classifier<C, T> {

  private Map<Integer, ItemProcessor<ProcessorInfo, ProcessorInfo>> processorMap = new HashMap<>();

  @Override
  public T classify(C classifiable) { // ProcessorInfo
    return (T)processorMap.get(((ProcessorInfo)classifiable).getId());
  }

  public void setProcessorMap(Map<Integer, ItemProcessor<ProcessorInfo, ProcessorInfo>> processorMap){
    this.processorMap = processorMap;
  }
}


public class CustomItemProcessor1 implements ItemProcessor<ProcessorInfo, ProcessorInfo>{

  @Override
  public ProcessorInfo process(ProcessorInfo item) throws Exception {

    System.out.println("CustomItemProcessor1");

    return item;
  }
}

public class CustomItemProcessor2 implements ItemProcessor<ProcessorInfo, ProcessorInfo>{

  @Override
  public ProcessorInfo process(ProcessorInfo item) throws Exception {

    System.out.println("CustomItemProcessor2");

    return item;
  }}

public class CustomItemProcessor3 implements ItemProcessor<ProcessorInfo, ProcessorInfo>{

  @Override
  public ProcessorInfo process(ProcessorInfo item) throws Exception {

    System.out.println("CustomItemProcessor3");

    return item;
  }}

```

![image](https://github.com/honeyosori/spring-batch/assets/53935439/b3aba4d7-8ced-41e8-b4d5-7094b341f212)