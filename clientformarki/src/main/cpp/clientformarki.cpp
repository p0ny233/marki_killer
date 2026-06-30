#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>
#include <media/NdkMediaDrm.h>
#include <media/NdkMediaError.h>
#include <openssl/bio.h>
#include <openssl/evp.h>
#include <openssl/buffer.h>
#include <memory>

#define LOG_TAG "DeviceIdNative"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

std::string base64_encode(const unsigned char* buffer, size_t length) {
    BIO *bio, *b64;
    BUF_MEM *bufferPtr;

    // 1. 创建Base64过滤器BIO和内存BIO
    b64 = BIO_new(BIO_f_base64());
    bio = BIO_new(BIO_s_mem());

    // 2. 将两者连接成一个BIO链: b64 -> bio
    bio = BIO_push(b64, bio);

    // 3. (可选) 设置标志，避免输出中的换行符
    //    如果希望Base64结果不换行，就取消下面这行注释
    BIO_set_flags(bio, BIO_FLAGS_BASE64_NO_NL);

    // 4. 写入待编码的数据
    BIO_write(bio, buffer, length);

    // 5. 刷新BIO，完成编码
    BIO_flush(bio);

    // 6. 从内存BIO中获取结果
    BIO_get_mem_ptr(bio, &bufferPtr);

    // 7. 将结果拷贝到std::string中
    std::string result(bufferPtr->data, bufferPtr->length);

    // 8. 释放所有BIO资源
    BIO_free_all(bio);

    return result;
}


std::string getPersistentDeviceIdNative() {
    const uint8_t uuid[] = {0xed,0xef,0x8b,0xa9,0x79,0xd6,0x4a,0xce,
                            0xa3,0xc8,0x27,0xdc,0xd5,0x1d,0x21,0xed
    };
    AMediaDrm *mediaDrm = AMediaDrm_createByUUID(uuid);
    /*
     * 不能过早调用AMediaDrm_createByUUID ，所以只能用户点击按钮后才能执行该函数
     */

    // 获取 deviceUniqueId
    AMediaDrmByteArray aMediaDrmByteArray;
    AMediaDrm_getPropertyByteArray(mediaDrm,PROPERTY_DEVICE_UNIQUE_ID, &aMediaDrmByteArray);
    std::string result = base64_encode((uint8_t *)aMediaDrmByteArray.ptr,aMediaDrmByteArray.length);
    LOGD("result-> %s", result.c_str());

    return result;
}




