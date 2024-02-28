package com.spring.batch.order.batch;

import org.springframework.batch.item.database.orm.AbstractJpaQueryProvider;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class QueryProvider extends AbstractJpaQueryProvider {

    private Integer date;

    @Override
    public Query createQuery() {
        EntityManager manager = getEntityManager();

        Query query = manager.createQuery("SELECT o FROM Order o WHERE o.date = :date order by o.id asc");
        query.setParameter("date", date);

        return query;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(date, "Date must be provided");
    }

    public void setDate(Integer date) {
        this.date = date;
    }
}
