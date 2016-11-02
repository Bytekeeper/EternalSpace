package org.bk.script;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.esotericsoftware.kryo.Kryo;
import org.bk.data.EntityTemplate;
import org.bk.data.GameData;
import org.bk.data.component.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Created by dante on 29.10.2016.
 */
public class ScriptContext {
    private final Object root;
    private Kryo kryo = new Kryo();
    private ObjectMap<String, Object> refTable = new ObjectMap<String, Object>();
    private ObjectMap<String, Class<?>> classForIdentifier = new ObjectMap<String, Class<?>>();

    public ScriptContext(Object root) {
        this.root = root;

        for (Class<? extends Component> cc : Arrays.asList(
                Transform.class, Body.class, Celestial.class, Physics.class,
                LandingPlace.class, Orbiting.class, Ship.class, Movement.class,
                Health.class, Steering.class, Mounts.class, Asteroid.class,
                LifeTime.class, Projectile.class)) {
            registerClass(cc.getSimpleName(), cc);
        }
    }

    public void registerClass(String identifier, Class<?> aClass) {
        classForIdentifier.put(identifier, aClass);
    }

    public void load(InputStream in) {
        ScannerWithPushBack sc = new ScannerWithPushBack(new InputStreamReader(in));
        try {
            executeScript(sc, root);
        } catch (ReflectionException e) {
            throw new IllegalStateException(e);
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                sc.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void executeScript(ScannerWithPushBack sc, Object context) throws ReflectionException, IllegalAccessException, InstantiationException {
        while (sc.hasNext()) {
            executeBlock(sc, context);
        }
    }

    private void executeBlock(ScannerWithPushBack sc, Object context) throws ReflectionException, IllegalAccessException, InstantiationException {
        if (context instanceof Entity) {
            handleEntity(sc, (Entity) context);
            return;
        } else if (context instanceof EntityTemplate) {
            handleTemplate(sc, (EntityTemplate) context);
            return;
        }
        while (sc.hasNext()) {
            String item = sc.next();
            if ("}".equals(item)) {
                return;
            }
            Field field = ClassReflection.getField(context.getClass(), item);
            if (ObjectMap.class.isAssignableFrom(field.getType())) {
                String key = sc.next();
                ObjectMap<String, Object> map = (ObjectMap<String, Object>) field.get(context);
                Object value = map.get(key);
                if (value == null) {
                    value = ref(field.getElementType(1), key);
                    map.put(key, value);
                }
                String next = sc.next();
                if ("{".equals(next)) {
                    executeBlock(sc, value);
                } else {
                    sc.pushBack(next);
                }
            } else if (Vector2.class.isAssignableFrom(field.getType())) {
                Vector2 v = (Vector2) field.get(context);
                float x = Float.parseFloat(sc.next());
                float y = Float.parseFloat(sc.next());
                v.set(x, y);
            } else if (Color.class.isAssignableFrom(field.getType())) {
                Color c = getOrCreate(context, field, Color.class);
                float r = Float.parseFloat(sc.next());
                float g = Float.parseFloat(sc.next());
                float b = Float.parseFloat(sc.next());
                float a = Float.parseFloat(sc.next());
                c.set(r, g, b, a);
            } else if (Array.class.isAssignableFrom(field.getType())) {
                Array<Object> array = (Array<Object>) field.get(context);
                if (array == null) {
                    throw new IllegalStateException("Field '" + item + "' on " + context.getClass().getName() + " in should be an initialized array!");
                }
                String next = sc.next();

                Class<?> elementType = field.getElementType(0);
                Object newInstance;

                if (!"-".equals(next) && !"{".equals(next)) {
                    elementType = classForIdentifier.get(next);
                    if (elementType == null) {
                        throw new IllegalStateException("No class found for '" + next + "'!");
                    }
                    next = sc.next();
                } else if ("-".equals(next)) {
                    next = sc.next();
                }
                if (!"{".equals(next)) {
                    newInstance = ref(elementType, next);
                    next = sc.next();
                } else {
                    newInstance = elementType.newInstance();
                }
                if ("{".equals(next)) {
                    executeBlock(sc, newInstance);
                } else {
                    throw new ParseException("Expected '{' for array, but found '" + next + "'");
                }
                array.add(newInstance);
            } else if (String.class == field.getType()) {
                field.set(context, sc.next());
            } else if (!field.getType().isPrimitive()) {
                String next = sc.next();
                if ("{".equals(next)) {
                    Object newInstance = field.getType().newInstance();
                    executeBlock(sc, newInstance);
                } else {
                    Object ref = ref(field.getType(), next);
                    field.set(context, ref);
                }
            } else if (field.getType() == Float.TYPE) {
                field.set(context, Float.parseFloat(sc.next()));
            } else if (field.getType() == Boolean.TYPE) {
                field.set(context, Boolean.parseBoolean(sc.next()));
            } else {
                throw new ParseException("Field '" + item + "' is not handled on " + context.getClass().getName());
            }
        }
    }

    private void handleTemplate(ScannerWithPushBack sc, EntityTemplate template) throws IllegalAccessException, InstantiationException, ReflectionException {
        while (sc.hasNext()) {
            String item = sc.next();
            if ("}".equals(item)) {
                return;
            }
            Class<Component> componentClass = (Class<Component>) classForIdentifier.get(item);
            if (componentClass == null) {
                throw new IllegalStateException("No class for identifier '" + item + "'!");
            }
            Component component = componentClass.newInstance();
            String next = sc.next();
            if (!"{".equals(next)) {
                throw new IllegalStateException("Expected component initializer, but found '" + next + "'!");
            }
            executeBlock(sc, component);
            template.component.add(component);
        }
    }

    private void handleEntity(ScannerWithPushBack sc, Entity entity) throws IllegalAccessException, InstantiationException, ReflectionException {
        while (sc.hasNext()) {
            String item = sc.next();
            if ("}".equals(item)) {
                return;
            }
            if ("template".equals(item)) {
                String templateName = sc.next();
                EntityTemplate entityTemplate = ref(EntityTemplate.class, templateName);
                for (Component c : entityTemplate.component) {
                    entity.add(kryo.copy(c));
                }
            } else {
                Class<Component> componentClass = (Class<Component>) classForIdentifier.get(item);
                if (componentClass == null) {
                    throw new IllegalStateException("No class for identifier '" + item + "'!");
                }
                Component component = entity.getComponent(componentClass);
                if (component == null) {
                    component = componentClass.newInstance();
                }
                String next = sc.next();
                if (!"{".equals(next)) {
                    throw new IllegalStateException("Expected component initializer, but found '" + next + "'!");
                }
                executeBlock(sc, component);
                entity.add(component);
            }
        }
    }

    private <T> T getOrCreate(Object context, Field field, Class<T> type) throws ReflectionException, IllegalAccessException, InstantiationException {
        T result = (T) field.get(context);
        if (result == null) {
            result = type.newInstance();
            field.set(context, result);
        }
        return result;
    }

    private <T> T ref(Class<T> type, String id) throws IllegalAccessException, InstantiationException {
        Object reference = refTable.get(id);
        if (reference == null) {
            T newInstance = type.newInstance();
            refTable.put(id, newInstance);
            return newInstance;
        }
        if (!type.isInstance(reference)) {
            throw new IllegalStateException("Expected '" + id + "' to be a '" + type.getName() + "', but was a '" + reference.getClass().getName() + "'");
        }
        return (T) reference;
    }

    public static class ParseException extends RuntimeException {
        public ParseException(String message) {
            super(message);
        }
    }

    public static class ScannerWithPushBack {
        private final InputStreamReader reader;
        private Array<String> pushedBack = new Array<String>();

        public ScannerWithPushBack(InputStreamReader reader) {
            this.reader = reader;
        }

        public void close() throws IOException {
            reader.close();
        }

        public String next() {
            if (pushedBack.size == 0) {
                produceNext();
            }
            return pushedBack.pop();
        }

        public boolean hasNext() {
            if (pushedBack.size > 0) {
                return true;
            }
            produceNext();
            return pushedBack.size > 0;
        }

        private void produceNext() {
            StringBuilder nextToken = new StringBuilder();
            int nxt;
            while ((nxt = nextChar()) != -1 && (nxt == '\n' || nxt == '\r' || nxt == ' ' || nxt == '\t')) ;
            if (nxt < 0) {
                return;
            }
            if (nxt == '"') {
                while ((nxt = nextChar()) != -1 && nxt != '"') nextToken.append((char) nxt);
            } else {
                nextToken.append((char) nxt);
                while ((nxt = nextChar()) != -1 && nxt != '\n' && nxt != '\r' && nxt != ' ' && nxt != '\t')
                    nextToken.append((char) nxt);
            }
            pushBack(nextToken.toString());
        }

        private int nextChar() {
            try {
                return reader.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void pushBack(String item) {
            pushedBack.add(item);
        }
    }
}
