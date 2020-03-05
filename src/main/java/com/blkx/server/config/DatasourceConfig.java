package com.blkx.server.config;

import com.blkx.server.models.DataSourceList;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfig {


    DataSourceList dataSourceList = DataSourceList.getInstance();

//    @Primary
    @Bean(name = "postgres")
    public DataSource createDataSource() {
        System.out.println("Creating data source");
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url("jdbc:postgresql://localhost:5432/blk_x");
        dataSourceBuilder.username("postgres");
        dataSourceBuilder.password("blkpassword");
        dataSourceList.put("postgres", dataSourceBuilder.build());
        return dataSourceBuilder.build();
    }

    @Bean(name = "postgres2")
    public DataSource createDataSourceAgain() {
        System.out.println("Creating data source again");
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url("jdbc:postgresql://localhost:5432/blk_x2");
        dataSourceBuilder.username("postgres");
        dataSourceBuilder.password("blkpassword");
        dataSourceList.put("postgres2", dataSourceBuilder.build());
        return dataSourceBuilder.build();
    }
}
