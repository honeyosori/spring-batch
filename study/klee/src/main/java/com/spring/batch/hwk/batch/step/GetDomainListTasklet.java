package com.spring.batch.hwk.batch.step;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.batch.hwk.module.DomainDto;
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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetDomainListTasklet implements Tasklet {

    @Qualifier("memberDataSource")
    private final DataSource memberDataSource;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        ExecutionContext executionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
        List<String> schemas = (List<String>) executionContext.get("schemas");
        List<DomainDto> domains = getDomains(schemas);

        executionContext.put("domains", objectMapper.writeValueAsString(domains));
        log.info("=== Fetched domains: {} ===", domains.size());

        return RepeatStatus.FINISHED;
    }

    private List<DomainDto> getDomains(List<String> schemas) {
        List<DomainDto> domains = new ArrayList<>();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(memberDataSource);
        String query = "SELECT * FROM domain WHERE del_date IS NULL";
        jdbcTemplate.query(query, resultSet -> {
            long vhId = resultSet.getLong("vh_id");
            String schemaName = getSchemaName(vhId);

            if(schemas.contains(schemaName)) {
                String name = resultSet.getString("org_name");
                String host = resultSet.getString("mbox_host");
                domains.add(new DomainDto(vhId, name, host, schemaName));
            }
        });
        return domains;
    }

    private String getSchemaName(Long virtualHostId) {
        return "s" + String.format("%011d", virtualHostId);
    }
}
