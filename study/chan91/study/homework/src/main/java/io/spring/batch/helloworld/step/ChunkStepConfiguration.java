package io.spring.batch.helloworld.step;

import io.spring.batch.helloworld.dao.MemberRepository;
import io.spring.batch.helloworld.listener.StepListener1;
import io.spring.batch.helloworld.listener.StepListener2;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChunkStepConfiguration {

    private final StepBuilderFactory stepBuilderFactory;
    private final MemberRepository memberRepository;

    private final StepListener1 stepListener1; // Annotation 기반
    private final StepListener2 stepListener2; // Impl 기반

    private static final int CHUNK_SIZE = 10;

    private int num = 0;

    @Bean
    public Step simpleChunkStep() {
        return
                this.stepBuilderFactory.get("simpleChunkStep")
                        .<String, String>chunk(CHUNK_SIZE)
                        .reader(simpleItemReader())
                        .writer(simpleItemWriter())
                        .listener(stepListener1)
                        .listener(stepListener2)
                        .build();
    }

    @Bean
    public ItemWriter<String> simpleItemWriter() {
        return (inputList) -> {
            inputList.forEach(input -> {
                System.out.println(input + "입니다.");
            });
        };
    }

//    public ItemProcessor<String, String> simpleItemProcessor() {
//        String input1 = "in: " + input;
//        String output1 = "out: " + input;
//        System.out.println(input1);
//        System.out.println(output1);
//
//    }

    @Bean
    public ItemReader<String> simpleItemReader() {
//        repeat Templeate

        return new ItemReader<String>() {
            int i = 0;

            @Override
            public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                i++;

                if (i > 100) {
                    return null;
                }

                return new RandomString().nextString();
            }
        };

    }

    @Bean
    public ItemReader<String> simpleItemReader2() {

        return new ItemReader<String>() {
            @Override
            public String read() {
                return new RandomString(10).nextString();
            }

        };
    }

}
