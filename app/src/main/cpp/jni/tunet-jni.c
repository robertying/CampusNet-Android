#include <jni.h>
#include <string.h>

#include "../tunet-c/src/tunet.h"

char *CA_BUNDLE_PATH;

JNIEXPORT void JNICALL Java_io_robertying_campusnet_TunetHelper_tunetInit(JNIEnv *env,
                                                                          jobject this,
                                                                          jstring path)
{
    tunet_init();
    CA_BUNDLE_PATH = (*env)->GetStringUTFChars(env, path, NULL);
}

JNIEXPORT void JNICALL Java_io_robertying_campusnet_TunetHelper_tunetCleanup(JNIEnv *env,
                                                                             jobject this)
{
    tunet_cleanup();
}

static jobject enum_c_to_java(JNIEnv *env, res c_enum)
{
    jclass response_type = (*env)->FindClass(env, "io/robertying/campusnet/TunetHelper$ResponseType");
    jfieldID response_field_id = (*env)->GetStaticFieldID(env, response_type, res_string[c_enum], "Lio/robertying/campusnet/TunetHelper$ResponseType;");
    return (*env)->GetStaticObjectField(env, response_type, response_field_id);
}

JNIEXPORT jobject JNICALL Java_io_robertying_campusnet_TunetHelper_netLogin(JNIEnv *env,
                                                                            jobject this,
                                                                            jstring username,
                                                                            jstring password)
{
    const char *c_username = (*env)->GetStringUTFChars(env, username, NULL);
    const char *c_password = (*env)->GetStringUTFChars(env, password, NULL);

    res response = net_login(c_username, c_password);
    return enum_c_to_java(env, response);
}

JNIEXPORT jobject JNICALL Java_io_robertying_campusnet_TunetHelper_auth4Login(JNIEnv *env,
                                                                              jobject this,
                                                                              jstring username,
                                                                              jstring password)
{
    const char *c_username = (*env)->GetStringUTFChars(env, username, NULL);
    const char *c_password = (*env)->GetStringUTFChars(env, password, NULL);

    res response = auth4_login(c_username, c_password);
    return enum_c_to_java(env, response);
}

JNIEXPORT jobject JNICALL Java_io_robertying_campusnet_TunetHelper_auth6Login(JNIEnv *env,
                                                                              jobject this,
                                                                              jstring username,
                                                                              jstring password)
{
    const char *c_username = (*env)->GetStringUTFChars(env, username, NULL);
    const char *c_password = (*env)->GetStringUTFChars(env, password, NULL);

    res response = auth6_login(c_username, c_password);
    return enum_c_to_java(env, response);
}

JNIEXPORT jobject JNICALL Java_io_robertying_campusnet_TunetHelper_netLogout(JNIEnv *env,
                                                                             jobject this)
{
    res response = net_logout();
    return enum_c_to_java(env, response);
}

JNIEXPORT jobject JNICALL Java_io_robertying_campusnet_TunetHelper_auth4Logout(JNIEnv *env,
                                                                               jobject this)
{
    res response = auth4_logout();
    return enum_c_to_java(env, response);
}

JNIEXPORT jobject JNICALL Java_io_robertying_campusnet_TunetHelper_auth6Logout(JNIEnv *env,
                                                                               jobject this)
{
    res response = auth6_logout();
    return enum_c_to_java(env, response);
}
