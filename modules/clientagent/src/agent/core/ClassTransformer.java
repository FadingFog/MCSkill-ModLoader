package agent.core;

import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.stream.Stream;

public class ClassTransformer implements ClassFileTransformer {

    private final ClassPool pool;
    ClassTransformer() {
        pool = ClassPool.getDefault();
        try (Stream<Path> paths = Files.walk(Paths.get(""))) {
            paths
                    .filter(Files::isDirectory)
                    .forEach(path -> {
                        try {
                            pool.appendPathList(path.toString() + "\\*");
                        } catch (NotFoundException e) {
                            System.out.println("[-] Failed to add dir to ClassPool: " + path.toString());
                        }
                    });
        } catch (Exception e) {
            System.out.println("[-] Failed add paths in ClassPool.");
            e.printStackTrace();
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        className = className.replace('/', '.');
        byte[] classBytes = new byte[0];

        CtClass currentClass;
        try {
            currentClass = pool.get(className);
        } catch (NotFoundException e) {
            return classBytes;
        }

        switch (className) {
            case "net.minecraftforge.fml.common.discovery.ModDiscoverer":
            case "cpw.mods.fml.common.discovery.ModDiscoverer":
                try {
                    CtMethod method = currentClass.getDeclaredMethod("identifyMods");
                    method.insertAfter("$_ = agent.core.Callbacks.onDiscoveringMods($_);");

                    classBytes = currentClass.toBytecode();

                    System.out.println("[+] ModDiscoverer patched.");
                } catch (NotFoundException | IOException | CannotCompileException e) {
                    e.printStackTrace();
                }
                break;

            case "cpw.mods.fml.common.FMLModContainer":
            case "net.minecraftforge.fml.common.FMLModContainer":
                try {

                    CtMethod setMethod = CtMethod.make(
                            "public void setModId(java.lang.String modId) { this.descriptor.put(\"modid\", modId); }", currentClass);
                    currentClass.addMethod(setMethod);

                    setMethod = CtMethod.make(
                            "public void setVersion(java.lang.String version) { this.internalVersion = version; }", currentClass);
                    currentClass.addMethod(setMethod);

//                    CtMethod method = currentClass.getDeclaredMethod("sanityCheckModId");
//                    method.insertBefore("{ agent.core.Callbacks.onCreateModContainer($0.source, $0.descriptor); }");
                    System.out.println("[+] FMLModContainer: ID message installed.");
                    classBytes = currentClass.toBytecode();
                } catch (CannotCompileException  | IOException e) {
                    System.out.println("[-] FMLModContainer: ID message has error while installing.");
                    e.printStackTrace();
                }

                break;
            // Minecraft Client run
            case "o.AUx":
                try {
                    CtMethod method = currentClass.getDeclaredMethod("main");
                    method.setBody("{ Object[] params = agent.core.Callbacks.onRunningClient($1); o.AUx.aux((o.AUX)params[0], (o.aUX)params[1]); }");

                    System.out.println("[+] ClientRun | AUx.main(): Callback was installed.");

                    classBytes = currentClass.toBytecode();

                } catch (NotFoundException | CannotCompileException | IOException e) {
                    System.out.println("[-] ClientRun | AUx.main(): Callback install failed.");
                    e.printStackTrace();
                }
                break;
        }

        return classBytes;
    }

}
