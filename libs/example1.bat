set /p arg1=Enter schema name. empty for default schema (dbo)
set /p arg2=Enter table name to generate entity. empty for all tables in schema

if /i "%arg1%" == "" (
	set arg1=dbo
	)

java -jar RebootGenerator-1.0.jar --url=jdbc:sqlserver://localhost\SQLEXPRESS;databaseName=TestDB --user-name=fuad --password=pass123 --schema-name=%arg1% --base-package=net.sinam.bxm table-name=%arg2% --output-dir=entities
