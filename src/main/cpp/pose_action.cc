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
#include "pose_action.h"
#include "global.h"
#define PI 3.141592653

/*
keypoint indexes:
0: 'nose',
1: 'left_eye',
2: 'right_eye',
3: 'left_ear',
4: 'right_ear',
5: 'left_shoulder',
6: 'right_shoulder',
7: 'left_elbow',
8: 'right_elbow',
9: 'left_wrist',
10: 'right_wrist',
11: 'left_hip',
12: 'right_hip',
13: 'left_knee',
14: 'right_knee',
15: 'left_ankle',
16: 'right_ankle'
*/

//Standard *stdard=NULL;
std::vector<action_helper> action_recs(2);

/**
 * 获取标准动作
 * @param recid
 * @return
 */

/***************************************************************************/


/**
 * 计数归零
 */
void clear_action_count() {
    action_recs[0].action_count = 0;
    action_recs[1].action_count = 0;
    return;
}

float thres_conf = 0.2;

/**
 * 动作计数
 * @param recid
 * @return
 */
int get_action_count(int recid) {
    return action_recs[recid].action_count;
}

/**
 * 动作分数
 * @param recid
 * @return
 */
int get_action_score(int recid) {
    return action_recs[recid].score*100;
}

/**
 * 获取xy角度
 * @param kpts_sframe
 * @param index_x
 * @param index_y
 * @return
 */
float get_xyratio(std::vector<float> &kpts_sframe, int index_x, int index_y) {
    float xdiff = kpts_sframe[index_y * 3 + 1] - kpts_sframe[index_x * 3 + 1];
    float ydiff = std::abs(kpts_sframe[index_x * 3 + 2] - kpts_sframe[index_y * 3 + 2]);
    return xdiff / ydiff;
}

float vector_angle(std::vector<float> &kpts_sframe, int index_x, int index_y, int index_z) {
    float vector1x = kpts_sframe[index_z * 3 + 1] - kpts_sframe[index_y * 3 + 1];
    float vector1y = kpts_sframe[index_z * 3 + 2] - kpts_sframe[index_y * 3 + 2];
    float vector2x = kpts_sframe[index_x * 3 + 1] - kpts_sframe[index_y * 3 + 1];
    float vector2y = kpts_sframe[index_x * 3 + 2] - kpts_sframe[index_y * 3 + 2];
    float t = (vector1x * vector2x + vector1y * vector2y) /
              (sqrt(pow(vector1x, 2) + pow(vector1y, 2)) *
               sqrt(pow(vector2x, 2) + pow(vector2y, 2)));
    return acos(t) * (180 / PI);
}

bool get_xyhigher(std::vector<float> &kpts_sframe, int index_x, int index_y) {
    float ydiff = kpts_sframe[index_x * 3 + 2] - kpts_sframe[index_y * 3 + 2];
    return ydiff > 0;
}

/**
 * 动作
 * @param kpts_sframe
 * @param recid
 * @return
 */


/****************************************************************************************/
int check_prelude(std::shared_ptr<AGCN> &agcn_, std::vector<float> &kpts_sframe, int recid) {
    std::vector<std::vector<float>> pre(113, std::vector<float>(51, 0));
    for(int i = 0; i < 113; i++){
        for(int j = 0 ; j < 51; j++){
            pre[i][j] = PRE[i][j];
        }
    }
    std::vector<int> kpts;
    if (face_toward(kpts_sframe))
        kpts = {6, 8, 10};
    else
        kpts = {5, 7, 9};
    if (kpts_sframe.empty()) {
        return action_recs[recid].action_count;
    }

    float xy_angle;

    if (kpts_sframe[kpts[0] * 3] > thres_conf && kpts_sframe[kpts[1] * 3] > thres_conf &&
        kpts_sframe[kpts[2] * 3] > thres_conf) {

        xy_angle =  angle_score({kpts_sframe[kpts[1] * 3 + 1] - kpts_sframe[kpts[0] * 3 + 1],
                                       kpts_sframe[kpts[1] * 3 + 2] - kpts_sframe[kpts[0] * 3 + 2]}); // 肩与肘  和  垂线

    }
    else {
        return action_recs[recid].action_count;
    }

    if (!action_recs[recid].mark) {
        action_recs[recid].history.push_back(kpts_sframe);
        action_recs[recid].max_angle =  xy_angle > action_recs[recid].max_angle?xy_angle:action_recs[recid].max_angle;
    }

    if ((kpts_sframe[kpts[2] * 3 + 2] > kpts_sframe[kpts[1] * 3 + 2]) &&
        (kpts_sframe[kpts[1] * 3 + 2] > kpts_sframe[kpts[0] * 3 + 2]) &&
        xy_angle < 30.0 && action_recs[recid].mark){
        action_recs[recid].latency += 1;
        if (action_recs[recid].latency == 5) {
            action_recs[recid].mark = false;
            action_recs[recid].latency = 0;
            action_recs[recid].max_angle = 0;
        }
    }
    else if((kpts_sframe[kpts[2] * 3 + 2] < kpts_sframe[kpts[1] * 3 + 2]) &&
            (kpts_sframe[kpts[1] * 3 + 2] < kpts_sframe[kpts[0] * 3 + 2]) &&
            xy_angle < action_recs[recid].max_angle && !(action_recs[recid].mark)){
        action_recs[recid].latency += 1;
        if (action_recs[recid].latency == 6){
            DTW_result ret = get_score(agcn_, action_recs[recid].history, pre, kpts, action_recs[recid].max_angle / 180.0);
            action_recs[recid].action_count += 1;
            action_recs[recid].mark = true;
            action_recs[recid].latency = 0;
            action_recs[recid].score = ret.smiliarity;
            }
        action_recs[recid].history.clear();
        }
    else if(kpts_sframe[kpts[2] * 3 + 1]){
        return action_recs[recid].action_count;
    }
    return action_recs[recid].action_count;

}

