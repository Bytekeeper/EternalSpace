package org.bk.component;

import com.badlogic.ashley.core.ComponentMapper;

/**
 * Created by dante on 10.10.2016.
 */
public class Mapper {
    public static final ComponentMapper<Transform> TRANSFORM = ComponentMapper.getFor(Transform.class);
    public static final ComponentMapper<Body> BODY = ComponentMapper.getFor(Body.class);
    public static final ComponentMapper<VisibleAddon> VISIBLE_ADDON = ComponentMapper.getFor(VisibleAddon.class);
    public static final ComponentMapper<Movement> MOVEMENT = ComponentMapper.getFor(Movement.class);
    public static final ComponentMapper<Ship> SHIP = ComponentMapper.getFor(Ship.class);
    public static final ComponentMapper<Projectile> PROJECTILE = ComponentMapper.getFor(Projectile.class);
    public static final ComponentMapper<Weapons> WEAPON = ComponentMapper.getFor(Weapons.class);
    public static final ComponentMapper<LifeTime> LIFE_TIME = ComponentMapper.getFor(LifeTime.class);
    public static final ComponentMapper<Touching> TOUCHING = ComponentMapper.getFor(Touching.class);
    public static final ComponentMapper<Health> HEALTH = ComponentMapper.getFor(Health.class);
    public static final ComponentMapper<Steering> STEERING = ComponentMapper.getFor(Steering.class);
    public static final ComponentMapper<AIControlled> AI_CONTROLLED = ComponentMapper.getFor(AIControlled.class);
    public static final ComponentMapper<Planet> PLANET = ComponentMapper.getFor(Planet.class);
}
