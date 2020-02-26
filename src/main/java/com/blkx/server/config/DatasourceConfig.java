package com.blkx.server.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfig {

    @Bean
    public DataSource createDataSource() {
        System.out.println("Creating data source");
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url("jdbc:postgresql://localhost:5432/blk_x");
        dataSourceBuilder.username("postgres");
        dataSourceBuilder.password("blkpassword");
        return dataSourceBuilder.build();
    }
}
