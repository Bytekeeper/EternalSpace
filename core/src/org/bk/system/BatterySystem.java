package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.data.component.Battery;
import org.bk.data.component.state.Landed;

import static org.bk.data.component.Mapper.BATTERY;

/**
 * Created by dante on 10.11.2016.
 */
public class BatterySystem extends IteratingSystem {
    public BatterySystem(int priority) {
        super(Family.all(Landed.class, Battery.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Battery battery = BATTERY.get(entity);
        battery.capacity = battery.maxCapacity;
    }
}
