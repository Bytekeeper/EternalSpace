package org.bk.fsm;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

/**
 * Created by dante on 12.11.2016.
 */
public class TransitionListener implements EntityListener {
    private final Array<Class<? extends Component>> allowTransitionFrom = new Array<Class<? extends Component>>();
    private final Class<? extends Component> myState;

    public TransitionListener(Class<? extends Component> myState, Class<? extends Component>... allowedFromStates) {
        this.myState = myState;
        allowTransitionFrom.addAll(allowedFromStates);
    }

    @Override
    public final void entityAdded(Entity entity) {
        Component oldState = null;
        for (Class<? extends Component> allowedPreviousState : allowTransitionFrom) {
            oldState = entity.getComponent(allowedPreviousState);
            if (oldState != null) {
                break;
            }
        }
        if (oldState == null) {
            Gdx.app.debug("Transition", "DENIED: " + myState.getSimpleName());
            entity.remove(myState);
        } else {
            enterState(entity);
            entity.remove(oldState.getClass());
        }
    }

    protected void enterState(Entity entity) {
    }

    @Override
    public final void entityRemoved(Entity entity) {

    }
}
