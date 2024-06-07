package com.baidu.paddle.lite.demo.yolo_detection;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.LinearLayout;

public class SelectActivity extends Activity implements View.OnClickListener {
    //动作选择 数组
    private String[] pose_name;
    //动作照片
    private String[] pose_image;
    //动作id
    private int[] pose_action_id;
    //动作提示
    private String[] pose_tips;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_select);
        initView();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initView() {
        pose_name = getResources().getStringArray(R.array.pose_name);
        pose_image = getResources().getStringArray(R.array.pose_image);
        pose_action_id = getResources().getIntArray(R.array.pose_action_id);
        pose_tips=getResources().getStringArray(R.array.pose_tips);
        //线性布局
        LinearLayout poseSelect = findViewById(R.id.pose_select);
        //为xml中配置的每一个动作配置 对应图片 按钮
        for (int i = 1; i < pose_name.length; i++) {
            PoseButton newPose = new PoseButton(getApplicationContext());
            final int t = i;
            newPose.setPoseName(pose_name[t]);
            int poseImageId = getResources().getIdentifier(pose_image[t], "drawable", getPackageName());
            newPose.setPoseImage(poseImageId);
            newPose.setPoseTip(pose_tips[t]);
            //点击事件 选择某一个动作
            newPose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent myIntent = new Intent(SelectActivity.this, BeforeSportActivity.class);
                    //为下一个activity 传值 动作id
                    myIntent.putExtra("pose", pose_action_id[t]);
                    myIntent.putExtra("i", t);
                    startActivity(myIntent);
                }
            });
            poseSelect.addView(newPose);

        }
        View btn_back = findViewById(R.id.btn_back);
        View btnHome = findViewById(R.id.btn_home);
        btn_back.setOnClickListener(this);
        btnHome.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
            case R.id.btn_home:
                startActivity(new Intent(SelectActivity.this,MainActivity.class));
                SelectActivity.this.finish();
                break;
            default:
        }
    }
}
