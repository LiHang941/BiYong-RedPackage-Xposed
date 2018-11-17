package xyz.lihang.biyong.xposed;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import xyz.lihang.biyong.xposed.extend.Timing;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class MyXposed implements IXposedHookLoadPackage {

    public static HookUserConfig hookUserConfig = new HookUserConfig();
    public static HookGetMessage hookGetMessage = new HookGetMessage();
    public static HookRequest hookRequest = new HookRequest();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals("org.telegram.btcchat")) return;
        new Timing(loadPackageParam, true) {
            @Override
            protected void onNewActivity(XC_MethodHook.MethodHookParam param) {
                super.onNewActivity(param);
                hookCall(loadPackageParam);
            }

            @Override
            protected void afterNewActivity(Activity activity) {
                super.afterNewActivity(activity);
                hookCall(loadPackageParam);
            }

            @Override
            protected void onNewApplication(Application application) {
                super.onNewApplication(application);
                hookCall(loadPackageParam);
            }

            @Override
            protected void afterNewApplication(Application application) {
                super.afterNewApplication(application);
                hookCall(loadPackageParam);
            }

            @Override
            protected void onAttachBaseContext(Context context) {
                super.onAttachBaseContext(context);
                hookCall(loadPackageParam);
            }

            @Override
            protected void afterAttachBaseContext(Context context) {
                super.afterAttachBaseContext(context);
                hookCall(loadPackageParam);
            }
        };
    }


    private void hookCall(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        hookUserConfig.initHook(loadPackageParam);
        hookGetMessage.initHook(loadPackageParam);
        hookRequest.initHook(loadPackageParam);
    }


}
