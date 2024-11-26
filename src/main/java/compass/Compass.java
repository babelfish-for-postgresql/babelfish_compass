/*
Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/

/*
 * Originally adapted from ANTLR4 org/antlr/v4/gui/TestRig.java
 *
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the THIRD-PARTY-LICENSES.txt file in the project root.
 */
 
package compass;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import parser.*;

/*
 * Invoke as:
 *  # java compass.Compass -help
 */
 
/*
* In a nutshell:
 * To perform an analysis, a two-pass approach is used:
 * - in pass 1, an input file is read/parsed and a copy stored in the imported directory in UTF8 format; a (rudimentary) symbopl table is built
 * - in pass 2, the copy is read again and analysis is performed using the symbpl table; captured items are stored in the 'captured' directory
 * Based on the items captured during analysis, a report is genereated
 *
 * When reading an input file, this is done on a batch-by-batch bases, similar to 'sqlcmd'.
 */
public class Compass {

	static Integer nrFileNotFound = 0;
	static Integer totalBatches = 0;
	static Integer totalParseErrors = 0;
	static Map<Integer,Integer> passCount = new HashMap<>();
	static Integer nrLinesTotalP1 = 0;
	static Integer nrLinesTotalP2 = 0;	
	static Integer retrySLL = 0;
	static Integer retrySLLFile = 0;
	static boolean hasParseError = false;
	static StringBuilder parseErrorMsg = new StringBuilder();

	static long startRun;
	static Date startRunDate;
	static String startRunFmt;
	static long endRun;
	static String endRunFmt;
	
	static long startTime = 0;
	static long endTime = 0;
	static long duration = 0;	

	static Map<String, Long>   timeCount = new HashMap<>();
	static Map<String, String> timeCountStr = new HashMap<>();

	protected static boolean quitNow = false;
	
	protected static boolean showVersion = false;
	protected static boolean readStdin = false;
	protected static boolean parseOnly = false;
	protected static boolean importOnly = false;
	protected static boolean dumpParseTree = false;
	protected static boolean dumpBatchFile = false;
	protected static boolean forceAppName = false; 
	protected static boolean forceReportName = false; 
	protected static int reportNameMaxLength = 70; 
	protected static String  reportFileName = ""; 
	protected static String  quotedIdentifier = "ON"; 
	protected static boolean addReport = false;
	protected static boolean replaceFiles = false;
	protected static boolean recursiveInputFiles = false;
	protected static String includePattern = null;
	protected static String excludePattern = null;
	protected static Set<String> defaultExcludes = new LinkedHashSet<>(Arrays.asList(
			".ppt",".pptx", ".xls",".xlsx", ".doc", ".docx", ".pdf", ".rtf", ".htm", ".html", ".css", ".zip", 
			".gzip", ".gz", ".rar", ".7z", ".tar", ".tgz", ".sh", ".bash", ".csh", ".tcsh", ".bat", ".csv", ".md", 
			".mp4", ".mov", ".jpg", ".gif", ".png",	".tmp", ".pl", ".py", ".cs", ".cpp", ".vb", ".vbproj", ".c", ".php", 
			".java", ".classpath", ".project", ".rb", ".js", ".exe", ".dll", ".sln", ".scc", ".gitignore", ".json", 
			".yml", ".yaml", ".xml", ".xel", ".xsl", ".xsd", ".xslt", ".rdl", ".properties", ".config", ".cfg", ".sdcs",
			".rpt", ".rptproj", ".rss", ".res", ".resx", ".cache", ".settings")
	);	
	protected static boolean generateReport = true;
	protected static boolean reAnalyze = false;
	protected static boolean reportOnly = false;
	protected static boolean reportOption = false;
	protected static boolean deleteReport = false;
	protected static boolean userSpecifiedBabelfishVersion = false;
	protected static boolean listContents = false;
	protected static boolean pgImport = false;
	protected static boolean pgImportAppend = false;
	protected static boolean pgImportTable = false;
	protected static boolean importFormatArg = false;	
	protected static String mergeReport = "";
	protected static String userCfgFile = "";		
	protected static boolean optimisticFlag = false;		
	protected static boolean popupwindow = true;		

	protected static boolean antlrSLL = true;
	protected static boolean antlrShowTokens = false;
	protected static boolean antlrTrace = false;
	protected static boolean antlrDiagnostics = false;
	protected static Charset charset;
	protected static String userEncoding = null;
	public static boolean analyzingDynamicSQL = false;
	public static int dynamicSQLLineNr = 0;
	public static int dynamicSQLBatchNr = 0;
	public static int dynamicSQLBatchLineNr = 0;	
	public static String dynamicSQLContext = "";	
	public static String dynamicSQLSubContext = "";	
	
	public static String reportName = null; // must be null when not initialized
	public static String applicationName;
	public static String sessionLog;
	
	public static List<String> inputFiles = new ArrayList<>();
	public static Map<String, Integer> inputFilesLastLine = new HashMap<>();
	public static List<String> inputFilesOrig = new ArrayList<>();
	public static Map<String,String> inputFilesMapped = new HashMap<>();
	public static List<String> cmdFlags = new ArrayList<>();
	public static List<String> pgImportFlags = new ArrayList<>();
	
	// auto-generating DDL script
	public static boolean autoDDL = false;
	public static String  sqlEndpoint = CompassUtilities.uninitialized;
	public static String  sqlLogin  = CompassUtilities.uninitialized;
	public static String  sqlPasswd = CompassUtilities.uninitialized;
	public static String  sqlDBList = CompassUtilities.uninitialized;
	public static String  SMOOutputFolder = "";
	public static String  SMODDLTag = "";

	protected static TSQLParser.Tsql_fileContext exportedParseTree;

	public static CompassUtilities u = CompassUtilities.getInstance();
	public static CompassConfig cfg = CompassConfig.getInstance();
	public static CompassAnalyze a = CompassAnalyze.getInstance();

