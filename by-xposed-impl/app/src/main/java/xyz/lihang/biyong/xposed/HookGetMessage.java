package xyz.lihang.biyong.xposed;

import android.content.Context;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author : lihang1329@gmail.com
 * @since : 2018/10/1
 */
public class HookGetMessage extends MyXposedHook {

    private Method updateInterfaceWithMessages(ClassLoader classLoader) throws NoSuchMethodException, ClassNotFoundException {
        if (classLoader == null) {
            throw new NoSuchMethodException("MyXposed.classLoader == null");
        }
        return Class.forName("org.telegram.messenger.MessagesController", false, classLoader)
                .getDeclaredMethod("updateInterfaceWithMessages", long.class, ArrayList.class);
    }


    private void getRedPacket2(String environment, String identifyId, String redCode) throws ClassNotFoundException {
        //Utils.debug("答题红包");
        Map hashMap = new HashMap();
        hashMap.put("redCode", redCode);
        hashMap.put("environment", environment);
        hashMap.put("identifyId", identifyId);
        request(json -> {
            //Utils.debug(json.toJSONString());
            String status = json.getJSONObject("data").getString("status");
            if ("NORMAL".equals(status)) {
                status = "un_geted_default";
            }
            if ("un_geted_default".equals(status)) {
                JSONArray answers = json.getJSONObject("data").getJSONArray("answers");
                if (answers == null || answers.size() == 0) {
                    return;
                }
                Map hashMap2 = new HashMap();
                hashMap2.put("redCode", redCode);
                hashMap2.put("environment", environment);
                hashMap2.put("identifyId", identifyId);
                hashMap2.put("source", "android");
                hashMap2.put("answerIds", answers.getJSONObject(0).getString("id"));
                qiangtRedPacket(redCode, hashMap2);
            }
        }, hashMap, URL_QUERY_ASK_RED_PACKET_INFO());

    }


    // 普通红包
    private void getRedPacket1(String environment, String identifyId, String redCode) throws ClassNotFoundException {
        HashMap hashMap = new HashMap();
        hashMap.put("redCode", redCode);
        hashMap.put("source", "android");
        hashMap.put("environment", environment);
        hashMap.put("identifyId", identifyId);
        qiangtRedPacket(redCode, hashMap);
    }

    // 抢红包
    private void qiangtRedPacket(String redCode, Map hashMap) throws ClassNotFoundException {
        request(json -> {
                    //Utils.debug(json.toJSONString());
                    if (json.getJSONObject("data") == null) {
                        return;
                    }
                    String status = json.getJSONObject("data").getString("status");
                    updateRedpacketStatus(redCode, json.getJSONObject("data").getString("status"));
                    if ("SUCCESS".equals(status)) {
                        request(json2 -> {
                                    //Utils.debug(json2.toJSONString());
                                    if (json2.getInteger("code") == 200) {
                                        toast("抢到一个红包[" + json2.getJSONObject("data").getString("coinName") + "]:" + json2.getJSONObject("data").getBigDecimal("receiverAmount") + " 个");
                                    }
                                }
                                , Collections.singletonMap("redCode", redCode)
                                , getRedPacketBasicInfoUrl()
                        );
                    } else {
                        toast("抢红包失败:" + getDescByStatus(status));
                    }
                }
                , hashMap, URL_RECEIVE_ASK_RED_PACKET());
    }

    // 解析状态
    public static String getDescByStatus(String str) {
        if (str == null || "".equals(str)) {
            return "";
        }
        String str2 = "";
        if ("SUCCESS".equals(str)) {
            str2 = "领取成功";
        } else if ("ONCEGET".equals(str)) {
            str2 = "已经领取";
        } else if ("FAILURE".equals(str)) {
            str2 = "领取失败";
        } else if ("GETEND".equals(str)) {
            str2 = "您来晚一步，红包已被抢完";
        } else if ("INVALID".equals(str)) {
            str2 = "无效红包";
        } else if ("EXPIRED".equals(str)) {
            str2 = "该红包已超过24小时，如果已领取可在领取记录中查看";
        } else if ("REPETITION".equals(str)) {
            str2 = "重复请求";
        } else if ("ENVIRONMENT".equals(str)) {
            str2 = "环境错误";
        } else if ("WRONG".equals(str)) {
            str2 = "很遗憾-回答错误";
        } else if ("INVOLVED".equals(str)) {
            str2 = "很遗憾-回答错误";
        } else if ("NOGET".equals(str)) {
            str2 = "对方尚未领取你的红包";
        } else if ("un_geted_default".equals(str)) {
            str2 = "领取红包";
        }
        return str2;
    }

