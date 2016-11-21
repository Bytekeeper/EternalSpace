package org.bk.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pools;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by dante on 21.11.2016.
 */
public class Messages extends Group {
    private FloatArray displayTimes = new FloatArray();
    private final Skin skin;

    public Messages(Skin skin) {
        this.skin = skin;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        for (int i = 0; i < displayTimes.size; i++) {
            float timeRemaining = displayTimes.get(i);
            if (timeRemaining > 0) {
                timeRemaining -= delta;
                displayTimes.set(i, timeRemaining);
                if (timeRemaining < 0) {
                    Actor actor = getChildren().get(i);
                    actor.addAction(sequence(fadeOut(1), Actions.removeActor()));
                }
            }
        }
    }

    @Override
    public boolean removeActor(Actor actor) {
        int i = getChildren().indexOf(actor, true);
        if (i >= 0) {
            displayTimes.removeIndex(i);
        }
        return super.removeActor(actor);
    }

    @Override
    protected void childrenChanged() {
        float y = getY();
        for (Actor actor : getChildren()) {
            actor.addAction(Actions.moveTo(getX(), y, 0.3f));
            y += actor.getHeight();
        }

    }

    public void append(String message, float duration) {
        displayTimes.insert(0, duration);
        Label actor = new Label(message, skin);
        actor.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(1)));
        addActorAt(0, actor);
    }
}
