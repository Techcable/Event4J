/**
 * The MIT License
 * Copyright (c) 2015-2016 Techcable
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
