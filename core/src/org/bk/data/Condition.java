package org.bk.data;

import com.badlogic.ashley.core.Entity;
import org.bk.Game;
import org.bk.data.component.state.Landing;

import static org.bk.data.component.Mapper.LANDING;

/**
 * Created by dante on 02.11.2016.
 */
public class Condition {
    public Entity at;
    public Mission completed;

    public boolean conditionsMet(Game game) {
        boolean hasCondition = false;
        if (at != null) {
            hasCondition = true;
            Landing landing = LANDING.get(game.playerEntity);
            if (landing != null && landing.on != at) {
                return false;
            }
        }
        if (completed != null) {
            hasCondition = true;
            if (!completed.wasSuccessful) {
                return false;
            }
        }
        return hasCondition;
    }
}
