package net.sinam.bxm.codegenerator.config;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;


@Configuration
@RequiredArgsConstructor
public class JPAConfig {

    final ArgumentConfig argumentConfig;

    @Value("${spring.datasource.driverClassName}")
    private String driverClassName;


    @Bean
    public DataSource getDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(argumentConfig.getUrl())
                .username(argumentConfig.getUserName())
                .password(argumentConfig.getPassword())
                .build();
    }
}
