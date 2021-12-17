package callow.common;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.swing.JOptionPane;

public class PatchManager implements ClassFileTransformer {

    private final ClassPool pool;
    private final HashMap<IClassPatch, HashMap<String, Boolean>> patchStatuses;

    public PatchManager(IClassPatch[] patches, boolean loadDirs) {
        this.patchStatuses = new HashMap<>();
        for (IClassPatch patch: patches)
        {
            HashMap<String, Boolean> patchClassStatuses = new HashMap<>();
            for (String className: patch.getListPatchedClasses()) {
                patchClassStatuses.put(className, false);
            }
            patchStatuses.put(patch, patchClassStatuses);
        }

        pool = ClassPool.getDefault();
        if (loadDirs)
            try (Stream<Path> paths = Files.walk(Paths.get(""))) {
                paths
                        .filter(Files::isDirectory)
                        .forEach(path -> {
                            try {
                                pool.appendPathList(path.toString() + "\\*");
                            } catch (NotFoundException e) {
                                System.out.println("[-] Failed to add dir to ClassPool: " + path);
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
        byte[] classBytes = new byte[0];

        CtClass currentClass;
        className = className.replace('/', '.');
        try {
            currentClass = pool.get(className);
        } catch (NotFoundException e) {
            return classBytes;
        }

        for (IClassPatch patch: patchStatuses.keySet()) {
            if (isClassValidForPatch(className, patch)) {
                if (patch.patch(pool, currentClass))
                    try {
                        classBytes = currentClass.toBytecode();
                        System.out.println("[+] Patch injected: " + patch.getClass().getSimpleName());
                        patchStatuses.get(patch).put(className, true);
                    } catch (CannotCompileException | IOException e) {
                        JOptionPane.showMessageDialog(null,  "Applying '" + patch.getPatchName() + "' ends with error.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                else
                    JOptionPane.showMessageDialog(null, "Applying '" + patch.getPatchName() + "' ends with error.",
                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return classBytes;
    }

    public List<String> getInactivePatches() {
        ArrayList<String> inactivePatches = new ArrayList<>();
        for (Map.Entry<IClassPatch, HashMap<String, Boolean>> patchEntry: patchStatuses.entrySet()) {
            if (!patchEntry.getKey().isPatchRequired())
                continue;

            IClassPatch.PatchClassMode patchMode = patchEntry.getKey().getPatchMode();
            boolean isFailed = patchMode == IClassPatch.PatchClassMode.ANY;
            for (Map.Entry<String, Boolean> classEntry: patchEntry.getValue().entrySet()) {
                if (patchMode == IClassPatch.PatchClassMode.ALL && !classEntry.getValue()) {
                    isFailed = true;
                    break;
                } else if (patchMode == IClassPatch.PatchClassMode.ANY && classEntry.getValue()) {
                    isFailed = false;
                    break;
                }
            }

            if (isFailed)
                inactivePatches.add(patchEntry.getKey().getPatchName());
        }
        return inactivePatches;
    }

    public void showInactivePatchesError() {
        List<String> inactivePatches = getInactivePatches();
        if (inactivePatches.size() == 0)
            return;

        StringBuilder message = new StringBuilder("Не удалось провести патчи:\n");
        for (String name: inactivePatches)
            message.append(name).append("\n");
        message.append("Возможно ModLoader устарел и не подходит для данной версии программы.");

        JOptionPane.showMessageDialog(null, message.toString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isClassValidForPatch(String className, IClassPatch patch) {
        return patch.getListPatchedClasses().contains(className);
    }
}
