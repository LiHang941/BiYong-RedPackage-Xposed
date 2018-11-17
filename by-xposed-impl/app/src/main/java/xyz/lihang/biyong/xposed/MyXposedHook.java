package xyz.lihang.biyong.xposed;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class MyXposedHook extends XC_MethodHook {

    protected ClassLoader classLoader;
    protected ClassLoader hookSuccessClassLoader;

    protected Map<String, Method> methodCache = Collections.synchronizedMap(new HashMap<>());

    // 方法走方法缓存
    public Method getMethod(String key, MethodCacheManager methodCacheManager) throws NoSuchMethodException, ClassNotFoundException {
        Method cache = methodCache.get(key);
        if (cache != null) {
            return cache;
        }
        Method method = methodCacheManager.get();
        if (method == null)
            return null;
        methodCache.put(key, method);
        return method;
    }

    protected interface MethodCacheManager {
        Method get() throws NoSuchMethodException, ClassNotFoundException;
    }


    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        //Write your code here.
        Utils.log("<" + param.method.getDeclaringClass() + " method=" + MethodDescription(param).toString() + ">");
        try {
            for (int i = 0; i < param.args.length; i++) {
                Utils.log("<Arg index=" + i + ">" + translate(param.args[i]) + "</Arg>");
            }
        } catch (Throwable e) {
            Utils.error(e);
        }
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        Object result = param.getResult();
        //Write your code here.

        try {
            Utils.log("<Result>" + translate(result) + "</Result>");
        } catch (Throwable e) {
            Utils.error(e);
        } finally {
            Utils.log("</" + param.method.getDeclaringClass() + " method=" + MethodDescription(param).toString() + ">");
        }

        //You can replace it's result by uncomment this
        //param.setResult(result);
    }


    private String MethodDescription(MethodHookParam param) {
        StringBuilder sb = new StringBuilder();
        sb.append(param.method.getName().toString());
        sb.append("(");
        for (Object arg : param.args) {
            if (arg == null) sb.append("UnknownType");
            else if (arg.getClass().isPrimitive()) sb.append(arg.getClass().getSimpleName());
            else sb.append(arg.getClass().getName());
            sb.append(",");
        }
        sb.append(")");
        return sb.toString();
    }

    private String translate(Object obj) {
        //Write your translator here.
        return obj == null ? "NULL" : obj.toString();
    }

    public void hook(Member method) {
        Utils.log("HookMethod success:: " + method.getName());
        XposedBridge.hookMethod(method, this);
    }

    public void hook(Class clz, String methodRegEx) {
        Pattern pattern = Pattern.compile(methodRegEx);
        for (Member method : clz.getDeclaredMethods()) {
            if (pattern.matcher(method.getName()).matches()){
                hook(method);
            }
        }
    }

    public void initHook(XC_LoadPackage.LoadPackageParam loadPackageParam){

    }

}
