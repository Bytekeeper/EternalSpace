package org.bk.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.signals.Listener;
import com.badlogic.ashley.signals.Signal;
import org.bk.Game;

/**
 * Created by dante on 08.11.2016.
 */
public class BountySystem extends EntitySystem {
    public BountySystem(Game game, int priority) {


        game.entityDestroyed.add(new Listener<Entity>() {
            @Override
            public void receive(Signal<Entity> signal, Entity object) {

            }
        });
    }
}

