

#include "DTW.h"
#include "global.h"

#define INF 1e20
#define PI 3.141592653





DTW_result get_score(std::shared_ptr<AGCN> &agcn_, std::vector<std::vector<float>> A, std::vector<std::vector<float>> B, std::vector<int> kpts, float max_angle){
    DTW_result res;
    float alpha = 0.3;
    float beta = 0.1;
    //翻转骨骼点
    std::vector<std::vector<float>> flip_X(A.size(), std::vector<float>(A[0].size(), 0));
    if(face_toward(A[A.size() / 2]) != face_toward(B[B.size() / 2])){
        for(int i = 0; i < A.size(); i++){
            flip_X[i] = keypoint_flip(A[i]);
        }
        A = flip_X;
    }
    LOGD("Flip Success!!!!");
    // 采用活动骨骼点
    int use_keypoints = kpts.size();
    std::vector<std::vector<float>> new_X(A.size(), std::vector<float>(use_keypoints * 3, 0));
    std::vector<std::vector<float>> new_Y(B.size(), std::vector<float>(use_keypoints * 3, 0));
    for(int j = 0; j < use_keypoints; j++){
        for(int i = 0; i < A.size(); i++){
            new_X[i][j * 3 + 0] = A[i][kpts[j] * 3 + 0];
            new_X[i][j * 3 + 1] = A[i][kpts[j] * 3 + 1] / 720;
            new_X[i][j * 3 + 2] = A[i][kpts[j] * 3 + 2] / 1280;
        }
        for(int i = 0; i < B.size(); i++){
            new_Y[i][j * 3 + 0] = B[i][kpts[j] * 3 + 0];
            new_Y[i][j * 3 + 1] = B[i][kpts[j] * 3 + 1];
            new_Y[i][j * 3 + 2] = B[i][kpts[j] * 3 + 2];
        }
    }
    LOGD("Part Keypoint Success!!!!");
//    for(int i = 0; i < A.size(); i++){
//        for(int j = 0; j < A[0].size(); j++){
//            LOGD("%f", A[i][j]);
//        }
//    }
    A = new_X;
    B = new_Y;
    LOGD("A.size: %d, B.size: %d", A.size(), B.size());
    float process_score = pruneddtw(agcn_, A, B, getSimilarity, 1);
    LOGD("DTW Success !!!");

    float angle_score_ = getSimilarity(A[A.size() - 1], B[B.size() - 1]);
    LOGD("GetSimilarity Success !!!");
    std::vector<float> TMPA = A[A.size() - 1];
    std::vector<float> TMPB = B[B.size() - 1];

//    float angle_a = angle_score({TMPA[1 * 3 + 1] - TMPA[0 * 3 + 1],
//                                 TMPA[1 * 3 + 2] - TMPA[0 * 3 + 2]}, {TMPA[0 * 3 + 1] - TMPA[3 * 3 + 1],
//                                                                      TMPA[0 * 3 + 2] - TMPA[3 * 3 + 2]});
//    float angle_b = angle_score({TMPB[1 * 3 + 1] - TMPB[0 * 3 + 1],
//                                 TMPB[1 * 3 + 2] - TMPB[0 * 3 + 2]});
//    LOGD("%f %f", angle_a, angle_b);
//    float max_angle = angle_score(A[A.size() - 1]);
    res.dtw_cost = process_score;

    LOGD("min_agnle %f", max_angle);
    if(max_angle != -1.0){
        res.smiliarity = process_score * alpha + angle_score_ *  beta + max_angle * ( 1 - alpha - beta);
    }
    else{
        res.smiliarity = process_score * 0.5 + angle_score_ * 0.5;
    }

    if(res.smiliarity > 0.9){
        res.smiliarity = 1;
    }

//    res.smiliarity = process_score * 0.5 + (max_angle / 180) * 0.5;//+ angle_score_ * 0.6;
    LOGD("%f  %f  %f  %f", process_score, angle_score_, max_angle, res.smiliarity);
//    res.max_angle = max_angle;
    return res;

}

float angle_score(std::vector<float> X, std::vector<float> Y){
    float vector1x = X[0];
    float vector2x = Y[0];
    float vector1y = X[1];
    float vector2y = Y[1];

    float t = (vector1x * vector2x + vector1y * vector2y) /
          (float)(sqrt(pow(vector1x, 2) + pow(vector1y, 2)) *
                  sqrt(pow(vector2x, 2) + pow(vector2y, 2)));


return acos(t) * (180 / PI);
}


