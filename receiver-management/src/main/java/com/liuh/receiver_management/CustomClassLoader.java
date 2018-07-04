package com.liuh.receiver_management;

import java.io.File;
import java.io.IOException;

import dalvik.system.DexClassLoader;

/**
 * Date: 2018/7/4 16:53
 * Description:
 */

public class CustomClassLoader extends DexClassLoader {

    public CustomClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }

    /**
     * 获取插件中的ClassLoader，能够加载指定的插件中的类
     *
     * @param plugin
     * @param packageName
     * @return
     * @throws IOException
     */
    public static CustomClassLoader getPluginClassLoader(File plugin, String packageName) throws IOException {
        return new CustomClassLoader(plugin.getPath(),
                Utils.getPluginOptDexDir(packageName).getPath(),
                Utils.getPluginLibDir(packageName).getPath(),
                UPFApplication.getContext().getClassLoader());
    }


}
