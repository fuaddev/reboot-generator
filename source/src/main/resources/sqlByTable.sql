select * from (
                  SELECT Distinct cl.COLUMN_NAME, cl.IS_NULLABLE,cl.DATA_TYPE,cl.CHARACTER_MAXIMUM_LENGTH
                                ,cl.NUMERIC_PRECISION,cl.NUMERIC_SCALE,cl.DATETIME_PRECISION
                                ,COLUMNPROPERTY(OBJECT_ID(cl.TABLE_SCHEMA+'.'+cl.TABLE_NAME), cl.COLUMN_NAME, 'IsIdentity') AS IS_IDENTITY
                                ,CASE
                                     WHEN tc.CONSTRAINT_TYPE IS NULL THEN 0
                                     ELSE 1
                      END IS_FOREIGN_KEY
                                , ORDINAL_POSITION
                                , COLUMNPROPERTY(OBJECT_ID(cl.TABLE_SCHEMA+'.'+cl.TABLE_NAME),cl.COLUMN_NAME,'IsComputed') AS IS_COMPUTED
                                , CASE WHEN pk.TABLE_NAME is null then 0 else 1 end as IS_PRIMARY_KEY
                                , (select 1
                                   from sys.default_constraints con
                                            left outer join sys.objects t
                                                            on con.parent_object_id = t.object_id
                                            left outer join sys.all_columns col
                                                            on con.parent_column_id = col.column_id
                                                                and con.parent_object_id = col.object_id
                                   where schema_name(t.schema_id) = cl.TABLE_SCHEMA
                                     AND col.[name] = cl.COLUMN_NAME
                                     AND t.[name] = cl.TABLE_NAME) as CONTAINS_DEFAULT,
                      des.[Description]
                  FROM   INFORMATION_SCHEMA.COLUMNS cl
                             LEFT JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu
                                       ON  ccu.TABLE_NAME = cl.TABLE_NAME
                                           AND ccu.COLUMN_NAME = cl.COLUMN_NAME
                                           AND EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc2 WHERE tc2.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME AND tc2.CONSTRAINT_TYPE='FOREIGN KEY')
                             LEFT JOIN INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
                                       ON  ccu.TABLE_NAME = tc.TABLE_NAME
                                           AND ccu.CONSTRAINT_NAME = tc.CONSTRAINT_NAME
                             LEFT JOIN (
                      SELECT K.TABLE_NAME, C.CONSTRAINT_TYPE, K.COLUMN_NAME, K.CONSTRAINT_NAME,  K.CONSTRAINT_CATALOG
                      FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS AS C
                               JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE AS K
                                    ON C.TABLE_NAME = K.TABLE_NAME
                                        AND C.CONSTRAINT_CATALOG = K.CONSTRAINT_CATALOG
                                        AND C.CONSTRAINT_SCHEMA = K.CONSTRAINT_SCHEMA
                                        AND C.CONSTRAINT_NAME = K.CONSTRAINT_NAME
                      WHERE C.CONSTRAINT_TYPE = 'PRIMARY KEY'
                  ) pk on cl.TABLE_NAME = pk.TABLE_NAME and cl.TABLE_CATALOG = pk.CONSTRAINT_CATALOG and pk.COLUMN_NAME = cl.COLUMN_NAME
                             LEFT JOIN ( select schema_name(st.schema_id) schema_name,
                                                st.name [TableName],
                                                sc.name [ColumnName],
                                                sep.value [Description]
                                         from sys.tables st
                                                  inner join sys.columns sc on st.object_id = sc.object_id
                                                  left join sys.extended_properties sep on st.object_id = sep.major_id
                                             and sc.column_id = sep.minor_id
                                             and sep.name = 'MS_Description'
                  ) des
                                       ON des.schema_name = cl.TABLE_SCHEMA and des.TableName = cl.TABLE_NAME and des.ColumnName=cl.COLUMN_NAME
                  WHERE  (cl.TABLE_NAME = '{0}' and cl.TABLE_SCHEMA='{1}')
              ) ff
ORDER BY ORDINAL_POSITION;


