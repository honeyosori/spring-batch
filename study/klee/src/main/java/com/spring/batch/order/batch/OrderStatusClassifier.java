package com.spring.batch.order.batch;

import com.spring.batch.order.module.constants.OrderStatus;
import com.spring.batch.order.module.domain.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.classify.Classifier;

@Slf4j
public class OrderStatusClassifier implements Classifier<Order, ItemProcessor<Order, Order>> {

    private ItemProcessor<Order, Order> orderItemProcessor;
    private ItemProcessor<Order, Order> cancelItemProcessor;

    public OrderStatusClassifier(ItemProcessor<Order, Order> orderItemProcessor, ItemProcessor<Order, Order> cancelItemProcessor) {
        this.orderItemProcessor = orderItemProcessor;
        this.cancelItemProcessor = cancelItemProcessor;
    }

    @Override
    public ItemProcessor<Order, Order> classify(Order classifiable) {

        log.info("Classifying order with status: {} (id = {})", classifiable.getStatus(), classifiable.getId());
        if(classifiable.getStatus().equals(OrderStatus.ORDERED)) {
            return orderItemProcessor;
        } else {
            return cancelItemProcessor;
        }
    }
}
