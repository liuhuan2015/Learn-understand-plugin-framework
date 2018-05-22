##### 1.新建一个工程，然后需要在测试类Test里面跑一个main函数，汇报这个错误：

    Error:Gradle: failed to create directory 'D:\workplace\Learn-understand-plugin-framework\dynamic-proxy-hook\build\generated\source\r\debug\com\liuh\dynamic_proxy_hook'.
    Error:Gradle: java.util.concurrent.ExecutionException: java.util.concurrent.ExecutionException: com.android.tools.aapt2.Aapt2Exception: AAPT2 error: check logs for details
    Error:Gradle: java.util.concurrent.ExecutionException: com.android.tools.aapt2.Aapt2Exception: AAPT2 error: check logs for details
    Error:Gradle: com.android.tools.aapt2.Aapt2Exception: AAPT2 error: check logs for details
    Error:Gradle: Execution failed for task ':dynamic-proxy-hook:processDebugResources'.
        > Failed to execute aapt
网上收到的解决方案是：在项目的gradle.properties中添加，android.enableAapt2=false ，经测试该方案有效。
不知道为什么这样改就可以了，网上查了下，也没找到满意的解释。字面意思大概是：禁用aapt2.
        
