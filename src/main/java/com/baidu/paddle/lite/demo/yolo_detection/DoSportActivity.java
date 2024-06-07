package com.baidu.paddle.lite.demo.yolo_detection;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.app.Activity;
import android.widget.*;

import com.baidu.paddle.lite.demo.common.CameraSurfaceView;
import com.baidu.paddle.lite.demo.common.Utils;
import com.baidu.paddle.lite.demo.utils.MyTTS;

import java.util.ArrayList;

import java.util.Collections;
import java.util.Locale;



public class DoSportActivity extends Activity implements View.OnClickListener, CameraSurfaceView.OnTextureChangedListener, Chronometer.OnChronometerTickListener{
    CameraSurfaceView svPreview;
    String savedImagePath = "";
    Native predictor;
    boolean isStart;
    //上方指示器
    private long recordingTime;
    //主计时器
    private Chronometer time;
    private TextView count;
    private TextView score;
    //运行状态变量
    private boolean playing;
    private boolean pausing;
    //提示工具
    private Toast myToast;
    private TextView overlayText;

    //动作计数
    private int actionCount;
    //动作 分数
    private float actionScore;

    private float aveScore;

    private int tmp;

    private int[] action_id;
    //动作代码
    private int pose;

    private TextView countShow;

    private Button btnPause;
    private Button btnStop;
    private Button btnRemake;

    private View btnSwitch;
    private int countSelector=0;


    //分数记录
    private ArrayList<Float> scoreList;
    //定时器


