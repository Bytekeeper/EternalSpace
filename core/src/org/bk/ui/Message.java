package org.bk.ui;

import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 13.11.2016.
 */
public class Message implements Pool.Poolable {
    private final float TIME_TO_DISPLAY = 5;
    public Type type;
    public String text;
    public float displayTime = TIME_TO_DISPLAY;

    @Override
    public void reset() {
        type = null;
        text = null;
        displayTime = TIME_TO_DISPLAY;
    }

    public enum Type {
        INFO,
        WARNING,
        CHATTER;
    }
}
