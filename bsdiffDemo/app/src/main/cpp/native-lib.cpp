#include <jni.h>
#include <string>

extern "C" {
    extern int main(int argc, const char * argv[]);   //导入bspatch文件的main函数
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_bsdiffdemo_MainActivity_getNewApk(JNIEnv *env, jobject thiz, jstring old_path,
                                                   jstring new_path, jstring patch) {
    const char *charOldPath = env->GetStringUTFChars(old_path, 0);
    const char *charNewPath = env->GetStringUTFChars(new_path, 0);
    const char *charPatch = env->GetStringUTFChars(patch, 0);

    const char *argv[] = {"",charOldPath, charNewPath, charPatch};
    main(4, argv); //通过函数将旧的APK和差分包处理得到新的apk，并通过第2个参数输出出来

    env->ReleaseStringUTFChars(old_path, charOldPath);
    env->ReleaseStringUTFChars(new_path, charNewPath);
    env->ReleaseStringUTFChars(patch, charPatch);
}