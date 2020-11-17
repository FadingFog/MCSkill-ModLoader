package main.mcagent;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.regex.Pattern;

public class ClassTransformer implements ClassFileTransformer {

    private final ClassPool pool;
    ClassTransformer() {
        pool = ClassPool.getDefault();
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        className = className.replace('/', '.');
        if (Pattern.matches("^o.[^.]+$", className)){
            if (PropertiesFields.debug)
                System.out.println("[Debug] Class loaded: " + className);
            try {
                CtClass currentClass = pool.get(className);
                return currentClass.toBytecode();
            } catch (NotFoundException | IOException | CannotCompileException ignored) { }
        }

        return new byte[0];
    }
}
