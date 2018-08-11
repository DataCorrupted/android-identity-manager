# android-identity-manager

Zhehao <zhehao@cs.ucla.edu> (Original work by Jiewen Tan, https://github.com/alanwaketan/android-identity-manager)

Jan 9, 2015 - v0.2

Screen recording on Android 4.4.4 (Kitkat): https://vimeo.com/151273751

### What it does:

* Request an Open mHealth id (similar with NDN cert)
  * Generate user and device identity
  * Request a user identity to be signed by Open mHealth root
  * Sign a device identity with user identity
* Authorize NDNFit application
  * Sign an application identity with user identity
  * Display authorized applications

### How to use:

* Steps to create an OpenmHealth identity:
  * Launch application, tap one of the plus signs above each "Create new ID"
  * Accept the terms of service
  * Put your email address and give the identity a name (this name is kept locally, and has nothing to do with the ID's NDN name), click next
  * Choose a profile picture (optional), click submit
  * Open email application, and check email for the subject "[NDN Open mHealth Certification] request confirmation", open the link with the option "open link in ID manager"
  * Wait for the ID manager to confirm the request, then open email application again, and check email for the subject "[NDN Open mHealth Certification] certificate issued", open the link with the option "open link in ID manager", and wait till the "cert installed" confirmation shows up.
* To see what applications are authorized with a certain identity, click the identity image in main activity.
* (Coming soon) Upon launching NDNFit capture application for the first time, the user will be prompted to choose an identity with ID manager. Current NDNFit apk that behaves like this can be built from: https://github.com/zhehaowang/ndnfit/tree/with-id-manager.

* Common problems:
  * Getting the apk: https://github.com/zhehaowang/android-identity-manager/releases/download/v0.2/identity-manager-0.2.apk; Please check "allow from unverified sources" if accessed from an Android device
  * Stuck on "submit token", or "confirm request": Check phone's internet connection; If working, the connection may have blocked outgoing traffic to port 5001 on memoria.ndn.ucla.edu (which runs the Open mHealth cert service)

### TODOs:

* Testing on post-lollipop Android devices (5.0+)
* Config verification policy; Integration tests with capture app, DSU and basic DVU
* Exception handling
  * Installing the same ID twice
  * Unexpected interaction between apps, and between cert website
  * No network connection
  * ...
* UI improvements
  * Weird looking Floating Action Buttons on Main activity on pre-lollipop devices
  * Profile images, and their selection layout; selecting images from gallery
  * UI for authorized applications page
* Conforming with user id, device id, app id design
  * Current implementation has a "device identity" which is signed by "user id", and "user id" used to sign "app id", which signs the data
* Miscellaneous
  * Check on the location of "KEY" component in Open mHealth cert names
  * ...

### Notes:

* The app uses a default certificate issueing website at: http://memoria.ndn.ucla.edu:5001, whose interface's documented at: https://github.com/zhehaowang/openmhealth-cert#webmobile-app-interface. The site's based on https://github.com/named-data/ndncert
* To see the list of issued identities, go to http://memoria.ndn.ucla.edu:5001/cert/list/html
* The NDNFit capture application will be released soon; 
* The two interactions with email application: first one gets the assigned namespace, and verifies that the user owns the email address; second one gets the signed certificate, if it's approved. (Currently split in two, could merge into one?)

### Development:

* Open in Android Studio, SDK 22, build tools 22.0.1; sync Gradle
* Clone 'volley': git clone https://android.googlesource.com/platform/frameworks/volley (currently not in submodule)


# TODO

## Put ndncert in

Let's use JNI, Alex provided a great example of how to use it. The thoughts should be the following:

Zhiyi has provided a great demo. In his demo, he created a ClientTool class, which, to me, can be used almost verbatim with little change, the changes includes but not limited to:

1. add JNI environment to it so that we can transfer out result(certificate) back.
2. change getline() to a Java function that can: a) pop out a window with input text place 2) gets the string. After that we can transfer that jobject to std::string and do everything as normal. Please refer to [here](https://stackoverflow.com/questions/5198105/calling-a-java-method-from-c-in-android) for how this should be done.
3. As to Java String and std::string's difference and how to transferring from one to another, refer to [here](http://electrofriends.com/articles/jni/jni-part-4-jni-strings/)

## Status

I have setup the environment, almost a whole week is spent on that.

### Init JNI

For how to init JNI, refer to these:

https://medium.com/@ssaurel/create-your-first-jni-application-on-android-with-the-ndk-5f149508fb12

http://kn-gloryo.github.io/Build_NDK_AndroidStudio_detail/

For compiling, although you can set up gradle and ask it to compile cpp for you, it's awkward as gradle will not tell where is the error in cpp, it only reports java exception. 

It's suggested that you setup ndk-build tool mentioned [here](http://kn-gloryo.github.io/Build_NDK_AndroidStudio_detail/) and use it to compile when you are debugging and use gradle when your are done with JNI.

For testing, you can initiate a Android Virtual Device(AVD)(Android-studio -> Tools -> AVD). But be aware that this device is not stable. Do notice that your device only have 1G space by default. When you run out of space, you can kill it and create a new one.

### Put ndn-cxx as dependency

Please refer to Alex's NFD-android's [README-dev.md](https://github.com/named-data-mobile/NFD-android/blob/master/README-dev.md).

Alex is using crew, a tool that will compile your cpp code into different architectures and you can import the compiled dynamically linked library to your Android project as your cpp's dependency.

Reproduce Alex's work so that you have a better understanding of crew and then switch to ours:

1. You don't need his crew repo, use the one I forked [here](https://github.com/DataCorrupted/android-crew-staging/tree/for-ndk-r18). I have put a script for ndncert inside.

2. Alex is using command line tool, that's why in his script you need to download sdk first. But we are using IDE, sdk is (most likely) already presented in _~/Android/Sdk_. So please do the following:

```shell
cd ~/Android/Sdk/ndk-bundle
git clone https://github.com/DataCorrupted/android-crew-staging.git crew.dir
cd crew.dir

# If install is not working, download the source code and built it.
# I encountered bad SHA256 sum problem. Not figure out why yet
./crew install target/sqlite target/openssl target/boost
./crew install target/ndn_cxx
```

3. The script I wrote for ndncert is not working as it has linking problems. I have asked Alex, he hasn't replied yet. **Please help me fix it**

4. Go to IDE and see our jni part. I have included ndn-cxx and it's working. The only thing I left un-handled is ndncert. Once you finish that, you can add do the following:
add _ndncert_ to _LOCAL_SHARED_LIBRARIES_ and in _$(call ...)_ change ndncxx to ndncert.

5. What we want is using Zhiyi's tool(ndncert-client), thus there isn't much coding here. Put _ClientTool_ he defined in ndncert-client.cpp into our code is sufficient.

6. However, you much change every getline() to a new function. What we want here is not getline, but to prompt a window and ask the user to imput something, so I have defined a getline() myself. 
You may need a [dialog](https://developer.android.com/guide/topics/ui/dialogs) for user input. [This](https://www.viralandroid.com/2016/04/android-user-input-dialog-example.html) seems to be a good tutorial for create dialogs, I haven't read it carefully yet.