float pruneddtw(std::shared_ptr<AGCN> &agcn_, std::vector<std::vector<float>> A, std::vector<std::vector<float>> B, float (*func)(std::vector<float>, std::vector<float>), float w) {

    int path_length = 0;

    float *cost;
    float *cost_prev;
    float *cost_tmp;
    int i,j,k;
    float x,y,z;

    int m = A.size();
    int r = floor(m*w);

    // Variables to implement the pruning - PrunedDTW
    int sc = 0, ec = 0, next_ec, lp; //lp stands for last pruning
    float UB = 0;
    bool foundSC, prunedEC;
    int iniJ;

    /// Instead of using matrix of size O(m^2) or O(mr), we will reuse two array of size O(m).
    cost = (float*)malloc(sizeof(float)*(m));
    cost_prev = (float*)malloc(sizeof(float)*(m));

    float *ub_partials;
    ub_partials = (float*)malloc(sizeof(float)*(m));

    for(j=0; j<m; j++) {

        cost[j]=INF;
        cost_prev[j]=INF;

        if (j==0)
            ub_partials[m-j-1] = func(A[m-j-1],B[m-j-1]);
        else
            ub_partials[m-j-1] = ub_partials[m-j] + func(A[m-j-1],B[m-j-1]);

    }
    UB = ub_partials[0];

    for (i=0; i<m; i++)
    {

        foundSC = false;
        prunedEC = false;
        next_ec = i+r+1;

        iniJ = fmax(0,fmax(i-r, sc));

        for(j=iniJ; j<=fmin(m-1,i+r); j++)
        {
            /// Initialize all row and column
            if ((i==0)&&(j==0))
            {
                cost[j]=func(A[0],B[0]);
//                LOGD("cost %f", cost[j]);
                foundSC = true;
                continue;
            }

            if (j==iniJ)                         y = INF;
            else                                 y = cost[j-1];
            if ((i==0)||(j==i+r)||(j>=lp))       x = INF;
            else                                 x = cost_prev[j];
            if ((i==0)||(j==0)||(j>lp))          z = INF;
            else                                 z = cost_prev[j-1];

            /// Classic DTW calculation
            cost[j] = fmin( fmin( x, y) , z) + func(A[i],B[j]);
//            path_length++;
            /// Pruning criteria
            if (!foundSC && cost[j] <= UB) {
                sc = j;
                foundSC = true;
            }

            if (cost[j] > UB) {
                if (j > ec) {
                    lp = j;
                    prunedEC = true;
                    break;
                }
            } else {
                next_ec = j+1;
            }

        }
        UB = ub_partials[i+1]+cost[i];

        /// Move current array to previous array.
        cost_tmp = cost;
        cost = cost_prev;
        cost_prev = cost_tmp;

        /// Pruning statistics update
        if(sc > 0)
            cost_prev[sc-1] = INF;

        if (!prunedEC)
            lp = i+r+1;

        ec = next_ec;

    }

    /// the DTW distance is in the last cell in the matrix of size O(m^2) or at the middle of our array.
    float final_dtw = cost_prev[m-1];
    free(cost);
    free(cost_prev);
//    final_dtw = final_dtw / (A.size() + B.size());
//    LOGD("%f %d", final_dtw, m - 1);

    return final_dtw / m;
}




