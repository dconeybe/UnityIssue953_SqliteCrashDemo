#include <jni.h>
#include <string>
#include <iostream>

extern "C" JNIEXPORT void JNICALL
Java_com_google_dconeybe_MainActivity_CauseSegmentationFault(
        JNIEnv* env,
        jobject /* this */) {
    static_cast<std::string*>(0)->append("foo");
}