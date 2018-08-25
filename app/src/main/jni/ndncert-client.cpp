//
// Created by peter on 8/7/18.
//
#include <android/log.h>

#include "ndncert-client.hpp"

#include "client-module.hpp"
#include "challenge-module.hpp"

#include <iostream>
#include <string>
#include <boost/filesystem.hpp>
#include <ndn-cxx/security/verification-helpers.hpp>

using namespace ndn;
using namespace ndn::ndncert;

static ClientModule* mClient;
static shared_ptr<RequestState> mState;

void errorCb(const std::string& errorInfo){
     __android_log_print(
        ANDROID_LOG_ERROR,
        "ndncert-client.cpp: ",
        "%s", errorInfo.c_str());
}
void newCb(const shared_ptr<RequestState>& requestState, JNIEnv* env, jobject obj);
void selectCb(const shared_ptr<RequestState>& requestState, JNIEnv* env, jobject obj);
void validateCb(const shared_ptr<RequestState>& requestState, JNIEnv* env, jobject obj);
void downloadCb(const shared_ptr<RequestState>& requestState, JNIEnv* env, jobject obj);

std::list<std::string> jStrArr2CppStrList(JNIEnv* env, jobjectArray arr){
    std::list<std::string> cppStrList;
    int strCnt = env->GetArrayLength(arr);
    for (int i=0; i<strCnt; i++){
        jstring tmpJStr = (jstring) env->GetObjectArrayElement(arr, i);
        cppStrList.push_back(std::string(env->GetStringUTFChars(tmpJStr, nullptr)));
    }
    return cppStrList;
}

jobjectArray cppStrList2JStrArr(JNIEnv* env, const std::list<std::string>& l){
    jclass strClass = env->FindClass("java/lang/String");
    jobjectArray jStrArr = env->NewObjectArray(l.size(), strClass, nullptr);
    int i=0;
    for (std::string s: l){
        jstring tmp = env->NewStringUTF(s.c_str());
        env->SetObjectArrayElement(jStrArr, i, tmp);
        i++;
    }
    return jStrArr;
}

void callJavaFunction(
  JNIEnv* env, jobject obj,
  const char* funcName, const char* callbackFuncName,
  std::list<std::string>& textList,
  std::list<std::string>& hintList){
    // Prepare all the args.
    jobjectArray textJArr = cppStrList2JStrArr(env, textList);
    jobjectArray hintJArr = cppStrList2JStrArr(env, hintList);
    jclass cls = env->FindClass("com/ndn/jwtan/identitymanager/NdncertClient");
    jfieldID cbField = env->GetFieldID(
        cls, callbackFuncName,
        "Lcom/ndn/jwtan/identitymanager/NdncertClient$Callback;");
    jobject cb = env->GetObjectField(obj, cbField);

    // Call next function.
    jmethodID promptInputDialog = env->GetMethodID(
        cls, funcName,
        "([Ljava/lang/String;[Ljava/lang/String;Lcom/ndn/jwtan/identitymanager/NdncertClient$Callback;)V");
    env->CallVoidMethod(obj, promptInputDialog, textJArr, hintJArr, cb);
}
void callJavaFunction(
  JNIEnv* env, jobject obj,
  const char* callbackFuncName,
  std::list<std::string>& textList,
  std::list<std::string>& hintList){
  callJavaFunction(env, obj, "promptInputDialog", callbackFuncName, textList, hintList);
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
    boost::filesystem::path path = boost::filesystem::current_path();
    __android_log_print(
            ANDROID_LOG_ERROR,
            "ndncert-client.cpp: ",
            "%s", path.string().c_str());
    // Face face;
    //security::v2::KeyChain keyChain;
    // mClient = new ClientModule(face, keyChain);
    // TODO: Give a clear filepath, I actually wanted a hardcoded one.
    // mClient->getClientConf().load("FilePath");
}

/*
 * Class:     com_ndn_jwtan_identitymanager_NdncertClient
 * Method:    cppSendNew
 * Signature: ([Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_ndn_jwtan_identitymanager_NdncertClient_cppSendNew
  (JNIEnv * env, jobject obj, jobjectArray arr){
    // Get input in the form of List<String>
    auto paramList = jStrArr2CppStrList(env, arr);

    ClientCaItem clientCaItem;
    Name identityName = clientCaItem.m_caName.getPrefix(-1);
    identityName.append(paramList.front());
    /*mClient->sendNew(
        clientCaItem, identityName,
        std::bind(&newCb, _1, env, obj),
        errorCb);*/
    // TODO: fake server below.
    mState = make_shared<RequestState>();
    mState->m_challengeList = std::list<std::string>({"Email", "PIN", "SMS"});
    mState->m_status = "Select";
    newCb(mState, env, obj);
}
void newCb(
  const shared_ptr<RequestState>& requestState,
  JNIEnv* env, jobject obj){
    mState = requestState;
    std::list<std::string> textList({"Please select one challenge from following types"});
    std::list<std::string>& hintList = requestState->m_challengeList;
    // Call next function.
    callJavaFunction(env, obj, "promptSelectDialog", "selectChallenge", textList, hintList);
}

