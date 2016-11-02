package org.bk.data.script;

import com.badlogic.gdx.utils.Array;

public class Text implements ScriptItem {
    public Array<String> line = new Array<String>();

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
