/*
Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/
package compass;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.stream.*;
import java.lang.Math;

import parser.*;

public class CompassAnalyze {
	public static CompassConfig cfg;
	public static CompassUtilities u = CompassUtilities.getInstance();

	// user-visible reporting groups: some of these are also in the .cfg file so must match exactly
	static final String DDLReportGroup         = "DDL";
	static final String DMLReportGroup         = "DML";
	static final String DatabasesReportGroup   = "Databases";
	static final String ViewsReportGroup       = "Views";
	static final String ProceduresReportGroup  = "Procedures";
	static final String FunctionsReportGroup   = "Functions";
	static final String TriggersReportGroup    = "Triggers";
	static final String ControlFlowReportGroup = "Control flow";
	static final String XMLReportGroup         = "XML";
	static final String JSONReportGroup        = "JSON";
	static final String HIERARCHYIDReportGroup = "HIERARCHYID";
	static final String CursorsReportGroup     = "Cursors";
	static final String UsersReportGroup       = "Users";
	static final String PermissionsReportGroup = "Permissions";
	static final String OperatorsReportGroup   = "Operators";
	static final String TransitionTableMultiDMLTrigFmt = "INSERTED/DELETED cannot be referenced in trigger with >1 action";
	static final String DMLTableSrcFmt         = "INSERT-SELECT FROM(";
	static final String VarAggrAcrossRowsFmt   = "Variable may aggregate across rows";
	static final String SpatialReportGroup     = "Geospatial";
	static final String SpatialMethodCallFmt   = "Spatial method";
	static final String WithRollupCubeOldSyntax= "GROUP BY...WITH ROLLUP/CUBE (old syntax)";
	static final String sqlcmdReportGroup      = "'sqlcmd' client utility features";
	static final String FullTextSearchReportGroup = "Fulltext Search";

	// for reporting items that do not fit elsewhere
	static final String MiscReportGroup       = "Miscellaneous SQL Features";

	// sections in the .cfg file: string must match exactly
	static final String AggregateFunctions    = "Aggregate functions";
	static final String BuiltInFunctions      = "Built-in functions";
	static final String ProcVersionBase       = "Procedure versioning ";
	static final String ProcVersionDeclare    = ProcVersionBase+"(declaration)";
	static final String ProcVersionExecute    = ProcVersionBase+"(execution)";
	static final String NumericAsDateTime     = "Numeric representation of datetime";
	static final String BinaryAsDateTime      = "Binary representation of datetime";
	static final String NumericDateTimeVarAssign = "Numeric assignment to datetime variable/parameter/column";
	static final String DateTimeToNumeric     = "Datetime converted to numeric";
	static final String DateTimeToBinary      = "Datetime converted to (VAR)BINARY";
	static final String Datatypes             = "Datatypes";
	static final String UDDatatypes           = "User-Defined Datatypes";
	static final String DatatypeConversion    = "Datatype conversion";
	static final String TableVariables        = "Table variables";
	static final String TableVariablesType    = "Table variables/Table types";
	static final String TableValueConstructor = "Table value constructor";
	static final String ParamValueDEFAULT     = "Parameter value DEFAULT";
	static final String ProcedureOptions      = "Procedure options";
	static final String ExecProcedureOptions  = "Execute procedure options";
	static final String ExecStringOptions     = "Execute string options";
	static final String TemporaryProcedures   = "Temporary procedures";
	static final String ExecuteSQLFunction    = "EXECUTE SQL function";
	static final String FunctionOptions       = "Function options";
	static final String ScalarUDFOptionalASKwd= "Missing AS keyword in scalar CREATE FUNCTION";
	static final String TriggerOptions        = "Trigger options";
	static final String TransitionTableMultiDMLTrig = "Transition table reference for multi-DML trigger";
	static final String TriggerSchemaName     = "Trigger created with schema name";
	static final String DDLTrigger            = "DDL TRIGGER";
	static final String EnableTrigger         = "ENABLE TRIGGER";
	static final String DisableTrigger        = "DISABLE TRIGGER";
	static final String ProcExecAsVariable    = "Variable procedure name";
	static final String DynamicSQL            = "Dynamic SQL";
	static final String DynamicSQLEXECStringReview    = "EXECUTE(string): dynamic SQL statements must be analyzed manually";
	static final String DynamicSQLEXECStringReviewKey = "EXECUTEstring";
	static final String DynamicSQLEXECSPReview        = "sp_executesql: dynamic SQL statements must be analyzed manually";
	static final String SystemStoredProcs     = "System Stored Procedures";
	static final String SystemFunctions       = "System Functions";
	static final String Catalogs              = "Catalogs";
	static final String InformationSchema     = "INFORMATION_SCHEMA";
	static final String XMLFeatures           = "XML features";
	static final String HIERARCHYIDFeatures   = "HIERARCHYID features";
	static final String Geospatial            = "Geospatial features";
	static final String JSONFeatures          = "JSON features";
	static final String GlobalTmpTableFmt     = "##globaltmptable";     // for display
	static final String GlobalTmpTable        = "Global Temporary Tables";
	static final String DropMultipleObjects   = "DROP multiple objects";
	static final String DropIfExists          = "DROP IF EXISTS";
	static final String DropIndex             = "DROP INDEX";
	static final String SetOptions            = "SET options";
	static final String SetMultipleOptions    = "SET, multiple options combined";
	static final String SelectTopWithTies     = "SELECT TOP WITH TIES";
	static final String SelectTopPercent      = "SELECT TOP PERCENT";
	static final String SelectTop             = "SELECT TOP";
	static final String SelectTopInTUDF       = "SELECT TOP in Table-Valued Function";
	static final String InsteadOfTrigger      = "Instead-Of Trigger";
	static final String CursorVariables       = "CURSOR variables";
	static final String CursorParameters      = "CURSOR parameters";
	static final String CursorOptions         = "Cursor options";
	static final String CursorGlobal          = "GLOBAL cursor";
	static final String CursorFetch           = "FETCH cursor";
	static final String DynamicCreateCursor   = "Dynamically created cursor";
	static final String NonPersistedCompCol   = "Non-PERSISTED computed columns";
	static final String CompColFeatures       = "Features in computed columns";
	static final String SUDFinTableDDL        = "Scalar UDF in table DDL";
	static final String IndexOptions          = "Index options";
	static final String MaxColumnsIndex       = "Maximum columns per index";
	static final String MaxProcParameters     = "Maximum parameters per procedure";
	static final String MaxFuncParameters     = "Maximum parameters per function";
	static final String MaxIdentityPrecision  = "Maximum precision IDENTITY column";
	static final String UDDForIdentity        = "User-defined datatype for IDENTITY";
	static final String TinyintForIdentity    = "TINYINT datatype for IDENTITY() function";
	static final String ViewOptions           = "View options";
	static final String IndexAttribute        = "Index attribute";
	static final String IgnoreDupkeyIndex     = "IGNORE_DUP_KEY index";
	static final String UniqueOnNullableCol   = "Nullable column";
	static final String ConstraintAttribute   = "Constraint attribute";
	static final String ColumnAttribute       = "Column attribute";
	static final String SQLGraph              = "SQL graph";
	static final String ForReplication        = "FOR REPLICATION";
	static final String NotForReplication     = "NOT FOR REPLICATION";
	static final String Partitioning          = "Partitioning";
	static final String InlineIndex           = "Inline index";
	static final String ClusteredIndex        = "CLUSTERED index";
	static final String HashIndex             = "NONCLUSTERED HASH index";
	static final String IndexedView           = "Indexed view";
	static final String MaterializedView      = "Materialized view";
	static final String DescConstraint        = "DESC constraint";
	static final String FKrefDBname           = "FK constraint referencing DB name";
	static final String SelectPivot           = "SELECT..PIVOT";
	static final String SelectUnpivot         = "SELECT..UNPIVOT";
	static final String SelectInto            = "SELECT..INTO";
	static final String LateralJoin           = "Lateral join";
	static final String QueryHint             = "Query hint";
	static final String TableHint             = "Table hint";
	static final String JoinHint              = "Join hint";
	static final String DoubleQuotedString    = "Double-quoted string";
	static final String UnQuotedString        = "Unquoted string";
	static final String LineContinuationChar  = "Line continuation character";
	static final String SetQuotedIdInBatch    = "SET QUOTED_IDENTIFIER in batch";
    static final String SetXactIsolationLevel = "SET TRANSACTION ISOLATION LEVEL";
	static final String GroupByAll            = "GROUP BY ALL";
	static final String RollupCubeOldSyntax   = "GROUP BY ROLLUP/CUBE (old syntax)";
	static final String ODBCScalarFunction    = "ODBC scalar function";
	static final String ODBCLiterals          = "ODBC literal";
	static final String ODBCOJ                = "ODBC Outer Join";
	static final String ODBCEscape            = "ODBC {ESCAPE} clause";
	static final String SelectTopWoOrderBy    = "SELECT TOP without ORDER BY";
	static final String SelectToClientWoOrderBy = "SELECT to client without ORDER BY";
	static final String ReadText              = "READTEXT";
	static final String WriteText             = "WRITETEXT";
	static final String UpdateText            = "UPDATETEXT";
	static final String CreateDatabaseOptions = "CREATE DATABASE options";
	static final String AlterDatabase         = "ALTER DATABASE";
	static final String AlterDatabaseOptions  = "ALTER DATABASE options";
	static final String DbccStatements        = "DBCC statements";
	static final String Traceflags            = "Traceflags";
	static final String LeadingDotsId         = "Leading dots in identifier";
	static final String CrossDbReference      = "Cross-database reference";
	static final String RemoteObjectReference = "Remote object reference";
	static final String SpecialColumNames     = "Special column names";
	static final String MaxIdentifierLength   = "Maximum identifier length";
	static final String SpecialCharsIdentifier= "Special characters in identifier";
	static final String SpecialCharsParameter = "Special characters in parameter";
	static final String Transactions          = "Transactions";
	static final String SecurityDefinerXact   = "SECURITY DEFINER transaction mgmt";
	static final String ExecuteAsRevert       = "EXECUTE AS";
	static final String InsertStmt            = "INSERT";
	static final String InsertBulkStmt        = "INSERT BULK";
	static final String BulkInsertStmt        = "BULK INSERT";
	static final String UpdateStmt            = "UPDATE";
	static final String DeleteStmt            = "DELETE";
	static final String MergeStmt             = "MERGE";
	static final String TruncateTableStmt     = "TRUNCATE TABLE";
	static final String UpdateStatisticsStmt  = "UPDATE STATISTICS";
	static final String TemporalTable         = "Temporal table";
	static final String WaitForStmt           = "WAITFOR";
	static final String CheckpointStmt        = "CHECKPOINT";
	static final String GotoStmt              = "GOTO";
	static final String GrantStmt             = "GRANT";
	static final String RevokeStmt            = "REVOKE";
	static final String DenyStmt              = "DENY";
	static final String SetuserStmt           = "SETUSER";
	static final String AlterAuthStmt         = "ALTER AUTHORIZATION";
	static final String AlterServerConfig     = "ALTER SERVER CONFIGURATION";
	static final String AddSignature          = "ADD SIGNATURE";
	static final String DropSignature         = "DROP SIGNATURE";
	static final String AddSensitivityClass   = "ADD SENSITIVITY CLASSIFICATION";
	static final String DropSensitivityClass  = "DROP SENSITIVITY CLASSIFICATION";
	static final String ColonColonFunctionCall= "::function call (old syntax)";
	static final String IFELSEblockDeclare    = "IF-ELSE-block containing only DECLARE";
	static final String IFblockDeclare        = "IF-block containing only DECLARE";   // for reporting
	static final String ELSEblockDeclare      = "IF-ELSE-block containing only DECLARE"; // for reporting
	static final String NextValueFor          = "NEXT VALUE FOR";
	static final String NextValueForContext   = "NEXT VALUE FOR context";
	static final String LoginOptions          = "Login options";
	static final String UserOptions           = "User options";
	static final String SchemaOptions         = "Schema options";
	static final String SequenceOptions       = "Sequence options";
	static final String AlterSchema           = "ALTER SCHEMA";
	static final String DbRoleOptions         = "DB role options";
	static final String DbRoles               = "DB roles";
	static final String AlterDbRole           = "ALTER ROLE";
	static final String CreateDbRole          = "CREATE ROLE";
	static final String SrvRoleOptions        = "Server role options";
	static final String AlterSrvRole          = "ALTER SERVER ROLE";
	static final String CreateSrvRole         = "CREATE SERVER ROLE";
	static final String ServiceBroker         = "Service Broker";
	static final String OpenKeyStmt           = "OPEN KEY";
	static final String CloseKeyStmt          = "CLOSE KEY";
	static final String Collations            = "Collations";
	static final String CaseSensitiveCollation= "Case-sensitive collation";
	static final String DBAStmts              = "DBA statements";
	static final String MiscObjects           = "Miscellaneous objects";
	static final String MoneyLiteral          = "MONEY literal";
	static final String AtAtVariable          = "@@variable";
	static final String VarDeclareAtAt        = "Regular variable named @@v";
	static final String ProcParDeclareAtAt    = "Procedure parameter named @@v";
	static final String FuncParDeclareAtAt    = "Function parameter named @@v";
	static final String FuncTypeDeclareAtAt   = "Function return table named @@v";
	static final String NamedArgAtAt          = "Named procedure call argument named @@v";
	static final String AtAtErrorValueRef     = "@@ERROR value";
	static final String AlterIndex            = "ALTER INDEX";
	static final String AlterTable            = "ALTER TABLE";
	static final String AlterTableAddMultiple = "ADD multiple columns/constraints";
	static final String TimestampColumnSolo   = "TIMESTAMP column without column name";
	static final String NumericColNonNumDft   = "NUMERIC/DECIMAL column with non-numeric default";
	static final String DMLTableSrc           = "DML Table Source";
	static final String AtTimeZone            = "expression AT TIME ZONE";
	static final String VarAggrAcrossRows     = "Variable aggregates across rows";
	static final String VarAssignDependency   = "Variable assignment dependency";
	static final String TSQLOJ                = "T-SQL Outer Join operator";
	static final String CompoundOpWhitespace  = "Compound operator containing whitespace";
	static final String CompoundOpWhitespaceFmt = "Operator containing whitespace";
	static final String PGOpWhitespace        = "PG operator requiring whitespace";
	static final String PGOpWhitespaceFmt     = "Operator requiring whitespace";
	static final String ComparisonOperator    = "Comparison operator";
	static final String UnaryStringPlusOp     = "Unary string '+' operator";
	static final String LikeSquareBrackets    = "LIKE '[...]'";
	static final String LikeSquareBracketsCfg = "LIKE '[...";  // for checking against the cfg file; when reading it we lose the closing square bracket and  beyond
	static final String StringAggWithinGroup  = "STRING_AGG() WITHIN GROUP";
	static final String SyntaxIssues          = "Syntax Issues";
	static final String FormatCulture         = "FORMAT() culture";
	static final String DistinctFromOperator  = "DISTINCT FROM";
	static final String sqlcmdCommand         = "sqlcmd command";
	static final String sqlcmdVariable        = "sqlcmd variable";
	static final String RaiserrorSybase       = "RAISERROR (Sybase syntax)";
	static final String ExtendedPropType      = "Extended property type";
	static final String FullTextIndex         = "FULLTEXT INDEX";
	static final String FullTextContains      = "FULLTEXT CONTAINS";
	static final String StringAggXMLPath      = "STRING_AGG() workaround with FOR XML PATH";
	static final String StringAggXMLPathMultCols = "STRING_AGG() workaround with FOR XML PATH with multiple SELECT columns";

	// matching special values in the .cfg file
	static final String cfgNonZero            = "NONZERO";
	static final String cfgVariable           = "VARIABLE";
	static final String cfgExpression         = "EXPRESSION";
	static final String cfgXmlMethodCall      = "XML_METHOD_CALL";
	static final String cfgHierachyIdMethodCall = "HIERACHYID_METHOD_CALL";
	static final String cfgScalarUdfCall      = "SCALAR_UDF_CALL";
	static final String cfgXMLSchema          = "XML(xmlschema)";
	static final String cfgStringConcatPlus   = "STRING_CONCAT_PLUS";
	static final String cfgCastDatetime       = "DATETIME";
	static final String cfgConvertDatetime    = "DATETIME";
	static final String cfgDoubleQuotedString                = "STRING";
	static final String cfgDoubleQuoteEmbeddedSingleQuote    = "EMBEDDED_SINGLE_QUOTE";
	static final String cfgDoubleQuoteEmbeddedDoubleQuote    = "EMBEDDED_DOUBLE_QUOTE";
	static final String cfgDMLTabVarCorrNameUDFError         = " TABVAR VIA CORRELATION IN FUNCTION";
	static final String DMLTabVarCorrNameUDFErrorText        = " in SQL function, on table variable via correlation name may raise run-time error";
	static final String cfgUpdateCorrColumnUnqualifiedError  = "UPDATE CORRELATION SET UNQUALIFIED COLUMN";
	static final String UpdateCorrColumnUnqualifiedErrorText = "UPDATE of correlation name with unqualified column name in SET expression may raise run-time error";
	static final String cfgUpdateQualifiedSetColumnError     = "UPDATE QUALIFIED SET COLUMN";
	static final String UpdateQualifiedSetColumnErrorText    = "UPDATE of qualified SET column name may raise run-time error";
	static final String ScalarUDFOptionalAsKwdUDD            = "USER-DEFINED DATATYPE";
	static final String NoCommaInColumnWithTableConstraint   = "NO COMMA COLUMN TABCONSTRAINT";
	static final String SelectIntoVariantNoTopNoIdentityOrderBy = "NO TOP NO IDENTITY ORDER BY";
	static final String SelectIntoVariantIdentityNotLast     = "IDENTITY NOT LAST COLUMN";

	// misc strings
	static final String TrigMultiDMLAttr      = "TRIGGER_MULTI_DML";
	static final String SelectIntoVariantFmtNoTopNoIdentityOrderBy = "SELECT..INTO with ORDER BY";
	static final String SelectIntoVariantFmtIdentityNotLast = "SELECT..INTO with IDENTITY()";
	static final String globalAtAtVarDeclared  = ", declaring predefined @@variable name";
	static final String globalAtAtVarNamedArg  = ", using predefined @@variable name as named argument";
	static final String globalAtAtVarReference = ", reference"; 

	// use when there's no name
	static final String Undefined = "-undefined-";

	// use when there's no name
	static final String noName = "-unnamed-";

	// display string, if we don't want to use the section names in the .cfg file
	static Map<String, String> displayString = new HashMap<>();

	// variables/parameters only for current object
	static Map<String, String> localVars = new HashMap<>();
	static Map<String, String> localAtAtErrorVars = new HashMap<>();

	// sqlcmd variables only for current file
	static List<String> sqlcmdVars = new ArrayList<>();

	// DATExxx BIFs
	static final List<String> dateBIFs = Arrays.asList("DATENAME", "DATEPART", "DATEDIFF", "DATEADD");

	// result types of BIFs. This list is 99% complete
	static final List<String> stringBIFs   = Arrays.asList("DATENAME", "SUBSTRING", "STR", "REPLICATE", "SPACE", "CHAR", "NCHAR", "APP_NAME", "COL_NAME", "CONCAT", "CONCAT_WS", "CURRENT_TIMEZONE", "CURRENT_USER", "USER", "SYSTEM_USER", "SESSION_USER", "FORMAT", "FORMATMESSAGE", "LEFT", "RIGHT", "LOWER", "UPPER", "LTRIM", "RTRIM", "TRIM", "INDEX_COL", "OBJECT_NAME", "OBJECT_SCHEMA_NAME", "ORIGINAL_DB_NAME", "QUOTENAME", "REPLACE", "REVERSE", "TRANSLATE", "DB_NAME", "ERROR_MESSAGE", "ERROR_PROCEDURE", "SCHEMA_NAME", "ORIGINAL_LOGIN", "STRING_AGG", "STRING_ESCAPE", "STUFF", "SUSER_NAME", "USER_NAME", "SUSER_SNAME", "TYPE_NAME");
	static final List<String> numericBIFs  = Arrays.asList("DATEPART", "DATEDIFF", "DATEDIFF_BIG", "ASCII", "LEN", "DATALENGTH", "SUM", "AVG", "ABS", "ACOS", "ASIN", "ATAN", "ATN2", "COS", "COT", "DEGREES", "RADIANS", "SIN", "TAN", "EXP", "LOG", "LOG10", "CHECKSUM", "CEILING", "FLOOR", "COL_LENGTH", "CHARINDEX", "PATINDEX", "CURRENT_TRANSACTION_ID", "DAY", "MONTH", "YEAR", "DB_ID", "ERROR_SEVERITY", "ERROR_STATE", "ERROR_LINE", "ERROR_NUMBER", "IDENT_CURRENT", "IDENT_INCR", "IDENT_SEED", "ISNUMERIC", "ISDATE", "OBJECT_ID", "PI", "POWER", "RAND", "ROUND", "ROUND", "ROWCOUNT_BIG", "SCHEMA_ID", "SCOPE_IDENTITY", "SIGN", "SQRT", "SQUARE", "USER_ID", "SUSER_ID", "TRIGGER_NESTLEVEL", "TYPE_ID", "UNICODE");
	static final List<String> datetimeBIFs = Arrays.asList("DATEADD", "GETDATE", "GETUTCDATE", "CURRENT_TIMESTAMP", "DATEFROMPARTS", "DATETIME2FROMPARTS", "DATETIMEFROMPARTS", "DATETIMEOFFSETFROMPARTS", "EOMONTH", "SMALLDATEFROMPARTS", "SMALLDATETIMEFROMPARTS", "STATS_DATE", "SYSDATETIME", "SYSDATETIMEOFFSET", "SYSUTCDATETIME", "TIMEFROMPARTS", "TODATETIMEOFFSET");
	static final List<String> binaryBIFs   = Arrays.asList("HASHBYTES", "BINARY_CHECKSUM", "COLUMNS_UPDATED", "NEWID", "NEWSEQUENTIALID", "SID_BINARY", "SUSER_SID");

	// result types of ODBC calls. NB: IFNULL has varyign datatype, is not handled
	static final List<String> stringODBCs   = Arrays.asList("CHAR", "CONCAT", "DATABASE", "DAYNAME", "INSERT", "LCASE", "LEFT", "LTRIM", "MONTHNAME", "REPEAT", "REPLACE", "RIGHT", "RTRIM", "SOUNDEX", "SPACE", "SUBSTRING", "UCASE", "USER");
	static final List<String> numericODBCs  = Arrays.asList("ABS", "ACOS", "ASCII", "ASIN", "ATAN", "ATAN2", "BIT_LENGTH", "CEILING", "CHARACTER_LENGTH", "CHAR_LENGTH", "COS", "COT", "DAYOFMONTH", "DAYOFWEEK", "DAYOFYEAR", "DEGREES", "DIFFERENCE", "EXP", "EXTRACT", "FLOOR", "HOUR", "LENGTH", "LOCATE", "LOG", "LOG10", "MINUTE", "MOD", "MONTH", "OCTET_LENGTH", "PI", "POSITION", "POWER", "QUARTER", "RADIANS", "RAND", "ROUND", "SECOND", "SIGN", "SIN", "SQRT", "TAN", "TIMESTAMPDIFF", "TRUNCATE", "WEEK", "YEAR");
	static final List<String> datetimeODBCs = Arrays.asList("CURDATE", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURTIME", "NOW", "TIMESTAMPADD");
	static final List<String> binaryODBCs   = Arrays.asList("");

	// datatypes
	static final List<String> baseNumericTypes    = Arrays.asList("INT", "INTEGER", "TINYINT", "SMALLINT", "BIGINT", "NUMERIC", "DECIMAL", "DEC", "FLOAT", "REAL", "DOUBLE", "DOUBLE PRECISION", "BIT", "MONEY", "SMALLMONEY");
	static final List<String> baseStringTypes     = Arrays.asList("CHAR", "CHARACTER", "VARCHAR", "NCHAR", "NCHARACTER",  "NATIONAL CHARACTER", "NVARCHAR", "CHARACTER VARYING", "NCHARACTER VARYING", "NATIONAL CHARACTER VARYING", "TEXT", "NTEXT", "SYSNAME");  // todo: handle VARYING
	static final List<String> baseDateTimeTypes   = Arrays.asList("DATE", "TIME", "DATETIME", "SMALLDATETIME", "DATETIME2", "DATETIMEOFFSET");
	static final List<String> baseBinaryTypes     = Arrays.asList("UNIQUEIDENTIFIER", "BINARY", "VARBINARY", "IMAGE", "TIMESTAMP", "ROWVERSION");
	static final List<String> baseGeospatialTypes = Arrays.asList("GEOMETRY", "GEOGRAPHY");
	static final List<String> baseMiscTypes       = Arrays.asList("SQL_VARIANT", "HIERARCHYID", "CURSOR", "XML");

	// predefined server-level roles
	static final List<String> srvRoles  = Arrays.asList("SYSADMIN", "SERVERADMIN", "SECURITYADMIN", "PROCESSADMIN", "SETUPADMIN", "BULKADMIN", "DISKADMIN", "DBCREATOR");

	// encoded BIF arg values for lookup
	static final String BIFArgStar = CompassUtilities.BBFMark + "STAR";
	static final String BIFNoArg = "NO ARGUMENT";
	static final String BIFSingleArg = "SINGLE ARGUMENT";
	static final String BIFMultipleArg = "MULTIPLE ARGUMENTS";
	static final String withoutArgumentValidateStr = " without arguments"; // must match section name in .cfg file
	static final String withNArgumentValidateStr = " with N arguments"; // must match section name in .cfg file
	static final String withNArgumentValidateStrRegex = " with \\d arguments"; // for popups

	// expressions for rewriting
	static String rewriteTagCustom = "~~custom~~";
	static String rewriteTag1 = "~~1~~";
	static String rewriteTag2 = "~~2~~";
	static String rewriteWaitforDelay = "CASE WHEN "+rewriteTag1+" NOT LIKE '%[^0-9:.]%' THEN CASE LEN("+rewriteTag1+") - LEN(REPLACE("+rewriteTag1+",':','')) WHEN 1 THEN (SUBSTRING("+rewriteTag1+",1,CHARINDEX(':',"+rewriteTag1+")-1)*60) + SUBSTRING("+rewriteTag1+",CHARINDEX(':',"+rewriteTag1+")+1,2) WHEN 2 THEN (SUBSTRING("+rewriteTag1+",1,CHARINDEX(':',"+rewriteTag1+")-1)*3600.0) + (SUBSTRING("+rewriteTag1+",CHARINDEX(':',"+rewriteTag1+")+1,CHARINDEX(':',"+rewriteTag1+",CHARINDEX(':',"+rewriteTag1+"))-1)*60.0)+ CAST(SUBSTRING("+rewriteTag1+",CHARINDEX(':',"+rewriteTag1+",CHARINDEX(':',"+rewriteTag1+")+1)+1,9) AS NUMERIC(5,3)) ELSE 0  END ELSE 0 END";
	static Integer nrWaitForDelayRewrites = 0;

	static String rewriteEOMonth = "DATEADD(dd,-1,DATEADD(mm,1,DATEFROMPARTS(DATEPART(yy, CAST("+rewriteTag1+" AS DATE)),DATEPART(MM,CAST("+rewriteTag1+" AS DATE)),1)))";
	static String rewriteDbPrincipalID = "USER_ID("+rewriteTag1+")";
	static String rewriteNumericAsDate = "DATEADD(minute,("+rewriteTag1+")*1440,DATETIMEFROMPARTS(1900,1,1,0,0,0,0))";
	static String rewriteNumericAsDateZero = "DATETIMEFROMPARTS(1900,1,1,0,0,0,0)";

	// some 1:1 string rewrites
	static List<String> rewriteDirectOrig    = Arrays.asList( "SYSTEM_USER", "{ FN CURRENT_DATE }", "{ FN CURDATE }", "{ FN CURRENT_TIME }", "{ FN CURTIME }", "NONCLUSTERED HASH",
	                                                          "DATENAME(MI)", "DATEPART(MI)", "DATEDIFF(MI)", "DATEADD(MI)", "DATEPART(Y)", "DATENAME(Y)", "DATEPART(W)",
	                                                          "DATENAME(W)", "DATEDIFF(W)", "DATEADD(W)", "DATEDIFF(WEEKDAY)", "DATEDIFF(DW)"
	                                                        );
	static List<String> rewriteDirectReplace = Arrays.asList( "SUSER_NAME()",  "CONVERT(VARCHAR(10),CONVERT(DATE,GETDATE()))", "CONVERT(VARCHAR(10),CONVERT(DATE,GETDATE()))", "CONVERT(VARCHAR(30),CONVERT(TIME,GETDATE()))", "CONVERT(VARCHAR(30),CONVERT(TIME,GETDATE()))", "NONCLUSTERED",
	                                                          "minute", "minute", "minute", "minute", "dy", "dy", "dw", "dw", "dd", "dd", "day", "dd"
													        );
	static List<String> rewriteDirectODBCfuncOrig    = Arrays.asList("REPEAT", "UCASE", "LCASE", "SPACE", "LTRIM", "RTRIM", "LEFT", "RIGHT", "REPLACE", "CONCAT", "ASCII", "LENGTH", "CHARACTER_LENGTH", "CHAR_LENGTH",
	                                                                 "NOW", "HOUR", "MINUTE", "SECOND", "WEEK", "MONTH", "QUARTER", "YEAR", "MOD",
	                                                                 "D", "T", "TS", "GUID"
													 );
	static List<String> rewriteDirectODBCfuncReplace = Arrays.asList("REPLICATE", "UPPER", "LOWER", "SPACE", "LTRIM", "RTRIM", "LEFT", "RIGHT", "REPLACE", "CONCAT", "ASCII", "LEN", "LEN", "LEN",
	                                                                 "GETDATE", "DATEPART(hour,", "DATEPART(minute,",  "DATEPART(second,", "DATEPART(week,", "DATEPART(month,", "DATEPART(quarter,",  "DATEPART(year,",	rewriteTagCustom,
	                                                                 "CONVERT(DATE,", "CONVERT(TIME,", "CONVERT(DATETIME,", "CONVERT(UNIQUEIDENTIFIER,"
													 );

	// flags
	static boolean inCompCol = false;
	static String  inCompColType = "";

	// see mostRecentDatatype();
	static String mostRecentDatatypeSpatialOrHierarchy = "";

	//--- init -----------------------------------------------------------------
	private static final CompassAnalyze instance = new CompassAnalyze();

	private CompassAnalyze() {}

	public static CompassAnalyze getInstance() {
		initVarious();
		return instance;
	}

	private static void initVarious() {
		displayString.put(TransitionTableMultiDMLTrig, TransitionTableMultiDMLTrigFmt);
	}

	//--- wrappers ------------------------------------------------------------
	private static boolean featureExists(String section) {
		return cfg.featureExists(section);
	}
	private static boolean featureExists(String section, String name) {
		return cfg.featureExists(section, name);
	}
	private static String featureArgSupportedInVersion(String section, String arg, String argValue) {
		return cfg.featureArgSupportedInVersion(u.targetBabelfishVersion, section, arg, argValue);
	}
	public static String featureSupportedInVersion(String section) {
		return cfg.featureSupportedInVersion(u.targetBabelfishVersion, section);
	}
	public static String featureSupportedInVersion(String section, String name) {
		return cfg.featureSupportedInVersion(u.targetBabelfishVersion, section, name);
	}
	private static String featureSupportedInVersion(String section, String name, String optionValue) {
		return cfg.featureSupportedInVersion(u.targetBabelfishVersion, section, name, optionValue);
	}
	private static int featureIntValueSupportedInVersion(String section) {
		return cfg.featureIntValueSupportedInVersion(u.targetBabelfishVersion, section);
	}
	private static String featureValueSupportedInVersion(String section) {
		return cfg.featureValueSupportedInVersion(u.targetBabelfishVersion, section);
	}
	private static List<String> featureValueList(String section) {
		return cfg.featureValueList(section);
	}
	public static String featureGroup(String section) {
		return cfg.featureGroup(section);
	}
	private static String featureGroup(String section, String name) {
		return cfg.featureGroup(section, name);
	}
	private static String featureDefaultStatus(String section) {
		return cfg.featureDefaultStatus(section);
	}
	private static String featureDefaultStatus(String section, String name) {
		return cfg.featureDefaultStatus(section, name);
	}

	//--- debugging -----------------------------------------------------------
	String dbgTraceBasicIndent = "    ";
	StringBuilder dbgTraceIndent = new StringBuilder();
	int nestingLevel = 0, dbgTraceBasicIndentLength = dbgTraceBasicIndent.length();

	void dbgTraceVisitEntry(String s) {
		nestingLevel++;
		dbgVisitOutput(s+ " entry ");
		dbgTraceIndent.append(dbgTraceBasicIndent);
	}
	void dbgTraceVisitExit(String s) {
		dbgTraceIndent.delete(dbgTraceIndent.length() - dbgTraceBasicIndentLength, dbgTraceIndent.length());
		dbgVisitOutput(s + " exit ");
		nestingLevel--;
	}
	void dbgVisitOutput(String s) {
		if (u.debugging) u.dbgOutput(dbgTraceIndent + "(" + nestingLevel + ") " + s, u.debugPtree);
	}

	//--- rule names ------------------------------------------------------------
	//	currentRuleName(ctx.getRuleIndex())	: [expression]						
	//	ctx.getClass().getName()            : [parser.TSQLParser$Constant_exprContext]						
	//	ctx.getClass().getSimpleName()      : [Constant_exprContext]						
	
	private String currentRuleName(int ruleIndex) {
		return  CompassUtilities.grammarRuleNames[ruleIndex];
	}

	private String parentRuleName(RuleContext parent) {
		return parentRuleName(parent, 1);
	}

	private String parentRuleName(RuleContext parent, int level) {
		if (level <= 1) {  // should never be < 1 but play it safe
			return CompassUtilities.grammarRuleNames[parent.getRuleIndex()];
		}
		else {
			return parentRuleName(parent.parent, level-1);
		}
	}

	private boolean hasParent(RuleContext parent, String parentRuleName) {
		int ruleIxParent = parent.getRuleIndex();
		if (CompassUtilities.grammarRuleNames[ruleIxParent].equals(CompassUtilities.startRuleName)) {
			// top level reached
			return false;
		}
		else if (CompassUtilities.grammarRuleNames[ruleIxParent].equals(parentRuleName)) {
			// found it
			return true;
		}
		return hasParent(parent.parent, parentRuleName);
	}

	private RuleContext findParent(RuleContext parent, String parentRuleName) {
		int ruleIxParent = parent.getRuleIndex();
		if (CompassUtilities.grammarRuleNames[ruleIxParent].equals(CompassUtilities.startRuleName)) {
			// top level reached
			return null;
		}
		else if (CompassUtilities.grammarRuleNames[ruleIxParent].equals(parentRuleName)) {
			// found it
			return parent;
		}
		return findParent(parent.parent, parentRuleName);
	}

	// covnert a section of a parse tree to text
	public String parseTreeToString(RuleContext ctx) {
		String treeString = "";
		TSQLParser parser = new TSQLParser(null);
		treeString = ctx.toStringTree(parser);
		//u.appOutput(u.thisProc()+"treeString=["+treeString+"] ");
		return treeString;
	}

	//--- set QUOTED_IDENTIFIER -------------------------------------------------
	public void setQuotedIdentifier (String on_off) {
		assert CompassUtilities.OnOffOption.contains(on_off) : CompassUtilities.thisProc()+"parameter must be ON or OFF";
		if (on_off.equalsIgnoreCase("ON")) {
			TSQLLexer.QUOTED_IDENTIFIER_FLAG = true;
			u.QuotedIdentifierFlag = true;
		}
		else {
			TSQLLexer.QUOTED_IDENTIFIER_FLAG = false;
			u.QuotedIdentifierFlag = false;
		}
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"initializing QUOTED_IDENTIFIER="+on_off, u.debugPtree);
	}

	//--- datatype evaluation -------------------------------------------------
	// get the datatype of a variable or parameter
	private String varDataType(String v) {
		// look up datatype of this variable or parameter
		// ToDo: can also be a system-defined @@variable
		if (localVars.containsKey(v.toUpperCase())) {
			String varType = localVars.get(v.toUpperCase());
			String UDDtype = lookupUDD(varType);
			if (!UDDtype.isEmpty()) {
				if (!UDDtype.equals(Undefined)) varType = UDDtype;
			}
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"v=["+v+"]  varType=["+varType+"]  UDDtype=["+UDDtype+"] ", u.debugPtree);
			return varType.toUpperCase();
		}
		else {
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"v=["+v+"]  var not found  ", u.debugPtree);
		}
		// unknown as catchall
		return CompassUtilities.BBFUnknownType;
	}

	private Integer getIntegerConstant(String expr) {
		return getIntegerConstant(expr, false);
	}
	private Integer getIntegerConstant(String expr, boolean stripQuotes) {
		if (expr.isEmpty()) return null;
		if (stripQuotes) {
			expr = u.stripStringQuotes(expr);
		}
		try {
			int i = Integer.parseInt(expr);
			return i;
		}
		catch (Exception e) {
			return null;
		}
	}

	private Float getNumericConstant(String expr) {
		return getNumericConstant(expr, false);
	}
	private Float getNumericConstant(String expr, boolean stripQuotes) {
		if (expr.isEmpty()) return null;
		if (stripQuotes) {
			expr = u.stripStringQuotes(expr);
		}
		try {
			Float f = Float.parseFloat(expr);
			return f;
		}
		catch (Exception e) {
			return null;
		}
	}

	private String cleanupTermErrorCode(String s) {
		s = s.trim();
		while (s.startsWith("(")) s = s.substring(1).trim();
		while (s.endsWith(")"))   s = CompassUtilities.removeLastChar(s).trim();
		if (s.endsWith("(")) s += ")";
		return s;
	}

	// get base scalar datatype for checking against .cfg file
	private String getBaseDataType(String s) {
		// ToDo: find base type for UDD
		if (s.contains("CHAR VARYING")) s = u.applyPatternFirst(s, "CHAR VARYING", "CHARACTER VARYING");
		if (s.contains("NATIONAL CHAR")) s = u.applyPatternFirst(s, "NATIONAL CHAR\\b", "NATIONAL CHARACTER");
		if (s.contains("(")) {  // remove length
			if (s.startsWith("XML(")) {
				// XML(xmlschema) : do not remove brackets; it's in the .cfg file this way
			}
			else {
				return s.substring(0,s.indexOf('('));
			}
		}
		return s;
	}

	private boolean isNumeric(String s) {
		if (s.equals(CompassUtilities.BBFNumericType)) {
			return true;
		}
		if (baseNumericTypes.contains(getBaseDataType(s))) {
			return true;
		}
		if (getIntegerConstant(s) != null) {
			return true;
		}
		if (getNumericConstant(s) != null) {
			return true;
		}
		return false;
	}

	private boolean isString(String s) {
		if (s.equals(CompassUtilities.BBFStringType)) {
			return true;
		}
		if (baseStringTypes.contains(getBaseDataType(s))) {
			return true;
		}
		return false;
	}
	private boolean isStringConstant(String s) {
		if (!u.stripStringQuotes(s).equals(s)) return true;
		return false;
	}
	private boolean isDateTime(String s) {
		if (s.equals(CompassUtilities.BBFDateTimeType)) {
			return true;
		}
		if (baseDateTimeTypes.contains(getBaseDataType(s))) {
			return true;
		}
		return false;
	}
	private boolean isBinary(String s) {
		if (s.equals(CompassUtilities.BBFBinaryType)) {
			return true;
		}
		if (baseBinaryTypes.contains(getBaseDataType(s))) {
			return true;
		}
		return false;
	}
	private boolean isUnknown(String s) {
		if (s.equals(CompassUtilities.BBFUnknownType)) {
			return true;
		}
		return false;
	}

	// lookup an object's type (only table or view)
	private String lookupTableView(String name) {
		String objType = "";
		String resolvedName = u.decodeIdentifier(u.resolveName(name.toUpperCase()));
		if (CompassUtilities.tableViewSymTab.containsKey(resolvedName)) {
			objType = CompassUtilities.tableViewSymTab.get(resolvedName);
		}
		else {
			String resolvedName2 = u.resolveName(resolvedName, "dbo");
			if (CompassUtilities.tableViewSymTab.containsKey(resolvedName2)) {
				objType = CompassUtilities.tableViewSymTab.get(resolvedName2);
			}
		}
		return objType;
	}

	// lookup an SUDF
	private String lookupSUDF(String name) {
		String resultType = "";
		String resolvedName = u.decodeIdentifier(u.resolveName(name.toUpperCase()));
		if (CompassUtilities.SUDFSymTab.containsKey(resolvedName)) {
			resultType = CompassUtilities.SUDFSymTab.get(resolvedName);
		}
		else {
			String resolvedName2 = u.resolveName(resolvedName, "dbo");
			if (CompassUtilities.SUDFSymTab.containsKey(resolvedName2)) {
				resultType = CompassUtilities.SUDFSymTab.get(resolvedName2);
			}
		}
		return resultType;
	}

	// lookup a TUDF
	private String lookupTUDF(String name) {
		String resultType = "";
		String resolvedName = u.decodeIdentifier(u.resolveName(name.toUpperCase()));
		if (CompassUtilities.TUDFSymTab.containsKey(resolvedName)) {
			resultType = CompassUtilities.TUDFSymTab.get(resolvedName);
		}
		else {
			String resolvedName2 = u.resolveName(resolvedName, "dbo");
			if (CompassUtilities.TUDFSymTab.containsKey(resolvedName2)) {
				resultType = CompassUtilities.TUDFSymTab.get(resolvedName2);
			}
		}
		return resultType;
	}

	// lookup a name that could be a UDD
	// return blank if it is not a UDD but a system datatype
	// return undefined if it is not a UDD and not a system datatype
	private String lookupUDD(String name) {
		String resultType = "";
		name = name.toUpperCase();
		if (name.startsWith("SYS.")) {
			name = name.substring(4);
		}
		if (name.endsWith(" IDENTITY")) {
			name = name.substring(0,name.indexOf(" IDENTITY"));
		}
		else if (name.contains(" IDENTITY(")) {
			name = name.substring(0,name.indexOf(" IDENTITY"));
		}
		String resolvedName = u.decodeIdentifier(u.resolveName(name));
		if (CompassUtilities.UDDSymTab.containsKey(resolvedName)) {
			resultType = CompassUtilities.UDDSymTab.get(resolvedName);
		}
		else {
			String resolvedName2 = u.resolveName(resolvedName, "dbo");
			if (CompassUtilities.UDDSymTab.containsKey(resolvedName2)) {
				resultType = CompassUtilities.UDDSymTab.get(resolvedName2);
			}
		}
		if (resultType.isEmpty()) {
			// is this a system datatype?
			resolvedName = u.getObjectNameFromID(resolvedName);

			if (baseNumericTypes.contains(getBaseDataType(resolvedName))) return "";
			if (baseStringTypes.contains(getBaseDataType(resolvedName))) return "";
			if (baseDateTimeTypes.contains(getBaseDataType(resolvedName))) return "";
			if (baseBinaryTypes.contains(getBaseDataType(resolvedName))) return "";
			if (baseGeospatialTypes.contains(getBaseDataType(resolvedName))) return "";
			if (baseMiscTypes.contains(getBaseDataType(resolvedName))) return "";
			if (resolvedName.equalsIgnoreCase("XML(xmlschema)")) return "";

			// not a system datatype, so assuming it is a UDD for which we don't have the DDL
			return Undefined;
		}

		// it's a UDD:
		return resultType;
	}

	// lookup an procedure
	private String lookupProc(String name) {
		String resultType = "";
		String resolvedName = u.decodeIdentifier(u.resolveName(name.toUpperCase()));
		if (CompassUtilities.procSymTab.containsKey(resolvedName)) {
			resultType = CompassUtilities.procSymTab.get(resolvedName);
		}
		else {
			String resolvedName2 = u.resolveName(resolvedName, "dbo");
			if (CompassUtilities.procSymTab.containsKey(resolvedName2)) {
				resultType = CompassUtilities.procSymTab.get(resolvedName2);
			}
		}
		return resultType;
	}

	// lookup a column; must be called with resolved object name
	private String lookupCol(String objName, String colName) {
		String resultType = "";
		String resolvedObjName = u.resolveName(objName.toUpperCase());
		String colKey = u.makeColSymTabKey(resolvedObjName, colName);
		//u.appOutput(u.thisProc()+"objName=["+objName+"] colName=["+colName+"]  resolvedName=["+resolvedObjName+"] colKey=["+colKey+"] ");
		//u.dumpSymTab("lookupCol objName=["+objName+"] colName=["+colName+"]  resolvedName=["+resolvedObjName+"] colKey=["+colKey+"]");
		if (CompassUtilities.colSymTab.containsKey(colKey)) {
			resultType = CompassUtilities.colSymTab.get(colKey);
		}
		//u.appOutput(u.thisProc()+"resultType=["+resultType+"] ");
		return resultType;
	}

	// lookup a parameter by position(only used for parameters that have a default)
	private String lookupParDft(String objName, int parNo) {
		String parDft = "";
		String resolvedName = u.decodeIdentifier(u.resolveName(objName.toUpperCase()));
		String parNoKey = u.makeParSymTabKey(resolvedName, parNo);
		if (CompassUtilities.parSymTab.containsKey(parNoKey)) {
			parDft = CompassUtilities.parSymTab.get(parNoKey);
		}
		return parDft;
	}

	private String lookupParDft(String objName, String parName) {
		String parDft = "";
		String resolvedName = u.decodeIdentifier(u.resolveName(objName.toUpperCase()));
		String parNameKey = u.makeParSymTabKey(resolvedName, parName);
		if (CompassUtilities.parSymTab.containsKey(parNameKey)) {
			parDft = CompassUtilities.parSymTab.get(parNameKey);
		}
		return parDft;
	}

	// Given a name, try to find the object type. if not found, it's probably a procedure since we don't keep those in the symtab
	// This is used to report the object type for synonyms
	private String findObjectType(String objName) {
		if (u.debugging) u.dbgOutput(u.thisProc()+"entry: objName=["+objName+"] ", u.debugSymtab);
		objName = objName.toUpperCase();
		String objType = "";
		objType = lookupTableView(objName);
		if (u.debugging) u.dbgOutput(u.thisProc()+"lookupTableView: objType=["+objType+"] ", u.debugSymtab);
		if (objType.isEmpty()) {
			objType = lookupSUDF(objName);
			if (u.debugging) u.dbgOutput(u.thisProc()+"lookupSUDF: objType=["+objType+"] ", u.debugSymtab);
			if (!objType.isEmpty()) objType = "FUNCTION";
		}
		if (objType.isEmpty()) {
			objType = lookupTUDF(objName);
			if (u.debugging) u.dbgOutput(u.thisProc()+"lookupTUDF: objType=["+objType+"] ", u.debugSymtab);
			if (!objType.isEmpty()) objType = "FUNCTION";
		}
		if (objType.isEmpty()) {
			objType = lookupProc(objName);
			if (u.debugging) u.dbgOutput(u.thisProc()+"lookupProc: objType=["+objType+"] ", u.debugSymtab);
		}
		if (objType.isEmpty()) {
			// leave blank - cannot find the name
		}
		if (u.debugging) u.dbgOutput(u.thisProc()+"exit: objName=["+objName+"]  objType=["+objType+"] ", u.debugSymtab);
		return objType;
	}

	// contains variables and parameters applicable to the current batch/block
	public void addLocalVar(String varName, String dataType) {
		localVars.put(varName.toUpperCase(), getBaseDataType(dataType).toUpperCase());
		//debug
//		int j=0;
//		for (String v: localVars.keySet()) {
//			j++;
//			u.appOutput("localVar "+j+":"+v+"=["+localVars.get(v)+"]");
//		}
	}

	// contains sqlcmd variables applicable to the current input file (only used in pass 2, assuming setvar comes before references)
	public void addSqlcmdVar(String varName) {
		sqlcmdVars.add(varName.toUpperCase());
		//u.appOutput(u.thisProc()+"adding varName=["+varName+"] sqlcmdVars.size()=["+sqlcmdVars.size()+"] ");
		//debug
//		for (String v: sqlcmdVars) {
//			u.appOutput("sqlcmdVar=["+v+"]");
//		}
	}

	// contains variables containing an @@ERROR value context
	public void addAtAtErrorVars(String varName) {
		localAtAtErrorVars.put(varName.toUpperCase(), "");
	}

	// determine kind of temp table
	public static String getTmpTableType(String tableName) {
		return getTmpTableType(tableName, false);
	}
	public static String getTmpTableType(String tableName, boolean defaultTable) {
		if (tableName.charAt(0) == '#') {
			if (tableName.charAt(1) == '#') {
				return GlobalTmpTableFmt;
			}
			else {
				return "#tmptable";
			}
		}
		else {
			if (tableName.length()> 8) {
				if (tableName.substring(0,7).equalsIgnoreCase("tempdb.")) {
					return "tempdb..table";
				}
			}
		}
		// not a temp table
		if (defaultTable) return "table";
		else return "";
	}

	public void mostRecentDatatype(String dataType) {
		// keeping track of the most recent datatype encountered.
		// This is very fuzzy and not very reliable, but this is a quick hack to try and distinguish between spatial and HIERARCHYID types when encountering the .ToString() method
		// we should not clear this flag between batches; ideally we should clear it when starting pass 2, but since we're already taking big gambles here, who cares...
		if (dataType.equals("GEOGRAPHY") || dataType.equals("GEOMETRY") || dataType.equals("HIERARCHYID")) {
			mostRecentDatatypeSpatialOrHierarchy = dataType;
		}
	}

	// get # elements in a list
	int argListCount(ParserRuleContext argList) {
		if (argList ==  null) return 0;
		int n = argList.getChildCount();
		return (n+1)/2; // account for commas in the arglist
	}

	public int nrColumn_name_list_with_order(TSQLParser.Column_name_list_with_orderContext cList) {
		int cnt = 0;
		for (int i = 0; i <cList.getChildCount(); i++) {
			String t = cList.getChild(i).getText().toUpperCase();
			if (t.equals(",")) continue;
			if (t.equals("ASC")) continue;
			if (t.equals("DESC")) continue;
			cnt++;
		}
		return cnt;
	}

	public int nrColumn_name_list(TSQLParser.Column_name_listContext cList) {
		int cnt = 0;
		cnt = (cList.getChildCount()+1)/2;
		return cnt;
	}

	//--- formatting -----------------------------------------------

	private String formatItemDisplay(String s) {
		// some standard formatting for display
		if (s.contains("@")) {
			s = u.applyPatternAll(s, u.varPattern, "@v");
		}
		else if (s.toUpperCase().contains("0X")) {
			s = u.applyPatternAll(s, "\\b"+u.hexPattern+"\\b", u.escapeHTMLChars("0x<hex>"));
		}
		return s;
	}

	private String formatOptionDisplay(String option, String optionValue) {
		if (optionValue.isEmpty()) return option;
		optionValue = u.applyPatternFirst(optionValue, "^(\\d+)(SECONDS)", "$1 $2");
		return option+"="+optionValue.trim();
	}

	public static String getOptionValue(String option) {
		return getOptionValue(option, "=");
	}
	public static String getOptionValue(String option, String delimiter) {
		assert !delimiter.isEmpty() : "delimiter cannot be empty";
		if (option.startsWith("QUERY_STORE(")) {  //special case
			return "(<options>)";
		}
		if (option.contains(delimiter)) {
			return option.substring(option.indexOf(delimiter)+1);
		}
		return "";
	}

	public static String getOptionName(String option) {
		return getOptionName(option, "=");
	}
	public static String getOptionName(String option, String delimiter) {
		assert !delimiter.isEmpty() : "delimiter cannot be empty";
		if (option.startsWith("QUERY_STORE(")) { //special case
			return "QUERY_STORE";
		}
		if (option.contains(delimiter)) {
			return option.substring(0,option.indexOf(delimiter));
		}
		else if (option.startsWith("EXECUTEAS") || option.startsWith("EXECAS")) {
			String execAs = option.substring(option.indexOf("AS")+2);
			if (u.isQuotedString(execAs)) execAs = "USER";
			option = "EXECUTE AS " + execAs;
		}
		return option;
	}

	private String formatRemoveStringQuotes(String s) {
		// some standard formatting for display
		return u.stripStringQuotes(s);
	}

	private String formatUDDMsg(String dataType, String UDD) {
		String dataTypeFmt = "";
		if (UDD.equals(Undefined)) {
			dataTypeFmt = " (UDD, unknown base type)";
		}
		else {
			dataTypeFmt = " (UDD "+dataType+")";
		}
		return dataTypeFmt;
	}

	public boolean addRewrite (String report, String origText, String rewriteType, String rewriteText, int startLine, int startCol, int endLine, int endCol, int startPos, int endPos) {
		return addRewrite(report, origText, rewriteType, rewriteText, startLine, startCol, endLine, endCol, startPos, endPos, null);
	}
	public boolean addRewrite (String report, String origText, String rewriteType, String rewriteText, int startLine, int startCol, int endLine, int endCol, int startPos, int endPos, Integer rewriteID) {
		if (u.dynamicSQLFlag) {
			// don't attempt to rewrite dynamic SQL; it's too complex to get right and it does not happen often enough to make the effort worthwhile
			return false;
		}

		if (u.debugging) u.dbgOutput(u.thisProc()+"rewriteID=["+rewriteID+"] rewriteType=["+rewriteType+"] origText=["+origText+"] => ["+rewriteText+"] startLine=["+startLine+"]  u.lineNrInFile=["+u.lineNrInFile+"] startLineinBatch=["+startLine+"] startLine=["+startLine+"] startCol=["+startCol+"] endLine=["+endLine+"] endCol=["+endCol+"] startPos=["+startPos+"] endPos=["+endPos+"] ", u.debugRewrite);

		String separator = CompassUtilities.captureFileSeparator;
		int startLineInFile = u.lineNrInFile + startLine - 1;
		int endLineInFile   = u.lineNrInFile + endLine - 1;

		// if it's a single token only, determine last column
		if ((startLine == endLine) && (startCol == endCol) && (origText.length()>0)) endCol = startCol + origText.length() -1;

		if (u.debugging) u.dbgOutput(u.thisProc()+"rewriteID=["+rewriteID+"] rewriteType=["+rewriteType+"] origText=["+origText+"] => ["+rewriteText+"] startLineInFile=["+startLineInFile+"]  u.lineNrInFile=["+u.lineNrInFile+"] startLineinBatch=["+startLine+"] startLine=["+startLine+"] startCol=["+startCol+"] endLine=["+endLine+"] endCol=["+endCol+"] startPos=["+startPos+"] endPos=["+endPos+"] ", u.debugRewrite);

		if ((startCol < 0) || (endCol < 0) || (startPos < 0) || (endPos < 0)) {
			if (startCol < 0) u.appOutput(u.thisProc()+"Internal error: startCol=["+startCol+"] Skipping rewrite.");
			if (endCol < 0)   u.appOutput(u.thisProc()+"Internal error: endCol=  ["+endCol+"] Skipping rewrite.");
			if (startPos < 0) u.appOutput(u.thisProc()+"Internal error: startPos=["+startPos+"] Skipping rewrite.");
			if (endPos < 0)   u.appOutput(u.thisProc()+"Internal error: endPos=  ["+endPos+"] Skipping rewrite.");

			if (CompassUtilities.devOptions) {
				u.dbgOutput(u.thisProc()+"rewriteID=["+rewriteID+"] rewriteType=["+rewriteType+"] origText=["+origText+"] => ["+rewriteText+"] startLineInFile=["+startLineInFile+"]  u.lineNrInFile=["+u.lineNrInFile+"] startLineinBatch=["+startLine+"] startLine=["+startLine+"] startCol=["+startCol+"] endLine=["+endLine+"] endCol=["+endCol+"] startPos=["+startPos+"] endPos=["+endPos+"] ", true);
				u.errorExitStackTrace();
				// we'll never get here
			}

			// don't add this rewrite
			return true;
		}

		if (rewriteID != null) {
			u.rewriteTextListOrigText.put(rewriteID.toString(), rewriteText);
			rewriteText = rewriteID.toString();
		}

		String sortKey = String.format("%08d", u.batchNrInFile) +separator+ String.format("%08d", startPos) + separator + String.format("%08d", endPos) + separator + String.format("%08d", startLineInFile) + separator + String.format("%08d", startCol);
		String key = sortKey + separator+ u.lineNrInFile +separator+ endLineInFile + separator + endCol + separator + rewriteType + separator + report;
		if (u.debugging) u.dbgOutput(u.thisProc()+"adding key(batch;startPos;endPos;startLineInFile;startCol;lineNrInFile;endLineInFile)=["+key+"] ", u.debugRewrite);
		u.rewriteTextListKeys.add(key);
		u.rewriteTextList.put(key, rewriteText);
		return true;
	}

	// for reporting rewrite oppties
	public boolean addRewrite (String report) {
		captureItem(report, "", "", "", u.RewriteOppty, 0);
		return true;
	}

	// get text representation of subtree, with spaces between tokens and string constants removed
	private String getTextSpaced(ParseTree ctx) {
		String t = ctx.getText();
		if (ctx.getChildCount() == 0) {
			if (u.stripStringQuotes(t).length() != t.length()) {
				return "''";
			}
			return t;
		}

		String s = "";
		for (int i = 0; i <ctx.getChildCount(); i++) {
		    s += getTextSpaced(ctx.getChild(i)) + " ";
		}
		s = s.replaceAll(" \\. ", ".");
		s = s.replaceAll("\\(", "( ");
		s = s.replaceAll("\\) ", " )");
		while (s.contains("  ")) {
			s = s.replaceAll("  ", " ");
		}
		return " " + s.trim() + " ";
	}

	//--- item capture entry point --------------------------------------------
	protected void captureItem(String item, String itemDetail, String section, String sectionItem, String status, Integer lineNr) {
		captureItem(item, itemDetail, section, sectionItem, status, lineNr, "");
	}
	protected void captureItem(String item, String itemDetail, String section,  String sectionItem, String status, Integer lineNr, Integer misc) {
		captureItem(item, itemDetail, section, sectionItem, status, lineNr, misc.toString());
	}
	protected void captureItem(String item, String itemDetail, String section,  String sectionItem, String status, Integer lineNr, String misc) {
		assert u.supportOptions.contains(status): CompassUtilities.thisProc()+"invalid status value: ["+status+"] ";
		if (!status.equals(u.ObjCountOnly) && !status.equals(u.ObjectReference)) u.constructsFound++;

		String separator = CompassUtilities.captureFileSeparator;

		String currentContext = u.currentObjectType + " " + u.currentObjectName;
		String subContext = u.currentObjectTypeSub + " " + u.currentObjectNameSub;

		// for CREATE TABLE, if the context is set to the table itself, reset it
		if (item.startsWith("CREATE TABLE") && u.currentObjectType.equals("TABLE")) currentContext = u.BatchContext;

		// check if an item has a modified presentation string
		item = item.trim();
		if (displayString.containsKey(item)) {
			item = displayString.get(item);
		}

		// determine report group for this item
		//   - default = section as specified
		//   - when section+feature found, use report group specified (if any)
		//   - when section found, use report group specified (if any)
		//   - when section not found, but feature found as section, use report group specified (if any)
		//   - if no group found, use Misc SQL features
		String itemGroup = section;
		String reportGroupCfg = "";
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"getting reportGroup: section=["+section+"] sectionItem=["+sectionItem+"]  item=["+item+"] status=["+status+"] ", u.debugCfg);
		if (!status.equals(u.ObjectReference)) {
			if (!sectionItem.isEmpty()) {
				reportGroupCfg = featureGroup(section, sectionItem);
				if (reportGroupCfg.isEmpty()) {
					reportGroupCfg = featureGroup(section);
					if (reportGroupCfg.isEmpty())  {
						reportGroupCfg = featureGroup(sectionItem);
					}
				}
			}
			else {
				reportGroupCfg = featureGroup(section);
			}
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"reportGroupCfg=["+reportGroupCfg+"] ", u.debugCfg);
			if (!reportGroupCfg.isEmpty()) itemGroup = reportGroupCfg;
			if (itemGroup.isEmpty() || itemGroup.equalsIgnoreCase("DEFAULT")) {
				itemGroup = MiscReportGroup;
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"section=["+section+"] sectionItem=["+sectionItem+"], using MiscReportGroup=["+MiscReportGroup+"] ", u.debugCfg);
			}
		}

		// check for separator in text
		if (item.contains(separator)) item = item.replaceAll(separator, CompassUtilities.captureFileSeparatorMarker);
		if (itemDetail.contains(separator)) itemDetail = itemDetail.replaceAll(separator, CompassUtilities.captureFileSeparatorMarker);
		if (itemGroup.contains(separator)) itemGroup = itemGroup.replaceAll(separator, CompassUtilities.captureFileSeparatorMarker);
		if (currentContext.contains(separator)) currentContext = currentContext.replaceAll(separator, CompassUtilities.captureFileSeparatorMarker);
		if (subContext.contains(separator)) subContext = subContext.replaceAll(separator, CompassUtilities.captureFileSeparatorMarker);
		itemGroup = itemGroup.trim();

		// for (optional) effort estimation, try to link the original cfg section/item to what is shown in the report (since the effort estimation csv file is based on the report)
		//u.appOutput(u.thisProc()+"lastCfgCheck=["+CompassConfig.lastCfgCheckSection+"] name=["+CompassConfig.lastCfgCheckName+"] ");
		//u.appOutput(u.thisProc()+"item=["+item+"]  itemGroup=["+itemGroup+"] sectionItem=["+sectionItem+"] status=["+status+"] ");
		String xrefLine = "";
		if (!status.equals(u.Supported) && !status.equals(u.RewriteOppty)) {
			if (!CompassConfig.lastCfgCheckSection.isEmpty()) {
				String xrefLineKey = item +separator+ itemGroup +separator+ CompassConfig.lastCfgCheckSection +separator+ CompassConfig.lastCfgCheckName+separator;
				xrefLineKey = xrefLineKey.toUpperCase();
				if (!u.xrefLineFilter.containsKey(xrefLineKey)) {
					u.xrefLineFilter.put(xrefLineKey, 1);
					//u.appOutput(u.thisProc()+"keep: ["+CompassConfig.lastCfgCheckSection+"], ["+CompassConfig.lastCfgCheckName+"]  ==>  ["+itemGroup+"], ["+item+"], ["+sectionItem+"]");
					xrefLine = item +separator+ "" +separator+ itemGroup +separator+ u.XRefOnly +separator+ CompassConfig.lastCfgCheckSection +separator+ CompassConfig.lastCfgCheckName +separator+ "" +separator+ "" +separator+ "" +separator+ "" +separator+ "" +separator+ "" + separator + "~" + separator;
				}
			}
		}
		// Only wipe out in case the current item is not supported since we do complexity scores only for NotSupported items
		if (status.equals(u.NotSupported)) {
			CompassConfig.lastCfgCheckSection = "";
			CompassConfig.lastCfgCheckName = "";
		}

		// newlines are allowed in delimited identifiers (very rare, but possible). Remove 'm from itemDetail
		// treat these chars the same as when writing to the symtab
		if (itemDetail.contains("\n") || itemDetail.contains("\r")) {
			if (u.debugging) u.dbgOutput("Newline or CR found in itemDetail (removed): ["+itemDetail+"] ", u.debugSymtab||u.debugPtree);
			itemDetail = itemDetail.replaceAll("\\n", " ");
			itemDetail = itemDetail.replaceAll("\\r", " ");
		}

		// create the record
		// NB: this format corresponds to 'captureFileFormatVersion = 1'
		// if this format is ever changed, we need to provide backward compatibility to avoid breaking apps relying on the format; also potentially affects -pgimport upload file preparation
		String currentContext_copy = currentContext;
		String subContext_copy = subContext;
		if (Compass.analyzingDynamicSQL) {
			lineNr = Compass.dynamicSQLLineNr + lineNr - 1;
			currentContext_copy = Compass.dynamicSQLContext;
			subContext_copy = Compass.dynamicSQLSubContext;
		}
		String itemLine = item +separator+ itemDetail.trim() +separator+ itemGroup +separator+ status +separator+ lineNr +separator+ u.currentAppName +separator+ u.currentSrcFile  +separator+ u.batchNrInFile +separator+ u.lineNrInFile +separator+ currentContext_copy.trim() +separator+ subContext_copy.trim() +separator+ misc + separator + "~" + separator;

		// check for newlines -- these will mess everything up (could still occur due to identifiers containing a newline)
		// printing a warning so that any cases that may results from bugs, are not being lost and may be reported back
		// ToDo: also check for \f, VT, etc?
		if (itemLine.contains("\r\n")) {
			u.appOutput("CRLF found in captured item: ["+itemLine+"] ");
			itemLine = itemLine.replaceAll("\\r\\n", "  ");
			if (CompassUtilities.devOptions) {
				u.errorExitStackTrace();
				// we'll never get here
			}
			u.appOutput("Continuing with CRLF removed, but errors may occur.");
		}

		if (itemLine.contains("\n")) {
			u.appOutput("Newline found in captured item: ["+itemLine+"] ");
			itemLine = itemLine.replaceAll("\\n", " ");
			if (CompassUtilities.devOptions) {
				u.errorExitStackTrace();
				// we'll never get here
			}
			u.appOutput("Continuing with newline removed, but errors may occur.");
		}

		if (itemLine.contains("\r")) {
			u.appOutput("Carriage Return found in captured item: ["+itemLine+"] ");
			itemLine = itemLine.replaceAll("\\r", " ");
			if (CompassUtilities.devOptions) {
				u.errorExitStackTrace();
				// we'll never get here
			}
			u.appOutput("Continuing with Carriage Return removed, but errors may occur.");
		}

		// avoid end-of-input chars (\.) , for later loading into PG through COPY
		if (itemLine.contains("\\")) {
			itemLine = u.applyPatternAll(itemLine, "\\\\", "\\\\\\\\");
		}

		// cleanup
		itemLine = u.applyPatternAll(itemLine, ",\\s*,", ",");

		//write record
		try {
			u.appendCaptureFile(itemLine);
		} catch (Exception e) {
			u.appOutput("Error writing to capture file");
		}

		// write Xref record
		if (!xrefLine.isEmpty()) {
			try {
				u.appendCaptureFile(xrefLine);
			} catch (Exception e) {
				u.appOutput("Error writing XRefOnly record to capture file");
			}
		}

	    // debug
	    if (u.echoCapture) {
			u.appOutput("captured: itemLine=["+itemLine+"] ");
		   	u.printStackTrace();
		}

	    return;
	}

	//--- actual capturing of SELECT -----------------------------------------------

	private void captureSELECT(CompassItem sel, int qID) {
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"stmt: qID=["+qID+"] name=["+sel.getName()+"] (line "+sel.getLineNr()+") attributes=["+sel.getAttributes()+"]", u.debugPtree);
		if (sel.getName().equals("SELECT")) {
			String item = "SELECT";
			if (sel.getAttributes().contains(" SUBQUERY ")) {
				item = "SELECT subquery";
			}
			else if (sel.getAttributes().contains(" CTE ")) {
				item = "SELECT in Common Table Expression";
			}
			else if (sel.getAttributes().contains(" WITH ")) {
				item = "WITH (Common Table Expression) SELECT";
			}

			String status = u.Supported;
			String objName = "";
			if (sel.getAttributes().contains(" INTO ")) {
				String tabType = CompassUtilities.getPatternGroup(sel.getAttributes(), " INTO (.*?) ", 1);
				if (tabType.equalsIgnoreCase("table")) tabType = "";
				item = item.replaceFirst("SELECT", "SELECT..INTO "+tabType);
				objName = sel.getObjectName();
				assert !objName.isEmpty(): "object name for SELECT-INTO cannot be empty";

				if (tabType.equals(GlobalTmpTableFmt)) {
					status = featureSupportedInVersion(GlobalTmpTable);
				}
			}

			captureItem(item, objName, DMLReportGroup, "", status, sel.getLineNr());

			if (sel.getAttributes().contains(" VIEW ")) return;  //cases below do not apply
			if (sel.getAttributes().contains(" RETURNS ")) return;  //cases below do not apply

			if ((sel.getAttributes().contains(" TOP ")) && (!sel.getAttributes().contains(" ORDERBY "))) {
				// flag TOP without ORDER-BY, but not when it is in an EXISTS predicate
				if (!sel.getAttributes().contains(" EXISTS ")) {
					String statusTOP = featureSupportedInVersion(SelectTopWoOrderBy);
					captureItem(SelectTopWoOrderBy, "", DMLReportGroup, "", statusTOP, sel.getLineNr());
				}
			}

			// Highlight some specific variants of SELECT-INTO, which require a review of the semantics
			if (sel.getAttributes().contains("INTO ") && featureExists(SelectInto)) {
				//u.appOutput(u.thisProc()+"SELECT-INTO: ["+sel.getAttributes()+"] ");
				String SelectIntoVariant = "";
				String SelectIntoVariantFmt = "";
				if (!sel.getAttributes().contains(" IDENTITY ")) {
					if (sel.getAttributes().contains(" ORDERBY ")) {
						if (!sel.getAttributes().contains(" TOP "))	{
							// no IDENTITY(), no TOP, but there is an ORDER BY: mention that ORDER BY has no impact on anything
							SelectIntoVariant = SelectIntoVariantNoTopNoIdentityOrderBy;
							SelectIntoVariantFmt = SelectIntoVariantFmtNoTopNoIdentityOrderBy;
						}
					}
					else {
						// no ORDER BY: nothing special to report
					}
				}
				else {
					// IDENTITY() function in SELECT-INTO
					if (sel.getAttributes().contains(" IDENTITY_NOTLAST ")) {
						// IDENTITY() function in SELECT-INTO: the IDENTITY column will be the last column in the table in BBF, but it is not the last in the statement
						SelectIntoVariant = SelectIntoVariantIdentityNotLast;
						SelectIntoVariantFmt = SelectIntoVariantFmtIdentityNotLast;
					}
				}
				if (!SelectIntoVariant.isEmpty()) {
					String siStatus = featureSupportedInVersion(SelectInto, SelectIntoVariant);
					captureItem(SelectIntoVariantFmt, "", DMLReportGroup, "", siStatus, sel.getLineNr());
				}
			}

			if ( !(sel.getAttributes().contains(" INTO ")) &&
			    (!sel.getAttributes().contains(" SUBQUERY ")) &&
			    (!sel.getAttributes().contains(" INSERT ")) &&
			    (!sel.getAttributes().contains(" VARIABLE_ASSIGN ")) &&
			    (!sel.getAttributes().contains(" ORDERBY ")) &&
			    (!sel.getAttributes().contains(" EXISTS "))  ) {
				// SELECT-to-client without ORDER-BY

				// commented out because many of these case appear to be single-row SELECTs, or cases where the sorting is done in the client
				// we might want to offer this as an optional feature to detect, but then we should also report on all cases of 'SELECT @v = column'

				//String statusClient = featureSupportedInVersion(SelectToClientWoOrderBy);
				//captureItem(SelectToClientWoOrderBy, "", DMLReportGroup, "", statusClient, sel.getLineNr());
			}
		}
		else {
			assert false : "unexpected branch";
		}
	}

	public void CaptureXMLNameSpaces(RuleContext parent, String stmt, int lineNr) {
		if (parent == null) {
			String status = featureSupportedInVersion(XMLFeatures,"WITH XMLNAMESPACES");
			captureItem("WITH XMLNAMESPACES", "", XMLFeatures, "WITH XMLNAMESPACES", status, lineNr);
		}
		else {
			// this may not be relevant anymore after the grammar incorporates XMLNAMESPACES as part of CTE
			if (hasParent(parent,"declare_xmlnamespaces_statement")) {
				String status = featureSupportedInVersion(XMLFeatures,"WITH XMLNAMESPACES");
				captureItem("WITH XMLNAMESPACES.."+stmt, "", XMLFeatures, "WITH XMLNAMESPACES", status, lineNr);
			}
		}
	}

	private void captureXMLFeature (String stmt, String feature, String fmt, int lineNr) {
		String status = featureSupportedInVersion(XMLFeatures, feature);
		String compCol = "";
		if (inCompCol) {
			// this is a XML method call inside a computed column
			compCol += ", in computed column"+inCompColType;
			if (status.equals(u.Supported)) {
				String statusUDF = featureSupportedInVersion(CompColFeatures, cfgXmlMethodCall);
				status = statusUDF;
			}
		}
		captureItem(stmt+feature+fmt+compCol, "", XMLFeatures, feature, status, lineNr, "0");
	}

	private void captureHIERARCHYIDFeature (String stmt, String feature, String fmt, int lineNr) {
		String status = featureSupportedInVersion(HIERARCHYIDFeatures, feature);
		String compCol = "";
		if (inCompCol) {
			// this is a HIERARCHYID method call inside a computed column
			compCol += ", in computed column"+inCompColType;
			if (status.equals(u.Supported)) {
				String statusUDF = featureSupportedInVersion(CompColFeatures, cfgHierachyIdMethodCall);
				status = statusUDF;
			}
		}
		// method names are case-sensitive, apply correct case
		feature = CompassUtilities.HIERARCHYIDmethodsFmt.get(CompassUtilities.HIERARCHYIDmethods.indexOf(feature.toUpperCase()));
		captureItem(stmt+feature+fmt+compCol, "", HIERARCHYIDFeatures, feature, status, lineNr, "0");
	}

	private void captureDoubleQuotedString(String s, int startLine, int startPos, int stopLine, int stopPos, int startIndex, int stopIndex) {
		String itemChk = cfgDoubleQuotedString;   // default, in case no embedded quotes
		String item = "";
		s = s.substring(1, s.length()-1);

		// a string may have both embedded double and single quotes, but only reporting one
		if (s.contains("'")) {
			itemChk = cfgDoubleQuoteEmbeddedSingleQuote;
			item = ", embedded single quote";
		}
		else if (s.contains("\"")) {
			itemChk = cfgDoubleQuoteEmbeddedDoubleQuote;
			item = ", embedded double quote";
		}
		else {
			// no embedded quotes
		}

		item = DoubleQuotedString + item;
		String status = featureSupportedInVersion(DoubleQuotedString, itemChk);
		if (!status.equals(u.Supported)) {
			if (u.rewrite) {
				s = s.replaceAll("\"\"", "\"");   // remove escaped double quotes
				s = s.replaceAll("'", "''");      // escape single quotes
				String rewriteText = "'" + s + "'";
				if (addRewrite(item, DoubleQuotedString, u.rewriteTypeReplace, rewriteText, startLine, startPos, stopLine, stopPos, startIndex, stopIndex))
					status = u.Rewritten;
			}
			else {
				addRewrite(item);
			}
		}
		captureItem(item, "", "", "", status, startLine);
	}

	// --- handling SET QUOTED_IDENTIFIER ----------------------------------------------

	// Get ancestor by its name
	private RuleContext getRule (RuleContext ctx, String name) {
		if (ctx == null || name == null) {
			return null;
		}
		String ruleName = CompassUtilities.grammarRuleNames[ctx.getRuleIndex()];
		if (ruleName.equals(name)) {
			return ctx;
		}
		return getRule(ctx.parent, name);
	}

	private void captureQuotedIdentifierInBatch(TSQLParser.Set_specialContext ctx, String on_off) {
		String status = featureSupportedInVersion(SetQuotedIdInBatch);
		String warningMessage = "";
		if (!status.equals(u.Supported)) warningMessage = ": applies at next batch";
		captureItem("SET QUOTED_IDENTIFIER "+on_off+", before end of batch" + warningMessage, "", "", "", status, ctx.start.getLine());
	}

	// Check if the subsequent statements in the batch are session-level SET statements (i.e. the set_special rule)
	private boolean setQuotedIdentifierNextStmts(List<TSQLParser.Sql_clausesContext> allSqlClauses, int index) {
		List<TSQLParser.Sql_clausesContext> sqlClauses = allSqlClauses.subList(index, allSqlClauses.size());
		for (TSQLParser.Sql_clausesContext sqlClause : sqlClauses) {
			TSQLParser.Another_statementContext anotherStmt = sqlClause.another_statement();
			if (anotherStmt == null) {
				return true;
			}
			TSQLParser.Set_statementContext setStmt = anotherStmt.set_statement();
			if (setStmt == null) {
				return true;
			}
			TSQLParser.Set_specialContext setSpecial = setStmt.set_special();
			if (setSpecial == null) {
				return true;
			}
		}
		return false;
	}

	private void detectSetQuotedIdentifier(int passNr, TSQLParser.Set_specialContext ctx, String option) {
		if (option.equals("QUOTED_IDENTIFIER")) {
			if (hasParent(ctx.parent,"create_or_alter_procedure") ||
					hasParent(ctx.parent,"create_or_alter_function") ||
					hasParent(ctx.parent,"create_or_alter_trigger")
			) {
				// don't do anything -- inside a proc/func body, SET QUOTED_IDENTIFIER has no effect
			}
			else {
				String on_off = ctx.on_off().getText().toUpperCase();
				TSQLLexer.QUOTED_IDENTIFIER_FLAG = on_off.equals("ON");
				u.QuotedIdentifierFlag = TSQLLexer.QUOTED_IDENTIFIER_FLAG;

				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"pass" + passNr + ": setting QUOTED_IDENTIFIER: on_off=["+on_off+"]  TSQLLexer.QUOTED_IDENTIFIER_FLAG=["+TSQLLexer.QUOTED_IDENTIFIER_FLAG+"] ", u.debugPtree);
				if (passNr == 2) {
					TSQLParser.Sql_clausesContext sqlClauses;
					RuleContext parentSqlClauses = ctx;
					String parentSqlClausesName = "";

					while (true) {
						do {
							sqlClauses = (TSQLParser.Sql_clausesContext) getRule(parentSqlClauses, "sql_clauses");
							parentSqlClauses = sqlClauses.parent;
							parentSqlClausesName = CompassUtilities.grammarRuleNames[parentSqlClauses.getRuleIndex()];
						} while (parentSqlClausesName.equals("block_statement"));

						if (parentSqlClausesName.equals(CompassUtilities.startRuleName)) {
							// check if it's the last statement in the sql_clauses list
							TSQLParser.Tsql_fileContext tsqlCtx = (TSQLParser.Tsql_fileContext) parentSqlClauses;
							int sqlClauseIndex = tsqlCtx.sql_clauses().indexOf(sqlClauses);
							if (sqlClauseIndex != tsqlCtx.sql_clauses().size() - 1) {
								// it's not the last statement in the batch
								if (setQuotedIdentifierNextStmts(tsqlCtx.sql_clauses(), sqlClauseIndex)) {
									// it's followed only by SET statements
									captureQuotedIdentifierInBatch(ctx, on_off);
								}
							}
							break;
						}
						captureQuotedIdentifierInBatch(ctx, on_off);
						break;
					}
				}
			}
		}
	}

	// ---------------------------------------------------------------------------------

	public void analyzeTree(TSQLParser.Tsql_fileContext tree, Integer batchNr, Integer batchLines, Integer pass)  {
		final StringBuilder visitTracker = new StringBuilder("");
		Map<Integer, CompassItem> stmt = new HashMap<Integer, CompassItem>();

		TSQLParserBaseVisitor<String> pass1Analysis = new TSQLParserBaseVisitor<String>() {

			@Override public String visitSet_special(TSQLParser.Set_specialContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				int nrOptions = ctx.set_on_off_option().size();
				int nrId = ctx.id().size();
				if (nrOptions > 0) {
					List<TSQLParser.Set_on_off_optionContext> options = ctx.set_on_off_option();
					for (int i=0; i<nrOptions; i++) {
						String option = options.get(i).getText().toUpperCase();
						detectSetQuotedIdentifier(pass, ctx, option);
					}
				}
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitUse_statement(TSQLParser.Use_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				u.setCurrentDB(ctx.dbname.getText().toUpperCase());
				//visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitCreate_type(TSQLParser.Create_typeContext ctx) {
				// this is duplicated in pass 2
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String UDDname = ctx.simple_name().getText();
				String UDDdatatype = "";
				if (ctx.FROM() != null) {
					// scalar UDD
					UDDdatatype = ctx.data_type().getText().toUpperCase();
					// cannot create a UDD for IDENTITY	with CREATE TYPE (that's a Sybase feature)
					if (ctx.data_type().IDENTITY() != null) {
						UDDdatatype = u.applyPatternFirst(UDDdatatype, "^(.*)(\\bIDENTITY\\b.*?)$", "$1").trim();
					}
				}
				else {
					// table type
					UDDdatatype = "TABLE";
				}

				u.addUDDSymTab(UDDname.toUpperCase(), UDDdatatype);
				//visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitExecute_body(TSQLParser.Execute_bodyContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if (ctx.func_proc_name_server_database_schema() != null) {
					HandleSystemProcPass1(ctx.func_proc_name_server_database_schema(), ctx.execute_statement_arg());
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitExecute_body_batch(TSQLParser.Execute_body_batchContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if (ctx.func_proc_name_server_database_schema() != null) {
					HandleSystemProcPass1(ctx.func_proc_name_server_database_schema(), (ctx.execute_statement_arg().size() == 0) ? null : ctx.execute_statement_arg().get(0));
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			private void HandleSystemProcPass1(TSQLParser.Func_proc_name_server_database_schemaContext name, TSQLParser.Execute_statement_argContext args) {
				String procName = u.normalizeName(name.getText());
				procName = u.getObjectNameFromID(procName).toLowerCase();
				if (procName.startsWith("sp_") || procName.startsWith("xp_")) {
					if (procName.equals("sp_addtype")) {
						if (args != null) {
							// ToDo: handle named parameter calls; not very likely to occur for sp_addtype however
							if (args.execute_statement_arg_unnamed() != null) {
								String UDDname = u.stripStringQuotes(args.execute_statement_arg_unnamed().getText()).toUpperCase().trim();
								if (args.execute_statement_arg() != null) {
									if (args.execute_statement_arg().execute_statement_arg_unnamed() != null) {
										String UDDdatatype = u.stripStringQuotes(args.execute_statement_arg().execute_statement_arg_unnamed().getText()).toUpperCase().trim();
										u.addUDDSymTab(UDDname, UDDdatatype);
									}
								}
							}
						}
					}
				}
				return;
			}

			@Override public String visitCreate_table(TSQLParser.Create_tableContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String tableName = ctx.table_name().getText().toUpperCase();
				u.addtableViewSymTab(tableName, "TABLE");
				u.setContext("TABLE", tableName);
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitCreate_or_alter_view(TSQLParser.Create_or_alter_viewContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String viewName = ctx.simple_name().getText().toUpperCase();
				u.addtableViewSymTab(viewName, "VIEW");
				//visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitCreate_or_alter_function(TSQLParser.Create_or_alter_functionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String funcName = ctx.func_proc_name_schema().getText();
				if (ctx.func_body_returns_scalar() != null) {
					String sudfDataType = u.normalizeName(ctx.func_body_returns_scalar().data_type().getText().toUpperCase(), "datatype");
					u.addSUDFSymTab(funcName, sudfDataType);
				}
				else {
					u.addTUDFSymTab(funcName, "TABLE");
				}
				// set context
				u.setContext("FUNCTION", funcName);
				captureParameters("function", ctx.procedure_param());

				//visitChildren(ctx);

				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitCreate_or_alter_procedure(TSQLParser.Create_or_alter_procedureContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String procName = ctx.func_proc_name_schema().getText();
				u.addProcSymTab(procName, "PROCEDURE");
				// set context
				u.setContext("PROCEDURE", procName);
				captureParameters("procedure", ctx.procedure_param());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitCreate_or_alter_dml_trigger(TSQLParser.Create_or_alter_dml_triggerContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String trigName = ctx.simple_name().getText();

				// set context
				u.setContext("TRIGGER", trigName);

				//visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitCreate_or_alter_ddl_trigger(TSQLParser.Create_or_alter_ddl_triggerContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String trigName = ctx.simple_name().getText();

				// set context
				u.setContext("TRIGGER", trigName);

				//visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitColumn_definition(TSQLParser.Column_definitionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"ctx=["+ctx.getText()+"] buildColSymTab=["+u.buildColSymTab+"] ", u.debugPtree);
				if (!u.buildColSymTab) return null; //only proceed if we really build a permament symtab for columns in pass 1

				// find type of column by looking for parent
				// todo: view columns, datatypes of view columns and computed columns
				String colType = ""; // default: regular table
				if (hasParent(ctx.parent,"declare_statement"))                 colType = "(table variable)";
				else if (hasParent(ctx.parent,"create_type"))                  colType = "(table type)";
				else if (hasParent(ctx.parent,"func_body_returns_table"))      colType = "(table function result)";
				else if (hasParent(ctx.parent,"func_body_returns_table_clr"))  colType = "(table function result)";

				if (!colType.isEmpty()) return null;

				String colName  = "";
				String dataType = "";

				if (ctx.TIMESTAMP() != null) {
					colName = "timestamp";
					dataType = "TIMESTAMP";
				}
				else {
					colName = u.normalizeName(ctx.id().getText());

		            if (ctx.data_type() == null) return null;

	            	// regular column
	            	dataType = u.normalizeName(ctx.data_type().getText().toUpperCase(), "datatype");
	            	if (ctx.data_type().IDENTITY() != null) {
	            		dataType = u.applyPatternFirst(dataType, "^(.*)(IDENTITY.*?)$", "$1");
	            		dataType = u.applyPatternFirst(dataType, "((NOT)?\\s+NULL)?$", "");
	            	}
	            }

            	if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"column: colName=["+colName+"] dataType=["+dataType+"] currentObjectName=["+u.currentObjectName+"] ", u.debugPtree);

            	// add to symbol table (experimental)
            	// only do this for CREATE/ALTER TABLE
            	// Todo: handle table type, table variable
            	if (hasParent(ctx.parent,"create_table") || hasParent(ctx.parent,"alter_table")) {
	            	boolean nullable = false;
	            	if (ctx.null_notnull().size() == 0) {
	            		if (ctx.column_constraint().size() > 0) {
	            			if (ctx.column_constraint().get(0).null_notnull() != null) {
	            				if (ctx.column_constraint().get(0).null_notnull().NOT() == null) nullable = true;
	            			}
	            			else if (ctx.column_constraint().size() > 1) {
	            				if (ctx.column_constraint().get(1).null_notnull() != null) {
		            				if (ctx.column_constraint().get(1).null_notnull().NOT() == null) nullable = true;
		            			}
	            			}
	            		}
	            	}
	            	else if (ctx.null_notnull().size() > 0) {
	            		if (ctx.null_notnull().get(0).NOT() == null) nullable = true;
	            	}
	            	//u.appOutput(u.thisProc()+"currentObjectName=["+u.currentObjectName+"] currentObjectType=["+u.currentObjectType+"] currentObjectNameSub=["+u.currentObjectNameSub+"]  currentObjectTypeSub=["+u.currentObjectTypeSub+"] ");
	            	String objName = "";
	            	if (u.currentObjectType.equals("TABLE")) objName = u.currentObjectName;
	            	else if (u.currentObjectTypeSub.equals("TABLE")) objName = u.currentObjectNameSub;
	            	if (!objName.isEmpty()) {
		            	u.addColSymTab(objName, colName, dataType, nullable, false);
		            }
		        }

				//visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			public void captureParameters(String objType, List<TSQLParser.Procedure_paramContext> params) {
				for(int i=0; i<params.size(); i++) {
					if (params.get(i).default_val == null) continue;
		            String parName = params.get(i).LOCAL_ID().getText();
		            //String dataType = u.normalizeName(params.get(i).data_type().getText().toUpperCase(), "datatype");
		            String parDft = params.get(i).default_val != null ? params.get(i).default_val.getText() : "";
				  	if (!parDft.isEmpty()) {
						// add parameter to symbol table, for resolving DEFAULT argument when called
						int parNo = i + 1;
						u.addParSymTab(u.resolveName(u.currentObjectName), parName, parNo, parDft);
				  	}
		        }
		    }

		};

		TSQLParserBaseVisitor<String> pass2Analysis = new TSQLParserBaseVisitor<String>() {
			int queryCnt = 0;
			int queryIDNr = 0;
			int selectListColumnNr = 0;
			int selectListNrColumns = 0;
			boolean inAtAtErrorPredicate = false;
			boolean inExistsPredicate = false;
			boolean inCTE = false;
			boolean inCTESelectAttribute = false;
			boolean inPivot = false;
			int lineNrPivot = 0;
			boolean inAnsiJoin = false;
			boolean inCommaJoin = false;
			boolean inSubquery = false;
			boolean inDerivedTB = false;
			boolean inSelectStandalone = false;
			boolean inTUDFCall = false;
			boolean inMultiStmtTUDF = false;
			boolean hasSystemVersioningColumn = false;
			boolean stringAggWorkaround = false;
			List<String> hasDeclareCursorName = new ArrayList<>();
			boolean STRING_AGG_WITHIN_GROUP = false;
			int execute_statement_argParamCount = 0;
			String execute_statement_procName = "";
			String updVarAssign = "";
			Map<String, TSQLParser.ExpressionContext> variableAssignDepends = new HashMap<String, TSQLParser.ExpressionContext>();
			Stack<Integer> queryID = new Stack<>();

			// assign unique number to each SELECT query in the statement. Right now, just a sequence number
			int newQueryIDNr() {
				int i = newQueryIDNr(queryCnt);
				return i;
			}
			int newQueryIDNr(int cnt) {
				int i = cnt;
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"generating new queryIDNr=["+queryIDNr+"]  ", u.debugPtree);
				//u.printStackTrace();
				return i;
			}

			void popSelectLevel() {
				int popped = queryID.pop();
				if (!queryID.empty()) {
					queryIDNr = queryID.peek();
				}
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"popped=["+popped+"]: new queryIDNr=["+queryIDNr+"]  queryCnt=["+queryCnt+"] queryID.size=["+queryID.size()+"] ", u.debugPtree);
			}

			void newSelectStmt(String s, int lineNr) {
				queryCnt++;
				queryIDNr = newQueryIDNr();
				queryID.push(queryIDNr);
				CompassItem item = new CompassItem(s, lineNr);
				stmt.put(queryIDNr, item);
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"new queryIDNr=["+queryIDNr+"]: s=["+s+"]  lineNr=["+lineNr+"] item.lineNr=["+item.getLineNr()+"] ", u.debugPtree);
			}

			CompassItem getStmt(int i) {
				return stmt.get(i);
			}

			void addStmtAttribute(String s) {
				addStmtAttribute(queryIDNr, s);
			}
			void addStmtAttribute(int IDNr, String s) {
				CompassItem item = getStmt(IDNr);
				item.attributeAppend(s);
				stmt.put(IDNr, item);
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"appended to IDNr=["+IDNr+"]: s=["+s+"] result=["+item.getAttributes()+"] ", u.debugPtree);
			}
			void setStmtAttributeObjName(String name) {
				setStmtAttributeObjName(queryIDNr, name);
			}
			void setStmtAttributeObjName(int IDNr, String name) {
				CompassItem item = getStmt(IDNr);
				item.setObjectName(name);
				stmt.put(IDNr, item);
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"objName set to ["+name+"] in IDNr=["+IDNr+"]: result=["+item.getObjectName()+"] ", u.debugPtree);
			}

			private void captureAtAtVariables(String varName, int lineNr) {
				captureAtAtVariables(varName, lineNr, "reference");
			}
			private void captureAtAtVariables(String varName, int lineNr, String type) {
				// Catch references to @@ variables. @@PGERROR is a special case
				if (varName.startsWith("@@")) {
					if (inAtAtErrorPredicate && varName.equalsIgnoreCase("@@ERROR")) return;  // this is captured elsewhere
					if (!CompassUtilities.getPatternGroup(varName, "^\\@\\@(["+u.identifierChars+"]*)$", 1).isEmpty()) {
						String varType = "variable";
						if (u.currentObjectType.equals("PROCEDURE")) varType = "procedure parameter";
						if (u.currentObjectType.equals("FUNCTION")) varType = "function parameter";
						if (type.equals("named_arg")) varType = "named procedure argument";
						
						// is this a known global variable, or a user-defined variable starting with '@@' ?
						if (featureExists(AtAtVariable, varName)) {
							String status = u.NotSupported;
							String msg = "";
							if (type.equals("declare") || type.equals("named_arg")) {
								// predefined @@globalvar names not allowed here; in older BBF versions it would not raise an error but would produce incorrect results
								status = u.NotSupported;
								msg = globalAtAtVarDeclared;
								if (type.equals("named_arg")) msg = globalAtAtVarNamedArg;
							}
							else {
								status = featureSupportedInVersion(AtAtVariable, varName);
								msg = globalAtAtVarReference;
							}
							captureItem(varName+msg, "", AtAtVariable, varName, status, lineNr);
						}
						else {
							// it's a user-defined name
							String varTypeReport = VarDeclareAtAt;
							if (varType.startsWith("procedure")) varTypeReport = ProcParDeclareAtAt;
							if (varType.startsWith("function")) varTypeReport = FuncParDeclareAtAt;
							if (type.equals("named_arg")) varTypeReport = NamedArgAtAt;
							
							String status = featureSupportedInVersion(VarDeclareAtAt);
							captureItem(varTypeReport, varName, "", varName, status, lineNr);
						}
					}
				}
			}

			// --- eval the datatype of an expression ----------------------------------------------------------------------
			private String expressionDataType(String s) {
				String result = "";
				s = s.toUpperCase();
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"s=["+s+"] ", u.debugPtree);
				if (isNumeric(s)) result = CompassUtilities.BBFNumericType;
				else if (isString(s)) result = CompassUtilities.BBFStringType;
				else if (isDateTime(s)) result = CompassUtilities.BBFDateTimeType;
				else if (isBinary(s)) result = CompassUtilities.BBFBinaryType;
				else result = CompassUtilities.BBFUnknownType;
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"s=["+s+"] result=["+result+"] ", u.debugPtree);
				return result;
			}

			private String expressionDataType(TSQLParser.ExpressionContext expr) {
				String s = "";
				if (u.debugging && u.debugPtree) s = expr.getText();
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"expr=["+s+"] ", u.debugPtree);

				if (expr == null) {
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"expr == null; result=["+CompassUtilities.BBFUnknownType+"] ", u.debugPtree);
					return CompassUtilities.BBFUnknownType;
				}

				if ((expr instanceof TSQLParser.Constant_exprContext))  {
					TSQLParser.Constant_exprContext x = (TSQLParser.Constant_exprContext) expr;
					String result = "";
					if (x.constant().char_string() != null) result = CompassUtilities.BBFStringType;
					else if (x.constant().hex_string() != null) result = CompassUtilities.BBFBinaryType;
					else if (x.constant().NULL() != null) result = CompassUtilities.BBFNullType;
					else result = CompassUtilities.BBFNumericType;
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"Constant_exprContext: result=["+result+"] ", u.debugPtree);
					return result;
				}

				if ((expr instanceof TSQLParser.Collate_exprContext))  {
					String result = "";
					result = CompassUtilities.BBFStringType;
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"Collate_exprContext: result=["+result+"] ", u.debugPtree);
					return result;
				}

				if ((expr instanceof TSQLParser.Time_zone_exprContext))  {
					String result = "";
					result = CompassUtilities.BBFDateTimeType;
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"Time_zone_exprContext: result=["+result+"] ", u.debugPtree);
					return result;
				}

				if ((expr instanceof TSQLParser.Mult_div_percent_exprContext))  {
					String result = "";
					result = CompassUtilities.BBFNumericType;
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"Mult_div_percent_exprContext: result=["+result+"] ", u.debugPtree);
					return result;
				}

				if ((expr instanceof TSQLParser.Plus_minus_bit_exprContext))  {
					TSQLParser.Plus_minus_bit_exprContext x = (TSQLParser.Plus_minus_bit_exprContext) expr;
					String result = "";
					// optimize for deeply nested expressions (e.g. complex views): try to detect a string early to avoid endless left-deep recursions
					String expr0Text = x.expression().get(0).getText();
					if (expr0Text.endsWith("'")) return CompassUtilities.BBFStringType;
					String expr1Text = x.expression().get(1).getText();
					if (expr1Text.startsWith("'")) return CompassUtilities.BBFStringType;

					String expr0 = expressionDataType(x.expression().get(0));
					String expr1 = expressionDataType(x.expression().get(1));
					if (x.PLUS() != null) {
						if (isDateTime(expr0) || isDateTime(expr1)) result = CompassUtilities.BBFDateTimeType;
						else if (isString(expr0) && isString(expr1)) result = CompassUtilities.BBFStringType;
						else if (isBinary(expr0) || isBinary(expr1)) result = CompassUtilities.BBFBinaryType;
						else if (isUnknown(expr0) || isUnknown(expr1)) result = CompassUtilities.BBFUnknownType;
						else result = CompassUtilities.BBFNumericType;
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"op=PLUS", u.debugPtree);
					}
					else if (x.MINUS() != null) {
						if (isDateTime(expr0) || isDateTime(expr1)) result = CompassUtilities.BBFDateTimeType;
						else result = CompassUtilities.BBFNumericType;
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"op=MINUS", u.debugPtree);
					}
					else if (isBinary(expr0) || isBinary(expr1)) result = CompassUtilities.BBFBinaryType;
					else result = CompassUtilities.BBFNumericType;

					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"Plus_minus_bit_exprContext: result=["+result+"] ", u.debugPtree);
					return result;
				}

				if ((expr instanceof TSQLParser.Unary_op_exprContext))  {
					TSQLParser.Unary_op_exprContext x = (TSQLParser.Unary_op_exprContext) expr;
					String result = expressionDataType(x.expression());
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"Unary_op_exprContext: result=["+result+"] ", u.debugPtree);
					return result;
				}

				if ((expr instanceof TSQLParser.Default_exprContext))  {
					// don't know the datatype, would have to look up the proc/func to get its parameters
					String result = "";
					result = CompassUtilities.BBFUnknownType;
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"Default_exprContext: result=["+result+"] ", u.debugPtree);
					return result;
				}

				if ((expr instanceof TSQLParser.Full_col_name_exprContext))  {
					String result = "";
					String col = expr.getText().toUpperCase();
					// look up the column, but since we don't have a way to resolve column references in a query yet,
					// only do this inside a CREATE TABLE/ALTER TABLE stmt
					// ToDo: maybe identify single-table queries as an easy case?
					String objName = "";
	            	if (u.currentObjectType.equals("TABLE")) objName = u.currentObjectName;
	            	else if (u.currentObjectTypeSub.equals("TABLE")) objName = u.currentObjectNameSub;
	            	if (!objName.isEmpty()) {	// only do this for CREATE/ALTER TABLE:
	            		String colName = u.getObjectNameFromID(col);  // get the last name in a combined name, which is the column name here
						String colDataType = lookupCol(objName, colName);
						if (!colDataType.isEmpty()) result = colDataType;
	            	}

					if (result.isEmpty()) result = CompassUtilities.BBFUnknownType;
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"Full_col_name_exprContext: ["+col+"] result=["+result+"] ", u.debugPtree);
					return result;
				}

				if ((expr instanceof TSQLParser.Dollar_action_exprContext))  {
					String result = "";
					// Assume integer as default since we have nothing better; this is unlikely to occur anyway
					result = CompassUtilities.BBFNumericType;
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"Dollar_action_exprContext: result=["+result+"] ", u.debugPtree);
					return result;
				}

				if ((expr instanceof TSQLParser.Local_id_exprContext))  {
					TSQLParser.Local_id_exprContext x = (TSQLParser.Local_id_exprContext) expr;
					String result = CompassUtilities.BBFUnknownType;
					if (x.DOT().size() > 0) {
						if (x.method_call().size() > 0) {
							TSQLParser.Method_callContext m = x.method_call().get(0);
							if (m.xml_methods() != null) {
								if (m.xml_methods().xml_value_call() != null) {
									String sqltype = u.stripStringQuotes(m.xml_methods().xml_value_call().sqltype.getText());
									result = expressionDataType(sqltype);
								}
							}
						}
					}
					// look up datatype of this variable or parameter
					else {
						s = expr.getText();
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"looking up var=["+s+"] varDataType(s)=["+varDataType(s)+"]  ", u.debugPtree);
						result = expressionDataType(varDataType(s));
					}
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"Local_id_exprContext: result=["+result+"] ", u.debugPtree);
					return result;
				}

				if ((expr instanceof TSQLParser.Func_call_exprContext))  {
					TSQLParser.Func_call_exprContext x = (TSQLParser.Func_call_exprContext) expr;
					String result = "";
					if (x.DOT().size() > 0) {
						result = CompassUtilities.BBFStringType;
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"Func_call_exprContext: result=["+result+"] ", u.debugPtree);
						return result;
					}

					// look up datatype of this function
					String funcCall = x.function_call().getText().toUpperCase();
					boolean isODBC = false;
					if (funcCall.toUpperCase().startsWith("{FN")) {
						isODBC = true;
						funcCall = funcCall.substring(3);
					}
					String funcName = funcCall;
					if (funcCall.indexOf("(") != -1) funcName = funcCall.substring(0,funcCall.indexOf("("));
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"funcCall=["+funcCall+"] funcName=["+funcName+"] isODBC=["+isODBC+"] ", u.debugPtree);
					if (funcName.isEmpty()) {
						result = CompassUtilities.BBFUnknownType;
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"empty: result=["+result+"] ", u.debugPtree);
						return result;
					}

					if (isODBC) {
						String odbcDataType = "";
						if (funcName.equals("CONVERT")) {
							// data type is at the end
							String tgtTypeRaw = x.function_call().getText().toUpperCase();
							String tgtType = u.applyPatternAll(tgtTypeRaw, "^.*\\b(\\w+)\\W*$", "$1");
							tgtType = u.applyPatternAll(tgtType, "^SQL_", "");
							odbcDataType = expressionDataType(tgtType);
						}
						else if (stringODBCs.contains(funcName)) odbcDataType = CompassUtilities.BBFStringType;
						else if (numericODBCs.contains(funcName)) odbcDataType = CompassUtilities.BBFNumericType;
						else if (datetimeODBCs.contains(funcName)) odbcDataType = CompassUtilities.BBFDateTimeType;
						else if (binaryODBCs.contains(funcName)) odbcDataType = CompassUtilities.BBFBinaryType;
						else odbcDataType = CompassUtilities.BBFUnknownType;
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"ODBC: funcCall=["+funcCall+"] funcName=["+funcName+"] odbcDataType=["+odbcDataType+"] ", u.debugPtree);
						result = odbcDataType;
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"ODBC function: result=["+result+"] ", u.debugPtree);
						return result;
					}

					if (funcName.equalsIgnoreCase("MIN") || funcName.equalsIgnoreCase("MAX")) {
						TSQLParser.ExpressionContext aggrExpr = x.function_call().aggregate_windowed_function().all_distinct_expression().expression();
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"MIN/MAX: expr=["+aggrExpr.getText()+"] ", u.debugPtree);
						return expressionDataType(aggrExpr);
					}
					else if (funcName.equalsIgnoreCase("ISNULL")) {
						// only looking at first arg; if unclear also look at the second arg (beware of implicit type conversions)
						TSQLParser.ExpressionContext isnullExpr = x.function_call().function_arg_list().expression().get(0);
						result = expressionDataType(isnullExpr);
						if (result.equals(CompassUtilities.BBFUnknownType)) {
							TSQLParser.ExpressionContext isnullExpr2 = x.function_call().function_arg_list().expression().get(1);
							result = expressionDataType(isnullExpr2);
						}
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"ISNULL: result=["+result+"] ", u.debugPtree);
						return result;
					}
					else if (funcName.equalsIgnoreCase("NULLIF")) {
						// only looking at first arg; if unclear also look at the second arg (beware of implicit type conversions)
						TSQLParser.ExpressionContext nullifExpr = x.function_call().function_arg_list().expression().get(0);
						result = expressionDataType(nullifExpr);
						if (result.equals(CompassUtilities.BBFUnknownType)) {
							TSQLParser.ExpressionContext nullifExpr2 = x.function_call().function_arg_list().expression().get(1);
							result = expressionDataType(nullifExpr2);
						}
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"ISNULL: result=["+result+"] ", u.debugPtree);
						return result;
					}
					else if (funcName.equalsIgnoreCase("COALESCE")) {
						// only looking at first arg; if unclear also look at the second arg, or even further (beware of implicit type conversions)
						TSQLParser.ExpressionContext coalExpr = x.function_call().function_arg_list().expression().get(0);
						result = expressionDataType(coalExpr);
						if (result.equals(CompassUtilities.BBFUnknownType)) {
							TSQLParser.ExpressionContext coalExpr2 = x.function_call().function_arg_list().expression().get(1);
							result = expressionDataType(coalExpr2);
						}
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"COALESCE: result=["+result+"] ", u.debugPtree);
						return result;
					}
					else if (funcName.equalsIgnoreCase("CHOOSE")) {
						// only looking at second arg(=first return value); if unclear we also look at the third arg (beware of implicit type conversions)
						if (x.function_call().function_arg_list().expression().size() > 1) {
							TSQLParser.ExpressionContext chooseExpr = x.function_call().function_arg_list().expression().get(1);
							result = expressionDataType(chooseExpr);
							if (result.equals(CompassUtilities.BBFUnknownType)) {
								if (x.function_call().function_arg_list().expression().size() > 2) {
									TSQLParser.ExpressionContext chooseExpr3 = x.function_call().function_arg_list().expression().get(2);
									result = expressionDataType(chooseExpr3);
								}
							}
							if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"CHOOSE: result=["+result+"] ", u.debugPtree);
							return result;
						}
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"CHOOSE: result=["+result+"] ", u.debugPtree);
						return CompassUtilities.BBFUnknownType;
					}
					else if (funcName.equalsIgnoreCase("IIF")) {
						// only looking at second arg(=first return value); if unclear we also look at the third arg (beware of implicit type conversions)
						if (x.function_call().built_in_functions() != null) {
							TSQLParser.IIFContext iif = (TSQLParser.IIFContext) x.function_call().built_in_functions().bif_other();
							TSQLParser.ExpressionContext iifExpr = iif.left;
							result = expressionDataType(iifExpr);
							if (result.equals(CompassUtilities.BBFUnknownType)) {
								TSQLParser.ExpressionContext iifExpr2 = iif.right;
								result = expressionDataType(iifExpr2);
							}
							if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"IIF: result=["+result+"] ", u.debugPtree);
							return result;
						}
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"IIF: result=["+result+"] ", u.debugPtree);
						return CompassUtilities.BBFUnknownType;
					}
					else if (funcName.equalsIgnoreCase("CAST")) {
						String castType = x.function_call().built_in_functions().bif_cast_parse().data_type().getText();
						return expressionDataType(castType);
					}
					else if (funcName.equalsIgnoreCase("CONVERT")) {
						String convertType = x.function_call().built_in_functions().bif_convert().data_type().getText();
						return expressionDataType(convertType);
					}

					funcName = u.normalizeName(funcName).toUpperCase();
					String sudfDataType = lookupSUDF(funcName);
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"funcCall=["+funcCall+"] funcName=["+funcName+"] sudfDataType=["+sudfDataType+"] ", u.debugPtree);
					if (!sudfDataType.isEmpty()) return expressionDataType(sudfDataType);

					if (stringBIFs.contains(funcName)) sudfDataType = CompassUtilities.BBFStringType;
					else if (numericBIFs.contains(funcName)) sudfDataType = CompassUtilities.BBFNumericType;
					else if (datetimeBIFs.contains(funcName)) sudfDataType = CompassUtilities.BBFDateTimeType;
					else if (binaryBIFs.contains(funcName)) sudfDataType = CompassUtilities.BBFBinaryType;
					else sudfDataType = CompassUtilities.BBFUnknownType;
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"BIF: funcCall=["+funcCall+"] funcName=["+funcName+"] sudfDataType=["+sudfDataType+"] ", u.debugPtree);

					// check for XML VALUE method
					if (sudfDataType.equals(CompassUtilities.BBFUnknownType)) {
						if (funcName.toUpperCase().endsWith(".VALUE")) {
							// get last argument
							String typeArg = CompassUtilities.BBFUnknownType;
							if (funcCall.lastIndexOf(",") != -1) {
								typeArg = funcCall.substring(funcCall.lastIndexOf(",")+1);
								typeArg = u.applyPatternAll(typeArg, "[\\)'\"]", "");
								sudfDataType = expressionDataType(typeArg);
							}
						}
					}

					result = sudfDataType;
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"result=["+result+"] ", u.debugPtree);
					return result;
				}

				if ((expr instanceof TSQLParser.Odbc_literal_exprContext)) {
					TSQLParser.Odbc_literal_exprContext x = (TSQLParser.Odbc_literal_exprContext) expr;
					String result = "";
					String op = x.odbc_literal().op.getText().toUpperCase();
					if (op.equals("GUID")) result = CompassUtilities.BBFBinaryType;
					else result = CompassUtilities.BBFDateTimeType;
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"Odbc_literal_exprContext: result=["+result+"] ", u.debugPtree);
					return result;
				}

				if ((expr instanceof TSQLParser.Bracket_exprContext))  {
					TSQLParser.Bracket_exprContext x = (TSQLParser.Bracket_exprContext) expr;
					String result = "";
					if (x.DOT().size() > 0) result = CompassUtilities.BBFStringType;  // assume string for all method calls
					else result = expressionDataType(x.expression());
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"Bracket_exprContext: result=["+result+"] ", u.debugPtree);
					return result;
				}

				if ((expr instanceof TSQLParser.Subquery_exprContext))  {
					TSQLParser.Subquery_exprContext x = (TSQLParser.Subquery_exprContext) expr;
					String result = "";
					if (x.DOT().size() > 0) result = CompassUtilities.BBFStringType;  // assume string for all method calls
					else {
						// try to determine the result type of the result set column
						// this needs to be extended to catch all cases
						result = CompassUtilities.BBFUnknownType;
						if (x.subquery().select_statement().query_expression().query_specification() != null) {
							TSQLParser.Select_list_elemContext se = x.subquery().select_statement().query_expression().query_specification().select_list().select_list_elem;
							if (se.asterisk() != null) result = CompassUtilities.BBFUnknownType;
							else {
								if (se.expression_elem() != null) {
									TSQLParser.ExpressionContext subqExpr = se.expression_elem().expression();
									result = expressionDataType(subqExpr);
								}
							}
						}
					}
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"Subquery_exprContext: result=["+result+"] ", u.debugPtree);
					return result;
				}

				if ((expr instanceof TSQLParser.Case_exprContext))  {
					TSQLParser.Case_exprContext x = (TSQLParser.Case_exprContext) expr;
					String result = "";
					// try to determine the result type of the CASE expression; look at the first THEN expression only
					TSQLParser.ExpressionContext thenExpr = null;
					if (x.case_expression().switch_section().size() > 0) {
						List<TSQLParser.Switch_sectionContext> caseList = x.case_expression().switch_section();
						thenExpr = caseList.get(0).expression().get(1);
					}
					else if (x.case_expression().switch_search_condition_section().size() > 0) {
						List<TSQLParser.Switch_search_condition_sectionContext> caseList = x.case_expression().switch_search_condition_section();
						thenExpr = caseList.get(0).expression();
					}

					result = expressionDataType(thenExpr);
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"Case_exprContext: result=["+result+"] ", u.debugPtree);
					return result;
				}

				if ((expr instanceof TSQLParser.Over_clause_exprContext))  {
					TSQLParser.Over_clause_exprContext x = (TSQLParser.Over_clause_exprContext) expr;
					String result = "";

					// ToDo: determine the result type of the OVER() clause expression
					result = CompassUtilities.BBFUnknownType;
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"Over_clause_exprContext: result=["+result+"] ", u.debugPtree);
					return result;
				}

				if ((expr instanceof TSQLParser.Hierarchyid_coloncolonContext))  {
					String result = "";
					// Todo what to return? let's do binary
					result = CompassUtilities.BBFBinaryType;
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"Hierarchyid_coloncolonContext: result=["+result+"] ", u.debugPtree);
					return result;
				}

				// we're out of options...
				String result = CompassUtilities.BBFUnknownType;
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"final unknown: result=["+result+"] ", u.debugPtree);
				return result;
			}

			// ---- common code for BIFs ------------------------------------------------------------------
			private void captureBIF(String funcName, int lineNr) {
				captureBIF(funcName, lineNr, "", 0, null, null, null, null, null, null, null, null);
			}
			private void captureBIF(String funcName, int lineNr, String options) {
				captureBIF(funcName, lineNr, options, 0, null, null, null, null, null, null, null, null);
			}
			private void captureBIF(String funcName, int lineNr, String options, int nrArgs) {
				captureBIF(funcName, lineNr, options, nrArgs, null, null, null, null, null, null, null, null);
			}

			private void captureBIF(String funcName, int lineNr, String options, int nrArgs, List<TSQLParser.ExpressionContext> argList) {
				captureBIF(funcName, lineNr, options, nrArgs, argList, null, null, null, null, null, null, null);
			}

			private void captureBIF(String funcName, int lineNr, String options, int nrArgs, List<TSQLParser.ExpressionContext> argList, List<String> argListText, TSQLParser.Func_proc_name_server_database_schemaContext func,  TSQLParser.Function_callContext funccall) {
				captureBIF(funcName, lineNr, options, nrArgs, argList, argListText, funccall.start.getLine(), funccall.start.getCharPositionInLine(), funccall.stop.getLine(), funccall.stop.getCharPositionInLine(), funccall.start.getStartIndex(), funccall.stop.getStopIndex());
			}
			private void captureBIF(String funcName, int lineNr, String options, Integer nrArgs, List<TSQLParser.ExpressionContext> argList, List<String> argListText, Integer startLine, Integer startCharPositionInLine, Integer stopLine, Integer stopCharPositionInLine, Integer startIndex, Integer stopIndex) {
				String status = u.NotSupported;
				String statusArgN = u.NotSupported;
				String groupCapture = BuiltInFunctions;  // used only for STRING_AGG()
				int argNum = 0;
				funcName = funcName.toUpperCase();
				String funcNameReport = funcName;
				String funcDetail = "";
				if (!options.contains("nobracket")) {
					funcNameReport = funcName + "()";
				}
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"BIF=["+funcName+"()] nrArgs=["+nrArgs+"] inCompCol=["+inCompCol+"] ", u.debugPtree);
				if (argList != null) if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"BIF=["+funcName+"()] nrArgs=["+nrArgs+"] argList.size()=["+argList.size()+"]  ", u.debugPtree);

				if (featureExists(BuiltInFunctions, funcName)) {
					status = featureSupportedInVersion(BuiltInFunctions, funcName);
					// any argument needs to be validated?
					String argN = cfg.featureExistsArg(funcName);

					if (!argN.isEmpty()) {
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"validating arg=["+argN+"] for BIF=["+funcName+"()]", u.debugPtree);
						argNum = Integer.parseInt(argN.substring(3));
						if (argNum <= argListText.size()) {
							String argStr = argListText.get(argNum-1);
							if (argStr.startsWith("N'")) argStr = argStr.substring(1);
							if (dateBIFs.contains(funcName)) {
								if (argNum == 1) {
									argStr = u.normalizeName(argStr);
								}
							}
							String argStrValidate = mapBIFArgStrValidate(funcName, nrArgs, argStr);
							String argStrReport = mapBIFArgStrReport(funcName, argStr, argStrValidate);
							statusArgN = status = featureArgSupportedInVersion(funcName, argN, argStrValidate);
							funcNameReport = funcName + "("+ argStrReport.toLowerCase()+")";

							if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"funcName=["+funcName+"] funcNameReport=["+funcNameReport+"] argStr=["+argStr+"] argStrValidate=["+argStrValidate+"] argStrReport=["+argStrReport+"] argN=["+argN+"] nrArgs=["+nrArgs+"] status=["+status+"] ", u.debugPtree);
						}
					}

					// check for cases where #arguments matters
					if (featureExists(funcName+withNArgumentValidateStr)) {
						String statusNrArg = featureSupportedInVersion(funcName+withNArgumentValidateStr, nrArgs.toString());
						if (!statusNrArg.equals(u.Supported)) {
							if (nrArgs == 0) funcNameReport += "," + withoutArgumentValidateStr;
							else {
								String N = withNArgumentValidateStr.replace(" N ", " " +nrArgs.toString()+" ");
								funcNameReport += "," + N;
							}
							if (status.equals(u.Supported)) {
								status = statusNrArg;
							}
						}
					}

					// check for numeric-as-date
					if (dateBIFs.contains(funcName)) {
						String statusNumDate = u.NotSupported;

						// if testing on the first arg for the DATExxx() BIFs, and the arg value is supported, don't report it (this is a bug, and should be temporary)
						if (argNum > 0) {
							if ((argNum == 1) && statusArgN.equals(u.Supported)) {
								funcNameReport = funcName + "()";
							}
						}

						// report the unit used
						String unit = argList.get(0).getText().toLowerCase();
						funcDetail = unit;

						for (int i = 2; i <= 3; i++) {
							if (funcName.equals("DATEADD")) {
								if (i == 2) continue;
							}
							if (funcName.equals("DATENAME") || funcName.equals("DATEPART")) {
								if (i == 3) continue;
							}

							// check for numeric-as-date
							if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"dateBIFs: funcName=["+funcName+"] i=["+i+"] argi=["+argList.get(i-1).getText()+"] argtype=["+expressionDataType(argList.get(i-1))+"] funcDetail=["+funcDetail+"] ", u.debugPtree);
							checkNumericAsDate("DATETIME", funcName, funcNameReport, argList.get(i-1), i, lineNr);
						}
					}
				}
				else {
					// missing entry in the .cfg file?
					assert false: CompassUtilities.thisProc()+"funcName=["+funcName+"] not found under ["+BuiltInFunctions+"] ";
				}

				if (inCompCol) {
					// this is a BIF call inside a computed column
					funcNameReport += ", in computed column"+inCompColType;
					String statusCC = u.Supported;
					if (featureExists(CompColFeatures, funcName)) {
						statusCC = featureSupportedInVersion(CompColFeatures, funcName);
					}
					// if BIF not supported in a computed column, but supported by itself, then mark as not supported
					if (!statusCC.equals(u.Supported)) {
						if (status.equals(u.Supported)) {
							status = statusCC;
						}
					}
				}

				boolean captured = false;
				if (funcName.equals("STRING_AGG")) {
					// the grammar finds this as a function call if invoked without the WITHIN GROUP clause, so correct that here
					groupCapture = AggregateFunctions;
					if (STRING_AGG_WITHIN_GROUP) { // true=WITHIN GROUP was specified
						funcNameReport = StringAggWithinGroup;
						String statusWG = featureSupportedInVersion(StringAggWithinGroup);
						if (!statusWG.equals(u.Supported)) {
							captureItem(StringAggWithinGroup, "", AggregateFunctions, funcName, statusWG, lineNr);
							captured = true;
						}
					}
				}

				if (funcName.equals("IDENTITY")) {
					assert(nrArgs > 0) : "#arguments for IDENTITY() must be > 0";  // IDENTITY must always have the datatype as arg
					String idType = u.normalizeName(argListText.get(0)).toUpperCase();
					funcNameReport = "IDENTITY("+idType+")";
					addStmtAttribute("IDENTITY");
					// the following tests only make sense if IDENTITY() is supported
					if (status.equals(u.Supported)) {
						if (selectListNrColumns > selectListColumnNr) {
							addStmtAttribute("IDENTITY_NOTLAST");
						}

						// Check for UDD for IDENTITY()
						String UDDtype = lookupUDD(idType);
						String statusIDtype = u.Supported;
						if (!UDDtype.isEmpty()) {
							if (featureExists(UDDForIdentity)) {
								statusIDtype = featureSupportedInVersion(UDDForIdentity, "IDENTITY()");
								if (!statusIDtype.equals(u.Supported)) {
									captureItem(UDDForIdentity + "() function, IDENTITY("+idType+")", idType, UDDatatypes, funcName, statusIDtype, lineNr);
								}
							}
						}
						else {
							if (idType.equals("TINYINT")) {
								if (featureExists(UDDForIdentity)) {
									statusIDtype = featureSupportedInVersion(UDDForIdentity, "IDENTITY(TINYINT)");
									if (!statusIDtype.equals(u.Supported)) {
										captureItem(TinyintForIdentity, idType, UDDatatypes, funcName, statusIDtype, lineNr);
									}
								}
							}
						}

						if (statusIDtype.equals(u.Supported)) {
							// raise warning about BIGINT datatype being used for NUMERIC/IDENTITY
							String idTypeFmt = idType;
							if (!UDDtype.isEmpty()) idTypeFmt = UDDtype;
							//u.appOutput(u.thisProc()+"idTypeFmt=["+idTypeFmt+"] ");
							if (idTypeFmt.startsWith("NUMERIC(") || idTypeFmt.startsWith("DECIMAL(") || idTypeFmt.startsWith("DEC(")) {
								String statusNumericBigint = featureSupportedInVersion("NUMERIC datatype for IDENTITY", "IDENTITY()");
								if (!statusNumericBigint.equals(u.Supported)) {
									captureItem("IDENTITY column created as BIGINT, declared as "+idTypeFmt, "", Datatypes, "", statusNumericBigint, lineNr);
								}
							}
						}

					}
				}

				if (funcName.equals("FORMAT")) {
					String fmtArg = argListText.get(1);
					if (fmtArg.startsWith("N'")) fmtArg = fmtArg.substring(1);
					if (fmtArg.startsWith("'") || (fmtArg.startsWith("\"") && !u.QuotedIdentifierFlag)) {
						// it's a string, keep it
					}
					else if (fmtArg.startsWith("@")) {
						fmtArg = "@var";
					}
					else {
						fmtArg = "expression"; // could be a column or function call
					}
					funcNameReport = "FORMAT("+fmtArg+ ")";

					if (argListText.size() == 3) {
						// report culture param if present
						String culture = argListText.get(2);
						String statusCulture = u.NotSupported;
						if (culture.charAt(0) == '@') {
							culture = "@var";
							statusCulture = u.ReviewManually;
						}
						else if (!u.isQuotedString(culture)) {
							culture = "expression";
							statusCulture = u.ReviewManually;
						}
						else {
							culture = u.stripStringQuotes(culture);
							statusCulture = featureSupportedInVersion(FormatCulture, culture);
							//format culture arg
							culture = culture.toLowerCase();
							int rix = culture.lastIndexOf("-");
							if (rix != -1) {
								culture = culture.substring(0,rix) + culture.substring(rix).toUpperCase();
							}
							culture = "'" + culture + "'";
						}
						String cultureReport = "FORMAT() culture "+culture;
						captureItem(cultureReport, funcDetail, BuiltInFunctions, funcName, statusCulture, lineNr);
					}
				}

				if (funcName.equals("OPENXML")) {
					if (nrArgs == 3) {
						// report flags param if present
						String flags = argList.get(2).getText();
						if (getNumericConstant(flags) != null) {
							funcNameReport = "OPENXML(flags="+flags+ ")";
						}
						else {
							funcNameReport = u.escapeHTMLChars("OPENXML(flags=<expression>)");
						}
					}
				}

				if (funcName.equals("CONTAINS")) {
					TSQLParser.ExpressionContext expr = argList.get(0);
					assert (expr != null) : "CONTAINS(): expr is null";

					if (isString(expressionDataType(expr))) {
						String exprStr = expr.getText();
						if (u.isQuotedString(exprStr)) {
							// NB: this is not really parsed, so there is a small chance of mixing up with actual searched-for text strings
							exprStr = u.stripStringQuotes(exprStr).trim().toUpperCase();
							if (!u.getPatternGroup(exprStr, "^([\\s\\w]+)(\\*)?$", 1).isEmpty()) {
								// If it's a single word, assume we're good in case CONTAINS() is not-not-supported
								if (!status.equals(u.NotSupported)) {
									status = u.Supported;
								}
							}
							else {
								String keywd1 = u.getPatternGroup(exprStr, "^(\\w+)\\b", 1);
								String keywd2 = "";
								if (keywd1.equals("FORMSOF") || keywd1.equals("ISABOUT") || keywd1.equals("NEAR")) {
									if (keywd1.equals("FORMSOF")) {
										keywd2 = u.getPatternGroup(exprStr, "\\(\\s*(\\w+)\\b", 1);
									}
									funcNameReport = funcName + "(" + keywd1;
									if (!keywd2.isEmpty()) funcNameReport += "(" + keywd2 + ")";
									funcNameReport += ")";
								}
								if (featureExists(FullTextContains, keywd1)) {
									if (!status.equals(u.NotSupported)) {
										String statusContains = featureSupportedInVersion(FullTextContains, keywd1);
										if (statusContains.equals(u.Supported)) {
											if (!keywd2.isEmpty() && featureExists(FullTextContains, keywd2)) {
												statusContains = featureSupportedInVersion(FullTextContains, keywd2);
											}
										}
										if (!statusContains.equals(u.Supported)) {
											status = statusContains;
										}
									}
								}
							}
						}
					}
					else {
						//u.appOutput(u.thisProc()+"expr is not string");
					}

					// clarify reported item in case of not beign able to evaluate second argument
					if (funcNameReport.equals("CONTAINS()") && !status.equals(u.Supported)) {
						funcNameReport = "CONTAINS(expression)";
					}
				}

				// some rewrites
				if (startLine != null) {
					if (rewriteDirectOrig.contains(funcName)) {
						if (!status.equals(u.Supported)) {
							if (u.rewrite) {
								String rewriteText = rewriteDirectReplace.get(rewriteDirectOrig.indexOf(funcName));
								if (addRewrite(BuiltInFunctions, funcName, u.rewriteTypeReplace, rewriteText, startLine, startCharPositionInLine, stopLine, stopCharPositionInLine, startIndex, stopIndex))
									status = u.Rewritten;
							}
							else {
								String groupReport = u.applyPatternFirst(BuiltInFunctions, "s$", "");
								addRewrite(groupReport + " " + funcNameReport);
							}
						}
					}
					else if (funcName.equals("EOMONTH") || funcName.equals("DATABASE_PRINCIPAL_ID")) {
						if (!status.equals(u.Supported)) {
							if (u.rewrite) {
								//String eomArg = argList.get(0).getText();
								//String rewriteText = rewriteEOMonth.replaceAll(rewriteTag1, u.escapeRegexChars(eomArg));
								//addRewrite(BuiltInFunctions, funcNameReport, u.rewriteTypeReplace, rewriteText, startLine, startCharPositionInLine, stopLine, stopCharPositionInLine, startIndex, stopIndex);

								String rewriteText = rewriteEOMonth;
								if (funcName.equals("DATABASE_PRINCIPAL_ID")) {
									rewriteText = rewriteDbPrincipalID;
								}
								if (addRewrite(BuiltInFunctions, funcNameReport, u.rewriteTypeExpr2, rewriteText, startLine, startCharPositionInLine, stopLine, stopCharPositionInLine, startIndex, stopIndex))
									status = u.Rewritten;
							}
							else {
								String groupReport = u.applyPatternFirst(BuiltInFunctions, "s$", "");
								addRewrite(groupReport + " " + funcNameReport);
							}
						}
					}
					else if (dateBIFs.contains(funcName) && (argNum > 0) && (!statusArgN.equals(u.Supported))) {
						TSQLParser.ExpressionContext arg1Raw = argList.get(0);
						if (u.rewrite) {
							// rewrite the unsupported unit arguments for DATEXXX()
							String arg1 = u.normalizeName(arg1Raw.getText());
							String mapStr = funcName + "(" + arg1 +")";
							if (rewriteDirectOrig.indexOf(mapStr.toUpperCase()) > -1) {
								String rewriteText = rewriteDirectReplace.get(rewriteDirectOrig.indexOf(mapStr.toUpperCase()));

								if (addRewrite(BuiltInFunctions, funcName+"("+arg1Raw.getText()+")", u.rewriteTypeReplace, rewriteText, arg1Raw.start.getLine(), arg1Raw.start.getCharPositionInLine(), arg1Raw.stop.getLine(), arg1Raw.stop.getCharPositionInLine(), arg1Raw.start.getStartIndex(), arg1Raw.stop.getStopIndex()))
									status = u.Rewritten;
							}
							else {
								// unit is not supported, but no rewrite is available
							}
						}
						else {
							addRewrite(funcName+"("+arg1Raw.getText()+")");
						}
					}
				}

				if (!captured) {
					captureItem(funcNameReport, funcDetail, groupCapture, funcName, status, lineNr);
				}
			}

			private String mapBIFArgStrValidate (String funcName, int nrArgs, String arg) {
				if (arg.equalsIgnoreCase(BIFArgStar)) return "STAR";  // must match .cfg file
				if (funcName.equalsIgnoreCase("CHECKSUM")) {
					if (nrArgs == 1) return BIFSingleArg; // must match .cfg file
					else return BIFMultipleArg; // must match .cfg file
				}
				else if (funcName.equalsIgnoreCase("TRIGGER_NESTLEVEL")) {
					if (nrArgs == 0) return BIFNoArg; // must match .cfg file
					else if (nrArgs == 1) return BIFSingleArg; // must match .cfg file
					else return BIFMultipleArg; // must match .cfg file
				}
				return arg;
			}

			private String mapBIFArgStrReport (String funcName, String arg, String argValidate) {
				if (arg.equalsIgnoreCase(BIFArgStar)) return "*";
				else if (argValidate.equalsIgnoreCase(BIFNoArg)) return "";
				else if (argValidate.equalsIgnoreCase(BIFSingleArg)) return "arg";
				else if (argValidate.equalsIgnoreCase(BIFMultipleArg)) return "arg, arg,...";
				if (u.isQuotedString(arg)) {
					arg = '\'' + u.stripStringQuotes(arg) + '\'';
				}
				return arg;
			}

			// check for numeric-as-date
			private void checkNumericAsDate(String dataType, String funcName, String funcNameReport, TSQLParser.ExpressionContext expr, int argNum, int lineNr) {
				// argNum indicates which argument it concerns
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"funcName=["+funcName+"] dataType=["+dataType+"] argNum=["+argNum+"] expr=["+expr.getText()+"]  start=["+expr.start.getLine()+"," +expr.start.getCharPositionInLine()+"] stop=["+expr.stop.getLine()+"," +expr.stop.getCharPositionInLine()+"] index=[" +expr.start.getStartIndex()+ ", " +expr.stop.getStopIndex()+"] ", u.debugPtree);
				String exprType = expressionDataType(expr);
				if (dataType.equals("DATETIME") || dataType.equals("SMALLDATETIME")) {
					// check for converting numeric expressions to [SMALL]DATETIME
					boolean isANumber = false;
					if (isNumeric(exprType)) {
						isANumber = true;
					}
					else if (isString(exprType)) {
						String n = u.stripStringQuotes(expr.getText());
						if (isNumeric(n)) {
							if (((n.length() == 4) || (n.length() == 6) || (n.length() == 8))  && (!n.contains("."))) {
								// when 4/6/8 long, can be a valid datetime string, e.g. '20080304'
							}
							else {
								isANumber = true;
							}
						}
					}

					if (isANumber) {
						String statusNumDate = featureSupportedInVersion(NumericAsDateTime, funcName);
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"funcName=["+funcName+"] expr=["+expr.getText()+"]  statusNumDate=["+statusNumDate+"]  ", u.debugPtree);
						// remove any arguments from the call for display
						funcNameReport = u.applyPatternFirst(funcNameReport, "^(.*\\().*?(\\).*)$", "$1$2");
						String funcNameReportNumDate = NumericAsDateTime + " in " + funcNameReport;

						if (!statusNumDate.equals(u.Supported)) {
							if (u.rewrite) {
								String rewriteText = rewriteNumericAsDate;
								if (!CompassUtilities.getPatternGroup(expr.getText(), "(^[\\+\\-]*[0]+(\\.[0]*)?)$", 1).isEmpty()) rewriteText = rewriteNumericAsDateZero;
								if (addRewrite(funcNameReportNumDate, expr.getText(), u.rewriteTypeExpr1, rewriteText, expr.start.getLine(), expr.start.getCharPositionInLine(), expr.stop.getLine(), expr.stop.getCharPositionInLine(),expr.start.getStartIndex(), expr.stop.getStopIndex()))
									statusNumDate = u.Rewritten;
							}
							else {
								addRewrite(funcNameReportNumDate);
							}
						}

						captureItem(funcNameReportNumDate, "argument #"+argNum, NumericAsDateTime, "", statusNumDate, lineNr);
					}
				}
				else if (isNumeric(dataType)) {
					// check for converting [SMALL]DATETIME to a numeric type
					// other date/time types are not valid in MSSQL to start with so no need to check for those
					if (isDateTime(exprType)) {
						String statusDateToNum = featureSupportedInVersion(DateTimeToNumeric, funcName);
						String funcNameReportDateNum = DateTimeToNumeric + " in " + funcNameReport;
						captureItem(funcNameReportDateNum, "", DateTimeToNumeric, "", statusDateToNum, lineNr);
					}
				}
				else {
					// Conversion to (VAR)BINARY
					if (isBinary(dataType)) {
						// check for converting datetime to a binary type
						if (isDateTime(exprType)) {  // T-SQL can convert any datetime type to binary, not just [SMALL]DATETIME
							String statusDateToBin = featureSupportedInVersion(DateTimeToBinary, funcName);
							String funcNameReportDateBin = DateTimeToBinary + " in " + funcNameReport;
							captureItem(funcNameReportDateBin, "", DateTimeToBinary, "", statusDateToBin, lineNr);
						}
					}
					else if (isBinary(exprType)) {
						if (isDateTime(dataType)) {
							String statusBinDate = featureSupportedInVersion(BinaryAsDateTime, funcName);
							if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"funcName=["+funcName+"] expr=["+expr.getText()+"]  statusBinDate=["+statusBinDate+"]  ", u.debugPtree);
							// remove any arguments from the call for display
							funcNameReport = u.applyPatternFirst(funcNameReport, "^(.*\\().*?(\\).*)$", "$1$2");
							String funcNameReportBinDate = BinaryAsDateTime + " in " + funcNameReport;
							captureItem(funcNameReportBinDate, "argument #"+argNum, BinaryAsDateTime, "", statusBinDate, lineNr);
						}
					}
				}
			}

			// check for numeric-as-date
			private void checkNumericDateVarAssign(String varName, TSQLParser.ExpressionContext expr, int lineNr) {
				varName = varName.toUpperCase();
				String dataType = varDataType(varName);
				checkNumericDateVarAssign(varName, dataType, expr, lineNr);
			}

			private void checkNumericDateVarAssign(String name, String dataType, TSQLParser.ExpressionContext expr, int lineNr) {
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"name=["+name+"] dataType=["+dataType+"] expr=["+expr.getText()+"] expressionDataType(expr)=["+expressionDataType(expr)+"] ", u.debugPtree);
				if (dataType.equals("DATETIME") || dataType.equals("SMALLDATETIME")) {
					if (isNumeric(expressionDataType(expr))) {
						String statusNumDate = featureSupportedInVersion(NumericDateTimeVarAssign);
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"name=["+name+"]  expr=["+expr.getText()+"]  statusNumDate=["+statusNumDate+"]  ", u.debugPtree);

						if (!statusNumDate.equals(u.Supported)) {
							if (u.rewrite) {
								String rewriteText = rewriteNumericAsDate;
								if (!CompassUtilities.getPatternGroup(expr.getText(), "(^[\\+\\-]*[0]+(\\.[0]*)?)$", 1).isEmpty()) rewriteText = rewriteNumericAsDateZero;
								if (addRewrite(NumericDateTimeVarAssign, expr.getText(), u.rewriteTypeExpr1, rewriteText, expr.start.getLine(), expr.start.getCharPositionInLine(), expr.stop.getLine(), expr.stop.getCharPositionInLine(), expr.start.getStartIndex(), expr.stop.getStopIndex()))
									statusNumDate = u.Rewritten;
							}
							else {
								addRewrite(NumericDateTimeVarAssign);
							}
						}

						captureItem(NumericDateTimeVarAssign, name, NumericAsDateTime, "", statusNumDate, lineNr);
					}
				}
			}


			// --- visit the tree nodes ----------------------------------------

			@Override public String visitUse_statement(TSQLParser.Use_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String DBName = u.normalizeName(ctx.dbname.getText());
				u.setCurrentDB(DBName);
				captureItem("USE " + DBName, DBName, DatabasesReportGroup, "", u.Supported, ctx.start.getLine());
				if (u.execTest) {
			        String use = "USE " + DBName + "\ngo\n";
			        try { u.writeExecTestFile(use); } catch (Exception e) { };
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitSelect_statement_standalone(TSQLParser.Select_statement_standaloneContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				inSelectStandalone = true;
				newSelectStmt("SELECT", ctx.start.getLine());
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"new SELECT in standalone", u.debugPtree);

				if (ctx.with_expression() != null) {
					addStmtAttribute("WITH");
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"added WITH...SELECT in standalone", u.debugPtree);
				}
				visitChildren(ctx);
				inSelectStandalone = false;
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitSelect_statement(TSQLParser.Select_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"SELECT=["+ctx.getText()+"]  ", u.debugPtree);
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"inSelectStandalone=["+inSelectStandalone+"]  inSubquery=["+inSubquery+"]  inDerivedTB=["+inDerivedTB+"] ", u.debugPtree);
				boolean createdNew = false;

				if (inSelectStandalone || inSubquery || inDerivedTB) {
					if (inSelectStandalone) inSelectStandalone = false;
					if (inSubquery) inSubquery = false;
					if (inDerivedTB) inDerivedTB = false;
				}
				else {
					createdNew = true;
					newSelectStmt("SELECT", ctx.start.getLine());
				}

				if (ctx.order_by_clause() != null) {
					addStmtAttribute("ORDERBY");
				}
				if (inCTESelectAttribute) {
					addStmtAttribute("CTE");
					inCTESelectAttribute = false;
				}

				if (hasParent(ctx.parent,"insert_statement")) {
					addStmtAttribute("INSERT");
				}
				else if (hasParent(ctx.parent,"create_or_alter_view")) {
					addStmtAttribute("VIEW");
				}
				else if (hasParent(ctx.parent,"func_body_returns_select")) {
					addStmtAttribute("RETURNS");
				}

				visitChildren(ctx);

				if (createdNew) {
					popSelectLevel();
				}
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitQuery_specification(TSQLParser.Query_specificationContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				// DEV: how many tables?
				// Try to exclude reporting single-table queries without an ORDER-BY that select on a PK or unique index
//				if ((ctx.table_sources() != null) && (ctx.where != null)) {
//					int nrTables = ctx.table_sources().table_source_item().size();
//					u.appOutput(u.thisProc()+"nrTables=["+nrTables+"] ");
//					if (nrTables == 1) {
//						// single-table
//						u.appOutput(u.thisProc()+"search_condition=["+ctx.where.getText()+"] OR=["+ctx.where.OR().size()+"] ");
//						// get search condition - cannot contain OR
//						if (ctx.where.OR().size() == 0) {
//							List<TSQLParser.Predicate_brContext> preds = ctx.where.predicate_br();
//							u.appOutput(u.thisProc()+"preds=["+preds.size()+"] ");
//							for (int i=0; i < preds.size(); i++) {
//								String s = u.stripEnclosingBrackets(getTextSpaced(preds.get(i)));
//								u.appOutput(u.thisProc()+"pred=["+s+"] NOT=["+preds.get(i).NOT().size()+"] ");
//								// must be: column EQUALS expression (or v.v. , but not testing for that one)
//								// predicate cannot include NOT!
//								if (preds.get(i).NOT().size() > 0) {
//									u.appOutput(u.thisProc()+"break on NOT");
//									break;
//								}
//								// get column name, check for EQUALS operators
//							}
//						}
//					}
//				}

				if (ctx.INTO() != null) {
					String intoTableNameRaw = ctx.into.getText();
					String intoTableName = u.normalizeName(intoTableNameRaw);
					CaptureIdentifier(intoTableNameRaw, intoTableName, "SELECT..INTO", ctx.start.getLine());
					String tabType = getTmpTableType(intoTableName, true);
					addStmtAttribute("INTO "+tabType);
					setStmtAttributeObjName(intoTableName);
				}

				if (ctx.top_clause() != null) {
					String topClauseText = ctx.top_clause().getText().toUpperCase();
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"topClauseText  =["+topClauseText+"]", u.debugPtree);

					topClauseText = topClauseText.substring(3);

					boolean hasPercent = false;
					if (topClauseText.contains("PERCENT")) {
						if (!CompassUtilities.getPatternGroup(topClauseText, "\\b(PERCENT)\\b", 1).isEmpty()) {
							topClauseText = u.applyPatternFirst(topClauseText, "\\bPERCENT\\b", "");
							hasPercent = true;
						}
						else if (!CompassUtilities.getPatternGroup(topClauseText, "\\d(PERCENT)\\b", 1).isEmpty()) {
							topClauseText = u.applyPatternFirst(topClauseText, "(\\d)(PERCENT)\\b", "$1");
							hasPercent = true;
						}
					}

					if (topClauseText.contains("(")) {
						topClauseText = u.applyPatternFirst(topClauseText, "^[\\(]+", "");
						topClauseText = u.applyPatternFirst(topClauseText, "[\\)]+$", "");
					}

					if (!(hasPercent && topClauseText.equals("100"))) {
						// TOP 100 PERCENT can be ignored for the sake of SELECT TOP without ORDER-BY
						addStmtAttribute("TOP");
					}

					String topClauseCopy = topClauseText;
					String topClauseTest = topClauseText;
					if (!CompassUtilities.getPatternGroup(topClauseText, "^(\\d+)$", 1).isEmpty()) {
						if (!topClauseText.equals("0") && !(topClauseText.equals("100")&&(hasPercent))) {
							topClauseTest = cfgNonZero;
							topClauseText = u.escapeHTMLChars("<number>");
						}
					}
					else if (!CompassUtilities.getPatternGroup(topClauseText, "^("+u.varPattern+")$", 1).isEmpty()) {
						topClauseTest = cfgVariable;
						topClauseText = "(@v)";
					}
					else {
						String topSubq = getTextSpaced(ctx.top_clause());
						if (topSubq.toUpperCase().contains(" SELECT ")) {
							topClauseText = u.escapeHTMLChars("(subquery)");
						}
						else {
							topClauseTest = cfgExpression;
							topClauseText = u.escapeHTMLChars("(expression)");
						}
					}

					if (hasPercent) {
						String status = featureSupportedInVersion(SelectTopPercent, topClauseTest);
						captureItem("SELECT TOP "+topClauseText+ " PERCENT", topClauseCopy, SelectTopPercent, topClauseTest, status, ctx.top_clause().start.getLine());
					}
					else {
						captureItem("SELECT TOP "+topClauseText, topClauseCopy, DMLReportGroup, "", u.Supported, ctx.top_clause().start.getLine());
					}

					if (ctx.top_clause().TIES() != null) {
						String status = featureSupportedInVersion(SelectTopWithTies);
						captureItem(SelectTopWithTies, topClauseCopy, SelectTopWithTies, "", status, ctx.top_clause().start.getLine());
					}

					// capture SELECT TOP inside a multi-stmt TUDF/TVF (but not in subquery): can be created but not called
					if (inMultiStmtTUDF) {
						if (!hasParent(ctx.parent,"subquery")) {
							String status = featureSupportedInVersion(SelectTopInTUDF);
							captureItem(SelectTopInTUDF, "", SelectTopInTUDF, "", status, ctx.top_clause().start.getLine());
						}
					}
				}

				if (ctx.groupByAll != null) {
					String status = featureSupportedInVersion(GroupByAll);
					captureItem(GroupByAll, ctx.getText(), GroupByAll, "", status, ctx.start.getLine());
				}

				CaptureXMLNameSpaces(ctx.parent, "SELECT", ctx.start.getLine());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitOrder_by_clause(TSQLParser.Order_by_clauseContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if (ctx.OFFSET() != null) {
					String fetch = "";
					if (ctx.FETCH() != null) {
						fetch = "..FETCH";
					}
					captureItem("SELECT..ORDER BY OFFSET"+fetch, "", DMLReportGroup, "", u.Supported, ctx.start.getLine());
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitQuery_expression(TSQLParser.Query_expressionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if ((ctx.order_by_qs != null) || (ctx.order_by_qe != null)) {
					addStmtAttribute("ORDERBY");
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitCommon_table_expression(TSQLParser.Common_table_expressionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				inCTE = true;
				inCTESelectAttribute = true;
				visitChildren(ctx);
				inCTE = false;
				inCTESelectAttribute = false;
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitSubquery(TSQLParser.SubqueryContext ctx) {
				boolean subqInExists = false;
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				inSubquery = true;

				newSelectStmt("SELECT", ctx.start.getLine());
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"added SELECT for subq", u.debugPtree);

				if (inExistsPredicate) {
					subqInExists = true;
					inExistsPredicate = false;
				}

				addStmtAttribute("SUBQUERY");
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"added SELECT SUBQUERY ", u.debugPtree);

				if (subqInExists) {
					addStmtAttribute("EXISTS");
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"added EXISTS", u.debugPtree);
				}

				if (hasParent(ctx.parent,"insert_statement")) {
					addStmtAttribute("INSERT");
				}
				else if (hasParent(ctx.parent,"create_or_alter_view")) {
					addStmtAttribute("VIEW");
				}
				else if (hasParent(ctx.parent,"func_body_returns_select")) {
					addStmtAttribute("RETURNS");
				}

				// Try to detect STUFF(SELECT FOR XML PATH) as workaround for STRING_AGG(), per old (pre-STRING_AGG()) SQL Server blogs
				if (ctx.select_statement().for_clause() != null) {
					if (ctx.select_statement().for_clause().XML() != null) {
						if (ctx.select_statement().for_clause().PATH() != null) {
//							u.appOutput(u.thisProc()+"parent 1=["+parentRuleName(ctx.parent,1)+"] ");
							if (parentRuleName(ctx.parent,3).equals("function_call") && parentRuleName(ctx.parent,4).equals("expression")) {
								TSQLParser.Function_callContext f = (TSQLParser.Function_callContext) ctx.getParent().getParent().getParent();
								String sf = f.getText().toUpperCase().replaceAll("N'", "'");
								int selectListElem = ctx.select_statement().query_expression().query_specification().select_list().select_list_elem().size();
								if (sf.startsWith("STUFF(") || sf.startsWith("SUBSTRING(")) {
									boolean isOK = false;
									boolean step1OK = false;

									// PATH() may not have any brackets
									sf = sf.replaceFirst("\\[VALUE\\]", "VALUE");
									// PATH() may not have any brackets
									sf = sf.replaceFirst("FORXMLPATH,TYPE\\)\\.VALUE", "FORXMLPATH(''),TYPE).VALUE");

									String chk1 = u.findClosingBracket(sf, sf.indexOf("(")+1);
									String chk1a = chk1.substring(chk1.indexOf("FORXMLPATH"));
									String chkRoot = CompassUtilities.getPatternGroup(chk1a, ",(ROOT)\\b(\\('.*?'\\))?", 1);
									String chkRootName = CompassUtilities.getPatternGroup(chk1a, ",(ROOT)\\b(\\('(.*?)'\\))?", 3);

									String chk2 = sf.substring(sf.indexOf("(")+1+chk1.length());

									if (!chkRoot.isEmpty()) {
										chk1 = chk1.replaceFirst(",ROOT\\('.*?'\\)", "");
										chk1 = chk1.replaceFirst(",ROOT", "");

										if ((chkRootName == null) || chkRootName.isEmpty()) {
											chkRoot = "";
										}
									}

									if (chk2.startsWith(",")) {
										// it's the BIF's arglist
										chk2 = "";
										step1OK = true;
									}
									else if (chk2.startsWith(".VALUE(")) {
										chk2 = ".VALUE" + u.findClosingBracket(chk2, chk2.indexOf("("));
										step1OK = true;
									}
									else {
										// don't know how to handle, will fall through below
									}

									String xType = "";
									if (step1OK) {
										if (chk1.endsWith("FORXMLPATH(''))") && chk2.isEmpty()) {
											isOK = true;
											//xType = "simple";
										}
										else if (chk1.endsWith("FORXMLPATH(''),TYPE)") && !chk2.isEmpty()) {
											if (!chkRoot.isEmpty()) {
												if (chkRootName.isEmpty()) chkRootName = "ROOT";
												chk2 = CompassUtilities.applyPatternFirst(chk2, "(\\.VALUE\\('(\\()?(\\.)?)(\\/)?" + chkRootName + "\\b", "$1");
												chk2 = chk2.replaceFirst(".VALUE\\('\\[1\\]'", ".VALUE('.[1]'");
											}
											String patt = "FORXMLPATH\\(''\\),TYPE\\)\\.VALUE\\('(\\.(\\[1\\])?|\\(\\.(\\[1\\])?\\)|\\(\\.\\)\\[1\\]?|(\\.\\/)?TEXT\\(\\)\\[1\\]|\\((\\.\\/)?TEXT\\(\\)\\)\\[1\\])','([^']*?)'\\)$";
											String charType = CompassUtilities.getPatternGroup(chk1+chk2, patt, 6);
											String xtp = CompassUtilities.getPatternGroup(chk1+chk2, patt, 1);
											if (!CompassUtilities.getPatternGroup(charType, "^((N)?(VAR)?CHAR(\\((\\d+|MAX)\\))?)$",1).isEmpty()) isOK = true;

											if (chk2.contains(".VALUE")) xType = ", with .VALUE()";
										}
									}
									
									// do not try to rewrite a case 'SELECT *', instead of 'SELECT column'
									if (selectListElem == 1) {
										String col = ctx.select_statement().query_expression().query_specification().select_list().select_list_elem().get(0).getText();
										if (col.startsWith("*")) {
											isOK = false;											
										}
									}
									
									//u.appOutput(u.thisProc()+"f ("+(u.lineNrInFile + ctx.start.getLine() - 1)+")=["+getTextSpaced(f)+"] selectListElem=["+selectListElem+"] ");
									if (isOK) {
										String xStatus = u.NotSupported;
										String tag = StringAggXMLPath + xType;

										stringAggWorkaround = true;
										String selStmt = getTextSpaced(ctx.select_statement());

										xStatus = featureSupportedInVersion(SyntaxIssues, StringAggXMLPath);

										if (selectListElem == 1) {
											if (!xStatus.equals(u.Supported)) {
												if (u.rewrite) {
													String rewriteText = "";
													Integer rwrID = rewriteXMLStringAgg(ctx, f);

													if (addRewrite(StringAggXMLPath, f.getText(), u.rewriteTypeBlockReplace, rewriteText, f.start.getLine(), f.start.getCharPositionInLine(), f.stop.getLine(), f.stop.getCharPositionInLine(), f.start.getStartIndex(), f.stop.getStopIndex(), rwrID))
														xStatus = u.Rewritten;
												}
												else {
													addRewrite(tag);
												}
											}

											captureItem(tag, "", AggregateFunctions, "", xStatus, ctx.start.getLine());

											if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
											return null;	// any other constructs in the subquery will not be reported -- can we fix that?
										}
										else {
											// more than 1 element in the SELECT list, don't know how to rewrite
											tag = StringAggXMLPathMultCols;
											String status = u.ReviewManually;
											captureItem(tag, "", AggregateFunctions, "", status, ctx.start.getLine());
										}
									}
									else {
										//String s = sf.substring(sf.lastIndexOf("FORXMLPATH"));
										//u.appOutput(u.thisProc()+"NOT ENDING expected: line=["+(u.lineNrInFile + ctx.start.getLine() - 1)+"] s=["+s+"] ");
									}
								}
							}
						}
					}
				}
				stringAggWorkaround = false;
				//-------------------------------------------------------------------------------------

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			public Integer rewriteXMLStringAgg(TSQLParser.SubqueryContext ctx, TSQLParser.Function_callContext f) {
				Map<String, List<Integer>> positions = new HashMap<>();
				int rwrID = u.rewriteTextListKeys.size() + 1;

				positions.put("select", Arrays.asList(ctx.select_statement().start.getStartIndex(), ctx.select_statement().stop.getStopIndex()));
				positions.put("bif", Arrays.asList(f.start.getStartIndex(), f.stop.getStopIndex()));
				positions.put("bifname", Arrays.asList(f.start.getStartIndex(), f.start.getStartIndex() + f.func_proc_name_server_database_schema().getText().length()));
				positions.put("bifcol", Arrays.asList(f.start.getCharPositionInLine(), -1));
				positions.put("bifargstart", Arrays.asList(f.function_arg_list().COMMA().get(0).getSymbol().getStartIndex(), -1));
				positions.put("forxml", Arrays.asList(ctx.select_statement().for_clause().start.getStartIndex(), -1));
				positions.put("selectlist", Arrays.asList(ctx.select_statement().query_expression().query_specification().select_list().start.getStartIndex(), ctx.select_statement().query_expression().query_specification().select_list().stop.getStopIndex()));

				if (ctx.select_statement().query_expression().query_specification().top_clause() != null) {
					positions.put("hasTop", Arrays.asList(-1,-1));
				}

				// detect column alias
				int nrSelectListElem = ctx.select_statement().query_expression().query_specification().select_list().select_list_elem().size();
				if (ctx.select_statement().query_expression().query_specification().select_list().select_list_elem().get(0).expression_elem().as_column_alias() != null) {
					int aliasStart = ctx.select_statement().query_expression().query_specification().select_list().select_list_elem().get(0).expression_elem().as_column_alias().column_alias().start.getStartIndex();
					String alias = ctx.select_statement().query_expression().query_specification().select_list().select_list_elem().get(0).expression_elem().as_column_alias().column_alias().getText();
					positions.put("alias", Arrays.asList(aliasStart, aliasStart+alias.length()-1));
				}

				u.rewriteIDDetails.put(rwrID, positions);

				return rwrID;
			}

			@Override public String visitDerived_table(TSQLParser.Derived_tableContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if (ctx.select_statement() != null) {
					inDerivedTB = true;

					newSelectStmt("SELECT", ctx.start.getLine());
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"added SELECT for derived_tb", u.debugPtree);

					if (!hasParent(ctx.parent,"insert_statement"))  {
						addStmtAttribute("SUBQUERY");
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"added SELECT SUBQUERY for derived_tb", u.debugPtree);
					}

					if (hasParent(ctx.parent,"insert_statement")) {
						addStmtAttribute("INSERT");
					}
					else if (hasParent(ctx.parent,"create_or_alter_view")) {
						addStmtAttribute("VIEW");
					}
					else if (hasParent(ctx.parent,"func_body_returns_select")) {
						addStmtAttribute("RETURNS");
					}
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitFor_clause(TSQLParser.For_clauseContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if (ctx.XML() != null) {
					String forXMLType = "";
					if (ctx.AUTO() != null) forXMLType = "AUTO";
					if (ctx.EXPLICIT() != null) forXMLType = "EXPLICIT";
					if (ctx.PATH() != null) forXMLType = "PATH";
					if (ctx.RAW() != null) forXMLType = "RAW";
					forXMLType = "SELECT FOR XML "+forXMLType;
					if (ctx.ELEMENTS().size() > 0) forXMLType += " ELEMENTS";
					captureXMLFeature("", forXMLType, "", ctx.start.getLine());
				}
				if (ctx.JSON() != null) {
					String forJSONType = "";
					if (ctx.AUTO() != null) forJSONType = "AUTO";
					if (ctx.PATH() != null) forJSONType = "PATH";

					List<String> opts = new ArrayList<>();
					opts.add(forJSONType);
					if (ctx.ROOT().size() > 0) opts.add("ROOT");
					if (ctx.INCLUDE_NULL_VALUES().size() > 0) opts.add("INCLUDE_NULL_VALUES");
					if (ctx.WITHOUT_ARRAY_WRAPPER().size() > 0) opts.add("WITHOUT_ARRAY_WRAPPER");

					String status = u.Supported;
					String forJSONreport = "";
					String forJSONreportErr = forJSONType + ",";
					for (String opt : opts) {
						opt = opt.trim();
						String forJSONtest = "SELECT FOR JSON "+forJSONType + " " + opt;

						// for some ad-hoc diagnostics
//						u.appOutput(u.thisProc()+"SELECT FOR JSON: ["+getTextSpaced(ctx)+"] ");
//						ParserRuleContext parentRule = ctx.getParent();
//						if (parentRule instanceof TSQLParser.Select_statementContext) {
//							u.appOutput(u.thisProc()+"SELECT FOR JSON: ["+getTextSpaced(parentRule)+"] ");
//						}
//						else{
//							u.appOutput(u.thisProc()+"not SELECT_STATEMENT with FOR JSON");
//						}

						forJSONtest = forJSONtest.replaceAll(forJSONType + " " + forJSONType, forJSONType);
						forJSONtest = forJSONtest.trim();
						forJSONreport += opt + ",";
						String status2 = featureSupportedInVersion(JSONFeatures, forJSONtest);
						if (!status2.equals(u.Supported)) {
							forJSONreportErr += opt + ",";
							status = u.NotSupported;
						}
					}
					forJSONreport = u.applyPatternFirst(forJSONreport, ",$", "");
					forJSONreportErr = u.applyPatternFirst(forJSONreportErr, ",$", "");
					if (!status.equals(u.Supported)) {
						if (!forJSONreport.equals(forJSONreportErr)) {
							forJSONreport = forJSONreportErr;
						}
					}
					forJSONreport = u.applyPatternFirst(forJSONreport, "AUTO,AUTO", "AUTO");
					captureItem("SELECT FOR JSON " +forJSONreport, "", JSONFeatures, forJSONreport, status, ctx.start.getLine());
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitLocal_id_expr(TSQLParser.Local_id_exprContext ctx) {
				if (ctx.LOCAL_ID() != null) {
					captureAtAtVariables(ctx.LOCAL_ID().getText().toUpperCase(), ctx.start.getLine());
				}
				visitChildren(ctx);
				return null;
			}

			@Override public String visitExecute_statement_arg_named(TSQLParser.Execute_statement_arg_namedContext ctx) {
				if (u.debugging) u.dbgOutput(u.thisProc()+"ctx=["+getTextSpaced(ctx)+"] ", u.debugPtree);
				if (ctx.LOCAL_ID() != null) {
					captureAtAtVariables(ctx.LOCAL_ID().getText().toUpperCase(), ctx.start.getLine(), "named_arg");
				}
				visitChildren(ctx);
				return null;
			}

			@Override public String visitExecute_parameter(TSQLParser.Execute_parameterContext ctx) {
				if (u.debugging) u.dbgOutput(u.thisProc()+"ctx=["+getTextSpaced(ctx)+"] ", u.debugPtree);
				execute_statement_argParamCount++;

				if (ctx.LOCAL_ID() != null) {
					captureAtAtVariables(ctx.LOCAL_ID().getText().toUpperCase(), ctx.start.getLine());
				}

				if (ctx.DEFAULT() != null) {
					// parameter-by-name?
					String parName = "";
					ParserRuleContext parentRule = ctx.getParent();
					if (parentRule instanceof TSQLParser.Execute_statement_arg_namedContext) {
						TSQLParser.Execute_statement_arg_namedContext parentCtx = (TSQLParser.Execute_statement_arg_namedContext)parentRule;
						parName = parentCtx.LOCAL_ID().getText().toUpperCase();
					}

					String statusDft = featureSupportedInVersion(ParamValueDEFAULT, "procedure");
					String itemTxt = ParamValueDEFAULT+", procedure call";
					if (!execute_statement_procName.startsWith("@")) {
						if (!statusDft.equals(u.Supported)) {
							// look up param default value: proc calls only use either by-position arguments, or by-name
							int parNo = execute_statement_argParamCount;
							String procName = execute_statement_procName;
							String parDft = "";
							if (!parName.isEmpty()) {
								parDft = lookupParDft(procName, parName);
							}
							else {
								parDft = lookupParDft(procName, parNo);
							}
							if (!parDft.isEmpty()) {
								if (u.rewrite) {
									String rewriteText = parDft;
									//u.appOutput(u.thisProc()+"param DEFAULT in procedure call procName=["+procName+"] parNo=["+parNo+"] parDft=["+parDft+"]");
									if (addRewrite(itemTxt, procName, u.rewriteTypeReplace, rewriteText, ctx.start.getLine(), ctx.start.getCharPositionInLine(), ctx.stop.getLine(), ctx.stop.getCharPositionInLine(), ctx.start.getStartIndex(), ctx.stop.getStopIndex()))
										statusDft = u.Rewritten;
								}
								else {
									addRewrite(itemTxt);
								}
							}
							else {
								// couldn't find the default, so don't attempt to rewrite
							}
						}
					}
					captureItem(itemTxt, "", ParamValueDEFAULT, "procedure", statusDft, ctx.start.getLine());
				}

				if (ctx.id() != null) {
					String s = ctx.id().getText();
					if (s.charAt(0) == '"') {
						captureDoubleQuotedString(s, ctx.id().start.getLine(), ctx.id().start.getCharPositionInLine(), ctx.id().stop.getLine(), ctx.id().stop.getCharPositionInLine(), ctx.id().start.getStartIndex(), ctx.id().stop.getStopIndex());
					}
					else {
						captureUnquotedString(s, "parameter value", ctx.id().start.getLine(), ctx.id().start.getCharPositionInLine(), ctx.id().stop.getLine(), ctx.id().stop.getCharPositionInLine(), ctx.id().start.getStartIndex(), ctx.id().stop.getStopIndex());
					}
				}

				visitChildren(ctx);
				return null;
			}

			private void captureUnquotedString(String s, String type, int startLine, int startPos, int stopLine, int stopPos, int startIndex, int stopIndex) {
				// check for unquoted char string: quotes are optional in some cases
				if ((s.charAt(0) != '"') && (s.charAt(0) != '\'')) {
					if (!s.startsWith("N'")) {
						if (Arrays.asList("CURRENT_TIMESTAMP", "CURRENT_USER", "SESSION_USER", "SYSTEM_USER", "USER").contains(s.toUpperCase())) {
							// skip BIFs
						}
						else if (!s.equalsIgnoreCase("NULL")) {
							if (!CompassUtilities.getPatternGroup(s, "^([A-Z\\#][\\w\\#\\$\\@]*)$", 1).isEmpty()) {

								String item = UnQuotedString +" as " + type;
								String status = featureSupportedInVersion(UnQuotedString, type);

								if (!status.equals(u.Supported)) {
									if (u.rewrite) {
										String rewriteText = "'" + s + "'";
										if (addRewrite(item, UnQuotedString, u.rewriteTypeReplace, rewriteText, startLine, startPos, stopLine, stopPos, startIndex, stopIndex))
											status = u.Rewritten;
									}
									else {
										addRewrite(item);
									}
								}

								captureItem(item, s, UnQuotedString, "", status, startLine);
							}
						}
					}
				}
			}

			private void captureAtAtErrorValue(Integer exprInt, String via, String op, int lineNr) {
				String status = featureSupportedInVersion(AtAtErrorValueRef, exprInt.toString());
				String usrDefined = "";
				if (exprInt >= 50000) {
					usrDefined = " (user-defined)";
					status = u.Supported;
				}
				captureItem(AtAtErrorValueRef+ " " +String.format("%6d",exprInt)+usrDefined+via, via, AtAtErrorValueRef, exprInt.toString(), status, lineNr);
			}

			private void captureAtAtErrorValueRef (String varName, TSQLParser.PredicateContext ctx) {
				// capturing @@ERROR = 999, ERROR_NUMBER() = 999 , as well as via variable assignment
				// not captured: more than 1 variable assignment level; 999 = @@ERROR; assignment or comparison through CASE expressions; column = @@ERROR/ERROR_NUMBER()
				// Use sp_mapped_system_error_list() to get a list of currently supported error values
				String via = "";
				String via2 = "";
				if (varName.equalsIgnoreCase("@@ERROR"))
					inAtAtErrorPredicate = true;
				else {
					if (!ctx.getText().toUpperCase().contains(varName.toUpperCase())) {
						return;
					}

					via2 = "via "+ varName.toUpperCase();
					if (varName.startsWith("RAISERROR")) {
						via = ", via "+ varName.toUpperCase();
					}
				}

				List<TSQLParser.ExpressionContext> exprList = ctx.expression();
				List<String> valueList = new ArrayList<>();

				String expr1 = cleanupTermErrorCode(exprList.get(0).getText());
				String expr2 = "";
				if (exprList.size() > 1) expr2 = cleanupTermErrorCode(exprList.get(1).getText());
				String op = "";

				if (ctx.comparison_operator() != null) {
					op = ctx.comparison_operator().getText();
					String expr = "";
					if (expr1.equalsIgnoreCase(varName)) {
						valueList.add(expr2);
					}
					else if (expr2.equalsIgnoreCase(varName)) {
						valueList.add(expr1);
					}
				}
				else if ((ctx.IN() != null) && (ctx.subquery() == null)) {
					op = "IN";
					valueList = new ArrayList<String>(Arrays.asList(ctx.expression_list().getText().split(",")));
					for (int i=0; i < valueList.size(); i++) valueList.set(i, cleanupTermErrorCode(valueList.get(i)));
				}
				else if (ctx.BETWEEN() != null) {
					op = "BETWEEN";
					valueList.add(expr2);
					String expr3 = cleanupTermErrorCode(exprList.get(2).getText());
					valueList.add(expr3);
				}
				if (!op.isEmpty() && (valueList.size() > 0)) {
					for (String v : valueList) {
						if (v.isEmpty()) continue;
						Integer exprInt	= getIntegerConstant(v, true);
						if (exprInt != null) {
							captureAtAtErrorValue(exprInt, via, op, ctx.start.getLine());
						}
						else {
							// cannot determine the value being compared against
							captureItem(AtAtErrorValueRef+ ", comparison with identifier/expression"+via, op+ " " + v + via2, AtAtErrorValueRef, "", u.ReviewManually, ctx.start.getLine());
						}
					}
				}
				else if (expr1.equalsIgnoreCase("@@ERROR")) {
					if (ctx.subquery() != null) {
						// cannot automate this
						String status = featureSupportedInVersion(AtAtErrorValueRef, "-999");  // status is not actually used, but will allow effort estimate to be found
						captureItem(AtAtErrorValueRef+ ", comparison with subquery"+via, via2, AtAtErrorValueRef, "", u.ReviewManually, ctx.start.getLine());
					}
				}
				else {
					// cannot figure it out
					String status = featureSupportedInVersion(AtAtErrorValueRef, "-999");  // status is not actually used, but will allow effort estimate to be found
					captureItem(AtAtErrorValueRef+ ", referenced value unclear"+via, via2, AtAtErrorValueRef, "", u.ReviewManually, ctx.start.getLine());
				}
			}

			@Override public String visitPredicate(TSQLParser.PredicateContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if (ctx.EXISTS() != null) {
					inExistsPredicate = true;
				}

				// temporary one-off: find integer variables being compared to empty string
//				if (ctx.expression().size() > 0) {
//					String varName = ctx.expression().get(0).getText().toUpperCase();
//					if (varName.charAt(0) == '@') {
//						String varType = localVars.get(varName.toUpperCase());
//						//u.appOutput(CompassUtilities.thisProc()+"varName=["+varName+"] varType=["+varType+"] txt=["+ctx.getText()+"]");
//						if (varType != null) {
//							if (varType.contains("INT")) {
//								if (ctx.expression().size() > 1) {
//									String expr = ctx.expression().get(1).getText();
//									//u.appOutput(CompassUtilities.thisProc()+"expr=["+expr+"] ");
//									if (expr.equals("''") || expr.equals("' '")) {
//										u.appOutput(CompassUtilities.thisProc()+"Found: varName=["+varName+"] varType=["+varType+"] op expr=["+expr+"] ");
//									}
//								}
//							}
//						}
//					}
//				}

				// find cases like =@@var, <@@var, >@@var , i.e. without spacing
				if (ctx.expression().size() > 1) { // this is the case 'expression operator expression'
					if (ctx.comparison_operator() != null) {
						String op = ctx.comparison_operator().getText();
						//u.appOutput(u.thisProc()+"ctx=["+getTextSpaced(ctx)+"] op=["+op+"] ctx.expression().size()=["+ctx.expression().size()+"]  ctx.expression(0)=["+ ctx.expression().get(0).getText()+"]  ctx.expression(1)=["+ ctx.expression().get(1).getText()+"] ");
						if (op.endsWith("=") || op.endsWith("<")  || op.endsWith(">")) {
							String varName = ctx.expression().get(1).getText().toUpperCase();
							if (varName.startsWith("@@")) {
								// is there spacing?
								int ixOpEnd = ctx.comparison_operator().stop.getStopIndex();
								int ixVarStart = ctx.expression().get(1).start.getStartIndex();
								if (ixVarStart == (ixOpEnd + 1)) {
									// no spacing between operator and right-hand @@var, determine if this is supported
									String status = featureSupportedInVersion(PGOpWhitespace, op+"@@");
									if (!status.equals(u.Supported)) {
										String item = PGOpWhitespaceFmt+"("+op+"@@var)";
										if (u.rewrite) {
											String rewriteText = op + " "; // insert a space before "@@"

											int line = ctx.expression().get(1).start.getLine();
											int startPos = ctx.expression().get(1).start.getCharPositionInLine() - op.length();
											int endPos = startPos + op.length() - 1;
											int startIx = ctx.expression().get(1).start.getStartIndex();
											int endIx = startIx + op.length() - 1;

											//u.appOutput(u.thisProc()+"ctx.expression().get(1).start.getCharPositionInLine()=["+ctx.expression().get(1).start.getCharPositionInLine()+"] startPos=["+startPos+"] endPos=["+endPos+"] op.length()=["+op.length()+"] ");

											if (addRewrite(item, op, u.rewriteTypeReplace, rewriteText, line, startPos, line, endPos, startIx, endIx))
												status = u.Rewritten;
										}
										else {
											addRewrite(item);
										}
										captureItem(item, op+"@@", OperatorsReportGroup, "", status, ctx.start.getLine());
									}
								}
							}
						}
					}
				}

				if (ctx.expression().size() > 0) { // this is the case 'expression operator expression'
					// find @@ERROR value references
					if ((ctx.comparison_operator() != null) || (ctx.IN() != null) || (ctx.BETWEEN() != null)) {
						if (ctx.getText().toUpperCase().contains("@@ERROR")) {
							captureAtAtErrorValueRef("@@ERROR", ctx);
						}
						if (ctx.getText().toUpperCase().contains("ERROR_NUMBER()")) {
							captureAtAtErrorValueRef("ERROR_NUMBER()", ctx);
						}
						for (String v : localAtAtErrorVars.keySet()) {
							captureAtAtErrorValueRef(v, ctx);
						}
					}

					if (ctx.comparison_operator() != null) {
						String opOJ = "";
						if ((ctx.comparison_operator().MULT_ASSIGN() != null) || (ctx.comparison_operator().EQUAL_STAR_OJ() != null)) opOJ = ctx.comparison_operator().getText();
						String typeOJ = "Right OJ";
						if (opOJ.equals("*=")) typeOJ = "Left OJ";
						if (!opOJ.isEmpty()) {
							String statusOJ = featureSupportedInVersion(TSQLOJ, typeOJ);
							captureItem("T-SQL " + typeOJ.replaceAll("OJ", "Outer Join") + " " + opOJ, "", TSQLOJ, "", statusOJ, ctx.comparison_operator().start.getLine());
						}
					}

					if (ctx.LIKE() != null) {
						boolean captured = false;
						String escapeClause = "";
						String statusODBCEscape = u.Supported;
						String statusLike = u.Supported;
						String status = statusLike;
						String itemReport = "LIKE operator";
						if (ctx.like_escape_clause() != null) {
							escapeClause = ", with ESCAPE";
							if (ctx.like_escape_clause().L_CURLY() != null) {
								escapeClause = ", with ODBC {ESCAPE}";
								statusODBCEscape = featureSupportedInVersion(ODBCEscape);
								//u.appOutput(u.thisProc()+"statusODBCEscape=["+statusODBCEscape+"] ");
							}
						}
						String patt = ctx.expression().get(1).getText();
						if ((patt.contains("[")) && (patt.contains("]"))) {   // quick first test
							//u.appOutput(u.thisProc()+"LIKE found: patt=["+patt+"] ");

							// the expression can be something like these: f([identifier]) or f('abc') --> so we need to remove the functions
							// we may not be correctly capturing cases as LIKE CASE WHEN... END where some square brackets shows up in a condition in the CASE. Guessing that's going to be rare.
							patt = u.extractStringLiteral(patt);
							//u.appOutput(u.thisProc()+"stripped: patt=["+patt+"] ");

							if ((patt.contains("[")) && (patt.contains("]"))) { // test again
								String esc = "";
								if (ctx.like_escape_clause() != null) {
									esc = ctx.like_escape_clause().expression().getText();
									//u.appOutput(u.thisProc()+"esc=["+esc+"] ");
									esc = u.stripStringQuotes(esc);
									// see if we are escaping square brackets: if so, don't report those
									patt = patt.replaceAll( u.escapeRegexChars(esc)+"\\[" , "");
									patt = patt.replaceAll( u.escapeRegexChars(esc)+"\\]" , "");
									//u.appOutput(u.thisProc()+"after esc: patt=["+patt+"] ");
								}

								if ((patt.contains("[")) && (patt.contains("]"))) {
									//u.appOutput(u.thisProc()+"LIKE [] found: x=["+ctx.expression().get(1).getText()+"] ");
									statusLike = featureSupportedInVersion(LikeSquareBracketsCfg);
									status = statusLike;
									if (!statusODBCEscape.equals(u.Supported)) {
										if (!statusLike.equals(u.NotSupported)) {
											status = statusODBCEscape;
										}
										if (!statusLike.equals(u.Supported)) {
											escapeClause = ""; // don't report this if LIKE [...] is not supported itself
										}
									}

									itemReport = LikeSquareBrackets+escapeClause;
									captured = true;
								}
							}
						}
						else {
							// pattern may be in a variable, no action currently
						}

						if (!captured) {
							// report supported LIKE operator but take ESCAPE into account
							statusLike = u.Supported;
							if (!statusODBCEscape.equals(u.Supported)) status = statusODBCEscape;
							itemReport = "LIKE operator"+escapeClause;
						}

						//u.appOutput(u.thisProc()+"status=["+status+"]  statusLike=["+statusLike+"] statusODBCEscape=["+statusODBCEscape+"] ");
						if ((ctx.like_escape_clause() != null) && (statusLike.equals(u.Supported)) && (statusODBCEscape.equals(u.NotSupported))) {
							if (u.rewrite) {
								String rewriteText = " " + ctx.like_escape_clause().ESCAPE().getText() + " " + ctx.like_escape_clause().expression().getText() + " ";
								String origText = ctx.like_escape_clause().getText();
								if (addRewrite(ODBCEscape, origText, u.rewriteTypeReplace, rewriteText, ctx.like_escape_clause().L_CURLY().getSymbol().getLine(), ctx.like_escape_clause().L_CURLY().getSymbol().getCharPositionInLine(), ctx.like_escape_clause().R_CURLY().getSymbol().getLine(), ctx.like_escape_clause().R_CURLY().getSymbol().getCharPositionInLine(), ctx.like_escape_clause().L_CURLY().getSymbol().getStartIndex(), ctx.like_escape_clause().R_CURLY().getSymbol().getStopIndex()))
									status = u.Rewritten;
							}
							else {
								addRewrite(ODBCEscape+", with LIKE operator");
							}
						}
						captureItem(itemReport, "",  OperatorsReportGroup, "", status, ctx.LIKE().getSymbol().getLine());
					}
				}
				visitChildren(ctx);
				inAtAtErrorPredicate = false;
				inExistsPredicate = false;
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitSql_union(TSQLParser.Sql_unionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String unionKwd = ctx.union_keyword().getText().toUpperCase();
				unionKwd = unionKwd.replace("ALL", " ALL");
				captureItem(unionKwd, "", DMLReportGroup, "", u.Supported, ctx.start.getLine());

				newSelectStmt("SELECT", ctx.start.getLine());
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"new SELECT for UNION", u.debugPtree);

				if ((ctx.order_by_qs != null) || (ctx.order_by_qe != null)) {
					addStmtAttribute("ORDERBY");
				}

				if (hasParent(ctx.parent,"insert_statement")) {
					addStmtAttribute("INSERT");
				}
				else if (hasParent(ctx.parent,"create_or_alter_view")) {
					addStmtAttribute("VIEW");
				}
				else if (hasParent(ctx.parent,"func_body_returns_select")) {
					addStmtAttribute("RETURNS");
				}

				visitChildren(ctx);
				popSelectLevel();
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitCreate_type(TSQLParser.Create_typeContext ctx) {
				// this is duplicated in pass 1
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String UDDname = u.normalizeName(ctx.simple_name().getText());
				String UDDdatatype = "";
				String section = UDDatatypes;
				String statusDataType = u.Supported;
				if (ctx.external_type() != null) {
					// EXTERNAL NAME
					statusDataType = featureSupportedInVersion(Datatypes, "EXTERNAL NAME");
					UDDdatatype = "EXTERNAL NAME";
				}
				else if (ctx.FROM() != null) {
					// scalar UDD
					UDDdatatype = u.normalizeName(ctx.data_type().getText().toUpperCase(), "datatype");
					if (featureExists(Datatypes, getBaseDataType(UDDdatatype))) {
						statusDataType = featureSupportedInVersion(Datatypes, getBaseDataType(UDDdatatype));
					}
					else {
						// datatype is not listed, means: supported
					}
					UDDdatatype = "scalar, " + UDDdatatype;
				}
				else {
					// table type
					UDDdatatype = "table";
					section = TableVariablesType;

					// set context, as a table
					u.setContext("TABLE", UDDname);
				}
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"UDD "+ ctx.getText()+", UDDname=["+UDDname+"] UDDdatatype=["+UDDdatatype+"] ", u.debugPtree);
				captureItem("CREATE TYPE, "+UDDdatatype, UDDname, section, "", statusDataType, ctx.start.getLine(), 0);

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitDrop_index(TSQLParser.Drop_indexContext ctx) {
				if (ctx.drop_relational_or_xml_or_spatial_index().size() > 0) {
					if (ctx.drop_relational_or_xml_or_spatial_index().size() > 1) {
						List<String> nameList = new ArrayList<>();
						for (int i = 0; i<ctx.drop_relational_or_xml_or_spatial_index().size(); i++) nameList.add(u.normalizeName(ctx.drop_relational_or_xml_or_spatial_index().get(i).id().getText()) + " ON " + u.normalizeName(ctx.drop_relational_or_xml_or_spatial_index().get(i).full_object_name().getText()));
						captureDropObject("INDEX", ctx.drop_relational_or_xml_or_spatial_index().size(), nameList, ctx.if_exists(), "", ctx.start.getLine());
					}
					for (TSQLParser.Drop_relational_or_xml_or_spatial_indexContext ix : ctx.drop_relational_or_xml_or_spatial_index()) {
						String objName = u.normalizeName(ix.full_object_name().getText());
						String ixName = u.normalizeName(ix.index_name.getText());
						String schema = "";
						if (objName.indexOf(".") != -1) schema = "schema.";
						CaptureIdentifier(objName, objName, DropIndex, ctx.start.getLine());

						String chk = "index ON "+schema+"table";
						String status = featureSupportedInVersion(DropIndex, chk);
						captureItem(DropIndex + " " + chk, ixName + " ON " +objName, DropIndex, "", status, ix.start.getLine());
					}
					captureIndexOptions("", "", DropIndex, ctx.with_index_options());
				}
				else if (ctx.drop_backward_compatible_index().size() > 0) {
					if (ctx.drop_backward_compatible_index().size() > 1) {
						List<String> nameList = new ArrayList<>();
						for (int i = 0; i<ctx.drop_backward_compatible_index().size(); i++) nameList.add(u.normalizeName(ctx.drop_backward_compatible_index().get(i).getText()));
						captureDropObject("INDEX", ctx.drop_backward_compatible_index().size(), nameList, ctx.if_exists(), "", ctx.start.getLine());
					}
					for (TSQLParser.Drop_backward_compatible_indexContext ix : ctx.drop_backward_compatible_index()) {
						String objName = u.normalizeName(ix.table_or_view_name.getText());
						String ixName = u.normalizeName(ix.index_name.getText());
						String chk = "table.index";
						if (ix.owner_name != null) {
							objName = u.normalizeName(ix.owner_name.getText()) + "." + objName;
							chk = "schema.table.index";
						}
						CaptureIdentifier(objName, objName, DropIndex, ctx.start.getLine());

						String status = featureSupportedInVersion(DropIndex, chk);
						String s = DropIndex + " " + chk;
						if (!status.equals(u.Supported)) {
							if (chk.equals("table.index")) {
								if (u.rewrite) {
									String rewriteText = ixName + " ON " + objName;
									if (addRewrite(DDLReportGroup, ix.getText(), u.rewriteTypeReplace, rewriteText, ix.start.getLine(), ix.start.getCharPositionInLine(), ix.start.getLine(), ctx.start.getCharPositionInLine(), ix.start.getStartIndex(), ix.stop.getStopIndex()))
										status = u.Rewritten;
									captureItem(s, "", DropIndex, "", status, ix.start.getLine());
								}
								else {
									addRewrite(s);
								}
							}
						}
						captureItem(s, ixName + " ON " +objName, DropIndex, "", status, ix.start.getLine());
					}
					captureIndexOptions("", "", DropIndex, ctx.with_index_options());
				}

				visitChildren(ctx); return null;
			}

			@Override public String visitDrop_table(TSQLParser.Drop_tableContext ctx) {
				List<String> nameList = new ArrayList<>();
				for (int i = 0; i<ctx.table_name().size(); i++) nameList.add(u.normalizeName(ctx.table_name().get(i).getText()));
				captureDropObject("TABLE", ctx.table_name().size(), nameList, ctx.if_exists(), "", ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitDrop_view(TSQLParser.Drop_viewContext ctx) {
				List<String> nameList = new ArrayList<>();
				for (int i = 0; i<ctx.simple_name().size(); i++) nameList.add(u.normalizeName(ctx.simple_name().get(i).getText()));
				captureDropObject("VIEW", ctx.simple_name().size(), nameList, ctx.if_exists(), ViewsReportGroup, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitDrop_procedure(TSQLParser.Drop_procedureContext ctx) {
				List<String> nameList = new ArrayList<>();
				for (int i = 0; i<ctx.func_proc_name_schema().size(); i++) nameList.add(u.normalizeName(ctx.func_proc_name_schema().get(i).getText()));
				captureDropObject("PROCEDURE", ctx.func_proc_name_schema().size(), nameList, ctx.if_exists(), ProceduresReportGroup, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitDrop_function(TSQLParser.Drop_functionContext ctx) {
				List<String> nameList = new ArrayList<>();
				for (int i = 0; i<ctx.func_proc_name_schema().size(); i++) nameList.add(u.normalizeName(ctx.func_proc_name_schema().get(i).getText()));
				captureDropObject("FUNCTION", ctx.func_proc_name_schema().size(), nameList, ctx.if_exists(), FunctionsReportGroup, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitDrop_trigger(TSQLParser.Drop_triggerContext ctx) {
				List<String> nameList = new ArrayList<>();
				for (int i = 0; i<ctx.simple_name().size(); i++) nameList.add(u.normalizeName(ctx.simple_name().get(i).getText()));
				captureDropObject("TRIGGER", ctx.simple_name().size(), nameList, ctx.if_exists(), TriggersReportGroup, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitDrop_database(TSQLParser.Drop_databaseContext ctx) {
				List<String> nameList = new ArrayList<>();
				for (int i = 0; i<ctx.id().size(); i++) nameList.add(u.normalizeName(ctx.id().get(i).getText()));
				captureDropObject("DATABASE", ctx.id().size(), nameList, ctx.if_exists(), DatabasesReportGroup, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitDrop_user(TSQLParser.Drop_userContext ctx) {
				List<String> nameList = new ArrayList<>();
				nameList.add(u.normalizeName(ctx.id().getText()));
				captureDropObject("USER", 1, nameList, ctx.if_exists(), UsersReportGroup, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitDrop_schema(TSQLParser.Drop_schemaContext ctx) {
				List<String> nameList = new ArrayList<>();
				nameList.add(u.normalizeName(ctx.id().getText()));
				captureDropObject("SCHEMA", 1, nameList, ctx.if_exists(), UsersReportGroup, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			private void captureDropObject(String objType, int nrDropped, List<String> nameList, TSQLParser.If_existsContext if_exists, String reportGroup, int lineNr) {
				String status = u.Supported; // true for tables, views, etc.
				String nrDroppedFmt = "";
				String ifExists = "";
				objType = objType.toUpperCase();
				if (nrDropped > 1) {
					status = featureSupportedInVersion(DropMultipleObjects, objType);
					nrDroppedFmt = ", >1 object";
				}
				if (if_exists != null) {
					ifExists = " IF EXISTS";
					if (status.equals(u.Supported)) {
						status = featureSupportedInVersion(DropIfExists, objType);
					}
				}
				if (reportGroup.isEmpty()) reportGroup = DDLReportGroup;
				if (status.equals(u.Supported)) {
					if (featureExists(MiscObjects, objType)) {
						status = featureSupportedInVersion(MiscObjects, objType);
					}
				}
				// don't report IF EXISTS separately if supported
				if (status.equals(u.Supported)) {
					if (if_exists != null) {
						ifExists = "";
					}
				}
				captureItem("DROP "+objType+ifExists+nrDroppedFmt, nameList.get(0).toUpperCase(), reportGroup, "", status, lineNr);
				if (nrDropped > 1) {
					for (int i=1; i<nrDropped; i++)
					captureItem("DROP "+objType+ifExists+nrDroppedFmt, nameList.get(i).toUpperCase(), reportGroup, "", u.ObjectReference, lineNr);
				}
			}

			@Override public String visitColumn_def_table_constraints(TSQLParser.Column_def_table_constraintsContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				// -syntax flag must be specified to capture NoCommaInColumnWithTableConstraint
				if (u.reportSyntaxIssues) {
					if (hasParent(ctx.parent,"create_table")) {
						// this issue applies to table variables and to the return table definition of a TVF
						// for regular tables, the issue does not apply
					}
					else {
						List<TSQLParser.Column_def_table_constraintContext> collist = ctx.column_def_table_constraint();
						List<TerminalNode> comma = ctx.COMMA();

						// check for missing comma, i.e. DECLARE @tv TABLE(a int not null primary key(a))  --> parsed as table_constraint
						// note that this is valid:      DECLARE @tv TABLE(a int not null primary key) --> parsed as column_constraint which is part of column_definition
						if (comma.size() < collist.size()-1) {
							for (int i=0; i < collist.size(); i++) {
								if (i >= collist.size()-1) break;

								if (collist.get(i).column_definition() == null) continue;
								if (i < collist.size()-1) {
									if (collist.get(i+1).table_constraint() == null) continue;
								}
								// can only determine whether there is a comma in between by looking at token positions
								int ixEnd = collist.get(i).stop.getStopIndex();
								int ixStart = collist.get(i+1).start.getStartIndex();
								//u.appOutput(u.thisProc()+"ixStart=["+ixStart+"] collist.get(i+1).start.getCharPositionInLine()=["+collist.get(i+1).start.getCharPositionInLine()+"] ");
								boolean commaFound = false;
								for (int j=0; j < comma.size(); j++) {
									int ixComma = comma.get(j).getSymbol().getStartIndex();
									if (ixComma > ixEnd && ixComma < ixStart) {
										 commaFound = true;
									}
								}

								if (commaFound) {
									i++; //skip over the table_constraint
									continue;
								}

								if (featureExists(SyntaxIssues, NoCommaInColumnWithTableConstraint)) {
									String status = featureSupportedInVersion(SyntaxIssues, NoCommaInColumnWithTableConstraint);
									String s = "Column+constraint without comma separator";
									if (!status.equals(u.Supported)) {
										if (u.rewrite) {
											int posInLine = collist.get(i+1).start.getCharPositionInLine()-1;
											if (posInLine < 0) posInLine = 0;

											String rewriteText = " , ";
											if (addRewrite(SyntaxIssues, "", u.rewriteTypeReplace, rewriteText, collist.get(i+1).start.getLine(), posInLine, collist.get(i+1).start.getLine(), posInLine, ixStart, ixStart))
												status = u.Rewritten;
										}
										else {
											addRewrite(s);
										}
									}
									captureItem(s, "", SyntaxIssues, "", status, collist.get(i).stop.getLine());
								}

								i++; //skip over the table_constraint
							}
						}
					}
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitCreate_table(TSQLParser.Create_tableContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String tableNameRaw = ctx.tabname.getText();
				String tableName = u.normalizeName(tableNameRaw);
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"CREATE TABLE "+ ctx.getText()+", tabName=["+tableName+"] ", u.debugPtree);

				// set context
				u.setContext("TABLE", tableName);

				CaptureIdentifier(tableName, tableName, "CREATE TABLE", ctx.start.getLine());

				String status = u.Supported;
				String tableType = getTmpTableType(tableName);
				if (tableType.equals(GlobalTmpTableFmt)) {
					status = featureSupportedInVersion(GlobalTmpTable);
				}

				// ToDo: check for multiple constraints in a column if there's one FK involved
				// ToDo: could this also occur for table types?
//				if (ctx.column_def_table_constraints() != null) {
//					u.appOutput(u.thisProc()+"column_def_table_constraints found, ctx.column_def_table_constraint.size()=["+ctx.column_def_table_constraints().column_def_table_constraint().size()+"]  ");
//					if (ctx.column_def_table_constraints().column_def_table_constraint() != null) {  // should always be true
//						if (ctx.column_def_table_constraints().column_def_table_constraint().size() > 0) {
//							u.appOutput(u.thisProc()+"getTextSpaced(ctx=["+getTextSpaced(ctx.column_def_table_constraints())+"] ");
//							for (TSQLParser.Column_def_table_constraintContext cdtc : ctx.column_def_table_constraints().column_def_table_constraint()) {
//								u.appOutput(u.thisProc()+"iter: ["+getTextSpaced(cdtc)+"] ");
//							}
//
//						}
//					}
//				}

				// ptns
				List<TSQLParser.Create_table_optionsContext> options = ctx.create_table_options();
				if (options.size() > 0) {
					boolean hasPtn = false;
					for (TSQLParser.Create_table_optionsContext option : options) {
						if (option.storage_partition_clause() != null) {
							if (option.storage_partition_clause().LR_BRACKET() != null) {
								hasPtn = true;
								break;
							}
						}
					}
					if (hasPtn) {
						capturePartitioning("CREATE TABLE", tableName, ctx.start.getLine());
					}
				}

				hasSystemVersioningColumn = false;

				visitChildren(ctx);

				if (hasSystemVersioningColumn) {
					tableType = "(temporal)";
					if (status.equals(u.Supported)) {
						status = featureSupportedInVersion(TemporalTable);
					}
				}
				String item = "CREATE TABLE" + " " +tableType;
				captureItem(item, tableName.toUpperCase(), DDLReportGroup, "", status, ctx.CREATE().getSymbol().getLine(), 0);

				// clear context
				u.resetSubContext();

				if (u.execTest) {
					if (u.execTestPLL > 0) {
				        if (!u.execTestPLLOptionsSet) {
				        	// set these just once
				        	u.execTestPLLOptionsSet = true;
				        	String execTest = "select set_config('force_parallel_mode', '1', false)\nselect set_config('parallel_setup_cost', '0', false)\nselect set_config('parallel_tuple_cost', '0', false)\ngo\n";
				        	try { u.writeExecTestFile(execTest); } catch (Exception e) { };
				        }

						String execTest = "ALTER TABLE " +tableNameRaw+ " SET (parallel_workers = "+u.execTestPLL+") -- NB: PG syntax\ngo\n";
				        try { u.writeExecTestFile(execTest); } catch (Exception e) { };
				    }
				}

				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitColumn_constraint(TSQLParser.Column_constraintContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String riName = noName;
				if (ctx.constraint != null) {
					riName = ctx.constraint.getText();
				}

				String riContext = u.uninitialized;
				String riType = u.uninitialized;
				if (ctx.PRIMARY() != null)         riType = "PRIMARY KEY";
				else if (ctx.UNIQUE() != null)     riType = "UNIQUE";
				else if (ctx.REFERENCES() != null) riType = "FOREIGN KEY";
				else if (ctx.DEFAULT() != null)    riType = "column DEFAULT";
				else if (ctx.CHECK() != null)      riType = "CHECK";
				else if (ctx.CONNECTION() != null) riType = "CONNECTION(SQL graph)";
				else if (ctx.VALUES() != null)     riType = "Constraint WITH VALUES";	// unclear what this means exactly
				else if (ctx.null_notnull() != null) riType = "(NOT) NULL";

				if (ctx.null_notnull() == null) {
					riType += fkOnClause(ctx.on_update(), ctx.on_delete());

					if (hasParent(ctx.parent,"create_table"))           riContext = "CREATE TABLE";
					else if (hasParent(ctx.parent,"alter_table"))       riContext = "ALTER TABLE";
					else if (hasParent(ctx.parent,"declare_statement")) riContext = "DECLARE @tableVariable";
					else if (hasParent(ctx.parent,"create_type"))       riContext = "CREATE TYPE(table)";
					else if (hasParent(ctx.parent,"create_or_alter_function")) riContext = "CREATE FUNCTION, table return type";	  // ToDo: could alo be ALTER FUNCTION

					captureForReplication(riName, riType, "", ctx.for_replication());

					captureIndexOptions(riName, riType, riContext, ctx.with_index_options());

					captureIndexConstraint(riName, riType, riContext, ctx.clustered(), false, ctx.start.getLine());
				}

				if (ctx.REFERENCES() != null) {
					String status = featureSupportedInVersion(FKrefDBname);
					captureItem(FKrefDBname, riName, FKrefDBname, "", status, ctx.REFERENCES().getSymbol().getLine());
				}

				if (ctx.UNIQUE() != null) {
					// check for constraint on nullable column
					ParserRuleContext parentRule = ctx.getParent();
					if (parentRule instanceof TSQLParser.Column_definitionContext) {
						// parent = column_definition
        				TSQLParser.Column_definitionContext parentCtx = (TSQLParser.Column_definitionContext)parentRule;
        				String colName = parentCtx.id().getText();
        				captureUniqueOnNullableCol(colName, ctx.UNIQUE().getSymbol().getLine(), null, "UNIQUE constraint");
					}
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitTable_constraint(TSQLParser.Table_constraintContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				String riName = noName;
				if (ctx.constraint != null) {
					riName = ctx.constraint.getText();
				}

				String riContext = u.uninitialized;
				String riType = u.uninitialized;
				if (ctx.PRIMARY() != null)         riType = "PRIMARY KEY";
				else if (ctx.UNIQUE() != null)     riType = "UNIQUE";
				else if (ctx.REFERENCES() != null) riType = "FOREIGN KEY";
				else if (ctx.DEFAULT() != null)    riType = "column DEFAULT";
				else if (ctx.CHECK() != null)      riType = "CHECK";

				riType += fkOnClause(ctx.on_update(), ctx.on_delete());

				if (hasParent(ctx.parent,"create_table"))           riContext = "CREATE TABLE";
				else if (hasParent(ctx.parent,"alter_table"))       riContext = "ALTER TABLE";
				else if (hasParent(ctx.parent,"declare_statement")) riContext = "DECLARE @tableVariable";
				else if (hasParent(ctx.parent,"create_type"))       riContext = "CREATE TYPE(table)";
				else if (hasParent(ctx.parent,"create_or_alter_function")) riContext = "CREATE FUNCTION, table return type";	  // ToDo: could alo be ALTER FUNCTION

				boolean desc = false;
				if (ctx.column_name_list_with_order() != null) {
					List<TerminalNode> descList = ctx.column_name_list_with_order().DESC();
					if (descList.size() > 0) desc = true;
				}

				List<TSQLParser.For_replicationContext> rep = ctx.for_replication();
				captureForReplication(riName, riType, "", rep.size()>0?rep.get(0):null);

				captureIndexOptions(riName, riType, riContext, ctx.with_index_options());

				captureIndexConstraint(riName, riType, riContext, ctx.clustered(), desc, ctx.start.getLine());

				if (ctx.REFERENCES() != null) {
					String tableName = ctx.table_name().getText();
					if (!u.getDBNameFromID(tableName).isEmpty()) {
						String status = featureSupportedInVersion(FKrefDBname);
						captureItem(FKrefDBname, riName, FKrefDBname, "", status, ctx.REFERENCES().getSymbol().getLine());
					}
				}

				if (ctx.UNIQUE() != null) {
					// skip CREATE TYPE/tabvar cases for now; we don't have the symbol table to resolve this
					if (!hasParent(ctx.parent,"create_type") && !hasParent(ctx.parent,"declare_statement")) {
						captureUniqueOnNullableCol("", 0, ctx.column_name_list_with_order(), "UNIQUE constraint");
					}
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}


			private void captureUniqueOnNullableCol(String colName, int lineNr, TSQLParser.Column_name_list_with_orderContext collist, String type) {
				captureUniqueOnNullableCol(colName, lineNr, collist, type, u.currentObjectName);
			}
			private void captureUniqueOnNullableCol(String colName, int lineNr, TSQLParser.Column_name_list_with_orderContext collist, String type, String tableName) {
				if ((collist == null) && colName.isEmpty()) return;
				assert !tableName.isEmpty() : "tableName should not be blank";
				String n = " SINGLE";
				String typeChk = (type + n).toUpperCase();

				if (!colName.isEmpty()) {
					String tabcol = tableName + "." + colName;
					String colDataType = lookupCol(tableName, colName);
					//u.appOutput(CompassUtilities.thisProc()+"colName=["+colName+"] colDataType=["+colDataType+"] typeChk=["+typeChk+"] ");
					if (colDataType.endsWith(" NULL")) {
						String status = featureSupportedInVersion(UniqueOnNullableCol, typeChk);
						String typeFmt = type;
						captureItem(UniqueOnNullableCol+" with "+typeFmt, tabcol, "", UniqueOnNullableCol, status, lineNr);
						//u.appOutput(CompassUtilities.thisProc()+"found typeChk=["+typeChk+"] on nullable column tabcol=["+tabcol+"] status=["+status+"] ");
					}
					return;
				}

				List<TSQLParser.IdContext> cols = collist.id();
				if (cols.size() > 1) n = " MULTIPLE";
				typeChk = (type + n).toUpperCase();
				for (TSQLParser.IdContext col : cols) {
					String colName2 = u.normalizeName(col.getText());
					//u.appOutput(CompassUtilities.thisProc()+"colname2=["+colName2+"] ");

					String tabcol = tableName + "." + colName2;
					String colDataType = lookupCol(tableName, colName2);
					//u.appOutput(CompassUtilities.thisProc()+"colDataType=["+colDataType+"] ");
					if (colDataType.endsWith(" NULL")) {
						String status = featureSupportedInVersion(UniqueOnNullableCol, typeChk);
						String typeFmt = "";
						if (status.equals(u.ReviewSemantics)) typeFmt = type + ", on multiple columns";
						else typeFmt = type;
						captureItem(UniqueOnNullableCol+" with "+typeFmt, tabcol, "", UniqueOnNullableCol, status, col.start.getLine());
						//u.appOutput(CompassUtilities.thisProc()+"found typeChk=["+typeChk+"] on nullable column tabcol=["+tabcol+"] status=["+status+"] ");
						break;
					}
				}
			}

			public String fkOnClause(List<TSQLParser.On_updateContext> on_update, List<TSQLParser.On_deleteContext> on_delete) {
				String fkOn = "";
				if (on_update.size() > 0) fkOn += on_update.get(0).getText().toUpperCase();
				if (on_delete.size() > 0) fkOn += on_delete.get(0).getText().toUpperCase();
				if (!fkOn.isEmpty()) {
					fkOn = u.applyPatternAll(fkOn, "(ON|SET|NO|CASCADE)", " $1 ");
					fkOn = u.collapseWhitespace(fkOn);
					fkOn = ", " + fkOn.trim();
				}
				return fkOn;
			}

			@Override public String visitCreate_index(TSQLParser.Create_indexContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				String ixName = u.normalizeName(ctx.id().getText());
				String tableName = u.normalizeName(ctx.table_name().getText());
				String baseObjType = lookupTableView(tableName.toUpperCase());  // returns only table or view (or blank)
				if (baseObjType.equals("VIEW")) {
					String status = featureSupportedInVersion(IndexedView);
					captureItem(IndexedView+" (materialized view)", ixName, DDLReportGroup, IndexedView, status, ctx.start.getLine(), 0);
					captureItem("CREATE " +"indexed view (materialized view)", "", u.ObjCountOnly, "", u.ObjCountOnly, ctx.start.getLine(), 0);
				}

				CaptureIdentifier(tableName, tableName, "CREATE INDEX", ctx.start.getLine());

				String ixContext = "CREATE INDEX";
				String ixType = "Index"; // uppercase I -- but not sure why this was done
				if (ctx.UNIQUE() != null) ixType = "Index, UNIQUE";

				captureIndexOptions(ixName, ixType, ixContext, ctx.with_index_options());

				TSQLParser.ClusteredContext clustered = ctx.clustered();
				if (ctx.COLUMNSTORE() != null) {
					String type = "COLUMNSTORE";
					String hint = "created as regular index in PG";
					if (ctx.clustered() != null) {
						if (ctx.clustered().getText().equalsIgnoreCase("CLUSTERED")) {
							type = "CLUSTERED COLUMNSTORE";
							hint = "no equivalent in PG";
							clustered = null; // do not report CLUSTERED again below
						}
					}
					String status = featureSupportedInVersion(IndexAttribute, type);
					captureItem(type + " index: " + hint, ixName, DDLReportGroup, type, status, ctx.start.getLine(), 0);
				}

				captureIndexConstraint(ixName, ixType, ixContext, clustered, false, ctx.start.getLine());

				if (ctx.column_name_list() != null) {
					// if there are INCLUDE columns, we can go over the PG max. of 32; (these do not count to the MSSQL max. of 32)
					int nrCols = nrColumn_name_list_with_order(ctx.column_name_list_with_order());
					int nrIncludeCols = nrColumn_name_list(ctx.column_name_list());
					int maxCols = featureIntValueSupportedInVersion(MaxColumnsIndex);
					if (nrCols+nrIncludeCols > maxCols) {
						String status = u.ReviewPerformance; // we cannot actually get this from the .cfg file since it'll be seen as supported
						captureItem("Index exceeds "+maxCols+" columns("+nrCols+" column"+((nrCols==1)?"":"s")+", +"+nrIncludeCols+ " included)" , "", DDLReportGroup, MaxColumnsIndex, status, ctx.start.getLine(), 0);
					}
				}

				//ptns
				if (ctx.storage_partition_clause() != null) {
					if (ctx.storage_partition_clause().LR_BRACKET() != null) {
						capturePartitioning("CREATE INDEX", ixName, ctx.start.getLine());
					}
				}

				// check for UNIQUE index on nullable column
				if (ctx.UNIQUE() != null) {
					captureUniqueOnNullableCol("", 0, ctx.column_name_list_with_order(), "UNIQUE index", tableName);
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitAlter_index(TSQLParser.Alter_indexContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				String ixName = "ALL";
				if (ctx.id() != null) {
					ixName = u.normalizeName(ctx.id().getText());
				}
				String tableName = u.normalizeName(ctx.table_name().getText());

				String ixContext = "INDEX on " + tableName.toUpperCase();

				if (ctx.alter_index_options() != null) {
					String option = getTextSpaced(ctx.alter_index_options()).trim().toUpperCase();
					option = u.applyPatternFirst(option, "^(\\w+)\\b.*$", "$1");
					//u.appOutput(u.thisProc()+"option=["+option+"] ");
					String status = featureSupportedInVersion(AlterIndex, option);
					captureItem(AlterIndex+".."+option, tableName+"."+ixName, DDLReportGroup, "", status, ctx.start.getLine(), 0);
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitTable_type_indices(TSQLParser.Table_type_indicesContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				String ixName = noName;

				String ixContext = u.uninitialized;
				String ixType = "index";
				if (ctx.UNIQUE() != null) ixType = "index, UNIQUE";

				if (hasParent(ctx.parent,"declare_statement")) ixContext = "DECLARE @tableVariable";
				else if (hasParent(ctx.parent,"create_type"))  ixContext = "CREATE TYPE(table)";

				if (!ixContext.equals(u.uninitialized)) {
					captureIndexOptions(ixName, ixType, ixContext, null);

					captureIndexConstraint(ixName, ixType, ixContext, ctx.clustered(), false, ctx.start.getLine());
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitInline_index(TSQLParser.Inline_indexContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				String ixName = noName;

				String ixContext = u.uninitialized;
				String ixType = "index";
				if (ctx.UNIQUE() != null) ixType = "index, UNIQUE";

				if (hasParent(ctx.parent,"create_index"))           ixContext = "CREATE INDEX";
				else if (hasParent(ctx.parent,"create_table"))      ixContext = "CREATE TABLE";
				else if (hasParent(ctx.parent,"alter_table"))       ixContext = "ALTER TABLE";
				else if (hasParent(ctx.parent,"declare_statement")) ixContext = "DECLARE @tableVariable";
				else if (hasParent(ctx.parent,"create_type"))       ixContext = "CREATE TYPE(table)";
				else if (hasParent(ctx.parent,"create_or_alter_function")) ixContext = "CREATE FUNCTION, table return type";	  // ToDo: could alo be ALTER FUNCTION

				captureIndexOptions(ixName, ixType, ixContext, ctx.with_index_options());

				captureItem("CREATE " + ixType.toUpperCase(), "", u.ObjCountOnly, "", u.ObjCountOnly, ctx.start.getLine(), 0);

				String ixStatus = featureSupportedInVersion(InlineIndex);

				boolean clustered_columnstore = false;
				// do not rewrite CLUSTERED COLUMNSTORE indexes, which have no column list
				if (ctx.COLUMNSTORE() != null) {
					if (ctx.clustered() != null) {
						if (ctx.clustered().getText().equalsIgnoreCase("CLUSTERED")) {
							clustered_columnstore = true;
						}
					}
				}

				if (hasParent(ctx.parent,"create_table")) {
					if (!clustered_columnstore) {
						if (!ixStatus.equals(u.Supported)) {
							if (u.rewrite) {
								ParserRuleContext parentRule = ctx.getParent();
								TSQLParser.Column_definitionContext parentCtx = null;
								if (parentRule instanceof TSQLParser.Column_definitionContext) {
					        		parentCtx = (TSQLParser.Column_definitionContext)parentRule;
					        		parentRule = parentCtx.getParent();
					        		if (parentRule instanceof TSQLParser.Column_def_table_constraintContext) {
					        			TSQLParser.Column_def_table_constraintContext cdtcCtx = (TSQLParser.Column_def_table_constraintContext)parentRule;
					        			parentRule = cdtcCtx.getParent();
					        			TSQLParser.Column_def_table_constraintsContext cdtcsCtx = (TSQLParser.Column_def_table_constraintsContext)parentRule;
					        			parentRule = cdtcsCtx.getParent();
					        		}
					        	}
				        		TSQLParser.Create_tableContext tableCtx = (TSQLParser.Create_tableContext)parentRule;

								Integer rwrID = rewriteInlineIndex(ctx, tableCtx);

								// compose CREATE INDEX statement
								String rewriteText = "CREATE ";
								if (ctx.UNIQUE() != null) rewriteText += "UNIQUE ";
								if (ctx.clustered() != null) {
									String c = ctx.clustered().getText().toUpperCase();
									c = u.applyPatternAll(c, "HASH$", "");
									rewriteText += c + " ";
								}
								rewriteText += "INDEX ";
								rewriteText += ctx.id().getText();
								rewriteText += " ON ";
								String tableName = tableCtx.tabname.getText();
								rewriteText += tableName;
								if (ctx.column_name_list_with_order() != null) {
									// need to get column name list, but we'll do this later during actual rewrite
								}
								else {
									String col = parentCtx.id().getText();
									rewriteText += "("+col+")";
								}

								if (addRewrite(InlineIndex, ctx.getText(), u.rewriteTypeCommentAndAppend, rewriteText, ctx.start.getLine(), ctx.start.getCharPositionInLine(), tableCtx.stop.getLine(), tableCtx.stop.getCharPositionInLine(), ctx.start.getStartIndex(), tableCtx.stop.getStopIndex(), rwrID))
									ixStatus = u.Rewritten;
							}
							else {
								addRewrite(InlineIndex+" in "+ixContext);
							}
						}
					}
				}

				if (!clustered_columnstore) {
					captureItem(InlineIndex+" in "+ixContext, "", DDLReportGroup, InlineIndex, ixStatus, ctx.start.getLine(), 0);
				}
				else {
					// CLUSTERED COLUMNSTORE inline index
					String type = "CLUSTERED COLUMNSTORE";
					String hint = ": no equivalent in PG";
					String ccIxstatus = featureSupportedInVersion(IndexAttribute, type);
					captureItem(type + " inline index in "+ixContext+hint, "", DDLReportGroup, InlineIndex, ccIxstatus, ctx.start.getLine(), 0);
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			public Integer rewriteInlineIndex(TSQLParser.Inline_indexContext ctx, TSQLParser.Create_tableContext tableCtx) {
				Map<String, List<Integer>> positions = new HashMap<>();
				int rwrID = u.rewriteTextListKeys.size() + 1;

				positions.put("inlineindexclause", Arrays.asList(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
				positions.put("indent", Arrays.asList(tableCtx.start.getCharPositionInLine(), -1));

				if (ctx.column_name_list_with_order() != null) {
					positions.put("collist", Arrays.asList(ctx.LR_BRACKET().getSymbol().getStartIndex() - ctx.start.getStartIndex(), ctx.RR_BRACKET().getSymbol().getStartIndex() - ctx.start.getStartIndex()+1));
				}

				if (ctx.WHERE() != null) {
					positions.put("where_clause", Arrays.asList(ctx.WHERE().getSymbol().getStartIndex() - ctx.start.getStartIndex(), -1));
				}

				if (ctx.with_index_options() != null) {
					positions.put("index_options", Arrays.asList(ctx.with_index_options().start.getStartIndex() - ctx.start.getStartIndex(), -1));
				}

				u.rewriteIDDetails.put(rwrID, positions);
				return rwrID;
			}

			private void captureIndexConstraint(String name,
			                              String type,
			                              String context,
			                              TSQLParser.ClusteredContext clustered,
			                              boolean desc,
			                              int lineNr) {
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"name=["+name+"] type=["+type+"] context=["+context+"] ", u.debugPtree);

				String status = u.Supported;
				String userHint = "";

				if (type.startsWith("FOREIGN")) {
					type = u.applyPatternAll(type, "\\b(ON|SET|NO|CASCADE)\\b", " $1 ");
					type = u.collapseWhitespace(type);
					type = type.trim();
				}

				if (type.startsWith("CONNECTION")) {
					status = featureSupportedInVersion(SQLGraph);
				}

				if (type.equals("PRIMARY KEY")) {
					type = "constraint PRIMARY KEY/UNIQUE";
					captureItem("constraint PRIMARY KEY", "", u.ObjCountOnly, "", u.ObjCountOnly, 0, 0);
				}
				else if (type.equals("UNIQUE")) {
					type = "constraint PRIMARY KEY/UNIQUE";
					captureItem("constraint UNIQUE", "", u.ObjCountOnly, "", u.ObjCountOnly, 0, 0);
				}
				else if (type.equals("FOREIGN KEY")) {
					type = "constraint FOREIGN KEY";
				}
				else if (type.equals("CHECK")) {
					type = "constraint CHECK";
				}
				else if (type.equals("column DEFAULT")) {
					type = "constraint column DEFAULT";
				}

				if (status.equals(u.Supported)) {
					if (clustered != null) {
						if (clustered.HASH() != null) {
							status = featureSupportedInVersion(HashIndex);
							String hash = "NONCLUSTERED HASH";
							type = "NONCLUSTERED HASH " + type;

							if (rewriteDirectOrig.contains(hash)) {
								if (!status.equals(u.Supported)) {
									if (u.rewrite) {
										String rewriteText = rewriteDirectReplace.get(rewriteDirectOrig.indexOf(hash));
										if (addRewrite(HashIndex, hash, u.rewriteTypeReplace, rewriteText, clustered.start.getLine(), clustered.start.getCharPositionInLine(), clustered.stop.getLine(), clustered.stop.getCharPositionInLine(), clustered.start.getStartIndex(), clustered.stop.getStopIndex()))
											status = u.Rewritten;
									}
									else {
										addRewrite(HashIndex);
									}
								}
							}
						}

						String clusteredKwd = clustered.getText().toUpperCase();
						if (clusteredKwd.startsWith("CLUSTERED")) {
							status = featureSupportedInVersion(ClusteredIndex);
							if (!status.equals(u.Supported)) {
								type += ", CLUSTERED";
								userHint += " : created as NONCLUSTERED, no physical row order in PG";
							}
						}
					}
				}

				if (type.startsWith("index")) { // lowercase i matters -- this is messy as we're called with 'Index' as well as 'index'
					captureItem("CREATE " + type.toUpperCase(), "", u.ObjCountOnly, "", u.ObjCountOnly, 0, 0);
					if (context.contains("CREATE TABLE") || context.contains("ALTER TABLE")) {
						// table index
						String ixStatus = featureSupportedInVersion(InlineIndex);
						if (!ixStatus.equals(u.Supported)) type = "index";
						captureItem("Inline "+type+" in "+context, name, DDLReportGroup, InlineIndex, ixStatus, lineNr, 0);
						return;  // do not capture this index again	below
					}
				}

				// only for constraints
				if (desc) {
					if (status.equals(u.Supported)) {
						status = featureSupportedInVersion(DescConstraint);
						type += ", with DESC order";
						userHint += " : created as ASC in PG";
					}
				}

				String fmt = context + " " + type + userHint;
				if (status.equals(u.Supported) && context.equals("CREATE INDEX")) {
					// format differently for supported CREATE INDEX cases
					fmt = u.applyPatternFirst(fmt, " index index", " INDEX");
				}
				else {
					if (!context.isEmpty()) {
						context= ", in " + context;
					}
					fmt = type+context+userHint;
				}
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"capturing: type=["+type+"] context=["+context+"] ", u.debugPtree);
				captureItem(CompassUtilities.capitalizeFirstChar(fmt), name, DDLReportGroup, "", status, lineNr, 0);
			}

			private void captureForReplication(String name, String type, String kwd, TSQLParser.For_replicationContext forReplication) {
				if (forReplication == null) return;
				String feature = ForReplication;
				if (forReplication.getText().toUpperCase().startsWith("NOT")) {
					feature = NotForReplication;
				}
				if (featureExists(feature, type)) {
					String status = featureSupportedInVersion(feature, type);

					String section = DDLReportGroup;
					String typeOrig = type;
					if (type.equalsIgnoreCase("PROCEDURE")) {
						section = ProceduresReportGroup;
						type = kwd + " " + type;
					}
					else if (type.equalsIgnoreCase("TRIGGER")) {
						section = TriggersReportGroup;
						type = kwd + " " + type;
					}
					else if (type.equalsIgnoreCase("FOREIGN KEY")) {
						type = "constraint " + type;
					}
					else if (type.equalsIgnoreCase("CHECK")) {
						type = "constraint " + type;
					}
					else if (type.equalsIgnoreCase("IDENTITY")) {
						type += " column";
					}

					captureItem(feature+", " + type, name, section, typeOrig, status, forReplication.start.getLine());
				}
			}

			private void captureIndexOptions(String name, String type, String context, TSQLParser.With_index_optionsContext ixOptions) {
				if (ixOptions == null) return;
				if (ixOptions.index_option_list() == null) return;
				if (ixOptions.index_option_list().index_option() == null) return;

				if (!context.isEmpty()) {
					context= ", in " + context;
				}
				List<TSQLParser.Index_optionContext> ixOpts = ixOptions.index_option_list().index_option();
				for (TSQLParser.Index_optionContext ixOp : ixOpts) {
					if (ixOp != null) {
						String option = ixOp.option_id.getText().toUpperCase();
						String optVal = "";
						if (ixOp.set_id != null) optVal = ixOp.set_id.getText();
						else if (ixOp.on_off() != null) optVal = ixOp.on_off().getText();
						else if (ixOp.DECIMAL() != null) optVal = ixOp.DECIMAL().getText();
						optVal = optVal.toUpperCase();
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"type=["+type+"]  option=["+option+"] optVal=["+optVal+"]  ", u.debugPtree);

						if (type.equals("PRIMARY KEY")) {
							type = "constraint PRIMARY KEY";
						}
						else if (type.equals("UNIQUE")) {
							type = "constraint UNIQUE";
						}

						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"type=["+type+"] ", u.debugPtree);

						if (option.equals("IGNORE_DUP_KEY")) {
							// check for old syntax
							if (optVal.isEmpty()) optVal = "ON";

							String status = "";
							String userHint = "";
							if (optVal.equals("ON")) {
								status = featureSupportedInVersion(IgnoreDupkeyIndex,optVal);
								if (!status.equals(u.Supported)) {
									userHint = ": duplicate-key errors cannot be ignored in PG";
								}
							}
							else { // OFF
								status = u.Ignored;
							}
							type = u.applyPatternFirst(type, "^(Index)\\b", "index");   // for unclear historic reasons
							captureItem("Option "+option+"="+optVal+", "+type+context+userHint, name, IgnoreDupkeyIndex, "", status, ixOp.start.getLine());
						}
						else if (featureExists(IndexOptions,option)) {
							String type2 = u.applyPatternFirst(type, "^index, UNIQUE\\b", "index");
							type2 = u.applyPatternFirst(type2, "^(Index)\\b", "index");   // for unclear historic reasons
							String status = featureSupportedInVersion(IndexOptions,option);
							String optionFmt = option;
							if (!optVal.isEmpty()) optionFmt += "="+optVal;
							captureItem("Option "+optionFmt+", "+type2+context, name, DDLReportGroup, IndexOptions, status, ixOp.start.getLine());
						}
						else {
							// if we get here, something is missing from the .cfg file
							u.appOutput("["+IndexOptions+"] feature '"+option+"' not found in .cfg file");
							if (CompassUtilities.devOptions) u.errorExitStackTrace();
						}
					}
				}
			}

			@Override public String visitColumn_definition(TSQLParser.Column_definitionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"ctx=["+getTextSpaced(ctx)+"] ", u.debugPtree);

	            //find type of column by looking for parent
				String colType = ""; // default: regular table
				if (hasParent(ctx.parent,"declare_statement"))                 colType = "(table variable)";
				else if (hasParent(ctx.parent,"create_type"))                  colType = "(table type)";
				else if (hasParent(ctx.parent,"func_body_returns_table"))      colType = "(table function result)";
				else if (hasParent(ctx.parent,"func_body_returns_table_clr"))  colType = "(table function result)";

				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+" colType=["+colType+"] ", u.debugPtree);

				if ((ctx.TIMESTAMP() == null) && (ctx.id() == null) && (ctx.inline_index() != null)) {
					visitChildren(ctx);
					if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
					return null;
				}

				if (ctx.TIMESTAMP() != null) {
					// a TIMESTAMP column declared only as 'timestamp'
					String colName = "timestamp";
					String dataType = "TIMESTAMP";
					String status = u.NotSupported;
					if (featureExists(TimestampColumnSolo)) {
						status = featureSupportedInVersion(TimestampColumnSolo);
					}
					captureItem(TimestampColumnSolo, colName, Datatypes, "", status, ctx.start.getLine());
					return null;
				}

				String colName = u.normalizeName(ctx.id().getText());
	            boolean isCompCol = false;

	            if (ctx.data_type() != null) {
	            	// regular column
	            	String dataType = u.normalizeName(ctx.data_type().getText().toUpperCase(), "datatype");
	            	if (ctx.data_type().IDENTITY() != null) {
	            		dataType = u.applyPatternFirst(dataType, "^(.*)(IDENTITY.*?)$", "$1 $2");
	            		dataType = u.applyPatternFirst(dataType, "((NOT)?\\s+NULL)?$", "");
	            		String identityCol = CompassUtilities.getPatternGroup(dataType, "^.*?(IDENTITY(\\(.*?\\))?)((NOT)?\\s+NULL)?$", 1);
	            		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"IDENTITY in data_type: colName=["+colName+"] dataType=["+dataType+"] identityCol=["+identityCol+"] ", u.debugPtree);

	            		// find a NOT FOR REPLICATION clause, it's under special_column_option
	            		List<TSQLParser.Special_column_optionContext> specialOption = ctx.special_column_option();
	            		TSQLParser.For_replicationContext forReplication = null;
	            		for (TSQLParser.Special_column_optionContext s : specialOption) {
	            			if (s.for_replication() != null) {
	            				forReplication = s.for_replication();
	            				break;
	            			}
	            		}
	            		captureForReplication(colName, "IDENTITY", "", forReplication);
	            	}
	            	// not sure if IDENTITY can really occur at this level (it is usually inside data_type), but check anyway
	            	else if (ctx.IDENTITY() != null) {
	            		String identityCol = "IDENTITY";
	            		if (ctx.seed != null) {
	            			identityCol = CompassUtilities.getPatternGroup(ctx.getText().toUpperCase(), "^.*?(IDENTITY\\(.*?\\)).*$", 1);
	            			//u.appOutput(u.thisProc()+"seed identityCol=["+identityCol+"] ");
	            		}
	            		dataType += " " + identityCol;
	            		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"IDENTITY in column_definition: colName=["+colName+"] dataType=["+dataType+"] identityCol=["+identityCol+"] ", u.debugPtree);

	            		captureForReplication(colName, "IDENTITY", "", ctx.for_replication());
	            	}
	            	else if (getBaseDataType(dataType).contains("DATETIME")) {
	            		if (ctx.column_constraint().size() > 0)  {
	            			if (ctx.column_constraint().get(0).DEFAULT() != null)  {   // assuming the DEFAULT is for indexid=0
								// check for numeric-as-date
								checkNumericDateVarAssign(colName, getBaseDataType(dataType), ctx.column_constraint().get(0).expression(), ctx.start.getLine());
							}
						}
	            	}
	            	else if (getBaseDataType(dataType).equalsIgnoreCase("NUMERIC") || getBaseDataType(dataType).equalsIgnoreCase("DECIMAL")) {
	            		List<TSQLParser.Column_constraintContext> colConstraints = ctx.column_constraint();
	            		if (colConstraints.size() > 0) {
	            			for (TSQLParser.Column_constraintContext c : colConstraints) {
		            			if (c.DEFAULT() != null) {
		            				if (isString(expressionDataType(c.expression()))) {
		            					String s = c.expression().getText();
		            					s = u.stripEnclosingBrackets(s);
		            					s = u.stripStringQuotes(s);
		            					s = s.trim();
		            					if (isNumeric(s)) {
		            						// this is OK
		            					}
		            					else {
		            						// error upon insert in SQL Server, but DDL-time error in Babelfish
											if (featureExists(NumericColNonNumDft)) {
												String statusDft = featureSupportedInVersion(NumericColNonNumDft);
												if (!statusDft.equals(u.Supported)) {
													captureItem(NumericColNonNumDft, colName, DatatypeConversion, "", statusDft, ctx.start.getLine());
												}
											}
		            					}
		            				}
		            			}
		            		}
	            		}
	            	}

	            	if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"column: colName=["+colName+"] dataType=["+dataType+"]", u.debugPtree);

	            	if (dataType.equals("XML COLUMN_SET FOR ALL_SPARSE_COLUMNS")) {
						captureXMLFeature("", "XML COLUMN_SET FOR ALL_SPARSE_COLUMNS", "", ctx.start.getLine());
						dataType = "XML";
	            	}

			        if (!u.buildColSymTab) {
			        	// record columns defined in current batch, also when no permanent symtab is built: needed for unique-on-nullable check
		            	// only do this for CREATE/ALTER TABLE
		            	if (hasParent(ctx.parent,"create_table") || hasParent(ctx.parent,"alter_table")) {
			            	boolean nullable = false;
			            	if (ctx.null_notnull().size() == 0) {
			            		if (ctx.column_constraint().size() > 0) {
			            			if (ctx.column_constraint().get(0).null_notnull() != null) {
			            				if (ctx.column_constraint().get(0).null_notnull().NOT() == null) nullable = true;
			            			}
			            			else if (ctx.column_constraint().size() > 1) {
			            				if (ctx.column_constraint().get(1).null_notnull() != null) {
				            				if (ctx.column_constraint().get(1).null_notnull().NOT() == null) nullable = true;
				            			}
			            			}
			            		}
			            	}
			            	else if (ctx.null_notnull().size() > 0) {
			            		if (ctx.null_notnull().get(0).NOT() == null) nullable = true;
			            	}
			            	u.addColSymTab(u.currentObjectName, colName, dataType, nullable, false);
			            }
					}

					// check the datatype
					String UDD = lookupUDD(dataType);
					String dataTypeOrig = "";
					String UDDName = "";
					if (!UDD.isEmpty()) {
						UDDName = u.applyPatternFirst(dataType, " IDENTITY\\b.*$", "").trim();
						dataTypeOrig = formatUDDMsg(dataType,UDD);
						if (!UDD.equals(Undefined)) dataType = UDD;
					}

					String status = u.Supported;
					mostRecentDatatype(getBaseDataType(dataType));
					if (featureExists(Datatypes, getBaseDataType(dataType))) {
						status = featureSupportedInVersion(Datatypes, getBaseDataType(dataType));
					}
					else {
						// datatype is not listed, means: supported
					}
					if (dataType.contains(" IDENTITY(") || dataType.endsWith(" IDENTITY") || dataTypeOrig.contains(" IDENTITY)")) {
						String stmtType = "CREATE TABLE";
						if (hasParent(ctx.parent,"alter_table")) stmtType = "ALTER TABLE";

						// check UDD
						if (!UDDName.isEmpty()) {
							if (featureExists(UDDForIdentity)) {
								String statusIDtype = featureSupportedInVersion(UDDForIdentity, stmtType);
								if (!statusIDtype.equals(u.Supported)) {
									captureItem(UDDForIdentity + " column, " + stmtType + " ("+UDDName+")", UDDName, UDDatatypes, "", statusIDtype, ctx.start.getLine());
								}
							}
						}

						// check precision
						if (dataType.startsWith("NUMERIC(") || dataType.startsWith("DECIMAL(") || dataType.startsWith("DEC(")) {
							String precStr = CompassUtilities.getPatternGroup(dataType, "^\\w+\\((\\d+)\\b", 1);
							if (!precStr.isEmpty()) {
								int idPrec = Integer.parseInt(precStr);

								Integer maxIdPrec = 17;
								if (featureExists(MaxIdentityPrecision)) {
									maxIdPrec = featureIntValueSupportedInVersion(MaxIdentityPrecision);
								}
								if (idPrec > maxIdPrec) {
									String statusPrecision = featureDefaultStatus(MaxIdentityPrecision);
									captureItem("Precision of IDENTITY column ("+idPrec+") exceeds "+maxIdPrec, maxIdPrec.toString(), Datatypes, maxIdPrec.toString(), statusPrecision, ctx.start.getLine());
								}
							}

							// raise warning about BIGINT datatype being used for NUMERIC/IDENTITY
							String statusNumericBigint = featureSupportedInVersion("NUMERIC datatype for IDENTITY", stmtType);
							if (!statusNumericBigint.equals(u.Supported)) {
								captureItem("IDENTITY column created as BIGINT, declared as "+dataType, "", Datatypes, "", statusNumericBigint, ctx.start.getLine());
							}
						}
					}
					captureItem(dataType+dataTypeOrig+" column "+colType, colName, Datatypes, getBaseDataType(dataType), status, ctx.start.getLine());
	            }

	            if (ctx.AS() != null) {
	            	// computed column
	            	isCompCol = true;
	            	String status = u.Supported;
	            	String persisted = "";
	            	String expression = ctx.expression().getText().toUpperCase();
	            	if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"compcol: colName=["+colName+"] expression=["+expression+"] ", u.debugPtree);

	            	if (ctx.PERSISTED() == null) {
	            		status = featureSupportedInVersion(NonPersistedCompCol);
	            		if (status.equals(u.Supported)) {
	            			persisted = " (not persisted)";
	            		}
	            		else {
	            			// BBF will create the column if it can
	            			persisted = " (not persisted, but created as persisted in PG)";
	            		}
	            	}
	            	else {
	            		persisted = " (persisted)";
	            	}
					captureItem("Computed column"+persisted+colType, colName, NonPersistedCompCol, "", status, ctx.start.getLine());
	            }

				if (isCompCol) inCompCol = true;
				inCompColType = " (persisted)";
				if (ctx.PERSISTED() == null) inCompColType = " (not persisted)";

				visitChildren(ctx);

				if (isCompCol) inCompCol = false;
				inCompColType = "";
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitSpecial_column_option(TSQLParser.Special_column_optionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String option = u.normalizeName(ctx.getText().toUpperCase());
				option = u.applyPatternAll(option, "^(\\w+)\\b.*$", "$1");
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"option=["+option+"]  ", u.debugPtree);
				if (!option.contains("FORREPLICATION") ){
					if (option.startsWith("MASKED")) option = "MASKED";
					if (option.startsWith("ENCRYPTED")) option = "ENCRYPTED";

					// find column name
					String colName = "";
					ParserRuleContext parentRule = ctx.getParent();
					if (parentRule instanceof TSQLParser.Column_definitionContext) {
						// parent = column_definition
        				TSQLParser.Column_definitionContext parentCtx = (TSQLParser.Column_definitionContext)parentRule;
        				colName = parentCtx.id().getText();
					}
					else {
						// parent = alter_table
        				TSQLParser.Alter_tableContext parentCtx = (TSQLParser.Alter_tableContext)parentRule;
        				if (parentCtx.column_definition() != null)
        					colName = parentCtx.column_definition().id().getText();
        				else
        					colName = parentCtx.colname.getText();
					}

					String status = featureSupportedInVersion(ColumnAttribute, option);
					captureItem(ColumnAttribute+" " +option, colName, ColumnAttribute, option, status, ctx.start.getLine(), 0);
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitAlter_table(TSQLParser.Alter_tableContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String tableName = u.normalizeName(ctx.tabname.getText());

				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"ALTER table "+ ctx.getText()+", tableName=["+tableName+"] ", u.debugPtree);

				// set context
				u.setContext("TABLE", tableName);

				CaptureIdentifier(tableName, tableName, "ALTER TABLE", ctx.start.getLine());

				//  find sub-command & determine if supported
				String status = u.Supported;
				String subcmd = "";
				boolean captured = false;
				String EnDisAble = "ENABLE";
				if (ctx.DISABLE() != null) EnDisAble = "DISABLE";

				String CheckNoCheck = "CHECK";
				if (ctx.NOCHECK().size() > 0) CheckNoCheck = "NOCHECK";

				// ptns
				if (ctx.SWITCH() != null) {
					capturePartitioning("ALTER TABLE SWITCH", tableName, ctx.start.getLine());
					captured = true;
				}
				else if (ctx.REBUILD() != null) {
					capturePartitioning("ALTER TABLE REBUILD", tableName, ctx.start.getLine());
					captured = true;
				}
				else if (ctx.SPLIT() != null) {
					capturePartitioning("ALTER TABLE SPLIT", tableName, ctx.start.getLine());
					captured = true;
				}
				else if (ctx.MERGE() != null) {
					capturePartitioning("ALTER TABLE MERGE", tableName, ctx.start.getLine());
					captured = true;
				}
				else if (ctx.SYSTEM_VERSIONING() != null) {
					subcmd = "SET SYSTEM_VERSIONING";
					status = featureSupportedInVersion(AlterTable, subcmd);
				}
				else if (ctx.FILESTREAM_ON() != null) {
					subcmd = "SET FILESTREAM_ON";
					status = featureSupportedInVersion(AlterTable, subcmd);
				}
				else if (ctx.LOCK_ESCALATION() != null) {
					subcmd = "SET LOCK_ESCALATION";
					status = featureSupportedInVersion(AlterTable, subcmd);
				}
				else if (ctx.file_table_option().size() > 0) {
					// only taking the first option
					String opt = ctx.file_table_option().get(0).id().keyword().getText().toUpperCase();
					subcmd = u.escapeHTMLChars("SET "+opt);
					status = featureSupportedInVersion(AlterTable, "SET "+opt);
				}
				else if (ctx.REBUILD() != null) {
					subcmd = "REBUILD";
					status = featureSupportedInVersion(AlterTable, subcmd);
				}
				else if (ctx.CHANGE_TRACKING() != null) {
					subcmd = EnDisAble + " CHANGE_TRACKING";
					status = featureSupportedInVersion(AlterTable, subcmd);
				}
				else if (ctx.TRIGGER() != null) {
					subcmd = EnDisAble + " TRIGGER";
					status = featureSupportedInVersion(AlterTable, subcmd);
				}
				else if (ctx.CONSTRAINT() != null) {
					subcmd = CheckNoCheck + " CONSTRAINT";
					status = featureSupportedInVersion(AlterTable, subcmd);
				}
				else if (ctx.COLUMN() != null) {
					subcmd = "ALTER COLUMN"; // todo: not checking all possible options here
					if (ctx.column_definition() != null) {
						if (ctx.column_definition().null_notnull().size() > 0) {
							String n = ctx.column_definition().null_notnull().get(0).getText().toUpperCase();
							if (n.startsWith("NOT")) n = "NOT NULL";
							subcmd += " " + n;
						}
					}
					status = featureSupportedInVersion(AlterTable, subcmd);
				}
				else if (ctx.column_def_table_constraints() != null) {
					int nrAdd = ctx.column_def_table_constraints().column_def_table_constraint().size();
					if (nrAdd > 1) {
						subcmd = AlterTableAddMultiple;
						status = featureSupportedInVersion(AlterTable, subcmd);
						if (!status.equals(u.Supported)) {
							if (u.rewrite) {
								String rewriteText = "";
								Integer rwrID = rewriteAlterTableAddMultiple(ctx);

								if (addRewrite(AlterTableAddMultiple, ctx.getText(), u.rewriteTypeBlockReplace, rewriteText, ctx.start.getLine(), ctx.start.getCharPositionInLine(), ctx.stop.getLine(), ctx.stop.getCharPositionInLine(), ctx.start.getStartIndex(), ctx.stop.getStopIndex(), rwrID))
									status = u.Rewritten;
							}
							else {
								addRewrite(AlterTable+".."+AlterTableAddMultiple);
							}
						}
					}
					else {
						// report adding of single column
						if (ctx.column_def_table_constraints().column_def_table_constraint().get(0).period_for_system_time() != null) {
							subcmd = "ADD PERIOD FOR SYSTEM TIME";
							status = featureSupportedInVersion(AlterTable, subcmd);
						}
						else if (ctx.column_def_table_constraints().column_def_table_constraint().get(0).column_definition() != null) {
							subcmd = "ADD column";
							status = featureSupportedInVersion(AlterTable, subcmd);
						}
						else {
							captured = true; // single-ADD constraint case is captured elsewhere
						}
					}
				}
				else if (ctx.alter_table_drop().size() > 0) {
					List<TSQLParser.Alter_table_dropContext> dropList = ctx.alter_table_drop();
					String dropType = "constraint";
					if (dropList.get(0).alter_table_drop_column() != null) dropType = "column";
					else if (dropList.get(0).period_for_system_time() != null) dropType = "PERIOD FOR SYSTEM_TIME";

					if ((dropList.size() > 1) && (!dropType.startsWith("P")))  {
						subcmd = "DROP multiple items";
						status = featureSupportedInVersion(AlterTable, subcmd);
					}
					else {
						if (dropType.startsWith("P")) {
							subcmd = "DROP " + dropType;
							status = featureSupportedInVersion(AlterTable, subcmd);
						}
						else {
							for (TSQLParser.Alter_table_dropContext d : dropList) {
								if (d.alter_table_drop_column() != null) {
									subcmd = "DROP COLUMN";
									int nrCols = d.alter_table_drop_column().COMMA().size() + 1;
									if (nrCols > 1) subcmd = "DROP multiple columns";
									status = featureSupportedInVersion(AlterTable, subcmd);
								}
								else if (d.alter_table_drop_constraint_id() != null) {
									subcmd = "DROP CONSTRAINT_BY_NAME_ONLY";
									status = featureSupportedInVersion(AlterTable, subcmd);
									subcmd = u.escapeHTMLChars("DROP <constraint-name>");
								}
								else if (d.alter_table_drop_constraint() != null) {
									subcmd = "DROP CONSTRAINT";
									int nrCols = d.alter_table_drop_constraint().COMMA().size() + 1;
									if (nrCols > 1) subcmd = "DROP multiple constraints";
									status = featureSupportedInVersion(AlterTable, subcmd);
								}
								if (!subcmd.isEmpty()) subcmd = ".." + subcmd;
								captureItem("ALTER TABLE"+subcmd, tableName, DDLReportGroup, "", status, ctx.start.getLine(), 0);
								captured = true;
							}
						}
					}
				}

				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"subcmd=["+subcmd+"] captured=["+captured+"] ", u.debugPtree);
				if (!captured) {
					if (!subcmd.isEmpty()) subcmd = ".." + subcmd;
					captureItem("ALTER TABLE"+subcmd, tableName, DDLReportGroup, "", status, ctx.start.getLine(), 0);
				}

				visitChildren(ctx);

				// clear context
				u.resetSubContext();

				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			public Integer rewriteAlterTableAddMultiple(TSQLParser.Alter_tableContext ctx) {
				Map<String, List<Integer>> positions = new HashMap<>();
				int rwrID = u.rewriteTextListKeys.size() + 1;

				boolean startFound = false;
				for (TSQLParser.Column_def_table_constraintContext c : ctx.column_def_table_constraints().column_def_table_constraint()) {
					int ixStart = c.start.getStartIndex();
					int ixStop  = c.stop.getStopIndex();

					if (!startFound) {
						// determine positions of each clause of the ALTER TABLE statement
						positions.put("start", Arrays.asList(ctx.start.getStartIndex(), ixStart));
						positions.put("indent", Arrays.asList(ctx.start.getCharPositionInLine(), -1));
						startFound = true;
					}

					positions.put("part"+String.format("%05d", positions.size()+1), Arrays.asList(ixStart,ixStop));
				}

				for (String p : positions.keySet()) {
					int startPos = positions.get(p).get(0);
					int endPos = positions.get(p).get(1);
				}

				u.rewriteIDDetails.put(rwrID, positions);
				return rwrID;
			}

			@Override public String visitCreate_or_alter_dml_trigger(TSQLParser.Create_or_alter_dml_triggerContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String trigName = u.normalizeName(ctx.simple_name().getText());
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"proc "+ ctx.getText()+", trigName=["+trigName+"] ", u.debugPtree);

				// set context
				u.setContext("TRIGGER", trigName);

				String kwd = "CREATE";
				String status = u.Supported;
				if (ctx.ALTER() != null) {
					kwd = "ALTER";
					if (ctx.CREATE() != null) kwd = "CREATE OR ALTER";
					status = featureSupportedInVersion("ALTER TRIGGER"); // ALTER and CREATE OR ALTER go together
				}

				String trigType = "";
				if ((ctx.FOR() != null) || (ctx.AFTER() != null)) {
					trigType = "FOR/AFTER";
				}
				else if (ctx.INSTEAD() != null) {
					trigType = "INSTEAD OF";
				}

				String trigBaseTable = u.normalizeName(ctx.table_name().getText());

				List<TSQLParser.Dml_trigger_operationContext> trigOpsListRaw = ctx.dml_trigger_operation();
				List<String> trigOpsList = new ArrayList<>();
				for(TSQLParser.Dml_trigger_operationContext opRaw : trigOpsListRaw) {
					trigOpsList.add(opRaw.getText().toUpperCase());
				}
				List<String> sortedOps = trigOpsList.stream().sorted().collect(Collectors.toList());
				String trigOps = String.join(",", sortedOps);

				if (trigOpsListRaw.size() > 1) {
					u.currentObjectAttributes += " " + TrigMultiDMLAttr + " ";
				}

				String trigStatus = "";
				if (ctx.external_name() != null) {
					trigStatus = featureSupportedInVersion(TriggerOptions, "external");
					captureItem(kwd + " TRIGGER, external", trigName, TriggerOptions, "EXTERNAL", trigStatus, ctx.start.getLine(), 0);
				}
				else {
					String IOT = "";
					if (trigType.equals("INSTEAD OF")) {
						boolean IOT_supported = false;
						if (featureSupportedInVersion(InsteadOfTrigger, "TABLE").equals(u.Supported) || featureSupportedInVersion(InsteadOfTrigger, "VIEW").equals(u.Supported)) {
							IOT_supported = true;
						}
						String baseObj = lookupTableView(trigBaseTable.toUpperCase());
						if (!baseObj.isEmpty()) {
							IOT = " on " + baseObj.toLowerCase();
							trigStatus = featureSupportedInVersion(InsteadOfTrigger, baseObj);
							if (status.equals(u.Supported)) {
								status = trigStatus;
							}
						}
						else {
							// in case we cannot determine the base object type
							if (IOT_supported) {
								IOT = ", base object unresolved: table or view?";
								if (status.equals(u.Supported)) {
									status = u.ReviewManually;
								}
							}
							else {
								IOT = " on table/view";
								status = u.NotSupported;
							}
						}
						captureItem(kwd + " TRIGGER, " + trigType + " " + trigOps + IOT, trigName, InsteadOfTrigger, baseObj, status, ctx.start.getLine(), batchLines.toString());
					}
					else {
						// FOR/AFTER trigger
						captureItem(kwd + " TRIGGER, " + trigType + " " + trigOps, trigName, TriggersReportGroup, "", status, ctx.start.getLine(), batchLines.toString());
					}
				}

				String trigSchema = u.getSchemaNameFromID(trigName);
				if (!trigSchema.isEmpty()) {
					String statusSchema = featureSupportedInVersion(TriggerSchemaName);
					if (!statusSchema.equals(u.Supported)) {
						captureItem(TriggerSchemaName, trigName, TriggerSchemaName, "", statusSchema, ctx.start.getLine());
					}
				}

				// options
				if (status.equals(u.Supported) && (trigStatus.isEmpty() || trigStatus.equals(u.Supported))) {
					List<TSQLParser.Trigger_optionContext> options = ctx.trigger_option();
					captureTriggerOptions("DML", trigName, options, ctx.start.getLine());
				}

				captureForReplication(trigName, "TRIGGER", kwd, ctx.for_replication());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			private void captureTriggerOptions(String type, String trigName, List<TSQLParser.Trigger_optionContext> options, int lineNr) {
				for (int i=0; i<options.size(); i++) {
					String option = options.get(i).getText().toUpperCase();
					String optionValue = getOptionValue(option);
					option = getOptionName(option);
					if (option.equals("SCHEMABINDING")) {
						if (type.equals("DML")) {
							captureSchemabinding(true, "Trigger", trigName, TriggerOptions, lineNr);
							continue;
						}
					}
					String trigStatus = featureSupportedInVersion(TriggerOptions, option, optionValue);
					String hint = "";
					if (option.startsWith("EXECUTE AS") && (!trigStatus.equals(u.Supported))) {
						// commented out, this no longer applies after an earlier fix for schema name resolution
						//hint = ": name resolution aspect not included in PG";
					}
					captureItem("Trigger, option WITH "+formatOptionDisplay(option,optionValue)+hint, trigName, TriggerOptions, option, trigStatus, lineNr);
				}
			}

			@Override public String visitCreate_or_alter_ddl_trigger(TSQLParser.Create_or_alter_ddl_triggerContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String trigName = u.normalizeName(ctx.simple_name().getText());
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"proc "+ ctx.getText()+", trigName=["+trigName+"] ", u.debugPtree);

				// set context
				u.setContext("TRIGGER", trigName);

				String kwd = "CREATE";
				String status = u.Supported;
				if (ctx.ALTER() != null) {
					kwd = "ALTER";
					if (ctx.CREATE() != null) kwd = "CREATE OR ALTER";
					status = featureSupportedInVersion("ALTER TRIGGER");  // ALTER and CREATE OR ALTER go together
				}

				// ToDo: get & validate DDL events
				List<TerminalNode> trigActionList = ctx.ID();
				Integer nrLines = batchLines;
				for (TerminalNode n : trigActionList) {
					String trigAction = n.getText().toUpperCase();
					status = featureSupportedInVersion(DDLTrigger, trigAction);
					// capturing each action separately
					captureItem(kwd + " TRIGGER (DDL, "+trigAction+")", trigName, DDLTrigger, trigAction, status, ctx.start.getLine(),  nrLines.toString());
					nrLines = 0;   // do not count lines double
				}

				// options
				List<TSQLParser.Trigger_optionContext> options = ctx.trigger_option();
				captureTriggerOptions("DDL", trigName, options, ctx.start.getLine());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitEnable_trigger(TSQLParser.Enable_triggerContext ctx) {
				String status = featureSupportedInVersion(EnableTrigger);
				captureItem(EnableTrigger, "", EnableTrigger, "", status, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitDisable_trigger(TSQLParser.Disable_triggerContext ctx) {
				String status = featureSupportedInVersion(DisableTrigger);
				captureItem(DisableTrigger, "", DisableTrigger, "", status, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitCreate_or_alter_function(TSQLParser.Create_or_alter_functionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String funcName = u.normalizeName(ctx.func_proc_name_schema().getText());
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"func "+ ctx.getText()+", funcName=["+funcName+"] ", u.debugPtree);
				// set context
				u.setContext("FUNCTION", funcName);

				String udfType = "";
				String udfType2 = "";
				List<TSQLParser.Function_optionContext> options = null;
				if (ctx.func_body_returns_scalar() != null) {
					options = ctx.func_body_returns_scalar().function_option();
					udfType = "scalar";
					String sudfDataType = u.normalizeName(ctx.func_body_returns_scalar().data_type().getText().toUpperCase(), "datatype");
					if (ctx.func_body_returns_scalar().external_name() != null) {
						udfType2 = "external";
					}
					if (ctx.func_body_returns_scalar().atomic_func_body() != null) {
						udfType2 = "atomic natively compiled";
					}

					String sudfDataTypeOrig = sudfDataType;
					String sudfDataTypeReport = "";
					String UDD = lookupUDD(sudfDataType);
					if (!UDD.isEmpty()) {
						sudfDataTypeReport = formatUDDMsg(sudfDataType,UDD);
						if (!UDD.equals(Undefined)) sudfDataType = UDD;
					}
					String statusDataType = u.Supported;
					if (featureExists(Datatypes, getBaseDataType(sudfDataType))) {
						statusDataType = featureSupportedInVersion(Datatypes, getBaseDataType(sudfDataType));
					}
					else {
						// datatype is not listed, means: supported
					}

					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"UDF "+ ctx.getText()+", funcName=["+funcName+"] sudfDataType=["+sudfDataType+"] ", u.debugPtree);
					captureItem(sudfDataType + sudfDataTypeReport + " scalar function result type", "", Datatypes, getBaseDataType(sudfDataType), statusDataType, ctx.start.getLine());
					// for UDDs, the AS keyword is mandatory in Babelfish, though it should be optional. For system datatypes, it is optional
					if (ctx.func_body_returns_scalar().AS() == null) {
						if (ctx.func_body_returns_scalar().BEGIN() != null) {
							String dataTypeChk = sudfDataTypeOrig;
							if (!UDD.isEmpty()) {
								if (!UDD.equalsIgnoreCase(sudfDataTypeOrig)) {
									dataTypeChk = ScalarUDFOptionalAsKwdUDD;
								}
							}
							if (featureExists(ScalarUDFOptionalASKwd, dataTypeChk)) {
								String statusAs = featureSupportedInVersion(ScalarUDFOptionalASKwd, dataTypeChk);
								if (!statusAs.equals(u.Supported)) {
									if (u.rewrite) {
										String rewriteText = "AS " + ctx.func_body_returns_scalar().BEGIN().getText();
										int line = ctx.func_body_returns_scalar().BEGIN().getSymbol().getLine();
										int startPos = ctx.func_body_returns_scalar().BEGIN().getSymbol().getCharPositionInLine();
										int endPos = startPos + 4;  // 4: length(BEGIN) -1
										int startIx = ctx.func_body_returns_scalar().BEGIN().getSymbol().getStartIndex();
										int endIx = startIx + 4;
										if (addRewrite(ScalarUDFOptionalASKwd, "", u.rewriteTypeReplace, rewriteText, line, startPos, line, endPos, startIx, endIx))
											statusAs = u.Rewritten;
									}
									else {
										addRewrite(ScalarUDFOptionalASKwd);
									}
									captureItem(ScalarUDFOptionalASKwd, dataTypeChk, FunctionOptions, "", statusAs, ctx.start.getLine());
								}
							}
						}
					}

//					if (ctx.func_body_returns_scalar().RETURN() != null) {
						// this is captured as a RETURN statement now
//						captureItem("RETURN"+" scalar, in function", "", ControlFlowReportGroup, "RETURN", u.Supported, ctx.func_body_returns_scalar().RETURN().getSymbol().getLine());
//					}
				}
				else if (ctx.func_body_returns_table() != null) {
					udfType = "table";
					inMultiStmtTUDF = true;
					options = ctx.func_body_returns_table().function_option();
					captureAtAtVariables(ctx.func_body_returns_table().LOCAL_ID().getText().toUpperCase(), ctx.func_body_returns_table().LOCAL_ID().getSymbol().getLine(), "declare");	
					// this is captured as a RETURN statement instead
//					captureItem("RETURN"+" result set, in function", "", ControlFlowReportGroup, "RETURN", u.Supported, ctx.func_body_returns_table().RETURN().getSymbol().getLine());
				}
				else if (ctx.func_body_returns_select() != null) {
					udfType = "inline table";
					options = ctx.func_body_returns_select().function_option();
					captureItem("RETURN"+" result set, in function", "", ControlFlowReportGroup, "RETURN", u.Supported, ctx.func_body_returns_select().RETURN().getSymbol().getLine());
				}
				else if (ctx.func_body_returns_table_clr() != null) {
					udfType = "table";
					options = ctx.func_body_returns_table_clr().function_option();
					if (ctx.func_body_returns_table_clr().external_name() != null) {
						udfType2 = "CLR";
					}
				}

				String kwd = "CREATE";
				String status = u.Supported;
				if (ctx.ALTER() != null) {
					kwd = "ALTER";
					if (ctx.CREATE() != null) kwd = "CREATE OR ALTER";
					status = featureSupportedInVersion("ALTER FUNCTION");  // ALTER and CREATE OR ALTER go together
				}

				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"UDF "+ getTextSpaced(ctx) +", funcName=["+funcName+"] udfType=["+udfType+"] udfType2=["+udfType2+"] ", u.debugPtree);

				// capture UDF
				String udfStatus = "";
				if (!udfType2.isEmpty()) {
					udfStatus = featureSupportedInVersion(FunctionOptions, udfType2);
					captureItem(kwd + " FUNCTION, " + udfType + ", " + udfType2, funcName, FunctionOptions, udfType2, udfStatus, ctx.start.getLine(), 0);
				}
				else {
					captureItem(kwd + " FUNCTION, " + udfType, funcName, FunctionOptions, udfType, status, ctx.start.getLine(), batchLines.toString());
				}

				captureParameters("function", funcName, udfType, ctx.procedure_param());

				// options, but only if the stmt is supported
				if (status.equals(u.Supported) && (udfStatus.isEmpty() || udfStatus.equals(u.Supported))) {
					boolean schemabindingFound = false;
					boolean nativeCompileFound = false;
					for (int i=0; i<options.size(); i++) {
						String option = options.get(i).getText().toUpperCase();
						if (option.startsWith("RETURNSNULL")) option = "RETURNS NULL ON NULL INPUT";
						if (option.startsWith("CALLED")) option = "CALLED ON NULL INPUT";
						String optionValue = getOptionValue(option);
						option = getOptionName(option);
						if (option.equals("SCHEMABINDING")) {
							schemabindingFound = true;
							continue;
						}
						if (option.equals("NATIVE_COMPILATION")) nativeCompileFound = true;
						String funcStatus = featureSupportedInVersion(FunctionOptions, option, optionValue);
						String hint = "";
						if (option.startsWith("EXECUTE AS") && (!funcStatus.equals(u.Supported))) {
							// commented out, this no longer applies after an earlier fix for schema name resolution
							//hint = ": name resolution aspect not included in PG";
						}
						captureItem("Function ("+kwd+"), option WITH "+formatOptionDisplay(option,optionValue)+hint, funcName, FunctionOptions, option, funcStatus, ctx.start.getLine());
					}
					if (nativeCompileFound && schemabindingFound) {
						captureSchemabinding(schemabindingFound, "Function", funcName, FunctionOptions, ctx.start.getLine());
					}
				}

				visitChildren(ctx);
				inMultiStmtTUDF = false;
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitCreate_or_alter_procedure(TSQLParser.Create_or_alter_procedureContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String procName = u.normalizeName(ctx.func_proc_name_schema().getText());
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"proc "+ ctx.getText()+", procName=["+procName+"] ", u.debugPtree);

				// set context
				u.setContext("PROCEDURE", procName);

				String procType = "";
				if (ctx.atomic_proc_body() != null) {
					procType = "atomic natively compiled";
				}
				else if (ctx.external_name() != null) {
					procType = "external";
				}

				String kwd = "CREATE";
				String status = u.Supported;
				if (ctx.ALTER() != null) {
					kwd = "ALTER";
					if (ctx.CREATE() != null) kwd = "CREATE OR ALTER";
					status = featureSupportedInVersion("ALTER PROCEDURE"); // ALTER and CREATE OR ALTER go together
				}
				boolean captured = false;

				if (ctx.proc_version != null) {
					String procStatus = featureSupportedInVersion(ProcVersionDeclare);
					captureItem(kwd + " PROCEDURE proc;version", procName+";"+ctx.proc_version.getText(), ProcVersionDeclare, "", procStatus, ctx.start.getLine(), batchLines.toString());
					captured = true;
				}

				String procStatus = "";
				if (!captured) {
					if (procName.startsWith("#") || procName.startsWith("[#")) {
						procStatus = featureSupportedInVersion(TemporaryProcedures);
						if (!procStatus.equals(u.Supported)) {
							captureItem(kwd + " PROCEDURE, #temporary: not dropped automatically", procName, TemporaryProcedures, "", procStatus, ctx.start.getLine(), batchLines.toString());
							captured = true;
						}
					}
				}

				if (!captured) {
					if (!procType.isEmpty()) {
						procStatus = featureSupportedInVersion(ProcedureOptions,procType);
						captureItem(kwd + " PROCEDURE, "+procType, procName, ProcedureOptions, procType, procStatus, ctx.start.getLine(), batchLines.toString());
						captured = true;
					}
				}

				if (!captured) {
					captureItem(kwd + " PROCEDURE", procName, ProceduresReportGroup, "", status, ctx.start.getLine(), batchLines.toString());
					captured = true;
				}

				captureParameters("procedure", procName, "", ctx.procedure_param());

				// options
				if (status.equals(u.Supported) && (procStatus.isEmpty() || procStatus.equals(u.Supported))) {
					boolean schemabindingFound = false;
					boolean nativeCompileFound = false;
					List<TSQLParser.Procedure_optionContext> options = ctx.procedure_option();
					for (TSQLParser.Procedure_optionContext optionX : options) {
						String option = optionX.getText().toUpperCase();
						String optionValue = getOptionValue(option);
						option = getOptionName(option);
						u.currentObjectAttributes += " " + option + " ";
						if (option.equals("SCHEMABINDING")) {
							schemabindingFound = true;
							continue;
						}
						if (option.equals("NATIVE_COMPILATION")) nativeCompileFound = true;
						String procOptionStatus = featureSupportedInVersion(ProcedureOptions, option, optionValue);

						String s = "Procedure ("+kwd+"), option WITH "+formatOptionDisplay(option,optionValue);
						if (option.equals("RECOMPILE") && (ctx.procedure_option().size() == 1)) {
							// rewrite RECOMPILE, but only when no other options are specified
							if (!procOptionStatus.equals(u.Supported)) {
								if (u.rewrite) {
									String origText = ctx.WITH().getText() + " " + optionX.getText();
									String rewriteText = "";
									if (addRewrite(s, origText, u.rewriteTypeReplace, rewriteText, ctx.WITH().getSymbol().getLine(), ctx.WITH().getSymbol().getCharPositionInLine(), optionX.stop.getLine(), optionX.stop.getCharPositionInLine(), ctx.WITH().getSymbol().getStartIndex(), optionX.stop.getStopIndex()))
										procOptionStatus = u.Rewritten;
								}
								else {
									addRewrite(s);
								}
							}
						}
						captureItem(s, procName, ProcedureOptions, option, procOptionStatus, ctx.start.getLine());
					}
					if (nativeCompileFound && schemabindingFound) {
						captureSchemabinding(schemabindingFound, "Procedure", procName, ProcedureOptions, ctx.start.getLine());
					}
				}

				captureForReplication(procName, "PROCEDURE", kwd, ctx.for_replication());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			public void captureParameters(String objType, String objName, String funcType, List<TSQLParser.Procedure_paramContext> params) { 
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"objType=["+objType+"] objName=["+objName+"] nr.params=["+params.size()+"] ", u.debugPtree);

				String execParams = "";
				String execParamsNull = "";
				String execParamsRand = "";
				for(int i=0; i<params.size(); i++) {
		            String parName = params.get(i).LOCAL_ID().getText();
		            String dataType = u.normalizeName(params.get(i).data_type().getText().toUpperCase(), "datatype");
		            String parDft = params.get(i).default_val != null ? params.get(i).default_val.getText() : "";
		            String parOpt = params.get(i).param_option != null ? params.get(i).param_option.getText().toUpperCase() : "";
		            addLocalVar(parName, dataType);
					captureAtAtVariables(parName.toUpperCase(), params.get(i).start.getLine(), "declare");		            

					String UDD = lookupUDD(dataType);
					String UDDfmt = "";
					if (UDD.equals("TABLE")) {
						dataType = "TABLE";
					}
					else if (!UDD.isEmpty()) {
						UDDfmt = formatUDDMsg(dataType,UDD);
						if (!UDD.equals(Undefined)) dataType = UDD;
					}

		            String parItem = dataType + UDDfmt + " " + objType +  " parameter";
				  	if (!parDft.isEmpty()) {
				  		parItem += " (with default value)";
						// check for numeric-as-date
						checkNumericDateVarAssign(parName, params.get(i).default_val, params.get(i).start.getLine());

						// check for unquoted string
						captureUnquotedString(parDft, "parameter default", params.get(i).default_val.start.getLine(), params.get(i).default_val.start.getCharPositionInLine(), params.get(i).default_val.stop.getLine(), params.get(i).default_val.stop.getCharPositionInLine(), params.get(i).default_val.start.getStartIndex(), params.get(i).default_val.stop.getStopIndex());

						if (objType.equalsIgnoreCase("FUNCTION")) {
							execParams     += parDft + ", ";
							execParamsNull += parDft + ", ";
							execParamsRand += parDft + ", ";
						}
				  	}
				  	else {
				  		int dataTypeLen = 1;
						String dataTypeLenStr = CompassUtilities.getPatternGroup(dataType, "^\\w+\\((\\d+|MAX)\\b", 1);
						if (!dataTypeLenStr.isEmpty()) {
							if (dataTypeLenStr.equals("MAX")) {
								dataTypeLen = 8000;
							}
							else {
								dataTypeLen = Integer.parseInt(dataTypeLenStr);
							}
						}
						else {
							if (dataType.equals("TEXT")) dataTypeLen = 100000;
							if (dataType.equals("IMAGE")) dataTypeLen = 100000;
							if (dataType.equals("SYSNAME")) dataTypeLen = 256;
							if (dataType.equals("UNIQUEIDENTIFIER")) dataTypeLen = 32;
						}
						if (dataTypeLen > 100) dataTypeLen = 100; // don't overdo it


				  		String execValue = "NULL";
				  		if (dataType.equals("TABLE")) execValue = "NULL";
				  		else if (isString(dataType)) execValue = "' '";
				  		else if (isNumeric(dataType)) execValue = "1";
				  		else if (isDateTime(dataType)) execValue = "'01-Jan-2023 23:24:25'";
				  		else if (isBinary(dataType)) {
				  			execValue = "0x0abc";
				  			if (dataType.equals("UNIQUEIDENTIFIER")) execValue = "'00000000-0000-0000-0000-000000000000'";
				  		}

				  		String execValueRand = "NULL";
				  		if (dataType.equals("TABLE")) execValueRand = "NULL";
				  		else if (isString(dataType)) {
				  			execValueRand = u.generateRandomString("char", dataTypeLen);
				  		}
				  		else if (isNumeric(dataType)) {
				  			Random rand = new Random();
							execValueRand = Integer.valueOf(rand.nextInt(256)).toString();
				  		}
				  		else if (isDateTime(dataType)) {
				  			execValueRand = "'01-Jan-2023 23:24:25'";
				  		}
				  		else if (isBinary(dataType)) {
				  			String type = "hex";
				  			if (dataType.equals("UNIQUEIDENTIFIER")) type = "uuid";
				  			execValueRand = u.generateRandomString(type, dataTypeLen);
				  		}

				  		if (objType.equalsIgnoreCase("PROCEDURE")) {
				  			execParams     += " " + parName + " =  "+execValue+", ";
				  			execParamsNull += " " + parName + " =  NULL, ";
				  			execParamsRand += " " + parName + " =  "+execValueRand+", ";
				  		}
				  		else if (objType.equalsIgnoreCase("FUNCTION")) {
				  			execParams     += execValue+", ";
				  			execParamsNull += "NULL, ";
				  			execParamsRand += execValueRand+", ";
				  		}
				  	}

				  	if (parOpt.equals("OUT") || parOpt.equals("OUTPUT")) parItem += " (OUTPUT)";
				  	parItem = parItem.replaceFirst("value\\) \\(", "value, ");

					String statusDataType = u.Supported;
					mostRecentDatatype(getBaseDataType(dataType));
					if (featureExists(Datatypes, getBaseDataType(dataType))) {
						statusDataType = featureSupportedInVersion(Datatypes, getBaseDataType(dataType));
					}
					else {
						// datatype is not listed, means: supported
					}
					if (dataType.equals("CURSOR")) {
						String statusCursorParam = featureSupportedInVersion(CursorParameters);
						captureItem(parItem, parName, CursorsReportGroup, dataType, statusCursorParam, params.get(i).start.getLine());
					}
					else {
						captureItem(parItem, parName, dataType.equals("TABLE")? TableVariablesType : Datatypes, getBaseDataType(dataType), statusDataType, params.get(i).start.getLine());
					}

					// test for special chars in identifiers not currently supported
					List<String> specialChars = featureValueList(SpecialCharsIdentifier);
					for (int j=0; j<specialChars.size(); j++) {
						String c = specialChars.get(j);
						CaptureSpecialCharIdentifier(parName, c, SpecialCharsIdentifier, params.get(i).start.getLine());
					}

					// test for special chars in parameters not currently supported
					List<String> specialCharsParam = featureValueList(SpecialCharsParameter);
					for (int j=0; j<specialCharsParam.size(); j++) {
						String c = specialCharsParam.get(j);
						CaptureSpecialCharIdentifier(parName, c, SpecialCharsParameter, params.get(i).start.getLine());
					}
		        }
		        if (pass == 1) return;

		        // check max # parameters
		        String maxParSection = MaxProcParameters;
				if (objType.equalsIgnoreCase("FUNCTION")) {
		        	maxParSection = MaxFuncParameters;
		        }
				Integer maxPars = featureIntValueSupportedInVersion(maxParSection);
				String statusMaxParams = featureDefaultStatus(maxParSection);
				//u.appOutput(u.thisProc()+"objType=["+objType+"] objName=["+objName+"] maxPars=["+maxPars+"] statusMaxParams=["+statusMaxParams+"] ");
				if (params.size() > maxPars) {
					captureItem("Number of "+objType+" parameters ("+params.size()+") exceeds "+maxPars, maxPars.toString(), maxParSection, maxPars.toString(), statusMaxParams, 0, 0);
				}

				// exec test
				if (u.execTest) {
			        execParams = u.collapseWhitespace(u.applyPatternFirst(execParams, ", $", ""));
			        execParamsNull = u.collapseWhitespace(u.applyPatternFirst(execParamsNull, ", $", ""));
			        execParamsRand = u.collapseWhitespace(u.applyPatternFirst(execParamsRand, ", $", ""));
			        String execTest = "";
			        if (objType.equalsIgnoreCase("PROCEDURE")) {
			        	execTest = "EXECUTE " + u.currentObjectName + " " + execParamsNull + "\ngo\n";
			        	try { u.writeExecTestFile(execTest); } catch (Exception e) { };
			        	execTest = "EXECUTE " + u.currentObjectName + " " + execParams + "\ngo\n";
			        	try { u.writeExecTestFile(execTest); } catch (Exception e) { };
			        	if (u.execTestRandomArgs) {
			        		execTest = "EXECUTE " + u.currentObjectName + " " + execParamsRand + "\ngo\n";
			        		try { u.writeExecTestFile(execTest); } catch (Exception e) { };
			        	}
			        }
			        else if (objType.equalsIgnoreCase("FUNCTION")) {
			       		String selectFrom = "";
			       		if (funcType.equalsIgnoreCase("scalar")) selectFrom = "";
			       		else if (funcType.equalsIgnoreCase("table")) selectFrom = "* FROM ";
			       		else if (funcType.equalsIgnoreCase("inline table")) selectFrom = "* FROM ";
			       		else selectFrom = "x";  // don't generate calls
			       		if (!selectFrom.equals("x")) {
				        	execTest = "SELECT " + selectFrom + u.currentObjectName + "(" + execParamsNull + ")\ngo\n";
				        	try { u.writeExecTestFile(execTest); } catch (Exception e) { };
				        	execTest = "SELECT " + selectFrom + u.currentObjectName + "(" + execParams + ")\ngo\n";
				        	try { u.writeExecTestFile(execTest); } catch (Exception e) { };
			        		if (u.execTestRandomArgs) {
					        	execTest = "SELECT " + selectFrom + u.currentObjectName + "(" + execParamsRand + ")\ngo\n";
					        	try { u.writeExecTestFile(execTest); } catch (Exception e) { };
					        }
				        }
			        }
			    }
		    }

			private void captureSchemabinding (boolean schemabindingFound, String objType, String objName, String section, int lineNr) {
				String optionFmt = "";
				String option = "";
				String hint = "";
				if (objType.equalsIgnoreCase("VIEW")) {
					if (schemabindingFound) return;
					option = "without SCHEMABINDING";
					optionFmt = option;
					hint = ": created in PG as WITH SCHEMABINDING";
				}
				else if (objType.equalsIgnoreCase("TRIGGER")) {
					if (!schemabindingFound) return;
					option = "SCHEMABINDING";
					optionFmt = "WITH SCHEMABINDING";
					hint = ": created in PG as without SCHEMABINDING";
				}
				else {
					// procs & functions; we only get here when both NATIVE_COMPILATION and SCHEMABINDING were specified
					if (!schemabindingFound) return;
					option = "SCHEMABINDING";
					optionFmt = "WITH SCHEMABINDING";
					hint = ": created in PG as without SCHEMABINDING";
				}
				String status = featureSupportedInVersion(section, option);
				captureItem(objType+", "+optionFmt+hint, objName, section, option, status, lineNr);
			}

			@Override public String visitCreate_or_alter_view(TSQLParser.Create_or_alter_viewContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String viewName = u.normalizeName(ctx.simple_name().getText());
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"view "+ ctx.getText()+", viewName=["+viewName+"] ", u.debugPtree);

				String kwd = "CREATE";
				String status = u.Supported;
				if (ctx.ALTER() != null) {
					kwd = "ALTER";
					if (ctx.CREATE() != null) kwd = "CREATE OR ALTER";
					status = featureSupportedInVersion("ALTER VIEW");  // ALTER and CREATE OR ALTER go together
				}
				captureItem(kwd + " VIEW", viewName, ViewsReportGroup, "", status, ctx.start.getLine(),  batchLines.toString());

				// set context
				u.setContext("VIEW", viewName);

				boolean schemabindingFound = false;
				List<TSQLParser.View_attributeContext> vOptions = ctx.view_attribute();
				for (int i = 0; i <vOptions.size(); i++) {
					TSQLParser.View_attributeContext vOption = vOptions.get(i);
					String option = vOption.getText().toUpperCase();
					if (option.equals("SCHEMABINDING")) schemabindingFound = true;  	// need to check for absence of SCHEMABINDING
					String statusOpt = featureSupportedInVersion(ViewOptions, option);
					captureItem("View, with "+option, "", ViewOptions, option, statusOpt, vOption.start.getLine());
				}
				captureSchemabinding(schemabindingFound, "View", viewName, ViewOptions, ctx.start.getLine());

				if (ctx.CHECK() != null) {
					String option = "CHECK OPTION";
					String statusOpt = featureSupportedInVersion(ViewOptions, option);
					captureItem("View, with "+option, "", ViewOptions, option, statusOpt, ctx.start.getLine());
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitRanking_windowed_function(TSQLParser.Ranking_windowed_functionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String aggFuncName = u.uninitialized;
				if (ctx.agg_func != null) aggFuncName = ctx.agg_func.getText();
				else if (ctx.NTILE() != null) aggFuncName = ctx.NTILE().getText();
				captureAggregateFunction(aggFuncName, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitAnalytic_windowed_function(TSQLParser.Analytic_windowed_functionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String aggFuncName = u.uninitialized;
				if (ctx.first_last != null) aggFuncName = ctx.first_last.getText();
				else if (ctx.lag_lead != null) aggFuncName = ctx.lag_lead.getText();
				else if (ctx.rank != null) aggFuncName = ctx.rank.getText();
				else if (ctx.pct != null) aggFuncName = ctx.pct.getText();
				else if (ctx.apct != null) aggFuncName = ctx.apct.getText();
				captureAggregateFunction(aggFuncName, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitAggregate_windowed_function(TSQLParser.Aggregate_windowed_functionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String aggFuncName = u.uninitialized;
				if (ctx.agg_func != null) aggFuncName = ctx.agg_func.getText();
				else if (ctx.cnt != null) aggFuncName = ctx.cnt.getText();
				else if (ctx.CHECKSUM_AGG() != null) aggFuncName = ctx.CHECKSUM_AGG().getText();
				else if (ctx.GROUPING() != null) aggFuncName = ctx.GROUPING().getText();
				else if (ctx.GROUPING_ID() != null) aggFuncName = ctx.GROUPING_ID().getText();
				captureAggregateFunction(aggFuncName, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			private void captureAggregateFunction(String aggFuncName, int lineNr) {
				aggFuncName = aggFuncName.toUpperCase();
				String status = u.NotSupported;
				if (featureExists(AggregateFunctions, aggFuncName)) {
					status = featureSupportedInVersion(AggregateFunctions, aggFuncName);
				}
				else {
					// this means we're missing an item in the list= key in the .cfg file
					u.appOutput("Function ["+aggFuncName+"] not found in section ["+AggregateFunctions+"] in .cfg file");
					if (CompassUtilities.devOptions) u.errorExitStackTrace();
				}
				captureItem(aggFuncName+"()", "", AggregateFunctions, aggFuncName, status, lineNr);
			}

			@Override public String visitFunction_call(TSQLParser.Function_callContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				if (ctx.func_proc_name_server_database_schema() != null) {
					String funcName = u.normalizeName(ctx.func_proc_name_server_database_schema().getText().toUpperCase());
					TSQLParser.Function_arg_listContext argListRaw = ctx.function_arg_list();
					int nrArgs = argListCount( ctx.function_arg_list());
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"scalar fname fulltxt=["+ ctx.getText()+"], funcName=["+funcName+"]nrArgs=["+nrArgs+"]  ", u.debugPtree);

					List<TSQLParser.ExpressionContext> argList = new ArrayList<>();
					List<String> argListText = new ArrayList<>();
					if (nrArgs > 0) {
						argList = argListRaw.expression();
					}

					boolean done = false;
					String funcSchemaName = u.getSchemaNameFromID(funcName).toUpperCase();
					String funcObjName = u.getObjectNameFromID(funcName).toUpperCase();

					if (funcSchemaName.equals("SYS") || funcSchemaName.isEmpty() || funcObjName.startsWith("FN_")) {
						// is this a system function?
						if (featureExists(SystemFunctions, funcObjName)) {
							String status = featureSupportedInVersion(SystemFunctions, funcObjName);
							String funcNameFmt = funcObjName.toLowerCase()+"()";
							int lineNr = ctx.start.getLine();
							if (funcNameFmt.startsWith("dm_")) funcNameFmt = "sys." + funcNameFmt;
							captureItem(funcNameFmt, "", SystemFunctions, funcObjName, status, lineNr);
							done = true;

							if (funcObjName.equalsIgnoreCase("fn_listextendedproperty")) {
								if (status.equals(u.Supported)) {
									if (nrArgs >= 2) {	// there should always be 7 arguments, but our input may not be valid
										int i = 2;
										String exProp = argList.get(i-1).getText();
										captureExtendedPropertyType(exProp, funcNameFmt, SystemFunctions, status, lineNr);
									}
									if (nrArgs >= 4) {
										int i = 4;
										String exProp = argList.get(i-1).getText();
										captureExtendedPropertyType(exProp, funcNameFmt, SystemFunctions, status, lineNr);
									}
									if (nrArgs >= 6) {
										int i = 6;
										String exProp = argList.get(i-1).getText();
										captureExtendedPropertyType(exProp, funcNameFmt, SystemFunctions, status, lineNr);
									}
								}
							}
						}
						else {
							// it's a BIF or UDF
						}
					}

					if (!done) {
						// it's a BIF or UDF

						//debug
						if (u.debugging) {
							u.dbgOutput("scalar nrArgs=["+nrArgs+"]  ctx childcount=["+ctx.getChildCount()+"]", u.debugPtree);
							if (argListRaw != null) u.dbgOutput("scalar arglist childcount=["+argListRaw.getChildCount()+"]  arglist=["+ argListRaw.getText()+"]", u.debugPtree);
							for (int i = 0; i <ctx.getChildCount(); i++) {
								u.dbgOutput(CompassUtilities.thisProc()+"child i=["+i+"/"+nrArgs+"]  txt=["+ctx.getChild(i).getText()+"] ", u.debugPtree);
							}
						}

						if (nrArgs > 0) {
							if (argListRaw.STAR() != null) nrArgs--;
							if (argListRaw.STAR() != null) argListText.add(BIFArgStar);
							if (nrArgs > 0) {
								argList = argListRaw.expression();
								if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"nrArgs=["+nrArgs+"] arglist exprlist size=["+argList.size()+"] ", u.debugPtree);
								for (int i = 0; i <nrArgs; i++) {
									TSQLParser.ExpressionContext expr = argList.get(i);
									argListText.add(argList.get(i).getText());
									if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"arglist expr i=["+i+"/"+nrArgs+"] =["+argList.get(i).getText()+"] ", u.debugPtree);

									String itemTxt = ParamValueDEFAULT+", function call";
									if (argList.get(i).getText().equalsIgnoreCase("DEFAULT")) {
										String statusDft = featureSupportedInVersion(ParamValueDEFAULT, "function");
										if (!statusDft.equals(u.Supported)) {
											// look up param default value: function calls only use by-position arguments, cannot use by-name
											int parNo = i+1;
											String parDft = lookupParDft(funcName, parNo);
											if (parDft.isEmpty()) {
												// if function is found but there is no parameter default, the DEFAULT results in NULL being (determined emperically)
												// (for procs, this is not the case and there'll just be an error)
												if (!lookupSUDF(funcName).isEmpty()) parDft = "NULL";
												else if (!lookupTUDF(funcName).isEmpty()) parDft = "NULL";
											}
											if (!parDft.isEmpty()) {
												if (u.rewrite) {
													String rewriteText = parDft;
													//u.appOutput(u.thisProc()+"param DEFAULT in function call: funcName=["+funcName+"] parNo=["+parNo+"] parDft=["+parDft+"]");
													if (addRewrite(itemTxt, funcName, u.rewriteTypeReplace, rewriteText, argList.get(i).start.getLine(), argList.get(i).start.getCharPositionInLine(), argList.get(i).stop.getLine(), argList.get(i).stop.getCharPositionInLine(), argList.get(i).start.getStartIndex(), argList.get(i).stop.getStopIndex()))
														statusDft = u.Rewritten;
												}
												else {
													addRewrite(itemTxt);
												}
											}
											else {
												// couldn't find the function, so don't attempt to rewrite
											}
										}
										captureItem(itemTxt, "", ParamValueDEFAULT, "function", statusDft, ctx.start.getLine());
									}
								}
							}
							if (argListRaw.STAR() != null) nrArgs++;
						}

						if (inTUDFCall) {
							// this is to avoid reporting a TUDF call also as a scalar UDF call

							// check for TUDF BIFs
							if (funcName.equals("STRING_SPLIT")) {
								captureBIF("STRING_SPLIT", ctx.start.getLine(), "", nrArgs, argList, argListText, ctx.func_proc_name_server_database_schema(), ctx);
							}
							else if (funcName.equals("GENERATE_SERIES")) {
								captureBIF("GENERATE_SERIES", ctx.start.getLine(), "", nrArgs, argList, argListText, ctx.func_proc_name_server_database_schema(), ctx);
							}
							else {
								captureItem("Function call, table", funcName, FunctionsReportGroup, "", u.Supported, ctx.start.getLine());
								inTUDFCall = false;
							}
						}
						else {
							// is this a BIF or SUDF?
							if (featureExists(BuiltInFunctions, funcName)) {
								captureBIF(funcName, ctx.start.getLine(), "", nrArgs, argList, argListText, ctx.func_proc_name_server_database_schema(), ctx);
							}
							else {
								// check for XML/HIERARCHYID methods, these can be parsed as UDF calls
								if (udfIsBifMethod(funcName, CompassUtilities.XMLmethods, CompassUtilities.SUDFNamesLikeXML, CompassUtilities.SUDFSymTab)) {
									captureXMLFeature("XML.", u.getObjectNameFromID(funcName).toLowerCase(), "()", ctx.start.getLine());

									// check for EVENTDATA(), in case of EVENTDATA().VALUE(...)
									if (u.currentObjectType.equals("TRIGGER")) {
										if (funcName.startsWith("EVENTDATA()")) {
											captureBIF("EVENTDATA", ctx.start.getLine(), "", 0);
										}
									}
								}
								else if (udfIsBifMethod(funcName, CompassUtilities.HIERARCHYIDmethods, CompassUtilities.SUDFNamesLikeHIERARCHYID, CompassUtilities.SUDFSymTab)) {
									captureHIERARCHYIDFeature("HIERARCHYID.", u.getObjectNameFromID(funcName), "()", ctx.start.getLine());
								}
								else {
									String statusUDF = u.Supported;
									String UDFcontext = "";
									if (inCompCol) {
										// this is a SUDF call inside a computed column
										UDFcontext = ", in computed column"+inCompColType;
										statusUDF = featureSupportedInVersion(CompColFeatures, cfgScalarUdfCall);
									}
									else {
										if (hasParent(ctx.parent, "table_constraint") || hasParent(ctx.parent, "column_constraint")) {
											// UDF is either in a column default or in a CHECK constraint
											// ToDo: tabvars?
											String ddl = "CREATE TABLE";
											if (hasParent(ctx.parent, "alter_table")) ddl = "ALTER TABLE";

											if (hasParent(ctx.parent, "search_condition")) {
												// CHECK constraint
												String statusUDFInCHECK = featureSupportedInVersion(SUDFinTableDDL, ddl + " CHECK");
												if (!statusUDFInCHECK.equals(u.Supported)) statusUDF = statusUDFInCHECK;
												UDFcontext = ", in CHECK constraint ("+ddl+")";
											}
											else {
												// column DEFAULT
												String statusUDFInDEFAULT = featureSupportedInVersion(SUDFinTableDDL, ddl + " DEFAULT");
												if (!statusUDFInDEFAULT.equals(u.Supported)) statusUDF = statusUDFInDEFAULT;
												UDFcontext = ", in column DEFAULT ("+ddl+")";
											}
										}
									}
									captureItem("Function call, scalar"+UDFcontext, funcName+"()", FunctionsReportGroup, "", statusUDF, ctx.start.getLine());
								}
							}
						}
					}
				}
				else if (ctx.partition_function_call() != null) {
					String funcName = u.normalizeName(ctx.partition_function_call().func_name.getText());
					capturePartitioning("$PARTITION", funcName, ctx.start.getLine());
				}
				visitChildren(ctx);
				if (stringAggWorkaround) stringAggWorkaround = false;
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitNext_value_for(TSQLParser.Next_value_forContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				String seqName = u.normalizeName(ctx.full_object_name().getText());
				String statusNVF = featureSupportedInVersion(NextValueFor);
				captureItem(NextValueFor, seqName, NextValueFor, "", statusNVF, ctx.start.getLine());

				String nvfContext = "";
				if (hasParent(ctx.parent, "declare_local")) {
					//u.appOutput(u.thisProc()+"NEXT VALUE FOR in DECLARE @v, "+u.currentSrcFile+", "+u.currentObjectName+", line "+ctx.start.getLine());
					nvfContext = "DECLARE @v";
				}
				else if (hasParent(ctx.parent, "set_statement")) {
					//u.appOutput(u.thisProc()+"NEXT VALUE FOR in SET @v, "+u.currentSrcFile+", "+u.currentObjectName+", line "+ctx.start.getLine());
					nvfContext = "SET @v";
				}
				else if (ctx.over_clause() != null) {
					//u.appOutput(u.thisProc()+"NEXT VALUE FOR in OVER(), "+u.currentSrcFile+", "+u.currentObjectName+", line "+ctx.start.getLine());
					nvfContext = "OVER()";
				}
				else if (hasParent(ctx.parent, "if_statement") || hasParent(ctx.parent, "while_statement")) {
					if (hasParent(ctx.parent, "update_statement") ||
			            hasParent(ctx.parent, "delete_statement") ||
			            hasParent(ctx.parent, "insert_statement") ||
			            hasParent(ctx.parent, "merge_statement")  ||
			            hasParent(ctx.parent, "select_statement") ||
			            hasParent(ctx.parent, "alter_table")      ||
			            hasParent(ctx.parent, "create_table")) {
			            	// ignore, it's OK
			            }
			            else {
							if (hasParent(ctx.parent, "if_statement")) {
								//u.appOutput(u.thisProc()+"NEXT VALUE FOR in IF, "+u.currentSrcFile+", "+u.currentObjectName+", line "+ctx.start.getLine());
								nvfContext = "IF";
							}
							else if (hasParent(ctx.parent, "while_statement")) {
								//u.appOutput(u.thisProc()+"NEXT VALUE FOR in WHILE, "+u.currentSrcFile+", "+u.currentObjectName+", line "+ctx.start.getLine());
								nvfContext = "WHILE";
							}
			            }
				}
				if (!nvfContext.isEmpty()) {
					String statusNVFC = featureSupportedInVersion(NextValueForContext, nvfContext);
					if (!statusNVFC.equals(u.Supported)) {
						String s = " in ";
						if (nvfContext.startsWith("OVER")) s = " with ";
						captureItem(NextValueFor + s + nvfContext, seqName, NextValueFor, "", statusNVFC, ctx.start.getLine());
					}
				}

				//u.appOutput(u.thisProc()+"seqName=["+seqName+"] ");
				if (!CompassUtilities.getPatternGroup(seqName, "^(\\w+\\.\\.\\w+)$", 1).isEmpty()) {
					u.appOutput(u.thisProc()+"dobueld-ot in seqname");
					nvfContext = "DOTDOT";
					String statusNVFC = featureSupportedInVersion(NextValueForContext, nvfContext);
					if (!statusNVFC.equals(u.Supported)) {
						String s = " with '..' syntax";
						captureItem(NextValueFor + s , seqName, NextValueFor, "", statusNVFC, ctx.start.getLine());
					}
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			private boolean udfIsBifMethod(String funcName, List<String>refMethodList,  Map<String, String>UDFListLikeMethod,  Map<String, String>UDFList) {
				boolean isMethod = false;
				// ToDo: we can improve here: if the function takes a non-string as input argument, it cannot be an XML method for example
				String funcNameBase = u.getObjectNameFromID(funcName);
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"funcName=["+funcName+"]  funcNameBase=["+funcNameBase+"] refMethodList=["+refMethodList+"]  UDFListLikeMethod=["+UDFListLikeMethod.keySet()+"] UDFList=["+UDFList.keySet()+"] ", u.debugPtree);
				if (refMethodList.contains((funcNameBase))) {
					String schemaName = u.getSchemaNameFromID(funcNameBase);
					if (schemaName.equals("DBO") || schemaName.equals("GUEST")) {
						// cannot be an method call, so must be scalar
						isMethod = false;
					}
					else {
						if (!UDFListLikeMethod.containsKey(funcNameBase)) {
							// there is no known UDF with a name similar to a method
							isMethod = true;
						}
						else {
							// is this UDF in the symbol table?
							String resolvedName = u.resolveName(funcName);
							if (UDFList.containsKey(resolvedName)) {
								isMethod = false;  //  UDF found, so not a method
							}
							else {
								isMethod = true;   //  UDF found, so assume it's a method
							}
						}
					}
				}
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"return: isMethod=["+isMethod+"]  ", u.debugPtree);
				return isMethod;
			}

			@Override public String visitTrigger_column_updated(TSQLParser.Trigger_column_updatedContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				captureBIF("UPDATE", ctx.start.getLine(), "", 0);
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitHierarchyid_methods(TSQLParser.Hierarchyid_methodsContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String method = ctx.method.getText().toUpperCase();

				boolean isSpatial = false;
				if (method.equals("TOSTRING")) {
					// can be hierachyID or geospatial - how can we tell?
					if (parentRuleName(ctx.parent,1).equals("method_call")) {
						RuleContext parentCtx = ctx.parent;
						if (parentRuleName(ctx.parent,2).equals("expression")) {
							parentCtx = parentCtx.parent;
							String parentExpr = parentCtx.getText().toUpperCase();
							if (parentExpr.startsWith("@")) {
								String varName = parentExpr.substring(0, parentExpr.indexOf(".TOSTRING"));
								String varType = varDataType(varName);
								if ((varType.equals("GEOGRAPHY")) || (varType.equals("GEOMETRY"))) {
									isSpatial = true;
								}
							}
						}
					}
				}
				if (!isSpatial) {
					// it's not a variable or we cannot figure out the type: do a last-resort attempt: did we see a HIERARCHYID datatype of a sptail datatype most recently?
					if ((mostRecentDatatypeSpatialOrHierarchy.equals("GEOGRAPHY")) || (mostRecentDatatypeSpatialOrHierarchy.equals("GEOMETRY"))) {
						isSpatial = true;
					}
				}

				if (isSpatial) {
					String status = u.NotSupported; // no support for geospatial in sight...
					captureItem(SpatialMethodCallFmt + " .ToString()", "", SpatialReportGroup, "", status, ctx.start.getLine());
				}

				if (!isSpatial) {
					// in case we don't know the type and we cannot guess it, assume it's HIERARCHYID
					captureHIERARCHYIDFeature("HIERARCHYID.", method, "()", ctx.start.getLine());
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitHierarchyid_coloncolon_methods(TSQLParser.Hierarchyid_coloncolon_methodsContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				captureHIERARCHYIDFeature("HIERARCHYID.", ctx.method.getText().toUpperCase(), "()", ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitGraph_clause(TSQLParser.Graph_clauseContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				String txt = ctx.getText().toUpperCase();
				txt = txt.substring(2);
				String status = featureSupportedInVersion(SQLGraph);
				captureItem("CREATE TABLE..AS "+txt, "", SQLGraph, "", status, ctx.start.getLine());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitRowset_function(TSQLParser.Rowset_functionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String funcType = "";
				if (ctx.open_xml() != null) funcType = "OPENXML";
				else if (ctx.open_json() != null) funcType = "OPENJSON";
				else if (ctx.open_query() != null) funcType = "OPENQUERY";
				else if (ctx.open_datasource() != null) funcType = "OPENDATASOURCE";
				else if (ctx.open_rowset() != null) funcType = "OPENROWSET";
				else if (ctx.change_table() != null) funcType = "CHANGETABLE";
				else if (ctx.predict_function() != null) funcType = "PREDICT";

				// don't report OPENQUERY() twice
				boolean openQueryDML = false;
				if (ctx.open_query() != null) {
					if (parentRuleName(ctx.parent).equals("update_statement") ||
					    parentRuleName(ctx.parent).equals("delete_statement") ||
					    parentRuleName(ctx.parent).equals("delete_statement_from") ||
					    parentRuleName(ctx.parent).equals("insert_statement")) {
							openQueryDML = true;
					}
				}

				List<TSQLParser.ExpressionContext> args = new ArrayList<>();
				if (ctx.open_xml() != null) {
					args = ctx.open_xml().expression();
				}
				if (!openQueryDML) {
					captureBIF(funcType, ctx.start.getLine(), "", args.size(), args);
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			private void captureXpath (String function, String qry, int lineNr){
				qry = u.collapseWhitespace(u.stripStringQuotes(qry));

				// mask the actual strings so that we get an idea of the type of query
				qry = u.applyPatternAll(qry, "\\/\\w+", "/field");
				qry = u.applyPatternAll(qry, "@\\w+", "@var");

				//u.appOutput("Xpath:" + function + " " + qry);
				if (!qry.startsWith("@")) {
					if (!qry.startsWith("/") && !qry.startsWith(".") && !qry.startsWith("(")) {
						//u.appOutput("Not Xpath 1.0? qry=["+qry+"] function=["+function+"] lineNr=["+u.currentSrcFile+":"+lineNr+"] ");
					}
				}
				return;
			}

			@Override public String visitFreetext_function(TSQLParser.Freetext_functionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String funcType = "";
				if (ctx.CONTAINSTABLE() != null) funcType = "CONTAINSTABLE";
				else if (ctx.FREETEXTTABLE() != null) funcType = "FREETEXTTABLE";
				else if (ctx.SEMANTICSIMILARITYTABLE() != null) funcType = "SEMANTICSIMILARITYTABLE";
				else if (ctx.SEMANTICKEYPHRASETABLE() != null) funcType = "SEMANTICKEYPHRASETABLE";
				else if (ctx.SEMANTICSIMILARITYDETAILSTABLE() != null) funcType = "SEMANTICSIMILARITYDETAILSTABLE";
				captureBIF(funcType, ctx.start.getLine());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitFreetext_predicate(TSQLParser.Freetext_predicateContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String funcType = "";
				if (ctx.CONTAINS() != null) {
					funcType = "CONTAINS";
					//u.appOutput(u.thisProc()+"CONTAINS() ["+getTextSpaced(ctx)+"] ");
				}
				else if (ctx.FREETEXT() != null) {
					funcType = "FREETEXT";
				}

				int nrArgs = 2;
				captureBIF(funcType, ctx.start.getLine(), "", nrArgs, ctx.expression());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitBuilt_in_functions(TSQLParser.Built_in_functionsContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if (ctx.bif_no_brackets != null) {
					captureBIF(ctx.bif_no_brackets.getText().toUpperCase(), ctx.start.getLine(), "nobracket", 0, null, null, ctx.start.getLine(), ctx.start.getCharPositionInLine(), ctx.stop.getLine(), ctx.stop.getCharPositionInLine(), ctx.start.getStartIndex(), ctx.stop.getStopIndex());
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitBif_cast_parse(TSQLParser.Bif_cast_parseContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				String status = u.Supported;
				String funcName = ctx.bif.getText().toUpperCase();
				String dataType = u.normalizeName(ctx.data_type().getText().toUpperCase(), "datatype");
				if (featureExists(BuiltInFunctions, funcName)) {
					status = featureSupportedInVersion(BuiltInFunctions, funcName);
				}
				captureItem(funcName+"()", "", BuiltInFunctions, funcName, status, ctx.start.getLine());

				String statusDataType = u.Supported;
				if (featureExists(Datatypes, getBaseDataType(dataType))) {
					statusDataType = featureSupportedInVersion(Datatypes, getBaseDataType(dataType));
				}
				else {
					// datatype is not listed, means: supported
					// cannot CAST to a UDD, so no need to check
				}
				captureItem(funcName+"() to "+dataType, "",DatatypeConversion, getBaseDataType(dataType), statusDataType, ctx.start.getLine());

				// check for numeric-as-date
				checkNumericAsDate(dataType, funcName, funcName+"()", ctx.expression(), 1, ctx.start.getLine());

				if (inCompCol) {
					if (funcName.equals("CAST")) {
						captureCompColTypeCast(funcName, dataType, ctx.expression(), ctx.start.getLine());
					}
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitBif_convert(TSQLParser.Bif_convertContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				String status = u.NotSupported;
				String funcName = ctx.bif.getText().toUpperCase();
				String dataType = u.normalizeName(ctx.data_type().getText().toUpperCase(), "datatype");
				if (featureExists(BuiltInFunctions, funcName)) {
					status = featureSupportedInVersion(BuiltInFunctions, funcName);
				}

				String fmt = "";
				String style = "";
				if (ctx.style != null) {
					style = ctx.style.getText();
					style = style.replaceAll("\\(", "");
					style = style.replaceAll("\\)", "");
					fmt = " with style";
				}
				captureItem(funcName+"()"+fmt, style, BuiltInFunctions, funcName, status, ctx.start.getLine());

				// only do the tests below when the BIF is supported
				if (status.equals(u.Supported)) {
					String statusDataType = u.Supported;
					if (featureExists(Datatypes, getBaseDataType(dataType))) {
						statusDataType = featureSupportedInVersion(Datatypes, getBaseDataType(dataType));
					}
					else {
						// datatype is not listed, means: supported
						// cannot CONVERT to a UDD, so no need to check
					}
					captureItem(funcName+"() to "+dataType+fmt, "", DatatypeConversion, dataType, statusDataType, ctx.start.getLine());

					// check for numeric-as-date
					checkNumericAsDate(dataType, funcName, funcName+"()", ctx.convert_expression, 2, ctx.start.getLine());
				}

				if (inCompCol) {
					if (funcName.equals("CONVERT")) {
						captureCompColTypeCast(funcName, dataType, ctx.convert_expression, ctx.start.getLine());
					}
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			private void captureCompColTypeCast(String funcName, String dataType, TSQLParser.ExpressionContext expr, int lineNo) {
				// in a computed column with CAST() or CONVERT(), when datetime is involved it may not be supported
				String srcType = expressionDataType(expr);
				String tgtType = dataType;
				String chkType = "";
				if (isDateTime(srcType) || isDateTime(tgtType)) {
					chkType = cfgCastDatetime;
				}
				if (!chkType.isEmpty()) {
					String chkCast = "CAST("+chkType+")";
					String statusCC = u.Supported;
					if (featureExists(CompColFeatures, chkCast)) {
						statusCC = featureSupportedInVersion(CompColFeatures, chkCast);
						captureItem(funcName + "() involving "+chkType+", in computed column"+inCompColType, "", DatatypeConversion, "", statusCC, lineNo);
					}
				}
				else {
					String statusCC = u.Supported;
					captureItem(funcName + "(), in computed column"+inCompColType, "", DatatypeConversion, "", statusCC, lineNo);
				}
			}

			@Override public String visitTRIM(TSQLParser.TRIMContext ctx) {
				boolean captured = false;
				if (ctx.arg != null) {
					String arg = ctx.arg.getText().toUpperCase();
					String status = featureSupportedInVersion("TRIM", arg);
					if (!status.equals(u.Supported)) {
						captureItem("TRIM("+arg+")", arg, BuiltInFunctions, "TRIM", status, ctx.start.getLine());
						captured = true;
					}
				}
				if (!captured) {
					captureBIF("TRIM", ctx.start.getLine());
				}
				visitChildren(ctx);
				return null;
			}

			@Override public String visitIIF(TSQLParser.IIFContext ctx) {
				captureBIF("IIF", ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitSTRING_AGG(TSQLParser.STRING_AGGContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				// This rule matches STRING_AGG() WITHIN GROUP(ORDER BY...)
				// STRING_AGG() without the WITHIN GROUP clause is matched by rule func_proc_name_server_database_schema, caught in captureBIF()
				STRING_AGG_WITHIN_GROUP = true;
				captureBIF("STRING_AGG", ctx.start.getLine());
				STRING_AGG_WITHIN_GROUP = false;
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitXml_exist_call(TSQLParser.Xml_exist_callContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				captureXMLFeature("XML.", "exist", "()", ctx.start.getLine());
				captureXpath(".EXIST()", ctx.xquery.getText(), ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}
			@Override public String visitXml_modify_call(TSQLParser.Xml_modify_callContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				captureXMLFeature("XML.", "modify", "()", ctx.start.getLine());
				captureXpath(".MODIFY()", ctx.xml_dml.getText(), ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}
			@Override public String visitXml_query_call(TSQLParser.Xml_query_callContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				captureXMLFeature("XML.", "query", "()", ctx.start.getLine());
				captureXpath(".QUERY()", ctx.xquery.getText(), ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}
			@Override public String visitXml_value_call(TSQLParser.Xml_value_callContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if (stringAggWorkaround) {
					stringAggWorkaround = false;
					return(null);
				}
				captureXMLFeature("XML.", "value", "()", ctx.start.getLine());
				captureXpath(".VALUE()", ctx.xquery.getText(), ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}
			@Override public String visitXml_nodes_method(TSQLParser.Xml_nodes_methodContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				captureXMLFeature("XML.", "nodes", "()", ctx.start.getLine());
				captureXpath(".NODES()", ctx.xquery.getText(), ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}
//
//			@Override public String visitXml_type_definition(TSQLParser.Xml_type_definitionContext ctx) {
//				captureXMLFeature("", "XML TYPE DEFINITION", "", ctx.start.getLine());
//				visitChildren(ctx);
//				return null;
//			}
			@Override public String visitCreate_xml_schema_collection(TSQLParser.Create_xml_schema_collectionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				captureXMLFeature("CREATE ", "XML SCHEMA COLLECTION", "", ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}
			@Override public String visitAlter_xml_schema_collection(TSQLParser.Alter_xml_schema_collectionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				captureXMLFeature("ALTER ", "XML SCHEMA COLLECTION", "", ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}
			@Override public String visitDrop_xml_schema_collection(TSQLParser.Drop_xml_schema_collectionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				captureXMLFeature("DROP ", "XML SCHEMA COLLECTION", "", ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}
			@Override public String visitCreate_selective_xml_index(TSQLParser.Create_selective_xml_indexContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				captureXMLFeature("CREATE ", "XML INDEX, SELECTIVE", "", ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}
			@Override public String visitCreate_xml_index(TSQLParser.Create_xml_indexContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				captureXMLFeature("CREATE ", "XML INDEX", "", ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitDeclare_statement(TSQLParser.Declare_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if (ctx.LOCAL_ID() != null) {
					String varName = ctx.LOCAL_ID().getText();
					u.setContext("TABLE", varName);
					String status = featureSupportedInVersion(TableVariables);
					captureItem("TABLE variable declaration", varName, TableVariablesType, "", status, ctx.start.getLine());
					
					captureAtAtVariables(varName.toUpperCase(), ctx.LOCAL_ID().getSymbol().getLine(), "declare");	
				}
				visitChildren(ctx);

				// clear context
				u.resetSubContext();

				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitDeclare_local(TSQLParser.Declare_localContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String varName  = ctx.LOCAL_ID().getText();
				String dataType = u.normalizeName(ctx.data_type().getText().toUpperCase(), "datatype");
				String varDft = ctx.expression() != null ? ctx.expression().getText() : "";
				captureAtAtVariables(varName.toUpperCase(), ctx.LOCAL_ID().getSymbol().getLine(), "declare");	

				String varItem = dataType+" variable";
				String statusDataType = u.Supported;
				String section = Datatypes;
				boolean captured = false;

				if (ctx.data_type().xml_type_definition() != null) {
					statusDataType = featureSupportedInVersion(XMLFeatures, "XML TYPE DEFINITION");
					dataType = cfgXMLSchema;
					varItem = dataType+" variable";
					captureItem(varItem, varName, XMLFeatures, "", statusDataType, ctx.start.getLine());
					captured = true;
				}
				else {
					String UDD = lookupUDD(dataType);
					if (UDD.equals("TABLE")) {
						varItem = "TABLE variable declaration, for TABLE type";
						dataType = "TABLE";
						section = TableVariablesType;
					}
					else if (!UDD.isEmpty()) {
						if (!UDD.equals(Undefined)) {
							varItem = UDD+formatUDDMsg(dataType,UDD) + " variable";
							dataType = UDD;
						}
						else { // Undefined
							varItem = dataType+formatUDDMsg(dataType,UDD) + " variable";
						}
					}
					else {
						varItem = dataType+" variable";
					}
					if (!varDft.isEmpty()) {
						varItem += " (with default value)";
					}

					mostRecentDatatype(getBaseDataType(dataType));
					if (featureExists(Datatypes, getBaseDataType(dataType))) {
						statusDataType = featureSupportedInVersion(Datatypes, getBaseDataType(dataType));
					}
					else {
						// datatype is not listed, means: supported
					}
					addLocalVar(varName, dataType);

					if (!varDft.isEmpty()) {
						// check for numeric-as-date
						checkNumericDateVarAssign(varName, ctx.expression(), ctx.start.getLine());
					}
				}

				if (!captured)
				captureItem(varItem, varName, section, dataType, statusDataType, ctx.start.getLine());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitTable_source_item(TSQLParser.Table_source_itemContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				boolean setInTUDFCall = false;
				String TUDFname = "";

				String stmt = "SELECT";
				if (hasParent(ctx.parent, "update_statement")) stmt = "UPDATE";
				else if (hasParent(ctx.parent, "delete_statement")) stmt = "DELETE";
				else if (hasParent(ctx.parent, "insert_statement")) stmt = "INSERT";
				else if (hasParent(ctx.parent, "merge_statement")) stmt = "MERGE";
				if (hasParent(ctx.parent, "select_statement")) stmt = "SELECT";

				String nameRaw = "";
				String name = "";
				if (ctx.full_object_name() != null) {
					// some TUDF calls are parsed as a table reference with table hints (without WITH keyword)
					nameRaw = ctx.full_object_name().getText();
					name = u.normalizeName(nameRaw);

					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"nameRaw=["+nameRaw+"] name=["+name+"] ctx=["+ctx.getText()+"]  ", u.debugPtree);
					if (ctx.getText().startsWith(name+"(")) {
						String TUDFtype = lookupTUDF(name);
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"is this a TUDF? name=["+name+"] TUDFtype=["+TUDFtype+"] ", u.debugPtree);
						if (TUDFtype.isEmpty()) {
							// it's not a TUDF, so assume it is a table or view being selected from
							if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"name=["+name+"] : no TUDF found, assume it is a table/view", u.debugPtree);
							TUDFname = "";
						}
					}

					// transition table reference for multi-trigger action case
					if (u.currentObjectType.equals("TRIGGER")) {
						if (name.equalsIgnoreCase("INSERTED") || name.equalsIgnoreCase("DELETED")) {
							if (u.currentObjectAttributes.contains(" " + TrigMultiDMLAttr + " ")) {
								String status = featureSupportedInVersion(TransitionTableMultiDMLTrig);
								if (!status.equals(u.Supported)) {
									captureItem(TransitionTableMultiDMLTrig, u.currentObjectName, TransitionTableMultiDMLTrig, name.toUpperCase(), status, ctx.start.getLine());
								}
							}
						}
					}

					// generate Xref records for tables being read-accessed; this is relevant only to determine dependencies, after uploading into PG
					//u.appOutput(u.thisProc()+"SELECT nameRaw=["+nameRaw+"] name=["+name+"] stmt=["+stmt+"] ");
					captureItem("SELECT", name.toUpperCase(), stmt, "", u.ObjectReference, ctx.start.getLine());
				}

				if (TUDFname.isEmpty()) {
					if (ctx.function_call() != null) {
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"table_source_item is a function_call: ["+ctx.function_call().getText()+"] ", u.debugPtree);
						if (ctx.function_call().func_proc_name_server_database_schema() != null) {
							TUDFname = u.normalizeName(ctx.function_call().func_proc_name_server_database_schema().getText().toUpperCase());
						}
					}
				}

				if (!TUDFname.isEmpty()) {
					boolean isXMLMethod = false;
					// first check if this is an XML.nodes() call, it is sometimes parsed as a TUDF call
					if (TUDFname.contains(".NODES")) {
						isXMLMethod = udfIsBifMethod(TUDFname, u.XMLmethods, u.TUDFNamesLikeXML, u.TUDFSymTab);
					}

					if (isXMLMethod) {
						captureXMLFeature("XML.", u.getObjectNameFromID(TUDFname).toLowerCase(), "()", ctx.start.getLine());
					}
					else {
						// the TUDF call is captured in visitFunction_call()
						//captureItem("Function call, table", TUDFname, FunctionsReportGroup, "", u.Supported, ctx.start.getLine());
						CaptureIdentifier(TUDFname, TUDFname, stmt, ctx.start.getLine(), "()");
					}
					// avoid also capturing this same call in the function_call rule deeper down
					inTUDFCall = true;
					setInTUDFCall = true;
				}

				if (TUDFname.isEmpty()) {
					if (!name.isEmpty()) {
						CaptureIdentifier(nameRaw, name, stmt, ctx.start.getLine());
					}
				}

				if (ctx.LOCAL_ID() != null) {
					String tvName = ctx.LOCAL_ID().getText();
					CaptureIdentifier(tvName, tvName, stmt, ctx.LOCAL_ID().getSymbol().getLine());

					// generate Xref records for tables being read-accessed; this is relevant only to determine dependencies, after uploading into PG
					captureItem("SELECT", tvName.toUpperCase(), stmt, "", u.ObjectReference, ctx.start.getLine());
				}

				String statusPivot = "";
				String statusPivotChk = "";
				String statusPivotChkFmt = "";
				if (ctx.PIVOT() != null) {
					inPivot = true;
					lineNrPivot = ctx.PIVOT().getSymbol().getLine();
					statusPivot = featureSupportedInVersion(SelectPivot, "PIVOT");
					if (statusPivot.equals(u.Supported)) {
						if (u.currentObjectType.equals("VIEW")) {
							statusPivotChk = "VIEW";
							statusPivotChkFmt = ", in CREATE/ALTER VIEW";
						}
						else if (inCTE) {
							statusPivotChk = "CTE";
							statusPivotChkFmt = ", in Common Table Expression";
						}
						else if (inAnsiJoin) {
							statusPivotChk = "JOIN";
							statusPivotChkFmt = ", in join";
						}
						else if (inCommaJoin) {
							statusPivotChk = "JOIN";
							statusPivotChkFmt = ", in join";
						}
					}
					//u.appOutput(u.thisProc()+"chk=["+statusPivotChk+"] statusPivotChkFmt=["+statusPivotChkFmt+"] inCTE=["+inCTE+"] inAnsiJoin=["+inAnsiJoin+"] inCommaJoin=["+inCommaJoin+"] inPivot=["+inPivot+"] ");
				}

				if (ctx.UNPIVOT() != null) {
					String status = featureSupportedInVersion(SelectUnpivot);
					captureItem(SelectUnpivot, "", SelectUnpivot, "", status, ctx.UNPIVOT().getSymbol().getLine());
				}

				if (ctx.JOIN() != null) {
					inAnsiJoin = true;
					String type = "INNER JOIN";
					if (ctx.oj != null) type = ctx.oj.getText().toUpperCase() + " OUTER JOIN";
					else if (ctx.cj != null) type = "CROSS JOIN";
					captureItem(type, "", DMLReportGroup, type, u.Supported, ctx.JOIN().getSymbol().getLine());

					if (inPivot) {
						// we never seem to get here? regardless of what side of the join PIVOT is located, we always go through the '(ctx.PIVOT() != null' branch above
						statusPivot = featureSupportedInVersion(SelectPivot, "PIVOT");
						if (statusPivot.equals(u.Supported)) {
							statusPivotChk = "JOIN";
							statusPivotChkFmt = ", in join";
						}
					}
				}

				if (ctx.lj != null) {
					String type = ctx.lj.getText().toUpperCase() + " APPLY";
					String status = featureSupportedInVersion(LateralJoin, type);
					String inDML = "";
					if (hasParent(ctx.parent,"update_statement")) inDML = ", in UPDATE";
					else if (hasParent(ctx.parent,"delete_statement")) inDML = ", in DELETE";
					captureItem(type+" ("+LateralJoin.toLowerCase()+")"+inDML, "", LateralJoin, type, status, ctx.lj.getLine());
				}

				if (ctx.colon_colon() != null) {
					String funcName = u.normalizeName(ctx.function_call().getText());
					String status = featureSupportedInVersion(ColonColonFunctionCall);
					if (!status.equals(u.Supported)) {
						if (u.rewrite) {
							String rewriteText = "";
							if (addRewrite(ColonColonFunctionCall, funcName, u.rewriteTypeReplace, rewriteText, ctx.colon_colon().start.getLine(), ctx.colon_colon().start.getCharPositionInLine(), ctx.colon_colon().stop.getLine(), ctx.colon_colon().stop.getCharPositionInLine(), ctx.colon_colon().start.getStartIndex(), ctx.colon_colon().stop.getStopIndex()))
								status = u.Rewritten;
						}
						else {
							addRewrite(ColonColonFunctionCall);
						}
					}
					captureItem(ColonColonFunctionCall, funcName, ColonColonFunctionCall, "", status, ctx.colon_colon().start.getLine());
				}

				visitChildren(ctx);

				// perform capture for PIVOT
				if (statusPivot.equals(u.Supported)) {
					if (!statusPivotChk.isEmpty()) {
						statusPivot = featureSupportedInVersion(SelectPivot, statusPivotChk);
					}
					if (statusPivot.equals(u.Supported)) {
						statusPivotChkFmt = "";
					}
					//u.appOutput(u.thisProc()+"statusPivot=["+statusPivot+"] statusPivotChk=["+statusPivotChk+"] statusPivotChkFmt=["+statusPivotChkFmt+"] inAnsiJoin=["+inAnsiJoin+"] inPivot=["+inPivot+"] lineNrPivot=["+lineNrPivot+"] ");
					captureItem(SelectPivot+statusPivotChkFmt, "", SelectPivot, "", statusPivot, lineNrPivot);
				}

				if (ctx.JOIN() != null) {
					inAnsiJoin = false;
				}
				if (ctx.PIVOT() != null) {
					inPivot= false;
				}
				if (setInTUDFCall) inTUDFCall = false;
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitTable_sources(TSQLParser.Table_sourcesContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				for (TerminalNode t : ctx.COMMA()) {
					captureItem("INNER JOIN", "", DMLReportGroup, "INNER JOIN WITH COMMA", u.Supported, t.getSymbol().getLine());
				}
				if (ctx.COMMA().size() > 0) inCommaJoin = true;
				visitChildren(ctx);
				if (ctx.COMMA().size() > 0) inCommaJoin = false;
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitJoin_hint(TSQLParser.Join_hintContext ctx) {
				String hint = ctx.getText().toUpperCase();
				String status = featureSupportedInVersion(JoinHint, hint);
				captureItem(JoinHint+ " " + hint, "", JoinHint, hint, status, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitTable_hint(TSQLParser.Table_hintContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				// A complicating factor for table hints is that the WITH keyword is mandatory these days in SQL Server,
				// but customer applications running older or backward-compatible version may still use the old syntax without WITH
				// therefore, extra tests are needed to determine if it is really a table hint

				// the column list for an INSERT is parsed as table hints, so skip that case
				if (parentRuleName(ctx.parent,2).equals("insert_statement")) {
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"skipping INSERT collist - not a table hint", u.debugPtree);
				}
				else {
					String hint = u.encodeIdentifier(ctx.getText().toUpperCase());
					//u.appOutput(u.thisProc()+"hint=["+hint+"] ");
					//column names in a table alias may be parsed as a table hint
					if (! CompassUtilities.getPatternGroup(hint, "^(NOEXPAND|INDEX|NOEXPANDINDEX|FORCESEEK|SERIALIZABLE|SNAPSHOT|SPATIAL_WINDOW_MAX_CELLS|FORCESCAN|HOLDLOCK|NOLOCK|NOWAIT|PAGLOCK|READCOMMITTED|READCOMMITTEDLOCK|READPAST|READUNCOMMITTED|REPEATABLEREAD|ROWLOCK|TABLOCK|TABLOCKX|UPDLOCK|XLOCK)\\b", 1).isEmpty()) {
						String w = CompassUtilities.getPatternGroup(hint, "^([A-Z ]+)", 1).trim();
						String w2 = u.stripEnclosingBrackets(hint.substring(w.length()));
						String hintChk = w;
						if (hint.startsWith("INDEX") || hint.startsWith("NOEXPANDINDEX")) {
							hint = u.collapseWhitespace(u.applyPatternAll(hint, "INDEX\\(\\d+\\)", "INDEX(index id)"));
							hint = u.collapseWhitespace(u.applyPatternAll(hint, "INDEX\\(\\D["+u.identifierChars+"]*\\)", "INDEX(index name)"));
							hint = u.collapseWhitespace(u.applyPatternAll(hint, "INDEX\\(\\D.*\\)", "INDEX(index name)"));
							hint = u.collapseWhitespace(u.applyPatternAll(hint, "INDEX\\=\\D["+u.identifierChars+"]*", "INDEX=index name"));
							hint = u.collapseWhitespace(u.applyPatternAll(hint, "INDEX\\=\\D.*", "INDEX=index name"));
							w = hint;
							hintChk = hint;

							// use the following special values for testing against the .cfg file (no values listed there at this time)
							if (hint.equalsIgnoreCase("INDEX(index id)")) hintChk = "INDEX_ID";
							else if (hint.equalsIgnoreCase("INDEX(index name)")) hintChk = "INDEX_NAME";
							else if (hint.equalsIgnoreCase("INDEX=index name")) hintChk = "INDEX_NAME";
						}
						//w = u.collapseWhitespace(u.applyPatternAll(w, "(FORCE|DISABLE|HINT|PLAN|GROUP|UNION|JOIN|NOEXPAND|VIEWS|FOR|PARAMETERIZATION)", " $1 "));
						String status = featureSupportedInVersion(TableHint, hintChk);
						captureItem(TableHint+ " " + w, w2, TableHint, u.decodeIdentifier(hint), status, ctx.start.getLine());
					}
					else {
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"not a table hint: ["+hint+"] ", u.debugPtree);
					}
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitOption(TSQLParser.OptionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String hint = getTextSpaced(ctx).toUpperCase().trim();
				hint = CompassUtilities.getPatternGroup(hint, "^([A-Z ]+)", 1).trim();
				if (hint.startsWith("TABLE HINT")) {
					// already handled elsewhere
				}
				else {
					String status = featureSupportedInVersion(QueryHint, hint);
					captureItem(QueryHint+ " " + hint, "", QueryHint, hint, status, ctx.start.getLine());
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitChar_string(TSQLParser.Char_stringContext ctx) {
				String s = ctx.getText();
				if (s.charAt(0) == '"') {
					captureDoubleQuotedString(s, ctx.start.getLine(), ctx.start.getCharPositionInLine(), ctx.stop.getLine(), ctx.stop.getCharPositionInLine(), ctx.start.getStartIndex(), ctx.stop.getStopIndex());
				}
				if (s.contains("\\\n")) {  // note that CRLF has been changed to LF by now
					String status = featureSupportedInVersion(LineContinuationChar,"CHAR");
					captureItem(LineContinuationChar + " in character string", "", LineContinuationChar, "", status, ctx.start.getLine());
				}
				if (s.contains("\\\n")) {  // note that CRLF has been changed to LF by now
					String status = featureSupportedInVersion(LineContinuationChar,"CHAR");
					captureItem(LineContinuationChar + " in character string", "", LineContinuationChar, "", status, ctx.start.getLine());
				}

				// check for sqlcmd variables. Here only checking for the exact string. May not report all occurrences in a string
				if (s.contains("$(")) {
					String status = featureSupportedInVersion(sqlcmdVariable);
					s = u.stripStringQuotes(s).trim();
					// using a regex directly is slower
					boolean found = false;
					if (s.startsWith("$(")) {
						if (s.endsWith(")")) {
							if (!u.getPatternGroup(s, "^(\\$\\(\\w+\\))$", 1).isEmpty()) {
				 				// todo: highlight predefined sqlcmd variables?
				 				captureItem(sqlcmdVariable + " " + s, "", sqlcmdReportGroup, "", status, ctx.start.getLine());
				 				found = true;
							}
						}
					}

					if (!found) {
						// check for a variable that was defined in :setvar
						if (sqlcmdVars.size() > 0) {
							for (String v: sqlcmdVars) {
								String var = u.getPatternGroup(s, "(\\$\\("+v+"\\))", 1); // case-insensitive by default
								if (!var.isEmpty()) {
									captureItem(sqlcmdVariable + " " + var, "", sqlcmdReportGroup, "", status, ctx.start.getLine());
								}
							}
						}
					}

				}

				//visitChildren(ctx); // has no children
				return null;
			}

			@Override public String visitConstant(TSQLParser.ConstantContext ctx) {
				if (ctx.MONEY() != null) {
					captureItem(MoneyLiteral+" with "+ctx.getText().charAt(0), "", MoneyLiteral, "", u.Supported, ctx.start.getLine());
				}
				visitChildren(ctx);
				return null;
			}

			@Override public String visitWith_rollup_cube(TSQLParser.With_rollup_cubeContext ctx) {
				String status = featureSupportedInVersion(RollupCubeOldSyntax);
				String s = ctx.getText().toUpperCase();
				s = s.replaceFirst("WITH", "WITH ");
				// reporting here as WithRollupCubeOldSyntax for better clarity. But we cannot change the .cfg file so keeping RollupCubeOldSyntax there
				captureItem(WithRollupCubeOldSyntax, s, RollupCubeOldSyntax, s, status, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitExecute_body(TSQLParser.Execute_bodyContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				execute_statement_argParamCount = 0;

				String return_status = "";
				if (ctx.return_status != null) {
					return_status = ", with return status";
				}

				String proc_var = "";
				if (ctx.proc_var != null) {
					proc_var = ", name in variable";
				}

				String execImm = "";
				if (ctx.LR_BRACKET() != null) {
					execImm = "execimm";
				}

				String procName = "";
				if (ctx.func_proc_name_server_database_schema() != null) {
					procName = u.normalizeName(ctx.func_proc_name_server_database_schema().getText());
					execute_statement_procName = procName;
					if (!lookupSUDF(procName).isEmpty()) {
						String status = featureSupportedInVersion(ExecuteSQLFunction);
						captureItem(ExecuteSQLFunction, procName, ExecuteSQLFunction, "", status, ctx.start.getLine());
					}
				}

				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"EXECUTE: "+ ctx.getText()+", procName=["+procName+"] return_status=["+return_status+"] proc_var=["+proc_var+"] execImm=["+execImm+"] ", u.debugPtree);

				if (!procName.isEmpty()) {
					captureSystemproc(procName, return_status, u.grammarRuleNames[ctx.getRuleIndex()], ctx.proc_version, ctx.start.getLine(), ctx.execute_statement_arg());
				}

				if (!proc_var.isEmpty()) {
					String procStatus = featureSupportedInVersion(ProcExecAsVariable);
					captureItem("EXECUTE procedure"+proc_var+return_status, ctx.proc_var.getText(), ProcExecAsVariable, "", procStatus, ctx.start.getLine());
					//String args= getTextSpaced(ctx.execute_statement_arg());
					//u.appOutput(u.thisProc()+"EXECUTE procedure"+proc_var+return_status+":" + args);
				}

				if (!execImm.isEmpty()) {
					captureItem("EXECUTE(string)", "", DynamicSQL, "", u.Supported, ctx.start.getLine());

					List<TSQLParser.Execute_var_stringContext> arg = ctx.execute_var_string();
					String stringArg = "";
					boolean stringArgFound = true;
					for (int i=0; i<arg.size(); i++) {
						String a = arg.get(i).getText();
						if (a.charAt(0) == '@') {
							stringArgFound = false;
							break;
						}
						String quote = u.getPatternGroup(a, "(['\"])", 1);
						a = u.stripStringQuotes(a);						
						a = u.applyPatternAll(a, quote+quote, quote);
						stringArg += a;
					}			
					// Don't process exec-imm strings that have '?' placeholders -- test can be improved by first removing any strings, but this is less obvious than it looks
					String hasPlaceholder = u.getPatternGroup(stringArg, "(\\?)", 1);						
					if (u.debugging) u.dbgOutput("capturing dynamic SQL query: EXECUTE(): stringArgFound=["+stringArgFound+"]  stringArg=["+stringArg+"] hasPlaceholder=["+hasPlaceholder+"] ", u.debugDynamicSQL);					
					if (stringArgFound && (!stringArg.trim().isEmpty()) && (hasPlaceholder.isEmpty())) {
						u.dynamicSQLBuffer.add(u.dynamicSQLBatchLine+arg.get(0).start.getLine()+","+u.batchNrInFile+","+u.lineNrInFile+","+(u.currentObjectType + " " + u.currentObjectName).trim()+",EXECUTE()");
						u.dynamicSQLBuffer.add(stringArg);
						u.dynamicSQLBuffer.add("go");
						u.dynamicSQLNrStmts++;
					}
					else {
						// if we cannot figure out the query in the argument, mark as review manually
						String DynamicSQLStatus = featureSupportedInVersion(DynamicSQL);
						captureItem(DynamicSQLEXECStringReview, "", DynamicSQL, "", DynamicSQLStatus, ctx.start.getLine());
					}
				}

				// process any options
				captureExecOptions(procName, ctx.execute_option(), ctx.WITH(), ctx.start.getLine());

				List<TSQLParser.Execute_var_string_optionContext> execVarOptions = ctx.execute_var_string_option();
				for (int i = 0; i <execVarOptions.size(); i++) {
					String optionRaw = execVarOptions.get(i).getText().toUpperCase();
					String optionRawSpaced = getTextSpaced(execVarOptions.get(i)).toUpperCase().trim();
					String option = optionRaw;
					String optionValue = "";
					String optionFmt = "";
					String execArg2 = "";
					if (optionRawSpaced.startsWith("AT ")) {
						if (optionRawSpaced.startsWith("AT DATA_SOURCE")) {
							option = "AT DATA_SOURCE";
							optionValue = optionRaw.substring("ATDATA_SOURCE".length());
							optionFmt = u.escapeHTMLChars(" <data-source>");
						}
						else {
							option = "AT";
							optionValue = optionRawSpaced.substring("AT ".length());
							optionFmt = u.escapeHTMLChars(" <linked-server>");
						}
						if (ctx.execarg2 != null) {
							execArg2 = ", ";
							String execArg2Str = ctx.execarg2.getText();
							if (execArg2Str.charAt(0) == '@') {
								execArg2 += "@var";
								if (ctx.execarg2output != null) {
									execArg2 += " OUTPUT";
								}
							}
							else {
								if (getIntegerConstant(execArg2Str) == null) {
									execArg2 += "expression";
								}
								else {
									execArg2 += "number";
								}
							}
						}
					}
					else if (optionRawSpaced.startsWith("AS ")) {
						if (optionRawSpaced.startsWith("AS LOGIN")) {
							option = "AS LOGIN";
							optionValue = optionRaw.substring("ASLOGIN=".length());
						}
						else {
							option = "AS USER";
							optionValue = optionRaw.substring("ASUSER=".length());
						}
					}
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"execVarOptions i=["+i+"] option=["+option+"]  optionRawSpaced=["+optionRawSpaced+"]", u.debugPtree);
					String status = featureSupportedInVersion(ExecStringOptions,option);
					captureItem("EXECUTE(string"+execArg2+"), "+option+optionFmt, optionValue, DynamicSQL, option, status, ctx.start.getLine());
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}


			@Override public String visitExecute_body_batch(TSQLParser.Execute_body_batchContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				execute_statement_argParamCount = 0;

				String return_status = "";
				String procName = "";
				if (ctx.func_proc_name_server_database_schema() != null) {
					//procName = u.normalizeName(ctx.func_proc_name_server_database_schema().getText());
					procName = ctx.func_proc_name_server_database_schema().getText();
					execute_statement_procName = procName;
				}

				TSQLParser.Execute_statement_argContext arg0 = null;
				if (ctx.execute_statement_arg().size() > 0) arg0 = ctx.execute_statement_arg(0);
				captureSystemproc(procName, return_status, u.grammarRuleNames[ctx.getRuleIndex()], null, ctx.start.getLine(), arg0);

				captureExecOptions(procName, ctx.execute_option(), ctx.WITH(), ctx.start.getLine());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			private void captureSystemproc(String procName, String return_status, String rule, Token proc_version, int lineNr, TSQLParser.Execute_statement_argContext arg) {
				String section = ProceduresReportGroup;
				String sysProcName = "";
				String argMsg = "";
				String firstStmt = "";
				if (rule.equals("execute_body_batch")) firstStmt = " (without EXECUTE keyword)";

				CaptureIdentifier(procName, procName, "EXECUTE procedure", lineNr);

				procName = u.getObjectNameFromID(procName).toLowerCase();
				if (procName.equals("sp_executesql") ||
				    procName.equals("sp_prepare") ||
				    procName.equals("sp_prepexec") ||
				    procName.equals("sp_cursorprepare") ||
				    procName.equals("sp_cursorprepexec"))  {
					// todo: also report OUTPUT parameters for sp_executesql?
					//sysProcName = " sp_executesql";
					sysProcName = " " + procName;
					section = DynamicSQL;
					captureItem("EXECUTE"+sysProcName+firstStmt+return_status, sysProcName, DynamicSQL, "", u.Supported, lineNr);

					// see if the dynamic SQL argument is a string literal so that we can analyze it
					boolean stringArgFound = false;
					String stringArg = "";

					int argNo = 0;
					if (procName.equals("sp_executesql")) argNo = 1;
					if (procName.equals("sp_prepare")||procName.equals("sp_prepexec")||procName.equals("sp_cursorprepare")) argNo = 3;
					if (procName.equals("sp_cursorprepexec")) argNo = 4;
					assert (argNo > 0) : "argNo must be > 0; procName=["+procName+"] ";

					String a = getArgProcExec(argNo, procName, arg);
					if (!a.isEmpty()){
						if (a.charAt(0) != '@') {
							stringArgFound = true;
							stringArg = a;
						}
					}

					if (stringArgFound) {
						String quote = u.getPatternGroup(stringArg, "(['\"])", 1);
						stringArg = u.stripStringQuotes(stringArg);
						stringArg = u.applyPatternAll(stringArg, quote+quote, quote);
						//u.appOutput("capturing dynamic SQL query: "+procName+": stringArg=["+stringArg+"] ");
						if (u.debugging) u.dbgOutput("capturing dynamic SQL query: "+procName+": stringArg=["+stringArg+"] ", u.debugDynamicSQL);
						u.dynamicSQLBuffer.add(u.dynamicSQLBatchLine+arg.start.getLine()+","+u.batchNrInFile+","+u.lineNrInFile+","+(u.currentObjectType + " " + u.currentObjectName).trim()+","+procName);
						u.dynamicSQLBuffer.add(stringArg);
						u.dynamicSQLBuffer.add("go");
						u.dynamicSQLNrStmts++;
					}
					else {
						// if we cannot figure out the query in the argument, mark as review manually
						String DynamicSQLStatus = featureSupportedInVersion(DynamicSQL);
						captureItem(DynamicSQLEXECSPReview, "", DynamicSQL, "", DynamicSQLStatus, lineNr);
					}
				}
				else {
					if ((procName.startsWith("sp_")) || (procName.startsWith("xp_")))  {
						// is this a system sproc?
						sysProcName = procName;
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"sysProcName=["+sysProcName+"]  ", u.debugPtree);
						if (featureExists(SystemStoredProcs, sysProcName)) {
							if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"featureExists: "+ " sysProcName=["+sysProcName+"]  ", u.debugPtree);
							section = SystemStoredProcs;
							String procStatus = featureSupportedInVersion(SystemStoredProcs, sysProcName);

							if (procStatus.equals(u.Supported)) {
								String argN = cfg.featureExistsArg(sysProcName);
								if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"sysProcName=["+sysProcName+"] argN=["+argN+"] ", u.debugPtree);

								if (!argN.isEmpty()) {
									String statusArgN = u.NotSupported;
									int argNum = Integer.parseInt(argN.substring(3));
									String argNValue = u.stripStringQuotes(getArgProcExec(argNum, procName, arg)).trim().toUpperCase();
									if (argNValue.isEmpty()) argNValue="NULL_OR_BLANK"; 						
									if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"validating arg=["+argN+"] argNValue=["+argNValue+"] for system proc=["+sysProcName+"]", u.debugPtree);
									if (!argNValue.isEmpty()) {
										if (argNValue.charAt(0) != '@') {
											statusArgN = featureArgSupportedInVersion(procName, argN, argNValue);
											argMsg = ", '"+argNValue+"'";
											if (argNValue.equals("NULL_OR_BLANK")) {
												argMsg = ", no value for argument #"+argNum;
											}	
										}
										else {
											statusArgN = u.ReviewManually;
											argMsg = ", @var";
										}
									}
									procStatus = statusArgN;
									if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"validating arg=["+argN+"] argNValue=["+argNValue+"] for system proc=["+sysProcName+"]: statusArgN=["+statusArgN+"] ", u.debugPtree);
								}
							}
							captureItem("EXECUTE procedure "+sysProcName+argMsg+firstStmt+return_status, procName, SystemStoredProcs, sysProcName, procStatus, lineNr);

							// check extended property object types
							if (sysProcName.equals("sp_addextendedproperty") || sysProcName.equals("sp_updateextendedproperty") || sysProcName.equals("sp_dropextendedproperty")) {
								if (procStatus.equals(u.Supported)) {
									int nrArgs = getArgProcExec(sysProcName, arg);
									if (nrArgs > 0) {
										// named arguments
										String exProp = getArgProcExec("@level0type", sysProcName, arg);
										captureExtendedPropertyType(exProp, sysProcName, SystemStoredProcs, procStatus, lineNr);

										exProp = getArgProcExec("@level1type", sysProcName, arg);
										captureExtendedPropertyType(exProp, sysProcName, SystemStoredProcs, procStatus, lineNr);

										exProp = getArgProcExec("@level2type", sysProcName, arg);
										captureExtendedPropertyType(exProp, sysProcName, SystemStoredProcs, procStatus, lineNr);
									}
									else {
										// unnamed arguments
										int argNr = 3;
										if (sysProcName.equalsIgnoreCase("sp_dropextendedproperty")) argNr--;

										String exProp = getArgProcExec(argNr, sysProcName, arg);
										captureExtendedPropertyType(exProp, sysProcName, SystemStoredProcs, procStatus, lineNr);

										argNr += 2;
										exProp = getArgProcExec(argNr, sysProcName, arg);
										captureExtendedPropertyType(exProp, sysProcName, SystemStoredProcs, procStatus, lineNr);

										argNr += 2;
										exProp = getArgProcExec(argNr, sysProcName, arg);
										captureExtendedPropertyType(exProp, sysProcName, SystemStoredProcs, procStatus, lineNr);
									}
								}
							}
						}
						else {
							//not found as a system proc, so it's a user-defined proc
							sysProcName = "";
						}
					}
				}

				if ((!procName.isEmpty()) && (sysProcName.isEmpty())) {
					// proc versioning?
					if (proc_version != null) {
						String procStatus = featureSupportedInVersion(ProcVersionExecute);
						captureItem("EXECUTE proc;version"+firstStmt+return_status, procName+";"+proc_version.getText(), ProcVersionExecute, "", procStatus, lineNr);
					}
					else {
						captureItem("EXECUTE procedure"+firstStmt+return_status, procName, section, "", u.Supported, lineNr);
					}
				}
			}

			private void captureExtendedPropertyType(String exProp, String procName, String group, String status, int lineNr) {
				if (exProp.equalsIgnoreCase("DEFAULT")) return;
				if (exProp.equalsIgnoreCase("DEFAULT")) return;
				if (exProp.equalsIgnoreCase("NULL")) return;
				exProp = u.stripStringQuotes(exProp).toUpperCase();
				if (exProp.isEmpty()) return;
				String varName = "";
				String typeStatus = featureSupportedInVersion(ExtendedPropType, exProp);
				if (exProp.charAt(0) == '@') {
					typeStatus = u.ReviewManually;
					varName = exProp;
					exProp = "in @var";
				}
				//u.appOutput(u.thisProc()+"exProp=["+exProp+"] procName=["+procName+"] typeStatus=["+typeStatus+"] ");
				if (!typeStatus.equals(u.Supported)) {
					captureItem("Extended property type "+exProp+" in " + procName, varName, group, "", typeStatus, lineNr);
				}
				return;
			}

			private Integer getArgProcExec(String procName, TSQLParser.Execute_statement_argContext arg) {
				// return the #args: >0 when using named args; 0 when using positional args or no args at all
				// assuming either fully named or unnamed notation, but this is not verified
				int nrArgs = 0;
				if (arg.execute_statement_arg_named().size() > 0) {
					nrArgs = arg.execute_statement_arg_named().size() ;
				}
				return nrArgs;
			}
			private String getArgProcExec(int argNo, String procName, TSQLParser.Execute_statement_argContext arg) {
				// if argNo is > #actual args, empty string is returned
				assert (argNo > 0) : "argNo must be > 0";

				String result = "";
				if (u.debugging) u.dbgOutput("argNo=["+argNo+"] procName=["+procName+"] arg=["+arg+"] ", u.debugDynamicSQL||u.debugPtree);
				if (arg != null) {
					if (u.debugging) u.dbgOutput("arg=["+arg.getText()+"] ", u.debugDynamicSQL);

					if (arg.execute_statement_arg_unnamed() != null) {
						//u.appOutput(u.thisProc()+"arg unnamed=["+arg.execute_statement_arg_unnamed().getText()+"]");
						if (argNo == 1) result = arg.execute_statement_arg_unnamed().getText();
						else {
							if (u.debugging) u.dbgOutput("recursive call with argNo-1=["+(argNo-1)+"] ", u.debugDynamicSQL||u.debugPtree);
							result = getArgProcExec(argNo-1, procName, arg.execute_statement_arg());
						}
					}
					else if (arg.execute_statement_arg_named().size() > 0) {
						//u.appOutput(u.thisProc()+"arg named=["+arg.execute_statement_arg_named(0).getText()+"] ");
						if (arg.execute_statement_arg_named().size() > argNo-1) {
							if (arg.execute_statement_arg_named(argNo-1).execute_parameter() != null) {  // cannot be null, but anyway...
								result = arg.execute_statement_arg_named(argNo-1).execute_parameter().getText();
							}
						}
					}
					else {
						// something's wrong
						assert false : "unexpected branch";
					}
				}
				if (u.debugging) u.dbgOutput("result=["+result+"] ", u.debugDynamicSQL);
				return result;
			}

			private String getArgProcExec(String argName, String procName, TSQLParser.Execute_statement_argContext arg) {
				// get the value of a named argument; assuming all args in named notation but this is not verified
				String result = "";
				if (u.debugging) u.dbgOutput("argName=["+argName+"] procName=["+procName+"] ", u.debugPtree);
				if (arg != null) {
					if (u.debugging) u.dbgOutput("arg=["+arg.getText()+"] ", u.debugPtree);

					if (arg.execute_statement_arg_named().size() > 0) {
						for (int i = 0; i <arg.execute_statement_arg_named().size(); i++) {
							 String name  = arg.execute_statement_arg_named(i).name.getText();
							 String value = arg.execute_statement_arg_named(i).execute_parameter().getText();
							 if (u.debugging) u.dbgOutput(u.thisProc()+"name=["+name+"] value=["+value+"] ", u.debugPtree);
							 if (name.equalsIgnoreCase(argName)) {
							 	result = value;
							}
						}
					}
					else {
						// something's wrong
						assert false : "unexpected branch";
					}
				}
				if (u.debugging) u.dbgOutput("result=["+result+"] ", u.debugDynamicSQL);
				return result;
			}

			private void captureExecOptions(String procName, List<TSQLParser.Execute_optionContext> execOptions, TerminalNode with_keywd, int lineNr) {
				for (int i = 0; i <execOptions.size(); i++) {
					String option = execOptions.get(i).getText().toUpperCase();
					if (option.startsWith("RESULTSETS")) {
						option = option.replaceFirst("SETS", " SETS ").trim();
						if (option.startsWith("RESULT SETS (")) {
							option = "RESULT SETS(schema)";
						}
					}
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"execOptions i=["+i+"] option=["+option+"]", u.debugPtree);
					String status = featureSupportedInVersion(ExecProcedureOptions,option);

					String s = "EXECUTE procedure, WITH "+option;
					if (option.equals("RECOMPILE") && (execOptions.size() == 1)) {
						// rewrite RECOMPILE, but only when no other options are specified
						if (!status.equals(u.Supported)) {
							if (u.rewrite) {
								// need to get the WITH token, which is one level up
								String origText = with_keywd.getText() + " " + execOptions.get(i).getText();
								String rewriteText = "";
								if (addRewrite(s, origText, u.rewriteTypeReplace, rewriteText, with_keywd.getSymbol().getLine(), with_keywd.getSymbol().getCharPositionInLine(), execOptions.get(i).stop.getLine(), execOptions.get(i).stop.getCharPositionInLine(), with_keywd.getSymbol().getStartIndex(), execOptions.get(i).stop.getStopIndex()))
									status = u.Rewritten;
							}
							else {
								addRewrite(s);
							}
						}
					}
					captureItem(s, procName, ExecProcedureOptions, option, status, lineNr);
				}
			}

			@Override public String visitSet_special(TSQLParser.Set_specialContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				if (ctx.set_on_off_option().size() > 0) {
					List<TSQLParser.Set_on_off_optionContext> options = ctx.set_on_off_option();
					for (TSQLParser.Set_on_off_optionContext optionX : options) {
						String option = optionX.getText().toUpperCase();
						String on_off = ctx.on_off().getText().toUpperCase();
						captureSEToption("SET "+option, on_off, "", ctx.start.getLine());
						detectSetQuotedIdentifier(pass, ctx, option);
					}
					if (options.size() > 1) {
						String status = featureSupportedInVersion(SetMultipleOptions);
						captureItem(SetMultipleOptions+" ("+options.size()+")", "", SetMultipleOptions, "", status, ctx.start.getLine());
					}
				}
				else if (ctx.IDENTITY_INSERT() != null) {
					String tableNameRaw = ctx.table_name().getText().toUpperCase();
					String tableName = u.normalizeName(tableNameRaw);					
					captureSEToption("SET IDENTITY_INSERT", ctx.on_off().getText().toUpperCase(), "", ctx.start.getLine(), tableName);
					CaptureIdentifier(tableNameRaw, tableName, "SET IDENTITY_INSERT", ctx.start.getLine());					
				}
				else if (ctx.ROWCOUNT() != null) {
					String setValue = "";
					String setValueFmt = "";
					String setValueOrig = "";
					if (ctx.DECIMAL() != null) {
						setValueOrig = ctx.DECIMAL().getText();
						setValueFmt = setValueOrig;
						if (!CompassUtilities.getPatternGroup(setValueOrig, "^[\\+\\-]*([0]+)$", 1).isEmpty()) {
							setValue = "0";
							setValueFmt = "0";
						}
						else {
							setValue = cfgNonZero;
							setValueFmt = u.escapeHTMLChars("<number>");
						}
					}
					else {
						setValueOrig = ctx.LOCAL_ID().getText();
						setValueFmt = "@v";
						setValue = cfgVariable;
					}
					captureSEToption("SET ROWCOUNT", setValue, setValueFmt, ctx.start.getLine(), setValueOrig);
				}
				else if (ctx.TEXTSIZE() != null) {
					String setValueFmt = u.escapeHTMLChars("<number>");
					String setValueOrig = ctx.DECIMAL().getText();
					captureSEToption("SET TEXTSIZE", setValueOrig, setValueFmt, ctx.start.getLine(), setValueOrig);
				}
				//else if ((ctx.STATISTICS() != null) || (ctx.BABELFISH_STATISTICS() != null)) {
				else if ((ctx.stats != null)) {
					String stats = ctx.stats.getText().toUpperCase();
					List<TSQLParser.Set_statistics_keywordContext> options = ctx.set_statistics_keyword();
					String setValue = ctx.on_off().getText().toUpperCase();
					if (options.size() > 0) {
						for (TSQLParser.Set_statistics_keywordContext optionX : options) {
							captureSEToption("SET "+stats, optionX.getText().toUpperCase() + " " + setValue, "", ctx.start.getLine());
						}
						if (options.size() > 1) {
							String status = featureSupportedInVersion(SetMultipleOptions);
							captureItem(SetMultipleOptions+" ("+options.size()+")", "", SetMultipleOptions, "STATISTICS", status, ctx.start.getLine());
						}
					}
				}
				else if (ctx.OFFSETS() != null) {
					List<TSQLParser.Set_offsets_keywordContext> options = ctx.set_offsets_keyword();
					String setValue = ctx.on_off().getText().toUpperCase();
					if (options.size() > 0) {
						for (TSQLParser.Set_offsets_keywordContext optionX : options) {
							captureSEToption("SET OFFSETS", optionX.getText().toUpperCase() + " " + setValue, "", ctx.start.getLine());
						}
						if (options.size() > 1) {
							String status = featureSupportedInVersion(SetMultipleOptions);
							captureItem(SetMultipleOptions+" ("+options.size()+")", "", SetMultipleOptions, "OFFSETS", status, ctx.start.getLine());
						}
					}
				}
				else if (ctx.id_set != null) {
					String option = ctx.id_set.getText().toUpperCase();
					String feature = "SET " + option;
					String setValue = "";
					String setValueFmt = "";
					if (ctx.id_val != null) {
						setValue = ctx.id_val.getText().toUpperCase();
					}
					else if (ctx.constant_LOCAL_ID() != null) {
						setValue = ctx.constant_LOCAL_ID().getText().toUpperCase();
					}
					else {
						setValue = ctx.on_off().getText().toUpperCase();
					}
					String setValueOrig = setValue;

					if (setValue.charAt(0) == '@') {
						setValueFmt = "@v";
						setValue = cfgVariable;
					}
					else {
						Integer exprInt = getIntegerConstant(setValue);
						if (exprInt != null) {
							if ((exprInt == -1) || (exprInt == 0)) {
								setValueFmt = setValue;
							}
							else {
								setValueFmt = u.escapeHTMLChars("<number>");
								setValue = cfgNonZero;
							}
						}
					}

					if (option.equals("DATEFORMAT") || option.equals("DEADLOCK_PRIORITY")) {
						setValue = setValue.toLowerCase();
					}
					else if (option.equals("LANGUAGE")) {
						setValue = formatRemoveStringQuotes(setValue.toLowerCase());
					}
					captureSEToption(feature, setValue, setValueFmt, ctx.start.getLine(), setValueOrig);
				}
				else if (ctx.ISOLATION() != null) {
					String feature = SetXactIsolationLevel;
					String setValue = ctx.getText().toUpperCase();
					setValue = setValue.substring(setValue.indexOf("LEVEL")+5);
					setValue = setValue.replaceFirst("READ", " READ ").trim();
					captureSEToption(feature, setValue, "", ctx.start.getLine());
				}
				else if (ctx.xml_modify_method() != null) {
					// do nothing
				}
				else {
					if (u.devOptions) {
						u.appOutput(u.thisProc()+"unexpected branch: ctx=["+ctx.getText()+"] ");
						u.printStackTrace();
					}
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			private void captureSEToption(String feature, String setValueTest, String setValueFmt, int lineNr) {
				captureSEToption(feature, setValueTest, setValueFmt, lineNr, "");
			}
			private void captureSEToption(String feature, String setValueTest, String setValueFmt, int lineNr, String itemDetail) {
				if (itemDetail.isEmpty())  itemDetail  = setValueTest;
				if (setValueFmt.isEmpty()) setValueFmt = setValueTest;
				if (!featureExists(feature)) {
					// something is missing from the .cfg file
					u.appOutput("Feature ["+feature+"] not found in .cfg file - assuming not supported");
					captureItem(formatItemDisplay(feature+" "+setValueFmt), itemDetail, SetOptions, feature, u.NotSupported, lineNr);
					return;
				}
				if (!CompassUtilities.getPatternGroup(setValueTest, "^("+u.varPattern+")$", 1).isEmpty()) {
					setValueTest = cfgVariable;
				}
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"feature=["+feature+"] setValueTest=["+setValueTest+"] ", u.debugPtree);
				String status = featureSupportedInVersion(feature, setValueTest);
				if (setValueTest.equals(cfgVariable)) {
					if (status.equals(u.NotSupported)) {
						if (!feature.equals("SET ROWCOUNT")) {
							status = u.ReviewManually;
						}
					}
				}
				captureItem(formatItemDisplay(feature+" "+setValueFmt), itemDetail, SetOptions, feature, status, lineNr);
			}

			@Override public String visitSet_statement(TSQLParser.Set_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if (ctx.LOCAL_ID() != null) {
					String varName = ctx.LOCAL_ID().getText().toUpperCase();

					String SrvInfoTag = "SMO_DDL SQL Server info:";  // do not change this string: must match SMO_DDL.ps1 script
					String SrvInfoVarName = "@srvinfo";              // do not change this string: must match SMO_DDL.ps1 script
					if (varName.equalsIgnoreCase(SrvInfoVarName) && (ctx.EQUAL() != null) && (ctx.expression() != null)) {
						// first, intercept SMO-generated resource info about the source SQL Server
						String s = u.stripStringQuotes(ctx.expression().getText());
						if (s.startsWith(SrvInfoTag)) {
							s = s.substring(SrvInfoTag.length());
							List<String> parts = new ArrayList<>(Arrays.asList(s.split("=")));
							if (parts.size() != 2) {
								u.appOutput("Ignoring unexpected format for ["+SrvInfoTag+"] : ["+s+"]");
							}
							else {
								u.SQLSrvResourcesDetail.put(parts.get(0).trim(), parts.get(1).trim()+u.miscDelimiter+u.currentAppName);
							}
							// do not include these statements in the Compass report
							return null;
						}
					}

					if (ctx.CURSOR() != null) {
						String status = featureSupportedInVersion(CursorVariables);
						captureItem("CURSOR variable assignment", varName, CursorVariables, "SET", status, ctx.start.getLine());
					}
					else {
						String op = "=";
						if (ctx.assignment_operator() != null) {
							op = ctx.assignment_operator().getText();
						}
						captureItem("Variable assignment by SET @v "+op, varName, MiscReportGroup, "variable assignment set", u.Supported, ctx.start.getLine());

						// check for numeric-as-date
						checkNumericDateVarAssign(varName, ctx.expression(), ctx.start.getLine());

						// try to identify @@ERROR codes being checked via intermediate variables
						// assumption is we hit the assignment stmt before the value comparisons
						if ((ctx.expression().getText().equalsIgnoreCase("@@ERROR")) || (ctx.expression().getText().equalsIgnoreCase("ERROR_NUMBER()"))) {
							addAtAtErrorVars(varName);
							//u.appOutput(CompassUtilities.thisProc()+"SET assigning @@ERROR to varName=["+varName+"] x=["+ctx.getText()+"] ");
						}
					}
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitSelect_list(TSQLParser.Select_listContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				selectListColumnNr = 0;
				selectListNrColumns = ctx.select_list_elem().size();
				boolean hasTable = false;

				ParserRuleContext parentRule = ctx.getParent();
				if (parentRule instanceof TSQLParser.Query_specificationContext) {
    				TSQLParser.Query_specificationContext parentCtx = (TSQLParser.Query_specificationContext)parentRule;
    				if (parentCtx.FROM() != null) {
    					hasTable = true;
    				}
				}

				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"variableAssignDepends.clear(), hasTable=["+hasTable+"] ", u.debugPtree);
				variableAssignDepends.clear();
				visitChildren(ctx);

				captureVariableAssignDepends("SELECT", ctx.start.getLine(), hasTable);

				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitSelect_list_elem(TSQLParser.Select_list_elemContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				selectListColumnNr++;
				if (ctx.LOCAL_ID() != null) {
					String varName = ctx.LOCAL_ID().getText().toUpperCase();
					String op = "=";
					if (ctx.assignment_operator() != null) {
						op = ctx.assignment_operator().getText();
					}
					captureItem("Variable assignment by SELECT @v "+op, varName, MiscReportGroup, "variable assignment select", u.Supported, ctx.start.getLine());

					// check for numeric-as-date
					checkNumericDateVarAssign(varName, ctx.expression(), ctx.start.getLine());

					// experimental; try to identify @@ERROR codes being checked via intermediate variables
					// assumption is we hit the assignment stmt before the value comparisons
					// (select_list_elem @v = (expression @@error))
					if ((ctx.expression().getText().equalsIgnoreCase("@@ERROR")) || (ctx.expression().getText().equalsIgnoreCase("ERROR_NUMBER()"))) {
						addAtAtErrorVars(varName);
						//u.appOutput(CompassUtilities.thisProc()+"SELECT assigning @@ERROR to varName=["+varName+"] x=["+ctx.getText()+"] ");
					}

					if (ctx.LOCAL_ID() != null) {
						addVariableAssignDepends(ctx.LOCAL_ID(), ctx.expression());
					}

					addStmtAttribute("VARIABLE_ASSIGN");
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"added VARIABLE_ASSIGN", u.debugPtree);

				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitTime_zone_expr(TSQLParser.Time_zone_exprContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String expr = ctx.expression().get(1).getText();
				if (u.stripStringQuotes(expr).length() == expr.length()) {
					// it's not a string constant, assume it's a variable
					expr = "@v";
				}

				String status = featureSupportedInVersion(AtTimeZone);
				captureItem(AtTimeZone + " " + expr, "", "", "", status, ctx.start.getLine());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitUnary_op_expr(TSQLParser.Unary_op_exprContext ctx) {
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"op=["+ctx.op.getText()+"]  expr=["+ctx.expression().getText()+"] ctx=["+ctx.getText()+"] ", u.debugPtree);
				if (ctx.BIT_NOT() != null) {
					captureItem("Bitwise operator ~ (NOT)", "", OperatorsReportGroup, "", u.Supported, ctx.start.getLine());
				}					
				else if (ctx.PLUS() != null) {
					// detect '+' operators for string expressions
					if (isString(expressionDataType(ctx.expression()))) {
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"Unary plus operator for string ", u.debugPtree);						
						String origText = "+";						
						String item = UnaryStringPlusOp;
						String status = featureSupportedInVersion(item);
						if (status.equals(u.NotSupported)) {
							if (u.rewrite) {
								String rewriteText = "";
								if (addRewrite(item, origText, u.rewriteTypeReplace, rewriteText, ctx.PLUS().getSymbol().getLine(), ctx.PLUS().getSymbol().getCharPositionInLine(), ctx.PLUS().getSymbol().getLine(), ctx.PLUS().getSymbol().getCharPositionInLine(), ctx.PLUS().getSymbol().getStartIndex(), ctx.PLUS().getSymbol().getStopIndex()))
									status = u.Rewritten; 
							}
							else {
								addRewrite(item);
							}
						}
						captureItem(item, "", OperatorsReportGroup, "", status, ctx.PLUS().getSymbol().getLine());						
					}
				}
				visitChildren(ctx);
				return null;
			}

			@Override public String visitMult_div_percent_expr(TSQLParser.Mult_div_percent_exprContext ctx) {
				if (ctx.STAR() != null) captureItem("Arithmetic operator *", "", OperatorsReportGroup, "", u.Supported, ctx.start.getLine());
				else if (ctx.DIVIDE() != null) captureItem("Arithmetic operator /", "", OperatorsReportGroup, "", u.Supported, ctx.start.getLine());
				else if (ctx.PERCENT_SIGN() != null) captureItem("Arithmetic operator % (modulo)", "", OperatorsReportGroup, "", u.Supported, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitPlus_minus_bit_expr(TSQLParser.Plus_minus_bit_exprContext ctx) {
				if (ctx.BIT_AND() != null) captureItem("Bitwise operator & (AND)", "", OperatorsReportGroup, "", u.Supported, ctx.start.getLine());
				else if (ctx.BIT_OR() != null) captureItem("Bitwise operator | (OR)", "", OperatorsReportGroup, "", u.Supported, ctx.start.getLine());
				else if (ctx.BIT_XOR() != null) captureItem("Bitwise operator ^ (XOR)", "", OperatorsReportGroup, "", u.Supported, ctx.start.getLine());
				else {
					// + can be for numeric, string or datetime
					// - can be for numeric or datetime
					String opFmt = "";
					String op = ctx.op.getText();
					List<TSQLParser.ExpressionContext> expr = ctx.expression();
					String lhsType = expressionDataType(expr.get(0));
					String rhsType = expressionDataType(expr.get(1));
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"lhs=["+expr.get(0).getText()+"] op=["+ctx.op.getText()+"] rhs=["+expr.get(1).getText()+"] ", u.debugPtree);
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"lhsType=["+lhsType+"] rhsType=["+rhsType+"] ctx=["+ctx.getText()+"] ", u.debugPtree);
					if (op.equals("+")) {
						opFmt = "Arithmetic operator +";

						if (isString(lhsType) && isString(rhsType)) {
							opFmt = "String concatenation operator +";
						}
						else if (isBinary(lhsType) && isBinary(rhsType)) {
							opFmt = "Binary string concatenation operator +";
						}
						else if (isDateTime(lhsType) || isDateTime(rhsType)) {
							opFmt = "Date/time arithmetic operator +";
						}
						else if (isUnknown(lhsType) || isUnknown(rhsType)) {
							opFmt = "";  // cannot determine
						}

						// check for string operator in computed column
						if (!opFmt.isEmpty()) {
							if (opFmt.startsWith("String")) {  // see above
								if (inCompCol) {
									String statusCC = u.Supported;
									if (featureExists(CompColFeatures, cfgStringConcatPlus)) {
										statusCC = featureSupportedInVersion(CompColFeatures, cfgStringConcatPlus);
										captureItem(opFmt + ", in computed column"+inCompColType, "", OperatorsReportGroup, "", statusCC, ctx.start.getLine());
									}
								}
							}
						}
					}
					else {
						// op = '-'
						opFmt = "Arithmetic operator -";
						if (isDateTime(lhsType) || isDateTime(rhsType)) {
							opFmt = "Date/time arithmetic operator -";
						}
					}
					if (!opFmt.isEmpty()) {
						captureItem(opFmt, "", OperatorsReportGroup, "", u.Supported, ctx.start.getLine());
					}
				}
				visitChildren(ctx);
				return null;
			}

			@Override public String visitComparison_operator(TSQLParser.Comparison_operatorContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				if ((ctx.MULT_ASSIGN() != null) || (ctx.EQUAL_STAR_OJ() != null)) {
					// these are the *=, =*  TSQL OJ operators, they are reported elsewhere
					return null;
				}

				String op = ctx.getText();
				// only for operators consisting of 2 chars:
				if (op.length() < 2) {
					// these are all supported
					String item = ComparisonOperator + " " + op;
					captureItem(item, op, OperatorsReportGroup, "", u.Supported, ctx.start.getLine());
					return null;
				}

				// check for unsupported operators
				String opChk = op.replaceAll("\\s+", "");
				if (featureExists(ComparisonOperator,opChk)) {
					String status = featureSupportedInVersion(ComparisonOperator, opChk);
					String item = ComparisonOperator + " " + opChk;
					if (!status.equals(u.Supported)) {
						if (u.rewrite) {
							String rewriteText = "";
							if (opChk.equals("!<")) rewriteText = ">=";
							if (opChk.equals("!>")) rewriteText = "<=";
							if (!rewriteText.isEmpty()) {
								if (addRewrite(item, "", u.rewriteTypeReplace, rewriteText, ctx.start.getLine(), ctx.start.getCharPositionInLine(), ctx.stop.getLine(), ctx.stop.getCharPositionInLine(), ctx.start.getStartIndex(), ctx.stop.getStopIndex()))
									status = u.Rewritten;
							}
							else {
								// somehow we're missing something here, so just report it
							}
						}
						else {
							addRewrite(item);
						}
					}

					captureItem(item, op, OperatorsReportGroup, "", status, ctx.start.getLine());

					// no children to visit
					return null;
				}

				// This is only for capturing compound operators containing whitespace
				// NB: SQL Server does not allow whitespace inside compound assignment operators (+=, -=, etc.), so no need to test for those
				String status = featureSupportedInVersion(CompoundOpWhitespace, op);
				String item = CompoundOpWhitespaceFmt+"("+op+")";
				int ixStart = ctx.start.getStartIndex();
				int ixStop = ctx.stop.getStopIndex();
				if (ixStop - ixStart <= 1) {
					// 1-char operators, these are OK as far as spaces are concerned
					item = ComparisonOperator + " " + op;
					captureItem(item, op, OperatorsReportGroup, "", u.Supported, ctx.start.getLine());
					return null;
				}

				// is this operator listed? If not, whitespace inside is supported
				if (featureExists(CompoundOpWhitespace,op)) {
					if (!status.equals(u.Supported)) {
						if (u.rewrite) {
							String rewriteText = op;
							if (addRewrite(item, "", u.rewriteTypeReplace, rewriteText, ctx.start.getLine(), ctx.start.getCharPositionInLine(), ctx.stop.getLine(), ctx.stop.getCharPositionInLine(), ctx.start.getStartIndex(), ctx.stop.getStopIndex()))
								status = u.Rewritten;
						}
						else {
							addRewrite(item);
						}
					}
				}
				captureItem(item, op, OperatorsReportGroup, "", status, ctx.start.getLine());

				// no children to visit
				return null;
			}

			@Override public String visitDistinct_from_operator(TSQLParser.Distinct_from_operatorContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				// SQL2022 feature

				String status = featureSupportedInVersion(DistinctFromOperator);
				captureItem("DISTINCT FROM operator", "", OperatorsReportGroup, "", status, ctx.start.getLine());

				// no children to visit
				return null;
			}

			@Override public String visitOdbc_scalar_function(TSQLParser.Odbc_scalar_functionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String funcName = ctx.odbc_scalar_function_name().getText().toUpperCase();
				funcName = funcName.substring(0,funcName.indexOf("("));
				String status = featureSupportedInVersion(ODBCScalarFunction,funcName);

				if (!status.equals(u.Supported)) {
					String origText = ctx.getText();
					if (rewriteDirectODBCfuncOrig.contains(funcName)) {
						if (u.rewrite) {
							// for most ODBC functions, the argument list can be copied more or less unchanged, but for some cases (POSITION, EXTRACT, INSERT, TRUNCATE,...) more changes are needed; those are not currently supported for rewriting
							String rewriteText = rewriteDirectODBCfuncReplace.get(rewriteDirectODBCfuncOrig.indexOf(funcName));

							String rewriteType = "";
							if (rewriteText.startsWith(rewriteTagCustom)) {
								if (funcName.equals("MOD")) {
									rewriteType = u.rewriteTypeReplace;
									List<TSQLParser.ExpressionContext> args = ctx.odbc_scalar_function_name().expression();
									rewriteText = "(("+args.get(0).getText()+ ")%("+args.get(1).getText()+"))";   // Note: losing inter-keyword whitespace here, but let's gamble that's OK in this case
									                                                                              // If not, we need to apply spacing around keywords, but that may mess up identifiers (eg 'datefrom')
								}
							}
							if (rewriteType.isEmpty()) {
								if (ctx.odbc_scalar_function_name().id() != null) {
									rewriteType = u.rewriteTypeODBCfunc1;  // only replace the function name, keep arguments
								}
							}

							if (!rewriteType.isEmpty()) {
								if (addRewrite(ODBCScalarFunction, origText, rewriteType, rewriteText, ctx.L_CURLY().getSymbol().getLine(), ctx.L_CURLY().getSymbol().getCharPositionInLine(), ctx.R_CURLY().getSymbol().getLine(), ctx.R_CURLY().getSymbol().getCharPositionInLine(), ctx.L_CURLY().getSymbol().getStartIndex(), ctx.R_CURLY().getSymbol().getStopIndex()))
									status = u.Rewritten;
							}
						}
						else {
							addRewrite(ODBCScalarFunction + " { fn " + funcName + "() }");
						}
					}
					else {
						String f = "{ FN " + funcName + " }";
						if (rewriteDirectOrig.contains(f)) {
							if (u.rewrite) {
								String rewriteText = rewriteDirectReplace.get(rewriteDirectOrig.indexOf(f));
								if (addRewrite(ODBCScalarFunction, origText, u.rewriteTypeReplace, rewriteText, ctx.L_CURLY().getSymbol().getLine(), ctx.L_CURLY().getSymbol().getCharPositionInLine(), ctx.R_CURLY().getSymbol().getLine(), ctx.R_CURLY().getSymbol().getCharPositionInLine(), ctx.L_CURLY().getSymbol().getStartIndex(), ctx.R_CURLY().getSymbol().getStopIndex()))
									status = u.Rewritten;
							}
							else {
								addRewrite(ODBCScalarFunction + " { fn " + funcName + "() }");
							}
						}
					}
				}
				 // ctx.getText() could be very long due to embedded expressions
				captureItem(ODBCScalarFunction+" { fn "+funcName+"() }", "{ fn "+funcName+"() }", ODBCScalarFunction, funcName, status, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitOdbc_literal(TSQLParser.Odbc_literalContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String funcName = "";
				if (ctx.op != null) funcName = ctx.op.getText().toUpperCase();
				else funcName = "INTERVAL";
				String status = featureSupportedInVersion(ODBCLiterals,funcName);
				if (rewriteDirectODBCfuncOrig.contains(funcName)) {
					if (!status.equals(u.Supported)) {
						if (u.rewrite) {
							String origText = ctx.getText();
							String rewriteText = rewriteDirectODBCfuncReplace.get(rewriteDirectODBCfuncOrig.indexOf(funcName));
							String rewriteType = u.rewriteTypeODBClit1;  // only replace the function name, keep argumentS
							if (!rewriteType.isEmpty()) {
								if (addRewrite(ODBCLiterals, origText, rewriteType, rewriteText, ctx.L_CURLY().getSymbol().getLine(), ctx.L_CURLY().getSymbol().getCharPositionInLine(), ctx.R_CURLY().getSymbol().getLine(), ctx.R_CURLY().getSymbol().getCharPositionInLine(), ctx.L_CURLY().getSymbol().getStartIndex(), ctx.R_CURLY().getSymbol().getStopIndex()))
									status = u.Rewritten;
							}
						}
						else {
							addRewrite(ODBCLiterals + " { " + funcName + " }");
						}
					}
				}
				captureItem(ODBCLiterals+" { "+funcName+u.escapeHTMLChars(" <string> }"), ctx.getText(), ODBCLiterals, funcName, status, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitOdbc_outer_join(TSQLParser.Odbc_outer_joinContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String status = featureSupportedInVersion(ODBCOJ);
				captureItem(ODBCOJ, "", ODBCOJ, "", status, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitReadtext_statement(TSQLParser.Readtext_statementContext ctx) {
				String status = featureSupportedInVersion(ReadText);
				captureItem(ReadText, ctx.col.getText(), ReadText, "", status, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitWritetext_statement(TSQLParser.Writetext_statementContext ctx) {
				String status = featureSupportedInVersion(WriteText);
				captureItem(WriteText, ctx.col.getText(), WriteText, "", status, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitUpdatetext_statement(TSQLParser.Updatetext_statementContext ctx) {
				String status = featureSupportedInVersion(UpdateText);
				captureItem(UpdateText, ctx.col.getText(), UpdateText, "", status, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitDbcc_statement(TSQLParser.Dbcc_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String cmd = "";
				if (ctx.SHRINKLOG() != null) cmd = "SHRINKLOG";
				else cmd = ctx.name.getText().toUpperCase();
				if (cmd.equals("TRACEON") || cmd.equals("TRACEOFF")) {
					String flagsRaw = ctx.expression_list().getText().toUpperCase();
					List<String> flags = new ArrayList<>(Arrays.asList(flagsRaw.split(",")));
					for (int i=0; i<flags.size(); i++) {
						String flag = u.applyPatternFirst(flags.get(i), "^[0]+", "");
						if (flag.equals("-1")) continue;
						String status = u.NotSupported;
						if (flag.charAt(0) == '@') {
							status = u.ReviewManually;
							flag = formatItemDisplay(flag);
						}
						else {
							status = featureSupportedInVersion(Traceflags,flag);
						}
						captureItem("DBCC "+cmd+"("+flag+")", flag, Traceflags, flag, status, ctx.start.getLine());
					}
				}
				else {
					String status = featureSupportedInVersion(DbccStatements,cmd);
					captureItem("DBCC "+cmd, cmd, DbccStatements, cmd, status, ctx.start.getLine());
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitCollation(TSQLParser.CollationContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String collationName = u.normalizeName(ctx.id().getText().toUpperCase());
				String columnName = "";

				String context = "for column";
				String contextCS = "COLUMN";
				if (hasParent(ctx.parent,"expression")) context = "for expression";
				else if (hasParent(ctx.parent,"declare_statement")) {
					if (hasParent(ctx.parent,"declare_local")) {
						context = "for scalar variable";
					}
					else {
						TSQLParser.Column_definitionContext parent_ctx = (TSQLParser.Column_definitionContext) ctx.getParent();
						columnName = u.normalizeName(parent_ctx.id().getText());
						context = "for table variable column";
					}
				}
				else if (hasParent(ctx.parent,"create_or_alter_function")) {
					TSQLParser.Column_definitionContext parent_ctx = (TSQLParser.Column_definitionContext) ctx.getParent();
					columnName = u.normalizeName(parent_ctx.id().getText());
					context = "for table function result";
				}
				else if (hasParent(ctx.parent,"create_type")) {
					TSQLParser.Column_definitionContext parent_ctx = (TSQLParser.Column_definitionContext) ctx.getParent();
					columnName = u.normalizeName(parent_ctx.id().getText());
					context = "in CREATE TYPE";
				}
				else if (hasParent(ctx.parent,"column_declaration")) {
					TSQLParser.Column_declarationContext parent_ctx = (TSQLParser.Column_declarationContext) ctx.getParent();
					columnName = u.normalizeName(parent_ctx.id().getText());
					context = "for column";
				}
				else if (hasParent(ctx.parent,"column_definition")) {
					TSQLParser.Column_definitionContext parent_ctx = (TSQLParser.Column_definitionContext) ctx.getParent();
					columnName = u.normalizeName(parent_ctx.id().getText());
					context = "for column";
				}
				else if (hasParent(ctx.parent,"create_database")) {
					context = "in CREATE DATABASE";
					contextCS = "DATABASE";	
				}
				else if (hasParent(ctx.parent,"alter_database")) {
					context = "in ALTER DATABASE";
					contextCS = "DATABASE";
				}

				String status = featureSupportedInVersion(Collations,collationName);
				String CSmsg = "";

				if (collationName.toUpperCase().contains("_CS_")) {
					if (status.equals(u.Supported)) {
						String CSstatus = featureSupportedInVersion(CaseSensitiveCollation,contextCS);
						if (!CSstatus.equals(u.Supported)) {
							status = CSstatus;
							CSmsg = CaseSensitiveCollation + " ";
						}
					}
				}
				
				String msg = CSmsg+collationName+", "+context;
				
				if (hasParent(ctx.parent,"create_database")) {
					// Sort out the status of COLLATE - this is a bit messy due to how support in Babelfish developed over time
					//u.appOutput(u.thisProc()+"collationName=["+collationName+"]");
					
					String statusCrdb = featureSupportedInVersion(CreateDatabaseOptions, "COLLATE");
					String statusCrdbV24 = "";
					if (statusCrdb.equals(u.NotSupported)) {
						msg = "CREATE DATABASE...COLLATE";
						status = statusCrdb;
					}
					else {
						status = statusCrdbV24 = statusCrdb;  // CREATE DATABASE...COLLATE is IGNORED
						
						statusCrdb = featureSupportedInVersion(CreateDatabaseOptions, "COLLATE " + collationName);
						String statusv31 = featureSupportedInVersion(CreateDatabaseOptions,"COLLATE SQL_LATIN1_GENERAL_CP1_CI_AS");
						if (statusCrdb.equals(u.Supported)) {
							msg = CSmsg+collationName+", "+context;
							status = statusCrdb;
						}
						else {
							if (statusv31.equals(u.Supported)) {
								status = u.NotSupported;
							}
							else {
								status = statusCrdbV24;
							}
						}
					}
				}
				
				captureItem(msg, columnName, Collations, "", status, ctx.COLLATE().getSymbol().getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitCreate_database(TSQLParser.Create_databaseContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String dbName = u.normalizeName(ctx.database.getText());

				List<TSQLParser.Create_database_optionContext> options = ctx.create_database_option();
				for (int i=0; i<options.size(); i++) {
					String option = options.get(i).getText().toUpperCase();
					String optionChk = option;
					//if (option.equals("CATALOG_COLLATION=DATABASE_DEFAULT")) continue;

					String status = u.uninitialized;
					boolean captured = false;
					if (option.startsWith("CATALOG_COLLATION=")) {
						optionChk = optionChk.replaceFirst("=", " ");
						String catalogCollation = getOptionValue(option);
						if (catalogCollation.toUpperCase().contains("_CS_")) {
							status = featureSupportedInVersion(CaseSensitiveCollation,"CATALOG_COLLATION");
							if (!status.equals(u.Supported)) {
								captureItem(CaseSensitiveCollation + " "+catalogCollation+", in CREATE DATABASE..CATALOG_COLLATION", "", Collations, "", status, options.get(i).start.getLine());
								captured = true;
							}
						}
					}
			
					if (status.equals(u.uninitialized) && !captured) {
						status = featureSupportedInVersion(CreateDatabaseOptions,optionChk);
					}
					if (!captured) {
						captureItem("Option "+option+", in CREATE DATABASE", option, CreateDatabaseOptions, option, status, options.get(i).start.getLine());
					}
				}

				if (ctx.CONTAINMENT() != null) {
					String option = "CONTAINMENT";
					String optionValue = ctx.containment.getText().toUpperCase();
					String status = featureSupportedInVersion(CreateDatabaseOptions, option, optionValue);
					captureItem("Option "+formatOptionDisplay(option,optionValue)+	", in CREATE DATABASE", option, CreateDatabaseOptions, option, status, ctx.start.getLine());
				}
				captureItem("CREATE DATABASE "+dbName, dbName, DatabasesReportGroup, "", u.Supported, ctx.start.getLine(), "0");
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitAlter_database(TSQLParser.Alter_databaseContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String dbName = "";

				// is ALTER DATABASE supported at all?
				String AlterDBStatus = featureSupportedInVersion(AlterDatabase);
				if (!AlterDBStatus.equals(u.Supported)) {
					captureItem("ALTER DATABASE", "", DatabasesReportGroup, "", AlterDBStatus, ctx.start.getLine());
				}
				else {
					if (ctx.database != null) dbName = ctx.database.getText();
					else {
						dbName = "CURRENT";
						String status = featureSupportedInVersion(AlterDatabaseOptions, dbName);
						captureItem("ALTER DATABASE CURRENT", "", AlterDatabaseOptions, "CURRENT", status, ctx.start.getLine());
					}
					dbName = u.normalizeName(dbName.toUpperCase());

					List<TSQLParser.Database_optionspecContext> options = ctx.database_optionspec();
					for (int i=0; i<options.size(); i++) {
						String option = options.get(i).getText().toUpperCase();
						String optionValue = "";
						if (option.contains("=")) {
							optionValue = getOptionValue(option);
							option = getOptionName(option);
						}
						else if (option.endsWith("ON")) {
							optionValue = "ON";
							option = option.substring(0,option.length()-"ON".length());
						}
						else if (option.endsWith("OFF")) {
							optionValue = "OFF";
							option = option.substring(0,option.length()-"OFF".length());
						}
						else {
							optionValue = u.applyPatternFirst(option, "^(CURSOR_DEFAULT|PAGE_VERIFY|RECOVERY|PARAMETERIZATION|MULTI_USER|ENABLE_BROKER)(.*$)", "$2");
							option      = u.applyPatternFirst(option, "^(CURSOR_DEFAULT|PAGE_VERIFY|RECOVERY|PARAMETERIZATION|MULTI_USER|ENABLE_BROKER)(.*$)", "$1");
							if (optionValue.equals(option)) optionValue="";
						}
						if (option.contains("(")) {
							optionValue = getOptionValue(option, "(") + optionValue;
							option = getOptionName(option, "(");
						}

						String status = featureSupportedInVersion(AlterDatabaseOptions, option, optionValue);
						captureItem("Option "+formatOptionDisplay(option,optionValue)+", in ALTER DATABASE", option, AlterDatabaseOptions, option, status, ctx.start.getLine());
						if (u.debugging) u.dbgOutput("ALTER DATABASE, dbName=["+dbName+"]  option=["+option+"] optionValue=["+optionValue+"]  status=["+status+"] ", u.debugPtree);
					}

					if (ctx.MODIFY() != null) {
						String option = "MODIFY NAME";
						String status = featureSupportedInVersion(AlterDatabaseOptions, option, "");
						captureItem("Option "+option+", in ALTER DATABASE", option, AlterDatabaseOptions, option, status, ctx.start.getLine());
					}
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitId(TSQLParser.IdContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				// it is not an identifier if this is an actual argument in a proc call
				if (hasParent(ctx.parent,"execute_parameter")) return null;

				// it is not an identifier if this is in a parameter default
				if (hasParent(ctx.parent,"procedure_param")) {
					if (!hasParent(ctx.parent,"data_type")) {
						return null;
					}
				}

				String idOrig = ctx.getText();
				String id = u.normalizeName(idOrig).toUpperCase();

				if (!u.getPatternGroup(idOrig, "^(GEOGRAPHY|GEOMETRY)\\s*::", 1).isEmpty()) {
					String spatialCall = u.getPatternGroup(idOrig, "^\\w+\\s*::\\s*(\\w+)\\b", 1);					
					String status = featureSupportedInVersion(Geospatial,spatialCall);
					if (status.equals(u.Supported)) {
						if (featureExists(spatialCall)) {
							// for function calls like STGeomFromText, assuming this is about validating the first argument only
							if (parentRuleName(ctx.parent,1).equals("func_proc_name_server_database_schema") && parentRuleName(ctx.parent,2).equals("function_call")) {
								TSQLParser.Function_callContext f = (TSQLParser.Function_callContext) ctx.getParent().getParent();
								if (f.function_arg_list() != null) {
									int nrArgs = argListCount( f.function_arg_list());
									if (nrArgs >= 1) {
										String arg1 = f.function_arg_list().expression().get(0).getText();
										String spatialFunction = "expression";
										if (isStringConstant(arg1)) {
											arg1 = u.stripStringQuotes(arg1).trim();
											spatialFunction = CompassUtilities.getPatternGroup(arg1, "^(\\w+)\\b", 1).toUpperCase(); // not 100% watertight but good enough
										}			
										String status2 = featureSupportedInVersion(spatialCall, spatialFunction);
										status = status2;
										spatialCall += "("+spatialFunction+")";
									}
								}
							}
						}
					}
					captureItem(SpatialMethodCallFmt + " " + spatialCall, "", SpatialReportGroup, "", status, ctx.start.getLine());
				}
				else {
					if (featureExists(SpecialColumNames,id)) {
						String status = featureSupportedInVersion(SpecialColumNames,id);
						captureItem("Special column name "+id, id, SpecialColumNames, id, status, ctx.start.getLine());
					}

					int maxIdLen = featureIntValueSupportedInVersion(MaxIdentifierLength);
					if (id.length() > maxIdLen) { // quick first filter
						String idDecoded = u.decodeIdentifier(id);
						int idLen = idDecoded.length();
						if (idLen > maxIdLen) {
							// identifier delimiters do not count towards the max. PG length of 63
							if (idDecoded.startsWith("[") || idDecoded.startsWith("\"")) {
								// don't know what to do if not ending in a delimiter, so just assume it is correct
								idLen -= 2;
							}
							if (idLen > maxIdLen) {
								captureItem("Identifier > 63 characters", idOrig, MaxIdentifierLength, ""+id.length()+"", u.Supported, ctx.start.getLine());
							}
						}
					}

					// test for special chars in identifiers not currently supported
					List<String> specialChars = featureValueList(SpecialCharsIdentifier);
					for (int i=0; i<specialChars.size(); i++) {
						String c = specialChars.get(i);
						CaptureSpecialCharIdentifier(idOrig, c, SpecialCharsIdentifier, ctx.start.getLine());
					}
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			public void CaptureSpecialCharIdentifier (String id, String SpecialChar, String section, int lineNr) {
				if (SpecialChar.isEmpty()) return;
				if (id.startsWith("[") && !id.contains("]]")) return;  // ]] not handled inside a delimited identifier
				if (id.startsWith("\"")) return;
				if (id.startsWith("@")) id = id.substring(1);
				if (id.startsWith("@")) id = id.substring(1); // for @@ variables
				if (id.startsWith("#")) id = id.substring(1);
				if (id.startsWith("#")) id = id.substring(1); // for global temp tabs
				if (id.contains(SpecialChar)) {
					if (featureExists(section,u.decodeIdentifier(SpecialChar))) {
						String status = featureSupportedInVersion(section,u.decodeIdentifier(SpecialChar));
						captureItem(section+": '"+u.decodeIdentifier(SpecialChar)+"'", id, SpecialCharsIdentifier, u.decodeIdentifier(SpecialChar), status, lineNr);
					}
				}
			}

			@Override public String visitTransaction_statement(TSQLParser.Transaction_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				boolean captured = false;
				String stmt = "";
				if (ctx.BEGIN() != null) {
					stmt = "BEGIN TRANSACTION";
				}
				else if (ctx.ROLLBACK() != null) {
					stmt = "ROLLBACK TRANSACTION";
				}
				else if (ctx.COMMIT() != null) {
					stmt = "COMMIT TRANSACTION";
				}
				else if (ctx.SAVE() != null) {
					stmt = "SAVE TRANSACTION";
				}

				String scope = "";

				if (hasParent(ctx.parent,"create_or_alter_procedure")) scope = ", in procedure";
				else if (hasParent(ctx.parent,"create_or_alter_trigger")) scope = ", in trigger";
				else if (hasParent(ctx.parent,"create_or_alter_function")) scope = ", in function";  // shouldn't be possible

				String xactName = "";
				String xactNameFmt = "";
				if (ctx.id() != null) {
					xactNameFmt = ", with xact name";
					xactName = ctx.id().getText();
				}
				else if (ctx.xactnamevar != null) {
					xactNameFmt = ", with xact name in variable";
					xactName = ctx.xactnamevar.getText();
					String xactNameTest = "TRANSACTION NAME IN VARIABLE";
					String status = featureSupportedInVersion(Transactions,xactNameTest);
					captureItem(stmt+scope+xactNameFmt, xactName, Transactions, xactNameTest, status, ctx.start.getLine());
					captured = true;
				}

				if (ctx.DISTRIBUTED() != null) {
					stmt = "BEGIN DISTRIBUTED TRANSACTION";
					String status = featureSupportedInVersion(Transactions,stmt);
					captureItem(stmt+scope+xactNameFmt, "", Transactions, stmt, status, ctx.start.getLine());
					captured = true;
				}

				if (ctx.MARK() != null) {
					String option = "MARK";
					String hint = ", WITH " + option;
					String status = featureSupportedInVersion(Transactions,option);
					captureItem(stmt+scope+xactNameFmt+hint, "", Transactions, option, status, ctx.start.getLine());
					captured = true;
				}

				if (ctx.DELAYED_DURABILITY() != null) {
					String option = "DELAYED_DURABILITY";
					String optionValue = "ON";
					if (ctx.OFF() != null) optionValue = "OFF";
					String status = featureSupportedInVersion(Transactions, option, optionValue);
					captureItem(stmt+scope+xactNameFmt+", WITH " +formatOptionDisplay(option,optionValue), "", Transactions, option, status, ctx.start.getLine());
					captured = true;
				}

				if (!captured) {
					captureItem(stmt+scope+xactNameFmt, xactName, Transactions, "", u.Supported, ctx.start.getLine());
				}

				// PG does not support xact control in SECURITY DEFINER procedures (=EXECUTE AS OWNER)
				if (u.currentObjectAttributes.contains(" EXECUTE AS OWNER ")) {
					String status = featureSupportedInVersion(SecurityDefinerXact);
					if (!status.equals(u.Supported)) {
						captureItem(stmt+" not supported with PostgreSQL SECURITY DEFINER(=EXECUTE AS OWNER)", stmt, SecurityDefinerXact, stmt, status, ctx.start.getLine());
					}
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitHex_string(TSQLParser.Hex_stringContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if (ctx.getText().contains("\\\n")) {  // note that CRLF has been changed to LF by now
					String status = featureSupportedInVersion(LineContinuationChar,"HEX");
					captureItem(LineContinuationChar + " in hex string", "", LineContinuationChar, "", status, ctx.start.getLine());

					//visitChildren(ctx); // has no children
				}
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitExecute_as_statement(TSQLParser.Execute_as_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				// ToDo: handle WITH (NO REVERT | COOKIE)
				String stmt = "";
				if (ctx.CALLER() != null) stmt = "CALLER";
				else if (ctx.LOGIN() != null) stmt = "LOGIN";
				else if (ctx.USER() != null) stmt = "USER";
				stmt = "EXECUTE AS " + stmt;

				String status = featureSupportedInVersion(ExecuteAsRevert,stmt);
				captureItem(stmt + " (statement)", stmt, ExecuteAsRevert, stmt, status, ctx.start.getLine());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitRevert_statement(TSQLParser.Revert_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				// ToDo: handle COOKIE
				String stmt = "REVERT";
				String status = featureSupportedInVersion(ExecuteAsRevert,stmt);
				captureItem(stmt, stmt, ExecuteAsRevert, stmt, status, ctx.start.getLine());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitTable_value_constructor(TSQLParser.Table_value_constructorContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if (hasParent(ctx.parent,"table_source_item")) {
					String status = featureSupportedInVersion(TableValueConstructor);
					captureItem(TableValueConstructor+": VALUES(...)", "", TableValueConstructor, "", status, ctx.start.getLine());
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitInsert_statement(TSQLParser.Insert_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String tableName = "";
				String targetTableName = "";
				String type = "VALUES()";
				String typeFmt = "";
				String updateTVFStatus = u.Supported;
				String functionCall = "";
				if (ctx.ddl_object() != null) {
					String tableNameRaw = ctx.ddl_object().getText().toUpperCase();
					tableName = targetTableName = u.normalizeName(tableNameRaw);
					CaptureIdentifier(tableNameRaw, tableName, "INSERT(target)", ctx.start.getLine());
				}
				else if (ctx.function_call() != null) {
					String tableNameRaw = ctx.function_call().func_proc_name_server_database_schema().getText().toUpperCase();
					tableName = u.normalizeName(tableNameRaw);
					updateTVFStatus = featureSupportedInVersion(InsertStmt,"TABLE FUNCTION");
					functionCall = ", on table function";
				}
				else {
					tableName = ctx.rowset_function().getText().toUpperCase();
					type = tableName = tableName.substring(0,tableName.indexOf("("));
					functionCall = ", " + tableName + "()";
					updateTVFStatus = featureSupportedInVersion(InsertStmt,tableName);
				}
				String itemDetail = tableName;

				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"tableName=["+tableName+"] type=["+type+"] ", u.debugPtree);
				if (ctx.insert_statement_value().derived_table() != null) {
					type = "SELECT";
					if (ctx.insert_statement_value().derived_table().table_value_constructor() != null) type = "VALUES";
				}
				else if (ctx.insert_statement_value().execute_statement() != null) {
					if (ctx.insert_statement_value().execute_statement().execute_body().execute_var_string().size() > 0) {
						type = "EXECUTE(string)";
						typeFmt = "EXECUTE('...')";
						// single-constant INSERT..EXEC('string') actually appears to work in BBF 2.1.0, but any type of expression or single variable does not
						String s = ctx.insert_statement_value().execute_statement().execute_body().getText();
						s = u.removeLastChar(s.substring(1));
						s = u.maskStringConstants(s, "s");
						if (!s.equals("'s'")) {
							type = "EXECUTE(expression)";
							typeFmt = "";
						}
					}
					else {
						type = "EXECUTE procedure";
						if (ctx.insert_statement_value().execute_statement().execute_body().func_proc_name_server_database_schema() != null) {
							String procName = ctx.insert_statement_value().execute_statement().execute_body().func_proc_name_server_database_schema().getText();
							if (u.getObjectNameFromID(procName).equalsIgnoreCase("SP_EXECUTESQL")) {
								type = "EXECUTE sp_executesql";
							}
						}
						else {
							// not used
							String procName = ctx.insert_statement_value().execute_statement().execute_body().proc_var.getText();
						}
					}
				}
				else if (ctx.insert_statement_value().DEFAULT() != null) type = "DEFAULT VALUES";

				String status = u.Supported;
				String top = "";
				if (ctx.TOP() != null) {
					top = " TOP";
					status = featureSupportedInVersion(InsertStmt,"TOP");
					if (ctx.select_statement() != null) {
						String statusSubq = featureSupportedInVersion(InsertStmt,"TOP SUBQUERY");
						if (!statusSubq.equals(u.Supported)) {
							captureItem("INSERT TOP(subquery)", tableName, InsertStmt, "INSERT", statusSubq, ctx.start.getLine());
						}
					}
				}
				if (status.equals(u.Supported)) {
					status = featureSupportedInVersion(InsertStmt,type);
				}

				String outputClause = getOutputClause(ctx.output_clause(), status, InsertStmt, "text", tableName);
				status = getOutputClause(ctx.output_clause(), status, InsertStmt, "status", tableName);

				StringBuilder CTE = new StringBuilder("");
 				status = checkDMLCTE(InsertStmt, targetTableName, ctx.with_expression(), status, CTE, ctx.start.getLine());

				if (!updateTVFStatus.equals(u.Supported)) {
					if (updateTVFStatus.equals(u.NotSupported)) status = u.NotSupported;
					else if (!status.equals(u.Supported)) status = updateTVFStatus;
				}

				if (typeFmt.isEmpty()) typeFmt = type;
				if (!captureTableSrcDML(ctx.parent, tableName, "INSERT", ctx.start.getLine())) {
					captureItem("INSERT"+top+".."+typeFmt+CTE.toString()+outputClause+functionCall, itemDetail, InsertStmt, "", status, ctx.start.getLine());
					CaptureXMLNameSpaces(ctx.parent, "INSERT", ctx.start.getLine());
				}

				captureDMLChangeTrackingContext(InsertStmt, ctx.start.getLine(), ctx.parent);

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			private void captureDMLChangeTrackingContext(String stmtType, int lineNr, RuleContext parent) {
				//u.appOutput(u.thisProc()+"stmtType=["+stmtType+"] hasParent(parent,dml_statement_with_change_tracking_context)=["+hasParent(parent,"dml_statement_with_change_tracking_context")+"] ");
				if (hasParent(parent,"dml_statement_with_change_tracking_context")) {
					String status = featureSupportedInVersion(BuiltInFunctions,"CHANGE_TRACKING_CONTEXT");
					//u.appOutput(u.thisProc()+"status=["+status+"] ");
					captureItem("CHANGE_TRACKING_CONTEXT(), with "+stmtType, "", BuiltInFunctions, "", status, lineNr);
				}
				return;
			}

			private String checkDMLCTE(String stmtType, String targetTableName, TSQLParser.With_expressionContext with, String status, StringBuilder CTE, int lineNr) {
				if (with != null) {
					// ToDo: determine whether CTE is recursive
					String recursive = ""; // ", recursive"
					CTE.append(", WITH (Common Table Expression"+recursive+")");
					if (status.equals(u.Supported)) {
						status = featureSupportedInVersion(stmtType,"CTE");
					}

					for (int i = 0; i < with.ctes.size(); i++) {
					    String cteName = with.common_table_expression(i).expression_name.getText();
					    cteName = u.normalizeName(cteName.toUpperCase());
					    if (targetTableName.equals(cteName)) {
					    	CTE.append(" as target");
					    	String statusCTEtarget = featureSupportedInVersion(stmtType,"TARGET CTE");
							if (status.equals(u.Supported)) {
								status = statusCTEtarget;
							}

							if (stmtType.equals(MergeStmt)) {
								captureItem("MERGE"+CTE.toString(), "", stmtType, "", statusCTEtarget, lineNr);
							}
					    	break;
					    }
					}
				}
				return status;
			}

			public boolean captureTableSrcDML(RuleContext parent, String tableName, String stmt, int lineNr) {
				if (!hasParent(parent,"table_source_item_dml")) return false;

				// not expecting to support this soon
				// assuming there is an OUTPUT clause, but not checking it
				String DMLTableSrcStatus = u.NotSupported;
				DMLTableSrcStatus = featureSupportedInVersion(DMLTableSrc, stmt);
				String msg = DMLTableSrcFmt+stmt+"..OUTPUT)";
				captureItem(msg, tableName, DMLTableSrc, msg, DMLTableSrcStatus, lineNr);

				return true;
			}

			public String getOutputClause(TSQLParser.Output_clauseContext opClause, String status, String section, String callType) {
				return getOutputClause(opClause, status, section, callType, "");
			}
			public String getOutputClause(TSQLParser.Output_clauseContext opClause, String status, String section, String callType, String baseTableName) {
				assert (callType.equals("text") || callType.equals("status")) : CompassUtilities.thisProc()+"invalid callType=["+callType+"] ";
				if (opClause == null) {
					if (callType.equals("text")) return "";
					else return status;
				}

				String tmpTabType = "";
				if (opClause.INTO() != null) {
					if (opClause.LOCAL_ID() == null) {
						String tableName = u.normalizeName(opClause.table_name().getText());
						tmpTabType = getTmpTableType(tableName, true);
					}
				}

				if (callType.equals("status")) {
					if (status.equals(u.Supported)) {
						String item = "OUTPUT";
						if (opClause.output_clause() != null) item = "OUTPUT OUTPUT";
						status = featureSupportedInVersion(section,item);
					}
					if (opClause.INTO() != null) {
						if (opClause.LOCAL_ID() == null) {
							if (tmpTabType.equals(GlobalTmpTableFmt)) {
								String statusGlobTmpTab = featureSupportedInVersion(GlobalTmpTable);
								if (!statusGlobTmpTab.equals(u.Supported)) {
									status = statusGlobTmpTab;
								}
							}

						}
					}
					return status;
				}

				// callType = text
				List<String> tmp = new ArrayList<>();
				String result = "to client";
				if (opClause.INTO() != null) {
					if (opClause.LOCAL_ID() != null) {
						result = "INTO @tableVariable";
					}
					else {
						result = "INTO "+tmpTabType;
					}
				}
				tmp.add(", OUTPUT " + result);
				if (opClause.output_clause() != null) {
					tmp.add(getOutputClause(opClause.output_clause(), "", "", "text"));
				}

				// experimental
				//getOutputDmlListElem(opClause.output_dml_list_elem(), section, baseTableName);

				result = String.join("", tmp.stream().sorted().collect(Collectors.toList()));
				return result;
			}

			// experimental: identify cases where the OUTPUT clause references a joined table other than the base table (UPDATE/DELETE only)
//			public void getOutputDmlListElem(List<TSQLParser.Output_dml_list_elemContext> opDmlElemList, String section, String tableName) {
//				//u.appOutput(CompassUtilities.thisProc()+"section=["+section+"] size=["+opDmlElemList.size()+"] tableName=["+tableName+"] ");
//				String baseTable = u.getObjectNameFromID(tableName);
//				for (TSQLParser.Output_dml_list_elemContext e : opDmlElemList) {
//					if (e.output_column_name() == null) continue;
//					String c = e.output_column_name().getText();
//					String obj = u.getObjectNameFromColumnID(c);
//					//u.appOutput(CompassUtilities.thisProc()+"opDmlElemList=["+c+"] obj=["+obj+"] baseTable=["+baseTable+"] section=["+section+"] ");
//					if (!baseTable.equalsIgnoreCase(obj)) {
//						if ((section.equalsIgnoreCase("UPDATE") || section.equalsIgnoreCase("DELETE")) ) {
//							if (obj.equalsIgnoreCase("INSERTED") || obj.equalsIgnoreCase("DELETED") || obj.equalsIgnoreCase("$ACTION")) {
//								// not interested right now
//							}
//							else {
//								u.appOutput("OUTPUT column table=["+obj+"] differs from base table=["+baseTable+"] section=["+section+"] context=["+u.currentObjectType + " " + u.currentObjectName+"] ");
//							}
//						}
//					}
//				}
//			}

			public void CaptureIdentifier(String objNameRaw, String objName, String stmt, int lineNr) {
				CaptureIdentifier(objNameRaw, objName, stmt, lineNr, "");
			}
			public void CaptureIdentifier(String objNameRaw, String objName, String stmt, int lineNr, String fmt) {
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"objNameRaw=["+objNameRaw+"] objName=["+objName+"] stmt=["+stmt+"] fmt=["+fmt+"] ", u.debugPtree);
				if (objNameRaw.trim().isEmpty()) return;
				if (objName.trim().isEmpty()) return;

				if (objNameRaw.charAt(0) == '.') {
					String status = featureSupportedInVersion(LeadingDotsId);
					captureItem(LeadingDotsId, objNameRaw, LeadingDotsId, stmt, status, lineNr);
				}

				if (objName.charAt(0) == '@') {
					String stmtFmt = stmt;
					if (stmt.equals("SELECT")) stmtFmt = "SELECT FROM";
					String status = featureSupportedInVersion(TableVariables);
					if (!status.equals(u.Supported)) {
//						if (stmt.equals("MERGE")) {
//							status = featureSupportedInVersion(stmt);
//						}
						captureItem(stmtFmt + " @tableVariable", objName.toUpperCase(), TableVariables, stmt, status, lineNr);
					}
				}
				if (objName.contains(".")) {
					List<String> parts = new ArrayList<String>(Arrays.asList(objName.split("\\.")));
					
					boolean ObjTypeSupported = true;
					if (stmt.equals("CREATE SYNONYM")) {
						String statusObj = featureSupportedInVersion(MiscObjects, "SYNONYM");
						u.appOutput(u.thisProc()+"statusObj=["+statusObj+"] ");
						if (!statusObj.equals(u.Supported))  ObjTypeSupported = false;
					}

					if (ObjTypeSupported) {
						if (parts.size() == 3) {
							String dbName = u.getDBNameFromID(objName);
							String status = "";
							String ownDB = "";
							if (dbName.equalsIgnoreCase(u.currentDatabase)) {
								status = u.Supported;
								ownDB = " (in current database)";
							}
							else {
								String stmtTest = stmt;
								stmtTest = u.applyPatternFirst(stmtTest, "\\(target\\)$", "");
								stmtTest = u.applyPatternFirst(stmtTest, "^EXECUTE procedure$", "EXECUTE");
								//u.appOutput(u.thisProc()+"stmt=["+stmt+"]  stmtTest=["+stmtTest+"] ");
								status = featureSupportedInVersion(CrossDbReference,stmtTest);
							}
							captureItem(CrossDbReference+" by "+stmt+ownDB, objName.toUpperCase(), CrossDbReference, stmt, status, lineNr);
						}
					}

					if (ObjTypeSupported) {
						if (parts.size() == 4) {
							String serverName = u.getServerNameFromID(objName);
							String status = featureSupportedInVersion(RemoteObjectReference, stmt);						
							captureItem(RemoteObjectReference+" by "+stmt, objName.toUpperCase()+fmt, RemoteObjectReference, stmt, status, lineNr);
						}
					}
				}

				String name = u.getObjectNameFromID(objName).toUpperCase();
				String schema = u.getSchemaNameFromID(objName).toUpperCase();
				String catName = "";
				String section = Catalogs;
				String reportGroup = Catalogs;
				if (name.startsWith("SYS")) {
					if (schema.isEmpty() || schema.equals("DBO") || schema.equals("SYS")) {
						if (featureExists(Catalogs, name)) {
							catName = name.toLowerCase();
						}
					}
				}
				else if (schema.equals("SYS") && (!name.startsWith("SYS"))) {
					if (featureExists(Catalogs, name)) {
						catName = "sys." + name.toLowerCase();
					}
				}
				else if (schema.equals("INFORMATION_SCHEMA")) {
					catName = "INFORMATION_SCHEMA." + name.toUpperCase();
					reportGroup = InformationSchema;
					section = InformationSchema;
				}
				if (!catName.isEmpty()) {
					String status = featureSupportedInVersion(section,name);
					captureItem("Catalog reference "+catName, "", reportGroup, "", status, lineNr);
				}

				if (stmt.startsWith("INSERT") || stmt.startsWith("UPDATE") || stmt.startsWith("DELETE") || stmt.startsWith("MERGE")) {
					if (getTmpTableType(name).equals(GlobalTmpTableFmt)) {
						String statusGlobTmpTab = featureSupportedInVersion(GlobalTmpTable);
						if (!statusGlobTmpTab.equals(u.Supported)) {
							String reportStr = stmt + " " + GlobalTmpTableFmt;
							captureItem(reportStr, objName, DMLReportGroup, "", statusGlobTmpTab, lineNr);
						}
					}
				}
			}

			@Override public String visitBulk_insert_statement(TSQLParser.Bulk_insert_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String tableName = u.normalizeName(ctx.ddl_object().getText().toUpperCase());
				String status = featureSupportedInVersion(BulkInsertStmt);
				captureItem("BULK INSERT", tableName, BulkInsertStmt, "BULK INSERT", status, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitInsert_bulk_statement(TSQLParser.Insert_bulk_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String tableName = u.normalizeName(ctx.table_name().getText().toUpperCase());
				String status = featureSupportedInVersion(InsertBulkStmt);
				captureItem("INSERT BULK (via bulk API only)", tableName, InsertBulkStmt, "INSERT BULK", status, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitUpdate_statement(TSQLParser.Update_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String status = u.Supported;
				String tableName = "";
				String targetTableName = "";
				String tableNameRaw = "";
				String functionCall = "";
				String updateTVFStatus = u.Supported;
				if (ctx.ddl_object() != null) {
					tableNameRaw = ctx.ddl_object().getText().toUpperCase();
					tableName = targetTableName = u.normalizeName(tableNameRaw);
					CaptureIdentifier(tableNameRaw, tableName, "UPDATE(target)", ctx.start.getLine());
				}
				else if (ctx.function_call() != null) {
					tableNameRaw = ctx.function_call().func_proc_name_server_database_schema().getText().toUpperCase();
					tableName = u.normalizeName(tableNameRaw);
					updateTVFStatus = featureSupportedInVersion(UpdateStmt,"TABLE FUNCTION");
					functionCall = ", on table function";
				}
				else {
					tableName = ctx.rowset_function().getText().toUpperCase();
					tableName = tableName.substring(0,tableName.indexOf("("));
					status = featureSupportedInVersion(UpdateStmt,tableName);
					functionCall = ", " + tableName + "()";
				}

				String top = "";
				if (ctx.TOP() != null) {
					top = " TOP";
					status = featureSupportedInVersion(UpdateStmt,"TOP");
					if (ctx.select_statement() != null) {
						String statusSubq = featureSupportedInVersion(UpdateStmt,"TOP SUBQUERY");
						if (!statusSubq.equals(u.Supported)) {
							captureItem("UPDATE TOP(subquery)", tableName, UpdateStmt, "UPDATE", statusSubq, ctx.start.getLine());
						}
					}
				}

				String outputClause = getOutputClause(ctx.output_clause(), status, UpdateStmt, "text", tableName);
				status = getOutputClause(ctx.output_clause(), status, UpdateStmt, "status", tableName);

				StringBuilder CTE = new StringBuilder("");
 				status = checkDMLCTE(UpdateStmt, targetTableName, ctx.with_expression(), status, CTE, ctx.start.getLine());

				String whereCurrentOf = "";
				if (ctx.CURRENT() != null) {
					whereCurrentOf = ", WHERE CURRENT OF";
					if (status.equals(u.Supported)) {
						status = featureSupportedInVersion(UpdateStmt,whereCurrentOf);
					}
				}

				captureUpdDelBugs("UPDATE", tableNameRaw, tableName, ctx.table_sources(), ctx.start.getLine(), ctx.update_elem());

				variableAssignDepends.clear();
				updVarAssign = "";

				visitChildren(ctx);

				if (!updateTVFStatus.equals(u.Supported)) {
					if (updateTVFStatus.equals(u.NotSupported)) status = u.NotSupported;
					else if (!status.equals(u.Supported)) status = updateTVFStatus;
				}

				if (!captureTableSrcDML(ctx.parent, tableName, "UPDATE", ctx.start.getLine())) {
					captureItem("UPDATE"+top+updVarAssign+CTE.toString()+outputClause+whereCurrentOf+functionCall, tableName, UpdateStmt, "UPDATE", status, ctx.start.getLine());
					captureVariableAssignDepends("UPDATE", ctx.start.getLine(), true);

					CaptureXMLNameSpaces(ctx.parent, "UPDATE", ctx.start.getLine());
				}

				captureDMLChangeTrackingContext(UpdateStmt, ctx.start.getLine(), ctx.parent);

				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitUpdate_elem(TSQLParser.Update_elemContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if (ctx.LOCAL_ID() != null) {
					if (updVarAssign.isEmpty()) updVarAssign = " SET @v = expression";
					if (ctx.EQUAL().size() > 1) updVarAssign = " SET @v = column = expression";
					addVariableAssignDepends(ctx.LOCAL_ID(), ctx.expression());
				}
				else if (ctx.method_name != null) {
					captureXMLFeature("XML.", ctx.method_name.getText().toLowerCase(), "()", ctx.start.getLine());
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			private void addVariableAssignDepends(TerminalNode id, TSQLParser.ExpressionContext expr) {
				// ToDo: need to record the assignment operator, since += indicates a string concat
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"entry: id=["+id.getText()+"] expr=["+expr.getText()+"] ", u.debugPtree);
				
				// contrary to the earlier comments below, we cannot ignore the constant assignments. FOr example: 'SELECT @s=1, @t=@s' -- this would not be flagged with the code below
//				if (expr instanceof TSQLParser.Constant_exprContext) {
//					// this assignment can be ignored for determining variable assignment dependencies
//					return;
//				}
//				else if (expr instanceof TSQLParser.Unary_op_exprContext) {
//					TSQLParser.Unary_op_exprContext x = (TSQLParser.Unary_op_exprContext) expr;
//					if (x.expression() instanceof TSQLParser.Constant_exprContext) {
//					// this assignment can be ignored for determining variable assignment dependencies
//						return;
//					}
//				}
				variableAssignDepends.put(id.getText().toUpperCase(), expr);
			}

			// try to determine if a variable assignment (SELECT or UPDATE) does cross-row aggregation or
			// depends on other assigned variables
			private void captureVariableAssignDepends(String stmt, int lineNr, boolean hasTable) {
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"entry: stmt=["+stmt+"] hasTable=["+hasTable+"] variableAssignDepends=["+variableAssignDepends.size()+"] ", u.debugPtree);
				if (variableAssignDepends.size() == 0) {
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"exit zero", u.debugPtree);					
					return;
				}

				Map<String, String> tmpVarDepends = new HashMap<String, String>();
				for (String k : variableAssignDepends.keySet()) {
					String expr = getTextSpaced(variableAssignDepends.get(k));  // getTextSpaced() removes char string contents
					tmpVarDepends.put(k, expr);
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"tmpVarDepends: k=["+k+"] expr=["+expr+"] ", u.debugPtree);
				}

				// first find cases like SELECT @v = @v + 1 FROM mytable, where the variable may accumulate across rows
				// don't do this for SELECT without a table
				if (hasTable) {
					for (Map.Entry<String, String> entry : tmpVarDepends.entrySet()) {
						String k = entry.getKey();
						String expr = entry.getValue().toUpperCase();
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+stmt+ "("+variableAssignDepends.size()+") :"+ k +" => "+expr, u.debugPtree);
						if (expr.contains(k)) {
							while(true) {
								int len1 = expr.length();
								expr = u.applyPatternAll(expr, "( CASE\\b).*?\\bWHEN\\b.*?(\\bTHEN\\b)", "$1 $2");
								if (expr.length() == len1) break;
							}
							expr = u.applyPatternAll(expr, "\\b[\\w\\.]+\\(\\s*((@)?[\\w\\.]+)\\s*\\)", " $1 ");

							if (!CompassUtilities.getPatternGroup(expr, " (" + k + ") ", 1).isEmpty()) {
								// try to determine if there is some sort of operation in the assigment expression.
								// Note this is not a 100% test (won't find function calls), but looks for some common cases like string concatention with '+'.
								// will currently also find false positives like @v = @v + 1
								if (expr.contains("+") || expr.contains("-") || expr.contains("*")) {
									if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"capturing possible variable aggregation: k=["+k+"] ", u.debugPtree);
									String status = u.ReviewSemantics;
									status = featureSupportedInVersion(VarAggrAcrossRows);
									captureItem(VarAggrAcrossRowsFmt + " in "+stmt, k, "DML", "", status, lineNr);
								}
							}
						}
					}
				}

				// now find variable assignments depending on other variables; first remove the assigned variable from the expression
				Map<String, String> tmp = new HashMap<String, String>();
				String allValues = "";
				for (Map.Entry<String, String> entry : tmpVarDepends.entrySet()) {
					String k = entry.getKey().toUpperCase();
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+stmt+ "("+variableAssignDepends.size()+") :"+ k +" => "+entry.getValue(), u.debugPtree);
					String expr = entry.getValue().toUpperCase();
					if (expr.contains(k)) {
						String v = u.applyPatternAll(expr, " " + k + " ", " ");
						if (v.contains("@")) {
							tmp.put(k, v);
							allValues += " " + v;
						}
						else {
							//no @var in value
						}
					}
					else {
						if (expr.contains("@")) {
							tmp.put(k, expr);
							allValues += " " + expr;
						}
						else {
							tmp.put(k, " ");
						}
					}
				}
				allValues = " " + String.join(" ", tmp.values()) + " ";
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+" allValues=["+allValues+"]", u.debugPtree);
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+" tmp=["+tmp+"]", u.debugPtree);

				// now see if there is a case of @v = @w, while @w is also an assignment target in the same statement
				for (Map.Entry<String, String> entry : tmp.entrySet()) {
					String k = entry.getKey();
					String varRegex = "([^@\\$\\w]" + k + "[^@\\$\\w])";
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+" k=["+k+"]  v=["+entry.getValue()+"]  ", u.debugPtree);
					if (allValues.contains(k)) { // quicker test but less accurate
						if (!CompassUtilities.getPatternGroup(allValues, varRegex, 1).isEmpty()) { // slower test but accurate
							// find variable on lhs for this case
							String v = "";
							for (Map.Entry<String, String> e2 : tmp.entrySet()) {
								if (e2.getKey().equals(k)) {
									continue;
								}
								if (!CompassUtilities.getPatternGroup(" " +e2.getValue()+" ", varRegex, 1).isEmpty()) {
									v = e2.getKey();
									break;
								}
							}
							if (!v.isEmpty()) {
								if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"capturing: v=["+v+"] => k=["+k+"] ", u.debugPtree);
								String status = u.ReviewSemantics;
								status = featureSupportedInVersion(VarAssignDependency);
								captureItem(VarAssignDependency+" in "+stmt+": order of assignments not guaranteed; may work differently on Babelfish", v+"->"+k, "DML", "", status, lineNr);
								break;
							}
						}
					}
				}
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"exit", u.debugPtree);
			}

			@Override public String visitDelete_statement(TSQLParser.Delete_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String status = u.Supported;
				String tableName = "";
				String tableNameRaw = "";
				String targetTableName = "";
				String functionCall = "";
				String updateTVFStatus = u.Supported;
				if (ctx.delete_statement_from().rowset_function() != null) {
					tableName = ctx.delete_statement_from().rowset_function().getText().toUpperCase();
					tableName = tableName.substring(0,tableName.indexOf("("));
					status = featureSupportedInVersion(DeleteStmt,tableName);
					functionCall = ", " + tableName + "()";
				}
				else if (ctx.delete_statement_from().function_call() != null) {
					tableNameRaw = ctx.delete_statement_from().function_call().func_proc_name_server_database_schema().getText().toUpperCase();
					tableName = u.normalizeName(tableNameRaw);
					updateTVFStatus = featureSupportedInVersion(DeleteStmt,"TABLE FUNCTION");
					functionCall = ", on table function";
				}
				else {
					tableNameRaw = ctx.delete_statement_from().getText().toUpperCase();
					tableName = targetTableName = u.normalizeName(tableNameRaw);
					CaptureIdentifier(tableNameRaw, tableName, "DELETE(target)", ctx.start.getLine());
				}

				String top = "";
				if (ctx.TOP() != null) {
					top = " TOP";
					if (status.equals(u.Supported)) {
						status = featureSupportedInVersion(DeleteStmt,"TOP");
						if (ctx.select_statement() != null) {
							String statusSubq = featureSupportedInVersion(DeleteStmt,"TOP SUBQUERY");
							if (!statusSubq.equals(u.Supported)) {
								captureItem("DELETE TOP(subquery)", tableName, DeleteStmt, "DELETE", statusSubq, ctx.start.getLine());
							}
						}
					}
				}

				String outputClause = getOutputClause(ctx.output_clause(), status, DeleteStmt, "text", tableName);
				status = getOutputClause(ctx.output_clause(), status, DeleteStmt, "status", tableName);

				StringBuilder CTE = new StringBuilder("");
 				status = checkDMLCTE(DeleteStmt, targetTableName, ctx.with_expression(), status, CTE, ctx.start.getLine());

				String whereCurrentOf = "";
				if (ctx.CURRENT() != null) {
					whereCurrentOf = ", WHERE CURRENT OF";
					if (status.equals(u.Supported)) {
						status = featureSupportedInVersion(DeleteStmt,whereCurrentOf);
					}
				}

				if (!updateTVFStatus.equals(u.Supported)) {
					if (updateTVFStatus.equals(u.NotSupported)) status = u.NotSupported;
					else if (!status.equals(u.Supported)) status = updateTVFStatus;
				}

				if (!captureTableSrcDML(ctx.parent, tableName, "DELETE", ctx.start.getLine())) {
					captureItem("DELETE"+top+CTE.toString()+outputClause+whereCurrentOf+functionCall, tableName, DeleteStmt, "DELETE", status, ctx.start.getLine());
					CaptureXMLNameSpaces(ctx.parent, "DELETE", ctx.start.getLine());
				}

				captureUpdDelBugs("DELETE", tableNameRaw, tableName, ctx.table_sources(), ctx.start.getLine());

				captureDMLChangeTrackingContext(DeleteStmt, ctx.start.getLine(), ctx.parent);

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			// Find various UPDATE/DELETE-related Babelfish syntax bugs

			// grab each table plus its correlation name; only for table variables and tables for now
			private List<String> getTableSourceItem(TSQLParser.Table_source_itemContext tsi) {
				List<String> tmp = new ArrayList<>();
				for (TSQLParser.Table_source_itemContext t : tsi.table_source_item()) {
					tmp.addAll(getTableSourceItem(t));
				}
				if (tsi.table_source_item().size() > 0) return tmp;

				String tabName = "";
				String alias = "";
				if (tsi.full_object_name() != null) {
					tabName = u.normalizeName(tsi.full_object_name().getText());
				}
				else if (tsi.LOCAL_ID() != null) {
					tabName = tsi.LOCAL_ID().getText();
				}
				else {
					if ((tsi.ij != null) || (tsi.oj != null) || (tsi.cj != null) || (tsi.lj != null)) {
						tmp.addAll(getTableSourceItem(tsi.table_source_item().get(0)));
						tmp.addAll(getTableSourceItem(tsi.table_source_item().get(1)));
					}
				}
				if (!tabName.isEmpty()) {
					if (tsi.as_table_alias().size() > 0) alias = u.normalizeName(tsi.as_table_alias().get(0).table_alias().getText());
					//u.appOutput(u.thisProc()+"adding: tabName=["+tabName+"] alias=["+alias+"] ");
					tmp.add(tabName.toUpperCase() + u.miscDelimiter + alias.toUpperCase());
				}
				return tmp;
			}

			// where target table is correlation name
			private void captureUpdDelBugs(String stmt, String tableNameRaw, String tableName, TSQLParser.Table_sourcesContext ts, int lineNr) {
				 captureUpdDelBugs(stmt, tableNameRaw, tableName, ts, lineNr, null);
			}
			private void captureUpdDelBugs(String stmt, String tableNameRaw, String tableName, TSQLParser.Table_sourcesContext ts, int lineNr, List<TSQLParser.Update_elemContext> ue) {
				// only go here if explicitly requested
				if (!u.reportSyntaxIssues) return;

				String section = UpdateStmt;
				if (stmt.equals("DELETE")) section = DeleteStmt;
				tableName = u.resolveName(tableName);
				List<TSQLParser.Table_source_itemContext> tabs = new ArrayList<>();
				List<String> tableAlias = new ArrayList<>();
				if (ts != null) {
					tabs = ts.table_source_item();
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+": nr_tabs=["+tabs.size()+"] ", u.debugPtree);
					for (TSQLParser.Table_source_itemContext t : tabs) {
						tableAlias.addAll(getTableSourceItem(t));
					}
				}
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+": stmt=["+stmt+"] tableNameRaw=["+tableNameRaw+"] tableName=["+tableName+"] nr_tabs=["+tabs.size()+"] tableAlias.size()=["+tableAlias.size()+"] tableAlias=["+tableAlias+"]  u.currentObjectType=["+u.currentObjectType+"] ", u.debugPtree);

				if (ts != null) {
					if (u.currentObjectType.equals("FUNCTION") && (tabs.size() > 0) && (!tableNameRaw.startsWith("@"))) {
						// UPDATE x SET.. FROM @tv x: in a SQL function, is incorrectly reported as error 'not allowed in SQL Function'
						// rewrite by directly updating @tv
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+": checking for 'UPDATE x SET.. FROM @tv x' in UDF ", u.debugPtree);
						for (String ta : tableAlias) {
							String tName = ta.substring(0,ta.indexOf(u.miscDelimiter));
							String alias = ta.substring(ta.indexOf(u.miscDelimiter)+u.miscDelimiter.length());
							if (tName.startsWith("@") && tableNameRaw.equalsIgnoreCase(alias)) {
								//u.appOutput(u.thisProc()+"tableNameRaw=["+tableNameRaw+"] tName=["+tName+"] alias=["+alias+"] ");
								// found a case
								String itemChk = stmt+cfgDMLTabVarCorrNameUDFError;
								String item    = stmt+DMLTabVarCorrNameUDFErrorText;
								String status  = featureSupportedInVersion(SyntaxIssues, itemChk);
								if (!status.equals(u.Supported)) {
									captureItem(item, tableNameRaw, SyntaxIssues, tableNameRaw, status, lineNr);
									break;
								}
							}
						}
					}
				}

				if (stmt.equals("UPDATE")) {
					// Qualifying the SET column with the table name raises error 'column "t" of relation "t" does not exist'
					// Same problem when the correlation name is used instead of the table name
					//update t set t.a = 1
					//update x set x.a = 1 from t as x

					// NB: bracketed and quoted identifiers may not be reported correctly
					for (TSQLParser.Update_elemContext e : ue) {
						if (e.full_column_name() != null) {
							String setCol  = e.full_column_name().getText();
							if (!u.getPatternGroup(setCol, "^(\\w+\\.\\w+)$", 1).isEmpty()) {
								// found a case
								String itemChk = cfgUpdateQualifiedSetColumnError;
								String item    = UpdateQualifiedSetColumnErrorText;
								String status  = featureSupportedInVersion(SyntaxIssues, itemChk);
								if (!status.equals(u.Supported)) {
									captureItem(item, setCol, SyntaxIssues, "", status, e.full_column_name().start.getLine());
								}
							}
						}
					}
				}


				if (ts != null) {
					if (stmt.equals("UPDATE") && (tabs.size() == 1)) {
						//The following raises error 'column reference "a" is ambiguous'
						//update x set a = a + 1 from t as x
						// Must either prefix the SET column with the correlation name and update the table directly or remove the FROM clause:
						//update t set a = a + 1
						//update x set a = x.a + 1 from t as x

						// only doing this for single-table queries: the issue only occurs for a column in a table with a correlation name; for a join,
						// we'd have to resolve each unqualified column name to its table, which is beyond what we can currently do

						// NB: bracketed and quoted identifiers may not be reported correctly

						// first determine if it's one table only; it may be 'UPDATE s FROM t [as t1]'
						// if > 1 table, skip the query (see reason above)
						TSQLParser.Table_source_itemContext t = tabs.get(0);
						String tName = "";
						if (t.full_object_name() != null) {
							tName = t.full_object_name().getText();
						}
						else if (t.LOCAL_ID() != null) {
							tName = t.LOCAL_ID().getText();
						}

						boolean skip = false;
						String alias = "";
						if (t.as_table_alias().size() > 0) {
							alias = u.normalizeName(t.as_table_alias().get(0).table_alias().getText());
							if (!alias.equalsIgnoreCase(tableNameRaw)) skip = true;   // don't analyze this query for this specific issue, see reasons above
						}
						//u.appOutput(u.thisProc()+"tName=["+tName+"] alias=["+alias+"] skip=["+skip+"] ");
						if (!skip) {
							if (t.as_table_alias().size() > 0) {
								if (!tName.isEmpty()) {
									// now get the SET clause
									for (TSQLParser.Update_elemContext e : ue) {
										if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+": e=["+getTextSpaced(e)+"]  ", u.debugPtree);
										if ((e.full_column_name() != null) && (e.expression() != null)) {
											String setCol  = e.full_column_name().getText();
											String origSetExpr = getTextSpaced(e.expression());
											String setExpr = " " + origSetExpr  + " ";
											//u.appOutput(u.thisProc()+"setCol=["+setCol+"] setExpr=["+setExpr+"] ");
											if (!u.getPatternGroup(setExpr, "\\b(SELECT)\\b", 1).isEmpty()) {
												// skip expressions with a subquery for now -- too many keywords
												//u.appOutput(u.thisProc()+"skip subquery");
												break;
											}

											// find column ref without correlation name prefix -- strip out everything else. this is the tricky part
											setExpr = u.maskStringConstants(setExpr, "string");
											setExpr = setExpr.replaceAll("'string'", " ");
											setExpr = u.applyPatternAll(setExpr, "\\bAS\\s+\\w+\\b", " ");	// for CAST
											setExpr = u.applyPatternAll(setExpr, "\\b\\d+(\\.\\d+)?\\b", " ");
											setExpr = u.applyPatternAll(setExpr, "\\b0x[0-9a-f]+\\b", " ");
											setExpr = u.applyPatternAll(setExpr, "\\W@\\w+\\b", " ");
											setExpr = u.applyPatternAll(setExpr, "\\b(DATEPART|DATEADD|DATEDIFF|DATENAME)\\s*(\\().*?,", "$1$2");
											setExpr = u.applyPatternAll(setExpr, "\\b(CURRENT_TIMESTAMP|CURRENT_USER|SESSION_USER|SYSTEM_USER|USER)\\b", " ");
											setExpr = u.applyPatternAll(setExpr, "\\b(AND|OR|NOT|IS|NULL|CASE|WHEN|THEN|ELSE|END|IN|EXISTS|ALL|ANY|LIKE|CONTAINS)\\b", " ");

											// NB: need to protect against risk of getting into loops when bracketed identifier contains ( or )
											// For loop detection, string length must change (increase) when making a substitution
											int prevLength = -1;
											while (!u.getPatternGroup(setExpr, "\\w\\s*(\\(.*?\\))", 1).isEmpty()) {
												setExpr = u.applyPatternAll(setExpr, "\\b\\w+(\\.\\w+)?\\s*\\(([^\\(\\)]*?)\\)", "  $2  ");
												if (prevLength == setExpr.length()) {
													// look for pairs of expression brackets inside function arg brackets
													//u.appOutput(u.thisProc()+"nothing stripped, try nested expression");
													setExpr = u.applyPatternAll(setExpr, "(\\w)(\\s+)(\\()(.*)$", "$1 $3 $4$2");
													setExpr = u.applyPatternAll(setExpr, "(^|\\W)\\(([^\\(\\)]*?)\\)", " $1  $2  ");
													setExpr = " " + u.collapseWhitespace(setExpr) + " ";
													//u.appOutput(u.thisProc()+"stripped nested brackets setExpr=["+setExpr+"] ");
													if (prevLength == setExpr.length()) {
														//u.appOutput(u.thisProc()+"still nothing stripped - exiting loop");
														break;
													}
												}
												prevLength = setExpr.length();
											}

											prevLength = -1;
											while (!u.getPatternGroup(setExpr, "(\\(.*?\\))", 1).isEmpty()) {
												setExpr = u.applyPatternAll(setExpr, "\\(([^\\(\\)]*?)\\)", "  $1  ");
												//u.appOutput(u.thisProc()+"stripped B setExpr=["+setExpr+"] ");
												if (prevLength == setExpr.length()) {
													//u.appOutput(u.thisProc()+"still nothing stripped B - exiting loop");
													break;
												}
												prevLength = setExpr.length();
											}

											setExpr = u.applyPatternAll(setExpr, "\\b(VARCHAR|NVARCHAR|CHAR|NCHAR)\\b", " ");
											setExpr = u.applyPatternAll(setExpr, "\\b(\\w+\\.)*"+alias+"\\.\\w+\\b", " ");

											String colFound = u.getPatternGroup(setExpr, "[^\\.\\[]\\b(\\w+)\\b[^\\.\\]]", 1);
											if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+": colFound=["+colFound+"] setExpr=["+setExpr+"]  ", u.debugPtree);
											if (!colFound.isEmpty()) {
												// found a case
												String itemChk = cfgUpdateCorrColumnUnqualifiedError;
												String item    = UpdateCorrColumnUnqualifiedErrorText;
												String status  = featureSupportedInVersion(SyntaxIssues, itemChk);
												if (!status.equals(u.Supported)) {
													captureItem(item, "column '" + colFound+"' for correlation name '"+tableNameRaw+"'", SyntaxIssues, tableNameRaw, status, e.expression().start.getLine());
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}

			@Override public String visitMerge_statement(TSQLParser.Merge_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String status = u.Supported;
				String statusMerge = featureSupportedInVersion(MergeStmt, "MERGE");
				status = statusMerge;

				String tableName = "";
				String targetTableName = "";
				String updateTVFStatus = u.Supported;
				String functionCall = "";
				if (ctx.ddl_object() != null) {
					String tableNameRaw = ctx.ddl_object().getText().toUpperCase();
					tableName = targetTableName = u.normalizeName(tableNameRaw);
					CaptureIdentifier(tableNameRaw, tableName, "MERGE(target)", ctx.start.getLine());
				}
				else if (ctx.function_call() != null) {
					String tableNameRaw = ctx.function_call().func_proc_name_server_database_schema().getText().toUpperCase();
					tableName = u.normalizeName(tableNameRaw);
					updateTVFStatus = featureSupportedInVersion(MergeStmt,"TABLE FUNCTION");
					functionCall = ", on table function";
				}

				String top = "";
				if (ctx.TOP() != null) {
					top = " TOP";
					status = featureSupportedInVersion(MergeStmt,"TOP");
				}

				String outputClause = getOutputClause(ctx.output_clause(), status, MergeStmt, "text", tableName);
				status = getOutputClause(ctx.output_clause(), status, MergeStmt, "status", tableName);

				StringBuilder CTE = new StringBuilder("");
 				status = checkDMLCTE(MergeStmt, targetTableName, ctx.with_expression(), status, CTE, ctx.start.getLine());

				if (!updateTVFStatus.equals(u.Supported)) {
					if (updateTVFStatus.equals(u.NotSupported)) status = u.NotSupported;
					else if (!status.equals(u.Supported)) status = updateTVFStatus;
				}

				if (!captureTableSrcDML(ctx.parent, tableName, "MERGE", ctx.start.getLine())) {
					if ((!statusMerge.equals(u.Supported)) && (ctx.function_call() == null)) {
						if (u.rewrite) {
							String rewriteText = "";
							Integer rwrID = rewriteMerge(ctx);

							if (addRewrite(MergeStmt, ctx.getText(), u.rewriteTypeBlockReplace, rewriteText, ctx.start.getLine(), ctx.start.getCharPositionInLine(), ctx.final_char.getLine(), ctx.final_char.getCharPositionInLine(), ctx.start.getStartIndex(), ctx.final_char.getStopIndex(), rwrID))
								status = u.Rewritten;
						}
						else {
							addRewrite(MergeStmt);
						}
					}
					captureItem("MERGE"+top+CTE.toString()+outputClause+functionCall, tableName, MergeStmt, "MERGE", status, ctx.start.getLine());

					CaptureXMLNameSpaces(ctx.parent, "MERGE", ctx.start.getLine());
				}

				captureDMLChangeTrackingContext(MergeStmt, ctx.start.getLine(), ctx.parent);

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			public Integer rewriteMerge(TSQLParser.Merge_statementContext ctx) {
				Map<String, List<Integer>> positions = new HashMap<>();
				int rwrID = u.rewriteTextListKeys.size() + 1;

				// determine positions of each clause of the MERGE statement
				positions.put("start", Arrays.asList(ctx.start.getStartIndex(), -1));

				String tableName = "";
				if (ctx.ddl_object() != null) {
					positions.put("ddl_object", Arrays.asList(ctx.ddl_object().start.getStartIndex(),ctx.ddl_object().stop.getStopIndex()));
				}

				String tableAlias = "";
				if (ctx.as_table_alias() != null) {
					positions.put("table_alias", Arrays.asList(ctx.as_table_alias().table_alias().start.getStartIndex(),ctx.as_table_alias().table_alias().stop.getStopIndex()));
				}

				String topClause = "";
				if (ctx.TOP() != null) {
					int endIx = ctx.final_char.getStopIndex();
					if (ctx.PERCENT() != null) endIx = ctx.PERCENT().getSymbol().getStopIndex();
					positions.put("top", Arrays.asList(ctx.TOP().getSymbol().getStartIndex(),endIx));
				}

				String withExpr = "";
				if (ctx.with_expression() != null) {
					positions.put("with_expression", Arrays.asList(ctx.with_expression().start.getStartIndex(),ctx.with_expression().stop.getStopIndex()));
				}

				String srcTab = ctx.table_sources().getText();
				positions.put("table_sources", Arrays.asList(ctx.table_sources().start.getStartIndex(),ctx.table_sources().stop.getStopIndex()));

				String searchCond = ctx.search_condition().getText();
				positions.put("search_condition", Arrays.asList(ctx.search_condition().start.getStartIndex(),ctx.search_condition().stop.getStopIndex()));

				List<TSQLParser.When_matchesContext> matches = ctx.when_matches();
				positions.put("when_matches", Arrays.asList(matches.size(), -1));
				for (TSQLParser.When_matchesContext m : matches) {
					String k = m.getText();
					k = u.applyPatternFirst(k, "^(WHEN.*?THEN)(INSERT|UPDATE|DELETE).*$", "$1 $2");
					k = u.applyPatternFirst(k, "BYTARGET", "");
//					u.appOutput(u.thisProc()+"   matches=["+k+"] ");
					positions.put("when_matches " + k, Arrays.asList(m.start.getStartIndex(),m.stop.getStopIndex()));
				}

				String outputClauseText = "";
				if (ctx.output_clause() != null) {
					positions.put("output_clause", Arrays.asList(ctx.output_clause().start.getStartIndex(),ctx.output_clause().stop.getStopIndex()));
				}

				String optionClause = "";
				if (ctx.option_clause() != null) {
					positions.put("option_clause", Arrays.asList(ctx.option_clause().start.getStartIndex(), ctx.option_clause().stop.getStopIndex()));
				}

				u.rewriteIDDetails.put(rwrID, positions);

				return rwrID;
			}

			@Override public String visitTruncate_table(TSQLParser.Truncate_tableContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				// todo: handle ptn clause
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String tableNameRaw = ctx.table_name().getText().toUpperCase();
				String tableName = u.normalizeName(tableNameRaw);
				CaptureIdentifier(tableNameRaw, tableName, "TRUNCATE TABLE", ctx.start.getLine());

				if (ctx.WITH() != null) {
					capturePartitioning("TRUNCATE TABLE", tableName, ctx.start.getLine());
				}

				String status = featureSupportedInVersion(TruncateTableStmt,"TRUNCATE TABLE");
				captureItem("TRUNCATE TABLE", tableName, TruncateTableStmt, "", status, ctx.start.getLine());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			private void capturePartitioning(String stmt, String tableName, int lineNr) {
				capturePartitioning(stmt, stmt, tableName, lineNr, "");
			}
			private void capturePartitioning(String stmt, String stmtFmt, String tableName, int lineNr) {
				capturePartitioning(stmt, stmtFmt, tableName, lineNr, "");
			}
			
			private void capturePartitioning(String stmt, String stmtFmt, String tableName, int lineNr, String dataType) {
				String status = featureSupportedInVersion(Partitioning,stmt);
				String fmt = "Partitioning, "+stmtFmt;
				if (stmt.startsWith("CREATE PARTITION") || stmt.startsWith("ALTER PARTITION") || stmt.startsWith("DROP PARTITION")) fmt = stmtFmt;
				else if (stmt.equals("$PARTITION")) fmt = "$PARTITION.function()";
				else if (stmt.endsWith("SPLIT")) fmt = "ALTER TABLE..SPLIT partition";
				else if (stmt.endsWith("MERGE")) fmt = "ALTER TABLE..MERGE partition";
				else if (stmt.endsWith("SWITCH")) fmt = "ALTER TABLE..SWITCH partition";
				else if (stmt.endsWith("TABLE REBUILD")) fmt = "ALTER TABLE..REBUILD partition";
				else if (stmt.endsWith("INDEX REBUILD")) fmt = "ALTER INDEX..REBUILD partition";
				
				if (status.equals(u.Supported)) {
					if (stmt.startsWith("CREATE PARTITION FUNCTION")) {
						if (dataType.equalsIgnoreCase("SQL_VARIANT")) {
							String statusSV = featureSupportedInVersion(Partitioning,"SQL_VARIANT");
							if (! statusSV.equals(u.Supported)) {
								status = statusSV;
								fmt = "CREATE PARTITION FUNCTION, on SQL_VARIANT datatype";
							}
						}					
					}
				}
				captureItem(fmt, tableName, Partitioning, stmt, status, lineNr);
			}

			@Override public String visitCreate_partition_scheme(TSQLParser.Create_partition_schemeContext ctx) {
				capturePartitioning("CREATE PARTITION SCHEME", u.normalizeName(ctx.partition_scheme_name.getText()), ctx.start.getLine());
				visitChildren(ctx);	return null;
			}

			@Override public String visitAlter_partition_scheme(TSQLParser.Alter_partition_schemeContext ctx) {
				capturePartitioning("ALTER PARTITION SCHEME, NEXT USED", u.normalizeName(ctx.partition_scheme_name.getText()), ctx.start.getLine());
				visitChildren(ctx);	return null;
			}

			@Override public String visitDrop_partition_scheme(TSQLParser.Drop_partition_schemeContext ctx) {
				capturePartitioning("DROP PARTITION SCHEME", u.normalizeName(ctx.partition_scheme_name.getText()), ctx.start.getLine());
				visitChildren(ctx);	return null;
			}

			@Override public String visitCreate_partition_function(TSQLParser.Create_partition_functionContext ctx) {
				String rl = "RIGHT";
				if (ctx.LEFT() != null) rl = "LEFT";

				String dt = ctx.data_type().getText().toUpperCase();
				dt = u.normalizeName(dt, "datatype");

				int nrPfValues = 0;
				if (ctx.expression_list() != null) {  // should never be null, but seen it been empty in customer scripts
					nrPfValues = argListCount(ctx.expression_list());
				}
				String pfValues = ", for " + nrPfValues + " " + dt + " values";				

				capturePartitioning("CREATE PARTITION FUNCTION RANGE "+rl, "CREATE PARTITION FUNCTION, RANGE "+rl+pfValues, u.normalizeName(ctx.partition_function_name.getText()), ctx.start.getLine(), dt);
				visitChildren(ctx);	return null;
			}

			@Override public String visitAlter_partition_function(TSQLParser.Alter_partition_functionContext ctx) {
				String sm = "SPLIT RANGE";
				if (ctx.MERGE() != null) sm = "MERGE RANGE";

				capturePartitioning("ALTER PARTITION FUNCTION, "+sm, u.normalizeName(ctx.partition_function_name.getText()), ctx.start.getLine());
				visitChildren(ctx);	return null;
			}

			@Override public String visitDrop_partition_function(TSQLParser.Drop_partition_functionContext ctx) {
				capturePartitioning("DROP PARTITION FUNCTION", u.normalizeName(ctx.partition_function_name.getText()), ctx.start.getLine());
				visitChildren(ctx);	return null;
			}

			@Override public String visitBreak_statement(TSQLParser.Break_statementContext ctx) {
				captureItem("BREAK", "", ControlFlowReportGroup, "", u.Supported, ctx.start.getLine());
				visitChildren(ctx);	return null;
			}

			@Override public String visitContinue_statement(TSQLParser.Continue_statementContext ctx) {
				captureItem("CONTINUE", "", ControlFlowReportGroup, "", u.Supported, ctx.start.getLine());
				visitChildren(ctx);	return null;
			}

			@Override public String visitUpdate_statistics(TSQLParser.Update_statisticsContext ctx) {
				String status = featureSupportedInVersion(UpdateStatisticsStmt);
				captureItem(UpdateStatisticsStmt, "", UpdateStatisticsStmt, "", status, ctx.start.getLine());

				if (status.equals(u.Supported)) {
					String tableNameRaw = ctx.table_name().getText().toUpperCase();
					String tableName = u.normalizeName(tableNameRaw);
					CaptureIdentifier(tableNameRaw, tableName, "UPDATE STATISTICS", ctx.start.getLine());
				}

				visitChildren(ctx);	return null;
			}

			@Override public String visitWaitfor_statement(TSQLParser.Waitfor_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String type = "DELAY";
				if (ctx.TIME() != null) type = "TIME";
				String status = featureSupportedInVersion(WaitForStmt, type);
				if (type.equals("DELAY")) {
					if (!status.equals(u.Supported)) {
						// convert the T-SQL string to a number of seconds for pg_sleep()
						String arg = ctx.expression().getText();
						boolean isString = false;
						if (isStringConstant(arg)) {
							 isString = true;
						}
						if (u.rewrite) {
							double nrSeconds = 0.0d;
							String nrSecondsStr = "0";
							if (isString) {
								String origTime = u.stripStringQuotes(arg);
								List<String> tmpTime = new ArrayList<>(Arrays.asList(origTime.split(":")));
								boolean isOK = true;
								for (int i=0; i<tmpTime.size(); i++) {
									if (!isNumeric(tmpTime.get(i))) {
										isOK = false;
										break;
									}
									double f = Double.parseDouble(tmpTime.get(i));
									nrSeconds += f * Math.pow(60,2-i) ;
								}
							}
							else {
								// variable or expression
								nrSecondsStr = rewriteWaitforDelay.replaceAll(rewriteTag1, arg);
							}

							if (!isString) nrWaitForDelayRewrites++;
							String waitForDelayVar = "@WAITFORDELAY_VAR" + nrWaitForDelayRewrites;
							String rewriteText = "";
							if (!isString) rewriteText += "BEGIN\nDECLARE "+waitForDelayVar+" NUMERIC(8,3) = "+nrSecondsStr+"\n";
							rewriteText += "EXECUTE pg_sleep ";
							if (isString) rewriteText += nrSeconds;
							else rewriteText += waitForDelayVar;
							if (!isString) rewriteText += "\nEND\n";
							if (addRewrite(WaitForStmt + " DELAY", ctx.getText(), u.rewriteTypeReplace, rewriteText, ctx.start.getLine(), ctx.start.getCharPositionInLine(), ctx.expression().stop.getLine(), ctx.expression().stop.getCharPositionInLine(), ctx.start.getStartIndex(), ctx.expression().stop.getStopIndex()))
								status = u.Rewritten;
						}
						else {
							addRewrite(WaitForStmt + " DELAY");
						}
					}
				}
				captureItem("WAITFOR "+type, "", WaitForStmt, type, status, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitBackup_statement(TSQLParser.Backup_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String cmd = "BACKUP";
				if (ctx.backup_master_key() != null) {
					cmd = "BACKUP MASTER KEY";
				}
				if (ctx.backup_service_master_key() != null) {
					cmd = "BACKUP SERVICE MASTER KEY";
				}
				else if (ctx.backup_symmetric_key() != null) {
					cmd = "BACKUP SYMMETRIC KEY";
				}
				else if (ctx.backup_certificate() != null) {
					cmd = "BACKUP CERTIFICATE";
				}
				String cmdFormat = cmd;
				if (cmd.equals("BACKUP")) cmdFormat = "BACKUP DATABASE/LOG";
				String status = featureSupportedInVersion(DBAStmts, cmd);
				captureItem(cmdFormat, "", DBAStmts, cmd, status, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitRestore_statement(TSQLParser.Restore_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String cmd = "RESTORE";
				if (ctx.restore_symmetric_key() != null) {
					cmd = "RESTORE SYMMETRIC KEY";
				}
				String cmdFormat = cmd;
				if (cmd.equals("RESTORE")) cmdFormat = "RESTORE DATABASE/LOG";
				String status = featureSupportedInVersion(DBAStmts, cmd);
				captureItem(cmdFormat, "", DBAStmts, cmd, status, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitKill_statement(TSQLParser.Kill_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String killType = "";
				String killTypeFmt = "";
				if (ctx.kill_process() != null) {
					if (ctx.kill_process().DECIMAL() != null) {
						killType += " SPID";
					}
					if (ctx.kill_process().UOW() != null) {
						killType += " UOW";
					}
					if (ctx.kill_process().char_string() != null) {
						killType += " SESSION-ID-STRING";
					}
					if (ctx.kill_process().STATUSONLY() != null) {
						killType += " WITH STATUSONLY";
					}
				}
				else if (ctx.kill_query_notification() != null) {
					killType += " QUERY NOTIFICATION";
				}
				else if (ctx.kill_stats_job() != null) {
					killType += " STATS JOB";
				}
				killType = "KILL " + killType.trim();
				killType = killType.trim();
				killTypeFmt = u.escapeHTMLChars(killType.replace("SPID", "<spid>"));
				String status = featureSupportedInVersion(DBAStmts, killType);
				captureItem(killTypeFmt, "", DBAStmts, killTypeFmt, status, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitCheckpoint_statement(TSQLParser.Checkpoint_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String status = featureSupportedInVersion(CheckpointStmt);
				captureItem(CheckpointStmt, "", CheckpointStmt, "", status, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitIf_statement(TSQLParser.If_statementContext ctx) {
				captureItem("IF", "", ControlFlowReportGroup, "", u.Supported, ctx.start.getLine());

				if (ctx.sql_clauses().get(0).another_statement() != null) {
					if (ctx.sql_clauses().get(0).another_statement().declare_statement() != null) {
						// an IF-block with only a DECLARE -- that's weird, but it happens: review semantics
						String status = featureSupportedInVersion(IFELSEblockDeclare);
						if (!status.equals(u.Supported)) {
							captureItem(IFblockDeclare, "", ControlFlowReportGroup, "", status, ctx.sql_clauses().get(0).start.getLine());
						}
					}
				}
				if (ctx.ELSE() != null) {
					if (ctx.sql_clauses().get(1).another_statement() != null) {
						if (ctx.sql_clauses().get(1).another_statement().declare_statement() != null) {
							// an ELSE-block with only a DECLARE -- that's weird, but it happens: review semantics
							String status = featureSupportedInVersion(IFELSEblockDeclare);
							if (!status.equals(u.Supported)) {
								captureItem(ELSEblockDeclare, "", ControlFlowReportGroup, "", status, ctx.sql_clauses().get(1).start.getLine());
							}
						}
					}
				}

				visitChildren(ctx);
				return null;
			}

			@Override public String visitWhile_statement(TSQLParser.While_statementContext ctx) {
				captureItem("WHILE", "", ControlFlowReportGroup, "", u.Supported, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitPrint_statement(TSQLParser.Print_statementContext ctx) {
				captureItem("PRINT", "", ControlFlowReportGroup, "", u.Supported, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitRaiserror_statement(TSQLParser.Raiserror_statementContext ctx) {
				captureItem("RAISERROR", "", ControlFlowReportGroup, "", u.Supported, ctx.start.getLine());
				String msg = ctx.msg.getText();
				Integer exprInt	= getIntegerConstant(msg, true);
				if (exprInt != null) {
					captureAtAtErrorValue(exprInt, ", via RAISERROR()", "", ctx.start.getLine());
				}
				else {
					// cannot determine the value being compared against
				}
				visitChildren(ctx);
				return null;
			}

			@Override public String visitRaiserror_statement_sybase(TSQLParser.Raiserror_statement_sybaseContext ctx) {
				captureItem(RaiserrorSybase, "", ControlFlowReportGroup, "", u.NotSupported, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitThrow_statement(TSQLParser.Throw_statementContext ctx) {
				//THROW 51515, 'EHR is disabled', 1;
				String xtra = "";
				String errno = "";
				if (ctx.throw_error_number() != null) {
					errno = ctx.throw_error_number().getText();
					Integer exprInt	= getIntegerConstant(errno, true);
					xtra = u.escapeHTMLChars(" <error-nr>");
					if (exprInt != null) {
						captureAtAtErrorValue(exprInt, ", via THROW", "", ctx.start.getLine());
					}
					else {
						// cannot determine the value being compared against -- should not happen for THROW...
						// ...but if it does, keep message set above
					}
				}
				captureItem("THROW"+xtra, errno, ControlFlowReportGroup, "", u.Supported, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitTry_catch_statement(TSQLParser.Try_catch_statementContext ctx) {
				captureItem("TRY-CATCH", "", ControlFlowReportGroup, "", u.Supported, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitGoto_statement(TSQLParser.Goto_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String fmt = "";
				if (ctx.GOTO() != null) fmt = "GOTO label";
				else fmt = "label: (for GOTO)";
				String status = featureSupportedInVersion(GotoStmt);
				captureItem(fmt, "", GotoStmt, "", status, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitSystem_versioning_column(TSQLParser.System_versioning_columnContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				// ToDo: handle ALTER TABLE
				hasSystemVersioningColumn = true;
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitReturn_statement(TSQLParser.Return_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String context = "";
				if (u.currentObjectType.equals("PROCEDURE")) {
					if (ctx.expression() != null) context = " integer, in procedure";
					else context = ", in procedure";
				}
				else if (u.currentObjectType.equals("FUNCTION")) {
					if (hasParent(ctx.parent,"func_body_returns_scalar")) context = " scalar, in function";
					else context = " result set, in function";
				}
				else if (u.currentObjectType.equals("TRIGGER")) {
					context = ", in trigger";
				}
				captureItem("RETURN"+context, "", ControlFlowReportGroup, "RETURN", u.Supported, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitWith_expression(TSQLParser.With_expressionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				if (ctx.XMLNAMESPACES() != null) {
					CaptureXMLNameSpaces(null, "", ctx.start.getLine());
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitDeclare_cursor(TSQLParser.Declare_cursorContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String cursorName = u.normalizeName(ctx.cursor_name().getText().toUpperCase());
				hasDeclareCursorName.add(cursorName);
				//u.appOutput(u.thisProc()+"cursorName=["+cursorName+"] hasDeclareCursorName=["+hasDeclareCursorName+"] ");

				if (ctx.INSENSITIVE() != null)
					captureOption(CursorOptions, "INSENSITIVE", ctx.start.getLine());

				if (ctx.SCROLL() != null)
					captureOption(CursorOptions, "SCROLL", ctx.start.getLine());

				if (ctx.READ() != null)
					captureOption(CursorOptions, "READ_ONLY", ctx.start.getLine());

				if (ctx.UPDATE() != null)
					captureOption(CursorOptions, "FOR UPDATE", ctx.start.getLine());

				List<TSQLParser.Declare_cursor_optionsContext> optionsRaw = ctx.declare_cursor_options();
				for (TSQLParser.Declare_cursor_optionsContext optionX : optionsRaw) {
					String option = optionX.getText().toUpperCase();
					String status = "";
					if (option.equals("GLOBAL")) {
						captureOption(CursorGlobal, "DECLARE", ctx.start.getLine());
					}
					else {
						captureOption(CursorOptions, option, ctx.start.getLine());
					}
				}
				captureItem("DECLARE CURSOR", "", CursorsReportGroup, "", u.Supported, ctx.start.getLine());
				captureItem("CREATE CURSOR", "", u.ObjCountOnly, "", u.ObjCountOnly, ctx.start.getLine(), "0");

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitFetch_cursor(TSQLParser.Fetch_cursorContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
 				String stmt = "FETCH";
 				String kwd = "";
				if (ctx.NEXT() != null) kwd = "NEXT";
				else if (ctx.PRIOR() != null) kwd = "PRIOR";
				else if (ctx.FIRST() != null) kwd = "FIRST";
				else if (ctx.LAST() != null) kwd = "LAST";
				else if (ctx.ABSOLUTE() != null) kwd = "ABSOLUTE";
				else if (ctx.RELATIVE() != null) kwd = "RELATIVE";

				String status = u.Supported;
				if (!kwd.isEmpty()) {
					status = featureSupportedInVersion(CursorFetch, kwd);
				}
				captureItem(stmt + " " +kwd, "", CursorsReportGroup, "", status, ctx.start.getLine());

				if (ctx.GLOBAL() != null) {
					String statusG = featureSupportedInVersion(CursorGlobal, stmt);
					captureItem("GLOBAL option for " + stmt, stmt, CursorsReportGroup, stmt, statusG, ctx.start.getLine());
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitCursor_statement(TSQLParser.Cursor_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if ((ctx.declare_cursor() == null) && (ctx.fetch_cursor() == null)) {
	 				String stmt = "";
					if (ctx.OPEN() != null) stmt = "OPEN";
					if (ctx.CLOSE() != null) stmt = "CLOSE";
					if (ctx.DEALLOCATE() != null) stmt = "DEALLOCATE CURSOR";
					captureItem(stmt, "", CursorsReportGroup, "", u.Supported, ctx.start.getLine());

					if (ctx.GLOBAL() != null) {
						String statusG = featureSupportedInVersion(CursorGlobal, stmt);
						captureItem("GLOBAL option for " + stmt, stmt, CursorsReportGroup, stmt, statusG, ctx.start.getLine());
					}

					// When OPEN is found, check if there is a declare_cursor before it in the same block
					// This is only 99.9% accurate since theoretically there could be control flow that puts the DECLARE after the OPEN
					// but executes the DECLARE before the OPEN. We'll take that risk.
					if (ctx.OPEN() != null) {
						String cursorName = u.normalizeName(ctx.cursor_name().getText());
						boolean doCapture = false;
						if (cursorName.charAt(0) != '@') {  // skip cursor variables for this test
							if (u.debugging) u.dbgOutput(u.thisProc()+"OPEN found for cursorName=["+cursorName+"]; hasDeclareCursorName.size()=["+hasDeclareCursorName.size()+"] hasDeclareCursorName=["+hasDeclareCursorName+"] ", u.debugPtree);
							if (hasDeclareCursorName.size() == 0) {
								// no DECLARE at all
								doCapture = true;
								if (u.debugging) u.dbgOutput(u.thisProc()+"OPEN found for cursorName=["+cursorName+"] without DECLARE CURSOR, u.currentObjectName=["+u.currentObjectName+"] ", u.debugPtree);
							}
							else if (!hasDeclareCursorName.contains(cursorName.toUpperCase())) {
								// DECLARE is for a different cursor than OPEN
								doCapture = true;
								if (u.debugging) u.dbgOutput(u.thisProc()+"OPEN found for cursorName=["+cursorName+"] without DECLARE CURSOR for this cursor, u.currentObjectName=["+u.currentObjectName+"] ", u.debugPtree);
							}

							if (doCapture) {
								String status = featureSupportedInVersion(DynamicCreateCursor);
								captureItem(DynamicCreateCursor, cursorName, CursorsReportGroup, cursorName, status, ctx.start.getLine());
							}
						}
					}
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitReconfigure_statement(TSQLParser.Reconfigure_statementContext ctx) {
				String override = "";
				if (ctx.OVERRIDE() != null) override = " WITH OVERRIDE";
				String status = featureSupportedInVersion(DBAStmts, "RECONFIGURE");
				captureItem("RECONFIGURE"+override, "", DBAStmts, "RECONFIGURE", status, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitShutdown_statement(TSQLParser.Shutdown_statementContext ctx) {
				String status = featureSupportedInVersion(DBAStmts, "SHUTDOWN");
				captureItem("SHUTDOWN", "", DBAStmts, "SHUTDOWN", status, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitSetuser_statement(TSQLParser.Setuser_statementContext ctx) {
				String status = featureSupportedInVersion(SetuserStmt, "SHUTDOWN");
				captureItem(SetuserStmt, "", SetuserStmt, "", status, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitAlter_server_configuration(TSQLParser.Alter_server_configurationContext ctx) {
				String status = featureSupportedInVersion(AlterServerConfig);
				captureItem(AlterServerConfig, "", AlterServerConfig, "", status, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitAdd_signature_statement(TSQLParser.Add_signature_statementContext ctx) {
				String status = featureSupportedInVersion(AddSignature);
				captureItem(AddSignature, "", AddSignature, "", status, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitDrop_signature_statement(TSQLParser.Drop_signature_statementContext ctx) {
				String status = featureSupportedInVersion(DropSignature);
				captureItem(DropSignature, "", DropSignature, "", status, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitAdd_sensitivity_classification(TSQLParser.Add_sensitivity_classificationContext ctx) {
				String status = featureSupportedInVersion(AddSensitivityClass);
				captureItem(AddSensitivityClass, "", AddSensitivityClass, "", status, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitDrop_sensitivity_classification(TSQLParser.Drop_sensitivity_classificationContext ctx) {
				String status = featureSupportedInVersion(DropSensitivityClass);
				captureItem(DropSensitivityClass, "", DropSensitivityClass, "", status, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitCreate_materialized_view(TSQLParser.Create_materialized_viewContext ctx) {
				String status = featureSupportedInVersion(MaterializedView);
				captureItem("CREATE MATERIALIZED VIEW", "", MaterializedView, "", status, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			@Override public String visitAlter_materialized_view(TSQLParser.Alter_materialized_viewContext ctx) {
				String status = featureSupportedInVersion(MaterializedView);
				captureItem("ALTER MATERIALIZED VIEW", "", MaterializedView, "", status, ctx.start.getLine());
				visitChildren(ctx);
				return null;
			}

			// generic test-and-capture in case of 'options'
			private void captureOption(String section, String option, int lineNr) {
				captureOption(section, option, lineNr, "", "", "");
			}
			private void captureOption(String section, String option, int lineNr, String fmt2) {
				captureOption(section, option, lineNr, fmt2, "", "");
			}
			private void captureOption(String section, String option, int lineNr, String fmt2, String fmt3, String sectionReport) {
				//u.appOutput(CompassUtilities.thisProc()+"section=["+section+"] option=["+option+"] fmt2=["+fmt2+"] fmt3=["+fmt3+"] sectionReport=["+sectionReport+"]  ");
				if (option.trim().isEmpty()) return;
				String fmt = section;
				if (fmt.endsWith("options")) fmt = CompassUtilities.removeLastChar(fmt);
				String status = featureSupportedInVersion(section, option);
				if (!fmt3.isEmpty()) fmt = fmt3;
				if (!sectionReport.isEmpty()) section = sectionReport;
				captureItem(fmt+" " + option+fmt2, option, section, option, status, lineNr);
			}

			// Generic catchall, called only for rules named '{create|alter|drop}_stmt'
			private String captureSimpleStmt (String ruleName, ParseTree ctx, int lineNr) {
				return captureSimpleStmt(ruleName, ctx, lineNr, "", "");
			}
			private String captureSimpleStmt (String ruleName, ParseTree ctx, int lineNr, String objectName, String misc) {
				String kwd = u.getPatternGroup(getTextSpaced(ctx), "\\b(CREATE|ALTER|DROP)\\b", 1).toUpperCase();
				ruleName = ruleName.replaceFirst("create_or_alter", kwd);
				List<String> words = new ArrayList<>(Arrays.asList(ruleName.toUpperCase().split("_")));
				words.remove(0);
				String obj = String.join(" ", words);
				String status = featureSupportedInVersion(MiscObjects, obj);
				u.appOutput(u.thisProc()+"obj=["+obj+"] status=["+status+"] ");
				captureItem(kwd + " " + obj + misc, objectName, MiscObjects, obj, status, lineNr);
				if (!kwd.equals("CREATE") && !kwd.equals("DROP")) {
					captureItem("CREATE " + obj, objectName, "", "", u.ObjCountOnly, 0, 0);
				}
				return status;
			}

			@Override public String visitAlter_login(TSQLParser.Alter_loginContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
 				String name = u.normalizeName(ctx.login_name.getText(), ", in ALTER LOGIN");

				if (ctx.ENABLE() != null)
					captureOption(LoginOptions, "ENABLE", ctx.start.getLine(), ", in ALTER LOGIN");

				if (ctx.DISABLE() != null)
					captureOption(LoginOptions, "DISABLE", ctx.start.getLine(), ", in ALTER LOGIN");

				if (ctx.ADD() != null)
					captureOption(LoginOptions, "ADD CREDENTIAL", ctx.start.getLine(), ", in ALTER LOGIN");

				if (ctx.DROP() != null)
					captureOption(LoginOptions, "DROP CREDENTIAL", ctx.start.getLine(), ", in ALTER LOGIN");

				if (ctx.DROP() != null)
					captureOption(LoginOptions, "DROP CREDENTIAL", ctx.start.getLine(), ", in ALTER LOGIN");


				List<TSQLParser.Alter_login_set_optionContext> options = ctx.alter_login_set_option();
				for (TSQLParser.Alter_login_set_optionContext optionX : options) {
					String option = optionX.getText().toUpperCase();
					String optionValue = getOptionValue(option);
					option = getOptionName(option);
					if (option.equals("NOCREDENTIAL")) option = "NO CREDENTIAL";
					if (option.equals("PASSWORD")) {
						if (optionX.HASHED() != null) {
							option = "PASSWORD HASHED";
						}
						else {
							option = "PASSWORD";
						}
					}
					captureOption(LoginOptions, option, ctx.start.getLine(), ", in ALTER LOGIN");

					if (optionX.MUST_CHANGE().size() > 0)
						captureOption(LoginOptions, "MUST_CHANGE", ctx.start.getLine(), ", in ALTER LOGIN");

					if (optionX.UNLOCK().size() > 0)
						captureOption(LoginOptions, "UNLOCK", ctx.start.getLine(), ", in ALTER LOGIN");

					if (optionX.OLD_PASSWORD() != null)
						captureOption(LoginOptions, "OLD_PASSWORD", ctx.start.getLine(), ", in ALTER LOGIN");

					if (optionX.DEFAULT_LANGUAGE() != null) {
						captureOption("SET LANGUAGE", optionValue, ctx.start.getLine(), ", in ALTER LOGIN", "DEFAULT_LANGUAGE=", UsersReportGroup);
					}
				}

				captureItem("ALTER LOGIN", name, UsersReportGroup, "", u.Supported, ctx.start.getLine());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitCreate_login(TSQLParser.Create_loginContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
 				String name = u.normalizeName(ctx.login_name.getText());

				if (ctx.HASHED() != null)
					captureOption(LoginOptions, "PASSWORD HASHED", ctx.start.getLine(), ", in CREATE LOGIN");

				if (ctx.MUST_CHANGE() != null)
					captureOption(LoginOptions, "MUST_CHANGE", ctx.start.getLine(), ", in CREATE LOGIN");

				if (ctx.WINDOWS() != null)
					captureOption(LoginOptions, "FROM WINDOWS", ctx.start.getLine(), ", in CREATE LOGIN");

				if (ctx.CERTIFICATE() != null)
					captureOption(LoginOptions, "FROM CERTIFICATE", ctx.start.getLine(), ", in CREATE LOGIN");

				if (ctx.KEY() != null)
					captureOption(LoginOptions, "FROM ASYMMETRIC KEY", ctx.start.getLine(), ", in CREATE LOGIN");

				List<TSQLParser.Create_login_option_listContext> options = ctx.create_login_option_list();
				for (TSQLParser.Create_login_option_listContext optionX : options) {
					String option = optionX.getText().toUpperCase();
					option = getOptionName(option);
					captureOption(LoginOptions, option, ctx.start.getLine(), ", in CREATE LOGIN");
				}

				captureItem("CREATE LOGIN", name, UsersReportGroup, "", u.Supported, ctx.start.getLine());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitCreate_user(TSQLParser.Create_userContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				//ToDO: detect special cases of users, like for contained databases
 				String name = "";
 				if (ctx.user_name != null) name = u.normalizeName(ctx.user_name.getText());

 				// find out if USER objects are supported at all
 				String status = featureSupportedInVersion(MiscObjects, "USER");
 				captureItem("CREATE USER", name, UsersReportGroup, "", status, ctx.start.getLine());

 				if (status.equals(u.Supported)) {
					if (ctx.ALLOW_ENCRYPTED_VALUE_MODIFICATIONS().size() > 0)
						captureOption(UserOptions, "ALLOW_ENCRYPTED_VALUE_MODIFICATIONS", ctx.start.getLine(), ", in CREATE USER");

					if (ctx.DEFAULT_SCHEMA().size() > 0)
						captureOption(UserOptions, "DEFAULT_SCHEMA", ctx.start.getLine(), ", in CREATE USER");

					if (ctx.DEFAULT_LANGUAGE().size() > 0)
						captureOption(UserOptions, "DEFAULT_LANGUAGE", ctx.start.getLine(), ", in CREATE USER");

					if (ctx.WITHOUT() != null)
						captureOption(UserOptions, "WITHOUT LOGIN", ctx.start.getLine(), ", in CREATE USER");
					else if (ctx.LOGIN() != null)
						if (ctx.FROM() != null)
							captureOption(UserOptions, "FROM LOGIN", ctx.start.getLine(), ", in CREATE USER");
						else
							captureOption(UserOptions, "FOR LOGIN", ctx.start.getLine(), ", in CREATE USER");

					if (ctx.CERTIFICATE() != null)
						captureOption(UserOptions, "FROM CERTIFICATE", ctx.start.getLine(), ", in CREATE USER");

					if (ctx.KEY() != null)
						captureOption(UserOptions, "FROM ASYMMETRIC KEY", ctx.start.getLine(), ", in CREATE USER");
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitAlter_user(TSQLParser.Alter_userContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				//ToDO: detect special cases of users, like for contained databases
 				String name = u.normalizeName(ctx.username.getText());

 				// find out if USER objects are supported at all
 				String status = featureSupportedInVersion(MiscObjects, "USER");
 				captureItem("ALTER USER", name, UsersReportGroup, "", status, ctx.start.getLine());

 				if (status.equals(u.Supported)) {
					if (ctx.ALLOW_ENCRYPTED_VALUE_MODIFICATIONS().size() > 0)
						captureOption(UserOptions, "ALLOW_ENCRYPTED_VALUE_MODIFICATIONS", ctx.start.getLine(), ", in ALTER USER");

					if (ctx.DEFAULT_SCHEMA().size() > 0)
						captureOption(UserOptions, "DEFAULT_SCHEMA", ctx.start.getLine(), ", in ALTER USER");

					if (ctx.DEFAULT_LANGUAGE().size() > 0)
						captureOption(UserOptions, "DEFAULT_LANGUAGE", ctx.start.getLine(), ", in ALTER USER");

					if (ctx.NAME().size() > 0)
						captureOption(UserOptions, "WITH NAME", ctx.start.getLine(), ", in ALTER USER");

					if (ctx.LOGIN().size() > 0)
						captureOption(UserOptions, "WITH LOGIN", ctx.start.getLine(), ", in ALTER USER");

					if (ctx.PASSWORD().size() > 0)
						captureOption(UserOptions, "PASSWORD", ctx.start.getLine(), ", in ALTER USER");

					if (ctx.OLD_PASSWORD().size() > 0)
						captureOption(UserOptions, "OLD_PASSWORD", ctx.start.getLine(), ", in ALTER USER");
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitCreate_db_role(TSQLParser.Create_db_roleContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				String name = u.normalizeName(ctx.role_name.getText());
				String fmtRole = "<dbrole>";
 				// find out if CREATE ROLE is supported at all
 				String status = featureSupportedInVersion(CreateDbRole);
 				captureItem(CreateDbRole+" "+u.escapeHTMLChars(fmtRole), name, UsersReportGroup, "", status, ctx.start.getLine());

				if (status.equals(u.Supported)) {
					if (ctx.AUTHORIZATION() != null)
						captureOption(DbRoleOptions, "AUTHORIZATION", ctx.start.getLine(), ", in "+CreateDbRole);
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitAlter_db_role(TSQLParser.Alter_db_roleContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

 				String name = u.normalizeName(ctx.role_name.getText().toLowerCase());
 				String memberName = u.normalizeName(ctx.database_principal.getText().toLowerCase());

 				// find out if ALTER ROLE is supported for this release
 				String status = featureSupportedInVersion(AlterDbRole);
				String fmtRole = "<dbrole>";

				// predefined role ?
				if (featureExists(DbRoles, name)) {
					fmtRole = name;

					String roleStatus = featureSupportedInVersion(DbRoles, name);
					if (status.equals(u.Supported)) {
						if (!roleStatus.equals(u.Supported)) status = roleStatus;
					}
				}

				String option = "";
				if (ctx.ADD() != null) option = "ADD MEMBER";
				if (ctx.DROP() != null) option = "DROP MEMBER";
				if (ctx.NAME() != null) option = "WITH NAME";

 				if (status.equals(u.Supported)) {
 					String optionStatus = featureSupportedInVersion(DbRoleOptions, option);
					if (status.equals(u.Supported)) {
						if (!optionStatus.equals(u.Supported)) status = optionStatus;
					}
				}

				captureItem(AlterDbRole+" "+ u.escapeHTMLChars(fmtRole) + " " + option, u.escapeHTMLChars(memberName), UsersReportGroup, "", status, ctx.start.getLine());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitDrop_db_role(TSQLParser.Drop_db_roleContext ctx) {
				List<String> nameList = new ArrayList<>();
				nameList.add(u.normalizeName(ctx.id().getText()));
				captureDropObject("ROLE", 1, nameList, ctx.if_exists(), UsersReportGroup, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_server_role(TSQLParser.Create_server_roleContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				String name = u.normalizeName(ctx.server_role_name.getText());

 				// find out if CREATE SERVER ROLE is supported at all
 				String status = featureSupportedInVersion(CreateSrvRole);
				String fmtRole = "<srvrole>";
 				captureItem(CreateSrvRole+" "+u.escapeHTMLChars(fmtRole), name, UsersReportGroup, "", status, ctx.start.getLine());
				if (status.equals(u.Supported)) {
					if (ctx.AUTHORIZATION() != null)
						captureOption(SrvRoleOptions, "AUTHORIZATION", ctx.start.getLine(), ", in "+CreateSrvRole+" "+u.escapeHTMLChars(fmtRole));
				}

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitAlter_server_role(TSQLParser.Alter_server_roleContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String name = u.normalizeName(ctx.server_role_name.getText().toLowerCase());

 				// find out if ALTER SERVER ROLE is supported for this role
 				String status = featureSupportedInVersion(AlterSrvRole,name);
 				String fmtRole = "<srvrole>";

 				// predefined role ?
				if (featureExists(AlterSrvRole, name)) {
					fmtRole = name;

					String roleStatus = featureSupportedInVersion(AlterSrvRole, name);
					if (status.equals(u.Supported)) {
						if (!roleStatus.equals(u.Supported)) status = roleStatus;
					}
				}

				String option = "";
				if (ctx.ADD() != null) option = "ADD MEMBER";
				if (ctx.DROP() != null) option = "DROP MEMBER";
				if (ctx.NAME() != null) option = "WITH NAME";

 				if (status.equals(u.Supported)) {
 					String optionStatus = featureSupportedInVersion(SrvRoleOptions, option);
					if (status.equals(u.Supported)) {
						if (!optionStatus.equals(u.Supported)) status = optionStatus;
					}
				}
				captureItem(AlterSrvRole+" "+u.escapeHTMLChars(fmtRole) + " " + option, name, UsersReportGroup, "", status, ctx.start.getLine());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitDrop_server_role(TSQLParser.Drop_server_roleContext ctx) {
				List<String> nameList = new ArrayList<>();
				nameList.add(u.normalizeName(ctx.id().getText()));
				captureDropObject("SERVER ROLE", 1, nameList, null, UsersReportGroup, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_schema(TSQLParser.Create_schemaContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

 				String name = "";
 				if (ctx.schema_name != null) name = u.normalizeName(ctx.schema_name.getText());
 				captureItem("CREATE SCHEMA", name, UsersReportGroup, "", u.Supported, ctx.start.getLine());

				if (ctx.AUTHORIZATION() != null)
					captureOption(SchemaOptions, "AUTHORIZATION", ctx.start.getLine(), ", in CREATE SCHEMA");

				if (ctx.create_table().size() > 0)
					captureOption(SchemaOptions, "CREATE TABLE", ctx.start.getLine(), ", in CREATE SCHEMA");

				if (ctx.create_or_alter_view().size() > 0)
					captureOption(SchemaOptions, "CREATE VIEW", ctx.start.getLine(), ", in CREATE SCHEMA");

				if (ctx.grant_statement().size() > 0)
					captureOption(SchemaOptions, "GRANT", ctx.start.getLine(), ", in CREATE SCHEMA");

				if (ctx.revoke_statement().size() > 0)
					captureOption(SchemaOptions, "REVOKE", ctx.start.getLine(), ", in CREATE SCHEMA");

				if (ctx.deny_statement().size() > 0)
					captureOption(SchemaOptions, "DENY", ctx.start.getLine(), ", in CREATE SCHEMA");

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitAlter_schema(TSQLParser.Alter_schemaContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				String name = u.normalizeName(ctx.schema_name.getText());

 				// find out if ALTER SCHEMA is supported at all
 				String status = featureSupportedInVersion(AlterSchema);
 				captureItem(AlterSchema, name, UsersReportGroup, "", status, ctx.start.getLine());
 				if (status.equals(u.Supported)) {
					if (ctx.OBJECT() != null)
						captureOption(SchemaOptions, "TRANSFER OBJECT", ctx.start.getLine(), ", in ALTER SCHEMA");

					else if (ctx.TYPE() != null)
						captureOption(SchemaOptions, "TRANSFER TYPE", ctx.start.getLine(), ", in ALTER SCHEMA");

					else if (ctx.XML() != null)
						captureOption(SchemaOptions, "TRANSFER XML SCHEMA COLLECTION", ctx.start.getLine(), ", in ALTER SCHEMA");

					else
						captureOption(SchemaOptions, "TRANSFER", ctx.start.getLine(), ", in ALTER SCHEMA");
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}


			private void capturePermissions(String stmt, TSQLParser.PermissionsContext permissions, String onObject, String grantee, String grantOption, String asGrantor, String cascade, int lineNo) {
				String permRaw = "";
				if (permissions != null) {
					permRaw = getTextSpaced(permissions).toUpperCase().trim();  // this may be a list
				}
				if ((" " + permRaw + " ").indexOf(" ALL ") > -1) permRaw = "ALL PRIVILEGES";

				if (u.debugging) u.dbgOutput(u.thisProc()+"stmt=["+stmt+"] permRaw=["+permRaw+"] onObject=["+onObject+"] grantee=["+grantee+"] grantOption=["+grantOption+"] asGrantor=["+asGrantor+"] cascade=["+cascade+"] ", u.debugPtree);
				String statusStmt = featureSupportedInVersion(stmt);
				String status = u.NotSupported;
				permRaw = u.applyPatternAll(permRaw, "\\(.+?\\)", "(col)");

				List<String> permList = new ArrayList<String>(Arrays.asList(permRaw.split(",")));
				onObject = u.encodeIdentifier(onObject);
				for (String perm : permList.stream().sorted().collect(Collectors.toList())) {
					if (perm.trim().isEmpty()) continue;
					// this may no longer be needed to space the keywords:
					perm = u.applyPatternAll(perm, "(CREATE|DROP|ALTER|SELECT|VIEW|ANY|COLUMN|ENCRYPTION|MASTER|KEY|DEFINITION|CONNECT|DATABASE|SCHEMA|TRIGGER|DDL|MESSAGE|SERVICE|BINDING|EVENT|OWNERSHIP|BACKUP|RESTORE|NOTIFICATION(S)?|FULLTEXT|CATALOG|SUBSCRIBE|QUERY|XML|COLLECTION|SESSION|SERVER|AVAILABILITY|ACCESS|USER|ASSEMBLY|BULK|PRIVILEGES|APPLICATION)", " $1 ");
					perm = perm.trim();
					if (!perm.contains("(")) {
						if (onObject.endsWith(")")) {
							perm += "(col)";
							onObject = u.applyPatternAll(onObject, "^(.*)\\(.*?\\)$", "$1");
						}
					}
					String onObjectFmt = u.applyPatternAll(onObject, "(^\\w+::).*$", "$1").toUpperCase();
					if (onObjectFmt.equals("OBJECT::")) onObjectFmt = "object";
					else if (!u.getPatternGroup(onObjectFmt, "^(\\w+)$", 1).isEmpty()) onObjectFmt = "object";
					if (!onObjectFmt.isEmpty()) onObjectFmt = "ON " + onObjectFmt;

					perm = perm.replaceAll("\\(col\\)", "(column)");
					perm = perm.replaceAll(" \\(", "(");
					perm = u.applyPatternAll(perm, "^ALL\\s+PRIVILEGES$", "ALL");
					perm = u.applyPatternAll(perm, "^EXEC$", "EXECUTE");
					perm = u.collapseWhitespace(perm);

					if (!u.getPatternGroup(perm.trim(), "^(ALL|ALTER|EXECUTE|SELECT|INSERT|UPDATE|DELETE|REFERENCES|VIEW DEFINITION)\\b", 1).isEmpty()) {
						if (onObject.isEmpty()) {
							// applies to DATABASE::
							onObjectFmt = "(on database)";
							onObject = "ON DATABASE::";
						}
					}

					perm = u.applyPatternAll(perm, "^ALL$", "ALL PRIVILEGES");
					if (u.debugging) u.dbgOutput(u.thisProc()+"stmt=["+stmt+"]  perm=["+perm+"] status=["+status+"] ", u.debugPtree);
					String reportPerm = perm;

					reportPerm = reportPerm + " " + onObjectFmt;
					String validatePerm = reportPerm;
					reportPerm = stmt + " " + reportPerm + " " + grantOption;
					reportPerm = u.collapseWhitespace(reportPerm);

					validatePerm = u.applyPatternAll(validatePerm, "[\\(\\)]", "");
					validatePerm = u.collapseWhitespace(validatePerm);
					if (statusStmt.equals(u.Supported)) {
						validatePerm = validatePerm.replaceAll("\\(column\\)", "");
						validatePerm = validatePerm.replaceAll("::", "");
						status = featureSupportedInVersion(stmt, validatePerm);
						if (u.debugging) u.dbgOutput(u.thisProc()+"stmt=["+stmt+"]  validatePerm=["+validatePerm+"]  status=["+status+"] ", u.debugPtree);
					}
	 				captureItem(reportPerm.trim(), onObject, stmt, "", status, lineNo);

	 				// v.1.2.0 does not support GRANT..TO PUBLIC WITH GRANT OPTION, test for that explicitly
	 				if (grantee.trim().equalsIgnoreCase("PUBLIC")) {
	 					if (grantOption.equalsIgnoreCase("WITH GRANT OPTION")) {
	 						String validateStr = "TO PUBLIC WITH GRANT OPTION";  // put this in .cfg file when supported
		 					String statusPublicGrantOption = featureSupportedInVersion(stmt, validateStr);
			 				captureItem("GRANT.." + validateStr, onObject, stmt, "", statusPublicGrantOption, lineNo);
	 					}
	 				}

	 				// handle 'AS principal' separately
	 				// ToDo: treat 'AS dbo' as a case on its own?
	 				if (!asGrantor.isEmpty()) {
	 					String reportAs = "AS principal";
	 					String validateAs = reportAs;
	 					reportAs = stmt + ".." + reportAs;
	 					String statusAs = featureSupportedInVersion(stmt, validateAs);
		 				captureItem(reportAs.trim(), onObject, stmt, "", statusAs, lineNo);
	 				}

	 				// handle 'CASCADE' separately
	 				if (!cascade.isEmpty()) {
	 					String reportCascade = "CASCADE";
	 					String validateCascade = reportCascade;
	 					reportCascade = stmt + ".." + reportCascade;
	 					String statusCascade = featureSupportedInVersion(stmt, validateCascade);
		 				captureItem(reportCascade.trim(), onObject, stmt, "", statusCascade, lineNo);
	 				}
	 			}
			}

			@Override public String visitGrant_statement(TSQLParser.Grant_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				String grantOn = "";
				if (ctx.ON() != null) {
					grantOn = ctx.permission_object().getText().toUpperCase();
				}

				String grantee = "";
				if (ctx.principals() != null) grantee = ctx.principals().getText().toUpperCase();

				String grantOption = "";
				if (ctx.WITH() != null) {
					grantOption = "WITH GRANT OPTION";
				}

				String asGrantor = "";
				if (ctx.AS() != null) {
					asGrantor = ctx.principal_id().getText().toUpperCase();
				}

				capturePermissions(GrantStmt, ctx.permissions(), grantOn, grantee, grantOption, asGrantor, "", ctx.start.getLine());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitRevoke_statement(TSQLParser.Revoke_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				String revokeOn = "";
				if (ctx.ON() != null) {
					revokeOn = ctx.permission_object().getText().toUpperCase();
				}

				String grantee = "";
				if (ctx.principals() != null) grantee = ctx.principals().getText().toUpperCase();

				String grantOption = "";
				if (ctx.GRANT() != null) {
					grantOption = "GRANT OPTION FOR";
				}

				String asGrantor = "";
				if (ctx.AS() != null) {
					asGrantor = ctx.principal_id().getText().toUpperCase();
				}

				String cascade = "";
				if (ctx.CASCADE() != null) {
					cascade = ctx.CASCADE().getText().toUpperCase();
				}

				capturePermissions(RevokeStmt, ctx.permissions(), revokeOn, grantee, grantOption, asGrantor, cascade, ctx.start.getLine());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitDeny_statement(TSQLParser.Deny_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				String denyOn = "";
				if (ctx.ON() != null) {
					denyOn = ctx.permission_object().getText().toUpperCase();
				}

				String grantee = "";
				if (ctx.principals() != null) grantee = ctx.principals().getText().toUpperCase();

				String asGrantor = "";
				if (ctx.AS() != null) {
					asGrantor = ctx.principal_id().getText().toUpperCase();
				}

				String cascade = "";
				if (ctx.CASCADE() != null) {
					cascade = ctx.CASCADE().getText().toUpperCase();
				}

				capturePermissions(DenyStmt, ctx.permissions(), denyOn, grantee, "", asGrantor, cascade, ctx.start.getLine());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitAlter_authorization(TSQLParser.Alter_authorizationContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());

				String grantee = ctx.authorization_grantee().getText().toUpperCase();

				String objType = "";
				String objTypeChk = "";
				String objName = "";
				if (ctx.object_type() != null) {
					objType = ctx.object_type().getText().toUpperCase();
					objTypeChk = objType;
				}

				if (ctx.entity_name() != null) {
					objName = ctx.entity_name().getText().toUpperCase();
				}

				String objReport = objType + "::";
				String objDetail = objType + "::" + objName;
				if (objType.isEmpty()) {
					objDetail = objName;
					objReport = "object";
					objTypeChk = "NO_OBJECT_TYPE";  // matches .cfg file
				}
				//u.appOutput(u.thisProc()+"objType=["+objType+"] objTypeChk=["+objTypeChk+"] ");

				// ToDo: test for object types when ALTER AUTHORIZATION gets supported
				String status = featureSupportedInVersion(AlterAuthStmt, objTypeChk);
				if (objType.isEmpty()) {
					objDetail = objName;
				}
				if (objReport.equals("OBJECT")) objReport = "object";
				String granteeReport = grantee;
				if (grantee.equals("SCHEMAOWNER")) granteeReport = "SCHEMA OWNER";
				else granteeReport = "principal";

 				captureItem(AlterAuthStmt+ " ON " + objReport + " TO " + granteeReport, objDetail, AlterAuthStmt, "", status, ctx.start.getLine());

				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitDrop_login(TSQLParser.Drop_loginContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitSpatial_methods(TSQLParser.Spatial_methodsContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String spatialCall = ctx.method.getText();
				//u.appOutput(u.thisProc()+"spatialCall=["+spatialCall+"] ");
				if (spatialCall.equalsIgnoreCase("[value]")) {
					// this is very likely an XML method call, but the square brackets have sent us the wrong way in the grammar
					if (stringAggWorkaround) {
						stringAggWorkaround = false;
						return(null);
					}
					captureXMLFeature("XML.", "value", "()", ctx.start.getLine());
				}
				else {
					String status = featureSupportedInVersion(Geospatial,spatialCall);
					captureItem(SpatialMethodCallFmt + " ." + spatialCall, "", SpatialReportGroup, "", status, ctx.start.getLine());
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitConversation_statement(TSQLParser.Conversation_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				if (ctx.begin_conversation_timer() != null)
					captureServiceBroker("BEGIN CONVERSATION", ctx.start.getLine());
				else if (ctx.begin_conversation_dialog() != null)
					captureServiceBroker("BEGIN DIALOG", ctx.start.getLine());
				else if (ctx.end_conversation() != null)
					captureServiceBroker("END CONVERSATION", ctx.start.getLine());
				else if (ctx.get_conversation() != null)
					captureServiceBroker("GET CONVERSATION", ctx.start.getLine());
				else if (ctx.send_conversation() != null)
					captureServiceBroker("SEND ON CONVERSATION", ctx.start.getLine());
				else if (ctx.waitfor_conversation() != null)
					captureServiceBroker("WAITFOR GET CONVERSATION", ctx.start.getLine());
				else if (ctx.waitfor_receive_statement() != null)
					captureServiceBroker("WAITFOR RECEIVE", ctx.start.getLine());
				else if (ctx.receive_statement() != null)
					captureServiceBroker("RECEIVE", ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			private void captureServiceBroker(String stmt, int lineNr) {
				String status = featureSupportedInVersion(ServiceBroker, stmt);
				captureItem(stmt, "", ServiceBroker, stmt, status, lineNr);
			}

			@Override public String visitOpen_key(TSQLParser.Open_keyContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String stmt = "OPEN SYMMETRIC KEY";
				if (ctx.MASTER() != null) stmt = "OPEN MASTER KEY";
 				String status = featureSupportedInVersion(OpenKeyStmt);
 				captureItem(stmt, "", OpenKeyStmt, stmt, status, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitClose_key(TSQLParser.Close_keyContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String stmt = "CLOSE SYMMETRIC KEY";
				if (ctx.ALL() != null) stmt = "CLOSE ALL SYMMETRIC KEYS";
				else if (ctx.MASTER() != null) stmt = "CLOSE MASTER KEY";
 				String status = featureSupportedInVersion(CloseKeyStmt);
 				captureItem(stmt, "", CloseKeyStmt, stmt, status, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitSqlcmd_command(TSQLParser.Sqlcmd_commandContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String cmd = "";
				if (ctx.SETVAR() != null) {
					cmd = "SETVAR";
					String varName = ctx.id().get(0).getText();
					addSqlcmdVar(varName);
				}
				else if (ctx.LIST() != null) cmd = "LIST";
				else if (ctx.LISTVAR() != null) cmd = "LISTVAR";
				else if (ctx.HELP() != null) cmd = "HELP";
				else if (ctx.XML() != null) cmd = "XML";
				else if (ctx.ED() != null) cmd = "ED";
				else if (ctx.ON() != null) {
					if (ctx.IGNORE() != null) cmd = "ON ERROR IGNORE";
					else cmd = "ON ERROR EXIT";
				}
				else if (ctx.SERVERLIST() != null) cmd = "SERVERLIST";
				else if (ctx.ERROR() != null) cmd = "ERROR";
				else if (ctx.OUT() != null) cmd = "OUT";
				else if (ctx.PERFTRACE() != null) cmd = "PERFTRACE";
				else if (ctx.R() != null) cmd = "R";
				else if (ctx.EXCLAMATION() != null) cmd = "!!";
				else if (ctx.CONNECT() != null) cmd = "CONNECT";
				else {
					assert false : "sqlcmd command: cannot be empty";
				}
 				String status = featureSupportedInVersion(sqlcmdCommand);
 				captureItem(sqlcmdCommand + " :" + cmd, "", sqlcmdReportGroup, "", status, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitSqlcmd_variable(TSQLParser.Sqlcmd_variableContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String varName = "";
				if (ctx.ID() != null) varName = ctx.ID().getText();
				else varName = ctx.keyword().getText();
 				String status = featureSupportedInVersion(sqlcmdVariable);
 				// todo: highlight predefined slqcmd variables?
 				captureItem(sqlcmdVariable + " $(" + varName + ")", "", sqlcmdReportGroup, "", status, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			@Override public String visitCreate_sequence(TSQLParser.Create_sequenceContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine());
 				String seqName = ctx.sequence_name.getText();
 				if (ctx.schema_name != null) seqName = ctx.schema_name.getText() + "." + seqName;
 				seqName = u.normalizeName(seqName);
				Token noCache = ctx.no_cache;
				if (noCache != null) {
	 				String status = featureSupportedInVersion(SequenceOptions, "NO CACHE");
					if (!status.equals(u.Supported)) {
						if (u.rewrite) {
							String s = ctx.no_cache.getText() + " " + ctx.no_cache_kwd.getText();
							String rewriteText = "";
							if (addRewrite(DDLReportGroup, "NO CACHE", u.rewriteTypeReplace, rewriteText, ctx.no_cache.getLine(), ctx.no_cache.getCharPositionInLine(), ctx.no_cache_kwd.getLine(), ctx.no_cache_kwd.getCharPositionInLine(), ctx.no_cache.getStartIndex(), ctx.no_cache_kwd.getStopIndex()))
								status = u.Rewritten;
							captureItem("Option NO CACHE in CREATE SEQUENCE", "", DDLReportGroup, "", status, ctx.start.getLine());
							noCache = null;
						}
						else {
							addRewrite("Option NO CACHE in CREATE SEQUENCE");
						}
					}
				}
				captureSequenceOptions(ctx.cache_kwd, ctx.cache_value, noCache, ctx.start.getLine(), "CREATE");
				visitChildren(ctx);
				return null;
			}

			@Override public String visitAlter_sequence(TSQLParser.Alter_sequenceContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine());
 				String seqName = ctx.sequence_name.getText();
 				if (ctx.schema_name != null) seqName = ctx.schema_name.getText() + "." + seqName;
 				seqName = u.normalizeName(seqName);
				Token noCache = ctx.no_cache;
				if (noCache != null) {
	 				String status = featureSupportedInVersion(SequenceOptions, "NO CACHE");
					if (!status.equals(u.Supported)) {
						if (u.rewrite) {
							String s = ctx.no_cache.getText() + " " + ctx.no_cache_kwd.getText();
							String rewriteText = "";
							if (addRewrite(DDLReportGroup, "NO CACHE", u.rewriteTypeReplace, rewriteText, ctx.no_cache.getLine(), ctx.no_cache.getCharPositionInLine(), ctx.no_cache_kwd.getLine(), ctx.no_cache_kwd.getCharPositionInLine(), ctx.no_cache.getStartIndex(), ctx.no_cache_kwd.getStopIndex()))
								status = u.Rewritten;
							captureItem("Option NO CACHE in ALTER SEQUENCE", "", DDLReportGroup, "", status, ctx.start.getLine());
							noCache = null;
						}
						else {
							addRewrite("Option NO CACHE in ALTER SEQUENCE");
						}
					}
				}
				captureSequenceOptions(ctx.cache_kwd, ctx.cache_value, noCache, ctx.start.getLine(), "ALTER");
				visitChildren(ctx); return null;
			}

			private void captureSequenceOptions(Token cache_kwd, Token cache_value, Token no_cache, int lineNr, String stmt) {
 				if ((cache_kwd != null) && (cache_value == null)) {
					captureOption(SequenceOptions, "CACHE (without number)", lineNr, ", in "+stmt+" SEQUENCE");
 				}
 				else if (no_cache != null) {
					captureOption(SequenceOptions, "NO CACHE", lineNr, ", in "+stmt+" SEQUENCE");
 				}
			}

			@Override public String visitDrop_sequence(TSQLParser.Drop_sequenceContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_fulltext_index(TSQLParser.Create_fulltext_indexContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				//u.appOutput(u.thisProc()+"ctx=["+getTextSpaced(ctx)+"] ");
				String tableName = u.normalizeName(ctx.table_name().getText());
				String indexName = u.normalizeName(ctx.id().getText());
				String option = "";
				if (ctx.fulltext_with_option().size() > 0) option = "WITH"; // ToDo: extract actual properties being specified and test for their status
				captureFullTextIndex("CREATE", tableName, indexName, option, ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}
			@Override public String visitAlter_fulltext_index(TSQLParser.Alter_fulltext_indexContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String tableName = u.normalizeName(ctx.table_name().getText());
				captureFullTextIndex("ALTER", tableName, "", "", ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;

			}
			@Override public String visitDrop_fulltext_index(TSQLParser.Drop_fulltext_indexContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(CompassUtilities.thisProc());
				String tableName = u.normalizeName(ctx.table_name().getText());
				captureFullTextIndex("DROP", tableName, "", "", ctx.start.getLine());
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(CompassUtilities.thisProc());
				return null;
			}

			private void captureFullTextIndex(String kwd, String objectName, String objectName2, String option, int lineNr) {
				captureFullTextIndex(kwd, objectName, objectName2, option, lineNr, null);
			}
			private void captureFullTextIndex(String kwd, String objectName, String objectName2, String option, int lineNr, TSQLParser.Create_fulltext_indexContext ctx) {
				kwd = kwd.trim().toUpperCase();
				option = option.trim().toUpperCase();
				String status = featureSupportedInVersion(MiscObjects, FullTextIndex);
				//u.appOutput(u.thisProc()+"kwd=["+kwd+"] status FULLTEXT INDEX=["+status+"] ");
				if (status.equals(u.Supported)) {
					// check for detailed features
					status = featureSupportedInVersion(FullTextIndex, kwd);
					//u.appOutput(u.thisProc()+"kwd=["+kwd+"] status kwd=["+status+"] ");
					if (!option.isEmpty()) {
						if (status.equals(u.Supported)) {
							status = featureSupportedInVersion(FullTextIndex, kwd + " " + option);
							//u.appOutput(u.thisProc()+"kwd=["+kwd+"] status option=["+status+"] ");
						}
					}
					if (kwd.equals("CREATE") && (ctx != null)) {
						if (status.equals(u.Supported)) {
							for (TSQLParser.Fulltext_index_columnContext c : ctx.fulltext_index_column()) {
								//u.appOutput(u.thisProc()+"Fulltext_index_column=["+c.getText()+"] ");
								String colName = u.normalizeName(c.full_column_name().get(0).getText());
								if (status.equals(u.Supported) && (c.TYPE() != null)) {
									option = "TYPE COLUMN";
									status = featureSupportedInVersion(FullTextIndex, kwd + " " + option);
								}
								if (status.equals(u.Supported) && (c.LANGUAGE() != null)) {
									option = "LANGUAGE";
									status = featureSupportedInVersion(FullTextIndex, kwd + " " + option);
								}
								if (status.equals(u.Supported) && (c.STATISTICAL_SEMANTICS() != null)) {
									option = "STATISTICAL_SEMANTICS";
									status = featureSupportedInVersion(FullTextIndex, kwd + " " + option);
								}
								if (!status.equals(u.Supported)) {
									break;
								}
							}
						}
					}
				}
				//u.appOutput(u.thisProc()+"kwd=["+kwd+"] option=["+option+"] status final=["+status+"] ");
				captureItem(kwd + " " + FullTextIndex + " " + option, objectName, FullTextSearchReportGroup, objectName2, status, lineNr);

				return;
			}

			@Override public String visitCreate_application_role(TSQLParser.Create_application_roleContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_application_role(TSQLParser.Alter_application_roleContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_application_role(TSQLParser.Drop_application_roleContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitCreate_column_encryption_key(TSQLParser.Create_column_encryption_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_column_encryption_key(TSQLParser.Alter_column_encryption_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_column_encryption_key(TSQLParser.Drop_column_encryption_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitCreate_column_master_key(TSQLParser.Create_column_master_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_column_master_key(TSQLParser.Drop_column_master_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitCreate_asymmetric_key(TSQLParser.Create_asymmetric_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_asymmetric_key(TSQLParser.Alter_asymmetric_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_asymmetric_key(TSQLParser.Drop_asymmetric_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitCreate_master_key(TSQLParser.Create_master_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_master_key(TSQLParser.Alter_master_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_master_key(TSQLParser.Drop_master_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_service_master_key(TSQLParser.Alter_service_master_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitCreate_symmetric_key(TSQLParser.Create_symmetric_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_symmetric_key(TSQLParser.Alter_symmetric_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_symmetric_key(TSQLParser.Drop_symmetric_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitCreate_assembly(TSQLParser.Create_assemblyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_assembly(TSQLParser.Alter_assemblyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_assembly(TSQLParser.Drop_assemblyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitCreate_synonym(TSQLParser.Create_synonymContext ctx) {
				String synNameRaw = ctx.synonym_name.getText();
				String synName    = u.normalizeName(synNameRaw);
				CaptureIdentifier(synNameRaw, synName, "CREATE SYNONYM", ctx.start.getLine());
				String synBaseObjRaw = ctx.full_object_name().getText();
				String synBaseObj = u.normalizeName(synBaseObjRaw);
				CaptureIdentifier(synBaseObjRaw, synBaseObj, "CREATE SYNONYM", ctx.start.getLine());
				String baseObjType = findObjectType(synBaseObj);
				String baseObjTypeFmt = "(unknown object type)";
				if (!baseObjType.isEmpty()) baseObjTypeFmt = baseObjType.toUpperCase();
				String status = captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine(), synName, ", for " + baseObjTypeFmt);

				if (status.equals(u.Supported)) {
					// tbd
				}
				visitChildren(ctx);
				return null;
			}
			@Override public String visitDrop_synonym(TSQLParser.Drop_synonymContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitCreate_aggregate(TSQLParser.Create_aggregateContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_aggregate(TSQLParser.Drop_aggregateContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitCreate_credential(TSQLParser.Create_credentialContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_credential(TSQLParser.Alter_credentialContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_credential(TSQLParser.Drop_credentialContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitCreate_database_scoped_credential(TSQLParser.Create_database_scoped_credentialContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_database_scoped_credential(TSQLParser.Alter_database_scoped_credentialContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_database_scoped_credential(TSQLParser.Drop_database_scoped_credentialContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitCreate_cryptographic_provider(TSQLParser.Create_cryptographic_providerContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_cryptographic_provider(TSQLParser.Alter_cryptographic_providerContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_cryptographic_provider(TSQLParser.Drop_cryptographic_providerContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitCreate_contract(TSQLParser.Create_contractContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_contract(TSQLParser.Drop_contractContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitCreate_diagnostic_session(TSQLParser.Create_diagnostic_sessionContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_diagnostic_session(TSQLParser.Drop_diagnostic_sessionContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitCreate_default(TSQLParser.Create_defaultContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_default(TSQLParser.Drop_defaultContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitCreate_rule(TSQLParser.Create_ruleContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_rule(TSQLParser.Drop_ruleContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_route(TSQLParser.Create_routeContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_route(TSQLParser.Drop_routeContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_queue(TSQLParser.Create_queueContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_queue(TSQLParser.Alter_queueContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_queue(TSQLParser.Drop_queueContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_service(TSQLParser.Create_serviceContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_service(TSQLParser.Alter_serviceContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_service(TSQLParser.Drop_serviceContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_message_type(TSQLParser.Create_message_typeContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_message_type(TSQLParser.Alter_message_typeContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_message_type(TSQLParser.Drop_message_typeContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_certificate(TSQLParser.Create_certificateContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_certificate(TSQLParser.Alter_certificateContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_certificate(TSQLParser.Drop_certificateContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_availability_group(TSQLParser.Create_availability_groupContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_availability_group(TSQLParser.Alter_availability_groupContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_availability_group(TSQLParser.Drop_availability_groupContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_external_data_source(TSQLParser.Create_external_data_sourceContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_external_data_source(TSQLParser.Alter_external_data_sourceContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_external_data_source(TSQLParser.Drop_external_data_sourceContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_external_library(TSQLParser.Create_external_libraryContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_external_library(TSQLParser.Alter_external_libraryContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_external_library(TSQLParser.Drop_external_libraryContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_external_language(TSQLParser.Create_external_languageContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_external_resource_pool(TSQLParser.Create_external_resource_poolContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_external_resource_pool(TSQLParser.Alter_external_resource_poolContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_external_resource_pool(TSQLParser.Drop_external_resource_poolContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitAlter_resource_governor(TSQLParser.Alter_resource_governorContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_workload_classifier(TSQLParser.Create_workload_classifierContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_workload_classifier(TSQLParser.Drop_workload_classifierContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_workload_group(TSQLParser.Create_workload_groupContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_workload_group(TSQLParser.Alter_workload_groupContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_workload_group(TSQLParser.Drop_workload_groupContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_fulltext_catalog(TSQLParser.Create_fulltext_catalogContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_fulltext_catalog(TSQLParser.Alter_fulltext_catalogContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_fulltext_catalog(TSQLParser.Drop_fulltext_catalogContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}


			@Override public String visitCreate_fulltext_stoplist(TSQLParser.Create_fulltext_stoplistContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_fulltext_stoplist(TSQLParser.Alter_fulltext_stoplistContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_fulltext_stoplist(TSQLParser.Drop_fulltext_stoplistContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_remote_service_binding(TSQLParser.Create_remote_service_bindingContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitAlter_remote_service_binding(TSQLParser.Alter_remote_service_bindingContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_remote_service_binding(TSQLParser.Drop_remote_service_bindingContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_or_alter_endpoint(TSQLParser.Create_or_alter_endpointContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_endpoint(TSQLParser.Drop_endpointContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_or_alter_event_session(TSQLParser.Create_or_alter_event_sessionContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_event_session(TSQLParser.Drop_event_sessionContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_event_notification(TSQLParser.Create_event_notificationContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_event_notification(TSQLParser.Drop_event_notificationContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_or_alter_database_audit_specification(TSQLParser.Create_or_alter_database_audit_specificationContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitCreate_or_alter_broker_priority(TSQLParser.Create_or_alter_broker_priorityContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitDrop_broker_priority(TSQLParser.Drop_broker_priorityContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

			@Override public String visitDrop_statistics (TSQLParser.Drop_statisticsContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}
			@Override public String visitCreate_statistics (TSQLParser.Create_statisticsContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx, ctx.start.getLine()); visitChildren(ctx); return null;
			}

		};

		assert (tree != null) : "parse tree is null";

		//report setting at start of batch
		String on_off = TSQLLexer.QUOTED_IDENTIFIER_FLAG?"ON":"OFF";

		if (pass == 1) {
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"-- doing pass 1, batchNr="+batchNr+" QUOTED_IDENTIFIER="+on_off+" ---", u.debugPtree);
			u.clearContext();
			pass1Analysis.visit(tree);
		}

		if (pass == 2) {
			//dumpSymTab("");  // debug
			localVars.clear();
			localAtAtErrorVars.clear();
			u.clearContext();

			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"-- doing pass 2, batchNr="+batchNr+" QUOTED_IDENTIFIER="+on_off+" ---", u.debugPtree);
			stmt.clear();
			pass2Analysis.visit(tree);

			// classify & capture found SELECT statements
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc()+"SELECT stmts found: " + stmt.size(), u.debugPtree);
			for (int i: stmt.keySet()) {
				CompassItem item = stmt.get(i);
				captureSELECT(item, i);
			}
			stmt.clear();
		}
	}
}