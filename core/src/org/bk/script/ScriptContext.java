package org.bk.script;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
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
import org.bk.data.component.Character;
import org.bk.data.script.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

/**
 * Created by dante on 29.10.2016.
 */
public class ScriptContext {
    private final Initializable root;
    private Kryo kryo = new Kryo();
    private ObjectMap<Object, Object> refTable = new ObjectMap<Object, Object>();
    private ObjectMap<String, Class<?>> classForIdentifier = new ObjectMap<String, Class<?>>();

    public ScriptContext(Initializable root) {
        this.root = root;
        for (Class<? extends Component> component : Mapper.MAPPED_COMPONENTS) {
            registerClass(component.getSimpleName(), component);
        }
    }

    public void registerClass(String identifier, Class<?> aClass) {
        classForIdentifier.put(identifier, aClass);
    }

    public void load(Reader in) {
        ScannerWithPushBack sc = new ScannerWithPushBack(in);
        try {
            executeScript(sc, root);
            root.afterFieldsSet();
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
        } else if (context instanceof Script) {
            parseScript(sc, (Script) context);
            return;
        }
        while (sc.hasNext()) {
            String item = sc.next();
            if ("}".equals(item)) {
                return;
            }
            Field field = ClassReflection.getField(context.getClass(), item);
            if (ObjectMap.class.isAssignableFrom(field.getType())) {
                parseObjectMapAssignment(sc, context, field);
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
                    Object instanceToUse = field.get(context);
                    if (instanceToUse == null) {
                        instanceToUse = field.getType().newInstance();
                    }
                    executeBlock(sc, instanceToUse);
                    field.set(context, instanceToUse);
                } else {
                    Object ref = ref(field.getType(), next);
                    field.set(context, ref);
                }
            } else if (field.getType() == Float.TYPE) {
                field.set(context, Float.parseFloat(sc.next()));
            } else if (field.getType() == Boolean.TYPE) {
                field.set(context, Boolean.parseBoolean(sc.next()));
            } else if (field.getType() == Long.TYPE) {
                applyLong(sc, context, field);
            } else {
                unexpectedToken(context, item);
            }
        }
    }

    private void parseObjectMapAssignment(ScannerWithPushBack sc, Object context, Field field) throws ReflectionException, IllegalAccessException, InstantiationException {
        String key = sc.next();
        Object keyObject;
        ObjectMap<Object, Object> map = (ObjectMap<Object, Object>) field.get(context);
        Object value;
        if (String.class == field.getElementType(0)) {
            keyObject = key;
        } else {
            keyObject = ref(field.getElementType(0), key);
        }
        value = map.get(keyObject);
        if (value == null) {
            if (context instanceof GameData) {
                value = ref(field.getElementType(1), keyObject);
            } else {
                value = field.getElementType(1).newInstance();
            }
            map.put(keyObject, value);
        }
        consume(sc, "{");
        executeBlock(sc, value);
    }

    private void applyLong(ScannerWithPushBack sc, Object context, Field field) throws ReflectionException {
        long value;
        String valueOrOp = sc.next();
        if ("+".equals(valueOrOp)) {
            value = ((Long) field.get(context)).longValue() + Long.parseLong(sc.next());
        } else {
            value = Long.parseLong(valueOrOp);
        }

        field.set(context, value);
    }

    private void unexpectedToken(Object context, String item) {
        throw new ParseException("Token '" + item + "' is not handled on " + context.getClass().getName());
    }

    private void parseScript(ScannerWithPushBack sc, Script scripted) {
        while (sc.hasNext()) {
            String item = sc.next();
            if ("}".equals(item)) {
                return;
            }
            ScriptItem itemToAdd;
            if ("text".equals(item)) {
                consume(sc, "{");
                Text text = new Text();
                parseText(sc, text);
                itemToAdd = text;
            } else if ("if".equals(item)) {
                If scriptIf = new If();
                scriptIf.condition = sc.next();
                consume(sc, "{");
                parseScript(sc, scriptIf.script);
                itemToAdd = scriptIf;
            } else if ("choice".equals(item)) {
                Choice choice = new Choice();
                consume(sc, "{");
                parseChoice(sc, choice);
                itemToAdd = choice;
            } else if ("change".equals(item)) {
                Change change = new Change();
                consume(sc, "{");
                parseChange(sc, change);
                itemToAdd = change;
            } else {
                unexpectedToken(scripted, item);
                itemToAdd = null;
            }
            scripted.items.add(itemToAdd);
        }
    }

    private void parseChange(ScannerWithPushBack sc, Change change) {
        int openParens = 1;
        StringBuilder script = new StringBuilder();
        while (sc.hasNext()) {
            String item = sc.next();
            if ("}".equals(item)) {
                openParens--;
                if (openParens == 0) {
                    break;
                }
            } else if ("{".equals(item)) {
                openParens++;
            }
            if (script.length > 0) {
                script.append(' ');
            }
            script.append('"');
            script.append(item);
            script.append('"');
        }
        change.script = script.toString();
    }

    private void parseChoice(ScannerWithPushBack sc, Choice choice) {
        while (sc.hasNext()) {
            String item = sc.next();
            if ("}".equals(item)) {
                return;
            }
            Choice.Option option = new Choice.Option();
            option.condition = item;
            Text text = new Text();
            consume(sc, "{");
            parseText(sc, text);
            option.text = text;
            choice.options.add(option);
        }
    }

    private void parseText(ScannerWithPushBack sc, Text text) {
        while (sc.hasNext()) {
            String item = sc.next();
            if ("}".equals(item)) {
                return;
            }
            text.line.add(item);
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
            consume(sc, "{");
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
                consume(sc, "{");
                executeBlock(sc, component);
                entity.add(component);
            }
        }
    }

    private void consume(ScannerWithPushBack sc, String expected) {
        String next = sc.next();
        if (!expected.equals(next)) {
            throw new IllegalStateException("Expected '" + expected + "', but found '" + next + "'!");
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

    private <T> T ref(Class<T> type, Object id) throws IllegalAccessException, InstantiationException {
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
        private final Reader reader;
        private Array<String> pushedBack = new Array<String>();

        public ScannerWithPushBack(Reader reader) {
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
                while ((nxt = nextChar()) != -1 && nxt != '"') {
                    if (nxt == '\r' || nxt == '\n') {
                        throw new ParseException("Unclosed token: '" + nextToken + "...'");
                    }
                    nextToken.append((char) nxt);
                }
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
