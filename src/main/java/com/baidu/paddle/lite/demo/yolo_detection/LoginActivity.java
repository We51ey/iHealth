package com.baidu.paddle.lite.demo.yolo_detection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.paddle.lite.demo.component.MyCallBack;
import com.baidu.paddle.lite.demo.component.NetClient;
import com.baidu.paddle.lite.demo.component.ToastShow;
import com.baidu.paddle.lite.demo.bean.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends Activity implements View.OnClickListener{

    private Button login_btn;
    private Button visitorMode;
    private EditText account;
    private EditText passwd;
    ToastShow myToast;



    private TextView forgetPasswd;
    private TextView toRegister;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void  initView() {

        myToast =new ToastShow(LoginActivity.this);

        account=findViewById(R.id.account);
        passwd=findViewById(R.id.passwd);

        login_btn = findViewById(R.id.login); //登录
        visitorMode=findViewById(R.id.visitor);

        forgetPasswd=findViewById(R.id.forgetPasswd);
        toRegister=findViewById(R.id.ReturntoRegister);

        login_btn.setOnClickListener(this);
        visitorMode.setOnClickListener(this);
        forgetPasswd.setOnClickListener(this);
        toRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 登录
            case R.id.login:
                login();
                break;
            // 游客模式
            case R.id.visitor:
                visitor();
                break;
            case R.id.forgetPasswd:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        myToast.toastShow("联系管理员找回密码!");
                    }
                });
                break;
            case R.id.ReturntoRegister:
                changeToRegister();
                break;
            default:

        }
    }

    private void changeToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        LoginActivity.this.finish();
    }

    /**
     * 游客模式
     */
    private void visitor(){
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        User.getInstance().setAccount("visitor");
        User.getInstance().setPassword(null);
        User.getInstance().setLogin(true);
        startActivity(i);
        LoginActivity.this.finish();
    }

    /**
     * 登录功能
     */
    private void login(){
        // 判断账号密码非空
        // 账号不为空
        if(!TextUtils.isEmpty(account.getText())){
            //密码不为空
            if(!TextUtils.isEmpty(passwd.getText())){
                // 判断是否为手机号格式
//                "^((13[0-9])|(15[^4])|(18[0-9])|(17[0-8])|(147,145))\\d{8}$"
                Pattern p=Pattern.compile("^(\\w+([-.][A-Za-z0-9]+)*){3,18}@\\w+([-.][A-Za-z0-9]+)*\\.\\w+([-.][A-Za-z0-9]+)*$");
                Pattern p1=Pattern.compile("^1([358][0-9]|4[579]|66|7[0135678]|9[89])[0-9]{8}$");
                Matcher m=p.matcher(account.getText().toString());
                Matcher m1=p1.matcher(account.getText().toString());
//                System.out.println("判断m"+m.matches());
//                System.out.println("判断m1"+m1.matches());
                if(m.matches()||m1.matches()) {
                    // 连接服务器 ，验证账号密码
//                if(true){
                    checkLogin();
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myToast.toastShow("账号格式错误!");
                        }
                    });
                }
            }else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        myToast.toastShow("请输入密码!");
                    }
                });
            }
        }else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myToast.toastShow("请输入账号!");
                }
            });
        }

    }

    /**
     * 登录检查
     * @return
     */
    private void checkLogin(){
        String basePath ="http://110.40.168.206:5000/login";
        String data="?account="+account.getText().toString()+"&"+"passwd="+passwd.getText().toString();
        System.out.println(basePath+data);
        NetClient.getNetClient().callNet(basePath + data, new MyCallBack() {
            @Override
            public void onFailure(int code) {
                System.out.println(code);
                // 服务器连接失败
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        myToast.toastShow("ServerError");
                    }
                });
            }

            @Override
            public void onResponse(String json) {
                System.out.println("json:"+json);
                String result = json;
                // 登录成功
                if(result.equals("success")){
                    System.out.println("success");
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);

                    //TODO：设置用户信息
                    User.getInstance().setAccount(account.getText().toString());
                    User.getInstance().setPassword(passwd.getText().toString());
                    User.getInstance().setLogin(true);
                    startActivity(i);
                    LoginActivity.this.finish();
                }else if(result.equals("false")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 密码错误
                            myToast.toastShow("密码错误!");
                        }
                    });
                }else if(result.equals("unregistered")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 未注册
                            myToast.toastShow("账号不存在!");

                        }
                    });
                }
            }
        });
    }
}
