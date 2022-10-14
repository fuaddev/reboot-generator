package net.sinam.bxm.codegenerator.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.sinam.bxm.codegenerator.config.ArgumentConfig;
import net.sinam.bxm.codegenerator.config.Constants;
import net.sinam.bxm.codegenerator.config.ResourceReader;
import net.sinam.bxm.codegenerator.config.ResourceReaderImpl;
import net.sinam.bxm.codegenerator.model.ColumnInfoModel;
import net.sinam.bxm.codegenerator.model.FkInfoModel;
import net.sinam.bxm.codegenerator.model.TableInfoModel;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Log4j2
@RequiredArgsConstructor
public class EntityGenerator {

    final ArgumentConfig argumentConfig;
    final ResourceReader resourceReader;
    final DirectoryManager directoryManager;

    public void generateEntity(TableInfoModel tableInfo, List<ColumnInfoModel> columnInfoModelList,
                               List<FkInfoModel> fkInfoModels) throws FileNotFoundException {
        String fileContent = resourceReader.readAllContent("Entity.template");
        fileContent = fileContent.replace(Constants.TableName, tableInfo.getTableName())
                .replace(Constants.SchemaName, tableInfo.getTableSchema())
                .replace(Constants.BASE_PACKAGE, argumentConfig.getBasePackage());
        fileContent = fileContent
                .replace(Constants.ColumnList, GenerateColumns(tableInfo, columnInfoModelList, fkInfoModels));
        String folderPath = "";
        directoryManager.addFile(folderPath, tableInfo.getTableName() + "Entity.java", fileContent);

        return;
        //TODO: implementing optional Listener classes
        /*
        if (checkExist(tableInfo.getTableName() + "Listener",
                argumentConfig.getBasePackage(), tableInfo.getTableSchema())) {
            return;
        }
        String eventFileContent = resourceReader.readAllContent("Event.template");
        eventFileContent = eventFileContent.replace(Constants.TableName, tableInfo.getTableName())
                .replace(Constants.SchemaName, tableInfo.getTableSchema())
                .replace(Constants.EnterNewLine, Constants.NewLine)
                .replace(Constants.BASE_PACKAGE, argumentConfig.getBasePackage());
        String eventFolderPath = directoryManager.prepareDirectoryForPackage(
                argumentConfig.getBasePackage() + "." + argumentConfig.getSchemaName().toLowerCase() + "." +
                        "entity.event");
        directoryManager.addFile(eventFolderPath, tableInfo.getTableName() + "Listener.java", eventFileContent);
        */
    }

    public String GenerateColumns(TableInfoModel tableInfo, List<ColumnInfoModel> columnInfoModelList,
                                  List<FkInfoModel> fkInfoModels) {
        String columnList = Constants.NewLine;
        for (ColumnInfoModel columnInfo : columnInfoModelList) {
            columnList += GenerateColumn(tableInfo, columnInfo);
        }
        columnList += generateEntityRelationColumns(fkInfoModels, tableInfo);
        return columnList;
    }

    public String GenerateColumn(TableInfoModel tableInfo, ColumnInfoModel columnInfoModel) {
        log.info(columnInfoModel);
        String columnDefinition = "";

        if (columnInfoModel.getDescription() != null) {
            columnDefinition += "/**" + Constants.NewLine +
                    "*   " + columnInfoModel.getDescription() + Constants.NewLine +
                    "*/" + Constants.NewLine;
        }

        if (columnInfoModel.getIsPrimaryKey() == 1) {
            columnDefinition += "    @Id" + Constants.NewLine;
            if (columnInfoModel.getDataType().equalsIgnoreCase("uniqueidentifier")) {
                columnDefinition += "    @Type(type = \"org.hibernate.type.UUIDCharType\")" + Constants.NewLine;
                if (Integer.valueOf(1).equals(columnInfoModel.getContainsDefault())) {
                    columnDefinition += "    @GeneratedValue(generator = \"UUID\")" + Constants.NewLine;
                    columnDefinition +=
                            "    @GenericGenerator(name = \"UUID\", strategy = \"org.hibernate.id.UUIDGenerator\")"
                                    + Constants.NewLine;
                }
            } else if (columnInfoModel.getIsIdentity() == 1) {
                columnDefinition += "    @GeneratedValue(strategy = GenerationType.IDENTITY)" + Constants.NewLine;
            }
        }
        if (columnInfoModel.getIsPrimaryKey() == 0 &&
                columnInfoModel.getDataType().equalsIgnoreCase("uniqueidentifier")) {
            columnDefinition += "    @Type(type = \"org.hibernate.type.UUIDCharType\")" + Constants.NewLine;
        }
        if (columnInfoModel.getColumnName().equalsIgnoreCase("JpaVersion")) {
            columnDefinition += "    @Version" + Constants.NewLine;

        }
        columnDefinition += getColumnAnnotation(tableInfo, columnInfoModel);
        columnDefinition += "    private ";
        columnDefinition += getJavaType(columnInfoModel);
        columnDefinition +=
                " " + getFieldName(columnInfoModel.getColumnName()) + ";" + Constants.NewLine;
        columnDefinition += Constants.NewLine;
        return columnDefinition;
    }

