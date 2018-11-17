package xyz.lihang.biyong.xposed;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.lang.reflect.Method;

/**
 * @author : lihang1329@gmail.com
 * @since : 2018/10/1
 */
public class HookRequest extends MyXposedHook {


    private Method isRedPacketEx(ClassLoader classLoader) throws NoSuchMethodException, ClassNotFoundException {
        if (classLoader == null) {
            throw new NoSuchMethodException("MyXposed.classLoader == null");
        }
        return Class
                .forName("org.telegram.messenger.BiYongMessageFilter", false, classLoader)
                .getMethod("isRedPacketEx", Class.forName("org.telegram.messenger.MessageObject", false, classLoader), int.class, String.class);
    }


    public void initHook(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            this.classLoader = loadPackageParam.classLoader;
            //this.hook(Class.forName("org.telegram.btcchat.network.NetHelper", false, classLoader),".+");
            this.hookSuccessClassLoader = this.classLoader;
        } catch (Exception e) {
            Utils.error(e);
        }
    }


}
