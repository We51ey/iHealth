// Copyright (c) 2019 PaddlePaddle Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include "Native.h"
#include "Pipeline.h"
#include "postprocess.h"

#ifdef __cplusplus

extern "C" {
#endif
/*
 * Class:     com_baidu_paddle_lite_demo_yolo_detection_Native
 * Method:    nativeInit
 * Signature:
 * (Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;II[F[FF)J
 */

float PRE[113][51] = {0.0f};
float BACK[144][51] = {0.0f};
float BOW[91][51] = {0.0f};
float LUNGE[229][51] = {0.0f};
JNIEXPORT jlong JNICALL
Java_com_baidu_paddle_lite_demo_yolo_1detection_Native_nativeInit(
        JNIEnv *env, jclass thiz, jstring jModelDir, jstring jLabelPath,
        jint cpuThreadNum, jstring jCPUPowerMode, jint inputWidth, jint inputHeight,
        jfloatArray jInputMean, jfloatArray jInputStd, jfloat scoreThreshold) {
    std::string modelDir = jstring_to_cpp_string(env, jModelDir);
    std::string labelPath = jstring_to_cpp_string(env, jLabelPath);
    std::string cpuPowerMode = jstring_to_cpp_string(env, jCPUPowerMode);
    std::vector<float> inputMean = jfloatarray_to_float_vector(env, jInputMean);
    std::vector<float> inputStd = jfloatarray_to_float_vector(env, jInputStd);
//  LOGD("jModelDir:----------",jModelDir);
    return reinterpret_cast<jlong>(
            new Pipeline(modelDir, labelPath, cpuThreadNum, cpuPowerMode, inputWidth,
                         inputHeight, inputMean, inputStd, scoreThreshold));
}

/*
 * Class:     com_baidu_paddle_lite_demo_yolo_detection_Native
 * Method:    nativeRelease
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_baidu_paddle_lite_demo_yolo_1detection_Native_nativeRelease(
        JNIEnv *env, jclass thiz, jlong ctx) {
    if (ctx == 0) {
        return JNI_FALSE;
    }
    Pipeline *pipeline = reinterpret_cast<Pipeline *>(ctx);
    delete pipeline;
    return JNI_TRUE;
}

/*
 * Class:     com_baidu_paddle_lite_demo_yolo_detection_Native
 * Method:    nativeProcess
 * Signature: (JIIIILjava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_baidu_paddle_lite_demo_yolo_1detection_Native_nativeProcess(
        JNIEnv *env, jclass thiz, jlong ctx, jint inTextureId, jint outTextureId,
        jint textureWidth, jint textureHeight, jstring jsavedImagePath, jint actionid,
        jboolean single) {
    if (ctx == 0) {
        return JNI_FALSE;
    }
    std::string savedImagePath = jstring_to_cpp_string(env, jsavedImagePath);
    Pipeline *pipeline = reinterpret_cast<Pipeline *>(ctx);
    pipeline->Process(inTextureId, outTextureId, textureWidth,
                      textureHeight, savedImagePath, actionid, single);
    return JNI_TRUE;
}

JNIEXPORT jintArray JNICALL
Java_com_baidu_paddle_lite_demo_yolo_1detection_Native_nativeGetActionCountAndScore(
        JNIEnv *env, jclass thiz, jlong ctx) {
    if (ctx == 0) {
        return JNI_FALSE;
    }
    Pipeline *pipeline = reinterpret_cast<Pipeline *>(ctx);
    //此处获取 count 和 score
    std::vector<int> records = pipeline->GetCountAndScore();
    return intvector_to_jintarray(env, records);
}

JNIEXPORT jboolean JNICALL
Java_com_baidu_paddle_lite_demo_yolo_1detection_Native_nativeReset(
        JNIEnv *env, jclass thiz, jlong ctx) {
    if (ctx == 0) {
        return JNI_FALSE;
    }
    Pipeline *pipeline = reinterpret_cast<Pipeline *>(ctx);
    pipeline->ClearCount();
    return JNI_TRUE;
}

#ifdef __cplusplus
}
#endif

extern "C"
JNIEXPORT void JNICALL
Java_com_baidu_paddle_lite_demo_yolo_1detection_Native_SetAssetManager(JNIEnv *env, jclass clazz,
                                                                       jobject asset_manager) {
    AAssetManager *mgr = AAssetManager_fromJava(env, asset_manager);
    jstring filename1 = cpp_string_to_jstring(env, "models/yolov3_mobilenet_v3_for_cpu/0_113.npy");
    jstring filename2 = cpp_string_to_jstring(env, "models/yolov3_mobilenet_v3_for_cpu/1_144.npy");
    jstring filename3 = cpp_string_to_jstring(env, "models/yolov3_mobilenet_v3_for_cpu/2_91.npy");
    jstring filename4 = cpp_string_to_jstring(env, "models/yolov3_mobilenet_v3_for_cpu/3_229.npy");
    get_standar(env, mgr, 1, filename1);
    get_standar(env, mgr, 2, filename2);
    get_standar(env, mgr, 3, filename3);
    get_standar(env, mgr, 4, filename4);
//    LOGD("-----------------%f-----------",PRE[0][1]);
}

char *dataRead(AAssetManager *mManeger, const char *datafile1) {

    AAsset *dataAsset = AAssetManager_open(mManeger, datafile1,
                                           AASSET_MODE_UNKNOWN);//get file read AAsset
    off_t dataBufferSize = AAsset_getLength(dataAsset);

    int num = dataBufferSize / sizeof(float);

    char *ret;
    //float *data = (float*) malloc(num * sizeof(float));  //allocate the data, the same with the later line
    char *data = (char *) malloc(dataBufferSize);

    int numBytesRead = AAsset_read(dataAsset, data, dataBufferSize);  //begin to read data once time
    //note: numBytesRead is the total bytes, then num = dataBufferSize/sizeof(float) = numBytesRead/sizeof(float)

    if (numBytesRead < 0) {
        LOGI("read data failed");
    } else {
        LOGI("numBytesRead: %d", numBytesRead);
    }

    ret = data;
    AAsset_close(dataAsset);
    free(data);

    return ret;
}

void get_standar(JNIEnv *env, AAssetManager *mgr, int actionId, jstring filename) {
    jboolean iscopy;
    float tmp = 0.0f;
    unsigned char block[4];
    const char *datafile = env->GetStringUTFChars(filename, &iscopy);
    char *data = dataRead(mgr, datafile) + 128;
    switch (actionId) {
        case 1:
            for (int i = 0; i < 113 * 51 * 4; i = i + 4) {
                block[3] = data[i + 3];
                block[2] = data[i + 2];
                block[1] = data[i + 1];
                block[0] = data[i + 0];
                memcpy(&tmp, block, sizeof(float));
                PRE[i / (51 * 4)][(i % (51 * 4) / 4)] = tmp;
            }
            env->ReleaseStringUTFChars(filename, datafile);
            break;
        case 2:
            for (int i = 0; i < 144 * 51 * 4; i = i + 4) {
                block[3] = data[i + 3];
                block[2] = data[i + 2];
                block[1] = data[i + 1];
                block[0] = data[i + 0];
                memcpy(&tmp, block, sizeof(float));
                BACK[i / (51 * 4)][(i % (51 * 4) / 4)] = tmp;
            }
            env->ReleaseStringUTFChars(filename, datafile);
            break;
        case 3:
            for (int i = 0; i < 91 * 51 * 4; i = i + 4) {
                block[3] = data[i + 3];
                block[2] = data[i + 2];
                block[1] = data[i + 1];
                block[0] = data[i + 0];
                memcpy(&tmp, block, sizeof(float));
                BOW[i / (51 * 4)][(i % (51 * 4) / 4)] = tmp;
            }
            env->ReleaseStringUTFChars(filename, datafile);
            break;
        case 4:
            for (int i = 0; i < 229 * 51 * 4; i = i + 4) {
                block[3] = data[i + 3];
                block[2] = data[i + 2];
                block[1] = data[i + 1];
                block[0] = data[i + 0];
                memcpy(&tmp, block, sizeof(float));
                LUNGE[i / (51 * 4)][(i % (51 * 4) / 4)] = tmp;
            }
            env->ReleaseStringUTFChars(filename, datafile);
            break;
        default:
            break;
    }

}