	public Compass(String[] args) {		
		u.setPlatformAndOptions(System.getProperty("os.name"));		
			
		if (args.length < 1) {
			System.out.println("Must specify arguments. Try -help");
			return;
		}

		u.targetBabelfishVersion = CompassUtilities.baseBabelfishVersion;  // init at 1.00

		for (int i = 0; i < args.length; i++) {
			// don't log these flags anywhere
			if (args[i].equals("-sqlpasswd") || args[i].equals("-sqlpassword")) {
				cmdFlags.add(args[i]);
				cmdFlags.add("********");
				i++;
				continue;
			}
			cmdFlags.add(args[i]);
		}					
					
		// Need to queue up the input files so we can process them after knowing whether any of
		// -recursive, -include [pattern] or -exclude [pattern] have been set.
		List<String> tmpInputFiles = new ArrayList<>();

		for (int i = 0; i < args.length; ) {
			String arg = args[i];
			//u.appOutput(u.thisProc()+"arg=["+arg+"] ");
			i++;
			if (arg.equals("-version")) {
				// info already printed by main(String[] args)
				showVersion = true;
				return;
			}
			
			// ToDo: ideally, all command flags are put in a list rather than using strings everywhere
			if (arg.equals("-help")) {
				String helpOption  = "";
				if (args.length > i) {
					if (args[i].equals("-reportoption")  || args[i].equals("reportoption")) helpOption = "reportoption";
					if (args[i].equals("-reportoptions") || args[i].equals("reportoptions")) helpOption = "reportoption";
					if (args[i].equals("-exclude") || args[i].equals("exclude")) helpOption = "exclude";
					if (args[i].equals("-encoding") || args[i].equals("encoding")) helpOption = "encoding";
					if (args[i].equals("-importfmt") || args[i].equals("importfmt") || args[i].equals("-importformat") || args[i].equals("importformat")) helpOption = "importfmt";
					if (args[i].equals("-csvfmt") || args[i].equals("csvfmt") || args[i].equals("-csvformat") || args[i].equals("csvformat")) helpOption = "csvfmt";
				}
				if (helpOption.equals("reportoption")) {
					u.appOutput("-reportoption  [options] : additional reporting detail. ");
					u.appOutput(" [options] are comma-separated as follows:");
					u.appOutput(" One of the following:");				
					u.appOutput("    xref=feature     : generate X-ref by feature");				
					u.appOutput("    xref=object      : generate X-ref by object");				
					u.appOutput("    xref=all         : generate both X-refs");				
					u.appOutput("    xref             : same as xref=all");				
					u.appOutput(" One or more of the following:");				
					u.appOutput("    detail           : generate additional X-ref detail (e.g. object names)");				
					u.appOutput("    apps             : with >1 app, shows app count in summary section");				
					u.appOutput("    status=<status>  : generate X-ref for items with the specified status");				
					u.appOutput("        Without status=, no X-refs are generated for 'Supported' and 'Ignored' features");				
					u.appOutput("    filter=<pattern> : only report X-ref items matching the pattern (case-insensitive)");				
					u.appOutput("    linenrs=<number> : max.nr of line numbers shown in list (default="+CompassUtilities.maxLineNrsInListDefault+")");				
					u.appOutput("    notabs           : do not open a Xref link in a new tab(default=open in new tab)");				
					u.appOutput("    batchnr          : in xref, show batch number + line nr in batch");				
					u.appOutput("    hints            : list all popup hints from the SQL Summary section (included with xref)");				
					u.appOutput("Without -reportoption, only the assessment summary is generated (no X-refs)");				
					u.appOutput("NB: generating X-refs may produce a very large report.");							
					u.appOutput("NB: do not put spaces anywhere in the options");							
					u.appOutput("");				
					u.appOutput("Examples:");				
					u.appOutput("  -reportoption xref,status=supported,detail ");				
					u.appOutput("  -reportoption xref -reportoption status=supported -reportoption detail ");				
					u.appOutput("  -reportoption xref=object,status=reviewsemantics,status=reviewperformance,detail");				
					quitNow = true;
					return;					
				}
				if (helpOption.equals("exclude")) {
					u.appOutput("The following file type suffixes are excluded by default:\n"+defaultExcludes);							
					quitNow = true;
					return;					
				}
				if (helpOption.equals("encoding")) {
					encodingHelp();							
					quitNow = true;
					return;					
				}
				if (helpOption.equals("importfmt")) {
					u.appOutput("Valid values for -importfmt: "+u.importFormatSupportedDisplay);
					quitNow = true;
					return;					
				}							
				if (helpOption.equals("csvfmt")) {
					u.appOutput("Valid values for -csvformat: "+u.CSVFormats);
					quitNow = true;
					return;					
				}				
				
				u.appOutput("Usage: " + CompassUtilities.thisProgExec + "  <reportName>  [options] ");
				u.appOutput("[options] can be:");
				u.appOutput("   inputfile [inputfile ...]    : one or more input files to import into the report");
				u.appOutput("   -delete                      : first deletes report directory, incl. all report files");
				u.appOutput("   -appname <appname>           : use application name <appname> for all inputfiles");
				u.appOutput("   -add                         : import additional inputfile(s) into existing report");  
				u.appOutput("   -replace                     : replace already-imported input file(s)"); 
				u.appOutput("   -noreport                    : analyze only, do not generate a report");
				u.appOutput("   -importonly                  : import input file(s), no analysis or report");				
				u.appOutput("   -reportonly                  : (re)generate report based on earlier analysis");				
				u.appOutput("   -reportoption <options>      : additional reporting detail (try -help -reportoption)");				
				u.appOutput("   -reportfile <name>           : specifies file name for report file (without .html)");				
				u.appOutput("   -list                        : display imported files/applications for a report");				
				u.appOutput("   -analyze                     : (re-)run analysis on imported files, and generate report");					
				u.appOutput("   -userconfigfile <filename>   : specifies user-defined .cfg file (default= " + CompassUtilities.defaultUserCfgFileName+")");	
				u.appOutput("   -optimistic                  : use predefined " + CompassUtilities.optimisticUserCfgFileName+")");	
				u.appOutput("   -nooverride                  : do not use overrides from user-defined .cfg file");												
				u.appOutput("   -babelfish-version <version> : specify target Babelfish version (default=latest)");
				u.appOutput("   -encoding <encoding>         : input file encoding, e.g. '-encoding UTF16'. Default="+Charset.defaultCharset());
				u.appOutput("                                  use '-encoding help' to list available encodings");
				u.appOutput("   -quotedid {on|off}           : set QUOTED_IDENTIFIER at start of script (default=ON)");
				u.appOutput("   -pgimport \"<comma-list>\"     : imports captured items into a PostgreSQL table for SQL querying");
				u.appOutput("                                  <comma-list> is: host,port,username,password,dbname");
				u.appOutput("                                  (requires psql to be installed)");
				u.appOutput("   -pgimportappend              : with -pgimport, appends to existing table (instead of drop/recreate)");
				u.appOutput("   -pgimporttable <table-name>  : table name for -pgimport; default="+u.psqlImportTableNameDefault);
				u.appOutput("   -recursive                   : recursively add files if inputfile is a directory");
				u.appOutput("   -include <list>              : pattern of input file types to include (e.g.: .txt,.ddl)");
				u.appOutput("   -exclude <list>              : pattern of input file types to exclude (e.g.: .pptx)");
  				u.appOutput("   -anon                        : remove all customer-specific identifiers");
  				u.appOutput("   -rewrite                     : rewrites selected unsupported SQL features");
  				u.appOutput("   -noupdatechk                 : do not check for " + CompassUtilities.thisProgName + " updates");
				u.appOutput("   -nopopupwindow               : do not automatically open report in browser");  				
				u.appOutput("   -importformat <fmt>          : process special-format captured query files");
				u.appOutput("   -nodedup                     : with -importfmt, do not de-duplicate captured queries");
				u.appOutput("   -noreportcomplexity          : do not include complexity scores in report");
				u.appOutput("   -csvformat <fmt>             : format for generated .csv file");				
				u.appOutput("   -csvitemidfile <filename>    : filename for item IDs when .csv format=flat (default=BabelfishCompassItemID.csv)");				
			  	// always set to true now:
			  	//u.appOutput("   -syntax_issues               : also report selected Babelfish syntax errors (experimental)");				
				u.appOutput("   -sqlendpoint <host-or-IP>[,port] : SQL Server host");				
				u.appOutput("   -sqllogin <login-name>       : SQL Server login");				
				u.appOutput("   -sqlpasswd <password>        : SQL Server password");				
				u.appOutput("   -sqldblist <list>            : Comma-separated list of databases (default=blank=ALL)");				
				u.appOutput("   -version                     : show version of this tool");
				u.appOutput("   -help [ <helpoption> ]       : show help information. <helpoption> can be one of:");		
				u.appOutput("                                  reportoption, encoding, importfmt, exclude");		
				u.appOutput("   -explain                     : some high-level migration guidance");		
				if (CompassUtilities.devOptions) {		
				u.appOutput("");
				u.appOutput("For development only:");
				u.appOutput("   -dbgreport                   : use fixed report name (no timestamp)");
				u.appOutput("   -dbgnotimestamp              : suppress timestamp in DEBUG output lines");
				u.appOutput("   -echocapture                 : echo captured items to stdout");
				u.appOutput("   -parsetree                   : print parse tree & copy parsed batches to file");
				u.appOutput("   -parseonly                   : parse & print parse tree, no analysis");
				u.appOutput("   -stdin                       : read from stdin, parse only (no analysis) and exit");
				u.appOutput("   -noSLL                       : do not use SLL mode (slow; for troubleshooting only)");
				u.appOutput("   -showtokens                  : print lexer tokens");
				u.appOutput("   -antlrtrace                  : print ANTLR parsing trace");
				u.appOutput("   -antlrdiagnostics            : print ANTLR diagnostics");
				u.appOutput("   -popupwindow                 : automatically open report in browser");				
				}
				u.appOutput("");
				u.appOutput(CompassUtilities.userDocText+": "+CompassUtilities.userDocURL);
				if (!u.newVersionAvailable.isEmpty()) u.appOutput("\n"+u.newVersionAvailable);
				quitNow = true;
				return;
			}
			if (arg.equals("-explain")) {
				u.appOutput("Babelfish Compass is a command-line-only tool, running on Windows, Linux and Mac.\nIt takes one or more DDL/SQL scripts as input and generates a compatibility assessment report.\nThe purpose of Babelfish Compass is to analyze a SQL Server DDL/SQL script for compatibility with Babelfish,\nto inform a decision about whether it is worth considering starting a migration project to Babelfish.\nBabelfish Compass can optionally connect directly to a SQL Server instance, by skipping steps 1 & 2 below.\n\nTake the following steps:\n1. Generate DDL for the SQL Server database(s) in question\n   with SSMS (right-click a database --> Tasks --> Generate Scripts.\n   Make sure to enable triggers, collations, logins, owners and permissions (disabled in SSMS by default).\n2. Use the resulting DDL/SQL script as input for Babelfish Compass to generate an assessment report\n(NB: instead of steps 1 & 2 above, you can also use the -sqlendpoint/-sqllogin/-sqlpasswd flags to connect\nto the SQL Server and generate the DDL automatically; make sure to read the corresponding\nsection in the Compass User Guide first)\n3. Discuss the contents of the Compass report with the application owner and interpret the findings in the\n   context of the application to be migrated. Try identifying parts of the application that can be excluded\n   from the migration, for example because it is no longer used.\n4. Keep in mind that a Babelfish migration involves more than just the server-side DDL/SQL code\n   (e.g. data migration, client applications, external interfaces, etc.)\n\nRun "+CompassUtilities.thisProgExec+" -help for further usage info.\n\nMore information about working with Babelfish is available at\nhttps://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/babelfish.html and https://babelfishpg.org/");
				u.appOutput("");
				u.appOutput(CompassUtilities.userDocText+": "+CompassUtilities.userDocURL);
				quitNow = true;
				return;				
			}
			if ((arg.equals("-babelfish-version") || arg.equals("-babelfish_version"))) {
				if (i == args.length) {
					u.appOutput("Must specify argument for -babelfish-version ");
					u.errorExit();
				}

				u.targetBabelfishVersion = args[i];
				userSpecifiedBabelfishVersion = true;
				i++;
				continue;
			}
			if (arg.equals("-appname")) {
				if (i == args.length) {
					u.appOutput("Must specify argument for -appname");
					u.errorExit();
				}
				forceAppName = true;
				applicationName = args[i];
				String invalidMsg = CompassUtilities.nameFormatValid("appname", applicationName);
				if (!invalidMsg.isEmpty()) {
					u.appOutput("Application name '"+applicationName+"' contains invalid character(s) "+invalidMsg);
					u.errorExit();
				}
				i++;
				continue;
			}
			if (arg.equals("-reportfile")) {
				if (i == args.length) {
					u.appOutput("Must specify argument for -reportfile");
					u.errorExit();
				}
				forceReportName = true;
				reportFileName = args[i];
				String invalidMsg = CompassUtilities.nameFormatValid("report", reportFileName);
				if (!invalidMsg.isEmpty()) {
					u.appOutput("Report file name '"+reportFileName+"' contains invalid character(s) "+invalidMsg);
					u.errorExit();
				}
				i++;
				continue;
			}
			if (arg.equals("-replace")) {
				replaceFiles = true;
				continue;
			}
			if (arg.equals("-add")) {
				addReport = true;
				continue;
			}
			if (arg.equals("-list")) {
				listContents = true;
				continue;
			}
			if (arg.equals("-nooverride")) {
				u.userConfig = false;
				continue;
			}			
			if (arg.equals("-userconfigfile") || arg.equals("-usercfgfile") || arg.equals("-userconfig") || arg.equals("-usercfg")) { 
				userCfgFile = arg;				
				if (i >= args.length) {
					System.out.println("Must specify value with -userconfigfile");
					u.errorExit();
				}
				u.userCfgFileName = args[i];
				
				// does file exist?	
				boolean hasNoOverride = false;			
				for (int j = 0; j < args.length; j++) {
					if (args[j].equals("-nooverride")) {
						hasNoOverride = true;
						break;
					}
				}				
				if (!hasNoOverride) {	
					String userConfigFilePathName = "";				
					try { 
						userConfigFilePathName = u.getUserCfgFilePathName(u.userCfgFileName);
					} catch (Exception e) {}													
					File f = new File(userConfigFilePathName);
					if (!f.exists()) {
						System.out.println("User config file ["+userConfigFilePathName+"] not found");
						if (u.userCfgFileName.toUpperCase().startsWith(u.getDocDirPathname().toUpperCase())) {
							System.out.println("NB: Specify only the file name, not the full pathname!");							
						}											
						u.errorExit();	
					}
				}
				i++;
				continue;
			}				
			if (arg.equals("-optimistic") || arg.equals("-optimist")) { 
				optimisticFlag = true;
				u.userCfgFileName = u.optimisticUserCfgFileName;
				
				// does file exist?	
				boolean hasNoOverride = false;			
				for (int j = 0; j < args.length; j++) {
					if (args[j].equals("-nooverride")) {
						hasNoOverride = true;
						break;
					}
				}				
				if (!hasNoOverride) {	
					String userConfigFilePathName = "";				
					try { 
						userConfigFilePathName = u.getUserCfgFilePathName(u.userCfgFileName);
					} catch (Exception e) {}													
					File f = new File(userConfigFilePathName);
					if (!f.exists()) {
						// perhaps we need to install the optimistic.cfg file first
						try {
							u.installOptimisticCfgFile();
						} catch (Exception e) { /* nothing*/ }
						
						// now check again
						if (!f.exists()) {
							System.out.println("User config file ["+userConfigFilePathName+"] not found");
							u.errorExit();	
						}
						if (userConfigFilePathName.equalsIgnoreCase(u.optimisticUserCfgFileName)) {
							System.out.println("Restore the 'optimistic' .cfg file manually by picking it up from the Compass .zip file.");							
						}							
					}
					u.appOutput("Using predefined user config file '"+userConfigFilePathName+"'");
				}
				
				// also enable rewrite
				u.rewrite = true;
				
				continue;
			}				
			
			if (arg.equals("-importfmt") || arg.equals("-importformat")) {
				if (i == args.length) {
					u.appOutput("Must specify argument for -importfmt. Valid values are: "+u.importFormatSupportedDisplay);
					u.errorExit();
				}
				importFormatArg = true;
				u.importFormat = args[i].toLowerCase();
				if (!u.importFormatSupported.contains(u.importFormat)) {
					u.appOutput("Invalid value for -importfmt. Valid values are: "+u.importFormatSupportedDisplay);
					u.errorExit();
				}
				i++;
				continue;
			}			
			if (arg.equals("-nodedup")) {
				u.deDupExtracted = false;
				continue;			
			}
			if (arg.equals("-delete")) {
				deleteReport = true;
				continue;			
			}
			if (arg.equals("-quotedid")) {
				if (i == args.length) {
					u.appOutput("Must specify -quotedid ON or -quotedid OFF");
					u.errorExit();
				}
				quotedIdentifier = args[i].toUpperCase();
				if (!CompassUtilities.OnOffOption.contains(quotedIdentifier)) {
					u.appOutput("Must specify -quotedid ON or -quotedid OFF");
					u.errorExit();
				}
				i++;
				continue;
			}
			if (arg.equals("-noreport")) {
				generateReport = false;
				continue;
			}
			if (arg.equals("-reportonly")) {
				reportOnly = true;
				continue;
			}
			if (arg.equals("-analyze")) {
				reAnalyze = true;
				continue;
			}
			if (arg.equals("-importonly")) {
				importOnly = true;
				continue;
			}
			if (arg.equals("-rewrite")) {
				u.rewrite = true;
				continue;
			}						
			if (arg.equals("-noupdatechk")) {
				u.updateCheck = false;
				continue;
			}
			if (arg.equals("-nopopupwindow")) {
				popupwindow = false;
				continue;
			}	
			if (arg.equals("-popupwindow")) {  // should only be used in develop mode but it does not hurt
				popupwindow = true;
				continue;
			}												
			if (arg.equals("-noreportcomplexity")) {
				u.reportComplexityScore = false;
				continue;
			}		
			if (arg.equals("-encoding")) {
				if (i >= args.length) {
					System.out.println("missing encoding on -encoding");
					return;
				}
				userEncoding = args[i];
				if (userEncoding.equalsIgnoreCase("help")) {
					encodingHelp();
				}
				i++; 
				continue;
			}			
			if ((arg.equals("-reportoption") || arg.equals("-reportoptions"))) {   
				if (i == args.length) {
					u.appOutput("Must specify arguments for -reportoption ");
					u.errorExit();
				}
				reportOption = true;
				List<String> reportOptions = Arrays.asList("xref", "detail", "status", "filter", "apps", "batchnr", "linenrs", "notabs", "hints");
				List<String> reportOptionsXref = Arrays.asList("", "all", "object", "feature");
				List<String> reportFlags = new LinkedList<>(Arrays.asList(args[i].split(",")));
				reportFlags.removeIf(String::isEmpty);
				for(String option : reportFlags) {
					String optionValue = a.getOptionValue(option);
					option = a.getOptionName(option);							
					if (reportOptions.contains(option.toLowerCase())) {
						// OK
						if (option.equals("xref")) {
							if (!reportOptionsXref.contains(optionValue)) {
								u.appOutput("Invalid option '"+optionValue+"' for -reportoption xref=");
								u.appOutput("Valid options: "+reportOptionsXref);
								u.errorExit();								
							}
							CompassUtilities.listHints = true;
							if (optionValue.isEmpty()) CompassUtilities.reportOptionXref = "all";
							else CompassUtilities.reportOptionXref = optionValue.toLowerCase();
						}
						else if (option.equals("status")) {
							List<String> statusOptions = new ArrayList<>(u.validSupportOptionsCfgFile);
							statusOptions.add(0,u.Supported);
							statusOptions.add(0,"ALL");
							if (!statusOptions.contains(optionValue.toUpperCase())) {
								u.appOutput("Invalid option '"+optionValue+"' for -reportoption status=");
								CompassUtilities.listToLowerCase(statusOptions);
								u.appOutput("Valid options: "+statusOptions);
								u.errorExit();								
							}
							CompassUtilities.reportOptionStatus += " " + optionValue.toLowerCase() + " ";
						}
						else if (option.equals("detail")) {
							CompassUtilities.reportOptionDetail = option;  // no option values defined right now
						}
						else if (option.equals("apps")) {
							CompassUtilities.reportOptionApps = option;  
						}
						else if (option.equals("batchnr")) {
							CompassUtilities.reportShowBatchNr = option;  
						}
						else if (option.equals("notabs")) {
							CompassUtilities.linkInNewTab = false;  
							CompassUtilities.tgtBlank = "";  
							CompassUtilities.reportOptionNotabs = true;
						}
						else if (option.equals("linenrs")) {
							Integer ln = 0;
							try {
								ln = Integer.parseInt(optionValue);
								if (ln < 1) Integer.parseInt("x");
							} catch (Exception e) { 
								u.appOutput("Invalid option '"+optionValue+"' for -reportoption linenrs=, must be number > 0");
								u.errorExit();								
							}
							CompassUtilities.reportOptionLineNrs = true;
							CompassUtilities.maxLineNrsInList = ln;  
						}
						else if (option.equals("filter")) {							 
							if (optionValue.isEmpty()) {
								u.appOutput("No value specified for option 'filter='");
								u.errorExit();				
							}
							CompassUtilities.reportOptionFilter = optionValue; 
							u.generateCSV = false;
						}
						else if (option.equals("hints")) {
							CompassUtilities.listHints = true;
						}
					}
					else {
						u.appOutput("Invalid option '"+option+"' for -reportoption");
						u.appOutput("Valid options: "+reportOptions);
						u.errorExit();
					}
				}				
				i++;
				continue;
			}
			if (arg.equals("-pgimportappend")) {	
				pgImportAppend = true;	
				continue;
			}
			if (arg.equals("-pgimportnodoublequotes")) {	
				u.pgImportNoDoubleQuotes = true;	
				continue;
			}	
			if (arg.equals("-pgimporttable")) {	
				pgImportTable = true;					
				if (i >= args.length) {
					System.out.println("Must specify table name with -pgimporttable");
					u.errorExit();
				}
				u.psqlImportTableName = args[i];
				if (CompassUtilities.getPatternGroup(u.psqlImportTableName, "^(\\w+(\\.\\w+)?)$", 1).isEmpty()) {
					u.appOutput("Invalid table name specified with -pgimporttable");
					u.errorExit();
				}
				i++; 
				continue;
			}
			if (arg.equals("-pgimport")) {					
				if (i == args.length) {
					u.appOutput("Must specify arguments for -pgimport: host,port,username,password,dbname ");
					u.errorExit();
				}
				pgImportFlags = new LinkedList<>(Arrays.asList(args[i].split(",")));
				pgImportFlags.removeIf(String::isEmpty);
				if (pgImportFlags.size() != 5) {
					u.appOutput("Must specify 5 arguments for -pgimport: host,port,username,password,dbname ");
					u.errorExit();								
				}
				pgImport = true;	
				generateReport = false;
				i++;
				continue;		
			}
			// always report these syntax issues from now on:
//			if (arg.equals("-syntax") || arg.equals("-syntax_issues")) {	
//				u.reportSyntaxIssues = true;   	
//				continue;
//			}			
			if (arg.equals("-"+CompassUtilities.reverseString("m"+"u"+"s"+"k"+"c"+"e"+"h"+"c"))) {
				u.configOnly = true;
				continue;
			}
			if (arg.equals("-mergereport")) { // special purpose only, to process Very Large Numbers of SQL files		
				if (i >= args.length) {
					System.out.println("Must specify target report name with -mergereport");
					u.errorExit();
				}
				mergeReport = args[i];
				i++; 				
								
				try { 
					if ((!u.checkReportExists(mergeReport))) {
						u.checkDir(u.getReportDirPathname(mergeReport), false, true);
						if ((!u.checkReportExists(mergeReport))) {
							u.appOutput("Target merge report '"+mergeReport+"' not found");
							u.errorExit();							
						}
					}
				}
				catch (Exception e) {
					u.appOutput("Error checking directory existence for merge target report ("+mergeReport+")");
					u.errorExit();
				}			
				
				if (mergeReport.equalsIgnoreCase(reportName)) {
					u.appOutput("merge target report cannot be the saem as current report ("+mergeReport+")");
					u.errorExit();					
				}
				
				continue;								
			}
			if (arg.equals("-recursive")) {
				recursiveInputFiles = true;
				continue;
			}
			if (arg.equals("-include")) {
				if (i >= args.length) {
					System.out.println("missing include file name pattern on -include");
					u.errorExit();
				}
				if (includePattern != null) {
					// Can only specify -include once per invocation
					System.out.println("Only one -include flag allowed. Separate multiple file name patterns with a comma.");
					u.errorExit();
				}
				includePattern = parseInputPattern(args[i]);
				i++;
				continue;
			}
			if (arg.equals("-exclude")) {
				if (i >= args.length) {
					System.out.println("missing exclude file name pattern on -exclude");
					u.errorExit();
				}
				if (excludePattern != null) {
					// Can only specify -exclude once per invocation
					System.out.println("Only one -exclude flag allowed. Separate multiple file name patterns with a comma.");
					u.errorExit();
				}
				excludePattern = parseInputPattern(args[i]);
				i++;
				continue;
			}
			if (arg.equals("-anon")) { 
				u.anonymizedData = true;
				continue;
			}							
			if (arg.equals("-sqlendpoint")) { 
				if (i >= args.length) {
					System.out.println("Must specify value with -sqlendpoint");
					u.errorExit();
				}
				autoDDL = true;
				sqlEndpoint = args[i];
				sqlEndpoint = u.stripStringQuotes(sqlEndpoint).trim();		
				CompassUtilities.reportOptionApps = "apps";					
				i++;
				continue;
			}
			if (arg.equals("-sqllogin")) { 
				if (i >= args.length) {
					System.out.println("Must specify value with -sqllogin");
					u.errorExit();
				}
				autoDDL = true;					
				sqlLogin = args[i];	
				sqlLogin = u.stripStringQuotes(sqlLogin).trim();							
				i++;				
				continue;
			}
			// Note: all variations should be tested for when remove the password from the cmdline for the report header
			if (arg.equals("-sqlpasswd") || arg.equals("-sqlpassword")) {   
				if (i >= args.length) {
					System.out.println("Must specify value with "+arg);
					u.errorExit();
				}
				autoDDL = true;					
				sqlPasswd = args[i];	
				sqlPasswd = u.stripStringQuotes(sqlPasswd).trim();	
				if (u.onWindows) {
					// if the passwd contains a ^ char, double it -- it's a Windows batch escape char and it will be lost unless doubled up
					if (sqlPasswd.contains("^")) {
						sqlPasswd = sqlPasswd.replaceAll("\\^", "^^");	
					}
				}				
				i++;					
				continue;
			}
			if (arg.equals("-sqldblist")) { 
				if (i >= args.length) {
					System.out.println("Must specify value with -sqldblist");
					u.errorExit();
				}				
				sqlDBList = args[i].trim();	
				String systemDB = CompassUtilities.getPatternGroup(sqlDBList, "^.*?\\b(master|tempdb|model|msdb)\\b.*$", 1); 
				if (!systemDB.isEmpty()) {
					System.out.println("Generating DDL for SQL Server system database '"+systemDB+"' not supported");
					u.errorExit();					
				}
				systemDB = CompassUtilities.getPatternGroup(sqlDBList, "^.*?\\b(rdsadmin)\\b.*$", 1); 
				if (!systemDB.isEmpty()) {
					System.out.println("Generating DDL for '"+systemDB+"' database not supported");
					u.errorExit();
				}				
				sqlDBList = u.stripStringQuotes(sqlDBList).trim();					
				i++;					
				continue;
			}
			if (arg.equals("-csvformat") || arg.equals("-csvfmt")) {	
				if (i == args.length) {
					u.appOutput("Must specify value with -csvformat");
					u.errorExit();
				}
				u.generateCSVFormat = args[i].toLowerCase();
				if (!u.CSVFormats.contains(u.generateCSVFormat)) {
					u.appOutput("Invalid value for -csvformat. Valid options: "+ u.CSVFormats);
					u.errorExit();
				}
				i++;
				continue;
			}												
			if (arg.equals("-csvitemidfile")) {	
				if (i == args.length) {
					u.appOutput("Must specify value with -csvitemidfile");
					u.errorExit();
				}

				u.CustomItemIDFileName = args[i];
				
				String CustomItemIDPathName = "";
				try { 
					CustomItemIDPathName = u.getCustomItemIDPathName();
				} catch (Exception e) {}					
				u.customItemIDPathNameUser = true;
				File f = new File(CustomItemIDPathName);
				if (!f.exists()) {
					u.appOutput("Specified item ID file '"+CustomItemIDPathName+"' not found: not generating item IDs in .csv file");
					if (u.CustomItemIDFileName.toUpperCase().startsWith(u.getDocDirPathname().toUpperCase())) {
						System.out.println("NB: Specify only the file name, not the full pathname!");							
					}						
				}		
				i++;
				continue;
			}
			if (CompassUtilities.devOptions) {
				popupwindow = false;				
				if (arg.equals("-popupwindow")) { 
					popupwindow = true;
					continue;
				}							
				if (arg.equals("-debug")) {   // development only
					if (i == args.length) {
						u.appOutput("Must specify arguments for -debug ");
						u.errorExit();
					}
					List<String> debugValues = new LinkedList<>(Arrays.asList(args[i].split(",")));		
					debugValues.removeIf(String::isEmpty);
					u.specifiedDbgOptions.addAll(debugValues);
					u.specifiedDbgOptions.retainAll(u.dbgOptions);
					u.appOutput("Debug flag set: "+ u.specifiedDbgOptions);
					if (u.specifiedDbgOptions.isEmpty()) {
						u.appOutput("Valid debug options: " + u.dbgOptions);
						u.errorExit();
					}
					u.setDebugFlags();
					i++;
					continue;
				}
				if (arg.equals("-echocapture")) { // development only
					u.echoCapture = true;
					continue;
				}
				if (arg.equals("-dbgreport") || (arg.equals("-devreport"))) { // development only
					u.stdReport = true;
					continue;
				}
				if (arg.equals("-dbgnotimestamp")) { // development only
					u.dbgTimestamp = false;
					continue;
				}
				if (arg.equals("-parseonly")) { // development only
					parseOnly = true;
					dumpParseTree = true;
					continue;
				}
				if (arg.equals("-parsetree")) { // development only
					dumpParseTree = true;
					dumpBatchFile = true;
					continue;
				} 
				if (arg.equals("-stdin")) {  // development only
					readStdin = true;
					dumpParseTree = true;
					parseOnly = true;
					continue;
				}			
				if (arg.equals("-noSLL")) {  // development only
					antlrSLL = false;
					continue;
				}
				if (arg.equals("-showtokens")) {  // development only
					antlrShowTokens = true;
					continue;
				}
				if (arg.equals("-antlrtrace")) {  // development only
					antlrTrace = true;
					continue;
				}
				if (arg.equals("-antlrdiagnostics")) {  // development only
					antlrDiagnostics = true;
					continue;
				}
				if (arg.equals("-caching")) { // development only
					CompassUtilities.caching = true;
					continue;
				}
				if (arg.equals("-symtab_col")) { // development only: put columns in symbol table (requires further development to be useful)
					u.buildColSymTab = true;
					u.appOutput("Experimental: Including columns in symbol table in pass 1");
					continue;
				}					
				if (arg.equals("-symtab_all")) { // development only: load symtab for all applications -- but we shouldn't need this
					u.symTabAll = true;
					continue;
				}											
			}
			// arguments must start with [A-Za-z0-9 _-./] : anything else is invalid
			if (CompassUtilities.getPatternGroup(arg.substring(0,1), "^([\\w\\-\\.\\/])$", 1).isEmpty()) {
				System.out.println("Invalid option ["+arg+"]. Try -help");
				u.errorExit();
			}
							
			if (arg.charAt(0) != '-') { // non-options
				if (reportName == null) {
					// this is the report name, must be first argument
					if (i != 1) {
						u.appOutput("Report name must be the first argument (try -help)");
						u.errorExit();
					}
					reportName = arg.trim();
					u.reportName = reportName;
					if (reportName.isEmpty()) {
						u.appOutput("Report name cannot be blank");
						u.errorExit();
					}
					if (reportName.length() > reportNameMaxLength) {
						u.appOutput("Report name '"+reportName+"' exceeds "+reportNameMaxLength+" characters");
						u.errorExit();						
					}
					String invalidMsg = CompassUtilities.nameFormatValid("report", reportName);
					if (!invalidMsg.isEmpty()) {
						u.appOutput("Report name '"+reportName+"' contains invalid character(s) "+invalidMsg);
						boolean hint = false;
						if (invalidMsg.equals("'"+File.separator+"'")) hint = true;
						if (u.onWindows) {
							if (!CompassUtilities.getPatternGroup(reportName, "^([a-z]:\\\\)", 1).isEmpty()) hint = true;
						}
						else {
							if (reportName.startsWith(File.separator)) hint = true;
						}

						if (hint) {
							u.appOutput("Did you forget to specify the report name? This must be the first argument (try -help)");							
						}
						u.errorExit();
					}
				} else {
					if (u.onMac || u.onLinux) {
						if (arg.contains("\\")) {
							// handle case of file specified with both quotes and backslashes (shouldn't happen, but just on case):  "My\ Big\ File.sql"
							arg = arg.replaceAll("\\\\", "");
						}						
					}
					if (arg.endsWith(","))  {
						//user has specified a comma-separated list of files; be kind to the user and strip the comma
						arg = u.removeLastChar(arg);
					}
					
					tmpInputFiles.add(arg);
				}
				continue;
			}
			
			// exit on invalid argument
			System.out.println("Invalid option ["+arg+"]. Try -help");
			u.errorExit();
		}

		// Now we can processes all of the input files and properly deal with
		// -recursive, -include and -exclude
		for (String file : tmpInputFiles) {
			addInputFile(file);
		}

		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "onWindows=["+CompassUtilities.onWindows+"] onMac=["+CompassUtilities.onMac+"] onLinux=["+CompassUtilities.onLinux+"]  ", u.debugOS);
		
