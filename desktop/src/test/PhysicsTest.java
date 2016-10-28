import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import org.junit.Test;

/**
 * Created by dante on 27.10.2016.
 */
public class PhysicsTest {
    @Test
    public void box2dAccelerationBehavesAsExpected() {
        World world = new World(Vector2.Zero, true);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        Body body = world.createBody(bodyDef);
        Shape shape = new CircleShape();
        shape.setRadius(1);
        Fixture fixture = body.createFixture(shape, 0.5f);
        fixture.setFriction(0);
        fixture.setRestitution(0);
        shape.dispose();

        float steps = 60;
        float force = 100;

        for (int i = 0; i < steps; i++) {
            body.applyForceToCenter(new Vector2(force, 0), true);
            world.step(1 / 60f, 6, 2);
        }
        System.out.println(body.getPosition() + " " + body.getLinearVelocity());

        body.setLinearVelocity(0, 0);
        body.setTransform(0, 0, 0);

        body.applyForceToCenter(new Vector2(force, 0), true);
        world.step(steps/60f, 6, 2);

        System.out.println(body.getPosition() + " " + body.getLinearVelocity());
        float t = steps / 60f;
        float a = force / body.getMass();
        float v = a * t;
        float d = 0.5f * v * t;
        System.out.println(d + " " + v);
        world.dispose();
    }
}
