//
// Created by Boris Denisenko on 2019-10-09.
//
#include <jni.h>
#include <android/log.h>
#include "image_platform_converter.hpp"

#define LOG_TAG "JNI_Core_Wrapper"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

extern "C" {

static std::vector<uint32_t> ms_resizeOffsets;

JNIEXPORT void JNICALL
Java_com_bendenen_visionai_utils_NativeImageUtilsWrapper_resizeImage(JNIEnv *env,
                                                                     jobject /* this */,
                                                                     jbyteArray rgbaByteArray,
                                                                     jint imgWidth,
                                                                     jint imgHeight,
                                                                     jint newWidth,
                                                                     jint newHeight,
                                                                     jbyteArray outArray) {
    jbyte *bytePointer = env->GetByteArrayElements(rgbaByteArray, 0);

    vsa::Point2U newSize(static_cast<uint32_t>(newWidth), static_cast<uint32_t>(newHeight));

    vsa::ImagePtr img(new vsa::Image());
    img->Set(static_cast<uint32_t>(imgWidth), static_cast<uint32_t>(imgHeight),
             vsa::ImageFormat::RGBA,
             (uint8_t *) (bytePointer));


    // update resize offsets
    if (ms_resizeOffsets.size() != newSize.x * newSize.y)
        ms_resizeOffsets = vsa::utils::CalcResizeOffsets(img->GetChannelsNum(), img->Size(),
                                                         newSize);

    jbyte *outData = env->GetByteArrayElements(outArray, nullptr);
    vsa::utils::ResizeImage<vsa::ImageFormat::RGB>(img, (uint8_t *) outData, newSize,
                                                   ms_resizeOffsets);

    env->ReleaseByteArrayElements(outArray, outData, JNI_ABORT);
    env->ReleaseByteArrayElements(rgbaByteArray, bytePointer, JNI_ABORT);

}


}