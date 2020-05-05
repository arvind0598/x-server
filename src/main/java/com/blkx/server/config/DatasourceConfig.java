package com.blkx.server.config;

import com.blkx.server.beans.DataSourceRegistry;
import com.blkx.server.models.Database;
import com.blkx.server.services.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@ComponentScan("com.blkx.server.beans")
public class DatasourceConfig {

    @Autowired
    private DatabaseService databaseService;
    private DataSourceRegistry registry;

    @Autowired
    public DatasourceConfig(DataSourceRegistry registry) {
        this.registry = registry;
    }

    @Bean(name = "postgres6")
    public DataSource createDataSource() {
        System.out.println("Creating data source");
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url("jdbc:postgresql://localhost:5432/blk_x1");
        dataSourceBuilder.username("postgres");
        dataSourceBuilder.password("blkpassword");
        DataSource dataSource = dataSourceBuilder.build();
        registry.registerDataSource("postgres6", dataSource);
        databaseService.setDbRegistry("postgres6", "postgres://postgres:blkpassword@localhost:5432/blk_x1");
        return dataSource;
    }
//
//    @Bean(name = "postgres2")
//    public DataSource createDataSourceAgain() {
//        System.out.println("Creating data source again");
//        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
//        dataSourceBuilder.url("jdbc:postgresql://localhost:5432/blk_x2");
//        dataSourceBuilder.username("postgres");
//        dataSourceBuilder.password("blkpassword");
//        DataSource dataSource = dataSourceBuilder.build();
//        registry.registerDataSource("postgres2", dataSource);
//        databaseService.setDbRegistry("postgres2", "postgres://postgres:blkpassword@localhost:5432/blk_x2");
//        return dataSource;
//    }

    public DataSource createDataSourceDynamically(Database db) {
        System.out.println("Creating datasource dynamically");
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        String temp[] = db.getDatasourceUrl().split("@");
        String url = String.format("jdbc:postgresql://%s", temp[1]);
        dataSourceBuilder.url(url);
        dataSourceBuilder.username("postgres");//db.getUserName());
        dataSourceBuilder.password("blkpassword");//db.getPassword());
        DataSource dataSource = dataSourceBuilder.build();
        registry.registerDataSource(db.getDatasourceName(), dataSource);
        databaseService.setDbRegistry(db.getDatasourceName(), db.getDatasourceUrl());
        return dataSource;
    }
}
