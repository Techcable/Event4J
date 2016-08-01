package net.techcable.event4j.asm;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GeneratedClassLoader extends ClassLoader {
    private static final ConcurrentMap<ClassLoader, GeneratedClassLoader> loaders = new ConcurrentHashMap<>();
    public static GeneratedClassLoader getLoader(ClassLoader parent) {
        return loaders.computeIfAbsent(Objects.requireNonNull(parent, "Null parent class-loader"), GeneratedClassLoader::new);
    }

    public Class<?> defineClass(String name, byte[] data) {
        name = Objects.requireNonNull(name, "Null name").replace('/', '.');
        synchronized (getClassLoadingLock(name)) {
            if (hasClass(name)) throw new IllegalStateException(name + " already defined");
            Class<?> c = this.define(name, Objects.requireNonNull(data, "Null data"));
            if (!c.getName().equals(name)) throw new IllegalArgumentException("class name " + c.getName() + " != requested name " + name);
            return c;
        }
    }

    protected GeneratedClassLoader(ClassLoader parent) {
        super(parent);
    }

    private Class<?> define(String name, byte[] data) {
        synchronized (getClassLoadingLock(name)) {
            if (hasClass(name)) throw new IllegalStateException("Already has class: " + name);
            Class<?> c;
            try {
                c = defineClass(name, data, 0, data.length);
            } catch (ClassFormatError e) {
                throw new IllegalArgumentException("Illegal class data", e);
            }
            resolveClass(c);
            return c;
        }
    }

    @Override
    public Object getClassLoadingLock(String name) {
        return super.getClassLoadingLock(name);
    }

    public boolean hasClass(String name) {
        synchronized (getClassLoadingLock(name)) {
            try {
                Class.forName(name);
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }

}
