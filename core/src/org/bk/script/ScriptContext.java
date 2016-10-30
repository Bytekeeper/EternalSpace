package org.bk.script;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import org.bk.data.GameData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by dante on 29.10.2016.
 */
public class ScriptContext {
    private final GameData root;
    private ObjectMap<String, Object> refTable = new ObjectMap<String, Object>();

    public ScriptContext(GameData root) {
        this.root = root;
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
            } else if (Array.class.isAssignableFrom(field.getType())) {
                Array<Object> array = (Array<Object>) field.get(context);
                if (array == null) {
                    throw new IllegalStateException("Field '" + item + "' on " + context.getClass().getName() + " in should be an initialized array!");
                }
                Class elementType = field.getElementType(0);
                Object newInstance = elementType.newInstance();
                String next = sc.next();
                if ("{".equals(next)) {
                    executeBlock(sc, newInstance);
                } else {
                    sc.pushBack(next);
                }
                array.add(newInstance);
            } else if (String.class == field.getType()){
                field.set(context, item);
            } else if (!field.getType().isPrimitive()) {
                String next = sc.next();
                if ("{".equals(next)) {
                    Object newInstance = field.getType().newInstance();
                    executeBlock(sc, newInstance);
                } else {
                    Object ref = ref(field.getType(), next);
                    field.set(context, ref);
                }
            } else {
                throw new ParseException("Field '" + item + "' is not handled on " + context.getClass().getName());
            }
        }
    }

    private Object ref(Class type, String id) throws IllegalAccessException, InstantiationException {
        Object reference = refTable.get(id);
        if (reference == null) {
            Object newInstance = type.newInstance();
            refTable.put(id, newInstance);
            return newInstance;
        }
        if (!type.isInstance(reference)) {
            throw new IllegalStateException("Expected '" + id + "' to be a '" + type.getName() + "', but was a '" + reference.getClass().getName() + "'");
        }
        return reference;
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
                while ((nxt = nextChar()) != -1 && nxt != '\n' && nxt != '\r' && nxt != ' ' && nxt != '\t') nextToken.append((char) nxt);
            }
            pushBack(nextToken.toString());
        }

        private int nextChar()  {
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
