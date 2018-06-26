package com.liuh.classloader_hook.classloader_hook;

import dalvik.system.DexClassLoader;

/**
 * Created by huan on 2018/6/26.
 * 自定义的ClassLoader，用于加载“插件”的资源和代码
 */

public class CustomClassLoader extends DexClassLoader {
    public CustomClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }
}
