package net.sinam.bxm.codegenerator.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Data
@Slf4j
@Component
public class ArgumentConfig {

    @Value("${url}")
    String url;
    @Value("${user-name}")
    String userName;
    @Value("${password}")
    String password;
    @Value("${schema-name}")
    String schemaName;
    @Value("${table-name}")
    String tableName;
    @Value("${base-package}")
    String basePackage;
    @Value("${ignored-table-names}")
    String ignoredTableNames;
    @Value("${output-dir}")
    String outputDirectory;

    public void validateArguments(){
        if (!StringUtils.hasText(url)){
            throw new IllegalArgumentException("url not provided");
        }
        if (!StringUtils.hasText(userName)){
            throw new IllegalArgumentException("userName not provided");
        }
        if (!StringUtils.hasText(password)){
            throw new IllegalArgumentException("password not provided");
        }
        if (!StringUtils.hasText(basePackage)){
            throw new IllegalArgumentException("basePackage not provided");
        }
        if (!StringUtils.hasText(schemaName) && !StringUtils.hasText(tableName)){
            throw new IllegalArgumentException("schemaName or tableName should be provided");
        }
    }
}
