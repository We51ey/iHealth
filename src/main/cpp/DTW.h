//
// Created by weilai on 2022/3/3.
//

#include <vector>
#include <iostream>
#include <math.h> /* isinf */
#include <algorithm>
#include <assert.h>
#include <fstream>/* 用于读取标准动作npy */
#include "Utils.h"
#include "AGCN.h"



struct Path
{
    std::vector<int> x;
    std::vector<int> y;

};

struct DTW_result
{
    float smiliarity;
    float max_angle;
    float dtw_cost;
};



//Path _traceback(std::vector<std::vector<float>> D);
//DTW_result dtw(std::shared_ptr<AGCN> &agcn_, std::vector <std::vector<float>> X, std::vector <std::vector<float>> Y,
//               float (*func)(std::vector<float>, std::vector<float>), std::vector<int> kpts,
//               int warp = 1, float w = float('inf'), float s = 1.0);
DTW_result get_score(std::shared_ptr<AGCN> &agcn_, std::vector<std::vector<float>> A, std::vector<std::vector<float>> B, std::vector<int> kpts, float max_angle = -1.0);
float angle_score(std::vector<float> X, std::vector<float> Y = {0, 1});
float pruneddtw(std::shared_ptr<AGCN> &agcn_, std::vector<std::vector<float>> A, std::vector<std::vector<float>> B, float (*func)(std::vector<float>, std::vector<float>), float w = 1);

float getMold(const std::vector<float>& vec);
float getSimilarity(std::vector<float> lhs, std::vector<float> rhs);
float Eucildean_Distance(std::vector<float> x, std::vector<float> y);

int face_toward(std::vector<float> &kpts_sframe);  // 判断脸部朝向



std::vector<float> keypoint_flip(std::vector<float> &kpts_sframe);