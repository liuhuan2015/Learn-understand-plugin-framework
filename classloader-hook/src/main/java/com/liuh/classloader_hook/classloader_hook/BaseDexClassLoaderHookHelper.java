package com.liuh.classloader_hook.classloader_hook;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

/**
 * Date: 2018/6/26 10:08
 * Description:
 * 由于应用程序使用的ClassLoader为PathClassLoader，
 * 最终继承自BaseDexClassLoader,
 * 查看源码得知，这个BaseDexClassLoader加载代码是根据一个叫做
 * dexElements的数组来进行的，因此我们把包含代码的dex文件插入这个数组，
 * 系统的classloader就能帮助我们找到这个类
 * <p>
 * 这个类是用来进行对于BaseDexClassLoader的Hook的
 */

public final class BaseDexClassLoaderHookHelper {

    public static void patchClassLoader(ClassLoader cl, File apkFile, File optDexFile)
            throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, IOException, InvocationTargetException, InstantiationException {

        //获取BaseDexClassLoader：pathList
        Field pathListField = DexClassLoader.class.getSuperclass().getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object pathListObj = pathListField.get(cl);

        //获取PathList： Element[] dexElements
        Field dexElementArray = pathListObj.getClass().getDeclaredField("dexElements");
        dexElementArray.setAccessible(true);
        Object[] dexElements = (Object[]) dexElementArray.get(pathListObj);

        //Element类型
        Class<?> elementClass = dexElements.getClass().getComponentType();

        //创建一个数组，用来替换原始的数组
        Object[] newElements = (Object[]) Array.newInstance(elementClass, dexElements.length + 1);

        //构造插件Element(File file,boolean isDirectory,File zip,DexFile dexFile)
        Constructor<?> constructor = elementClass.getConstructor(File.class, boolean.class, File.class, DexFile.class);
        Object o = constructor.newInstance(apkFile, false, apkFile, DexFile.loadDex(apkFile.getCanonicalPath(), optDexFile.getAbsolutePath(), 0));

        Object[] toAddElementArray = new Object[]{o};
        //把原始的elements复制进去
        System.arraycopy(dexElements, 0, newElements, 0, dexElements.length);
        //把插件的那个element复制进去
        System.arraycopy(toAddElementArray, 0, newElements, dexElements.length, toAddElementArray.length);
        //替换
        dexElementArray.set(pathListObj, newElements);
    }

}
