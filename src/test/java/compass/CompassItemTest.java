/*
Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/
package compass;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CompassItemTest {

    @Test
    @DisplayName("Compass Item attribute append")
    void testAttributeAppend() {
        CompassItem item = new CompassItem();
        assertEquals("", item.getAttributes(), "New instance has empty attributes string");

        item = new CompassItem("foo", 1);
        assertEquals("", item.getAttributes(), "New instance with name and line number has empty attributes string");

        String attribute = null;
        item.attributeAppend(attribute);
        assertEquals("", item.getAttributes(), "NULL attribute does not change attributes string");

        attribute = "";
        item.attributeAppend(attribute);
        assertEquals("", item.getAttributes(), "Empty attribute does not change attributes string");

        attribute = " ";
        item.attributeAppend(attribute);
        assertEquals("", item.getAttributes(), "Blank attribute does not change attributes string");

        attribute = "  ";
        item.attributeAppend(attribute);
        assertEquals("", item.getAttributes(), "Multiple blank attribute do not change attributes string");

        attribute = "\t";
        item.attributeAppend(attribute);
        assertEquals("", item.getAttributes(), "Blank character attribute does not change attributes string");

        attribute = "foo";
        item.attributeAppend(attribute);
        assertEquals("foo", item.getAttributes(), "First attribute does not have a blank space in front of it");

        attribute = "bar";
        item.attributeAppend(attribute);
        assertEquals("foo bar", item.getAttributes(), "Additional attributes do not append a blank space at the end");

        attribute = "baz";
        item.attributeAppend(attribute);
        assertEquals("foo bar baz", item.getAttributes(), "Additional attributes do not append a blank space at the end");
    }
}
