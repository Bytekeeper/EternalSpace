package org.bk.data;

import com.badlogic.gdx.utils.ObjectMap;

/**
 * Created by dante on 23.10.2016.
 */
public class Faction {
    public static final Relation UNMET = new Relation();
    public ObjectMap<Faction, Relation> relation = new ObjectMap<Faction, Relation>();
    public String name;

    public boolean isEnemy(Faction other) {
        if (other == this) {
            return false;
        }
        return relation.get(other, other.relation.get(this, UNMET)).reputation < 0;
    }

    public static class Relation {
        public float reputation;
    }
}
