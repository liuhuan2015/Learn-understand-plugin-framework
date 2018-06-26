package com.liuh.classloader_hook;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Date: 2018/6/26 09:12
 * Description:
 */

public class Utils {

    /**
     * 把Assets里面的文件复制到/data/data/<package>/files目录下
     *
     * @param context
     * @param sourceName
     */
    public static void extractAssets(Context context, String sourceName) {
        AssetManager am = context.getAssets();
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            is = am.open(sourceName);
            File extractFile = context.getFileStreamPath(sourceName);
            fos = new FileOutputStream(extractFile);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSilently(is);
            closeSilently(fos);
        }
    }

    private static void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 待加载插件经过opt优化之后存放odex的路径
     *
     * @param packageName
     * @return
     */
    public static File getPluginOptDexDir(String packageName) {
        return enforceDirExists(new File(getPluginBaseDir(packageName), "odex"));
    }

    /**
     * 插件得lib库路径, 这个demo里面没有用
     */
    public static File getPluginLibDir(String packageName) {
        return enforceDirExists(new File(getPluginBaseDir(packageName), "lib"));
    }

    private static File sBaseDir;

    //需要加载的插件的基本目录 /data/data/<package>/files/plugin/
    private static File getPluginBaseDir(String packageName) {
        if (sBaseDir == null) {
            sBaseDir = UPFApplication.getContext().getFileStreamPath("plugin");
            enforceDirExists(sBaseDir);
        }
        return enforceDirExists(new File(sBaseDir, packageName));
    }


    private static synchronized File enforceDirExists(File sBaseDir) {
        if (!sBaseDir.exists()) {
            boolean ret = sBaseDir.mkdir();
            if (!ret) {
                throw new RuntimeException("create dir " + sBaseDir + " failed");
            }
        }
        return sBaseDir;
    }

}
