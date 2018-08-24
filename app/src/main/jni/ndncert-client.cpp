//
// Created by peter on 8/7/18.
//
#include <android/log.h>

#include "ndncert-client.hpp"

#include "client-module.hpp"
#include "challenge-module.hpp"

#include <iostream>
#include <string>

#include <boost/program_options/options_description.hpp>
#include <boost/program_options/variables_map.hpp>
#include <boost/program_options/parsers.hpp>
#include <ndn-cxx/security/verification-helpers.hpp>

static ndn::ndncert::ClientModule* client;

namespace ndn {
namespace ndncert {

void startNdncertClient(){
    // Face face;
    // security::v2::KeyChain keyChain;
    // client = new ClientModule(face, keyChain);
    // TODO: Give a clear filepath, I actually wanted a hardcoded one.
    // client->getClientConf().load("FilePath");
}

} // namespace ndncert
} // namespace ndn

std::vector<std::string> jStrArr2CppStrVec(JNIEnv* env, jobjectArray arr){
    std::vector<std::string> cppStrVec;
    int strCnt = env->GetArrayLength(arr);
    for (int i=0; i<strCnt; i++){
        jstring tmpJStr = (jstring) env->GetObjectArrayElement(arr, i);
        cppStrVec.push_back(std::string(env->GetStringUTFChars(tmpJStr, nullptr)));
    }
    return cppStrVec;
}

jobjectArray cppStrVec2JStrArr(JNIEnv* env, const std::vector<std::string>& vec){
    jclass strClass = env->FindClass("java/lang/String");
    jobjectArray jStrArr = env->NewObjectArray(vec.size(), strClass, nullptr);
    for (int i=0; i<vec.size(); i++){
        jstring tmp = env->NewStringUTF(vec[i].c_str());
        env->SetObjectArrayElement(jStrArr, i, tmp);
    }
    return jStrArr;
}

void callNextFunction(
  JNIEnv* env, jobject obj,
  const char* callbackFuncName,
  std::vector<std::string>& textVec,
  std::vector<std::string>& hintVec){
    // Prepare all the args.
    jobjectArray textJArr = cppStrVec2JStrArr(env, textVec);
    jobjectArray hintJArr = cppStrVec2JStrArr(env, hintVec);
    jclass cls = env->FindClass("com/ndn/jwtan/identitymanager/NdncertClient");
    jfieldID cbField = env->GetFieldID(
        cls, callbackFuncName,
        "Lcom/ndn/jwtan/identitymanager/NdncertClient$Callback;");
    jobject cb = env->GetObjectField(obj, cbField);

    // Call next function.
    jmethodID promptInputDialog = env->GetMethodID(
        cls, "promptInputDialog",
        "([Ljava/lang/String;[Ljava/lang/String;Lcom/ndn/jwtan/identitymanager/NdncertClient$Callback;)V");
    env->CallVoidMethod(obj, promptInputDialog, textJArr, hintJArr, cb);
}

/*
 * Class:     com_ndn_jwtan_identitymanager_NdncertClient
 * Method:    init
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_ndn_jwtan_identitymanager_NdncertClient_init
  (JNIEnv * env, jclass cls){
    return env->NewStringUTF("JNI init succeeded");
}

/*
 * Class:     com_ndn_jwtan_identitymanager_NdncertClient
 * Method:    startNdncertClient
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_ndn_jwtan_identitymanager_NdncertClient_startNdncertClient
  (JNIEnv * env, jobject obj){
    ndn::ndncert::startNdncertClient();
}

/*
 * Class:     com_ndn_jwtan_identitymanager_NdncertClient
 * Method:    cppSendNew
 * Signature: ([Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_ndn_jwtan_identitymanager_NdncertClient_cppSendNew
  (JNIEnv * env, jobject obj, jobjectArray arr){
    // Get input in the form of Vec<String>
    auto strVec = jStrArr2CppStrVec(env, arr);

    // TODO: Do something. Send out message using NDN

    // Get info that are required, put them in vec.
    auto textVec = std::vector<std::string>();
    auto hintVec = std::vector<std::string>();
    textVec = strVec;
    hintVec = strVec;

    // Call next function.
    // TODO: you can use a checkbox to sendSelect.
    callNextFunction(env, obj, "sendSelect", textVec, hintVec);
}

/*
 * Class:     com_ndn_jwtan_identitymanager_NdncertClient
 * Method:    cppSendSelect
 * Signature: ([Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_ndn_jwtan_identitymanager_NdncertClient_cppSendSelect
  (JNIEnv * env, jobject obj, jobjectArray arr){
    // Get input in the form of Vec<String>
    auto strVec = jStrArr2CppStrVec(env, arr);

    // TODO: Do something. Send out message using NDN

    // Get info that are required, put them in vec.
    auto textVec = std::vector<std::string>();
    auto hintVec = std::vector<std::string>();
    textVec = strVec;
    hintVec = strVec;

    // Call next function.
    callNextFunction(env, obj, "sendValidate", textVec, hintVec);
}

/*
 * Class:     com_ndn_jwtan_identitymanager_NdncertClient
 * Method:    cppSendValidate
 * Signature: ([Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_ndn_jwtan_identitymanager_NdncertClient_cppSendValidate
  (JNIEnv * env, jobject obj, jobjectArray arr){
    // Get input in the form of Vec<String>
    auto strVec = jStrArr2CppStrVec(env, arr);

    // TODO: Do something. Send out message using NDN

    // Get info that are required, put them in vec.
    auto textVec = std::vector<std::string>();
    auto hintVec = std::vector<std::string>();
    textVec = strVec;
    hintVec = strVec;

    // Call next function.
    callNextFunction(env, obj, "download", textVec, hintVec);
}

/*
 * Class:     com_ndn_jwtan_identitymanager_NdncertClient
 * Method:    cppDownload
 * Signature: ([Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_ndn_jwtan_identitymanager_NdncertClient_cppDownload
  (JNIEnv * env, jobject obj, jobjectArray arr){
    // TODO: Create a TextView to inform that the idendity has been created.
    __android_log_print(ANDROID_LOG_ERROR, "TRACKERS", "%s", "Succeeded.");
}