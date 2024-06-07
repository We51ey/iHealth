//
// Created by weilai on 2022/3/17.
//
#pragma once

#include "Utils.h"
#include "paddle_api.h"
#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include "opencv2/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgcodecs.hpp"
#include "opencv2/imgproc.hpp"
#include <string>
#include <vector>
//#include "paddle_use_kernels.h"
//#include "paddle_use_ops.h"

struct AGCN_RESULT {
    std::string class_name;
    int class_id;
    float score;
};

class AGCN {
public:
    explicit AGCN(const std::string &modelDir, const std::string &labelPath,
                  const int cpuThreadNum, const std::string &cpuPowerMode,
                  float scoreThreshold);

    void Predict(const std::vector<std::vector<float>> keypoint, AGCN_RESULT *results);

private:
    std::vector<std::string> LoadLabelList(const std::string &path);
    void Preprocess(const std::vector<std::vector<float>> keypoint);
    void Postprocess(AGCN_RESULT *results);

private:
    float scoreThreshold_;
    std::vector<std::string> labelList_;
    std::shared_ptr<paddle::lite_api::PaddlePredictor> predictor_;

};