int check_backward_extension(std::shared_ptr<AGCN> &agcn_,  std::vector<float> &kpts_sframe, int recid) {
    std::vector<std::vector<float>> back(144, std::vector<float>(51, 0));
    for(int i = 0; i < 144; i++){
        for(int j = 0 ; j < 51; j++){
            back[i][j] = BACK[i][j];
        }
    }

    std::vector<int> kpts;
    if (face_toward(kpts_sframe))
        kpts = {6, 8, 10, 12};
    else
        kpts = {5, 7, 9, 11};
    if (kpts_sframe.empty()) {
        return action_recs[recid].action_count;
    }
//    float xy_ratio, xz_ratio;
    float xy_angle;
    float down_thres = 0.2;
    float up_thres = 0.5;
    if (kpts_sframe[kpts[0] * 3] > thres_conf && kpts_sframe[kpts[1] * 3] > thres_conf &&
        kpts_sframe[kpts[2] * 3] > thres_conf) {
        xy_angle =  angle_score({kpts_sframe[kpts[1] * 3 + 1] - kpts_sframe[kpts[0] * 3 + 1],
                                 kpts_sframe[kpts[1] * 3 + 2] - kpts_sframe[kpts[0] * 3 + 2]}); // 肩与肘  和  垂线
//        xy_ratio = get_xyratio(kpts_sframe, kpts[0], kpts[1]);
//        xz_ratio = get_xyratio(kpts_sframe, kpts[1], kpts[2]);
    }
//  else if (kpts_sframe[kpts[2]*3] > thres_conf && kpts_sframe[kpts[3]*3] > thres_conf) {
//    xy_ratio = get_xyratio(kpts_sframe, kpts[2], kpts[3]);
//  }
    else {
        return action_recs[recid].action_count;
    }
//    LOGD("xy ration: %f,     xz ratio: %f", xy_ratio, xz_ratio);
    if (!action_recs[recid].mark) {
        action_recs[recid].history.push_back(kpts_sframe);
        action_recs[recid].max_angle = (abs(xy_angle) > action_recs[recid].max_angle)?abs(xy_angle):action_recs[recid].max_angle;
    }
    if (xy_angle < 10 && action_recs[recid].mark) {
        action_recs[recid].latency += 1;
        if (action_recs[recid].latency == 6) {
            action_recs[recid].mark = false;
            action_recs[recid].latency = 0;
            action_recs[recid].max_angle = 0;
        }
    } else if (abs(xy_angle) > 10 && abs(xy_angle) < abs(action_recs[recid].max_angle)  && !(action_recs[recid].mark)) {
        action_recs[recid].latency += 1;
        if (action_recs[recid].latency == 6) {
            action_recs[recid].action_count += 1;
            action_recs[recid].mark = true;
            action_recs[recid].latency = 0;
            action_recs[recid].score = get_score(agcn_, action_recs[recid].history, back, kpts, action_recs[recid].max_angle / 70.0).smiliarity;
            action_recs[recid].history.clear();
        }
    }
    return action_recs[recid].action_count;
}

