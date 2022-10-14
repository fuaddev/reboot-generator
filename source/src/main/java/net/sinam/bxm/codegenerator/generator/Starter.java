package net.sinam.bxm.codegenerator.generator;

import java.io.FileNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.sinam.bxm.codegenerator.config.ArgumentConfig;
import net.sinam.bxm.codegenerator.config.ResourceReaderImpl;
import net.sinam.bxm.codegenerator.model.ColumnInfoModel;
import net.sinam.bxm.codegenerator.model.FkInfoModel;
import net.sinam.bxm.codegenerator.model.TableInfoModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Log4j2
@RequiredArgsConstructor
public class Starter {
    final ResourceReaderImpl resourceReader;
    final JdbcTemplate jdbcTemplate;
    final ArgumentConfig argumentConfig;
    final EntityGenerator entityGenerator;

    public void startGeneration() throws FileNotFoundException {
        String sqlTables = resourceReader.readAllContent("sqlAllTables.sql");
        sqlTables = sqlTables.replace("{0}", argumentConfig.getSchemaName());

        if (StringUtils.hasText(argumentConfig.getTableName())) {
            sqlTables += " and LOWER(name) = LOWER('" + argumentConfig.getTableName() + "') ";
        }
        log.info(sqlTables);
        List<TableInfoModel> tableInfoModelList =
                jdbcTemplate.query(sqlTables, new BeanPropertyRowMapper<>(TableInfoModel.class));
                jdbcTemplate.query(sqlTables, new BeanPropertyRowMapper<TableInfoModel>(TableInfoModel.class));
        String[] ignoredTableNames = getIgnoredTableNames();
        removeIgnoredTables(tableInfoModelList, ignoredTableNames);
        for (TableInfoModel tableInfo : tableInfoModelList) {
            log.info(tableInfo);
            String sqlColumns = resourceReader.readAllContent("sqlByTable.sql");
            sqlColumns = sqlColumns.replace("{0}", tableInfo.getTableName())
                    .replace("{1}", tableInfo.getTableSchema());
            log.info(sqlColumns);
            List<ColumnInfoModel> columnInfoModelList =
                    jdbcTemplate.query(sqlColumns, new BeanPropertyRowMapper<>(ColumnInfoModel.class));
            String sqlForeignKeys = resourceReader.readAllContent("sqlAllTablesForeignKeys.sql");
            List<FkInfoModel> fkInfoModels =
                    jdbcTemplate.query(sqlForeignKeys, new BeanPropertyRowMapper<>(FkInfoModel.class));
            entityGenerator.generateEntity(tableInfo, columnInfoModelList, fkInfoModels);
        }
    }

    private void removeIgnoredTables(List<TableInfoModel> tableInfoModelList, String[] ignoredTableNames) {
        if (ignoredTableNames != null) {
            for (String ignoredTableName : ignoredTableNames) {
                String[] schemaTableName = ignoredTableName.split("\\.");
                TableInfoModel tableInfoModelToRemove = tableInfoModelList.stream()
                        .filter(tableInfoModel -> tableInfoModel.getTableName().equalsIgnoreCase(schemaTableName[1]) &&
                                tableInfoModel.getTableSchema().equalsIgnoreCase(schemaTableName[0]))
                        .findFirst().orElse(null);
                tableInfoModelList.remove(tableInfoModelToRemove);
            }
        }
    }

    private String[] getIgnoredTableNames() {
        if (StringUtils.hasText(argumentConfig.getIgnoredTableNames())) {
            if (argumentConfig.getIgnoredTableNames().contains(",")) {
                return argumentConfig.getIgnoredTableNames().split(",");
            } else {
                return new String[] {argumentConfig.getIgnoredTableNames()};
            }
        }
        return null;
    }
}
