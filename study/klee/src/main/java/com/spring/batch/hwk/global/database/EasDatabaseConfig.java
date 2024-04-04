package com.spring.batch.hwk.global.database;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class EasDatabaseConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.eas")
    public DataSource easDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public PlatformTransactionManager easTransactionManager() {
        return new DataSourceTransactionManager(easDataSource());
    }
}
