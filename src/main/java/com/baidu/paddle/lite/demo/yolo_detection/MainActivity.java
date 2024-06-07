package com.baidu.paddle.lite.demo.yolo_detection;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.baidu.paddle.lite.demo.bean.HeroBean;
import com.baidu.paddle.lite.demo.common.MsAdapter;
import com.baidu.paddle.lite.demo.component.MyDialog;
import com.baidu.paddle.lite.demo.bean.User;

import java.util.ArrayList;


public class MainActivity extends Activity implements View.OnClickListener,AdapterView.OnItemSelectedListener{

    private Button start;
    private ImageView userImg;
    private TextView userName;
    private Spinner process;
    private Spinner course;
    private Context mContext;
    private String[] processNameList;
    private String[] courseNameList;


    //判断是否为刚进去时触发 onItemSelected 的标志
    private boolean one_selected = false;
    private boolean two_selected = false;
    private ArrayList<HeroBean> processData = null;
    private ArrayList<HeroBean> courseData = null;
    private MsAdapter processAdadpter = null;
    private MsAdapter courseAdadpter = null;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_main);
        initView();

    }

    private void bindViews() {
        process = (Spinner) findViewById(R.id.process_selector);
        course = (Spinner) findViewById(R.id.course_selector);

        // 添加选择器绑定
        processNameList =getResources().getStringArray(R.array.process_names);
        courseNameList =getResources().getStringArray(R.array.course_names);

        processData.add(new HeroBean(R.drawable.bed, processNameList[0]));
        processData.add(new HeroBean(R.drawable.outbed, processNameList[1]));
        processData.add(new HeroBean(R.drawable.hospital, processNameList[2]));

        courseData.add(new HeroBean(R.drawable.arm, courseNameList[0]));
        courseData.add(new HeroBean(R.drawable.leg, courseNameList[1]));
        courseData.add(new HeroBean(R.drawable.neck, courseNameList[2]));
        courseData.add(new HeroBean(R.drawable.waist, courseNameList[3]));



        processAdadpter = new MsAdapter<HeroBean>(processData,R.layout.spinner_item_process) {
            @Override
            public void bindView(MsAdapter.ViewHolder holder, HeroBean obj) {
                holder.setImageResource(R.id.process_icon,obj.getIcon());
                holder.setText(R.id.process_name, obj.getName());
            }
        };

        courseAdadpter = new MsAdapter<HeroBean>(courseData,R.layout.spinner_item_course) {
            @Override
            public void bindView(MsAdapter.ViewHolder holder, HeroBean obj) {
                holder.setImageResource(R.id.course_icon,obj.getIcon());
                holder.setText(R.id.course_name, obj.getName());
            }
        };

        process.setAdapter(processAdadpter);
        process.setOnItemSelectedListener(this);

        course.setAdapter(courseAdadpter);
        course.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }



    private void  initView() {
        mContext = MainActivity.this;
        processData =new ArrayList<HeroBean>();
        courseData=new ArrayList<HeroBean>();

        start = findViewById(R.id.btn_entry);
        userImg =findViewById(R.id.userImg); // 用户头像
        userName = findViewById(R.id.userName); // 用户名字

        start.setOnClickListener(this);
        userImg.setOnClickListener(this);
        initUser();
        bindViews();
    }

    /**
     * 初始化用户
     */
    public void initUser(){
        //如果已经登录过，则直接获取用户信息
        if (User.getInstance().isLogin()) {
            userName.setText(User.getInstance().getAccount()); // 设置用户名字
        }
    }






    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }



    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()){
            case R.id.btn_entry:
                i = new Intent(MainActivity.this, SelectActivity.class);
                startActivity(i);
                break;
            case R.id.userImg:
                showDialog();
                break;
        }
    }


    public void logout(){
        if(User.getInstance().isLogin()){
            User.getInstance().setLogin(false);
        }
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        finish();
        startActivity(i);
    }

    /**
     * 显示对话框
     */
    private void showDialog() {
        MyDialog dialog = new MyDialog(MainActivity.this, R.style.mdialog,
                new MyDialog.OncloseListener() {
                    @Override
                    public void onClick(boolean confirm) {
                        if (confirm) {
                            // TODO: 切换用户/退出登录
                            logout();
                        } else {
                            // TODO:
                        }
                    }
                });
        dialog.show();
    }

}
