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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.stream.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;

public class CompassUtilities {

	// not-initialized strings
	public final String uninitialized = "-init-";

	public static boolean onWindows;

	public static final String thisProgVersion      = "0.1";
	public static final String thisProgVersionDate  = "September 2021";
	public static final String thisProgName         = "Babelfish Compass";
	public static final String thisProgNameLong     = "Compatibility assessment tool for Babelfish for T-SQL";
	public static final String thisProgNameExec     = "Compass";
	public static final String thisProgPathExec     = "compass";
	public static final String babelfishProg        = "Babelfish";
	public static final String copyrightLine        = "Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.";
	public static String thisProgExec               = "java " + thisProgPathExec + "." + thisProgNameExec;
	public static final String thisProgExecWindows  = "BabelfishCompass.bat";
	public static final String thisProgExecLinux    = "BabelfishCompass.sh";

	public static final String disclaimerMsg  =
            "Notice:\n"
          + "This report contains an assessment based on the resources you scanned with the\n"
          + "Babelfish Compass tool. The information contained in this report, including whether\n"
          + "or not a feature is 'supported' or 'not supported', is made available 'as is',\n"
          + "and may be incomplete, incorrect, and subject to interpretation.\n"
          + "You should not base decisions on the information in this report without independently\n"
          + "validating it against the actual SQL/DDL code on which this report is based.\n";

	// .cfg file is in a fixed place, namely, %COMPASS%/<defaultfilename>
	public String cfgFileName = uninitialized;
	public final String defaultCfgFileName  = "BabelfishFeatures.cfg";

	// .cfg file format version as found in the .cfg file; this is validated
	public Integer cfgFileFormatVersionRead = 0;
	// .cfg file format version supported by this version of the Babelfish Compass tool
	public Integer cfgFileFormatVersionSupported = 1;

	// .cfg file timestamp
	public String cfgFileTimestamp = uninitialized;

	// user-specified
	public final String fileNameCharsAllowed = "[^\\w\\_\\.\\-\\/\\(\\)]";
	public String targetBabelfishVersion = ""; // Babelfish version for which we're doing the analysis
	public boolean stdReport = false;	// development only

	// minimum Babelfish version; this is fixed
	public static final String baseBabelfishVersion = "1.0";

	// standard line length
	public final int reportLineLength = 80;
	public final String lineIndent = "   ";


	// file handling
	public final String BabelfishCompassFolderName = "BabelfishCompass";
	public final String batchDirName = "batches";
	public final String batchFileSuffix = "batch.txt";
	public final String errBatchDirName = "errorbatches";
	public final String errBatchFileSuffix = "errbatch.txt";
	public final String capDirName = "captured";
	public final String captureFileName = "captured";
	public final String captureFileSuffix = "dat";
	public final String symTabDirName = "sym";
	public final String symTabFileTag = "bbf~symtab";
	public final String symTabFileSuffix = "dat";
	public final String importDirName = "imported";
	public final String importFileTag = "bbf~imported";
	public final String importFileSuffix = "dat";
	public final String logDirName = "log";

	//public final String parsedFileSuffix = "parsed";
	public String reportFilePathName = uninitialized;
	public BufferedWriter reportFileWriter;
	public String batchFilePathName;
	public BufferedWriter batchFileWriter;
	public String errBatchFilePathName;
	public String symTabFilePathName;
	public BufferedWriter symTabFileWriter;
	public int symTabFileLineCount=0;
	public BufferedWriter errBatchFileWriter = null;
	public String importFilePathName;
	public BufferedWriter importFileWriter;
	public String sessionLogPathName;
	public BufferedWriter sessionLogWriter;

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

	// first line in capture file:
	public final String captureFileLinePart1 = "# Captured items for report ";
	public final String captureFileLinePart2 = " with targeted "+babelfishProg+ "version ";
	public final String captureFileLinePart3 = " generated at ";

	// line separator
	public final String newLine = System.getProperty("line.separator");

	// report generation
	public final String sortKeySeparator = "  ~~~";
	public final String lastItem = "~ZZZZZZ~LastItem";
	public static boolean reportShowAppName = true;
	public static boolean reportShowSrcFile = true;
	public static boolean reportAppsCount = true;
	public static String reportOptionXref = "";
	public static String reportOptionStatus = "";
	public static String reportOptionApps = "";
	public static String reportOptionDetail = "";
	public static String reportOptionFilter = "";
	public static int linesSQLInReport = 0;
	public static String reportHdrLines = "";
	public static int maxLineNrsInList = 10;	//todo: make user-configurable

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
	static Map<String, String> objTypeSymTab = new HashMap<String, String>();
	static Map<String, String> UDDSymTab = new HashMap<String, String>();
	static Map<String, String> SUDFSymTab = new HashMap<String, String>();
	static Map<String, String> TUDFSymTab = new HashMap<String, String>();

	//XML methods
	static final List<String> XMLmethods = Arrays.asList("EXIST", "MODIFY", "QUERY", "VALUE", "NODES");
	static Map<String, String> SUDFNamesLikeXML = new HashMap<String, String>();
	static Map<String, String> TUDFNamesLikeXML = new HashMap<String, String>();

	//HIERARCHYID methods
	static final List<String> HIERARCHYIDmethods = Arrays.asList("GETANCESTOR", "GETDESCENDANT", "GETLEVEL", "ISDESCENDANTOF", "READ", "GETREPARENTEDVALUE", "TOSTRING", "GETROOT", "PARSE");
	static Map<String, String> SUDFNamesLikeHIERARCHYID = new HashMap<String, String>();

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
	public final HashSet<String> dbgOptions = new HashSet<>(Arrays.asList("all", "batch", "ptree", "cfg", "dir", "symtab", "report", "calc"));
	public final HashSet<String> specifiedDbgOptions = new HashSet<>(dbgOptions.size());
	public boolean debugBatch;
	public boolean debugPtree;
	public boolean debugCfg;
	public boolean debugDir;
	public boolean debugSymtab;
	public boolean debugReport;
	public boolean debugCalc;
	public boolean debugging;

