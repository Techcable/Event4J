package net.techcable.event4j.asm;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.techcable.event4j.EventBus;
import net.techcable.event4j.EventExecutor;
import net.techcable.event4j.RegisteredListener;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class ASMEventExecutorFactory implements EventExecutor.Factory {
    public static final ASMEventExecutorFactory INSTANCE;
    static {
        ASMEventExecutorFactory instance = null;
        try {
            Class.forName("org.objectweb.asm.Opcodes");
            instance = new ASMEventExecutorFactory();
        } catch (ClassNotFoundException ignored) { }
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
        final boolean staticMethod = Modifier.isStatic(m.getModifiers());
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(V1_8, ACC_PUBLIC, name, null, "java/lang/Object", new String[] {Type.getInternalName(EventExecutor.class)});
        // Generate constructor
        MethodVisitor methodGenerator = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        methodGenerator.visitVarInsn(ALOAD, 0);
        methodGenerator.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false); // Invoke the super class (Object) constructor
        methodGenerator.visitInsn(RETURN);
        methodGenerator.visitMaxs(1, 1);
        methodGenerator.visitEnd();
        // Generate the execute method
        methodGenerator = writer.visitMethod(ACC_PUBLIC, "fire", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
        if (!staticMethod) {
            methodGenerator.visitVarInsn(ALOAD, 1);
            methodGenerator.visitTypeInsn(CHECKCAST, Type.getInternalName(m.getDeclaringClass()));
        }
        methodGenerator.visitVarInsn(ALOAD, 2);
        methodGenerator.visitTypeInsn(CHECKCAST, Type.getInternalName(m.getParameterTypes()[0]));
        methodGenerator.visitMethodInsn(staticMethod ? INVOKESTATIC : INVOKEVIRTUAL, Type.getInternalName(m.getDeclaringClass()), m.getName(), Type.getMethodDescriptor(m), m.getDeclaringClass().isInterface());
        if (m.getReturnType() != void.class) {
            methodGenerator.visitInsn(POP);
        }
        methodGenerator.visitInsn(RETURN);
        methodGenerator.visitMaxs(staticMethod ? 1 : 2, 3);
        methodGenerator.visitEnd();
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