    private void updateRedpacketStatus(String redCode, String obj) throws ClassNotFoundException {
        if ("NORMAL".equals(obj)) {
            obj = "un_geted_default";//RedConstant.RED_STATUS_DEFAULT;
        }
        XposedHelpers.callStaticMethod(
                Class.forName("org.telegram.btcchat.db.dao.AskRedPacketInfoDao", false, classLoader),
                "updateRedpacketStatus", redCode, obj
        );
    }


    interface NetListener {
        void success(JSONObject json) throws Throwable;
    }


    private String URL_RECEIVE_ASK_RED_PACKET() throws ClassNotFoundException {
        return (String) XposedHelpers.getStaticObjectField(Class.forName("org.telegram.btcchat.config.UrlConfig", false, classLoader), "URL_RECEIVE_ASK_RED_PACKET");
    }

    private String URL_QUERY_ASK_RED_PACKET_INFO() throws ClassNotFoundException {
        return (String) XposedHelpers.getStaticObjectField(Class.forName("org.telegram.btcchat.config.UrlConfig", false, classLoader), "URL_QUERY_ASK_RED_PACKET_INFO");
    }


    private String getRedPacketBasicInfoUrl() throws ClassNotFoundException {
        return (String) XposedHelpers.callStaticMethod(
                Class.forName("org.telegram.btcchat.config.UrlConfig", false, classLoader),
                "getRedPacketBasicInfoUrl",
                new Class[]{}
        );
    }

    private void request(NetListener netListenerCall, Map hashMap, String url) throws ClassNotFoundException {
        Object instanceNetHelper = XposedHelpers.callStaticMethod(
                Class.forName("org.telegram.btcchat.network.NetHelper", false, classLoader),
                "instance",
                new Class[]{}
        );
        Object netListener = Proxy.newProxyInstance(classLoader, new Class[]{Class.forName("org.telegram.btcchat.network.NetHelper$NetListener", false, classLoader)}, (o, method, args) -> {
            try {
                if (method.getName().equals("onSuccess")) {
                    JSONObject jsonObject = JSON.parseObject((String) args[1]);
                    if (jsonObject != null) netListenerCall.success(jsonObject);
                    else {
                        //Utils.debug("onSuccess -> Failed:" + JSON.toJSONString(args));
                    }
                } else if (method.getName().equals("onFailed")) {
                    //Utils.debug("onFailed:" + JSON.toJSONString(args));
                }
            } catch (Throwable e) {
                Utils.error(e);
            }
            return null;
        });
        XposedHelpers.callMethod(instanceNetHelper, "sendRequest", url, hashMap, netListener);
    }


    public void toast(String text) {
        try {
            Context applicationContext = (Context) XposedHelpers.getStaticObjectField(
                    Class.forName("org.telegram.messenger.ApplicationLoader", false, classLoader)
                    , "applicationContext");
            if (applicationContext == null) {
                Utils.error("applicationContext == null");
                return;
            }
            Toast.makeText(applicationContext, "币用红包助手: " + text, Toast.LENGTH_LONG).show();
        } catch (Throwable e) {
            Utils.error(e);
        }
    }

    class Environment {
        int type;
        int id;
    }

    public String getIdentifyId(Environment environment) throws Throwable {
        if (environment.type == 0 || environment.type == 1) {
            return Integer.toString(environment.id);
        }
        return Integer.toString(MyXposed.hookUserConfig.getUserId());
    }


