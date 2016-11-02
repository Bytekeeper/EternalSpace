package org.bk.data.script;

/**
 * Created by dante on 02.11.2016.
 */
public class Change implements ScriptItem {
    public String script;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
