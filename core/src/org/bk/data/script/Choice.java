package org.bk.data.script;

import com.badlogic.gdx.utils.Array;

public class Choice implements ScriptItem {
    public Array<Option> options = new Array<Option>();

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public static class Option {
        public String condition;
        public Text text;
    }
}
