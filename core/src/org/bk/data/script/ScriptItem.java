package org.bk.data.script;

public interface ScriptItem {
    void accept(Visitor visitor);
}
