/*
Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/

package compass;

import java.nio.file.Path;
import java.util.*;

public class CompassInputFileFilter {

    public static final Collection<String> ALLOWED_FILE_EXTENSIONS = Collections.unmodifiableList(
            Arrays.asList("SQL", "TXT", "DAT", "XML")
    );

    public static boolean test(Path path) {
        boolean valid = ALLOWED_FILE_EXTENSIONS.contains(
                        getFilenameExtension(path.getFileName().toString().toUpperCase())
        );
        return valid;
    }

    public static String getFilenameExtension(String filename) {
        String extension = "";
        if (filename != null && filename.contains(".")) {
            extension = filename.substring(filename.lastIndexOf(".") + 1);
        }
        return extension;
    }
}
