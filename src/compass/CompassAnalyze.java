/*
Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/
package compass;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.File;
import javax.print.PrintException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.stream.*;

import parser.*;

public class CompassAnalyze {
	public static CompassConfig cfg;
	public static CompassUtilities u = CompassUtilities.getInstance();

	// variables/parameters only for current object
	static Map<String, String> localVars = new HashMap<String, String>();		
	static  Map<String, String> localAtAtErrorVars = new HashMap<String, String>();		
	
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
	
	// for reporting items that do not fit elsewhere
	static final String MiscReportGroup       = "Miscellaneous SQL Features";	
	
	// sections in the .cfg file: string must match exactly
	static final String AggregateFunctions    = "Aggregate functions";
	static final String BuiltInFunctions      = "Built-in functions";
	static final String ProcVersionDeclare    = "Procedure versioning (declaration)";
	static final String ProcVersionExecute    = "Procedure versioning (execution)";
	static final String NumericAsDateTime     = "Numeric representation of datetime";	
	static final String NumericDateTimeVarAssign = "Numeric assignment to datetime variable/parameter";	
	static final String Datatypes             = "Datatypes";
	static final String UDDatatypes           = "User-Defined Datatypes";
	static final String DatatypeConversion    = "Datatype conversion";		
	static final String TableVariables        = "Table variables";
	static final String TableVariablesType    = "Table variables/Table types";
	static final String ParamValueDEFAULT     = "Parameter value DEFAULT";				
	static final String ProcedureOptions      = "Procedure options";				
	static final String ExecProcedureOptions  = "Execute procedure options";				
	static final String ExecStringOptions     = "Execute string options";				
	static final String TemporaryProcedures   = "Temporary procedures";				
	static final String ExecuteSQLFunction    = "EXECUTE SQL function";				
	static final String FunctionOptions       = "Function options";				
	static final String TriggerOptions        = "Trigger options";	
	static final String TransitionTableMultiDMLTrig = "Transition table reference for multi-DML trigger";	
	static final String TriggerSchemaName     = "Trigger created with schema name";	
	static final String DDLTrigger            = "DDL TRIGGER";			
	static final String EnableTrigger         = "ENABLE TRIGGER";			
	static final String DisableTrigger        = "DISABLE TRIGGER";			
	static final String ProcExecAsVariable    = "Variable procedure name";
	static final String DynamicSQL            = "Dynamic SQL";			
	static final String SystemStoredProcs     = "System Stored Procedures";		
	static final String SystemFunctions       = "System Functions";		
	static final String Catalogs              = "Catalogs";		
	static final String InformationSchema     = "INFORMATION_SCHEMA";		
	static final String XMLFeatures           = "XML features";		
	static final String HIERARCHYIDFeatures   = "HIERARCHYID features";		
	static final String JSONFeatures          = "JSON features";		
	static final String GlobalTmpTableFmt     = "##globaltmptable";     // for display 
	static final String GlobalTmpTable        = "Global Temporary Tables";		
	static final String DropMultipleObjects   = "DROP multiple objects";
	static final String DropIfExists          = "DROP IF EXISTS";
	static final String SetOptions            = "SET options";
	static final String SetMultipleOptions    = "SET, multiple options combined";
	static final String SelectTopWithTies     = "SELECT TOP WITH TIES";
	static final String SelectTopPercent      = "SELECT TOP PERCENT";
	static final String SelectTop             = "SELECT TOP";
	static final String InsteadOfTrigger      = "Instead-Of Trigger";
	static final String CursorVariables       = "CURSOR variables";
	static final String CursorOptions         = "Cursor options";
	static final String CursorGlobal          = "GLOBAL cursor";
	static final String CursorFetch           = "FETCH cursor";
	static final String NonPersistedCompCol   = "Non-PERSISTED computed columns";
	static final String CompColFeatures       = "Features in computed columns";
	static final String IndexOptions          = "Index options";
	static final String MaxColumnsIndex       = "Maximum columns per index";
	static final String MaxProcParameters     = "Maximum parameters per procedure";
	static final String MaxFuncParameters     = "Maximum parameters per function";
	static final String ViewOptions           = "View options";
	static final String IndexAttribute        = "Index attribute";
	static final String IgnoreDupkeyIndex     = "IGNORE_DUP_KEY index";
	static final String ConstraintAttribute   = "Constraint attribute";
	static final String ColumnAttribute       = "Column attribute";
	static final String SQLGraph              = "SQL graph";
	static final String ForReplication        = "FOR REPLICATION";
	static final String NotForReplication     = "NOT FOR REPLICATION";
	static final String Partitioning          = "Partitioning";				
	static final String InlineIndex           = "Inline index";				
	static final String ClusteredIndex        = "CLUSTERED index";				
	static final String IndexedView           = "Indexed view";				
	static final String MaterializedView      = "Materialized view";				
	static final String DescConstraint        = "DESC constraint";			
	static final String FKrefDBname           = "FK constraint referencing DB name";			
	static final String SelectPivot           = "SELECT..PIVOT";			
	static final String SelectUnpivot         = "SELECT..UNPIVOT";			
	static final String LateralJoin           = "Lateral join";			
	static final String QueryHint             = "Query hint";			
	static final String TableHint             = "Table hint";			
	static final String JoinHint              = "Join hint";			
	static final String DoubleQuotedString    = "Double-quoted string";			
	static final String SetQuotedIdInBatch    = "SET QUOTED_IDENTIFIER in batch";			
	static final String GroupByAll            = "GROUP BY ALL";			
	static final String RollupCubeOldSyntax   = "GROUP BY ROLLUP/CUBE (old syntax)";			
	static final String ODBCScalarFunction    = "ODBC scalar function";			
	static final String ODBCLiterals          = "ODBC literal";	
	static final String SelectTopWoOrderBy    = "SELECT TOP without ORDER BY";		
	static final String SelectToClientWoOrderBy = "SELECT to client without ORDER BY";		
	static final String ReadText              = "READTEXT";		
	static final String WriteText             = "WRITETEXT";		
	static final String UpdateText            = "UPDATETEXT";		
	static final String CreateDatabaseOptions = "CREATE DATABASE options";		
	static final String AlterDatabaseOptions  = "ALTER DATABASE options";		
	static final String DbccStatements        = "DBCC statements";		
	static final String Traceflags            = "Traceflags";		
	static final String LeadingDotsId         = "Leading dots in identifier";		
	static final String CrossDbReference      = "Cross-database reference";		
	static final String RemoteObjectReference = "Remote object reference";		
	static final String SpecialColumNames     = "Special column names";		
	static final String MaxIdentifierLength   = "Maximum identifier length";		
	static final String SpecialCharsIdentifier= "Special characters in identifier";		
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
	static final String ColonColonFunctionCall= "::function call (old syntax)";		
	static final String NextValueFor          = "NEXT VALUE FOR";		
	static final String LoginOptions          = "Login options";		
	static final String UserOptions           = "User options";		
	static final String SchemaOptions         = "Schema options";		
	static final String AlterSchema           = "ALTER SCHEMA";		
	static final String DbRoleOptions         = "DB role options";		
	static final String AlterDbRole           = "ALTER ROLE";		
	static final String SrvRoleOptions        = "Server role options";			
	static final String ServiceBroker         = "Service Broker";			
	static final String OpenKeyStmt           = "OPEN KEY";			
	static final String CloseKeyStmt          = "CLOSE KEY";			
	static final String Collations            = "Collations";			
	static final String CaseSensitiveCollation = "Case-sensitive collation";			
	static final String DBAStmts              = "DBA statements";		
	static final String MiscObjects           = "Miscellaneous objects";		
	static final String MoneyLiteral          = "MONEY literal";		
	static final String AtAtVariable          = "@@variable";	
	static final String VarDeclareAtAt        = "Regular variable named @@v";	
	static final String AtAtErrorValueRef     = "@@ERROR value";		
	static final String AlterIndex            = "ALTER INDEX";		
	static final String AlterTable            = "ALTER TABLE";		
		
	// matching special values in the .cfg file
	static final String CfgNonZero            = "NONZERO";		
	static final String CfgVariable           = "VARIABLE";		
	static final String CfgExpression         = "EXPRESSION";		
	static final String CfgXmlMethodCall      = "XML_METHOD_CALL";		
	static final String CfgHierachyIdMethodCall = "HIERACHYID_METHOD_CALL";		
	static final String CfgScalarUdfCall      = "SCALAR_UDF_CALL";	
	static final String CfgXMLSchema          = "XML(xmlschema)";	
	
	// misc strings
	static final String TrigMultiDMLAttr      = "TRIGGER_MULTI_DML";		
	
	// use when there's no name
	static final String noName = "-unnamed-";

	// DATExxx BIFs
	static final List<String> dateBIFs = Arrays.asList("DATENAME", "DATEPART", "DATEDIFF", "DATEADD");
	
	// datatypes
	static final List<String> baseNumericTypes  = Arrays.asList("INT", "INTEGER", "TINYINT", "SMALLINT", "BIGINT", "NUMERIC", "DECIMAL", "FLOAT", "DOUBLE", "DOUBLE PRECISION", "BIT");
	static final List<String> baseStringTypes   = Arrays.asList("CHAR", "VARCHAR", "NCHAR", "NVARCHAR", "TEXT", "NTEXT");  // todo: handle VARYING 
	static final List<String> baseDateTimeTypes = Arrays.asList("DATE", "TIME", "DATETIME", "SMALLDATETIME", "DATETIME2", "DATETIMEOFFSET"); 
	static final List<String> baseBinaryTypes   = Arrays.asList("UNIQUEIDENTIFIER", "BINARY", "VARBINARY", "IMAGE", "TIMESTAMP", "ROWVERSION"); 
	
	// flags
	static boolean inCompCol = false;

	//--- wrappers ------------------------------------------------------------
	public static boolean featureExists(String section) {
		return cfg.featureExists(section);
	}
	public static boolean featureExists(String section, String name) {
		return cfg.featureExists(section, name);
	}	
	public static String featureArgSupportedInVersion(String section, String arg, String argValue) {
		return cfg.featureArgSupportedInVersion(u.targetBabelfishVersion, section, arg, argValue);
	}
	public static String featureSupportedInVersion(String section) {
		return cfg.featureSupportedInVersion(u.targetBabelfishVersion, section);
	}
	public static String featureSupportedInVersion(String section, String name) {
		return cfg.featureSupportedInVersion(u.targetBabelfishVersion, section, name);
	}	
	public static String featureSupportedInVersion(String defaultStatus, String section, String name, String optionValue) {
		return cfg.featureSupportedInVersion(u.targetBabelfishVersion, defaultStatus, section, name, optionValue);
	}
	public static int featureIntValueSupportedInVersion(String section) {
		return cfg.featureIntValueSupportedInVersion(u.targetBabelfishVersion, section);
	}	
	public static String featureValueSupportedInVersion(String section) {
		return cfg.featureValueSupportedInVersion(u.targetBabelfishVersion, section);
	}	
	public static List<String> featureValueList(String section) {
		return cfg.featureValueList(section);
	}
	public static String featureGroup(String section) {
		return cfg.featureGroup(section);
	}
	public static String featureGroup(String section, String name) {
		return cfg.featureGroup(section, name);
	}
	
	//--- debugging -----------------------------------------------------------
	String dbgTraceBasicIndent = "    ";
	StringBuilder dbgTraceIndent = new StringBuilder();
	int nestingLevel = 0, dbgTraceBasicIndentLength = dbgTraceBasicIndent.length();

	void dbgTraceVisitEntry(String s) {
		nestingLevel++;
		dbgVisitOutput(s+ " entry");
		dbgTraceIndent.append(dbgTraceBasicIndent);
	}
	void dbgTraceVisitExit(String s) {
		dbgTraceIndent.delete(dbgTraceIndent.length() - dbgTraceBasicIndentLength, dbgTraceIndent.length());
		dbgVisitOutput(s + " exit");
		nestingLevel--;
	}
	void dbgVisitOutput(String s) {
		if (u.debugging) u.dbgOutput(dbgTraceIndent + "(" + nestingLevel + ") " + s, u.debugPtree);
	}

	//--- rule names ------------------------------------------------------------
	private String currentRuleName(int ruleIndex) {	
		return  u.grammarRuleNames[ruleIndex];
	}
	
	private String parentRuleName(RuleContext parent) {	
		return parentRuleName(parent, 1);
	}
	
	private String parentRuleName(RuleContext parent, int level) {	
		if (level <= 1) {  // should never be < 1
			return u.grammarRuleNames[parent.getRuleIndex()];
		} 
		else {
			return parentRuleName(parent.parent, level-1);
		}
	}
	            					
	private boolean hasParent(RuleContext parent, String parentRuleName) {	
		int ruleIxParent = parent.getRuleIndex();
		if (u.grammarRuleNames[ruleIxParent].equals(u.startRuleName)) {
			// top level reached
			return false;
		} 
		else if (u.grammarRuleNames[ruleIxParent].equals(parentRuleName)) {
			// found it
			return true;
		}
		return hasParent(parent.parent, parentRuleName);
	}
	            					
			
	//--- set QUOTED_IDENTIFIER -------------------------------------------------
	public void setQuotedIdentifier (String on_off) {	
		assert u.OnOffOption.contains(on_off) : u.thisProc()+"parameter must be ON or OFF";
		if (on_off.equalsIgnoreCase("ON")) {
			TSQLLexer.QUOTED_IDENTIFIER_FLAG = true;			
		} 
		else {
			TSQLLexer.QUOTED_IDENTIFIER_FLAG = false;			
		}	
		if (u.debugging) u.dbgOutput(u.thisProc()+"initializing QUOTED_IDENTIFIER="+on_off, u.debugPtree);	
	}
	
	//--- datatype evaluation -------------------------------------------------
	// get the datatype of a variable or parameter
	private String varDataType(String v) {		
		// look up datatype of this variable or parameter
		// NB: can also be a system-defined @@variable
		if (localVars.containsKey(v.toUpperCase())) {
			String varType = localVars.get(v.toUpperCase());
			if (u.debugging) u.dbgOutput(u.thisProc()+"v=["+v+"]  varType=["+varType+"]  ", u.debugPtree);
			return varType.toUpperCase();
		} 
		else {
			if (u.debugging) u.dbgOutput(u.thisProc()+"v=["+v+"]  var not found  ", u.debugPtree);
		}
		// use Numeric as a catchall 
		return u.BBFNumericType;	
	}
	
	// eval the datatype of an expression 
	private String expressionDataType(String s) {		
		if (isNumeric(s)) return u.BBFNumericType;
		if (isString(s)) return u.BBFStringType;
		if (isDateTime(s)) return u.BBFDateTimeType;	
		if (isBinary(s)) return u.BBFBinaryType;	
		return u.BBFUnknownType;			
	}
	
	private String expressionDataType(TSQLParser.ExpressionContext expr) {
		// to be written - only look at the string for now (Dev only)
		String s = expr.getText();
		if (u.debugging) u.dbgOutput(u.thisProc()+"expr=["+s+"] ", u.debugPtree);

		// basic checks only at this time
		if (s.charAt(0) == '\'') return u.BBFStringType;
		if ((s.charAt(0) == '"') && (!TSQLLexer.QUOTED_IDENTIFIER_FLAG)) return u.BBFStringType;
		
		try {
			Integer i = Integer.parseInt(s);
			return u.BBFNumericType;
		} catch (Exception e) { /*nothing,proceed*/ }
		
						
		if (s.charAt(0) == '0') {
			if (Character.toUpperCase(s.charAt(1)) == 'X') {
				// ToDo: can this be done more efficiently? NB: it may be a long binary string, exceeding any numeric type's capacity
				if (!u.getPatternGroup(s, "^0x([0-9A-F]*)$", 1).isEmpty()) {
					return u.BBFBinaryType;
				}
			}
		}

		if (s.equalsIgnoreCase("NULL")) return u.BBFNullType;
		
		if (s.charAt(0) == '@') {
			// look up datatype of this variable or parameter
			return expressionDataType(varDataType(s));
		}
		return u.BBFUnknownType;		
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
		if (s.equals(u.BBFNumericType)) {
			return true;
		}
		if (baseNumericTypes.contains(getBaseDataType(s))) {
			return true;
		}
		return false;
	}
	private boolean isString(String s) {
		if (s.equals(u.BBFStringType)) {
			return true;
		}
		if (baseStringTypes.contains(getBaseDataType(s))) {
			return true;
		}		
		return false;
	}
	private boolean isDateTime(String s) {
		if (s.equals(u.BBFDateTimeType)) {
			return true;
		}
		if (baseDateTimeTypes.contains(getBaseDataType(s))) {
			return true;
		}			
		return false;
	}
	private boolean isBinary(String s) {
		if (s.equals(u.BBFBinaryType)) {
			return true;
		}
		if (baseBinaryTypes.contains(getBaseDataType(s))) {
			return true;
		}			
		return false;
	}
	
	// lookup an object's type (only table or view)
	private String lookupObjType(String name) { 
		String objType = "";
		String resolvedName = u.resolveName(name.toUpperCase());
		if (u.objTypeSymTab.containsKey(resolvedName)) {	
			objType = u.objTypeSymTab.get(resolvedName);
		}
		return objType;
	}	
	
	// lookup an SUDF							
	private String lookupSUDF(String name) { 
		String resultType = "";
		String resolvedName = u.resolveName(name.toUpperCase());
		if (u.SUDFSymTab.containsKey(resolvedName)) {	
			resultType = u.SUDFSymTab.get(resolvedName);
		}
		return resultType;
	}

	// lookup a TUDF									
	private String lookupTUDF(String name) { 
		String resultType = "";
		String resolvedName = u.resolveName(name.toUpperCase());
		if (u.TUDFSymTab.containsKey(resolvedName)) {	
			resultType = u.TUDFSymTab.get(resolvedName);
		}
		return resultType;
	}	
	
	// contains variables and parameters applicable to the current batch/block
	public void addLocalVars(String varName, String dataType) {
		localVars.put(varName.toUpperCase(), getBaseDataType(dataType).toUpperCase());		
		//debug
//		int j=0;
//		for (String v: localVars.keySet()) {
//			j++; 
//			u.appOutput("localVar "+j+":"+v+"=["+localVars.get(v)+"]");
//		}		
	}

	// experimental
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
			s = u.applyPatternAll(s, "\\b"+u.hexPattern+"\\b", "0x<hex>");					
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
			if ((execAs.charAt(0) == '\'') || execAs.startsWith("N'") || (execAs.charAt(0) == '"')) execAs = "USER";
			option = "EXECUTE AS " + execAs;
		}
		return option;
	}				
							
	private String formatRemoveStringQuotes(String s) {
		// some standard formatting for display
		if ((s.charAt(0) == '\'') || (s.charAt(0) == '"')) {					
			s = s.substring(1,s.length()-1);		
		} 
		else if (s.toUpperCase().startsWith("N'")) {
			s = s.substring(2,s.length()-1);		
		}
		return s;
	}
		
	//--- item capture entry point --------------------------------------------
	protected void captureItem(String item, String itemDetail, String section, String sectionItem, String status, Integer lineNr) {
		captureItem(item, itemDetail, section, sectionItem, status, lineNr, "");
	}
	protected void captureItem(String item, String itemDetail, String section,  String sectionItem, String status, Integer lineNr, Integer misc) {
		captureItem(item, itemDetail, section, sectionItem, status, lineNr, misc.toString());
	}
	protected void captureItem(String item, String itemDetail, String section,  String sectionItem, String status, Integer lineNr, String misc) {
		assert u.supportOptions.contains(status): u.thisProc()+"invalid status value: ["+status+"] ";		
		if (!status.equals(u.ObjCountOnly)) u.constructsFound++;
		
		String separator = u.captureFileSeparator;

		String currentContext = u.currentObjectType + " " + u.currentObjectName;
		String subContext = u.currentObjectTypeSub + " " + u.currentObjectNameSub;
		
		// determine report group for this item
		//   - default = section as specified
		//   - when section+feature found, use report group specified (if any)
		//   - when section found, use report group specified (if any)
		//   - when section not found, but feature found as section, use report group specified (if any)
		//   - if no group found, use Misc SQL features
		String itemGroup = section;	
		String reportGroupCfg = "";
		if (u.debugging) u.dbgOutput(u.thisProc()+"getting reportGroup: section=["+section+"] sectionItem=["+sectionItem+"]  ", u.debugCfg);
		//u.appOutput(u.thisProc()+"getting reportGroup A: section=["+section+"] sectionItem=["+sectionItem+"]  ");
		if (!sectionItem.isEmpty()) {
			reportGroupCfg = featureGroup(section, sectionItem);
			//u.appOutput(u.thisProc()+"getting reportGroup B: section=["+section+"] sectionItem=["+sectionItem+"] reportGroupCfg=["+reportGroupCfg+"]  ");
			if (reportGroupCfg.isEmpty()) {
				reportGroupCfg = featureGroup(section);
				//u.appOutput(u.thisProc()+"getting reportGroup C: section=["+section+"] sectionItem=["+sectionItem+"]  reportGroupCfg=["+reportGroupCfg+"] ");
				if (reportGroupCfg.isEmpty())  {
					reportGroupCfg = featureGroup(sectionItem);
					//u.appOutput(u.thisProc()+"getting reportGroup D: section=["+section+"] sectionItem=["+sectionItem+"]  reportGroupCfg=["+reportGroupCfg+"] ");
				}
			}
		} 
		else {
			reportGroupCfg = featureGroup(section);
			//u.appOutput(u.thisProc()+"getting reportGroup E: section=["+section+"] sectionItem=["+sectionItem+"]  ");
		}
		if (u.debugging) u.dbgOutput(u.thisProc()+"reportGroupCfg=["+reportGroupCfg+"] ", u.debugCfg);
		//u.appOutput(u.thisProc()+"reportGroupCfg=["+reportGroupCfg+"] ");
		if (!reportGroupCfg.isEmpty()) itemGroup = reportGroupCfg;
		if (itemGroup.isEmpty() || itemGroup.equalsIgnoreCase("DEFAULT")) {
			itemGroup = MiscReportGroup;
			if (u.debugging) u.dbgOutput(u.thisProc()+"section=["+section+"] sectionItem=["+sectionItem+"], using MiscReportGroup=["+MiscReportGroup+"] ", u.debugCfg);			
		}

		// check for separator in text
		// ToDo: is it faster to check existence with contains() first?
		item = item.replaceAll(u.captureFileSeparator, u.captureFileSeparatorMarker);
		itemDetail = itemDetail.replaceAll(u.captureFileSeparator, u.captureFileSeparatorMarker);
		itemGroup = itemGroup.replaceAll(u.captureFileSeparator, u.captureFileSeparatorMarker);
		subContext = subContext.replaceAll(u.captureFileSeparator, u.captureFileSeparatorMarker);
		currentContext = currentContext.replaceAll(u.captureFileSeparator, u.captureFileSeparatorMarker);
		
		// create the record
		String itemLine = item.trim() +separator+ itemDetail.trim() +separator+ itemGroup.trim() +separator+ status +separator+ lineNr +separator+ u.currentAppName +separator+ u.currentSrcFile  +separator+ u.batchNrInFile +separator+ u.lineNrInFile +separator+ currentContext.trim() +separator+ subContext.trim() +separator+ misc + separator + "~" + separator;
		
		// check for newlines -- these will mess everything up
		// should also check for \r, \f, VT, etc.
		if (itemLine.contains(u.newLine)) {
			u.appOutput("Newline found in capture line: ["+itemLine+"] ");
			u.errorExitStackTrace();
		}
		
		//write record
		try {
			u.appendCaptureFile(itemLine);
		} catch (Exception e) {
			u.appOutput("Error writing to capture file");
		}
		
	    if (u.echoCapture) {
			u.appOutput("captured: itemLine=["+itemLine+"] ");
	    }
	    
	    // debug
	    if (u.echoCapture) {
		   	u.printStackTrace();
		}
		
	    return;
	}	
			
	//--- actual capturing of SELECT -----------------------------------------------	
	
	private void captureSELECT(CompassItem sel, int qID) {
		if (u.debugging) u.dbgOutput(u.thisProc()+"stmt: qID=["+qID+"] name=["+sel.getName()+"] (line "+sel.getLineNr()+") attributes=["+sel.getAttributes()+"]", u.debugPtree);
		String status = "";
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
			
			if (sel.getAttributes().contains(" INTO ")) {
				String tabType = u.getPatternGroup(sel.getAttributes(), " INTO (.*?) ", 1);
				if (tabType.equals("table")) tabType = "";
				item = item.replaceFirst("SELECT", "SELECT..INTO "+tabType);
			}
			
			captureItem(item, "", DMLReportGroup, "", u.Supported, sel.getLineNr());
			
			if ((sel.getAttributes().contains(" TOP ")) && (!sel.getAttributes().contains(" ORDERBY "))) {
				// flag TOP without ORDER-BY, but not when it is in an EXISTS predicate
				if (!sel.getAttributes().contains(" EXISTS ")) {
					String statusTOP = featureSupportedInVersion(SelectTopWoOrderBy);
					captureItem(SelectTopWoOrderBy, "", DMLReportGroup, "", statusTOP, sel.getLineNr());
				}
			}
			
			if (!(sel.getAttributes().contains(" INTO ")) && (!sel.getAttributes().contains(" SUBQUERY ")) && (!sel.getAttributes().contains(" INSERT ")) && (!sel.getAttributes().contains(" ORDERBY "))) {
				// SELECT-to-client without ORDER-BY, but not in EXISTS-predicate or subquery
				if (!sel.getAttributes().contains(" EXISTS ")) {
					String statusClient = featureSupportedInVersion(SelectToClientWoOrderBy);
					captureItem(SelectToClientWoOrderBy, "", DMLReportGroup, "", statusClient, sel.getLineNr());
				}
			}
		} 
		else {
			assert false : "unexpected branch";
		}
	}	
			
	public void CaptureXMLNameSpaces(RuleContext parent, String stmt, int lineNr) {	
		if (parent == null) {
			String status = featureSupportedInVersion(XMLFeatures,"WITH XMLNAMESPACES");
			captureItem("WITH XMLNAMESPACES"+" in Common Table Expression", "", XMLFeatures, "WITH XMLNAMESPACES", status, lineNr);				
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
			compCol += ", in computed column";
			if (status.equals(u.Supported)) {
				String statusUDF = featureSupportedInVersion(CompColFeatures, CfgXmlMethodCall);
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
			compCol += ", in computed column";
			if (status.equals(u.Supported)) {
				String statusUDF = featureSupportedInVersion(CompColFeatures, CfgHierachyIdMethodCall);
				status = statusUDF;
			}
		}							
		captureItem(stmt+feature+fmt+compCol, "", HIERARCHYIDFeatures, feature, status, lineNr, "0");				
	}	
			
	private void captureDoubleQuotedString(String s, int lineNr) {	
		String item = "STRING";			
		String xtra = "";	
		s = s.substring(1, s.length()-1);
		if (s.contains("'")) {
			item = "EMBEDDED_SINGLE_QUOTE";	
			xtra = ", embedded single quote";
		}		
		else if (s.contains("\"")) {
			item = "EMBEDDED_DOUBLE_QUOTE";			
			xtra = ", embedded double quote";
		}
		String status = featureSupportedInVersion(DoubleQuotedString, item);
		captureItem(DoubleQuotedString+xtra, "", "", "", status, lineNr);					
	}					
	
	// common code for BIFs		
	private void captureBIF(String funcName, int lineNr) {
		captureBIF(funcName, lineNr, "", 0, null);
	}
	private void captureBIF(String funcName, int lineNr, String options) {
		captureBIF(funcName, lineNr, options, 0, null);
	}
	private void captureBIF(String funcName, int lineNr, String options, int nrArgs, List<TSQLParser.ExpressionContext> argList) {
		String status = u.NotSupported;
		funcName = funcName.toUpperCase();
		String funcNameReport = funcName;	
		String funcDetail = "";	
		if (!options.contains("nobracket")) {
			funcNameReport = funcName + "()";
		}		
		
		if (featureExists(BuiltInFunctions, funcName)) {
			status = featureSupportedInVersion(BuiltInFunctions, funcName);
			
			// any argument needs to be validated?
			String argN = cfg.featureExistsArg(funcName);	
			if (!argN.isEmpty()) {
				if (u.debugging) u.dbgOutput(u.thisProc()+"validating arg=["+argN+"] for BIF=["+funcName+"()]", u.debugPtree);
				int argNum = Integer.parseInt(argN.substring(3));
				assert (argNum <= argList.size()) : u.thisProc()+"argN argNum out of range: "+argNum+", should be <="+argList.size();

				String argStr = argList.get(argNum-1).getText();				
				status = featureArgSupportedInVersion(funcName, argN, argStr);	
				funcNameReport = funcName + "("+ argStr.toLowerCase()+")";

				if (u.debugging) u.dbgOutput(u.thisProc()+"funcName=["+funcName+"] funcNameReport=["+funcNameReport+"] argN=["+argN+"] nrArgs=["+nrArgs+"] status=["+status+"] ", u.debugPtree);						
			}
			
			// check for numeric-as-date						
			if (dateBIFs.contains(funcName)) {
				String statusNumDate = u.NotSupported;
				
				// report the unit used
				String unit = argList.get(0).getText().toLowerCase();
				funcNameReport = funcName + "()";
				funcDetail = unit;
				
				for (int i = 2; i <= 3; i++) {
					if (funcName.equals("DATEADD")) {
						if (i == 2) continue;
					}
					if (funcName.equals("DATENAME") || funcName.equals("DATEPART")) {
						if (i == 3) continue;
					}

					// check for numeric-as-date
					if (u.debugging) u.dbgOutput(u.thisProc()+"dateBIFs: funcName=["+funcName+"] i=["+i+"] argi=["+argList.get(i-1).getText()+"] argtype=["+expressionDataType(argList.get(i-1))+"] ", u.debugPtree);	
					checkNumericAsDate("DATETIME", funcName, funcNameReport, argList.get(i-1), lineNr);
				}					
			}			
		} 
		else {
			// missing entry in the .cfg file?
			assert false: u.thisProc()+"funcName=["+funcName+"] not found under ["+BuiltInFunctions+"] ";
		}
		
		if (inCompCol) {
			// this is a BIF call inside a computed column
			funcNameReport += ", in computed column";
			String statusCC = u.Supported;
			if (featureExists(CompColFeatures, funcName)) {
				statusCC = featureSupportedInVersion(CompColFeatures, funcName);
			}
			if (!statusCC.equals(u.Supported)) {
				if (!status.equals(u.Supported)) {
					status = statusCC;
				}
			}
		}

		if (funcName.equals("NEWSEQUENTIALID")) {
			if (status.equals(u.ReviewSemantics)) {
				funcNameReport += ", implemented as NEWID(): sequential values not guaranteed";
			}
		}
		captureItem(funcNameReport, funcDetail, BuiltInFunctions, funcName, status, lineNr);				
	}	
			
	// check for numeric-as-date						
	private void checkNumericAsDate(String dataType, String funcName, String funcNameReport, TSQLParser.ExpressionContext expr, int lineNr) {	
		// ToDo: handle UDD
		if (dataType.equals("DATETIME") || dataType.equals("SMALLDATETIME")) {		
			if (isNumeric(expressionDataType(expr))) {
				String statusNumDate = featureSupportedInVersion(NumericAsDateTime, funcName);
				if (u.debugging) u.dbgOutput(u.thisProc()+"funcName=["+funcName+"] expr=["+expr.getText()+"]  statusNumDate=["+statusNumDate+"]  ", u.debugPtree);	
				funcNameReport = u.applyPatternFirst(funcNameReport, "^(.*\\().*?(\\).*)$", "$1$2");
				String funcNameReportNumDate = NumericAsDateTime + " in " + funcNameReport;
				captureItem(funcNameReportNumDate, "", NumericAsDateTime, "", statusNumDate, lineNr);				
			} 			
		}
	}	
		
	// check for numeric-as-date						
	private void checkNumericDateVarAssign(String varName, TSQLParser.ExpressionContext expr, int lineNr) {	
		// ToDo: handle UDD
		varName = varName.toUpperCase();
		String dataType = varDataType(varName);
		if (u.debugging) u.dbgOutput(u.thisProc()+"varName=["+varName+"] dataType=["+dataType+"] ", u.debugPtree);
		if (dataType.equals("DATETIME") || dataType.equals("SMALLDATETIME")) {		
			if (isNumeric(expressionDataType(expr))) {
				String statusNumDate = featureSupportedInVersion(NumericDateTimeVarAssign);
				if (u.debugging) u.dbgOutput(u.thisProc()+"varName=["+varName+"]  expr=["+expr.getText()+"]  statusNumDate=["+statusNumDate+"]  ", u.debugPtree);				
				captureItem(NumericDateTimeVarAssign, varName, NumericAsDateTime, "", statusNumDate, lineNr);				
			} 	
		}		
	}
				
	// --- handling SET QUOTED_IDENTIFIER ----------------------------------------------

	// Get ancestor by its name
	private RuleContext getRule (RuleContext ctx, String name) {
		if (ctx == null || name == null) {
			return null;
		}
		String ruleName = u.grammarRuleNames[ctx.getRuleIndex()];
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
				if (u.debugging) u.dbgOutput(u.thisProc()+"pass" + passNr + ": setting QUOTED_IDENTIFIER: on_off=["+on_off+"]  QUOTED_IDENTIFIER_FLAG=["+TSQLLexer.QUOTED_IDENTIFIER_FLAG+"] ", u.debugPtree);
				if (passNr == 2) {
					TSQLParser.Sql_clausesContext sqlClauses;
					RuleContext parentSqlClauses = ctx;
					String parentSqlClausesName = "";

					while (true) {
						do {
							sqlClauses = (TSQLParser.Sql_clausesContext) getRule(parentSqlClauses, "sql_clauses");
							parentSqlClauses = sqlClauses.parent;
							parentSqlClausesName = u.grammarRuleNames[parentSqlClauses.getRuleIndex()];
						} while (parentSqlClausesName.equals("block_statement"));

						if (parentSqlClausesName.equals(u.startRuleName)) {
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
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				int nrOptions = ctx.set_on_off_option().size();
				int nrId = ctx.id().size();
				if (nrOptions > 0) { 	
					List<TSQLParser.Set_on_off_optionContext> options = ctx.set_on_off_option();
					for (int i=0; i<nrOptions; i++) {	
						String option = options.get(i).getText().toUpperCase();
						detectSetQuotedIdentifier(pass, ctx, option);
					}																				
				}
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}				
						
			@Override public String visitUse_statement(TSQLParser.Use_statementContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				u.setCurrentDB(ctx.dbname.getText().toUpperCase());
				//visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}	
											
			@Override public String visitCreate_type(TSQLParser.Create_typeContext ctx) { 
				// this is duplicated in pass 2
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String UDDname = ctx.simple_name().getText();
				String UDDdatatype = "";
				// TerminalNode UDD_FROM = ctx.FROM();
				if (ctx.FROM() != null) {
					// scalar UDD
					UDDdatatype = ctx.data_type().getText().toUpperCase();
				} 
				else {
					// table type
					UDDdatatype = "TABLE";
				}
				u.addUDDSymTab(UDDname.toUpperCase(), UDDdatatype); 
				//visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}		
			
			@Override public String visitCreate_table(TSQLParser.Create_tableContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String tableName = ctx.table_name().getText().toUpperCase();
				u.addObjectTypeSymTab(tableName, "TABLE");
				//visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}				
			
			@Override public String visitCreate_or_alter_view(TSQLParser.Create_or_alter_viewContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String viewName = ctx.simple_name().getText().toUpperCase();
				u.addObjectTypeSymTab(viewName, "VIEW");		
				//visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}	
			
			@Override public String visitCreate_or_alter_function(TSQLParser.Create_or_alter_functionContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String funcName = ctx.func_proc_name_schema().getText();	
				if (ctx.func_body_returns_scalar() != null) {				
					String sudfDataType = u.normalizeName(ctx.func_body_returns_scalar().data_type().getText().toUpperCase(), "datatype");	
					u.addSUDFSymTab(funcName, sudfDataType);								
				} 
				else {
					u.addTUDFSymTab(funcName, "TABLE");													
				}
				// set context -- not really needed here, but keep consistent
				u.setContext("FUNCTION", funcName);	
				
				//visitChildren(ctx);	
			
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}	

			@Override public String visitCreate_or_alter_procedure(TSQLParser.Create_or_alter_procedureContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String procName = ctx.func_proc_name_schema().getText();
				// set context
				u.setContext("PROCEDURE", procName);
						
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}	
			
			@Override public String visitCreate_or_alter_dml_trigger(TSQLParser.Create_or_alter_dml_triggerContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String trigName = ctx.simple_name().getText();
			
				// set context
				u.setContext("TRIGGER", trigName);
						
				//visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}		
			
			@Override public String visitCreate_or_alter_ddl_trigger(TSQLParser.Create_or_alter_ddl_triggerContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String trigName = ctx.simple_name().getText();
			
				// set context
				u.setContext("TRIGGER", trigName);
						
				//visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}		
			
		};		
		
		TSQLParserBaseVisitor<String> pass2Analysis = new TSQLParserBaseVisitor<String>() {
			int queryCnt = 0;
			int queryIDNr = 0;
			boolean inAtAtErrorPredicate = false;
			boolean inExistsPredicate = false;
			boolean inCTE = false;
			boolean inSubquery = false;
			boolean inDerivedTB = false;
			boolean inSelectStandalone = false;
			boolean inTUDF = false;
			boolean hasSystemVersioningColumn = false;
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
				if (u.debugging) u.dbgOutput(u.thisProc()+"generating new queryIDNr=["+queryIDNr+"]  ", u.debugPtree);		
				//u.printStackTrace();		
				return i;
			}
									
			void popSelectLevel() {
				int popped = queryID.pop();
				if (!queryID.empty()) {
					queryIDNr = queryID.peek();					
				}
				if (u.debugging) u.dbgOutput(u.thisProc()+"popped=["+popped+"]: new queryIDNr=["+queryIDNr+"]  queryCnt=["+queryCnt+"] queryID.size=["+queryID.size()+"] ", u.debugPtree);				
			}			
			
			void newSelectStmt(String s, int lineNr) {	
				queryCnt++;		
				queryIDNr = newQueryIDNr();		
				queryID.push(queryIDNr);
				CompassItem item = new CompassItem(s, lineNr);
				stmt.put(queryIDNr, item);	
				if (u.debugging) u.dbgOutput(u.thisProc()+"new queryIDNr=["+queryIDNr+"]: s=["+s+"]  lineNr=["+lineNr+"] item.lineNr=["+item.getLineNr()+"] ", u.debugPtree);												
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
				if (u.debugging) u.dbgOutput(u.thisProc()+"appended to IDNr=["+IDNr+"]: s=["+s+"] result=["+item.getAttributes()+"] ", u.debugPtree);						
			}		
			
			private void captureAtAtVariables(String varName, int lineNr) {					
				// Catch references to @@ variables. User can also declare @@variables, and that is OK, but not reference them
				if (varName.charAt(1) == '@') {
					if (varName.charAt(0) == '@') {
						if (inAtAtErrorPredicate && varName.equalsIgnoreCase("@@ERROR")) return;  // this is captured elsewhere
						if (!u.getPatternGroup(varName, "^\\@\\@(["+u.identifierChars+"]*)$", 1).isEmpty()) { 
							// is this a known global variable, or a user-defined variable starting with '@@' ?
							if (featureExists(AtAtVariable, varName)) {
								String status = featureSupportedInVersion(AtAtVariable, varName);
								captureItem(varName, "", AtAtVariable, varName, status, lineNr);	
							} 
							else {
								// it's a user-defined name
								String status = featureSupportedInVersion(VarDeclareAtAt);
								captureItem(VarDeclareAtAt, varName, "", varName, status, lineNr);													
							}		 
						}	  						
					}
				}				
			}				
			// --- visit the tree nodes ----------------------------------------
			
			@Override public String visitUse_statement(TSQLParser.Use_statementContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String DBName = u.normalizeName(ctx.dbname.getText());
				u.setCurrentDB(DBName);
				captureItem("USE " + DBName, DBName, DatabasesReportGroup, "", u.Supported, ctx.start.getLine());
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}	
						
			@Override public String visitSelect_statement_standalone(TSQLParser.Select_statement_standaloneContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				inSelectStandalone = true;				
				newSelectStmt("SELECT", ctx.start.getLine());
				if (u.debugging) u.dbgOutput(u.thisProc()+"new SELECT in standalone", u.debugPtree);

				if (ctx.with_expression() != null) {
					addStmtAttribute("WITH");
					if (u.debugging) u.dbgOutput(u.thisProc()+"added WITH...SELECT in standalone", u.debugPtree);
				}			
				visitChildren(ctx);
				inSelectStandalone = false;
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}
										
			@Override public String visitSelect_statement(TSQLParser.Select_statementContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				if (u.debugging) u.dbgOutput(u.thisProc()+"SELECT=["+ctx.getText()+"]  ", u.debugPtree);
				if (u.debugging) u.dbgOutput(u.thisProc()+"inSelectStandalone=["+inSelectStandalone+"]  inSubquery=["+inSubquery+"]  inDerivedTB=["+inDerivedTB+"] ", u.debugPtree);				
				boolean createdNew = false;
				
				if (inSelectStandalone || inSubquery || inDerivedTB) {
					if (inSelectStandalone) inSelectStandalone = false;
					if (inSubquery) inSubquery = false;
					if (inDerivedTB) inDerivedTB = false;
				} 
				else {
					createdNew = true;
					newSelectStmt("SELECT", ctx.start.getLine());
					if (u.debugging) u.dbgOutput(u.thisProc()+"added SELECT in stmt", u.debugPtree);							
				}
				
				if (ctx.order_by_clause() != null) {
					addStmtAttribute("ORDERBY");
				}					
				if (inCTE) {
					addStmtAttribute("CTE");
					inCTE = false;
					if (u.debugging) u.dbgOutput(u.thisProc()+"added CTE", u.debugPtree);		
				}
				
				if (hasParent(ctx.parent,"insert_statement")) {
					addStmtAttribute("INSERT");
					if (u.debugging) u.dbgOutput(u.thisProc()+"added CTE", u.debugPtree);							
				}
				
				visitChildren(ctx);						
				
				if (createdNew) {
					popSelectLevel();
				}				
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}							
				
			@Override public String visitQuery_specification(TSQLParser.Query_specificationContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());

				if (ctx.INTO() != null) {
					String intoTableNameRaw = ctx.into.getText();
					String intoTableName = u.normalizeName(intoTableNameRaw);
					CaptureIdentifier(intoTableNameRaw, intoTableName, "SELECT..INTO", ctx.start.getLine());
					String tabType = getTmpTableType(intoTableName, true);
					addStmtAttribute("INTO "+tabType);						
				}
												
				if (ctx.top_clause() != null) {
					String topClauseText = ctx.top_clause().getText().toUpperCase();
					if (u.debugging) u.dbgOutput(u.thisProc()+"topClauseText  =["+topClauseText+"]", u.debugPtree);
					
					topClauseText = topClauseText.substring(3);
					
					boolean hasPercent = false;
					if (topClauseText.contains("PERCENT")) { // ToDo: is this really faster than a regex directly?
						if (!u.getPatternGroup(topClauseText, "\\b(PERCENT)\\b", 1).isEmpty()) {
							topClauseText = u.applyPatternFirst(topClauseText, "\\bPERCENT\\b", "");
							hasPercent = true;
						} 
						else if (!u.getPatternGroup(topClauseText, "\\d(PERCENT)\\b", 1).isEmpty()) {
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
					if (!u.getPatternGroup(topClauseText, "^(\\d+)$", 1).isEmpty()) {						
						if (!topClauseText.equals("0") && !(topClauseText.equals("100")&&(hasPercent))) {
							topClauseTest = CfgNonZero;
							topClauseText = "<number>";
						}
					} 
					else if (!u.getPatternGroup(topClauseText, "^("+u.varPattern+")$", 1).isEmpty()) {
						topClauseTest = CfgVariable;
						topClauseText = "(@v)";
					} 
					else {
						topClauseTest = CfgExpression;
						topClauseText = "(<expression>)";
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
				}	
				
				if (ctx.groupByAll != null) {
					String status = featureSupportedInVersion(GroupByAll);					
					captureItem(GroupByAll, ctx.getText(), GroupByAll, "", status, ctx.start.getLine());											
				}
				
				CaptureXMLNameSpaces(ctx.parent, "SELECT", ctx.start.getLine());				
				
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}
				
			@Override public String visitOrder_by_clause(TSQLParser.Order_by_clauseContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				if (ctx.OFFSET() != null) {
					String fetch = "";
					if (ctx.FETCH() != null) {
						fetch = "..FETCH";
					}
					captureItem("SELECT..ORDER BY OFFSET"+fetch, "", DMLReportGroup, "", u.Supported, ctx.start.getLine());			
				}		
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}
								
			@Override public String visitQuery_expression(TSQLParser.Query_expressionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				if ((ctx.order_by_qs != null) || (ctx.order_by_qe != null)) {
					addStmtAttribute("ORDERBY");
				}		
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}
														
			@Override public String visitCommon_table_expression(TSQLParser.Common_table_expressionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				inCTE = true;
				visitChildren(ctx);
				inCTE = false;
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}
			
			@Override public String visitSubquery(TSQLParser.SubqueryContext ctx) {
				boolean subqInExists = false;
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				inSubquery = true;
				
				newSelectStmt("SELECT", ctx.start.getLine());
				if (u.debugging) u.dbgOutput(u.thisProc()+"added SELECT for subq", u.debugPtree);							

				if (inExistsPredicate) {
					subqInExists = true;
					inExistsPredicate = false;
				}
								
				addStmtAttribute("SUBQUERY");
				if (u.debugging) u.dbgOutput(u.thisProc()+"added SELECT SUBQUERY ", u.debugPtree);
				
				if (subqInExists) {
					addStmtAttribute("EXISTS");
					if (u.debugging) u.dbgOutput(u.thisProc()+"added EXISTS", u.debugPtree);
				}
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}
			
			@Override public String visitDerived_table(TSQLParser.Derived_tableContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				if (ctx.select_statement() != null) {
					inDerivedTB = true;
					
					newSelectStmt("SELECT", ctx.start.getLine());
					if (u.debugging) u.dbgOutput(u.thisProc()+"added SELECT for derived_tb", u.debugPtree);				
					
					if (!hasParent(ctx.parent,"insert_statement"))  {	
						addStmtAttribute("SUBQUERY");
						if (u.debugging) u.dbgOutput(u.thisProc()+"added SELECT SUBQUERY for derived_tb", u.debugPtree);
					}
				}
				
				visitChildren(ctx);
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}
			
			@Override public String visitFor_clause(TSQLParser.For_clauseContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
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
					forJSONType = "SELECT FOR JSON "+forJSONType;	
					String status = featureSupportedInVersion(JSONFeatures, forJSONType);
					captureItem(forJSONType, "", JSONFeatures, forJSONType, status, ctx.start.getLine());		
				}						
				visitChildren(ctx);				
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}				

			@Override public String visitLocal_id_expr(TSQLParser.Local_id_exprContext ctx) { 
				if (ctx.LOCAL_ID() != null) {				
					captureAtAtVariables(ctx.LOCAL_ID().getText().toUpperCase(), ctx.start.getLine());	
				}					
				visitChildren(ctx);				
				return null; 
			}
			
			@Override public String visitExecute_parameter(TSQLParser.Execute_parameterContext ctx) { 
				if (ctx.LOCAL_ID() != null) {
					captureAtAtVariables(ctx.LOCAL_ID().getText().toUpperCase(), ctx.start.getLine());					
				}	
				
				if (ctx.DEFAULT() != null) {	
					String statusDft = featureSupportedInVersion(ParamValueDEFAULT, "procedure");
					captureItem(ParamValueDEFAULT+", procedure call", "", ParamValueDEFAULT, "procedure", statusDft, ctx.start.getLine());		            
				}
					
				if (ctx.id() != null) {
					String s = ctx.id().getText();
					if (s.charAt(0) == '"') {
						captureDoubleQuotedString(s, ctx.start.getLine());
					}
				}
									
				visitChildren(ctx);				
				return null; 
			}						

			private void captureAtAtErrorValue(Integer exprInt, String via, String op, int lineNr) {
				String status = featureSupportedInVersion(AtAtErrorValueRef, exprInt.toString());
				String usrDefined = "";
				if (exprInt > 50000) usrDefined = " (user-defined)";
				captureItem(AtAtErrorValueRef+ " " +String.format("%6d",exprInt)+usrDefined+via, via, AtAtErrorValueRef, exprInt.toString(), status, lineNr);				
			}					

			private void captureAtAtErrorValueRef (String varName, TSQLParser.PredicateContext ctx) { 
				// capturing @@ERROR = 999, ERROR_NUMBER() = 999 , as well as via variable assignment
				// not captured: more than 1 variable assignment level; 999 = @@ERROR; assignment or comparison through CASE expressions; column = @@ERROR/ERROR_NUMBER()
								
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
				
				String expr1 = cleanupTerm(exprList.get(0).getText());
				String expr2 = "";
				if (exprList.size() > 1) expr2 = cleanupTerm(exprList.get(1).getText());
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
					valueList = new ArrayList<>(Arrays.asList(ctx.expression_list().getText().split(",")));
					for (int i=0; i < valueList.size(); i++) valueList.set(i, cleanupTerm(valueList.get(i)));
				}
				else if (ctx.BETWEEN() != null) {
					op = "BETWEEN";		
					valueList.add(expr2);	
					String expr3 = cleanupTerm(exprList.get(2).getText());
					valueList.add(expr3);	
				}																			
				
				if (!op.isEmpty()) {	
					for (String v : valueList) {	
						Integer exprInt	= getNumericConstant(v, true);
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
						captureItem(AtAtErrorValueRef+ ", comparison with subquery"+via, via2, AtAtErrorValueRef, "", u.ReviewManually, ctx.start.getLine());
					}
				}
				else {
					// cannot figure it out
					captureItem(AtAtErrorValueRef+ ", referenced value unclear"+via, via2, AtAtErrorValueRef, "", u.ReviewManually, ctx.start.getLine());
				}				
			}
										
			@Override public String visitPredicate(TSQLParser.PredicateContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				if (ctx.EXISTS() != null) {
					inExistsPredicate = true;
				}		
								
				// temporary one-off: find integer variables being compared to empty string
//				if (ctx.expression().size() > 0) {
//					String varName = ctx.expression().get(0).getText().toUpperCase();
//					if (varName.charAt(0) == '@') {
//						String varType = localVars.get(varName.toUpperCase());
//						//u.appOutput(u.thisProc()+"varName=["+varName+"] varType=["+varType+"] txt=["+ctx.getText()+"]");
//						if (varType != null) {
//							if (varType.contains("INT")) {
//								if (ctx.expression().size() > 1) {
//									String expr = ctx.expression().get(1).getText();
//									//u.appOutput(u.thisProc()+"expr=["+expr+"] ");
//									if (expr.equals("''") || expr.equals("' '")) {
//										u.appOutput(u.thisProc()+"Found: varName=["+varName+"] varType=["+varType+"] op expr=["+expr+"] ");
//									}
//								}
//							}	
//						}	
//					}
//				}
				
				// find @@ERROR value references						
				if (ctx.expression().size() > 0) {
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
				}
				visitChildren(ctx);				
				inAtAtErrorPredicate = false;
				inExistsPredicate = false;
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}		
			
			private Integer getNumericConstant(String expr) {
				return getNumericConstant(expr, false);
			}
			private Integer getNumericConstant(String expr, boolean stripQuotes) {
				if (stripQuotes) {
					if (expr.startsWith("'") && expr.endsWith("'")) expr = expr.substring(1,expr.length()-1);
					else if (expr.startsWith("N'") && expr.endsWith("'")) expr = expr.substring(2,expr.length()-1);
					else if (expr.startsWith("\"") && expr.endsWith("\"")) expr = expr.substring(1,expr.length()-1);
				}
				try { 
					int i = Integer.parseInt(expr);
					return i;
				} 
				catch (Exception e) { return null; }	
			}
			
			private String cleanupTerm(String s) {
				s = s.trim();
				while (s.startsWith("(")) s = s.substring(1).trim();
				while (s.endsWith(")"))   s = u.removeLastChar(s).trim();				
				return s;
			}	

			@Override public String visitSql_union(TSQLParser.Sql_unionContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());					
				String unionKwd = ctx.union_keyword().getText().toUpperCase();
				unionKwd = unionKwd.replace("ALL", " ALL");
				captureItem(unionKwd, "", DMLReportGroup, "", u.Supported, ctx.start.getLine());
				
				newSelectStmt("SELECT", ctx.start.getLine());
				if (u.debugging) u.dbgOutput(u.thisProc()+"new SELECT for UNION", u.debugPtree);
				
				if ((ctx.order_by_qs != null) || (ctx.order_by_qe != null)) {
					addStmtAttribute("ORDERBY");
				}	
								
				visitChildren(ctx);	
				popSelectLevel();										
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}				
				
			@Override public String visitCreate_type(TSQLParser.Create_typeContext ctx) { 
				// this is duplicated in pass 1
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String UDDname = u.normalizeName(ctx.simple_name().getText());
				String UDDdatatype = "";
				String section = UDDatatypes;
				String statusDataType = u.Supported;
				if (ctx.FROM() != null) {
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
				}
				if (u.debugging) u.dbgOutput(u.thisProc()+"UDD "+ ctx.getText()+", UDDname=["+UDDname+"] UDDdatatype=["+UDDdatatype+"] ", u.debugPtree);
				captureItem("CREATE TYPE, "+UDDdatatype, UDDname, section, "", statusDataType, ctx.start.getLine(), 0);	
				
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}		
			
			@Override public String visitDrop_table(TSQLParser.Drop_tableContext ctx) { 
				captureDropObject("TABLE", ctx.table_name().size(), ctx.if_exists(), "", ctx.start.getLine()); visitChildren(ctx); return null; 
			}	
						
			@Override public String visitDrop_view(TSQLParser.Drop_viewContext ctx) { 
				captureDropObject("VIEW", ctx.simple_name().size(), ctx.if_exists(), ViewsReportGroup, ctx.start.getLine()); visitChildren(ctx); return null; 
			}	
			
			@Override public String visitDrop_procedure(TSQLParser.Drop_procedureContext ctx) { 
				captureDropObject("PROCEDURE", ctx.func_proc_name_schema().size(), ctx.if_exists(), ProceduresReportGroup, ctx.start.getLine()); visitChildren(ctx); return null; 
			}	
						
			@Override public String visitDrop_function(TSQLParser.Drop_functionContext ctx) { 
				captureDropObject("FUNCTION", ctx.func_proc_name_schema().size(), ctx.if_exists(), FunctionsReportGroup, ctx.start.getLine()); visitChildren(ctx); return null; 
			}	

			@Override public String visitDrop_trigger(TSQLParser.Drop_triggerContext ctx) { 
				captureDropObject("TRIGGER", ctx.simple_name().size(), ctx.if_exists(), TriggersReportGroup, ctx.start.getLine()); visitChildren(ctx); return null; 
			}	
			
			@Override public String visitDrop_database(TSQLParser.Drop_databaseContext ctx) { 
				captureDropObject("DATABASE", ctx.id().size(), ctx.if_exists(), DatabasesReportGroup, ctx.start.getLine()); visitChildren(ctx); return null; 
			}	
						
			@Override public String visitDrop_user(TSQLParser.Drop_userContext ctx) { 
				captureDropObject("USER", 1, ctx.if_exists(), UsersReportGroup, ctx.start.getLine()); visitChildren(ctx); return null; 
			}	
						
			@Override public String visitDrop_schema(TSQLParser.Drop_schemaContext ctx) { 
				captureDropObject("SCHEMA", 1, ctx.if_exists(), UsersReportGroup, ctx.start.getLine()); visitChildren(ctx); return null; 
			}	
			
						
			private void captureDropObject(String objType, int nrDropped, TSQLParser.If_existsContext if_exists, String reportGroup, int lineNr) { 
				String status = u.Supported; 		
				String nrDroppedFmt = "";	
				String ifExists = "";	
				if (nrDropped > 1) {
					status = featureSupportedInVersion(DropMultipleObjects, "TABLE");
					nrDroppedFmt = ", >1 object";
				}	
				if (if_exists != null) {				
					ifExists = " IF EXISTS";
					if (status.equals(u.Supported)) {					
						status = featureSupportedInVersion(DropIfExists, "TABLE");
					}
				}			
				if (reportGroup.isEmpty()) reportGroup = DDLReportGroup;
				if (status.equals(u.Supported)) {
					if (featureExists(MiscObjects, objType.toUpperCase())) {
						status = featureSupportedInVersion(MiscObjects, objType.toUpperCase());
					}
				}
				captureItem("DROP "+objType+ifExists+nrDroppedFmt, "", reportGroup, "", status, lineNr);				
			}				
			
			@Override public String visitCreate_table(TSQLParser.Create_tableContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String tableName = u.normalizeName(ctx.tabname.getText());
				if (u.debugging) u.dbgOutput(u.thisProc()+"CREATE table "+ ctx.getText()+", tabName=["+tableName+"] ", u.debugPtree);
				
				// set context
				u.setContext("TABLE", tableName);
												
				String status = u.Supported; // for now
				String tableType = getTmpTableType(tableName);
				if (tableType.equals(GlobalTmpTableFmt)) {
					status = featureSupportedInVersion(GlobalTmpTable);
				}

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
				captureItem(item, tableName, DDLReportGroup, "", status, ctx.CREATE().getSymbol().getLine(), 0);				

				// clear context
				u.resetSubContext();
								
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}				
						
			@Override public String visitColumn_constraint(TSQLParser.Column_constraintContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	

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
				visitChildren(ctx);			
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 				
			}
			
			@Override public String visitTable_constraint(TSQLParser.Table_constraintContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				
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
				
				visitChildren(ctx);			
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
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
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				
				String ixName = u.normalizeName(ctx.id().getText());
				String tableName = u.normalizeName(ctx.table_name().getText());
				String baseObjType = lookupObjType(tableName.toUpperCase());
				if (baseObjType.equals("VIEW")) {
					String status = featureSupportedInVersion(IndexedView);
					captureItem(IndexedView+" (materialized view)", ixName, DDLReportGroup, IndexedView, status, 0, 0);	
					captureItem("CREATE " +"indexed view (materialized view)", "", u.ObjCountOnly, "", u.ObjCountOnly, 0, 0);							
				}
				
				String ixContext = "CREATE INDEX";
				String ixType = "Index"; // uppercase I 
				if (ctx.UNIQUE() != null) ixType = "Index, UNIQUE";		

				captureIndexOptions(ixName, ixType, ixContext, ctx.with_index_options());	
								
				captureIndexConstraint(ixName, ixType, ixContext, ctx.clustered(), false, ctx.start.getLine());
				
				if (ctx.column_name_list() != null) {
					// if there are INCLUDE columns, we can go over the PG max. of 32; (these do not count to the MSSQL max. of 32)
					int nrCols = nrColumn_name_list_with_order(ctx.column_name_list_with_order());
					int nrIncludeCols = nrColumn_name_list(ctx.column_name_list());
					int maxCols = featureIntValueSupportedInVersion(MaxColumnsIndex);
					if (nrCols+nrIncludeCols > maxCols) {
						captureItem("Index exceeds "+maxCols+" columns("+nrCols+", +"+nrIncludeCols+ " included)" , "", DDLReportGroup, MaxColumnsIndex, u.NotSupported, 0, 0);							
					}
				}
				
				if (ctx.COLUMNSTORE() != null) {
					String status = featureSupportedInVersion(IndexAttribute, "COLUMNSTORE");
					captureItem("COLUMNSTORE index: created as regular index", ixName, DDLReportGroup, "COLUMNSTORE", status, 0, 0);							
				}
				
				//ptns
				if (ctx.storage_partition_clause() != null) {
					if (ctx.storage_partition_clause().LR_BRACKET() != null) {
						capturePartitioning("CREATE INDEX", ixName, ctx.start.getLine());
					}
				}	
								
				visitChildren(ctx);			
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}			

			@Override public String visitAlter_index(TSQLParser.Alter_indexContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				
				String ixName = "ALL";
				if (ctx.id() != null) {
					ixName = u.normalizeName(ctx.id().getText());
				}
				String tableName = u.normalizeName(ctx.table_name().getText());
				
				String ixContext = "ALTER INDEX";  //ToDo: handle all options

				if (ctx.alter_index_options() != null) {
					if (ctx.alter_index_options().REBUILD() != null) {
						capturePartitioning("ALTER INDEX REBUILD", ixName, ctx.start.getLine());
					}
				}	
								
				visitChildren(ctx);			
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}			

			@Override public String visitTable_indices(TSQLParser.Table_indicesContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				
				String ixName = noName;
				
				String ixContext = u.uninitialized;
				String ixType = "index"; // lowercase i
				if (ctx.UNIQUE() != null) ixType = "index, UNIQUE";		
				
				if (hasParent(ctx.parent,"create_index"))           ixContext = "CREATE INDEX";
				else if (hasParent(ctx.parent,"create_table"))      ixContext = "CREATE TABLE";
				else if (hasParent(ctx.parent,"alter_table"))       ixContext = "ALTER TABLE";
				else if (hasParent(ctx.parent,"declare_statement")) ixContext = "DECLARE @tableVariable";
				else if (hasParent(ctx.parent,"create_type"))       ixContext = "CREATE TYPE(table)";	

				captureIndexOptions(ixName, ixType, ixContext, ctx.with_index_options());	
								
				captureIndexConstraint(ixName, ixType, ixContext, ctx.clustered(), false, ctx.start.getLine());
				visitChildren(ctx);			
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}			

			@Override public String visitTable_type_indices(TSQLParser.Table_type_indicesContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				
				String ixName = noName;
								
				String ixContext = u.uninitialized;
				String ixType = "index";
				if (ctx.UNIQUE() != null) ixType = "index, UNIQUE";		
				
				if (hasParent(ctx.parent,"declare_statement")) ixContext = "DECLARE @tableVariable";
				else if (hasParent(ctx.parent,"create_type"))       ixContext = "CREATE TYPE(table)";	

				captureIndexOptions(ixName, ixType, ixContext, null);	
								
				captureIndexConstraint(ixName, ixType, ixContext, ctx.clustered(), false, ctx.start.getLine());
				visitChildren(ctx);			
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}			

			@Override public String visitColumn_inline_index(TSQLParser.Column_inline_indexContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				
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
								
				captureItem("CREATE " + ixType.toUpperCase(), "", u.ObjCountOnly, "", u.ObjCountOnly, 0, 0);	
	
				String ixStatus = featureSupportedInVersion(InlineIndex);	
				captureItem(InlineIndex+" in "+ixContext, "", DDLReportGroup, InlineIndex, ixStatus, ctx.start.getLine(), 0);			  								
					
				visitChildren(ctx);			
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}			

			private void captureIndexConstraint(String name, 
			                              String type, 
			                              String context, 
			                              TSQLParser.ClusteredContext clustered, 
			                              boolean desc, 
			                              int lineNr) { 
				if (u.debugging) u.dbgOutput(u.thisProc()+"name=["+name+"] type=["+type+"] context=["+context+"] ", u.debugPtree);
							                              	
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
						String clusteredKwd = clustered.getText().toUpperCase();
						if (clusteredKwd.startsWith("CLUSTERED")) {
							status = featureSupportedInVersion(ClusteredIndex);	
							if (!status.equals(u.Supported)) {
								type += ", CLUSTERED"; 
								userHint += " : created as NONCLUSTERED, no physical row order";
							}				
						}
					}
				}

				if (type.startsWith("index")) { // lowercase i matters -- this is messy as we're callied with 'Index' as well as 'index'
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
						userHint += " : created as ASC";		
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
				if (u.debugging) u.dbgOutput(u.thisProc()+"capturing: type=["+type+"] context=["+context+"] ", u.debugPtree);
				captureItem(u.capitalizeFirstChar(fmt), name, DDLReportGroup, "", status, lineNr, 0);			  				
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
						if (u.debugging) u.dbgOutput(u.thisProc()+"type=["+type+"]  option=["+option+"] optVal=["+optVal+"]  ", u.debugPtree);

						if (type.equals("PRIMARY KEY")) {
							type = "Constraint PRIMARY KEY";
						} 
						else if (type.equals("UNIQUE")) {
							type = "Constraint UNIQUE";
						}
						
						if (u.debugging) u.dbgOutput(u.thisProc()+"type=["+type+"] ", u.debugPtree);
									
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
							captureItem(u.capitalizeFirstChar(type)+", "+option+"="+optVal+context+userHint, name, IgnoreDupkeyIndex, "", status, ixOp.start.getLine());
						} 
						else if (featureExists(IndexOptions,option)) {
							String type2 = u.applyPatternFirst(type, "^index, UNIQUE\\b", "index");
							String status = featureSupportedInVersion(IndexOptions,option);
							captureItem(u.capitalizeFirstChar(type2)+", "+option+"="+optVal+context, name, DDLReportGroup, IndexOptions, status, ixOp.start.getLine());
						} 
						else {
							// if we get here, something is missing from the .cfg file
							u.appOutput("["+IndexOptions+"] feature '"+option+"' not found in .cfg file");
							u.errorExitStackTrace();    // in production, we should probably just continue	
						}
					}
				}
			}
							
			@Override public String visitColumn_definition(TSQLParser.Column_definitionContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String colName = u.normalizeName(ctx.id().getText());
	            boolean isCompCol = false;
	            
	            //find type of column by looking for parent				
				String colType = ""; // default: regular table
				if (hasParent(ctx.parent,"declare_statement"))                 colType = "(table variable)";
				else if (hasParent(ctx.parent,"create_type"))                  colType = "(table type)";	
				else if (hasParent(ctx.parent,"func_body_returns_table"))      colType = "(table function result)";	
				else if (hasParent(ctx.parent,"func_body_returns_table_clr"))  colType = "(table function result)";	

				if (u.debugging) u.dbgOutput(u.thisProc()+"colName=["+colName+"] colType=["+colType+"] ", u.debugPtree);
	            
	            if (ctx.data_type() != null) {
	            	// regular column
	            	String dataType = u.normalizeName(ctx.data_type().getText().toUpperCase(), "datatype");
	            		            	
	            	if (ctx.data_type().IDENTITY() != null) {
	            		dataType = u.applyPatternFirst(dataType, "^(.*)(IDENTITY.*?)$", "$1 $2");
	            		dataType = u.applyPatternFirst(dataType, "((NOT)?\\s+NULL)?$", "");
	            		String identityCol = u.getPatternGroup(dataType, "^.*?(IDENTITY(\\(.*?\\))?)((NOT)?\\s+NULL)?$", 1);        			
	            		if (u.debugging) u.dbgOutput(u.thisProc()+"IDENTITY in data_type: colName=["+colName+"] dataType=["+dataType+"] identityCol=["+identityCol+"] ", u.debugPtree);
	            		
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
	            			identityCol = u.getPatternGroup(ctx.getText(), "^.*?(IDENTITY\\(.*?\\)).*$", 1);
	            		}
	            		dataType += " " + identityCol;
	            		if (u.debugging) u.dbgOutput(u.thisProc()+"IDENTITY in column_definition: colName=["+colName+"] dataType=["+dataType+"] identityCol=["+identityCol+"] ", u.debugPtree);
	            		
	            		captureForReplication(colName, "IDENTITY", "", ctx.for_replication());	
	            	}
	            	
	            	if (u.debugging) u.dbgOutput(u.thisProc()+"column: colName=["+colName+"] dataType=["+dataType+"]", u.debugPtree);
	            	
					String status = u.Supported;
					if (featureExists(Datatypes, getBaseDataType(dataType))) {
						status = featureSupportedInVersion(Datatypes, getBaseDataType(dataType));
					} 
					else {
						// datatype is not listed, means: supported 					
					}		            
					captureItem(dataType+" column "+colType, colName, Datatypes, getBaseDataType(dataType), status, ctx.start.getLine());			            	
	            }
	            
	            if (ctx.AS() != null) {
	            	// computed column
	            	isCompCol = true;	
	            	String status = u.Supported;
	            	String persisted = "";
	            	String expression = ctx.expression().getText().toUpperCase();
	            	if (u.debugging) u.dbgOutput(u.thisProc()+"compcol: colName=["+colName+"] expression=["+expression+"] ", u.debugPtree);
	            	
	            	if (ctx.PERSISTED() == null) {   
	            		status = featureSupportedInVersion(NonPersistedCompCol);
	            		if (status.equals(u.Supported)) {
	            			persisted = " (not persisted)";
	            		} 
	            		else {
	            			// BBF will create the column if it can
	            			persisted = " (not persisted, but created as persisted)";
	            		}	            		
	            	} 
	            	else {
	            		persisted = " (persisted)";
	            	}
					captureItem("Computed column"+persisted+colType, colName, NonPersistedCompCol, "", status, ctx.start.getLine());				
	            }
				
				if (isCompCol) inCompCol = true;				
				visitChildren(ctx);			
				if (isCompCol) inCompCol = false;						
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}				
									
			@Override public String visitSpecial_column_option(TSQLParser.Special_column_optionContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String option = u.normalizeName(ctx.getText().toUpperCase());	
				option = u.applyPatternAll(option, "^(\\w+)\\b.*$", "$1");
				if (u.debugging) u.dbgOutput(u.thisProc()+"option=["+option+"]  ", u.debugPtree);
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
					
					String status = featureSupportedInVersion(ColumnAttribute);
					captureItem(ColumnAttribute+" " +option, colName, ColumnAttribute, option, status, ctx.start.getLine(), 0);				
				}
															
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}	
												
			@Override public String visitAlter_table(TSQLParser.Alter_tableContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String tableName = u.normalizeName(ctx.tabname.getText());
				
				if (u.debugging) u.dbgOutput(u.thisProc()+"ALTER table "+ ctx.getText()+", tableName=["+tableName+"] ", u.debugPtree);
				
				// set context
				u.setContext("TABLE", tableName);
				
				//  find sub-command & determine if supported
				String status = u.Supported; 
				String subcmd = "";
				boolean captured = false;
				String EnDisAble = "ENABLE";
				if (ctx.DISABLE() != null) EnDisAble = "DISABLE";
				
				String CheckNoCheck = "CHECK";
				if (ctx.NOCHECK().size() > 0) EnDisAble = "NOCHECK";
				
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
				else if (ctx.file_table_option().size() >0) {
					subcmd = "SET <filetable>";		
					status = featureSupportedInVersion(AlterTable, "SET FILETABLE");								
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
					status = featureSupportedInVersion(AlterTable, subcmd);									
				}
				else if (ctx.column_def_table_constraints() != null) {
					int nrAdd = ctx.column_def_table_constraints().COMMA().size() + 1;
					if (nrAdd > 1) {
						subcmd = "ADD multiple columns/constraints";
						status = featureSupportedInVersion(AlterTable, subcmd);		
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
									subcmd = "DROP <constraint-name>";					
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
				
				if (u.debugging) u.dbgOutput(u.thisProc()+"subcmd=["+subcmd+"] captured=["+captured+"] ", u.debugPtree);
				if (!captured) {
					if (!subcmd.isEmpty()) subcmd = ".." + subcmd;
					captureItem("ALTER TABLE"+subcmd, tableName, DDLReportGroup, "", status, ctx.start.getLine(), 0);				
				}
															
				visitChildren(ctx);	

				// clear context
				u.resetSubContext();
								
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}				
			
			@Override public String visitCreate_or_alter_dml_trigger(TSQLParser.Create_or_alter_dml_triggerContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String trigName = u.normalizeName(ctx.simple_name().getText());
				if (u.debugging) u.dbgOutput(u.thisProc()+"proc "+ ctx.getText()+", trigName=["+trigName+"] ", u.debugPtree);
				
				String kwd = "CREATE";
				String status = u.Supported;
				if (ctx.ALTER() != null) {
					kwd = "ALTER";
					status = featureSupportedInVersion("ALTER TRIGGER");
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
				
				if (ctx.external_name() != null) {
					String trigStatus = featureSupportedInVersion(TriggerOptions, "external");					
					captureItem(kwd + " TRIGGER, external", trigName, TriggerOptions, "EXTERNAL", trigStatus, ctx.start.getLine(), 0);
				} 
				else {
					String IOT = "";
					if (trigType.equals("INSTEAD OF")) {
						boolean IOT_supported = false;
						if (featureSupportedInVersion(InsteadOfTrigger, "TABLE").equals(u.Supported) || featureSupportedInVersion(InsteadOfTrigger, "VIEW").equals(u.Supported)) {
							IOT_supported = true;
						}
						String baseObj = lookupObjType(trigBaseTable.toUpperCase());
						if (!baseObj.isEmpty()) {
							IOT = " on " + baseObj.toLowerCase();							
							String trigStatus = featureSupportedInVersion(InsteadOfTrigger, baseObj);	
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
								
				// set context
				u.setContext("TRIGGER", trigName);		
				
				String trigSchema = u.getSchemaNameFromID(trigName);
				if (!trigSchema.isEmpty()) {
					String statusSchema = featureSupportedInVersion(TriggerSchemaName);
					if (!statusSchema.equals(u.Supported)) {
						captureItem(TriggerSchemaName, trigName, TriggerSchemaName, "", statusSchema, ctx.start.getLine());						
					}
				}		

				// options
				List<TSQLParser.Trigger_optionContext> options = ctx.trigger_option();
				captureTriggerOptions("DML", trigName, options, ctx.start.getLine());
				
				captureForReplication(trigName, "TRIGGER", kwd, ctx.for_replication());	
						
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}		

			private void captureTriggerOptions(String type, String trigName, List<TSQLParser.Trigger_optionContext> options, int lineNr) {				
				boolean schemabindingFound = false;				
				for (int i=0; i<options.size(); i++) {	
					String option = options.get(i).getText().toUpperCase();
					String optionValue = getOptionValue(option);
					option = getOptionName(option);
					if (option.equals("SCHEMABINDING")) schemabindingFound = true;  	// need to check for absence of SCHEMABINDING
					String trigStatus = featureSupportedInVersion("", TriggerOptions, option, optionValue);
					captureItem("Trigger, option WITH "+formatOptionDisplay(option,optionValue), trigName, TriggerOptions, option, trigStatus, lineNr);	
				}
				if (type.equals("DML")) {
					captureNoSchemabinding(schemabindingFound, "Trigger", trigName, TriggerOptions, lineNr);										
				}
			}
			
			@Override public String visitCreate_or_alter_ddl_trigger(TSQLParser.Create_or_alter_ddl_triggerContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String trigName = u.normalizeName(ctx.simple_name().getText());
				if (u.debugging) u.dbgOutput(u.thisProc()+"proc "+ ctx.getText()+", trigName=["+trigName+"] ", u.debugPtree);
				
				String kwd = "CREATE";
				String status = u.Supported;
				if (ctx.ALTER() != null) {
					kwd = "ALTER";
					status = featureSupportedInVersion("ALTER TRIGGER");
				}

				// ToDo: get & validate DDL events
				String trigAction = "ToBeRetrieved";
				if (featureSupportedInVersion(DDLTrigger, trigAction).equals(u.NotSupported)) {
					status = u.NotSupported;
				}
				captureItem(kwd + " TRIGGER (DDL)", trigName, DDLTrigger, trigAction, status, ctx.start.getLine(),  batchLines.toString());

				// set context
				u.setContext("TRIGGER", trigName);

				// options
				List<TSQLParser.Trigger_optionContext> options = ctx.trigger_option();
				captureTriggerOptions("DDL", trigName, options, ctx.start.getLine());
						
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
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
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String funcName = u.normalizeName(ctx.func_proc_name_schema().getText());
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
					
					String statusDataType = u.Supported;
					if (featureExists(Datatypes, getBaseDataType(sudfDataType))) {
						statusDataType = featureSupportedInVersion(Datatypes, getBaseDataType(sudfDataType));
					} 
					else {
						// datatype is not listed, means: supported 	
					}		 
					if (u.debugging) u.dbgOutput(u.thisProc()+"UDF "+ ctx.getText()+", funcName=["+funcName+"] sudfDataType=["+sudfDataType+"] ", u.debugPtree);					          								
					captureItem(sudfDataType + " scalar function result type", "", Datatypes, getBaseDataType(sudfDataType), statusDataType, ctx.start.getLine());	
					
					if (ctx.func_body_returns_scalar().RETURN() != null) {
						captureItem("RETURN"+" <scalar>, in function", "", ControlFlowReportGroup, "RETURN", u.Supported, ctx.func_body_returns_scalar().RETURN().getSymbol().getLine());							  
					}
				} 
				else if (ctx.func_body_returns_table() != null) {
					udfType = "table";
					options = ctx.func_body_returns_table().function_option(); 
					captureItem("RETURN"+" <result set>, in function", "", ControlFlowReportGroup, "RETURN", u.Supported, ctx.func_body_returns_table().RETURN().getSymbol().getLine());	
				} 
				else if (ctx.func_body_returns_select() != null) {
					udfType = "inline table";
					options = ctx.func_body_returns_select().function_option();
					captureItem("RETURN"+" <result set>, in function", "", ControlFlowReportGroup, "RETURN", u.Supported, ctx.func_body_returns_select().RETURN().getSymbol().getLine());	
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
					status = featureSupportedInVersion("ALTER FUNCTION");
				}
			
				if (u.debugging) u.dbgOutput(u.thisProc()+"UDF "+ ctx.getText()+", funcName=["+funcName+"] udfType=["+udfType+"] udfType2=["+udfType2+"] ", u.debugPtree);

				// capture UDF
				if (!udfType2.isEmpty()) {
					String udfStatus = featureSupportedInVersion(FunctionOptions, udfType2);
					captureItem(kwd + " FUNCTION, " + udfType + ", " + udfType2, funcName, FunctionOptions, udfType2, udfStatus, ctx.start.getLine(), 0);
				} 
				else {
					captureItem(kwd + " FUNCTION, " + udfType, funcName, FunctionOptions, udfType, status, ctx.start.getLine(), batchLines.toString());
				}
				
				// set context
				u.setContext("FUNCTION", funcName);		
				captureParameters("function", ctx.procedure_param());						

				// options
				boolean schemabindingFound = false;
				for (int i=0; i<options.size(); i++) {	
					String option = options.get(i).getText().toUpperCase();
					if (option.startsWith("RETURNSNULL")) option = "RETURNS NULL ON NULL INPUT"; 
					if (option.startsWith("CALLED")) option = "CALLED ON NULL INPUT"; 
					String optionValue = getOptionValue(option);
					option = getOptionName(option);
					if (option.equals("SCHEMABINDING")) schemabindingFound = true;  	// need to check for absence of SCHEMABINDING
					String funcStatus = featureSupportedInVersion("", FunctionOptions, option, optionValue);
					captureItem("Function, option WITH "+formatOptionDisplay(option,optionValue), funcName, FunctionOptions, option, funcStatus, ctx.start.getLine());	
				}
				captureNoSchemabinding(schemabindingFound, "Function", funcName, FunctionOptions, ctx.start.getLine());
				
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}	
						
			@Override public String visitCreate_or_alter_procedure(TSQLParser.Create_or_alter_procedureContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String procName = u.normalizeName(ctx.func_proc_name_schema().getText());
				if (u.debugging) u.dbgOutput(u.thisProc()+"proc "+ ctx.getText()+", procName=["+procName+"] ", u.debugPtree);
				
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
					status = featureSupportedInVersion("ALTER PROCEDURE");
				}
				boolean captured = false;
				
				if (ctx.proc_version != null) {
					String procStatus = featureSupportedInVersion(ProcVersionDeclare);
					captureItem(kwd + " PROCEDURE proc;version", procName+";"+ctx.proc_version.getText(), ProcVersionDeclare, "", procStatus, ctx.start.getLine(), batchLines.toString());	
					captured = true;
				}

				if (!captured) { 
					if (procName.startsWith("#") || procName.startsWith("[#")) {
						String procStatus = featureSupportedInVersion(TemporaryProcedures);
						if (!procStatus.equals(u.Supported)) {
							captureItem(kwd + " PROCEDURE, #temporary: not dropped automatically", procName, TemporaryProcedures, "", procStatus, ctx.start.getLine(), batchLines.toString());	
							captured = true;
						}
					}
				}
				
				if (!captured) { 
					if (!procType.isEmpty()) {
						String procStatus = featureSupportedInVersion(ProcedureOptions,procType);
						captureItem(kwd + " PROCEDURE, "+procType, procName, ProcedureOptions, procType, procStatus, ctx.start.getLine(), batchLines.toString());	
						captured = true;		
					}
				}

				if (!captured) { 
					captureItem(kwd + " PROCEDURE", procName, ProceduresReportGroup, "", status, ctx.start.getLine(), batchLines.toString());	
					captured = true;		
				}
								
				// set context
				u.setContext("PROCEDURE", procName);				
				captureParameters("procedure", ctx.procedure_param());

				// options
				boolean schemabindingFound = false;
				List<TSQLParser.Procedure_optionContext> options = ctx.procedure_option();				
				for (TSQLParser.Procedure_optionContext optionX : options) {	
					String option = optionX.getText().toUpperCase();
					String optionValue = getOptionValue(option);
					option = getOptionName(option);
					u.currentObjectAttributes += " " + option + " ";
					if (option.equals("SCHEMABINDING")) schemabindingFound = true;  	// need to check for absence of SCHEMABINDING
					String procStatus = featureSupportedInVersion("", ProcedureOptions, option, optionValue);
					if (option.equals("RECOMPILE") && (!procStatus.equals(u.Supported))) procStatus = u.ReviewPerformance;
					captureItem("Procedure, option WITH "+formatOptionDisplay(option,optionValue), procName, ProcedureOptions, option, procStatus, ctx.start.getLine());	
				}
				captureNoSchemabinding(schemabindingFound, "Procedure", procName, ProcedureOptions, ctx.start.getLine());
						
				captureForReplication(procName, "PROCEDURE", kwd, ctx.for_replication());	
						
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}		
		
			private void captureParameters(String objType, List<TSQLParser.Procedure_paramContext> params) {			
				for(int i=0; i<params.size(); i++) {
		            String parName = params.get(i).LOCAL_ID().getText();
		            String dataType = u.normalizeName(params.get(i).data_type().getText().toUpperCase(), "datatype");
		            String parDft = params.get(i).default_val != null ? params.get(i).default_val.getText().toUpperCase() : "";
		            String parOpt = params.get(i).param_option != null ? params.get(i).param_option.getText().toUpperCase() : "";
		            addLocalVars(parName, dataType);

					String UDD = u.isUDD(dataType);		
					String UDDfmt = "";
					if (UDD.equals("TABLE")) {
						dataType = "TABLE";
					} 
					else if (!UDD.isEmpty()) {
						UDDfmt = "(UDD "+dataType+") ";
						dataType = UDD;
					}							

		            String parItem = dataType + " " + UDDfmt + objType +  " parameter";		            
				  	if (!parDft.isEmpty()) {				
				  		parItem += " (with default value)";
						// check for numeric-as-date
						checkNumericDateVarAssign(parName, params.get(i).default_val, params.get(i).start.getLine());				  	
				  	}
				  	
				  	if (parOpt.equals("OUT") || parOpt.equals("OUTPUT")) parItem += " (OUTPUT)";
				  	parItem = parItem.replaceFirst("value\\) \\(", "value, ");
		            
					String statusDataType = u.Supported;
					if (featureExists(Datatypes, getBaseDataType(dataType))) {
						statusDataType = featureSupportedInVersion(Datatypes, getBaseDataType(dataType));
					} 
					else {
						// datatype is not listed, means: supported 		
					}		            
					// ToDo: show UDD mapping
					captureItem(parItem, parName, dataType.equals("TABLE")? TableVariablesType : Datatypes, getBaseDataType(dataType), statusDataType, params.get(i).start.getLine());		            
		        }
		        
		        // check max # parameters
		        String maxParSection = MaxProcParameters;
		        if (objType.equalsIgnoreCase("FUNCTION")) {
		        	maxParSection = MaxFuncParameters;
		        }
				Integer maxPars = featureIntValueSupportedInVersion(maxParSection);
				if (params.size() > maxPars) {
					captureItem("Number of "+objType+" parameters ("+params.size()+") exceeds "+maxPars, maxPars.toString(), maxParSection, maxPars.toString(), u.NotSupported, 0, 0);							
				}
							        
		    }		
				
			private void captureNoSchemabinding (boolean schemabindingFound, String objType, String objName, String section, int lineNr) {				
				if (schemabindingFound) return;	
				String option = "without SCHEMABINDING";
				String hint = ": created as WITH SCHEMABINDING";
				String status = featureSupportedInVersion(section, option);
				captureItem(objType+", "+option+hint, objName, section, option, status, lineNr);
			}

			@Override public String visitCreate_or_alter_view(TSQLParser.Create_or_alter_viewContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String viewName = u.normalizeName(ctx.simple_name().getText());
				if (u.debugging) u.dbgOutput(u.thisProc()+"view "+ ctx.getText()+", viewName=["+viewName+"] ", u.debugPtree);
				
				String kwd = "CREATE";
				String status = u.Supported;
				if (ctx.ALTER() != null) {
					kwd = "ALTER";
					status = featureSupportedInVersion("ALTER VIEW");
				}
				captureItem(kwd + " VIEW", "", ViewsReportGroup, "", status, ctx.start.getLine(),  batchLines.toString());
				
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
				captureNoSchemabinding(schemabindingFound, "View", viewName, ViewOptions, ctx.start.getLine());
				
				if (ctx.CHECK() != null) {
					String option = "CHECK OPTION";
					String statusOpt = featureSupportedInVersion(ViewOptions, option);
					captureItem("View, with "+option, "", ViewOptions, option, statusOpt, ctx.start.getLine());					
				}
						
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}					
									
			@Override public String visitRanking_windowed_function(TSQLParser.Ranking_windowed_functionContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String aggFuncName = u.uninitialized;
				if (ctx.agg_func != null) aggFuncName = ctx.agg_func.getText();
				else if (ctx.NTILE() != null) aggFuncName = ctx.NTILE().getText();				
				captureAggregateFunction(aggFuncName, ctx.start.getLine());
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 					
			}

			@Override public String visitAnalytic_windowed_function(TSQLParser.Analytic_windowed_functionContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String aggFuncName = "-init";
				if (ctx.first_last != null) aggFuncName = ctx.first_last.getText();
				else if (ctx.lag_lead != null) aggFuncName = ctx.lag_lead.getText();
				else if (ctx.rank != null) aggFuncName = ctx.rank.getText();
				else if (ctx.pct != null) aggFuncName = ctx.pct.getText();
				captureAggregateFunction(aggFuncName, ctx.start.getLine());
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 					
			}

			@Override public String visitAggregate_windowed_function(TSQLParser.Aggregate_windowed_functionContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String aggFuncName = u.uninitialized;
				if (ctx.agg_func != null) aggFuncName = ctx.agg_func.getText();
				else if (ctx.cnt != null) aggFuncName = ctx.cnt.getText();
				else if (ctx.CHECKSUM_AGG() != null) aggFuncName = ctx.CHECKSUM_AGG().getText();
				else if (ctx.GROUPING() != null) aggFuncName = ctx.GROUPING().getText();
				else if (ctx.GROUPING_ID() != null) aggFuncName = ctx.GROUPING_ID().getText();				
				captureAggregateFunction(aggFuncName, ctx.start.getLine());
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
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
					u.errorExitStackTrace(); // in production, we should probably just continue		
				}
				captureItem(aggFuncName+"()", "", AggregateFunctions, aggFuncName, status, lineNr);				
			}
						
			@Override public String visitFunction_call(TSQLParser.Function_callContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				
				if (ctx.func_proc_name_server_database_schema() != null) {
					String funcName = u.normalizeName(ctx.func_proc_name_server_database_schema().getText().toUpperCase());
					if (u.debugging) u.dbgOutput(u.thisProc()+"scalar fname ["+ ctx.getText()+"], funcName=["+funcName+"] ", u.debugPtree);	
					
					boolean done = false;	
					String funcSchemaName = u.getSchemaNameFromID(funcName);
					String funcObjName = u.getObjectNameFromID(funcName);
					if (funcSchemaName.equals("SYS") || funcSchemaName.isEmpty() || funcObjName.startsWith("FN_")) {
						// is this a system function?
						if (featureExists(SystemFunctions, funcObjName)) {
							String status = featureSupportedInVersion(SystemFunctions, funcObjName);
							captureItem(funcObjName, "", SystemFunctions, funcObjName, status, ctx.start.getLine());	
							done = true;
						}
						else {
							if (u.debugging) u.dbgOutput(u.thisProc()+"feature not exists: funcObjName=["+funcObjName+"] ", u.debugPtree);	
						}
					}

					if (!done) {
						if (!inTUDF) {					
							TSQLParser.Function_arg_listContext argListRaw = ctx.function_arg_list();
							int nrArgs = argListCount( ctx.function_arg_list()); 
			//				u.appOutput("scalar nrArgs=["+nrArgs+"]  ctx childcount=["+ctx.getChildCount()+"]");					
			//				if (argList != null) u.appOutput("scalar arglist childcount=["+argList.getChildCount()+"]  arglist=["+ argList.getText()+"]  ");					
			//				for (int i = 0; i <ctx.getChildCount(); i++) {
			//					u.appOutput(u.thisProc()+"child i=["+i+"/"+nrArgs+"]  txt ib=["+ctx.getChild(i).getText()+"] ");
			//				}		
							
							List<TSQLParser.ExpressionContext> argList = new ArrayList<>();						
							if (nrArgs > 0) {
								if (argListRaw.STAR() != null) nrArgs--;
								if (nrArgs > 0) {
									argList = argListRaw.expression();
									if (u.debugging) u.dbgOutput(u.thisProc()+"nrArgs=["+nrArgs+"] arglist exprlist size=["+argList.size()+"]  ", u.debugPtree);
									for (int i = 0; i <nrArgs; i++) {
										TSQLParser.ExpressionContext expr = argList.get(i);
										if (u.debugging) u.dbgOutput(u.thisProc()+"arglist expr i=["+i+"/"+nrArgs+"] =["+argList.get(i).getText()+"]  ", u.debugPtree);
										// need a function to evaluate the datatype of an expression
										
										if (argList.get(i).getText().equalsIgnoreCase("DEFAULT")) {	
											String statusDft = featureSupportedInVersion(ParamValueDEFAULT, "function");
											captureItem(ParamValueDEFAULT+", function call", "", ParamValueDEFAULT, "function", statusDft, ctx.start.getLine());		            
										}								
									}
								}
							}						
							
							// is this a BIF or SUDF?
							if (featureExists(BuiltInFunctions, funcName)) {
								captureBIF(funcName, ctx.start.getLine(), "", nrArgs, argList);	
							} 
							else {  
								// check for XML/HIERARCHYID methods, these can be parsed as UDF calls				
								if (udfIsBifMethod(funcName, u.XMLmethods, u.SUDFNamesLikeXML, u.SUDFSymTab)) {															
									captureXMLFeature("XML.", u.getObjectNameFromID(funcName), "()", ctx.start.getLine());	
									
									// check for EVENTDATA(), in case of EVENTDATA().VALUE(...)
									if (u.currentObjectType.equals("TRIGGER")) {
										if (funcName.startsWith("EVENTDATA()")) {
											captureBIF("EVENTDATA", ctx.start.getLine(), "", 0, null);	
										}
									}
								}
								else if (udfIsBifMethod(funcName, u.HIERARCHYIDmethods, u.SUDFNamesLikeHIERARCHYID, u.SUDFSymTab)) {															
									captureHIERARCHYIDFeature("HIERARCHYID.", u.getObjectNameFromID(funcName), "()", ctx.start.getLine());	
								} 
								else {
									String statusUDF = u.Supported;	
									String compCol = "";
									if (inCompCol) {
										// this is a SUDF call inside a computed column
										compCol += ", in computed column";
										statusUDF = featureSupportedInVersion(CompColFeatures, CfgScalarUdfCall);
									}																
									captureItem("Function call, scalar"+compCol, funcName+"()", FunctionsReportGroup, "", statusUDF, ctx.start.getLine());
								}
							}
						} 
						else {
							captureItem("Function call, table", funcName, FunctionsReportGroup, "", u.Supported, ctx.start.getLine());
							inTUDF = false;
						}
					}
				}
				else if (ctx.partition_function_call() != null) {
					String funcName = u.normalizeName(ctx.partition_function_call().func_name.getText());
					capturePartitioning("$PARTITION", funcName, ctx.start.getLine());					
				}
				else if (ctx.NEXT() != null) {
					String seqName = u.normalizeName(ctx.full_object_name().getText());
					String statusNVF = featureSupportedInVersion(NextValueFor);
					captureItem(NextValueFor, seqName, NextValueFor, "", statusNVF, ctx.start.getLine());				
				}
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}		

						
			private boolean udfIsBifMethod(String funcName, List<String>refMethodList,  Map<String, String>UDFListLikeMethod,  Map<String, String>UDFList) {
				boolean isMethod = false;
				// ToDo: we can improve here: if the function takes a non-string as input argument, it cannot be an XML method for example
				String funcNameBase = u.getObjectNameFromID(funcName);
				if (u.debugging) u.dbgOutput(u.thisProc()+"funcName=["+funcName+"]  funcNameBase=["+funcNameBase+"] refMethodList=["+refMethodList+"]  UDFListLikeMethod=["+UDFListLikeMethod.keySet()+"] UDFList=["+UDFList.keySet()+"] ", u.debugPtree);	
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
				if (u.debugging) u.dbgOutput(u.thisProc()+"return: isMethod=["+isMethod+"]  ", u.debugPtree);
				return isMethod;
			}												
			
			@Override public String visitHierarchyid_coloncolon_methods(TSQLParser.Hierarchyid_coloncolon_methodsContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());				
				String funcType = "";
				captureHIERARCHYIDFeature("HIERARCHYID.", ctx.method.getText().toUpperCase(), "()", ctx.start.getLine());						
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}	
			
			@Override public String visitGraph_clause(TSQLParser.Graph_clauseContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());				

				String txt = ctx.getText().toUpperCase();
				txt = txt.substring(2);
				String status = featureSupportedInVersion(SQLGraph);
				captureItem("CREATE TABLE..AS "+txt, "", SQLGraph, "", status, ctx.start.getLine());
					
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}						
				
			@Override public String visitRowset_function(TSQLParser.Rowset_functionContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());				
				String funcType = "";
				if (ctx.open_xml() != null) funcType = "OPENXML";
				else if (ctx.open_json() != null) funcType = "OPENJSON";
				else if (ctx.open_query() != null) funcType = "OPENQUERY";
				else if (ctx.open_datasource() != null) funcType = "OPENDATASOURCE";
				else if (ctx.open_rowset() != null) funcType = "OPENROWSET";
				else if (ctx.change_table() != null) funcType = "CHANGETABLE";
				else if (ctx.predict_function() != null) funcType = "PREDICT";
				captureBIF(funcType, ctx.start.getLine());							

				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}	
												
			@Override public String visitFreetext_function(TSQLParser.Freetext_functionContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());				
				String funcType = "";
				if (ctx.CONTAINSTABLE() != null) funcType = "CONTAINSTABLE";
				else if (ctx.FREETEXTTABLE() != null) funcType = "FREETEXTTABLE";
				else if (ctx.SEMANTICSIMILARITYTABLE() != null) funcType = "SEMANTICSIMILARITYTABLE";
				else if (ctx.SEMANTICKEYPHRASETABLE() != null) funcType = "SEMANTICKEYPHRASETABLE";
				else if (ctx.SEMANTICSIMILARITYDETAILSTABLE() != null) funcType = "SEMANTICSIMILARITYDETAILSTABLE";
				captureBIF(funcType, ctx.start.getLine());							

				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}	
									
			@Override public String visitFreetext_predicate(TSQLParser.Freetext_predicateContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());				
				String funcType = "";
				if (ctx.CONTAINS() != null) funcType = "CONTAINS";
				else if (ctx.FREETEXT() != null) funcType = "FREETEXT";
				captureBIF(funcType, ctx.start.getLine());							

				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}	
									
			@Override public String visitBuilt_in_functions(TSQLParser.Built_in_functionsContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());				
				if (ctx.bif_no_brackets != null) {
					captureBIF(ctx.bif_no_brackets.getText().toUpperCase(), ctx.start.getLine(), "nobracket");							
				}
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}	
				
			@Override public String visitBif_cast_parse(TSQLParser.Bif_cast_parseContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());				

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
				checkNumericAsDate(dataType, funcName, funcName+"()", ctx.expression(), ctx.start.getLine());

				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}	
				
			@Override public String visitBif_convert(TSQLParser.Bif_convertContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());				

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
				checkNumericAsDate(dataType, funcName, funcName+"()", ctx.convert_expression, ctx.start.getLine());

				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}	
			
			@Override public String visitTRIM(TSQLParser.TRIMContext ctx) { 
				captureBIF("TRIM", ctx.start.getLine());			
				visitChildren(ctx);	
				return null; 
			}		
				
			@Override public String visitIIF(TSQLParser.IIFContext ctx) { 
				captureBIF("IIF", ctx.start.getLine());			
				visitChildren(ctx);	
				return null; 
			}		
								
			@Override public String visitSTRING_AGG(TSQLParser.STRING_AGGContext ctx) { 
				captureBIF("STRING_AGG", ctx.start.getLine());			
				visitChildren(ctx);	
				return null; 
			}				
											
			@Override public String visitXml_exist_call(TSQLParser.Xml_exist_callContext ctx) { 
				captureXMLFeature("XML.", "EXIST", "()", ctx.start.getLine());			
				visitChildren(ctx);	
				return null; 
			}							
			@Override public String visitXml_modify_call(TSQLParser.Xml_modify_callContext ctx) { 
				captureXMLFeature("XML.", "MODIFY", "()", ctx.start.getLine());			
				visitChildren(ctx);	
				return null; 
			}				
			@Override public String visitXml_query_call(TSQLParser.Xml_query_callContext ctx) { 
				captureXMLFeature("XML.", "QUERY", "()", ctx.start.getLine());			
				visitChildren(ctx);	
				return null; 
			}				
			@Override public String visitXml_value_call(TSQLParser.Xml_value_callContext ctx) { 
				captureXMLFeature("XML.", "VALUE", "()", ctx.start.getLine());			
				visitChildren(ctx);	
				return null; 
			}		
			@Override public String visitXml_nodes_method(TSQLParser.Xml_nodes_methodContext ctx) { 
				captureXMLFeature("XML.", "NODES", "()", ctx.start.getLine());			
				visitChildren(ctx);	
				return null; 
			}										
