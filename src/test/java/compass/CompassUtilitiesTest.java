/*
Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/
package compass;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompassUtilitiesTest {

    @Test
    @DisplayName("Remove last n characters")
    void testRemoveLastChars() {
        assertNull(CompassUtilities.removeLastChar(null), "NULL retruns NULL");

        String shortenMe = "";
        String expected = "";
        String actual = CompassUtilities.removeLastChar(shortenMe);
        assertEquals(expected, actual, "Empty string returns empty string");

        shortenMe = "A";
        expected = "";
        actual = CompassUtilities.removeLastChars(shortenMe, 10);
        assertEquals(expected, actual, "Remove more characters than are in string returns empty string");

        shortenMe = "foo";
        expected = "foo";
        actual = CompassUtilities.removeLastChars(shortenMe, -1);
        assertEquals(expected, actual, "Negative number of characters to shorten string by is no op");

        shortenMe = "foobar";
        expected = "";
        actual = CompassUtilities.removeLastChars(shortenMe, 6);
        assertEquals(expected, actual, "Remove characters equal to length of string returns empty string");

        shortenMe = "foobar";
        expected = "foo";
        actual = CompassUtilities.removeLastChars(shortenMe, 3);
        assertEquals(expected, actual, "Golden path");
    }

    @Test
    @DisplayName("Capitalize first character")
    void testCapitalizeFirstChar() {
        assertNull(CompassUtilities.capitalizeFirstChar(null), "NULL returns NULL");

        String capitalizeMe = "";
        String expected = "";
        String actual = CompassUtilities.capitalizeFirstChar(capitalizeMe);
        assertEquals(expected, actual, "Empty string returns empty string");

        capitalizeMe = "a";
        expected = "A";
        actual = CompassUtilities.capitalizeFirstChar(capitalizeMe);
        assertEquals(expected, actual, "Single character string capitalized");

        capitalizeMe = "foo";
        expected = "Foo";
        actual = CompassUtilities.capitalizeFirstChar(capitalizeMe);
        assertEquals(expected, actual, "Golden path");

        capitalizeMe = " foo";
        expected = " foo";
        actual = CompassUtilities.capitalizeFirstChar(capitalizeMe);
        assertEquals(expected, actual, "Uncapitalizable first character");

        capitalizeMe = "Foo";
        expected = "Foo";
        actual = CompassUtilities.capitalizeFirstChar(capitalizeMe);
        assertEquals(expected, actual, "First capital maintained");

        capitalizeMe = "FOO";
        expected = "FOO";
        actual = CompassUtilities.capitalizeFirstChar(capitalizeMe);
        assertEquals(expected, actual, "Only first character effected");
    }

    @Test
    @DisplayName("All list elements upper case")
    void testListToUpperCase() {
        List<String> list = null;

        CompassUtilities.listToUpperCase(list);
        assertNull(list, "NULL returns NULL");

        list = new ArrayList<>();
        List<String> expected = new ArrayList<>();
        CompassUtilities.listToUpperCase(list);
        assertIterableEquals(expected, list, "Empty list returns empty list");

        list.add(null);
        expected.add(null);
        CompassUtilities.listToUpperCase(list);
        assertIterableEquals(expected, list, "NULL safe");

        list.set(0, "");
        expected.set(0, "");
        CompassUtilities.listToUpperCase(list);
        assertIterableEquals(expected, list, "Empty safe");

        list.set(0, "FOO");
        list.add("BAR");
        expected.set(0, "FOO");
        expected.add("BAR");
        CompassUtilities.listToUpperCase(list);
        assertIterableEquals(expected, list, "Already upper case");

        list.set(0, "foo");
        list.set(1, "bar");
        CompassUtilities.listToUpperCase(list);
        assertIterableEquals(expected, list, "Golden path");
    }

    @Test
    @DisplayName("All list elements lower case")
    void testListToLowerCase() {
        List<String> list = null;

        CompassUtilities.listToLowerCase(list);
        assertNull(list, "NULL returns NULL");

        list = new ArrayList<>();
        List<String> expected = new ArrayList<>();
        CompassUtilities.listToLowerCase(list);
        assertIterableEquals(expected, list, "Empty list returns empty list");

        list.add(null);
        expected.add(null);
        CompassUtilities.listToLowerCase(list);
        assertIterableEquals(expected, list, "NULL safe");

        list.set(0, "");
        expected.set(0, "");
        CompassUtilities.listToLowerCase(list);
        assertIterableEquals(expected, list, "Empty safe");

        list.set(0, "foo");
        list.add("bar");
        expected.set(0, "foo");
        expected.add("bar");
        CompassUtilities.listToLowerCase(list);
        assertIterableEquals(expected, list, "Already lower case");

        list.set(0, "FOO");
        list.set(1, "BAR");
        CompassUtilities.listToLowerCase(list);
        assertIterableEquals(expected, list, "Golden path");
    }

    @Test
    @DisplayName("Reverse string")
    void testReverseString() {
        assertNull(CompassUtilities.reverseString(null), "NULL returns NULL");

        String reverseMe = "";
        String expected = "";
        String actual = CompassUtilities.reverseString(reverseMe);
        assertEquals(expected, actual, "Empty returns empty");

        reverseMe = "A";
        expected = "A";
        actual = CompassUtilities.reverseString(reverseMe);
        assertEquals(expected, actual, "Single character returns itself");

        reverseMe = "()";
        expected = "()";
        actual = CompassUtilities.reverseString(reverseMe);
        assertEquals(expected, actual, "Parenthesis maintained");

        reverseMe = "foobar";
        expected = "raboof";
        actual = CompassUtilities.reverseString(reverseMe);
        assertEquals(expected, actual, "Golden path");

        reverseMe = "foo (bar)";
        expected = "(rab) oof";
        actual = CompassUtilities.reverseString(reverseMe);
        assertEquals(expected, actual, "Parenthesis swapped properly");
    }

    @Test
    @DisplayName("Validate report or application name")
    void testNameFormatValid() {
        assertThrows(IllegalArgumentException.class,
                () -> CompassUtilities.nameFormatValid(null, null),
                "Null nameType throws IllegalArgumentException"
        );

        assertThrows(IllegalArgumentException.class,
                () -> CompassUtilities.nameFormatValid("", null),
                "Blank nameType throws IllegalArgumentException"
        );

        assertThrows(IllegalArgumentException.class,
                () -> CompassUtilities.nameFormatValid("foobar", null),
                "nameType not \"report\" or \"appname\" throws IllegalArgumentException"
        );

        assertDoesNotThrow(
                () -> CompassUtilities.nameFormatValid("report", null),
                "nameType \"report\" doesn't throw IllegalArgumentException"
        );

        assertDoesNotThrow(
                () -> CompassUtilities.nameFormatValid("appname", null),
                "nameType \"appname\" doesn't throw IllegalArgumentException"
        );

        String name = null;
        String expected = "[empty name]";
        String actual = CompassUtilities.nameFormatValid("report", name);
        assertEquals(expected, actual, "NULL name returns error message");

        name = "";
        actual = CompassUtilities.nameFormatValid("report", name);
        assertEquals(expected, actual, "Empty name returns error message");

        name = " ";
        actual = CompassUtilities.nameFormatValid("report", name);
        assertEquals(expected, actual, "Blank name returns error message");

        name = "\\WindowsDirectorySeparator";
        expected = "'\\'";
        actual = CompassUtilities.nameFormatValid("report", name);
        assertEquals(expected, actual, "Name with Windows path separator returns error message");

        name = "/MacLinuxDirectorySeparator";
        expected = "'/'";
        actual = CompassUtilities.nameFormatValid("report", name);
        assertEquals(expected, actual, "Name with Mac/Linux path separator returns error message");

        name = "..\\WindowsParentDirectorySeparator";
        expected = "'..\\'";
        actual = CompassUtilities.nameFormatValid("report", name);
        assertEquals(expected, actual, "Name with Windows relative path separator returns error message");

        name = "../MacLinuxParentDirectorySeparator";
        expected = "'../'";
        actual = CompassUtilities.nameFormatValid("report", name);
        assertEquals(expected, actual, "Name with Mac/Linux relative path separator returns error message");

        for (int ascii = 32; ascii < 127; ascii++) {
            // Skip numbers, letters
            if ((ascii >= 48 && ascii <= 57) || (ascii >= 65 && ascii <= 90) || (ascii >= 97 && ascii <= 122)) {
                continue;
            }
            // Skip valid characters
            if (ascii == (int) '.' || ascii == (int) '-' || ascii == (int) '(' || ascii == (int) ')' || ascii == (int) '_') {
                continue;
            }
            // Skip slashes because we dealt with them above
            if (ascii == (int) '\\' || ascii == (int) '/') {
                continue;
            }
            String badChar = Character.toString((char) ascii);
            name = "NameWith" + badChar + "BadCharacter";
            expected = "[" + badChar + "]  (allowed characters: [A-Za-z0-9\\.-()_])";
            actual = CompassUtilities.nameFormatValid("appname", name);
            assertEquals(expected, actual, "Name with characters other than A-Za-z0-9.-()_ should return error message");
        }

        name = "Valid_Name(of)-Report.Or123Application";
        expected = "";
        actual = CompassUtilities.nameFormatValid("report", name);
        assertEquals(expected, actual, "Golden path");
    }
}
