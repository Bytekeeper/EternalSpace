package org.bk;

import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 13.11.2016.
 */
public class Message implements Pool.Poolable {
    public Type type;
    public String text;

    @Override
    public void reset() {
        type = null;
        text = null;
    }


    public enum Type {
        INFO,
        WARNING,
        CHATTER;
    }
}
