package com.baidu.paddle.lite.demo.yolo_detection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.baidu.paddle.lite.demo.common.CameraSurfaceView;
import com.baidu.paddle.lite.demo.common.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class BeforeSportActivity extends Activity implements View.OnClickListener,  SeekBar.OnSeekBarChangeListener {

    //动作提示
    private TextView tip;

    private int pose;
    private int[] action_id;
    //计数选择器
    private int countSelector = 15;
    private TextView countShow;
    private SeekBar countPicker;
    private Button btnStart;
    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_beforesport);
        //初始化设置
        initSettings();
        //初始化视图
        initView();

        //检测用户是否授权了某个权限
        //如果未授权
        if (!checkAllPermissions()) {
            //申请权限
            requestAllPermissions();
        }
    }


    /**
     * 初始化视图函数
     */
    @SuppressLint("WrongViewCast")
    private void initView() {

        //获取动作名字 title
        String[] title = getResources().getStringArray(R.array.pose_name);
        //获取动作提示
        String[] tips = getResources().getStringArray(R.array.pose_tips);
        countShow = findViewById(R.id.count_show);
        //活动条
        countPicker = findViewById(R.id.count_picker);
        countPicker.setOnSeekBarChangeListener(this);

        //所有按钮
        btnStart = findViewById(R.id.start); // 1.确定 开始按钮
        View btnBack = findViewById(R.id.btn_back);//5.右上角返回上一级
        View btnHome = findViewById(R.id.btn_home);//5.右上角回到主页

        //每个按钮设置监听事件
        btnStart.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnHome.setOnClickListener(this);

        TextView poseTitle = findViewById(R.id.pose_name);
        //设置动作名字
        //获取上个 activity 传递的值 动作id
        pose = getIntent().getIntExtra("i", pose);
        System.out.println("----------------------------"+pose);
        //获取动作id
        action_id = getResources().getIntArray(R.array.pose_action_id);

        //获取动作名字 title
        title = getResources().getStringArray(R.array.pose_name);
        //获取动作提示
        tips = getResources().getStringArray(R.array.pose_tips);
        poseTitle.setText(title[pose]);
        //动作提示
        tip = findViewById(R.id.tip);
        tip.setText(tips[pose]);

        //选择演示视频  todo 修改成动态
        String uri = "android.resource://" + getPackageName() + "/";
        if (pose == 1) {
            uri += R.raw.pose_a_single;
//            svPreview.setBackgroundResource(R.drawable.bg_video_1);
        } else if (pose == 2) {
            uri += R.raw.pose_b_single;
//            svPreview.setBackgroundResource(R.drawable.bg_video_2);
        } else if (pose == 3) {
            uri += R.raw.pose_c_single;
//            svPreview.setBackgroundResource(R.drawable.bg_video_3);
        } else if (pose == 4) {
            uri += R.raw.pose_d_single;
//            svPreview.setBackgroundResource(R.drawable.bg_video_4);
        }
        //演示视频
        VideoView sampleVideo = findViewById(R.id.sample_video);
        sampleVideo.setVideoPath(uri);//视频路径
        sampleVideo.setVideoURI(Uri.parse(uri));
        // 设置视频监听器
        sampleVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setVolume(0f, 0f);//设置左右声道音量
                mediaPlayer.setLooping(true);//设置循环播放
            }
        });
        //视频播放
        sampleVideo.start();
    }

    /**
     * 初始化 此activity 设置
     */
    public void initSettings() {
        //存储系统的配置信息
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
        SettingsActivity.resetSettings();
    }


    /**
     * 点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.start:
                i=new Intent(BeforeSportActivity.this,DoSportActivity.class);
                i.putExtra("i", pose);
                i.putExtra("countSelector",countSelector);
                finish();
                startActivity(i);
                break;
            case R.id.btn_home:
                i = new Intent(BeforeSportActivity.this, MainActivity.class);
                finish();
                startActivity(i);
                break;
            case R.id.btn_back:
                i = new Intent(BeforeSportActivity.this, SelectActivity.class);
                finish();
                startActivity(i);
                break;
        }
    }

    /**
     * 滑动条设置
     * @param seekBar
     * @param progressValue
     * @param fromUser
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
        countSelector = progressValue;
        countShow.setText(countSelector + "个");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }



    /**
     * 权限申请结果回调函数
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(BeforeSportActivity.this)
                    .setTitle("Permission denied")
                    .setMessage("Click to force quit the app, then open Settings->Apps & notifications->Target " +
                            "App->Permissions to grant all of the permissions.")
                    .setCancelable(false)
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BeforeSportActivity.this.finish();
                        }
                    }).show();
        }
    }

    /**
     * 申请权限注册
     */
    private void requestAllPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA}, 0);
        System.out.println("权限申请");
    }

    /**
     * 权限检查
     * @return
     */
    private boolean checkAllPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

}
