package com.spring.batch.order.module.application;

import com.spring.batch.order.module.dao.OrderRepository;
import com.spring.batch.order.module.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderFindService {

    private final OrderRepository orderRepository;

    public List<Order> findOrders(int date) {
        List<Order> orders = orderRepository.findAllByDate(date);

        for (Order order : orders) {
            log.info("주문 정보 : {}", order);
        }

        return orders;
    }
}
