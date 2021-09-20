/*
Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/

package compass;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

public class CompassLastTokenListener implements ParseTreeListener {
    private Token lastToken;

    public Token getLastToken() {
        return lastToken;
    }

    public void enterEveryRule(ParserRuleContext ctx) { }

    public void visitTerminal(TerminalNode node) { }

    public void visitErrorNode(ErrorNode node) { }

    public void exitEveryRule(ParserRuleContext ctx) {
        lastToken = ctx.getStop();
    }
}
