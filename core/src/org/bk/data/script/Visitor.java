package org.bk.data.script;

public interface Visitor {
    void visit(Text text);

    void visit(If scriptIf);

    void visit(Choice choice);

    void visit(Script script);

    void visit(Change change);
}
