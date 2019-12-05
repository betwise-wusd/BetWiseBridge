package com.betwise.betwisebridge.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Date: 2018/10/17
 */
public class JSONUtils {
    public static String buildResult(String name, String value) {
        JSONObject json = new JSONObject();
        try {
            json.put(name, value);
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String buildWalletPluginJsonResult(int errCode, String msg, JSONObject result){
        JSONObject json = new JSONObject();
        try {
            json.put("errorCode", errCode);
            json.put("errorMsg", msg);
            json.put("result", result);
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String buildWalletPluginStringResult(int errCode, String msg, String result){
        JSONObject json = new JSONObject();
        try {
            json.put("errorCode", errCode );
            json.put("errorMsg", msg);
            json.put("result", result);
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
}
