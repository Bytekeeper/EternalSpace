package org.bk.data;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Created by dante on 23.10.2016.
 */
public class Faction {
    public static final Relation UNMET = new Relation();
    private static final float REPUTATION_HATE = -1000;
    public ObjectMap<Faction, Relation> relation = new ObjectMap<Faction, Relation>();
    public String name;
    public final Color color = new Color(Color.WHITE);

    public boolean isEnemy(Faction other) {
        if (other == this) {
            return false;
        }
        return relation.get(other, UNMET).reputation < 0 || other.relation.get(this, UNMET).reputation < 0;
    }

    public void makeEnemies(Faction other) {
        Relation relToOther = relation.get(other);
        if(relToOther == null) {
            relToOther = new Relation();
            relation.put(other, relToOther);
        }
        relToOther.reputation = REPUTATION_HATE;
    }

    public static class Relation {
        public float reputation;
    }
}
