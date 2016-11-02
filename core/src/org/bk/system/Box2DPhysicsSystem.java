package org.bk.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import org.bk.ai.SteeringUtil;
import org.bk.data.component.Body;
import org.bk.data.component.*;
import org.bk.data.component.Character;
import org.bk.data.component.Transform;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 10.10.2016.
 */
public class Box2DPhysicsSystem extends EntitySystem {
    private static final short CATEGORY_POI = 0x0001;
    private static final short CATEGORY_DEBRIS = 0x0002;
    private static final short CATEGORY_SHIPS = 0x0004;
    private static final short CATEGORY_WEAPON = 0x0008;

    private static final float W2B = 1f / 10;
    public static final float B2W = 1f / W2B;
    private final ContactListener myContactListener = new MyContactListener();
    private final ContactFilter myContactFilter = new MyContactFilter();
    private World world;
    private ImmutableArray<Entity> entities;
    private final Vector2 tv = new Vector2();
    private float nextStep;

    public Box2DPhysicsSystem(int priority) {
        super(priority);
        world = new World(Vector2.Zero, true);
        world.setContactListener(myContactListener);
        world.setContactFilter(myContactFilter);
    }

    @Override
    public void addedToEngine(Engine engine) {
        Family family = Family.all(Transform.class, Physics.class).get();
        entities = engine.getEntitiesFor(family);
        engine.addEntityListener(family, new MyEntityListener());
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }

    @Override
    public void update(float deltaTime) {
        for (Entity entity : entities) {
            Transform transform = TRANSFORM.get(entity);
            Physics physics = PHYSICS.get(entity);
            com.badlogic.gdx.physics.box2d.Body physicsBody = physics.physicsBody;
            tv.set(physicsBody.getPosition()).scl(B2W);
            if (transform.location.dst2(tv) > 0.1f ||
                    Math.abs(transform.orientRad - physicsBody.getAngle()) > 0.01f) {
                tv.set(transform.location).scl(W2B);
                physicsBody.setTransform(tv, transform.orientRad);
            }
        }
        nextStep -= Math.min(deltaTime, 0.1f);
        while (nextStep < 0) {
            for (Entity entity : entities) {
                Movement movement = MOVEMENT.get(entity);
                if (movement != null) {
                    Physics physics = PHYSICS.get(entity);
                    com.badlogic.gdx.physics.box2d.Body physicsBody = physics.physicsBody;
                    tv.set(movement.linearAccel).scl(W2B);
                    physicsBody.applyForceToCenter(tv, true);
                    physicsBody.applyTorque(movement.angularAccel, true);
                    if (movement.velocity.len() > movement.maxVelocity) {
                        float scale = movement.velocity.len() - movement.maxVelocity + 0.01f;
                        tv.set(movement.velocity).nor().scl(-physicsBody.getMass() * scale / deltaTime * W2B);
                        physicsBody.applyForceToCenter(tv.x, tv.y, true);
                    }
                    if (movement.maxVelocity == 0) {
                        Gdx.app.error(Box2DPhysicsSystem.class.getSimpleName(), "No max velocity set for " + entity.getComponents());
                    }
                }
            }
            nextStep += 1 / 60f;
            world.step(1 / 60f, 6, 2);
        }
        for (Entity entity : entities) {
            Physics physics = PHYSICS.get(entity);
            Transform transform = TRANSFORM.get(entity);
            com.badlogic.gdx.physics.box2d.Body physicsBody = physics.physicsBody;
            transform.orientRad = (physicsBody.getAngle() % MathUtils.PI2 + MathUtils.PI2) % MathUtils.PI2;
            transform.location.set(physicsBody.getPosition()).scl(B2W);
            physics.physicsBody.setTransform(physicsBody.getPosition(), transform.orientRad);
            Movement movement = MOVEMENT.get(entity);
            if (movement != null) {
                movement.linearAccel.setZero();
                movement.angularAccel = 0;
                movement.velocity.set(physicsBody.getLinearVelocity()).scl(B2W);
                movement.angularVelocity = physicsBody.getAngularVelocity();
            }
        }
    }

    private class MyEntityListener implements EntityListener {
        BodyDef bodyDef = new BodyDef();
        FixtureDef fixtureDef = new FixtureDef();

