package org.bk.data;

import com.badlogic.gdx.utils.ObjectMap;

/**
 * Created by dante on 23.10.2016.
 */
public class Faction {
    private ObjectMap<Faction, Float> relations = new ObjectMap<Faction, Float>();

    public boolean isEnemy(Faction other) {
        if (other == this) {
            return false;
        }
        return relations.get(other, other.relations.get(this)) < 0;
    }
}
