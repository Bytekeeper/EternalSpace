package org.bk.data.component.state;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.utils.Array;

/**
 * Created by dante on 13.11.2016.
 */
public class States {
    public static final Array<? extends Class<? extends Component>> ABORTABLE_ACTIONS = Array.with(Jump.class, Land.class);
    public static Family UNABORTABLE_ACTIONS = Family.one(Landed.class, JumpingIn.class, JumpingOut.class, Landing.class, LiftingOff.class).get();

    public static void abortActions(Entity e, Class<? extends Component> allBut) {
        for (Class<? extends Component> abortableAction : ABORTABLE_ACTIONS) {
            if (allBut == abortableAction) {
                continue;
            }
            e.remove(abortableAction);
        }
    }

    public static void abortActions(Entity e) {
        for (Class<? extends Component> abortableAction : ABORTABLE_ACTIONS) {
            e.remove(abortableAction);
        }
    }
}
