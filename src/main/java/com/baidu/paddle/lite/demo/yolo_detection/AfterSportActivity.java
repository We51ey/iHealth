package com.baidu.paddle.lite.demo.yolo_detection;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.paddle.lite.demo.utils.EChartOptionUtil;
import com.baidu.paddle.lite.demo.utils.EChartView;
import com.baidu.paddle.lite.demo.component.MyCallBack;
import com.baidu.paddle.lite.demo.component.NetClient;
import com.baidu.paddle.lite.demo.component.ToastShow;
import com.baidu.paddle.lite.demo.bean.User;


public class AfterSportActivity extends Activity implements View.OnClickListener{

    private int pose;
    private int[] action_id;
    private Button btnAfterReplay;
    private Button btnAfterHome;
    private Button btnUpLoadData;
    private EChartView lineChart;
    private ToastShow myToast;
    private String action_Count;
    private String ave_Score;
    private String max_Score;
    private String min_Score;
    private boolean flag;



    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_aftersport);
        initView();
    }

    private void  initView() {
        flag=false;

        myToast =new ToastShow(AfterSportActivity.this);
        lineChart = findViewById(R.id.lineChart);
        btnAfterReplay = findViewById(R.id.after_replay);//再来一次
        btnAfterHome = findViewById(R.id.after_home);//返回主页
        btnUpLoadData = findViewById(R.id.upload_data);//上传数据
        btnAfterReplay.setOnClickListener(this);
        btnAfterHome.setOnClickListener(this);
        btnUpLoadData.setOnClickListener(this);


        Intent i=getIntent();
        pose=i.getIntExtra("i",1);

        action_id=getResources().getIntArray(R.array.pose_action_id);
        action_Count=i.getStringExtra("actionCount");
        ave_Score = i.getStringExtra("aveScore");
        max_Score=i.getStringExtra("maxScore");
        min_Score=i.getStringExtra("minScore");

        TextView c = findViewById(R.id.total_count_text);
        TextView s=findViewById(R.id.final_score);

        c.setText("  总计：" + action_Count);

        s.setText("  得分："+ave_Score);

        TextView maxScoreText=findViewById(R.id.max_score);
        TextView minScoreText=findViewById(R.id.min_score);
        maxScoreText.setText("  最高："+(max_Score==null?0:max_Score));
        minScoreText.setText("  最低："+(min_Score==null?0:min_Score));
        lineChart.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //最好在h5页面加载完毕后再加载数据，防止html的标签还未加载完成，不能正常显示
                float[] y_temp = i.getFloatArrayExtra("scoreList");
                System.out.println("11111111111111111111111111");
                System.out.println(y_temp!=null);
                if(y_temp!=null){
                    Object[] x=new Object[y_temp.length];
                    Object[] y=new Object[y_temp.length];
                    for (int i=0;i<y_temp.length;i++){
                        x[i]=i+1;
                        y[i]=y_temp[i];
                    }
                    refreshLineChart(x,y);
                }else {
                    refreshLineChart(null,null);
                }
            }
        });
    }

    public void uploadPracticeData(){
        if(!flag) {
            String basePath = "http://110.40.168.206:5000/uploadPracticeData";
            String data = "?account=" + User.getInstance().getAccount() + "&actionId=" + pose + "&actionCount=" + action_Count + "&maxScore=" + max_Score + "&minScore=" + min_Score + "&aveScore=" + ave_Score;
            NetClient.getNetClient().callNet(basePath + data, new MyCallBack() {
                @Override
                public void onFailure(int code) {
                    System.out.println(code);
                    // 服务器连接失败
                    myToast.toastShow("ServerError");
                }

                @Override
                public void onResponse(String json) {
                    System.out.println("json:" + json);
                    String result = json;
                    if (result.equals("success")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myToast.toastShow("上传成功");
                            }
                        });
                        flag=true;
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myToast.toastShow("上传失败");
                            }
                        });
                    }
                }
            });
        }else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myToast.toastShow("已经上传过了");
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()){
            case R.id.after_replay:
                i = new Intent(AfterSportActivity.this, BeforeSportActivity.class);
                i.putExtra("pose", action_id[pose]);
                i.putExtra("i", pose);
                AfterSportActivity.this.finish();
                startActivity(i);
                break;
            case R.id.after_home:
                i = new Intent(AfterSportActivity.this, SelectActivity.class);
                AfterSportActivity.this.finish();
                startActivity(i);
                break;
            case R.id.upload_data:
                uploadPracticeData();
                break;


        }
    }

    /**
     * 刷新折线图
     */
    private void refreshLineChart(Object[] x, Object[] y){
        lineChart.refreshEchartsWithOption(EChartOptionUtil.getLineChartOptions(x, y));
    }


    private void upLoadData(){

    }
}