		inputFilesOrig.addAll(tmpInputFiles);
		
		// check for updates of Compass and print reminder
		u.checkForUpdate();		
	}

	public static void main(String[] args) throws Exception {
		u.appOutput(CompassUtilities.thisProgName + " v." + CompassUtilities.thisProgVersion + ", " + CompassUtilities.thisProgVersionDate);
		u.appOutput(CompassUtilities.thisProgNameLong);
		u.appOutput(CompassUtilities.copyrightLine);
		u.appOutput("");			
		
 		if (args.length < 1) {
			u.appOutput("No arguments specified. Try -help");
			return;
 		}
 		
 		// get timestamp
		startRunDate = new Date();		
		startRunFmt = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(startRunDate);		
		
 		// validate arguments
		Compass comp = new Compass(args);

 		// don't allow installation into reports root directory; this casues problems with the optimistic .cfg file for example
 		String cwd = FileSystems.getDefault().getPath("").toAbsolutePath().toString();
 		String dirPath = u.getDocDirPathname();
 		if (cwd.equalsIgnoreCase(dirPath)) {
 			u.appOutput("You cannot install/run Babelfish Compass in the reports root folder ("+dirPath+")");
 			String suggestedDir = "C:\\BabelfishCompass";
 			if (u.onLinux) suggestedDir = "/home/<your-username>/BabelfishCompass";
 			if (u.onMac) suggestedDir = "/Users/<your-username>/BabelfishCompass";
 			u.appOutput("Suggested install directory: "+suggestedDir);
 			quitNow = true;
 			return; 			
 		}
 		
 		if (quitNow) {
			return;
 		}
 		
		if (showVersion) {
			return;
		} 		
		
		// ensure report root dir exists
		u.checkDir(u.getDocDirPathname(), false, true);
		 	
 		// read config file; also creates user config file if it does not exist
 		u.cfgFileName = u.defaultCfgFileName; // todo: make configurable?
 		if (u.userCfgFileName.equals(u.uninitialized)) u.userCfgFileName = u.defaultUserCfgFileName;  // using default
		cfg.validateCfgFile(u.cfgFileName, u.userCfgFileName);		
		assert u.cfgFileFormatVersionRead > 0 : "cfgFileFormatVersionRead=[" + u.cfgFileFormatVersionRead + "], must be > 0";
		if (u.cfgFileFormatVersionRead > u.cfgFileFormatVersionSupported) {
			u.appOutput("File format version number in " + u.cfgFileName + " is " + u.cfgFileFormatVersionRead+".");
			u.appOutput("This version of " + CompassUtilities.thisProgName + " supports version " + u.cfgFileFormatVersionSupported + " or earlier.");
			u.errorExit();			
		}		
		
		// validate combinations of options specified		
		if (!optionsValid()) {
			return;
		}		
				 		
 		// perform PG import
		if (pgImport) {
			runPGImport();
			return;
		}
		
		// generate DDL through SMO
		if (autoDDL) {
			getAutoDDL();
		}					

		// only for debugging the config class:
//		if (u.debugCfg) {
//			CompassTestConfig.testConfig();
//		}

		// copy cfg structure
		a.cfg = cfg;

		// init Babelfish target version at latest version, unless user specified a version
		if (userSpecifiedBabelfishVersion) {
			// validate user-specified version
			if (!CompassUtilities.getPatternGroup(u.targetBabelfishVersion, "^(\\d+\\.\\d+)(\\.)?$", 1).isEmpty()) {
				u.targetBabelfishVersion += ".0";
				u.targetBabelfishVersion = u.applyPatternFirst(u.targetBabelfishVersion, "\\.\\.", ".");				
			}
			if (!cfg.isValidBabelfishVersion(u.targetBabelfishVersion)) {
				u.appOutput("Invalid target Babelfish version specified: [" + u.targetBabelfishVersion + "]\nValid Babelfish versions: " + cfg.validBabelfishVersions());
				return;
			}
		} else {
			u.targetBabelfishVersion = cfg.latestBabelfishVersion();
		}
				
		if (userEncoding != null) {
			if (userEncoding.equals("help")) {
				u.appOutput("Allowed values for -encoding:");
				for (String cs : Charset.availableCharsets().keySet()) {
					u.appOutput(u.lineIndent + cs);
				}
				return;
			}
			
			// process specified encoding 
			try {
				charset = Charset.forName(userEncoding);
			} catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
				u.appOutput("Invalid -encoding value specified: [" + userEncoding + "]\nUse '-encoding help' to list available encodings.");
				return;
			}			
		}
		
		if (readStdin) {
			// only take stdin input, skip other steps
		} 
		else {
			if (!reportOnly) {
				if (!inputFilesValid()) {
					u.appOutput("Input file(s) not found, aborting.");
					return;
				}
			}

			if (deleteReport) {
				// first delete the report dir before proceeding
				u.deleteReportDir(reportName); 
				if (inputFiles.size() == 0) {
					// nothing to do
					u.appOutput("No input files to process");
					return;
				}
			}

			if ((inputFiles.size() == 0)) {
				if (nrFileNotFound > 0) {
					u.appOutput("No input files to process");					
					return;
				}
				// check for non-existing report (e.g. misspelled) when doing report only
				if (!u.checkReportExists(reportName)) {
					u.appOutput("Report '"+reportName+"' does not exist");					
					return;
				}
			}
			if (u.checkReportExists(reportName, inputFiles, forceAppName, applicationName, replaceFiles, addReport)) {
				// we cannot proceed for some reason
				return;
			}			
			String reportDirName = u.getReportDirPathname(reportName);
			sessionLog = u.openSessionLogFile(reportName, startRunDate);
			//sessionLog = sessionLog.substring(reportDirName.length()+1);		
			if (u.execTest) u.openExecTestFile(reportName);
					
			if (listContents) {
				u.appOutput("Report name               : " + reportName);
				u.appOutput("Report directory location : " + reportDirName);
				u.appOutput("");				
				// list all files/apps currently imported for this report
				u.listReportFiles(reportName);
				return;
			}
			
			cmdFlags.removeAll(inputFilesOrig);	
			
			//format PG version
			u.targetBabelfishPGVersionFmt = u.formatPGversion(u.targetBabelfishVersion);		

			// write Compass version to log file now that we have it opened
			u.reportOutputOnly(CompassUtilities.thisProgName + " v." + CompassUtilities.thisProgVersion + ", " + CompassUtilities.thisProgVersionDate);
			u.reportOutputOnly(CompassUtilities.thisProgNameLong);
			u.reportOutputOnly(CompassUtilities.copyrightLine);
			
			u.appOutput("");
							
			u.appOutput("Run starting               : " + startRunFmt + " (" + u.onPlatform + ")");
			String tmp = "";
			tmp = u.cfgFileName + " file : v." + cfg.latestBabelfishVersion() + ", " + u.cfgFileTimestamp;
			CompassUtilities.reportHdrLines += tmp + "\n";			
			u.appOutput(tmp);
			tmp = u.targetBabelfishVersionReportLine + u.targetBabelfishVersion + u.targetBabelfishPGVersionFmt;
			CompassUtilities.reportHdrLines += tmp + "\n";			
			u.appOutput(tmp);
			tmp = "Command line arguments     : " + String.join(" ", cmdFlags);
			CompassUtilities.reportHdrLines += tmp + "\n";			
			u.appOutput(tmp);

			String inputFilesReport = String.join(" ", inputFilesOrig);
			if (!autoDDL) {
				if (inputFilesReport.isEmpty() && reAnalyze) inputFilesReport = "(using previously imported files)";
				tmp = "Command line input files   : " + inputFilesReport;
			}
			else {
				inputFilesReport = inputFilesReport.replaceAll(u.escapeRegexChars(SMOOutputFolder + File.separator), "");
				tmp =  "DDL input files            : " + inputFilesReport + "\n";
				tmp += "DDL input files location   : " + SMOOutputFolder;
			}
			
			CompassUtilities.reportHdrLines += tmp + "\n";			
			u.appOutput(tmp);
			
			tmp = "no";
			if (u.rewrite) tmp = "yes";
		    u.appOutput("Rewriting enabled          : " + tmp);			
		    
			tmp = "User .cfg file (overrides) : " + CompassConfig.userConfigFilePathName;
			if (!u.userConfig) {
				tmp += " (ignored)";
			}
			CompassUtilities.reportHdrLines += tmp + "\n";			
			u.appOutput(tmp);
			u.appOutput("QUOTED_IDENTIFIER default  : " + quotedIdentifier);
			tmp = "Report name                : " + reportName;
			CompassUtilities.reportHdrLines += tmp;
			u.appOutput(tmp);
			u.appOutput("Report directory location  : " + reportDirName);
			u.appOutput("Session log file           : " + sessionLog);
			u.appOutput("");
		}
		// init time counters
		timeCount.put("elapsedRun",0L);
		timeCount.put("parseTime",0L);
		timeCount.put("parseTimeMax",0L);
		timeCount.put("rewriteTimeMax",0L);
		timeCount.put("analysisTimeP1",0L);
		timeCount.put("analysisTimeP1Max",0L);
		timeCount.put("analysisTimeP2",0L);
		timeCount.put("analysisTimeP2Max",0L);
		timeCount.put("report",0L);

		// start
		startRun = System.currentTimeMillis();								
									
		if (reAnalyze) {
			// create fresh symbol table and capture file
			u.deleteReAnalyze(reportName);					
		}
		
		// read custom item ID file, if applicable
		u.openCustomItemIDFile();

		if (reportOnly) {
			// only generate reported from already-captured items
			startTime = System.currentTimeMillis();
			u.createReport(reportName);
			endTime = System.currentTimeMillis();
			duration = (endTime - startTime);
			timeCount.put("report", timeCount.get("report") + (int) duration);
		} else {
			// ---- pass 1 --------------------------------------

			if ((inputFiles.size() > 0) || readStdin || reAnalyze) {
				u.analysisPass = 1;
				comp.processInput(startRunFmt);

				if (readStdin) {
					if (hasParseError) {
						u.appOutput(parseErrorMsg);
					}
					return;
				}
			}

			// ---- pass 2 --------------------------------------
			if (!parseOnly) {				
				if (importOnly) {
					u.appOutput("Not performing analysis or generating assessment report.\nUse -analyze later to analyze & generate a report.");
					u.appOutput("");		
				} else {
					u.analysisPass = 2;
					
					// do we have any input files in this report?
					List<Path> importFiles = u.getImportFiles(reportName);
					int nrImportFiles = importFiles.size();
					
					if (nrImportFiles > 0) {
						// read symbol table from disk, for all apps -- should not be required but keeping just because
						if (u.symTabAll) {
							try { u.readSymTab(reportName, ""); }
							catch (Exception e) {								
								u.appOutput("Error reading symbol table " + u.symTabFilePathName);
								throw e;
							}
						}
						comp.processInput(startRunFmt);
					} else {
						u.appOutput(nrImportFiles + " input files found for report " + reportName);
					}

					if (!generateReport) {
						// -noreport
						u.appOutput("Not generating assessment report.\nUse -reportonly or -reportoption to generate a report based on current analysis.");
					} else {
						startTime = System.currentTimeMillis();
						u.createReport(reportName);
						endTime = System.currentTimeMillis();
						duration = (endTime - startTime);
						timeCount.put("report", timeCount.get("report") + duration);						
					}
				}		
			}
		}

		endRun = System.currentTimeMillis();
		endRunFmt = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());		
		timeCount.put("elapsedRun", (endRun - startRun));
				
		comp.reportFinalStats();
		if (generateReport) {
			if (!(parseOnly || importOnly)) {
				u.closeReportFile();
			}
		}
		
		if (u.execTest) u.closeExecTestFile();
		
		
		if (!mergeReport.isEmpty()) {
			// copy all files into the target report (performance optimization for very large operations only)		
			String src = CompassUtilities.getReportDirPathname(reportName);	
			String tgt = CompassUtilities.getReportDirPathname(mergeReport);	
			u.appOutput("mergereport: copying details into '"+mergeReport+"'...");
			copyMergeReport(src, tgt, u.importDirName);
			copyMergeReport(src, tgt, u.capDirName);
			copyMergeReport(src, tgt, u.logDirName);
			if (u.rewrite && (u.nrRewritesDone > 0)) {
				copyMergeReport(src, tgt, u.rewrittenDirName);
			}
			if (totalParseErrors > 0) {
				copyMergeReport(src, tgt, u.errBatchDirName);	
			}		
		}
		
		// open generated report in browser
		if (generateReport) {
			if (popupwindow) {
				if (!(parseOnly || importOnly)) {
					// TODO consider replacing this with java.awt.Desktop::open
					if (CompassUtilities.onWindows) {
						u.runOScmd("cmd /c \"explorer.exe /n,/select,\"\"" + u.reportFilePathName + "\"\" \"");
						u.runOScmd("cmd /c \"explorer.exe \"\"" + u.reportFilePathName + "\"\" \"");
					} else if (CompassUtilities.onMac) {
						String cmd = " open . " + u.reportFilePathName;
						// temporary:
						//u.appOutput("*** Mac (dev msg): opening report in browser: cmd=["+cmd+"] ");			
						u.runOScmd(cmd);
					} else if (CompassUtilities.onLinux) {
						// TBD - assuming no GUI is present
						// TODO We could check for an X session or we could open in lynx if it's available
						//String cmd = " open . " + u.reportFilePathName;
						//u.runOScmd(cmd);				
					}
				}
			}
		}
		
		u.closeSessionLogFile();		
	}
	
	protected static void copyMergeReport(String src, String tgt, String dirName) throws Exception {	
		if (CompassUtilities.onWindows) {
			String cmdA = "robocopy "+src+File.separator;
			String cmdB = " "+tgt+File.separator;
			String cmdC = " *.* /E /R:1 /W:1 /XX > NUL 2>&1";
			String cmd = cmdA+dirName+cmdB+dirName+cmdC;
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "mergereport: copying '"+dirName+"' details into '"+mergeReport+"' :  ["+cmd+"]" , u.debugDir);				
			u.runOScmd(cmd);				
		}
		else {
			String cmdA = "cp -R "+src+File.separator;
			String cmdB = File.separator+"* "+tgt+File.separator;
			String cmdC = " > /dev/null";
			String cmd = cmdA+dirName+cmdB+dirName+cmdC;	
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "mergereport: copying '"+dirName+"' details into '"+mergeReport+"' :  ["+cmd+"]" , u.debugDir);				
			u.runOScmd(cmd);										
		}				
	}
	
	protected void encodingHelp() {			
		u.appOutput("Default encoding on this system: " + Charset.defaultCharset());
		u.appOutput("\nAvailable encodings:");
		String s="";
		for (String c : Charset.availableCharsets().keySet()) {
			s += c + "  ";
			if (s.length() > 80) {
				u.appOutput(s);
				s = "";
			}
		}
		if (s.length() > 0) {
			u.appOutput(s);
		}
		u.errorExit();
	}

	protected static void addInputFile(String file) throws InvalidPathException {
		String fileOrig = file;
		if (file == null) return;
		
		Path path = null;
		int depth = Integer.MAX_VALUE;

		// We got a literal asterisk character in the path because the command shell didn't expand the filenames
		// Note that this shouldn't happen in normal use cases since all shells on both *nix and Windows should
		// expand the * character unless quoted/escaped. On Windows, Powershell will properly expand glob characters
		// in the middle of a path like *nix shells do, but Command.com will only expand glob characters at the end
		// of a path. On Windows, java.nio.Path will throw an exception if the path string includes an asterisk.
		// We can at least try to deal with *, *.*, *.ext
		if (file.contains("*")) {
			String endOfPath = file.substring(file.lastIndexOf(FileSystems.getDefault().getSeparator()) + 1);
			if (endOfPath.contains("*")) {
				// Remove the asterisk from the file so we don't get an IllegalCharacterException on Windows
				file = file.substring(0, file.length() - endOfPath.length());
				if (file.isEmpty()) {
					file = ".";
				}
				path = Paths.get(file).normalize();
				if (!path.toString().isEmpty()) {
					if (!recursiveInputFiles) {
						// The asterisk character is at the end of the path, but we weren't asked to process
						// input recursively. Process only the immediately enclosing parent directory (depth of 1).
						recursiveInputFiles = true;
						depth = 1;
					}
					// Use the glob that didn't get expanded by the command shell as the inlcude pattern
					includePattern = endOfPath;
				}
			} else {
				// Error - path contains an asterisk character but it's not at the end. User should use the
				// -includes switch to build a glob pattern for the files they want to process.
			}
		} else {
			// Normal use case
			path = Paths.get(file);
		}

		// Make -include override -exclude if they both contain the same pattern
		normalizeIncludeExcludePatterns();		

		// These matcher instances need to be final to be included in the lambda expression below
		final PathMatcher includes = (includePattern != null && !includePattern.isEmpty()) ?
				FileSystems.getDefault().getPathMatcher(globSyntaxAndPattern(includePattern, path)) :
				null;
		final PathMatcher excludes = (excludePattern != null && !excludePattern.isEmpty()) ?
				FileSystems.getDefault().getPathMatcher(globSyntaxAndPattern(excludePattern, path)) :
				null;

		if ((Files.isDirectory(path)) && (!path.toString().isEmpty())) {
			if (recursiveInputFiles) {
				// Recursively walk the directory tree and add files that we can read and match our filter patterns
				Set<String> inputFilesToAdd;
				try (Stream<Path> directoryStream = Files.walk(path, depth, FileVisitOption.FOLLOW_LINKS)) {
					inputFilesToAdd = directoryStream
							.filter(Files::isRegularFile)
							.filter(Files::isReadable)
							.filter(p -> {
								if (includes != null && !includes.matches(p)) {
									u.appOutput("Ignoring not included path '" + p.toString() + "'");
								}
								return includes == null || includes.matches(p);
							})
							.filter(p -> {
								if (excludes != null && excludes.matches(p)) {
									u.appOutput("Excluding path '" + p.toString() + "'");
								}
								return excludes == null || !excludes.matches(p);
							})
							.map(Path::toString)
							.collect(Collectors.toSet());
					inputFiles.addAll(inputFilesToAdd);
				} catch (IOException | UncheckedIOException ioe) {
					nrFileNotFound++;
					u.appOutput("Can't access input file '" + file + "'");
				}
			} // otherwise ignore directories
		} else if (Files.isRegularFile(path) && (includes == null || includes.matches(path))
				&& (excludes == null || !excludes.matches(path))) {
			inputFiles.add(path.toString());
		} else {
			if (excludes != null && excludes.matches(path)) {
				u.appOutput("Excluding path '" + path.toString() + "'");
			} else if (includes != null && !includes.matches(path)) {				
				u.appOutput("Ignoring not included path '" + path.toString() + "'");
			} else {
				// TODO log unexpected case here?
				// File does not exist or can't be read by the current process
				nrFileNotFound++;
				String notFile = path.toString();
				if (notFile.isEmpty()) notFile = fileOrig;
				u.appOutput("Input file '" + notFile + "' is not a directory or a file");
			}
		}		
	}

	protected static String globSyntaxAndPattern(String pattern, Path path) {
		String syntaxAndPattern = null;
		if (path != null && pattern != null && !pattern.isEmpty()) {
			String syntax = "glob:";
			if (recursiveInputFiles || path.getNameCount() > 1) {
				if (!pattern.contains("*")) {
					syntax += "**";
				} else if (!pattern.contains("**")) {
					syntax += "*";
				}
			} else {
				if (!pattern.contains("*") && (pattern.startsWith(".") || pattern.startsWith("{"))) {
					syntax += "*";
				}
			}
			syntaxAndPattern = syntax + pattern;
		}
		return syntaxAndPattern;
	}

	protected static String parseInputPattern(String input) {
		String pattern = null;
		if (input != null) {
			input = input.replaceAll("'", "");
			input = input.replaceAll("\"", "");
			input = input.trim();			
			input = input.replaceAll(" ", ",");
			input = input.replaceAll(",,", ",");
			input = input.replaceAll(",,", ",");
			if (!input.isEmpty()) {
				if (input.contains(",") && !input.startsWith("{") && !input.endsWith("}")) {
					// PathMatcher doesn't like spaces between subpatterns when using {}
					input = input.replaceAll(", *", ",");
					pattern = "{" + input + "}";
				} else {
					pattern = input;
				}
			}
		}
		return pattern;
	}

	protected static void normalizeIncludeExcludePatterns() {
		LinkedHashSet<String> excludes = new LinkedHashSet<>(defaultExcludes);

		if (includePattern != null || excludePattern != null) {
			String tmpIncludePattern = includePattern;
			String tmpExcludePattern = excludePattern;
			
			if (tmpExcludePattern != null) {
				if (tmpExcludePattern.startsWith("{")) {
					tmpExcludePattern = tmpExcludePattern.substring(1);
				}
				if (tmpExcludePattern.endsWith("}")) {
					tmpExcludePattern = tmpExcludePattern.substring(0, (tmpExcludePattern.length() - 1));
				}
				// Rare case of a glob character being escaped on the command line
				if (tmpExcludePattern.startsWith("*")) {
					tmpExcludePattern = tmpExcludePattern.substring(1);
				}
				excludes.addAll(new LinkedHashSet<>(Arrays.asList(tmpExcludePattern.split(","))));
			}

			if (tmpIncludePattern != null) {
				if (tmpIncludePattern.startsWith("{")) {
					tmpIncludePattern = tmpIncludePattern.substring(1);
				}
				if (tmpIncludePattern.endsWith("}")) {
					tmpIncludePattern = tmpIncludePattern.substring(0, (tmpIncludePattern.length() - 1));
				}
				// Rare case of a glob character being escaped on the command line
				if (tmpIncludePattern.startsWith("*")) {
					tmpIncludePattern = tmpIncludePattern.substring(1);
				}
				String[] includes = tmpIncludePattern.split(",");
				for (String include : includes) {
					for (Iterator<String> iter = excludes.iterator(); iter.hasNext(); ) {
						String exclude = iter.next();
						if (include.equals(exclude) || ("." + include).equals(exclude) || include.equals(("." + exclude))) {
							u.appOutput("Warning: -include pattern overrides -exclude pattern " + exclude);
							iter.remove();
						}
					}
				}
			}
		}

		// if an XML format for a captured-query file is specified, allow .xml file types			
		if (!u.importFormat.isEmpty()) {
			if (u.importFormat.toUpperCase().contains("XML")) {
				excludes.remove(".xml");
			}
			if (u.importFormat.equalsIgnoreCase(u.extendedEventsXMLFmt)) {
				excludes.remove(".xel");
			}
		}

		excludePattern = parseInputPattern(String.join(",", excludes));
	}

	protected static boolean inputFilesValid() {
		// Do we have any input files to process?
		// Note that addInputFile already filters out input file paths that don't exist or can't
		// be read or don't match our include and exclude file patterns
		if (inputFiles.size() == 0) {
			if (addReport) {
				u.appOutput("With -add, must specify input file(s)");
				return false;
			}
		}
		return true;		
	}
	
	private static boolean optionsValid() throws Exception {
		// validate combinations of various options specified
		// return true if options are valid
		
 		if (reportName == null) {
 			reportName = "";
 		}
 		if (reportName.isEmpty()) {
 			if (!readStdin) {
				u.appOutput("Report name must be specified");
				return false;
			}
		}
		
 		if (u.anonymizedData) {
 			if (readStdin || listContents) {
				u.appOutput("-anon cannot be combined with other options");
				return false;
			}
 			if (reportOption) {
				u.appOutput("-anon cannot be combined with -reportoption");
				return false;
			}
 			if ((inputFiles.size() == 0) && !pgImport && !reportOnly && !reAnalyze) {
				u.appOutput("-anon can only be specified when analyzing input files");
				return false;
			}
			return true;
		}								
				
 		if (pgImport) {
 			if (readStdin || listContents || importOnly || reAnalyze || deleteReport || reportOnly || importFormatArg) {
				u.appOutput("-pgimport cannot be combined with other options");
				return false;
			}
 			if ((inputFiles.size() > 0) || autoDDL) {
				u.appOutput("-pgimport cannot be combined with input files");
				return false;
			}
			return true;
		}		
		
 		if (pgImportAppend) {
 			if (!pgImport) {
				u.appOutput("-pgimportappend requires -pgimport");
				return false; 				
 			}
			return true;
		}	
		
		
 		if (u.pgImportNoDoubleQuotes) {
 			if (!pgImport) {
				u.appOutput("-pgimportnodoublequotes requires -pgimport");
				return false; 				
 			}
			return true;
		}					
		
 		if (pgImportTable) {
 			if (!pgImport) {
				u.appOutput("-pgimporttable requires -pgimport");
				return false; 				
 			}
			return true;
		}	
		
		if (forceReportName) {
			if (parseOnly ||  (!generateReport) || importOnly) {
				u.appOutput("Can specify -reportfile only when a report is generated");
				return false;				
			}
		}
		
		if (listContents && readStdin) {
			u.appOutput("Cannot combine -list and -stdin");
			return false;
		}
		
		if (importOnly && readStdin) {
			u.appOutput("Cannot combine -importonly and -stdin");
			return false;
		}
		
		if (importOnly && reAnalyze) {
			u.appOutput("Cannot combine -importonly and -analyze");
			return false;
		}
		
		if (importOnly && reportOnly) {
			u.appOutput("Cannot combine -importonly and -reportonly");
			return false;
		}
		
		if (importOnly && listContents) {
			u.appOutput("Cannot combine -list and -importonly");
			return false;
		}
		
		if (importOnly && importFormatArg) {
			u.appOutput("Cannot combine -importonly and -importfmt");
			return false;
		}
		
		if (!u.deDupExtracted && !importFormatArg) {
			u.appOutput("Cannot use -nodedup without -importfmt");
			return false;
		}
				
		if (reAnalyze && importFormatArg) {
			u.appOutput("Cannot combine -analyze and -importfmt");
			return false;
		}
		
		if (importFormatArg && readStdin) {
			u.appOutput("Cannot combine -importfmt and -stdin");
			return false;
		}
		
		if (reAnalyze && readStdin) {
			u.appOutput("Cannot combine -analyze and -stdin");
			return false;
		}
		
		if (reAnalyze && parseOnly) {
			u.appOutput("Cannot combine -analyze and -parseonly");
			return false;
		}
		
		if (reAnalyze && forceAppName) {
			u.appOutput("Cannot combine -analyze and -appname");
			return false;
		}
		
		if (reAnalyze && (!generateReport)) {
			// actually, no reason to disallow this: running report generation separately can be significantly faster for very large cases
			//u.appOutput("Cannot combine -analyze and -noreport");
			//return false;
		}
		
		if (reportOnly && forceAppName) {
			u.appOutput("Cannot combine -reportonly and -appname");
			return false;
		}
		
		if ((inputFiles.size() > 0) && readStdin) {
			u.appOutput("Cannot combine -stdin and input files");
			return false;
		}
		
		if ((inputFiles.size() > 0) && reAnalyze) {
			u.appOutput("Cannot combine -analyze and input files");
			return false;
		}
		
		if ((inputFiles.size() > 0) && listContents) {
			u.appOutput("Cannot combine -list and input files");
			return false;
		}
		
		if ((inputFiles.size() > 0) && reportOnly) {
			u.appOutput("Cannot combine -reportonly and input files");
			return false;
		}
		
		if (deleteReport && reAnalyze) {
			u.appOutput("Cannot combine -delete and -analyze");
			return false;
		}		
		
		if (deleteReport && readStdin) {
			u.appOutput("Cannot combine -delete and -stdin");
			return false;
		}		
		
		if (reportOnly && readStdin) {
			u.appOutput("Cannot combine -reportonly and -stdin");
			return false;
		}		
		
		if (reportOnly && reAnalyze) {
			u.appOutput("Cannot combine -reportonly and -analyze");
			return false;
		}		
		
		if (deleteReport && listContents) {
			u.appOutput("Cannot combine -delete and -list");
			return false;
		}
		
		if (deleteReport && reportOnly) {
			u.appOutput("Cannot combine -delete and -reportonly");
			return false;
		}
		
		if (listContents && (addReport || replaceFiles)) {
			u.appOutput("Cannot combine -list and -add/-replace");
			return false;
		}
		
		if (deleteReport && (addReport)) {
			u.appOutput("Ignoring -add since -delete is specified");
			addReport = false;
		}
		
		if (deleteReport && (replaceFiles)) {
			u.appOutput("Ignoring -replace since -delete is specified");
			replaceFiles = false;
		}
		
		if (reportOnly && (addReport||replaceFiles)) {
			u.appOutput("Cannot combine -reportonly and -add/-replace");
			return false;
		}			
				
		if (inputFiles.size()==0) {
//			if (deleteReport) {
//				u.appOutput("With -delete, must specify input file(s)");
//				return false;
//			}
			if (addReport) {
				u.appOutput("With -add, must specify input file(s)");
				return false;
			}
		}	

 		if ((inputFiles.size()==0) && (!readStdin) && (!generateReport)) {
 			if (!reAnalyze) {
	 			u.appOutput("No input files specified. Try -help");
				if (!reportName.isEmpty()) {
	 				u.appOutput("(NB: first argument is the report name, not the input file)");
	 			}
				return false;
			}
		}
		
		// when only specifying the report name, must at least specify -list or -analyze or -reportonly/-reportoption
		if (!reportName.isEmpty()) {
			if ((inputFiles.size()==0) && (!readStdin) && (!deleteReport) && (!autoDDL)) {
				if (!(listContents || (reportOnly || reportOption) || reAnalyze || autoDDL)) {
	 				u.appOutput("Must specify input file(s), or -list/-analyze/-reportonly/-reportoption");
	 				return false;
				}
			}
		}
		
		if ((inputFiles.size() > 0) && (reportOnly)) {
			u.appOutput("Cannot combine -reportonly and input files");
			return false;
		}
		
		if ((!userCfgFile.isEmpty()) && (optimisticFlag)) {
			String flag = "-userconfigfile";  // show flag that was actually used
			if (!userCfgFile.equals(flag)) flag += "/"+userCfgFile;
			u.appOutput("Cannot combine "+flag+" and -optimistic");
			return false;	
		}				
		
		// validate reportoptions
		if (!CompassUtilities.reportOptionStatus.isEmpty() || !CompassUtilities.reportOptionDetail.isEmpty() || !CompassUtilities.reportOptionFilter.isEmpty() || CompassUtilities.reportOptionNotabs || CompassUtilities.reportOptionLineNrs) {
			if(CompassUtilities.reportOptionXref.isEmpty()) {
				u.appOutput("Must also specify report option 'xref' when specifying option 'status', 'detail', 'filter', 'linenrs' or 'notabs' ");
				return false;
			}
		}
		
		if ((!reportName.isEmpty()) && (readStdin)) {
			// print message
			u.appOutput("Ignoring report name with -stdin");
		}
		
		// user-defined itemID file
		if (u.customItemIDPathNameUser) {
			if (!u.generateCSVFormat.equals(u.CSVFormatFlat)) {
				u.appOutput("-csvitemidfile can only be specified with '-csvformat flat'");
				return false;					
			}
		}
				
		// some options can be used only when when doing actual analysis
		if ((((inputFiles.size() > 0) || autoDDL) && (!(parseOnly || importOnly))) || reAnalyze) {
			// ok
		}
		else {
			String noInputFilesMsg = "";
			if ((inputFiles.size() == 0) && (!autoDDL) && (!reAnalyze)) {
				noInputFilesMsg = "\n(no valid input files specified)"; // add helpful msg
			}
			if (userSpecifiedBabelfishVersion) {
				u.appOutput("Cannot specify -babelfish-version when not performing analysis"+noInputFilesMsg);
				return false;					
			}			
			if (u.rewrite) {
				if (optimisticFlag) {
					// -rewrite is implied by -optimistic , which can also be used with non-analysis cases so no need to warn 
				}
				else {
					// -rewrite only applies to analysis, but it's harmless when specified otherwise
					u.appOutput("Ignoring -rewrite when not performing analysis"+noInputFilesMsg);
				}			
			}			
		}
		
		// cannot specify input files when generating DDL script
		if (autoDDL) {		
			if (u.checkReportExists(reportName)) {
				if (!deleteReport) {
					u.appOutput("Report already exist. Must specify -delete when generating DDL");
					return false;					
				}
			}			
					
			if (inputFiles.size() > 0) {
				u.appOutput("Cannot specify input files when generating DDL");
				return false;					
			}
			
			// various other options wouldn't be valid either
			if (recursiveInputFiles) { 
				u.appOutput("Cannot specify -recusrsive when generating DDL");
				return false;					
			}		
			if (addReport) { 
				u.appOutput("Cannot specify -add when generating DDL");
				return false;					
			}		
			if (replaceFiles) { 
				u.appOutput("Cannot specify -replace when generating DDL");
				return false;					
			}						
			if (listContents) { 
				u.appOutput("Cannot specify -list when generating DDL");
				return false;					
			}				
			if (importFormatArg) { 
				u.appOutput("Cannot specify -importformat when generating DDL");
				return false;					
			}				
			if (reAnalyze) { 
				u.appOutput("Cannot specify -analyze when generating DDL");
				return false;					
			}			
			if (readStdin) {
				u.appOutput("Cannot specify -stdin when generating DDL");
				return false;					
			}			
			
			// validate endpoint inputs
			if (sqlEndpoint.equals(u.uninitialized) || sqlLogin.equals(u.uninitialized) || sqlPasswd.equals(u.uninitialized)) {	
				u.appOutput("Must specify endpoint, login and password when generating DDL");
				return false;					
			}			
		}
		
		if (!u.userConfig) {  
			if (!u.userCfgFileName.equals(u.uninitialized)) {
				u.appOutput("Ignoring -userconfigfile since -nooverride was specified");					
			}
		}

		if (!sqlDBList.equals(u.uninitialized)) {
			if (!autoDDL) {
				u.appOutput("Cannot specify -sqldblist when not generating DDL");
				return false;					
			}
		}
				
		// if we get here, we're good
		
		// ensure report-only case is reflected in the flag
		if ((inputFiles.size() == 0) && (!reportOnly)) { 
			if ((!reAnalyze) && (!autoDDL) && (!readStdin)) {
				reportOnly = true;
			}
		}
		
		return true;
	}
	
	private static void runPGImport () throws Exception { 
		// import the captured.dat file into a PG table					
		u.importPG(pgImportAppend, pgImportFlags);	
		
		//Note: by exiting here, the password entered on command line is not getting written into the log files		
		// If this is ever changed, the password should be blanked out in the command-line argument before written to the log file
		u.errorExit();
	}

	private static void anonymizeCapturedData () { 
		// generated anon report file 		
		try { u.anonymizeCapturedData(); } catch (Exception e) { 
			//System.out.println(e.toString());
			e.printStackTrace();
		}		
	}

	private void reportFinalStats() throws Exception {
		if (readStdin) {
			return;
		}
		
		int nrFiles = (inputFiles.size());

		if (nrFiles > 1) {
			u.appOutput("");
			u.appOutput(u.composeOutputLine("--- Report '"+reportName+"' Contents ", "-"));
			u.listReportFiles(reportName);
		}

		String errFiles = "";
		if (nrFileNotFound > 0) {
			errFiles = " (+"+nrFileNotFound+" specified, but not found)";
		}

		String parseErrorMsg = "";
		if (totalParseErrors > 0) {
			parseErrorMsg = "  (see "+u.getReportDirPathname(reportName, u.errBatchDirName)+File.separator+"*."+u.errBatchFileSuffix+")";
		}
		
		
		// convert durations to seconds
		for (String k : timeCount.keySet()) {
			if (k.contains("Batch")) continue;
			timeCount.put(k, timeCount.get(k)/1000);
		}
				
		int linesSQL=0;
		if (CompassUtilities.linesSQLInReport > 0) linesSQL = CompassUtilities.linesSQLInReport;
		else if (nrLinesTotalP1 > 0) linesSQL = nrLinesTotalP1;
		else if (nrLinesTotalP2 > 0) linesSQL = nrLinesTotalP2;
		
		String linesSQLPerSecFmt = "";
		Long linesSQLPerSec = (timeCount.get("elapsedRun") > 0) ? (linesSQL / timeCount.get("elapsedRun")) : linesSQL;	
		if (linesSQLPerSec > 0) linesSQLPerSecFmt = "  ("+linesSQLPerSec.toString()+" lines/sec)";

		boolean writeToReport = true;
		if (parseOnly || importOnly) writeToReport = false;
		
		u.appOutput("");
		u.appOutput(u.composeOutputLine("--- Run Metrics ", "-"), writeToReport);
		u.appOutput("Run start            : "+ startRunFmt, writeToReport);
		u.appOutput("Run end              : "+ endRunFmt, writeToReport);
		u.appOutput("Run time             : "+ timeCount.get("elapsedRun") + " seconds", writeToReport);
		u.appOutput("#Lines of SQL        : "+ linesSQL + linesSQLPerSecFmt, writeToReport);
		
		if ((totalParseErrors > 0) || CompassUtilities.devOptions) {
			u.appOutput("#syntax errors       : "+ totalParseErrors + parseErrorMsg, writeToReport);
		}
			
		if (CompassUtilities.devOptions) {
			String parseMax = "";
			if (timeCount.get("parseTimeMax") > 0) {
				String batchFile = "";  
				if (inputFiles.size() > 1) batchFile = "/"+timeCountStr.get("parseTimeMaxBatchFile");
				parseMax += " -- batch max time: " + timeCount.get("parseTimeMax") + " sec(in pass "+ timeCount.get("parseTimeMaxBatchPass") + "), " + timeCount.get("parseTimeMaxBatchLines") + " lines: batch " + timeCount.get("parseTimeMaxBatchNr") + " at line " + timeCount.get("parseTimeMaxBatchLine") + batchFile;
			}
			String p1Max = "";
			if (timeCount.get("analysisTimeP1Max") > 0) {
				String batchFile = "";  
				if (inputFiles.size() > 1) batchFile = "/"+timeCountStr.get("analysisTimeP1MaxBatchFile");
				p1Max += " -- batch max time: " + timeCount.get("analysisTimeP1Max") + " sec(in pass "+ timeCount.get("analysisTimeP1MaxBatchPass") + "), " + timeCount.get("analysisTimeP1MaxBatchLines") + " lines: batch " + timeCount.get("analysisTimeP1MaxBatchNr") + " at line " + timeCount.get("analysisTimeP1MaxBatchLine") + batchFile;
			}
			String p2Max = "";			
			if (timeCount.get("analysisTimeP2Max") > 0) {
				String batchFile = "";  
				if (inputFiles.size() > 1) batchFile = "/"+timeCountStr.get("analysisTimeP2MaxBatchFile");
				p2Max += " -- batch max time: " + timeCount.get("analysisTimeP2Max") + " sec(in pass "+ timeCount.get("analysisTimeP2MaxBatchPass") + "), " + timeCount.get("analysisTimeP2MaxBatchLines") + " lines: batch " + timeCount.get("analysisTimeP2MaxBatchNr") + " at line " + timeCount.get("analysisTimeP2MaxBatchLine") + batchFile;
			}								
			
			u.appOutput("#input files         : "+ nrFiles+errFiles);
			u.appOutput("#batches             : "+ totalBatches);
			u.appOutput("#dynamic SQL         : "+ u.dynamicSQLNrStmts);
			u.appOutput("#lines of SQL pass 1 : "+ nrLinesTotalP1);
			u.appOutput("#lines of SQL pass 2 : "+ nrLinesTotalP2);
			u.appOutput("#SQL features        : "+ u.constructsFound, writeToReport);
			u.appOutput("Parse time           : "+ timeCount.get("parseTime") + " seconds"+parseMax);
			u.appOutput("Analysis time pass 1 : "+ timeCount.get("analysisTimeP1") + " seconds"+p1Max);
			u.appOutput("Analysis time pass 2 : "+ timeCount.get("analysisTimeP2") + " seconds"+p2Max);
			u.appOutput("Report gen time      : "+ timeCount.get("report") + " seconds");

			Integer retryPct = 0;
			String SLL_fmt = "-noSLL";
			if (antlrSLL) {
				if (passCount.size() == 2) retrySLL = retrySLL/2;
				if (totalBatches > 0) { retryPct = (retrySLL*100/totalBatches); }
				SLL_fmt = retrySLL.toString() + "/"+totalBatches.toString()+ " ("+retryPct.toString()+"%)";
			}
			
			u.appOutput("#SLL retries         : "+ SLL_fmt);
			if (u.showPercentage) {
				u.appOutput("Compatibility        : "+ u.compatPctStr + "%   (uncorrected: "+u.compatPctStrRaw+"%)" );
			}
		}
		
		String rewriteSecs = "";
		if (u.nrRewritesDone > 0) rewriteSecs = " in " + timeCount.get("secsRewrite") + " seconds";
		if (u.rewrite) u.appOutput("#SQL rewrites        : "+ u.nrRewritesDone + rewriteSecs , writeToReport);
		else           u.appOutput("#SQL rewrite oppties : "+ u.rewriteOppties.getOrDefault(u.rewriteOpptiesTotal,0), writeToReport);
		if (u.execTest)u.appOutput("ExecTest statements  : "+ u.execTestStatements, writeToReport);
		
		if (u.queriesExtractedAll > 0) {
			           u.appOutput("Batches extracted    : "+ u.queriesExtractedAll, writeToReport);
			           u.appOutput("Duplicates removed   : "+ u.deDupSkippedAll, writeToReport);
		}
		u.appOutput("Session log          : "+ sessionLog, writeToReport);
		if (!u.reportFilePathName.equals(u.uninitialized)) {
			u.appOutput("Assessment report    : "+ u.reportFilePathName, writeToReport);
		}
		u.appOutput(u.composeOutputLine("","="), writeToReport);
		
		if (CompassUtilities.devOptions) {
			u.appOutput(CompassUtilities.thisProc()+"caching  =["+CompassUtilities.caching+"] ");
			u.appOutput(CompassUtilities.thisProc()+"stripDelimitedIdentifierCall  =["+u.stripDelimitedIdentifierCall+"] ");
			u.appOutput(CompassUtilities.thisProc()+"stripDelimitedIdentifierCached=["+u.stripDelimitedIdentifierCached+"] ");
			u.appOutput(CompassUtilities.thisProc()+"normalizeNameCall  =["+u.normalizeNameCall+"] ");
			u.appOutput(CompassUtilities.thisProc()+"normalizeNameCached=["+u.normalizeNameCached+"] ");
		}
		
		if (!u.newVersionAvailable.isEmpty()) u.appOutput("\nNote: "+u.removeHTMLTags(u.newVersionAvailable));

	}

	private void processInput(String runStartTime) throws Exception {		
		if (readStdin) {
			// quick parse option, for development only
			if (u.analysisPass > 1) return;

			String EOT = "CTRL-D";
			if (CompassUtilities.onWindows) {
				EOT = "CTRL-Z";
			}

			u.appOutput("Setting QUOTED_IDENTIFIER=" + quotedIdentifier);
			u.appOutput("Enter SQL to be parsed, and close with " + EOT + " + <ENTER> (on separate line):");

			charset = Charset.defaultCharset();
			CharStream charStream = CharStreams.fromStream(System.in, charset);
			String ptree = parseBatch(charStream, "", 0, 0, false);
			u.appOutput("parse tree:");
			u.appOutput(ptree);
			return;
		}

		// in first pass, process input files and create symbol table (a very basic version)
		// in second pass, process copies of input files made in the first pass, and perform analysis
		// with -analyze, run both passes on the copies of the originally imported input files
		if (u.debugging) u.dbgOutput("pass=" + u.analysisPass + ".  reAnalyze=[" + reAnalyze + "] ", u.debugBatch);

		if (parseOnly) {
			if (u.analysisPass > 1) {
				return;
			}
		}		

		if (reAnalyze) {
			// reprocess from the start, based on the input copy files, so no need to re-import source files
			// do this when:
			//  - with a new version of the Babelfish Compass tool, with changes to the analysis logic or grammar/parser
			//  - re-running the analysis for a different Babelfish version than originally

			// set up input files for processing
			List<Path> importFiles = u.getImportFiles(reportName);

			//copy imported filenames into inputFiles
			inputFiles.clear();  // should be redundant
			for (Path imf : importFiles) {
				addInputFile(imf.toString());
			}
		} 
		
		// skip file names starting with a '.'
		List<String> inputFilesTmp = new ArrayList<>(inputFiles);
		for (String inFile : inputFilesTmp) {
			if (!CompassUtilities.getPatternGroup(inFile, u.escapeRegexChars(File.separator) + "(\\.\\w)", 1).isEmpty()) {
				u.appOutput("Excluding file '"+inFile+"'");
				inputFiles.remove(inFile);
			}
		}
		if (inputFiles.size() == 0) {
			u.appOutput("No input files specified");
			u.errorExit();
		}				

		// remove duplicate input files
		// sort files in app+pathname order so that we always process them in the same order
		List<String> tmpInputFiles = new ArrayList<>();
		List<String> tmpInputFilesUpperCase = new ArrayList<>();
		List<String> sortInputFiles = new ArrayList<>();
		String sortKeySeparator = "~!~@~!~";
		tmpInputFiles.addAll(inputFiles);
		tmpInputFilesUpperCase.addAll(inputFiles);
		u.listToUpperCase(tmpInputFilesUpperCase);
		
		inputFiles.clear();
		for (int i = 0; i < tmpInputFiles.size(); i++) {
			String f = tmpInputFiles.get(i);
			if (f.isEmpty()) continue;
			//if ((u.analysisPass == 1) && (!reAnalyze)) {
			if (!reAnalyze) {
				inputFiles.add(f);
			} 
			else {
				// already-imported files: need to sort on app name + original src file path
				String app = u.getAppNameFromImported(f);					
				String origSrcFile = u.importFileAttribute(u.importFileFirstLine(f), 1);	
				sortInputFiles.add(app.toUpperCase() + sortKeySeparator + origSrcFile + sortKeySeparator + f);
			}

			if ((u.analysisPass == 1) && (!reAnalyze)) {
				// remove duplicate input files during initial import only
				// using tmpInputFilesUpperCase to speed up things in case of large numbers (1000's) of files
				tmpInputFilesUpperCase.set(i,"");
				if (tmpInputFilesUpperCase.contains(f.toUpperCase())) {
					//u.appOutput(u.thisProc()+"searching to remove duplicate input file f=["+f+"] ");
					for (int j = i+1; j < tmpInputFiles.size(); j++) {
						if (f.equalsIgnoreCase(tmpInputFiles.get(j))) {
							u.appOutput("Removing duplicate input file '"+f+"'");
							tmpInputFiles.set(j,"");
							tmpInputFilesUpperCase.set(j,"");
						}
					}
				}
				
				//intercept .xel files
				if (u.importFormat.equalsIgnoreCase(u.extendedEventsXMLFmt)) {
					String suffix = f.substring(f.lastIndexOf(".")+1);
					if (suffix.equalsIgnoreCase("XEL")) {
						u.appOutput("For Extended Events files, .xel files cannot be processed; instead, extract the XML into .xml files");
						u.errorExit();
					}
				}
				
			}
		}			
		
		// sort the input files on their original pathnames so as to process files for all apps together 
		// (performance-relevant when combining apps recursively read from directory trees)
		//if ((u.analysisPass == 1) && (!reAnalyze)) {
		if (!reAnalyze) {
			// first-time import
			inputFiles = inputFiles.stream().sorted().collect(Collectors.toList());
		}
		else {
			List<String> tmp = sortInputFiles.stream().sorted().collect(Collectors.toList());
			inputFiles.clear();
			for (String k : tmp) {
				String f = k.substring(k.lastIndexOf(sortKeySeparator)+sortKeySeparator.length());
				inputFiles.add(f);
			}
		}

		// process the input files
		int nrFiles = inputFiles.size();
		int fileCount = 0;
		
		fileCount = 0;
		for (String inFile : inputFiles) {			
			fileCount++;
			String appName = "";
			String origSrcFile = "";
			String inFileCopy = "";
			FileInputStream fis = null;
			InputStreamReader isr = null;
			u.dynamicSQLBuffer.clear();
			a.sqlcmdVars.clear();
			retrySLLFile = 0;
			if (u.rewrite) u.resetRewrites();
			u.currentDatabase  = "";
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "u.analysisPass=["+u.analysisPass+"] inFile=["+inFile+"] ", u.debugDir);
			
			if (!reAnalyze) {
				// process the input files when importing. i.e. the very first time
				if (!Files.exists(Paths.get(inFile))) {
					continue;
				}
						
				// regular case
				appName = forceAppName ? applicationName : u.getFileNameFromPathName(inFile);
				if (autoDDL) {
					appName = appName.replaceFirst(SMODDLTag, "");
				}				
				appName = u.fixNameChars("appname", appName);		
				
				if (recursiveInputFiles && (!forceAppName)) {
					// for recursive cases, without -appname, try to guess the appname to avoid ending up with as many appnames as inputfiles
					String inFileTest = inFile.replaceAll("\\\\", "/"); // doesn't hurt on non-Windows			
					for (String origFile : inputFilesOrig) {
						origFile = origFile.replaceAll("\\\\", "/");  // doesn't hurt on non-Windows			
						if (inFileTest.toUpperCase().startsWith(origFile.toUpperCase() + "/")) {							
							appName = u.fixNameChars("appname", u.getFileNameFromPathName(origFile));		
							break;
						}
					}
				}
			
				if (appName.isEmpty()) {
					u.appOutput("Application name '" + appName + "' is blank for '"+inFile+"' . Use -appname");
					u.errorExit();
				}
				String invalidMsg = CompassUtilities.nameFormatValid("appname", appName);
				if (!invalidMsg.isEmpty()) {
					// if we get here, then fixNameChars() is not right
					u.appOutput("Application name '" + appName + "' still contains invalid character after removing known characters " + invalidMsg);
					u.errorExit();
				}

				inFileCopy = u.getImportFilePathName(reportName, inFile, appName);
			} 
			else {
				// re-analyze: process the already-imported files				
				inFileCopy = inFile;
				String line = u.importFileFirstLine(inFile);   // read only first line to pick up file attributes
				u.currentSrcFile = u.importFileAttribute(line, 1);
				u.currentAppName = u.importFileAttribute(line, 2);
				appName = u.currentAppName;
			}
						
			if (inputFilesMapped.containsKey(inFileCopy)) {
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "using mapped inFileCopy=["+inputFilesMapped.get(inFileCopy)+"] instead of ["+inFileCopy+"]" , u.debugFmt || u.debugDir);
				inFileCopy = inputFilesMapped.get(inFileCopy);
			}

			if (u.analysisPass == 1) {
				boolean replacing = false;
				if (!reAnalyze) {
					// process the input files when importing. i.e. the very first time
					if (Files.exists(Paths.get(inFileCopy))) {
						replacing = true;
					}
					u.appOutput(u.progressCnt(fileCount, nrFiles) + "Importing " + Paths.get(inFile).toAbsolutePath() + ", for application '" + appName + "'");
					if (replacing) {
						if (replaceFiles) {
							u.appOutput("Replacing input file " + Paths.get(inFile).toAbsolutePath());
						}
					}
				} 
				else {
					// process the already-imported files
					u.appOutput(u.progressCnt(fileCount, nrFiles) + "Re-processing " + u.currentSrcFile + ", for application '" + appName + "'");
				}

				if (dumpBatchFile) {
					String f = u.openBatchFile(reportName, inFile);
					u.appOutput("Logging SQL batches + parse trees to " + f);
				} 
				else {
					u.deleteBatchFile(reportName, inFile);
				}

				// handle charset conversion
				// NB: when specifying multiple input files, all input files will be handled with the same encoding
				if (!reAnalyze) {
					if (userEncoding == null) {
						// if no encoding specified, try to detect cases of UTF16
						String detectedEncoding = u.detectEncoding(inFile);
						if (detectedEncoding != null) {
							charset = Charset.forName(detectedEncoding);
							u.appOutput(CompassUtilities.stringRepeat(" ", u.progressCnt(fileCount, nrFiles).length()) + "Detected encoding '" + detectedEncoding + "' for input file " + inFile);
						} 
						else {
							charset = Charset.defaultCharset();
						}
					} 
					else {
						try {
							charset = Charset.forName(userEncoding);
						} catch (Exception e) {
							u.appOutput("Invalid -encoding value specified: [" + userEncoding + "]\nUse '-encoding help' to list available encodings.");
							return;
						}
					}
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "Using encoding=[" + charset.toString() + "]", u.debugBatch);

					String detectedFmt = u.detectImportFileFormat(inFile, u.importFormat, charset);					
					String useImportFormat = u.sqlcmdFmt;
					if (u.importFormat.equalsIgnoreCase(u.autoFmt)) {
						useImportFormat = detectedFmt;
					}
					if (u.importFormat.equalsIgnoreCase(detectedFmt)) {
						useImportFormat = detectedFmt;
					}
					if (useImportFormat.isEmpty()) useImportFormat = u.sqlcmdFmt; // catchall
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "u.importFormat=["+u.importFormat+"] detected fmt=["+detectedFmt+"] useImportFormat=["+useImportFormat+"] ", u.debugFmt || u.debugDir);
					
    				String useCharset = charset.toString();
					if (useImportFormat.equalsIgnoreCase(u.sqlcmdFmt)) {	
						// continue, no conversion needed
					}
					else {			
						// need to convert input format first
						String inFileConverted = u.convertInputFileFormat(reportName, inFile, appName, useImportFormat, charset);
						inFile = inFileConverted;
						useCharset = "UTF-8";
						charset = StandardCharsets.UTF_8;
						String inFileCopyCopy = inFileCopy;
						inFileCopy = u.getImportFilePathName(reportName, inFileConverted, appName);
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "mapping inFileCopy from ["+inFileCopyCopy+"] to ["+inFileCopy+"]  inFileConverted=["+inFileConverted+"] ", u.debugFmt || u.debugDir);
						inputFilesMapped.put(inFileCopyCopy, inFileCopy);
					}
					
					u.openImportFile(reportName, inFile, appName, useCharset);  // open to write a copy of the input file

					fis = new FileInputStream(inFile);
					isr = new InputStreamReader(fis, charset);
					if (u.debugging) u.dbgOutput("reading inFile=["+inFile+"] ", u.debugDir);
				}
			}

			if ((u.analysisPass == 2) || (reAnalyze && (u.analysisPass == 1))) {
				String line = u.importFileFirstLine(inFileCopy);   // read only first line to pick up file attributes
				u.currentSrcFile = u.importFileAttribute(line, 1);
				u.currentAppName = u.importFileAttribute(line, 2);
				String batchesLines = u.importFileAttribute(line, 4);
				if ((reAnalyze && (u.analysisPass == 1))) {
					// don't print					
				}
				else {
					String w = "Analyzing";
					if (reAnalyze) w = "Re-analyzing";
					u.appOutput(u.progressCnt(fileCount, nrFiles) + w+" " + u.currentSrcFile + ", for application '" + u.currentAppName + "'; #batches/lines: " + batchesLines);
				}
			
				fis = new FileInputStream(inFileCopy);
				isr = new InputStreamReader(fis, StandardCharsets.UTF_8);				
				if (u.debugging) u.dbgOutput("reading inFileCopy=["+inFileCopy+"] ", u.debugDir);
			}
			
			BufferedReader inFileReader = new BufferedReader(isr);
			
			if (u.analysisPass== 2) {
				u.openCaptureFile(reportName, u.currentSrcFile, u.currentAppName);
				if (!u.symTabAll) {
					// read only the symtab for this app, it must exist
					try { u.readSymTab(reportName, appName); }
					catch (Exception e) {
						u.appOutput("Error reading symbol table " + u.symTabFilePathName);
						throw e;
					}
				}				
			}
			
			CodePointCharStream charStream;

			// set QUOTED_IDENTIFIER to the default value at the start of the input file
			a.setQuotedIdentifier(quotedIdentifier);			

			// Process input file line by line, identifying batches to be parsed
			// This follows the 'sqlcmd' utility which uses 'go' and 'reset' as batch terminators
			// Other sqlcmd commands/directives are not handled except 'exit'/'quit'
			// Note that we are not trying to parse the entire input file, which would require the
			// sqlcmd commands to be part of the grammar. Not only would this complicate the grammar,
			// slow down parsing and complicate overall processing, but more importantly it would lead 
			// to the entire file being rejected if there is a single syntax error or bad character 
			// somewhere, instead of just rejecting the offending batch.
			// The price to pay is having to work through each input file to properly process these 
			// sqlcmd commands.

			StringBuilder batchText = new StringBuilder();
			int batchLines = 0;
			int lineNr = 0;
			int nrLinesInFile = 0;
			String line =  null;
			boolean endBatchFound = false;
			boolean exitFound = false;
			boolean startOfNewBatch = true;
			int startBatchLineNr = 1;
			int inComment = 0;
			boolean inString = false;
			String openQuote = "";
			boolean endOfFile = false;
			boolean pass2Init = false;

			// set to true to ignore leading blank lines in a batch; 
			// this has the downside of line numbers being off by as many lines as were ignored
			// also, it may mess up the -rewrite functionality, so keep this set to FALSE!
			boolean skipLeadingBlankLines = false; 
			
			// keeps track if leading lines are all blank
			boolean leadingBlankLines = true; 

			long timeElapsed = 0;
			long timeElapsedFile = 0;

			int batchNr = 0;
			int nrParseErrors = 0;

			boolean doEncodingChecks = true;
			int nrEncodingWarnings = 0;
			int maxEncodingWarnings = 5;	
			boolean lastLineRead = false;	
			int dynSQLCount = 0;
			analyzingDynamicSQL = false;
			dynamicSQLLineNr = 0;
			dynamicSQLBatchNr = 0;
			dynamicSQLBatchLineNr = 0;
			dynamicSQLContext = "";
			dynamicSQLSubContext = "";
			u.dynamicSQLFlag = false;
			
			int lastLineWithoutTerminator = -1;
			if (u.analysisPass == 2) {
				if (inputFilesLastLine.containsKey(inFile)) {
					lastLineWithoutTerminator = inputFilesLastLine.get(inFile);
					if (u.debugging) u.dbgOutput(u.thisProc()+"getting lastLineWithoutTerminator for inFile=["+inFile+"]=["+lastLineWithoutTerminator+"] ", u.debugBatch||u.debugDynamicSQL);
				}
			}			

			while (true) {
				boolean somethingFoundOnLine = false;
				boolean orphanSquareBracket = false;
				if (!lastLineRead) {
					if ((u.analysisPass == 2) && (lastLineWithoutTerminator == lineNr)) {
						if (u.debugging) u.dbgOutput("inserting lastLineWithoutTerminator, lineNr=["+lineNr+"]",  u.debugBatch||u.debugDynamicSQL);
						line = "go";   // this only applies in case the last batch does not have a terminator AND there is dynamic SQL to process
					}
					else {
						line = inFileReader.readLine();
					}
				}
				else if (u.analysisPass == 2) {
					if (u.debugging) u.dbgOutput(u.thisProc()+"dynSQLCount=["+dynSQLCount+"] u.dynamicSQLBuffer.size()=["+u.dynamicSQLBuffer.size()+"] ", u.debugDynamicSQL);		
					if (dynSQLCount < u.dynamicSQLBuffer.size()) {
						if (!analyzingDynamicSQL) {
							u.dynamicSQLBuffer.add(0, "go");
							analyzingDynamicSQL = true;							
						}
						if (u.debugging) u.dbgOutput(u.thisProc()+"u.dynamicSQLBuffer=["+u.dynamicSQLBuffer+"] ", u.debugDynamicSQL);							
						line = u.dynamicSQLBuffer.get(dynSQLCount);
						dynSQLCount++;
						if (u.debugging) u.dbgOutput(u.thisProc()+"dynamic SQL: line=["+line+"] ", u.debugDynamicSQL);		
						if (line.startsWith(u.dynamicSQLBatchLine)) {
							List<String> tmpBatchLine = new ArrayList<>(Arrays.asList(line.substring(u.dynamicSQLBatchLine.length()).split(",")));
							//u.appOutput(u.thisProc()+"tmpBatchLine=["+tmpBatchLine+"] ");
							u.dynamicSQLFlag = true;
							dynamicSQLLineNr = Integer.parseInt(tmpBatchLine.get(0));
							dynamicSQLBatchNr = Integer.parseInt(tmpBatchLine.get(1));
							dynamicSQLBatchLineNr = Integer.parseInt(tmpBatchLine.get(2));
							dynamicSQLContext = tmpBatchLine.get(3);
							dynamicSQLSubContext = tmpBatchLine.get(4);
							if (u.debugging) u.dbgOutput(u.thisProc()+"dynamic SQL: dynamicSQLLineNr=["+dynamicSQLLineNr+"] dynamicSQLBatchNr=["+dynamicSQLBatchNr+"] dynamicSQLBatchLineNr=["+dynamicSQLBatchLineNr+"] dynamicSQLContext=["+dynamicSQLContext+"] dynamicSQLSubContext=["+dynamicSQLSubContext+"] ", u.debugDynamicSQL);		
							line = u.dynamicSQLBuffer.get(dynSQLCount);
							dynSQLCount++;							
							if (u.debugging) u.dbgOutput(u.thisProc()+"dynamic SQL: line=["+line+"] ", u.debugDynamicSQL);		
						}
					}
					else if (dynSQLCount == u.dynamicSQLBuffer.size()) {
						line = null;
					}
				}
				if ((line == null) && (u.analysisPass == 1)  && !lastLineRead && !startOfNewBatch) {	
					// this only matters in case there is dynamic SQL to process AND the last batch does not have a terminator			
					inputFilesLastLine.put(inFile, lineNr);
					if (u.debugging) u.dbgOutput("last line read without GO: lineNr=["+lineNr+"] startOfNewBatch=["+startOfNewBatch+"] lastLineWithoutTerminator=["+inputFilesLastLine.get(lineNr)+"]" , u.debugBatch||u.debugDynamicSQL);	
				}				
				if ((line == null) && (u.analysisPass == 2)  && !lastLineRead) {
					lastLineRead = true;		
					if (u.debugging) u.dbgOutput("last line was read! ", u.debugBatch);			
					if (u.dynamicSQLBuffer.size() > 0) {
						// there was some dynamic SQL in this file that needs to be analyzed, so process it now						
						if (u.debugging) u.dbgOutput("dynamic SQL still to be processed : "+u.dynamicSQLNrStmts+" batches, "+u.dynamicSQLBuffer.size()+" lines", u.debugBatch||u.debugDynamicSQL);	
						continue;
					}
				}
				
				if (line == null) {
					if (u.debugging) u.dbgOutput("end of file", u.debugBatch);
					endBatchFound = true;
					endOfFile = true;
					if (inComment > 0) {
						// seems we missed a comment close mark, let's add it
						if (u.debugging) u.dbgOutput("unclosed bracketed comment at end of file, adding " + inComment + " comment close marker(s)", u.debugBatch);
						for (int i = 0; i < inComment; i++) {
							batchText.append("\n */");
						}
					}
					if (inString) {
						// seems we missed a string close mark, let's add it
						if (u.debugging) u.dbgOutput("unclosed string at end of file, adding string delimiter [" + openQuote + "]", u.debugBatch);
						batchText.append(openQuote);
					}
				} 
				else {
					// make sure it's not a file taken from the imported directory that is used as source here; this will cause trouble downstream
					if (u.analysisPass == 1) {
						if (!reAnalyze) {
							if (lineNr == 0) {
								if (!u.importFileAttribute(line,1).isEmpty()) {
									if (!u.importFileAttribute(line,2).isEmpty()) {
										// this is the header line from the import copy, abort
										u.appOutput("This file contains a header line that indicates it was taken from an 'imported' subdirectory\nof a "+u.thisProgName+" report.");
										u.appOutput("You must remove the first line; when reprocessing, ensure the file is encoded as UTF-8, or use '-encoding utf8'.");
										u.appOutput("Aborting...");
										u.errorExit();
									}
								}					
							}
						}
					}

					// remove UTF-8 BOM if present: Java doesn't handle this. The BOM bytes for UTF8 are 0xEF 0xBB 0xBF, but the show up here as 0xFEFF
					if (lineNr == 0) {
						if (line.startsWith("\uFEFF")) {
							if (u.debugging) u.dbgOutput("UTF-8 BOM found, removed", u.debugBatch);
							line = line.substring(1);
						}		
					}						
																			
					if (u.analysisPass == 1) {
						if (!reAnalyze) {
							u.writeImportFile(line);
						}
					}
					
					if ((u.analysisPass == 2) || (reAnalyze && (u.analysisPass == 1))) {
						if ((lineNr == 0) && (!pass2Init)) {
							// skip first line
							pass2Init = true;
							continue;
						}
					}

					// process the line. CR & LF have been stripped off the end
					// objective is to find the batch terminator, taking into account multi-line strings and potentially nested comments
					lineNr++;
					batchLines++;
					if (u.debugging) u.dbgOutput("read line " + lineNr + "(len:" + line.length() + ")=[" + line + "]", u.debugBatch);
					
					// check for indications that encoding is not correctly specified
					if (doEncodingChecks) {
						if (line.length() == 1) {
							if (line.charAt(0) == 0) {
								String cs = Charset.defaultCharset().toString();
								if (userEncoding != null) cs = Charset.forName(userEncoding).toString();
								u.appOutput("Line " + lineNr + " contains only 0x00. Please verify input file encoding (using "+cs+"). Continuing, but errors may occur.");
								nrEncodingWarnings++;
							}
						}

						// don't drown the session in warnings
						if (nrEncodingWarnings > maxEncodingWarnings) {
							u.appOutput("(not reporting further 0x00-related errors)");
							doEncodingChecks = false;
						}
					}

					String lineCopy = line;
					boolean lineCopyProcessed = false;

					lineCopy = lineCopy.replaceAll("''", "");
					lineCopy = lineCopy.replaceAll("\"\"", " ");  // do not remove, we may need to detect this as a double-quoted string

					int lineCopyLenChk = lineCopy.length();
					int lineCopyLoopCntMax = 2;  // #times to check on line length not reducing
					int lineCopyLoopCnt = 0;
					int lineCopyLoopChk = 0;

					while (!lineCopyProcessed) {
						// loop protection, for some cases of invalid syntax, or incorrectly specified encoding
						lineCopyLoopCnt++;
						if (lineCopyLoopCnt > 1) {
							if (u.debugging) u.dbgOutput("loop chk top: prev length=[" + lineCopyLenChk + "], current length=[" + lineCopy.length() + "], lineCopy=[" + lineCopy + "]", u.debugBatch);
							if (lineCopyLenChk == lineCopy.length()) {
								lineCopyLoopChk++;
							} 
							else {
								lineCopyLoopChk = 0;
								lineCopyLenChk = lineCopy.length();
							}
							if (u.debugging) u.dbgOutput("loop chk top: lineCopyLoopChk=[" + lineCopyLoopChk + "]", u.debugBatch);
						}
						if (lineCopyLoopChk > lineCopyLoopCntMax) {
							// we seem to be in a loop...
							if (u.debugging) u.dbgOutput("loop chk: exit: lineNr=["+lineNr+"] orphanSquareBracket=["+orphanSquareBracket+"] lineCopy=[" + lineCopy + "]", u.debugBatch);
							String bracketMsg = "";
							if (orphanSquareBracket) bracketMsg = "Possibly delimited identifier containing newline? "; 
							u.appOutput("Error processing input file at line "+lineNr+". Is input file encoding correct? "+bracketMsg+"Continuing, but errors may occur.");
							break;
						}
						if (u.debugging) u.dbgOutput("top loop: lineCopyLoopCnt=[" + lineCopyLoopCnt + "] inComment=" + inComment + ", inString=" + inString + ", lineCopy top=[" + lineCopy + "]", u.debugBatch);
						if (inString) {
							// do nothing until we find a matching closing quote
							int ix = lineCopy.indexOf(openQuote);
							if (ix > -1) {
								lineCopy = (lineCopy + ' ').substring(ix + 1);
								inString = false;
								somethingFoundOnLine = true;
								if (u.debugging) u.dbgOutput("string close found", u.debugBatch);
							} 
							else {
								break;
							}
						}

						if ((!inString) && (inComment == 0)) {
							// search for string or comment
							Pattern linePattern = Pattern.compile("^(.*?)((--|/\\*|'|\"|[\\[]).*$)");
							Matcher lineMatcher = linePattern.matcher(lineCopy);
							while (!lineCopyProcessed) {
								if (u.debugging) u.dbgOutput("lineCopy top loop A=[" + lineCopy + "]", u.debugBatch);
								if (lineMatcher.find()) {
									String prefix = lineMatcher.group(1);
									String token = lineMatcher.group(3);
									lineCopy = lineMatcher.group(2);
									if (u.debugging) u.dbgOutput("token=[" + token + "]", u.debugBatch);
									if (token.equals("--")) { // can be on a batch delimiter line
										if (u.debugging) u.dbgOutput("simple comment", u.debugBatch);
										lineCopyProcessed = true;
										break;
									}
									somethingFoundOnLine = true;

									int lineCopyLen = lineCopy.length();

									if (token.equals("[")) { // delimiter identifier, will not span line boundary
										lineCopy = u.applyPatternFirst(lineCopy, "[\\[].*?[\\]]", "");  // seen identifier in an XPath context:  SELECT ... AS [Account/*]
										if (u.debugging) u.dbgOutput("bracketed identifier", u.debugBatch);
										if (lineCopy.length() == lineCopyLen) {
											// likely invalid syntax, avoid getting into a loop
											orphanSquareBracket = true;
											if (u.debugging) u.dbgOutput("ignoring orphan square bracket", u.debugBatch);
											break;
										}
										// do another round of stripping
										lineMatcher = linePattern.matcher(lineCopy);
										continue;
									}

									switch (token) {
										case "/*":
											lineCopy = u.applyPatternFirst(lineCopy, "/\\*.*?\\*/", "");
											break;
										case "'":
											lineCopy = u.applyPatternFirst(lineCopy, "'.*?'", "");
											break;
										case "\"":
											lineCopy = u.applyPatternFirst(lineCopy, "\".*?\"", "");
											break;
									}
									if (u.debugging) u.dbgOutput("lineCopy after initial strip: len=" + lineCopyLen + ", [" + lineCopy + "]", u.debugBatch);

									// were any chars removed, or do we have an open string or comment?
									if (lineCopy.length() == lineCopyLen) {
										if (token.equals("/*")) {
											inComment++;
											lineCopy = (lineCopy + ' ').substring(2);
										} 
										else {
											openQuote = token;
											inString = true;
											lineCopy = (lineCopy + ' ').substring(1);
										}
										break;
									}
									// do another round of stripping
									lineMatcher = linePattern.matcher(lineCopy);
								} 
								else {
									if (u.debugging) u.dbgOutput("no match, top", u.debugBatch);
									lineCopyProcessed = true;
									break;
								}
							} // while
						}
						if (u.debugging) u.dbgOutput("somethingFoundOnLine=" + somethingFoundOnLine + ", inComment=" + inComment + ", inString=" + inString + ", lineCopy after strip=[" + lineCopy + "]", u.debugBatch);

						if (inComment > 0) {
							// do nothing until we find a matching closing delimiter - which can be nested in T-SQL
							Pattern commentPattern = Pattern.compile("^(.*?)(\\/\\*|\\*\\/)");
							Matcher commentMatcher = commentPattern.matcher(lineCopy);
							if (u.debugging) u.dbgOutput("lineCopy before loop B=[" + lineCopy + "]", u.debugBatch);
							boolean commentFound = false;
							while (commentMatcher.find()) {
								String p1 = commentMatcher.group(1);
								String c = commentMatcher.group(2);
								lineCopy = (lineCopy + ' ').substring((p1 + c).length());
								commentFound = true;
								if (c.equals("/*")) inComment++;
								else if (c.equals("*/")) inComment--;
								commentMatcher = commentPattern.matcher(lineCopy);
								somethingFoundOnLine = true;
								if (u.debugging) u.dbgOutput("inComment=[" + inComment + "]  p1=[" + p1 + "]  c=[" + c + "]  lineCopy=[" + lineCopy + "]", u.debugBatch);
								if (inComment == 0) {
									if (u.debugging) u.dbgOutput("break on inComment=0, [" + lineCopy + "]", u.debugBatch);
									break;
								}
							} // while
							if (!commentFound) {
								break;
							}
						}
					} // while

					if (!somethingFoundOnLine) {
						if ((!inString) && (inComment == 0)) {
							// check line for batch terminator
							line = u.applyPatternFirst(line, "^\\s*GO\\s*?(\\s\\d+\\s*|--.*)?$", "go");
							line = u.applyPatternFirst(line, "^\\s*(:)?RESET\\s*(--.*)?$", "reset");
							line = u.applyPatternFirst(line, "^\\s*(:)?(EXIT|QUIT)\\s*(--.*)?$", "exit");
							if (u.debugging) u.dbgOutput("read2=[" + line + "]", u.debugBatch);
							if (line.trim().equalsIgnoreCase("go")) {
								if (u.debugging) u.dbgOutput("line is go=[" + line + "]", u.debugBatch);
								line = "";
								endBatchFound = true;
							}
							if (line.trim().equalsIgnoreCase("reset")) {
								// Todo: batch is not written to the .batch file
								if (u.debugging) u.dbgOutput("line is reset=[" + line + "]", u.debugBatch);
								endBatchFound = false;
								startOfNewBatch = true;
								startBatchLineNr = lineNr + 1;
								batchText = new StringBuilder();
								continue;
							}
							if (line.trim().equalsIgnoreCase("exit")) {
								if (u.debugging) u.dbgOutput("exit found", u.debugBatch);
								if (u.dynamicSQLBuffer.size() == 0) endOfFile = true;
								endBatchFound = false;
								if (u.analysisPass == 1) {
									if (batchNr == 0) {
										u.appOutput("No batches found in this file.");
									}
								}
								exitFound = true;								
								lastLineRead = true;
								line = null;
								batchText.setLength(0); // wipe out the current batch, just as sqlcmd does
								batchNr--;
								continue;	
							}
						}
					}
					if (u.debugging) u.dbgOutput("startOfNewBatch=["+startOfNewBatch+"]  endBatchFound=["+endBatchFound+"]  leadingBlankLines=["+leadingBlankLines+"] ", u.debugBatch);

					boolean emptyLine = false;
					if (line.trim().isEmpty()) {
						emptyLine = true;	
					}
					else {
						leadingBlankLines = false;
						if (u.debugging) u.dbgOutput("setting leadingBlankLines=["+leadingBlankLines+"] ", u.debugBatch);												
					}
					
					if (startOfNewBatch && !endBatchFound) {
						if (emptyLine) {							
							if (skipLeadingBlankLines) {
								if (u.debugging) u.dbgOutput("skipping LeadingBlankLines", u.debugBatch);
								continue;
							}
						}
						startOfNewBatch = false;
					}

					if (!endBatchFound) {
						if (u.debugging) u.dbgOutput("adding line=[" + line + "]", u.debugBatch);
						batchText.append(line).append("\n");
					}
				}

				if (endBatchFound) {
					if (u.debugging) u.dbgOutput("endBatchFound=["+endBatchFound+"] analyzingDynamicSQL=["+analyzingDynamicSQL+"] ", u.debugBatch);
					// process the batch
					if (!analyzingDynamicSQL) {
						batchNr++;
						if (endOfFile && leadingBlankLines && !exitFound) batchNr--;
						u.batchNrInFile = batchNr;
						u.lineNrInFile = startBatchLineNr;		
					}
					else {
						// Dynamic SQL: use numbers from original batch
						batchNr = u.batchNrInFile = dynamicSQLBatchNr;
						startBatchLineNr = u.lineNrInFile = dynamicSQLBatchLineNr;		
					}
					
					if (startOfNewBatch || leadingBlankLines) {
						// nothing to process
						if (endOfFile) {
							if (u.analysisPass == 1) {
								if (batchNr == 0) {
									u.appOutput("No batches found in this file.");
								}
							}
							break;
						}
						if (u.debugging) u.dbgOutput("endBatchFound, startOfNewBatch: nothing to process leadingBlankLines=["+leadingBlankLines+"] ", u.debugBatch);

						// prep for next batch
						endBatchFound = false;
						startOfNewBatch = true;
						leadingBlankLines = true;
						startBatchLineNr = lineNr + 1;
						batchText = new StringBuilder();
						nrLinesInFile += batchLines;
						batchLines = 0;
						inComment = 0;
						inString = false;						
						continue;
					} 
					else {
						// Count the last line: while the line represents the batch terminator and therefore not contains any SQL,
						// not counting this line results in the number of lines reported to the user to differ from the #lines in the 
						// actual input file (the difference being the number of batches)
						
						if (u.analysisPass == 1) {
							nrLinesTotalP1 += batchLines;
						}
						if (u.analysisPass == 2) {
							nrLinesTotalP2 += batchLines;
						}

						if (dumpBatchFile) {
							if (u.analysisPass == 1) {
								u.writeBatchFile("\npass=[" + u.analysisPass + "] Batch " + batchNr + ", lineNrinFile=[" + lineNr + "],  batch=[" + batchText + "]");
							}
						}
						if (dumpParseTree) {
							u.appOutput("\npass=[" + u.analysisPass + "] Batch " + batchNr + "=[" + batchText + "]");
						}

						if (u.debugging) u.dbgOutput("handing off to parser: u.analysisPass=["+u.analysisPass+"] batchNr=["+batchNr+"]  batchLines=["+batchLines+"]  ", u.debugBatch);
						
						charStream = CharStreams.fromString(batchText.toString());

						// parse a batch and put the parse tree in the list for subsequent analysis
						startTime = System.currentTimeMillis();			
						String ptreeText = parseBatch(charStream, inFile, batchNr, batchLines, antlrSLL);
						endTime = System.currentTimeMillis();
						duration = (endTime - startTime);
						timeElapsed = duration;
						timeElapsedFile += duration;
						timeCount.put("parseTime", timeCount.get("parseTime") + duration);
												
						if (duration > timeCount.get("parseTimeMax")) {
							timeCount.put("parseTimeMax", duration);
							timeCount.put("parseTimeMaxBatchNr", (long) batchNr);
							timeCount.put("parseTimeMaxBatchLine", (long) startBatchLineNr);
							timeCount.put("parseTimeMaxBatchLines", (long) batchLines);
							timeCount.put("parseTimeMaxBatchPass", (long) u.analysisPass);
							timeCountStr.put("parseTimeMaxBatchFile", u.currentSrcFile.substring(u.currentSrcFile.lastIndexOf(File.separator) + 1));
						}

						if (u.debugging) u.dbgOutput("returning from parser", u.debugBatch);

						if (dumpBatchFile) {
							if (!hasParseError) {
								u.writeBatchFile(ptreeText);
							}
							u.writeBatchFile("Batch " + batchNr + ": lines=" + batchLines + ", parse time(secs)=" + (timeElapsed/1000));
						}

						if (hasParseError) {
							nrParseErrors++;
						}

						boolean printErrMsg = true;
						if (u.analysisPass == 2) {
							if (!dumpParseTree) {
								printErrMsg = false;
							}
							if (analyzingDynamicSQL) {
								printErrMsg = true;								
								if (hasParseError) {
									totalParseErrors++;
								}								
							}
						}
						if (hasParseError) {
							if (printErrMsg) {
								// print to session
								String errMsg = "Syntax error in batch " + batchNr + ", starting at line " + startBatchLineNr + " in input file\n" + parseErrorMsg.toString().trim();
								u.appOutput(errMsg);
							}
						}

						if ((u.analysisPass == 1) || ((u.analysisPass == 2) && analyzingDynamicSQL)) {
							if (u.debugging) u.dbgOutput("u.analysisPass=["+u.analysisPass+"]  analyzingDynamicSQL=["+analyzingDynamicSQL+"] dumpParseTree=["+dumpParseTree+"] ", u.debugBatch);
							if (hasParseError) {
								// write error batch
								if (u.errBatchFileWriter == null) {
									u.openErrBatchFile(reportName, inFile, runStartTime);
								}

								// log error batch to file
								String b = "Batch";
								String b2 = "";
								if (analyzingDynamicSQL) {
									b = "Dynamic SQL";
									b2 = "dynamic SQL ";
								}
								u.writeErrBatchFile("Syntax error "+b2+"in batch " + batchNr + ", starting at line " + startBatchLineNr + " in file " + Paths.get(inFile).toAbsolutePath() + "\n"+b+"=[" + batchText + "]");
								u.writeErrBatchFile(parseErrorMsg.toString().trim() + "\n");
								u.writeErrBatchFile(u.composeOutputLine("-", "-") + "\n");			
								
								if (printErrMsg) {
									if (!dumpParseTree) {
										u.appOutput("(see "+u.errBatchFilePathName+")");									
										u.appOutput("");
									}  // need separator line	
								}											
							}

							if (dumpParseTree) {
								if (!hasParseError) {
									u.appOutput(ptreeText);
								}
								u.appOutput("Batch " + batchNr + ": lines=" + batchLines + ", parse time(secs)=" + (timeElapsed/1000));
							}
						}

						// analyze the tree
						if (!hasParseError) {
							if (parseOnly && (u.analysisPass > 1)) {
								// do nothing
							} 
							else if (exportedParseTree != null) {
								// even with -parseonly, we need to run analysis in order to process set quoted_identifier, which affects parsing
								if (u.debugging) u.dbgOutput("pass=["+u.analysisPass+"] Analyzing tree for batchNr=["+batchNr+"] batchLines=["+batchLines+"] ", u.debugBatch);
								String phase = "analysisTimeP" + u.analysisPass;
								startTime = System.currentTimeMillis(); 								

								a.analyzeTree(exportedParseTree, batchNr, batchLines, u.analysisPass);
								
								endTime = System.currentTimeMillis();
								duration = (endTime - startTime);
								timeElapsed = duration;
								timeElapsedFile += duration;
								timeCount.put(phase, timeCount.get(phase) + duration);
								
								if (duration > timeCount.get(phase+"Max")) {
									timeCount.put(phase+"Max", duration);
									timeCount.put(phase+"MaxBatchNr", (long) batchNr);
									timeCount.put(phase+"MaxBatchLine", (long) startBatchLineNr);
									timeCount.put(phase+"MaxBatchLines", (long) batchLines);
									timeCount.put(phase+"MaxBatchPass", (long) u.analysisPass);
									timeCountStr.put(phase+"MaxBatchFile", u.currentSrcFile.substring(u.currentSrcFile.lastIndexOf(File.separator) + 1));
								}
							}
						}

						if (hasParseError) {
							// clear error indication
							hasParseError = false;
							parseErrorMsg = new StringBuilder();
						}

						// prep for next batch
						endBatchFound = false;
						startOfNewBatch = true;
						leadingBlankLines = true;
						startBatchLineNr = lineNr + 1;
						batchText = new StringBuilder();
						nrLinesInFile += batchLines;
						batchLines = 0;
						inComment = 0;
						inString = false;
						if (u.debugging) u.dbgOutput("resetting: startOfNewBatch=" + startOfNewBatch + ", endBatchFound=" + endBatchFound, u.debugBatch);
					}
				}

				if (endOfFile) {
					break;
				}
			}  // while

			passCount.put(u.analysisPass,1);
			if (passCount.size() > 1) {
				// don't add, or we'd be doubling up the totals
				// note: no idea what the thinking was here. sorry!
			} 
			else {
				totalBatches += batchNr;
				totalParseErrors += nrParseErrors;				
			}

			inFileReader.close();

			if (u.analysisPass == 1) {
				// save symbol table to disk
				try {
					String inFileTmp = inFile;
					if (reAnalyze) inFileTmp = u.currentSrcFile;
					//u.appOutput(CompassUtilities.thisProc()+"symtab inFile=["+inFileTmp+"] ");
					if (!importOnly) u.writeSymTab(reportName, inFileTmp, appName);
				} catch (Exception e) {
					u.appOutput("Error writing symbol table " + u.symTabFilePathName);
					throw e;
				}
				CompassUtilities.clearSymTab();
			}

			if (u.analysisPass == 2) {
				u.appendCaptureFile(CompassUtilities.makeMetricsLine(u.currentSrcFile, u.currentAppName, batchNr, nrParseErrors, lineNr));
				u.closeCaptureFile();
			}
				
			if (u.analysisPass == 2) {
				// if substitutions required, apply them
				if (u.rewriteTextList.size() > 0) {
					long startRewrite = System.currentTimeMillis();							
					u.performRewriting(reportName, u.currentAppName,inFileCopy);	
					long endRewrite = System.currentTimeMillis();										
					long secsRewrite = endRewrite - startRewrite;	
					timeCount.put("secsRewrite", secsRewrite);
											
					if (CompassUtilities.devOptions) {	  
						u.appOutput("SQL rewrite time: " + (timeCount.get("secsRewrite")/1000) + " seconds" );
					} 										
				}
			}

			if (u.analysisPass == 2) {	
				if (CompassUtilities.devOptions) {
					// temporary, for development
					int secs = ((int) timeElapsedFile / 1000);
					int linesSec = (secs > 0) ? (nrLinesInFile / secs) : nrLinesInFile;
					int batchesSec = (secs > 0) ? (batchNr / secs) : batchNr;
					u.appOutput("ELAPSED TIME: " + u.currentSrcFile + " : seconds=" + secs + "   lines/sec=" + linesSec + "   batches/sec=" + batchesSec + " retrySLL="+retrySLLFile ); 
				}
			}

			u.currentSrcFile = "";
			u.currentAppName = "";

			if (!reAnalyze) {
				u.closeImportFile();
				if (u.analysisPass == 1) {
					u.importFileUpdateBatchLines(inFileCopy, batchNr, nrLinesInFile);
				}
			}

			if (dumpBatchFile) {
				u.closeBatchFile();
			}
			if (u.errBatchFileWriter != null) {
				u.closeErrBatchFile();
			}		
		} //for inputfiles
		
	
		// generate anonymized capture files
		if (u.analysisPass == 2) {
			if (u.anonymizedData) {
				anonymizeCapturedData();		
			}			
		}
	}
	
	private static void getAutoDDL () throws Exception {
		// auto-generate DDL script
		String PScmd = "powershell";
		if (!CompassUtilities.onWindows) {
			PScmd = "pwsh";
		}
		String autoDDLTag = "BabelfishCompassAutoDDL";		
		if (u.debugging) u.dbgOutput(autoDDLTag + ": start: Compass Powershell", u.debugAutoDDL);

		String autoDDLScript = "SMO_DDL.ps1"; 			
		if (u.onWindows) autoDDLScript = ".\\" + autoDDLScript;
		else autoDDLScript = "./" + autoDDLScript;		

// uncomment these lines for testing without SMO invocation:
//		SMODDLTag = "_DDL_2023-05-20";
//		SMOOutputFolder = "C:\\Users\\rcv\\AppData\\Local\\Temp\\CompassAutoDDL-2023-May-20-12.40.25";
//if (false) {				
		// check if Powershell is installed
		String PSCheckTag = "BabelfishCompassPowershellTest";	
		String cmd = PScmd + " -Command \"Write-Output " + PSCheckTag + "\"";
		String cmdOut = u.runOScmd(cmd, true);
		if (u.debugging) u.dbgOutput("cmd=["+cmd+"] ", u.debugAutoDDL);
		if (u.debugging) u.dbgOutput("cmdOut=["+cmdOut+"] ", u.debugAutoDDL);
		if (!cmdOut.contains(PSCheckTag)) {
			u.appOutput("\nERROR: Powershell is not available (expecting '"+PScmd.trim()+"' to be in the PATH)");
			if (!u.onWindows) {
				u.appOutput("\nTo install Powershell on Linux, see https://learn.microsoft.com/en-us/powershell/scripting/install/installing-powershell-on-linux");
			}
			u.errorExit();	
		}
		
		// On Windows, check Powershell execution policy; on Linux/MacOS, it's always Unrestricted
		if (u.onWindows) {
			cmd = PScmd + " -Command Get-ExecutionPolicy ";
			cmdOut = u.runOScmd(cmd, true);
			if (u.debugging) u.dbgOutput("cmd=["+cmd+"] ", u.debugAutoDDL);
			if (u.debugging) u.dbgOutput("cmdOut=["+cmdOut+"] ", u.debugAutoDDL);
			cmdOut = cmdOut.trim();
			if (!cmdOut.equals("Unrestricted")) {
				u.appOutput("\nERROR: The Powershell execution policy must be set to 'Unrestricted' to run script "+autoDDLScript + ",\nwhich is required for the -sqlendpoint option.\nCurrent setting: " + cmdOut);
				u.appOutput("Run 'Set-ExecutionPolicy -ExecutionPolicy Unrestricted' in Powershell to modify the\nexecution policy; run 'Get-ExecutionPolicy' to verify.");
				u.errorExit();	
			}	
		}	
		
		// On Windows, Unblock the PS script: as it is a downloaded file, PS will not execute without prompting unless unblocked first
		if (u.onWindows) {
			cmd = PScmd + " -Command Unblock-File -Path " + autoDDLScript;
			cmdOut = u.runOScmd(cmd, true);
			if (u.debugging) u.dbgOutput("cmd=["+cmd+"] ", u.debugAutoDDL);
			if (u.debugging) u.dbgOutput("cmdOut=["+cmdOut+"] ", u.debugAutoDDL);
			cmdOut = cmdOut.trim();  // we don't use the output
		}	
		
		// now spawn Powershell SMO script	
		String nowTS = new SimpleDateFormat("yyyy-MMM-dd-HH.mm.ss").format(startRunDate);		
		String nowD = new SimpleDateFormat("yyyy-MMM-dd").format(startRunDate);
		SMODDLTag = "_SMO_DDL_"+nowD;
		
		SMOOutputFolder = System.getenv().get("TEMP");
		if (!u.onWindows) SMOOutputFolder = "/tmp";
		SMOOutputFolder += File.separator + "CompassAutoDDL-" + nowTS;
			
		u.appOutput("\nRunning Powershell/SMO script to generate DDL for server '"+sqlEndpoint+"' into directory "+SMOOutputFolder+"...");
		u.appOutput("Note: run times strongly depend on network proximity to the SQL Server. Alternatively, generate DDL manually\nthrough SQL Server Management Studio on the SQL Server host.\nYou can abort by hitting CTRL-C\n");
		u.checkDir(SMOOutputFolder, false, false);
		
		sqlDBList = sqlDBList.trim();
		if (sqlDBList.isEmpty()) {
			sqlDBList = "ALL";
		}
		else if (sqlDBList.equals(u.uninitialized)) {
			sqlDBList = "ALL";
		}	
		if (sqlDBList.isEmpty()) {
			sqlDBList = "ALL";
		}			

		// check PS script is present
		File f = new File(autoDDLScript);
		if (!f.exists()) {
			u.appOutput("\nERROR: Powershell SMO script '"+autoDDLScript+"' not found"); 
			u.errorExit();					
		}
						
		// In case of databases = ALL, show list of all DBs first, as it could potentially take long to process all
		// In any case, find out the approx roundtrip time first so that we can issue a warning if it looks like it may take a long time
		int loopCnt = 0;
		while (true) {
			loopCnt++;
			String DDLTagArg = SMODDLTag;
			if (loopCnt == 1) {
				DDLTagArg = "report-roundtrip"; // matches the string in the .ps script!
			}
			if (sqlDBList.equalsIgnoreCase("ALL") && (loopCnt == 1)) {
				DDLTagArg = "report-all-dbs"; // matches the string in the .ps script!
			}			
					
			// compose command line		
			String runautoDDLScript = PScmd + " " + autoDDLScript; 		
			runautoDDLScript += " -Databases '"+sqlDBList+"' -OutputFolder '"+SMOOutputFolder+"' -SMOOutputDir '' -DDLTag '"+DDLTagArg+"' -ServerName '"+sqlEndpoint+"' -Username '"+sqlLogin+"' -Password '"+sqlPasswd+"'";
			cmd = runautoDDLScript;
			if (u.debugging) u.dbgOutput("loopCnt=["+loopCnt+"] cmd=["+cmd+"] ", u.debugAutoDDL);			
			cmdOut = u.runOScmd(cmd, true);
			//u.appOutput(u.thisProc()+"loopCnt=["+loopCnt+"] cmdDuration=["+cmdDuration+"] ");
			if (u.debugging) u.dbgOutput("loopCnt=["+loopCnt+"] cmdOut=["+cmdOut+"] ", u.debugAutoDDL);
			
			// Installing SMO may be tricky and may not always work 
			// But if we see the message that the connection is successful, we should be good
			if (!cmdOut.contains("Connected to SQL Server")) {			
				if (cmdOut.contains("A positional parameter cannot be found")) {
					u.appOutput("\nERROR: Error invoking Powershell SMO script"); 
				    u.appOutput("Command line:" + cmd);		
				    u.appOutput(cmdOut);					    				
					u.errorExit();	
				}			
				if (cmdOut.contains("New-Object : Cannot find type [Microsoft.SqlServer.Management.Smo.Server]")) {
					u.appOutput("\nERROR: SMO does not seem to be installed (New-Object SMO)"); 
					u.errorExit();	
				}
				if (cmdOut.contains("New-Object : Could not load file or assembly 'System.Data.SqlClient")) {
					u.appOutput("\nERROR: SMO does not seem to be installed (New-Object SqlClient)"); 
					u.errorExit();	
				}
				if (cmdOut.contains("The property 'Login' cannot be found on this object")) {
					u.appOutput("\nERROR: SMO does not seem to be installed (Login)"); 
					u.errorExit();	
				}
				if (cmdOut.contains("specified output folder not found")) {
					u.appOutput("\nERROR: Error while running Powershell SMO script\n");
				    u.appOutput(cmdOut);				
					u.errorExit();	
				}			
				// some other error			
				u.appOutput("\nERROR: Unable to connect to SQL Server "+sqlEndpoint + " with login '"+sqlLogin+"' and password specified\n");
				u.appOutput(cmdOut);
				u.errorExit();									
			}
			
			// if we get here, SMO seems to be working as we have connected to the DB
			if (sqlDBList.equalsIgnoreCase("ALL") && (loopCnt == 1)) { 
				// show list of DBs so that user knows what's going to happen
				String sOut = CompassUtilities.getPatternGroup(cmdOut, "\\b(\\d+ user databases found in server.*?)\n", 1);
				u.appOutput(sOut);
			}
						
			if (loopCnt == 1) { 
				// figure out round trip time
				Integer trip = 0;
				String tripStr = "";
				try {
					tripStr = CompassUtilities.getPatternGroup(cmdOut, "INFO: roundtrip millisec=(\\d+)\n", 1);
					trip = Integer.parseInt(tripStr);
				} catch (Exception e) { 
					trip = -1;							
				}		
				
				if (u.debugging) u.dbgOutput("Approximate client-server roundtrip time: "+ tripStr + " millisec.", u.debugAutoDDL);							
				if (trip > 200) { // empirically determined: within an AWS region, a roundtrip tends to be < 100 millisec. 
					String roundtripStr = "(approx. "+ tripStr +" millisec. roundtrip)";	
					u.appOutput("Note: the connection to the SQL Server does not seem to be very fast "+roundtripStr+".\nGenerating DDL may take some time...");										
				}		
			}
			
			if (loopCnt == 1) { 
				continue;
			}			
			
			if (cmdOut.contains("No user databases found")) {
				u.appOutput("\nERROR: No user databases found in SQL Server "+sqlEndpoint+"\n");
				u.errorExit();				
			}
			if (cmdOut.contains("Cannot find path")) {
				// something went wrong 
				u.appOutput("\nERROR: Error while running Powershell SMO script\n");
				u.appOutput(cmdOut);
				u.errorExit();				
			}			
					
			// exit loop
			if (loopCnt >= 2) { 
				break;
			}																						
		}
//}// uncomment for testing without SMO invocation

		//  Check output for errors
		if (u.debugging) u.dbgOutput("cmdOut=["+cmdOut+"] ", u.debugAutoDDL);			
		String s = cmdOut;
		while (true) {
			String errLine = CompassUtilities.getPatternGroup(s, "(^.*?\nERROR:)", 1, "multiline");
			if (errLine.isEmpty()) {
				break;
			}
			s = s.substring(errLine.length());
			errLine  = CompassUtilities.getPatternGroup(s, "(^.*?)[\n\r]", 1, "multiline");
			if (errLine.isEmpty()) errLine = s;
			if (errLine.contains("Specified database not found")) {			
				String dbName = CompassUtilities.getPatternGroup(errLine, "Specified database not found: ('.*?')", 1);
				u.appOutput("ERROR: Specified database "+dbName+ " not found in SQL Server "+sqlEndpoint+"\n");							
			}		
			if (errLine.contains("Script transfer failed")) {
				// Something did not work, for example the database is offline or not accessible		
				String dbName = CompassUtilities.getPatternGroup(errLine, "ScriptTransfer\\(\\) for database \\[(.*?)\\] to ", 1);
				u.appOutput("ERROR: Error generating script for database '"+dbName+ "' in SQL Server "+sqlEndpoint+"\n");										
			}		
		}

		// generated DDL, now proceed to run Compass analysis on the DDL
		u.appOutput("DDL generated in "+SMOOutputFolder + " :");
		String cmdDir = "dir ";
		if (!u.onWindows) cmdDir = "ls -lh";
		cmdDir += " " + SMOOutputFolder + File.separator;	
		cmdOut = u.runOScmd(cmdDir, true);
		u.appOutput(cmdOut);
		
		String ddlFiles = SMOOutputFolder + File.separator + "*.sql";		
		inputFiles.clear();
		addInputFile(ddlFiles);
		inputFilesOrig = inputFiles;	
	
		if (u.debugging) u.dbgOutput(autoDDLTag + ": ready: Compass Powershell", u.debugAutoDDL);
	}
	
	protected String parseBatch(CharStream batchText, String fileName, int batchNr, int batchLines, boolean useSLL)  {
		String batchTextCopy = batchText.toString();
		TSQLLexer lexer = new TSQLLexer(batchText);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		TSQLParser parser = new TSQLParser(tokenStream);

		// get the grammar rule names
		if (CompassUtilities.grammarRuleNames == null) {
			CompassUtilities.grammarRuleNames = parser.getRuleNames();
		}
		
		if ( antlrShowTokens ) {
			tokenStream.fill();
			for (Token tok : tokenStream.getTokens()) {
				if ( tok instanceof CommonToken ) {
					String stok = "text=["+tok.getText()+"] line="+ tok.getLine()+ "  col="+tok.getCharPositionInLine();
					System.out.println("stok: "+stok );
				}
				else {
					String stok = "text2=["+tok.getText()+"] line2="+ tok.getLine()+ "  col2="+tok.getCharPositionInLine();
					System.out.println("stok2: "+stok );
				}
			}
		}

		if ( antlrDiagnostics ) {
			parser.addErrorListener(new DiagnosticErrorListener());
			parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
		}

		// set up parsing
		parser.setBuildParseTree(true);  
		parser.setTrace(antlrTrace);

		// capture parser error messages
		BaseErrorListener errorListener = new BaseErrorListener() {
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
									int charPositionInLine, String msg, RecognitionException e) {
				Token token = (Token)offendingSymbol;
				msg = u.limitTextSize(msg);				
				parseErrorMsg.append("Line ").append(line).append(":").append(charPositionInLine + 1).append(", ");

				if (token.getType() == TSQLLexer.UNMATCHED_CHARACTER) {
					u.addLexicalErrorHex(parseErrorMsg, token.getText());
				}
				else {
					parseErrorMsg.append("syntax error");
					if (analyzingDynamicSQL) parseErrorMsg.append(" in dynamic SQL");
					parseErrorMsg.append(": ").append(msg);
				}
				hasParseError = true;
			}
		};
		parser.removeErrorListeners();
		parser.addErrorListener(errorListener);

		// add listener to always know last token
		CompassLastTokenListener lastTokenListener = new CompassLastTokenListener();
		parser.addParseListener(lastTokenListener);

		// stop parsing when an error is encountered
		parser.setErrorHandler(new BailErrorStrategy());

		if (useSLL) {
			if (u.debugging) u.dbgOutput("useSLL=[" + useSLL + "] batchNr=[" + batchNr + "] batchLines=[" + batchLines + "]", u.debugBatch||u.debugPtree);
			parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
		}

		String treeString = "";
		try {
			// get the parse tree
			TSQLParser.Tsql_fileContext tree = parser.tsql_file();

			// catch lexer errors, currently these don't throw an exception
			// like parser errors do
			if (parseErrorMsg.length() > 0) {
				return "";
			}

			// export the parse tree
			if ((batchNr > 0) || (batchNr == 0) && analyzingDynamicSQL) {
				exportedParseTree = tree;
			}

			// return parse tree as string, if required
			if (dumpParseTree) {
				treeString = tree.toStringTree(parser);
			}

		} catch (Exception e) {
			// we get here for parser errors
			if (u.debugging) u.dbgOutput("syntax error in catch; pass=" + u.analysisPass + " useSLL=[" + useSLL + "] batchNr=[" + batchNr + "] ",  u.debugBatch||u.debugPtree);
			if (useSLL) {
				retrySLL++;
				retrySLLFile++;
				parseErrorMsg = new StringBuilder();
				hasParseError = false;
				return parseBatch(CharStreams.fromString(batchTextCopy), fileName, batchNr, batchLines, false);
			}

			if (parseErrorMsg.length() == 0) {
				String unmatchedLexerError = u.getAndSetNullErrorMsg();
				if (unmatchedLexerError != null) {
					parseErrorMsg.append(unmatchedLexerError);
				} 
				else {
					Token lastToken = lastTokenListener.getLastToken();
					parseErrorMsg.append("Line ").append(lastToken.getLine()).append(":").append(lastToken.getCharPositionInLine() + 1).
							append(", ").append("syntax error: Unable to parse token '").append(lastToken.getText()).append("'");
				}
			}
			hasParseError = true;
		}
		return treeString;
	}	
}
