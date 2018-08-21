LOCAL_PATH := $(call my-dir)
LOCAL_PATH_SAVED := $(LOCAL_PATH)

include $(CLEAR_VARS)
LOCAL_MODULE := ndncert-client
LOCAL_SRC_FILES := ndncert-client.cpp
LOCAL_SHARED_LIBRARIES := ndncert_shared ndn_cxx_shared boost_system_shared boost_thread_shared boost_log_shared
LOCAL_LDLIBS := -llog -latomic
LOCAL_CFLAGS := -DBOOST_LOG_DYN_LINK=1
include $(BUILD_SHARED_LIBRARY)

$(call import-module,../packages/ndncert/0.1.0)
