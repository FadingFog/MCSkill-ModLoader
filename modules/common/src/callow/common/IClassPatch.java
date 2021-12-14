package callow.common;

import javassist.ClassPool;
import javassist.CtClass;

import java.util.Enumeration;
import java.util.List;

public interface IClassPatch {

    enum PatchClassMode {
        ANY,
        ALL
    }
    /**
     * @return List of class names to catch in patch
     */
    List<String> getListPatchedClasses();

    /**
     * @return Is the patch required to success
     */
    default boolean isPatchRequired() {
        return false;
    }

    /**
     * @return Mode for patch
     */
    default PatchClassMode getPatchMode() {
        return PatchClassMode.ALL;
    }

    String getPatchName();

    /**
     * @param pool Pool of classes
     * @param ctClass Class that is loading now, will be class from the list returned by getListPatchedClasses
     * @return Is patch successful or not
     */
    boolean patch(ClassPool pool, CtClass ctClass);
}
