package org.bk.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteerableAdapter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pools;
import org.bk.Assets;
import org.bk.Game;
import org.bk.Outliner;
import org.bk.ai.SteeringUtil;
import org.bk.data.component.Body;
import org.bk.data.component.*;
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

    static final float W2B = 1f / 10;
    public static final float B2W = 1f / W2B;
    public static final float SIMULATION_STEP = 1 / 60f;
    private final ContactListener myContactListener = new MyContactListener();
    //    private Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
    private final Assets assets;
    private World world;
    private final Vector2 tv = new Vector2();
    private float nextStep;
    private ImmutableArray<Entity> touchingEntities;
    private ObjectMap<Entity, com.badlogic.gdx.physics.box2d.Body> entityBody = new ObjectMap<Entity, com.badlogic.gdx.physics.box2d.Body>();
    private ObjectMap<Entity, PositionAndOrientation> lastPositionAndOrientation = new ObjectMap<Entity, PositionAndOrientation>();
    private Array<com.badlogic.gdx.physics.box2d.Body> bodies = new Array<com.badlogic.gdx.physics.box2d.Body>();
    private Entity lastPick;
    private QueryCallback pickCallback = new QueryCallback() {
        @Override
        public boolean reportFixture(Fixture fixture) {
            Entity picked = (Entity) fixture.getBody().getUserData();
            if (CONTROLLABLE.has(picked)) {
                lastPick = picked;
                return false;
            }
            return true;
        }
    };

    public Box2DPhysicsSystem(Game game) {

        assets = game.assets;
        world = new World(Vector2.Zero, true);
        world.setContactListener(myContactListener);
    }

    @Override
    public void addedToEngine(Engine engine) {
        Family family = Family.all(Body.class, Transform.class, Physics.class).get();
        touchingEntities = engine.getEntitiesFor(Family.all(Touching.class).get());
        engine.addEntityListener(family, new MyEntityListener());
        engine.addEntityListener(Family.all(Steering.class, Physics.class, Transform.class, Movement.class).get(), new SetupSteerableListener());
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }

    @Override
    public void update(float deltaTime) {
        world.getBodies(bodies);
        for (Entity entity : touchingEntities) {
            Touching touching = TOUCHING.get(entity);
            Array<TouchInfo> touchList = touching.touchList;
            for (int i = 0; i < touchList.size; i++) {
                if (touching.untouchList.contains(touchList.get(i).other, true)) {
                    touchList.removeIndex(i);
                }
            }
            if (touchList.size == 0) {
                entity.remove(Touching.class);
            }
            if (!MOVEMENT.has(entity)) {
                com.badlogic.gdx.physics.box2d.Body body = entityBody.get(entity);
                Transform transform = TRANSFORM.get(entity);
                body.setTransform(tv.set(transform.location).scl(W2B), transform.orientRad);
            }
        }
        nextStep -= Math.min(deltaTime, 0.1f);
        while (nextStep < 0) {
            for (com.badlogic.gdx.physics.box2d.Body body : bodies) {
                Entity entity = (Entity) body.getUserData();
                Movement movement = MOVEMENT.get(entity);
                if (movement != null) {
                    tv.set(movement.linearAccel).scl(W2B);
                    body.applyForceToCenter(tv, true);
                    body.applyTorque(movement.angularAccel, true);
                    if (movement.velocity.len() > movement.maxVelocity) {
                        float scale = movement.velocity.len() - movement.maxVelocity + 0.01f;
                        tv.set(movement.velocity).nor().scl(-body.getMass() * scale / deltaTime * W2B);
                        body.applyForceToCenter(tv.x, tv.y, true);
                    }
                    if (movement.maxVelocity == 0) {
                        Gdx.app.error(Box2DPhysicsSystem.class.getSimpleName(), "No max velocity set for " + entity.getComponents());
                    }
                }
                PositionAndOrientation positionAndOrientation = lastPositionAndOrientation.get(entity);
                positionAndOrientation.orientation = body.getAngle();
                positionAndOrientation.position.set(body.getPosition());
            }
            nextStep += SIMULATION_STEP;
            world.step(SIMULATION_STEP, 8, 3);
        }

        float alpha = nextStep / SIMULATION_STEP;

        for (com.badlogic.gdx.physics.box2d.Body body : bodies) {
            Entity entity = (Entity) body.getUserData();
            Movement movement = MOVEMENT.get(entity);
            if (movement != null) {
                PositionAndOrientation lastPO = lastPositionAndOrientation.get(entity);
                float fixedAngle = (body.getAngle() % MathUtils.PI2 + MathUtils.PI2) % MathUtils.PI2;
                body.setTransform(body.getPosition(), fixedAngle);
                Transform transform = TRANSFORM.get(entity);
                transform.orientRad = MathUtils.lerpAngle(fixedAngle, lastPO.orientation, alpha);
                transform.location.set(body.getPosition()).scl(1 - alpha);
                transform.location.mulAdd(lastPO.position, alpha);
                transform.location.scl(B2W);
                movement.linearAccel.setZero();
                movement.angularAccel = 0;
                movement.velocity.set(body.getLinearVelocity()).scl(B2W);
                movement.angularVelocity = body.getAngularVelocity();
            }
        }
    }

    public Entity pick(Vector2 at) {
        lastPick = null;
        world.QueryAABB(pickCallback, at.x, at.y, at.x, at.y);
        Entity tmp = lastPick;
        lastPick = null;
        return tmp;
    }

    private Steerable<Vector2> toSteeringBehavior(final Movement movement, final Transform transform, final com.badlogic.gdx.physics.box2d.Body body) {
        return new SteerableAdapter<Vector2>() {
            @Override
            public Vector2 getLinearVelocity() {
                return movement.velocity;
            }

            @Override
            public Vector2 getPosition() {
                return transform.location;
            }

            @Override
            public float getOrientation() {
                return transform.orientRad;
            }

            @Override
            public float getMaxLinearAcceleration() {
                return movement.linearThrust / body.getMass();
            }

            @Override
            public float getMaxLinearSpeed() {
                return movement.maxVelocity;
            }

            @Override
            public float getAngularVelocity() {
                return body.getAngularVelocity();
            }

            @Override
            public float getMaxAngularSpeed() {
                return movement.angularThrust / body.getInertia() / body.getAngularDamping();
            }

            @Override
            public float getMaxAngularAcceleration() {
                return getMaxAngularSpeed();
            }

            @Override
            public float vectorToAngle(Vector2 vector) {
                return vector.angleRad();
            }

            @Override
            public Vector2 angleToVector(Vector2 outVector, float angle) {
                return outVector.set(1, 0).setAngleRad(angle);
            }
        };
    }

    private class MyEntityListener implements EntityListener {
        BodyDef bodyDef = new BodyDef();
        FixtureDef fixtureDef = new FixtureDef();
        ObjectMap<String, Array<PolygonShape>> shapesOf = new ObjectMap<String, Array<PolygonShape>>();

        @Override
        public void entityAdded(Entity entity) {
            Transform transform = TRANSFORM.get(entity);
            transform.steerableLocation = SteeringUtil.toLocation(transform.location);

            bodyDef.type = BodyDef.BodyType.DynamicBody;
            if (CONTROLLABLE.has(entity)) {
                bodyDef.angularDamping = 5f;
            } else {
                bodyDef.angularDamping = 0;
            }
            bodyDef.angle = transform.orientRad;
            bodyDef.position.set(transform.location).scl(W2B);
            Movement movement = MOVEMENT.get(entity);
            if (movement != null) {
                bodyDef.linearVelocity.set(movement.velocity).scl(W2B);
                bodyDef.angularVelocity = movement.angularVelocity;
            } else {
                bodyDef.linearVelocity.setZero();
                bodyDef.angularVelocity = 0;
            }

            bodyDef.bullet = false;

            fixtureDef.density = 40f;
            fixtureDef.friction = 0.5f;
            fixtureDef.restitution = 0.2f;
            if (CONTROLLABLE.has(entity)) {
                fixtureDef.filter.categoryBits = CATEGORY_SHIPS;
                fixtureDef.filter.maskBits = CATEGORY_WEAPON | CATEGORY_POI;
            } else if (PROJECTILE.has(entity)) {
                fixtureDef.filter.categoryBits = CATEGORY_WEAPON;
                fixtureDef.filter.maskBits = CATEGORY_DEBRIS | CATEGORY_SHIPS | CATEGORY_WEAPON;
                fixtureDef.density = 1E-10f;
                bodyDef.bullet = true;
            } else if (CELESTIAL.has(entity)) {
                fixtureDef.filter.categoryBits = CATEGORY_POI;
                fixtureDef.filter.maskBits = CATEGORY_SHIPS;
                bodyDef.type = BodyDef.BodyType.StaticBody;
                fixtureDef.isSensor = true;
            } else if (ASTEROID.has(entity)) {
                fixtureDef.filter.categoryBits = CATEGORY_DEBRIS;
                fixtureDef.filter.maskBits = CATEGORY_DEBRIS | CATEGORY_WEAPON;
                fixtureDef.isSensor = false;
            }

            com.badlogic.gdx.physics.box2d.Body physicsBody = world.createBody(bodyDef);
            physicsBody.setUserData(entity);
            Body body = BODY.get(entity);
            entityBody.put(entity, physicsBody);

            Array<PolygonShape> shapes = shapesOf.get(body.graphics);
            if (shapes == null) {
                TextureRegion region = assets.textures.get(body.graphics);
                Array<float[]> polygons = new Array<float[]>(assets.outlineOf(body.graphics));
                for (int i = 0; i < polygons.size; i++) {
                    polygons.set(i, Outliner.douglasPeucker(polygons.get(i), 7));
                }
                Array<float[]> triangles = assets.outliner.triangulate(polygons, W2B * body.dimension.x / region.getRegionWidth());
                shapes = new Array<PolygonShape>(triangles.size);
                for (float[] triangle : triangles) {
                    PolygonShape shape = new PolygonShape();
                    shape.set(triangle);
                    shapes.add(shape);
                }
                shapesOf.put(body.graphics, shapes);
            }
            for (PolygonShape shape : shapes) {
                fixtureDef.shape = shape;
                physicsBody.createFixture(fixtureDef);
            }

            lastPositionAndOrientation.put(entity, Pools.obtain(PositionAndOrientation.class));
        }

        @Override
        public void entityRemoved(Entity entity) {
            PositionAndOrientation positionAndOrientation = lastPositionAndOrientation.remove(entity);
            if (positionAndOrientation != null) {
                Pools.free(positionAndOrientation);
            }
            com.badlogic.gdx.physics.box2d.Body bodyToDestroy = entityBody.remove(entity);
            if (bodyToDestroy != null) {
                world.destroyBody(bodyToDestroy);
            }
            entity.remove(Touching.class);
        }
    }

    private class MyContactListener implements ContactListener {
        @Override
        public void beginContact(Contact contact) {
            WorldManifold worldManifold = contact.getWorldManifold();
            Vector2 collisionPoint = worldManifold.getPoints()[0];
            TouchInfo touchA = Pools.obtain(TouchInfo.class);
            TouchInfo touchB = Pools.obtain(TouchInfo.class);
            Entity a = (Entity) contact.getFixtureA().getBody().getUserData();
            Entity b = (Entity) contact.getFixtureB().getBody().getUserData();
            touchA.other = b;
            touchB.other = a;
            if (worldManifold.getNumberOfContactPoints() > 0) {
                touchA.collisionPoint.set(collisionPoint).scl(B2W);
                touchB.collisionPoint.set(collisionPoint).scl(B2W);
                touchA.normal.set(worldManifold.getNormal()).scl(-1);
                touchB.normal.set(worldManifold.getNormal());
            } else {
                touchA.collisionPoint.setZero();
                touchB.collisionPoint.setZero();
                touchA.normal.setZero();
                touchB.normal.setZero();
            }

            Array<TouchInfo> touchListA = touchingOf(a).touchList;
            Array<TouchInfo> touchListB = touchingOf(b).touchList;
            touchListA.add(touchA);
            touchListB.add(touchB);
        }

        @Override
        public void endContact(Contact contact) {
            Entity a = (Entity) contact.getFixtureA().getBody().getUserData();
            Entity b = (Entity) contact.getFixtureB().getBody().getUserData();
            Touching touchingA = touchingOf(a);
            Touching touchingB = touchingOf(b);
            touchingA.untouchList.add(b);
            touchingB.untouchList.add(a);
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
            if (((contact.getFixtureA().getFilterData().categoryBits | contact.getFixtureB().getFilterData().categoryBits) & CATEGORY_WEAPON) != 0) {
                contact.setEnabled(false);
            }
        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {
        }
    }

    public static class TouchInfo {
        public Entity other;
        public final Vector2 collisionPoint = new Vector2();
        public final Vector2 normal = new Vector2();
    }

    private class SetupSteerableListener implements EntityListener {
        @Override
        public void entityAdded(Entity entity) {
            Steering steering = STEERING.get(entity);
            Movement movement = MOVEMENT.get(entity);
            Transform transform = TRANSFORM.get(entity);
            steering.steerable = toSteeringBehavior(movement, transform, entityBody.get(entity));
        }

        @Override
        public void entityRemoved(Entity entity) {

        }
    }

    private static class PositionAndOrientation {
        final Vector2 position = new Vector2();
        float orientation;
    }
}