	// classification values for SQL features
	public final String Supported         = "SUPPORTED";
	public final String NotSupported      = "NOTSUPPORTED";
	public final String ReviewSemantics   = "REVIEWSEMANTICS";
	public final String ReviewPerformance = "REVIEWPERFORMANCE";
	public final String ReviewManually    = "REVIEWMANUALLY";
	public final String Ignored           = "IGNORED";
	public final String ObjCountOnly      = "OBJECTCOUNTONLY";

	// TODO Convert these lists in sets for efficiency
	public List<String> supportOptions        = Arrays.asList(Supported,    NotSupported,    ReviewSemantics,    ReviewPerformance,    ReviewManually,    Ignored, ObjCountOnly);
	// values for default_classification in .cfg file:
	public List<String> supportOptionsCfgFile = Arrays.asList("Supported", "NotSupported",  "ReviewSemantics",  "ReviewPerformance",  "ReviewManually",  "Ignored", ObjCountOnly);
	public List<String> validSupportOptionsCfgFile = Arrays.asList("NOTSUPPORTED",  "REVIEWSEMANTICS",  "REVIEWPERFORMANCE",  "REVIEWMANUALLY", "IGNORED");
	// keys for default_classification in .cfg file, e.g. '-ReviewSemantics':
	public List<String> defaultClassificationsKeys = Arrays.asList("DEFAULT_CLASSIFICATION-REVIEWSEMANTICS", "DEFAULT_CLASSIFICATION-REVIEWPERFORMANCE",
			"DEFAULT_CLASSIFICATION-REVIEWMANUALLY", "DEFAULT_CLASSIFICATION-IGNORED", "DEFAULT_CLASSIFICATION");

	// display values
	public List<String> supportOptionsDisplay = Arrays.asList("Supported", "Not Supported", "Review Semantics", "Review Performance", "Review Manually", "Ignored", ObjCountOnly);

	// default weight factors for computing compatibility %age, corresponding to each option value in the list above
	public List<Integer> supportOptionsWeightDefault = Arrays.asList(100,   // Supported
	                                                                 200,   // NotSupported
	                                                                 150,    // ReviewSemantics
	                                                                 150,    // ReviewPerformance
	                                                                 150,    // ReviewManually
	                                                                 0,     // Ignored
	                                                                 0      // ObjCountOnly, not applicable
	                                                                );
	public final String WeightedStr = "Weighted";

	// user-defined weight factors
	Map<String, Integer> userWeightFactor = new HashMap<String, Integer>();

	// used in both CompassAnalyze and for reporting, need to be same string
	static final String Datatypes           = "Datatypes";
	static final String UDDatatypes         = "User-Defined Datatypes";
	static final String DatatypeConversion  = "Datatype conversion";

	// overall compatibility %age
	String compatPctStr = uninitialized;
	String compatPctStrRaw = uninitialized;

