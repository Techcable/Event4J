package net.techcable.event4j.asm;

import lombok.*;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.techcable.event4j.EventBus;
import net.techcable.event4j.EventExecutor;
import net.techcable.event4j.RegisteredListener;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import static org.objectweb.asm.Opcodes.*;

public class ASMEventExecutorFactory implements EventExecutor.Factory {
    public static final Optional<ASMEventExecutorFactory> INSTANCE;
    static {
        Optional<ASMEventExecutorFactory> instance = Optional.empty();
        try {
            Class.forName("org.objectweb.asm.Opcodes");
            instance = Optional.of(new ASMEventExecutorFactory());
        } catch (ClassNotFoundException ignored) {}
        INSTANCE = instance;
    }

    protected static Class<? extends EventExecutor> generateExecutor(Method method) { // DOESN'T CACHE!
        Objects.requireNonNull(method, "Null method");
        String name = generateName();
        byte[] data = generateEventExecutor(method, name);
        ClassLoader listenerLoader = method.getDeclaringClass().getClassLoader();
        GeneratedClassLoader loader = GeneratedClassLoader.getLoader(Objects.requireNonNull(listenerLoader, "Null class loader for " + method.getDeclaringClass()));
        return loader.defineClass(name, data).asSubclass(EventExecutor.class);
    }

    private static byte[] generateEventExecutor(Method m, String name) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(V1_8, ACC_PUBLIC, name, null, "java/lang/Object", new String[] {Type.getInternalName(EventExecutor.class)});
        // Generate constructor
        GeneratorAdapter methodGenerator = new GeneratorAdapter(writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null), ACC_PUBLIC, "<init>", "()V");
        methodGenerator.loadThis();
        methodGenerator.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false); // Invoke the super class (Object) constructor
        methodGenerator.returnValue();
        methodGenerator.endMethod();
        // Generate the execute method
        methodGenerator = new GeneratorAdapter(writer.visitMethod(ACC_PUBLIC, "fire", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null), ACC_PUBLIC, "fire", "(Ljava/lang/Object;Ljava/lang/Object;)V");;
        methodGenerator.loadArg(0);
        methodGenerator.checkCast(Type.getType(m.getDeclaringClass()));
        methodGenerator.loadArg(1);
        methodGenerator.checkCast(Type.getType(m.getParameterTypes()[0]));
        methodGenerator.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(m.getDeclaringClass()), m.getName(), Type.getMethodDescriptor(m), m.getDeclaringClass().isInterface());
        if (m.getReturnType() != void.class) {
            methodGenerator.pop();
        }
        methodGenerator.returnValue();
        methodGenerator.endMethod();
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static final String GENERATED_EXECUTOR_BASE_NAME;
    static {
        String className = ASMEventExecutorFactory.class.getName().replace('.', '/');
        String packageName = className.substring(0, className.lastIndexOf('/'));
        GENERATED_EXECUTOR_BASE_NAME = packageName + "/GeneratedEventExecutor";
    }
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    public static String generateName() {
        return GENERATED_EXECUTOR_BASE_NAME.concat(String.valueOf(NEXT_ID.getAndIncrement()));
    }

    private final ConcurrentMap<Method, Class<? extends EventExecutor>> cache = new ConcurrentHashMap<>();

    @Override
    public <E, L> EventExecutor<E, L> create(EventBus<E, L> eventBus, Method method) {
        RegisteredListener.validate(Objects.requireNonNull(eventBus, "Null eventBus"), Objects.requireNonNull(method, "Null method"));
        Class<? extends EventExecutor> executorClass = cache.computeIfAbsent(method, ASMEventExecutorFactory::generateExecutor);
        try {
            return executorClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to initialize " + executorClass, e);
        }
    }


}