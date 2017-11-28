package com.pixel.adi;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Created by Administrator on 2017/11/28 0028.
 * <p>
 * 获取设备唯一编号 直接将该对象放入到自己的项目中即可使用
 */

public class AndroidDevice {
    private static volatile AndroidDevice ad;
    private static volatile String deviceId;

    public synchronized static AndroidDevice getAndroidDevice(Context context) {
        if (ad == null || isNull(deviceId)) {
            ad = new AndroidDevice(context);
        }
        return ad;
    }

    private static String getMd5(String string) {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5"); //获取摘要器 MessageDigest
            byte[] digest = messageDigest.digest(string.getBytes());  //通过摘要器对字符串的二进制字节数组进行hash计算
            for (int i = 0; i < digest.length; i++) {   //循环每个字符 将计算结果转化为正整数;
                int digestInt = digest[i] & 0xff;   //将10进制转化为较短的16进制
                String hexString = Integer.toHexString(digestInt);  //转化结果如果是个位数会省略0,因此判断并补0
                if (hexString.length() < 2) {
                    sb.append(0);
                }
                sb.append(hexString);   //将循环结果添加到缓冲区
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sb.toString();   // 返回整个结果
    }

    // 检查权限
    private static boolean checkPerm(Context context, String perm) {
        if (ActivityCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    // 非空检查
    private static boolean isNull(String value) {
        if (value == null || value.length() <= 0 || "null".equalsIgnoreCase(value)) {
            return true;
        }
        return false;
    }

    //设置保存的参数
    private void setString(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(AndroidDevice.class.getSimpleName() + "ad.db", Context.MODE_PRIVATE);
        sp.edit().putString(key, value).commit();
    }

    //获取保存的参数
    private String getString(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(AndroidDevice.class.getSimpleName() + "ad.db", Context.MODE_PRIVATE);
        return sp.getString(key, null);
    }

    // 获取缓存ID
    private String getDeviceUUID(Context context) {
        String uuid = getString(context, AndroidDevice.class.getSimpleName() + "key");
        if (isNull(uuid)) {
            uuid = UUID.randomUUID().toString();
            setString(context, AndroidDevice.class.getSimpleName() + "key", uuid);
        }
        return uuid;
    }

    private AndroidDevice(Context context) {
        deviceId = getString(context, AndroidDevice.class.getSimpleName() + "DID");
        if (isNull(deviceId)) {
            StringBuilder sb = new StringBuilder("");
            try {
                String serialNumber = null; // 获取序列号
                if (checkPerm(context, Manifest.permission.READ_PHONE_STATE) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    serialNumber = Build.getSerial();
                } else {
                    serialNumber = Build.SERIAL;
                }
                if (!isNull(serialNumber)) {
                    sb.append(serialNumber);
                }

                String androidId = Settings.System.getString(  // Android初始化时生成的ID 恢复出厂设置会被重置
                        context.getContentResolver(), Settings.Secure.ANDROID_ID);
                if (!isNull(androidId)) {
                    sb.append(androidId);
                }

                String imei = null;
                if (checkPerm(context, Manifest.permission.READ_PHONE_STATE)) {
                    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    imei = tm.getDeviceId();   // 需要权限 android.permission.READ_PHONE_STATE
                }
                if (!isNull(imei)) {
                    sb.append(imei);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 最后结果值
            if (isNull(sb.toString())) {
                sb.append(getDeviceUUID(context));  // 自己生成一个 APP被卸载就会重置
            }
            // 转为MD5
            deviceId = getMd5(sb.toString());
            // 缓存
            setString(context, AndroidDevice.class.getSimpleName() + "DID", deviceId);
        }
    }

    public String getDeviceId() {
        return deviceId;
    }

}