	// flag
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
		return instance;
	}

    public void getPlatform () {
		onWindows = false;
		if (System.getProperty("os.name").startsWith("Windows")) {
			onWindows = true;
			thisProgExec = thisProgExecWindows;
		}
		else {
			thisProgExec = thisProgExecLinux;
		}

		if (System.getenv().containsKey("COMPASS_DEVELOP")) {
			devOptions = true;
		}
    }

	// for debugging, and for launching the window with the final report
    public void runOScmd (String cmd) throws IOException {
	ProcessBuilder builder;
	if (onWindows) {
	        builder = new ProcessBuilder("cmd.exe", "/c", cmd );
	    }
	    else {
	        builder = new ProcessBuilder("bash", "/c", cmd );
	    }
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String cmdOutput;
        while (true) {
            cmdOutput = r.readLine();
            if (cmdOutput == null) { break; }
            System.out.println(cmdOutput);
        }
    }

    private void forceGC (){
	// for large input sets, we may run out of memory
	// calling this seems to help a bit, though no guarantees
	System.gc ();
		System.runFinalization ();
    }

	public String escapeRegexChars(String s)
	{
	    Matcher m = regexSpecialCharsPatt.matcher(s);
	    return m.replaceAll("\\\\$0");
	}

	public boolean wordAndBoundary(String s, String prefix)
	{
		int len = prefix.length();
		return s.startsWith(prefix) && (s.length() == len || PatternMatches(Character.toString(s.charAt(len)), "\\W"));
	}

	public String getPatternGroup(String s, String patt, int groupNr)
	{
		Pattern p = Pattern.compile(patt, Pattern.CASE_INSENSITIVE);
		return getPatternGroup(s, p, groupNr, MatchMethod.FIND);
	}

	public String getPatternGroup(String s, Pattern p, int groupNr, MatchMethod matchMethod)
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
		if (options.contains("case_sensitive")) {
			p = Pattern.compile(patt);
		}
		else {
			p = Pattern.compile(patt, Pattern.CASE_INSENSITIVE);
		}
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

	public String applyPatternAll(String s, String patt, String replace) {
		return applyPattern(s, patt, replace, "");
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
	public String removeLastChar(String s) {
	    return removeLastChars(s, 1);
	}

	public String removeLastChars(String s, int nrChars) {
	    return s.substring(0, s.length() - nrChars);
	}

	public String capitalizeFirstChar(String s) {
		if (s.length() == 0) return s;
	    return s.substring(0,1).toUpperCase() + s.substring(1);
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

	public void listToUpperCase(List<String> thisList) {
		for(int i=0; i<thisList.size(); i++) {
			thisList.set(i, thisList.get(i).toUpperCase());
	}
	}

	public void listToLowerCase(List<String> thisList) {
		for(int i=0; i<thisList.size(); i++) {
			thisList.set(i, thisList.get(i).toLowerCase());
	}
	}

	public String reverseString(String s) {
		s = new StringBuilder(s).reverse().toString();
		String tmp = "~~!~=tmp=String=Babelfish~!~~";  // should not occur in actual data
		s = s.replaceAll("\\(", tmp);
		s = s.replaceAll("\\)", "(");
		s = s.replaceAll(tmp, ")");
		return s;
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

	// align datatypes with a length specifier
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
			List<String> typeLines = new ArrayList<String>();
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
		return composeSeparatorBar(s, "-");
	}
	public String composeSeparatorBar(String s, String filler) {
		if (filler.isEmpty()) filler = "-";
		String s2 = "";
		s2 += composeOutputLine("", "-") + "\n";
		s2 += composeOutputLine("--- "+s+" ", "-") + "\n";
		s2 += composeOutputLine("", "-") + "\n";
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
		appOutput(s.toString(), false);
	}
	public void appOutput(String s) {
		appOutput(s, false);
	}
	public void appOutput(String s, boolean inReport) {
		System.out.println(s);
		if (sessionLogWriter != null) {
			try { writeSessionLogFile(s + "\n"); } catch (Exception e) { System.out.println("Error writing to "+ sessionLogPathName); }
		}
		if (inReport) {
			try { writeReportFile(s); } catch (Exception e) { System.out.println("Error writing to "+ reportFilePathName); }
		}
	}

	public void setDebugFlags()
	{
		debugging = true;
		if (specifiedDbgOptions.contains("all")) {
			debugBatch = debugPtree = debugCfg = debugDir = debugSymtab = debugReport = debugCalc = true;
			return;
		}
		if (specifiedDbgOptions.contains("batch")) {
			debugBatch = true;
		}
		if (specifiedDbgOptions.contains("ptree")) {
			debugPtree = true;
		}
		if (specifiedDbgOptions.contains("cfg")) {
			debugCfg = true;
		}
		if (specifiedDbgOptions.contains("dir")) {
			debugDir = true;
		}
		if (specifiedDbgOptions.contains("symtab")) {
			debugSymtab = true;
		}
		if (specifiedDbgOptions.contains("report")) {
			debugReport = true;
		}
		if (specifiedDbgOptions.contains("calc")) {
			debugCalc = true;
		}
	}

	public void dbgOutput(String s, boolean toDebug) {
		if (toDebug) {
			System.out.println("DEBUG: " + s);
			if (sessionLogWriter != null) {
				try { writeSessionLogFile("DEBUG: " + s +"\n"); } catch (Exception e) { System.out.println("Error writing to "+ sessionLogPathName); }
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

	public String nameFormatValid (String nameType, String name) {
		String result = "";
		if (nameType.equals("report") || nameType.equals("appname")) {
			// check for characters not allowed in report/app name
			// first remove slashes
			if (name.contains("\\")) {
				result = "'\\'";
			}
			else if (name.contains("/")) {
				result = "'/'";
			}
			else {
				// other not-allowed chars
				String badChar = getPatternGroup(name, "("+fileNameCharsAllowed+")", 1);
				if (!badChar.isEmpty()) {
					result = "["+badChar + "]  (allowed characters: [A-Za-z0-9\\.-()])";
				}
				else if (name.contains("..\\")) {
					result = "'..\\'";
				}
				else if (name.contains("../")) {
					result = "'../'";
				}
			}
		}
		return result;
	}

	// doc dir pathname: %USERPROFILE% on Windows, /home/<user> on Linux
	// ToDo: test on Linux/Mac
    public String getDocDirPathname() {
		String dirPath = System.getProperty("user.home") + File.separator + "Documents" + File.separator + BabelfishCompassFolderName;
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

	// capture file pathname
    public String getCaptureFilePathname(String reportName, String fileName, String appName) {
		String f = Paths.get(fileName).getFileName().toString();
		f = captureFileName + "." + f + "." + appName + "." + captureFileSuffix;
		String filePath = getFilePathname(getReportDirPathname(reportName, capDirName), f);
		return filePath;
	}

	// session log pathname
    public String getSessionLogPathName(String reportName, String now) {
	String sessionLogName = "session-log-" + reportName + "-" + now + ".txt";
	if (stdReport) { // development only
		sessionLogName = "session-log.txt";
	}
		return getFilePathname(getReportDirPathname(reportName, logDirName), sessionLogName);
	}

	// report file pathname
    public String getReportFilePathname(String reportName, String now) {
	String reportNameFull = "report-" + reportName + "-" + now + ".txt";
	if (stdReport) { // development only
		reportNameFull = "report.txt";
	}
		return getFilePathname(getReportDirPathname(reportName), reportNameFull);
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
						appOutput("Created " + dirPath);
					}
				}
				else {
					// sometimes this fails for no apparent reason; unclear why. Wait a little while and retry
					try {
						Thread.sleep(500); /* argument=millisecs */
					} catch (Exception e) {
					}
					if (docDir.mkdirs()) {
						if (debugging) dbgOutput("Created (after retry)" + dirPath, debugDir);
						if (echoCreate) {
							appOutput("Created " + dirPath);
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
		//appOutput(thisProc()+"filePattern=["+filePattern+"] dir=["+dir+"] fileList=["+fileList+"] ");
		fileList.remove(dirPath);
		//printStackTrace();
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

	public String openErrBatchFile(String reportName, String inputFileName, String runStartTime) throws IOException {
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
		f += "." + symTabFileTag + "." + appName + "." + symTabFileSuffix;
		String dirPath = getReportDirPathname(reportName, importDirName, symTabDirName);
		f = getFilePathname(dirPath, f);
		return f;
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

	public String getImportFilePathName(String reportName, String inputFileName, String appName) throws IOException {
		String f = Paths.get(inputFileName).getFileName().toString();
		f += "." + importFileTag + "." + appName + "." + importFileSuffix;
		String dirPath = getReportDirPathname(reportName, importDirName);
		f = getFilePathname(dirPath, f);
		return f;
	}

	public String openImportFile(String reportName, String inputFileName, String appName, String encoding) throws IOException {
		Path fullPath = Paths.get(inputFileName).toAbsolutePath();
		importFilePathName = getImportFilePathName(reportName, inputFileName, appName);
		checkDir(getReportDirPathname(reportName, importDirName), true);
		importFileWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(importFilePathName), StandardCharsets.UTF_8)));
		String now = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());
		String initLine = importFileLinePart1 +"["+fullPath.toString()+"]"+importFileLinePart2+"["+appName+"]" + importFileLinePart3 +"["+encoding+"]" + importFileLinePart4 +"["+importFileNrBatchesPlaceholder+"/"+importFileNrLinesPlaceholder+"]" + importFileLinePart5 + now;
		writeImportFile(initLine);
		return importFilePathName;
	}

	public void writeImportFile(String line) throws IOException {
		importFileWriter.write(line + "\n");
		importFileWriter.flush();
	}

    public void closeImportFile() throws IOException {
	    importFileWriter.close();
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
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
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

	// validate all capture files for this report
	// if valid, returns an empty string
	// if invalid, return message why it's invalid
    public String captureFilesValid(List<Path> captureFiles) throws IOException {
	String result = "";
	String tmp = "";
	boolean identicalTargetVersion = true;
	boolean otherwiseInvalid = false;
	String targetVersionTest = null;
		for (Path cf: captureFiles) {
			String line = captureFileFirstLine(cf.toString());   // read only first line
			String reportName     = captureFileAttribute(line, 1);
			String tgtVersion     = captureFileAttribute(line, 2);
			String dt             = captureFileAttribute(line, 3);

			if (targetVersionTest == null) {
				targetVersionTest = tgtVersion;
			}
			if (!targetVersionTest.equals(tgtVersion)) {
				identicalTargetVersion = false;
			}

			if (tgtVersion.isEmpty()) {
				otherwiseInvalid = true;
				tmp += "   Missign header line? No targeted "+babelfishProg+" version "+tgtVersion+ " in "+cf.toString()+"\n";
			}
			else {
				tmp += "   version "+tgtVersion+ " is target of report "+reportName+"("+cf.toString()+")\n";
			}
		}

		if (!targetVersionTest.equals(targetBabelfishVersion)) {
			result = "Analysis was performed for a different "+babelfishProg+" version than targeted by this run (v."+targetBabelfishVersion+"):\n";
		}
		if (!identicalTargetVersion) {
			result = "Analysis files are for different "+babelfishProg+" versions:\n";
		}
		if (otherwiseInvalid) {
			result = "Invalid analysis file(s) found:\n";
		}
		if (!result.isEmpty()) {
			result = "\nCannot generate report based on existing analysis files.\n" + result;
			result += tmp;
			result += "\nRe-run analysis for all imported files with -reanalyze";
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
				if (inputFiles.size() > 0) {
					// must specify at least one of the flags
					appOutput("Report '" + reportName + "' already exists (" + reportDir.toString() + ")");
					appOutput("Specify -add to add the input file(s) to this report");
					return true;
				}
				else {
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

			return false;
		}
		else {
			if (debugging) dbgOutput(thisProc() + "reportDir does not exist:[" + reportDir + "] ", debugDir);
			checkDir(getReportDirPathname(reportName), false, true);

			// create subdirs
			checkDir(getReportDirPathname(reportName, importDirName), false);
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
		checkDir(getReportDirPathname(reportName, capDirName), false);
	}

	public String openSessionLogFile(String reportName, String now) throws IOException {
		sessionLogPathName = getSessionLogPathName(reportName, fixNameChars("report", now));
		checkDir(getReportDirPathname(reportName, logDirName), true);
		sessionLogWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(sessionLogPathName), StandardCharsets.UTF_8)));
		return sessionLogPathName;
	}

	public void writeSessionLogFile(String line) throws IOException {
		sessionLogWriter.write(line);
		sessionLogWriter.flush();
	}

    public void closeSessionLogFile() throws IOException {
	    sessionLogWriter.close();
	}

	// Try to detect the encoding of the input file.
	// In particular, check for UTF8/UTF16/UTF32 by lookign at the BOM bytes: SQL Server Mgmt Studio generates UTF16LE by default.
	// In case the detected encoding is different from the system default (as in Charset.defaultCharset()), return the name of the encoding.
	public String detectEncoding(String fileName) throws IOException {
		BufferedReader inFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		StringBuilder bomSB = new StringBuilder(4);
		int readValue;

		while((readValue = inFileReader.read()) != -1 && bomSB.length() < 4) {
			char content = (char) readValue;
			bomSB.append(content);
		}

		inFileReader.close();
		String bom = bomSB.toString();

		if (bom.startsWith("\u00EF\u00BB\u00BF")) {
			return "UTF-8";
		}
		if (bom.startsWith("\u00FE\u00FF")) {
			return "UTF-16"; // UTF-16BE
		}
		if (bom.equals("\u0000\u0000\u00FE\u00FF")) {
			return "UTF-32"; // UTF-32BE
		}
		if (bom.equals("\u00FF\u00FE\u0000\u0000")) {
			return "UTF-32"; // UTF-32LE
		}
		if (bom.startsWith("\u00FF\u00FE")) {
			return "UTF-16"; // UTF-16LE
		}
		return null;
	}

	// validate an input file: check minimum requirements, and figure out if this was created
	// by an unsupported reveng tool
	public boolean inputScriptValid(String inputFileName)
	{
		// to be written
		return true;
	}

	// capture file
    public void openCaptureFile(String reportName, String fileName, String appName) throws IOException {
	captureFilePathName = getCaptureFilePathname(reportName, fileName, appName);
	checkDir(getReportDirPathname(reportName, capDirName), true);
		captureFileWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(captureFilePathName), StandardCharsets.UTF_8)));
		String now = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());
		String initLine = captureFileLinePart1+"["+reportName+"]" + captureFileLinePart2 +"["+targetBabelfishVersion+"]" + captureFileLinePart3 + now;
		appendCaptureFile(initLine);
	}

    public void closeCaptureFile() throws IOException {
	    captureFileWriter.close();
	}

	// append line to the capture file
    public void appendCaptureFile(String itemLine) throws IOException {
	    captureFileWriter.write(itemLine+"\n");
	    captureFileWriter.flush();
	}

	// get attribute from imported file first line
    public String captureFileAttribute(String line, int part) throws IOException {
	assert (part >= 1 && part <= 3): "invalid part value ["+part+"] ";
	return getPatternGroup(line, "^"+captureFileLinePart1+"[\\[](.*?)[\\]]"+captureFileLinePart2+"[\\[](.*?)[\\]]"+captureFileLinePart3+"(.*)$", part);
    }

	// read capture file first line
    public String captureFileFirstLine(String fileName) throws IOException {
		FileInputStream fis = new FileInputStream(fileName);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
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

	// extract object name from identifier
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

	// extract schema from identifier
	public String getSchemaNameFromID(String ID) {
		String schemaName = "";
		if (!ID.contains(".")) {
			return schemaName;
		}
		List<String> parts = new ArrayList<String>(Arrays.asList(ID.split("\\.")));
		schemaName = parts.get(parts.size()-2);
		return schemaName;
	}

	// extract DB from identifier
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

	// normalize a namee?
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
		name = stripDelimitedIdentifier(name.toUpperCase());
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
	public String resolveName(String objName)
	{
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
			if (!currentObjectType.equals(BatchContext)) {
				tmpSchema = getSchemaNameFromID(currentObjectName);
			}
			if (tmpSchema.isEmpty()) {
				tmpSchema = "DBO";  // ToDo: we can keep track of the current schema that would apply?
			}
			objName = tmpSchema + objName;
		}
		if (tmpDB.isEmpty()) {
			// currentDatabase can be blank if no USE stmt seen
			objName = currentDatabase + "." + objName;
		}
		return objName;
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

	// is the specifie dname a UDD?
	public String isUDD(String uddName)
	{
		uddName = resolveName(uddName.toUpperCase());
		if (UDDSymTab.containsKey(uddName)) {
			return UDDSymTab.get(uddName);
		}
		else {
			return "";
		}
	}

	// write symbol table
	public void writeSymTab(String reportName, String inputFileName, String appName) throws IOException {
		checkDir(getReportDirPathname(reportName, importDirName, symTabDirName), true);
		symTabFilePathName = getSymTabFilePathName(reportName, inputFileName, appName);
		if (debugging) dbgOutput("symTabFilePathName=[" + symTabFilePathName + "] ", debugSymtab);
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
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
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
		return s;
	}

	// clear the symbol table
	public static void clearSymTab()
	{
		objTypeSymTab.clear();
		SUDFSymTab.clear();
		TUDFSymTab.clear();
		UDDSymTab.clear();
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
			if (!currentObjectType.equals(BatchContext)) {
				currentObjectTypeSub = objType;
				currentObjectNameSub = objName;
				subContext = true;
				//u.dbgOutput("Setting sub context to ["+currentObjectTypeSub+"], ["+currentObjectNameSub+"]");
			}
		}
		if (!subContext) {
			currentObjectType = objType;
			currentObjectName = objName;
			//u.dbgOutput("Setting context to ["+currentObjectType+"], ["+currentObjectName+"]");
		}
	}
	public void resetSubContext() {
		if (currentObjectTypeSub != "") {
			currentObjectTypeSub = "";
			currentObjectNameSub = "";
			//u.dbgOutput("clearing sub context, main context still ["+currentObjectType+"], ["+currentObjectName+"]");
		}
		else {
			// ignore
		}
	}

	public void writeReportFile(StringBuilder line) throws IOException {
		reportFileWriter.write(line.toString() + "\n");
		reportFileWriter.flush();
	}

	public void writeReportFile(String line) throws IOException {
		reportFileWriter.write(line + "\n");
		reportFileWriter.flush();
	}

	public void writeReportFile() throws IOException {
		reportFileWriter.write("\n");
		reportFileWriter.flush();
	}

	public String progressCnt(int currentCount, int totalCount) {
		assert currentCount >= 1 : "currentCount must be >= 1";
		assert totalCount >= 1 : "totalCount must be >= 1";
		if (totalCount == 1) return "";
		return "("+currentCount+"/"+totalCount+") ";
	}

	public void reportSummaryItems(String status, List<String> sortedList, Map<String, Integer> itemCount, Map<String, String>appItemList) throws IOException {
		StringBuilder lines = new StringBuilder();
		StringBuilder prevGroup = new StringBuilder(uninitialized);
		final String statsMarker = "~STATSHERE~";

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
			itemCnt.put(item.toString(),0);
			StringBuilder thisItem = new StringBuilder(lineIndent + item +" : " + itemCount.get(s).toString());
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
			writeReportFile(composeOutputLine("", "-"));
			writeReportFile(composeOutputLine("--- SQL features '"+supportOptionsDisplay.get(supportOptions.indexOf(status))+"' in " + babelfishProg +" v." + targetBabelfishVersion + " ", "-"));
			writeReportFile(composeOutputLine("", "-"));
			if (status.equals(ReviewManually) && writeNote) {
				writeReportFile("Note: Items in this section could not be assessed automatically");
			}
			writeReportFile();
			writeReportFile(lines);
		}
	}

	public void reportXrefByFeature(String status, List<String> sortedList) throws IOException {
		StringBuilder lines = new StringBuilder(doXrefMsg(status, "feature"));
		Integer skippedFilter = 0;
		Integer countFilter = 0;

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
			Integer itemCount = 0;
			boolean initLineNr = false;

			for (String s: sortedList) {
				if ((!s.startsWith(status)) && (!s.startsWith(lastItem))) continue;
				if (debugging) dbgOutput(thisProc()+"s=["+s+"] ", debugReport);

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
				if (debugging) dbgOutput(thisProc()+"itemSort=["+itemSort+"]  srcFile=["+srcFile+"] ", debugReport);

				if (!s.startsWith(lastItem)) {
					if (!reportOptionFilter.isEmpty()) {
						String filter = "^.*"+reportOptionFilter+".*$";
						countFilter++;
						if (debugging) dbgOutput(thisProc()+"filter: item=["+item.toString()+"]  reportOptionFilter=["+reportOptionFilter+"] ", debugReport);
						if (PatternMatches(item.toString(), filter)) {
							// keep it
							if (debugging) dbgOutput(thisProc()+"matching filter - keeping", debugReport);
						}
						else {
							// skip it
							if (debugging) dbgOutput(thisProc()+"no match with filter - skipping", debugReport);
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
						linesTmp = new StringBuilder(completeLine(linesTmp, lineNrs, prevContext, prevBatchNr, prevLineNrInFile, prevSrcFile, prevAppName));
						lineNrs.clear();
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

				contextSort = new StringBuilder(createSortKey(context.toString(),batchNr.toString()));
				if (debugging) dbgOutput(thisProc()+"contextSort=["+contextSort+"] ", debugReport);
				if (debugging) dbgOutput(thisProc()+"prevContextSort=["+prevContextSort+"] ", debugReport);
				if (contextSort.toString().equalsIgnoreCase(prevContextSort.toString())) {
					if (!initLineNr) {
						// same line, accumulate line nrs
						lineNrs.add(lineNr.toString());
						if (debugging) dbgOutput(thisProc()+"adding line number to list: ["+lineNr+"] total=["+lineNrs.size()+"] x=["+String.join(", ", lineNrs)+"] ", debugReport);
					}
					initLineNr = false;
				}
				else {
					if (linesTmp.length() > 0) {
						// complete previous line
						linesTmp = new StringBuilder(completeLine(linesTmp, lineNrs, prevContext, prevBatchNr, prevLineNrInFile, prevSrcFile, prevAppName));
						if (debugging) dbgOutput(thisProc()+"new line, changed context, completing current line completed linesTmp=["+linesTmp+"] ", debugReport);
					}

					// new line
					lineNrs.clear();
					lineNrs.add(lineNr.toString());
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

		String filterMsg = "";
		if (skippedFilter > 0) {
			filterMsg = "Filter applied: "+skippedFilter.toString()+" of " + countFilter.toString()+" items skipped by filter '"+reportOptionFilter+"'\n\n";
		}

		if (lines.length() == 0) lines = new StringBuilder("-no items to report-\n");
		writeReportFile();
		writeReportFile(composeSeparatorBar("X-ref: '"+supportOptionsDisplay.get(supportOptions.indexOf(status))+"' SQL Features, to objects"));
		writeReportFile(filterMsg+lines);
	}

	public void reportXrefByObject(String status, List<String> sortedList) throws IOException {
		StringBuilder lines = new StringBuilder(doXrefMsg(status, "object"));
		Integer skippedFilter = 0;
		Integer countFilter = 0;

		if (doXref(status, "object")) {
			lines = new StringBuilder();
			StringBuilder hdr = new StringBuilder();
			StringBuilder contextSort = new StringBuilder();
			StringBuilder prevContextSort = new StringBuilder();

			StringBuilder group = new StringBuilder();
			StringBuilder item = new StringBuilder();
			StringBuilder itemDetail = new StringBuilder();
			StringBuilder context = new StringBuilder();
			StringBuilder lineNr = new StringBuilder();
			StringBuilder batchNr = new StringBuilder();
			StringBuilder lineNrSort = new StringBuilder();
			StringBuilder lineNrInFile = new StringBuilder();
			StringBuilder srcFile = new StringBuilder();
			StringBuilder appName = new StringBuilder();

			boolean init = false;

			for (String s: sortedList) {
				if ((!s.startsWith(status)) && (!s.startsWith(lastItem))) continue;
				if (debugging) dbgOutput(thisProc()+"s=["+s+"] ", debugReport);

				List<String> sortedFields = new ArrayList<String>(Arrays.asList(s.split(sortKeySeparator)));
				context = new StringBuilder(sortedFields.get(1));
				appName = new StringBuilder(sortedFields.get(2));
				srcFile = new StringBuilder(getSrcFileNameMap(sortedFields.get(3)));
				lineNrSort = new StringBuilder(sortedFields.get(4));
				group = new StringBuilder(sortedFields.get(5).substring(groupSortLength));
				item = new StringBuilder(sortedFields.get(6));
				lineNr = new StringBuilder(sortedFields.get(7));
				batchNr = new StringBuilder(sortedFields.get(8));
				lineNrInFile = new StringBuilder(sortedFields.get(9));
				if (context.toString().equals(BatchContextLastSort)) context = new StringBuilder(BatchContext);

				contextSort = new StringBuilder(createSortKey(context.toString(),appName.toString(),srcFile.toString(),lineNrSort.toString()));
				if (debugging) dbgOutput(thisProc()+"contextSort=["+contextSort+"] ", debugReport);

				if (!s.startsWith(lastItem)) {
					if (!reportOptionFilter.isEmpty()) {
						String filter = "^.*"+reportOptionFilter+".*$";
						countFilter++;
						if (debugging) dbgOutput(thisProc()+"filter: item=["+item.toString()+"]  reportOptionFilter=["+reportOptionFilter+"] ", debugReport);
						if (PatternMatches(item.toString(), filter)) {
							// keep it
							if (debugging) dbgOutput(thisProc()+"matching filter - keeping", debugReport);
						}
						else {
							// skip it
							if (debugging) dbgOutput(thisProc()+"no match with filter - skipping", debugReport);
							skippedFilter++;
							continue;
						}
					}
				}

				if (s.startsWith(lastItem)) {
					break;
				}

				if (!contextSort.toString().equalsIgnoreCase(prevContextSort.toString())) {
					if (!init) init = true;
					else lines.append("\n");

					lines.append(context);
					if (!contextSort.toString().equalsIgnoreCase(BatchContext)) {
						lines.append(", batch ");
					}
					else {
						lines.append(", ");
					}
					lines.append(batchNr.toString()+ " at line " + lineNrInFile);
					if (reportShowSrcFile) lines.append(" in "+ srcFile);
					else lines.append(" in input file");
					if (reportShowAppName) lines.append(", app "+ appName);
					lines.append("\n");

					prevContextSort = new StringBuilder(contextSort);
					if (s.startsWith(lastItem)) {
						break;
					}
				}

				lines.append(lineIndent+item.toString()+" ("+group.toString()+") : line "+lineNr.toString()+"\n");
				prevContextSort = new StringBuilder(contextSort);

			}
		}

		String filterMsg = "";
		if (skippedFilter > 0) {
			filterMsg = "Filter applied: "+skippedFilter.toString()+" of " + countFilter.toString()+" items skipped by filter '"+reportOptionFilter+"'\n\n";
		}

		if (lines.length() == 0) lines = new StringBuilder("-no items to report-\n");
		writeReportFile();
		writeReportFile(composeSeparatorBar("X-ref: objects, to '"+supportOptionsDisplay.get(supportOptions.indexOf(status))+"' SQL Features"));
		writeReportFile(filterMsg+lines);
	}

	private StringBuilder completeLine(StringBuilder linesTmp, List<String> lineNrs, StringBuilder prevContext, StringBuilder prevBatchNr, StringBuilder prevLineNrInFile, StringBuilder srcFile, StringBuilder appName) {
		linesTmp.append(makeLineNrList(lineNrs));

		//if (!prevContext.toString().equalsIgnoreCase(BatchContext)) linesTmp.append(" (");
		//else linesTmp.append(" ");
		linesTmp.append(" in batch "+ prevBatchNr.toString() + " (at line " + prevLineNrInFile+")");

		if (reportShowSrcFile) linesTmp.append(" "+ srcFile);
		else linesTmp.append(" in input file");
		if (reportShowAppName) linesTmp.append(", app "+ appName);

		linesTmp.append("\n");
		return linesTmp;
	}

	private boolean doXref(String status, String type) {
		boolean doIt = false;
		if (reportOptionXref.contains("all") || reportOptionXref.contains(type)) {
			if (reportOptionStatus.isEmpty()) {
				if (status.equals(NotSupported) ||
				    status.equals(ReviewSemantics) ||
				    status.equals(ReviewManually) ||
				    status.equals(ReviewPerformance))
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

	private String makeLineNrList(List<String> lineNrs) {
		int nrLineNrs = lineNrs.size();
		String xtra = "";
		if (maxLineNrsInList < nrLineNrs) {
			xtra = " (+"+Integer.toString(nrLineNrs-maxLineNrsInList)+" more)";
			nrLineNrs = maxLineNrsInList;
		}
		String joined = String.join(", ", lineNrs.subList(0,nrLineNrs));
		return joined.trim() + xtra;
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
		if (reportGroupSortAdjustment.get(group.toUpperCase()) != null) sortGroup = reportGroupSortAdjustment.get(group.toUpperCase());
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
		tmp.put(CompassAnalyze.DatatypeConversion, 930);
		tmp.put(CompassAnalyze.XMLReportGroup, 930);
		tmp.put(CompassAnalyze.JSONReportGroup, 930);
		tmp.put(CompassAnalyze.HIERARCHYIDReportGroup, 930);
		tmp.put(CompassAnalyze.DatatypeConversion, 930);
		tmp.put(CompassAnalyze.UDDatatypes, 940);
		tmp.put(CompassAnalyze.Datatypes, 950);

		// map keys to uppercase
		for(String k: tmp.keySet()) {
			reportGroupSortAdjustment.put(k.toUpperCase(), tmp.get(k));
		}
	}

	public boolean createReport(String reportName) throws IOException {
		if (debugging) dbgOutput(thisProc()+"reportOptionXref=["+reportOptionXref+"] ", debugReport);
		if (debugging) dbgOutput(thisProc()+"reportOptionStatus=["+reportOptionStatus+"] ", debugReport);
		if (debugging) dbgOutput(thisProc()+"reportOptionDetail=["+reportOptionDetail+"] ", debugReport);
		if (debugging) dbgOutput(thisProc()+"reportOptionApps=["+reportOptionApps+"] ", debugReport);
		if (debugging) dbgOutput(thisProc()+"reportOptionFilter=["+reportOptionFilter+"] ", debugReport);

		Date now = new Date();
		String now_fname = new SimpleDateFormat("yyyy-MMM-dd-HH.mm.ss").format(now);
		String now_report = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss").format(now);
		reportFilePathName = getReportFilePathname(reportName, now_fname);
		appOutput("");
		appOutput("Generating report " + reportFilePathName + "...");

		String line = "";
		forceGC();

		reportFileWriter = new BufferedWriter((new OutputStreamWriter(new FileOutputStream(reportFilePathName), StandardCharsets.UTF_8)));

		String hdrLine = "Report for: " + reportName + " : Generated at " + now_report;
		writeReportFile(hdrLine);
		writeReportFile(stringRepeat("-", hdrLine.length()));

		writeReportFile("\n" + thisProgName + " version " + thisProgVersion);
		writeReportFile(thisProgNameLong);
		writeReportFile(copyrightLine + "\n");
		writeReportFile(disclaimerMsg + "\n");
		writeReportFile(composeOutputLine("--- Report Setup ", "-"));
		writeReportFile(reportHdrLines);
		writeReportFile("This file                  : "+reportFilePathName);
		writeReportFile(composeOutputLine("", "=") + "\n");


		Map<String, Integer> appCount = new HashMap<>();
		Map<String, Integer> srcFileCount = new HashMap<>();
		Map<String, Integer> objTypeCount = new HashMap<>();
		Map<String, Integer> objTypeLineCount = new HashMap<>();
		int linesSQLInObjects = 0;
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
				appOutput("No analysis files found. Use -reanalyze to perform analysis and generate a report.");
			}
			errorExit();
		}
		String cfv = captureFilesValid(captureFiles);
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
			if (cfReportName.isEmpty()) {
				appOutput("Invalid format in "+cfReportName+"; run with -reanalyze to fix.");
				errorExit();
			}
			if (!reportName.equalsIgnoreCase(cfReportName)) {
				String rDir = getFilePathname(getDocDirPathname(), capDirName);
				appOutput("Found analysis file for report '" + cfReportName + "' in " + rDir + " -- including contents in this report");
			}

			FileInputStream cfis = new FileInputStream(new File(cf.toString()));
			InputStreamReader cfisr = new InputStreamReader(cfis, "UTF-8");
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
				if (debugging) dbgOutput(thisProc() + "capLine=[" + capLine + "]", debugReport);

				List<String> itemList = new ArrayList<String>(Arrays.asList(capLine.split(captureFileSeparator)));
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
					if (debugging) dbgOutput(thisProc() + "objType=[" + objType + "] ", debugReport);

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
						if ((!objType.equals("constraint column DEFAULT")) && (!objType.equals("constraint PRIMARY KEY/UNIQUE"))) {
							objType = applyPatternFirst(objType, "^(.*?,.*?),.*$", "$1");
							if (objType.startsWith("TRIGGER,")) objType = "TRIGGER";
							objType = objType.replaceFirst(", external", "");
							objType = objType.replaceFirst(", CLUSTERED", "");
							if (objType.contains(captureFileSeparatorMarker))
								objType = getPatternGroup(objType, "^(.*?)\\s*\\b\\w*" + captureFileSeparatorMarker + ".*$", 1);       // for proc versioning
							objType = objType.trim();
							objTypeCount.put(objType, objTypeCount.getOrDefault(objType, 0) + 1);
							if (debugging) dbgOutput(thisProc() + "counting objType=[" + objType + "] ", debugReport);
							int loc = 0;
							if (!misc.isEmpty()) loc = Integer.parseInt(misc);
							objTypeLineCount.put(objType, objTypeLineCount.getOrDefault(objType, 0) + loc);  // misc contains #lines for procedural CREATE object stmts
							linesSQLInObjects += loc;
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
				if (debugging) dbgOutput(thisProc() + "status=[" + status + "] val=[" + statusCount.getOrDefault(status, 0L) + "]  sw=[" + sw + "] weighted=[" + weighted + "] weightFactor=[" + weightFactor + "] ", debugReport);


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

					lineNrSort = String.format("%08d", Integer.parseInt(lineNrInFile));
					if (context.equals(BatchContext)) context = BatchContextLastSort;
					sortKey = createSortKey(status,context,appName,addSrcFileNameMap(srcFile),lineNrSort,itemGroupSort,item,lineNr,batchNr,lineNrInFile);
					xRefByObject.add(sortKey);
					sortSizeXRefByObject += sortKey.length();
				}
			}
			capFile.close();

			if (debugging) dbgOutput(thisProc()+"capCount=["+capCount+"] sortCnt="+itemCount.size()+" sortSizeSummary KB=["+sortSizeSummary/1024+"] ", debugReport);
			if (debugging) dbgOutput(thisProc()+"capCount=["+capCount+"] sortCnt="+xRefByFeature.size()+" sortSizeXRefByFeature KB=["+sortSizeXRefByFeature/1024+"]", debugReport);
			if (debugging) dbgOutput(thisProc()+"capCount=["+capCount+"] sortCnt="+xRefByObject.size()+" sortSizeXRefByObject KB=["+sortSizeXRefByObject/1024+"]", debugReport);
		}

		// reporting options
		if (Compass.inputFiles.size() <= 1) reportShowSrcFile = false;
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

		summarySection.append(composeSeparatorBar("Applications Analyzed (" + appCount.size() + ")"));
		for (String app : appCount.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList())) {
			int loc = appCount.get(app);
			summarySection.append(lineIndent + " " + app + " (" + appCount.get(app) + " lines SQL)\n");
		}
		summarySection.append("\n");

		linesSQLInReport = totalLinesDDL;

		summarySection.append(composeSeparatorBar("Assessment Summary"));
		statusCount.put("linesDDL", Long.valueOf(totalLinesDDL));
		statusCount.put(fmtLinesSQL, Long.valueOf(linesSQLInObjects));
		statusCount.put(fmtBatches, Long.valueOf(totalBatches));
		statusCount.put("inputfiles", Long.valueOf(srcFileCount.size()));
		statusCount.put("apps", Long.valueOf(appCount.size()));
		statusCount.put("invalid syntax", Long.valueOf(totalErrorBatches)); // #batches with parse errors
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
				xtra = lineIndent + "(compatibility weight factor: " + w + "%)";
			}
			summaryTmp2.append(lineIndent).append(fmtStatusDisplay.get(i) + " : " + statusCount.get(reportItem) + xtra + "\n");
		}
		summaryTmp2 = new StringBuilder(alignColumn(summaryTmp2, " : ", "before", "left"));
		summaryTmp2 = new StringBuilder(alignColumn(summaryTmp2, " : ", "after", "right"));
		summarySection.append(summaryTmp2);
		summarySection.append("\n");

		// calc %age
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


		summarySection.append(composeSeparatorBar("Compatibility Estimate"));
		summarySection.append("Estimated compatibility for " + babelfishProg + " v." + targetBabelfishVersion + " : " + compatPctStr + "%" + "\n");
		summarySection.append(customWeightsMsg);
		summarySection.append("\n");

		summarySection.append(composeSeparatorBar("Object Count"));
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
			summaryTmp.append(lineIndent).append(objType + " : " + objTypeCount.get(objType) + locStr.toString() + "\n");
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
		forceGC();

		reportSummaryItems(NotSupported, sortedList, itemCount, appItemList);
		reportSummaryItems(ReviewManually, sortedList, itemCount, appItemList);
		reportSummaryItems(ReviewSemantics, sortedList, itemCount, appItemList);
		reportSummaryItems(ReviewPerformance, sortedList, itemCount, appItemList);
		reportSummaryItems(Ignored, sortedList, itemCount, appItemList);
		reportSummaryItems(Supported, sortedList, itemCount, appItemList);
		sortedList.clear();
		itemCount.clear();
		appItemList.clear();
		forceGC();

		// sort for X-ref by feature
		List<String> sortedListXRefByFeature = xRefByFeature.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
		sortedListXRefByFeature.add(stringRepeat(lastItem + sortKeySeparator, 20));
		xRefByFeature.clear();

		reportXrefByFeature(NotSupported, sortedListXRefByFeature);
		reportXrefByFeature(ReviewManually, sortedListXRefByFeature);
		reportXrefByFeature(ReviewSemantics, sortedListXRefByFeature);
		reportXrefByFeature(ReviewPerformance, sortedListXRefByFeature);
		reportXrefByFeature(Ignored, sortedListXRefByFeature);
		reportXrefByFeature(Supported, sortedListXRefByFeature);
		sortedListXRefByFeature.clear();
		forceGC();

		// sort for X-ref by object
		List<String> sortedListXRefByObject = xRefByObject.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
		sortedListXRefByObject.add(stringRepeat(lastItem + sortKeySeparator, 20));
		xRefByObject.clear();

		reportXrefByObject(NotSupported, sortedListXRefByObject);
		reportXrefByObject(ReviewManually, sortedListXRefByObject);
		reportXrefByObject(ReviewSemantics, sortedListXRefByObject);
		reportXrefByObject(ReviewPerformance, sortedListXRefByObject);
		reportXrefByObject(Ignored, sortedListXRefByObject);
		reportXrefByObject(Supported, sortedListXRefByObject);
		sortedListXRefByObject.clear();
		forceGC();

		writeReportFile();
		writeReportFile(composeOutputLine("", "="));
		writeReportFile();

		return true;
	}

	// LEXER CODE
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

	public void addLexicalErrorHex(StringBuilder sb, String text) {
		text = limitTextSize(text);
		sb.append("lexical error: ").append(text).append(" with hex=");

		for (char c : text.toCharArray()) {
			sb.append("0x");
			String hexString = Integer.toHexString(c);
			int leadingZerosNr = 4 - hexString.length();
			for (int i = 0; i < leadingZerosNr; i++) {
				sb.append("0");
			}
			sb.append(hexString);
		}
	}

	public void setErrorMsg(int line, int col, String text) {
		StringBuilder sb = new StringBuilder();
		sb.append("Line ").append(line).append(":").append(col + 1).append(", ");
		addLexicalErrorHex(sb, text);
		errorMsg = sb.toString();
	}
}
