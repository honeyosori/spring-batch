package com.spring.batch.extension;

import com.spring.batch.order.module.dao.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class OrderIdRangePartitioner implements Partitioner {

    private final OrderRepository orderRepository;
    private final Integer startDate;
    private final Integer endDate;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        long min = orderRepository.findMinId(startDate, endDate);
        long max = orderRepository.findMaxId(startDate, endDate);
        long targetSize = (max - min) / gridSize + 1;

        Map<String, ExecutionContext> result = new HashMap<>();
        long number = 0;
        long start = min;
        long end = start + targetSize - 1;

        while (start <= max) {
            ExecutionContext value = new ExecutionContext();
            result.put("partition" + number, value);

            if (end >= max) {
                end = max;
            }

            value.putLong("minId", start); // 각 파티션마다 사용될 minId
            value.putLong("maxId", end); // 각 파티션마다 사용될 maxId
            start += targetSize;
            end += targetSize;
            number++;
        }

        return result;
    }
}
