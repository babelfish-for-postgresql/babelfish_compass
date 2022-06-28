/*
Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/
package compass;

// this class is used for capturing attributes of a SELECT statement since there is a lot of context which we need
// before we can decide what type of SELECT it actually is
public class CompassItem {
    private String name;
    private String attributes;
    private int lineNr;

    public CompassItem() {
        this(null, 0);
    }

    public CompassItem(String name, int pLineNr) {
        this.name = name;
        this.attributes = "";
        this.lineNr = pLineNr;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public void setLineNr(int pLineNr) {
        this.lineNr = pLineNr;
    }
    
    public int getLineNr() {
        return lineNr;
    }
    
    public void attributeAppend(String s) {
        if (s != null && !s.isEmpty() && !s.matches("^\\s+$")) {
            if (attributes.length() > 0) {
                attributes += " ";
            }
            attributes += s;
        }
    }
    
    public String getAttributes() {     
    	if (attributes.isEmpty()) return "";
    	return " " + attributes + " ";
    }
}
