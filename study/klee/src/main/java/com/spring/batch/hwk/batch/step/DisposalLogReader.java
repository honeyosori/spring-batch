package com.spring.batch.hwk.batch.step;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.batch.hwk.module.DomainDto;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
public class DisposalLogReader<T> implements ItemReader<T> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Iterator<DomainDto> domainIterator;

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if(domainIterator != null && domainIterator.hasNext()) {
            return (T) domainIterator.next();
        } else {
            return null;
        }
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) throws JsonProcessingException {
        Object domains =  stepExecution.getJobExecution().getExecutionContext().get("domains");
        if(domains != null) {
            List<DomainDto> domainDto = objectMapper.readValue(domains.toString(), new TypeReference<List<DomainDto>>() {});
            this.domainIterator = domainDto.iterator();
        }
    }
}
