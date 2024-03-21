package com.spring.batch.order.module.dao;

import com.spring.batch.order.module.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByDate(int date);


    @Query("SELECT MAX(o.id) " +
            "FROM Order o " +
            "WHERE o.date BETWEEN :startDate AND :endDate")
    Long findMaxId(@Param("startDate") Integer startDate,
                   @Param("endDate") Integer endDate);

    @Query("SELECT MIN(o.id) " +
            "FROM Order o " +
            "WHERE o.date BETWEEN :startDate AND :endDate")
    Long findMinId(@Param("startDate") Integer startDate,
                   @Param("endDate") Integer endDate);
}
