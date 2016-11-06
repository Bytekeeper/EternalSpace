package org.bk.data;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import org.bk.script.Initializable;

/**
 * Created by dante on 06.11.2016.
 */
public class EntityGroup implements Initializable {
    public Faction faction;
    public Array<ActualGroup> variant = new Array<ActualGroup>();
    private float overallQuota;

    @Override
    public void afterFieldsSet() {
        for (ActualGroup ag: variant) {
            overallQuota += ag.quota;
        }
    }

    public Array<EntityTemplate> randomVariant() {
        float sum = MathUtils.random(overallQuota);
        for (ActualGroup ag: variant) {
            sum -= ag.quota;
            if (sum <= 0) {
                return ag.entity;
            }
        }
        return variant.first().entity;
    }

    public static class ActualGroup {
        public float quota;
        public Array<EntityTemplate> entity = new Array<EntityTemplate>();
    }
}
