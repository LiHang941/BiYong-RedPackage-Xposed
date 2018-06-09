package hdfg159.qqsendpoke.hook;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Project:QQSendPoke
 * Package:hdfg159.qqsendpoke.hook
 * Created by hdfg159 on 2017/2/6 23:20.
 */
class QQSendPokeHook {
    private final XC_LoadPackage.LoadPackageParam loadPackageParam;

    public QQSendPokeHook(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        this.loadPackageParam = loadPackageParam;
    }


    Set<Method> set = new HashSet<>();

    public void initAndHook() {
        XposedHelpers.findAndHookMethod(XposedHelpers.findClass("java.lang.ClassLoader", loadPackageParam.classLoader)
                , "loadClass", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Class<?> clazz = (Class<?>) param.getResult();
                if( clazz!= null && clazz.getName().indexOf("org.telegram")!=-1){
                    Method[] declaredMethods = clazz.getDeclaredMethods();
                    Log.e("[执行方法]:" , "-------------init--------------" + Arrays.toString(declaredMethods));
                    for (final Method method : declaredMethods) {
                        if(!set.contains(method)){
                            set.add(method);
                            XposedBridge.hookMethod(method, new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    super.afterHookedMethod(param);
                                    Log.e("[执行方法]:" ,
                                            "class -- " + param.thisObject == null ? null : param.thisObject.getClass()
                                            + "-- methodName -- "  + param.method.getName()
                                            + "-- ParameterTypes --" + Arrays.toString(method.getParameterTypes())
                                            + "-- result -- " + param.getResult()
                                            + "-- param.args -- " + Arrays.toString(param.args) + "--" );
                                }
                            });
                        }
                    }
                }
            }
        });


    }

}