//DTW_result dtw(std::shared_ptr<AGCN> &agcn_, std::vector<std::vector<float>> X, std::vector<std::vector<float>> Y,
//               float (*func)(std::vector<float>, std::vector<float>), std::vector<int> kpts,
//               int warp, float w, float s) {
//
//    //TODO: 分类，获取置信度
//
////    AGCN_RESULT res;
////    agcn_->Predict(X, &res);
//
//
////    LOGD("------------------------------------------------------%f",PRE[0][1]);
//    //翻转骨骼点
//    std::vector<std::vector<float>> flip_X(X.size(), std::vector<float>(X[0].size(), 0));
//    if(face_toward(X[X.size() / 2]) != face_toward(Y[Y.size() / 2])){
//        for(int i = 0; i < X.size(); i++){
//            flip_X[i] = keypoint_flip(X[i]);
////            LOGD("%f -  -------------------------------------------", flip_X[i][1]);
//        }
//        X = flip_X;
//    }
//
//    // 采用活动骨骼点
//    int use_keypoints = kpts.size();
//    std::vector<std::vector<float>> new_X(X.size(), std::vector<float>(use_keypoints * 3, 0));
//    std::vector<std::vector<float>> new_Y(Y.size(), std::vector<float>(use_keypoints * 3, 0));
//    for(int j = 0; j < use_keypoints; j++){
//        for(int i = 0; i < X.size(); i++){
//            new_X[i][j * 3 + 0] = X[i][kpts[j] * 3 + 0];
//            new_X[i][j * 3 + 1] = X[i][kpts[j] * 3 + 1];
//            new_X[i][j * 3 + 2] = X[i][kpts[j] * 3 + 2];
//        }
//        for(int i = 0; i < Y.size(); i++){
//            new_Y[i][j * 3 + 0] = Y[i][kpts[j] * 3 + 0];
//            new_Y[i][j * 3 + 1] = Y[i][kpts[j] * 3 + 1];
//            new_Y[i][j * 3 + 2] = Y[i][kpts[j] * 3 + 2];
//        }
//
//    }
////    LOGD("%d %d", X.size(), Y.size());
//    X = new_X;
//    Y = new_Y;
//
//
//
//
//
//    int r = X.size();
//    int c = Y.size();
//    std::vector<std::vector<float>> D0(r + 1, std::vector<float>(c + 1));
//    if (!isinf(w)) {
//        for (int i = 0; i < r + 1; i++)
//            for (int j = 0; j < c + 1; j++)
//                D0[i][j] = float('inf');
//        // std::vector<std::vector<float>> D0(r + 1, std::vector<float>(c + 1, float('inf')));
//        for (int i = 1; i < r + 1; i++) {
//            int start = std::max(1, int(i - w));
//            int end = std::min(c + 1, int(i + w + 1));
//            for (int j = start; j < end; j++) {
//                D0[i][j] = 0;
//            }
//        }
//    } else {
//        // std::vector<std::vector<float>> D0(r + 1, std::vector<float>(c + 1, 0));
//        for (int i = 0; i < r + 1; i++)
//            for (int j = 0; j < c + 1; j++)
//                D0[i][j] = float(0);
//        for (int i = 1; i < c + 1; i++) {
//            D0[0][i] = float('inf');
//            D0[i][0] = float('inf');
//        }
//    }
//    std::vector<std::vector<float>> D1(r, std::vector<float>(c, 0));
//    for (int i = 1; i < r + 1; i++) {
//        for (int j = 1; j < c + 1; j++)
//            D1[i - 1][j - 1] = D0[i][j];
//    }
//    for (int i = 0; i < r; i++)
//        for (int j = 0; j < c; j++)
//            if (isinf(w) || (std::max(0, int(i - w)) <= j && j <= std::min(c - 1, int(i + w))))
//                D1[i][j] = func(X[i], Y[j]);
//
//    std::vector<std::vector<float>> C(D1.begin(), D1.end());
//    std::vector<int> jrange(c);
//    for (int i = 0; i < c; i++)
//        jrange[i] = i;
//    for (int i = 1; i < r; i++) {
//        if (!isinf(w)) {
//            int start = std::max(1, int(i - w));
//            int end = std::min(c + 1, int(i + w + 1));
//            std::vector<int> jrange(end - start);
//            for (int j = 0; j < end - start; j++)
//                jrange[j] = start + j;
//        }
//        for (int j = 1; j < c; j++) {
//            std::vector<std::vector<float>> min_list(1, std::vector<float>(1, 0));
//            min_list[0] = {D0[i][j]};
//            for (int k = 1; k < int(warp) + 1; k++) {
//                int i_k = std::min(i + k, r);
//                int j_k = std::min(j + k, c);
//                min_list.push_back({D0[i_k][j] * s, D0[i][j_k] * s});
//            }
//            D1[i][j] = D1[i][j] + *max_element(min_list[0].begin(), min_list[0].end());
//        }
//    }
////    LOGD("PathpPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP");
//    Path path;
//    if (X.size() == 1) {
//        for (int i = 0; i < Y.size(); i++) {
//            path.x.push_back(0);
//        }
//
//        for (int i = 0; i < Y.size(); i++)
//            path.y.push_back(i);
//    } else if (Y.size() == 1) {
//        for (int i = 0; i < X.size(); i++)
//            path.x.push_back(i);
//        for (int i = 0; i < Y.size(); i++)
//            path.x.push_back(0);
//    } else {
//        path = _traceback(D0);
//    }
//    DTW_result result;
//    result.cost = D1[r - 1][c - 1];
////    LOGD("%f", result.cost);
//    result.cost_matrix = C;
//    result.acc_cost_matrix = D1;
//    result.path = path;
//    LOGD("-------------------%f----------------",result.cost);
////    result.cost = 1 - result.cost;
//    return result;
//
//}
//
//Path _traceback(std::vector<std::vector<float>> D) {
//    int i = D.size() - 2;
//    int j = D[0].size() - 2;
//    std::vector<int> p = {i};
//    std::vector<int> q = {j};
//    while (i > 0 || j > 0) {
//        i=i>0?i:0;
//        j=j>0?j:0;
//        std::vector<float> temp = {D[i][j], D[i][j + 1], D[i + 1][j]};
//        int tb = *min_element(temp.begin(), temp.end());
//        if (tb == 0) {
//            i -= 1;
//            j -= 1;
//        } else if (tb == 1) {
//            i -= 1;
//        } else {
//            j -= 1;
//        }
////        LOGD("TrackBack");
//        p.insert(p.begin(), i);
////        LOGD("TrackBack111");
//        q.insert(q.begin(), j);
////        LOGD("TrackBack222222222");
//    }
////    LOGD("1111111111111111while-----------------------------");
//    Path path;
////    LOGD("2222222222222222while-----------------------------");
//    path = {p, q};
////    LOGD("3333333333333333while-----------------------------");
//    return path;
//}


