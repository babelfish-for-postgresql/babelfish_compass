# 2025-02
- Added support for Babelfish v.5.0.0, v.4.5.0.
- Detect various server-level and database-level fixed roles.

# 2024-12
- Added support for Babelfish v.4.4.0.
- Detect object type argument to sp_rename.
- Detect cross-database references by SET IDENTITY_INSERT.
- Added -nopopupwindow flag to suppress automatically opening the report in the browser.
- Automatic DDL generation (with -sqlendpoint) for multiple databases continues when a database does not exist or is not accessible.
- Include Compass User Guide in distro .zip file.

# 2024-10b
- Detect and rewrite redundant unary '+' operators for strings.

# 2024-10
- Added support for Babelfish v.4.3.0.
- Various small fixes.

# 2024-07
- Added support for Babelfish v.4.2.0.
- Added -anon flag to allow sharing Compass reports with customer-specific information removed.
- Include additional geosptial aspects that were already supported in 4.1.0.
- Include INFORMATION_SCHEMA.KEY_COLUMN_USAGE that was already supported in 3.4.0.
- Various small fixes.

# 2024-04
- Added support for Babelfish v.4.1.0 and v.3.5.0.
- Detect and rewrite often-seen FOR XML PATH workaround for STRING_AGG().
- Various small enhancements.

# 2024-02
- Updated support for Babelfish v.4.0.0.

# 2023-12-a
- Added DEFAULT parameters as supported in 3.4.0 (previously omitted).

# 2023-12
- Updated support for Babelfish v.3.4.0.

# 2023-11
- Updated User Guide with sections about scaling and advanced features.
- Additional usability features for large-scale cases.
- Minor report enhancements.

# 2023-10
- Updated support for Babelfish v.3.3.0.
- Rewrite of WITH RECOMPILE (only if no other options are present).
- Rewrite of DROP INDEX table.index
- When uploading with the '-pgimport' flag, the uploaded details now contain additional information for tracking object dependencies.
- Minor report enhancements.

# 2023-08
- Fixed an issue with non-English locale causing aborting with error "Analysis was performed for a different Babelfish version (v.) than targeted by this run (v.3.2.0)".
- Fixed a problem when using the -sqlendpoint flag, causing Compass to hang.
- When using -sqlendpoint, include SQL Server license type and resource info in the Compass report. 
- Support for new 'one-time learning curve' component in user-defined effort estimates.
- New user-defined defaults for complexity scores and effort estimates, for categories of items.
- New flag '-csvformat flat' to generate the .csv file in 'flat' format.
- Small report enhancements.

# 2023-06
- Updated support for Babelfish v.3.2.0.
- Expanded scope of -optimistic flag.
- New feature to generate DDL automatically.
- Detect/rewrite !< and !> operators.
- Suppress rewrite for dynamically composed SQL.
- UPDATE/DELETE TOP are now supported but reclassified as "Review Semantics" due to inherent row order unpredictability.

# 2023-03-a
- Updated support for Babelfish v.2.4.0 and v.3.1.0.
- Detect and report dynamically declared cursors.

# 2023-03
- Support for Babelfish v.2.4.0 and v.3.1.0.
- Detect and report SQL2022 features.
- Added predefined 'optimistic' user-defined .cfg file and -optimistic option (see user guide).
- Minor grammar fixes.

# 2022-12
- Support for Babelfish v.2.3.0.
- Include condensed 'Executive Summary' section at top of the report.
- Dynamic SQL statements consisting only of a string literal are now also analyzed, and the executing statement is not flagged as 'Review Manually'; this is done for EXECUTE(), sp_executesql, sp_prepare, sp_prepexec, sp_cursorprepare, sp_cursorprepexec.
- Detect and report multiple concatenated constraints in a column definition.
- New option -userconfigfile to use a specific user-defined .cfg file (default=BabelfishCompassUser.cfg).
- Include low/medium/high complexity score for not-supported features only.
- Include complexity score when uploading details with -pgimport.
- Minor grammar fixes.

# 2022-11
- Report SELECT FOR JSON options.
- Allow generating a report only when analysis was done on an older Babelfish version.
- Detecting and reporting scalar user-defined function calls in column defaults and CHECK constraints.
- Line numbers in Xref reports were not correct for some index-related items.
- `@@error value 50000, via THROW` now classified as supported.
- Indexes with additional included columns exceeding the maximum of 32 now classified as 'Review Performance'.
- Better error message for some cases of a non-existing report name.
- Optimization for -pgimport.
- Fix reporting of duplicate table names for multiple apps in Xref report.
- Classifying column attributes ROWGUIDCOL,SPARSE,FILESTREAM as Ignored by default.
- Minor grammar fix.

# 2022-10
- Detect THROW when it is first statement in the batch.
- Support processing of Extended Events capture files. 
- Adjusted various default complexity scores.

# 2022-09
- Support for Babelfish v.2.2.0.
- Mark ALTER TABLE..{ENABLE|DISABLE} TRIGGER as supported since v.1.0.0 (it was incorrectly marked as not supported).
- Mark CHECKPOINT as not supported (it was incorrectly marked as supported).
- Add -rewrite support for compound comparison operators containing whitespace.
- Better detection of non-supported operators and function calls in computed columns.
- Generating a .csv file to facilitate user-defined quantifying of migration effort.

# 2022-07
- Support for Babelfish 1.3.1 and 2.1.1.
- Add -rewrite support for cases of DATE{PART|NAME|DIFF|ADD}() functions with an unsupported 'unit' argument.
- Add -rewrite support for DEFAULT parameter values in function/procedure calls.
- When generating reports from prior analysis, do not lose the 'rewrite' cases from the report.

# 2022-06-a
- Consume XML files with queries captured by SQL Server Profiler.
- Mark cursor variables as supported since v.1.0.0.
- Better detection of variable dependencies in SELECT.
- Detect table-valued functions using SELECT TOP.
- Detect INSERT..EXECUTE() on a single string constant (which is supported).
- The -rewrite option now handles unquoted strings and double-quoted strings with embedded quotes.
- Detect syntax issues with UPDATE/DELETE statements when specifying -syntax_issues option (experimental).
- Performance improvements when analyzing large number of applications together.
- Reporting improvements with better hyperlinks.
- Various small fixes.

# 2022-06
- Support for Babelfish v.2.1.0 and 1.3.0.
- Added -pgimporttable flag.
- Do not process duplicate input files.
- Enhancements to -rewrite.
- Rationalize -delete/-add/-replace options.
- Exclude additional file types by default.
- User guide: cleanup, example added.

# 2022-04
- Support for Babelfish v.1.2.1.
- Automatic check for newer versions of Compass.
- Record Compass version in session log file.
- Various small grammar fixes.
- Skip input files/folders starting with a dot.

# 2022-03-a
- Support for `INSTEAD OF` triggers on tables in v.1.2.0.

# 2022-03
- Support for Babelfish v.1.2.0.

- New `-recursive` option to process directory trees.

- New `-include` and `-exclude` options to filter on file type.

- Fix for processing UTF8-with-BOM input files.

- Various improvements to grammar and reporting.

# 2022-02
- New version numbering for Babelfish Compass.

- Small enhancements.

# 1.2
- New `-rewrite` feature.

- `.cfg` file updated for Babelfish Compass 1.1.0.

- Various minor fixes.

# 1.1
- Mac/Linux support.

- Added user-definable classification overrides.

- Expanded version of User Guide.

# 1.0.2
- Changes to test Mac/Linux support.

# 1.0.1
- Changes to test Mac support.

# 1.0
- Babelfish Compass 1.0, for Babelfish 1.0.0.
