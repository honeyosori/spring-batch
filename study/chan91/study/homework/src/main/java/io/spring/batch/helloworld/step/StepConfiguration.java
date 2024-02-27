package io.spring.batch.helloworld.step;


import io.spring.batch.helloworld.dao.MemberRepository;
import io.spring.batch.helloworld.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class StepConfiguration {

    private final StepBuilderFactory stepBuilderFactory;
    private final MemberRepository memberRepository;

    @Bean
    @Transactional
    public Step step1() {
        return this.stepBuilderFactory.get("step1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution,
                                                ChunkContext chunkContext) {

                        for (int i = 0; i < 100; i++) {
                            Member member = Member.builder()
                                    .name("test")
                                    .organizationId(i)
                                    .empId(1)
                                    .job("test")
                                    .hobby("test")
                                    .build();
                            memberRepository.save(member);
                        }

                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }


    @Bean
    public Step step2() {
        TaskletStep step = this.stepBuilderFactory.get("step2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution,
                                                ChunkContext chunkContext) {
                        System.out.println("Hello, World! step2");
                        return RepeatStatus.FINISHED;
                    }
                }).build();

        return step;
    }




}
