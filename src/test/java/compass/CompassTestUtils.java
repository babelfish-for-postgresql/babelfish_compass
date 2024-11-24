/*
Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/

package compass;

import java.util.*;

public class CompassTestUtils {

    private CompassTestUtils() {
        // no one should instantiate us
    }

    public static void resetStatics() {
        resetCompassUtilitiesStatics();
        resetCompassStatics();
    }

    static void resetCompassStatics() {
        Compass.nrFileNotFound = 0;
        Compass.totalBatches = 0;
        Compass.totalParseErrors = 0;
        Compass.passCount = new HashMap<>();
        Compass.nrLinesTotalP1 = 0;
        Compass.nrLinesTotalP2 = 0;
        Compass.retrySLL = 0;
        Compass.hasParseError = false;
        Compass.parseErrorMsg = new StringBuilder();

        Compass.startRun = 0;
        Compass.startRunDate = null;
        Compass.startRunFmt = null;
        Compass.endRun = 0;
        Compass.endRunFmt = null;

        Compass.startTime = 0;
        Compass.endTime = 0;
        Compass.duration = 0;

        Compass.timeCount = new HashMap<>();

        Compass.quitNow = false;

        Compass.showVersion = false;
        Compass.readStdin = false;
        Compass.parseOnly = false;
        Compass.importOnly = false;
        Compass.dumpParseTree = false;
        Compass.dumpBatchFile = false;
        Compass.forceAppName = false;
        Compass.forceReportName = false;
        Compass.reportFileName = "";
        Compass.quotedIdentifier = "ON";
        Compass.addReport = false;
        Compass.replaceFiles = false;
        Compass.recursiveInputFiles = false;
        Compass.includePattern = null;
        Compass.excludePattern = null;
        Compass.defaultExcludes = new LinkedHashSet<>(Arrays.asList(
                ".ppt",".pptx", ".xls",".xlsx", ".doc", ".docx", ".pdf", ".rtf", ".htm", ".html", ".zip", ".gzip", ".gz",
                ".rar", ".7z", ".tar", ".tgz", ".sh", ".bash", ".csh", ".tcsh", ".bat", ".csv", ".md", ".jpg", ".gif",
                ".png",	".tmp", ".pl", ".py", ".cs", ".cpp", ".vb", ".c", ".php", ".java", ".classpath", ".project", ".rb",
                ".js", ".exe", ".dll", ".sln", ".scc", ".gitignore", ".json", ".yml", ".yaml", ".xml", ".xsl", ".xsd", ".xslt")
        );
        Compass.generateReport = true;
        Compass.reAnalyze = false;
        Compass.reportOnly = false;
        Compass.reportOption = false;
        Compass.deleteReport = false;
        Compass.userSpecifiedBabelfishVersion = false;
        Compass.listContents = false;
        Compass.pgImport = false;
        Compass.pgImportAppend = false;

        Compass.antlrSLL = true;
        Compass.antlrShowTokens = false;
        Compass.antlrTrace = false;
        Compass.antlrDiagnostics = false;
        Compass.charset = null;
        Compass.userEncoding = null;

        Compass.reportName = null; // must be null when not initialized
        Compass.applicationName = null;
        Compass.sessionLog = null;

        Compass.inputFiles = new ArrayList<>();
        Compass.inputFilesOrig = new ArrayList<>();
        Compass.inputFilesMapped = new HashMap<>();
        Compass.cmdFlags = new ArrayList<>();
        Compass.pgImportFlags = new ArrayList<>();

        Compass.exportedParseTree = null;

        Compass.u = CompassUtilities.getInstance();
        Compass.cfg = CompassConfig.getInstance();
        Compass.a = CompassAnalyze.getInstance();
    }

    static void resetCompassUtilitiesStatics() {
        CompassUtilities.onWindows = false;
        CompassUtilities.onMac = false;
        CompassUtilities.onLinux = false;
        CompassUtilities.onPlatform = CompassUtilities.uninitialized;
        CompassUtilities.thisProgExec = "java " + CompassUtilities.thisProgPathExec + "." + CompassUtilities.thisProgNameExec;
        CompassUtilities.userConfig = true;
        CompassUtilities.captureFileFormatBaseVersion = "1";
        CompassUtilities.captureFileFormatVersionList = Arrays.asList(CompassUtilities.captureFileFormatBaseVersion);
        CompassUtilities.captureFileFormatVersion = CompassUtilities.captureFileFormatBaseVersion;
        CompassUtilities.importFormatOption = Arrays.asList(CompassUtilities.autoFmt, CompassUtilities.sqlcmdFmt,
                CompassUtilities.jsonQueryFmt,  CompassUtilities.extendedEventsXMLFmt, CompassUtilities.genericSQLXMLFmt);
        CompassUtilities.importFormatOptionDisplay = Arrays.asList(CompassUtilities.autoFmt, CompassUtilities.sqlcmdFmt,
                "JSON query", "extended events/XML", "generic SQL XML");
        CompassUtilities.importFormat = CompassUtilities.autoFmt.toLowerCase();
        CompassUtilities.deDupExtracted = true;
        CompassUtilities.deDupSkipped = 0;
        CompassUtilities.hintIcon = "&#x1F6C8;";
        CompassUtilities.reportShowAppName = true;
        CompassUtilities.reportShowSrcFile = true;
        CompassUtilities.reportAppsCount = true;
        CompassUtilities.reportShowBatchNr = "";
        CompassUtilities.reportOptionXref = "";
        CompassUtilities.reportOptionStatus = "";
        CompassUtilities.reportOptionApps = "";
        CompassUtilities.reportOptionDetail = "";
        CompassUtilities.reportOptionFilter = "";
        CompassUtilities.reportOptionNotabs = false;
        CompassUtilities.reportOptionLineNrs = false;
        CompassUtilities.linesSQLInReport = 0;
        CompassUtilities.reportHdrLines = "";
        CompassUtilities.maxLineNrsInListDefault = 10;
        CompassUtilities.maxLineNrsInList = CompassUtilities.maxLineNrsInListDefault;
        CompassUtilities.linkInNewTab = true;
        CompassUtilities.tgtBlank = " target=\"_blank\"";
        CompassUtilities.showPercentage = false;
        CompassUtilities.tableViewSymTab = new HashMap<>();
        CompassUtilities.UDDSymTab = new HashMap<>();
        CompassUtilities.SUDFSymTab = new HashMap<>();
        CompassUtilities.TUDFSymTab = new HashMap<>();
        CompassUtilities.colSymTab = new HashMap<>();
        CompassUtilities.SUDFNamesLikeXML = new HashMap<>();
        CompassUtilities.TUDFNamesLikeXML = new HashMap<>();
        CompassUtilities.HIERARCHYIDmethods = new ArrayList<>();
        CompassUtilities.SUDFNamesLikeHIERARCHYID = new HashMap<>();
        CompassUtilities.supportOptions = Arrays.asList(CompassUtilities.Supported, CompassUtilities.NotSupported,
                CompassUtilities.ReviewSemantics, CompassUtilities.ReviewPerformance, CompassUtilities.ReviewManually,
                CompassUtilities.Ignored, CompassUtilities.ObjCountOnly, CompassUtilities.Rewritten);
        CompassUtilities.supportOptionsCfgFile = Arrays.asList("Supported", "NotSupported",  "ReviewSemantics",
                "ReviewPerformance",  "ReviewManually",  "Ignored", CompassUtilities.ObjCountOnly, CompassUtilities.Rewritten);
        CompassUtilities.validSupportOptionsCfgFileOrig = Arrays.asList("NotSupported", "ReviewSemantics",
                "ReviewPerformance", "ReviewManually", "Ignored");
        CompassUtilities.validSupportOptionsCfgFile = new ArrayList<>();
        CompassUtilities.defaultClassificationsKeysOrig = Arrays.asList("default_classification-ReviewSemantics",
                "default_classification-ReviewPerformance", "default_classification-ReviewManually",
                "default_classification-Ignored", "default_classification");
        CompassUtilities.defaultClassificationsKeys = new ArrayList<>();
        CompassUtilities.overrideClassificationsKeysOrig = Arrays.asList("default_classification-ReviewSemantics",
                "default_classification-ReviewPerformance", "default_classification-ReviewManually",
                "default_classification-Ignored", "default_classification");
        CompassUtilities.overrideClassificationsKeys = new ArrayList<>();
        CompassUtilities.supportOptionsDisplay = Arrays.asList("Supported", "Not Supported", "Review Semantics",
                "Review Performance", "Review Manually", "Ignored", CompassUtilities.ObjCountOnly,
                "Rewritten by Babelfish Compass");
        CompassUtilities.supportOptionsIterate = Arrays.asList(CompassUtilities.NotSupported, CompassUtilities.ReviewManually,
                CompassUtilities.ReviewSemantics, CompassUtilities.ReviewPerformance, CompassUtilities.Ignored,
                CompassUtilities.Supported);
        CompassUtilities.supportOptionsWeightDefault = Arrays.asList(100, 200, 150, 150, 150, 0, 0, 100);
        CompassUtilities.rewrite = false;
        CompassUtilities.rewriteTextListKeys = new ArrayList<>();
        CompassUtilities.rewriteTextList = new HashMap<>();
        CompassUtilities.rewriteTextListOrigText = new HashMap<>();
        CompassUtilities.rewriteIDDetails = new HashMap<>();
        CompassUtilities.rewrittenOppties = new HashMap<>();
        CompassUtilities.rewriteNotes = CompassUtilities.uninitialized;
        CompassUtilities.rewriteOppties = new HashMap<>();
        CompassUtilities.rwrTabRegex = "";
        CompassUtilities.nrRewritesDone = 0;
        CompassUtilities.nrMergeRewrites = 0;
        CompassUtilities.rewriteTypeExpr1 = "expr(1)";
        CompassUtilities.rewriteTypeReplace = "replace";
        CompassUtilities.rewriteTypeODBCfunc1 = "ODBCfunc1";
        CompassUtilities.rewriteTypeODBClit1 = "ODBClit1";
        CompassUtilities.rewriteTypeBlockReplace = "BlockReplace";
        CompassUtilities.offsetCols = new ArrayList<>();
        CompassUtilities.offsetLines = new HashMap<>();
        CompassUtilities.rewritesDone = new ArrayList<>();
        CompassUtilities.devOptions = false;
        CompassUtilities.caching = false;
        CompassUtilities.grammarRuleNames = null;
    }
}
