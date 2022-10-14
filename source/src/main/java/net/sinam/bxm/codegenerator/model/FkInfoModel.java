package net.sinam.bxm.codegenerator.model;

import lombok.Data;

@Data
public class FkInfoModel {

    private String foreignKey;
    private String parentTableSchemaName;
    private String parentTableName;
    private String parentTableColumn;
    private String referencedTableSchemaName;
    private String referencedTableName;
    private String referencedTableColumn;
    private String parentDataType;
    private String referencedDataType;

}
