//
// Created by peter on 8/7/18.
//
#include "com_ndn_jwtan_identitymanager_NdncertClient.h"

// TODO: These includes are just a demo that jni, as included by Alex, works.
#include <boost/property_tree/info_parser.hpp>
#include <boost/thread.hpp>
#include <mutex>

JNIEXPORT jstring JNICALL Java_com_ndn_jwtan_identitymanager_NdncertClient_init
        (JNIEnv * env, jclass obj){
    return env->NewStringUTF("JNI init succeeded");
}

// TODO: We can treat this as a member function of ClientTool and put env as a member.
std::string getUserInputLine(JNIEnv * env, std::string info){

    jstring info_jstr = env->NewStringUTF(info.c_str());

    // TODO: UserInput not existed yet.
    jclass clazz = env->FindClass("com/ndn/jwtan/identitymanager/UserInput");

    jmethodID getLine = env->GetMethodID(clazz, "getLine", "()Lcom/ndn/jwtan/identitymanager/UserInput;");

    jobject result = env->CallObjectMethod(info_jstr, getLine);

    const char* str = env->GetStringUTFChars((jstring) result, nullptr);

    return str;
}