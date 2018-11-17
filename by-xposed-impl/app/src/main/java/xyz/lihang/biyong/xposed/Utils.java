package xyz.lihang.biyong.xposed;

import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import de.robv.android.xposed.XposedBridge;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author : lihang1329@gmail.com
 * @since : 2018/10/1
 */
public class Utils {

    public static String messageToJSON(Object o) {
        String json = "JSON ERROR";
        if (o == null) {
            return "null";
        }
        try {
            json = JSON.toJSONString(o, new SimplePropertyPreFilter(o.getClass(),
                    "type",
                    "messageOwner",
                    "contentType",
                    "caption",
                    "messageText"
            ), SerializerFeature.PrettyFormat);
        } catch (Exception e) {
            error(e);
        }
        return json;
    }

    public static void log(String log) {
        //Log.d("[li_test]", log);
        XposedBridge.log("[li_test]:"+ log);
    }

    public static void error(String log) {
        log("[error]" + log);
    }

    public static void debug(String log) {
        log("[debug]" + log);
    }

    public static void error(Throwable throwable) {
        try (StringWriter stringWriter = new StringWriter(); PrintWriter printWriter = new PrintWriter(stringWriter)) {
            error(throwable.getMessage());
            throwable.printStackTrace(printWriter);
            error("\n" + stringWriter.toString());
        } catch (IOException e) {

        }
    }

}
