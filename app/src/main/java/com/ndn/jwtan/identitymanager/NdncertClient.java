package com.ndn.jwtan.identitymanager;

public class NdncertClient {
    static{
        System.loadLibrary("ndncert-client");
    }

    static public native String init();

}
