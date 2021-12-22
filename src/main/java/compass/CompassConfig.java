/*
Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/

package compass;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

// this class reads/validates the .cfg file, and provides API calls to determine if a particular feature is supported
public class CompassConfig {
	static String configFileName;
	static String userConfigFileName;
	static String configFilePathName;
	static String userConfigFilePathName;
	static File cfgFile;
	static File userCfgFile;
    static Map<String, Map<String, String>> cfg;
    static Map<String, Map<String, String>> userCfg;

	// must be the first section in the .cfg file:
	static final String Babelfish_Compass_Name = "Babelfish for T-SQL";

	static List<String> Babelfish_VersionList = new ArrayList<>();
	static Map<String, Map<String, List<String>>> sectionList = new LinkedHashMap<>();
	static Map<String, Map<String, List<String>>> sectionOverrideList = new LinkedHashMap<>();
	static Map<String, String> featureArgOptions = new LinkedHashMap<>();  // assuming only one argument per feature. If more, the value needs to become a List
    static boolean cfgFileValid = true;
    static boolean userCfgFileValid = true;
    static boolean versionInvalid = false;
    static int overrideCount = 0;

    // keys in sections
    static final String validVersionsTag  = "VALID_VERSIONS";
    static final String fileFormatTag     = "FILE_FORMAT";
    static final String fileTimestampTag  = "FILE_TIMESTAMP";
    static final String listValuesTag     = "LIST";
    static final String defaultStatusTag  = "DEFAULT_CLASSIFICATION";
    static final String reportGroupTag    = "REPORT_GROUP";
    static final String supportedTag      = "SUPPORTED";
    static final String ruleTag           = "RULE";

	static final String wildcardChar = "%"; // can be used in list key as a wildcard
	static final String wildcardTag = "WILDCARD";

	// character between 'supported' and the version, or 'report_group' and the group, or 'default_classification' and the status
	// e.g. 'supported-1.0=', or 'report_group-XML=', or 'default_classification=ReviewSemantics'
	static final String subKeySeparator = "-";

    // separator for range of versions
	static final String cRangeSeparator = "-";
	
	static List<String> supportOptionsCfgFileUpperCase = new ArrayList<>();	
		
	// user-defined cfg file; keep original .cfg file order
	static List<String> cfgSections = new ArrayList<>();


	public static CompassUtilities u = CompassUtilities.getInstance();

	private static final CompassConfig instance = new CompassConfig();

	private CompassConfig() {}

	public static CompassConfig getInstance() {
		return instance;
	}

	// entry point for initializating the .cfg part
	public void validateCfgFile(String pCfgFileName, String pUserCfgFileName) throws Exception {
		supportOptionsCfgFileUpperCase = new ArrayList<>(u.supportOptionsCfgFile);	
		u.listToUpperCase(supportOptionsCfgFileUpperCase);	
		readCfgFile(pCfgFileName);

		if (u.debugCfg) {
			// this output may not get into the session log at that may nto be opened yet at this point
			dumpCfg();
		}

		if (!cfgFileValid) {
			if (versionInvalid) {
				cfgOutput("Valid Babelfish versions: " + validBabelfishVersions());
			}
			cfgOutput("Configuration file not valid");
			u.errorExit();
		}
		cfgOutput("Latest "+u.babelfishProg+" version supported: "+latestBabelfishVersion());
		
		// user .cfg file
		validateUserCfgFile(pUserCfgFileName); 
		if (!userCfgFileValid) {
			cfgOutput(userConfigFilePathName, "User configuration file not valid. Delete file or remove offendng sections.");
			u.errorExit();	
		}
	}

	// validate/update the user's .cfg file; create if not existing
	private static void validateUserCfgFile(String pUserCfgFileName) throws IOException {
		userConfigFileName = pUserCfgFileName;
		userConfigFilePathName = u.getUserCfgFilePathName(userConfigFileName);		
        userCfgFile = new File(userConfigFilePathName);
		if (u.userConfig) {
	       // nothing
	    }
	    else {
	        u.appOutput("Skipping "+userConfigFilePathName);
	        return;
	    }
                
        if (!userCfgFile.exists()) {
        	u.appOutput("Creating user configuration file "+userConfigFilePathName);
        	
			u.openUserCfgFileNew(userConfigFileName);	
			for (String s : cfgSections) {
				if (s.equals(Babelfish_Compass_Name)) continue;
				u.writeUserCfgFile("["+s+"]\n\n");
			}
			u.closeUserCfgFile();	    			    	
        }
        else {
        	// read and validate contents of user .cfg file
        	 u.appOutput("Reading "+userConfigFilePathName);
			userCfg = getCfg(userConfigFilePathName, true);    
			for (String sectionName: userCfg.keySet()) {         
				Map<String, String> section = userCfg.get(sectionName);
				//u.appOutput("sectionName=[" + sectionName + "]");
				if (!sectionList.containsKey(sectionName.toUpperCase())) {
					cfgOutput(userConfigFilePathName, "section ["+sectionName+"] not found in " + configFilePathName);
					userCfgFileValid = false;
					continue;
				}

				for (String optionKey : section.keySet()) {
					String optionVal = section.get(optionKey);
					String optionKeyCopy = optionKey;
					optionKey = optionKey.toUpperCase();
					String thisKey = createKey(optionKey);
					// key: 'default_classification' items
					if (optionKey.startsWith(defaultStatusTag)) {
						overrideCount++;						
						if (optionKey.equals(defaultStatusTag)) {
							if (u.validSupportOptionsCfgFile.contains(optionVal.toUpperCase())) {
								optionVal = u.supportOptions.get(supportOptionsCfgFileUpperCase.indexOf(optionVal.toUpperCase()));
							} 
							else {
								cfgOutput(userConfigFilePathName, "Section [" + sectionName + "]: override key " + defaultStatusTag + " has invalid value [" + optionVal + "]. Valid values are " + u.validSupportOptionsCfgFileOrig);
								userCfgFileValid = false;
							}
						} 
						else if (!u.overrideClassificationsKeys.contains(optionKey)) {
							cfgOutput(userConfigFilePathName, "Section [" + sectionName + "]: override key " + optionKey + " is invalid. Valid values are " + u.overrideClassificationsKeysOrig);
							userCfgFileValid = false;
						}
						else {
							if (optionVal.equals("*")) {
								// default_classification-xxx=* ==> change to: default_classification=xxx
								optionVal = optionKey.substring(defaultStatusTag.length() + 1);
								optionKey = optionKey.substring(0,defaultStatusTag.length());
								thisKey = createKey(optionKey);
							}
						}
					}
					// key: 'report_group', 'report_group-XYZ'
					else if (optionKey.startsWith(reportGroupTag)) {
						overrideCount++;						
						if (optionVal.isEmpty()) {
							cfgOutput(userConfigFilePathName, "Invalid override key '" + reportGroupTag + "=': value cannot be blank");
							userCfgFileValid = false;
						} 
						else if (optionVal.equals("*")) {
							if (optionKey.length() > reportGroupTag.length()) {
								// report_group-xxx=* ==> change to: report_group=xxx
								optionVal = optionKey.substring(reportGroupTag.length() + 1);
								optionKey = optionKey.substring(0,reportGroupTag.length());
								thisKey = createKey(optionKey);				
							}
							else {
								cfgOutput(userConfigFilePathName, "Invalid key '" + reportGroupTag + "=': value must be a group name, not '*'");
								cfgFileValid = false;
							}							
						}
						// do not lowercase the group name when it is part of the key
						if (optionKey.length() > reportGroupTag.length()) {
							String groupName = optionKeyCopy.substring(reportGroupTag.length() + 1);
							// the key is in uppercase, but we want the group name to preserve the case, so patch it in
							thisKey = reportGroupTag + subKeySeparator + groupName; 
						}
					} 
					else {
						// catchall
						cfgOutput(userConfigFilePathName, "Invalid override key '" + optionKey + "'");
						userCfgFileValid = false;
					}	
						
					// record the overrides
					List<String> theseItems = new ArrayList<>(Arrays.asList(optionVal.split(",")));
					if (!optionKey.equals(reportGroupTag)) {
						u.listToUpperCase(theseItems);
					}
							
					if (!sectionOverrideList.containsKey(sectionName.toUpperCase())) {
						sectionOverrideList.put(sectionName.toUpperCase(), new LinkedHashMap<>());
					}
					
					Map<String, List<String>> featureList;
					featureList = sectionOverrideList.get(sectionName.toUpperCase());
					featureList.put(thisKey, theseItems);
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "added override key: [" + thisKey + "] optionKey=[" + optionKey + "] of section=[" + sectionName + "] = [" + optionVal + "]", u.debugCfg);		
					
					// check items in 'report_group-xxx', default_classification-XXX'  are in the listValues key (if a list exists)
					if (optionKey.startsWith(reportGroupTag+subKeySeparator) ||
					    optionKey.startsWith(defaultStatusTag+subKeySeparator)
					   ) {					
						boolean itemValid = validateItemsListed(userConfigFilePathName, optionKey, optionVal, thisKey, sectionName, sectionList.get(sectionName.toUpperCase()), theseItems);				
						userCfgFileValid = userCfgFileValid & itemValid;
					}															
				}				
			}
			if (!userCfgFileValid) {
				return;
			}
			
			// patch up .cfg file
			// first determine if there are any keys missing compared to the main .cfg file
			// if so, append the missing keys 
			StringBuilder addLines = new StringBuilder("");
			for (String mainSection: cfg.keySet()) {
				if (mainSection.equals(Babelfish_Compass_Name)) continue;
				if (userCfg.containsKey(mainSection)) continue;
				u.appOutput("Appending section ["+mainSection+"] to "+ userConfigFilePathName);
				addLines.append("["+mainSection+"]\n\n");
			}
			
			// add missing sections
			if (addLines.length() > 0) {
				u.openUserCfgFileAppend(userConfigFileName);	
				u.writeUserCfgFile(addLines.toString());
				u.closeUserCfgFile();	 
			}
		}
		//u.errorExit();
	}
	
	public static void cfgOutput(String s) {
		cfgOutput(s, configFileName);
	}
	public static void cfgOutput(String cfgFileName, String s) {
		if (cfgFileName.isEmpty()) cfgFileName = configFilePathName;
		u.appOutput(cfgFileName+": "+s);
	}

	public static boolean isValidBabelfishVersion(String version) {
		return isValidBabelfishVersion(version, false);
	}

	public static boolean isValidBabelfishVersionWithStar(String version) {
		return isValidBabelfishVersion(version, true);
	}

	protected static boolean isValidBabelfishVersion(String version, boolean allowStar) {
		if (version.contains("*")) {
			if (!allowStar) { return false; }
			// found 1.* or 1.2.* or similar: a version with a number instead of * must exist
			String vStar = version.substring(0,version.indexOf("*")-1);
			for (String v : Babelfish_VersionList) {				
				if (v.startsWith(vStar)) {
					return true;
				}
			}
			return false;
		}
		else {
			return Babelfish_VersionList.contains(version);
		}
	}

	public static String validBabelfishVersions() {
		return Babelfish_VersionList.toString().replace("[", "").replace("]", "");
	}

	public static String latestBabelfishVersion() {
		// get the latest supported Babelfish version
		String v = CompassUtilities.baseBabelfishVersion;
		for (String s : Babelfish_VersionList) {
			v = higherBabelfishVersion(v, s);
		}
		return v;
	}

	public static String normalizedBabelfishVersion(String version) {
		// for comparing versions, use a normalized internal representation. Assumption: external format is \d+(\.\d+)* or \d+\.\*
		StringBuilder internalVers = new StringBuilder();
		String[] vParts = version.split("\\.");
		for(int i=0; i < vParts.length; i++) {
			if (vParts[i].equals("*")) {
				vParts[i] = "99999";
			}
			internalVers.append(String.format("%05d", Integer.parseInt(vParts[i]))).append('.');
		}
		internalVers.deleteCharAt(internalVers.length()-1);
		return internalVers.toString();
	}

	public static String lowerBabelfishVersion(String version1, String version2) {
		assert !(version1.isEmpty() && version2.isEmpty()) :  CompassUtilities.thisProc()+"version1 and version2 cannot both be blank";
		if (version1.isEmpty()) return version2;
		if (version2.isEmpty()) return version1;
		if (normalizedBabelfishVersion(version1).compareTo(normalizedBabelfishVersion(version2)) < 0) return version1;
		else return version2;
	}

	public static String higherBabelfishVersion(String version1, String version2) {
		assert !(version1.isEmpty() && version2.isEmpty()) :  CompassUtilities.thisProc()+"version1 and version2 cannot both be blank";
		if (version1.isEmpty()) return version2;
		if (version2.isEmpty()) return version1;
		if (normalizedBabelfishVersion(version1).compareTo(normalizedBabelfishVersion(version2)) > 0) return version1;
		else return version2;

	}

	public static boolean isLowerBabelfishVersion(String version1, String version2) {
		assert !(version1.isEmpty() && version2.isEmpty()) :  CompassUtilities.thisProc()+"version1 and version2 cannot both be blank";
		return normalizedBabelfishVersion(version1).compareTo(normalizedBabelfishVersion(version2)) < 0;
	}

	public static boolean isLowerOrEqualBabelfishVersion(String version1, String version2) {
		assert !(version1.isEmpty() && version2.isEmpty()) :  CompassUtilities.thisProc()+"version1 and version2 cannot both be blank";
		return normalizedBabelfishVersion(version1).compareTo(normalizedBabelfishVersion(version2)) <= 0;
	}

	public static boolean isHigherBabelfishVersion(String version1, String version2) {
		assert !(version1.isEmpty() && version2.isEmpty()) :  CompassUtilities.thisProc()+"version1 and version2 cannot both be blank";
		return normalizedBabelfishVersion(version1).compareTo(normalizedBabelfishVersion(version2)) > 0;
	}

	public static boolean isEqualBabelfishVersion(String version1, String version2) {
		assert !(version1.isEmpty() && version2.isEmpty()) :  CompassUtilities.thisProc()+"version1 and version2 cannot both be blank";
		return normalizedBabelfishVersion(version1).compareTo(normalizedBabelfishVersion(version2)) == 0;
	}

	// for a feature where the actual value of the Nth argument needs to be validated
	// return NotSupported in case no supported minimum version was found
	public static String featureArgSupportedMinimumVersion(String section, String arg, String argValue) {
		String minVersion = "";
		section = section.toUpperCase();
		arg = arg.toUpperCase();
		if (featureArgOptions.containsKey(section)) {
			argValue = argValue.toUpperCase();
			Map<String, List<String>> featureList = sectionList.get(section);
			for (String key: featureList.keySet()) {
				if (key.startsWith(supportedTag + "/") && key.endsWith("/" + arg)) {
					String foundVersion = key.substring(supportedTag.length() + 1, key.length() - arg.length() - 1);
					if (foundVersion.isEmpty()) continue;
					List<String> thisList = featureList.get(key);
					if (thisList.contains(argValue) || thisList.contains("*")) {
						// feature is supported in version 'foundVersion'
						if (foundVersion.contains(cRangeSeparator)) { //interval specified
							foundVersion = foundVersion.substring(0,foundVersion.indexOf(cRangeSeparator));
						}
						minVersion = lowerBabelfishVersion(foundVersion, minVersion);
					}
				}
			}
	    }
		return minVersion.isEmpty() ? u.NotSupported : minVersion;
	}

	// for a feature where the actual value of the Nth argument needs to be validated:
	public static String featureArgSupportedInVersion(String requestVersion, String section, String arg, String argValue) {
		String status = u.NotSupported;
		section = section.toUpperCase();
		arg = arg.toUpperCase();
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " entry: section=[" + section + "]  requestVersion=[" + requestVersion + "] arg=[" + arg + "] argValue=[" + argValue + "] ", u.debugCfg);
		if (featureArgOptions.containsKey(section)) {
			argValue = argValue.toUpperCase();
			if ((argValue.charAt(0) == '\'') || (argValue.charAt(0) == '"')) {
				argValue = u.stripStringQuotes(argValue);
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " stripped quotes: argValue=[" + argValue + "] ", u.debugCfg);
			}
			if (argValue.charAt(0) == '@') {
				// we cannot determine the contents of a variable
				return u.ReviewManually;
			}
			if (argValue.charAt(0) == '(') {
				// we cannot determine the contents of an expression
				// admittedly, this does not catch all expressions
				return u.ReviewManually;
			}
			
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " featureArgOptions found: section=[" + section + "]  requestVersion=[" + requestVersion + "] arg=[" + arg + "] argValue=[" + argValue + "] ", u.debugCfg);
			Map<String, List<String>> featureList = sectionList.get(section);

			for (String key : featureList.keySet()) {
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() +"key=[" + key + "] ", u.debugCfg);
				if (key.startsWith(supportedTag + "/") && key.endsWith("/" + arg)) {
					String foundVersion = key.substring(supportedTag.length() + 1, key.length() - arg.length() - 1);
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() +"key=[" + key + "] foundVersion=[" + foundVersion + "] ", u.debugCfg);
					if (foundVersion.isEmpty()) continue;  // should never be blank
					List<String> thisList = featureList.get(key);
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() +"featureList Key=[" + key + "] (" + thisList.size() + ") --> " + thisList, u.debugCfg);
					if (thisList.contains(argValue) || thisList.contains("*")) {
						// feature is supported in version 'foundVersion' -- is that same or earlier as what is being asked?
						if (isVersionSupported(requestVersion, foundVersion)) {
							status = u.Supported;
							break;
						}
					}
				}
			}
			if (!status.equals(u.Supported)) {
				status = featureDefaultStatus(section);				
			}		
		}
		return status;
	}

	// return NotSupported in case no supported minimum version was found
	public static String featureSupportedMinimumVersion(String section) {
		String minVersion = "";
		section = section.toUpperCase();
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " entry: section=[" + section + "] ", u.debugCfg);

		if (featureExists(section)) {
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " featureExists: section=[" + section + "] ", u.debugCfg);
			Map<String, List<String>> featureList = sectionList.get(section);
			for (String key : featureList.keySet()) {
				if (key.startsWith(supportedTag + "/")) {
					String foundVersion = key.substring(supportedTag.length() + 1);
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() +"key=[" + key + "] foundVersion=[" + foundVersion + "] ", u.debugCfg);
					if (foundVersion.isEmpty()) continue;

					List<String> thisList = featureList.get(key);
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() +" thisList  key=[" + key + "] => [" + thisList + "] ", u.debugCfg);
					if (thisList.contains("*")) {
						// feature is supported in version 'foundVersion'
						if (foundVersion.contains(cRangeSeparator)) { //interval was specified
							foundVersion = foundVersion.substring(0, foundVersion.indexOf(cRangeSeparator));
						}
						minVersion = lowerBabelfishVersion(foundVersion, minVersion);
					}
				}
			}
		}
		return minVersion.isEmpty() ? u.NotSupported : minVersion;
	}

	// return NotSupported in case no supported minimum version was found
	public static String featureSupportedMinimumVersion(String section, String name) {
		String minVersion = "";
		section = section.toUpperCase();
		name = name.toUpperCase();
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " entry: section=[" + section + "] name=[" + name + "] ", u.debugCfg);
		assert (!name.isEmpty()) : "name argument cannot be blank";

		if (featureExists(section, name)) {
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " featureExists: section=[" + section + "] name=[" + name + "] ", u.debugCfg);
			Map<String, List<String>> featureList = sectionList.get(section);
			for (String key : featureList.keySet()) {
				if (key.startsWith(supportedTag + "/")) {
					String foundVersion = key.substring(supportedTag.length() + 1);
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() +"key=[" + key + "] foundVersion=[" + foundVersion + "] ", u.debugCfg);
					if (foundVersion.isEmpty()) continue;
					List<String> thisList = featureList.get(key);
					if (thisList.contains(name) || thisList.contains("*")) {
						// feature is supported in version 'foundVersion'
						if (foundVersion.contains(cRangeSeparator)) { //interval specified
							foundVersion = foundVersion.substring(0, foundVersion.indexOf(cRangeSeparator));
						}
						minVersion = lowerBabelfishVersion(foundVersion, minVersion);
					}
				}
			}
		}
		return minVersion.isEmpty() ? u.NotSupported : minVersion;
	}

	public static String featureSupportedInVersion(String requestVersion, String section) {
		String status = u.NotSupported;
		section = section.toUpperCase();
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " entry: section=[" + section + "] requestVersion=[" + requestVersion + "] ", u.debugCfg);

		if (featureExists(section)) {
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " featureExists: section=[" + section + "] requestVersion=[" + requestVersion + "] featureDefault=[" + status + "] ", u.debugCfg);
			Map<String, List<String>> featureList = sectionList.get(section);
			for (String key : featureList.keySet()) {
				if (key.startsWith(supportedTag + "/")) {
					String foundVersion = key.substring(supportedTag.length() + 1);
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() +"key=[" + key + "] foundVersion=[" + foundVersion + "] ", u.debugCfg);
					if (foundVersion.isEmpty()) continue;
					if (isVersionSupported(requestVersion, foundVersion)) {
						status = u.Supported;
						break;
					}
				}
			}
			if (!status.equals(u.Supported)) {
				status = featureDefaultStatus(section);				
			}					
		}
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " return: status=[" + status + "] ", u.debugCfg);
		return status;
	}

	// gets the list of supported values for the version specified (not cumulative, only for the most recent supported version)
	public static int featureIntValueSupportedInVersion(String requestVersion, String section) {
		int result = -1;

		if (!featureExists(section)) {
			u.appOutput("Section [" + section + "] not found in .cfg file");
			u.errorExit();
		}

		String s = featureValueSupportedInVersion(requestVersion, section);
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "section=[" + section + "] requestVersion=[" + requestVersion + "] s=[" + s + "] ", u.debugCfg);

		try {
			result = Integer.parseInt(s);
		} catch (Exception ignored) { }

		if (result < 0) {
			u.appOutput("Value in [" + section + "]  must be a number: [" + s + "]");
			u.errorExit();
		}
		return result;
	}

	// gets the list of supported values for the version specified (not cumulative, only for the most recent supported version)
	public static String featureValueSupportedInVersion(String requestVersion, String section) {
		String value = "";
		section = section.toUpperCase();
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " entry: section=[" + section + "] requestVersion=[" + requestVersion + "] ", u.debugCfg);

		if (featureExists(section)) {
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " featureExists: section=[" + section + "] requestVersion=[" + requestVersion + "] ", u.debugCfg);
			Map<String, List<String>> featureList = sectionList.get(section);
			for (String key : featureList.keySet()) {
				if (key.startsWith(supportedTag + "/")) {
					String foundVersion = key.substring(supportedTag.length() + 1);
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() +"key=[" + key + "] foundVersion=[" + foundVersion + "] ", u.debugCfg);
					if (foundVersion.isEmpty()) continue;

					if (isVersionSupported(requestVersion, foundVersion)) {
						value = String.join(",", featureList.get(key));
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " featurelist=[" + featureList.get(key) + "]  value=[" + value + "] ", u.debugCfg);
						break;
					}
				}
			}
		}
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " return: value=[" + value + "]  ", u.debugCfg);
		return value;
	}
	
	// get the list of items in the 'list=' key
	public static List<String> featureValueList(String section) {
		section = section.toUpperCase();
		if (sectionExists(section)) {
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " featureExists: section=[" + section + "]  ", u.debugCfg);
			Map<String, List<String>> featureList = sectionList.get(section);
			String key = createKey(listValuesTag);
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "key=[" + key + "]", u.debugCfg);
			if (featureList.containsKey(key)) {
				List<String> allItems = featureList.get(key);
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "featureList containsKey [" + key + "] allItems=[" + allItems + "]", u.debugCfg);
				assert allItems != null : "list= items for section=[" + section + "] is empty";
				return allItems;
			}
		}
		return null;
	}

	// is this feature (section, name) supported in the specificed version?
	public static String featureSupportedInVersion(String requestVersion, String section, String name) {
		String status = u.NotSupported;
		name = name.toUpperCase();
		section = section.toUpperCase();
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " entry: section=[" + section + "] name=[" + name + "]  requestVersion=[" + requestVersion + "] ", u.debugCfg);
		assert (!name.isEmpty()) : "name argument cannot be blank";

		if (featureExists(section, name)) {			
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " featureExists: section=[" + section + "] name=[" + name + "]  requestVersion=[" + requestVersion + "]  featureDefault=[" + status + "] ", u.debugCfg);
			Map<String, List<String>> featureList = sectionList.get(section);
			for (String key : featureList.keySet()) {
				if (key.startsWith(supportedTag + "/")) {
					String foundVersion = key.substring(supportedTag.length() + 1);
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() +"key=[" + key + "] ", u.debugCfg);
					if (foundVersion.isEmpty()) continue;
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "foundVersion=[" + foundVersion + "] ", u.debugCfg);

					List<String> thisList = featureList.get(key);
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() +"featureList Key=[" + key + "] (" + thisList.size() + ") --> " + thisList, u.debugCfg);
					if (thisList.contains(name) || thisList.contains("*")) {
						if (isVersionSupported(requestVersion, foundVersion)) {
							status = u.Supported;
							break;
						}
					}
				}
			}
			if (!status.equals(u.Supported)) {
				status = featureDefaultStatus(section, name);				
			}
		}
		return status;
	}

	// first check if an entry 'option=value' exists; if not, try 'option' on its own
	public static String featureSupportedInVersion(String requestVersion,String section, String name, String optionValue) {
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " entry: section=[" + section + "]  name=[" + name + "]  optionValue=["+optionValue+"]  ", u.debugCfg);
		section = section.toUpperCase();
		String status = u.NotSupported;
		if (!optionValue.isEmpty()) {
			String option = name+"="+optionValue;
			if (featureExists(section, option)) {
				status = featureSupportedInVersion(requestVersion, section, option);
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "option=value found: status=["+status+"] ", u.debugCfg);		
			}
		}
		else if (featureExists(section, name)) {
			status = featureSupportedInVersion(requestVersion, section, name);
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "option found: status=["+status+"] ", u.debugCfg);		
		}
		if (!status.equals(u.Supported)) {
			status = featureDefaultStatus(section, name);				
		}		
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "return status=["+status+"] ", u.debugCfg);		
		return status;
	}	

	public static boolean isVersionSupported(String requestVersion, String testVersion) {
		boolean isSupported = false;
		String testVersionMax = "";
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " entry: requestVersion=[" + requestVersion + "] testVersion=[" + testVersion + "] ", u.debugCfg);

		// feature is supported in version 'testVersion', but there could be an interval
		if (testVersion.contains(cRangeSeparator)) { //interval specified
			testVersionMax = testVersion.substring(testVersion.indexOf(cRangeSeparator) + 1);
			testVersion = testVersion.substring(0, testVersion.indexOf(cRangeSeparator));
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "testVersion=[" + testVersion + "] testVersionMax=[" + testVersionMax + "] ", u.debugCfg);
		}
		if (testVersionMax.isEmpty() && isLowerOrEqualBabelfishVersion(testVersion, requestVersion)) {
			// yes, it is supported in the requested version
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "found lower/equal version: testVersion=[" + testVersion + "] requestVersion=[" + requestVersion + "] ", u.debugCfg);
			isSupported = true;
		} 
		else {
			if (!testVersionMax.isEmpty()) {
				// does the requested version match the interval?
				if (isLowerOrEqualBabelfishVersion(testVersion, requestVersion) &&
						isLowerOrEqualBabelfishVersion(requestVersion, testVersionMax)) {
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "found version in interval: testVersion=[" + testVersion + "] testVersionMax=[" + testVersionMax + "]  requestVersion=[" + requestVersion + "] ", u.debugCfg);
					isSupported = true;
				}
			}
		}
		return isSupported;
	}

	// TODO use section.toUpperCase() if called from outside (not by a Config method) -- but this is not currently the case
	public static String featureDefaultStatus(String section) {
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "entry: section=["+section+"]", u.debugCfg);			
		String status = u.NotSupported;
		section = section.toUpperCase();
		Map<String, List<String>> featureList = sectionList.get(section);
		String key = createKey(defaultStatusTag);
		List<String> thisList = featureList.get(key);
		if (thisList != null) {
			status = thisList.get(0);
		}
		
		// is there an override?
		if (sectionOverrideList.containsKey(section)) {
			String statusOrig = status;
			Map<String, List<String>> featureOverrideList = sectionOverrideList.get(section);
			String overrideKey = createKey(defaultStatusTag);
			List<String> thisOverrideList = featureOverrideList.get(overrideKey);
			if (thisOverrideList != null) {
				status = thisOverrideList.get(0);
				u.logStatusOverride(statusOrig, status, section);
			}		
		}
				
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "result: status=["+status+"] section=["+section+"] ", u.debugCfg);		
		return status;
	}

	// TODO use section.toUpperCase() if called from outside (not by a Config method) -- but this is not currently the case
	public static String featureDefaultStatus(String section, String name) {    
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "entry: section=["+section+"] name=["+name+"] ", u.debugCfg);		
		String status = u.NotSupported;
		section = section.toUpperCase();
		name = name.toUpperCase();
		Map<String, List<String>> featureList = sectionList.get(section.toUpperCase());

		for (String key : u.defaultClassificationsKeys) {
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " key=[" + key + "] featureList=["+featureList+"] ", u.debugCfg);
			if (featureList.containsKey(key)) {
				List<String> thisList = featureList.get(key);
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " found key=[" + key + "] thisList=[" + thisList + "] ", u.debugCfg);
				if (key.equals(defaultStatusTag)) {
					status = thisList.get(0);
					// keep searching for a more specific key
				} 
				else if (thisList.contains(name)) {
					status = key.substring(defaultStatusTag.length() + 1);
					break;
				}
			}
		}
		
		// is there an override?
		if (sectionOverrideList.containsKey(section)) {
			String statusOrig = status;
			String overrideStatus = "";
			Map<String, List<String>> featureOverrideList = sectionOverrideList.get(section);
			for (String key : u.overrideClassificationsKeys) {
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " override key=[" + key + "]", u.debugCfg);
				if (featureOverrideList.containsKey(key)) {
					List<String> thisList = featureOverrideList.get(key);
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " found override key=[" + key + "] thisList=[" + thisList + "] ", u.debugCfg);
					if (key.equals(defaultStatusTag)) {
						status = thisList.get(0);
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " section override found for key=[" + key + "] status=["+status+"]  ", u.debugCfg);
						overrideStatus = status;
						// keep searching for a more specific key
					} 
					else if (thisList.contains(name)) {
						status = key.substring(defaultStatusTag.length() + 1);
						overrideStatus = status;
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " section+name override found for key=[" + key + "] status=["+status+"]  ", u.debugCfg);
						break;
					}
				}
			}
			if (!overrideStatus.isEmpty()) {
				u.logStatusOverride(statusOrig, status, section, name);
			}			
		}
		
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "result: status=["+status+"] section=["+section+"] name=["+name+"] ", u.debugCfg);
		return status;
	}

	// TODO use section.toUpperCase() if called from outside
	public static String createKey (String key) {
		return key;
	}

	public static String createKey (String key1, String key2) {
		return createKey(key1) + "/" + createKey(key2);
	}

	public static String createKey (String key1, String key2, String key3) {
		return createKey(key1, createKey(key2, key3));
	}

	public static String createKey (String key1, String key2, String key3, String key4) {
		return createKey(key1, createKey(key2, createKey(key3, key4)));
	}

	// TODO use section.toUpperCase() if called from outside
	public static boolean sectionExists(String section) {
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "entry: section=[" + section + "] ", u.debugCfg);
		boolean result = sectionList.containsKey(section);
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "result: section=[" + section + "] result=[" + result + "] ", u.debugCfg);
		return result;
	}

	// does a feature exist in the .cfg file?
	public static boolean featureExists(String section) {
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "entry: section=[" + section + "] ", u.debugCfg);
		return sectionExists(section.toUpperCase());
	}

	// does this feature (section, name) exist in the .cfg file?
	public static boolean featureExists(String section, String name) {
		boolean result = false;
		name = name.toUpperCase();
		section = section.toUpperCase();
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "entry: section=[" + section + "] name=[" + name + "]", u.debugCfg);
		if (sectionExists(section)) {
			Map<String, List<String>> featureList = sectionList.get(section);
			String key = createKey(listValuesTag);
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "key=[" + key + "]", u.debugCfg);
			if (featureList.containsKey(key)) {
				List<String> allItems = featureList.get(key);
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "featureList containsKey [" + key + "] allItems=[" + allItems + "]", u.debugCfg);
				if (allItems != null) {
					if (allItems.contains(name)) {
						result = true;
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "match found for name=[" + name + "] ", u.debugCfg);
					} 
					else {
						// does this section have a wildcard?
						if (featureList.containsKey(createKey(wildcardTag))) {
							if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "section=[" + section + "] has wildcards", u.debugCfg);
							if (matchWildcard(section, name, allItems)) {
								if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "wildcard match found for name=[" + name + "] ", u.debugCfg);
								result = true;
							}
						}
					}
				}
			} 
			else {
				// the feature exists, but has no list= key (not needed for many features)
				result = true;
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "key [" + key + "] NOT found, but feature exists", u.debugCfg);
			}
		}
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "result=[" + result + "] ", u.debugCfg);
		return result;
	}

	// does this feature have a particular argument whose value needs validating?
	// at this point, featureExists() has already returned true
	public static String featureExistsArg(String section) {
		String argN = "";
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "entry: section=[" + section + "] ", u.debugCfg);
		section = section.toUpperCase();
		Map<String, List<String>> featureList = sectionList.get(section);
		if (featureList == null) {
			return argN;
		}
		for (String key : featureList.keySet()) {
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "key=[" + key + "] ", u.debugCfg);
			if (key.startsWith(supportedTag + "/")) {
				int argIndex = key.lastIndexOf("/ARG");
				if (argIndex != -1 && argIndex < key.length() - 4) {
					String foundArgN = key.substring(argIndex + 1);
					int argNum;
					try {
						argNum = Integer.parseInt(foundArgN.substring(3));
					} catch (Exception e) {
						continue;
					}
					if (argNum <= 0) {
						continue;
					}
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() +"foundArgN=[" + foundArgN + "] ", u.debugCfg);
					argN = foundArgN;
					break;
				}
			}
		}
		return argN;
	}

	// what is the reporting group for this feature?
	public static String featureGroup(String section, String name) {
		String group = "";
		section = section.toUpperCase();
		name = name.toUpperCase();
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " entry: section=[" + section + "] name=[" + name + "] ", u.debugCfg);

		if (featureExists(section, name)) {
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " featureExists: section=[" + section + "] name=[" + name + "] ", u.debugCfg);
			Map<String, List<String>> featureList = sectionList.get(section);
			for (String key : featureList.keySet()) {
				if (!key.startsWith(reportGroupTag)) continue;
				
				String foundGroup = key.substring(reportGroupTag.length());				
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " key=[" + key + "] foundGroup=[" + foundGroup + "] ", u.debugCfg);
				if (foundGroup.isEmpty()) {
					// e.g. 'report_group=Built-in functions', applies to all items
					List<String> thisList = featureList.get(key);
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " generic: foundGroup=[" + foundGroup + "] thisList=[" + thisList + "] ", u.debugCfg);
					group = thisList.get(0);
					//keep searching if there is a more specific group
				} 
				else if (foundGroup.charAt(0) != subKeySeparator.charAt(0)) {
					// should not happen
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " Unexpected branch, foundGroup=[" + foundGroup + "] ", u.debugCfg);
				} 
				else {
					// 'report_group-Fulltext Search=CONTAINSTABLE,FREETEXTTABLE,SEMANTICKEYPHRASETABLE[,...]'
					// applies only to the actual names listed
					List<String> thisList = featureList.get(key);
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " foundGroup=[" + foundGroup + "]  thisList=[" + thisList + "] ", u.debugCfg);
					if (thisList.contains(name)) {
						// this feature should be reported under 'foundGroup'
						foundGroup = u.getPatternGroup(foundGroup, "^\\" + subKeySeparator + "(.*?)(\\=|$)", 1); // TODO substring until equal or end (indexOf = -1)
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " foundGroup=[" + foundGroup + "] ", u.debugCfg);
						group = foundGroup;
						break;
					}
				}
			}
		}
		
		// is there an override?
		if (sectionOverrideList.containsKey(section)) {
			String groupOrig = group;
			String overrideGroup = "";			
			Map<String, List<String>> featureOverrideList = sectionOverrideList.get(section);
			for (String key : featureOverrideList.keySet()) {
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " override key=[" + key + "]", u.debugCfg);				
				if (!key.startsWith(reportGroupTag)) continue;
			
				List<String> thisList = featureOverrideList.get(key);
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " found override key=[" + key + "] thisList=[" + thisList + "] ", u.debugCfg);
				if (key.equals(reportGroupTag)) {
					group = thisList.get(0);
					overrideGroup = group;
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " section override found for key=[" + key + "] group=["+group+"]  ", u.debugCfg);
					// keep searching for a more specific key
				} 
				else if (thisList.contains(name)) {
					group = key.substring(reportGroupTag.length() + 1);
					overrideGroup = group;
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " section+name override found for key=[" + key + "] sgroup=["+group+"]  ", u.debugCfg);
					break;
				}
			}	
			if (!overrideGroup.isEmpty()) {
				u.logGroupOverride(groupOrig, group, section, name);
			}						
		}
		
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " return: group=[" + group + "]  ", u.debugCfg);
		return group;
	}

	// what is the reporting group for this feature?
	public static String featureGroup(String section) {
		String group = "";
		section = section.toUpperCase();
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " entry: section=[" + section + "] ", u.debugCfg);

		if (featureExists(section)) {
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " featureExists: section=[" + section + "] ", u.debugCfg);
			Map<String, List<String>> featureList = sectionList.get(section);
			String key = reportGroupTag;
			if (featureList.containsKey(key)) {
				List<String> thisList = featureList.get(key);
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " foundGroup=[" + key + "] thisList=[" + thisList + "] ", u.debugCfg);
				// e.g. 'report_group=Built-in functions'
				group = thisList.get(0);
			}
		}
		
		// is there an override?
		if (sectionOverrideList.containsKey(section)) {
			String groupOrig = group;
			Map<String, List<String>> featureOverrideList = sectionOverrideList.get(section);
			String overrideKey = createKey(reportGroupTag);
			List<String> thisOverrideList = featureOverrideList.get(overrideKey);
			if (thisOverrideList != null) {
				group = thisOverrideList.get(0);
				u.logGroupOverride(groupOrig, group, section);
			}		
		}
				
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + " return: group=[" + group + "] ", u.debugCfg);
		return group;
	}

	// debug: dump all config keys
	public static void dumpCfg() {
		int nrKeys = 0;
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "=== dumping config ================= ", u.debugCfg);
		if (u.debugging) u.dbgOutput("=== sections: ================= ", u.debugCfg);
		for (String section : sectionList.keySet()) {
			if (u.debugging) u.dbgOutput("  section=[" + section + "] ", u.debugCfg);
		}
		if (u.debugging) u.dbgOutput("=== Nr. sections: " + sectionList.size() + " ================= ", u.debugCfg);
		if (u.debugging) u.dbgOutput("", u.debugCfg);
		if (u.debugging) u.dbgOutput("=== keys: ================= ", u.debugCfg);

		for (String sectionName : sectionList.keySet()) {
			Map<String, List<String>> featureList = sectionList.get(sectionName);
			for (String key : featureList.keySet()) {
				nrKeys++;
				List<String> thisList = featureList.get(key);
				if (u.debugging) u.dbgOutput("  key=[" + sectionName + "/" + key + "] item list=[" + thisList + "]  ", u.debugCfg);
			}
		}
		if (u.debugging) u.dbgOutput("=== Nr. keys: " + nrKeys + " ================= ", u.debugCfg);
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "======= end config ================= ", u.debugCfg);
	}

	private static boolean matchWildcard(String sectionName, String s, List<String> allItems) {
		for (String w : allItems) {		// TODO This can probably be improved (if needed)
			if (w.contains(wildcardChar)) {
				String wCopy = w;
				w = w.replaceAll(wildcardChar, ".*");	// TODO Can this be done only once (when the list is created)?
				if (u.PatternMatches(s, w)) {
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "matched [" + s + "] with [" + wCopy + "] in sectionName=[" + sectionName + "] ", u.debugCfg);
					return true;
				}
				else {
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "wildcard NOmatch!", u.debugCfg);
				}
			}
		}
		return false;
	}

	// read the .cfg file
	private static void readCfgFile(String pCfgFileName) throws Exception {
		configFileName = pCfgFileName;
		configFilePathName = Paths.get(pCfgFileName).toAbsolutePath().toString();

        cfgFile = new File(configFileName);
        if (!cfgFile.exists()) {
        	u.appOutput("Babelfish configuration file not found: "+configFilePathName);
         	u.errorExit();
        }
		u.appOutput("Reading "+configFileName);

        cfg = getCfg(configFileName);

		// sanity check, expecting at least a particular number of entries. '25' is arbitrarily chosen here
        if (cfg.keySet().size() < 25) {
        	cfgFileValid = false;
        	cfgOutput("file invalid, only "+cfg.keySet().size()+" sections found; expecting many more");
			return;
        }

		int sectionCount = 0;
        for (String sectionName: cfg.keySet()) {
			sectionCount++;
			Map<String, String> section = cfg.get(sectionName);
			if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "sectionName=[" + sectionName + "]", u.debugCfg);
			if (sectionCount == 1 && !sectionName.equals(Babelfish_Compass_Name)) {
				cfgFileValid = false;
				cfgOutput("first section must be [" + Babelfish_Compass_Name + "]");
				return;  //just don't proceed;
			}

			if (sectionCount == 2) {
				if (cfgFileValid) {
					if (!isValidBabelfishVersion(CompassUtilities.baseBabelfishVersion)) {
						cfgFileValid = false;
						cfgOutput("version " + CompassUtilities.baseBabelfishVersion + " not found.");
					}
				}

				if (!cfgFileValid) {
					return; // just don't proceed if versions are not correct
				}
			}

			for (String optionKey : section.keySet()) {
				String optionVal = section.get(optionKey);
				String optionKeyCopy = optionKey;
				optionKey = optionKey.toUpperCase();
				String thisKey = createKey(optionKey);
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() +"section=[" + sectionName + "] optionKey=[" + optionKey + "] thisKey=[" + thisKey + "]  val=[" + optionVal + "]", u.debugCfg);

				if (sectionCount == 1) {
					// first section defines valid Babelfish versions:
					if (optionKey.equals(validVersionsTag)) {
						Babelfish_VersionList = new ArrayList<>(Arrays.asList(optionVal.split(",")));
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "Valid versions (" + Babelfish_VersionList.size() + ") :" + Babelfish_VersionList, u.debugCfg);
						
						// strip off any spaces
						for (int i = 0; i<Babelfish_VersionList.size(); i++) {
							Babelfish_VersionList.set(i, Babelfish_VersionList.get(i).trim());
						}						
						
						for (String v : Babelfish_VersionList) {
							if (!u.PatternMatches(v, "\\d+(\\.\\d+)*")) {
								cfgOutput("[" + Babelfish_Compass_Name + "]: Invalid version number found: [" + v + "]");
								cfgFileValid = false;
							} 
							else {
								if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "   version=[" + v + "] => [" + normalizedBabelfishVersion(v) + "]", u.debugCfg);
							}
						}
						continue;
					}

					// first section also contains .cfg file format version
					else if (optionKey.equals(fileFormatTag)) {
						try {
							u.cfgFileFormatVersionRead = Integer.parseInt(optionVal);
						} catch (Exception e) {
							cfgOutput(fileFormatTag + " key has invalid version number; must be integer > 0");
							u.errorExit(); // no point in continuing
						}
						if (u.cfgFileFormatVersionRead <= 0) {
							cfgOutput(fileFormatTag + " key has invalid version number; must be integer > 0");
							u.errorExit(); // no point in continuing
						}
						continue;
					}
					// first section also contains .cfg file timestamp
					else if (optionKey.equals(fileTimestampTag)) {
						String monthList = "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";
						u.cfgFileTimestamp = optionVal;
						if (u.getPatternGroup(optionVal, "^((\\d+\\-)?"+monthList+"\\-20\\d\\d)$", 1).isEmpty()) {
							cfgOutput(fileTimestampTag + " key has invalid format.");
							cfgOutput("Format must be: "+monthList+"-20xx");
							u.errorExit(); // no point in continuing							
						}
						continue;
					}
				}

				// key: list all values for a section:
				if (optionKey.equals(listValuesTag)) {
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "list of section=[" + sectionName + "] = [" + optionVal + "]", u.debugCfg);
				}
				// key: 'supported' values
				else if (optionKey.startsWith(supportedTag)) {
					String vRaw = optionKey.substring(supportedTag.length() + 1);
					String v = "", vMax = "";
					Matcher matcher = u.getMatcher(vRaw, "^([\\d]+(\\.([\\d]+|\\*))+)(" + cRangeSeparator + "([\\d]+(\\.([\\d]+|\\*))+))?$");
					if (matcher != null && matcher.find()) {
						v = matcher.group(1);
						vMax = matcher.group(5);
						if (vMax == null) {
							vMax = "";
						}
					}
					if (u.debugging) u.dbgOutput(supportedTag + "-version key: optionKey=[" + optionKey + "] vRaw=[" + vRaw + "] version=[" + v + "] vMax=[" + vMax + "] ", u.debugCfg);

					if (v.isEmpty()) {
						cfgFileValid = false;
						cfgOutput("Invalid version (empty) in [" + sectionName + "/" + optionKey + "]");
						continue;
					}

					if (!isValidBabelfishVersion(v)) {
						cfgFileValid = false;
						versionInvalid = true;
						cfgOutput("Invalid version '" + v + "' in [" + sectionName + "/" + optionKey + "]");
						continue;
					}

					if (!vMax.isEmpty()) {
						if (!isValidBabelfishVersionWithStar(vMax)) {
							cfgFileValid = false;
							versionInvalid = true;
							cfgOutput("Invalid version '" + vMax + "' in [" + sectionName + "/" + optionKey + "]");
							continue;
						}
						if (!isLowerOrEqualBabelfishVersion(v, vMax)) {
							cfgFileValid = false;
							cfgOutput("Version " + vMax + " cannot be lower than " + v + " in [" + sectionName + "/" + optionKey + "]");
							continue;
						}
						v = v + ("-") + (vMax);
					}

					// key: supported=argN=
					thisKey = createKey(supportedTag, v);
					int equalIndex = optionVal.indexOf("=", 4);
					if (equalIndex != -1 && optionVal.substring(0, 3).equalsIgnoreCase("arg")) {
						String argN = optionVal.substring(0, equalIndex).toUpperCase();
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "argN=[" + argN + "] ", u.debugCfg);
						try {
							int argNum = Integer.parseInt(argN.substring(3));
							if (argNum <= 0) {
								cfgFileValid = false;
								cfgOutput("Section [" + sectionName + "/" + optionVal + "]: Invalid 'arg0' specified: must be 'argN' (N=1,2,3...)");
							}
						} catch (Exception e) {
							cfgOutput("Section [" + sectionName + "/" + optionVal + "]: Invalid '" + argN + "' specified: must be 'argN' (N=1,2,3...)");
							cfgFileValid = false;
						}
						thisKey = createKey(thisKey, argN);
						optionVal = optionVal.substring(equalIndex + 1);

						featureArgOptions.put(sectionName.toUpperCase(), argN);
						if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "adding featureArgOptions for argKey=[" + sectionName + "] argN=[" + argN + "]", u.debugCfg);
					}
				}
				// key: 'default_classification' items
				else if (optionKey.startsWith(defaultStatusTag)) {
					// values in supportOptionsCfgFile have been converted to uppercase
					if (optionKey.equals(defaultStatusTag)) {
						if (u.validSupportOptionsCfgFile.contains(optionVal.toUpperCase())) {
							optionVal = u.supportOptions.get(supportOptionsCfgFileUpperCase.indexOf(optionVal.toUpperCase()));
						} 
						else {
							cfgOutput("Section [" + sectionName + "]: key " + defaultStatusTag + " has invalid value [" + optionVal + "]. Valid values are " + u.validSupportOptionsCfgFileOrig);
							cfgFileValid = false;
						}
					} 
					else if (!u.defaultClassificationsKeys.contains(optionKey)) {
						cfgOutput("Section [" + sectionName + "]: key " + optionKey + " is invalid. Valid values are " + u.defaultClassificationsKeysOrig);
						cfgFileValid = false;
					}
					else {
						if (optionVal.equals("*")) {
							// default_classification-xxx=* ==> change to: default_classification=xxx
							optionVal = optionKey.substring(defaultStatusTag.length() + 1);
							optionKey = optionKey.substring(0,defaultStatusTag.length());
							thisKey = createKey(optionKey);
						}
					}					
				}
				// key: 'report_group', 'report_group-XYZ'
				else if (optionKey.startsWith(reportGroupTag)) {							
					if (optionVal.isEmpty()) {
						cfgOutput("Invalid key '" + reportGroupTag + "=': value cannot be blank");
						cfgFileValid = false;
					} 
					else if (optionVal.equals("*")) {
						if (optionKey.length() > reportGroupTag.length()) {
							// report_group-xxx=* ==> change to: report_group=xxx
							optionVal = optionKey.substring(reportGroupTag.length() + 1);
							optionKey = optionKey.substring(0,reportGroupTag.length());
							thisKey = createKey(optionKey);				
						}
						else {
							cfgOutput("Invalid key '" + reportGroupTag + "=': value must be a group name, not '*'");
							cfgFileValid = false;
						}
					}
					// do not lowercase the group name when it is part of the key
					if (optionKey.length() > reportGroupTag.length()) {
						String groupName = optionKeyCopy.substring(reportGroupTag.length() + 1);
						// the key is in uppercase, but we want the group name to preserve the case, so patch it in
						thisKey = reportGroupTag + subKeySeparator + groupName; // TODO This probably works, but test this if there are still issues
					}
				}
				// key: 'rule'
				else if (optionKey.equals(ruleTag)) {
					//not yet processed
				} 
				else {
					// catchall
					cfgOutput("Invalid key '" + optionKey + "'");
					cfgFileValid = false;
					break;
				}

				List<String> theseItems = new ArrayList<>(Arrays.asList(optionVal.split(",")));
				if (!optionKey.equals(reportGroupTag)) {
					u.listToUpperCase(theseItems);
				}

				Map<String, List<String>> featureList;

				if (!sectionList.containsKey(sectionName.toUpperCase())) {
					sectionList.put(sectionName.toUpperCase(), new LinkedHashMap<>());
				}
				featureList = sectionList.get(sectionName.toUpperCase());
				featureList.put(thisKey, theseItems);
				if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "added key: [" + thisKey + "] optionKey=[" + optionKey + "] of section=[" + sectionName + "] = [" + optionVal + "]", u.debugCfg);

					// check items in 'supported-XXX', 'report_group-xxx', default_classification-XXX'  are in the listValues key (if a list exists)
				if (optionKey.startsWith(supportedTag+subKeySeparator) || 
				    optionKey.startsWith(reportGroupTag+subKeySeparator) ||
				    optionKey.startsWith(defaultStatusTag+subKeySeparator)
				   ) {					
					boolean itemValid = validateItemsListed("", optionKey, optionVal, thisKey, sectionName, featureList, theseItems);				
					cfgFileValid = cfgFileValid & itemValid;
				}
			}
		}
	}
	
	// check items in a non-list key are in the listValues key (if a list exists)
	private static boolean validateItemsListed (String cfgFileName, String optionKey, String optionVal, String thisKey, String sectionName, Map<String, List<String>> featureList, List<String> theseItems) {
		boolean isValid = true;
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "check items are in list=: key [" + optionKey + "] of section=[" + sectionName + "] = [" + optionVal + "]", u.debugCfg);
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "thisKey=[" + thisKey + "] -> [" + sectionName + "] : " + theseItems.size() + " items: " + theseItems, u.debugCfg);
		List<String> allItems = featureList.get(createKey(listValuesTag));
		if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "allItems=["+allItems+"] " + theseItems, u.debugCfg);
		if (allItems != null) {
			boolean hasWildcard = false;
			for (String allItem : allItems) {
				if (allItem.contains(wildcardChar)) {
					if (u.debugging) u.dbgOutput(CompassUtilities.thisProc() + "wildcard found for sectionName=[" + sectionName + "] in [" + allItem + "]", u.debugCfg);
					hasWildcard = true;
					String wildcardKey = createKey(wildcardTag);
					featureList.put(wildcardKey, null);  // only need key to exist
					break;
				}
			}
			for (String item : theseItems) {
				if (!item.equals("*")) {
					// does the value match a value in the list?
					if (!allItems.contains(item)) {
						// does the value match a wildcard?
						if (hasWildcard && matchWildcard(sectionName, item, allItems)) {
							continue;
						}
						// if we get here, the item was not listed
						allItems.add(item);
						isValid = false;
						cfgOutput(cfgFileName, "[" + thisKey + "] has item " + item + ", but this is not listed in '" + listValuesTag + "='");
					}
				}
			}
		}		
		return isValid;
	}

	private static void printError(int nrLine, String line, String msg) {
		if (!line.isEmpty()) line = ":" + line;
		cfgOutput("Configuration file error at line " + nrLine + ". " + msg + line);
		cfgFileValid = false;
		u.errorExit();
	}

	private static void updateChecksum(Checksum checksum, String s) {
		byte[] bytes = s.getBytes();
		checksum.update(bytes, 0, bytes.length);
	}

	// LinkedHashMap is used because insertion order is important
	private static Map<String, Map<String, String>> getCfg(String configFileName) throws IOException {
		return getCfg(configFileName, false);
	}
	private static Map<String, Map<String, String>> getCfg(String configFileName, boolean isUserCfg) throws IOException {
		String COMMENT_CHARS = "#;", SEPARATOR = ",", BEFORE_CHECKSUM="#file checksum=";
		char START_SECTION = '[', END_SECTION = ']';
		String line, separator = System.lineSeparator(), fileChecksum = null, sectionName = null;
		StringBuilder content = new StringBuilder();
		Checksum checksum = new CRC32();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(configFileName)));
		Map<String, Map<String, String>> sections = new LinkedHashMap<>();
		Map<String, String> section = null;
		int CHECKSUM_LENGTH = 8;
		int nrLine = 0, nrChecksumLine = -1;

		while ((line = br.readLine()) != null) {
			nrLine++;
			if (u.configOnly) {
				content.append(line).append(separator);
			}
			if (line.startsWith(BEFORE_CHECKSUM)) {
				fileChecksum = line.substring(BEFORE_CHECKSUM.length());
				nrChecksumLine = nrLine;
				continue;
			}
			StringBuilder lineSB = new StringBuilder(line);
			int commentIndex = lineSB.length();
			for (char commentChar : COMMENT_CHARS.toCharArray()) {
				int index = -1;
				do {
					index = lineSB.indexOf(Character.toString(commentChar), index + 1);
					if (index != -1 && index < commentIndex) {
						if (index == commentIndex - 1 || lineSB.charAt(index + 1) != commentChar) {	// This is a comment
							commentIndex = index;
							break;
						}
						lineSB.deleteCharAt(index);	// Two consecutive commentChars were found, so it's not a comment
						commentIndex--;
					}
				} while (index != -1);
			}
			line = lineSB.substring(0, commentIndex).trim();
			if (line.isEmpty()) {
				continue;
			}
			char firstChar = line.charAt(0);
			if (firstChar == START_SECTION) {
				int end = line.indexOf(END_SECTION);
				if (end == -1) {
					printError(nrLine, line, "End character " + END_SECTION + " is missing");
				}
				sectionName = line.substring(1, end).trim();
				cfgSections.add(sectionName);
				if (sections.containsKey(sectionName)) {
					printError(nrLine, line, "Section " + sectionName + " is already defined");
				}
				section = new LinkedHashMap<>();
				sections.put(sectionName, section);
				updateChecksum(checksum, sectionName);
				//u.appOutput(CompassUtilities.thisProc()+"sectionName=["+sectionName+"] ");
				continue;
			}
			if (sectionName == null) {
				printError(nrLine, line, "No section defined before key-value pairs");
			}
			int equalSignIndex = line.indexOf('=');
			if (equalSignIndex == -1) {
				printError(nrLine, line, "Key-value pair expected");
			}
			if (equalSignIndex == 0) {
				printError(nrLine, line, "Wrong key-value pair. Missing key");
			}
			if (equalSignIndex == line.length() - 1) {
				printError(nrLine, line, "Wrong key-value pair. Missing value");
			}
			String key = line.substring(0, equalSignIndex);
			String[] arrayValues = line.substring(equalSignIndex + 1).split(SEPARATOR);
			List<String> values = new LinkedList<>(Arrays.asList(arrayValues));
			values.removeIf(val -> val.trim().isEmpty());
			String value = String.join(",", values);
//			if (key.equals("rule")) {
//			}
//			else {
//				u.appOutput(CompassUtilities.thisProc()+"key=["+key+"] value=["+value+"] ");
//			}
			assert (section != null)  :  CompassUtilities.thisProc()+"null section found while reading"; // shouldn't happen
			section.put(key, value);
			updateChecksum(checksum, key);
			updateChecksum(checksum, value);
		}
		br.close();
		String hexChecksum = Long.toHexString(checksum.getValue());
		while (hexChecksum.length() < CHECKSUM_LENGTH) {
			hexChecksum = "0" + hexChecksum;
		}
		if (u.configOnly) {
			if (System.getenv().containsKey("COMPASS_CHECKSUM") || System.getenv().containsKey("compass_checksum")) {
				if (nrChecksumLine == -1) {
					BufferedWriter bw = new BufferedWriter(new FileWriter(configFileName, true));
					bw.write("#-----------------------------------------------------------------------------------");
					bw.newLine();
					bw.write(BEFORE_CHECKSUM + hexChecksum);
					bw.newLine();
					bw.write("#--- end ---------------------------------------------------------------------------");
					bw.newLine();
					bw.close();
					cfgOutput("Checksum added: "+hexChecksum);
				}
				else {
					int pos = -1, limit = nrChecksumLine - 1, offset, endPos;
					for (int i = 0; i < limit; i++) {
						pos = content.indexOf(separator, pos + 1);
					}
					offset = pos + separator.length() + BEFORE_CHECKSUM.length();
					endPos = content.indexOf(separator, pos + 1);
					if (endPos == -1) {
						endPos = content.length();
					}
					content.replace(offset, endPos, hexChecksum);
					BufferedWriter bw = new BufferedWriter(new FileWriter(configFileName));
					bw.write(content.toString());
					bw.close();
					cfgOutput("Checksum updated: "+hexChecksum);
				}
			}
			else {
				u.appOutput("No action taken, envvar required");
			}
			u.errorExit(0, false);
		}
		if (!isUserCfg) {
			if (nrChecksumLine == -1) {
				printError(nrLine, "", "No checksum found");
			}
			if (fileChecksum.length() != CHECKSUM_LENGTH) {
				printError(nrChecksumLine, "", "Invalid checksum format");
			}
			if (!fileChecksum.equals(hexChecksum)) {
				printError(nrChecksumLine, "", "Invalid checksum. PLEASE DO NOT EDIT THIS FILE!");
			}
		}
		return sections;
	}	
}
