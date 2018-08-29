//
// Created by peter on 8/7/18.
//
#include <android/log.h>

#include "ndncert-client.hpp"

#include "client-module.hpp"
#include "challenge-module.hpp"
#include "config.cpp"

#include <iostream>
#include <string>
#include <boost/filesystem.hpp>
#include <ndn-cxx/security/verification-helpers.hpp>

#ifndef NDNCERT_CLIENT_TAG
#define NDNCERT_CLIENT_TAG "nencert-client.cpp: "
#endif

using namespace ndn;
using namespace ndn::ndncert;

static ClientModule* mClient;
static shared_ptr<RequestState> mState;
static Face* mFace;
static security::v2::KeyChain* mKeyChain;


void callJavaTextDialog(
  JNIEnv* env, jobject obj,
  std::string title, std::string text){
     jstring jTitle = env->NewStringUTF(title.c_str());
     jstring jText = env->NewStringUTF(text.c_str());
     jclass cls = env->FindClass("com/ndn/jwtan/identitymanager/NdncertClient");
     jmethodID promptTextDialog = env->GetMethodID(
         cls, "promptTextDialog", "(Ljava/lang/String;Ljava/lang/String;)V");
     env->CallVoidMethod(obj, promptTextDialog, jTitle, jText);
}

void errorCb(const std::string& errorInfo, JNIEnv* env, jobject obj){
    __android_log_print(
        ANDROID_LOG_ERROR,
        NDNCERT_CLIENT_TAG,
        "%s", errorInfo.c_str());
    callJavaTextDialog(env, obj, "Error!", errorInfo);
}
void newCb(const shared_ptr<RequestState>& requestState, JNIEnv* env, jobject obj);
void selectCb(const shared_ptr<RequestState>& requestState, JNIEnv* env, jobject obj);
void validateCb(const shared_ptr<RequestState>& requestState, JNIEnv* env, jobject obj);
void downloadCb(const shared_ptr<RequestState>& requestState, JNIEnv* env, jobject obj);

// Helper function copied from Alex.
// Turns a java Map<String, String>
// to cpp std::map<std::string, std::string>
std::map<std::string, std::string>
getParams(JNIEnv* env, jobject jParams){
  std::map<std::string, std::string> params;

    jclass jcMap = env->GetObjectClass(jParams);
    jclass jcSet = env->FindClass("java/util/Set");
    jclass jcIterator = env->FindClass("java/util/Iterator");
    jclass jcMapEntry = env->FindClass("java/util/Map$Entry");

    jmethodID jcMapEntrySet      = env->GetMethodID(jcMap,      "entrySet", "()Ljava/util/Set;");
    jmethodID jcSetIterator      = env->GetMethodID(jcSet,      "iterator", "()Ljava/util/Iterator;");
    jmethodID jcIteratorHasNext  = env->GetMethodID(jcIterator, "hasNext",  "()Z");
    jmethodID jcIteratorNext     = env->GetMethodID(jcIterator, "next",     "()Ljava/lang/Object;");
    jmethodID jcMapEntryGetKey   = env->GetMethodID(jcMapEntry, "getKey",   "()Ljava/lang/Object;");
    jmethodID jcMapEntryGetValue = env->GetMethodID(jcMapEntry, "getValue", "()Ljava/lang/Object;");

    jobject jParamsEntrySet = env->CallObjectMethod(jParams, jcMapEntrySet);
    jobject jParamsIterator = env->CallObjectMethod(jParamsEntrySet, jcSetIterator);
    jboolean bHasNext = env->CallBooleanMethod(jParamsIterator, jcIteratorHasNext);
    while (bHasNext) {
        jobject entry = env->CallObjectMethod(jParamsIterator, jcIteratorNext);

        jstring jKey = (jstring)env->CallObjectMethod(entry, jcMapEntryGetKey);
        jstring jValue = (jstring)env->CallObjectMethod(entry, jcMapEntryGetValue);

        const char* cKey = env->GetStringUTFChars(jKey, nullptr);
        const char* cValue = env->GetStringUTFChars(jValue, nullptr);

        params.insert(std::make_pair(cKey, cValue));

        env->ReleaseStringUTFChars(jKey, cKey);
        env->ReleaseStringUTFChars(jValue, cValue);

        bHasNext = env->CallBooleanMethod(jParamsIterator, jcIteratorHasNext);
    }
    return params;
}


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
 * Signature: (Ljava/util/Map;)V
 */