int check_bow_stance(std::shared_ptr<AGCN> &agcn_, std::vector<float> &kpts_sframe, int recid) {
    std::vector<std::vector<float>> bow(91, std::vector<float>(51, 0));
    for(int i = 0; i < 91; i++){
        for(int j = 0 ; j < 51; j++){
            bow[i][j] = BOW[i][j];
        }
    }
    std::vector<int> kpts;
    if (face_toward(kpts_sframe))
        kpts = {6, 8, 10, 12, 14, 16};
    else
        kpts = {5, 7, 9, 11, 13, 15};
    if (kpts_sframe.empty()) {
        return action_recs[recid].action_count;
    }

    float knee_ratio;
    if (kpts_sframe[kpts[3] * 3] > thres_conf && kpts_sframe[kpts[4] * 3] > thres_conf &&
        kpts_sframe[kpts[5] * 3] > thres_conf) {
        knee_ratio = vector_angle(kpts_sframe, kpts[3], kpts[4], kpts[5]);
    }
    else {
        return action_recs[recid].action_count;
    }
    if (!action_recs[recid].mark) {
        action_recs[recid].history.push_back(kpts_sframe);
        action_recs[recid].min_agnle = knee_ratio < action_recs[recid].min_agnle?knee_ratio:action_recs[recid].min_agnle;
    }
//    std::cout << knee_ratio << std::endl;
    if (170 < knee_ratio && knee_ratio < 190 && action_recs[recid].mark) {
        action_recs[recid].latency += 1;
        if (action_recs[recid].latency == 6) {
            action_recs[recid].mark = false;
            action_recs[recid].latency = 0;
            action_recs[recid].min_agnle = INF;
        }
    } else if (knee_ratio < 150 && knee_ratio > action_recs[recid].min_agnle && !(action_recs[recid].mark)) {
        action_recs[recid].latency += 1;
        if (action_recs[recid].latency == 6) {
            action_recs[recid].action_count += 1;
            action_recs[recid].mark = true;
            LOGD("min angle %f", action_recs[recid].min_agnle);
            action_recs[recid].min_agnle = action_recs[recid].min_agnle < 60?60:action_recs[recid].min_agnle;
            action_recs[recid].score = get_score(agcn_, action_recs[recid].history, bow, kpts, 1 - (action_recs[recid].min_agnle - 60) / 120).smiliarity;
            action_recs[recid].history.clear();
            action_recs[recid].latency = 0;
        }
    }
    return action_recs[recid].action_count;
}

int check_lunge(std::shared_ptr<AGCN> &agcn_, std::vector<float> &kpts_sframe, int recid) {
    std::vector<std::vector<float>> lunge(229, std::vector<float>(51, 0));
    for(int i = 0; i < 229; i++){
        for(int j = 0 ; j < 51; j++){
            lunge[i][j] = LUNGE[i][j];
        }
    }
    std::vector<int> kpts;
    bool is_face_toward = face_toward(kpts_sframe);
    if (is_face_toward)
//        kpts = {12, 14, 16, 12, 13, 15};
        kpts = {11, 13, 15, 11, 14, 16};
//        kpts = {11, 13, 15, 12, 14, 16};
    else
//        kpts = {11, 13, 15, 11, 14, 16};
        kpts = {12, 14, 16, 12, 13, 15};
//        kpts = {12, 14, 16, 11, 13, 15};
    if (kpts_sframe.empty()) {
        return action_recs[recid].action_count;
    }
    float left_knee_ratio = 0.0;
    float right_knee_ratio = 0.0;
    float hip_ration;
    if (kpts_sframe[kpts[0] * 3] > thres_conf && kpts_sframe[kpts[1] * 3] > thres_conf &&
        kpts_sframe[kpts[2] * 3] > thres_conf)
        right_knee_ratio = vector_angle(kpts_sframe, kpts[0], kpts[1], kpts[2]);
    else {
        return action_recs[recid].action_count;
    }
    if (kpts_sframe[kpts[3] * 3] > thres_conf && kpts_sframe[kpts[4] * 3] > thres_conf &&
               kpts_sframe[kpts[5] * 3] > thres_conf)
        left_knee_ratio = vector_angle(kpts_sframe, kpts[3], kpts[4], kpts[5]);
    else {
        return action_recs[recid].action_count;
    }
    if (kpts_sframe[kpts[0] * 3] > thres_conf && kpts_sframe[kpts[1] * 3] > thres_conf &&
        kpts_sframe[kpts[4] * 3] > thres_conf)
        hip_ration = vector_angle(kpts_sframe, kpts[1], kpts[0], kpts[4]);
    else
        return action_recs[recid].action_count;

    float front_ration;
    float back_ration;

    if(kpts_sframe[kpts[4] * 3 + 1] < kpts_sframe[kpts[1] * 3 + 1]){ //右腿在前
        if(is_face_toward){
            front_ration = right_knee_ratio;
            back_ration = left_knee_ratio;
        }
        else{
            front_ration = left_knee_ratio;
            back_ration = right_knee_ratio;
        }
    }
    else{
        if(is_face_toward){
            front_ration = left_knee_ratio;
            back_ration = right_knee_ratio;
        }
        else{
            front_ration = right_knee_ratio;
            back_ration = left_knee_ratio;
        }
    }


    LOGD("Face toward: %d \n front knee_ration: %f,   back knee_ration: %f,  hip ration:  %f,   min_agnle: %f", face_toward(kpts_sframe), front_ration, back_ration, hip_ration, action_recs[recid].min_agnle);
    if (!action_recs[recid].mark) {
        action_recs[recid].history.push_back(kpts_sframe);
        action_recs[recid].min_agnle = front_ration < action_recs[recid].min_agnle?front_ration:action_recs[recid].min_agnle;
    }

    if (0 < hip_ration && hip_ration < 25 && action_recs[recid].mark) {
        action_recs[recid].latency += 1;
        if (action_recs[recid].latency == 6) {
            action_recs[recid].mark = false;
            action_recs[recid].latency = 0;
            action_recs[recid].min_agnle = INF;
        }
    } else if (front_ration < 150 && front_ration > action_recs[recid].min_agnle && !(action_recs[recid].mark)) {
        action_recs[recid].latency += 1;
        if (action_recs[recid].latency == 6) {
            action_recs[recid].action_count += 1;
            action_recs[recid].mark = true;
            action_recs[recid].min_agnle = action_recs[recid].min_agnle < 90?90:action_recs[recid].min_agnle;
            action_recs[recid].score = get_score(agcn_, action_recs[recid].history, lunge, kpts, 1 - (action_recs[recid].min_agnle - 90) / 90).smiliarity;
            action_recs[recid].history.clear();
            action_recs[recid].latency = 0;
        }
    }
    return action_recs[recid].action_count;

}
/*************************************************************************/



