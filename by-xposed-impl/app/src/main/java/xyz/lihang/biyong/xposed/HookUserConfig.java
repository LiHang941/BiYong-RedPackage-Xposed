package xyz.lihang.biyong.xposed;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.lang.reflect.Method;

/**
 * @author : lihang1329@gmail.com
 * @since : 2018/10/1
 */
public class HookUserConfig extends MyXposedHook {

    private int userId = -1;

    private Method loadConfig(ClassLoader classLoader) throws NoSuchMethodException, ClassNotFoundException {
        if (classLoader == null) {
            throw new NoSuchMethodException("classLoader == null");
        }
        return Class.forName("org.telegram.messenger.UserConfig", false, classLoader).getMethod("loadConfig");
    }

    private Method setCurrentUser(ClassLoader classLoader) throws NoSuchMethodException, ClassNotFoundException {
        if (classLoader == null) {
            throw new NoSuchMethodException("classLoader == null");
        }
        return Class.forName("org.telegram.messenger.UserConfig", false, classLoader)
                .getMethod("setCurrentUser", Class.forName("org.telegram.tgnet.TLRPC$User", false, classLoader));
    }

    private Method getClientUserId(ClassLoader classLoader) throws NoSuchMethodException, ClassNotFoundException {
        if (classLoader == null) {
            throw new NoSuchMethodException("classLoader == null");
        }
        return Class.forName("org.telegram.messenger.UserConfig", false, classLoader).getMethod("getClientUserId");
    }


    public void initHook(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            this.classLoader = loadPackageParam.classLoader;
            this.hook(getMethod("loadConfig", () -> loadConfig(this.classLoader)));
            this.hook(getMethod("setCurrentUser", () -> setCurrentUser(this.classLoader)));
            this.hookSuccessClassLoader = this.classLoader;
        } catch (Exception e) {
            Utils.error(e);
        }
    }


    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

    }

    public int getUserId() throws Throwable {
        return updateUserId();
    }

    private int updateUserId() throws  ClassNotFoundException {
        Object o = XposedHelpers.callStaticMethod(
                Class.forName("org.telegram.messenger.UserConfig", false, classLoader),
                "getClientUserId",
                new Class []{});
        if (o == null || (int) o == 0) {
            throw new RuntimeException("获取userId失败");
        }
        this.userId = (int) o;
        return userId;
    }





    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        updateUserId();
    }

}
