package com.alexcatarau.hba.config;

import org.jdbi.v3.core.Jdbi;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class AppConfig {

    @Bean
    @Profile("productionDataSource")
    public DataSource getDataSource() throws URISyntaxException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .username(username)
                .password(password)
                .url(dbUrl)
                .build();
    }

    @Bean
    public Jdbi getJdbi(DataSource dataSource) {
        return Jdbi.create(dataSource);
    }
}
