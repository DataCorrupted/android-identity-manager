LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := ndncert-client
LOCAL_SRC_FILES := ndncert-client.cpp
include $(BUILD_SHARED_LIBRARY)
