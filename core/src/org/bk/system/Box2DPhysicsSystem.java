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
import org.bk.Assets;
import org.bk.Game;
import org.bk.Outliner;
import org.bk.ai.SteeringUtil;
import org.bk.data.component.Body;
import org.bk.data.component.Character;
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
    private final ContactListener myContactListener = new MyContactListener();
    //    private Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
    private final Assets assets;
    private World world;
    private final Vector2 tv = new Vector2();
    private float nextStep;
    private ImmutableArray<Entity> touchingEntities;
    private ObjectMap<Entity, com.badlogic.gdx.physics.box2d.Body> entityBody = new ObjectMap<Entity, com.badlogic.gdx.physics.box2d.Body>();
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
            touching.touchList.removeAll(touching.untouchList, true);
            if (touching.touchList.size == 0) {
                entity.remove(Touching.class);
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
            }
            nextStep += 1 / 60f;
            world.step(1 / 60f, 8, 3);
        }
        for (com.badlogic.gdx.physics.box2d.Body body : bodies) {
            Entity entity = (Entity) body.getUserData();
            Transform transform = TRANSFORM.get(entity);
            transform.orientRad = (body.getAngle() % MathUtils.PI2 + MathUtils.PI2) % MathUtils.PI2;
            transform.location.set(body.getPosition()).scl(B2W);
            body.setTransform(body.getPosition(), transform.orientRad);
            Movement movement = MOVEMENT.get(entity);
            if (movement != null) {
                movement.linearAccel.setZero();
                movement.angularAccel = 0;
                movement.velocity.set(body.getLinearVelocity()).scl(B2W);
                movement.angularVelocity = body.getAngularVelocity();
            }
        }
//        Matrix4 m = new Matrix4();
//        m.set(game.viewport.getCamera().combined);
//        m.scl(B2W);
//        debugRenderer.render(world, m);
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
                fixtureDef.isSensor = true;
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

        }

        @Override
        public void entityRemoved(Entity entity) {
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
        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {
        }
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
}
