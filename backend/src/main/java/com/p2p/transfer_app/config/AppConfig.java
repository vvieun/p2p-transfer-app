package com.p2p.transfer_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;

@Configuration
@EnableAspectJAutoProxy
public class AppConfig 
{
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) 
    {
        return new JdbcTemplate(dataSource);
    }
}
