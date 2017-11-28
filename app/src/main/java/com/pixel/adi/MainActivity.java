package com.pixel.adi;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 获取Android设备唯一标识
 * <p>
 * http://blog.csdn.net/sunsteam/article/details/73189268
 */
public class MainActivity extends AppCompatActivity {
    private TextView mResultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mResultText = (TextView) findViewById(R.id.resultText);
    }

    public void getPerm(View view) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 111);
    }

    public void getImei(View view) {    // 获取IMEI 或者华为 MEID
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();   // 需要权限 android.permission.READ_PHONE_STATE
        mResultText.setText(imei);
    }

    public void getAndroidId(View view) {
        String ANDROID_ID = Settings.System.getString(getContentResolver(), Settings.System.ANDROID_ID);
        mResultText.setText(ANDROID_ID);
    }

    public void getSn(View view) {
        String SerialNumber = Build.SERIAL; // 获取序列号
        mResultText.setText(SerialNumber);
    }

    public void getSum(View view) {
        mResultText.setText(AndroidDevice.getAndroidDevice(this).getDeviceId());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Toast.makeText(this, "" + grantResults[0], Toast.LENGTH_LONG).show();
    }

    private static String toMD5(String text) {
        StringBuilder sb = new StringBuilder();
        try {
            //获取摘要器 MessageDigest
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            //通过摘要器对字符串的二进制字节数组进行hash计算
            byte[] digest = messageDigest.digest(text.getBytes());
            for (int i = 0; i < digest.length; i++) {
                //循环每个字符 将计算结果转化为正整数;
                int digestInt = digest[i] & 0xff;
                //将10进制转化为较短的16进制
                String hexString = Integer.toHexString(digestInt);
                //转化结果如果是个位数会省略0,因此判断并补0
                if (hexString.length() < 2) {
                    sb.append(0);
                }
                //将循环结果添加到缓冲区
                sb.append(hexString);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        //返回整个结果
        return sb.toString();
    }

}