JNIEXPORT void JNICALL Java_com_ndn_jwtan_identitymanager_NdncertClient_startNdncertClient
  (JNIEnv * env, jobject obj, jobject jParams){
    std::map<std::string, std::string> params = getParams(env, jParams);
    ::setenv("HOME", params["HOME"].c_str(), true);
    mFace = new Face();
    mKeyChain = new KeyChain();
    mClient = new ClientModule((*mFace), (*mKeyChain));
    mClient->getClientConf().load(getConfig());
    __android_log_print(
                ANDROID_LOG_ERROR,
                NDNCERT_CLIENT_TAG,
                "Finally I had a working ClientModule.");
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

    auto caList = mClient->getClientConf().m_caItems;
    std::vector<ClientCaItem> caVec{std::begin(caList), std::end(caList)};
    ClientCaItem clientCaItem = caVec[0];
    Name identityName = clientCaItem.m_caName.getPrefix(-1);
    identityName.append(paramList.front());

    mClient->sendNew(
        clientCaItem, identityName,
        std::bind(&newCb, _1, env, obj),
        std::bind(&errorCb, _1, env, obj));
    __android_log_print(
        ANDROID_LOG_INFO, NDNCERT_CLIENT_TAG,
        "Interest NEW_ sent!");
    // fake server below.
    /*mState = make_shared<RequestState>();
    mState->m_challengeList = std::list<std::string>({"Email", "PIN", "SMS"});
    mState->m_status = "Select";
    newCb(mState, env, obj);*/
}
void newCb(
  const shared_ptr<RequestState>& requestState,
  JNIEnv* env, jobject obj){
    __android_log_print(
        ANDROID_LOG_INFO, NDNCERT_CLIENT_TAG,
        "Data NEW_ got!");
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
    mClient->sendSelect(
        mState, paramList.front(), paramJson,
        std::bind(&selectCb, _1, env, obj),
        std::bind(&errorCb, _1, env, obj));
    __android_log_print(
        ANDROID_LOG_INFO, NDNCERT_CLIENT_TAG,
        "Interest SELECT_ sent!");
    // fake server below.
    /*mState->m_challengeType = choice;
    mState->m_status = "need-code";
    selectCb(mState, env, obj);*/
}
void selectCb(
  const shared_ptr<RequestState>& requestState,
  JNIEnv* env, jobject obj){
    __android_log_print(
        ANDROID_LOG_INFO, NDNCERT_CLIENT_TAG,
        "Data SELECT_ got!");
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
    mClient->sendValidate(
        mState, paramJson,
        std::bind(&validateCb, _1, env, obj),
        std::bind(&errorCb, _1, env, obj));
    __android_log_print(
        ANDROID_LOG_INFO, NDNCERT_CLIENT_TAG,
        "Interest VALIDATE_ sent!");
    // fake server below.
    /*if (paramList.front() == "961030"){
        mState->m_status = ChallengeModule::SUCCESS;
    } else {
        mState->m_status = ChallengeModule::FAILURE;
    }
    validateCb(mState, env, obj);*/
}
void validateCb(
  const shared_ptr<RequestState>& requestState,
  JNIEnv* env, jobject obj){
    __android_log_print(
        ANDROID_LOG_INFO, NDNCERT_CLIENT_TAG,
        "Data VALIDATE_ got!");
    mState = requestState;
    if (mState->m_status == ChallengeModule::SUCCESS) {
        __android_log_print(
            ANDROID_LOG_ERROR, NDNCERT_CLIENT_TAG,
            "DONE! Certificate has already been issued \n");
        mClient->requestDownload(mState,
                             bind(downloadCb, _1, env, obj),
                             std::bind(&errorCb, _1, env, obj));
        __android_log_print(
            ANDROID_LOG_INFO, NDNCERT_CLIENT_TAG,
            "Interest DOWNLOAT_ sent!");
        downloadCb(mState, env, obj);
    } else {
        auto challenge = ChallengeModule::createChallengeModule(mState->m_challengeType);
        auto requirementList = challenge->getRequirementForValidate(mState->m_status);

        auto textList = requirementList;
        auto hintList = std::list<std::string>({"961030"});

        // Call next function.
        callJavaFunction(env, obj, "sendValidate", textList, hintList);
    }
}

void downloadCb(
    const shared_ptr<RequestState>& requestState,
    JNIEnv* env, jobject obj){
    mState = requestState;

    __android_log_print(
        ANDROID_LOG_ERROR, NDNCERT_CLIENT_TAG,
        "DONE! Certificate has already been installed to local keychain");

    // Call text function.
    callJavaTextDialog(
        env, obj, "Congratulations!",
        "You certificate has already been installed to local keychain.");
}

/*
 * Class:     com_ndn_jwtan_identitymanager_NdncertClient
 * Method:    stopNdncertClient
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_ndn_jwtan_identitymanager_NdncertClient_stopNdncertClient
  (JNIEnv * env, jobject obj){
    delete mFace;
    delete mKeyChain;
    delete mClient;

    __android_log_print(
        ANDROID_LOG_ERROR, NDNCERT_CLIENT_TAG,
        "Resource cleaned up.");
}
