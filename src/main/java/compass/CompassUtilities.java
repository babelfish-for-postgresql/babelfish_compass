/*
Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/
package compass;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.math.*;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.stream.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;

public class CompassUtilities {

	// not-initialized strings
	public static final String uninitialized = "-init-";

	public static boolean onWindows  = false;
	public static boolean onMac      = false;
	public static boolean onLinux    = false;
	public static String  onPlatform = uninitialized;

	public static final String thisProgVersion      = "2022-03";
	public static final String thisProgVersionDate  = "March 2023";
	public static final String thisProgName         = "Babelfish Compass";
	public static final String thisProgNameLong     = "Compatibility assessment tool for Babelfish for PostgreSQL";
	public static final String thisProgNameExec     = "Compass";
	public static final String thisProgPathExec     = "compass";
	public static final String babelfishProg        = "Babelfish";
	public static final String copyrightLine        = "Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.";
	public static String thisProgExec               = "java " + thisProgPathExec + "." + thisProgNameExec;
	public static final String thisProgExecWindows  = "BabelfishCompass.bat";
	public static final String thisProgExecLinux    = "BabelfishCompass.sh";
	public static final String thisProgExecMac      = "BabelfishCompass.sh";
	
	// user docs
	public static final String userDocText          = thisProgName + " User Guide";
	public static final String userDocURL           = "https://github.com/babelfish-for-postgresql/babelfish_compass/blob/main/BabelfishCompass_UserGuide.pdf";

	public static final String disclaimerMsg  =
            "Notice:\n"
          + "This report contains an assessment based on the resources you scanned with the\n"
          + "Babelfish Compass tool. The information contained in this report, including whether\n"
          + "or not a feature is 'supported' or 'not supported', is made available 'as is',\n"
          + "and may be incomplete, incorrect, and subject to interpretation.\n"
          + "You should not base decisions on the information in this report without independently\n"
          + "validating it against the actual SQL/DDL code on which this report is based.\n";

	// .cfg file is in a fixed place, %COMPASS%/<defaultfilename>
	public String cfgFileName = uninitialized;
	public final String defaultCfgFileName = "BabelfishFeatures.cfg";

	// user .cfg file is in a fixed place, under document folder
	public String userCfgFileName = uninitialized;
	public static final String defaultUserCfgFileName  = "BabelfishCompassUser.cfg";
	public static boolean userConfig = true;

	// .cfg file format version as found in the .cfg file; this is validated
	public Integer cfgFileFormatVersionRead = 0;
	// .cfg file format version supported by this version of the Babelfish Compass tool
	public Integer cfgFileFormatVersionSupported = 1;

	// .cfg file timestamp
	public String cfgFileTimestamp = uninitialized;
	
	// capture file format
	// if this format is ever changed, we need to provide an option to keep generating a previous version so that we don't break apps relying on the format
	public static String captureFileFormatBaseVersion = "1";  // lowest format version
	public static List<String> captureFileFormatVersionList = Arrays.asList(captureFileFormatBaseVersion);  // supported format versions
	public static String captureFileFormatVersion = captureFileFormatBaseVersion;  // actual format version used

	// user-specified
	public static final String fileNameCharsAllowed = "[^\\w\\_\\.\\-\\/\\(\\)]";
	public String targetBabelfishVersion = ""; // Babelfish version for which we're doing the analysis
	public boolean stdReport = false;	// development only

	// minimum Babelfish version; this is fixed
	public static final String baseBabelfishVersion = "1.0.0";

	// standard line length
	public final int reportLineLength = 80;
	public final String lineIndent = "    ";

	// file handling
	public String BabelfishCompassFolderName = uninitialized;
	public final String BabelfishCompassFolderNameWindows = "BabelfishCompass";
	public final String BabelfishCompassFolderNameMac     = "BabelfishCompassReports";
	public final String BabelfishCompassFolderNameLinux   = "BabelfishCompassReports";
	public final String batchDirName = "batches";
	public final String batchFileSuffix = "batch.txt";
	public final String errBatchDirName = "errorbatches";
	public final String errBatchFileSuffix = "errbatch.txt";
	public final String capDirName = "captured";
	public final String captureFileName = "captured";
	public final String captureFileTag = "bbf~captured";
	public final String captureFileSuffix = "dat";
	public final String symTabDirName = "sym";
	public final String symTabFileTag = "bbf~symtab";
	public final String symTabFileSuffix = "dat";
	public final String importDirName = "imported";
	public final String importHTMLDirName = "html";
	public final String importFileTag = "bbf~imported";
	public final String importFileSuffix = "dat";
	public final String rewrittenDirName = "rewritten";
	public final String rewrittenFileSuffix = "rewritten";
	public final String rewrittenHTMLDirName = "html";
	public final String rewrittenFileTag = "bbf~rewritten";
	public final String rewrittenTmpFile = "bbf~rewritten.tmp";
	public final String textSuffix = "txt";
	public final String HTMLSuffix = "html";
	public final String logDirName = "log";
	public final String PGImportFileName = "pg_import";
	
	public final String getAnonymizedItemsFilename = "anonymizedBabelfishItems.dat";

	public String reportFileTextPathName = uninitialized;
	public String reportFileHTMLPathName = uninitialized;
	public String reportFilePathName = uninitialized;
	public BufferedWriter reportFileWriter;
	public BufferedWriter reportFileWriterHTML;
	public String batchFilePathName;
	public BufferedWriter batchFileWriter;
	public String errBatchFilePathName;
	public String symTabFilePathName;
	public BufferedWriter symTabFileWriter;
	public int symTabFileLineCount=0;
	public BufferedWriter errBatchFileWriter = null;
	public String importFilePathName;
	public BufferedWriter importFileWriter;
	public String importFileHTMLPathName;
	public BufferedWriter importFileHTMLWriter;
	public int importFileWriteLineNr = 0;
	public String sessionLogPathName;
	public BufferedWriter sessionLogWriter;
	public BufferedWriter userCfgFileWriter;	
	public String extractedFilePathName;
	public BufferedWriter extractedFileWriter;	
	public BufferedReader rewrittenInFileReader;	
	public BufferedWriter rewrittenFileWriter;	

	// importformat options
	public static final String autoFmt = "auto";
	public static final String sqlcmdFmt = "sqlcmd";
	public static final String jsonQueryFmt = "jsonQuery";
	public static final String extendedEventsXMLFmt = "extendedEventsXML";
	public static final String genericSQLXMLFmt = "sqlXML";
	public static List<String> importFormatOption        = Arrays.asList(autoFmt, sqlcmdFmt, jsonQueryFmt,  extendedEventsXMLFmt, genericSQLXMLFmt);
	public static List<String> importFormatOptionDisplay = Arrays.asList(autoFmt, sqlcmdFmt, "JSON query", "extended events/XML", "generic SQL XML");
	
	// user-definable, default = auto
	public static String importFormat = autoFmt.toLowerCase();
	
	// unduplication
	public static boolean deDupExtracted = true;
	public static int deDupSkipped = 0;
	private Map<String, String> deDupQueries = new HashMap<>();
	private Map<String, Integer> dupQueryCount = new HashMap<>();
	
	// HTML header/footer
	public String docLinkIcon              = "<div class=\"tooltip\"><span class=\"tooltip_icon\">&#x1F56E;</span> ";
	public String docLinkURL               = "<a href=\""+userDocURL+"\" target=\"_blank\">"+ userDocText +"</a></div>"; 
	public String docLinkURLText           = userDocText +" : " + userDocURL; 
	public String tocLinkIcon              = "<div class=\"tooltip\"><span class=\"tooltip_icon\">&#x2B06;</span>";
	public String backToToCText            = "Back to Table of Contents";
	public String tocLinkURL               = "<a href=\"#toc\">"+ backToToCText +"</a>";   
	public String headerHTMLPlaceholder    = "BBF_HEADERHTMLPLACEHOLDER";
	public String titleHTMLPlaceholder     = "BBF_TITLEHTMLPLACEHOLDER";
	public String footerHTMLPlaceholder    = "BBF_FOOTERHTMLPLACEHOLDER";
	public String reportHTMLPlaceholder    = "BBF_REPORTHTMLPLACEHOLDER";
	public String inputfileHTMLPlaceholder = "BBF_INPUTFILEHTMLPLACEHOLDER";
	public String inputfileTxtPlaceholder  = "BBF_INPUTFILETXTPLACEHOLDER";
	public String appnameHTMLPlaceholder   = "BBF_APPNAMEHTMLPLACEHOLDER";
	public String tocHTMLPlaceholder       = "BBF_TOCHTMLPLACEHOLDER";
	public String tooltipsHTMLPlaceholder  = "BBF_TOOLTIPSHTMLPLACEHOLDER";
	public String tagApps                  = "apps";
	public String tagEstimate              = "estimate";
	public String tagObjcount              = "objcount";
	public String tagRewrite               = "rewrite";
	public String tagSummaryTop            = "summary";
	public String tagSummary               = "summary_";
	public String tagByFeature             = "byfeature_";
	public String tagByObject              = "byobject_";
	public String anchorListOfRewrites     = "anchorlistofrewrites";

	public String cssText =
"a:visited { color: #6d00E6; }\n"+
"a:hover { color: red; }\n"+
"\n"+
"body { font-family: sans-serif; margin: 0px; }\n"+
".body_content { margin: 8px; }\n"+
"\n"+
".header { font-family: sans-serif; padding-left: 0.5em; padding-right: 0.5em; }\n"+
".headerForeground { color: white; padding: 10px; padding-top: 50px; }\n"+
"\n"+
".footer,\n"+
".footer > a:link,\n"+
".footer > a:visited {\n"+
"    color: #cccccc;\n"+
"}\n"+
".footer { margin: 30px; }\n"+
"\n"+
"table { \n"+
"    border-collapse: collapse; \n"+
"    border-spacing: 0px; \n"+
"    margin-top: 20px;\n"+
"}\n"+
"tr { \n"+
"    text-align : center;\n"+
"    vertical-align: top; \n"+
"}\n"+
"th,.linenr {\n"+
"    background-color: #dddddd;\n"+
"    border: solid 1px #666666;\n"+
"    padding: 0em 0.4em 0em 0.4em;\n"+
"    font-size:0.8em;\n"+
"}\n"+
"td { \n"+
"    border: solid 1px #cccccc; \n"+
"    padding: 0em 0.4em 0em 0.4em;\n"+
"}\n"+
"td.left {\n"+
"  border: solid 0px; \n"+
"  padding-left: 15px;\n"+
"  text-align: left;\n"+
"}\n"+
"th.left {\n"+
"  padding-left: 15px;\n"+
"  text-align: left;\n"+
"}\n"+
"pre,.sql {\n"+
"    text-align: left;\n"+
"    font-family: monospace;\n"+
"    white-space: pre;\n"+
"}\n"
;

	public String headerHTML =
"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"> \n"+
"<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"+
"<!-- "+ headerHTMLPlaceholder +" -->\n"+
"<head>\n"+
"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n"+
"<meta http-equiv=\"Content-Language\" content=\"en-us\" />\n"+
"<meta name=\"robots\" content=\"noindex,nofollow\" />\n"+
"<title>"+titleHTMLPlaceholder+"</title>\n";

	public String headerHTMLReport =
"<style>\n"+
tooltipsHTMLPlaceholder +
"/* Tooltip container */\n"+
".tooltip {\n"+
"  position: relative;\n"+
"  display: inline-block;\n"+
"  text-decoration:none;\n"+
"}\n"+
".tooltip_blank {\n"+
"  position: relative;\n"+
"  display: inline-block;\n"+
"  text-decoration:none;\n"+
"  width: 11px;\n"+
"  margin-top: 4px;\n"+
"}\n"+
".tooltip_icon {\n"+
"  position: relative;\n"+
"  display: inline-block;\n"+
"  text-decoration:none;\n"+
"  width: 11px;\n"+
"  margin-top: 0px;\n"+
"  font-family: Arial, Helvetica;\n"+
"}\n"+
"/* Tooltip text */\n"+
".tooltip .tooltip-content {\n"+
"  margin-top: 2px;\n"+
"  margin-left: 10px;\n"+
"  visibility: hidden;\n"+
"  width: 500px;\n"+
"  background-color: #eeeeee;\n"+
"  color: black;\n"+
"  text-align: left;\n"+
"  padding: 2px;\n"+

"  border-width: 1px;  \n"+
"  border-style: solid;\n"+
"  border-color: black;\n"+
"  white-space: pre-wrap;\n"+
"\n"+
"  /* Position the tooltip text */\n"+
"  position: absolute;\n"+
"  z-index: 1;\n"+
"}\n"+
"/* Show the tooltip text when you mouse over the tooltip container */\n"+
".tooltip:hover .tooltip-content {\n"+
"  visibility: visible;\n"+
"}\n"+
"/* Top Arrow */\n"+
".tooltip .tooltip-content::after {\n"+
"  content: \"\";\n"+
"  position: absolute;\n"+
"  bottom: 100%;  /* At the top of the tooltip */\n"+
"  left: 10%;\n"+
"  margin-left: -5px;\n"+
"  border-width: 5px;\n"+
"  border-style: solid;\n"+
"  border-color: transparent transparent black transparent;\n"+
"}\n"+
"</style>"+
"</head>\n"+
"<body>\n"
;

	public String headerHTMLSQL =
"<style>\n"+
"body { font-family: sans-serif; margin: 0px; }\n"+
"table { \n"+
"    border-collapse: collapse; \n"+
"    border-spacing: 0px; \n"+
"    margin-top: 20px;\n"+
"}\n"+
"tr { \n"+
"    text-align : center;\n"+
"    vertical-align: top; \n"+
"}\n"+
"th,.linenr {\n"+
"    background-color: #dddddd;\n"+
"    border: solid 1px #666666;\n"+
"    padding: 0em 0.5em 0em 0.5em;\n"+
"    font-size:0.8em;\n"+
"}\n"+
"td { \n"+
"    border: solid 1px #cccccc; \n"+
"    padding: 0em 0.5em 0em 0.5em;\n"+
"}\n"+
"td.hdr {\n"+
"  border: solid 0px; \n"+
"  text-align: left;\n"+
"}\n"+
"th.hdr {\n"+
"  text-align: left;\n"+
"}\n"+
".sql {\n"+
"    text-align: left;\n"+
"    font-family: monospace;\n"+
"    white-space: pre;\n"+
"}	\n"+
".hdr {\n"+
"    margin: 10px;\n"+
"}\n"+
".ftr {\n"+
"    color: #cccccc;\n"+
"    margin: 10px;\n"+
"	text-align: left;    \n"+
"}\n"+
"</style>\n"+
"</head>\n"+
"<body> \n"+
"<table border=\"0\" cellpadding=\"0\"> \n"+
"<tr><td class=\"hdr\">Report</td><td class=\"hdr\">:</td><td class=\"hdr\">"+reportHTMLPlaceholder+"</td></tr> \n"+
"<tr><td class=\"hdr\">"+inputfileTxtPlaceholder+"</td><td class=\"hdr\">:</td><td class=\"hdr\">"+inputfileHTMLPlaceholder+"</td></tr> \n"+
"<tr><td class=\"hdr\">Application</td><td class=\"hdr\">:</td><td class=\"hdr\">"+appnameHTMLPlaceholder+"</td></tr> \n"+
"<tr><td colspan=\"2\" class=\"hdr\">"+tocHTMLPlaceholder+"</td></tr> \n"+
"</table> \n"+
"<table border=\"0\" cellpadding=\"0\"> \n"+
"<tr><td class=\"hdr\">"+headerHTMLPlaceholder+"</td></tr> \n"+
"</table> \n"+
"<p></p> \n"+
"<table border=\"1\" cellpadding=\"0\">\n"+
"<thead>\n"+
"<tr><th>Line</th>\n"+
"<th class=\"hdr\">SQL code</th>\n"+
"</tr>\n"+
"</thead>\n"+
"<tbody>\n"
;
	public String footerHTML =
"</tbody>\n"+
"</table>\n"+
"<div class=\"ftr\">" + footerHTMLPlaceholder + "\n"+
"</div>\n"+
"</body></html>\n"
;
	private Map<String, String> toolTipsKeys = new HashMap<>();
	static final List<String> toolTipsKeysList = new ArrayList<>();
	static final String tttSeparator = "~~~";
	static final List<String> toolTipsText = Arrays.asList(		
		"STR("+tttSeparator+"STR() is not currently supported; rewrite with CONVERT(), but note the rounding that may be required",
		"CHECKSUM(*)"+tttSeparator+"CHECKSUM(*) is not currently supported; CHECKSUM() with a single non-asterisk argment is supported",
		"CHECKSUM(arg,arg,...)"+tttSeparator+"CHECKSUM() with multiple arguments is not currently supported; CHECKSUM() with a single non-asterisk argment is supported",
		"TRY_CONVERT("+tttSeparator+"TRY_CONVERT() is not currently supported; rewrite with CONVERT()",
		"PARSE("+tttSeparator+"PARSE() is not currently supported; rewrite with CAST() or CONVERT()",
		"TRY_PARSE("+tttSeparator+"PARSE() is not currently supported; rewrite with CAST() or CONVERT()",
		"SQUARE("+tttSeparator+"SQUARE() is not currently supported; rewrite with POWER()",
		"UNICODE("+tttSeparator+"UNICODE() returns the Unicode code point for the first character in a string; this is not currently supported",
		"HASHBYTES("+tttSeparator+"HASHBYTES() currently supports only MD5, SHA1, SHA2_256, SHA2_512",  /// this needs to be adjusted if additional agorithms are implemented
		"IDENTITY("+tttSeparator+"The IDENTITY() function in SELECT-INTO is not currently supported. Use ALTER TABLE to add an identity column",
		"Precision of IDENTITY column "+tttSeparator+"The maximum supported precision of a NUMERIC/DECIMAL type for an IDENTITY column is exceeded; change the precision to stay within the supported limit",
		"CHOOSE("+tttSeparator+"CHOOSE(): Rewrite as a CASE expression",
		"OBJECT_SCHEMA_NAME()"+tttSeparator+"OBJECT_SCHEMA_NAME(): Rewrite as catalog query",
		"ORIGINAL_LOGIN("+tttSeparator+"ORIGINAL_LOGIN() is not currently supported; Rewrite as SUSER_NAME()",
		"SESSION_USER"+tttSeparator+"SESSION_USER is not currently supported; Rewrite as USER_NAME()",
		"SYSTEM_USER"+tttSeparator+"SYSTEM_USER is not currently supported; Rewrite as SUSER_NAME() (the -rewrite option handles this for you)",
		"EOMONTH("+tttSeparator+"EOMONTH() is not currently supported; rewrite with DATEADD()/DATEPART() (the -rewrite option handles this for you)",
		"DATABASE_PRINCIPAL_ID("+tttSeparator+"DATABASE_PRINCIPAL_ID() is not currently supported; Rewrite as USER_NAME()",
		"FORMAT("+tttSeparator+"FORMAT() is not currently supported; some format specifiers may actually work, but others do not. Rewrite the formatting using available functions such as CONVERT()",
		"FORMATMESSAGE("+tttSeparator+"FORMATMESSAGE() is not currently supported; some format specifiers may actually work, but others do not. Rewrite the formatting using available functions such as CONVERT()",
		"FILEGROUP_NAME("+tttSeparator+"File(group)-related features are not currently supported; Consider rewriting your application to avoid using these features",
		"FILEPROPERTY("+tttSeparator+"File(group)-related features are not currently supported; Consider rewriting your application to avoid using these features",
		"NEWSEQUENTIALID()"+tttSeparator+"NEWSEQUENTIALID() is implemented as NEWID(); the sequential nature of the generated values is however not guaranteed, as is the case in SQL Server",
		"DIFFERENCE()"+tttSeparator+"DIFFERENCE() is not currently supported; this is a soundex-related function",
		"SUSER_SNAME()"+tttSeparator+"SUSER_SNAME() is not currently supported; rewrite as SUSER_NAME()",
		"SUSER_SID()"+tttSeparator+"SUSER_SID() is not currently supported; rewrite as SUSER_ID()",
		"\\w+PROPERTY\\("+tttSeparator+"This particular attribute for this PROPERTY function is not currently supported; consider rewriting it as a catalog query",
		"\\w+PROPERTYEX\\("+tttSeparator+"This particular attribute for this PROPERTY function is not currently supported; consider rewriting it as a catalog query",
		"\\w+\\(\\),"+CompassAnalyze.withoutArgumentValidateStr+tttSeparator+"This built-in function is not currently supported when called without arguments",
		"\\w+\\(\\),"+CompassAnalyze.withNArgumentValidateStrRegex+tttSeparator+"This built-in function is not currently supported when called with this number of arguments",
		"CONTAINS("+tttSeparator+"Fulltext search is not currently supported",
		"CONTAINSTABLE("+tttSeparator+"Fulltext search is not currently supported",
		"FREETEXTTABLE("+tttSeparator+"Fulltext search is not currently supported",
		"\\w+ FULLTEXT "+tttSeparator+"Fulltext search is not currently supported",
		"expression AT TIME ZONE"+tttSeparator+"A date/time expression with the AT TIME ZONE syntax is not currently supported; rewrite the expression with time zone offset syntax '+/-hh:mm', e.g. '01-Jan-2022 11:12:13 +02:00' ",
		"Sequence option CACHE" +tttSeparator+"For a sequence, the CACHE option without a number is not currently supported; add a number",
		"Sequence option NO CACHE" +tttSeparator+"For a sequence, the NO CACHE option without a number is not currently supported",
		CompassAnalyze.NextValueFor+tttSeparator+"The NEXT VALUE FOR function for sequence objects is not currently supported. Consider using identity columns instead",
		CompassAnalyze.ParamValueDEFAULT+tttSeparator+"Specifying DEFAULT as a parameter value in a procedure or function call is not currently supported; specify the actual default value instead",
		CompassAnalyze.UnQuotedString+tttSeparator+"Unquoted strings are not currently supported; enclose the string with quotes",
		CompassAnalyze.DoubleQuotedString+", embedded single"+tttSeparator+"An embedded single quote in a double-quotes string is not currently supported. Change the double-quote string delimiters to single quotes and escape the embedded single quote",
		CompassAnalyze.DoubleQuotedString+", embedded double"+tttSeparator+"An embedded double quote in a double-quotes string is not currently supported, and will result in two double quotes in the string. Change the double-quote string delimiters to single quotes and un-escape the embedded double quote",
		CompassAnalyze.ExecuteSQLFunction+tttSeparator+"Calling a SQL function with EXECUTE is not currently supported. Call the function in an expression instead",
		CompassAnalyze.ColonColonFunctionCall+tttSeparator+"Old-style function call with :: syntax is not supported; rewrite without ::",
		CompassAnalyze.TemporaryProcedures+tttSeparator+"Temporary stored procedures (with a name starting with #) are created, but not dropped automatically at the end of a session",
		CompassAnalyze.NumericAsDateTime+tttSeparator+"Using a numeric value in a datetime context is not currently supported. Rewrite the numeric value as an offset (in days) on top of 01-01-1900 00:00:00 (the -rewrite option handles this for you)",
		CompassAnalyze.NumericDateTimeVarAssign+tttSeparator+"Using a numeric value in a datetime context is not currently supported. Rewrite the numeric value as an offset (in days) on top of 01-01-1900 00:00:00 (the -rewrite option handles this for you)",
		"EXECUTE procedure sp_oacreate"+tttSeparator+"This OLE system stored procedure is not currently supported",
		"EXECUTE procedure sp_oadestroy"+tttSeparator+"This OLE system stored procedure is not currently supported",
		"EXECUTE procedure sp_oamethod"+tttSeparator+"This OLE system stored procedure is not currently supported",
		"EXECUTE procedure sp_oageterrorinfo"+tttSeparator+"This OLE system stored procedure is not currently supported",
		"EXECUTE procedure sp_recompile"+tttSeparator+"The recompile feature is not currently supported",
		"EXECUTE procedure sp_dbcmptlevel"+tttSeparator+"The SQL Server compatibility level is not currently supported",
		"EXECUTE procedure sp_"+tttSeparator+"This system stored procedure is not currently supported",
		"EXECUTE procedure xp_cmdshell"+tttSeparator+"xp_cmdshell is not currently supported; consider implementing this using a process external to the database to execute OS commands",
		"EXECUTE procedure xp_"+tttSeparator+"This system stored procedure is not currently supported",
		"EXECUTE procedure, name in variable"+tttSeparator+"Executing a stored procedure whose name is in a variable (i.e. EXECUTE @p) is not currently supported. Rewrite with dynamic SQL (i.e. EXECUTE(...) or sp_executesql)",
		"CREATE SYNONYM"+tttSeparator+"Synonyms are not currently supported; try to rewrite with views (for tables) or procedures/functions (for procedures/functions)",
		"BACKUP"+tttSeparator+"BACKUP/RESTORE is not currently supported, and must be handled with PostgreSQL features",
		"RESTORE"+tttSeparator+"BACKUP/RESTORE is not currently supported, and must be handled with PostgreSQL features",
		"GRANT"+tttSeparator+"This variation of GRANT is not currently supported",
		"REVOKE"+tttSeparator+"This variation of REVOKE is not currently supported",
		"DENY"+tttSeparator+"DENY is not currently supported",
		"ALTER AUTHORIZATION"+tttSeparator+"ALTER AUTHORIZATION (change object ownership) is not currently supported",
		"CREATE ROLE"+tttSeparator+"DB-level roles are not currently supported, except the predefined 'db_owner' role",
		"ALTER ROLE"+tttSeparator+"ALTER ROLE for DB-level roles is not currently supported",
		"CREATE SERVER ROLE"+tttSeparator+"Server-level roles are not currently supported, except the predefined 'sysadmin' role",
		"ALTER SERVER ROLE"+tttSeparator+"ALTER SERVER ROLE for server-level roles is not currently supported, except the predefined 'sysadmin' role",
		"CREATE USER"+tttSeparator+"DB users are not currently supported, except 'dbo' and 'guest'",
		"ALTER USER"+tttSeparator+"DB users are not currently supported, except 'dbo' and 'guest'",
		"ALTER VIEW"+tttSeparator+"ALTER VIEW is not currently supported; use DROP+CREATE",
		"CREATE OR ALTER VIEW"+tttSeparator+"CREATE OR ALTER VIEW is not currently supported; use DROP+CREATE",
		"ALTER PROCEDURE"+tttSeparator+"ALTER PROCEDURE is not currently supported; use DROP+CREATE",
		"CREATE OR ALTER PROCEDURE"+tttSeparator+"CREATE OR ALTER PROCEDURE is not currently supported; use DROP+CREATE",
		"ALTER FUNCTION"+tttSeparator+"ALTER FUNCTION is not currently supported; use DROP+CREATE",
		"CREATE OR ALTER FUNCTION"+tttSeparator+"CREATE OR ALTER FUNCTION is not currently supported; use DROP+CREATE",
		"ALTER TRIGGER"+tttSeparator+"ALTER TRIGGER is not currently supported; use DROP+CREATE",
		"CREATE OR ALTER TRIGGER"+tttSeparator+"CREATE OR ALTER TRIGGER is not currently supported; use DROP+CREATE",
		"ALTER DATABASE"+tttSeparator+"ALTER DATABASE is not currently supported",
		"Column attribute FILESTREAM"+tttSeparator+"The FILESTREAM attribute is not currently supported; use escape hatch \\\\\"sp_babelfish_configure 'escape_hatch_storage_options', 'ignore' [, 'server']\\\\\" to ignore and proceed",
		"Column attribute SPARSE"+tttSeparator+"The SPARSE attribute is not currently supported; use escape hatch \\\\\"sp_babelfish_configure 'escape_hatch_storage_options', 'ignore' [, 'server']\\\\\" to ignore and proceed",
		"Column attribute ROWGUIDCOL"+tttSeparator+"The ROWGUIDCOL attribute is not currently supported; use escape hatch \\\\\"sp_babelfish_configure 'escape_hatch_storage_options', 'ignore' [, 'server']\\\\\" to ignore and proceed",
		"ALTER TABLE..ADD multiple"+tttSeparator+"ALTER TABLE currently supports only a single action item; split multiple actions items into separate ALTER TABLE statements (the -rewrite option handles this for you)",
		"ALTER TABLE..CHECK CONSTRAINT"+tttSeparator+"Enabling/disabling FK or CHECK constraints is not currently supported; constraints are always enabled",
		"ALTER TABLE..NOCHECK CONSTRAINT"+tttSeparator+"Enabling/disabling FK or CHECK constraints is not currently supported; constraints are always enabled",
		"DBCC "+tttSeparator+"DBCC statements are not currently supported. Use PostgreSQL mechanisms for DBA- or troubleshooting tasks",
		CompassAnalyze.ODBCScalarFunction+tttSeparator+"ODBC scalar functions are not currently supported; rewrite with an equivalent built-in function (some cases can be handled automatically with the -rewrite option)",
		CompassAnalyze.ODBCLiterals+tttSeparator+"ODBC literal expressions are not currently supported; rewrite with CAST() to the desired datatype (some cases can be handled automatically with the -rewrite option)",
		CompassAnalyze.Traceflags+tttSeparator+"Traceflags are not currently supported. Use PostgreSQL mechanisms for DBA- or troubleshooting tasks",
		CompassAnalyze.RollupCubeOldSyntax+tttSeparator+"Deprecated WITH CUBE/ROLLUP syntax is not currently supported; rewrite as GROUP BY CUBE/ROLLUP",
		CompassAnalyze.GroupByAll+tttSeparator+"Deprecated GROUP BY ALL syntax is not currently supported; rewrite query",
		"DATABASE_DEFAULT,"+tttSeparator+"Many collations are supported, but the concept of a default collation on database level is currently not available",
		"Catalog reference "+tttSeparator+"This SQL Server catalog is not currently supported",
		"@@DBTS"+tttSeparator+"The database timestamp mechanism (with also the TIMESTAMP/ROWVERSION datatype) is not currently supported",
		"@@PROCID"+tttSeparator+"Rewrite as OBJECT_ID('object-name')",
		"HIERARCHYID"+tttSeparator+"The HIERARCHYID datatype is not supported",
		CompassAnalyze.TimestampColumnSolo+tttSeparator+"Declaring a TIMESTAMP column without a column name is not currently supported; declare as 'TIMESTAMP TIMESTAMP'",
		"TIMESTAMP "+tttSeparator+"The TIMESTAMP (=ROWVERSION) datatype is not currently supported",
		"ROWVERSION "+tttSeparator+"The ROWVERSION (=TIMESTAMP) datatype is not currently supported",
		"GEOGRAPHY "+tttSeparator+"The GEOGRAPHY datatype is not supported; consider using the PG PostGIS extension",
		"GEOMETRY "+tttSeparator+"The GEOMETRY datatype is not supported; consider using the PG PostGIS extension",
		"Spatial method "+tttSeparator+"GEOGRAPHY/GEOMETRY datatypes are not supported; consider using the PG PostGIS extension",
		CompassAnalyze.AtAtErrorValueRef+tttSeparator+"The application explicitly references the @@ERROR value shown here, but this particular SQL Server error code is not currently supported by Babelfish. Rewrite manually to check for the PostgreSQL error code",
		CompassAnalyze.VarDeclareAtAt+tttSeparator+"Local variables or parameters starting with '@@' can be declared, but cannot currently be referenced",
		"Cursor option "+tttSeparator+"Currently only static, read-only, read-next-only cursors are supported",
		"FETCH  "+tttSeparator+"Currently only static, read-only, read-next-only cursors are supported",
		"CURSOR variable"+tttSeparator+"CURSOR-types variables/parameters are not currently supported; rewrite with table variables or #tmp tables",
		"CURSOR procedure parameter"+tttSeparator+"CURSOR-types variables/parameters are not currently supported; rewrite with table variables or #tmp tables",
		"GLOBAL cursor"+tttSeparator+"Currently only LOCAL cursors are supported",
		"GLOBAL option for FETCH"+tttSeparator+"Currently only LOCAL cursors are supported",
		"ALTER TABLE..DISABLE TRIGGER"+tttSeparator+"Disabling triggers is not currently supported; triggers are always enabled",		
		"DISABLE TRIGGER"+tttSeparator+"Disabling triggers is not currently supported; triggers are always enabled",		
		"ALTER TABLE..ENABLE TRIGGER"+tttSeparator+"Enabling triggers is not currently supported; triggers are always enabled",		
		"ENABLE TRIGGER"+tttSeparator+"Enabling triggers is not currently supported; triggers are always enabled",		
		"CREATE TRIGGER, INSTEAD OF"+tttSeparator+"INSTEAD-OF triggers are not currently supported. Rewrite as FOR trigger",		
		"CREATE TRIGGER (DDL"+tttSeparator+"DDL triggers are not currently supported",
		CompassAnalyze.TriggerSchemaName+tttSeparator+"CREATE TRIGGER schemaname.triggername is not currently supported; Remove 'schemaname'",
		"\\w+, WHERE CURRENT OF"+tttSeparator+"Updatable cursors are not currently supported. Rewrite the application to use direct UPDATE/DELETE",
		"UPDATE STATISTICS"+tttSeparator+"UPDATE STATISTICS is not currently supported; use PG's ANALYZE instead",
		"UPDATE("+tttSeparator+"UPDATE(): detecting in a trigger which column is updated by the triggering DML, is not currently supported",
		"COLUMNS_UPDATED("+tttSeparator+"COLUMNS_UPDATED(): detecting in a trigger which column is updated by the triggering DML, is not currently supported",
		"EVENTDATA("+tttSeparator+"EVENTDATA(): detecting the triggering event for a DDL trigger is not currently supported",
		"Function call, scalar, in computed column"+tttSeparator+"Calling a scalar SQL function in a computed column, is not currently supported; consider implementing with triggers",
		"XML(xmlschema)"+tttSeparator+"XML declarations with an XML schema are not currently supported",
		"XML.value()"+tttSeparator+"XML .value() method is not currently supported",
		"XML.nodes()"+tttSeparator+"XML .nodes() method is not currently supported",
		"XML.modify()"+tttSeparator+"XML .modify() method is not currently supported",
		"XML.exist()"+tttSeparator+"XML .exist() method is not currently supported",
		"XML.query()"+tttSeparator+"XML .query() method is not currently supported",
		"XML.write()"+tttSeparator+"XML .write() method is not currently supported",
		"\\w+ XML SCHEMA COLLECTION"+tttSeparator+"XML SCHEMA objects are not currently supported",
		"\\w+ XML INDEX"+tttSeparator+"XML indexes are not currently supported",
		"WITH XMLNAMESPACES"+tttSeparator+"XML namespaces are not currently supported",
		"SELECT FOR XML AUTO"+tttSeparator+"SELECT FOR XML AUTO is not currently supported; SELECT FOR XML RAW/PATH are supported",
		"SELECT FOR XML EXPLICIT"+tttSeparator+"SELECT FOR XML EXPLICIT is not currently supported; SELECT FOR XML RAW/PATH are supported",
		"SELECT FOR XML RAW ELEMENTS"+tttSeparator+"SELECT FOR XML RAW, with ELEMENTS is not currently supported; SELECT FOR XML RAW without ELEMENTS is supported",
		"SELECT FOR XML PATH ELEMENTS"+tttSeparator+"SELECT FOR XML PATH, with ELEMENTS is not currently supported; SELECT FOR XML PATH without ELEMENTS is supported",
		"EXECUTE procedure sp_xml_"+tttSeparator+"This XML-related system stored procedure is not currently supported",
		"OPENXML("+tttSeparator+"OPENXML() is not currently supported",
		"OPENQUERY("+tttSeparator+"OPENQUERY() is not currently supported",
		"OPENDATASOURCE("+tttSeparator+"OPENDATASOURCE() is not currently supported",
		"OPENROWSET("+tttSeparator+"OPENROWSET() is not currently supported",
		"CHANGETABLE("+tttSeparator+"Change tracking is not currently supported",
		"CHANGE_TRACKING_MIN_VALID_VERSION"+tttSeparator+"Change tracking is not currently supported",
		"CHANGE_TRACKING_CURRENT_VERSION"+tttSeparator+"Change tracking is not currently supported",
		"CHANGE_TRACKING_IS_COLUMN_IN_MASK"+tttSeparator+"Change tracking is not currently supported",
		"CHANGE_TRACKING_CONTEXT"+tttSeparator+"Change tracking is not currently supported",
		"PREDICT("+tttSeparator+"PREDICT() is not currently supported",
		"OPENJSON("+tttSeparator+"JSON-related functionality is not currently supported",
		"ISJSON("+tttSeparator+"JSON-related functionality is not currently supported",
		"JSON_\\w+\\("+tttSeparator+"JSON-related functionality is not currently supported",
		"SELECT FOR JSON"+tttSeparator+"SELECT FOR JSON is not currently supported",
		CompassAnalyze.ReadText+tttSeparator+"READTEXT/WRITETEXT/UPDATETEXT are not currently supported",
		CompassAnalyze.WriteText+tttSeparator+"READTEXT/WRITETEXT/UPDATETEXT are not currently supported",
		CompassAnalyze.UpdateText+tttSeparator+"READTEXT/WRITETEXT/UPDATETEXT are not currently supported",
		"INSERT..EXECUTE(string)"+tttSeparator+"INSERT..EXECUTE with EXECUTE-immediate is not currently supported",
		"INSERT..EXECUTE sp_executesql"+tttSeparator+"INSERT..EXECUTE with sp_executesql is not currently supported",
		"INSERT..DEFAULT VALUES"+tttSeparator+"INSERT..DEFAULT VALUES: this syntax is not currently supported. Rewrite manually as an INSERT with actual values",
		"INSERT TOP..SELECT"+tttSeparator+"Rewrite as INSERT.. SELECT TOP",
		CompassAnalyze.InsertBulkStmt+tttSeparator+"INSERT BULK is not a T-SQL statement, but only available through specific client-server APIs",
		CompassAnalyze.BulkInsertStmt+tttSeparator+"BULK INSERT is not currently supported. Use a different method to load data from a file, for example PostgreSQL's COPY statement",
		CompassAnalyze.DMLTableSrcFmt+tttSeparator+"A DML statement with OUTPUT clause, as source of an INSERT-SELECT, is not currently supported. Rewrite with OUTPUT into a temp table or table variable, and INSERT-SELECT from that",
		CompassAnalyze.VarAggrAcrossRowsFmt+tttSeparator+"In SQL Server, an assignment of a variable where the same variable also occurs in the assigned expression, may carry over its value for every qualifying row, thus creating a kind of aggregation. This is not currently supported in Babelfish. Verify if more than 1 row may qualify and if so, rewrite the query manually",
		CompassAnalyze.VarAssignDependency+tttSeparator+"Assignment of a variable depending on another variable which is itself assigned in the same statement, may produce unexpected results since the order of assignment is not guaranteed. Rewrite the query manually",
		CompassAnalyze.SelectPivot+tttSeparator+"SELECT..PIVOT is not currently supported. Rewrite manually",
		CompassAnalyze.SelectUnpivot+tttSeparator+"SELECT..UNPIVOT is not currently supported. Rewrite manually",
		"SELECT TOP WITH TIES"+tttSeparator+"SELECT TOP WITH TIES is not currently supported. Rewrite manually",
		"SELECT TOP <number> PERCENT"+tttSeparator+"Only TOP 100 PERCENT is currently supported. Rewrite as TOP without PERCENT. Rewrite manually",
		"CROSS APPLY"+tttSeparator+"CROSS APPLY: lateral joins are not currently supported. Rewrite manually",
		"OUTER APPLY"+tttSeparator+"OUTER APPLY: lateral joins are not currently supported. Rewrite manually",
		"T-SQL Left Outer Join"+tttSeparator+"The (long-deprecated) T-SQL Outer Join syntax is not supported; rewrite with ANSI Outer Join syntax",
		"T-SQL Right Outer Join"+tttSeparator+"The (long-deprecated) T-SQL Outer Join syntax is not supported; rewrite with ANSI Outer Join syntax",
		"WAITFOR DELAY"+tttSeparator+"WAITFOR DELAY: Rewrite this as a call to pg_sleep, e.g. EXECUTE pg_sleep 60 (the -rewrite option handles this for you)",
		CompassAnalyze.SelectTopWoOrderBy+tttSeparator+"SELECT TOP without ORDER BY: without ORDER BY, the order of rows in the result is not guaranteed, and therefore the TOP n rows aren't either. Even though the order may still have been deterministic in SQL Server (for example, due to a clustered index), this cannot be relied on when migrating to Babelfish/PostgreSQL. Recommendation is to review these queries and in case it is possible that the result set has >1 row, add an ORDER BY before migrating to Babelfish",
		"Constraint PRIMARY KEY/UNIQUE, CLUSTERED,"+tttSeparator+"CLUSTERED constraints are not currently supported. The constraint will be created as if NONCLUSTERED was specified. Review all (implicit) assumptions about row ordering or performance due to existence of a CLUSTERED index",
		"Index, CLUSTERED,"+tttSeparator+"CLUSTERED indexes are not currently supported. The index will be created as if NONCLUSTERED was specified. Review all (implicit) assumptions about row ordering or performance due to existence of a CLUSTERED index",
		"Index, UNIQUE, CLUSTERED,"+tttSeparator+"CLUSTERED indexes are not currently supported. The index will be created as if NONCLUSTERED was specified. Review all (implicit) assumptions about row ordering or performance due to existence of a CLUSTERED index",
		"Inline index"+tttSeparator+"Inline indexes are not currently supported; create indexes separately with CREATE INDEX",
		"Indexed view "+tttSeparator+"Materialized views are not currently supported; consider implementing these via PostgreSQL",
		"CREATE TABLE ##"+tttSeparator+"Global temporary tables are not currently supported; unlike a regular #tmptable, a ##globaltmptable is accessible by all sessions, and is dropped automatically when the last session accessing the table disconnects",
		"CREATE TABLE (temporal)"+tttSeparator+"Temporal tables are not currently supported; not to be confused with temporary tables (#t), temporal tables -created with clause PERIOD FOR SYSTEM_TIME- contain the data contents history of a table over time",
		"ALTER TABLE..SET SYSTEM_VERSIONING"+tttSeparator+"Temporal tables are not currently supported; not to be confused with temporary tables (#t), temporal tables -created with clause PERIOD FOR SYSTEM_TIME- contain the data contents history of a table over time",
		CompassAnalyze.TableHint+tttSeparator+"Table hints are not currently supported by Babelfish; escape hatch \\\\\"sp_babelfish_configure 'escape_hatch_table_hints', 'ignore' [, 'server']\\\\\" will silently ignore table hints ('strict' will raise an error). Review the expected impact on concurrency or query plans in the original query and rewrite the query as needed",
		CompassAnalyze.JoinHint+tttSeparator+"Join hints are not currently supported by Babelfish; escape hatch \\\\\"sp_babelfish_configure 'escape_hatch_join_hints', 'ignore' [, 'server']\\\\\" will silently ignore join hints ('strict' will raise an error). Review the expected impact on query plans in the original query and rewrite the query as needed",
		CompassAnalyze.QueryHint+tttSeparator+"Query hints are not currently supported by Babelfish; escape hatch \\\\\"sp_babelfish_configure 'escape_hatch_query_hints', 'ignore' [, 'server']\\\\\" will silently ignore query hints ('strict' will raise an error). Review the expected impact on query plans in the original query and rewrite the query as needed",
		CompassAnalyze.NumericColNonNumDft+tttSeparator+"NUMERIC/DECIMAL table columns with a non-numeric column default still allow the table to be created in SQL Server but will raise an error only when the default is used; in Babelfish, the error is raised when the table is created. Remove the non-numeric default",
		CompassAnalyze.TableValueConstructor+tttSeparator+"Rewrite the VALUES() clause as SELECT statements and/or UNIONs",
		CompassAnalyze.MergeStmt+tttSeparator+"Rewrite MERGE as a series of INSERT/UPDATE/DELETE statements (the -rewrite option handles this for you)",
		CompassAnalyze.DynamicSQLEXECStringReview+tttSeparator+"Dynamic SQL with EXECUTE(string) is supported by Babelfish; however, the actual dynamically composed SQL statements cannot be analyzed in advance by this tool, so manual analysis is required",
		CompassAnalyze.DynamicSQLEXECSPReview+tttSeparator+"Dynamic SQL with sp_executesql is supported by Babelfish; however, the actual dynamically composed SQL statements cannot be analyzed in advance by this tool, so manual analysis is required",
		CompassAnalyze.FKrefDBname+tttSeparator+"Remove the database name from the referenced table. E.g. change: REFERENCES yourdb.dbo.yourtable(yourcol) to: REFERENCES dbo.yourtable(yourcol)",
		CompassAnalyze.CrossDbReference+tttSeparator+"Cross-database references with 3-part object names (e.g. SELECT * FROM yourdb.dbo.yourtable) are not currently supported, except for objects inside the current database",
		CompassAnalyze.RemoteObjectReference+tttSeparator+"Remote object references with 4-part object names (e.g. SELECT * FROM REMOTESRVR.somedb.dbo.sometable) are not currently supported",
		"EXECUTE proc;version"+tttSeparator+"Procedure versioning, whereby multiple identically named procedures are distinguished by a number (myproc;1 and myproc;2), is not currently supported",
		"CREATE PROCEDURE proc;version"+tttSeparator+"Procedure versioning, whereby multiple identically named procedures are distinguished by a number (myproc;1 and myproc;2), is not currently supported",
		"Number of procedure parameters"+tttSeparator+"More parameters than the PG maximum is not currently supported; rewrite the procedure to use less parameters",
		"Number of function parameters"+tttSeparator+"More parameters than the PG maximum is not currently supported; rewrite the function to use less parameters",
		CompassAnalyze.TransitionTableMultiDMLTrigFmt+tttSeparator+"Triggers for multiple trigger actions (e.g. FOR INSERT,UPDATE,DELETE) currently need to be split up into separate triggers for each action, in case the trigger body references the transition tables INSERTED or DELETED",
		"SET FMTONLY"+tttSeparator+"SET FMTONLY is ignored and has not effect, both for ON and OFF",
		"SET ANSI_PADDING OFF"+tttSeparator+"Currently, only the semantics of ANSI_PADDING=ON are supported. Use escape hatch \\\\\"sp_babelfish_configure 'escape_hatch_session_settings', 'ignore' [, 'server']\\\\\" to suppress the resulting error message",
		"SET ROWCOUNT"+tttSeparator+"Currently, only SET ROWCOUNT 0 is supported",
		"SET QUOTED_IDENTIFIER \\w+, before end of batch"+tttSeparator+"SET QUOTED_IDENTIFIER takes effect only at the start of the next batch in Babelfish; the SQL Server semantics where it applies to the next statement, is not currently supported",
		"SET DEADLOCK_PRIORITY"+tttSeparator+"Setting the deadlock victimization priority is not currently supported",
		"SET LOCK_TIMEOUT"+tttSeparator+"Setting the lock timeout is not currently supported",
		CompassAnalyze.SetXactIsolationLevel+tttSeparator+"This transaction isolation level is not currently supported, due to PostgreSQL\'s MVCC mechanism",
		
		CompassAnalyze.UniqueOnNullableCol+" with UNIQUE index "+tttSeparator+"SQL Server allows only one row with a NULL value in a column with a UNIQUE constraint/index. Because PostgreSQL allows multiple rows with NULL values in such a column, UNIQUE constraints/indexes on a single nullable column are not currently supported in Babelfish. Use escape hatch \\\\\"sp_babelfish_configure 'escape_hatch_unique_constraint', 'ignore' [, 'server']\\\\\" to override and create the table anyway",
		CompassAnalyze.UniqueOnNullableCol+" with UNIQUE constraint"+tttSeparator+"SQL Server allows only one row with a NULL value in a column with a UNIQUE constraint/index. Because PostgreSQL allows multiple rows with NULL values in such a column, UNIQUE constraints/indexes on a single nullable column are not currently supported in Babelfish. Use escape hatch \\\\\"sp_babelfish_configure 'escape_hatch_unique_constraint', 'ignore' [, 'server']\\\\\" to override and create the table anyway",
		CompassAnalyze.UniqueOnNullableCol+" with UNIQUE index, on multiple columns"+tttSeparator+"SQL Server allows only one row with a NULL value in a column with a UNIQUE constraint/index. Because PostgreSQL allows multiple rows with  multiple NULL values in such a column, UNIQUE constraints/indexes on multiple columns, including nullable columns, should be reviewed, even though these are currently not blocked in Babelfish. Use escape hatch \\\\\"sp_babelfish_configure 'escape_hatch_unique_constraint', 'ignore' [, 'server']\\\\\" to override and create the table anyway",
		CompassAnalyze.UniqueOnNullableCol+" with UNIQUE constraint, on multiple columns"+tttSeparator+"SQL Server allows only one row with a NULL value in a column with a UNIQUE constraint/index. Because PostgreSQL allows multiple rows with  multiple NULL values, UNIQUE constraints/indexes on multiple columns, including nullable columns, should be reviewed, even though these are currently not blocked in Babelfish. Use escape hatch \\\\\"sp_babelfish_configure 'escape_hatch_unique_constraint', 'ignore' [, 'server']\\\\\" to override and create the table anyway",
		
		CompassAnalyze.DropIndex+" index ON schema.table"+tttSeparator+"Syntax 'DROP INDEX indexname ON schema.table' is not currently supported; remove schema name",
		CompassAnalyze.DropIndex+" table.index"+tttSeparator+"Syntax 'DROP INDEX table.indexname' is not currently supported; use 'DROP INDEX indexname ON tablename'",
		CompassAnalyze.DropIndex+" schema.table.index"+tttSeparator+"Syntax 'DROP INDEX schema.table.indexname' is not currently supported; use 'DROP INDEX indexname ON tablename'",
		
		"Partitioning, CREATE "+tttSeparator+"Table/index partitioning is not currently supported.",
		"$PARTITION.function"+tttSeparator+"Table/index partitioning is not currently supported.",
		"ALTER TABLE..SWITCH"+tttSeparator+"Table/index partitioning is not currently supported.",
		"\\w+ PARTITION"+tttSeparator+"Table/index partitioning is not currently supported.",
		
		"CREATE DEFAULT"+tttSeparator+"DEFAULT objects are not currently supported; use column defaults instead",
		"CREATE RULE"+tttSeparator+"RULE objects are not currently supported; use CHECK constraints instead",
		
		"Special column name IDENTITYCOL"+tttSeparator+"The special column name IDENTITYCOL is not currently supported. Replace it by the actual name of the idebtity column",		
		CompassAnalyze.LeadingDotsId+tttSeparator+"Remove leading dots for identifiers, i.e. change 'SELECT * FROM ..mytable' to 'SELECT * FROM mytable'",		
		CompassAnalyze.SpecialCharsIdentifier+tttSeparator+"Some characters are not currently supported in identifiers; go to the cross-reference section to find the specific case",		
		CompassAnalyze.SpecialCharsParameter+tttSeparator+"Some characters are not currently supported in parameter declarations; go to the cross-reference section to find the specific case",		
		"EXECUTE AS"+tttSeparator+"The EXECUTE AS statement (not to be confused with the EXECUTE AS clause in CREATE PROCEDURE/FUNCTION/etc.) is not currently supported",		
		"EXECUTE procedure sp_addextendedproperty"+tttSeparator+"System stored procedure sp_addextendedproperty is not currently supported; this is most often used to create metadata comments (e.g. COMMENT ON in PostgreSQL) and does not otherwise affect SQL functionality",		
		"REVERT"+tttSeparator+"The REVERT statement is not currently supported",		
		
		"\\w+, option WITH EXECUTE AS CALLER"+tttSeparator+"The clause WITH EXECUTE AS CALLER for procedures, functions and triggers maps to SECURITY INVOKER in PostgreSQL. It affects only permissions in Babelfish; the name resolution aspect (as in SQL Server) does not apply in Babelfish/PostgreSQL",
		"\\w+, option WITH EXECUTE AS OWNER"+tttSeparator+"The clause WITH EXECUTE AS CALLER for procedures, functions and triggers maps to SECURITY DEFINER in PostgreSQL. It affects only permissions in Babelfish; the name resolution aspect (as in SQL Server) does not apply in Babelfish/PostgreSQL",
		"\\w+, option WITH EXECUTE AS SELF"+tttSeparator+"The clause WITH EXECUTE AS SELF for procedures, functions and triggers is not currently supported",
		"\\w+, option WITH EXECUTE AS USER"+tttSeparator+"The clause WITH EXECUTE AS <user> for procedures, functions and triggers is not currently supported",
		"Index exceeds \\d+ columns"+tttSeparator+"For the maximum number of columns per index, 'included' columns do not count in SQL Server, but they do count in PostgreSQL",
		"DROP \\w+, >1 object"+tttSeparator+"Use a separate DROP statement for each object to be dropped",
		"CREATE FUNCTION, \\w+( \\w+)?, atomic"+tttSeparator+"Atomic natively compiled functions are not currently supported; rewrite as a regular SQL functions",		
		"CREATE FUNCTION, \\w+( \\w+)?, external"+tttSeparator+"External functions are not currently supported; rewrite as a regular SQL functions",		
		"CREATE FUNCTION, \\w+( \\w+)?, CLR"+tttSeparator+"CLR functions are not currently supported; rewrite as a regular SQL functions",		
		"CREATE \\w+, atomic"+tttSeparator+"Atomic natively compiled procedures/triggers are not currently supported; rewrite as a regular SQL object",		
		"CREATE \\w+, external"+tttSeparator+"External procedures/triggers are not currently supported; rewrite as a regular SQL object",		
		"CREATE \\w+, CLR"+tttSeparator+"CLR functions are not currently supported; rewrite as a regular SQL functions",		
		"\\w+, WITH SCHEMABINDING: created in PG as without SCHEMABINDING"+tttSeparator+"WITH SCHEMABINDING is not currently supported for procedures/functions/triggers; these will be created in PG as if SCHEMABINDING was not specified",		
		"\\w+, option WITH ENCRYPTION"+tttSeparator+"Encryption of the SQL source code of an object is not currently supported",		
		"\\w+, option WITH NATIVE_COMPILATION"+tttSeparator+"Native compilation is not currently supported; rewrite as a regular SQL object",		
		"View, without SCHEMABINDING"+tttSeparator+"PostgreSQL only supports views with the equivalent of WITH SCHEMABINDING, i.e. a table cannot be dropped if a view depends on it. A view without the SCHEMABINDING clause will still be created by Babelfish, but as if WITH SCHEMABINDING was specified",		
		"View, with CHECK OPTION"+tttSeparator+"WITH CHECK OPTION is nto currently supported for views; remove the option",
		"\\w+ MATERIALIZED VIEW"+tttSeparator+"Materialized views are not currently supported; consider implementing these via PostgreSQL",
		"\\w+ TRANSACTION not supported with PostgreSQL SECURITY DEFINER"+tttSeparator+"T-SQL objects created with EXECUTE AS OWNER are mapped to PostgreSQL SECURITY DEFINER; PostgreSQL does not support transaction mgmt statementds for objects created with SECURITY DEFINER",
		"\\w+ ASSEMBLY"+tttSeparator+"This object type is not currently supported",
		"\\w+ AGGREGATE"+tttSeparator+"This object type is not currently supported",
		"\\w+ EVENT SESSION"+tttSeparator+"This object type is not currently supported",
		"\\w+ EVENT NOTIFICATION"+tttSeparator+"This object type is not currently supported",
        "\\w+.*?, in computed column"+tttSeparator+"This feature may be supported by itself, but is not currently supported when used in a computed column"		
	);

	// emoji to indicate popup info is available
  	static String hintIcon      = "&#x1F6C8;";  // information symbol: (i)
  	static String hintIconMac   = "&#x2139;";  // information symbol: i  -- x1F6C8 is not rendered correctly on some Macs
  	static String hintIconLinux = "&#x2139;";  // information symbol: i  -- x1F6C8 is not rendered correctly on some Macs
	
	// alternate emojis; use COMPASS_HINT_ICON how to specify your own favorite,i.e. SET COMPASS_HINT_ICON=1F4A1
  	//String hintIcon = "&#x2754;";   // white question mark
  	//String hintIcon = "&#x1F4A1;";  // light bulb
  	//String hintIcon = "&#x1F6C8;";  // information symbol: (i)
  	//String hintIcon = "&#x2606;";   // white star
  	//String hintIcon = "&#8505;";    // blue information symbol [i]
  	//String hintIcon = "&#10145;";   // right arrow
  	//String hintIcon =   "&#9651;";  // white triangle


	public String psqlImportFileName = "pg_import";
	public String psqlFileSuffix = "psql";
	public String psqlImportTableName = "BBFCompass";
	public String psqlImportFilePlaceholder    = "BBF_PSQLIMPORTFILEPLACEHOLDER";
	public String psqlImportTablePlaceholder   = "BBF_PSQLIMPORTTABLEPLACEHOLDER";
	public String psqlImportSQLCrTb   =
"DROP TABLE IF EXISTS "+psqlImportTablePlaceholder+";\n"+
"CREATE TABLE "+psqlImportTablePlaceholder+"(\n"+
"	babelfish_version VARCHAR(20) NOT NULL, -- Babelfish version for which analysis was performed\n"+
"	date_imported TIMESTAMP NOT NULL,       -- date/time of running -pgimport\n"+
"	item VARCHAR(200) NOT NULL,             -- line item as shown in the report\n"+
"	itemDetail VARCHAR(200) NOT NULL,       -- additional info for a line item\n"+
"	reportGroup VARCHAR(50) NOT NULL,       -- report group as show in the report\n"+
"	status VARCHAR(20) NOT NULL,            -- classification of the item, e.g. SUPPORTED, NOTSUPPORTED, etc.\n"+
"	lineNr INT NOT NULL,                    -- line number of the item in the T-SQL batch\n"+
"	appName VARCHAR(100) NOT NULL,          -- application name \n"+
"	srcFile VARCHAR(200) NOT NULL,          -- SQL source file name\n"+
"	batchNrinFile INT NOT NULL,             -- batch no. of T-SQL batch in SQL source file\n"+
"	batchLineInFile INT NOT NULL,           -- line number in file of start of batch\n"+
"	context VARCHAR(200) NOT NULL,          -- name of object, or 'T-SQL batch'\n"+
"	subcontext VARCHAR(200) NOT NULL,       -- (optional) name of table in object \n"+
"	misc VARCHAR(20) NOT NULL               -- not for customer use; please ignore\n"+
");\n";

	public String psqlImportSQLCopy =
"\\COPY "+psqlImportTablePlaceholder+" FROM '"+psqlImportFilePlaceholder+"'  WITH DELIMITER ';' ;\n"+
"\n"+
"-- restore any delimiter characters occurring in actual identifiers:\n"+
"UPDATE "+psqlImportTablePlaceholder+" SET\n"+
"item        = REPLACE(item, '"+captureFileSeparatorMarker+"', '"+captureFileSeparator+"'),\n"+
"itemDetail  = REPLACE(itemDetail, '"+captureFileSeparatorMarker+"', '"+captureFileSeparator+"'),\n"+
"reportGroup = REPLACE(reportGroup, '"+captureFileSeparatorMarker+"', '"+captureFileSeparator+"'),\n"+
"context     = REPLACE(context, '"+captureFileSeparatorMarker+"', '"+captureFileSeparator+"'),\n"+
"subcontext  = REPLACE(subcontext, '"+captureFileSeparatorMarker+"', '"+captureFileSeparator+"')\n"+
";\n"+
"SELECT count(*) AS total_rows_in_table FROM "+psqlImportTablePlaceholder+";\n"+
"\n"
;


	// capture file
	public boolean echoCapture = false;	// development only
	public boolean configOnly = false;	// development only
	public String captureFilePathName;
	public BufferedWriter captureFileWriter;
	public static final String symTabSeparator = ";";
	public static final char metricsLineChar1 = '*';
	public static final String metricsLineTag = "metrics";
	public static final String captureFileSeparator = ";";
	public int capPosItem = 0;
	public int capPosItemDetail = 1;
	public int capPosItemGroup = 2;
	public int capPosStatus = 3;
	public int capPosLineNr = 4;
	public int capPosappName = 5;
	public int capPosSrcFile = 6;
	public int capPosBatchNr = 7;
	public int capPosLineNrInFile = 8;
	public int capPosContext = 9;
	public int capPosSubContext = 10;
	public int capPosMisc = 11;
	public int capPosLastField = 11 + 2;   // last field in a capture record; used to perform check on data read; +2 is for the 0 start index plus the extra field at the end

	// first line in capture file:
	public final String captureFileLinePart1 = "# Captured items for report ";
	public final String captureFileLinePart2 = " with targeted "+babelfishProg+ "version ";
	public final String captureFileLinePart3 = " generated at ";
	public final String captureFileLinePart4 = " with capture file format ";

	// report generation
	public String reportName = uninitialized;
	public final String sortKeySeparator = "  ~~~";
	public final String lastItem = "~ZZZZZZ~LastItem";
	public static boolean reportShowAppName = true;
	public static boolean reportShowSrcFile = true;
	public static boolean reportAppsCount = true;
	public static String reportShowBatchNr = "";
	public static String reportOptionXref = "";
	public static String reportOptionStatus = "";
	public static String reportOptionApps = "";
	public static String reportOptionDetail = "";
	public static String reportOptionFilter = "";
	public static boolean reportOptionNotabs = false;
	public static boolean reportOptionLineNrs = false;
	public static int linesSQLInReport = 0;
	public static String reportHdrLines = "";
	public static int maxLineNrsInListDefault = 10;
	public static int maxLineNrsInList = maxLineNrsInListDefault;
	public final static String reportInputFileFmt = "input file";
	public static boolean linkInNewTab = true;
	public static String tgtBlank = " target=\"_blank\"";
	public static boolean showPercentage = false;

	// adjust ordering of groups in report - used to prefix alphabetically sorted sortkey. Default prefix = 000
	private static Integer groupSortLength = 3;
	private Map<String, Integer> reportGroupSortAdjustment = new HashMap<>();

	// to save space during sorting
	private Map<String, String> srcFileMap = new HashMap<>();
	private Map<String, String> srcFileMapIx = new HashMap<>();

	// caching
	Map<String, String> stripDelimiterCache = new HashMap<>();
	int stripDelimitedIdentifierCall = 0;
	int stripDelimitedIdentifierCached = 0;

	Map<String, String> normalizeNameCache = new HashMap<>();
	int normalizeNameCall = 0;
	int normalizeNameCached = 0;

	// first line in import file:
	public final String importFileLinePart1 = "# Input file ";
	public final String importFileLinePart2 = " for application ";
	public final String importFileLinePart3 = " encoding ";
	public final String importFileLinePart4 = " batches/lines ";
	public final String importFileNrBatchesPlaceholder = "nrBatchesGoesHere";
	public final String importFileNrLinesPlaceholder   = "nrLinesGoesHere";
	public final String importFileLinePart5 = " read at ";

	// context
	public String currentSrcFile="";
	public String currentAppName="";
	public String currentDatabase="";
	public final String BatchContext = "T-SQL batch";
	public final String BatchContextLastSort = "ZZZZT-SQL batch";
	public String currentObjectType= "";
	public String currentObjectName= "";
	public String currentObjectTypeSub= "";
	public String currentObjectNameSub= "";
	public String currentObjectAttributes= "";

	// counters
	public int lineNrInFile;
	public int batchNrInFile;
	public int linesInBatch;
	public int constructsFound = 0;

	// rudimentary symbol table, only for some very basic things needed
	// there's a lot of room for improvement here
	static Map<String, String> objTypeSymTab = new HashMap<>();
	static Map<String, String> UDDSymTab = new HashMap<>();
	static Map<String, String> SUDFSymTab = new HashMap<>();
	static Map<String, String> TUDFSymTab = new HashMap<>();
	static Map<String, String> colSymTab = new HashMap<>();

	//XML methods
	static final List<String> XMLmethods = Arrays.asList("EXIST", "MODIFY", "QUERY", "VALUE", "NODES");
	static Map<String, String> SUDFNamesLikeXML = new HashMap<>();
	static Map<String, String> TUDFNamesLikeXML = new HashMap<>();

	//HIERARCHYID methods
	static final List<String> HIERARCHYIDmethodsFmt = Arrays.asList("GetAncestor", "GetDescendant", "GetLevel", "IsDescendantOf", "read", "GetReparentedValue", "ToString", "GetRoot", "Parse");  // Write cannot occur in SQL code
	static List<String> HIERARCHYIDmethods = new ArrayList<>();
	static Map<String, String> SUDFNamesLikeHIERARCHYID = new HashMap<>();

	// masking chars in identifiers
	public static final String BBFMark            = "BBF_";
	public static final String BBFSeparatorMask   = BBFMark + "SEPARATOR~MASK~BBF";
	public static final String BBFSeparatorMaskLastResort = BBFMark + "SEPARATOR~MASK~LAST~RESORT~BBF";
	public static final String BBFEncodedMark     = BBFMark + "ENCODED_";
	public static final String BBFSqBracketOpen   = BBFEncodedMark + "SQBRACKETOPEN";
	public static final String BBFSqBracketClose  = BBFEncodedMark + "SQBRACKETCLOSE";
	public static final String BBFDot             = BBFEncodedMark + "DOT";
	public static final String BBFDollar          = BBFEncodedMark + "DOLLAR";
	public static final String BBFHash            = BBFEncodedMark + "HASH";
	public static final String captureFileSeparatorMarker = BBFMark + "SEPARATOR_MARKER_" + BBFMark;

	// datatype groups
	public static final String BBFTypeMark        = BBFMark + "DATATYPE_";
	public static final String BBFStringType      = BBFTypeMark + "STRING";
	public static final String BBFNumericType     = BBFTypeMark + "NUMERIC";   // include money
	public static final String BBFDateTimeType    = BBFTypeMark + "DATETIME";
	public static final String BBFBinaryType      = BBFTypeMark + "BINARY";
	public static final String BBFNullType        = BBFTypeMark + "NULL";
	public static final String BBFUnknownType     = BBFTypeMark + "UNKNOWN";

	// input validation
	static final List<String> OnOffOption = Arrays.asList("ON", "OFF");

	// debug flags
	public boolean dbgTimestamp = true;
	public final HashSet<String> dbgOptions = new HashSet<>(Arrays.asList("all", "batch", "ptree", "cfg", "dir", "symtab", "report", "calc", "os", "fmt", "rewrite"));
	public final HashSet<String> specifiedDbgOptions = new HashSet<>(dbgOptions.size());
	public boolean debugBatch;
	public boolean debugPtree;
	public boolean debugCfg;
	public boolean debugDir;
	public boolean debugOS;
	public boolean debugSymtab;
	public boolean debugReport;
	public boolean debugCalc;
	public boolean debugFmt;	
	public boolean debugRewrite;	
	public boolean debugging;
	public int debugSpecial = 0;

	// classification values for SQL features
	public static final String Supported         = "SUPPORTED";
	public static final String NotSupported      = "NOTSUPPORTED";
	public static final String ReviewSemantics   = "REVIEWSEMANTICS";
	public static final String ReviewPerformance = "REVIEWPERFORMANCE";
	public static final String ReviewManually    = "REVIEWMANUALLY";
	public static final String Ignored           = "IGNORED";
	public static final String ObjCountOnly      = "OBJECTCOUNTONLY";
	public static final String Rewritten         = "REWRITTEN";

	// TODO Convert these lists in sets for efficiency
	public static List<String> supportOptions        = Arrays.asList(Supported,    NotSupported,    ReviewSemantics,    ReviewPerformance,    ReviewManually,    Ignored, ObjCountOnly, Rewritten);
	// values for default_classification in .cfg file:
	public static List<String> supportOptionsCfgFile = Arrays.asList("Supported", "NotSupported",  "ReviewSemantics",  "ReviewPerformance",  "ReviewManually",  "Ignored", ObjCountOnly, Rewritten);
	public static List<String> validSupportOptionsCfgFileOrig = Arrays.asList("NotSupported",  "ReviewSemantics",  "ReviewPerformance",  "ReviewManually", "Ignored");
	public static List<String> validSupportOptionsCfgFile = new ArrayList<>();
	// keys for default_classification in .cfg file, e.g. '-ReviewSemantics':
	public static List<String> defaultClassificationsKeysOrig = Arrays.asList("default_classification-ReviewSemantics", "default_classification-ReviewPerformance",
			"default_classification-ReviewManually", "default_classification-Ignored", "default_classification");
	public static List<String> defaultClassificationsKeys = new ArrayList<>();
	// keys for overriding default_classification in user .cfg file, e.g. '-ReviewSemantics':
	public static List<String> overrideClassificationsKeysOrig = Arrays.asList("default_classification-ReviewSemantics", "default_classification-ReviewPerformance",
			"default_classification-ReviewManually", "default_classification-Ignored", "default_classification");
	public static List<String> overrideClassificationsKeys = new ArrayList<>();

	// display values
	public static List<String> supportOptionsDisplay = Arrays.asList("Supported", "Not Supported", "Review Semantics", "Review Performance", "Review Manually", "Ignored", ObjCountOnly, "Rewritten by Babelfish Compass");

	// iteration order for report
	public static List<String> supportOptionsIterate = Arrays.asList(NotSupported, ReviewManually, ReviewSemantics, ReviewPerformance, Ignored, Supported);

	// default weight factors for computing compatibility %age, corresponding to each option value in the list above
	// NB: this is not used
	public static List<Integer> supportOptionsWeightDefault = Arrays.asList(100,   // Supported
	                                                                 200,   // NotSupported
	                                                                 150,    // ReviewSemantics
	                                                                 150,    // ReviewPerformance
	                                                                 150,    // ReviewManually
	                                                                 0,     // Ignored
	                                                                 0,     // ObjCountOnly, not applicable
	                                                                 100    // Rewritten
	                                                                );
	public final String WeightedStr = "Weighted";

	// user-defined weight factors
	public Map<String, Integer> userWeightFactor = new HashMap<>();

	// overall compatibility %age
	public String compatPctStr = uninitialized;
	public String compatPctStrRaw = uninitialized;

	// for recording overrides
	public static final String overrideSeparator = ";;";
	public Map<String, Integer> statusOverrides       = new HashMap<>();
	public Map<String, Integer> statusOverridesDetail = new HashMap<>();
	public Map<String, Integer> groupOverrides        = new HashMap<>();
	public Map<String, Integer> groupOverridesDetail  = new HashMap<>();
	
	// for SQL rewrites
	public static boolean rewrite = false;  // main switch
	public static final String rewriteBlankLine = "BBF_REWRITE_BLANKLINE";	
			
	// text rewrites that need to be applied
	public static List<String>        rewriteTextListKeys = new ArrayList<>();	
	public static Map<String,String>  rewriteTextList     = new HashMap<>();	
	public static Map<String,String>  rewriteTextListOrigText = new HashMap<>();	
	public static Map<Integer, Map<String, List<Integer>>> rewriteIDDetails = new HashMap<>();
	public static Map<String,Integer> rewrittenOppties = new HashMap<>();	
	public static String rewriteNotes = uninitialized;	
	public static Map<String,Integer> rewriteOppties = new HashMap<>();	
	public static final String        rewriteOpptiesTotal = "totalcount";
	public static final String        SQLcodeRewrittenText = "SQL code sections rewritten by " + thisProgName+": ";
	public static final String rwrTag = " /*REWRITTEN*/ ";
	public static String rwrTabRegex = "";
	public static Integer nrRewritesDone = 0;
	public static Integer nrMergeRewrites = 0;
	public static String rewriteTypeExpr1 = "expr(1)";
	public static String rewriteTypeExpr2 = "expr(2)";
	public static String rewriteTypeReplace = "replace";	
	public static String rewriteTypeODBCfunc1 = "ODBCfunc1";	
	public static String rewriteTypeODBClit1 = "ODBClit1";	
	public static String rewriteTypeBlockReplace = "BlockReplace";	

	// added lines/columns: list index=iteration#; map key = line#; subkey = col# on line; value = #chars added at (line,col)	
	public static List<Map<Integer, Map<Integer, Integer>>> offsetCols = new ArrayList<>();
	public static Map<String, Map<Integer, Integer>> offsetLines = new HashMap<>();
	public static final Integer calcOffsetIterationMax = 999999;

	// SQL rewrites performed	
	public static List<String> rewritesDone = new ArrayList<>();

	// flags
	public static boolean devOptions = false;
	public static boolean caching = false;

	// formatting
	final String identifierChars = "\\w\\@\\#\\$";
	final String varPattern      = "\\@[\\w\\@\\#\\$]+";
	final String hexPattern      = "0X[0-9A-F]*";

	// Create pattern of escaped special regex characters
	final String regexSpecialChars = "<>()[]{}\\^$!?|*+-=.";
	final String regexSpecialCharsEscaped = regexSpecialChars.replaceAll(".", "\\\\$0");  // escape each character
	final Pattern regexSpecialCharsPatt = Pattern.compile("[" + regexSpecialCharsEscaped + "]");

	// indicates current analysis pass; there are 2 passes
	int analysisPass = 0;

	// grammar rule names
	public final static String startRuleName = "tsql_file";
	public static String[] grammarRuleNames;

	public enum MatchMethod {
		MATCHES,
		LOOKING_AT,
		FIND
	}

	private static final CompassUtilities instance = new CompassUtilities();

	private CompassUtilities() {}

	public static CompassUtilities getInstance() {
		initValues();
		return instance;
	}

    private static void initValues () {
    	HIERARCHYIDmethods = new ArrayList<>(HIERARCHYIDmethodsFmt);
    	listToUpperCase(HIERARCHYIDmethods);
    	
	   	validSupportOptionsCfgFile = new ArrayList<>(validSupportOptionsCfgFileOrig);
    	listToUpperCase(validSupportOptionsCfgFile);
    	
	   	defaultClassificationsKeys = new ArrayList<>(defaultClassificationsKeysOrig);
    	listToUpperCase(defaultClassificationsKeys);
	   	
	   	overrideClassificationsKeys = new ArrayList<>(overrideClassificationsKeysOrig);
    	listToUpperCase(overrideClassificationsKeys);   
    	
    	listToLowerCase(importFormatOption);    
    	
    	if (rewrite) {	
    		supportOptionsIterate = Arrays.asList(NotSupported, ReviewManually, ReviewSemantics, ReviewPerformance, Ignored, Rewritten, Supported);
    	} 	    	
    }

	/**
	 * Sets the operating system executable name, reports folder name based on osName.
	 * Optionally turns on the developer options flag. Optionally sets the hint icon value for reports. Optionally turns
	 * on the visibility of percent complete.
	 * @param osName the value of System.getProperty("os.name")
	 * @throws NullPointerException if osName is null
	 */
	public void setPlatformAndOptions(String osName) {
		osName = osName.toLowerCase();
		
		if (osName.startsWith("windows")) {
			onWindows = true;
			onPlatform  = "Windows";
			thisProgExec = thisProgExecWindows;
			BabelfishCompassFolderName = BabelfishCompassFolderNameWindows;
		}
		else if (osName.startsWith("mac os x")) {			
			onMac = true;
			onPlatform  = "MacOS";
			thisProgExec = thisProgExecMac;
			BabelfishCompassFolderName = BabelfishCompassFolderNameMac;
			hintIcon = hintIconMac;
		}
		else {
			// assume Linux
			onLinux = true;
			onPlatform  = "Linux";
			thisProgExec = thisProgExecLinux;
			BabelfishCompassFolderName = BabelfishCompassFolderNameLinux;
			hintIcon = hintIconLinux;
		}

		if (System.getenv().containsKey("COMPASS_DEVELOP") || System.getenv().containsKey("compass_develop")) {
			devOptions = true;
		}
		
		if (System.getenv().containsKey("COMPASS_HINT_ICON") || System.getenv().containsKey("compass_hint_icon")) {
			// you can get an arbitrary emoji as icon when you specify it in envvar COMPASS_HINT_ICON
			String iconEnv = System.getenv().get("COMPASS_HINT_ICON");
			if (getPatternGroup(iconEnv, "^((\\#)?(x)?[0-9A-F]{4,}(;)?)$", 1).isEmpty()) {
				appOutput("COMPASS_HINT_ICON: invalid value ["+iconEnv+"]. Must be '#x<hex>' or '#<number>', denoting a Unicode Emoji."); 
			}
			else {
				if (!iconEnv.startsWith("#")) iconEnv = "#" + iconEnv;
				if (!iconEnv.endsWith(";")) iconEnv += ";";
				hintIcon = "&" + iconEnv;
			}
		}	
		
		if (System.getenv().containsKey("COMPASS_COMPAT_PERCENTAGE") || System.getenv().containsKey("compass_compat_percentage")) {
			showPercentage = true;
		}
					
    }

 	// for debugging, and for launching the window with the final report
    public void runOScmd (String cmd) throws IOException {
    	ProcessBuilder builder;
    	if (debugging) dbgOutput(thisProc() + "onWindows=["+onWindows+"] onMac=["+onMac+"] onLinux=["+onLinux+"] cmd=["+cmd+"]  ", debugOS);
    	if (onWindows) {
	        builder = new ProcessBuilder("cmd.exe", "/c", cmd );
	    }
	    else {
	    	// Mac, Linux
	        builder = new ProcessBuilder("bash", "-c", cmd );
	    }
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
        String cmdOutput;
        while (true) {
            cmdOutput = r.readLine();
            if (cmdOutput == null) { break; }
            appOutput(cmdOutput);
        }
    }

	public String escapeRegexChars(String s)
	{
	    Matcher m = regexSpecialCharsPatt.matcher(s);
	    return m.replaceAll("\\\\$0");
	}

	public static String getPatternGroup(String s, String patt, int groupNr)
	{
		return getPatternGroup(s, patt, groupNr, "");
	}

	public static String getPatternGroup(String s, String patt, int groupNr, String options)
	{
		Pattern p;
		int flags = 0;
		if (options.contains("case_sensitive")) {
			flags = 0;
		}
		else {
			flags = flags | Pattern.CASE_INSENSITIVE;
		}
		if (options.contains("multiline")) {
			flags = flags | Pattern.MULTILINE | Pattern.DOTALL;
		}
		p = Pattern.compile(patt, flags);
				
		return getPatternGroup(s, p, groupNr, MatchMethod.FIND);
	}

	public static String getPatternGroup(String s, Pattern p, int groupNr, MatchMethod matchMethod)
	{
		String grpStr = "";
		Matcher m = p.matcher(s);
		if ( (matchMethod == MatchMethod.LOOKING_AT && m.lookingAt()) ||
			 (matchMethod == MatchMethod.FIND && m.find()) ||
			 (matchMethod == MatchMethod.MATCHES && m.matches()) ) {

			grpStr = m.group(groupNr);
		}
		return grpStr;
	}

	public boolean PatternMatches(String s, String patt)
	{
		return Pattern.matches(patt, s);
	}

	public Matcher getMatcher(String s, String patt)
	{
		return getMatcher(s, patt, "");
	}

	public Matcher getMatcher(String s, String patt, String options)
	{
		if (options.contains("case_sensitive")) {
			return Pattern.compile(patt).matcher(s);
		}
		return Pattern.compile(patt, Pattern.CASE_INSENSITIVE).matcher(s);
	}

	public String applyPattern(String s, String patt, String replace, String options)
	{
		Pattern p;
		int flags = 0;
		if (options.contains("case_sensitive")) {
			flags = 0;
		}
		else {
			flags = flags | Pattern.CASE_INSENSITIVE;
		}
		if (options.contains("multiline")) {
			flags = flags | Pattern.MULTILINE | Pattern.DOTALL;
		}
		
		p = Pattern.compile(patt, flags);
		
    	Matcher m = p.matcher(s);
    	if (options.contains("first"))
    		s = m.replaceFirst(replace);
    	else
    		s = m.replaceAll(replace);
		return s;
	}

	public String applyPatternFirst(String s, String patt, String replace) {
		return applyPattern(s, patt, replace, "first");
	}

	public String applyPatternFirst(String s, String patt, String replace, String options) {
		return applyPattern(s, patt, replace, "first "+options);
	}

	public String applyPatternAll(String s, String patt, String replace) {
		return applyPattern(s, patt, replace, "");
	}

	public String applyPatternAll(String s, String patt, String replace, String options) {
		return applyPattern(s, patt, replace, options);
	}

	public StringBuilder applyPatternSB(StringBuilder s, String patt, String replace, String options)
	{
		return new StringBuilder(applyPattern(s.toString(), patt, replace, options));
	}

	public StringBuilder applyPatternSBFirst(StringBuilder s, String patt, String replace) {
		return applyPatternSB(s, patt, replace, "first");
	}

	public StringBuilder applyPatternSBAll(StringBuilder s, String patt, String replace) {
		return applyPatternSB(s, patt, replace, "");
	}

	public static String stringRepeat(String s, int n) {
		StringBuilder str = new StringBuilder();
		for (int j = 0; j < n; ++j) {
			str.append(s);
		}
		return str.toString();
	}
	public static String removeLastChar(String s) {
	    return removeLastChars(s, 1);
	}

	public static String removeLastChars(String s, int nrChars) {
		String shortened = null;
		if (s != null) {
			if (nrChars > s.length()) {
				shortened = "";
			} else if (nrChars < 0) {
				shortened = s;
			} else {
				shortened = s.substring(0, s.length() - nrChars);
			}
		}
		return shortened;
	}

	// remove the enclosing quotes from a string constant
	public static String stripStringQuotes(String s) {
		if (s.isEmpty()) return s;
		if ((s.charAt(0) == '\'') && (s.charAt(s.length()-1) == '\'')) {
			s = s.substring(1,s.length()-1);
		}
		else if ((s.charAt(0) == '"') && (s.charAt(s.length()-1) == '"')) {
			s = s.substring(1,s.length()-1);
		}
		else if ((s.toUpperCase().startsWith("N'")) && (s.charAt(s.length()-1) == '\'')) {
			s = s.substring(2,s.length()-1);
		}
		return s;
	}
	
	// remove enclosing brackets from an expression - assuming cases like ((a)+(b)) do not occur 
	public String stripEnclosingBrackets(String s) {
		if (s.trim().isEmpty()) return s;
		while (true) {
			if ((s.charAt(0) == '(') && (s.charAt(s.length()-1) == ')')) {
				s = s.substring(1,s.length()-1);
			}
			else {
				break;
			}
		}
		return s;
	}
	
	public static String capitalizeFirstChar(String s) {
		String capitalized = null;
		if (s != null) {
			if (s.length() == 0) {
				capitalized = s;
			} else {
				capitalized = s.substring(0, 1).toUpperCase() + s.substring(1);
			}
		}
		return capitalized;
	}
	
	public String capitalizeInitChar(String s) {
		if (s.length() == 0) return s;
		StringTokenizer sTok = new StringTokenizer(s);
		StringBuilder sNew= new StringBuilder();
		while(sTok.hasMoreTokens()){
	        sNew.append(capitalizeFirstChar(sTok.nextToken())).append(" ");
		}
	    return removeLastChar(sNew.toString());
	}

	public void listTrim(List<String> thisList) {
		if (thisList != null) {
			for (int i = 0; i < thisList.size(); i++) {
				String str = thisList.get(i);
				thisList.set(i, str != null ? collapseWhitespace(str) : null);
			}
		}
	}

	public static void listToUpperCase(List<String> thisList) {
		if (thisList != null) {
			for (int i = 0; i < thisList.size(); i++) {
				String str = thisList.get(i);
				thisList.set(i, str != null ? str.toUpperCase() : null);
			}
		}
	}

	public static void listToLowerCase(List<String> thisList) {
		if (thisList != null) {
			for (int i = 0; i < thisList.size(); i++) {
				String str = thisList.get(i);
				thisList.set(i, str != null ? str.toLowerCase() : null);
			}
		}
	}

	public static String reverseString(String s) {
		if (s != null && s.length() > 1) {
			s = new StringBuilder(s).reverse().toString();
			String tmp = "~~!~=tmp=String=Babelfish~!~~";  // should not occur in actual data
			s = s.replaceAll("\\(", tmp);
			s = s.replaceAll("\\)", "(");
			s = s.replaceAll(tmp, ")");
		}
		return s;
	}
	
	public String calcMD5(String s) {
		MessageDigest md5 = null;
		try {
		md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {  }
		md5.reset();
		md5.update(s.getBytes());
		byte[] hash = md5.digest();
		BigInteger bigInt = new BigInteger(1,hash);
		String hashText = bigInt.toString(16);
		while(hashText.length() < 32) {
		  hashText = "0"+hashText;
		}		
		return hashText;
	}
		
	// align lines on the specified delimiter
	public String alignColumn(StringBuilder s, String alignStr, String alignBA, String alignLR) {
		return alignColumn(s.toString(), alignStr, alignBA, alignLR);
	}
	public String alignColumn(String s, String alignStr, String alignBA, String alignLR) {
		List<String> tmpLines = new ArrayList<>(Arrays.asList(s.split("\n")));
		int maxPos=-1;
		for (String value : tmpLines) {
			if (alignBA.equals("before")) {
				int ix = value.indexOf(alignStr);
				maxPos = Math.max(ix, maxPos);
			}
			else {  // align on first word after delimiter, typically, this is a number(count)
				int ix = value.indexOf(alignStr) + alignStr.length();
				String w = getPatternGroup(value.substring(ix), "^(\\w+)\\b", 1);
				maxPos = Math.max(w.length(), maxPos);
			}
		}

		StringBuilder sAligned = new StringBuilder();
		for (String value : tmpLines) {
			int ix = value.indexOf(alignStr);
			if (alignBA.equals("before")) {
				if (alignLR.equals("right")) {
					sAligned.append(stringRepeat(" ", (maxPos - ix))).append(value).append("\n");
				}
				else {
					sAligned.append(value.substring(0,ix)).append(stringRepeat(" ", (maxPos - ix))).append(value.substring(ix)).append("\n");
				}
			}
			else {  // after
				if (alignLR.equals("right")) {
					ix += alignStr.length();
					String w = getPatternGroup(value.substring(ix), "^(\\w+)\\b", 1);
					sAligned.append(value.substring(0,ix)).append(stringRepeat(" ", (maxPos - w.length()))).append(value.substring(ix)).append("\n");
				}
				else {
					sAligned.append(value).append("\n");
				}
			}
		}
		return sAligned.toString();
	}

	// align datatypes with a length specifier so that sorting results in the numeric order
	public String alignDataTypeLength(String s) {
		s = alignDataTypeLength(s, "CHAR");
		s = alignDataTypeLength(s, "NCHAR");
		s = alignDataTypeLength(s, "VARCHAR");
		s = alignDataTypeLength(s, "NVARCHAR");
		s = alignDataTypeLength(s, "BINARY");
		s = alignDataTypeLength(s, "VARBINARY");
		s = alignDataTypeLength(s, "NUMERIC");
		s = alignDataTypeLength(s, "DECIMAL");
		s = alignDataTypeLength(s, "IDENTITY");
		return s;
	}
	public String alignDataTypeLength(String s, String type) {
		String placeholder = "~~!!##BBF_PLACEHOLDER##!!~~";
		while (true) {
			List<String> tmpLines = new ArrayList<>(Arrays.asList(s.split("\n")));
			int startLine=-1;
			int endLine=-1;
			String prefix="";
			int lineCnt = 0;
			int maxLen = -1;
			for (String line : tmpLines) {
				lineCnt++;
				String p = getPatternGroup(line, "^(.*?\\b"+type+"\\()\\d+\\b", 1);
				if (!p.isEmpty()) {
					if (startLine == -1) {
						startLine = lineCnt;
						prefix = p;
					}
				}
				if (startLine > 0) {
					if (prefix.equals(p)) {
						// continue until end of range found
						endLine = lineCnt;
						String n = getPatternGroup(line.substring(prefix.length()), "^(\\d+)\\b", 1);
						int nLen = n.length();
						if (nLen > maxLen) maxLen = nLen;
					}
					else {
						break;
					}
				}
			}

			if (maxLen== -1) break;

			StringBuilder sAligned = new StringBuilder();
			StringBuilder tmp = new StringBuilder();
			List<String> typeLines = new ArrayList<>();
			lineCnt = 0;
			for (String line : tmpLines) {
				lineCnt++;
				if (lineCnt < startLine) {
					sAligned.append(line).append("\n");
					continue;
				}
				if (lineCnt > endLine) {
					tmp.append(line).append("\n");
					continue;
				}
				String n = getPatternGroup(line.substring(prefix.length()), "^(\\d+)\\b", 1);
				int nLen = n.length();
				String newLine = removeLastChar(prefix) + placeholder + stringRepeat(" ", (maxLen-nLen)) + n + line.substring(prefix.length()+nLen);
				typeLines.add(newLine+"\n");
			}
			List<String> sortedLines = typeLines.stream().sorted().collect(Collectors.toList());
			sAligned.append(String.join("", sortedLines)).append(tmp);
			s = sAligned.toString();
		}
		s = s.replaceAll(placeholder, "(");
		return s;
	}

	public String composeSeparatorBar(String s) {
		return composeSeparatorBar(s, "", true);
	}
	public String composeSeparatorBar(String s, String tag) {
		return composeSeparatorBar(s, tag, true);
	}
	public String composeSeparatorBar(String s, String tag, boolean generateTocLink) {
		String filler = "-";
		String tagHTML = "";
		tagHTML = "<a name=\""+tag.toLowerCase()+"\"></a>";
		String s2 = tagHTML;
		s2 += composeOutputLine("", "-") + "\n";
		s2 += composeOutputLine("--- "+s+" ", "-") + "\n";
		s2 += composeOutputLine("", "-") + "\n";
		if (generateTocLink) {
			// s2 += tocLinkIcon + " " + tocLinkURL + "\n";   // dos not look good
			s2 += tocLinkURL + "\n";
		}
		return s2;
	}

	public String composeOutputLine(String s, String filler, int lineLen) {
		if (filler.isEmpty()) filler = " ";
		s = s + stringRepeat(filler, (lineLen - s.length()));
		return s;
	}
	public String composeOutputLine(String s) {
		return composeOutputLine(s, " ");
	}
	public String composeOutputLine(String s, String filler) {
		return composeOutputLine(s, filler, reportLineLength);
	}

	public void appOutput(StringBuilder s) {
		appOutput(s.toString(), false, false);
	}
	public void appOutput(String s) {
		appOutput(s, false, false);
	}
	public void appOutput(String s, boolean inReport) {
		appOutput(s, inReport, false);
	}
	public void appOutput(String s, boolean inReport, boolean noNewline) {
		if (noNewline) {
			System.out.print(s);
		}
		else {
			System.out.println(s);
		}
		if (sessionLogWriter != null) {
			try { writeSessionLogFile(s + "\n"); } catch (Exception e) { System.out.println("Error writing to "+ sessionLogPathName); }
		}
		if (inReport) {
			try { writeReportFile(s); } catch (Exception e) { System.out.println("Error writing to "+ reportFileTextPathName); }
		}
	}

	public void setDebugFlags()
	{
		debugging = true;
		if (specifiedDbgOptions.contains("batch") || specifiedDbgOptions.contains("all")) {
			debugBatch = true;
		}
		if (specifiedDbgOptions.contains("ptree") || specifiedDbgOptions.contains("all")) {
			debugPtree = true;
		}
		if (specifiedDbgOptions.contains("cfg") || specifiedDbgOptions.contains("all")) {
			debugCfg = true;
		}
		if (specifiedDbgOptions.contains("dir") || specifiedDbgOptions.contains("all")) {
			debugDir = true;
		}
		if (specifiedDbgOptions.contains("os") || specifiedDbgOptions.contains("all")) {
			debugOS = true;
		}
		if (specifiedDbgOptions.contains("symtab") || specifiedDbgOptions.contains("all")) {
			debugSymtab = true;
		}
		if (specifiedDbgOptions.contains("report") || specifiedDbgOptions.contains("all")) {
			debugReport = true;
		}
		if (specifiedDbgOptions.contains("calc") || specifiedDbgOptions.contains("all")) {
			debugCalc = true;
		}
		if (specifiedDbgOptions.contains("fmt") || specifiedDbgOptions.contains("all")) {
			debugFmt = true;
		}
		if (specifiedDbgOptions.contains("rewrite") || specifiedDbgOptions.contains("all")) {
			debugRewrite = true;
		}
	}

	public void dbgOutput(String s, boolean toDebug) {
		if (toDebug) {
			String ts = "";
			if (dbgTimestamp) ts = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date()) + " ";
			System.out.println(ts + "DEBUG: " + s);
			if (sessionLogWriter != null) {
				try { writeSessionLogFile(ts + "DEBUG: " + s +"\n"); } catch (Exception e) { System.out.println("Error writing to "+ sessionLogPathName); }
			}
		}
	}

	public String dbgString(String s) {
		StringBuilder str = new StringBuilder();
		for (char ch: s.toCharArray()) {
			str.append(" x");
			str.append(String.format("%02x", (int) ch));
		}
		return str.toString();
	}

	public static String thisProc() {
		StackTraceElement traceElement = new Throwable().getStackTrace()[1];
		return traceElement.getMethodName() + ":" + traceElement.getLineNumber() + ": ";
	}

	public void printStackTrace() {
		Thread.dumpStack();
	}

	public void errorExitStackTrace() {
		errorExit(0, true);
	}
	public void errorExitStackTrace(int errNo) {
		errorExit(errNo, true);
	}
	public void errorExit() {
		errorExit(0, false);
	}
	public void errorExit(int errNo) {
		errorExit(errNo, false);
	}
	public void errorExit(int errNo, boolean stackTrace) {
		if (stackTrace) printStackTrace();
		System.exit(errNo);
	}

    public String collapseWhitespace(String s) {
        return s.replaceAll("\\s+", " ").trim();
    }

	public String maskChar(String s, String toBeMaskedChar) {
		if (s.contains(BBFSeparatorMask)) {
			// should not happen, but who knows...
			appOutput("Identifiers should not contain the string '"+BBFSeparatorMask+"'. Proceeding, but no guarantees...");
			s = s.replaceAll(BBFSeparatorMask, BBFSeparatorMaskLastResort);
		}
		return s.replaceAll(toBeMaskedChar, BBFSeparatorMask);
	}

	public String unmaskChar(String s, String toBeMaskedChar) {
		s = s.replaceAll(BBFSeparatorMaskLastResort, BBFSeparatorMask);
		return s.replaceAll(BBFSeparatorMask, toBeMaskedChar);
	}

	public String fixNameChars(String nameType, String name) {
		String result = name;
		// remove chars that cause problems in filenames
		result = applyPatternAll(result, fileNameCharsAllowed, "-");
		result = applyPatternAll(result, "[\\-]+", "-");
		return result;
	}
	
	/*
	 * Checks for valid user entered command line arguments for Application Name or Report Name. Returns a error message
	 * if the entered string contains directory separators (slashes) or any not allowed characters.
	 * @param nameType one of "report" or "appname"
	 * @param name name of report or appname
	 * @return empty string if name is valid or error message if invalid
	 * @throws IllegalArgumentException if nameType does not equal "report" or "appname"
	 * @see CompassUtilities#fileNameCharsAllowed
	 */
	public static String nameFormatValid(String nameType, String name) {
		String result = "";
		if (!"report".equals(nameType) && !"appname".equals(nameType)) {
			throw new IllegalArgumentException("invalid nameType=[" + nameType + "]");
		}
		if (name == null || name.isEmpty() || name.matches("^\\s+$")) {
			result = "[empty name]";
		} else {
			// first check for directory separator slashes
			if (name.contains("\\")) {
				if (!name.contains("..\\")) {
					result = "'\\'";
				} else {
					result = "'..\\'";
				}
			} else if (name.contains("/")) {
				if (!name.contains("../")) {
					result = "'/'";
				} else {
					result = "'../'";
				}
			} else {
				// check for characters not allowed in report/app name
				String badChar = getPatternGroup(name, "(" + fileNameCharsAllowed + ")", 1);
				if (!badChar.isEmpty()) {
					result = "[" + badChar + "]  (allowed characters: [A-Za-z0-9\\.-()_])";
				}
			}
		}
		return result;
	}

	// doc dir root pathname: %USERPROFILE% on Windows, /home/<user> on Linux
    public String getDocDirPathname() {
		String dirPath = "";
		if (onWindows) {
			dirPath = System.getProperty("user.home") + File.separator + "Documents" + File.separator + BabelfishCompassFolderName;
		}
		else { 
			// Linux, Mac
			dirPath = System.getProperty("user.home") + File.separator + BabelfishCompassFolderName;
		}
		return dirPath;
	}

	// construct file pathname
    public String getFilePathname(String dirPath, String fileName) {
		return dirPath + File.separator + fileName;
	}

	// report dir pathname
    public String getReportDirPathname(String reportName) {
    	return getReportDirPathname(reportName, "", "");
    }
    public String getReportDirPathname(String reportName, String subDir) {
    	return getReportDirPathname(reportName, subDir, "");
    }
    public String getReportDirPathname(String reportName, String subDir, String subSubDir) {
		String dirPath = getDocDirPathname();
		dirPath = getFilePathname(dirPath, reportName);
		if (!subDir.isEmpty()) {
			dirPath += File.separator + subDir;
			if (!subSubDir.isEmpty()) {
				dirPath += File.separator + subSubDir;
			}
		}
		return dirPath;
	}

	// change filename suffix
	private String changeFilenameSuffix(String f, String oldSuffix, String newSuffix) {
		f = applyPatternFirst(f, "\\." + oldSuffix + "$", "." + newSuffix);
		return f;
	}

	// capture file pathname
    public String getCaptureFilePathname(String reportName, String inputFileName, String appName) {
		String f = Paths.get(inputFileName).getFileName().toString();
		String capFileName = captureFileName + "." + adjustFileName(f, appName, captureFileTag, captureFileSuffix);	
		String dirPath = getReportDirPathname(reportName, capDirName);
		capFileName = getFilePathname(dirPath, capFileName);
		return capFileName;		
	}

	// PG import file pathname
    public String getPGImportFilePathname(String reportName) {
		String f = PGImportFileName + "." + captureFileSuffix;
		String filePath = getFilePathname(getReportDirPathname(reportName, capDirName), f);
		return filePath;
	}

	// anon items file pathname
    public String getAnonymizedItemsPathname(String reportName) {
		String f = getAnonymizedItemsFilename;
		String filePath = getFilePathname(getReportDirPathname(reportName, capDirName), f);
		return filePath;
	}

	// session log pathname
    public String getSessionLogPathName(String reportName, Date now) {
    	String now_fname = new SimpleDateFormat("yyyy-MMM-dd-HH.mm.ss").format(now);
    	String reportNamePart = reportName;
    	if (Compass.forceReportName) {
    		reportNamePart = Compass.reportFileName;
    	}
    	String sessionLogName = "session-log-" + reportNamePart +  "-" + "bbf." + targetBabelfishVersion + "-" + fixNameChars("report", now_fname) + "." + HTMLSuffix;
    	if (stdReport) { // development only
    		sessionLogName = "session-log" + "." + HTMLSuffix;
    	}
		return getFilePathname(getReportDirPathname(reportName, logDirName), sessionLogName);
	}

	// report file pathname
    public String getreportFilePathName(String reportName, Date now) {
    	String now_fname = new SimpleDateFormat("yyyy-MMM-dd-HH.mm.ss").format(now);
    	String reportNameFull = "report-" + reportName + "-" + "bbf." + targetBabelfishVersion + "-" + fixNameChars("report", now_fname) + "." + textSuffix;
    	if (stdReport) { // development only
    		reportNameFull = "report." + textSuffix;
    	}
    	else if (Compass.forceReportName) {
    		reportNameFull = Compass.reportFileName + "." + textSuffix;
    	}
		return getFilePathname(getReportDirPathname(reportName), reportNameFull);
	}

	public String getReportFileHTMLPathname(String reportName, Date now) {
		String f = getreportFilePathName(reportName, now);
		f = changeFilenameSuffix(f, textSuffix, HTMLSuffix);
		return f;
	}


	// check  dir exists, and create if not
    public void checkDir(String dirPath, boolean mustExist) {
    	checkDir(dirPath, mustExist, false);
    }
    public void checkDir(String dirPath, boolean mustExist, boolean echoCreate) {
		if (debugging) dbgOutput(thisProc() + "dirPath=[" + dirPath + "] mustExist=[" + mustExist + "] ", debugDir);
		File docDir = new File(dirPath);
		if (docDir.exists()) {
			if (debugging) dbgOutput(thisProc() + docDir + " already exists", debugDir);
			return;
		}
		else {
			if (mustExist) {
				appOutput("Directory " + docDir + " should exist, but not found. Continuing...");
				checkDir(dirPath, false, true);
			}
			else {
				// create the dir
				if (docDir.mkdirs()) {
					if (debugging) dbgOutput("Created " + dirPath, debugDir);
					if (echoCreate) {
						appOutput("Creating " + dirPath);
					}
				}
				else {
					// sometimes this fails for no apparent reason; unclear why. Wait a little while and retry
					try {
						Thread.sleep(500); /* argument=millisecs */
						//appOutput(thisProc()+"waiting a little while for creating "+dirPath+" ");
					} catch (Exception e) {
					}
					if (docDir.mkdirs()) {
						if (debugging) dbgOutput("Created (after retry)" + dirPath, debugDir);
						if (echoCreate) {
							appOutput("Creating " + dirPath);
						}
					}
					else {
						appOutput("Error creating " + dirPath + "; please try again");
						//errorExitStackTrace();
					}
				}
			}
		}
	}

    public boolean deleteFile(String pathName) throws IOException {
		File f = new File(pathName);
		if (debugging) dbgOutput("Deleting  " + pathName, debugDir);
		if (!f.exists()) return true;
		return f.delete();
	}

    public List<Path> getFilesPattern(String dir, String filePattern) throws IOException {
    	Path dirPath = Paths.get(dir);
    	Stream<Path> files = Files.find(dirPath, 1,
             (path, basicFileAttributes) -> path.toFile().getName().matches(filePattern));
 		List<Path> fileList = files.collect(Collectors.toList());
 		fileList.remove(dirPath);
		return fileList;
    }

	public String getErrBatchFilePathName(String reportName, String inputFileName, String runStartTime) throws IOException {
		String f = Paths.get(inputFileName).getFileName().toString();
    	if (stdReport) { // development only
    		f += "." + errBatchFileSuffix;
    	}
    	else {
    		f += "-" + runStartTime + "." + errBatchFileSuffix;
    	}
		String dirPath = getReportDirPathname(reportName, errBatchDirName);
		f = getFilePathname(dirPath, f);
		return f;
	}

	public String getBatchFilePathName(String reportName, String inputFileName) throws IOException {
		String f = Paths.get(inputFileName).getFileName().toString();
		f += "." + batchFileSuffix;
		String dirPath = getReportDirPathname(reportName, batchDirName);
		f = getFilePathname(dirPath, f);
		return f;
	}

	public String getFileNameFromPathName(String inputPathName) throws IOException {
		String f = Paths.get(inputPathName).getFileName().toString();
		// strip off suffix
		f = applyPatternFirst(f, "^(.*)\\.\\w*$", "$1");
		return f;
	}

	public void copyFile(String fnameSrc, String fnameDest) throws IOException {
		File fsrc  = new File(fnameSrc);
		File fdest = new File(fnameDest);
    	Files.copy(fsrc.toPath(), fdest.toPath(), REPLACE_EXISTING);
	}

	public String openErrBatchFile(String reportName, String inputFileName, String runStartTime) throws IOException {
		if (inputFileName.contains(importFileTag)) {
			inputFileName = inputFileName.substring(0,inputFileName.indexOf(importFileTag)-1);
		}
		Path fullPath = Paths.get(inputFileName).toAbsolutePath();
		errBatchFilePathName = getErrBatchFilePathName(reportName, inputFileName, fixNameChars("report", runStartTime));

		checkDir(getReportDirPathname(reportName, errBatchDirName), false);
		errBatchFileWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(errBatchFilePathName), StandardCharsets.UTF_8)));

		String now = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());
		String initLine = "# Input file: "+fullPath.toString()+". This file generated at " + now +"\n";
		writeErrBatchFile(initLine);
		return errBatchFilePathName;
	}

	public void writeErrBatchFile(String line) throws IOException {
		errBatchFileWriter.write(line + "\n");
		errBatchFileWriter.flush();
	}

    public void closeErrBatchFile() throws IOException {
    	if (errBatchFileWriter == null) return;
	    errBatchFileWriter.close();
	    errBatchFileWriter = null;
	}


	public String openBatchFile(String reportName, String inputFileName) throws IOException {
		Path fullPath = Paths.get(inputFileName).toAbsolutePath();
		batchFilePathName = getBatchFilePathName(reportName, inputFileName);
		checkDir(getReportDirPathname(reportName, batchDirName), false);
		batchFileWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(batchFilePathName), StandardCharsets.UTF_8)));
		String now = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());
		String initLine = "# Input file: "+fullPath.toString()+". This file generated at " + now;
		writeBatchFile(initLine);
		return batchFilePathName;
	}

	public void deleteBatchFile(String reportName, String inputFileName) throws IOException {
		Path fullPath = Paths.get(inputFileName).toAbsolutePath();
		batchFilePathName = getBatchFilePathName(reportName, inputFileName);
		if (!deleteFile(batchFilePathName)) {
		    appOutput("Error deleting "+batchFilePathName);
		}
		return;
	}

	public void writeBatchFile(String line) throws IOException {
		if (batchFileWriter == null) {
			appOutput(line);
			return;
		}
		batchFileWriter.write(line + "\n");
		batchFileWriter.flush();
	}

    public void closeBatchFile() throws IOException {
    	if (batchFileWriter == null) return;
	    batchFileWriter.close();
	    batchFileWriter = null;
	}

	public String getSymTabFilePathName(String reportName, String inputFileName, String appName) throws IOException {
		String f = Paths.get(inputFileName).getFileName().toString();
		String symtabFileName = adjustFileName(f, appName, symTabFileTag, symTabFileSuffix);	
		String dirPath = getReportDirPathname(reportName, importDirName, symTabDirName);
		symtabFileName = getFilePathname(dirPath, symtabFileName);
		return symtabFileName;
	}

	public void writeSymTabFile(String line) throws IOException {
		if (debugging) dbgOutput("writing symtab: line=[" + line + "] ", debugSymtab);
		symTabFileLineCount++;
		symTabFileWriter.write(line + "\n");
		symTabFileWriter.flush();
	}

    public void closeSymTabFile() throws IOException {
		writeSymTabFile("# end of file; " +symTabFileLineCount+" records written");
	    symTabFileWriter.close();
	    symTabFileWriter = null;
	}

	public String adjustFileName(String fileName, String appName, String fileTag, String fileSuffix) {
		Integer maxFilenameLength = 260;
		if (!onWindows) maxFilenameLength = 255;
		
		// compose filename
		String fullFileName = fileName + "." + fileTag + "." + appName + "." + fileSuffix;	

		if (debugging) dbgOutput(thisProc()+ "fullFileName=["+fullFileName+"], ["+fullFileName.length()+"] ", debugDir);
		if (debugging) dbgOutput(thisProc()+ "fileName=["+fileName+"], ["+fileName.length()+"] ", debugDir);
		if (debugging) dbgOutput(thisProc()+ "appName=["+appName+"] , ["+appName.length()+"] ", debugDir);
		
		int fileTooLong = fullFileName.length() - (maxFilenameLength -20); // 20 to keep a bit of margin since not all generated filenames are equally long
		if (fileTooLong > 0) {			
			// only shortening filename at this time; will error out when path part is too long
			if (debugging) dbgOutput(thisProc()+"importFilename is too long, tooLong=["+fileTooLong+"] ", debugDir);
			
			// take the first 50 chars, and append the MD5 hash of the full name
			String md5Text = calcMD5(fileName);
			String f2 = fileName.substring(0,50);
			f2 += "_" + md5Text;
			if (debugging) dbgOutput(thisProc()+"MD5=["+md5Text+"] f2=["+f2+"] ", debugDir);
			
			fullFileName = f2 + "." + fileTag + "." ;
			if (fileName.toUpperCase().startsWith(appName.toUpperCase())) fullFileName += f2;
			else fullFileName += appName;
			fullFileName += "." + fileSuffix;
			
			if (debugging) dbgOutput("fileName=["+fullFileName+"], ["+fullFileName.length()+"] ", debugDir);			
		}			
		return fullFileName;	
	}

	public String getImportFilePathName(String reportName, String inputFileName, String appName) throws IOException {
		String f = Paths.get(inputFileName).getFileName().toString();		
		String importFileName = adjustFileName(f, appName, importFileTag, importFileSuffix);			
		String dirPath = getReportDirPathname(reportName, importDirName);
		importFileName = getFilePathname(dirPath, importFileName);
		return importFileName;
	}	
			
	// relevant only in case of a single file, and when doing only reporting
	public String getImportFilePathNameFromCaptured(String capFileName) throws IOException {
		String f = capFileName;
		f = applyPatternFirst(f, "^(.*?"+escapeRegexChars(File.separator) + ")" + capDirName + escapeRegexChars(File.separator) + captureFileName + "\\.(.*?)" + captureFileTag + "(.*)$", "$1"+ importDirName + escapeRegexChars(File.separator) + "$2"+ importFileTag + "$3");
		return f;
	}

	public String getImportFileHTMLPathName(String reportName, String inputFileName, String appName) throws IOException {
		String f = getImportFilePathName(reportName, inputFileName, appName);
		f = applyPatternFirst(f, "(" + escapeRegexChars(File.separator) + importDirName + escapeRegexChars(File.separator)+")", "$1" + importHTMLDirName + escapeRegexChars(File.separator));
		f = changeFilenameSuffix(f, importFileSuffix, HTMLSuffix);
		return f;
	}

	public void openImportFile(String reportName, String inputFileName, String appName, String encoding) throws IOException {	
		Path fullPath = Paths.get(inputFileName).toAbsolutePath();
		importFilePathName = getImportFilePathName(reportName, inputFileName, appName);
		importFileHTMLPathName = getImportFileHTMLPathName(reportName, inputFileName, appName);
		checkDir(getReportDirPathname(reportName, importDirName), true);
		if (debugging) dbgOutput("opening importFilePathName=["+importFilePathName+"] ", debugDir);
		importFileWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(importFilePathName), StandardCharsets.UTF_8)));
		String now = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());
		String initLine = importFileLinePart1 +"["+fullPath.toString()+"]"+importFileLinePart2+"["+appName+"]" + importFileLinePart3 +"["+encoding+"]" + importFileLinePart4 +"["+importFileNrBatchesPlaceholder+"/"+importFileNrLinesPlaceholder+"]" + importFileLinePart5 + now;
		writeImportFile(initLine, false);

		importFileWriteLineNr = 0;
		importFileHTMLWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(importFileHTMLPathName), StandardCharsets.UTF_8)));
		String hdr = headerHTML + headerHTMLSQL;
		hdr = formatHeaderHTML(hdr, now, reportName, inputFileName, appName, "Imported file");
		formatFooterHTML();
		importFileHTMLWriter.write(hdr);
		importFileHTMLWriter.flush();

		return;
	}
	
	public String formatToolTips(String hdr) {
		// generate tooltip CSS entries for tooltips
		String css = "";
		for (int i=0; i<toolTipsText.size(); i++) {
			String t = toolTipsText.get(i);
			if (t.isEmpty()) continue;
			List<String> fields = new ArrayList<String>(Arrays.asList(t.split(tttSeparator)));
			String item = fields.get(0).trim();
			String tooltipText = fields.get(1).trim();
			String key = makeItemHintKey(item);
			if (!tooltipText.endsWith(".")) tooltipText += ".";
			toolTipsKeys.put(key, item.toLowerCase());
			toolTipsKeysList.add(key);
			css += ".tooltip .tooltip-content[data-tooltip='"+key+"']::before { content: \""+tooltipText+"\"; }\n";
		}
		// put tooltips CSS lines in the HTML header
		hdr = applyPatternFirst(hdr, tooltipsHTMLPlaceholder, css);
		return hdr;
	}

	public String formatHeaderHTML(String hdr, String now, String reportName, String inputFileName, String appName, String inputfileTxt) {
		String hdr1 = "Generated by " + thisProgName + " at " + now;
		// fill in various parts in the HTML header
		hdr = applyPatternAll(hdr, headerHTMLPlaceholder, hdr1);
		//String title = "Report " + reportName + ", file " + inputFileName + ", application " + appName;
		hdr = applyPatternFirst(hdr, titleHTMLPlaceholder, inputFileName);
		hdr = applyPatternFirst(hdr, reportHTMLPlaceholder, reportName);
		hdr = applyPatternFirst(hdr, inputfileHTMLPlaceholder, inputFileName);
		hdr = applyPatternFirst(hdr, appnameHTMLPlaceholder, appName);
		hdr = applyPatternFirst(hdr, inputfileTxtPlaceholder, inputfileTxt);
		
		if (inputfileTxt.contains("Rewritten")) {			
			hdr = applyPatternFirst(hdr, tocHTMLPlaceholder, "<br>Note: see end of file (<a href=\"#"+anchorListOfRewrites+"\">here</a>) for list of rewritten sections.");
		}
		else {
			hdr = applyPatternFirst(hdr, tocHTMLPlaceholder, " ");
		}
		
		return hdr;
	}

	public void formatFooterHTML() {
		String ftr = "Generated by " + thisProgName;
		// fill in the HTML footer
		footerHTML = applyPatternFirst(footerHTML, footerHTMLPlaceholder, ftr);
	}

	public void writeImportFile(String line) throws IOException {
		writeImportFile(line, true);
	}
	public void writeImportFile(String line, boolean writeHTML) throws IOException {
		importFileWriter.write(line + "\n");
		importFileWriter.flush();
		if (writeHTML) {
			importFileWriteLineNr++;
			String lineEscaped = escapeHTMLChars(line);
			String lineHTML = "<tr><td class=\"linenr\"><a name=\""+importFileWriteLineNr+"\"></a>" +importFileWriteLineNr+ "</td><td class=\"sql\">" + lineEscaped + "</td></tr>";
			importFileHTMLWriter.write(lineHTML + "\n");
			importFileHTMLWriter.flush();
		}
	}

	public void openRewrittenFile(String reportName, String appName, String tmpFile, String rewrittenFile) throws IOException {		
		FileInputStream fis = new FileInputStream(tmpFile);
		InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);				
		rewrittenInFileReader = new BufferedReader(isr);	
		rewrittenFileWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(rewrittenFile), StandardCharsets.UTF_8)));
		String now = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());
		return;
	}
			
	public void writeRewrittenFile(String line) throws IOException {
		if (rewrittenFileWriter == null) {
			appOutput(line);
			return;
		}
		rewrittenFileWriter.write(line);
		rewrittenFileWriter.flush();	
	}
	
	
	public void writeRewrittenHTMLFile(String reportName, String appName, String rewrittenFile, String rewrittenHTMLFile) throws IOException {
		FileInputStream fis = new FileInputStream(rewrittenFile);
		InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);				
		BufferedReader rewrittenInFileReader = new BufferedReader(isr);	
		
		BufferedWriter rewrittenHTMLFileWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(rewrittenHTMLFile), StandardCharsets.UTF_8)));
		String hdr = headerHTML + headerHTMLSQL;
		String f = Paths.get(rewrittenFile).getFileName().toString();
		String now = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());
		f = renameRewrittenFile(appName,f) + " (in "+ escapeRegexChars(getReportDirPathname(reportName, rewrittenDirName)) +")";
		hdr = formatHeaderHTML(hdr, now, reportName, f, appName, "Rewritten file");
		formatFooterHTML();
		rewrittenHTMLFileWriter.write(hdr);
		rewrittenHTMLFileWriter.flush();

		int rewrittenFileWriteLineNr = 0;
		boolean tocFound = false;
		while (true) {							
			String line = rewrittenInFileReader.readLine();
			if (line == null) break;
			
			rewrittenFileWriteLineNr++;
			line = escapeHTMLChars(line);
			if (line.contains(SQLcodeRewrittenText)) {
				tocFound = true;
				line = line.replace(SQLcodeRewrittenText, "<a name=\""+anchorListOfRewrites+"\"></a>" + SQLcodeRewrittenText);
			}
			if (tocFound) {
				line = applyPatternFirst(line, "^("+lineIndent+")(line )(\\d+)\\b", "$1<a href=\"#$3\">$2$3</a>");		
			}
			String lineHTML = "<tr><td class=\"linenr\"><a name=\""+rewrittenFileWriteLineNr+"\"></a>" +rewrittenFileWriteLineNr+ "</td><td class=\"sql\">"+line+"</td></tr>\n";

			rewrittenHTMLFileWriter.write(lineHTML);
		}		
		
		rewrittenHTMLFileWriter.write(footerHTML);
		rewrittenHTMLFileWriter.flush();
   		rewrittenHTMLFileWriter.close();
   		rewrittenInFileReader.close();
	}
	
	public void closeRewrittenFile() throws IOException {	
		rewrittenInFileReader.close();							
		rewrittenFileWriter.close();			
	}	

	public String renameRewrittenFile(String appName, String rewrittenFile) {
		// internal name => rewritten file
		String f = Paths.get(rewrittenFile).getFileName().toString();
		String p = rewrittenFile.substring(0,(rewrittenFile.length() - f.length()));
		f = f.substring(0,f.indexOf(rewrittenFileTag)-1);  
		String suffix = "";
		if (f.indexOf(".") == -1) {
			suffix = "";
		}
		else {
			suffix = "." + f.substring(f.lastIndexOf(".")+1);
			f = removeLastChars(f, suffix.length());
		}
		String renamedFile = p + f + "." + rewrittenFileSuffix + suffix;
		return renamedFile;
	}

	public String renameRewrittenFile(String reportName, String appName, String inputFile) {
		// original input file => rewritten file
		String inFileCopy = "";
		try {
			inFileCopy = getImportFilePathName(reportName, inputFile, appName);
		} catch  (Exception e) { /* nothing */ }
		
		String fName = Paths.get(inFileCopy).getFileName().toString().replaceAll(importFileTag, rewrittenFileTag);
		String renamedFile = getFilePathname(getReportDirPathname(reportName, rewrittenDirName), fName); 
				
		renamedFile = renameRewrittenFile(appName, renamedFile);
		return renamedFile;
	}

	public String writePsqlFile(boolean append, String reportName, String cmd) throws IOException {
		String psqlImportFilePathNameRoot = getFilePathname(getReportDirPathname(reportName, capDirName), psqlImportFileName)+".";
		String psqlImportFilePathName = psqlImportFilePathNameRoot + psqlFileSuffix;
		BufferedWriter psqlImportFileWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(psqlImportFilePathName), StandardCharsets.UTF_8)));
		String f = PGImportFileName+"."+captureFileSuffix;
		String psqlText = psqlImportSQLCopy;
		if (!append) psqlText = psqlImportSQLCrTb + psqlText;
		psqlText = applyPatternAll(psqlText, psqlImportFilePlaceholder, f);
		psqlText = applyPatternAll(psqlText, psqlImportTablePlaceholder, psqlImportTableName);
		psqlImportFileWriter.write(psqlText);
		psqlImportFileWriter.flush();
		psqlImportFileWriter.close();

		String psqlCmdFileSuffix = "bat";
		if (onMac || onLinux) psqlCmdFileSuffix = "sh";
		String psqlImportCmdFilePathName = psqlImportFilePathNameRoot + psqlCmdFileSuffix;
		BufferedWriter psqlImportCmdFileWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(psqlImportCmdFilePathName), StandardCharsets.UTF_8)));
		String commentLine = "rem Importing captured items into PostgreSQL table '" + psqlImportTableName+ "'...\n@echo off\n";
		if (onMac || onLinux) commentLine = "#!/bin/bash\n# Importing captured items into PostgreSQL table '" + psqlImportTableName+ "'...\n";
		psqlImportCmdFileWriter.write(commentLine);
		psqlImportCmdFileWriter.write("cd " + getReportDirPathname(reportName, capDirName) + "\n");
		psqlImportCmdFileWriter.write(cmd + "\n");
		psqlImportCmdFileWriter.flush();
		psqlImportCmdFileWriter.close();

		return psqlImportCmdFilePathName;
	}

	// couldn't find readily available solution in standard Java; should maybe use apache.commons to escape HTML chars
	public String escapeHTMLChars(String line) {
		if (line.contains("&")) line = applyPatternAll(line, "&", "&amp;");
		if (line.contains("<")) line = applyPatternAll(line, "<", "&lt;");
		if (line.contains(">")) line = applyPatternAll(line, ">", "&gt;");
		if (line.contains("\"")) line = applyPatternAll(line, "\"", "&quot;");
		if (line.contains("'")) line = applyPatternAll(line, "\"", "&apos;");
		return line;
	}

	public String unEscapeHTMLChars(String line) {
		if (line.contains("&")) {
			line = applyPatternAll(line, "&amp;", "&");
			line = applyPatternAll(line, "&amp"+captureFileSeparatorMarker, "&");
			line = applyPatternAll(line, "&lt;", "<");
			line = applyPatternAll(line, "&lt"+captureFileSeparatorMarker, "<");
			line = applyPatternAll(line, "&gt;", ">");
			line = applyPatternAll(line, "&gt"+captureFileSeparatorMarker, ">");
			line = applyPatternAll(line, "&quot;", "\"");
			line = applyPatternAll(line, "&quot"+captureFileSeparatorMarker, "\"");
			line = applyPatternAll(line, "&apos;", "\"");
			line = applyPatternAll(line, "&apos"+captureFileSeparatorMarker, "\"");
			line = applyPatternAll(line, "&nbsp;", " ");
			line = applyPatternAll(line, "&nbsp"+captureFileSeparatorMarker, " ");
		}
		return line;
	}

    public void closeImportFile() throws IOException {
	    importFileWriter.close();
		if (importFileHTMLWriter != null) {
			importFileHTMLWriter.write(footerHTML);
			importFileHTMLWriter.flush();
	   		importFileHTMLWriter.close();
	   		importFileHTMLWriter = null;
	   	}
	}

	// get attribute from imported file's first line
    public String importFileAttribute(String line, int part) throws IOException {
    	assert (part >= 1 && part <= 5): "invalid part value ["+part+"] ";
    	return getPatternGroup(line, "^"+importFileLinePart1+"[\\[](.*?)[\\]]"+importFileLinePart2+"[\\[](.*?)[\\]]"+importFileLinePart3+"[\\[](.*?)[\\]]"+importFileLinePart4+"[\\[](.*?)[\\]]"+importFileLinePart5+"(.*)$", part);
    }

	// update the imported file's first line
    public void importFileUpdateBatchLines(String fileName, Integer nrBatches, Integer nrLines) throws IOException {
		RandomAccessFile f = new RandomAccessFile(fileName, "rw");
        long position = f.getFilePointer();
        String line = f.readLine();
        int inLen = line.length();
        line = line.replaceFirst(importFileNrBatchesPlaceholder,nrBatches.toString());
        line = line.replaceFirst(importFileNrLinesPlaceholder,nrLines.toString());
       	int modLen = line.length();
       	//patch up the line with the # characters we lost
       	line = line.substring(0,modLen) + stringRepeat(" ", (inLen-modLen));
       	f.seek(position);
       	f.writeBytes(line);
        f.close();
        return;
    }

	// read imported file's first line
    public String importFileFirstLine(String fileName) throws IOException {
		FileInputStream fis = new FileInputStream(fileName);
		InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
		BufferedReader inFileReader = new BufferedReader(isr);
		String line = inFileReader.readLine();   // read only first line
		inFileReader.close();
		return line;
    }

	// get list of all capture files for this report
    public List<Path> getCaptureFiles(String reportName) throws IOException {
		String dirPath = getReportDirPathname(reportName, capDirName);
		File captureDir = new File(dirPath);
		List<Path> captureFiles = new ArrayList<>();
		if (captureDir.exists()) {
			captureFiles = getFilesPattern(dirPath, captureFileName+"\\..+\\." + captureFileSuffix);
		}
		return captureFiles;
 	}

	// handle backward compatibility for the html subdir
	// we do a copy instead of a move to make sure any existing reports with hyperlinks keep working
    public void moveImportedHTMLFiles(String reportName) throws IOException {
    	checkDir(getReportDirPathname(reportName, importDirName, importHTMLDirName), false);
		String dirPath = getReportDirPathname(reportName, importDirName);
		File importDir = new File(dirPath);
		if (importDir.exists()) {
			List<Path> htmlFiles = getFilesPattern(dirPath, "^.+"+importFileTag+".+\\." + HTMLSuffix);			
			for (Path p : htmlFiles) {
				//Path fullPath = Paths.get(p).toAbsolutePath();
				String pFull = p.toAbsolutePath().toString();
				File fSrc  = new File(pFull);
				String f = Paths.get(pFull).getFileName().toString();
				String pDir = pFull.substring(0,pFull.length()-f.length());
				String pNew = pDir + importHTMLDirName + File.separator + f;
				File fDest = new File(pNew);
				Files.copy(fSrc.toPath(), fDest.toPath(), StandardCopyOption.REPLACE_EXISTING);			 	
			}
		}
 	}

	// validate all capture files for this report
	// if valid, returns an empty string
	// if invalid, return message why it's invalid
    public String captureFilesValid(String type, List<Path> captureFiles) throws IOException {
    	return captureFilesValid(type, captureFiles, false);
    }
    public String captureFilesValid(String type, List<Path> captureFiles, boolean export) throws IOException {
    	String result = "";
    	String errInfo = "";
    	String errInfoOtherwise = "";
    	String errInfoTargetVersion = "";
    	String errInfoFormatVersion = "";
    	boolean otherwiseInvalid = false;
    	String targetVersionTest = null;
    	boolean identicalTargetVersion = true;
    	String formatVersionTest = null;
   		boolean identicalFormatVersion = true;
		for (Path cf: captureFiles) {
			String line = captureFileFirstLine(cf.toString());   // read only first line
			String reportName     = captureFileAttribute(line, 1);
			String tgtVersion     = captureFileAttribute(line, 2);
			String dt             = captureFileAttribute(line, 3);
			String fmtVersion     = captureFileAttribute(line, 4);

			if (targetVersionTest == null) {
				targetVersionTest = tgtVersion;
			}
			if (!targetVersionTest.equals(tgtVersion)) {
				identicalTargetVersion = false;				
			}
			if (fmtVersion == null) {
				// capture files from Babelfish Compass 1.0 and 1.1 do not have the capture file format version yet (no version for 1.0)
				fmtVersion = captureFileFormatBaseVersion;
			}
			else if (fmtVersion.isEmpty()) {
				// capture files from Babelfish Compass 1.0 and 1.1 do not have the capture file format version yet (no version for 1.0)
				fmtVersion = captureFileFormatBaseVersion;
			}
			if (formatVersionTest == null) {
				formatVersionTest = fmtVersion;
			}
			if (!formatVersionTest.equals(fmtVersion)) {
				identicalFormatVersion = false;
			}

			errInfoTargetVersion += " - version "+tgtVersion+ " is target of report "+reportName+" ("+cf.toString()+")\n";
			errInfoFormatVersion += " - file format version "+fmtVersion+ " for report "+reportName+" ("+cf.toString()+")\n";

			if (tgtVersion.isEmpty()) {
				otherwiseInvalid = true;
				errInfoOtherwise += " - missing header line? Targeted "+babelfishProg+" version "+tgtVersion+ " not found in "+cf.toString()+"\n";
			}			
		}

		if (!export && !targetVersionTest.equals(targetBabelfishVersion)) {
			result = "Analysis was performed for a different "+babelfishProg+" version ("+targetVersionTest+") than targeted by this run (v."+targetBabelfishVersion+"):\n";
		}
		else if (!identicalTargetVersion) {
			result = "Analysis files are for different "+babelfishProg+" versions:\n";
			errInfo = errInfoTargetVersion;
		}
		else if (!identicalFormatVersion) {
			result = "Analysis files are for different file format versions:\n";
			errInfo = errInfoFormatVersion;
		}
		else if (otherwiseInvalid) {
			result = "Invalid analysis file(s) found:\n";
			errInfo = errInfoOtherwise;
		}
		if (!result.isEmpty()) {
			if (type.equals("report")) result = "\nCannot generate report based on these analysis files with incompatible attributes.\n" + result;
			else result = "\nCannot import analysis files with incompatible attributes.\n" + result;
			result += errInfo;
			result += "\nRe-run analysis for all imported files with -analyze, or specify\nthe corresponding "+babelfishProg+" version with -babelfish-version.";
		}
		else {
			if (type.equals("tgtversion")) {
				result = targetVersionTest;
			}
		}
		return result;
 	}

	// get list of all files/apps imported for this report
    public List<Path> getImportFiles(String reportName) throws IOException {
		String dirPath = getReportDirPathname(reportName, importDirName);
		File importDir = new File(dirPath);
		List<Path> importFiles = new ArrayList<>();
		if (importDir.exists()) {
			importFiles = getFilesPattern(dirPath, ".+\\."+importFileTag+"\\..+" + importFileSuffix);
		}
		return importFiles;
 	}

	// list all files/apps imported for this report
    public void listReportFiles(String reportName) throws IOException {
		String dirPath = getReportDirPathname(reportName, importDirName);
		File reportDir = new File(dirPath);

		if (reportDir.exists()) {
			List<String> importedFiles = new ArrayList<String>();

			List<Path> reportFiles = getFilesPattern(dirPath, ".+"+importFileTag+".+" + importFileSuffix);
			for (Path icf: reportFiles) {
				String line = importFileFirstLine(icf.toString());   // read only first line
				String fName          = importFileAttribute(line, 1);
				String appName        = importFileAttribute(line, 2);
				String encoding       = importFileAttribute(line, 3);     // not used
				String nrBatchesLines = importFileAttribute(line, 4);
				String dt             = importFileAttribute(line, 5);
				importedFiles.add(appName+sortKeySeparator+fName+sortKeySeparator+nrBatchesLines+sortKeySeparator+dt.trim());
			}

			List<String> sortedFiles = importedFiles.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
			appOutput("Applications/files for report '"+reportName+"': "+ ((sortedFiles.size()>0) ? sortedFiles.size() : "-none-"));
			for (String sf: sortedFiles) {
				List<String> fields = new ArrayList<String>(Arrays.asList(sf.split(sortKeySeparator)));
				appOutput("   app: "+fields.get(0)+",  "+fields.get(1)+"; #batches/lines: "+fields.get(2)+"; imported "+fields.get(3));
			}
		}
 	}


	// check report dir exists, and create if not
    public boolean checkReportExists(String reportName, List<String> inputFiles, boolean forceAppName, String applicationName, boolean replaceFiles, boolean addReport) throws IOException {
		if (debugging) dbgOutput(thisProc() + "reportName=[" + reportName + "] forceAppName=[" + forceAppName + "] applicationName=[" + applicationName + "] replaceFiles=[" + replaceFiles + "] addReport=[" + addReport + "] ", debugDir);
		String dirPath = getReportDirPathname(reportName);
		File reportDir = new File(dirPath);

		if (reportDir.exists()) {
			if (debugging) dbgOutput(thisProc() + "reportDir exists:[" + reportDir + "] ", debugDir);

			if (inputFiles.size() == 0) {
				if (addReport) {
					appOutput("Cannot specify -add without input files.");
					return true;
				}
				if (replaceFiles) {
					appOutput("Cannot specify -replace without input files.");
					return true;
				}
			}

			if (!addReport) {
				if (inputFiles.size() == 0) {
					// no inputfiles
					return false;
				}
			}

			boolean fileExists = false;
			StringBuilder replaceMsg = new StringBuilder();
			// validate input files
			for (String inFile : inputFiles) {
				if (!Files.exists(Paths.get(inFile))) {
					continue;
				}
				String appName = forceAppName ? applicationName : getFileNameFromPathName(inFile);
				appName = fixNameChars("appname", appName);
				if (appName.isEmpty()) {
					continue; // this will be caught higher up
				}
				String importFile = getImportFilePathName(reportName, inFile, appName);
				if (Files.exists(Paths.get(importFile))) {
					if (fileExists) replaceMsg.append("\n");
					replaceMsg.append("File '" + Paths.get(inFile).getFileName().toString() + "' for application '" + appName + "' already exists for this report");
					fileExists = true;
				}
				else {
					if (addReport) {
						appOutput("Adding input file " + Paths.get(inFile).getFileName().toString());
					}
				}
			}

			if (fileExists) {
				if (!replaceFiles) {
					appOutput(replaceMsg);
					appOutput("Specify -replace to replace the existing input file(s)");
					return (!replaceFiles);
				}
			}
			else {
				if (!addReport) {
					// must specify at least one of the flags
					appOutput("Report '" + reportName + "' already exists (" + reportDir.toString() + ")");
					appOutput("Specify -add to import additional input file(s) for this report");
					return true;
				}
			}

			return false;
		}
		else {
			if (debugging) dbgOutput(thisProc() + "reportDir does not exist:[" + reportDir + "] ", debugDir);
			checkDir(getReportDirPathname(reportName), false, true);

			// create subdirs
			checkDir(getReportDirPathname(reportName, importDirName), false);
			checkDir(getReportDirPathname(reportName, importDirName, importHTMLDirName), false);			
			checkDir(getReportDirPathname(reportName, importDirName, symTabDirName), false);
			checkDir(getReportDirPathname(reportName, capDirName), false);
			checkDir(getReportDirPathname(reportName, logDirName), false);

			return false;
		}
	}

	public void deleteReportDir(String reportName) throws IOException {
		// delete a report directory and all files in it
		if (debugging) dbgOutput(thisProc() + " reportName=[" + reportName + "] ", debugDir);
		String reportDir = getReportDirPathname(reportName);
		File dirFile = new File(reportDir);
		if (!dirFile.exists()) {
			//appOutput("Cannot delete report '"+reportName+"' at " + reportDir+": not found");
			return;
		}
		appOutput("Deleting report '" + reportName + "' at " + reportDir);
		deleteDirectoryTree(new File(reportDir));
	}

	public void deleteDirectoryTree(File dirPath) throws IOException {
		if (debugging) dbgOutput(thisProc() + " Entry: dirPath=[" + dirPath + "] ", debugDir);
		if (dirPath.isDirectory()) {
			File[] dirFiles = dirPath.listFiles();
			for (File f : dirFiles) {
				deleteDirectoryTree(f);
			}
		}
		if (debugging) dbgOutput(thisProc() + " Deleting dirPath=[" + dirPath + "] ", debugDir);
		dirPath.delete();
		if (dirPath.exists()) {
			// wait a short while; sometimes this seems to be needed, unclear why
			//appOutput("Retrying delete after short sleep: "+dirPath.toString());
			try {
				Thread.sleep(100); /* argument=millisecs */
			} catch (Exception e) {
			}
			if (dirPath.exists()) {
				// retry once more
				try {
					Thread.sleep(1000); /* argument=millisecs */
				} catch (Exception e) {
				}
				dirPath.delete();      // retry
				if (dirPath.exists()) {
					appOutput("Error deleting " + dirPath);
					errorExitStackTrace();
				}
			}
		}
	}

	public void deleteReAnalyze(String reportName) throws IOException {
		// wipe out symtab+captured
		if (debugging) dbgOutput(thisProc() + " reportName=[" + reportName + "] ", debugDir);

		String delDir = getReportDirPathname(reportName, importDirName, symTabDirName);
		appOutput("Deleting "+ delDir);
		deleteDirectoryTree(new File(delDir));

		delDir = getReportDirPathname(reportName, capDirName);
		appOutput("Deleting "+ delDir);
		deleteDirectoryTree(new File(delDir));

		//recreate
		checkDir(getReportDirPathname(reportName, importDirName, symTabDirName), false);
		checkDir(getReportDirPathname(reportName, importDirName, importHTMLDirName), false);
		checkDir(getReportDirPathname(reportName, capDirName), false);
	}

	public String openSessionLogFile(String reportName, Date now) throws IOException {
		sessionLogPathName = getSessionLogPathName(reportName, now);
		checkDir(getReportDirPathname(reportName, logDirName), true);
		sessionLogWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(sessionLogPathName), StandardCharsets.UTF_8)));
		writeSessionLogFile("<pre>");
		return sessionLogPathName;
	}

	public void writeSessionLogFile(String line) throws IOException {
		sessionLogWriter.write(line);
		sessionLogWriter.flush();
	}

    public void closeSessionLogFile() throws IOException {
		writeSessionLogFile("</pre>");
	    sessionLogWriter.close();
	}

	// Try to detect the encoding of the input file.
	// In particular, check for UTF8/UTF16/UTF32 by looking at the BOM bytes: SQL Server Mgmt Studio generates UTF16LE by default.
	// In case the detected encoding is different from the system default (as in Charset.defaultCharset()), return the name of the encoding.
	public String detectEncoding(String fileName) throws IOException {
		// using this charset because it gives identical results across platforms
		String cs = "ISO-8859-1";
		if (debugging) dbgOutput(thisProc() + "reading fileName=["+fileName+"] as cs=["+cs+"] default on this system=["+Charset.defaultCharset()+"] ", debugOS);		
		BufferedReader inFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), Charset.forName(cs)));
		StringBuilder bomSB = new StringBuilder(4);
		int readValue;

		while((readValue = inFileReader.read()) != -1 && bomSB.length() < 4) {
			char content = (char) readValue;
			bomSB.append(content);
		}

		inFileReader.close();
		String bom = bomSB.toString();
		
		// dump the BOM bytes
		if (debugging) {
			if (debugOS) {	
				String x = stringAsHex(bom);
				dbgOutput(thisProc() + "bom=["+bom+"] hex=["+ x+"]", debugOS);		
			}
		}

		String encodingFound = null;
		if (bom.startsWith("\u00EF\u00BB\u00BF")) {
			if (debugging) dbgOutput(thisProc() + "Detecting BOM UTF-8", debugOS);
			encodingFound = "UTF-8";
		}
		else if (bom.startsWith("\u00FE\u00FF")) {
			if (debugging) dbgOutput(thisProc() + "Detecting BOM UTF-16BE", debugOS);			
			encodingFound = "UTF-16"; // UTF-16BE
		}
		else if (bom.equals("\u0000\u0000\u00FE\u00FF")) {
			if (debugging) dbgOutput(thisProc() + "Detecting BOM UTF32BE", debugOS);
			encodingFound = "UTF-32"; // UTF-32BE
		}
		else if (bom.equals("\u00FF\u00FE\u0000\u0000")) {
			if (debugging) dbgOutput(thisProc() + "Detecting BOM UTF-32LE", debugOS);
			encodingFound = "UTF-32"; // UTF-32LE
		}
		else if (bom.startsWith("\u00FF\u00FE")) {
			if (debugging) dbgOutput(thisProc() + "Detecting BOM UTF-16LE", debugOS);
			encodingFound = "UTF-16"; // UTF-16LE
		}
		else {
			if (debugging) dbgOutput(thisProc() + "Not detecting BOM", debugOS);
		}
		if (encodingFound != null) {
			if (encodingFound.equalsIgnoreCase(Charset.defaultCharset().toString())) {
				encodingFound = null;
			}
		}
		return encodingFound;
	}

	// validate an input file: check format, figure out if this was created
	// by an unsupported reveng tool -- etc.
	public String detectImportFileFormat(String inputFileName, String importFormat, Charset charset) throws IOException {		
		String fullPath = Paths.get(inputFileName).toAbsolutePath().toString();
		if (debugging) dbgOutput(thisProc()+"inputFileName=["+inputFileName+"] fullPath=["+fullPath+"] importFormat=["+importFormat+"] ", debugFmt);
		FileInputStream fis = new FileInputStream(fullPath);
		InputStreamReader isr = new InputStreamReader(fis, charset);							
		BufferedReader inFileReader = new BufferedReader(isr);
		
		// read first N lines to try and determine format
		int goFound = 0;
		int createFound = 0;
		int alterFound = 0;
		int jsonQueryFmtFound = 0;
		int extendedEventsXMLFound = 0;
		int genericSQLXMLFound = 0;
		
		int nrLinesPreRead = 500;
		int lineNr = 0;
		StringBuilder lines  = new StringBuilder("");
		while (lineNr < nrLinesPreRead) {
			lineNr++;
			String line = inFileReader.readLine();
			if (line == null) {
				break;
			}
			line = line.trim();
			//lines.append(line).append("\n");
			
			// look for fingerprints
			if (line.equalsIgnoreCase("go")) goFound++;
			else {
				if (line.toUpperCase().startsWith("CREATE ")) createFound++;
				else if (line.toUpperCase().startsWith("ALTER ")) alterFound++;
				else if (line.startsWith("\"query")) {      
					if (!getPatternGroup(line,"(\"query_\\d+\":\\s+)", 1).isEmpty()) {				
						jsonQueryFmtFound++;
					}
				}		
				else if (line.startsWith("<event name=\"sql_statement_completed\"")) { 
					extendedEventsXMLFound++;
				}
				else if (line.startsWith("<data name=\"statement\"")) { 
					extendedEventsXMLFound++;			
				}		
//				else if (line.startsWith("<sql>")) { 
//					genericSQLXMLFound++;			
//				}		
			}
		}
		inFileReader.close();
		//lines.append("\n");
		
		//if (debugging) dbgOutput(thisProc()+"First "+lineNr+" lines read: ["+lines.toString()+"]", debugFmt);
		if (debugging) dbgOutput(thisProc()+"First "+lineNr+" lines read", debugFmt);
		
		if (debugging) dbgOutput(thisProc()+"goFound               =["+goFound+"]", debugFmt);
		if (debugging) dbgOutput(thisProc()+"createFound           =["+createFound+"]", debugFmt);
		if (debugging) dbgOutput(thisProc()+"alterFound            =["+alterFound+"]", debugFmt);
		if (debugging) dbgOutput(thisProc()+"jsonQueryFmtFound   =["+jsonQueryFmtFound+"]", debugFmt);
		if (debugging) dbgOutput(thisProc()+"extendedEventsXMLFound=["+extendedEventsXMLFound+"]", debugFmt);
		if (debugging) dbgOutput(thisProc()+"genericSQLXMLFound    =["+genericSQLXMLFound+"]", debugFmt);
		
		String seemsFormat = "";
		String seemsFormatDisplay = "";
		if (jsonQueryFmtFound > 0) {
			seemsFormat = jsonQueryFmt;
		}
		else if (extendedEventsXMLFound > 0) {
			seemsFormat = extendedEventsXMLFmt;
		}		
		else if (goFound > 0) { 
			seemsFormat = sqlcmdFmt;
		}	
		if (!seemsFormatDisplay.isEmpty()) {
			seemsFormatDisplay = importFormatOptionDisplay.get(importFormatOption.indexOf(seemsFormat.toLowerCase()));
		}
	
		
		if (importFormat.equalsIgnoreCase(jsonQueryFmt)) {
			//appOutput(thisProc()+"jsonQueryFmt Found=["+jsonQueryFmtFound+"] ");
			if (jsonQueryFmtFound == 0) {
				importFormatSeemsInvalidMsg(inputFileName, jsonQueryFmt, seemsFormat, "prefix: \"query_999\"");			
			}			
		}
		if (importFormat.equalsIgnoreCase(extendedEventsXMLFmt)) {
			//appOutput(thisProc()+"jsonQueryFmt, jsonQueryFmtFound=["+jsonQueryFmtFound+"] ");
			if (extendedEventsXMLFound == 0) {
				importFormatSeemsInvalidMsg(inputFileName, extendedEventsXMLFmt, seemsFormat, "tag: <event name=\"sql_statement_completed\"");			
			}			
		}
		
		else if (importFormat.equalsIgnoreCase(sqlcmdFmt)) {
			if (jsonQueryFmtFound + extendedEventsXMLFound + genericSQLXMLFound > 0) {
				importFormatSeemsInvalidMsg(inputFileName, sqlcmdFmt, seemsFormat, "batch delimiters: 'go'");			
			}
			else {
				// was reverse-engineered by wrong tool? (i.e. batch delimiters missing)
				if (createFound + alterFound > 5) { // arbitrary number
					if (goFound == 0) {
						appOutput("Input file '"+inputFileName+"' formatting:\nNo batch delimiters 'go' were found. Input scripts need to be in 'sqlcmd' format,\nusing 'go' as batch delimiters.\nTo reverse engineer you SQL server database(s), best use SQL Server Management Studio.\nProceeding, but errors may occur.\n");		
					}
				}
			}
		}
		
		return seemsFormat;
	}
	
	private void importFormatSeemsInvalidMsg(String inputFileName, String importFmtSpecified, String seemsFormat, String fmtExample) {
		//appOutput(thisProc()+"inputFileName=["+inputFileName+"] seemsFormat=["+seemsFormat+"] importFmtSpecified=["+importFmtSpecified+"] ");
		String s = "Input file '"+inputFileName+"' formatting:\n";
		s = "Input format '"+importFmtSpecified+"' was specified, but input file does not seem to be in this format";
		if (seemsFormat.isEmpty()) 
			s += ",\nsince no corresponding formatting was found ("+fmtExample+").\n";
		else
			s += ".\nInstead, it seems to be in '"+importFormatOptionDisplay.get(importFormatOption.indexOf(seemsFormat.toLowerCase()))+"' format. To process accordingly, specify '-importfmt "+seemsFormat+"'.\n";
		s += "Proceeding, but errors may occur.\n";
		//appOutput(s);	
	}
		
	// convert a special-format file to sqlcmd format
	public String convertInputFileFormat(String reportName, String inputFileName, String appName, String importFormat, Charset charset) throws IOException  {
		String fullPath = Paths.get(inputFileName).toAbsolutePath().toString();
		if (debugging) dbgOutput(thisProc()+"inputFileName=["+inputFileName+"] fullPath=["+fullPath+"] importFormat=["+importFormat+"] with charset=["+charset+"] deDupExtracted=["+deDupExtracted+"] ", debugFmt);
		FileInputStream fis = new FileInputStream(fullPath);
		InputStreamReader isr = new InputStreamReader(fis, charset);							
		BufferedReader inFileReader = new BufferedReader(isr);
		
		// open file for extracted queries
		String extractedFilePathName = openExtractedFile(reportName, inputFileName, fullPath, appName, charset.toString());
				
		int lineNr = 0;
		int queriesExtracted = 0;
		int queriesWritten = 0;
		StringBuilder linesNew  = new StringBuilder("");
		while (true) {
			lineNr++;
			String line = inFileReader.readLine();
			if (line == null) {
				break;
			}
			String lineCopy = line;
			line = line.trim();
			//linesNew.append(line).append("\n");
			
			if (importFormat.equals(jsonQueryFmt)) {
				// each line has one query: "query_1234":  "(@v int[...]) query ",
				if (!line.startsWith("\"query")) continue;   
				line = applyPatternFirst(line,"^\"query_\\d+\":\\s*", "");
				if (!line.startsWith("\"")) {
					if (line.startsWith("null")) continue;
					else {
						appOutput("Unexpected formatting on line "+lineNr+", skipping line: ["+lineCopy+"] ");
						continue;
					}
				}
				line = line.substring(1);
				if (!line.startsWith("(")) {
					// no parameters 
				}
				else {
					// strip parameters
					String params = findClosingBracket(line);
					line = line.substring(params.length());
					params = removeLastChar(params.substring(1));
					line = "declare " + params + "\n" + line;
					//appOutput(thisProc()+"params=["+params+"] ");
				}
				//appOutput(thisProc()+"line C=["+line+"]");
				
				if (line.endsWith("\",")) line = removeLastChars(line,2);
				else if (line.endsWith("\"")) line = removeLastChar(line);
				else {
					// should not happen, not sure what to do. just hope it's all OK
				}
				//appOutput(thisProc()+"line Z=["+line+"] ");	
				
				line = patchupSpecialCharsExtractedSQL(line);
				
				if (deDupExtracted) {
					deDuplicateExtractedQueries(line);
				}
				else {
					// do not unduplicate, but write directly
					writeExtractedFile(line);
					writeExtractedFile("go\n");
					queriesWritten++;
				}
				
				queriesExtracted++;
			}			
		}
		inFileReader.close();	
		
		if (deDupExtracted) {
			for (String qry : deDupQueries.keySet()) {
				int dupCnt = dupQueryCount.getOrDefault(qry, 0);
				if (dupCnt > 0) {
					writeExtractedFile("-- Duplicate queries skipped: "+dupCnt);
				}
				writeExtractedFile(deDupQueries.get(qry));
				writeExtractedFile("go\n");				
				queriesWritten++;
			}
		}
//		appOutput(thisProc()+"Queries extracted         : "+queriesExtracted);
//		appOutput(thisProc()+"Queries written           : "+queriesWritten);
//		appOutput(thisProc()+"Duplicate queries skipped : "+deDupSkipped);
		
		closeExtractedFile();
//		appOutput(thisProc()+"queriesExtracted=["+queriesExtracted+"] extractedFilePathName=["+extractedFilePathName+"] ");	
		return extractedFilePathName;
	}
	
	// deDuplicate extracted/capture queries
	public void deDuplicateExtractedQueries (String qry) {
		String qryOrig = qry;
		qry = applyPatternAll(qry, "\\s+", " ");
		qry = applyPatternAll(qry, "\\s*;\\s*", "");
		//appOutput(thisProc()+"qry A=["+qry+"]");
		qry = qry.toLowerCase() + " ";
		qry = applyPatternAll(qry, "([^\\w#])#[#\\w]+([^\\w#])", "$1"+"#tmptab"+"$2");
		//appOutput(thisProc()+"qry B=["+qry+"]");
		
		//mask all bracketed identifiers:
		qry = applyPatternAll(qry, "[\\[].*?[\\]]", "[bracketedID]");
		
		//mask all numbers:
		qry = applyPatternAll(qry, "\\b\\d+\\b", "9999");
		
		//mask all stringd:
		qry = applyPatternAll(qry, "\\'.*?\\'", "'string'");
		qry = applyPatternAll(qry, "\\'string\\'\\'string\\'", "'string'");
		
	
		if (deDupQueries.containsKey(qry)) {
			// duplicate query found, skipped
			deDupSkipped++;
			dupQueryCount.put(qry, dupQueryCount.getOrDefault(qry, 0)+1);
		}
		else {
			// new query found
			deDupQueries.put(qry, qryOrig);
		}	
	}
	
	public String patchupSpecialCharsExtractedSQL (String s) {
		if (s.contains("\\n")) s = s.replaceAll("\\\\n", "\n");
		if (s.contains("\\r")) s = s.replaceAll("\\\\r", "\r");
		if (s.contains("\\t")) s = s.replaceAll("\\\\t", "\t");
		if (s.contains("\\f")) s = s.replaceAll("\\\\f", "\f");		
		if (s.contains("\\u0027")) s = s.replaceAll("\\\\u0027", "'");		
		if (s.contains("\\u003c")) s = s.replaceAll("\\\\u003c", "<");		
		if (s.contains("\\u003e")) s = s.replaceAll("\\\\u003e", ">");		
		return s;
	}
		
	public String openExtractedFile(String reportName, String inputFileName, String fullPath, String appName, String encoding) throws IOException {
		String extractedFileName = Paths.get(inputFileName).getFileName().toString() + ".extracted.sql";
		String dirName = getFilePathname(getFilePathname(getDocDirPathname(), reportName), importDirName);
		extractedFilePathName = getFilePathname(dirName, extractedFileName);
		if (debugging) dbgOutput(thisProc()+"inputFileName=["+inputFileName+"] extractedFileName=["+extractedFileName+"] extractedFilePathName=["+extractedFilePathName+"] ", debugFmt);
		
		checkDir(getReportDirPathname(reportName, importDirName), true);
		extractedFileWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(extractedFilePathName), StandardCharsets.UTF_8)));		
		String now = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());
		String initLine = "-- Extracted from "+fullPath+" (format '"+importFormat+"', encoding '"+encoding+"') at " + now;
		writeExtractedFile(initLine);
		return extractedFilePathName;
	}	

	public void writeExtractedFile(String line) throws IOException {
		extractedFileWriter.write(line + "\n");
		extractedFileWriter.flush();
	}

    public void closeExtractedFile() throws IOException {
	    extractedFileWriter.close();
	    extractedFileWriter = null;
	}

	// find the closing bracket that matches the opening bracket on the start position
	public String findClosingBracket(String s)  {
		return findClosingBracket(s,0);
	}
	public String findClosingBracket(String s, int startPos) 
	{
		if (s.isEmpty()) return "";
		if (s.charAt(startPos) != '(') {
			// should throw an exception, but don't worry for now
		}
		int brktCnt = 0;
		for (int i  = startPos; i < s.length() ; i++) {
			char c = s.charAt(i);
			if (c == '(') brktCnt++;
			else if (c == ')') brktCnt--;
			if (brktCnt == 0) {
				String item = s.substring(startPos,i+1);
				return item;
			}
		}
	    return ""; // didn't find closing bracket
	}
	
	// capture file
    public void openCaptureFile(String reportName, String fileName, String appName) throws IOException {
    	captureFilePathName = getCaptureFilePathname(reportName, fileName, appName);
    	checkDir(getReportDirPathname(reportName, capDirName), true);
		captureFileWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(captureFilePathName), StandardCharsets.UTF_8)));
		String now = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());
		String initLine = captureFileLinePart1+"["+reportName+"]" + captureFileLinePart2 +"["+targetBabelfishVersion+"]" + captureFileLinePart3 + now + captureFileLinePart4 +"["+captureFileFormatVersion+"]";
		appendCaptureFile(initLine);
	}

    public void closeCaptureFile() throws IOException {
	    captureFileWriter.close();
	}

	// append line to the capture file
    public void appendCaptureFile(String itemLine) throws IOException {
	    captureFileWriter.write(decodeIdentifier(itemLine)+"\n");
	    captureFileWriter.flush();
	}

	// get attribute from imported file first line
    public String captureFileAttribute(String line, int part) throws IOException {
    	assert (part >= 1 && part <= 4): "invalid part value ["+part+"] ";
    	if (part == 4) part++;
    	return getPatternGroup(line, "^"+captureFileLinePart1+"[\\[](.*?)[\\]]"+captureFileLinePart2+"[\\[](.*?)[\\]]"+captureFileLinePart3+"(.*?)("+captureFileLinePart4+"[\\[](.*?)[\\]])?\\s*$", part);
    }

	// read capture file first line
    public String captureFileFirstLine(String fileName) throws IOException {
		FileInputStream fis = new FileInputStream(fileName);
		InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
		BufferedReader inFileReader = new BufferedReader(isr);
		String line = inFileReader.readLine();   // read only first line
		inFileReader.close();
		return line;
    }

	// metrics line
    public static String makeMetricsLine(String srcFileName, String appName, int nrBatches, int nrBatchesError, int nrLines) {
    	return metricsLineChar1 + metricsLineTag + "=" + srcFileName + captureFileSeparator + appName + captureFileSeparator + nrBatches + captureFileSeparator + nrBatchesError + captureFileSeparator + nrLines;
	}

	// strip delimiters if possible
	public String stripDelimitedIdentifier(String ID) {
		ID = decodeIdentifier(ID);
		if (ID.contains("\"")) {
			ID = applyPatternAll(ID, "(^|\\.)\"(.+?)\"", "$1\\[$2\\]");
		}
		if (ID.contains(".#")) {
			ID = applyPatternAll(ID, "^(.*)\\.(\\#["+identifierChars+"]*)$", "$2");
		}
		if (!ID.contains("[")) {
			return ID;
		}
		ID = applyPatternAll(ID, "(^|\\.)\\[([\\w\\#]["+identifierChars+"]*)\\]", "$1$2");
		if (!ID.contains("[")) {
			if (ID.contains(".#")) {
				ID = applyPatternAll(ID, "^(.*)\\.(\\#["+identifierChars+"]*)$", "$2");
			}
			return ID;
		}

		// if we get here, we have delimited identifiers left, but this will be rare
		// Handle contained open-square-brackets or dots
		// E.g. [abc[def] or [abc.def] --> both are valid
		String tmpPattern="(^|^.*?\\.)\\[(.+?)\\](\\..*$|$)";
		String tmpID = ID;
		StringBuilder IDnew = new StringBuilder();
		while (true) {
			String p1 = getPatternGroup(tmpID, tmpPattern, 1);
			String p2 = getPatternGroup(tmpID, tmpPattern, 2);
			if (p2.isEmpty()) { break; }
			tmpID = tmpID.substring(p1.length()+p2.length()+2);
			p2 = encodeIdentifier(p2);
			IDnew.append(p1);
			IDnew.append("[" + p2 + "]");
		}
		IDnew.append(tmpID);
		return IDnew.toString();
	}

	// encode certain characters in  identifiers
	public String encodeIdentifier(String ID) {
		if (ID.contains("[")) ID = ID.replaceAll("\\[", BBFSqBracketOpen);
		if (ID.contains("]")) ID = ID.replaceAll("\\]", BBFSqBracketClose);
		if (ID.contains(".")) ID = ID.replaceAll("\\.", BBFDot);
		return ID;
	}

	// clean up identifiers with temporary encoded chars
	public String decodeIdentifier(String ID) {
		if (!ID.contains(BBFEncodedMark)) {
			return ID;
		}
		ID = ID.replaceAll(BBFSqBracketOpen, "[");
		ID = ID.replaceAll(BBFSqBracketClose, "]");
		ID = ID.replaceAll(BBFDot, ".");
		return ID;
	}

	// extract object name from column name
	public String getObjectNameFromColumnID(String ID) {
		// we need the 1-but-last name in the identifier, so use getSchemaNameFromID(0
		return getSchemaNameFromID(ID);
	}

	// extract object name from object name
	public String getObjectNameFromID(String ID) {
		String objName = ID;
		if (ID.startsWith("HIERARCHYID::")) {
			objName = ID.substring("HIERARCHYID::".length());
		}
		if (!ID.contains(".")) {
			return objName;
		}
		List<String> parts = new ArrayList<String>(Arrays.asList(ID.split("\\.")));
		objName = parts.get(parts.size()-1);
		return objName;
	}

	// extract schema from object name
	public String getSchemaNameFromID(String ID) {
		String schemaName = "";
		if (!ID.contains(".")) {
			return schemaName;
		}
		List<String> parts = new ArrayList<String>(Arrays.asList(ID.split("\\.")));
		schemaName = parts.get(parts.size()-2);
		return schemaName;
	}

	// extract DB from object name
	public String getDBNameFromID(String ID) {
		String DBName = "";
		if (!ID.contains(".")) {
			return DBName;
		}
		List<String> parts = new ArrayList<String>(Arrays.asList(ID.split("\\.")));
		if (parts.size() < 3) {
			return DBName;
		}
		DBName = parts.get(parts.size()-3);
		return DBName;
	}

	// extract remote servername from 4-part object name
	public String getServerNameFromID(String ID) {
		String serverName = "";
		if (!ID.contains(".")) {
			return serverName;
		}
		List<String> parts = new ArrayList<String>(Arrays.asList(ID.split("\\.")));
		if (parts.size() < 4) {
			return serverName;
		}
		serverName = parts.get(parts.size()-4);
		return serverName;
	}

	public String normalizeName(String objName) {
		return normalizeName(objName, "");
	}
	public String normalizeName(String name, String options)
	{
		normalizeNameCall++;
		String cacheKey = Integer.toString(name.length())+"~"+name+"~"+options;
		if (caching) {
			String cached = normalizeNameCache.get(cacheKey);
			if (cached != null) {
				normalizeNameCached++;
				return cached;
			}
		}

		// strip leading dots
		while (true) {
			if (name.charAt(0) == '.' ) {
				name = name.substring(1);
			}
			else break;
		}

		// handle delimited identifiers
		name = stripDelimitedIdentifier(name);
		if (options.contains("datatype")) {
			if (name.toUpperCase().startsWith("SYS.")) name = name.substring(4);
			else if (name.toUpperCase().equals("XMLCOLUMN_SETFORALL_SPARSE_COLUMNS")) name = "XML COLUMN_SET FOR ALL_SPARSE_COLUMNS";
			else if (name.toUpperCase().startsWith("XML(")) name = applyPatternFirst(name, "^XML\\([\\[\\]\\w\\.]+\\)", CompassAnalyze.CfgXMLSchema);
			else if (name.toUpperCase().startsWith("NATIONALCHAR")) name = "NATIONAL " + name.substring("NATIONAL".length());

			if (name.toUpperCase().contains("VARYING")) name = applyPatternFirst(name, "\\b((N)?CHAR(ACTER)?)(VARYING\\b)", "$1 $4");  // not handling a UDD named CHARVARYING, but let's accept that
			if (name.contains("(") || name.contains(",")) name = applyPatternFirst(name, "([\\(\\,])[0]+(\\d+)([\\)\\,])", "$1$2$3");
		}
		if (caching) normalizeNameCache.put(cacheKey, name);
		return name;
	}

	// resolve & normalize a name
	public String resolveName(String resolvedName, String schema) {
		// use this when retrying with dbo after initially resolved name not found
		String resolvedName2 = applyPatternAll(resolvedName,"^(.*?\\.)\\w+(\\.\\w+$)", "$1" + schema.toUpperCase() + "$2");
		return resolvedName2;
	}

	public String resolveName(String objName) {
		objName = normalizeName(objName.toUpperCase());

		// #tmp tables
		if (objName.charAt(0) == '#') {
			return objName;
		}

		String tmpSchema = getSchemaNameFromID(objName);
		String tmpDB     = getDBNameFromID(objName);

		if ((tmpSchema.isEmpty()) && (tmpDB.isEmpty())) {
			objName = "." + objName;
			// use the schema name from the current context, if any
			if (!currentObjectType.equalsIgnoreCase(BatchContext)) {
				tmpSchema = getSchemaNameFromID(currentObjectName);
			}
			if (tmpSchema.isEmpty()) {
				tmpSchema = "dbo";  // ToDo: we can keep track of the current schema that would apply?
			}
			objName = tmpSchema + objName;
		}
		if (tmpDB.isEmpty()) {
			// currentDatabase can be blank if no USE stmt seen
			objName = currentDatabase + "." + objName;
		}
		return objName.toUpperCase();
	}

	// add to symbol table
	public void addObjectTypeSymTab(String objName, String objType) {
		addObjectTypeSymTab(objName, objType, false);
	}
	public void addObjectTypeSymTab(String objName, String objType, boolean readingSymTab)
	{
		if (!readingSymTab) {
			objName = resolveName(objName);
		}
		// don't need #tmp tables in this symtab
		if (objName.charAt(0) == '#') {
			return;
		}
		objTypeSymTab.put(objName.toUpperCase(), objType);
	}

	// add to symbol table
	public void addSUDFSymTab(String udfName, String dataType) {
		addSUDFSymTab(udfName, dataType, false);
	}
	public void addSUDFSymTab(String udfName, String dataType, boolean readingSymTab)
	{
		if (!readingSymTab) {
			udfName = resolveName(udfName);
			dataType = normalizeName(dataType);
		}
		SUDFSymTab.put(udfName.toUpperCase(), dataType);
	}

	// add to symbol table
	public void addTUDFSymTab(String udfName, String dataType) {
		addTUDFSymTab(udfName, dataType, false);
	}
	public void addTUDFSymTab(String udfName, String dataType, boolean readingSymTab)
	{
		if (!readingSymTab) {
			udfName = resolveName(udfName);
			dataType = normalizeName(dataType);
		}
		TUDFSymTab.put(udfName.toUpperCase(), dataType);
	}

	// add to symbol table
	public void addUDDSymTab(String uddName, String dataType) {
		addUDDSymTab(uddName, dataType, false);
	}
	public void addUDDSymTab(String uddName, String dataType, boolean readingSymTab)
	{
		if (!readingSymTab) {
			uddName = resolveName(uddName);
			dataType = normalizeName(dataType);
		}
		UDDSymTab.put(uddName.toUpperCase(), dataType);
	}

	// add to symbol table
	public void addColSymTab(String tableName, String colName, String dataType) {
		addColSymTab(tableName, colName, dataType, false, false);
	}
	public void addColSymTab(String tableName, String colName, String dataType, boolean nullable, boolean readingSymTab)
	{
		if (!readingSymTab) {
			tableName = resolveName(tableName);
			colName = normalizeName(colName);
			dataType = normalizeName(dataType);
		}
		String tabcol = tableName + "." + colName;
		String nullFmt = "";
		if (nullable) nullFmt = " NULL";
		colSymTab.put(tabcol.toUpperCase(), dataType + nullFmt);
		//appOutput(thisProc()+"adding tabcol("+colSymTab.size()+")=["+tabcol+"] dataType=["+dataType+nullFmt+"] ");
	}

	

	// write symbol table
	public void writeSymTab(String reportName, String inputFileName, String appName) throws IOException {
		checkDir(getReportDirPathname(reportName, importDirName, symTabDirName), true);
		symTabFilePathName = getSymTabFilePathName(reportName, inputFileName, appName);
		if (debugging) dbgOutput("symTabFilePathName=[" + symTabFilePathName + "] ", debugSymtab||debugDir);
		symTabFileWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(symTabFilePathName), StandardCharsets.UTF_8)));
		symTabFileLineCount = 0;
		String now = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());
		writeSymTabFile("# This file: " + symTabFilePathName + "; generated at " + now);
		symTabFileLineCount--;
		writeSymTabFile("# *** DO NOT EDIT THIS FILE ***");
		symTabFileLineCount--;

		String line = "";
		for (String obj : objTypeSymTab.keySet()) {
			line = "objtype" + symTabSeparator + maskChar(obj, symTabSeparator) + symTabSeparator + maskChar(objTypeSymTab.get(obj), symTabSeparator);
			writeSymTabFile(decodeIdentifier(line));
		}
		for (String sudf : SUDFSymTab.keySet()) {
			line = "sudf" + symTabSeparator + maskChar(sudf, symTabSeparator) + symTabSeparator + maskChar(SUDFSymTab.get(sudf), symTabSeparator);
			writeSymTabFile(decodeIdentifier(line));
		}
		for (String tudf : TUDFSymTab.keySet()) {
			line = "tudf" + symTabSeparator + maskChar(tudf, symTabSeparator) + symTabSeparator + maskChar(TUDFSymTab.get(tudf), symTabSeparator);
			writeSymTabFile(decodeIdentifier(line));
		}
		for (String udd : UDDSymTab.keySet()) {
			line = "udd" + symTabSeparator + maskChar(udd, symTabSeparator) + symTabSeparator + maskChar(UDDSymTab.get(udd), symTabSeparator);
			writeSymTabFile(decodeIdentifier(line));
		}

		closeSymTabFile();
	}

	// read symbol table
	public void readSymTab(String reportName) throws IOException
	{
		clearSymTab();

		String dirPath = getReportDirPathname(reportName, importDirName, symTabDirName);
		File reportDir = new File(dirPath);

		List<Path> symTabFiles = getFilesPattern(dirPath, ".+\\."+symTabFileTag+"\\..+"+ symTabFileSuffix);
		for (Path sf: symTabFiles) {
			if (debugging) dbgOutput("reading symtab file=[" + sf.toString() + "] ", debugSymtab);

			FileInputStream fis = new FileInputStream(sf.toString());
			InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
			BufferedReader inFileReader = new BufferedReader(isr);
			String line;
			int lineCnt = 0;
			while (true) {
				line = inFileReader.readLine();
				if (line == null) {
					// EOF
					break;
				}
				if (debugging) dbgOutput("symtab read: [" + line + "] ", debugSymtab);
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				if (line.charAt(0) == '#') {
					// comment
					continue;
				}
				lineCnt++;
				line = processSymTabLineRead(line);
			}
			inFileReader.close();
		}
		if (debugSymtab) {
			dumpSymTab("after reading from disk");
		}
	}

	public String processSymTabLineRead(String s) {
		List<String> fields = new ArrayList<String>(Arrays.asList(s.split(symTabSeparator)));
		if (fields.get(0).equals("objtype")) {
			String objName = unmaskChar(fields.get(1),symTabSeparator);
			String objType = unmaskChar(fields.get(2),symTabSeparator);
			addObjectTypeSymTab(objName, objType, true);
		}
		else if (fields.get(0).equals("sudf")) {
			String objName = unmaskChar(fields.get(1),symTabSeparator);
			String dataType = unmaskChar(fields.get(2),symTabSeparator);
			addSUDFSymTab(objName, dataType, true);
			String objNameBase = getObjectNameFromID(objName);
			if (XMLmethods.contains(objNameBase)) {
				SUDFNamesLikeXML.put(objNameBase, "");
			}
			if (HIERARCHYIDmethods.contains(objNameBase)) {
				SUDFNamesLikeHIERARCHYID.put(objNameBase, "");
			}
		}
		else if (fields.get(0).equals("tudf")) {
			String objName = unmaskChar(fields.get(1),symTabSeparator);
			String dataType = unmaskChar(fields.get(2),symTabSeparator);   // always 'TABLE'
			addTUDFSymTab(objName, dataType, true);
			String objNameBase = getObjectNameFromID(objName);
			if (objNameBase.equals("NODES")) {   // there's only one case to test for
				TUDFNamesLikeXML.put(objNameBase, "");
			}
		}
		else if (fields.get(0).equals("udd")) {
			String objName = unmaskChar(fields.get(1),symTabSeparator);
			String dataType = unmaskChar(fields.get(2),symTabSeparator);
			addUDDSymTab(objName, dataType, true);
		}
		else if (fields.get(0).equals("col")) {
			String tableName = unmaskChar(fields.get(1),symTabSeparator);
			String colName = unmaskChar(fields.get(2),symTabSeparator);
			String dataType = unmaskChar(fields.get(3),symTabSeparator);
			addColSymTab(tableName, colName, dataType, false, true);
		}
		return s;
	}

	// clear the symbol table
	public static void clearSymTab()
	{
		objTypeSymTab.clear();
		SUDFSymTab.clear();
		TUDFSymTab.clear();
		UDDSymTab.clear();
		colSymTab.clear();
		SUDFNamesLikeXML.clear();
		TUDFNamesLikeXML.clear();
		SUDFNamesLikeHIERARCHYID.clear();
	}

	// dump the symbol table
	public void dumpSymTab(String tag)
	{
		int countSymTab = 0;
		appOutput(composeOutputLine("--- Symbol Table -- "+ tag + " ", "-"));
		appOutput("");
		appOutput("objTypeSymTab: "+objTypeSymTab.size());
		for (String obj: objTypeSymTab.keySet()) {
			appOutput("objType=["+obj+"] => ["+objTypeSymTab.get(obj)+"]");
			countSymTab++;
		}
		appOutput("");
		appOutput("SUDFSymTab: "+SUDFSymTab.size());
		for (String sudf: SUDFSymTab.keySet()) {
			appOutput("sudf=["+sudf+"] => ["+SUDFSymTab.get(sudf)+"]");
			countSymTab++;
		}
		appOutput("");
		appOutput("TUDFSymTab: "+TUDFSymTab.size());
		for (String tudf: TUDFSymTab.keySet()) {
			appOutput("tudf=["+tudf+"] => ["+TUDFSymTab.get(tudf)+"]");
			countSymTab++;
		}
		appOutput("");
		appOutput("UDDSymTab: "+UDDSymTab.size());
		for (String udd: UDDSymTab.keySet()) {
			appOutput("udd=["+udd+"] => ["+UDDSymTab.get(udd)+"]");
			countSymTab++;
		}
		appOutput("");
		appOutput("SUDFNamesLikeXML: "+SUDFNamesLikeXML.size());
		for (String sudf: SUDFNamesLikeXML.keySet()) {
			appOutput("sudf=["+sudf+"] => ["+SUDFNamesLikeXML.get(sudf)+"]");
		}
		appOutput("");
		appOutput("TUDFNamesLikeXML: "+TUDFNamesLikeXML.size());
		for (String tudf: TUDFNamesLikeXML.keySet()) {
			appOutput("tudf=["+tudf+"] => ["+TUDFNamesLikeXML.get(tudf)+"]");
		}
		appOutput("");
		appOutput("colSymTab: "+colSymTab.size());
		for (String udd: colSymTab.keySet()) {
			appOutput("udd=["+udd+"] => ["+colSymTab.get(udd)+"]");
			countSymTab++;
		}		
		appOutput("");
		appOutput("symtab records: "+countSymTab);
		appOutput(composeOutputLine("", "-"));
	}

	// keep track of current database
	public void setCurrentDB(String dbName) {
		dbName = normalizeName(dbName);
		currentDatabase = dbName;
	}

	// object context
	public void clearContext() {
		currentObjectType = BatchContext;
		currentObjectName = "";
		currentObjectTypeSub = "";
		currentObjectNameSub = "";
		currentObjectAttributes = "";
	}
	public void setContext(String objType) {
		// used only for 'batch'
		setContext(objType, "");
	}
	public void setContext(String objType, String objName) {
		boolean subContext = false;
		if (!objName.isEmpty()) {
			objName = normalizeName(objName);
		}
		if (objType.equalsIgnoreCase("TABLE")) {
			if (!currentObjectType.equalsIgnoreCase(BatchContext)) {
				currentObjectTypeSub = objType;
				currentObjectNameSub = objName;
				subContext = true;
				//dbgOutput("Setting sub context to ["+currentObjectTypeSub+"], ["+currentObjectNameSub+"]");
			}
		}
		if (!subContext) {
			currentObjectType = objType;
			currentObjectName = objName;
			//dbgOutput("Setting context to ["+currentObjectType+"], ["+currentObjectName+"]", true);
		}
	}
	public void resetSubContext() {
		if (currentObjectTypeSub != "") {
			currentObjectTypeSub = "";
			currentObjectNameSub = "";
			//dbgOutput("clearing sub context, main context still ["+currentObjectType+"], ["+currentObjectName+"]");
		}
		else {
			// ignore
		}
	}

	public void openReportFile(String reportName) throws IOException {
		if (Compass.forceReportName) {
			File f = new File(reportFileTextPathName);
			if (f.exists()) {
				appOutput("Report file "+reportFileHTMLPathName+" already exists. Overwriting...");
			}
    	}
		reportFileWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(reportFileTextPathName), StandardCharsets.UTF_8)));	
		String now = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());
		reportFileWriterHTML = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(reportFileHTMLPathName), StandardCharsets.UTF_8)));
		String hdr = headerHTML + headerHTMLReport;
		hdr = formatHeaderHTML(hdr, now, reportName, reportName, "", "");
		hdr = formatToolTips(hdr);
		reportFileWriterHTML.write(hdr);
		reportFileWriterHTML.write("<pre>\n");
	}

	public void writeReportFile(StringBuilder line) throws IOException {
		writeReportFile(line.toString());
	}

	public void writeReportFile(String line) throws IOException {
		reportFileWriterHTML.write(line + "\n");
		reportFileWriterHTML.flush();
		line = removeHTMLTags(line);
		reportFileWriter.write(unEscapeHTMLChars(line) + "\n");
		reportFileWriter.flush();
	}

	public String removeHTMLTags (String line) {
		// for the .txt version, remove HTML tags
		if (line.contains("<a ")) {			
			line = applyPatternFirst(line,docLinkURL, docLinkURLText);						
			line = applyPatternAll(line,"<a class=.*?>", "");
			line = applyPatternAll(line,"<a href=.*?>", "");
			line = applyPatternAll(line,"<a name=.*?>", "");
			line = applyPatternAll(line,"</a>", "");
			line = applyPatternAll(line,backToToCText, "");
		}
		if (line.contains("<div ")) {
			line = applyPatternAll(line,"<div class=.*?>", "");
			line = applyPatternAll(line,"</div>", "");
			line = applyPatternAll(line,hintIcon, " ");
		}
		
		if (line.contains("<span ")) {
			line = applyPatternAll(line,"<span class.*?</span>", "");
		}
		return line;		
	}

	public void writeReportFile() throws IOException {
		reportFileWriter.write("\n");
		reportFileWriter.flush();
		reportFileWriterHTML.write("\n");
		reportFileWriterHTML.flush();
	}

	public void closeReportFile() throws IOException {
		reportFileWriter.close();
		reportFileWriterHTML.write("\n</pre>\n");
		reportFileWriterHTML.close();
	}

	public String progressCnt(int currentCount, int totalCount) {
		assert currentCount >= 1 : "currentCount must be >= 1";
		assert totalCount >= 1 : "totalCount must be >= 1";
		if (totalCount == 1) return "";
		return "("+currentCount+"/"+totalCount+") ";
	}

	public void reportSummaryStatus(String status, List<String> sortedList, Map<String, Integer> itemCount, Map<String, String>appItemList) throws IOException {
		StringBuilder lines = new StringBuilder();
		StringBuilder prevGroup = new StringBuilder(uninitialized);
		final String statsMarker = "~STATSHERE~";
		
		//progress indicator
		printProgress();
		
		Integer totalCnt = 0;
		int grpCount = 0;
		Map<String, Integer> itemCnt = new HashMap<>();
		for (String s: sortedList) {
			//appOutput(thisProc()+"s=["+s+"] ");
			if ((!s.startsWith(status)) && (!s.startsWith(lastItem))) continue;
			List<String> sortedFields = new ArrayList<String>(Arrays.asList(s.split(sortKeySeparator)));
			StringBuilder sortStatus = new StringBuilder(sortedFields.get(0));
			StringBuilder group = new StringBuilder(sortedFields.get(1).substring(groupSortLength));
			StringBuilder item = new StringBuilder(sortedFields.get(2));
			//appOutput(thisProc()+"sortStatus=["+sortStatus+"] group=["+group+"] item=["+item+"] ");

			if (!group.toString().equalsIgnoreCase(prevGroup.toString())) {
				prevGroup = group;
				if (grpCount > 0) {
					String stats = grpCount+"/"+itemCnt.size();
					lines = applyPatternSBFirst(lines, statsMarker, stats);
					grpCount = 0;
					itemCnt.clear();
				}
				if (sortStatus.toString().equals(lastItem)) break;
				lines.append(group + " ("+statsMarker+")\n");
			}
			grpCount += itemCount.get(s);
			totalCnt += itemCount.get(s);
			itemCnt.put(item.toString(),0);
			StringBuilder thisItem = new StringBuilder(popupHint(item.toString(),status,group.toString()) + " : " + itemCount.get(s).toString());
			lines.append(thisItem);
			if (reportAppsCount) {
				int minSpacer = 3;
				int spacerTab = 8;
				int spacerLen = spacerTab - (thisItem.length()%spacerTab);
				if (spacerLen < minSpacer) spacerLen += spacerTab;
				lines.append(stringRepeat(" ", spacerLen) + appItemList.get(s));
			}
			lines.append("\n");
		}

		// align datatype lengths
		lines = new StringBuilder(alignDataTypeLength(lines.toString()));

		boolean writeNote = true;
		if (lines.length() == 0) {
			lines = new StringBuilder("-no items to report-\n");
			writeNote = false;
		}

		if (lines.toString().length() > 0) {
			writeReportFile();
			String totalCntStr = "";
			if (totalCnt > 0) totalCntStr = " --- (total="+totalCnt.toString() + ")";
			String hdrText = "SQL features '"+supportOptionsDisplay.get(supportOptions.indexOf(status))+"' in " + babelfishProg +" v." + targetBabelfishVersion + totalCntStr;
			if (status.equals(Rewritten)) hdrText = "SQL features '"+supportOptionsDisplay.get(supportOptions.indexOf(status))+"'" + totalCntStr;
			writeReportFile(composeSeparatorBar(hdrText, tagSummary+status));

			if (status.equals(ReviewManually) && writeNote) {
				writeReportFile("Note: Items in this section could not be assessed by " + thisProgName);
			}
			if (status.equals(Rewritten) && writeNote) {
				rewriteNotes = "Notes:\n";
				rewriteNotes += "  * non-supported SQL features were rewritten by "+thisProgName+" v."+ thisProgVersion+ " for " + babelfishProg + " v." + targetBabelfishVersion + "\n";
				rewriteNotes += "  * rewritten SQL files are located in " + getReportDirPathname(reportName, rewrittenDirName) + "\n";
				if (rewrittenOppties.containsKey(CompassAnalyze.MergeStmt)) {
				rewriteNotes += "  * rewritten MERGE statements should be reviewed manually" + "\n";
				}
				writeReportFile(rewriteNotes);
			}
			writeReportFile();
			writeReportFile(lines);
		}
	}

	public void reportXrefByFeature(String status, List<String> sortedList) throws IOException {
		StringBuilder lines = new StringBuilder(doXrefMsg(status, "feature"));
		Integer skippedFilter = 0;
		Integer countFilter = 0;
		
		//progress indicator
		printProgress();		

		if (doXref(status, "feature")) {
			lines = new StringBuilder();
			StringBuilder linesTmp = new StringBuilder();
			StringBuilder hdr = new StringBuilder();
			StringBuilder itemSort = new StringBuilder();
			StringBuilder prevItemSort = new StringBuilder();
			StringBuilder prevItem = new StringBuilder();
			StringBuilder prevGroup = new StringBuilder();
			StringBuilder contextSort = new StringBuilder();
			StringBuilder prevContextSort = new StringBuilder();

			StringBuilder group = new StringBuilder();
			StringBuilder item = new StringBuilder();
			StringBuilder itemDetail = new StringBuilder();
			StringBuilder context = new StringBuilder();
			StringBuilder prevContext = new StringBuilder();
			StringBuilder subContext = new StringBuilder();
			StringBuilder prevSubContext = new StringBuilder();
			StringBuilder lineNr = new StringBuilder();
			StringBuilder prevLineNr = new StringBuilder();
			StringBuilder batchNr = new StringBuilder();
			StringBuilder prevBatchNr = new StringBuilder();
			StringBuilder lineNrInFile = new StringBuilder();
			StringBuilder prevLineNrInFile = new StringBuilder();
			StringBuilder srcFile = new StringBuilder();
			StringBuilder prevSrcFile = new StringBuilder();
			StringBuilder appName = new StringBuilder();
			StringBuilder prevAppName = new StringBuilder();

			List<String> lineNrs = new ArrayList<String>();
			List<String> lineNrsBatch = new ArrayList<String>();
			Integer itemCount = 0;
			boolean initLineNr = false;

			for (String s: sortedList) {
				if ((!s.startsWith(status)) && (!s.startsWith(lastItem))) continue;
				//if (debugging) dbgOutput(thisProc()+"s=["+s+"] ", debugReport);

				List<String> sortedFields = new ArrayList<String>(Arrays.asList(s.split(sortKeySeparator)));
				group = new StringBuilder(sortedFields.get(1).substring(groupSortLength));
				item = new StringBuilder(sortedFields.get(2));
				appName = new StringBuilder(sortedFields.get(3));
				srcFile = new StringBuilder(getSrcFileNameMap(sortedFields.get(4)));
				lineNr = new StringBuilder(sortedFields.get(6));
				batchNr = new StringBuilder(sortedFields.get(7));
				lineNrInFile = new StringBuilder(sortedFields.get(8));
				context = new StringBuilder(sortedFields.get(9));
				subContext = new StringBuilder(sortedFields.get(10));

				itemSort = new StringBuilder(createSortKey(group.toString(),item.toString(),appName.toString(),srcFile.toString()));
				//if (debugging) dbgOutput(thisProc()+"itemSort=["+itemSort+"]  srcFile=["+srcFile+"] ", debugReport);

				if (Compass.reportOnly) {
					if (!s.startsWith(lastItem)) {
						importFilePathName = srcFile.toString();
					}
				}

				if (!s.startsWith(lastItem)) {
					if (!reportOptionFilter.isEmpty()) {
						//ToDo: perform filtering before creating sort records, keeping the data set smaller
						String filter = "^.*"+reportOptionFilter+".*$";
						countFilter++;
						//if (debugging) dbgOutput(thisProc()+"filter: item=["+item.toString()+"]  reportOptionFilter=["+reportOptionFilter+"] ", debugReport);
						if (!getPatternGroup(item.toString(), "("+filter+")", 1).isEmpty()) {
							// keep it
							if (debugging) dbgOutput(thisProc()+"matching filter - keeping", debugReport);
							if (debugging) dbgOutput(thisProc()+"filter: item=["+item.toString()+"]  reportOptionFilter=["+reportOptionFilter+"] s=["+s+"] ", debugReport);
						}
						else {
							// does not match filter, skip it
							//if (debugging) dbgOutput(thisProc()+"no match with filter - skipping", debugReport);
							skippedFilter++;
							continue;
						}
					}
				}

				if (!itemSort.toString().equalsIgnoreCase(prevItemSort.toString())) {
					if (itemCount > 0) {
						hdr.append(prevItem);
						hdr.append(" (").append(prevGroup).append(", ");
						lines.append(hdr).append(itemCount.toString()).append(")\n");

						// complete current line
						linesTmp.append(completeLineByFeature(status, linesTmp, lineNrs, lineNrsBatch, prevContext, prevBatchNr, prevLineNrInFile, prevSrcFile, prevAppName));
						lineNrs.clear();
						lineNrsBatch.clear();
						if (debugging) dbgOutput(thisProc()+"item change, completing current line, linesTmp=["+linesTmp+"] context=["+context+"] prevContext=["+prevContext+"] ", debugReport);

						lines.append(linesTmp);
						lines.append("\n");
						hdr.setLength(0);
						itemCount = 0;

						// new line
						linesTmp.setLength(0);
					}
					prevItemSort = new StringBuilder(itemSort);
					prevItem = new StringBuilder(item);
					prevGroup = new StringBuilder(group);
					prevContextSort.setLength(0);
					if (s.startsWith(lastItem)) {
						break;
					}
				}
				itemCount++;

				if (!reportShowBatchNr.isEmpty())  {
					contextSort = new StringBuilder(createSortKey(context.toString(),batchNr.toString()));
				}
				else {
					contextSort = new StringBuilder(createSortKey(context.toString()));
					lineNr = new StringBuilder(Integer.toString(Integer.parseInt(lineNr.toString()) + Integer.parseInt(lineNrInFile.toString()) - 1));
				}

				if (debugging) dbgOutput(thisProc()+"contextSort=["+contextSort+"] ", debugReport);
				if (debugging) dbgOutput(thisProc()+"prevContextSort=["+prevContextSort+"] ", debugReport);
				if (contextSort.toString().equalsIgnoreCase(prevContextSort.toString())) {
					if (!initLineNr) {
						// same line, accumulate line nrs
						lineNrs.add(lineNr.toString());
						lineNrsBatch.add(lineNrInFile.toString());
						if (debugging) dbgOutput(thisProc()+"adding line number to list: ["+lineNr+"] total=["+lineNrs.size()+"] x=["+String.join(", ", lineNrs)+"] ", debugReport);
					}
					initLineNr = false;
				}
				else {
					if (linesTmp.length() > 0) {
						// complete previous line
						linesTmp.append(completeLineByFeature(status, linesTmp, lineNrs, lineNrsBatch, prevContext, prevBatchNr, prevLineNrInFile, prevSrcFile, prevAppName));
						if (debugging) dbgOutput(thisProc()+"new line, changed context, completing current line completed linesTmp=["+linesTmp+"] ", debugReport);
					}

					// new line
					lineNrs.clear();
					lineNrs.add(lineNr.toString());
					lineNrsBatch.add(lineNrInFile.toString());
					linesTmp.append(lineIndent + context);
					if (subContext.length() > 0) {
						linesTmp.append(", "+subContext);
					}
					linesTmp.append(", line ");
					if (debugging) dbgOutput(thisProc()+"new line B, new line number ["+lineNr+"] linesTmp=["+linesTmp+"] context=["+context+"] prevContext=["+prevContext+"] ", debugReport);
				}
				prevContextSort = new StringBuilder(contextSort);
				prevContext = new StringBuilder(context);
				prevSubContext = new StringBuilder(subContext);
				prevBatchNr = new StringBuilder(batchNr);
				prevLineNrInFile = new StringBuilder(lineNrInFile);
				prevSrcFile = new StringBuilder(srcFile);
				prevAppName = new StringBuilder(appName);
			}
		}
		else {
			if (status.equals(Rewritten)) rewriteNotes = "";
		}

		String filterMsg = "";
		if (skippedFilter > 0) {
			filterMsg = "Filter applied: "+skippedFilter.toString()+" of " + countFilter.toString()+" items skipped by filter '"+reportOptionFilter+"'\n\n";
		}

		if (lines.toString().trim().length() == 0) {
			lines = new StringBuilder("-no items to report-\n");
		}		
		writeReportFile();
		writeReportFile(composeSeparatorBar("X-ref: '"+supportOptionsDisplay.get(supportOptions.indexOf(status))+"' by SQL feature", tagByFeature+status));
		writeReportFile(filterMsg);
		if (status.equals(Rewritten) && !rewriteNotes.isEmpty()) {
			writeReportFile(rewriteNotes);
		}		
		writeReportFile(lines);
	}

	private String completeLineByFeature(String status, StringBuilder linesTmp, List<String> lineNrs, List<String> lineNrsBatch, StringBuilder prevContext, StringBuilder prevBatchNr, StringBuilder prevLineNrInFile, StringBuilder srcFile, StringBuilder appName) {
		String inFile = reportInputFileFmt;
		if (reportShowSrcFile) inFile = srcFile.toString();
		String ln = makeLineNrList(status, lineNrs, lineNrsBatch, inFile, appName.toString());

		if (!reportShowBatchNr.isEmpty()) {
			ln += " in batch "+ prevBatchNr.toString() + " (at line " + hLink(status, Integer.parseInt(prevLineNrInFile.toString()),inFile, appName.toString())+")";
		}

		if (reportShowSrcFile && !inFile.equals(reportInputFileFmt)) {
			ln += " in " + hLink(status, inFile, appName.toString());
		}
		if (reportShowAppName) ln += ", app "+ appName;
		return ln + "\n";
	}

	public void reportXrefByObject(String status, List<String> sortedList) throws IOException {
		StringBuilder lines = new StringBuilder(doXrefMsg(status, "object")+"\n");
		Integer skippedFilter = 0;
		Integer countFilter = 0;

		//progress indicator
		printProgress();

		if (doXref(status, "object")) {
			lines = new StringBuilder();
			StringBuilder hdr = new StringBuilder();
			StringBuilder contextSort = new StringBuilder();
			StringBuilder prevContextSort = new StringBuilder();
			StringBuilder itemGroupSort = new StringBuilder();
			StringBuilder prevItemGroupSort = new StringBuilder();

			StringBuilder group = new StringBuilder();
			StringBuilder item = new StringBuilder();
			StringBuilder itemDetail = new StringBuilder();
			StringBuilder context = new StringBuilder();
			StringBuilder lineNr = new StringBuilder();
			StringBuilder batchNr = new StringBuilder();
			StringBuilder lineNrSort = new StringBuilder();
			StringBuilder lineNrInFile = new StringBuilder();
			StringBuilder srcFile = new StringBuilder();
			StringBuilder prevSrcFile = new StringBuilder();
			StringBuilder appName = new StringBuilder();
			StringBuilder prevAppName = new StringBuilder();

			boolean init = false;
			List<String> lineNrs = new ArrayList<String>();
			List<String> lineNrsBatch = new ArrayList<String>();

			for (String s: sortedList) {
				if ((!s.startsWith(status)) && (!s.startsWith(lastItem))) continue;
				//if (debugging) dbgOutput(thisProc()+"s=["+s+"] ", debugReport);

				List<String> sortedFields = new ArrayList<String>(Arrays.asList(s.split(sortKeySeparator)));
				context = new StringBuilder(sortedFields.get(1));
				appName = new StringBuilder(sortedFields.get(2));
				srcFile = new StringBuilder(getSrcFileNameMap(sortedFields.get(3)));
				if (!reportShowBatchNr.isEmpty()) {
					lineNrSort = new StringBuilder(sortedFields.get(4));
					group = new StringBuilder(sortedFields.get(5).substring(groupSortLength));
					item = new StringBuilder(sortedFields.get(6));
				}
				else {
					lineNrSort = new StringBuilder(sortedFields.get(6));
					group = new StringBuilder(sortedFields.get(4).substring(groupSortLength));
					item = new StringBuilder(sortedFields.get(5));
				}
				lineNr = new StringBuilder(sortedFields.get(7));
				batchNr = new StringBuilder(sortedFields.get(8));
				lineNrInFile = new StringBuilder(sortedFields.get(9));

				if (context.toString().equals(BatchContextLastSort)) context = new StringBuilder(BatchContext);

				String batchNrSort = "";
				if (context.toString().equalsIgnoreCase(BatchContext)) batchNrSort = batchNr.toString();
				contextSort = new StringBuilder(createSortKey(context.toString(),appName.toString(),srcFile.toString(),batchNrSort));
				itemGroupSort = new StringBuilder(createSortKey(item.toString(), group.toString()));

				//if (debugging) dbgOutput(thisProc()+"contextSort=["+contextSort+"] ", debugReport);
				//if (debugging) dbgOutput(thisProc()+"item=["+item+"] itemGroupSort=["+itemGroupSort+"] ", debugReport);

				if (!s.startsWith(lastItem)) {
					if (!reportOptionFilter.isEmpty()) {
						//ToDo: perform filtering before creating sort records, keeping the data set smaller
						String filter = "^.*"+reportOptionFilter+".*$";
						countFilter++;
						//if (debugging) dbgOutput(thisProc()+"filter: item=["+item.toString()+"]  reportOptionFilter=["+reportOptionFilter+"] s=["+s+"]", debugReport);
						if (!getPatternGroup(item.toString(), "("+filter+")", 1).isEmpty()) {
							// keep it
							if (debugging) dbgOutput(thisProc()+"matching filter - keeping", debugReport);
							if (debugging) dbgOutput(thisProc()+"filter: item=["+item.toString()+"]  reportOptionFilter=["+reportOptionFilter+"] s=["+s+"]", debugReport);
						}
						else {
							// does not match filter, skip it
							//if (debugging) dbgOutput(thisProc()+"no match with filter - skipping", debugReport);
							skippedFilter++;
							continue;
						}
					}
				}

				boolean changedContext = false;
				if (!contextSort.toString().equalsIgnoreCase(prevContextSort.toString()) && !s.startsWith(lastItem)) {
					changedContext = true;

					// complete the current line
					if (init) {
						lines.append(completeLineByObject(status, lineNrs, lineNrsBatch, prevSrcFile, prevAppName));
					}
					lineNrs.clear();
					lineNrsBatch.clear();

					if (!init) init = true;
					else lines.append("\n");

					lines.append(context);
					lines.append(", batch ");

					String inFile = reportInputFileFmt;
					if (reportShowSrcFile) inFile = srcFile.toString();
					lines.append(batchNr.toString()+ ", at line " + hLink(status, Integer.parseInt(lineNrInFile.toString()),inFile, appName.toString()));
					lines.append(" in " + hLink(status, inFile, appName.toString()));

					if (reportShowAppName) lines.append(", app "+ appName);
					lines.append("\n");

					prevContextSort = new StringBuilder(contextSort);
				}

				if (!s.startsWith(lastItem)) {
					if (reportShowBatchNr.isEmpty())  {
						lineNr = new StringBuilder(Integer.toString(Integer.parseInt(lineNr.toString()) + Integer.parseInt(lineNrInFile.toString()) - 1));
					}
				}

				if ((!prevItemGroupSort.toString().equalsIgnoreCase(itemGroupSort.toString()) && !s.startsWith(lastItem)) || changedContext) {
					if (!changedContext) {
						// complete the current line
						lines.append(completeLineByObject(status, lineNrs, lineNrsBatch, prevSrcFile, prevAppName));
						lineNrs.clear();
						lineNrsBatch.clear();
					}

					lines.append(lineIndent+item.toString()+" ("+group.toString()+") : line ");
				}
				if (!s.startsWith(lastItem)) {
					lineNrs.add(lineNr.toString());
					lineNrsBatch.add(lineNrInFile.toString());
				}
				changedContext = false;

				if (s.startsWith(lastItem)) {
					if (lineNrs.size() > 0) {
						lines.append(completeLineByObject(status, lineNrs, lineNrsBatch, prevSrcFile, prevAppName));
						break;
					}
				}

				prevContextSort = new StringBuilder(contextSort);
				prevItemGroupSort = new StringBuilder(itemGroupSort);
				prevSrcFile = new StringBuilder(srcFile);
				prevAppName = new StringBuilder(appName);
			}
		}
		else {
			if (status.equals(Rewritten)) rewriteNotes = "";
		}
		
		String filterMsg = "";
		if (skippedFilter > 0) {
			filterMsg = "Filter applied: "+skippedFilter.toString()+" of " + countFilter.toString()+" items skipped by filter '"+reportOptionFilter+"'\n\n";
		}

		if (lines.toString().trim().length() == 0) {
			lines = new StringBuilder("-no items to report-\n");
		}
		writeReportFile();
		writeReportFile(composeSeparatorBar("X-ref: '"+supportOptionsDisplay.get(supportOptions.indexOf(status))+"' by object", tagByObject+status));
		writeReportFile(filterMsg);
		if (status.equals(Rewritten) && !rewriteNotes.isEmpty()) {
			writeReportFile(rewriteNotes);
		}			
		writeReportFile(lines);
	}

	private String completeLineByObject(String status, List<String> lineNrs, List<String> lineNrsBatch, StringBuilder srcFile, StringBuilder appName) {
		String inFile = reportInputFileFmt;
		if (reportShowSrcFile) inFile = srcFile.toString();

		String ln = makeLineNrList(status, lineNrs, lineNrsBatch, inFile, appName.toString());
//		if (reportShowSrcFile && !inFile.equals(reportInputFileFmt)) {
//			ln += " in " + hLink(inFile, appName.toString());
//		}
		return ln + "\n";
	}

	private boolean doXref(String status, String type) {
		boolean doIt = false;
		if (reportOptionXref.contains("all") || reportOptionXref.contains(type)) {
			if (reportOptionStatus.isEmpty()) {
				if (status.equals(NotSupported) ||
				    status.equals(ReviewSemantics) ||
				    status.equals(ReviewManually) ||
				    status.equals(ReviewPerformance) ||
				    status.equals(Rewritten))
				    {
				    	doIt = true;
				    }
			}
			else if (reportOptionStatus.contains(" "+status.toLowerCase()+" ") ||
			         reportOptionStatus.contains(" all ")) {
				doIt = true;
			}
		}
		return doIt;
	}

	private String doXrefMsg(String status, String type) {
		String statusMsg = "";
		if (status.equals(Supported) || status.equals(Ignored)) {
			statusMsg = ", and 'status="+status.toLowerCase()+"' or 'status=all'";
		}
		String s = "";
		s += "To generate this section, specify these options with -reportoption:\n";
		s += "     'xref'  or  'xref="+type+"'" + statusMsg + "\n";
		s += "For more options and examples, use -help -reportoption";
		return s;
	}

	private String makeLineNrList(String status, List<String> lineNrs, List<String> lineNrsBatch, String fileName, String appName) {
		int nrLineNrs = lineNrs.size();
		String xtra = "";
		if (maxLineNrsInList < nrLineNrs) {
			xtra = " (+"+Integer.toString(nrLineNrs-maxLineNrsInList)+" more)";
			nrLineNrs = maxLineNrsInList;
		}
		String joined = "";
		for (int i=0; i < nrLineNrs; i++) {
			if (i > 0) joined += ", ";
			int lineNr = Integer.parseInt(lineNrs.get(i));
			int adjLineNr = lineNr;
			if (!reportShowBatchNr.isEmpty()) {
				int batchLineNr = Integer.parseInt(lineNrsBatch.get(i));
				adjLineNr = batchLineNr + lineNr - 1;
			}
			joined += hLink(status, lineNr, fileName, appName, adjLineNr);
		}
		return joined.trim() + xtra;
	}

	private String makeItemHintKey (String s) {
		// normalize the key for tooltips
		s = applyPatternAll(s, "[\\(]", "brktopen").toLowerCase();
		s = applyPatternAll(s, "[\\)]", "brktclose").toLowerCase();
		s = applyPatternAll(s, "[,]", "comma").toLowerCase();
		s = applyPatternAll(s, "[\\s]", "").toLowerCase();
		s = applyPatternAll(s, "[\\W]", "_");
		s = applyPatternAll(s, "[_]+", "_");
		if (s.startsWith("_")) s = s.substring(1);
		if (s.endsWith("_")) s = removeLastChar(s);
		return s;
	}

	private String getItemHintKey (String item) {
		//find tooltip key for this item
		String itemHintKey = "";
		String itemOrig = item;
		item = makeItemHintKey(item);

		if (toolTipsKeys.containsKey(item)) {
			itemHintKey = item;
		}
		else {
			for (int i=0; i<toolTipsKeysList.size(); i++) {			
			//for (Map.Entry<String,String> e : toolTipsKeys.entrySet()) {
				String k = toolTipsKeysList.get(i);
				String v = toolTipsKeys.get(k);

				if (item.equalsIgnoreCase(k)) {
					itemHintKey = k;
					break;
				}
				if (item.startsWith(k)) {
					itemHintKey = k;
					break;
				}
				if (itemOrig.toLowerCase().startsWith(v)) {
					itemHintKey = k;
					break;
				}
				if (v.contains("\\")) {
					if (!getPatternGroup(itemOrig, "^("+v+")", 1).isEmpty()) {
						itemHintKey = k;
						break;
					}
				}
			}
			if (itemHintKey.isEmpty()) {
				//appOutput("no tooltip key found for item=["+itemOrig+"] ");
			}
		}
		//appOutput(thisProc()+"final: item=["+itemOrig+"] item=["+item+"]  itemHintKey=["+itemHintKey+"] ");
		return itemHintKey;
	}

	private String popupHint (String item, String status, String group) {
		if (status.equals(Rewritten)) return lineIndent + item;
		boolean blank = false;
		if ((status.equals(Supported)) || (status.equals(Ignored))) blank = true;
		String itemHintKey = getItemHintKey(item);
		if (itemHintKey.isEmpty()) blank = true;
		if (blank) {
			String indent = "  <span class=\"tooltip_blank\">&nbsp;</span> ";
			return indent+item;
		}
		String hint = lineIndent.substring(0,lineIndent.length()-2)+"<div class=\"tooltip\"><span class=\"tooltip_icon\">"+hintIcon+"</span>"+" "+item+"<div class=\"tooltip-content\" data-tooltip=\""+itemHintKey+"\"></div></div>";
		return hint;
	}

	private String hLinkFileName (String status, String file, String appName) {
		if (!file.contains(importFileTag)) {
			try {
				String inFileCopy = getImportFilePathName(reportName, file, appName);
				file = inFileCopy;
			} catch  (Exception e) { /* nothing */ }
		}
		file = file.substring(file.lastIndexOf(File.separator)+1);
		String dirname = importDirName +File.separator+ importHTMLDirName;
		if (status.equals(Rewritten)) {
			dirname = rewrittenDirName +File.separator+ rewrittenHTMLDirName;
			file = file.replaceFirst(importFileTag, rewrittenFileTag);
		}		
		file = changeFilenameSuffix(file, importFileSuffix, HTMLSuffix);
		String result = dirname+File.separator+ file;
		return result;
	}

	private String hLink (String status, String file) {
		file = file.substring(file.lastIndexOf(File.separator)+1);
		file = logDirName + File.separator + file;
		String line = "<a href=\""+ file +"\""+tgtBlank+">"+file+"</a>";
		return line;
	}

	private String hLink (String status, String file, String appName) {
		String line = "";
		String thisFile = reportInputFileFmt;
		if (status.equals(Rewritten)) {
			if (file.equals(reportInputFileFmt)) {
				thisFile = "rewritten file";
			}
		}
		
		if (file.equals(reportInputFileFmt)) {
			line = "<a href=\""+ hLinkFileName(status, importFilePathName, appName) +"\""+tgtBlank+">"+thisFile+"</a>";
		}
		else {
			String hLinkFile = hLinkFileName(status, file, appName);
			String fileDisplay = file;
			if (status.equals(Rewritten)) {
				fileDisplay = renameRewrittenFile(reportName, appName, file);
				fileDisplay = fileDisplay.substring(fileDisplay.lastIndexOf(File.separator));
				fileDisplay = rewrittenDirName + fileDisplay;
			}
			line = "<a href=\""+ hLinkFile +"\""+tgtBlank+">"+fileDisplay+"</a>";
		}
		return line;
	}

	private String hLink (String status, Integer lineNr, String file, String appName) {
		return hLink(status, lineNr, file, appName, lineNr);
	}
	private String hLink (String status, Integer lineNr, String file, String appName, Integer lineNrDisplay) {
		if (status.equals(Rewritten)) {
			// adjust line numbers in case of rewrite
			if (offsetLines.size() > 0) {
				String tmpFName = "";
				if (file.equals(reportInputFileFmt)) {
					// keep blank
				}
				else {
					try { tmpFName = getImportFilePathName("DONTCARE", file, appName); }
					catch (Exception e) { /* shouldn't ever get here */ errorExitStackTrace(); }
					tmpFName = Paths.get(tmpFName).getFileName().toString().replaceAll(importFileTag, rewrittenFileTag);
				}
				Integer adjustedLineNr = calcOffsetLineHLink(tmpFName, lineNr);
				lineNr = lineNrDisplay = adjustedLineNr;
			}
		}
		
		String line = "";
		if (file.equals(reportInputFileFmt)) {
			line = "<a href=\""+ hLinkFileName(status, importFilePathName, appName) +"#"+lineNrDisplay.toString()+"\""+tgtBlank+">"+lineNr.toString()+"</a>";
		}
		else {
			line = "<a href=\""+ hLinkFileName(status, file, appName) +"#"+lineNrDisplay.toString()+"\""+tgtBlank+">"+lineNr.toString()+"</a>";
		}
		return line;
	}

	// generate sort key
	private String createSortKey(String... f) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < f.length; ++i) {
			s.append(f[i]);
			if (i == f.length-1) continue;
			s.append(sortKeySeparator);
		}
		return s.toString();
	}

	// add src file name to map
	private void addSrcFileNameMap(String srcFileName, String ix) {
		// only called for initialization
		srcFileMap.put(ix, srcFileName);
		srcFileMapIx.put(srcFileName,ix);
	}
	private String addSrcFileNameMap(String srcFileName) {
		String ix = srcFileMapIx.get(srcFileName);
		if (ix != null) return ix;

		Integer n = srcFileMap.size() + 1;
		ix = "f" + n.toString();
		srcFileMap.put(ix, srcFileName);
		srcFileMapIx.put(srcFileName, ix);
		return ix;
	}

	private String getSrcFileNameMap(String ix) {
		String f = srcFileMap.get(ix);
		assert f != null : "ix=["+ix+"]  not found in srcFileMap";
		return f;
	}

	// modify sort key to reposition groups in the report
	private String getGroupSortKey(String group) {
		int sortGroup = 0;
		if (reportGroupSortAdjustment.containsKey(group.toUpperCase())) sortGroup = reportGroupSortAdjustment.get(group.toUpperCase());
		String sortKey = String.format("%0"+groupSortLength.toString()+"d", sortGroup) + group;
		return sortKey;
	}

	// setup sort key map
	private void setupreportGroupSortAdjustment() {
		Map<String, Integer> tmp = new HashMap<>();

		// specify sort adjustments:
//		tmp.put(CompassAnalyze.ViewsReportGroup, 510);
//		tmp.put(CompassAnalyze.ProceduresReportGroup, 520);
//		tmp.put(CompassAnalyze.FunctionsReportGroup, 530);
//		tmp.put(CompassAnalyze.TriggersReportGroup, 540);

		tmp.put(CompassAnalyze.MiscReportGroup, 900);
		tmp.put(CompassAnalyze.XMLReportGroup, 920);
		tmp.put(CompassAnalyze.JSONReportGroup, 920);
		tmp.put(CompassAnalyze.SpatialReportGroup, 920);
		tmp.put(CompassAnalyze.HIERARCHYIDReportGroup, 920);
		tmp.put(CompassAnalyze.TableVariablesType, 930);
		tmp.put(CompassAnalyze.UDDatatypes, 940);
		tmp.put(CompassAnalyze.DatatypeConversion, 950);
		tmp.put(CompassAnalyze.Datatypes, 960);

		// map keys to uppercase
		for(String k: tmp.keySet()) {
			reportGroupSortAdjustment.put(k.toUpperCase(), tmp.get(k));
		}
	}

	private String tocLink(String tag, String txt1, String txt2, String status) {
		tag = tag.toLowerCase() + status.toLowerCase();
		String statusFmt = "";
		if (!status.isEmpty()) statusFmt = supportOptionsDisplay.get(supportOptions.indexOf(status));
		String s = lineIndent + "<a href=\"#"+tag+"\">"+ txt1 + statusFmt + txt2 +"</a>\n";
		return s;
	}
	
	private void printProgress() {
		appOutput( ".", false, true);		
	}

	public boolean createReport(String reportName) throws IOException {
		if (debugging) dbgOutput(thisProc()+"reportOptionXref=["+reportOptionXref+"] ", debugReport);
		if (debugging) dbgOutput(thisProc()+"reportOptionStatus=["+reportOptionStatus+"] ", debugReport);
		if (debugging) dbgOutput(thisProc()+"reportOptionDetail=["+reportOptionDetail+"] ", debugReport);
		if (debugging) dbgOutput(thisProc()+"reportOptionApps=["+reportOptionApps+"] ", debugReport);
		if (debugging) dbgOutput(thisProc()+"reportOptionFilter=["+reportOptionFilter+"] ", debugReport);

		// upgrade check for Compass 1.0/1.1 reports
		if (!reportOptionXref.isEmpty()) {
			// move any HTML files
			moveImportedHTMLFiles(reportName);
		}
		
		Date now = new Date();
		String now_report = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss").format(now);
		reportFileTextPathName = getreportFilePathName(reportName, now);
		reportFileHTMLPathName = getReportFileHTMLPathname(reportName, now);
		reportFilePathName = reportFileHTMLPathName;
		appOutput("");
		appOutput("Generating report " + reportFilePathName + "...", false, true);

		String line = "";

		openReportFile(reportName);

		String hdrLine = "Report for: " + reportName + " : Generated at " + now_report;
		writeReportFile(hdrLine);
		writeReportFile(stringRepeat("-", hdrLine.length()));

		writeReportFile("\n" + thisProgName + " version " + thisProgVersion + ", " + thisProgVersionDate);
		writeReportFile(thisProgNameLong);
		writeReportFile(copyrightLine + "\n");
		writeReportFile(disclaimerMsg + "\n");
		writeReportFile(docLinkIcon + " " +docLinkURL + "\n\n");
		writeReportFile(composeOutputLine("--- Report Setup ", "-"));
		writeReportFile(reportHdrLines);
		writeReportFile("This report                : "+reportFilePathName);
		writeReportFile("Session log                : "+hLink("", sessionLogPathName));
		writeReportFile(composeOutputLine("", "=") + "\n");


		Map<String, Integer> appCount = new HashMap<>();
		Map<String, Integer> srcFileCount = new HashMap<>();
		Map<String, Integer> objTypeCount = new HashMap<>();
		Map<String, Integer> objTypeLineCount = new HashMap<>();
		Map<String, String>  objTypeMap = new HashMap<>();
		Map<String, String>  objTypeMapCase = new HashMap<>();
		Map<String, Integer> objIssueCount = new HashMap<>();
		int linesSQLInObjects = 0;
		boolean showObjectIssuesList = false;
		Map<String, Long> statusCount = new HashMap<>();
		Map<String, Integer> itemCount = new HashMap<>();
		Map<String, Integer> appItemListRaw = new HashMap<>();
		Map<String, String> appItemList = new LinkedHashMap<>();
		List<String> xRefByFeature = new ArrayList<String>();
		List<String> xRefByObject = new ArrayList<String>();

		String currentAppName = "";
		String currentSrcFile = "";

		// init map
		addSrcFileNameMap(lastItem, lastItem);
		
		// check flag
		if (!reportOptionXref.isEmpty()) showObjectIssuesList = true;

		// sanity check
		assert supportOptions.size() == supportOptionsCfgFile.size() : "supportOptions.size() [" + supportOptions.size() + "] must be equal to supportOptionsCfgFile.size() " + supportOptionsCfgFile.size() + "]";
		assert supportOptions.size() == supportOptionsDisplay.size() : "supportOptions.size() [" + supportOptions.size() + "] must be equal to supportOptionsDisplay.size() " + supportOptionsDisplay.size() + "]";
		
		// set up arrays for processing groups of statuses
		String fmtBatches = "batches";
		String fmtLinesSQL = "linesSQL";
		List<String> fmtStatus = new ArrayList<String>(Arrays.asList("apps", "inputfiles", fmtBatches, "linesDDL", fmtLinesSQL, "constructs"));
		List<String> fmtStatusDisplay = new ArrayList<String>(Arrays.asList("#applications", "#input files", "#SQL batches", "#lines SQL/DDL processed", "#lines SQL in objects", "total #SQL features"));
		for (int i = 0; i < supportOptions.size(); i++) {
			fmtStatus.add(supportOptions.get(i));
			fmtStatusDisplay.add(supportOptionsDisplay.get(i));
		}

		// adjust category ordering
		setupreportGroupSortAdjustment();

		// get all capture files
		List<Path> captureFiles = getCaptureFiles(reportName);
		if (debugging) dbgOutput(thisProc() + "captureFiles(" + captureFiles.size() + ")=[" + captureFiles + "] ", debugReport);
		if (captureFiles.size() == 0) {
			List<Path> importedFiles = getImportFiles(reportName);
			if (importedFiles.size() == 0) {
				appOutput("No imported files found. Specify input file(s) to add to this report.");
			}
			else {
				String msg = "No analysis results found. Use -analyze to perform analysis and generate a report.";
				appOutput(msg);
				writeReportFile("\n"+msg);
			}
			errorExit();
		}
		String cfv = captureFilesValid("report", captureFiles);
		if (!cfv.isEmpty()) {
			// print error message and exit
			appOutput(cfv);
			errorExit();
		}

		// process captured items
		constructsFound = 0;
		Integer totalLinesDDL = 0;
		int totalBatches = 0;
		int totalErrorBatches = 0;

		long sortSizeSummary = 0L;
		long sortSizeXRefByFeature = 0L;
		long sortSizeXRefByObject = 0L;

		for (Path cf : captureFiles) {
			String cfLine = captureFileFirstLine(cf.toString());   // read only first line
			String cfReportName = captureFileAttribute(cfLine, 1);
			if (importFilePathName == null) importFilePathName = getImportFilePathNameFromCaptured(cf.toString());
			if (cfReportName.isEmpty()) {
				appOutput("Invalid format in "+cfReportName+"; run with -analyze to fix.");
				errorExit();
			}
			if (!reportName.equalsIgnoreCase(cfReportName)) {
				String rDir = getFilePathname(getDocDirPathname(), capDirName);
				appOutput("Found analysis file for report '" + cfReportName + "' in " + rDir + ": adding contents to report "+reportName);
			}

			FileInputStream cfis = new FileInputStream(new File(cf.toString()));
			InputStreamReader cfisr = new InputStreamReader(cfis, StandardCharsets.UTF_8);
			BufferedReader capFile = new BufferedReader(cfisr);
			if (debugging) dbgOutput(thisProc() + "reading captureFile=[" + cf + "]", debugReport);

			String capLine = "";
			int capCount = 0;

			while (true) {
				capLine = capFile.readLine();
				if (capLine == null) {
					//EOF
					break;
				}
				capLine = capLine.trim();
				if (capLine.isEmpty()) continue;
				if (capLine.charAt(0) == '#') {
					if (capCount == 0) {
						if (debugging) dbgOutput("first line of cf=[" + cf.toString() + "] : [" + capLine + "] ", debugReport);
					}
					continue;
				}
				capCount++;
				if (debugging) if (capCount%100000 == 0) dbgOutput("read "+capCount, debugReport);

				// check for metrics lines
				if (capLine.charAt(0) == metricsLineChar1) {
					String metricsLine = getPatternGroup(capLine, "^." + metricsLineTag + "=(.*)$", 1);

					assert !metricsLine.isEmpty() : "metricsLine cannot be blank";

					List<String> tmpList = new ArrayList<String>(Arrays.asList(metricsLine.split(captureFileSeparator)));
					String srcFileTmp = tmpList.get(0);
					String appNameTmp = tmpList.get(1);
					totalBatches += Integer.parseInt(tmpList.get(2));
					totalErrorBatches += Integer.parseInt(tmpList.get(3));
					int loc = Integer.parseInt(tmpList.get(4));
					totalLinesDDL += loc;
					appCount.put(appNameTmp, appCount.getOrDefault(appNameTmp, 0) + loc);
					srcFileCount.put(srcFileTmp, srcFileCount.getOrDefault(srcFileTmp, 0) + 1);

					continue;
				}
				//un-escape backslashes
				if (capLine.contains("\\\\")) {
					capLine = applyPatternAll(capLine, "\\\\\\\\", "\\\\");
				}

				List<String> itemList = new ArrayList<String>(Arrays.asList(capLine.split(captureFileSeparator)));
				// sanity checks on #fields on the line read
				if (itemList.size() < capPosLastField) {
					appOutput("\nError at line "+capCount+" of "+cf.toString()+":");					
					appOutput("Invalid capture item read: expected "+(capPosLastField)+" fields, found "+itemList.size()+". Skipping this item:");
					appOutput("["+capLine+"]");
					continue;
				}
				String objType = getPatternGroup(itemList.get(capPosItem), "^CREATE (.*)$", 1);
				if (objType.isEmpty()) {
					objType = getPatternGroup(itemList.get(capPosItem), "^Constraint (.*?)(\\(.*)?$", 1);
					String objTypeTmp = getPatternGroup(objType, "^(.*?),.*$", 1);
					objType = objTypeTmp.isEmpty() ? objType : objTypeTmp;
					if (!objType.isEmpty()) {
						objType = "constraint " + objType;
					}
				}
				else {
					if (objType.startsWith("TYPE")) {
						objType = objType.replaceFirst("TYPE", "user-defined datatype (UDD)");
					}
					else if (objType.startsWith("INDEX")) {
						objType = objType.replaceFirst("INDEX", "index");
					}
					else if (objType.startsWith("DATABASE")) {
						objType = objType.substring(0, "DATABASE".length());
					}
					else if (objType.startsWith("PROCEDURE")) {
						objType = "PROCEDURE";
					}
				}
				String item = itemList.get(capPosItem).replaceAll(captureFileSeparatorMarker, captureFileSeparator);
				String itemDetail = itemList.get(capPosItemDetail).replaceAll(captureFileSeparatorMarker, captureFileSeparator);
				String itemGroup = itemList.get(capPosItemGroup).replaceAll(captureFileSeparatorMarker, captureFileSeparator);
				String status = itemList.get(capPosStatus);
				String lineNr = itemList.get(capPosLineNr);
				String context = itemList.get(capPosContext).replaceAll(captureFileSeparatorMarker, captureFileSeparator);
				String subContext = itemList.get(capPosSubContext).replaceAll(captureFileSeparatorMarker, captureFileSeparator);
				String appName = itemList.get(capPosappName);
				String batchNr = itemList.get(capPosBatchNr);
				String lineNrInFile = itemList.get(capPosLineNrInFile);
				String srcFile = itemList.get(capPosSrcFile);
				String misc = itemList.get(capPosMisc);

				if (debugging) dbgOutput(thisProc() + "capLine=[" + capLine + "] objType=[" + objType + "] item=[" + item + "] itemDetail=[" + itemDetail + "] itemGroup=[" + itemGroup + "] status=[" + status + "] lineNr=[" + lineNr + "] misc=[" + misc + "] ", debugReport);
				assert supportOptions.contains(status) : "Invalid status value[" + status + "] in line=[" + capLine + "] ";

				if (!objType.isEmpty()) {
					if (!status.equals(Ignored)) {
						// massage the object type strings to the format we need for the object count output section
						if ((!objType.equals("constraint column DEFAULT")) && (!objType.equals("constraint PRIMARY KEY/UNIQUE"))) {
							objType = applyPatternFirst(objType, "^(.*?,.*?),.*$", "$1");
							if (objType.startsWith("TRIGGER,")) objType = "TRIGGER";
							if (objType.startsWith("TRIGGER (DDL")) {
								objType = "TRIGGER (DDL)";                 
								if (misc.equals("0")) {
									// this comes from a multi-action DDL trigger, count avoid counting double
									objTypeCount.put(objType, objTypeCount.getOrDefault(objType, 0) - 1);
								}
							}
							objType = objType.replaceFirst(", external", "");
							objType = objType.replaceFirst(", CLUSTERED", "");
							if (objType.contains("<"))  // for cases like CREATE xxx <somename>
								objType = objType.substring(0,objType.indexOf("<"));
							if (objType.contains("&"))  // for cases like CREATE xxx &gt;somename&lt;
								objType = objType.substring(0,objType.indexOf("&"));
							if (objType.contains(captureFileSeparatorMarker))
								objType = getPatternGroup(objType, "^(.*?)\\s*\\b\\w*" + captureFileSeparatorMarker + ".*$", 1);       // for proc versioning
							objType = objType.trim();
							objTypeCount.put(objType, objTypeCount.getOrDefault(objType, 0) + 1);
							if (debugging) dbgOutput(thisProc() + "counting objType=[" + objType + "] ", debugReport);
							int loc = 0;
							if (!misc.isEmpty()) loc = Integer.parseInt(misc);
							objTypeLineCount.put(objType, objTypeLineCount.getOrDefault(objType, 0) + loc);  // misc contains #lines for procedural CREATE object stmts
							linesSQLInObjects += loc;

							if (item.startsWith("CREATE ")) {
								if (objType.startsWith("PROCEDURE") || objType.startsWith("FUNCTION") || objType.startsWith("TRIGGER") || objType.startsWith("TABLE") || objType.startsWith("VIEW")) {
									if (!objType.startsWith("TABLE ")) {
										if (!objTypeMap.containsKey(itemDetail.toUpperCase())) {
											if (showObjectIssuesList) objTypeMapCase.put(itemDetail, objType);
										}										
										objTypeMap.put(itemDetail.toUpperCase(), objType);
									}
								}
							}

						}
					}
				}

				// count columns for tables; put this in objTypeLineCount as well
				if ((item.endsWith(" column")) || (item.startsWith("Computed column"))) {
					String tabType = "";
					if (context.startsWith("TABLE ")) {
						tabType = context.substring(6);
					}
					else if (subContext.startsWith("TABLE ")) {
						tabType = subContext.substring(6);
					}
					if (!tabType.isEmpty()) {
						String tabTypeReport = "TABLE " + CompassAnalyze.getTmpTableType(tabType);
						tabTypeReport = tabTypeReport.trim();
						objTypeLineCount.put(tabTypeReport, objTypeLineCount.getOrDefault(tabTypeReport, 0) + 1);
					}
				}

				// for items logged only to drive the object count, stop here
				if (status.equals(ObjCountOnly)) continue;

				statusCount.put(status, statusCount.getOrDefault(status, 0L) + 1);

				// count issues per object
				boolean skipItemIssue = false;
				if (status.equals(Supported) || status.equals(Ignored) || status.equals(ReviewSemantics) || status.equals(ReviewPerformance)  || status.equals(Rewritten) || status.equals(ObjCountOnly)) {
					// do not count as issue
					skipItemIssue = true;
				}
				if (context.equalsIgnoreCase(BatchContext)) { 
					// skip batches			
					skipItemIssue = true;				
				}	
				if (!getPatternGroup(item, "^(ALTER TABLE..(NO)?CHECK CONSTRAINT)", 1).isEmpty()) {
					// skip ALTER TABLE..[NO]CHECK CONSTRAINT, it does not affect the CREATE TABLE
					skipItemIssue = true;
				}
				if (!skipItemIssue) {
					String k = context.toUpperCase();
					if (k.contains(" ")) {
						k = k.substring(k.lastIndexOf(" ")+1);
					}
					objIssueCount.put(k, objIssueCount.getOrDefault(k,0)+1);
				}

				// apply weight factors
				String sw = status + WeightedStr;
				int weightFactor = supportOptionsWeightDefault.get(supportOptions.indexOf(status));
				// is there a user-defined weight factor?
				if (userWeightFactor.containsKey(itemGroup)) {
					weightFactor = userWeightFactor.get(itemGroup);
					if (debugging) dbgOutput(thisProc() + "found user-defined weight factor for itemGroup=[" + itemGroup + "] ", debugReport);
				}
				Long weighted = statusCount.getOrDefault(status, 0L) * weightFactor;
				statusCount.put(sw, weighted);
				//if (debugging) dbgOutput(thisProc() + "status=[" + status + "] val=[" + statusCount.getOrDefault(status, 0L) + "]  sw=[" + sw + "] weighted=[" + weighted + "] weightFactor=[" + weightFactor + "] ", debugReport);


				String itemGroupSort = getGroupSortKey(itemGroup);


				String itemTmp = item;
				// uncomment to make the 'detail' flag apply to the summary as well; but that doesn't look very useful.
//				if (!reportOptionDetail.isEmpty()) {
//					if (!itemDetail.isEmpty()) {
//						itemTmp = item + ": " + itemDetail;
//					}
//				}
				// sort key for status summary
				String sortKey = createSortKey(status,itemGroupSort,itemTmp);
				String keyApp = createSortKey(sortKey,appName);
				itemCount.put(sortKey, itemCount.getOrDefault(sortKey, 0) + 1);
				appItemListRaw.put(keyApp, appItemListRaw.getOrDefault(keyApp, 0) + 1);
				constructsFound++;
				sortSizeSummary += sortKey.length();

				// sort key for X-ref ordered by feature
				// this may run out of memory for very big data sets, would need different sort approach for smaller memory sizes
				if (!reportOptionXref.isEmpty()) {
					if (!reportOptionDetail.isEmpty()) {
						if (!itemDetail.isEmpty()) {
							item += ": " + itemDetail;
						}
					}

					String lineNrSort = String.format("%08d", Integer.parseInt(lineNrInFile)) + "." + String.format("%06d", Integer.parseInt(lineNr));
					sortKey = createSortKey(status,itemGroupSort,item,appName,addSrcFileNameMap(srcFile),lineNrSort,lineNr,batchNr,lineNrInFile,context, subContext, "closing dummy");
					xRefByFeature.add(sortKey);
					sortSizeXRefByFeature += sortKey.length();

					if (context.equals(BatchContext)) context = BatchContextLastSort;

					if (reportShowBatchNr.isEmpty()) {
						lineNrSort = String.format("%08d", Integer.parseInt(lineNr.toString()) + Integer.parseInt(lineNrInFile.toString()) - 1);
						sortKey = createSortKey(status,context,appName,addSrcFileNameMap(srcFile),itemGroupSort,item,lineNrSort,lineNr,batchNr,lineNrInFile);
					}
					else {
						// report batchnr
						lineNrSort = String.format("%08d", Integer.parseInt(lineNrInFile));
						sortKey = createSortKey(status,context,appName,addSrcFileNameMap(srcFile),lineNrSort,itemGroupSort,item,lineNr,batchNr,lineNrInFile);
					}

					xRefByObject.add(sortKey);
					sortSizeXRefByObject += sortKey.length();
				}
			}
			capFile.close();

			if (debugging) dbgOutput(thisProc()+"capCount=["+capCount+"] sortCnt="+itemCount.size()+" sortSizeSummary KB=["+sortSizeSummary/1024+"] ", debugReport);
			if (debugging) dbgOutput(thisProc()+"capCount=["+capCount+"] sortCnt="+xRefByFeature.size()+" sortSizeXRefByFeature KB=["+sortSizeXRefByFeature/1024+"]", debugReport);
			if (debugging) dbgOutput(thisProc()+"capCount=["+capCount+"] sortCnt="+xRefByObject.size()+" sortSizeXRefByObject KB=["+sortSizeXRefByObject/1024+"]", debugReport);
		}

		// calc #objects without issues for main object types
		Map<String,Integer> objTypeIssueCount = new HashMap<>();
		Map<String,Integer> objTypeNoIssueCount = new HashMap<>();
		Map<String,Integer> objTypeIssueMap = new HashMap<>();
		for (Map.Entry<String,String> e : objTypeMap.entrySet()) {
			String c = e.getKey();
			String type = e.getValue();
			Integer issues = objIssueCount.getOrDefault(c,0);

			objTypeIssueMap.put(type, 1);
			if (issues == 0)
				objTypeNoIssueCount.put(type, objTypeNoIssueCount.getOrDefault(type, 0) + 1);
			else
				objTypeIssueCount.put(type, objTypeIssueCount.getOrDefault(type, 0) + 1);
		}
		
//		// for debugging:
//		for (String t : objTypeIssueMap.keySet()) {
//			Integer iX = objTypeIssueCount.getOrDefault(t,0);
//			Integer i0 = objTypeNoIssueCount.getOrDefault(t,0);
//			appOutput(thisProc()+"t=["+t+"] iX=["+iX+"] i0=["+i0+"] ");
//		}

		// reporting options
		if (srcFileCount.size() <= 1) reportShowSrcFile = false;
		if (appCount.size() <= 1) reportShowAppName = false;
		if (appCount.size() <= 1) reportAppsCount = false;
		if (reportOptionApps.isEmpty()) reportAppsCount = false;

		if (reportAppsCount) {
			Map<String, Map<String, Integer>> appItemListTmp = new LinkedHashMap<>();
			// create aggregated app list
			for (String s : appItemListRaw.keySet()) {
				String k = s.substring(0,s.lastIndexOf(sortKeySeparator));
				String app = s.substring(k.length()+sortKeySeparator.length());
				if (!appItemListTmp.containsKey(k)) appItemListTmp.put(k, new LinkedHashMap<>());
				Map<String, Integer> tmpList = appItemListTmp.get(k);
				tmpList.put(app, tmpList.getOrDefault(app, 0)+appItemListRaw.get(s));
			}
			for (String k : appItemListTmp.keySet()) {
				Map<String,Integer> tmpList = appItemListTmp.get(k);
				String appList = "";
				for (String app : tmpList.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList())) {
					if (!appList.isEmpty()) appList += ", ";
					appList += app + "("+tmpList.get(app)+")";
				}
				appItemList.put(k,"#apps="+Integer.toString(tmpList.keySet().size())+ ": "+appList);
			}
		}
		appItemListRaw.clear();

		StringBuilder summarySection = new StringBuilder();

		summarySection.append(composeSeparatorBar("Table Of Contents", "toc", false));

		summarySection.append(tocLink(tagApps, "Applications Analyzed", "", ""));
		summarySection.append(tocLink(tagSummaryTop, "Assessment Summary", "", ""));
		if (showPercentage) {
			summarySection.append(tocLink(tagEstimate, "Compatibility Estimate", "", ""));
		}
		summarySection.append(tocLink(tagObjcount, "Object Count", "", ""));
		summarySection.append("\n");
				
		for (int i=0; i <supportOptionsIterate.size(); i++) {
			summarySection.append(tocLink(tagSummary, "Summary of SQL Features '", "'", supportOptionsIterate.get(i)));
		}
		
		if (!rewrite) {
			if (rewriteOppties.containsKey(rewriteOpptiesTotal)) {
				if (rewriteOppties.get(rewriteOpptiesTotal) > 0) {
					summarySection.append("\n");
					summarySection.append(tocLink(tagRewrite, "Automatic SQL Rewrite Opportunities", "", ""));
				}
			}
		}
		
		summarySection.append("\n");
		for (int i=0; i <supportOptionsIterate.size(); i++) {
			summarySection.append(tocLink(tagByFeature, "X-ref: '", "' by SQL feature", supportOptionsIterate.get(i)));
		}
		summarySection.append("\n");
		for (int i=0; i <supportOptionsIterate.size(); i++) {
			summarySection.append(tocLink(tagByObject, "X-ref: '", "' by object", supportOptionsIterate.get(i)));
		}
		summarySection.append("\n\n");

		summarySection.append(composeSeparatorBar("Applications Analyzed (" + appCount.size() + ")", tagApps));
		summarySection.append("\n");
		for (String app : appCount.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList())) {
			int loc = appCount.get(app);
			summarySection.append(lineIndent + " " + app + " (" + appCount.get(app) + " lines SQL)\n");
		}
		summarySection.append("\n");

		linesSQLInReport = totalLinesDDL;

		summarySection.append(composeSeparatorBar("Assessment Summary", tagSummaryTop));
		summarySection.append("\n");
		statusCount.put("linesDDL", Long.valueOf(totalLinesDDL));
		statusCount.put(fmtLinesSQL, Long.valueOf(linesSQLInObjects));
		statusCount.put(fmtBatches, Long.valueOf(totalBatches));
		statusCount.put("inputfiles", Long.valueOf(srcFileCount.size()));
		statusCount.put("apps", Long.valueOf(appCount.size()));
		statusCount.put("invalid syntax", Long.valueOf(totalErrorBatches)); // #batches with parse errors
		statusCount.put("constructs", Long.valueOf(constructsFound));
		statusCount.put("constructs", Long.valueOf(constructsFound));

		StringBuilder summaryTmp2 = new StringBuilder();
		for (int i = 0; i < fmtStatus.size(); i++) {
			String reportItem = fmtStatus.get(i);
			if (!statusCount.containsKey(reportItem)) continue;
			String xtra = "";
			if (reportItem.equals(fmtBatches)) {
				Long errs = statusCount.getOrDefault("invalid syntax", 0L);
				if (errs > 0) {
					xtra = lineIndent + "(with syntax error: " + errs + ")";
				}
			}
			else if (reportItem.equals(fmtLinesSQL)) {
				xtra = lineIndent + "(procedures/functions/triggers/views)";
			}
			if (statusCount.containsKey(reportItem + WeightedStr)) {
				int w = supportOptionsWeightDefault.get(supportOptions.indexOf(reportItem));
				if (showPercentage) {
					xtra = lineIndent + "(compatibility weight factor: " + w + "%)";
				}
			}
			String hrefStart = "";
			String hrefEnd= "";
			if (false) {  // disabled these hyperlinks as it messes up the alignment of columns; need to use a HTML table structure instead
				if (supportOptions.contains(fmtStatus.get(i))) {
					String tag = "summary_"+fmtStatus.get(i);
					hrefStart = "<a href=\"#"+tag.toLowerCase()+"\">";
					hrefEnd = "</a>";
				}
			}
			summaryTmp2.append(lineIndent).append(hrefStart+fmtStatusDisplay.get(i) + hrefEnd + " : " + statusCount.get(reportItem) + xtra + "\n");
		}
		if (!rewrite) {
			if (rewriteOppties.containsKey(rewriteOpptiesTotal)) {
				if (rewriteOppties.get(rewriteOpptiesTotal) > 0) {
					summaryTmp2.append(lineIndent).append("Automatic SQL rewrite opportunities" +  " : " + rewriteOppties.get(rewriteOpptiesTotal) + "\n");
				}
			}
		}
		summaryTmp2 = new StringBuilder(alignColumn(summaryTmp2, " : ", "before", "left"));
		summaryTmp2 = new StringBuilder(alignColumn(summaryTmp2, " : ", "after", "right"));
		summarySection.append(summaryTmp2);
		summarySection.append("\n");

		// add override info
		if (Compass.reportOnly) {			
			if (CompassConfig.overrideCount > 0) {
				// there are overrides in the user .cfg file, but indicate that overrides have not been applied in generating this report
				summarySection.append("NB: user-defined overrides (via "+CompassConfig.userConfigFilePathName+")\n");
				summarySection.append("are applied during analysis, not during report generation (this was a report-only run).\n");
				summarySection.append("To apply overrides for this report's imported files, use -analyze.\n");
				summarySection.append("\n");			
			}
			else {
				// there are no overrides in the user .cfg file, so no need to print any warnings
			}			
		}
		else {
			if (statusOverrides.size() > 0) {
				StringBuilder summaryTmp3 = new StringBuilder();
				for (String k : statusOverrides.keySet().stream().sorted().collect(Collectors.toList())) {
					List<String> ov = new ArrayList<>(Arrays.asList(k.split(overrideSeparator)));				
					Integer n = statusOverrides.get(k);
					summaryTmp3.append(lineIndent).append(supportOptionsDisplay.get(supportOptions.indexOf(ov.get(0))) + " (was: "+ supportOptionsDisplay.get(supportOptions.indexOf(ov.get(1))) + ") : " + n.toString()+"\n");
				}		
				
				summaryTmp3 = new StringBuilder(alignColumn(summaryTmp3, " : ", "before", "left"));
				summaryTmp3 = new StringBuilder(alignColumn(summaryTmp3, " : ", "after", "right"));
				summarySection.append("Overridden by user .cfg file ("+CompassConfig.userConfigFilePathName+"):\n");
				summarySection.append(summaryTmp3);
				summarySection.append("\n");				
			}
			else {
				// no overrides were applied, so no additional information needs to be printed
			}
		}

		// calc %age. NB: this has been removed from the report
		//--- debug -------
		if (debugCalc) {
			if (debugging) dbgOutput("", debugReport);
			if (debugging) dbgOutput(thisProc() + "----------compatPct calculation: -----------", debugReport);
			String thisProc = thisProc(); // To not call thisProc() more than once
			for (String sc : statusCount.keySet()) {
				appOutput(thisProc + "    statusCount=[" + sc + "] value=[" + statusCount.get(sc) + "] ");
			}
		}
		//--- end debug -------
		long compatPct = 0;
		if (statusCount.getOrDefault("constructs", 0L) == 0) {
			compatPctStr = "Not Applicable";
		}
		else {
			// calc the weighted items for not-supported, to substract from 100%
			Long subtract = (statusCount.getOrDefault(NotSupported + WeightedStr, 0L) +
					statusCount.getOrDefault(ReviewSemantics + WeightedStr, 0L) +
					statusCount.getOrDefault(ReviewManually + WeightedStr, 0L) +
					statusCount.getOrDefault(ReviewPerformance + WeightedStr, 0L)
			);
			Long baseTotal = (statusCount.get("constructs") * 100) - statusCount.getOrDefault(Ignored + WeightedStr, 0L);
			compatPct = ((baseTotal - subtract) * 100) / (baseTotal);
			if (debugging) dbgOutput(thisProc() + "compatPct calculation: constructs=[" + statusCount.get("constructs") * 100 + "]  subtract=[" + subtract + "]  baseTotal=[" + baseTotal + "] term1=[" + ((baseTotal - subtract) * 100) + "]  compatPct=[" + compatPct + "] ", debugCalc);
			compatPctStrRaw = Long.toString(compatPct);
			if (compatPct < 0) compatPct = 0;
			if (compatPct > 100) compatPct = 100;
			if ((subtract > 0) && (compatPct == 100)) {
				// don't report 100% if there is anything that is not supported
				compatPct = 99;
			}
			compatPctStr = Long.toString(compatPct);
		}

		boolean customWeights = false;  // if there are custom-defined weights, report here
		String customWeightsMsg = "";
		if (customWeights) {
			customWeightsMsg = "Custom compatibility weights used: <list> \n";
		}

		if (showPercentage) {
			// the compatibility percentage is not really a relevant number since the different elements are all equally weighted. 
			// it brings a risk of misinterpretation, so we don't show this anymore
			summarySection.append(composeSeparatorBar("Compatibility Estimate", tagEstimate));
			summarySection.append("\n");
			summarySection.append("Estimated compatibility for " + babelfishProg + " v." + targetBabelfishVersion + " : " + compatPctStr + "%" + "\n");
			summarySection.append("WARNING: this percentage has little meaning. Avoid using it.\n");
			summarySection.append(customWeightsMsg);
			summarySection.append("\n");
		}

		summarySection.append(composeSeparatorBar("Object Count", tagObjcount));
		summarySection.append("\n");
		StringBuilder summaryTmp = new StringBuilder();
		for (String objType : objTypeCount.keySet().stream().sorted().collect(Collectors.toList())) { // sort case-SENsitive!
			int loc = objTypeLineCount.getOrDefault(objType, 0);
			StringBuilder locStr = new StringBuilder();
			if (loc > 0) {
				if (objType.startsWith("TABLE")) {
					locStr.append(" (" + loc + " columns)");
				}
				else {
					locStr.append(" (" + loc + " lines SQL)");
				}
			}

			String noIssueCnt = "";
			if (objTypeIssueMap.containsKey(objType)) {
				String anchorName = reportObjectsIssuesListTag(objType); 
				String linkStart = "<a href=\"#"+anchorName+"\">";
				String linkEnd = "</a>";
				if (!showObjectIssuesList) {
					linkStart = linkEnd = "";
				}
				noIssueCnt = "  ("+linkStart+"without issues: " + objTypeNoIssueCount.getOrDefault(objType,0) + " of " + objTypeCount.get(objType) + linkEnd +")";
			}
			summaryTmp.append(lineIndent).append(objType + " : " + objTypeCount.get(objType) + locStr.toString() + noIssueCnt + "\n");
		}
		if (summaryTmp.length() > 0) {
			summaryTmp = new StringBuilder(alignColumn(summaryTmp, " : ", "before", "left"));
			summaryTmp = new StringBuilder(alignColumn(summaryTmp, " : ", "after", "right"));
		}
		else {
			summaryTmp = new StringBuilder("No objects were found.\n");
		}
		summarySection.append(summaryTmp);
		
		writeReportFile(summarySection);

		writeReportFile();
		writeReportFile(composeOutputLine("=== SQL Features Report ", "="));

		// sort for status summary
		List<String> sortedList = itemCount.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
		sortedList.add(stringRepeat(lastItem + sortKeySeparator, 5));

		for (int i=0; i <supportOptionsIterate.size(); i++) {
			reportSummaryStatus(supportOptionsIterate.get(i), sortedList, itemCount, appItemList);
		}
		sortedList.clear();
		itemCount.clear();
		appItemList.clear();

		if (!rewrite) {
			if (rewriteOppties.containsKey(rewriteOpptiesTotal)) {
				if (rewriteOppties.get(rewriteOpptiesTotal) > 0) {
					StringBuilder rStr = new StringBuilder();
					rStr.append("\n");
					rStr.append(composeSeparatorBar("Automatic SQL Rewrite Opportunities", tagRewrite));
					rStr.append("\n" + rewriteOppties.get(rewriteOpptiesTotal) + " unsupported SQL aspects that may be rewritten with the -rewrite option:\n\n");
									
					for (String r : rewriteOppties.keySet().stream().sorted().collect(Collectors.toList())) { 
						if (r.equals(rewriteOpptiesTotal)) continue;
						Integer cnt = rewriteOppties.get(r);
						rStr.append(lineIndent + r + " : " + cnt.toString() + "\n");
					}				
					
					writeReportFile(rStr);
				}
			}
		}	

		// sort for X-ref by feature
		List<String> sortedListXRefByFeature = xRefByFeature.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
		sortedListXRefByFeature.add(stringRepeat(lastItem + sortKeySeparator, 20));
		xRefByFeature.clear();
		for (int i=0; i <supportOptionsIterate.size(); i++) {
			reportXrefByFeature(supportOptionsIterate.get(i), sortedListXRefByFeature);
		}
		sortedListXRefByFeature.clear();

		// sort for X-ref by object
		List<String> sortedListXRefByObject = xRefByObject.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
		sortedListXRefByObject.add(stringRepeat(lastItem + sortKeySeparator, 20));
		xRefByObject.clear();
		for (int i=0; i <supportOptionsIterate.size(); i++) {
			reportXrefByObject(supportOptionsIterate.get(i), sortedListXRefByObject);
		}
		sortedListXRefByObject.clear();

		if (showObjectIssuesList) {	
			reportObjectsIssues(objTypeMapCase, objIssueCount);
		}
		
		writeReportFile();
		writeReportFile(composeOutputLine("", "="));
		writeReportFile();
		
		appOutput("\n", false, true);		

		return true;
	}

	private void reportObjectsIssues(Map<String, String> objTypeMap, Map<String,Integer> objIssueCount) throws IOException {
		Map<String, Integer> objTypes = new HashMap<>();
		for (Map.Entry<String,String> e : objTypeMap.entrySet()) {
			String c = e.getKey();
			String type = e.getValue();
			Integer issues = objIssueCount.getOrDefault(c.toUpperCase(),0);
			objTypes.put(type, 0);
			//appOutput(thisProc()+"c=["+c+"] type=["+type+"] issues=["+issues+"] ");
		}

		for (String type : objTypes.keySet().stream().sorted().collect(Collectors.toList())) {
			reportObjectsIssuesList(type, objTypeMap, objIssueCount);
		}
	}

	private void reportObjectsIssuesList(String objType, Map<String, String> objTypeMap, Map<String,Integer> objIssueCount) throws IOException {	
		String objTypeFmt = objType;
		if (objTypeFmt.contains(", ")) {
			Integer i = objTypeFmt.indexOf(", ");
			objTypeFmt = objTypeFmt.substring(i+2) + " " + objTypeFmt.substring(0,i);
		}	
		StringBuilder lines = new StringBuilder();
		boolean withIssues = true;		
		while (true) {			 
			String withStr = "without detected issues, expected to be created without errors:";
			if (withIssues) withStr = "with issues, may raise errors when created (for details, see report above):";			
			StringBuilder line = new StringBuilder();
			Integer cnt = 0;
			
			for (String objName : objTypeMap.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList())) {
				String type = objTypeMap.get(objName);
				if (!type.equals(objType)) continue;
				Integer issues = objIssueCount.getOrDefault(objName.toUpperCase(),0);
				if (!withIssues && (issues == 0)) {
					line.append(lineIndent + objName+"\n");
					cnt++;
				}	
				else if (withIssues && (issues > 0)) {
					line.append(lineIndent + objName+" ("+issues+" issues)\n");
					cnt++;
				}
			}
			if (line.length() > 0) {
				lines.append(cnt + " " + capitalizeInitChar(objTypeFmt.toLowerCase()) +"s "+withStr+"\n");	
				lines.append(line);	
				lines.append("\n");	
			}
			
			if (!withIssues) break;
			withIssues = false;
		}
		
		if (lines.length() > 0) {
			String tag = reportObjectsIssuesListTag(objType);
			writeReportFile(composeSeparatorBar("List of "+capitalizeInitChar(objTypeFmt.toLowerCase())+"s with/without issues", tag)+"\n");
			writeReportFile(lines);
		}
	}

	public String reportObjectsIssuesListTag(String objType) {
		String result = applyPatternAll(objType.toLowerCase(), "\\W", "");	
		result += "_issueslist";		
		return result;	
	}

	public void importPG(boolean append, List<String> pgImportFlags) throws IOException {
		// platform-dependent parts
		String envvarSet = "SET ";
		String cmdSeparator = "& ";
		String envvarPrefix = "%";
		String envvarSuffix = "%";
		if (onMac || onLinux) {
			envvarSet = "export ";		
			cmdSeparator = "; ";
			envvarPrefix = "\\$";
			envvarSuffix = "";
		}
				
		// Check for code injection risks - though it's difficult to see what could go wrong given that 
		// the current session as well as the PG session are owned by this user anyway.
		// But let's do the right thing.
		validatePGImportArg(cmdSeparator, pgImportFlags.get(2), "username");
		validatePGImportArg(cmdSeparator, pgImportFlags.get(3), "password");
		validatePGImportArg(cmdSeparator, pgImportFlags.get(0), "host");
		validatePGImportArg(cmdSeparator, pgImportFlags.get(1), "port");
		validatePGImportArg(cmdSeparator, pgImportFlags.get(4), "database name");	
		
		// get capture files
		List<Path> captureFiles = getCaptureFiles(reportName);
		if (debugging) dbgOutput(thisProc() + "captureFiles(" + captureFiles.size() + ")=[" + captureFiles + "] ", debugReport);
		if (captureFiles.size() == 0) {
			appOutput("No analysis files found. Use -analyze to perform analysis and generate a report.");
			errorExit();
		}
		String cfv = captureFilesValid("import", captureFiles, true);
		if (!cfv.isEmpty()) {
			// print error message and exit
			appOutput(cfv);
			errorExit();
		}
		String cfVersion = captureFilesValid("tgtversion", captureFiles, true);

		Date now = new Date();
		String nowFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);

		int capCount = 0;
		BufferedWriter PGImportFileWriter = null;
    	String PGImportFilePathName = getPGImportFilePathname(reportName);
    	checkDir(getReportDirPathname(reportName, capDirName), true);
		PGImportFileWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(PGImportFilePathName), StandardCharsets.UTF_8)));

		for (Path cf : captureFiles) {
			String cfLine = captureFileFirstLine(cf.toString());   // read only first line
			String cfReportName = captureFileAttribute(cfLine, 1);
			if (cfReportName.isEmpty()) {
				appOutput("Invalid format in "+cfReportName+"; run with -analyze to fix.");
				errorExit();
			}
			if (!reportName.equalsIgnoreCase(cfReportName)) {
				String rDir = getFilePathname(getDocDirPathname(), capDirName);
				appOutput("Found analysis file for report '" + cfReportName + "' in " + rDir + ": adding to import");
			}

			FileInputStream cfis = new FileInputStream(new File(cf.toString()));
			InputStreamReader cfisr = new InputStreamReader(cfis, StandardCharsets.UTF_8);
			BufferedReader capFile = new BufferedReader(cfisr);

			String capLine = "";

			while (true) {
				capLine = capFile.readLine();
				if (capLine == null) {
					//EOF
					break;
				}
				capLine = capLine.trim();
				if (capLine.isEmpty()) continue;
				if ((capLine.charAt(0) == '#') || (capLine.charAt(0) == '*')) {
					continue;
				}
				if (capLine.contains(captureFileSeparator+ObjCountOnly+captureFileSeparator)) continue;

				capCount++;

				// strip off the last two semicolons
				capLine = capLine.substring(0,capLine.lastIndexOf(captureFileSeparator));
				capLine = capLine.substring(0,capLine.lastIndexOf(captureFileSeparator));
				
				capLine = unEscapeHTMLChars(capLine);

				// add date & babelfish version
				capLine = cfVersion + captureFileSeparator + nowFmt + captureFileSeparator + capLine;

				PGImportFileWriter.write(capLine+"\n");
			}
			capFile.close();
		}
		PGImportFileWriter.close();
		appOutput("Items written for import: "+capCount + " (in "+PGImportFilePathName+")");
		
		// do not write the password etc. in any file but keep in envvar only:
		String PGUserEnvvar   = "BBFCOMPASSPSQLUSERNAME";
		String PGPasswdEnvvar = "BBFCOMPASSPSQLPASSWD";
		String PGHostEnvvar   = "BBFCOMPASSPSQLHOST";
		String PGPortEnvvar   = "BBFCOMPASSPSQLPORT";
		String PGDBnameEnvvar = "BBFCOMPASSPSQLDBNAME";					
		
		String psqlCmd= "psql --echo-all --file=~file~ \"postgresql://~username~:~password~@~host~:~port~/~dbname~\"";
		psqlCmd= applyPatternFirst(psqlCmd, "~username~", envvarPrefix+ PGUserEnvvar   +envvarSuffix);
		psqlCmd= applyPatternFirst(psqlCmd, "~password~", envvarPrefix+ PGPasswdEnvvar +envvarSuffix);
		psqlCmd= applyPatternFirst(psqlCmd, "~host~",     envvarPrefix+ PGHostEnvvar   +envvarSuffix);
		psqlCmd= applyPatternFirst(psqlCmd, "~port~",     envvarPrefix+ PGPortEnvvar   +envvarSuffix);
		psqlCmd= applyPatternFirst(psqlCmd, "~dbname~",   envvarPrefix+ PGDBnameEnvvar +envvarSuffix);

		String psqlFile = psqlImportFileName + "." + psqlFileSuffix;
		psqlCmd= applyPatternFirst(psqlCmd, "~file~", psqlFile);

		String cmdFile = writePsqlFile(append, reportName, psqlCmd);

		// compose the command line
		String runCmd= ""; 
		runCmd += envvarSet+PGUserEnvvar+"="+pgImportFlags.get(2)+cmdSeparator;
		runCmd += envvarSet+PGPasswdEnvvar+"="+pgImportFlags.get(3)+cmdSeparator;
		runCmd += envvarSet+PGHostEnvvar+"="+pgImportFlags.get(0)+cmdSeparator;
		runCmd += envvarSet+PGPortEnvvar+"="+pgImportFlags.get(1)+cmdSeparator;
		runCmd += envvarSet+PGDBnameEnvvar+"="+pgImportFlags.get(4)+cmdSeparator;
		if (onMac || onLinux) {
			runCmd += "chmod +x "+cmdFile+cmdSeparator;
		}
		runCmd += cmdFile;
		
		if (debugging) dbgOutput(thisProc() + "runCmd=["+runCmd+"]  ", debugReport);

		// finally, run the import
		runOScmd(runCmd);

		//done!
	}
	
	private void validatePGImportArg(String cmdSeparator, String envvar, String name) {
		cmdSeparator = cmdSeparator.trim();
		if (envvar.contains(cmdSeparator)) {
			appOutput("Value '"+envvar+"' for '"+name+"' contains invalid character(s): '"+cmdSeparator+"'");
			errorExit();
		}
	}

	public String getUserCfgFilePathName(String fileName) throws IOException {
		String dirPath = getDocDirPathname();
		String filePath = getFilePathname(dirPath, fileName);
		return filePath;
	}
	
	public String openUserCfgFileAppend(String fileName) throws IOException {
		return openUserCfgFile(fileName, false);
	}
	public String openUserCfgFileNew(String fileName) throws IOException {
		return openUserCfgFile(fileName, true);
	}
	public String openUserCfgFile(String fileName, boolean newFile) throws IOException {
		checkDir(getDocDirPathname(), false, true);
		String userCfgFilePathName = getUserCfgFilePathName(fileName);
		userCfgFileWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(userCfgFilePathName, (!newFile)), StandardCharsets.UTF_8)));
		String now = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());
		if (newFile) {
			String initLine = "# This file created at " + now + " by " + thisProgName + " version " + thisProgVersion + ", " + thisProgVersionDate;
			writeUserCfgFile(initLine);
			initLine = 
"#------------------------------------------------------------------------------\n" +
"#\n" + 
"# Babelfish Compass user .cfg file \n" + 
"#\n" + 
"# This file allows the user of Babelfish Compass to override the classification\n" + 
"# of not-supported features and of reporting groups.\n" + 
"# This file is automatically created by Babelfish Compass; the sections in this\n" + 
"# file will be identical to those in BabelfishFeatures.cfg. Do not modify the \n" + 
"# section headers!\n" + 
"# Users can add the following entries in a section:\n" + 
"#    default_classification=<value>\n" + 
"#    default_classification-<value>=commalist\n" + 
"#    report_group=<value>\n" + 
"#    report_group-<value>=commalist\n" + 
"#\n" + 
"# Examples:\n" + 
"#    [Built-in functions]\n" + 
"#    default_classification-Ignored=FULLTEXTSERVICEPROPERTY   # this often occurs in SSMS-generated scripts, assume it can be ignored\n" + 
"#\n" + 
"#    [DESC constraint]\n" + 
"#    default_classification=ReviewManually\n" + 
"#    report_group=Indexing\n" + 
"#\n" + 
"# NB: overriding the classification is possible only for items which are not \n" + 
"# classified as 'Supported': overrides for supported items will be ignored. \n" + 
"# Also, an item cannot be reclassified as 'Supported': only values 'Ignored',\n"+
"# 'ReviewSemantics', 'ReviewPerformance' and 'ReviewManually' can be used.\n"+
"#\n" + 
"#------------------------------------------------------------------------------\n" +
"#\n";

			writeUserCfgFile(initLine);
		}
		else {			
			String initLine = "\n# Sections below were added at " + now + " by " + thisProgName + " version " + thisProgVersion + ", " + thisProgVersionDate;
			writeUserCfgFile(initLine);
		}
		return userCfgFilePathName;
	}

	public void writeUserCfgFile(String line) throws IOException {
		userCfgFileWriter.write(line + "\n");
		userCfgFileWriter.flush();
	}

    public void closeUserCfgFile() throws IOException {
    	if (userCfgFileWriter == null) return;
    	String now = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());
		String endLine = "# end ("+now+")\n";
		writeUserCfgFile(endLine);    	
	    userCfgFileWriter.close();
	    userCfgFileWriter = null;
	}
	
	public void logGroupOverride(String groupOrig, String group, String section) {
		logGroupOverride(groupOrig, group, section, "");
	}
	public void logGroupOverride(String groupOrig, String group, String section, String name) {
		String key = group+overrideSeparator+groupOrig;
		groupOverrides.put(key, groupOverrides.getOrDefault(key,0)+1);
		
		key = group+overrideSeparator+groupOrig+overrideSeparator+section+overrideSeparator+name;
		groupOverridesDetail.put(key, groupOverridesDetail.getOrDefault(key,0)+1);
	}
	
	public void logStatusOverride(String statusOrig, String status, String section) {
		logStatusOverride(statusOrig, status, section, "");		
	}	
	public void logStatusOverride(String statusOrig, String status, String section, String name) {
		String key = status+overrideSeparator+statusOrig;
		statusOverrides.put(key, statusOverrides.getOrDefault(key,0)+1);
		
		key = status+overrideSeparator+statusOrig+overrideSeparator+section+overrideSeparator+name;
		statusOverridesDetail.put(key, statusOverridesDetail.getOrDefault(key,0)+1);		
	}	
	
	// keep track of added characters
	public void addOffsets(Integer iteration) {						
		Map<Integer, Map<Integer, Integer>> offsetIteration = new LinkedHashMap<>();
		for (int i = offsetCols.size(); i < iteration; i++) {
			offsetCols.add(offsetIteration);
			if (debugging) dbgOutput(thisProc()+"initializing iteration=["+(i+1)+"] at ["+i+"]", debugRewrite);
		}
	}
	
	public void addOffsets(Integer iteration, Integer lineNo, Integer lineNoOrig, Integer startColOrig, String origStr, String newStr, String newStrNoComment, String report, String fName) {						
		addOffsets(iteration);
		assert (offsetCols.get(iteration-1) != null) : thisProc()+"iteration=["+iteration+"] not found in offsetCols";
		
		List<String> linesOrig = new ArrayList<>(Arrays.asList(origStr.split("\n")));
		List<String> linesNew  = new ArrayList<>(Arrays.asList(newStr.split("\n")));
		
		int numChars = newStr.length() - origStr.length();
		if (debugging) dbgOutput(thisProc()+"adding: iteration=["+iteration+"] lineNo=["+lineNo+"] lineNoOrig=["+lineNoOrig+"] startColOrig=["+startColOrig+"]  numChars=["+numChars+"] linesOrig=["+linesOrig.size()+"]  linesNew=["+linesNew.size()+"] fName=["+fName+"] ", debugRewrite);
		
		Map<Integer, Map<Integer, Integer>> offsetIteration = new LinkedHashMap<>();
		offsetIteration = offsetCols.get(iteration-1);
		
		if (linesOrig.size() == linesNew.size()) {
			// #lines remains the same
			for (int i = lineNo; i<=(lineNo+linesNew.size()-1); i++) {
				
				int diffLength = linesNew.get(i-lineNo).length() - linesOrig.get(i-lineNo).length();
				if (debugging) dbgOutput(thisProc()+"lineNo=i=["+i+"] diffLength=["+diffLength+"]", debugRewrite);
				if (diffLength == 0) continue;
								
				Map<Integer, Integer> offsetLineNo = new LinkedHashMap<>();
				if (!offsetIteration.containsKey(i)) offsetIteration.put(i, offsetLineNo);
				offsetLineNo = offsetIteration.get(i);
				
				int col = 0;
				if (i == lineNo) col = startColOrig;
				offsetLineNo.put(col, diffLength);	
			}					
		}
		else if (linesOrig.size() < linesNew.size()) {
			// for lines that have been added: indicate by col = -1, and the #lines added as diffLength
			for (int i = lineNo; i<=(lineNo+linesNew.size()-1); i++) {
				Map<Integer, Integer> offsetLineNo = new LinkedHashMap<>();
				if (!offsetIteration.containsKey(i)) offsetIteration.put(i, offsetLineNo);
				offsetLineNo = offsetIteration.get(i);	
								
				if (i == (lineNo+linesOrig.size()-1)) {					
					int extraLines = linesNew.size() - linesOrig.size();
					offsetLineNo.put(-1, extraLines);	
					
					Map<Integer, Integer> lineNrOffset = new LinkedHashMap<>();
					if (!offsetLines.containsKey(fName)) offsetLines.put(fName, lineNrOffset);
					lineNrOffset = offsetLines.get(fName);
					lineNrOffset.put(i,extraLines);
					
					//if (debugging) dbgOutput(thisProc()+"lineNo=i=["+i+"] adding "+extraLines+" extra lines at col= -1", debugRewrite);
					break;	
				}

				int origLen = linesOrig.get(i-lineNo).length();
				int diffLength = linesNew.get(i-lineNo).length() - origLen;
				//if (debugging) dbgOutput(thisProc()+"lineNo=i=["+i+"] origLen=["+origLen+"] newlen=["+linesNew.get(i-lineNo).length()+"]  diffLength=["+diffLength+"]", debugRewrite);
				if (diffLength == 0) continue;
				
				int col = 0;
				if (i == lineNo) col = startColOrig;
				offsetLineNo.put(col, diffLength);	
			}					
		}
		else {
			// never reduce the number of lines
			assert false : thisProc()+"bad branch, linesOrig=["+linesOrig.size()+"]  linesNew=["+linesNew.size()+"]";
		}
		
		// log the rewrite				
		origStr = applyPatternAll(origStr, rwrTabRegex, "");	
		if (origStr.length() > 100) origStr = origStr.substring(0,100) + "(...)";
		if (newStrNoComment.length() > 100) newStrNoComment = newStrNoComment.substring(0,100) + "(...)";
		newStrNoComment = newStrNoComment.replace("/*", "/ *"); // avoid generating a nested bracketed comment causing 'reset' to be seen as a proc call
		newStrNoComment = newStrNoComment.replace("*/", "* /");
		String msg = String.format("%08d", lineNoOrig) + captureFileSeparator +  String.format("%08d", rewritesDone.size()) + captureFileSeparator +  Integer.toString(lineNoOrig + linesOrig.size() - 1) + captureFileSeparator+ report+": changed ["+origStr+"] to ["+newStrNoComment+"]";
		rewritesDone.add(msg);
	}


	public void resetRewrites() {						
		rewriteTextListKeys.clear();
		rewriteTextList.clear();
		rewriteTextListOrigText.clear();
		rewriteIDDetails.clear();
		offsetCols.clear();
		offsetLines.clear();
		rewritesDone.clear();
	}
		
	// calculate adjusted line number, taking earlier added lines into account
	public Integer calcOffsetLine (Integer iteration, Integer lineOrig) {		
		Integer lineNew = lineOrig;
		if (debugging) dbgOutput(thisProc()+"entry: iteration=["+iteration+"] lineOrig=["+lineOrig+"] offsetCols.size()=["+offsetCols.size()+"] ", debugRewrite);
		if (iteration > 0) {
			for (int i = 0; i < iteration; i++) {		
				if (i >= offsetCols.size()) {
					if (debugging) dbgOutput(thisProc()+"i=["+i+"], exiting", debugRewrite);
					break;		
				}
				Map<Integer, Map<Integer, Integer>> offsetIteration = new LinkedHashMap<>();
				offsetIteration = offsetCols.get(i);
				for (Integer lineNo : offsetIteration.keySet().stream().sorted().collect(Collectors.toList())) {
					for (Integer colx : offsetIteration.get(lineNo).keySet().stream().sorted().collect(Collectors.toList())) {
						Integer offset = offsetIteration.get(lineNo).get(colx);
						if (debugging) dbgOutput(thisProc()+"     offsets: lineNo=["+lineNo+"] colx=["+colx+"] offset=["+offset+"]", debugRewrite);
						if (colx != -1) continue; // this is not an added-line count
						if (lineOrig >= lineNo) {
							lineNew += offset;
							if (debugging) dbgOutput(thisProc()+"Adding line offset=["+offset+"], lineNew=["+lineNew+"]", debugRewrite);
						}							
					}
				}						
			}
		}
		if (debugging) dbgOutput(thisProc()+"result: lineNew=["+lineNew+"]", debugRewrite);
		return lineNew;
	}
	
			
	// calculate adjusted line number for the hyperlinks
	public Integer calcOffsetLineHLink (String fName, Integer lineOrig) {	
		Integer lineNew = lineOrig;
		if (debugging) dbgOutput(thisProc()+"entry: fName=["+fName+"] lineOrig=["+lineOrig+"]", debugRewrite);
		Map<Integer, Integer> lineNrOffset = new LinkedHashMap<>();		
		if (fName.isEmpty()) {
			fName = offsetLines.keySet().iterator().next();
		}
		if (offsetLines.containsKey(fName)) {			
			lineNrOffset = offsetLines.get(fName);	
			
			for (Integer l : lineNrOffset.keySet().stream().sorted().collect(Collectors.toList())) {
				Integer extraLines = lineNrOffset.get(l);
				if (debugging) dbgOutput(thisProc()+"fName=["+fName+"] line=["+l+"] extraLines=["+extraLines+"]", debugRewrite);				
				if (lineOrig >= l) {
					lineNew += extraLines;
					if (debugging) dbgOutput(thisProc()+"Adding extraLines=["+extraLines+"], lineNew=["+lineNew+"]", debugRewrite);
				}					
			}	
		}						
		if (debugging) dbgOutput(thisProc()+"result: lineNew=["+lineNew+"]", debugRewrite);
		return lineNew;
	}			
	
	// calculate adjusted column position, taking earlier added chars into account
	public Integer calcOffsetCol (Integer iteration, Integer lineNo, Integer col) {		
		Integer colNew = col;
		if (debugging) dbgOutput(thisProc()+"entry: iteration=["+iteration+"] lineNo=["+lineNo+"] col=["+col+"]", debugRewrite);
		if (iteration > 0) {
			if (offsetCols.size() >= iteration) {		
				for (int i = 0; i < iteration; i++) {
					Map<Integer, Map<Integer, Integer>> offsetIteration = new LinkedHashMap<>();
					offsetIteration = offsetCols.get(i);
					if (offsetIteration.containsKey(lineNo)) {
						if (debugging) dbgOutput(thisProc()+"found "+offsetIteration.get(lineNo).keySet().size()+" entries for iteration=["+(i+1)+"] lineNo=["+lineNo+"]", debugRewrite);
						for (Integer colx : offsetIteration.get(lineNo).keySet().stream().sorted().collect(Collectors.toList())) {
							Integer offset = offsetIteration.get(lineNo).get(colx);
							if (debugging) dbgOutput(thisProc()+"   lineNo=["+lineNo+"] colx=["+colx+"] offset=["+offset+"]", debugRewrite);
							if (colx == -1) continue; // this is an added-line count
							if (col > colx) {
								colNew += offset;
								if (debugging) dbgOutput(thisProc()+"Adding offset=["+offset+"], colNew=["+colNew+"]", debugRewrite);
							}
						}			
					}
				}
			}
		}
		if (debugging) dbgOutput(thisProc()+"result: colNew=["+colNew+"]", debugRewrite);
		return colNew;
	}
	
	// calculate adjusted length, taking earlier added chars into account
	public Integer calcOffsetLength (Integer iteration, Integer startLineNo, Integer startCol, Integer endLineNo, Integer endCol) {			
		Integer lengthNew = 0;
		if (debugging) dbgOutput(thisProc()+"entry: iteration=["+iteration+"] startLineNo=["+startLineNo+"] startCol=["+startCol+"] endLineNo=["+endLineNo+"] endCol=["+endCol+"] offsetCols.size()=["+offsetCols.size()+"] ", debugRewrite);
		if (iteration > 0) {
			if (offsetCols.size() >= iteration) {		
				for (int i = 0; i < iteration; i++) {
					if (debugging) dbgOutput(thisProc()+"i=["+i+"]", debugRewrite);
					Map<Integer, Map<Integer, Integer>> offsetIteration = new LinkedHashMap<>();	
					offsetIteration = offsetCols.get(i);
					if (debugging) dbgOutput(thisProc()+"offsetIteration.size()=["+offsetIteration.size()+"] ", debugRewrite);
					for (Integer lineNo=startLineNo; lineNo<=endLineNo; lineNo++) {
						if (debugging) dbgOutput(thisProc()+"lineNo=["+lineNo+"]", debugRewrite);
						if (offsetIteration.containsKey(lineNo)) {
							if (debugging) dbgOutput(thisProc()+"found entries for iteration=["+(i+1)+"] lineNo=["+lineNo+"]", debugRewrite);
							for (Integer colx : offsetIteration.get(lineNo).keySet().stream().sorted().collect(Collectors.toList())) {
								Integer offset = offsetIteration.get(lineNo).get(colx);
								if (debugging) dbgOutput(thisProc()+"   lineNo=["+lineNo+"] colx=["+colx+"] offset=["+offset+"]", debugRewrite);
								if (colx == -1) continue; // this is an added-line count
								
								if ((lineNo.equals(startLineNo)) && (startCol > colx)) {
									if (debugging) dbgOutput(thisProc()+"   == startLineNo: before startCol, skip, lengthNew=["+lengthNew+"]", debugRewrite);
									continue;
								}
								if ((lineNo.equals(startLineNo)) && (colx > endCol)) {
									if (debugging) dbgOutput(thisProc()+"   == startLineNo: after endCol, skip, lengthNew=["+lengthNew+"]", debugRewrite);
									continue;
								}
																
								if ((lineNo.equals(startLineNo)) && (colx >= startCol)) {
									lengthNew += offset;
									if (debugging) dbgOutput(thisProc()+"   == startLineNo: Adding offset=["+offset+"], lengthNew=["+lengthNew+"]", debugRewrite);
								}
								else if ((lineNo.equals(endLineNo)) && (endCol > colx)) {
									lengthNew += offset;
									if (debugging) dbgOutput(thisProc()+"   == endLineNo: Adding offset=["+offset+"], lengthNew=["+lengthNew+"]", debugRewrite);
								}
								else if ((lineNo > startLineNo) && (lineNo < endLineNo)) {
									lengthNew += offset;
									if (debugging) dbgOutput(thisProc()+"   >startlineNo, < endlineNo: Adding offset=["+offset+"], lengthNew=["+lengthNew+"]", debugRewrite);
								}
								else {
									if (debugging) dbgOutput(thisProc()+"   no match, lengthNew=["+lengthNew+"] ", debugRewrite);
								}
							}	
						}		
					}
				}
			}
		}
		if (debugging) dbgOutput(thisProc()+"result: lengthNew=["+lengthNew+"]", debugRewrite);
		return lengthNew;
	}

	public void dumpOffsetCols(String s) {
		if (debugging) dbgOutput(thisProc()+s+": offsetCols: #iteration levels=["+offsetCols.size()+"]", debugRewrite);
		for (int i = 0; i < offsetCols.size(); i++) {
			if (debugging) dbgOutput(thisProc()+"iteration=["+(i+1)+"] at i=["+i+"]", debugRewrite);
			Map<Integer, Map<Integer, Integer>> offsetIteration = new LinkedHashMap<>();
			offsetIteration = offsetCols.get(i);
			for (Integer lineNo : offsetIteration.keySet().stream().sorted().collect(Collectors.toList())) {
				//if (debugging) dbgOutput(thisProc()+"   offsets: lineNo=["+lineNo+"]", debugRewrite);
				for (Integer col : offsetIteration.get(lineNo).keySet().stream().sorted().collect(Collectors.toList())) {
					if (debugging) dbgOutput(thisProc()+"     offsets: lineNo=["+lineNo+"] col=["+col+"] offset=["+offsetIteration.get(lineNo).get(col)+"]", debugRewrite);
				}
			}		
		}
	}

	public void dumpOffsetLines(String s) {
		if (debugging) dbgOutput(thisProc()+s+": offsetLines: #files=["+offsetLines.size()+"]", debugRewrite);
		for (String f : offsetLines.keySet().stream().sorted().collect(Collectors.toList())) {
			Map<Integer, Integer> lineNrOffset = new LinkedHashMap<>();
			lineNrOffset = offsetLines.get(f);	
			for (Integer l : lineNrOffset.keySet().stream().sorted().collect(Collectors.toList())) {
				Integer extraLines = lineNrOffset.get(l);
				if (debugging) dbgOutput(thisProc()+"f=["+f+"] line=["+l+"] extraLines=["+extraLines+"]", debugRewrite);
			}	
		}
	}

	// apply text substitutions 
	public void performRewriting(String reportName, String appName, String inFileCopy) throws IOException {
		if (debugging) dbgOutput(thisProc()+"performing rewrites=["+rewriteTextListKeys.size()+"] to copy of ["+inFileCopy+"]", debugRewrite);
		if (rewriteTextListKeys.size() == 0) return;
		
		// determine whether there are any substitutions that are overlapping with other ones
		// when sorted by startPos, the larger range will be sorted first, and the smaller range within that range will be next
		// subs which contain a smaller sub will be processed in a second pass, since the smaller sub may also extend over multiple lines

		int startLine = -1;
		int startCol = -1;		
		int endLine = -1;
		int endCol = -1;		
		int startPos = -1;
		int endPos = -1;
		int origLen = -1;
		int batchLine = -1;
		int batchNo = -1;
		int batchNoPrev = -1;
		int batchLineInFile = -1;
		String srcFile = "";
		String rewriteType = "";
		String rewriteText = "";
		String report = "";
		int startPrev = -1;
		int endPrev = -1;
		String sPrev = "";
		List<String> tmpRemovedItems = new ArrayList<>();
		List<String> tmpToDoItems = new ArrayList<>();
		tmpToDoItems.addAll(rewriteTextListKeys);
		
		// one-time init
		if (rwrTabRegex.isEmpty()) rwrTabRegex = escapeRegexChars(rwrTag.trim());
		
		// next: copy inputfile copy to tmp file; read tmp file as input; write target file as output
		// next: keep track of added lines/columns and adjust in subsequent cycles
		String rewrittenDir = getReportDirPathname(reportName, rewrittenDirName);
		String rewrittenHTMLDir = getReportDirPathname(reportName, rewrittenDirName, rewrittenHTMLDirName);
		checkDir(rewrittenDir, false, true);	
		checkDir(rewrittenHTMLDir, false, true);	
					
		String tmpFile = getFilePathname(rewrittenDir, rewrittenTmpFile);	
		String fName = Paths.get(inFileCopy).getFileName().toString().replaceAll(importFileTag, rewrittenFileTag);
		String rewrittenFile = getFilePathname(rewrittenDir, fName); 
		String rewrittenHTMLFile = getFilePathname(rewrittenHTMLDir, fName); 
		rewrittenHTMLFile = changeFilenameSuffix(rewrittenHTMLFile, importFileSuffix, HTMLSuffix);
		String inFile = inFileCopy;
		
		Integer tmpCnt = 0;
		Integer iteration = -1;
		boolean abortNow = false;
				
		while (tmpToDoItems.size() > 0) {
			iteration++;
			if (debugging) dbgOutput(thisProc()+"top: iteration=["+iteration+"]", debugRewrite);

			checkDir(rewrittenDir, true, true);	
			checkDir(rewrittenHTMLDir, true, true);	
			deleteFile(tmpFile);
			copyFile(inFile, tmpFile);
			if (debugging) dbgOutput(thisProc()+"copied inFile=["+inFile+"] to tmpFile=["+tmpFile+"] target rewrittenFile=["+rewrittenFile+"] rewrittenHTMLFile=["+rewrittenHTMLFile+"] ", debugRewrite);

			openRewrittenFile(reportName, appName, tmpFile, rewrittenFile);
			inFile = rewrittenFile;
			
			// DEBUG Only: for next iteration, keep temp file
			if (debugging && debugRewrite) {
				tmpCnt++;
				tmpFile = applyPatternFirst(tmpFile, "\\.\\d+$", "") + "." + tmpCnt.toString();
			}

			List<String> tmpSorted = tmpToDoItems.stream().sorted().collect(Collectors.toList());
			tmpRemovedItems.clear();
			if (debugging) dbgOutput(thisProc()+"tmpToDoItems top=["+tmpToDoItems.size()+"]", debugRewrite);
			
			for (String s : tmpToDoItems.stream().sorted().collect(Collectors.toList())) {
				if (debugging) dbgOutput(thisProc()+"s=["+s+"]", debugRewrite);
				List<String> tmp = new ArrayList<>(Arrays.asList(s.split(captureFileSeparator)));
				batchNo  = Integer.parseInt(tmp.get(0));				
				startPos = Integer.parseInt(tmp.get(1));
				endPos = Integer.parseInt(tmp.get(2));
				startLine = Integer.parseInt(tmp.get(3));
				startCol  = Integer.parseInt(tmp.get(4));
				endLine = Integer.parseInt(tmp.get(6));
				endCol  = Integer.parseInt(tmp.get(7));
				origLen  = endPos - startPos + 1;
				rewriteType  = tmp.get(8);
				report   = tmp.get(9);
				rewriteText = rewriteTextList.get(s);
				if (debugging) dbgOutput(thisProc()+"startLine=["+startLine+"] startCol=["+startCol+"] startPos=["+startPos+"] endPos=["+endPos+"] endLine=["+endLine+"] endCol=["+endCol+"] origLen=["+origLen+"] rewriteType=["+rewriteType+"]  rewriteText=["+rewriteText+"] report=["+report+"]", debugRewrite);
				
				if (batchNoPrev == batchNo) {
					if (startPos >= startPrev && endPos <= endPrev) {
						// this one falls inside the range of the previous one
						// delete the previous one from the sorted list
						if (debugging) dbgOutput(thisProc()+"this rewrite is within range of previous. deleting previous sPrev=["+sPrev+"]", debugRewrite);
						tmpRemovedItems.add(sPrev);
						tmpSorted.remove(sPrev);
					}
					else if (startPos >= startPrev && endPos > endPrev) {
						// not expecting this, should not be possible
					}
				}
				
				batchNoPrev = batchNo;
				startPrev = startPos;
				endPrev = endPos;
				sPrev = s;
			}
			tmpToDoItems.clear();
			tmpToDoItems.addAll(tmpRemovedItems);
			
			if (debugging && debugRewrite) {
				for (String s : tmpSorted) {
					appOutput(thisProc()+"tmpSorted: after range check: s=["+s+"] ");
				}		
								
				for (String s : tmpToDoItems) {
					appOutput(thisProc()+"tmpToDoItems: after range check: s=["+s+"] ");
				}		
				dumpOffsetCols("after range chk");				
			}		
										
			int rewriteCount = 0;
			boolean nextRewrite = true;
			
			int lineNo = 0;
			int remainingLength = -1;
			boolean endOfFile = false;
			String origStrFull = "";
			int startLineOrig = -1;
			
			while (true) {							
				String line = rewrittenInFileReader.readLine();
				if (line == null) {
					endOfFile = true;
					break;
				}
				if (lineNo == 0) {
					if (!importFileAttribute(line,1).isEmpty()) {
						if (!importFileAttribute(line,2).isEmpty()) {
							// this is the header line from the import copy, discard it
							continue;
						}
					}					
				}
				lineNo++;
				int lineCut = 0;
				boolean keepLine = false;
				int startColOrig = -1;				
				while (true) {		
					if (nextRewrite) {
						String k = tmpSorted.get(rewriteCount);
						rewriteText = rewriteTextList.get(k);
						if (debugging) dbgOutput(thisProc()+"rewriteCount=["+rewriteCount+"]  k=["+k+"] rewriteText=["+rewriteText+"]", debugRewrite);
										
						List<String> tmp = new ArrayList<>(Arrays.asList(k.split(captureFileSeparator)));
						batchNo  = Integer.parseInt(tmp.get(0));	
						startPos = Integer.parseInt(tmp.get(1));
						endPos = Integer.parseInt(tmp.get(2));
						startLine = Integer.parseInt(tmp.get(3));
						startCol  = Integer.parseInt(tmp.get(4));
						endLine = Integer.parseInt(tmp.get(6));
						endCol  = Integer.parseInt(tmp.get(7));
						origLen  = endPos - startPos + 1;
						rewriteType  = tmp.get(8);
						report   = tmp.get(9);

						if (debugging) dbgOutput(thisProc()+"rewriteCount=["+rewriteCount+"] iteration=["+iteration+"] startPos=["+startPos+"] endPos=["+endPos+"] startLine=["+startLine+"] startCol=["+startCol+"] endLine=["+endLine+"] endCol=["+endCol+"] origLen=["+origLen+"] rewriteType=["+rewriteType+"] rewriteText=["+rewriteText+"] report=["+report+"]", debugRewrite);
						
						if (debugging && debugRewrite) dumpOffsetCols("before calc");
										
						startLineOrig = startLine;
						startLine = calcOffsetLine(iteration, startLine);
						if (debugging) dbgOutput(thisProc()+"startLine after adjust=["+startLine+"]", debugRewrite);
						
						Integer startColNew = calcOffsetCol(iteration, startLineOrig, startCol);
						if (debugging) dbgOutput(thisProc()+"startCol=["+startCol+"] startColNew=["+startColNew+"]", debugRewrite);

						Integer endColNew = calcOffsetCol(iteration, endLine, endCol);
						if (debugging) dbgOutput(thisProc()+"endCol=["+startCol+"] endColNew=["+endColNew+"]", debugRewrite);
						
						Integer offsetLength = calcOffsetLength(iteration, startLineOrig, startCol, endLine, endCol);
						Integer origLenNew = origLen + offsetLength;
						if (debugging) dbgOutput(thisProc()+"offsetLength=["+offsetLength+"]", debugRewrite);
						if (debugging) dbgOutput(thisProc()+"origLen=["+origLen+"] origLenNew=["+origLenNew+"]", debugRewrite);
						
						startCol = startColNew;
						startColOrig = startCol;
						origLen = origLenNew;
						
						nextRewrite = false;
					}

					if (debugging) dbgOutput(thisProc()+"lineNo=["+lineNo+"]=["+line+"]  lineCut=["+lineCut+"]", debugRewrite);
					if (debugging) dbgOutput(thisProc()+"startLine=["+startLine+"] remainingLength=["+remainingLength+"]", debugRewrite);					
					if (lineNo == startLine) {
						// reached startline for sub; apply it
						if (debugging) dbgOutput(thisProc()+"*** applying rewrite=["+rewriteCount+"] origLen=["+origLen+"]", debugRewrite);
						keepLine = false; // new rewrite to be done
																
						if (debugging) dbgOutput(thisProc()+"startCol orig=["+startColOrig+"] lineCut=["+lineCut+"] ", debugRewrite);
						if (startColOrig == -1) startColOrig = startCol;
						startCol = startColOrig - lineCut;
						if (debugging) dbgOutput(thisProc()+"startCol adjusted=["+startCol+"] ", debugRewrite);	
																	
						// sanity check on line length
						if (line.length() < startCol+1) {
							// something went wrong, exit without further processing
							appOutput(thisProc()+inFileCopy+": Internal error at line "+lineNo+": length="+line.length()+". expected at least "+(startCol+1)+". Aborting rewrite for this file.");
							appOutput(thisProc()+"line=["+line+"] ");
							abortNow = true;
							if (debugging || devOptions) errorExit(); 
							break;
						}					
						
						// find start & end of original part
						remainingLength = origLen;
						String pre = line.substring(0,startCol);
						lineCut += pre.length();
						writeRewrittenFile(pre);
						if (debugging) dbgOutput(thisProc()+"pre=["+pre+"] ", debugRewrite);
								
						if (debugging) dbgOutput(thisProc()+"remainingLength=["+remainingLength+"] line.length()=["+line.length()+"] pre.length()=["+pre.length()+"] keepLine=["+keepLine+"] ", debugRewrite);
						if (line.length() - pre.length() >= remainingLength) {
							if (debugging) dbgOutput(thisProc()+"ends on this line(A), remainingLength=["+remainingLength+"]", debugRewrite);
							// it's all on this line
							String origStr = line.substring(startCol, startCol+remainingLength);
							origStrFull += origStr;
							remainingLength -= origStr.length();
							if (debugging) dbgOutput(thisProc()+"origStr A=["+origStr+"] remainingLength=["+remainingLength+"]", debugRewrite);
							if (debugging) dbgOutput(thisProc()+"origStrFull=["+origStrFull+"] ", debugRewrite);
							assert (remainingLength == 0) : thisProc()+"error, remainingLength("+remainingLength+") should be 0: line=["+line+"] pre=["+pre+"] ";
							String post = line.substring(pre.length() + origStr.length());	
							line = post;
							lineCut += origStr.length();
							keepLine = true;				
							
							List<String> newStr = applyRewrite(rewriteType, report, rewriteText, origStrFull); 													
							addOffsets(iteration+1, startLine, startLineOrig, startColOrig, origStrFull, newStr.get(0), newStr.get(1), report, fName);
							
							writeRewrittenFile(newStr.get(0));
							origStrFull = "";
						}
						else {
							// original text extends to next line(s)
							if (debugging) dbgOutput(thisProc()+"extends on next line(B), remainingLength=["+remainingLength+"]", debugRewrite);
							String origStr = line.substring(startCol);
							origStrFull += origStr + "\n";
							remainingLength -= origStr.length();
							remainingLength--; // for the newline char
							if (debugging) dbgOutput(thisProc()+"extends on next line(B), origStrFull=["+origStrFull+"]", debugRewrite);							
						}				
					}
					else if (remainingLength > 0) {
						if (debugging) dbgOutput (thisProc()+"extending from previous line(C), remainingLength=["+remainingLength+"]", debugRewrite);

						if (debugging) dbgOutput(thisProc()+"startCol orig=["+startColOrig+"] lineCut=["+lineCut+"] ", debugRewrite);
						if (startColOrig == -1) startColOrig = startCol;
						startCol = startColOrig - lineCut;
						if (debugging) dbgOutput(thisProc()+"startCol adjusted=["+startCol+"] ", debugRewrite);	
												
						// continue collecting the original string
						if (line.length() < remainingLength) {
							// extends to another line
							remainingLength -= line.length();
							remainingLength--; // for the newline char	
							origStrFull += line + "\n";			
							if (debugging) dbgOutput(thisProc()+"extends on next line(D)=["+line+"] remainingLength=["+remainingLength+"] origStrFull=["+origStrFull+"]=["+origStrFull.length()+"]", debugRewrite);
						}
						else {
							// ends on this line
							if (debugging) dbgOutput(thisProc()+"ends on this line(E), remainingLength=["+remainingLength+"]", debugRewrite);
							String origStr = line.substring(0,remainingLength);
							origStrFull += origStr;
							remainingLength -= origStr.length();
							assert (remainingLength == 0) : "error, remainingLength("+remainingLength+") should be 0: line=["+line+"] ";
							String post = line.substring(origStr.length());
							line = post;
							lineCut += origStr.length();
							keepLine = true;
							
							if (debugging) dbgOutput(thisProc()+"origStrFull=["+origStrFull+"]=["+origStrFull.length()+"]", debugRewrite);
							List<String> newStr = applyRewrite(rewriteType, report, rewriteText, origStrFull); 														
							addOffsets(iteration+1, startLine, startLineOrig, startColOrig, origStrFull,  newStr.get(0),  newStr.get(1), report, fName);			
											
							writeRewrittenFile(newStr.get(0));							
							origStrFull = "";
						}
					}
					else {
						String newStr = line + "\n";
						writeRewrittenFile(newStr);
						if (debugging) dbgOutput(thisProc()+"write newStr(F), =["+newStr+"] ", debugRewrite);						
						break;
					}
					
					if (remainingLength == 0) {
						// when sub is done, move to next sub
						rewriteCount++;
						if (debugging) dbgOutput(thisProc()+"next rewrite: rewriteCount=["+rewriteCount+"]", debugRewrite);
						nextRewrite = true;
						remainingLength = -1;
						if (rewriteCount >= tmpSorted.size()) {
							// no more subs to apply
							if (debugging) dbgOutput(thisProc()+"rewriteCount=["+rewriteCount+"] no more rewrites to apply", debugRewrite);
							startLine = -1;
							nextRewrite = false;
						}
					}			
					
					if (!keepLine) {
						if (debugging) dbgOutput(thisProc()+"keepLine=["+keepLine+"], breaking", debugRewrite);
						break;
					}
				}
				if (abortNow) break;
			}
			if (abortNow) break;						
			
			// add list of rewrites to bottom of rewritten file
			if (tmpToDoItems.size() == 0) {
				writeRewrittenFile("\n");
				writeRewrittenFile("/*\n");
				writeRewrittenFile(SQLcodeRewrittenText +rewritesDone.size()+"\n");		
				if (rewritesDone.size() > 0) {
					for (String s : rewritesDone.stream().sorted().collect(Collectors.toList())) {
						nrRewritesDone++;
						List<String> tmp = new ArrayList<>(Arrays.asList(s.split(captureFileSeparator)));
						Integer firstLine = Integer.parseInt(tmp.get(0));
						Integer lastLine = Integer.parseInt(tmp.get(2));
						String origMsg = tmp.get(3);
						
						firstLine = calcOffsetLine(calcOffsetIterationMax, firstLine);
						lastLine = calcOffsetLine(calcOffsetIterationMax, lastLine);						
						
						String lastLineStr = "";
						if (!firstLine.equals(lastLine)) {
							lastLineStr = "-"  + lastLine.toString();
						}
						
						String msg = "line " + firstLine.toString() + lastLineStr + ": " + origMsg;
						msg = msg.replaceAll("[\\t ]+", " ");
						msg = msg.replaceAll("\n\\s*", " \\\\n ");		
						msg = lineIndent + msg;		
						writeRewrittenFile(msg+"\n");
					}
				}
				writeRewrittenFile("*/\n");
				writeRewrittenFile("reset\n");				
			}
			closeRewrittenFile();
			
			if (!(debugging && debugRewrite)) {
				deleteFile(tmpFile);
			}			
		}
						
		if (abortNow) {
			// clean up
			closeRewrittenFile();			
			deleteFile(rewrittenFile);
			deleteFile(rewrittenHTMLFile);
		}
		else {
			// copy to HTML format
			writeRewrittenHTMLFile(reportName, appName, rewrittenFile, rewrittenHTMLFile);
			
			// rename rewritten file
			File fSrc  = new File(rewrittenFile);
			File fDest = new File(renameRewrittenFile(appName, rewrittenFile));
	    	Files.move(fSrc.toPath(), fDest.toPath(), StandardCopyOption.REPLACE_EXISTING);		
	    }					
		
	}
	
	private List<String> applyRewrite(String rewriteType, String report, String rewriteText, String origStrFull) {
		if (debugging) dbgOutput(thisProc()+"rewriteType=["+rewriteType+"] report=["+report+"] rewriteText=["+rewriteText+"] origStrFull=["+origStrFull+"]", debugRewrite);
	
		List<String> result = new ArrayList<>();		
		String newStr = "";
		String newStrNoComment = "";		
		rewrittenOppties.put(report, rewrittenOppties.getOrDefault(report, 0)+1);
		if (rewriteType.equals(rewriteTypeExpr1)) {			
			newStr = rwrTag + rewriteText.replaceAll(CompassAnalyze.rewriteTag1, origStrFull);	
			newStr = newStr.trim();
			newStrNoComment = newStr;	
			newStrNoComment = applyPatternAll(newStrNoComment, rwrTabRegex, "");
		}
		else if (rewriteType.equals(rewriteTypeExpr2)) {		
			String origStrComment = getPatternGroup(origStrFull, "^(\\w+)\\s*\\(", 1);
			origStrFull = applyPatternFirst(origStrFull, "^\\w+\\s*\\(", "");
			origStrFull = applyPatternFirst(origStrFull, "\\)$", "");
			newStr = rwrTag + " /*"+origStrComment+"()*/ "+ rewriteText.replaceAll(CompassAnalyze.rewriteTag1, origStrFull);	
			newStr = newStr.trim();
			newStrNoComment = newStr;	
			newStrNoComment = applyPatternAll(newStrNoComment, rwrTabRegex, "");
		}
		else if (rewriteType.equals(rewriteTypeReplace)) {
			newStr = rwrTag + rewriteText + " /*"+origStrFull+"*/";
			newStr = newStr.trim();
			newStrNoComment = rewriteText;
			newStrNoComment = applyPatternAll(newStrNoComment, rwrTabRegex, "");
		}
		else if (rewriteType.equals(rewriteTypeODBCfunc1)) {
			String origStrCopy = applyPatternAll(origStrFull, "\\/\\*.*?\\*\\/", " ", "multiline");  // this won't handle nested comments, but let's ignore that
			origStrCopy = applyPatternAll(origStrCopy, "\\s+", " ");  
			String funcName = getPatternGroup(origStrCopy, "\\bFN\\b\\s+\\b(\\w+)\\b", 1);		
			String commentFuncName = " /*" + funcName + "*/ ";
			if (rewriteText.contains("(")) {
				newStr = applyPatternFirst(origStrFull, "\\b"+funcName+"\\b.*?\\(", rewriteText + commentFuncName);							
			}
			else {
				newStr = applyPatternFirst(origStrFull, "\\b"+funcName+"\\b", rewriteText + commentFuncName);			
			}
			newStrNoComment = newStr;			
			newStr = applyPatternFirst(newStr, "(\\{.*?\\bFN\\b)", "/*$1*/", "multiline");
			newStr = applyPatternFirst(newStr, "(\\})$", "/*$1*/");
			newStr = rwrTag + newStr.trim();
			newStrNoComment = applyPatternAll(newStrNoComment, rwrTabRegex, "");
			newStrNoComment = applyPatternFirst(newStrNoComment, "(\\{.*\\bFN\\b)", "", "multiline");
			newStrNoComment = applyPatternFirst(newStrNoComment, "(\\})$", "");
		}
		else if (rewriteType.equals(rewriteTypeODBClit1)) {
			String origStrCopy = applyPatternAll(origStrFull, "\\/\\*.*?\\*\\/", " ",  "multiline");  // won't handle nested comments, but let's ignore that
			origStrCopy = applyPatternAll(origStrCopy, "\\s+", " "); 
			String funcName = getPatternGroup(origStrCopy, "\\{.*?\\b(D|T|TS|GUID|INTERVAL)\\b", 1);			
			if (rewriteText.contains("(")) {
				newStr = applyPatternFirst(origStrFull, "\\b"+funcName+"\\b", rewriteText);		
			}
			else {
				newStr = applyPatternFirst(origStrFull, "\\b"+funcName+"\\b", rewriteText);			
			}
			newStrNoComment = newStr;			
			newStr = applyPatternFirst(newStr, "(\\{)", "/*$1*/ ");
			newStr = applyPatternFirst(newStr, "(\\})$", ") /*$1*/");
			newStr = rwrTag + newStr.trim();
			newStrNoComment = applyPatternAll(newStrNoComment, rwrTabRegex, "");
			newStrNoComment = applyPatternFirst(newStrNoComment, "(\\{)", "");
			newStrNoComment = applyPatternFirst(newStrNoComment, "(\\})$", ")");
		}
		else if (rewriteType.equals(rewriteTypeBlockReplace) && report.equals(CompassAnalyze.AlterTableAddMultiple)) {
			// ToDo: combine with MERGE below as parts are identical
			
			// for ALTER TABLE..ADD, pick up the various parts 
			Integer rwrID = Integer.valueOf(rewriteText);
			
			assert (rewriteIDDetails.containsKey(rwrID)) : thisProc()+"rwrID not found: "+rwrID;			
			Map<String, List<Integer>> positions = new HashMap<>();
			positions = rewriteIDDetails.get(rwrID);
			
			int startCtx = positions.get("start").get(0);
			int endStmt = positions.get("start").get(1);
			String startStmt = origStrFull.substring(0, endStmt-startCtx);
						
			int indent = positions.get("indent").get(0);
			Map<String, String> tmpRwr = new HashMap<>();		
			
			int cnt = 0;
			for (String p : positions.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList())) {
				if (p.equals("start")) continue;	
				if (p.equals("indent")) continue;	
				int startPos = positions.get(p).get(0);
				int endPos = positions.get(p).get(1);
				//if (debugging) dbgOutput(thisProc()+"     p=["+p+"] startPos=["+startPos+"]  endPos=["+endPos+"] ", debugRewrite);
				startPos -= startCtx;
				endPos -= startCtx;
				endPos++;
				String s = origStrFull.substring(startPos, endPos);				
				// strip comments
				if (s.contains("/*")) {
					s = applyPatternAll(s, "\\/\\*.*?\\*\\/", " ","multiline");  // this won't handle nested comments, but let's ignore that
				}
				if (s.contains("--")) {
					s = applyPatternAll(s, "\\-\\-.*?\n", "\n","multiline");  // betting that '--' won't occur in a string
				}					
				if (debugging) dbgOutput(thisProc()+"     p=["+p+"] startPos=["+startPos+"]  endPos=["+endPos+"]  s=["+s+"] ", debugRewrite);	
				tmpRwr.put(p, s);						
				cnt++;
			}
			
			String blankLine = "\n"+rewriteBlankLine+"\n";						
			String rwrSteps = "\n"+rwrTag+"\n/* --- start rewritten ALTER TABLE..ADD statement --- */\n";
						
			boolean addBlank = false;
			for (String p : tmpRwr.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList())) {
				String s = tmpRwr.get(p);
				if (debugging) dbgOutput(thisProc()+"    p=["+p+"]  s=["+s+"] ", debugRewrite);		
				if (addBlank) rwrSteps += blankLine;
				addBlank = true;															
				rwrSteps += startStmt + "\n";																
				rwrSteps += s + "\n";						
			}

			rwrSteps += "/* --- end rewritten ALTER TABLE..ADD statement --- */\n";
			rwrSteps = rewriteStmtPatchup(rwrSteps, origStrFull, indent);	
			rewriteText = rwrSteps;
			
			// comment out original block and append new block
			// NB: first line should not get shorter so start comment on first line
			String origStrCopy = applyPatternAll(origStrFull, "\\/\\*.*?\\*\\/", " "); 
			origStrCopy = applyPatternAll(origStrCopy, "\\s+", " "); 		
			newStr = "/* original ALTER TABLE statement -- " + origStrFull + " -- end original ALTER TABLE statement */\n" + rewriteText + "\n";
			
			// let last line past end of original text continue at original offset
			String lastLineTmp = origStrFull.substring(origStrFull.lastIndexOf("\n")+1);
			String lastLine = stringRepeat(" ", lastLineTmp.length());
			newStr += lastLine;			
			
			newStrNoComment = "ALTER TABLE..ADD, "+ cnt + " times";
			
		}
		else if (rewriteType.equals(rewriteTypeBlockReplace) && report.equals(CompassAnalyze.MergeStmt)) {
			// for MERGE, pick up the various parts 
			Integer rwrID = Integer.valueOf(rewriteText);
			
			assert (rewriteIDDetails.containsKey(rwrID)) : thisProc()+"rwrID not found: "+rwrID;			
			Map<String, List<Integer>> positions = new HashMap<>();
			positions = rewriteIDDetails.get(rwrID);
			
			int startCtx = positions.get("start").get(0);
						
			Map<String, String> tmpMerge = new HashMap<>();		
			
			for (String p : positions.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList())) {
				if (p.equals("start")) continue;
				if (p.equals("when_matches")) continue;
				int startPos = positions.get(p).get(0);
				int endPos = positions.get(p).get(1);
				//if (debugging) dbgOutput(thisProc()+"     p=["+p+"] startPos=["+startPos+"]  endPos=["+endPos+"] ", debugRewrite);
				startPos -= startCtx;
				endPos -= startCtx;
				endPos++;
				String s = origStrFull.substring(startPos, endPos);
				// strip comments
				if (s.contains("/*")) {
					s = applyPatternAll(s, "\\/\\*.*?\\*\\/", " ","multiline");  // this won't handle nested comments, but let's ignore that
				}
				if (s.contains("--")) {
					s = applyPatternAll(s, "\\-\\-.*?\n", "\n","multiline");  // betting that '--' won't occur in a string
				}				
				if (debugging) dbgOutput(thisProc()+"     p=["+p+"] startPos=["+startPos+"]  endPos=["+endPos+"]  s=["+s+"] ", debugRewrite);
				tmpMerge.put(p, s);
				
				if (p.toUpperCase().startsWith("WHEN_MATCHES WHENNOTMATCHED")) {
					if (!p.toUpperCase().endsWith("INSERT")) {
						// NOT MATCHED BY SOURCE: DELETE or UPDATE				
						// condition specified for BY SOURCE?
						String cond = getPatternGroup(s, "^.*?\\bMATCHED\\s+BY\\s+SOURCE\\s+AND\\b(.*?)\\bTHEN\\s+(UPDATE|DELETE)\\b.*$", 1, "multiline");
						if (!cond.isEmpty()) {						
							tmpMerge.put("by source cond", cond);    					
						}	
					}
				}
				
				if (p.toUpperCase().startsWith("WHEN_MATCHES WHENMATCHED")) {
					// MATCHED: DELETE or UPDATE				
					// condition specified?
					String cond = getPatternGroup(s, "^.*?\\bWHEN\\s+MATCHED\\s+AND\\b(.*?)\\bTHEN\\s+(UPDATE|DELETE)\\b.*$", 1, "multiline");
					if (!cond.isEmpty()) {
						tmpMerge.put("when matched cond", cond);  
					}  						
				}										
			}
			
			// ToDo: if we want to reset this counter in every batch (which would work fine), then 
			// we need to take the batch number along when we identify a rewrite case. Since applying
			// the rewrites is done on a per-file basis, there is no batch concept at this time
			nrMergeRewrites++;

			String savePt   = "savept_merge_rewritten_"+nrMergeRewrites;			
			String errVar   = "@MERGE_REWRITTEN_ERROR_"+nrMergeRewrites;			
			String rcTmpVar = "@MERGE_REWRITTEN_RCTMP_"+nrMergeRewrites;	
			String rcVar    = "@MERGE_REWRITTEN_ROWCOUNT_"+nrMergeRewrites;	
			String rollbkLbl= "lbl_rollback_merge_rewritten_"+nrMergeRewrites;	
			String commitLbl= "lbl_commit_merge_rewritten_"+nrMergeRewrites;	
			String stmtEnd  = "SELECT "+errVar+ "=@@ERROR, "+rcTmpVar+ "=@@ROWCOUNT\nIF "+errVar+ " <> 0 GOTO "+rollbkLbl+"\nSET "+rcVar+" += "+rcTmpVar+ "\n";		
			
			String mergeSteps = "\n"+rwrTag+"\n/* --- start rewritten MERGE statement #"+nrMergeRewrites+" --- */\n";
			mergeSteps += "/* Note: please review/modify the rewritten SQL code below, especially for handling of ROLLBACK */\n";
			mergeSteps += "BEGIN TRANSACTION\n";
			mergeSteps += "SAVE TRANSACTION "+savePt+"\n";
			mergeSteps += "DECLARE "+rcVar+" INT = 0 /* use instead of original @@ROWCOUNT */\n";
			mergeSteps += "DECLARE "+errVar+" INT /* temporary variable */\n";
			mergeSteps += "DECLARE "+rcTmpVar+" INT /* temporary variable */\n";
			String blankLine = "\n"+rewriteBlankLine+"\n";
			for (String p : tmpMerge.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList())) {
				String s = tmpMerge.get(p);
				if (debugging) dbgOutput(thisProc()+"    p=["+p+"]  s=["+s+"] ", debugRewrite);				
				
				String action = "INSERT";				
				if (p.toUpperCase().endsWith("UPDATE")) action = "UPDATE";
				else if (p.toUpperCase().endsWith("DELETE")) action = "DELETE";
				
				String bytgt = "";
				if (action.equals("INSERT") && !getPatternGroup(s, "(\\bBY\\s+TARGET\\b)", 1, "multiline").isEmpty()) bytgt = "BY TARGET ";
								
				// condition?
				String cond = "";
				String condDisplay = "";
				if (!getPatternGroup(s, "^.*?\\bMATCHED\\s+(BY\\s+\\w+\\s+)?\\bAND\\b(.*?)\\bTHEN\\s+(UPDATE|DELETE|INSERT)\\b.*$", 2, "multiline").isEmpty()) {
					cond = applyPatternFirst(s, "^.*?\\bMATCHED\\s+(BY\\s+\\w+\\s+)?\\bAND\\b(.*?)\\bTHEN\\s+(UPDATE|DELETE|INSERT)\\b.*$", "$2", "multiline");
					condDisplay = "AND (condition) ";
				}		

				if (p.toUpperCase().startsWith("WHEN_MATCHES WHENNOTMATCHED")) {
					if (action.equals("INSERT")) {
						String insCollist = applyPatternFirst(s, "^.*?\\bINSERT\\b", "", "multiline");
						if (tmpMerge.containsKey("output_clause")) {
							String outputClause = rewriteMergeStmtOutput(tmpMerge.get("output_clause"), action) + "\n";
							insCollist = applyPatternFirst(insCollist, "\\bVALUES\\b", "\n"+escapeRegexChars(outputClause+"\nVALUES"));
						}								
						// determine in VALUES clause contains anythinig other than constants/variables/function calls
						String valuesTest = applyPatternFirst(insCollist, "^.*?\\bVALUES\\b.*?\\(", " ", "multiline");
						valuesTest = applyPatternAll(valuesTest, "'.*?'", " ");
						valuesTest = applyPatternAll(valuesTest, "\\b0x[0-9A-F]+\\b", " ");
						valuesTest = applyPatternAll(valuesTest, "(\\W)@(@)?\\w+\\b", "$1 ");
						valuesTest = applyPatternAll(valuesTest, "([\\+\\-])?\\d*\\.\\d*(e([\\+\\-])?\\d+)?", " ");
						valuesTest = applyPatternAll(valuesTest, "\\b[\\w\\.]+\\s*\\(", " (");
						valuesTest = applyPatternAll(valuesTest, "\\b(CURRENT_TIMESTAMP|CURRENT_USER|SESSION_USER|SYSTEM_USER|USER)\\b", " ");
						valuesTest = applyPatternAll(valuesTest, "\\bAT\\s+TIME\\s+ZONE\\b", " ");
						boolean varsConstantsOnly = false;
						if (getPatternGroup(valuesTest, "(\\w)", 1).isEmpty()) varsConstantsOnly = true;
						
						if (!varsConstantsOnly) {
							if (!getPatternGroup(insCollist, "(\\bVALUES\\b.*?\\()", 1, "multiline").isEmpty()) {
								insCollist = applyPatternFirst(insCollist, "\\bVALUES\\b.*?\\(", "SELECT ", "multiline");
								insCollist = applyPatternFirst(insCollist, "\\)$", " ");
							}
						}
						
						String insStmt = blankLine;
						insStmt += "/* WHEN NOT MATCHED "+bytgt+condDisplay+"THEN "+action+" */\n";
						if (tmpMerge.containsKey("with_expression")) insStmt += ";" +tmpMerge.get("with_expression") + "\n";						
						insStmt += "INSERT INTO " + tmpMerge.get("ddl_object") + "\n";					
						insStmt += insCollist;
						
						if (!varsConstantsOnly) {
							insStmt += "\nFROM " + tmpMerge.get("table_sources");
							insStmt += "\nWHERE NOT EXISTS (";
							insStmt += "\nSELECT * FROM " + tmpMerge.get("ddl_object") + " " + tmpMerge.getOrDefault("table_alias", "");
							insStmt += "\nWHERE " + tmpMerge.get("search_condition") ;
							insStmt += "\n)\n";			
						}
						else {
							insStmt += "\n";			
						}
						insStmt += stmtEnd;		
						if (debugging) dbgOutput(thisProc()+"insStmt=["+insStmt+"] ", debugRewrite);
						mergeSteps += insStmt;
					}	
					else {
						// NOT MATCHED BY SOURCE: DELETE or UPDATE
						String stmt = blankLine;
						stmt += "/* WHEN NOT MATCHED BY SOURCE "+condDisplay+"THEN "+action+" */\n";
						if (tmpMerge.containsKey("with_expression")) stmt += ";" +tmpMerge.get("with_expression") + "\n";
						String tgtName = tmpMerge.get("ddl_object");
						String updSet = "";
						if (tmpMerge.containsKey("table_alias")) {
							tgtName = tmpMerge.get("table_alias");
						}
						if (action.equals("DELETE")) {
							stmt += "DELETE " + tgtName + "\n";
						}
						else {
							updSet = applyPatternFirst(s, "^.*?\\bUPDATE\\b\\s*", "", "multiline");
							stmt += "UPDATE " + tgtName + "\n";
							stmt += updSet + "\n";
						}
						if (tmpMerge.containsKey("output_clause")) {
							stmt += rewriteMergeStmtOutput(tmpMerge.get("output_clause"), action) + "\n";
						}						
						if (tmpMerge.containsKey("table_alias")) {
							stmt += "FROM " + tmpMerge.get("ddl_object") + " " + tmpMerge.getOrDefault("table_alias", "") + "\n";
						}
						stmt += "WHERE NOT EXISTS (\n";
						stmt += "SELECT * FROM " + tmpMerge.get("table_sources") + "\n";
						stmt += "WHERE " + tmpMerge.get("search_condition")+"\n";
						if (!cond.isEmpty()) {
							stmt += "AND (" + cond+")\n";						
						}			
						else if (tmpMerge.containsKey("by source cond")) {
							stmt += "AND NOT (" + tmpMerge.get("by source cond")+")\n"; //"
						}			
						stmt += ")\n";
						stmt += stmtEnd;
						if (debugging) dbgOutput(thisProc()+"stmt=["+stmt+"] ", debugRewrite);
						mergeSteps += stmt;
					}			
				}
				else if (p.toUpperCase().startsWith("WHEN_MATCHES WHENMATCHED")) {
					// MATCHED: DELETE or UPDATE
					String stmt = blankLine;
					stmt += "/* WHEN MATCHED "+condDisplay+"THEN "+action+" */\n";					
					if (tmpMerge.containsKey("with_expression")) stmt += ";"+tmpMerge.get("with_expression") + "\n";
					String tgtName = tmpMerge.get("ddl_object");
					String updSet = "";
					if (tmpMerge.containsKey("table_alias")) {
						tgtName = tmpMerge.get("table_alias");
					}
					if (action.equals("DELETE")) {
						stmt += "DELETE " + tgtName + "\n";
					}
					else {
						updSet = applyPatternFirst(s, "^.*?\\bUPDATE\\b\\s*", "", "multiline");
						stmt += "UPDATE " + tgtName + "\n";
						stmt += updSet + "\n";
					}
					if (debugging) dbgOutput(thisProc()+"stmt=["+stmt+"] ", debugRewrite);					
					if (tmpMerge.containsKey("output_clause")) {
						stmt += rewriteMergeStmtOutput(tmpMerge.get("output_clause"), action) + "\n";
					}						
					if (tmpMerge.containsKey("table_alias") || tmpMerge.containsKey("table_sources") ) {
						stmt += "FROM \n";
					}
					if (tmpMerge.containsKey("table_alias")) {
						stmt += tmpMerge.get("ddl_object") + " " + tmpMerge.get("table_alias");
						if (tmpMerge.containsKey("table_sources")) {
							stmt += ", ";
						}
						stmt += "\n";
					}
					if (tmpMerge.containsKey("table_sources")) {
						stmt += tmpMerge.get("table_sources") + "\n";
					}
					stmt += "WHERE " + tmpMerge.get("search_condition")+"\n";
					if (!cond.isEmpty()) {
						stmt += "AND (" + cond+")\n";						
					}			
					else if (tmpMerge.containsKey("when matched cond")) {
						stmt += "AND NOT (" + tmpMerge.get("when matched cond")+")\n"; //"
					}	
					stmt += stmtEnd;
					if (debugging) dbgOutput(thisProc()+"stmt=["+stmt+"] ", debugRewrite);
					mergeSteps += stmt;
				}			
			}
			mergeSteps += blankLine;
			mergeSteps += "\n"+"GOTO "+commitLbl+"\n";					
			mergeSteps += "/* in case of an error, roll back to savepoint at the start but do no abort the transaction: there may be an outermost transaction active*/\n";
			mergeSteps += "\n"+rollbkLbl+": ROLLBACK TRANSACTION "+savePt+"\n";	
			mergeSteps += "\n"+commitLbl+":   COMMIT\n";			
			mergeSteps += ";/* --- end rewritten MERGE statement #"+nrMergeRewrites+" --- */\n";
			mergeSteps = rewriteStmtPatchup(mergeSteps, origStrFull, "USING");	
			rewriteText = mergeSteps;
			
			// comment out original block and append new block
			// NB: first line should not get shorter so start comment on first line
			String origStrCopy = applyPatternAll(origStrFull, "\\/\\*.*?\\*\\/", " "); 
			origStrCopy = applyPatternAll(origStrCopy, "\\s+", " "); 		
			newStr = "/* original MERGE statement -- " + origStrFull + " -- end original MERGE statement */\n" + rewriteText + "\n";
			
			// let last line past end of original text continue at original offset
			String lastLineTmp = origStrFull.substring(origStrFull.lastIndexOf("\n")+1);
			String lastLine = stringRepeat(" ", lastLineTmp.length());
			newStr += lastLine;
		}		
		else {
			assert false : thisProc()+"invalid rewriteType=["+rewriteType+"] ";
		}
		
		result.add(newStr);
		if (newStrNoComment.isEmpty()) newStrNoComment = newStr;
		result.add(newStrNoComment.trim());
		return result;				
	}
	
	public String rewriteMergeStmtOutput(String s, String action) {
		s = applyPatternAll(s, "\\$ACTION\\b", "'"+action+"'");
		if (action.equals("DELETE")) {
			s = applyPatternAll(s, "\\bINSERTED\\.\\w+\\b", "NULL");
			s = applyPatternAll(s, "\\bINSERTED\\.[\\[].*?[\\]]", "NULL");
		}
		if (action.equals("INSERT")) {
			s = applyPatternAll(s, "\\bDELETED\\.\\w+\\b", "NULL");
			s = applyPatternAll(s, "\\bDELETED\\.[\\[].*?[\\]]", "NULL");
			
			// for an INSERT..OUTPUT, we don;t seem to be able to reference anything other than INSERTED
			String tmp = applyPatternAll(s, "\\b(OUTPUT)\\b", "");
			tmp = applyPatternAll(tmp, "[\\(\\)]", ",");
			tmp = "," + tmp + ",";
			List<String> tmpCols = new ArrayList<>(Arrays.asList(tmp.split(",")));
			for (String col : tmpCols) {
				String p = getPatternGroup(col.trim(), "^(\\w+)\\.", 1);
				if (p.isEmpty()) continue;
				if (!p.equalsIgnoreCase("INSERTED")) {
					s = applyPatternAll(s, "([^\\.])"+p+"\\.", "$1 INSERTED.");
				}
			}

		}
		return s;
	}	
	
	public String rewriteStmtPatchup(String s, String origStmt, String kwd) {
		return rewriteStmtPatchup(s, origStmt, kwd, 0);
	}
	public String rewriteStmtPatchup(String s, String origStmt, int indent) {
		return rewriteStmtPatchup(s, origStmt, "", indent);
	}
	public String rewriteStmtPatchup(String s, String origStmt, String kwd, int indent) {
		String leading = "";
		if (indent == 0) {
			leading = getPatternGroup("\n"+origStmt, "\n([^\n]*?)"+kwd+"\\b", 1, "multiline");
			leading = applyPatternAll(leading, "\\S", " ");
		}
		else {
			leading = stringRepeat(" ", indent);
		}
		s = applyPatternAll(s, "\\n[ ]*\\n", "\n");
		s = applyPatternAll(s, "\\n[\\t ]*\\n", "\n");
		s = applyPatternAll(s, "\\n[\\t ]+", "\n");
		s = applyPatternAll(s, "\\n", "\n"+leading);
		s = applyPatternAll(s, rewriteBlankLine, "");
		return s;
	}	
	
	public void createAnonymizedReport() throws IOException {			
		// get capture files
		List<Path> captureFiles = getCaptureFiles(reportName);
		if (debugging) dbgOutput(thisProc() + "captureFiles(" + captureFiles.size() + ")=[" + captureFiles + "] ", debugReport);
		if (captureFiles.size() == 0) {
			appOutput("No analysis files found. Use -analyze to perform analysis and generate a report.");
			errorExit();
		}
		String cfv = captureFilesValid("import", captureFiles, true);
		if (!cfv.isEmpty()) {
			// print error message and exit
			appOutput(cfv);
			errorExit();
		}
		String cfVersion = captureFilesValid("tgtversion", captureFiles, true);		

		Date now = new Date();
		String nowFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);

		BufferedWriter anonItemsFileWriter = null;
    	String anonItemPathName = getAnonymizedItemsPathname(reportName);
    	checkDir(getReportDirPathname(reportName, capDirName), true);
		anonItemsFileWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(anonItemPathName), StandardCharsets.UTF_8)));

		int itemCount = 0;
		Map<String,String> appNames = new HashMap<>();
		Map<String,String> objNames = new HashMap<>();
		Map<String,String> dbNames = new HashMap<>();
		for (Path cf : captureFiles) {
			String cfLine = captureFileFirstLine(cf.toString());   // read only first line
			String cfReportName = captureFileAttribute(cfLine, 1);
			if (cfReportName.isEmpty()) {
				appOutput("Invalid format in "+cfReportName+"; run with -analyze to fix.");
				errorExit();
			}
			if (!reportName.equalsIgnoreCase(cfReportName)) {
				String rDir = getFilePathname(getDocDirPathname(), capDirName);
				appOutput("Found analysis file for report '" + cfReportName + "' in " + rDir + ": adding to import");
			}

			FileInputStream cfis = new FileInputStream(new File(cf.toString()));
			InputStreamReader cfisr = new InputStreamReader(cfis, StandardCharsets.UTF_8);
			BufferedReader capFile = new BufferedReader(cfisr);

			String capLine = "";

			while (true) {
				capLine = capFile.readLine();
				if (capLine == null) {
					//EOF
					break;
				}
				capLine = capLine.trim();
				if (capLine.isEmpty()) continue;
				if ((capLine.charAt(0) == '#') || (capLine.charAt(0) == '*')) {
					continue;
				}
				if (capLine.contains(captureFileSeparator+ObjCountOnly+captureFileSeparator)) continue;

				itemCount++;

				// strip off the last two semicolons
				capLine = capLine.substring(0,capLine.lastIndexOf(captureFileSeparator));
				capLine = capLine.substring(0,capLine.lastIndexOf(captureFileSeparator));							
				capLine = unEscapeHTMLChars(capLine);
					
				// remove customer-specific items
				List<String> tmp = new ArrayList<>(Arrays.asList(capLine.split(captureFileSeparator)));
				
				if (!(tmp.get(9).equals(BatchContext))) {
					// wipe out identifiers in context
					String objType = getPatternGroup(tmp.get(9),"^(\\w+)\\s+(.*)$",1);	
					String objName = getPatternGroup(tmp.get(9),"^(\\w+)\\s+(.*)$",2).toLowerCase();
					if (!objNames.containsKey(objName)) objNames.put(objName, objType.toLowerCase()+(objNames.size()+1));
					tmp.set(9, objType+" "+objNames.get(objName));
				}
				if (tmp.size() >= 11) {
					if (!tmp.get(10).isEmpty()) {
						// wipe out identifiers in context
						String objType = getPatternGroup(tmp.get(10),"^(\\w+)\\s+(.*)$",1);							
						String objName = getPatternGroup(tmp.get(10),"^\\w+\\s+(.*)$",1).toLowerCase();
						if (!objNames.containsKey(objName)) objNames.put(objName, "table"+(objNames.size()+1));
						tmp.set(10, objType+" "+objNames.get(objName));
					}
				}
				
				// remove user-specific fields
				if (tmp.size() >= 12) {
					tmp.remove(11);	
				}
				tmp.remove(8);	
				tmp.remove(7);	
				tmp.remove(6);	

				// wipe out appname
				if (!appNames.containsKey(tmp.get(5).toLowerCase())) appNames.put(tmp.get(5).toLowerCase(), "app"+(appNames.size()+1));
				tmp.set(5,appNames.get(tmp.get(5).toLowerCase()));
				
				tmp.remove(4);	
				tmp.remove(3);	
				tmp.remove(1);	
				
				// wipe out UDD name				
				if (tmp.get(0).indexOf(" (UDD ") > -1) {
					tmp.set(0, tmp.get(0).substring(0, tmp.get(0).indexOf(" (UDD "))+" (UDD)" + tmp.get(0).substring(tmp.get(0).lastIndexOf(")")+1));			
				}	
				
				// wipe out DB name
				if (tmp.get(0).startsWith("USE ")) {
					String dbName = tmp.get(0).substring(4).toLowerCase();
					if (!dbNames.containsKey(dbName)) dbNames.put(dbName, "db"+(dbNames.size()+1));
					tmp.set(0, "USE " +dbNames.get(dbName));			
				}	
				if (tmp.get(0).contains(" DATABASE ")) {
					String dbName = tmp.get(0).substring(tmp.get(0).indexOf(" DATABASE ")+" DATABASE ".length()).toLowerCase();
					if (!dbNames.containsKey(dbName)) dbNames.put(dbName, "db"+(dbNames.size()+1));
					tmp.set(0, tmp.get(0).substring(0, tmp.get(0).indexOf(" DATABASE ")+" DATABASE ".length())+dbNames.get(dbName));			
				}	
				
				capLine = String.join(captureFileSeparator, tmp);

				// add babelfish version & compass version
				capLine = cfVersion + captureFileSeparator + thisProgVersion + captureFileSeparator + capLine;
				anonItemsFileWriter.write(capLine+"\n");
			}
			capFile.close();
		}
		anonItemsFileWriter.close();
		appOutput("Items for anonymized reporting: "+itemCount);
		appOutput("All identifiers and customer-specific details have been removed from the captured items\nin "+anonItemPathName);
	}	
	
	public String stringAsHex (String s) {		
		StringBuilder sb =  new StringBuilder();
		for (char c : s.toCharArray()) {
			sb.append("0x");
			String hexString = Integer.toHexString(c);
			int leadingZerosNr = 4 - hexString.length();
			for (int i = 0; i < leadingZerosNr; i++) {
				sb.append("0");
			}
			sb.append(hexString);
		}
		return sb.toString();	
	}	
											
 	// ---- error handling in Lexer ----------------------------------------
	private String errorMsg;

	public String limitTextSize(String text) {
		int TEXT_LIMIT = 100;
		char WHITESPACE = ' ', BACKSLASH = '\\', startQuote = 0;
		String DELIMS = "\t\r\n\f", QUOTES = "'\"";
		boolean inString = false;
		StringBuilder spacedText = new StringBuilder();

		if (text.length() > TEXT_LIMIT) {
			text = text.substring(0, TEXT_LIMIT) + " [...]'";
		}
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (QUOTES.indexOf(c) != -1) {
				if (!inString) {
					startQuote = c;
					inString = true;
				}
				else if (c == startQuote) {
					inString = false;
				}
				spacedText.append(c);
			}
			else if (DELIMS.indexOf(c) != -1) {
				spacedText.append(WHITESPACE);
			}
			else if (!inString && c == BACKSLASH) {
				i++;
				spacedText.append(WHITESPACE);
			}
			else {
				spacedText.append(c);
			}
		}

		return spacedText.toString();
	}

	public String getAndSetNullErrorMsg() {
		String msg = errorMsg;
		errorMsg = null;
		return msg;
	}

	public void addLexicalErrorHex(StringBuilder sb, String s) {
		s = limitTextSize(s);
		sb.append("lexical error: ").append(s).append(" with hex=");
		sb.append(stringAsHex(s));
	}
	
	public void setErrorMsg(int line, int col, String s) {
		StringBuilder sb = new StringBuilder();
		sb.append("Line ").append(line).append(":").append(col + 1).append(", ");
		addLexicalErrorHex(sb, s);
		errorMsg = sb.toString();
	}
}
