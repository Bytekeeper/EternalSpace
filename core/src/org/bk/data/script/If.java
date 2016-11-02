package org.bk.data.script;

public class If implements ScriptItem {
    public Script script = new Script();
    public String condition;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
