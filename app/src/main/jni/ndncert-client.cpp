//
// Created by peter on 8/7/18.
//

#include "com_ndn_jwtan_identitymanager_NdncertClient.h"

JNIEXPORT jstring JNICALL Java_com_ndn_jwtan_identitymanager_NdncertClient_helloWorld
        (JNIEnv * env, jclass obj){
    return env->NewStringUTF("Hello World.");
}