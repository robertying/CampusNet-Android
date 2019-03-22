LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := tidy
LOCAL_SRC_FILES := ../../../../../htmltidy/.externalNativeBuild/cmake/$(APP_OPTIM)/$(TARGET_ARCH_ABI)/libtidys.a
LOCAL_EXPORT_CFLAGS := -I$(LOCAL_PATH)/../tunet-c/tidy-html5/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := ssl
LOCAL_SRC_FILES := ../openssl-curl-android/build/openssl/$(TARGET_ARCH_ABI)/lib/libssl.a
LOCAL_EXPORT_CFLAGS := -I$(LOCAL_PATH)/../openssl-curl-android/build/openssl/$(TARGET_ARCH_ABI)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := crypto
LOCAL_SRC_FILES := ../openssl-curl-android/build/openssl/$(TARGET_ARCH_ABI)/lib/libcrypto.a
LOCAL_EXPORT_CFLAGS := -I$(LOCAL_PATH)/../openssl-curl-android/build/openssl/$(TARGET_ARCH_ABI)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := curl
LOCAL_SRC_FILES := ../openssl-curl-android/build/curl/$(TARGET_ARCH_ABI)/lib/libcurl.a
LOCAL_EXPORT_CFLAGS := -I$(LOCAL_PATH)/../openssl-curl-android/build/curl/$(TARGET_ARCH_ABI)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../openssl/include $(LOCAL_PATH)/../curl/include $(LOCAL_PATH)/../tunet-c/cJSON $(LOCAL_PATH)/../tunet-c/sds $(LOCAL_PATH)/../tunet-c/utf8proc -I$(LOCAL_PATH)/../tunet-c/tidy-html5/include
LOCAL_MODULE := tunet
LOCAL_SRC_FILES := tunet-jni.c ../tunet-c/src/tunet.c ../tunet-c/lib/portal.c ../tunet-c/lib/parser.c ../tunet-c/lib/list.c ../tunet-c/lib/utf.c ../tunet-c/utf8proc/utf8proc.c ../tunet-c/sds/sds.c ../tunet-c/cJSON/cJSON.c
LOCAL_STATIC_LIBRARIES := libcurl libssl libcrypto libtidy
include $(BUILD_SHARED_LIBRARY)
