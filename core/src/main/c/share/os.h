/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_questdb_misc_Os */

#ifndef _Included_com_questdb_misc_Os
#define _Included_com_questdb_misc_Os
#ifdef __cplusplus
extern "C" {
#endif
#undef com_questdb_misc_Os_WINDOWS
#define com_questdb_misc_Os_WINDOWS 3L
#undef com_questdb_misc_Os__32Bit
#define com_questdb_misc_Os__32Bit -2L
#undef com_questdb_misc_Os_OSX
#define com_questdb_misc_Os_OSX 1L
#undef com_questdb_misc_Os_LINUX
#define com_questdb_misc_Os_LINUX 2L
#undef com_questdb_misc_Os_UNKNOWN
#define com_questdb_misc_Os_UNKNOWN -1L
/*
 * Class:     com_questdb_misc_Os
 * Method:    errno
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_questdb_misc_Os_errno
        (JNIEnv *, jclass);

/*
 * Class:     com_questdb_misc_Os
 * Method:    getPid
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_questdb_misc_Os_getPid
        (JNIEnv *, jclass);

/*
 * Class:     com_questdb_misc_Os
 * Method:    generateKrbToken
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_questdb_misc_Os_generateKrbToken
        (JNIEnv *, jclass, jlong);

/*
 * Class:     com_questdb_misc_Os
 * Method:    freeKrbToken
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_questdb_misc_Os_freeKrbToken
        (JNIEnv *, jclass, jlong);

/*
 * Class:     com_questdb_misc_Os
 * Method:    forkExec
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_questdb_misc_Os_forkExec
        (JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif
#endif
