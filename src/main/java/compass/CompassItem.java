/*
Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/
package compass;

public class CompassItem {
    private String name;
    private String attributes;
    private int lineNr;

    public CompassItem() {
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
    	attributes += " " + s + " ";
    }
    
    public String getAttributes() {
    	return attributes;
    }
}
