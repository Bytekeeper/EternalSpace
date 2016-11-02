package org.bk.data;

import org.bk.data.script.Script;

/**
 * Created by dante on 02.11.2016.
 */
public class Mission {
    public Condition offerWhen = new Condition();
    public Condition succeedWhen = new Condition();
    public Condition failWhen = new Condition();
    public boolean done;
    public boolean active;
    public String title;
    public Script offered = new Script();
    public Script succeeded = new Script();
    public Script failed = new Script();
}