    //Environment,IdentifyId 0群组 269902895   1订阅  1383161983 2私信 633253753
    public Environment parseEnvironment(Object message) throws NoSuchFieldException, IllegalAccessException {
        checkMessageObject(message);
        Object messageOwner = message.getClass().getField("messageOwner").get(message);
        if (messageOwner == null) {
            throw new NoSuchFieldException("messageOwner 信息不存在");
        }
        Object to_id = messageOwner.getClass().getField("to_id").get(messageOwner);
        if (to_id == null) {
            throw new NoSuchFieldException("to_id 信息不存在");
        }
        int chat_id = to_id.getClass().getField("chat_id").getInt(to_id);
        int user_id = to_id.getClass().getField("user_id").getInt(to_id);
        int channel_id = to_id.getClass().getField("channel_id").getInt(to_id);

        Environment environment = new Environment();
        if (channel_id != 0) {
            environment.id = channel_id;
            environment.type = 1;
            return environment;
        }
        if (chat_id != 0) {
            environment.id = chat_id;
            environment.type = 0;
            return environment;
        }
        if (user_id != 0) {
            environment.id = user_id;
            environment.type = 2;
            return environment;
        }
        throw new NoSuchFieldException("检查类型失败");
    }

    // 检查消息
    private void checkMessageObject(Object message) throws NoSuchFieldException {
        if (message == null || !message.getClass().getName().contains("MessageObject")) {
            throw new NoSuchFieldException("MessageObject == null !!!");
        }
        if (!message.getClass().getName().contains("MessageObject")) {
            throw new NoSuchFieldException("这个不是MessageObject对象");
        }
    }

    // 解析消息
    private void parseMessage(Object message) {
        try {
            Environment environment = parseEnvironment(message);
            String IdentifyId = getIdentifyId(environment);
            boolean isRed = (boolean) XposedHelpers.callStaticMethod(
                    Class.forName("org.telegram.messenger.BiYongMessageFilter", false, classLoader),
                    "isRedPacketEx",
                    new Class[]{Class.forName("org.telegram.messenger.MessageObject", false, classLoader), int.class, String.class},
                    message, environment.type, IdentifyId
            );
            if (isRed) {
                // 抢红包方法
                //Utils.debug("发现红包");
                Object redPacketTextBlock = XposedHelpers.callStaticMethod(Class.forName("org.telegram.btcchat.utils.RedPacketUtils", false, classLoader), "extractRedPacketTextBlock",
                        new Class[]{String.class, int.class, String.class},
                        message.getClass().getField("messageText").get(message).toString(), environment.type, IdentifyId
                );
                //Utils.debug(JSON.toJSONString(redPacketTextBlock));

                // 检查类型
                // redType 普通 1 "redType":0 手气 "redType":3 答题
                String redCode = (String) XposedHelpers.getObjectField(redPacketTextBlock, "redCode");
                String environmentCode = Integer.toString(XposedHelpers.getIntField(redPacketTextBlock, "environment"));
                String identifyId = (String) XposedHelpers.getObjectField(redPacketTextBlock, "identifyId");
                int redType = XposedHelpers.getIntField(redPacketTextBlock, "redType");
                if (redType == 3) {
                    getRedPacket2(environmentCode, identifyId, redCode);
                } else {
                    getRedPacket1(environmentCode, identifyId, redCode);
                }
            }
        } catch (Throwable e) {
            Utils.error(e);
        }
    }


    @Override
    public void initHook(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            this.classLoader = loadPackageParam.classLoader;
            this.hook(getMethod("updateInterfaceWithMessages", () -> updateInterfaceWithMessages(this.classLoader)));
            this.hookSuccessClassLoader = this.classLoader;
        } catch (Throwable e) {
            Utils.error(e);
        }
    }


    private static ExecutorService executorService = Executors.newFixedThreadPool(3);

    @Override
    protected void beforeHookedMethod(MethodHookParam param) {
        ArrayList messageList = param.args[1] == null ? null : (ArrayList) param.args[1];
        if (messageList == null) {
            return;
        }
        executorService.submit(() -> {
            for (Object message : messageList) {
                parseMessage(message);
            }
        });
    }


    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

    }

}
