package com.newabel.service_management;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 2018/7/17 10:13
 * Description:
 */

public final class ServiceManager {

    private static final String TAG = "ServiceManager";

    private static volatile ServiceManager mInstance;

    //存储插件的Service信息
    private Map<ComponentName, Service> mServiceMap = new HashMap<ComponentName, Service>();

    public synchronized static ServiceManager getInstance() {
        if (mInstance == null) {
            mInstance = new ServiceManager();
        }
        return mInstance;
    }

    /**
     * 启动某个插件Service；如果插件Service还没有启动，那么会创建新的插件Service
     *
     * @param proxyIntent
     * @param startId
     */
    public void onStart(Intent proxyIntent, int startId) {
//        Intent targetIntent = proxyIntent.getParcelableExtra(AMSHookHelper.EXTRA_TARGET_INTENT);
//        ServiceInfo serviceInfo = selectPluginService(targetIntent);
//
//        if (serviceInfo == null) {
//            Log.w(TAG, "can not found service : " + targetIntent.getComponent());
//            return;
//        }
//
//        if (!mServiceMap.containsKey(serviceInfo.name)) {
//            //service还不存在，先创建
//            proxyCreateService(serviceInfo);
//        }
//
//        Service service = mServiceMap.get(serviceInfo.name);
//        service.onStart(targetIntent, startId);

    }

    /**
     * 停止某个插件Service，当全部的插件Service都停止之后，ProxyService也会停止
     *
     * @param targetIntent
     * @return
     */
    public int stopService(Intent targetIntent) {
//        ServiceInfo serviceInfo = selectPluginService(targetIntent);
//        if (serviceInfo == null) {
//            Log.w(TAG, "can not found service: " + targetIntent.getComponent());
//            return 0;
//        }
//
//        Service service = mServiceMap.get(serviceInfo.name);
//        if (service == null) {
//            Log.w(TAG, "can not running ,are you stopped it multi_times?");
//            return 0;
//        }
//
//        service.onDestroy();
//        mServiceMap.remove(serviceInfo.name);
//        if (mServiceMap.isEmpty()) {
//            //没有Service了，这个没有必要存在了
//            Log.d(TAG, "service all stopped, stop proxy");
//            Context appContext = UPFApplication.getContext();
//            appContext.stopService(new Intent().setComponent(new ComponentName(appContext.getPackageName(),
//                    ProxyService.class.getName())));
//        }
        return 1;
    }

}
