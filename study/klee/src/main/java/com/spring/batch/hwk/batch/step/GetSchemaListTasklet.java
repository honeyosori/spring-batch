package com.spring.batch.hwk.batch.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetSchemaListTasklet implements Tasklet {

    @Qualifier("easDataSource")
    private final DataSource easDataSource;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        ExecutionContext executionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(easDataSource);
        String query = "SELECT schema_name FROM information_schema.schemata where schema_name like 's%';";
        List<String> schemas = jdbcTemplate.query(query, (resultSet, i) -> resultSet.getString("schema_name"));

        executionContext.put("schemas", schemas);
        log.info("=== Fetched schemas: {} ===", schemas.size());

        return RepeatStatus.FINISHED;
    }
}
