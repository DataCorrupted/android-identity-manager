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