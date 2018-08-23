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

namespace ndn {
namespace ndncert {

int nStep;

class ClientTool
{
public:
  ClientTool(ClientModule& clientModule)
    : client(clientModule)
  {
  }

  void
  errorCb(const std::string& errorInfo)
  {
    std::cerr << "Error: " << errorInfo << std::endl;
  }

  void
  downloadCb(const shared_ptr<RequestState>& state)
  {
    std::cerr << "Step " << nStep++
              << "DONE! Certificate has already been installed to local keychain\n";
    return;
  }

  void
  anchorCb(const Interest& request, const Data& reply,
           const ClientCaItem& anchorItem, const Name& assignedName)
  {
    auto contentJson = ClientModule::getJsonFromData(reply);
    auto caItem = ClientConfig::extractCaItem(contentJson);

    if (!security::verifySignature(caItem.m_anchor, anchorItem.m_anchor)) {
      std::cerr << "Fail to verify fetched anchor" << std::endl;
      return;
    }
    client.getClientConf().m_caItems.push_back(caItem);

    if (assignedName.toUri() != "/") {
      client.sendNew(caItem, assignedName,
                     bind(&ClientTool::newCb, this, _1),
                     bind(&ClientTool::errorCb, this, _1));
    }
    else {
      if (caItem.m_probe != "") {
        std::cerr << "Step " << nStep++ << ": Probe Requirement-" << caItem.m_probe << std::endl;
        std::string probeInfo;
        getline(std::cin, probeInfo);
        client.sendProbe(caItem, probeInfo,
                         bind(&ClientTool::newCb, this, _1),
                         bind(&ClientTool::errorCb, this, _1));
      }
      else {
        std::cerr << "Step " << nStep++ << ": Please type in the identity name\n";
        std::string nameComponent;
        getline(std::cin, nameComponent);
        Name identityName = caItem.m_caName.getPrefix(-1);
        identityName.append(nameComponent);
        client.sendNew(caItem, identityName,
                       bind(&ClientTool::newCb, this, _1),
                       bind(&ClientTool::errorCb, this, _1));
      }
    }
  }

  void
  listCb(const std::list<Name>& caList, const Name& assignedName, const Name& schema,
         const ClientCaItem& caItem)
  {
    if (assignedName.toUri() != "" && caList.size() == 1) {
      // with recommendation

      std::cerr << "Get recommended CA: " << caList.front()
                << "Get recommended Identity: " << assignedName << std::endl;
      client.requestCaTrustAnchor(caList.front(),
                                  bind(&ClientTool::anchorCb, this, _1, _2, caItem, assignedName),
                                  bind(&ClientTool::errorCb, this, _1));
    }
    else {
      // without recommendation
      int count = 0;
      for (auto name : caList) {
        std::cerr << "***************************************\n"
                  << "Index: " << count++ << "\n"
                  << "CA prefix:" << name << "\n"
                  << "***************************************\n";
      }
      std::cerr << "Select an index to apply for a certificate\n";

      std::string option;
      getline(std::cin, option);
      int caIndex = std::stoi(option);

      std::vector<Name> caVector{std::begin(caList), std::end(caList)};
      Name targetCaName = caVector[caIndex];

      client.requestCaTrustAnchor(targetCaName,
                                  bind(&ClientTool::anchorCb, this, _1, _2, caItem, Name("")),
                                  bind(&ClientTool::errorCb, this, _1));
    }
  }

  void
  validateCb(const shared_ptr<RequestState>& state)
  {
    if (state->m_status == ChallengeModule::SUCCESS) {
      std::cerr << "DONE! Certificate has already been issued \n";
      client.requestDownload(state,
                             bind(&ClientTool::downloadCb, this, _1),
                             bind(&ClientTool::errorCb, this, _1));
      return;
    }

    auto challenge = ChallengeModule::createChallengeModule(state->m_challengeType);
    auto requirementList = challenge->getRequirementForValidate(state->m_status);

    std::cerr << "Step " << nStep++ << ": Please satisfy following instruction(s)\n";
    for (auto requirement : requirementList) {
      std::cerr << "\t" << requirement << std::endl;
    }
    std::list<std::string> paraList;
    for (size_t i = 0; i < requirementList.size(); i++) {
      std::string tempParam;
      getline(std::cin, tempParam);
      paraList.push_back(tempParam);
    }
    auto paramJson = challenge->genValidateParamsJson(state->m_status, paraList);
    client.sendValidate(state, paramJson,
                        bind(&ClientTool::validateCb, this, _1),
                        bind(&ClientTool::errorCb, this, _1));
  }