float getMold(const std::vector<float>& vec){   //求向量的模长
    int n = vec.size();
    float sum = 0.0;
    for (int i = 0; i<n; ++i)
        sum += vec[i] * vec[i];
    return sqrt(sum);
}


float getSimilarity(std::vector<float> lhs, std::vector<float> rhs) {

    int n = lhs.size();
//    LOGD("lhs.size : %d,,,,,  rhs.size %d", lhs.size(), rhs.size());
    assert(n == rhs.size());
    float tmp = 0.0;  //内积
    for (int i = 0; i < n; ++i)
        tmp += lhs[i] * rhs[i];

    return tmp / (getMold(lhs) * getMold(rhs));


}

float Eucildean_Distance(std::vector<float> x, std::vector<float> y){
     float square = 0;
     for(int i = 0; i < x.size(); i++)
         square += pow(x[i] - y[i], 2);
     return sqrt(square / x.size());
}

// float Eucildean_Distance(float x, float y){
//     return sqrt(pow(x - y, 2));
// }






//int main(void) {
//
//    std::vector <std::vector<float>> x = {{0, 0},
//                                {0, 1},
//                                {1, 1},
//                                {1, 2},
//                                {2, 2},
//                                {4, 3},
//                                {2, 3},
//                                {1, 1}};
//    std::vector <std::vector<float>> y = {{1, 0},
//                                {1, 1},
//                                {2, 1},
//                                {4, 3},
//                                {4, 3},
//                                {2, 3},
//                                {3, 1}};
//
//    DTW_result result = dtw(x, y, getSimilarity);
//    cout << result.cost << endl;
//
//    return 0;
//}


int face_toward(std::vector<float> &kpts_sframe) {
    float nose = kpts_sframe[1];
    float center = (kpts_sframe[5 * 3 + 1] + kpts_sframe[6 * 3 + 1]) / 2;
    return nose > center ? 1 : 0;
}





std::vector<float> keypoint_flip(std::vector<float> &kpts_sframe){
    std::vector<float> kpts_sframe_flip;
    std::vector<int> flip_permutation = {0, 2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11, 14, 13, 16, 15};
    for (int i = 0; i < kpts_sframe.size(); i += 3) {
    kpts_sframe_flip.push_back(kpts_sframe[flip_permutation[i / 3] * 3]);
    kpts_sframe_flip.push_back(kpts_sframe[flip_permutation[i / 3] * 3 + 1]);
    kpts_sframe_flip.push_back(kpts_sframe[flip_permutation[i / 3] * 3 + 2]);
    }
    for (int i = 0; i < kpts_sframe_flip.size(); i += 3) {
    kpts_sframe_flip[i + 1] = 720 - kpts_sframe_flip[i + 1];
    }
    return kpts_sframe_flip;
}