static std::string choice;
/*
 * Class:     com_ndn_jwtan_identitymanager_NdncertClient
 * Method:    cppSelectChallenge
 * Signature: ([Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_ndn_jwtan_identitymanager_NdncertClient_cppSelectChallenge
  (JNIEnv * env, jobject obj, jobjectArray arr){
    // Get input in the form of List<String>
    auto paramList = jStrArr2CppStrList(env, arr);
    choice = paramList.front();
    auto challenge = ChallengeModule::createChallengeModule(choice);
    // TODO: ask for more info, for example: email address.
    auto requirementList = challenge->getRequirementForSelect();

    std::list<std::string> textList = requirementList;
    std::list<std::string> hintList({"PeterRong96@gmail.com"});
    callJavaFunction(env, obj, "sendSelect", textList, hintList);
}

/*
 * Class:     com_ndn_jwtan_identitymanager_NdncertClient
 * Method:    cppSendSelect
 * Signature: ([Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_ndn_jwtan_identitymanager_NdncertClient_cppSendSelect
  (JNIEnv * env, jobject obj, jobjectArray arr){
    auto paramList = jStrArr2CppStrList(env, arr);
    auto challenge = ChallengeModule::createChallengeModule(choice);
    auto paramJson = challenge->genSelectParamsJson(mState->m_status, paramList);
    /*client->sendSelect(
        mState, paramList.front(), paramJson,
        std::bind(&selectCb, _1, env, obj),
        errorCb);*/
    // TODO: fake server below.
    mState->m_challengeType = choice;
    mState->m_status = "need-code";
    selectCb(mState, env, obj);
}

/*
 * Class:     com_ndn_jwtan_identitymanager_NdncertClient
 * Method:    selectChallenge
 * Signature: ([Ljava/lang/String;)V
 */
/*JNIEXPORT void JNICALL Java_com_ndn_jwtan_identitymanager_NdncertClient_selectChallenge
  (JNIEnv * env, jobject obj, jobjectArray arr){
    // Get input in the form of List<String>
    auto paramList = jStrArr2CppStrList(env, arr);
    auto challenge = ChallengeModule::createChallengeModule(paramList.front());
    // TODO: ask for more info, for example: email address.
    auto paramJson = challenge->genSelectParamsJson(mState->m_status, paramList);
    client->sendSelect(
        mState, paramList.front(), paramJson,
        std::bind(&selectCb, _1, env, obj),
        errorCb);
    // TODO: fake server below.
    mState->m_challengeType = "Email";
    mState->m_status = "need-code";
    selectCb(mState, env, obj);
}*/
void selectCb(
  const shared_ptr<RequestState>& requestState,
  JNIEnv* env, jobject obj){
    mState = requestState;

    auto challenge = ChallengeModule::createChallengeModule(mState->m_challengeType);
    auto requirementList = challenge->getRequirementForValidate(mState->m_status);

    auto textList = requirementList;
    auto hintList = std::list<std::string>({"961030"});

    // Call next function.
    callJavaFunction(env, obj, "sendValidate", textList, hintList);
}

/*
 * Class:     com_ndn_jwtan_identitymanager_NdncertClient
 * Method:    cppSendValidate
 * Signature: ([Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_ndn_jwtan_identitymanager_NdncertClient_cppSendValidate
  (JNIEnv * env, jobject obj, jobjectArray arr){
    // Get input in the form of List<String>
    auto paramList = jStrArr2CppStrList(env, arr);
    auto challenge = ChallengeModule::createChallengeModule(mState->m_challengeType);
    auto paramJson = challenge->genValidateParamsJson(mState->m_status, paramList);
    /*client->sendValidate(
        mState, paramJson,
        std::bind(validateCb&, _1, env, obj),
        errorCb);*/
    // TODO: fake server below.
    if (paramList.front() == "961030"){
        mState->m_status = ChallengeModule::SUCCESS;
    } else {
        mState->m_status = ChallengeModule::FAILURE;
    }
    validateCb(mState, env, obj);
}
void validateCb(
  const shared_ptr<RequestState>& requestState,
  JNIEnv* env, jobject obj){
    mState = requestState;
    if (mState->m_status == ChallengeModule::SUCCESS) {
        __android_log_print(
            ANDROID_LOG_ERROR,
            "ndncert-client.cpp: ",
            "DONE! Certificate has already been issued \n");
        /*client->requestDownload(mState,
                             bind(downloadCb, _1, env, obj),
                             errorCb);*/
        return;
    } else {
        auto challenge = ChallengeModule::createChallengeModule(mState->m_challengeType);
        auto requirementList = challenge->getRequirementForValidate(mState->m_status);

        auto textList = requirementList;
        auto hintList = std::list<std::string>({"961030"});

        // Call next function.
        callJavaFunction(env, obj, "sendValidate", textList, hintList);
    }
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