        @Override
        public void entityAdded(Entity entity) {
            Transform transform = TRANSFORM.get(entity);
            transform.steerableLocation = SteeringUtil.toLocation(transform.location);

            bodyDef.type = BodyDef.BodyType.DynamicBody;
            if (SHIP.has(entity)) {
                bodyDef.angularDamping = 5f;
            } else {
                bodyDef.angularDamping = 0;
            }
            bodyDef.angle = transform.orientRad;
            tv.set(transform.location).scl(W2B);
            bodyDef.position.set(tv);
            Movement movement = MOVEMENT.get(entity);
            if (movement != null) {
                bodyDef.linearVelocity.set(movement.velocity).scl(W2B);
                bodyDef.angularVelocity = movement.angularVelocity;
            } else {
                bodyDef.linearVelocity.setZero();
                bodyDef.angularVelocity = 0;
            }

            com.badlogic.gdx.physics.box2d.Body physicsBody = world.createBody(bodyDef);
            physicsBody.setUserData(entity);
            Physics physics = PHYSICS.get(entity);
            Body body = BODY.get(entity);
            physics.physicsBody = physicsBody;
            CircleShape circleShape = new CircleShape();
            circleShape.setRadius(body.dimension.len() * W2B / 2);

            fixtureDef.shape = circleShape;
            fixtureDef.density = 5f;
//            fixtureDef.density = 1f;
            fixtureDef.friction = 0.5f;
            fixtureDef.restitution = 0.2f;
            if (SHIP.has(entity)) {
                fixtureDef.filter.categoryBits = CATEGORY_SHIPS;
                fixtureDef.filter.maskBits = CATEGORY_WEAPON | CATEGORY_POI;
            } else if (PROJECTILE.has(entity)) {
                fixtureDef.filter.categoryBits = CATEGORY_WEAPON;
                fixtureDef.filter.maskBits = CATEGORY_DEBRIS | CATEGORY_SHIPS | CATEGORY_WEAPON;
                fixtureDef.isSensor = true;
            } else if (CELESTIAL.has(entity)) {
                fixtureDef.filter.categoryBits = CATEGORY_POI;
                fixtureDef.filter.maskBits = CATEGORY_SHIPS;
                fixtureDef.isSensor = true;
            } else if (ASTEROID.has(entity)) {
                fixtureDef.filter.categoryBits = CATEGORY_DEBRIS;
                fixtureDef.filter.maskBits = CATEGORY_DEBRIS | CATEGORY_WEAPON;
                fixtureDef.isSensor = false;
            }

            Fixture fixture = physicsBody.createFixture(fixtureDef);
            circleShape.dispose();
        }

        @Override
        public void entityRemoved(Entity entity) {
            Physics physics = PHYSICS.get(entity);
            if (physics != null && physics.physicsBody != null) {
                world.destroyBody(physics.physicsBody);
                physics.physicsBody = null;
            }
        }
    }

    private class MyContactListener implements ContactListener {
        @Override
        public void beginContact(Contact contact) {
            Entity a = (Entity) contact.getFixtureA().getBody().getUserData();
            Array<Entity> touchListA = touchingOf(a).touchList;
            Entity b = (Entity) contact.getFixtureB().getBody().getUserData();
            Array<Entity> touchListB = touchingOf(b).touchList;
            touchListA.add(b);
            touchListB.add(a);
        }

        @Override
        public void endContact(Contact contact) {
            Entity a = (Entity) contact.getFixtureA().getBody().getUserData();
            Array<Entity> touchListA = touchingOf(a).touchList;
            Entity b = (Entity) contact.getFixtureB().getBody().getUserData();
            Array<Entity> touchListB = touchingOf(b).touchList;
            if (touchListA.removeValue(b, true) && touchListA.size == 0) {
                a.remove(Touching.class);
            }
            if (touchListB.removeValue(a, true) && touchListB.size == 0) {
                b.remove(Touching.class);
            }
        }

        private Touching touchingOf(Entity e) {
            Touching touching = TOUCHING.get(e);
            if (touching == null) {
                touching = getEngine().createComponent(Touching.class);
                e.add(touching);
            }
            return touching;
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {
        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {

        }
    }

    private class MyContactFilter implements ContactFilter {
        @Override
        public boolean shouldCollide(Fixture fixtureA, Fixture fixtureB) {
            Filter filterA = fixtureA.getFilterData();
            Filter filterB = fixtureB.getFilterData();
            boolean collide = (filterA.maskBits & filterB.categoryBits) != 0 && (filterA.categoryBits & filterB.maskBits) != 0;
            if (collide) {
                Entity entityA = (Entity) fixtureA.getBody().getUserData();
                Entity entityB = (Entity) fixtureB.getBody().getUserData();
                Owned ownedA = OWNED.get(entityA);
                Owned ownedB = OWNED.get(entityB);
                if (ownedA != null) {
                    entityA = ownedA.owner;
                }
                if (ownedB != null) {
                    entityB = ownedB.owner;
                }
                Character characterA = CHARACTER.get(entityA);
                Character characterB = CHARACTER.get(entityB);
                if (characterA != null && characterB != null) {
                    return characterA != characterB && characterA.faction.isEnemy(characterB.faction);
                }
                return true;
            }
            return false;
        }
    }
}
