/*
Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/

package compass;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class CompassInputFileFilterTest {

    @Test
    @DisplayName("File filter predicate")
    void testFilterPredicate() {
        Path root = FileSystems.getDefault().getRootDirectories().iterator().next();
        assertTrue(CompassInputFileFilter.test(Paths.get(root.toString(), "foo.sql")));
        assertTrue(CompassInputFileFilter.test(Paths.get( "foo.sql")));
        assertTrue(CompassInputFileFilter.test(Paths.get( "foo.TXT")));
        assertTrue(CompassInputFileFilter.test(Paths.get( "foo.Dat")));
        assertTrue(CompassInputFileFilter.test(Paths.get( "foo.xml")));
        assertTrue(CompassInputFileFilter.test(Paths.get(root.toString(), "a", "b", "c", "foo.sql")));
        assertFalse(CompassInputFileFilter.test(Paths.get("invalid")));
        assertFalse(CompassInputFileFilter.test(Paths.get("invalid.docx")));
    }

    @Test
    @DisplayName("Filename extension")
    void testGetFilenameExtension() {
        assertEquals("", CompassInputFileFilter.getFilenameExtension(null));
        assertEquals("", CompassInputFileFilter.getFilenameExtension(""));
        assertEquals("", CompassInputFileFilter.getFilenameExtension("C:\\"));
        assertEquals("", CompassInputFileFilter.getFilenameExtension("/"));
        assertEquals("", CompassInputFileFilter.getFilenameExtension("."));
        assertEquals("", CompassInputFileFilter.getFilenameExtension("foo."));
        assertEquals("foobar", CompassInputFileFilter.getFilenameExtension(".foobar"));
        assertEquals("bar", CompassInputFileFilter.getFilenameExtension("foo.bar"));
    }
}
