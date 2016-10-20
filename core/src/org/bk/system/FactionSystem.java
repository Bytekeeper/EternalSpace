package org.bk.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.component.Character;
import org.bk.component.Faction;

import static org.bk.component.Mapper.CHARACTER;
import static org.bk.component.Mapper.FACTION;

/**
 * Created by dante on 20.10.2016.
 */
public class FactionSystem extends IteratingSystem {
    private int maxIndex = -1;

    public FactionSystem(int priority) {
        super(Family.all(Faction.class).get(), priority);
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(getFamily(), new NewFactionListener());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Faction faction = FACTION.get(entity);
        if (faction.assessment.size < maxIndex + 1) {
            faction.assessment.setSize(maxIndex + 1);
        }
    }

    public boolean areEnemies(Entity a, Entity b) {
        if (a == b) {
            return false;
        }
        Character characterA = CHARACTER.get(a);
        Character characterB = CHARACTER.get(b);
        Faction factionA = FACTION.get(characterA.faction);
        Faction factionB = FACTION.get(characterB.faction);
        return factionA.assessment.get(factionB.index) < 0 ||
                factionB.assessment.get(factionA.index) < 0;
    }

    private class NewFactionListener implements EntityListener {
        @Override
        public void entityAdded(Entity entity) {
            Faction faction = FACTION.get(entity);
            if (faction.index > maxIndex) {
                maxIndex = faction.index;
            }
        }

        @Override
        public void entityRemoved(Entity entity) {

        }
    }
}
