package net.sinam.bxm.codegenerator.model;

import lombok.Data;

@Data
public class ColumnInfoModel {
    String columnName;
    String isNullable;
    String dataType;
    String description;
    Integer characterMaximumLength;
    Integer numericPrecision;
    Integer numericScale;
    Integer datetimePrecision;
    Integer isIdentity;
    Integer isForeignKey;
    int ordinalPosition;
    Integer isComputed;
    int isPrimaryKey;
    Integer containsDefault;
}
