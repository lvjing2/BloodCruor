package com.liwn.zzl.bloodcruor.deviceConns;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zzl on 17/7/12.
 */

public class Constants {
    public static final int packageAllLen = 20;
    public static final int packageDataLen = 16;

    public static final Map<Integer, Integer> packageCntMap = new HashMap<>();
    static {
        packageCntMap.put(17, 3);
        packageCntMap.put(18, 4);
        packageCntMap.put(19, 4);
        packageCntMap.put(20, 4);
        packageCntMap.put(21, 4);
        packageCntMap.put(22, 28);
        packageCntMap.put(23, 16);
        packageCntMap.put(37, 1);       // 4 授权指令
        packageCntMap.put(83, 1);       // 3.2 切换设备屏
        packageCntMap.put(84, 1);       // 3.3 获取信息
    }

    public static byte calculateCheckSum(byte[] data) {
        int cnt = 0;
        for (int i = 0; i < (data.length - 1); ++i) {
            cnt += data[i];
        }

        byte res = (byte)(cnt & 0xff);
        return res;
    }

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    @NonNull
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x ", b & 0xff));
        }
        return sb.toString().toUpperCase();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