/**
 * 此处添加动作选择
 * 根据动作id
 * @param results_kpts
 * @param h
 * @param actionid
 * @param recid
 * @return
 */

int single_action_check(std::shared_ptr<AGCN> &agcn_, std::vector<float> &results_kpts, float h, int actionid,
                    int recid) {
    //相对路径
    if (results_kpts.size() == 0 || actionid > 4) {
        return action_recs[recid].action_count;
    }
    // 向上
    if (actionid == 1) {
        return check_prelude(agcn_, results_kpts, recid);
    }
        // 向后
    else if (actionid == 2) {
        return check_backward_extension(agcn_, results_kpts, recid);
    }
        // 深蹲
    else if (actionid == 3) {
        return check_bow_stance(agcn_, results_kpts, recid);
    }// 弓步
    else if (actionid == 4) {
        return check_lunge(agcn_, results_kpts, recid);
    } else {
        return 0;
    }
}

void double_action_check(std::vector<RESULT_KEYPOINT> &results_kpts, std::vector<RESULT> &results,
                         int actionid, int imgw) {
    std::vector<float> left, right;
    if (results_kpts.size() == 0) {
        return;
    }
    float rh = 0;
    float lh = 0;
    float maxconf = -1;
    float maxid = -1;
    for (int i = 0; i < results_kpts.size(); i++) {
        if (left.size() > 0 || right.size() > 0) {
            break;
        }
        for (int j = 0; j < results_kpts[i].num_joints; j++) {
            if (results_kpts[i].keypoints[j * 3] > 0.8) {
                if (results_kpts[i].keypoints[j * 3 + 1] < imgw / 2.) {
                    left = results_kpts[i].keypoints;
                    lh = results[i].h;
                    if (results_kpts.size() > 1) {
                        right = results_kpts[1 - i].keypoints;
                        rh = results[1 - i].h;
                    }
                } else {
                    right = results_kpts[i].keypoints;
                    rh = results[1 - i].h;
                    if (results_kpts.size() > 1) {
                        left = results_kpts[1 - i].keypoints;
                        lh = results[i].h;
                    }
                }
                break;
            } else if (results_kpts[i].keypoints[j * 3] > maxconf) {
                maxconf = results_kpts[i].keypoints[j * 3];
                maxid = i;
            }
        }
    }
    if (left.empty() && right.empty() &&
        results_kpts[maxid].keypoints[maxconf * 3 + 1] < imgw / 2.) {
        left = results_kpts[maxid].keypoints;
        lh = results[maxid].h;
        if (results_kpts.size() > 1) {
            right = results_kpts[1 - maxid].keypoints;
            rh = results[1 - maxid].h;
        }
    } else if (left.empty() && right.empty()) {
        right = results_kpts[maxid].keypoints;
        rh = results[1 - maxid].h;
        if (results_kpts.size() > 1) {
            left = results_kpts[1 - maxid].keypoints;
            lh = results[1 - maxid].h;
        }
    }

//  if (!left.empty()) {
//    single_action_check(modeldir, left, lh, actionid, 0);
//  }
//  if (!right.empty()) {
//    single_action_check(modeldir, right, rh, actionid, 1);
//  }
}

