# Chunk

## 기본 개념

- 여러 개의 아에팀을 묶은 하나의 덩어리 블록
- 한번에 하나씩 아이템을 입력 받아 Chunk 단위의 덩어리로 만든 후 **Chunk 단위로 트랜잭션을 처리**한다.
- 일반적으로 대용량 데이터를 한번에 처리하는 것이 아닌 Chunk 단위로 쪼개서 더 이상 처리할 데이터가 없을 때까지 반복해서 입출력하는데 사용된다.

![image](https://github.com/honeyosori/spring-batch/assets/53935439/2886a9d5-4f62-46b6-bf55-cf64e976a2a5)

> Item 단위로 입력받아 Chunk(Items) 단위로 출력한다 

- Chunk\<I>
  - ItemReader 로 읽은 하나의 아이템을 Chunk 에서 정한 개수만큼 반복해서 저장하는 타입
- Chunk\<O>
  - ItemReader 로 부터 전달받은 Chunk\<I\> 를 참조해서 ItemProcessor 에서 적절하게 가공 및 필터링 한 다음 ItemWriter 에 전달하는 타입

위 설명을 간단히 도식화 하면 아래와 같다.

![image](https://github.com/honeyosori/spring-batch/assets/53935439/12a34f7e-3a6e-441b-9571-97981ed8d694)

좀 더 자세하게 도식화 하면 아래와 같다.

![image](https://github.com/honeyosori/spring-batch/assets/53935439/a6849984-5601-4bc7-b80a-f20c3668b84d)

> 추가로 위 모든 단계가 하나의 트랜잭션으로 묶이기 때문에 과정에 대해 별도의 트랜잭션 처리 할 필요가 없다.

## 코드로 살펴보기

```java

@Configuration
@RequiredArgsConstructor
public class JobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job helloHob(){
        return jobBuilderFactory.get("helloJob")
                .start(helloStep())
                .next(helloStep2())
                .next(helloStep3())
                .build();
    }

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

    @Bean
    public Step helloStep2(){
        return stepBuilderFactory.get("helloStep2")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("hello2");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step helloStep3(){
        return stepBuilderFactory.get("helloStep3")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("hello3");
                    return RepeatStatus.FINISHED;
                }).build();
    }
}

```

> Step1 에서 Chunk 단위 작업 수행하고 있으며, 입력은 Item 단위로 출력은 Chunk 단위로 처리된다.

### 실행 결과 


![image](https://github.com/honeyosori/spring-batch/assets/53935439/47509b02-f38f-4372-996b-ea61f65b1d3e)
![image](https://github.com/honeyosori/spring-batch/assets/53935439/8c1dd03a-07bf-4e18-9dc4-bd00718fbc45)