//
//			@Override public String visitXml_type_definition(TSQLParser.Xml_type_definitionContext ctx) { 
//				captureXMLFeature("", "XML TYPE DEFINITION", "", ctx.start.getLine());										
//				visitChildren(ctx);				
//				return null; 
//			}			
			@Override public String visitCreate_xml_schema_collection(TSQLParser.Create_xml_schema_collectionContext ctx) { 
				captureXMLFeature("CREATE ", "XML SCHEMA COLLECTION", "", ctx.start.getLine());										
				visitChildren(ctx);				
				return null; 
			}			
			@Override public String visitAlter_xml_schema_collection(TSQLParser.Alter_xml_schema_collectionContext ctx) { 
				captureXMLFeature("ALTER ", "XML SCHEMA COLLECTION", "", ctx.start.getLine());										
				visitChildren(ctx);				
				return null; 
			}			
			@Override public String visitDrop_xml_schema_collection(TSQLParser.Drop_xml_schema_collectionContext ctx) { 
				captureXMLFeature("DROP ", "XML SCHEMA COLLECTION", "", ctx.start.getLine());										
				visitChildren(ctx);				
				return null; 
			}		
			@Override public String visitCreate_selective_xml_index(TSQLParser.Create_selective_xml_indexContext ctx) { 
				captureXMLFeature("CREATE ", "XML INDEX, SELECTIVE", "", ctx.start.getLine());										
				visitChildren(ctx);				
				return null; 
			}		
			@Override public String visitCreate_xml_index(TSQLParser.Create_xml_indexContext ctx) { 
				captureXMLFeature("CREATE ", "XML INDEX", "", ctx.start.getLine());										
				visitChildren(ctx);				
				return null; 
			}			
						
			@Override public String visitDeclare_statement(TSQLParser.Declare_statementContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());			
				if (ctx.LOCAL_ID() != null) {
					String varName = ctx.LOCAL_ID().getText();
					String status = featureSupportedInVersion(TableVariables); 
					captureItem("TABLE variable declaration", varName, TableVariablesType, "", status, ctx.start.getLine());	
				}			
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 				
			}
			
			@Override public String visitDeclare_local(TSQLParser.Declare_localContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());								
				String varName  = ctx.LOCAL_ID().getText();
				String dataType = u.normalizeName(ctx.data_type().getText().toUpperCase(), "datatype");	
				String varDft = ctx.expression() != null ? ctx.expression().getText() : "";		
				
				String varItem = dataType+" variable";    
				String statusDataType = u.Supported;
				String section = Datatypes;
				boolean captured = false;
				
				if (ctx.data_type().xml_type_definition() != null) {
					statusDataType = featureSupportedInVersion(XMLFeatures, "XML TYPE DEFINITION");
					dataType = CfgXMLSchema;					
					varItem = dataType+" variable";    
					captureItem(varItem, varName, XMLFeatures, "", statusDataType, ctx.start.getLine());	
					captured = true;	
				} 
				else {		
					String UDD = u.isUDD(dataType);		
					if (UDD.equals("TABLE")) {
						varItem = "TABLE variable declaration, for TABLE type";
						dataType = "TABLE";
						section = TableVariablesType;
					} 
					else if (!UDD.isEmpty()) {
						varItem = UDD+" (UDD "+dataType+") variable";  
						dataType = UDD;
					} 
					else {
						varItem = dataType+" variable";    
					}				
					if (!varDft.isEmpty()) {
						varItem += " (with default value)";			
					}						

					if (featureExists(Datatypes, getBaseDataType(dataType))) {
						statusDataType = featureSupportedInVersion(Datatypes, getBaseDataType(dataType));
					} 
					else {
						// datatype is not listed, means: supported 		
					}
					//u.appOutput("dataType=["+dataType+"] isNumeric=["+isNumeric(dataType)+"] isString=["+isString(dataType)+"] isDateTime=["+isDateTime(dataType)+"] ");
					addLocalVars(varName, dataType);
					
					if (!varDft.isEmpty()) {
						// check for numeric-as-date
						checkNumericDateVarAssign(varName, ctx.expression(), ctx.start.getLine());					
					}					
				}
				
				if (!captured)
				captureItem(varItem, varName, section, dataType, statusDataType, ctx.start.getLine());				
				
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}				
							
			@Override public String visitTable_source_item(TSQLParser.Table_source_itemContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				boolean setInTUDF = false;	
				String TUDFname = "";
				
				String nameRaw = "";
				String name = "";
				if (ctx.full_object_name() != null) {	
					// some TUDF calls are parsed as a table reference with table hints (without WITH keyword)
					nameRaw = ctx.full_object_name().getText();
					name = u.normalizeName(nameRaw);
					
					if (u.debugging) u.dbgOutput(u.thisProc()+"nameRaw=["+nameRaw+"] name=["+name+"] ctx=["+ctx.getText()+"]  ", u.debugPtree);
					if (ctx.getText().startsWith(name+"(")) {
						String TUDFtype = lookupTUDF(name);
						if (u.debugging) u.dbgOutput(u.thisProc()+"is this a TUDF? name=["+name+"] TUDFtype=["+TUDFtype+"] ", u.debugPtree);
						if (TUDFtype.isEmpty()) {
							// it's not a TUDF, so assume it is a table or view being selected from
							if (u.debugging) u.dbgOutput(u.thisProc()+"name=["+name+"] : no TUDF found, assume it is a table/view", u.debugPtree);
							TUDFname = "";
						}
					}
					
					// transition table reference for multi-trigger action case
					if (u.currentObjectType.equals("TRIGGER")) {
						if (name.equalsIgnoreCase("INSERTED") || name.equalsIgnoreCase("DELETED")) {
							if (u.currentObjectAttributes.contains(" " + TrigMultiDMLAttr + " ")) {
								String status = featureSupportedInVersion(TransitionTableMultiDMLTrig);
								captureItem(TransitionTableMultiDMLTrig, u.currentObjectName, TransitionTableMultiDMLTrig, name.toUpperCase(), status, ctx.start.getLine());							
							}
						}
					}
					
				}				
				if (TUDFname.isEmpty()) {
					if (ctx.function_call() != null) {						
						if (u.debugging) u.dbgOutput(u.thisProc()+"table_source_item is a function_call: ["+ctx.function_call().getText()+"] ", u.debugPtree);
						if (ctx.function_call().func_proc_name_server_database_schema() != null) {	
							TUDFname = u.normalizeName(ctx.function_call().func_proc_name_server_database_schema().getText().toUpperCase());
						}
					}
				}

				String stmt = "SELECT";					
				if (hasParent(ctx.parent, "update_statement")) stmt = "UPDATE";
				else if (hasParent(ctx.parent, "delete_statement")) stmt = "DELETE";
				else if (hasParent(ctx.parent, "merge_statement")) stmt = "MERGE";
				
				if (!TUDFname.isEmpty()) {
					boolean isXMLMethod = false;						
					// first check if this is an XML.nodes() call, it is sometimes parsed as a TUDF call
					if (TUDFname.contains(".NODES")) {						
						isXMLMethod = udfIsBifMethod(TUDFname, u.XMLmethods, u.TUDFNamesLikeXML, u.TUDFSymTab);
					}
					
					if (isXMLMethod) {															
						captureXMLFeature("XML.", u.getObjectNameFromID(TUDFname), "()", ctx.start.getLine());	
					} 
					else {
						// the TUDF call is captured in visitFunction_call()
						//captureItem("Function call, table", TUDFname, FunctionsReportGroup, "", u.Supported, ctx.start.getLine());		
						CaptureIdentifier(TUDFname, TUDFname, stmt, ctx.start.getLine(), "()");		
					}	
					// avoid also capturing this same call in the function_call rule deeper down
					inTUDF = true;					
					setInTUDF = true;						
				}				
				
				if (TUDFname.isEmpty()) {					
					if (!name.isEmpty()) {
						CaptureIdentifier(nameRaw, name, stmt, ctx.start.getLine());					
					}
				}

				if (ctx.LOCAL_ID() != null) {	
					String tvName = ctx.LOCAL_ID().getText();
					CaptureIdentifier(tvName, tvName, stmt, ctx.LOCAL_ID().getSymbol().getLine());					
				}
				
				if (ctx.PIVOT() != null) {	
					String status = featureSupportedInVersion(SelectPivot);
					captureItem(SelectPivot, "", SelectPivot, "", status, ctx.PIVOT().getSymbol().getLine());
				}				
				
				if (ctx.UNPIVOT() != null) {	
					String status = featureSupportedInVersion(SelectUnpivot);
					captureItem(SelectUnpivot, "", SelectUnpivot, "", status, ctx.UNPIVOT().getSymbol().getLine());
				}				
				
				if (ctx.JOIN() != null) {	   
					String type = "INNER JOIN";
					if (ctx.oj != null) type = ctx.oj.getText().toUpperCase() + " OUTER JOIN";
					else if (ctx.cj != null) type = "CROSS JOIN";
					captureItem(type, "", DMLReportGroup, type, u.Supported, ctx.JOIN().getSymbol().getLine());
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
					captureItem(ColonColonFunctionCall, funcName, ColonColonFunctionCall, "", status, ctx.colon_colon().start.getLine());
				}				
				
				visitChildren(ctx);	
				if (setInTUDF) inTUDF = false;
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}							
				
			@Override public String visitTable_sources(TSQLParser.Table_sourcesContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				for (TerminalNode t : ctx.COMMA()) {
					captureItem("INNER JOIN", "", DMLReportGroup, "INNER JOIN WITH COMMA", u.Supported, t.getSymbol().getLine());	
				}
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
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
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				// A complicating factor for table hints is that WITH keyword is mandatory these days in SQL Server, 
				// but customer applications running older or backward-compatible version may still use the old syntax without WITH 
				// therefore, extra tests are needed to determine if it is really a table hint

				// the column list for an INSERT is parsed as table hints, so skip that case
				if (parentRuleName(ctx.parent,2).equals("insert_statement")) {
					if (u.debugging) u.dbgOutput(u.thisProc()+"skipping INSERT collist - not a table hint", u.debugPtree);
				} 
				else {
					boolean isHint = true;
					String hint = ctx.getText().toUpperCase();		
					//column names in a table alias may be parsed as a table hint
					if (! u.getPatternGroup(hint, "^(NOEXPAND|INDEX|NOEXPANDINDEX|FORCESEEK|SERIALIZABLE|SNAPSHOT|SPATIAL_WINDOW_MAX_CELLS|NOWAIT|FORCESCAN|HOLDLOCK|NOLOCK|NOWAIT|PAGLOCK|READCOMMITTED,READCOMMITTEDLOCK|READPAST|READUNCOMMITTED|REPEATABLEREAD|ROWLOCK|TABLOCK|TABLOCKX|UPDLOCK|XLOCK)\\b", 1).isEmpty()) {
						String w = u.getPatternGroup(hint, "^([A-Z ]+)", 1);
						String w2 = hint.substring(w.length());
						if (hint.startsWith("INDEX") || hint.startsWith("NOEXPANDINDEX")) {
							hint = u.collapseWhitespace(u.applyPatternAll(hint, "INDEX\\(\\d+\\)", "INDEX(index id)"));
							hint = u.collapseWhitespace(u.applyPatternAll(hint, "INDEX\\(\\D["+u.identifierChars+"]*\\)", "INDEX(index name)"));
							hint = u.collapseWhitespace(u.applyPatternAll(hint, "INDEX\\=\\D["+u.identifierChars+"]*", "INDEX=index name"));
							w = hint;
							
							// use the following special values for testing against the .cfg file (no values listed there at this time)
							hint = u.applyPatternAll(hint, "INDEX\\(index id\\)", "INDEX_ID");
							hint = u.applyPatternAll(hint, "INDEX\\(index name\\)", "INDEX_NAME");
							hint = u.applyPatternAll(hint, "INDEX=index name", "INDEX_NAME");
						}
						w = u.collapseWhitespace(u.applyPatternAll(w, "(FORCE|DISABLE|HINT|PLAN|GROUP|UNION|JOIN|NOEXPAND|VIEWS|FOR|PARAMETERIZATION)", " $1 "));
						String status = featureSupportedInVersion(TableHint, hint);
					
						captureItem(TableHint+ " " + w, w2, TableHint, hint, status, ctx.start.getLine());	
					} 
					else {
						if (u.debugging) u.dbgOutput(u.thisProc()+"not a table hint: ["+hint+"] ", u.debugPtree);
					}
				}
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 				
			}			
									
			@Override public String visitOption(TSQLParser.OptionContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String hint = ctx.getText().toUpperCase();	
				String w = u.getPatternGroup(hint, "^([A-Z ]+)", 1);
				if (w.startsWith("TABLEHINT")) {
					// already handled elsewhere
				} 
				else {
					String w2 = hint.substring(w.length());
					w = u.collapseWhitespace(u.applyPatternAll(w, "(FORCE|DISABLE|HINT|PLAN|GROUP|UNION|JOIN|EXPAND|FOR|PARAMETERIZATION)", " $1 "));
					String status = featureSupportedInVersion(QueryHint, hint);
					captureItem(QueryHint+ " " + w, w2, QueryHint, hint, status, ctx.start.getLine());	
				}
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 				
			}		
										
			@Override public String visitChar_string(TSQLParser.Char_stringContext ctx) { 
				if (ctx.getText().charAt(0) == '"') {
					captureDoubleQuotedString(ctx.getText(), ctx.start.getLine());
				}
				visitChildren(ctx);		
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
				captureItem(RollupCubeOldSyntax, s, RollupCubeOldSyntax, s, status, ctx.start.getLine());	
				visitChildren(ctx);		
				return null; 				
			}			
							
			@Override public String visitExecute_body(TSQLParser.Execute_bodyContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());		
				
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
					procName = ctx.func_proc_name_server_database_schema().getText().toUpperCase();
					if (!lookupSUDF(procName).isEmpty()) {
						String status = featureSupportedInVersion(ExecuteSQLFunction);
						captureItem(ExecuteSQLFunction, procName, ExecuteSQLFunction, "", status, ctx.start.getLine());
					}
				}
				
				if (u.debugging) u.dbgOutput(u.thisProc()+"EXECUTE: "+ ctx.getText()+", procName=["+procName+"] return_status=["+return_status+"] proc_var=["+proc_var+"] execImm=["+execImm+"] ", u.debugPtree);
				
				String section = ProceduresReportGroup;
				String sysProcName = "";			
				if (procName.equalsIgnoreCase("SP_EXECUTESQL"))  {
					// todo: also report OUTPUT parameters for sp_executesql?
					sysProcName = " SP_EXECUTESQL";
					section = DynamicSQL;
					captureItem(sysProcName+": dynamic SQL statements must be analyzed manually", "", DynamicSQL, "", u.ReviewManually, ctx.start.getLine());	
					captureItem("EXECUTE"+sysProcName+return_status, "", DynamicSQL, "", u.Supported, ctx.start.getLine());	
				} 
				else {				
					if ((procName.toUpperCase().contains("SP_")) || (procName.toUpperCase().contains("XP_")))  {
						// is this a system sproc?
						sysProcName = u.getObjectNameFromID(procName);
						if (u.debugging) u.dbgOutput(u.thisProc()+"sysProcName=["+sysProcName+"]  ", u.debugPtree);
						if (featureExists(SystemStoredProcs, sysProcName)) {
							if (u.debugging) u.dbgOutput(u.thisProc()+"featureExists: "+ ctx.getText()+" sysProcName=["+sysProcName+"]  ", u.debugPtree);
							section = SystemStoredProcs;
							String procStatus = featureSupportedInVersion(SystemStoredProcs, sysProcName);
							if (u.debugging) u.dbgOutput(u.thisProc()+"procStatus=["+procStatus+"]  sysProcName=["+sysProcName+"] ", u.debugPtree);
							
							captureItem("EXECUTE procedure "+sysProcName+return_status, procName, SystemStoredProcs, sysProcName, procStatus, ctx.start.getLine());								
						} 
						else {
							//not found as a system proc, so it's a user-defined proc
							sysProcName = "";
						}
					}
				}
				
				if ((!procName.isEmpty()) && (sysProcName.isEmpty())) {
					// proc versioning?
					if (ctx.proc_version != null) {
						String procStatus = featureSupportedInVersion(ProcVersionExecute);
						captureItem("EXECUTE proc;version"+return_status, procName+";"+ctx.proc_version.getText(), ProcVersionExecute, "", procStatus, ctx.start.getLine());			
					} 
					else {
						captureItem("EXECUTE procedure"+return_status, procName, section, "", u.Supported, ctx.start.getLine());	
					}
					
					CaptureIdentifier(procName, procName, "EXECUTE procedure", ctx.start.getLine());					
				}

				if (!proc_var.isEmpty()) {
					String procStatus = featureSupportedInVersion(ProcExecAsVariable);
					captureItem("EXECUTE procedure"+proc_var+return_status, ctx.proc_var.getText(), ProcExecAsVariable, "", procStatus, ctx.start.getLine());	
				}
								
				if (!execImm.isEmpty()) {
					captureItem("EXECUTE(string)"+return_status, "", DynamicSQL, "", u.Supported, ctx.start.getLine());	
					captureItem("EXECUTE(string): dynamic SQL statements must be analyzed manually", "", DynamicSQL, "", u.ReviewManually, ctx.start.getLine());	
				}
				
				// process any options
				List<TSQLParser.Execute_optionContext> execOptions = ctx.execute_option();				
				for (int i = 0; i <execOptions.size(); i++) {
					String optionRaw = execOptions.get(i).getText().toUpperCase();
					String option = optionRaw;
					if (optionRaw.startsWith("RESULTSETS")) {
						if (optionRaw.startsWith("RESULTSETSNONE")) option = "RESULT SETS NONE";
						else if (optionRaw.startsWith("RESULTSETSUNDEFINED")) option = "RESULT SETS UNDEFINED";
						else option = "RESULT SETS";
					}					
					if (u.debugging) u.dbgOutput(u.thisProc()+"execOptions i=["+i+"] option=["+option+"]  optionRaw=["+optionRaw+"]", u.debugPtree);
					String status = featureSupportedInVersion(ExecProcedureOptions,option);
					if (option.equals("RECOMPILE") && (!status.equals(u.Supported))) status = u.ReviewPerformance;
					captureItem("EXECUTE procedure, WITH "+option, procName, ExecProcedureOptions, option, status, ctx.start.getLine());			
				}		
						
				List<TSQLParser.Execute_var_string_optionContext> execVarOptions = ctx.execute_var_string_option();
				for (int i = 0; i <execVarOptions.size(); i++) {
					String optionRaw = execVarOptions.get(i).getText().toUpperCase();
					String option = optionRaw;
					String optionValue = "";
					String optionFmt = "";
					if (optionRaw.startsWith("AT")) {
						if (optionRaw.startsWith("ATDATA_SOURCE")) {
							option = "AT DATA_SOURCE";
							optionValue = optionRaw.substring("ATDATA_SOURCE".length());
							optionFmt = " <data-source>";
						} 
						else {
							option = "AT";
							optionValue = optionRaw.substring("AT".length());
							optionFmt = " <linked-server>";
						}
					}
					else if (optionRaw.startsWith("AS")) {
						if (optionRaw.startsWith("ASLOGIN")) {
							option = "AS LOGIN";
							optionValue = optionRaw.substring("ASLOGIN=".length());
						} 
						else {
							option = "AS USER";
							optionValue = optionRaw.substring("ASUSER=".length());
						}
					}
					if (u.debugging) u.dbgOutput(u.thisProc()+"execVarOptions i=["+i+"] option=["+option+"]  optionRaw=["+optionRaw+"]", u.debugPtree);
					String status = featureSupportedInVersion(ExecStringOptions,option);
					captureItem("EXECUTE(string), "+option+optionFmt, optionValue, DynamicSQL, option, status, ctx.start.getLine());			
				}				
								
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
				return null; 
			}				
			
			@Override public String visitSet_special(TSQLParser.Set_specialContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				
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
					captureSEToption("SET IDENTITY_INSERT", ctx.on_off().getText().toUpperCase(), "", ctx.start.getLine(), ctx.table_name().getText().toUpperCase());									
				} 
				else if (ctx.ROWCOUNT() != null) { 			
					String setValue = "";
					String setValueFmt = ""; 
					String setValueOrig = "";
					if (ctx.DECIMAL() != null) {
						setValueOrig = ctx.DECIMAL().getText();
						setValueFmt = setValueOrig;	
						if (!u.getPatternGroup(setValueOrig, "^[\\+\\-]*([0]+)$", 1).isEmpty()) {
							setValue = "0";							
							setValueFmt = "0";							
						} 
						else {
							setValue = CfgNonZero;
							setValueFmt = "<number>";
						}
					} 
					else {
						setValueOrig = ctx.LOCAL_ID().getText();
						setValueFmt = "@v";
						setValue = CfgVariable;
					}		
					captureSEToption("SET ROWCOUNT", setValue, setValueFmt, ctx.start.getLine(), setValueOrig);											
				} 
				else if (ctx.TEXTSIZE() != null) {	
					String setValueFmt = "<number>";
					String setValueOrig = ctx.DECIMAL().getText();
					captureSEToption("SET TEXTSIZE", setValueOrig, setValueFmt, ctx.start.getLine(), setValueOrig);					
				} 
				else if (ctx.STATISTICS() != null) {	
					List<TSQLParser.Set_statistics_keywordContext> options = ctx.set_statistics_keyword();	
					String setValue = ctx.on_off().getText().toUpperCase();
					if (options.size() > 0) { 	
						for (TSQLParser.Set_statistics_keywordContext optionX : options) {
							captureSEToption("SET STATISTICS", optionX.getText().toUpperCase() + " " + setValue, "", ctx.start.getLine());						
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
						setValue = CfgVariable;						
					}
					else {
						Integer exprInt = getNumericConstant(setValue);
						if (exprInt != null) {
							if ((exprInt == -1) || (exprInt == 0)) {
								setValueFmt = setValue;
							}				
							else {
								setValueFmt = "<number>";	
								setValue = CfgNonZero;						
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
					String feature = "SET TRANSACTION ISOLATION LEVEL";
					String setValue = ctx.getText().toUpperCase();
					setValue = setValue.substring(setValue.indexOf("LEVEL")+5);
					setValue = setValue.replaceFirst("READ", " READ ").trim();
					captureSEToption(feature, setValue, "", ctx.start.getLine());	
				}
			
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());
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
				if (!u.getPatternGroup(setValueTest, "^("+u.varPattern+")$", 1).isEmpty()) {
					setValueTest = CfgVariable;
				}
				if (u.debugging) u.dbgOutput(u.thisProc()+"feature=["+feature+"] setValueTest=["+setValueTest+"] ", u.debugPtree);
				String status = featureSupportedInVersion(feature, setValueTest);	
				if (setValueTest.equals(CfgVariable)) {
					if (status.equals(u.NotSupported)) {
						status = u.ReviewManually;
					}
				}
				captureItem(formatItemDisplay(feature+" "+setValueFmt), itemDetail, SetOptions, feature, status, lineNr);		
			}								
			
			@Override public String visitSet_statement(TSQLParser.Set_statementContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());					
				if (ctx.LOCAL_ID() != null) {
					String varName = ctx.LOCAL_ID().getText().toUpperCase();
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
							//u.appOutput(u.thisProc()+"SET assigning @@ERROR to varName=["+varName+"] x=["+ctx.getText()+"] ");						
						}
					}
				}					
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 
			}				
							
			@Override public String visitSelect_list(TSQLParser.Select_listContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	

				boolean hasTable = false;
				
				ParserRuleContext parentRule = ctx.getParent();
				if (parentRule instanceof TSQLParser.Query_specificationContext) {
    				TSQLParser.Query_specificationContext parentCtx = (TSQLParser.Query_specificationContext)parentRule;
    				if (parentCtx.FROM() != null) {
    					hasTable = true;
    				}
				} 

				variableAssignDepends.clear();			
				visitChildren(ctx);		
						
				if (hasTable) captureVariabeAssignDepends("SELECT", ctx.start.getLine());		
				
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 
			}							
							
			@Override public String visitSelect_list_elem(TSQLParser.Select_list_elemContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());					
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
						//u.appOutput(u.thisProc()+"SELECT assigning @@ERROR to varName=["+varName+"] x=["+ctx.getText()+"] ");						
					}
					
					if (ctx.LOCAL_ID() != null) {
						addVariableAssignDepends(ctx.LOCAL_ID(), ctx.expression());										
					}					
					
				}					
				visitChildren(ctx);				
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 
			}							
							
			@Override public String visitUnary_op_expr(TSQLParser.Unary_op_exprContext ctx) { 
				// only report BIT-NOT here
				if (ctx.BIT_NOT() != null) captureItem("Bitwise operator ~ (NOT)", "", "Operators", "", u.Supported, ctx.start.getLine());							
				visitChildren(ctx);				
				return null; 
			}	
				
			@Override public String visitMult_div_percent_expr(TSQLParser.Mult_div_percent_exprContext ctx) { 
				if (ctx.STAR() != null) captureItem("Arithmetic operator *", "", "Operators", "", u.Supported, ctx.start.getLine());							
				else if (ctx.DIVIDE() != null) captureItem("Arithmetic operator /", "", "Operators", "", u.Supported, ctx.start.getLine());							
				else if (ctx.PERCENT_SIGN() != null) captureItem("Arithmetic operator % (modulo)", "", "Operators", "", u.Supported, ctx.start.getLine());	
				visitChildren(ctx);				
				return null;
			}
	
			@Override public String visitPlus_minus_bit_expr(TSQLParser.Plus_minus_bit_exprContext ctx) { 
				if (ctx.BIT_AND() != null) captureItem("Bitwise operator & (AND)", "", "Operators", "", u.Supported, ctx.start.getLine());							
				else if (ctx.BIT_OR() != null) captureItem("Bitwise operator | (OR)", "", "Operators", "", u.Supported, ctx.start.getLine());							
				else if (ctx.BIT_XOR() != null) captureItem("Bitwise operator ^ (XOR)", "", "Operators", "", u.Supported, ctx.start.getLine());	
				else {
					// + can be for numeric, string or datetime
					// - can be for numeric or datetime
					String opFmt = "";
					String op = ctx.op.getText();
					List<TSQLParser.ExpressionContext> expr = ctx.expression();
					String lhsType = expressionDataType(expr.get(0));
					String rhsType = expressionDataType(expr.get(1));
					
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
					} 
					else {
						// op = '-'
						opFmt = "Arithmetic operator -";
						if (isDateTime(lhsType) || isDateTime(rhsType)) {
							opFmt = "Date/time arithmetic operator -";
						}						
					}
					captureItem(opFmt, "", "Operators", "", u.Supported, ctx.start.getLine());	
				}						
				visitChildren(ctx);				
				return null; 
			}		
			
			@Override public String visitOdbc_scalar_function(TSQLParser.Odbc_scalar_functionContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String funcName = ctx.getText().toUpperCase();
				funcName = funcName.substring(0,funcName.indexOf("("));
				String status = featureSupportedInVersion(ODBCScalarFunction,funcName);
				captureItem(ODBCScalarFunction+" { fn "+funcName+"() }", ctx.getText(), ODBCScalarFunction, funcName, status, ctx.start.getLine());	
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 				
			}					

			@Override public String visitOdbc_literal(TSQLParser.Odbc_literalContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String funcName = "";
				if (ctx.op != null) funcName = ctx.op.getText().toUpperCase();
				else funcName = "INTERVAL";
				String status = featureSupportedInVersion(ODBCLiterals,funcName);
				captureItem(ODBCLiterals+" { fn "+funcName+" <string> }", ctx.getText(), ODBCLiterals, funcName, status, ctx.start.getLine());	
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
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
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
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
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 				
			}					

			@Override public String visitCollation(TSQLParser.CollationContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
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
				captureItem(CSmsg+collationName+", "+context, columnName, Collations, "", status, ctx.COLLATE().getSymbol().getLine());																
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 					
			}
				
			@Override public String visitCreate_database(TSQLParser.Create_databaseContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String dbName = u.normalizeName(ctx.database.getText());
				
				List<TSQLParser.Create_database_optionContext> options = ctx.create_database_option();
				for (int i=0; i<options.size(); i++) {	
					String option = options.get(i).getText().toUpperCase();
					if (option.equals("CATALOG_COLLATION=DATABASE_DEFAULT")) continue;
					
					String status = u.NotSupported;
					boolean captured = false;
					if (option.startsWith("CATALOG_COLLATION=")) {
						String catalogCollation = getOptionValue(option);
						u.appOutput(u.thisProc()+"catalogCollation=["+catalogCollation+"] ");
						if (catalogCollation.toUpperCase().contains("_CS_")) {
							status = featureSupportedInVersion(CaseSensitiveCollation,"CATALOG_COLLATION");
							if (!status.equals(u.Supported)) {
								captureItem(CaseSensitiveCollation + " "+catalogCollation+", in CREATE DATABASE..CATALOG_COLLATION", "", Collations, "", status, options.get(i).start.getLine());		
								captured = true;									
							}							
						}
					}		
					if (status.equals(u.Supported)) {
						status = featureSupportedInVersion(CreateDatabaseOptions,option);
					}
					if (!captured) {				
						captureItem("option "+option+", in CREATE DATABASE", ctx.getText(), CreateDatabaseOptions, option, status, options.get(i).start.getLine());								
					}
				}	
				
				if (ctx.CONTAINMENT() != null) {
					String option = "CONTAINMENT";
					String optionValue = ctx.containment.getText().toUpperCase();
					String status = featureSupportedInVersion(u.Ignored, CreateDatabaseOptions, option, optionValue);
					captureItem("Option "+formatOptionDisplay(option,optionValue)+	", in CREATE DATABASE", option, CreateDatabaseOptions, option, status, ctx.start.getLine());													
				}
				captureItem("CREATE DATABASE "+dbName, dbName, DatabasesReportGroup, "", u.Supported, ctx.start.getLine(), "0");																
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 					
			}
				
			@Override public String visitAlter_database(TSQLParser.Alter_databaseContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String dbName = "";
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
					String status = featureSupportedInVersion("", AlterDatabaseOptions, option, optionValue);
					captureItem("Option "+formatOptionDisplay(option,optionValue)+", in ALTER DATABASE", option, AlterDatabaseOptions, option, status, ctx.start.getLine());
				}	
				
				if (ctx.MODIFY() != null) {
					String option = "MODIFY NAME";
					String status = featureSupportedInVersion(u.Ignored, AlterDatabaseOptions, option, "");
					captureItem("Option "+option+", in ALTER DATABASE", option, AlterDatabaseOptions, option, status, ctx.start.getLine());													
				}
				
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 				
			}				
			
			@Override public String visitId(TSQLParser.IdContext ctx) { 
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());
				String id = u.normalizeName(ctx.getText().toUpperCase());
				// for cross-DB: table-source_item
				
				if (featureExists(SpecialColumNames,id)) {
					String status = featureSupportedInVersion(SpecialColumNames,id);
					captureItem("Special column name "+id, id, SpecialColumNames, id, status, ctx.start.getLine());	
				}	
							
				int maxIdLen = featureIntValueSupportedInVersion(MaxIdentifierLength);				
				if (id.length() > maxIdLen) { 					
					captureItem("Identifier > 63 characters", id, MaxIdentifierLength, ""+id.length()+"", u.Supported, ctx.start.getLine());	
				}	
				
				// test for special chars in identifiers not currently supported
				List<String> specialChars = featureValueList(SpecialCharsIdentifier);
				for (int i=0; i<specialChars.size(); i++) {	
					String c = specialChars.get(i);
					CaptureSpecialCharIdentifier(id, c, ctx.start.getLine());
				}				
				
				visitChildren(ctx);	
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 				
			}					
			
			public void CaptureSpecialCharIdentifier (String id, String SpecialChar, int lineNr) {
				if (SpecialChar.isEmpty()) return;
				if (id.startsWith("@")) id = id.substring(1);
				if (id.startsWith("#")) id = id.substring(1);
				if (id.startsWith("#")) id = id.substring(1); // for global temp tabs
				if (id.contains(SpecialChar)) {
					if (featureExists(SpecialCharsIdentifier,u.decodeIdentifier(SpecialChar))) {
						String status = featureSupportedInVersion(SpecialCharsIdentifier,u.decodeIdentifier(SpecialChar));
						captureItem("Special character in identifier: '"+u.decodeIdentifier(SpecialChar)+"'", id, SpecialCharsIdentifier, u.decodeIdentifier(SpecialChar), status, lineNr);	
					}	
				}								
			}
			
			@Override public String visitTransaction_statement(TSQLParser.Transaction_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				boolean captured = false;
				String stmt = "";
				if (ctx.BEGIN() != null) {
					stmt = "BEGIN TRANSACTION";
				} 
				else if (ctx.COMMIT() != null) {
					stmt = "ROLLBACK TRANSACTION";
				} 
				else if (ctx.ROLLBACK() != null) {
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
				else if (ctx.LOCAL_ID() != null) {
					xactNameFmt = ", with xact name in variable";
					xactName = ctx.LOCAL_ID().getText();
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
					String status = featureSupportedInVersion("", Transactions, option, optionValue);
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
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}
			
			@Override public String visitExecute_as_statement(TSQLParser.Execute_as_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				
				// ToDo: handle WITH (NO REVERT | COOKIE)
				String stmt = "";
				if (ctx.CALLER() != null) stmt = "CALLER";
				else if (ctx.LOGIN() != null) stmt = "LOGIN";
				else if (ctx.USER() != null) stmt = "USER";
				stmt = "EXECUTE AS " + stmt;

				String status = featureSupportedInVersion(ExecuteAsRevert,stmt);
				captureItem(stmt + " (statement)", stmt, ExecuteAsRevert, stmt, status, ctx.start.getLine());	

				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}				
			
			@Override public String visitRevert_statement(TSQLParser.Revert_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	

				// ToDo: handle COOKIE
				String stmt = "REVERT";
				String status = featureSupportedInVersion(ExecuteAsRevert,stmt);
				captureItem(stmt, stmt, ExecuteAsRevert, stmt, status, ctx.start.getLine());	

				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}				
			
			@Override public String visitInsert_statement(TSQLParser.Insert_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String tableName = "";
				String type = "VALUES()";
				if (ctx.ddl_object() != null) {
					String tableNameRaw = ctx.ddl_object().getText();
					tableName = u.normalizeName(tableNameRaw);	
					CaptureIdentifier(tableNameRaw, tableName, "INSERT", ctx.start.getLine());
				}
				else {
					tableName = ctx.rowset_function().getText().toUpperCase();					
					type = tableName.substring(0,tableName.indexOf("("));
				}
				String itemDetail = tableName;
				
				if (u.debugging) u.dbgOutput(u.thisProc()+"tableName=["+tableName+"] type=["+type+"] ", u.debugPtree);
				if (ctx.insert_statement_value().derived_table() != null) {
					type = "SELECT";
					if (ctx.insert_statement_value().derived_table().table_value_constructor() != null) type = "VALUES";
				}
				else if (ctx.insert_statement_value().execute_statement() != null) {		
					if (ctx.insert_statement_value().execute_statement().execute_body().execute_var_string().size() > 0) type = "EXECUTE(string)";
					else {
						type = "EXECUTE procedure";
						if (ctx.insert_statement_value().execute_statement().execute_body().func_proc_name_server_database_schema() != null) {
							itemDetail = ctx.insert_statement_value().execute_statement().execute_body().func_proc_name_server_database_schema().getText();
							if (u.getObjectNameFromID(itemDetail).equalsIgnoreCase("SP_EXECUTESQL")) {
								type = "EXECUTE sp_executesql";
							}
						}
						else {
							itemDetail = ctx.insert_statement_value().execute_statement().execute_body().proc_var.getText();
						}
					}		
				}
				else if (ctx.insert_statement_value().DEFAULT() != null) type = "DEFAULT VALUES";			
				
				String status = u.Supported;
				String top = "";
				if (ctx.TOP() != null) {
					top = " TOP";
					status = featureSupportedInVersion(InsertStmt,"TOP");				
				}
				if (status.equals(u.Supported)) {
					status = featureSupportedInVersion(InsertStmt,type);
				}
				
				String outputClause = getOutputClause(ctx.output_clause(), status, InsertStmt, "text");
				status = getOutputClause(ctx.output_clause(), status, InsertStmt, "status");				
				
				String CTE = "";
				if (ctx.with_expression() != null) {
					// ToDo: determine whether CTE is recursive
					String recursive = ""; // ", recursive"
					CTE = ", WITH (Common Table Expression"+recursive+")";
					if (status.equals(u.Supported)) {
						status = featureSupportedInVersion(InsertStmt,"CTE");
					}				
				}								
				
				captureItem("INSERT"+top+".."+type+CTE+outputClause, itemDetail, InsertStmt, "", status, ctx.start.getLine());	
				
				CaptureXMLNameSpaces(ctx.parent, "INSERT", ctx.start.getLine());
				
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}				
			
							
			public String getOutputClause(TSQLParser.Output_clauseContext opClause, String status, String section, String callType) {		
				assert (callType.equals("text") || callType.equals("status")) : u.thisProc()+"invalid callType=["+callType+"] ";				
				if (opClause == null) {
					if (callType.equals("text")) return "";
					else return status;
				}
				
				if (callType.equals("status")) {
					if (status.equals(u.Supported)) {
						String item = "OUTPUT";
						if (opClause.output_clause() != null) item = "OUTPUT OUTPUT";
						status = featureSupportedInVersion(section,item);					
					}				
					return status;
				}
					
				List<String> tmp = new ArrayList<>();
				String result = "to client";
				if (opClause.INTO() != null) {
					if (opClause.LOCAL_ID() != null) {
						result = "INTO @tableVariable";	
					}
					else {							
						String tableName = u.normalizeName(opClause.table_name().getText());	
						String tmpTabType = getTmpTableType(tableName, true);
						result = "INTO "+tmpTabType;	
					}
				}
				tmp.add(", OUTPUT " + result);				
				if (opClause.output_clause() != null) {
					tmp.add(getOutputClause(opClause.output_clause(), "", "", "text"));						
				}
				result = String.join("", tmp.stream().sorted().collect(Collectors.toList()));						
				return result;
			}
			
			public void CaptureIdentifier(String objNameRaw, String objName, String stmt, int lineNr) {
				CaptureIdentifier(objNameRaw, objName, stmt, lineNr, "");
			}
			public void CaptureIdentifier(String objNameRaw, String objName, String stmt, int lineNr, String fmt) {
				if (u.debugging) u.dbgOutput(u.thisProc()+"objNameRaw=["+objNameRaw+"] objName=["+objName+"] fmt=["+fmt+"] ", u.debugPtree);
				if (objNameRaw.charAt(0) == '.') {
					String status = featureSupportedInVersion(LeadingDotsId); 
					captureItem(LeadingDotsId, objNameRaw, LeadingDotsId, stmt, status, lineNr);	
				}

				if (objName.charAt(0) == '@') {
					String stmtFmt = stmt;
					if (stmt.equals("SELECT")) stmtFmt = "SELECT FROM";
					String status = featureSupportedInVersion(TableVariables); 
					captureItem(stmtFmt + " @tableVariable", objName, TableVariables, stmt, status, lineNr);	
				}
				
				if (objName.contains(".")) {
					List<String> parts = new ArrayList<String>(Arrays.asList(objName.split("\\.")));
					
					if (parts.size() == 3) {
						String dbName = u.getDBNameFromID(objName);
						String status = featureSupportedInVersion(CrossDbReference);
						captureItem(CrossDbReference+" by "+stmt, objName, CrossDbReference, stmt, status, lineNr);	
					}
					
					if (parts.size() == 4) {
						String serverName = u.getServerNameFromID(objName);
						String status = featureSupportedInVersion(RemoteObjectReference);
						captureItem(RemoteObjectReference+" by "+stmt, objName+fmt, RemoteObjectReference, stmt, status, lineNr);	
					}					
				}
				
				String name = u.getObjectNameFromID(objName).toUpperCase();
				String schema = u.getSchemaNameFromID(objName).toUpperCase();
				String catName = "";
				String reportGroup = Catalogs;
				if (name.startsWith("SYS")) {
					if (schema.isEmpty() || schema.equals("DBO") || schema.equals("SYS")) {
						if (featureExists(Catalogs, name)) {
							catName = name;
						}
					}
				}
				else if (schema.equals("SYS") && (!name.startsWith("SYS"))) {
					if (featureExists(Catalogs, name)) {
						catName = "SYS." + name;
					}					
				}
				else if (schema.equals("INFORMATION_SCHEMA")) {
					if (featureExists(InformationSchema, name)) {
						catName = "INFORMATION_SCHEMA." + name;
						reportGroup = InformationSchema;
					}					
				}
				if (!catName.isEmpty()) {
					String status = featureSupportedInVersion(Catalogs,name);
					captureItem("Catalog reference "+catName, "", reportGroup, "", status, lineNr);	
				}
			}

			@Override public String visitBulk_insert_statement(TSQLParser.Bulk_insert_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String tableName = u.normalizeName(ctx.ddl_object().getText().toUpperCase());	
				String status = featureSupportedInVersion(BulkInsertStmt);
				captureItem("BULK INSERT", tableName, BulkInsertStmt, "BULK INSERT", status, ctx.start.getLine());	
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}				
						
			@Override public String visitInsert_bulk_statement(TSQLParser.Insert_bulk_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String tableName = u.normalizeName(ctx.table_name().getText().toUpperCase());	
				String status = featureSupportedInVersion(InsertBulkStmt);
				captureItem("INSERT BULK (with bulk API only)", tableName, InsertBulkStmt, "INSERT BULK", status, ctx.start.getLine());	
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}				
						
			@Override public String visitUpdate_statement(TSQLParser.Update_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String status = u.Supported;
				String tableName = "";
				if (ctx.ddl_object() != null) {
					String tableNameRaw = ctx.ddl_object().getText().toUpperCase();
					tableName = u.normalizeName(tableNameRaw);	
					CaptureIdentifier(tableNameRaw, tableName, "UPDATE", ctx.start.getLine());
				}
				else {
					tableName = ctx.rowset_function().getText().toUpperCase();					
					tableName = tableName.substring(0,tableName.indexOf("("));
					status = featureSupportedInVersion(UpdateStmt,tableName);
				}

				String top = "";
				if (ctx.TOP() != null) {
					top = " TOP";
					status = featureSupportedInVersion(UpdateStmt,"TOP");				
				}
				
				String outputClause = getOutputClause(ctx.output_clause(), status, UpdateStmt, "text");
				status = getOutputClause(ctx.output_clause(), status, UpdateStmt, "status");
				
				String CTE = "";
				if (ctx.with_expression() != null) {
					// ToDo: determine whether CTE is recursive
					String recursive = ""; // ", recursive"
					CTE = ", WITH (Common Table Expression"+recursive+")";
					if (status.equals(u.Supported)) {
						status = featureSupportedInVersion(UpdateStmt,"CTE");
					}				
				}
				
				String whereCurrentOf = "";
				if (ctx.CURRENT() != null) {
					whereCurrentOf = ", WHERE CURRENT OF";
					if (status.equals(u.Supported)) {
						status = featureSupportedInVersion(UpdateStmt,whereCurrentOf);
					}				
				}				
				
				CaptureXMLNameSpaces(ctx.parent, "UPDATE", ctx.start.getLine());
				
				if (ctx.FROM() != null) {		
					captureUpdDelFromBug("UPDATE", tableName, ctx.table_sources(), ctx.table_sources().table_source_item(), ctx.start.getLine());
				}
				
				variableAssignDepends.clear();
				updVarAssign = "";
				
				visitChildren(ctx);		
				
				captureItem("UPDATE"+top+updVarAssign+CTE+outputClause+whereCurrentOf, tableName, UpdateStmt, "UPDATE", status, ctx.start.getLine());	
				captureVariabeAssignDepends("UPDATE", ctx.start.getLine());
				
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}					
			
			@Override public String visitUpdate_elem(TSQLParser.Update_elemContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				if (ctx.LOCAL_ID() != null) {
					if (updVarAssign.isEmpty()) updVarAssign = " SET @v = expression";
					if (ctx.EQUAL().size() > 1) updVarAssign = " SET @v = column = expression";
					addVariableAssignDepends(ctx.LOCAL_ID(), ctx.expression());							
				}				
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 		
			}				
			
			private void addVariableAssignDepends(TerminalNode id, TSQLParser.ExpressionContext expr) {	
				// ToDo: need to record the assignment operator, since += indicates a string concat
				if (expr instanceof TSQLParser.Constant_exprContext) {
					// this assignment can be ignored for determining variable assignment dependencies
					return;
				}
				else if (expr instanceof TSQLParser.Unary_op_exprContext) {
					TSQLParser.Unary_op_exprContext x = (TSQLParser.Unary_op_exprContext) expr;
					if (x.expression() instanceof TSQLParser.Constant_exprContext) {
					// this assignment can be ignored for determining variable assignment dependencies
						return;						
					}
				}
				variableAssignDepends.put(id.getText().toUpperCase(), expr);	
			}
								
			private void captureVariabeAssignDepends(String stmt, int lineNr) {
				// ToDo: rewrite logic to walk the tree to find occurrence of variable; due to whitespace being removed a variable name may concat with a keyword
				if (variableAssignDepends.size() == 0) return;

				Map<String, String> tmp = new HashMap<String, String>();
				String allValues = "";
				for (Map.Entry<String, TSQLParser.ExpressionContext> entry : variableAssignDepends.entrySet()) {
					//u.appOutput(u.thisProc()+stmt+ "("+variableAssignDepends.size()+") :"+ entry.getKey()+" = "+entry.getValue());
					String expr = entry.getValue().getText().toUpperCase();
					if (expr.contains(entry.getKey())) {
						// some variable names can perhaps be constructed that are not detected here, but that would be pretty exotic
						String varRegex = "([^@\\$\\w])" + entry.getKey() + "([^@\\$\\w])";
						String v = u.applyPatternAll(expr, varRegex, "$1 $2");  
						if (v.contains("@")) {
							tmp.put(entry.getKey(), v);
							allValues += " " + v;
						}
						else {
							//u.appOutput(u.thisProc()+"no @var left in value");
						}
					}
					else {
						tmp.put(entry.getKey(), expr);
						allValues += " " + expr;
					}
				}
				allValues += " ";
				
				for (Map.Entry<String, String> entry : tmp.entrySet()) {
					String varRegex = "([^@\\$\\w]" + entry.getKey() + "[^@\\$\\w])";
					if (allValues.contains(entry.getKey())) { // quicker test but not less accurate
						if (!u.getPatternGroup(allValues, varRegex, 1).isEmpty()) { // slower test but accurate 
							// find variable on lhs for this case
							String v = "";
							for (Map.Entry<String, String> e2 : tmp.entrySet()) {
								if (e2.getKey().equals(entry.getKey())) continue;
								String v2 = u.getPatternGroup(e2.getValue(), varRegex, 1);
								if (!u.getPatternGroup(e2.getValue(), varRegex, 1).isEmpty()) {
									v = e2.getKey();
									break;									
								}
							}
							captureItem("Variable assignment dependency in "+stmt+": order of assignments not guaranteed", v+"->"+entry.getKey(), "DML", "", u.ReviewSemantics, lineNr);
							break;
						}		
					}			
				}
			}
			
			@Override public String visitDelete_statement(TSQLParser.Delete_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String status = u.Supported;
				String tableName = "";
				if (ctx.delete_statement_from().rowset_function() != null) {
					tableName = ctx.delete_statement_from().rowset_function().getText().toUpperCase();					
					tableName = tableName.substring(0,tableName.indexOf("("));
					status = featureSupportedInVersion(DeleteStmt,tableName);
				}
				else {
					String tableNameRaw = ctx.delete_statement_from().getText().toUpperCase();
					tableName = u.normalizeName(tableNameRaw);	
					CaptureIdentifier(tableNameRaw, tableName, "DELETE", ctx.start.getLine());
				}

				String top = "";
				if (ctx.TOP() != null) {
					top = " TOP";
					status = featureSupportedInVersion(DeleteStmt,"TOP");				
				}
				
				String outputClause = getOutputClause(ctx.output_clause(), status, DeleteStmt, "text");
				status = getOutputClause(ctx.output_clause(), status, DeleteStmt, "status");
				
				String CTE = "";
				if (ctx.with_expression() != null) {
					// ToDo: determine whether CTE is recursive
					String recursive = ""; // ", recursive"
					CTE = ", WITH (Common Table Expression"+recursive+")";
					if (status.equals(u.Supported)) {
						status = featureSupportedInVersion(DeleteStmt,"CTE");
					}				
				}
				
				String whereCurrentOf = "";
				if (ctx.CURRENT() != null) {
					whereCurrentOf = ", WHERE CURRENT OF";
					if (status.equals(u.Supported)) {
						status = featureSupportedInVersion(DeleteStmt,whereCurrentOf);
					}				
				}				
				captureItem("DELETE"+top+CTE+outputClause+whereCurrentOf, tableName, DeleteStmt, "DELETE", status, ctx.start.getLine());	

				CaptureXMLNameSpaces(ctx.parent, "DELETE", ctx.start.getLine());

				if (ctx.table_sources() != null) {		
					captureUpdDelFromBug("DELETE", tableName, ctx.table_sources(), ctx.table_sources().table_source_item(), ctx.start.getLine());
				}
				
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}	
		
			// detect specific bugs
			// happens only for FROM clause with a single table or comma-join syntax , not with ANSI join syntax			
			private void captureUpdDelFromBug(String stmt, String tableName, TSQLParser.Table_sourcesContext ts, List<TSQLParser.Table_source_itemContext> tabs, int lineNr) {	
				String section = UpdateStmt;
				if (stmt.equals("DELETE")) section = DeleteStmt;
				String comma = ts.COMMA().size()>0?"null":"COMMA";
				tableName = u.resolveName(tableName);
				if (u.debugging) u.dbgOutput(u.thisProc()+stmt+" ["+tableName+"] FROM, nr_tabs=["+tabs.size()+"] COMMA=["+comma+"],["+ts.COMMA().size()+"] ", u.debugPtree);
				if ((tabs.size() == 1) || (ts.COMMA().size() > 0)) {
					boolean is_comma_join = true;
					for (TSQLParser.Table_source_itemContext t : tabs) {
						if (t.full_object_name() == null) {
							is_comma_join = false;
							break;
						}					
					}
					if (u.debugging) u.dbgOutput(u.thisProc()+"is_comma_join=["+is_comma_join+"] ", u.debugPtree);
					if (is_comma_join) {							
						for (TSQLParser.Table_source_itemContext t : tabs) {
							String fromTableName = u.resolveName(t.full_object_name().getText());
							String corrName      = "";
							if (t.as_table_alias().size() > 0) corrName = u.normalizeName(t.as_table_alias().get(0).table_alias().getText());
							if (u.debugging) u.dbgOutput(u.thisProc()+"   fromTableName=["+fromTableName+"] corrName=["+corrName+"] ", u.debugPtree);
							if (tableName.equalsIgnoreCase(fromTableName)) {
								if (u.debugging) u.dbgOutput(u.thisProc()+stmt+" ["+tableName+"]  FROM with itself table in FROM clause ", u.debugPtree);
								
								String SelfInFromClause = "TARGET_IN_FROM_CLAUSE"; // matches .cfg file item
								String status = featureSupportedInVersion(section,SelfInFromClause);
								if (!status.equals(u.Supported)) {
									String join = "";
									if (tabs.size() > 1) join = " ("+Integer.toString(tabs.size())+"-way join)";
									captureItem(stmt+", target table in FROM"+join, tableName, section, SelfInFromClause, status, lineNr);	
								}
							}						
						}
					}
				}
			}
					
			@Override public String visitMerge_statement(TSQLParser.Merge_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String status = u.Supported;
				status = featureSupportedInVersion(MergeStmt, "MERGE");  // the MERGE stmt is not supported in v.1.0
				
				String tableName = "";
				if (ctx.ddl_object() != null) {
					String tableNameRaw = ctx.ddl_object().getText().toUpperCase();
					tableName = u.normalizeName(tableNameRaw);	
					CaptureIdentifier(tableNameRaw, tableName, "MERGE", ctx.start.getLine());
				}

				String top = "";
				if (ctx.TOP() != null) {
					top = " TOP";
					status = featureSupportedInVersion(MergeStmt,"TOP");				
				}
				
				String outputClause = getOutputClause(ctx.output_clause(), status, MergeStmt, "text");
				status = getOutputClause(ctx.output_clause(), status, MergeStmt, "status");
				
				String CTE = "";
				if (ctx.with_expression() != null) {
					// ToDo: determine whether CTE is recursive
					String recursive = ""; // ", recursive"
					CTE = ", WITH (Common Table Expression"+recursive+")";
					if (status.equals(u.Supported)) {
						status = featureSupportedInVersion(MergeStmt,"CTE");
					}				
				}
				
				captureItem("MERGE"+top+CTE+outputClause, tableName, MergeStmt, "MERGE", status, ctx.start.getLine());	

				CaptureXMLNameSpaces(ctx.parent, "MERGE", ctx.start.getLine());

				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}				
			
			@Override public String visitTruncate_table(TSQLParser.Truncate_tableContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				// todo: handle ptn clause
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String tableNameRaw = ctx.table_name().getText().toUpperCase();
				String tableName = u.normalizeName(tableNameRaw);	
				CaptureIdentifier(tableNameRaw, tableName, "TRUNCATE TABLE", ctx.start.getLine());

				if (ctx.WITH() != null) {
					capturePartitioning("TRUNCATE TABLE", tableName, ctx.start.getLine());
				}				

				String status = featureSupportedInVersion(TruncateTableStmt,"TRUNCATE TABLE");
				captureItem("TRUNCATE TABLE", tableName, TruncateTableStmt, "", status, ctx.start.getLine());	
				
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}		
			
			private void capturePartitioning(String stmt, String tableName, int lineNr) {
				String status = featureSupportedInVersion(Partitioning,stmt);
				String fmt = "Partitioning, "+stmt;
				if (stmt.startsWith("CREATE PARTITION") || stmt.startsWith("ALTER PARTITION") || stmt.startsWith("DROP PARTITION")) fmt = stmt;
				else if (stmt.equals("$PARTITION")) fmt = "$PARTITION.function()";
				else if (stmt.endsWith("SPLIT")) fmt = "ALTER TABLE..SPLIT partition";
				else if (stmt.endsWith("MERGE")) fmt = "ALTER TABLE..MERGE partition";
				else if (stmt.endsWith("SWITCH")) fmt = "ALTER TABLE..SWITCH partition";
				else if (stmt.endsWith("TABLE REBUILD")) fmt = "ALTER TABLE..REBUILD partition";
				else if (stmt.endsWith("INDEX REBUILD")) fmt = "ALTER INDEX..REBUILD partition";
				captureItem(fmt, tableName, Partitioning, stmt, status, lineNr);	
			}					
			
			@Override public String visitCreate_partition_scheme(TSQLParser.Create_partition_schemeContext ctx) {
				capturePartitioning("CREATE PARTITION SCHEME", u.normalizeName(ctx.partition_scheme_name.getText()), ctx.start.getLine());
				visitChildren(ctx);	return null; 								
			}				
			
			@Override public String visitAlter_partition_scheme(TSQLParser.Alter_partition_schemeContext ctx) {
				capturePartitioning("ALTER PARTITION SCHEME", u.normalizeName(ctx.partition_scheme_name.getText()), ctx.start.getLine());
				visitChildren(ctx);	return null; 								
			}	
						
			@Override public String visitDrop_partition_scheme(TSQLParser.Drop_partition_schemeContext ctx) {
				capturePartitioning("DROP PARTITION SCHEME", u.normalizeName(ctx.partition_scheme_name.getText()), ctx.start.getLine());
				visitChildren(ctx);	return null; 								
			}				
			
			@Override public String visitCreate_partition_function(TSQLParser.Create_partition_functionContext ctx) {
				capturePartitioning("CREATE PARTITION FUNCTION", u.normalizeName(ctx.partition_function_name.getText()), ctx.start.getLine());
				visitChildren(ctx);	return null; 								
			}				
			
			@Override public String visitAlter_partition_function(TSQLParser.Alter_partition_functionContext ctx) {				
				capturePartitioning("ALTER PARTITION FUNCTION", u.normalizeName(ctx.partition_function_name.getText()), ctx.start.getLine()); 
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
				visitChildren(ctx);	return null; 								
			}	
						
			@Override public String visitWaitfor_statement(TSQLParser.Waitfor_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String type = "DELAY";
				if (ctx.TIME() != null) type = "TIME";
				String status = featureSupportedInVersion(WaitForStmt, type);
				captureItem("WAITFOR "+type, "", WaitForStmt, type, status, ctx.start.getLine());	
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}	
						
			@Override public String visitBackup_statement(TSQLParser.Backup_statementContext ctx) {
				//this combines a number of really different BACKUP statements
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String status = featureSupportedInVersion(DBAStmts, "BACKUP");
				captureItem("BACKUP", "", DBAStmts, "BACKUP", status, ctx.start.getLine());	
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}	

			@Override public String visitRestore_statement(TSQLParser.Restore_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String status = featureSupportedInVersion(DBAStmts, "RESTORE");
				captureItem("RESTORE", "", DBAStmts, "RESTORE", status, ctx.start.getLine());	
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}	
			
			@Override public String visitKill_statement(TSQLParser.Kill_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String status = featureSupportedInVersion(DBAStmts, "KILL");
				captureItem("KILL", "", DBAStmts, "KILL", status, ctx.start.getLine());	
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}	
						
			@Override public String visitCheckpoint_statement(TSQLParser.Checkpoint_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String status = featureSupportedInVersion(CheckpointStmt);
				captureItem(CheckpointStmt, "", CheckpointStmt, "", status, ctx.start.getLine());	
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}	
						
			@Override public String visitIf_statement(TSQLParser.If_statementContext ctx) {
				captureItem("IF", "", ControlFlowReportGroup, "", u.Supported, ctx.start.getLine());	
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
						
			@Override public String visitRaiseerror_statement(TSQLParser.Raiseerror_statementContext ctx) {
				captureItem("RAISERROR", "", ControlFlowReportGroup, "", u.Supported, ctx.start.getLine());	
				String msg = ctx.msg.getText();				
				Integer exprInt	= getNumericConstant(msg, true);
				if (exprInt != null) {
					captureAtAtErrorValue(exprInt, ", via RAISERROR()", "", ctx.start.getLine());															
				}	
				else {
					// cannot determine the value being compared against	
				}					
				visitChildren(ctx);		
				return null; 								
			}	
						
			@Override public String visitThrow_statement(TSQLParser.Throw_statementContext ctx) {
				//THROW 51515, 'EHR is disabled', 1;  
				String xtra = "";
				String errno = "";
				if (ctx.throw_error_number() != null) {
					errno = ctx.throw_error_number().getText();				
					Integer exprInt	= getNumericConstant(errno, true);
					xtra = " <error-nr>";
					if (exprInt != null) {
						captureAtAtErrorValue(exprInt, ", via THROW", "", ctx.start.getLine());															
					}	
					else {
						// cannot determine the value being compared against -- should never happen for THROW
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
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String fmt = "";
				if (ctx.GOTO() != null) fmt = "GOTO label";
				else fmt = "label: (for GOTO)";
				String status = featureSupportedInVersion(GotoStmt);
				captureItem(fmt, "", GotoStmt, "", status, ctx.start.getLine());	
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}	
						
			@Override public String visitSystem_versioning_column(TSQLParser.System_versioning_columnContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				// ToDo: handle ALTER TABLE
				hasSystemVersioningColumn = true;	
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}				

			@Override public String visitReturn_statement(TSQLParser.Return_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String context = "";
				if (u.currentObjectType.equals("PROCEDURE")) {
					if (ctx.expression() != null) context = " <integer>, in procedure";
					else context = ", in procedure";
				}
				else if (u.currentObjectType.equals("FUNCTION")) {
					context = ", in function";
					if (!lookupSUDF(u.currentObjectName.toUpperCase()).isEmpty()) context = " <scalar>, in function";
					else if (!lookupTUDF(u.currentObjectName.toUpperCase()).isEmpty()) context = " <result set>, in function";					
				}
				else if (u.currentObjectType.equals("TRIGGER")) {
					context = ", in trigger";
				}
				captureItem("RETURN"+context, "", ControlFlowReportGroup, "RETURN", u.Supported, ctx.start.getLine());	
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}	
			
			@Override public String visitWith_expression(TSQLParser.With_expressionContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				
				if (ctx.XMLNAMESPACES() != null) {
					CaptureXMLNameSpaces(null, "", ctx.start.getLine());
				}

				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}	
			
			@Override public String visitDeclare_cursor(TSQLParser.Declare_cursorContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
 
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
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}				

			@Override public String visitFetch_cursor(TSQLParser.Fetch_cursorContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
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
					captureItem("GLOBAL option for " + stmt, stmt, CursorGlobal, stmt, statusG, ctx.start.getLine());	
				}
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}
						
			@Override public String visitCursor_statement(TSQLParser.Cursor_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				if ((ctx.declare_cursor() != null) && (ctx.fetch_cursor() != null)) {
	 				String stmt = "";
					if (ctx.OPEN() != null) stmt = "OPEN";
					if (ctx.CLOSE() != null) stmt = "CLOSE";
					if (ctx.DEALLOCATE() != null) stmt = "DEALLOCATE CURSOR";
					captureItem(stmt, "", CursorsReportGroup, "", u.Supported, ctx.start.getLine());	
					
					if (ctx.GLOBAL() != null) {
						String status = featureSupportedInVersion(CursorGlobal, stmt);
						captureItem("GLOBAL option for " + stmt, stmt, CursorGlobal, stmt, status, ctx.start.getLine());	
					}
				}
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
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
				captureOption(section, option, lineNr, "");
			}
			private void captureOption(String section, String option, int lineNr, String fmt2) {	
				//u.appOutput(u.thisProc()+"section=["+section+"] option=["+option+"] fmt2=["+fmt2+"] ");	
				if (option.trim().isEmpty()) return;
				String fmt = section;
				if (fmt.endsWith("options")) fmt = u.removeLastChar(fmt);
				String status = featureSupportedInVersion(section, option);
				captureItem(fmt+" " + option+fmt2, option, section, option, status, lineNr);	
			}
			
			// called only for rules named '{create|alter|drop}_stmt'
			// todo: add object name for detailed reporting
			private String captureSimpleStmt (String ruleName, int lineNr) {		
				List<String> words = new ArrayList<>(Arrays.asList(ruleName.toUpperCase().split("_")));
				String kwd = words.get(0);
				words.remove(0);
				String obj = String.join(" ", words);
				String status = featureSupportedInVersion(MiscObjects, obj);
				captureItem(kwd + " " + obj, "", MiscObjects, obj, status, lineNr);					
				return status;
			}

			@Override public String visitAlter_login(TSQLParser.Alter_loginContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
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
					option = getOptionName(option);
					if (option.equals("NOCREDENTIAL")) option = "NO CREDENTIAL";
					if (option.equals("PASSWORD")) {
						if (optionX.HASHED() != null) option = "PASSWORD HASHED";
						else option = "PASSWORD";
					}
					captureOption(LoginOptions, option, ctx.start.getLine(), ", in ALTER LOGIN");
					
					if (optionX.MUST_CHANGE() != null) 
						captureOption(LoginOptions, "MUST_CHANGE", ctx.start.getLine(), ", in ALTER LOGIN");

					if (optionX.UNLOCK() != null) 
						captureOption(LoginOptions, "UNLOCK", ctx.start.getLine(), ", in ALTER LOGIN");

					if (optionX.OLD_PASSWORD() != null) 
						captureOption(LoginOptions, "OLD_PASSWORD", ctx.start.getLine(), ", in ALTER LOGIN");
				}
				
				captureItem("ALTER LOGIN", name, UsersReportGroup, "", u.Supported, ctx.start.getLine());	
				
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}

			@Override public String visitCreate_login(TSQLParser.Create_loginContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
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
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}

			@Override public String visitCreate_user(TSQLParser.Create_userContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				
				//ToDO: detect special cases of users, like for contained databases
 				String name = "";
 				if (ctx.user_name != null) name = u.normalizeName(ctx.user_name.getText());	
 				
 				// find out if USER objects are supported at all
 				String status = featureSupportedInVersion(MiscObjects, "USER");
 				captureItem("CREATE USER", name, UsersReportGroup, "", status, ctx.start.getLine());	

 				if (status.equals(u.Supported)) {	 		
					if (ctx.ALLOW_ENCRYPTED_VALUE_MODIFICATIONS() != null) 
						captureOption(UserOptions, "ALLOW_ENCRYPTED_VALUE_MODIFICATIONS", ctx.start.getLine(), ", in CREATE USER");

					if (ctx.DEFAULT_SCHEMA() != null) 
						captureOption(UserOptions, "DEFAULT_SCHEMA", ctx.start.getLine(), ", in CREATE USER");

					if (ctx.WITHOUT() != null) 
						captureOption(UserOptions, "WITHOUT LOGIN", ctx.start.getLine(), ", in CREATE USER");
					else if (ctx.LOGIN() != null) 
						captureOption(UserOptions, "FOR LOGIN", ctx.start.getLine(), ", in CREATE USER");

					if (ctx.CERTIFICATE() != null) 
						captureOption(UserOptions, "FROM CERTIFICATE", ctx.start.getLine(), ", in CREATE USER");

					if (ctx.KEY() != null) 
						captureOption(UserOptions, "FROM ASYMMETRIC KEY", ctx.start.getLine(), ", in CREATE USER");
				}
    				
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}
						
			@Override public String visitAlter_user(TSQLParser.Alter_userContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				
				//ToDO: detect special cases of users, like for contained databases
 				String name = u.normalizeName(ctx.username.getText());	
 				
 				// find out if USER objects are supported at all
 				String status = featureSupportedInVersion(MiscObjects, "USER");
 				captureItem("ALTER USER", name, UsersReportGroup, "", status, ctx.start.getLine());	

 				if (status.equals(u.Supported)) {	 		
					if (ctx.ALLOW_ENCRYPTED_VALUE_MODIFICATIONS() != null) 
						captureOption(UserOptions, "ALLOW_ENCRYPTED_VALUE_MODIFICATIONS", ctx.start.getLine(), ", in ALTER USER");

					if (ctx.DEFAULT_SCHEMA() != null) 
						captureOption(UserOptions, "DEFAULT_SCHEMA", ctx.start.getLine(), ", in ALTER USER");

					if (ctx.NAME() != null) 
						captureOption(UserOptions, "NAME", ctx.start.getLine(), ", in ALTER USER");
					
					if (ctx.LOGIN() != null) 
						captureOption(UserOptions, "LOGIN", ctx.start.getLine(), ", in ALTER USER");

					if (ctx.DEFAULT_LANGUAGE() != null) 
						captureOption(UserOptions, "DEFAULT_LANGUAGE", ctx.start.getLine(), ", in ALTER USER");

					if (ctx.PASSWORD() != null) 
						captureOption(UserOptions, "PASSWORD", ctx.start.getLine(), ", in ALTER USER");

					if (ctx.OLD_PASSWORD() != null) 
						captureOption(UserOptions, "OLD_PASSWORD", ctx.start.getLine(), ", in ALTER USER");
				}
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}
		
			@Override public String visitCreate_db_role(TSQLParser.Create_db_roleContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				
				String name = u.normalizeName(ctx.role_name.getText());				
 				captureItem("CREATE ROLE", name, UsersReportGroup, "", u.Supported, ctx.start.getLine());	
 				 		
				if (ctx.AUTHORIZATION() != null) 
					captureOption(DbRoleOptions, "AUTHORIZATION", ctx.start.getLine(), ", in CREATE ROLE");

				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}
						
			@Override public String visitAlter_db_role(TSQLParser.Alter_db_roleContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				
 				String name = u.normalizeName(ctx.role_name.getText());	
 				 				
 				// find out if ALTER ROLE is supported at all
 				String status = featureSupportedInVersion(AlterDbRole);
 				captureItem(AlterDbRole, name, UsersReportGroup, "", status, ctx.start.getLine());	
 				
 				if (status.equals(u.Supported)) {
					if (ctx.ADD() != null) 
						captureOption(SchemaOptions, "ADD MEMBER", ctx.start.getLine(), ", in ALTER ROLE");

					else if (ctx.DROP() != null) 
						captureOption(SchemaOptions, "DROP MEMBER", ctx.start.getLine(), ", in ALTER ROLE");

					else if (ctx.NAME() != null) 
						captureOption(SchemaOptions, "NAME", ctx.start.getLine(), ", in ALTER ROLE");
				}

				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}
			
			@Override public String visitDrop_db_role(TSQLParser.Drop_db_roleContext ctx) { 
				captureDropObject("ROLE", 1, ctx.if_exists(), UsersReportGroup, ctx.start.getLine()); visitChildren(ctx); return null; 
			}				
						
			@Override public String visitCreate_server_role(TSQLParser.Create_server_roleContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());			
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); 			
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}
						
			@Override public String visitAlter_server_role(TSQLParser.Alter_server_roleContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());			
				// toDo: when supported, handle stmt options		
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); 
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}
			@Override public String visitDrop_server_role(TSQLParser.Drop_server_roleContext ctx) { 
				captureDropObject("SERVER ROLE", 1, null, UsersReportGroup, ctx.start.getLine()); visitChildren(ctx); return null; 
			}				
						
			@Override public String visitCreate_schema(TSQLParser.Create_schemaContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				
 				String name = "";
 				if (ctx.schema_name != null) name = u.normalizeName(ctx.schema_name.getText());	 				
 				captureItem("CREATE ROLE", name, UsersReportGroup, "", u.Supported, ctx.start.getLine());	
 				 		
				if (ctx.AUTHORIZATION() != null) 
					captureOption(SchemaOptions, "AUTHORIZATION", ctx.start.getLine(), ", in CREATE ROLE");

				if (ctx.create_table().size() > 0) 
					captureOption(SchemaOptions, "CREATE TABLE", ctx.start.getLine(), ", in CREATE ROLE");
    				
				if (ctx.create_or_alter_view().size() > 0) 
					captureOption(SchemaOptions, "CREATE VIEW", ctx.start.getLine(), ", in CREATE ROLE");
    				
				if (ctx.grant_statement().size() > 0) 
					captureOption(SchemaOptions, "GRANT", ctx.start.getLine(), ", in CREATE ROLE");
    				
				if (ctx.revoke_statement().size() > 0) 
					captureOption(SchemaOptions, "REVOKE", ctx.start.getLine(), ", in CREATE ROLE");
    				
				if (ctx.deny_statement().size() > 0) 
					captureOption(SchemaOptions, "DENY", ctx.start.getLine(), ", in CREATE ROLE");
    				
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}
						
			@Override public String visitAlter_schema(TSQLParser.Alter_schemaContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				
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
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}
    			
			@Override public String visitGrant_statement(TSQLParser.Grant_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
 				String status = featureSupportedInVersion(GrantStmt);
 				captureItem(GrantStmt, "", GrantStmt, "", status, ctx.start.getLine());	
 				if (status.equals(u.Supported)) {
					// Todo: check for specific permissions
					// Todo: multiple permissionss are possible
					// Todo: the code below needs to be done right, it does not work (but GRANT?REVOKE/DENY ar ento supprote dnayway
					String perm = "";
					if (ctx.ALL() != null) perm = "ALL PRIVILEGES";
					else perm = ctx.permissions().getText().toUpperCase();  // this may be a list?
					u.appOutput(u.thisProc()+"perm=["+perm+"] ");
					captureOption(GrantStmt, perm, ctx.start.getLine(), ", in GRANT"); // ToDo: yet unclear how to report this
					
					if (ctx.ON() != null) {
						String grantObject = ctx.permission_object().getText().toUpperCase(); // can this be a list?
						captureOption(GrantStmt, "ON "+grantObject, ctx.start.getLine(), ", in GRANT"); // ToDo: yet unclear how to report this
					}

					String grantee = "";
					if (ctx.principals() != null) grantee = ctx.principals().getText().toUpperCase();  // list?
					
					if (ctx.WITH() != null) {
						String grantOption = "WITH GRANT OPTION";
						captureOption(GrantStmt, grantOption, ctx.start.getLine(), ", in GRANT"); // ToDo: yet unclear how to report this
					}
					
					if (ctx.AS() != null) {
						String grantor = ctx.principal_id().getText().toUpperCase();  
						captureOption(GrantStmt, "AS GRANTOR", ctx.start.getLine(), ", in GRANT"); // ToDo: yet unclear how to report this
					}
				}
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}

			@Override public String visitRevoke_statement(TSQLParser.Revoke_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
 				String status = featureSupportedInVersion(RevokeStmt);
 				captureItem(RevokeStmt, "", RevokeStmt, "", status, ctx.start.getLine());	
 				if (status.equals(u.Supported)) {
					// Todo: check for specific permissions
					// Todo: the code below needs to be done right
					String perm = "";
					if (ctx.ALL() != null) perm = "ALL PRIVILEGES";
					else perm = ctx.permissions().getText().toUpperCase();  // this may be a list?
					captureOption(PermissionsReportGroup, perm, ctx.start.getLine(), ", in REVOKE"); // ToDo: yet unclear how to report this
					
					if (ctx.ON() != null) {
						String revokeObject = ctx.permission_object().getText().toUpperCase(); // can this be a list?
						captureOption(RevokeStmt, "ON "+revokeObject, ctx.start.getLine(), ", in REVOKE"); // ToDo: yet unclear how to report this
					}

					String grantee = "";
					if (ctx.principals() != null) grantee = ctx.principals().getText().toUpperCase();  // list?
					
					if (ctx.GRANT() != null) {
						String grantOption = "GRANT OPTION FOR";
						captureOption(RevokeStmt, grantOption, ctx.start.getLine(), ", in REVOKE"); // ToDo: yet unclear how to report this
					}
					
					if (ctx.AS() != null) {
						String grantor = ctx.principal_id().getText().toUpperCase();  
						captureOption(RevokeStmt, "AS GRANTOR", ctx.start.getLine(), ", in REVOKE"); // ToDo: yet unclear how to report this
					}			
							
					if (ctx.CASCADE() != null) {
						String cascade = ctx.principal_id().getText().toUpperCase();  
						captureOption(RevokeStmt, "CASCADE", ctx.start.getLine(), ", in REVOKE"); // ToDo: yet unclear how to report this
					}
				}
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}		

			@Override public String visitDeny_statement(TSQLParser.Deny_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
 				String status = featureSupportedInVersion(DenyStmt);
 				captureItem(DenyStmt, "", DenyStmt, "", status, ctx.start.getLine());	
 				if (status.equals(u.Supported)) {
					// Todo: check for specific permissions
					// Todo: the code below needs to be done right
					String perm = "";
					if (ctx.ALL() != null) perm = "ALL PRIVILEGES";
					else perm = ctx.permissions().getText().toUpperCase();  // this may be a list?
					captureOption(PermissionsReportGroup, perm, ctx.start.getLine(), ", in DENY"); // ToDo: yet unclear how to report this
					
					if (ctx.ON() != null) {
						String revokeObject = ctx.permission_object().getText().toUpperCase(); // can this be a list?
						captureOption(DenyStmt, "ON "+revokeObject, ctx.start.getLine(), ", in DENY"); // ToDo: yet unclear how to report this
					}

					String grantee = "";
					if (ctx.principals() != null) grantee = ctx.principals().getText().toUpperCase();  // list?
					
					if (ctx.AS() != null) {
						String grantor = ctx.principal_id().getText().toUpperCase();  
						captureOption(DenyStmt, "AS GRANTOR", ctx.start.getLine(), ", in DENY"); // ToDo: yet unclear how to report this
					}			
							
					if (ctx.CASCADE() != null) {
						String cascade = ctx.principal_id().getText().toUpperCase();  
						captureOption(DenyStmt, "CASCADE", ctx.start.getLine(), ", in DENY"); // ToDo: yet unclear how to report this
					}
				}
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}				


			@Override public String visitAlter_authorization(TSQLParser.Alter_authorizationContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
 				String status = featureSupportedInVersion(AlterAuthStmt);
 				captureItem(AlterAuthStmt, "", AlterAuthStmt, "", status, ctx.start.getLine());	
 				if (status.equals(u.Supported)) {
					// Todo: check for specific permissions
					// Todo: the code below needs to be done right

					if (ctx.object_type() != null) {
						String objType = ctx.object_type().getText().toUpperCase(); 
					}

					String grantee = ctx.authorization_grantee().getText().toUpperCase(); 
					
					if (ctx.entity_name() != null) {
						String objName = ctx.entity_name().getText().toUpperCase();  
					}			
				}
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}				
    						
			@Override public String visitDrop_login(TSQLParser.Drop_loginContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}			

			@Override public String visitConversation_statement(TSQLParser.Conversation_statementContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
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
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}	
			
			private void captureServiceBroker(String stmt, int lineNr) {
				String status = featureSupportedInVersion(ServiceBroker, stmt);
				captureItem(stmt, "", ServiceBroker, stmt, status, lineNr);									
			}			
    				
			@Override public String visitOpen_key(TSQLParser.Open_keyContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String stmt = "OPEN SYMMETRIC KEY";
				if (ctx.MASTER() != null) stmt = "OPEN MASTER KEY";
 				String status = featureSupportedInVersion(OpenKeyStmt);
 				captureItem(stmt, "", OpenKeyStmt, stmt, status, ctx.start.getLine());	
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}				
    		
			@Override public String visitClose_key(TSQLParser.Close_keyContext ctx) {
				if (u.debugging) dbgTraceVisitEntry(u.thisProc());	
				String stmt = "CLOSE SYMMETRIC KEY";
				if (ctx.ALL() != null) stmt = "CLOSE ALL SYMMETRIC KEYS";
				else if (ctx.MASTER() != null) stmt = "CLOSE MASTER KEY";
 				String status = featureSupportedInVersion(CloseKeyStmt);
 				captureItem(stmt, "", CloseKeyStmt, stmt, status, ctx.start.getLine());	
				visitChildren(ctx);		
				if (u.debugging) dbgTraceVisitExit(u.thisProc());						
				return null; 								
			}	
			    										
			@Override public String visitCreate_sequence(TSQLParser.Create_sequenceContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_sequence(TSQLParser.Alter_sequenceContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_sequence(TSQLParser.Drop_sequenceContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}			
			@Override public String visitCreate_application_role(TSQLParser.Create_application_roleContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_application_role(TSQLParser.Alter_application_roleContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_application_role(TSQLParser.Drop_application_roleContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitCreate_column_encryption_key(TSQLParser.Create_column_encryption_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_column_encryption_key(TSQLParser.Alter_column_encryption_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_column_encryption_key(TSQLParser.Drop_column_encryption_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitCreate_column_master_key(TSQLParser.Create_column_master_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_column_master_key(TSQLParser.Drop_column_master_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitCreate_asymmetric_key(TSQLParser.Create_asymmetric_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_asymmetric_key(TSQLParser.Alter_asymmetric_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_asymmetric_key(TSQLParser.Drop_asymmetric_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitCreate_master_key(TSQLParser.Create_master_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_master_key(TSQLParser.Alter_master_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_master_key(TSQLParser.Drop_master_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_service_master_key(TSQLParser.Alter_service_master_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitCreate_symmetric_key(TSQLParser.Create_symmetric_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_symmetric_key(TSQLParser.Alter_symmetric_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_symmetric_key(TSQLParser.Drop_symmetric_keyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitCreate_assembly(TSQLParser.Create_assemblyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_assembly(TSQLParser.Alter_assemblyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_assembly(TSQLParser.Drop_assemblyContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}			
			@Override public String visitCreate_synonym(TSQLParser.Create_synonymContext ctx) {
				String status = captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); 
				if (status.equals(u.Supported)) {
					String synBaseObjRaw = ctx.full_object_name().getText();
					String synBaseObj = u.normalizeName(synBaseObjRaw);
					CaptureIdentifier(synBaseObjRaw, synBaseObj, "CREATE SYNONYM", ctx.start.getLine());					
				}
				visitChildren(ctx); 
				return null; 								
			}
			@Override public String visitDrop_synonym(TSQLParser.Drop_synonymContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitCreate_aggregate(TSQLParser.Create_aggregateContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_aggregate(TSQLParser.Drop_aggregateContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitCreate_credential(TSQLParser.Create_credentialContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_credential(TSQLParser.Alter_credentialContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_credential(TSQLParser.Drop_credentialContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitCreate_cryptographic_provider(TSQLParser.Create_cryptographic_providerContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_cryptographic_provider(TSQLParser.Alter_cryptographic_providerContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_cryptographic_provider(TSQLParser.Drop_cryptographic_providerContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitCreate_contract(TSQLParser.Create_contractContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_contract(TSQLParser.Drop_contractContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitCreate_diagnostic_session(TSQLParser.Create_diagnostic_sessionContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_diagnostic_session(TSQLParser.Drop_diagnostic_sessionContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitCreate_default(TSQLParser.Create_defaultContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_default(TSQLParser.Drop_defaultContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitCreate_rule(TSQLParser.Create_ruleContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_rule(TSQLParser.Drop_ruleContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}			
			
			@Override public String visitCreate_route(TSQLParser.Create_routeContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_route(TSQLParser.Drop_routeContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
						
			@Override public String visitCreate_queue(TSQLParser.Create_queueContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_queue(TSQLParser.Alter_queueContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_queue(TSQLParser.Drop_queueContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			
			@Override public String visitCreate_service(TSQLParser.Create_serviceContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_service(TSQLParser.Alter_serviceContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_service(TSQLParser.Drop_serviceContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
						
			@Override public String visitCreate_message_type(TSQLParser.Create_message_typeContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_message_type(TSQLParser.Alter_message_typeContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_message_type(TSQLParser.Drop_message_typeContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			
			@Override public String visitCreate_certificate(TSQLParser.Create_certificateContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_certificate(TSQLParser.Alter_certificateContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_certificate(TSQLParser.Drop_certificateContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
						
			@Override public String visitCreate_availability_group(TSQLParser.Create_availability_groupContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_availability_group(TSQLParser.Alter_availability_groupContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_availability_group(TSQLParser.Drop_availability_groupContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}

			@Override public String visitCreate_external_data_source(TSQLParser.Create_external_data_sourceContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_external_data_source(TSQLParser.Alter_external_data_sourceContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_external_data_source(TSQLParser.Drop_external_data_sourceContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}						
								
			@Override public String visitCreate_external_library(TSQLParser.Create_external_libraryContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_external_library(TSQLParser.Alter_external_libraryContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_external_library(TSQLParser.Drop_external_libraryContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}

			@Override public String visitCreate_external_resource_pool(TSQLParser.Create_external_resource_poolContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_external_resource_pool(TSQLParser.Alter_external_resource_poolContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_external_resource_pool(TSQLParser.Drop_external_resource_poolContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			
			@Override public String visitAlter_resource_governor(TSQLParser.Alter_resource_governorContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}

			@Override public String visitCreate_workload_group(TSQLParser.Create_workload_groupContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_workload_group(TSQLParser.Alter_workload_groupContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_workload_group(TSQLParser.Drop_workload_groupContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
								
			@Override public String visitCreate_fulltext_catalog(TSQLParser.Create_fulltext_catalogContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_fulltext_catalog(TSQLParser.Alter_fulltext_catalogContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_fulltext_catalog(TSQLParser.Drop_fulltext_catalogContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}

			@Override public String visitCreate_fulltext_index(TSQLParser.Create_fulltext_indexContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_fulltext_index(TSQLParser.Alter_fulltext_indexContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_fulltext_index(TSQLParser.Drop_fulltext_indexContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}

			@Override public String visitCreate_fulltext_stoplist(TSQLParser.Create_fulltext_stoplistContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_fulltext_stoplist(TSQLParser.Alter_fulltext_stoplistContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_fulltext_stoplist(TSQLParser.Drop_fulltext_stoplistContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}

			@Override public String visitCreate_remote_service_binding(TSQLParser.Create_remote_service_bindingContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitAlter_remote_service_binding(TSQLParser.Alter_remote_service_bindingContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
			@Override public String visitDrop_remote_service_binding(TSQLParser.Drop_remote_service_bindingContext ctx) {
				captureSimpleStmt(currentRuleName(ctx.getRuleIndex()), ctx.start.getLine()); visitChildren(ctx); return null; 								
			}
						
		};

		// this assert does not work?
		assert (tree != null) : "parse tree is null";
		
		//report setting at start of batch
		String on_off = TSQLLexer.QUOTED_IDENTIFIER_FLAG?"ON":"OFF";
		
		if (pass == 1) {
			if (u.debugging) u.dbgOutput(u.thisProc()+"-- doing pass 1, batchNr="+batchNr+" QUOTED_IDENTIFIER="+on_off+" ---", u.debugPtree);
			u.clearContext();	
			pass1Analysis.visit(tree);	
		}
			
		if (pass == 2) {
			//dumpSymTab("");  // debug
			localVars.clear(); 			
			localAtAtErrorVars.clear(); 			
			u.clearContext();	
			
			if (u.debugging) u.dbgOutput(u.thisProc()+"-- doing pass 2, batchNr="+batchNr+" QUOTED_IDENTIFIER="+on_off+" ---", u.debugPtree);
			stmt.clear();
			pass2Analysis.visit(tree);		
			
			// classify & capture found SELECT statements
			if (u.debugging) u.dbgOutput(u.thisProc()+"SELECT stmts found: " + stmt.size(), u.debugPtree);
			for (int i: stmt.keySet()) {
				CompassItem item = stmt.get(i);
				captureSELECT(item, i);
			}
			stmt.clear();
		}
	}
}