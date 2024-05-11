// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("pocketsapp");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("pocketsapp")
//      }
//    }

#include <jni.h>
#include <string>

#include <oboe/Oboe.h>

extern "C" JNIEXPORT jstring JNICALL
Java_yonatan_shabot_pocketsapp_MainActivity_stringFromJNI
        JNIEnv*
        jobject /* this */) {
    std::string hello = "Heloo from C++";
    oboe::AudioStream myStream;
    return env->NewStringUTF(hello.c_str());
}
