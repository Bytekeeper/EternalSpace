package org.bk.data.script;

import com.badlogic.gdx.utils.Array;

/**
 * Created by dante on 02.11.2016.
 */
public class Script implements ScriptItem {
    public Array<ScriptItem> items = new Array<ScriptItem>();


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
