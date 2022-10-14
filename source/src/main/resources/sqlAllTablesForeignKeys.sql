Select FK_COLUMNS.*, parent_column.DATA_TYPE as PARENT_DATA_TYPE
, referenced_column.DATA_TYPE as REFERENCED_DATA_TYPE
FROM (
SELECT
   OBJECT_NAME(constraint_object_id) AS 'FOREIGN_KEY',
   OBJECT_SCHEMA_NAME(parent_object_id) AS 'PARENT_TABLE_SCHEMA_NAME',
   OBJECT_NAME(parent_object_id) AS 'PARENT_TABLE_NAME',
   COL_NAME(parent_object_id, g.parent_column_id) as 'PARENT_TABLE_COLUMN',
   OBJECT_SCHEMA_NAME(referenced_object_id) AS 'REFERENCED_TABLE_SCHEMA_NAME',
   OBJECT_NAME(referenced_object_id) AS 'REFERENCED_TABLE_NAME',
   COL_NAME(referenced_object_id, g.referenced_column_id) as 'REFERENCED_TABLE_COLUMN'
FROM sys.foreign_key_columns g ) FK_COLUMNS
inner join INFORMATION_SCHEMA.COLUMNS parent_column
on FK_COLUMNS.PARENT_TABLE_NAME = parent_column.TABLE_NAME
	and FK_COLUMNS.PARENT_TABLE_COLUMN = parent_column.COLUMN_NAME
	and FK_COLUMNS.PARENT_TABLE_SCHEMA_NAME = parent_column.TABLE_SCHEMA
inner join INFORMATION_SCHEMA.COLUMNS referenced_column
on FK_COLUMNS.REFERENCED_TABLE_NAME = referenced_column.TABLE_NAME
	and FK_COLUMNS.REFERENCED_TABLE_COLUMN = referenced_column.COLUMN_NAME
	and FK_COLUMNS.REFERENCED_TABLE_SCHEMA_NAME = referenced_column.TABLE_SCHEMA
order by FK_COLUMNS.FOREIGN_KEY;