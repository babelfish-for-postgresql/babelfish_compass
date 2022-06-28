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
