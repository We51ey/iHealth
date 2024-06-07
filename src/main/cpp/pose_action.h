//   Copyright (c) 2021 PaddlePaddle Authors. All Rights Reserved.
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

#pragma once

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <vector>
#include "Detector_Kpts.h"
#include "DTW.h"
#include <string>
#include "global.h"
//typedef struct Standard{

//    std::vector<std::vector<float>> pre;
//    std::vector<std::vector<float>> back;
//    std::vector<std::vector<float>> bow;
//    std::vector<std::vector<float>> lunge;

//};

//extern Standard *stdard;
#define INF 1e20

struct action_helper {
  bool mark = true;
  int action_count = 0;
  int latency = 0;
  std::vector<std::vector<float>> history;
  float score = 0;
  float max_angle = 0;
  float min_agnle = INF;
};

void clear_action_count();
int get_action_count(int recid);
int get_action_score(int recid);
float get_xyratio(std::vector<float> &kpts_sframe, int index_x, int index_y);
float vector_angle(std::vector<float> &kpts_sframe, int index_x, int index_y, int index_z);
bool get_xyhigher(std::vector<float> &kpts_sframe, int index_x, int index_y);
int check_prelude(std::shared_ptr<AGCN> &agcn_, std::vector<float> &kpts_sframe, int recid);
int check_backward_extension(std::shared_ptr<AGCN> &agcn_, std::vector<float> &kpts_sframe, int recid);
int check_bow_stance(std::shared_ptr<AGCN> &agcn_,  std::vector<float> &kpts_sframe, int recid);
int check_lunge(std::shared_ptr<AGCN> &agcn_, std::vector<float> &kpts_sframe, int recid);
int single_action_check(std::shared_ptr<AGCN> &agcn_, std::vector<float> &results_kpts, float h, int actionid, int recid=0);
void double_action_check(std::vector<RESULT_KEYPOINT> &results_kpts, std::vector<RESULT> &results, int actionid, int imgw);
