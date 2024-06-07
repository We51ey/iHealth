#include "AGCN.h"

AGCN::AGCN(const std::string &modelDir, const std::string &labelPath,
                   const int cpuThreadNum, const std::string &cpuPowerMode,
                   float scoreThreshold)
        :scoreThreshold_(scoreThreshold) {
    paddle::lite_api::MobileConfig config;
    config.set_model_from_file(modelDir + "/AGCN.nb");
    config.set_threads(cpuThreadNum);
    config.set_power_mode(ParsePowerMode(cpuPowerMode));
    predictor_ =
            paddle::lite_api::CreatePaddlePredictor<paddle::lite_api::MobileConfig>(
                    config);

    labelList_ = LoadLabelList(labelPath);
}

std::vector<std::string> AGCN::LoadLabelList(const std::string &labelPath) {
    std::ifstream file;
    std::vector<std::string> labels;
    file.open(labelPath);
    while (file) {
        std::string line;
        std::getline(file, line);
        labels.push_back(line);
    }
    file.clear();
    file.close();
    return labels;
}

void AGCN::Postprocess(AGCN_RESULT *results) {
// TODO: Unified model output.
    auto outputTensor = predictor_->GetOutput(0);
    auto outputData = outputTensor->data<float>();
    auto outputShape = outputTensor->shape();
    int outputSize = ShapeProduction(outputShape);
    LOGD("AGCN OUTPUTSIZE %d", outputSize);
    auto class_id = static_cast<int>(round(outputData[0]));
    // Confidence score
    auto score = outputData[1];
    AGCN_RESULT object;
    object.class_name = class_id >= 0 && class_id < labelList_.size()
                        ? labelList_[class_id]
                        : "Unknow";
    object.class_id = class_id;
    object.score = score;
}




void AGCN::Preprocess(const std::vector<std::vector<float>> keypoint) {
//    std::vector<std::vector<std::vector<std::vector<std::vector<float>>>>> input_(1, std::vector<float>(2, std::vector<float>(350, std::vector<float>(17, std::vector<float >(1, 0)))));
//
//    for(int t = 0; t < (keypoint.size() < 350)?keypoint.size():350 ; t++){
//        for(int idx = 0; idx < 17; idx++){
//            input_[0][0][t][idx][0] = keypoint[t][idx * 3 + 1];
//            input_[0][1][t][idx][0] = keypoint[t][idx * 3 + 2];
//        }
//    }
//    cv::Mat ipt = cv::Mat(keypoint);

//    int inputShape[5] = {1, 2, 350, 17, 1};
//    cv::Mat ipt = cv::Mat(5, inputShape, CV_32FC1, cv::Scalar(0));
//    for(int t = 0; t < (keypoint.size()/51 < 350)?keypoint.size()/51:350 ; t++){
//        for(int idx = 0; idx < 17; idx++){
//            uchar id = *(ipt.data + 0 + 0 + t * 17 + idx);
//            id = keypoint[t][idx * 3 + 1];
//            uchar id2 = *(ipt.data + 0 + 1 * 350 * 17 + t * 17 + idx);
//            id2 = keypoint[t][idx * 3 + 2];
//        }
//   }
    std::unique_ptr<paddle::lite_api::Tensor> input_tensor(std::move(predictor_->GetInput(0)));
    input_tensor->Resize({1, 2, 350, 17, 1});
    auto *inputData = input_tensor->mutable_data<float>();

//    auto input_names = predictor_->GetInputNames();
//    const auto& tensor_name = input_names[0];
//    auto inputTensor = predictor_->GetInputByName(tensor_name);
//    inputTensor->Resize(paddle::lite_api::shape_t({1, 2, 350, 17, 1}));
//    auto inputData = inputTensor->mutable_data<float>();
//    LOGD("inputData,  %f", inputData);
//    ipt = ipt.reshape(inputShape);
//    Permute(&ipt, inputData);

//    for(int t = 0; t < (keypoint.size()/51 < 350)?keypoint.size()/51:350 ; t++){
//        for(int idx = 0; idx < 17; idx++){
//            uchar id = *(inputData + 0 + 0 + t * 17 + idx);
//            id = keypoint[t][idx * 3 + 1];
//            uchar id2 = *(inputData + 0 + 1 * 350 * 17 + t * 17 + idx);
//            id2 = keypoint[t][idx * 3 + 2];
//        }
//    }
    for(int i = 0; i < ShapeProduction(input_tensor->shape()); ++i){
        inputData[i] = 0.1;
    }
}


void AGCN::Predict(const std::vector<std::vector<float>> keypoint, AGCN_RESULT *results) {

    Preprocess(keypoint);
    LOGD("Test ----------------------------------- 1");
    predictor_->Run();
    LOGD("Test ----------------------------------- 2");
    Postprocess(results);
}

