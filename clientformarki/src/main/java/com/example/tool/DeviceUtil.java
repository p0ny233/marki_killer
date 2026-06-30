package com.example.tool;

import android.media.MediaDrm;
import android.os.Build;
import android.util.Base64;

import java.util.UUID;

public class DeviceUtil {

    // Widevine DRM 的固定 UUID
    private static final UUID WIDEVINE_UUID =
            new UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L);

    /**
     * 获取一个持久化的设备唯一ID (应用卸载后保持不变)
     * @return 经过 Base64 编码的唯一ID字符串，失败时返回 null
     */
    public static String getPersistentDeviceId() {
        // MediaDrm API 从 Android 4.3 (API 18) 开始引入
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return null;
        }

        MediaDrm mediaDrm = null;
        try {
            // 1. 创建 MediaDrm 实例
            mediaDrm = new MediaDrm(WIDEVINE_UUID);

            // 2. 获取硬件级别的唯一ID
            byte[] uniqueId = mediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID);

            if (uniqueId == null) {
                return null;
            }

            // 3. 将字节数组编码为字符串返回 (这里使用 Base64)
            return Base64.encodeToString(uniqueId, Base64.NO_WRAP);

        } catch (Exception e) {
            // 捕获并打印异常，例如设备不支持 Widevine DRM
            e.printStackTrace();
            return null;
        } finally {
            // 4. 正确释放资源
            if (mediaDrm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    mediaDrm.close();
                } else {
                    mediaDrm.release();
                }
            }
        }
    }
}