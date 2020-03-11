package com.blkx.server.config;

import com.blkx.server.beans.DataSourceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ComponentScan("com.blkx.server.beans")
public class DatasourceConfig {

    private DataSourceRegistry registry;

    @Autowired
    public DatasourceConfig(DataSourceRegistry registry) {
        this.registry = registry;
    }

    @Bean(name = "postgres")
    public DataSource createDataSource() {
        System.out.println("Creating data source");
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url("jdbc:postgresql://localhost:5432/blk_x");
        dataSourceBuilder.username("postgres");
        dataSourceBuilder.password("blkpassword");
        DataSource dataSource = dataSourceBuilder.build();
        registry.registerDataSource("postgres", dataSource);
        return dataSource;
    }

    @Bean(name = "postgres2")
    public DataSource createDataSourceAgain() {
        System.out.println("Creating data source again");
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url("jdbc:postgresql://localhost:5432/blk_x2");
        dataSourceBuilder.username("postgres");
        dataSourceBuilder.password("blkpassword");
        DataSource dataSource = dataSourceBuilder.build();
        registry.registerDataSource("postgres2", dataSource);
        return dataSource;
    }
}
