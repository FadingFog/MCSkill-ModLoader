package callow.common;

import javassist.ClassPool;
import javassist.CtClass;

public interface IClassPatcher {
    boolean patch(ClassPool pool, CtClass ctClass);
}
