package io.spring.batch.helloworld.step;

import io.spring.batch.helloworld.dao.MemberRepository;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChunkStepConfiguration {

    private final StepBuilderFactory stepBuilderFactory;
    private final MemberRepository memberRepository;

    private static final int CHUNK_SIZE = 10;

    private int num = 0;

    @Bean
    public Step simpleChunkStep2() {
        return
                this.stepBuilderFactory.get("simpleChunkStep")
                        .<String, String>chunk(CHUNK_SIZE)
                        .reader(simpleItemReader())
                        .writer(simpleItemWriter())
                        .build();
    }

    @Bean
    public ItemWriter<String> simpleItemWriter() {
        return (input) -> {
            System.out.println(input + "입니다.");
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

        return () -> new RandomString(10).nextString();
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