    //TTS
    private MyTTS myTTS;


    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_dosport);
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
        predictor.setAssetManager(getAssets());

        start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Open camera until the permissions have been granted
        checkAndUpdateSettings();
        if (!checkAllPermissions()) {
            svPreview.disableCamera();
        }
        svPreview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        svPreview.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(String.valueOf(actionCount), "onDestroy:--------------------------- ");
        if (predictor != null) {
            predictor.reset();
            predictor.release();
            super.onDestroy();
        }
        if(svPreview!=null){
            svPreview.releaseCamera();
        }
    }

    /**
     * 初始化视图函数
     */
    @SuppressLint("WrongViewCast")
    private void initView() {

        predictor = new Native();

        playing = false;
        pausing = false;
        actionCount = 0;
        actionScore = 0;
        aveScore = 0;
        tmp = 0;
        recordingTime = 0;
        scoreList = new ArrayList();

        myTTS = new MyTTS(this);

        //获取上个 activity 传递的值 动作id
        pose = getIntent().getIntExtra("i", 1);
        countSelector = getIntent().getIntExtra("countSelector",0);
        //获取动作id
        action_id = getResources().getIntArray(R.array.pose_action_id);

        //获取动作名字 title
        String[] title = getResources().getStringArray(R.array.pose_name);
        //获取动作提示
        String[] tips = getResources().getStringArray(R.array.pose_tips);
        //摄像头表层
        //即打开摄像头显示
        svPreview = findViewById(R.id.sv_preview);
        //监听器
        svPreview.setOnTextureChangedListener(this);

        //时间 动作 分数 计算
        time = (Chronometer) findViewById(R.id.time_count);
//        time.setFormat("%s");
        count = findViewById(R.id.count_count);
        score = findViewById(R.id.score_count);

        //所有按钮
        btnPause = findViewById(R.id.pause);//2.暂停
        btnStop = findViewById(R.id.stop);//2.停止
        btnRemake = findViewById(R.id.remake);//3.重开
        btnSwitch = findViewById(R.id.btn_switch);//4.切换摄像头

        //每个按钮设置监听事件
        btnPause.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnRemake.setOnClickListener(this);
        btnSwitch.setOnClickListener(this);

        overlayText = findViewById(R.id.overlay_text);
        countShow = findViewById(R.id.count_show);
        playing = false;
        pausing = true;
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
     * 开始按钮
     * 倒计时等参数设置
     */
    private void start() {
        actionCount = 0;
        actionScore = 0;
        final String[] hint = {"训练开始!",  "1",  "2",  "3","","准备好了吗"};
        //
        //设置按钮失灵
        disableBtn(btnPause);
        disableBtn(btnStop);
        disableBtn(btnRemake);


        new CountDownTimer(9000, 1500) {
            //设置倒计时文字 间隔 1s
            @Override
            public void onTick(long l) {
                overlayText.setText(hint[(int) Math.floor(l / 1500)]);

                myTTS.mySpeak(hint[(int) Math.floor(l / 1500)]);

            }
            //倒计时结束
            @Override
            public void onFinish() {
                overlayText.setText("");
                predictor.reset();
                actionCount = 0;
                actionScore = 0;
                tmp = 0;
                aveScore = 0;
                isStart=true;
                count.setText("0");
                score.setText("0.0");
                time.setBase(SystemClock.elapsedRealtime() - recordingTime);
                time.start();
                enableBtn(btnPause);
                enableBtn(btnStop);
                enableBtn(btnRemake);
            }
        }.start();
        playing = true;
        pausing = false;
    }

    /**
     * 按钮禁用
     */
    private void disableBtn(Button btn) {
        btn.setBackground(getResources().getDrawable(R.drawable.btn_disable));
        btn.setEnabled(false);
    }


    /**
     * 按钮启用
     */
    private void enableBtn(Button btn) {
        btn.setBackground(getResources().getDrawable(R.drawable.btn_selector));
        btn.setEnabled(true);
    }

    /**
     * 停止按钮
     */
    private void stop() {
        onDestroy();
        recordingTime = 0;
        time.stop();
        aveScore = actionCount != 0 ? aveScore /= actionCount : 0;
        Intent i = new Intent(DoSportActivity.this, AfterSportActivity.class);

        float[] score_list=new float[scoreList.size()];
        for (int j=0;j<scoreList.size();j++){
            score_list[j]=scoreList.get(j);
        }
        i.putExtra("scoreList", score_list);
        Collections.sort(scoreList);
        i.putExtra("actionCount", String.valueOf(actionCount));
        i.putExtra("aveScore", String.valueOf(aveScore));
        i.putExtra("maxScore", String.valueOf(scoreList.size() != 0 ? scoreList.get(scoreList.size() - 1) : 0));
        i.putExtra("minScore", String.valueOf(scoreList.size() != 0 ? scoreList.get(0) : 0));
        i.putExtra("pose", action_id[pose]);
        i.putExtra("i", pose);

        actionCount=0;
        actionScore=0;
        isStart=false;
        finish();
        startActivity(i);
    }

    /**
     * 暂停按钮
     */
    private void pause() {
//        Button btnPause = findViewById(R.id.pause);
        if (playing) {
            pausing = !pausing;
            if (pausing) {
                disableBtn(btnStop);
                disableBtn(btnRemake);
                btnPause.setText("恢复");
                overlayText.setText("暂停中");
                time.stop();
                recordingTime = SystemClock.elapsedRealtime() - time.getBase();
                svPreview.releaseCamera();
            } else {
                enableBtn(btnStop);
                enableBtn(btnRemake);
                btnPause.setText("暂停");
                overlayText.setText("");
                time.setBase(SystemClock.elapsedRealtime() - recordingTime);
                time.start();
                svPreview.openCamera();
            }
        }
    }

    /**
     * 重开按钮
     */
    private void remake() {
        onDestroy();
        finish();
        actionCount = 0;
        actionScore = 0;
        tmp = 0;
        aveScore = 0;
        count.setText("0");
        score.setText("0.0");
        Intent i = new Intent(DoSportActivity.this, BeforeSportActivity.class);
        i.putExtra("pose", action_id[pose]);
        i.putExtra("i", pose);
        startActivity(i);
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
            case R.id.pause:
                pause();
                break;
            case R.id.stop:
                stop();
                break;
            case R.id.remake:
                remake();
                break;
            case R.id.btn_switch:
                svPreview.switchCamera();
                break;
        }
    }


    @Override
    public boolean onTextureChanged(int inTextureId, int outTextureId, int textureWidth, int textureHeight) {
        synchronized (this) {
            savedImagePath = DoSportActivity.this.savedImagePath;
        }
        boolean modified = predictor.process(inTextureId, outTextureId, textureWidth, textureHeight, savedImagePath, action_id[pose], true);
        if (!savedImagePath.isEmpty()) {
            synchronized (this) {
                DoSportActivity.this.savedImagePath = "";
            }
        }

        int[] result = predictor.getActionCountAndScore();
        actionCount = result[0];
        actionScore = result[1];
        if (actionCount != 0 && tmp != actionCount && isStart) {
            aveScore += actionScore;
            //监听分数和计数 ，在主线程中修改UI
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    score.setText(actionCount != 0 ? String.valueOf(actionScore) : "0.0");
                    count.setText(actionCount != 0 ? String.valueOf(actionCount) : "0.0");
                    String color = null;
                    if (actionCount != 0) {
                        if(actionScore==0){
                            score.setTextColor(Color.rgb(255, 255, 51));
                        }else if (actionScore < 60) {
                            score.setTextColor(Color.rgb(255, 66, 51));
                        } else if (actionScore < 75) {
                            score.setTextColor(Color.rgb(255, 165, 0));
                        } else {
                            score.setTextColor(Color.rgb(50, 205, 50));
                        }
                    }
                }
            });
            myTTS.mySpeak(String.valueOf((int) actionScore));
            scoreList.add(actionScore);
        }
        tmp = actionCount;
        if (actionCount == countSelector) {
            stop();
        }
        return modified;
    }

    public void checkAndUpdateSettings() {
        if (SettingsActivity.checkAndUpdateSettings(this)) {
            String realModelDir = getCacheDir() + "/" + SettingsActivity.modelDir;
            Utils.copyDirectoryFromAssets(this, SettingsActivity.modelDir, realModelDir);
            String realLabelPath = getCacheDir() + "/" + SettingsActivity.labelPath;
            Utils.copyFileFromAssets(this, SettingsActivity.labelPath, realLabelPath);
            predictor.init(
                    realModelDir,
                    realLabelPath,
                    SettingsActivity.cpuThreadNum,
                    SettingsActivity.cpuPowerMode,
                    SettingsActivity.inputWidth,
                    SettingsActivity.inputHeight,
                    SettingsActivity.inputMean,
                    SettingsActivity.inputStd,
                    SettingsActivity.scoreThreshold);
        }
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
            new AlertDialog.Builder(DoSportActivity.this)
                    .setTitle("Permission denied")
                    .setMessage("Click to force quit the app, then open Settings->Apps & notifications->Target " +
                            "App->Permissions to grant all of the permissions.")
                    .setCancelable(false)
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DoSportActivity.this.finish();
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


    /**
     * 计时器回调函数
     * @param chronometer
     */
    @Override
    public void onChronometerTick(Chronometer chronometer) {
    }



}