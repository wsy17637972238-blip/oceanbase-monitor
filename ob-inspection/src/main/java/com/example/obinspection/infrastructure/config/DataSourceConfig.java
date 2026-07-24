package com.example.obinspection.infrastructure.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

/**
 * 数据源配置：H2 为主数据源（系统自身数据，含被巡检实例纳管表 inspection_instance）。
 * OceanBase 被巡检实例的连接不再由本类静态配置，改为按实例动态建池，
 * 见 {@link com.example.obinspection.infrastructure.collector.ObInstanceConnectionManager}。
 */
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.h2")
    public DataSource h2DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public JdbcTemplate h2JdbcTemplate(@Qualifier("h2DataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    /**
     * 手动执行 schema.sql / data.sql（多数据源下 spring.sql.init 自动装配不可靠）。
     * 注意：必须显式指定 UTF-8，否则 Windows 平台默认 GBK 读取脚本会导致中文乱码。
     */
    @Bean
    public SmartInitializingSingleton h2SchemaInitializer(@Qualifier("h2DataSource") DataSource ds) {
        return () -> {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("schema.sql"));
            populator.addScript(new ClassPathResource("data.sql"));
            populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
            populator.setContinueOnError(true);
            populator.execute(ds);
        };
    }
}
