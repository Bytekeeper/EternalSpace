package org.bk.ai.task;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import org.bk.data.Faction;
import org.bk.data.GameData;
import org.bk.data.component.Character;

import static org.bk.data.component.Mapper.CHARACTER;

/**
 * Created by dante on 04.11.2016.
 */
public class EnemyNearby extends LeafTask<Entity> {
    private final ImmutableArray<Entity> entitiesToObserve;

    public EnemyNearby(ImmutableArray<Entity> entitiesToObserve) {
        this.entitiesToObserve = entitiesToObserve;
    }

    @Override
    public Status execute() {
        Faction ownerFaction = CHARACTER.get(getObject()).faction;
        for (Entity e: entitiesToObserve) {
            if (e == getObject()) {
                continue;
            }
            Faction faction = CHARACTER.get(e).faction;
            if (faction.isEnemy(ownerFaction)) {
                return Status.SUCCEEDED;
            }
        }
        return Status.FAILED;
    }

    @Override
    protected Task<Entity> copyTo(Task<Entity> task) {
        return null;
    }
}
