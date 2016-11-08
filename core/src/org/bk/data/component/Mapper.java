package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Array;

/**
 * Created by dante on 10.10.2016.
 */
public class Mapper {
    public static final Array<Class<? extends Component>> MAPPED_COMPONENTS = new Array<Class<? extends Component>>();
    public static final ComponentMapper<Transform> TRANSFORM = mapperFor(Transform.class);
    public static final ComponentMapper<Body> BODY = mapperFor(Body.class);
    public static final ComponentMapper<Movement> MOVEMENT = mapperFor(Movement.class);
    public static final ComponentMapper<Controllable> CONTROLLABLE = mapperFor(Controllable.class);
    public static final ComponentMapper<Projectile> PROJECTILE = mapperFor(Projectile.class);
    public static final ComponentMapper<Weapons> WEAPONS = mapperFor(Weapons.class);
    public static final ComponentMapper<Thrusters> THRUSTERS = mapperFor(Thrusters.class);
    public static final ComponentMapper<LifeTime> LIFE_TIME = mapperFor(LifeTime.class);
    public static final ComponentMapper<Touching> TOUCHING = mapperFor(Touching.class);
    public static final ComponentMapper<Health> HEALTH = mapperFor(Health.class);
    public static final ComponentMapper<Steering> STEERING = mapperFor(Steering.class);
    public static final ComponentMapper<AIControlled> AI_CONTROLLED = mapperFor(AIControlled.class);
    public static final ComponentMapper<Celestial> CELESTIAL = mapperFor(Celestial.class);
    public static final ComponentMapper<Landing> LANDING = mapperFor(Landing.class);
    public static final ComponentMapper<Asteroid> ASTEROID = mapperFor(Asteroid.class);
    public static final ComponentMapper<Physics> PHYSICS = mapperFor(Physics.class);
    public static final ComponentMapper<Persistence> PERSISTENCE = mapperFor(Persistence.class);
    public static final ComponentMapper<Character> CHARACTER = mapperFor(Character.class);
    public static final ComponentMapper<Owned> OWNED = mapperFor(Owned.class);
    public static final ComponentMapper<Jumping> JUMPING = mapperFor(Jumping.class);
    public static final ComponentMapper<Orbiting> ORBITING = mapperFor(Orbiting.class);
    public static final ComponentMapper<LandingPlace> LANDING_PLACE = mapperFor(LandingPlace.class);
    public static final ComponentMapper<Battery> BATTERY = mapperFor(Battery.class);
    public static final ComponentMapper<JumpDrive> JUMP_DRIVE = mapperFor(JumpDrive.class);
    public static final ComponentMapper<Shield> SHIELD = mapperFor(Shield.class);
    public static final ComponentMapper<Account> ACCOUNT = mapperFor(Account.class);
    public static final ComponentMapper<Heat> HEAT = mapperFor(Heat.class);

    private static <T extends Component> ComponentMapper<T> mapperFor(Class<T> componentClass) {
        MAPPED_COMPONENTS.add(componentClass);
        return ComponentMapper.getFor(componentClass);
    }
}