    @SneakyThrows
    public String fixJpaFieldName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new Exception("name param is null");
        }
        if (name.length() < 3) {
            return name;
        }
        boolean hasCapitalLetters;
        do {
            hasCapitalLetters = false;
            for (int i = 0; i < name.length() - 2; i++) {
                if (Character.isUpperCase(name.charAt(i))
                        && Character.isUpperCase(name.charAt(i + 1))
                        && Character.isUpperCase(name.charAt(i + 2))) {
                    name = name.substring(0, i + 1)
                            + Character.toLowerCase(name.charAt(i + 1))
                            + name.substring(i + 2);
                    hasCapitalLetters = true;
                    break;
                }
            }
        }
        while (hasCapitalLetters);
        return name;
    }

    public String pluralizeWord(String name) {
        if (name.length() > 1 && name.charAt(name.length() - 1) == 'y') {
            return name.substring(0, name.length() - 1) + "ies";
        } else {
            return name + "s";
        }
    }

    public String getFieldName(String dbName) {
        return tableNameToCamelCase(fixJpaFieldName(dbName));
    }

    @SneakyThrows
    private String generateEntityRelationColumns(List<FkInfoModel> fkInfoModels,
                                                 TableInfoModel tableInfo) {
        final String entitySuf = "Entity";
        StringBuilder columnDefinition = new StringBuilder();

        for (FkInfoModel fkInfoModel : fkInfoModels) {
            if (fkInfoModel.getParentTableSchemaName().equals(tableInfo.getTableSchema())
                    && fkInfoModel.getParentTableName().equals(tableInfo.getTableName())
                    && !fkInfoModel.getForeignKey().contains("onetoone")) {
                columnDefinition.append("    @ToString.Exclude").append(Constants.NewLine);
                columnDefinition.append("    @EqualsAndHashCode.Exclude").append(Constants.NewLine);
                columnDefinition.append("    @ManyToOne(fetch = FetchType.LAZY)").append(Constants.NewLine);
                columnDefinition.append("    @JoinColumn(name = \"").append(fkInfoModel.getParentTableColumn())
                        .append("\"").append(", referencedColumnName = \"")
                        .append(fkInfoModel.getReferencedTableColumn())
                        .append("\", insertable = false, updatable = false)").append(Constants.NewLine);
                columnDefinition.append("    private ");
                columnDefinition.append(fkInfoModel.getReferencedTableName()).append(entitySuf).append(" ");
                columnDefinition.append(getFieldName(fkInfoModel.getReferencedTableName())).append(";")
                        .append(Constants.NewLine);
                columnDefinition.append(Constants.NewLine);
            }
            if (fkInfoModel.getParentTableSchemaName().equals(tableInfo.getTableSchema())
                    && fkInfoModel.getReferencedTableName().equals(tableInfo.getTableName())
                    && !fkInfoModel.getForeignKey().contains("onetoone")) {
                columnDefinition.append("    @ToString.Exclude").append(Constants.NewLine);
                columnDefinition.append("    @EqualsAndHashCode.Exclude").append(Constants.NewLine);
                columnDefinition.append("    @OneToMany(fetch = FetchType.LAZY, mappedBy = \"")
                        .append(getFieldName(fkInfoModel.getReferencedTableName())).append("\")")
                        .append(Constants.NewLine);
                columnDefinition.append("    private Set<");
                columnDefinition.append(fkInfoModel.getParentTableName()).append(entitySuf).append("> ");
                columnDefinition.append(pluralizeWord(getFieldName(fkInfoModel.getParentTableName()))).append(";")
                        .append(Constants.NewLine);
                columnDefinition.append(Constants.NewLine);
            }
            if (fkInfoModel.getParentTableSchemaName().equals(tableInfo.getTableSchema())
                    && fkInfoModel.getReferencedTableName().equals(tableInfo.getTableName())
                    && fkInfoModel.getForeignKey().contains("onetoone")) {
                columnDefinition.append("    @ToString.Exclude").append(Constants.NewLine);
                columnDefinition.append("    @EqualsAndHashCode.Exclude").append(Constants.NewLine);
                columnDefinition.append("    @OneToOne(fetch = FetchType.LAZY, mappedBy = \"")
                        .append(getFieldName(fkInfoModel.getReferencedTableName())).append("\")")
                        .append(Constants.NewLine);
                columnDefinition.append("    private ");
                columnDefinition.append(fkInfoModel.getParentTableName()).append(entitySuf).append(" ");
                columnDefinition.append(getFieldName(fkInfoModel.getParentTableName())).append(";")
                        .append(Constants.NewLine);
                columnDefinition.append(Constants.NewLine);
            }

            if (fkInfoModel.getParentTableSchemaName().equals(tableInfo.getTableSchema())
                    && fkInfoModel.getParentTableName().equals(tableInfo.getTableName())
                    && fkInfoModel.getForeignKey().contains("onetoone")) {
                columnDefinition.append("    @ToString.Exclude").append(Constants.NewLine);
                columnDefinition.append("    @EqualsAndHashCode.Exclude").append(Constants.NewLine);
                columnDefinition.append("    @OneToOne(fetch = FetchType.LAZY)").append(Constants.NewLine);
                columnDefinition.append("    @JoinColumn(name = \"").append(fkInfoModel.getParentTableColumn())
                        .append("\"").append(", referencedColumnName = \"")
                        .append(fkInfoModel.getReferencedTableColumn())
                        .append("\", insertable = false, updatable = false)").append(Constants.NewLine);
                columnDefinition.append("    private ");
                columnDefinition.append(fkInfoModel.getReferencedTableName()).append(entitySuf).append(" ");
                columnDefinition.append(getFieldName(fkInfoModel.getReferencedTableName())).append(";")
                        .append(Constants.NewLine);
                columnDefinition.append(Constants.NewLine);
            }
        }
        return columnDefinition.toString();

    }

    private String getColumnAnnotation(TableInfoModel tableInfo, ColumnInfoModel columnInfoMode) {
        String columnAnnotation = "    @Column(nullable = " + stringToJavaBool(columnInfoMode.getIsNullable());
        columnAnnotation += ", name = \"" + columnInfoMode.getColumnName() + "\"";
        if (columnInfoMode.getNumericScale() != null) {
            columnAnnotation += ", scale = " + columnInfoMode.getNumericScale().toString();
        }
        if (columnInfoMode.getIsComputed() == 1) {
            columnAnnotation += ", insertable = false, updatable = false";
        }
        if (columnInfoMode.getNumericPrecision() != null) {
            columnAnnotation += ", precision = " + columnInfoMode.getNumericPrecision().toString();
        }
        if (columnInfoMode.getCharacterMaximumLength() != null && isStringType(columnInfoMode)) {
            columnAnnotation += ", length = " + columnInfoMode.getCharacterMaximumLength().toString();
        }
        return columnAnnotation + ")" + Constants.NewLine;
    }

    private String intToJavaBool(Integer isTrue) {
        if (isTrue == Integer.valueOf(1)) {
            return "true";
        } else {
            return "false";
        }
    }

    @SneakyThrows
    private String stringToJavaBool(String isTrue) {
        if (!StringUtils.hasText(isTrue)) {
            throw new Exception(isTrue + " is empty or null");
        }
        if (isTrue.toUpperCase().equals("YES")) {
            return "true";
        }
        if (isTrue.toUpperCase().equals("NO")) {
            return "false";
        }
        throw new Exception(isTrue + " is not valid bool value");
    }

    private Boolean isStringType(ColumnInfoModel columnInfoModel) {
        if (columnInfoModel.getDataType().toLowerCase().contains("char")) {
            return true;
        }
        if (columnInfoModel.getDataType().toLowerCase().contains("text")) {
            return true;
        }
        return false;
    }

    @SneakyThrows
    private String getJavaType(ColumnInfoModel columnInfoModel) {
        switch (columnInfoModel.getDataType().toLowerCase()) {
            case "varchar":
            case "nvarchar":
            case "char":
            case "nchar":
            case "text":
                return "String";
            case "uniqueidentifier":
                return "UUID";
            case "varbinary":
                return "byte[]";
            case "bit":
                return "Boolean";
            case "date":
                return "LocalDate";
            case "datetime":
            case "datetime2":
                return "LocalDateTime";
            case "int":
                return "Integer";
            case "float":
                return "Float";
            case "geography":
                return "byte[]";
            case "decimal":
                return "BigDecimal";
            case "datetimeoffset":
                return "ZoneDateTime";
            default:
                throw new Exception("could not determine sql type " + columnInfoModel.getDataType());
        }
    }

    @SneakyThrows
    private String tableNameToCamelCase(String name) {
        if (!StringUtils.hasText(name)) {
            throw new Exception("name param is null");
        }
        return name.substring(0, 1).toLowerCase() + name.substring(1);

    }

    @SneakyThrows
    private List<String> getExistListenerClassNames(String basePackage, String schemaName) {
        List<String> list = new ArrayList<>();
        String packageUrl = directoryManager.prepareDirectoryForPackage(
                basePackage + "." + schemaName + "." + "entity.event");
        File dir = new File(packageUrl);
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            list.add(file.getName().split("\\.")[0]);
        }
        return list;
    }

    private boolean checkExist(String className, String basePackage, String schemaName) {
        for (String listenerName : getExistListenerClassNames(basePackage, schemaName)) {
            if (listenerName.equals(className)) {
                return true;
            }
        }
        return false;
    }
}