  void
  selectCb(const shared_ptr<RequestState>& state)
  {
    auto challenge = ChallengeModule::createChallengeModule(state->m_challengeType);
    auto requirementList = challenge->getRequirementForValidate(state->m_status);

    std::cerr << "Step " << nStep++ << ": Please satisfy following instruction(s)" << std::endl;
    for (auto item : requirementList) {
      std::cerr << "\t" << item << std::endl;
    }
    std::list<std::string> paraList;
    for (size_t i = 0; i < requirementList.size(); i++) {
      std::string tempParam;
      getline(std::cin, tempParam);
      paraList.push_back(tempParam);
    }

    auto paramJson = challenge->genValidateParamsJson(state->m_status, paraList);
    client.sendValidate(state, paramJson,
                        bind(&ClientTool::validateCb, this, _1),
                        bind(&ClientTool::errorCb, this, _1));
  }

  void
  newCb(const shared_ptr<RequestState>& state)
  {
    std::cerr << "Step " << nStep++ << ": Please select one challenge from following types\n";
    for (auto item : state->m_challengeList) {
      std::cerr << "\t" << item << std::endl;
    }
    std::string choice;
    getline(std::cin, choice);

    auto challenge = ChallengeModule::createChallengeModule(choice);
    auto requirementList = challenge->getRequirementForSelect();
    std::list<std::string> paraList;
    if (requirementList.size() != 0) {
      std::cerr << "Step " << nStep++ << ": Please satisfy following instruction(s)\n";
      for (auto item : requirementList) {
        std::cerr << "\t" << item << std::endl;
      }
      for (size_t i = 0; i < requirementList.size(); i++) {
        std::string tempParam;
        getline(std::cin, tempParam);
        paraList.push_back(tempParam);
      }
    }
    auto paramJson = challenge->genSelectParamsJson(state->m_status, paraList);
    client.sendSelect(state, choice, paramJson,
                      bind(&ClientTool::selectCb, this, _1),
                      bind(&ClientTool::errorCb, this, _1));
  }

public:
  ClientModule& client;
};
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

jobjectArray cppStrVec2JStrArr(JNIEnv* env, std::vector<std::string>& vec){
    jclass strClass = env->FindClass("java/lang/String");
    jobjectArray jStrArr = env->NewObjectArray(vec.size(), strClass, nullptr);
    for (int i=0; i<vec.size(); i++){
        jstring tmp = env->NewStringUTF(vec[i].c_str());
        env->SetObjectArrayElement(jStrArr, i, tmp);
    }
    return jStrArr;
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
 * Method:    cppSendNew
 * Signature: ([Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_ndn_jwtan_identitymanager_NdncertClient_cppSendNew
  (JNIEnv * env, jobject obj, jobjectArray arr){
    // Get input in the form of Vec<String>
    auto strVec = jStrArr2CppStrVec(env, arr);

    // TODO: Do something. Send out message using NDN

    auto textVec = std::vector<std::string>();
    auto hintVec = std::vector<std::string>();

    textVec = strVec;
    hintVec = strVec;

    // Get info that are required, put them in list.

    // Prepare all the args.
    jobjectArray textJArr = cppStrVec2JStrArr(env, textVec);
    jobjectArray hintJArr = cppStrVec2JStrArr(env, hintVec);
    jclass cls = env->FindClass("com/ndn/jwtan/identitymanager/NdncertClient");
    jfieldID cbField = env->GetFieldID(
        cls, "sendSelect",
        "Lcom/ndn/jwtan/identitymanager/NdncertClient$Callback;");
    jobject cb = env->GetObjectField(obj, cbField);

    // Call next function.
    jmethodID promptInputDialog = env->GetMethodID(
        cls, "promptInputDialog",
        "([Ljava/lang/String;[Ljava/lang/String;Lcom/ndn/jwtan/identitymanager/NdncertClient$Callback;)V");
    env->CallVoidMethod(obj, promptInputDialog, textJArr, hintJArr, cb);
}
