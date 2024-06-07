package com.baidu.paddle.lite.demo.yolo_detection;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.paddle.lite.demo.utils.JavaMailUtil;
import com.baidu.paddle.lite.demo.component.MyCallBack;
import com.baidu.paddle.lite.demo.component.NetClient;
import com.baidu.paddle.lite.demo.utils.RandomUtil;
import com.baidu.paddle.lite.demo.component.ToastShow;
import com.baidu.paddle.lite.demo.utils.htmlText;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener ,TextWatcher{

    private EditText account;
    private EditText password;
    private EditText code;
    private TextView get_code;
    private Button btn_register;
    private Button btn_backToLogin;
    private String returnCode;
    private String account_text;
    boolean isRegistered=false;

    private String basePath;
    private String data;

    ToastShow myToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
    }

    private void  initView() {

        account_text=null;
        returnCode=null;
        basePath=null;
        data=null;
        myToast =new ToastShow(RegisterActivity.this);
        account = findViewById(R.id.account);
        password = findViewById(R.id.passwd);
        get_code = findViewById(R.id.getCode);
        code = findViewById(R.id.code);
        btn_register = findViewById(R.id.btn_register);
        btn_backToLogin= findViewById(R.id.btn_backToLogin);
        get_code.setOnClickListener(this);
        btn_register.setOnClickListener(this);
        btn_backToLogin.setOnClickListener(this);
        btn_register.setEnabled(false);
        account.addTextChangedListener(this);
        password.addTextChangedListener(this);
        code.addTextChangedListener(this);
    }

    /**
     * 注册
     */
    private void register() {
        if(account_text!=null){
            if(account_text.equals(account.getText().toString())){
                //验证码是否正确
                    if(returnCode.equals(code.getText().toString())){
                        //连接服务器  发送 注册请求
                        basePath ="http://110.40.168.206:5000/register";
                        data = "?account="+account_text+"&passwd="+password.getText().toString();
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
                                if(result.equals("success")){
                                    dialog();
                                }else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            myToast.toastShow("注册失败");
                                        }
                                    });
                                }
                            }
                        });
                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myToast.toastShow("验证码错误");
                            }
                        });
                    }
            }
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myToast.toastShow("请输入账号！");
                }
            });

        }
    }


    private void checkRegister() {
        basePath ="http://110.40.168.206:5000/checkRegister";
        data="?account="+account.getText().toString();
        NetClient.getNetClient().callNet(basePath + data, new MyCallBack() {
            @Override
            public void onFailure(int code) {
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
                System.out.println("result:"+result);
                if(result.equals("unregistered")){
                    //TODO:可以注册
                    isRegistered=false;
                }else{
                    //TODO:已经注册
                    isRegistered=true;
                }
            }
        });
    }

    private void getCode() {
        if(!TextUtils.isEmpty(account.getText())){
            //TODO:判断账号是否注册
            System.out.println("account:"+account.getText().toString());
            checkRegister();
            System.out.println("---------"+isRegistered);
            if(!isRegistered){
                System.out.println("可以注册");
                account_text=account.getText().toString();
                Pattern p=Pattern.compile("^(\\w+([-.][A-Za-z0-9]+)*){3,18}@\\w+([-.][A-Za-z0-9]+)*\\.\\w+([-.][A-Za-z0-9]+)*$");
                Pattern p1=Pattern.compile("^1([358][0-9]|4[579]|66|7[0135678]|9[89])[0-9]{8}$");
                Matcher m=p.matcher(account_text);
                Matcher m1=p1.matcher(account_text);
                if(m.matches()||m1.matches()){
                    new CountDownTimer(30000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            get_code.setText("重新发送" + millisUntilFinished / 1000);
                        }
                        public void onFinish() {
                            get_code.setText("获取验证码");
                            get_code.setClickable(true);
                        }
                    }.start();
                    get_code.setClickable(false);
                    //判断邮箱格式
                    if(m.matches()){
                        //TODO:发送邮箱验证码
                        sendEmailCode(account_text);
                    }
                    //判断手机号格式
                    else if(m1.matches()){
                        //TODO:发送手机验证码
                        sendPhoneCode(account_text);
                    }
                }else {
                    //TODO:提示错误
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myToast.toastShow("请输入正确的邮箱或手机号");
                        }
                    });

                }
            }else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        myToast.toastShow("该账号已经注册");
                    }
                });

            }
        }else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myToast.toastShow("请输入邮箱或手机号");
                }
            });
        }
    }


    /**
     * 邮箱验证码
     */
    private void sendEmailCode(String email){
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            JavaMailUtil.receiveMailAccount = email; // 给用户输入的邮箱发送邮件
            // 1、创建参数配置，用于连接邮箱服务器的参数配置
            Properties props = new Properties();
            // 开启debug调试
            props.setProperty("mail.debug", "true");
            // 发送服务器需要身份验证
            props.setProperty("mail.smtp.auth", "true");
            // 设置右键服务器的主机名
            props.setProperty("mail.host", JavaMailUtil.emailSMTPHost);
            // 发送邮件协议名称
            props.setProperty("mail.transport.protocol", "smtp");
            // 2、根据配置创建会话对象，用于和邮件服务器交互
            Session session = Session.getInstance(props);
            // 设置debug，可以查看详细的发送log
//                session.setDebug(true);
            // 3、创建一封邮件
            returnCode = RandomUtil.getRandom();
            String html = htmlText.html(returnCode);
            MimeMessage message = JavaMailUtil.creatMimeMessage(session, JavaMailUtil.emailAccount,
                    JavaMailUtil.receiveMailAccount, html);

            // 4、根据session获取邮件传输对象
            Transport transport = session.getTransport();
            // 5、使用邮箱账号和密码连接邮箱服务器emailAccount必须与message中的发件人邮箱一致，否则报错
            transport.connect(JavaMailUtil.emailAccount, JavaMailUtil.emailPassword);
            // 6、发送邮件,发送所有收件人地址
            transport.sendMessage(message, message.getAllRecipients());
            // 7、关闭连接
            transport.close();
        } catch (Exception e) {
            e.printStackTrace();
            returnCode=null;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myToast.toastShow("验证码发送失败");
                }
            });

        }
    }

    /**
     * 手机短信验证码
     */
    private void sendPhoneCode(String phone){

        try{
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            // 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey,此处还需注意密钥对的保密
            // 密钥可前往https://console.cloud.tencent.com/cam/capi网站进行获取
            Credential cred = new Credential("AKIDgBRRBfsnRpw95l9G6j48uPecIhqjKejW", "XnlTu3vWrugCQN3SbT3MPAesqWv2sSlU");
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            SmsClient client = new SmsClient(cred, "ap-nanjing", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            SendSmsRequest req = new SendSmsRequest();
            returnCode=RandomUtil.getRandom();
            String [] code={returnCode};
            String[] phoneNumberSet1 = {phone};
            req.setPhoneNumberSet(phoneNumberSet1);
            req.setSmsSdkAppId("1400655516");
            req.setTemplateId("1354823");
            req.setSignName("神奇海螺个人网");
            req.setTemplateParamSet(code);
            // 返回的resp是一个SendSmsResponse的实例，与请求对象对应
            SendSmsResponse resp = client.SendSms(req);
            // 输出json格式的字符串回包
//            System.out.println(SendSmsResponse.toJsonString(resp));
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject=jsonParser.parse(SendSmsResponse.toJsonString(resp)).getAsJsonObject();
            if(!jsonObject.get("SendStatusSet").getAsJsonArray().get(0).getAsJsonObject().get("Code").toString().equals("\"Ok\"")){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        myToast.toastShow("验证码发送失败");
                    }
                });
                returnCode=null;
            }
        } catch (TencentCloudSDKException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myToast.toastShow("验证码发送失败");
                }
            });
            System.out.println(e.toString());
            returnCode=null;
        }
    }



    /**
     * 返回登录界面
     */
    private void backToLogin(){
        Intent i= new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getCode:
                getCode();
                break;
            case R.id.btn_register:
                register();
                break;
            case R.id.btn_backToLogin:
                 backToLogin();
                 break;
        }
    }



    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }


    /**
     * 判断所有输入框是否为空
     * @param s
     */
    @Override
    public synchronized void afterTextChanged(Editable s) {
        if(account.getText().toString().length()>0&&password.getText().toString().length()>0&&code.length()>0){
            btn_register.setEnabled(true);
        }else {
            btn_register.setEnabled(false);
        }
    }


    protected void dialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(RegisterActivity.this);
        builder.setMessage("注册成功！返回登录页面");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                // TODO Auto-generated method stub
                dialog.dismiss();
                backToLogin();
            }

        });
        builder.create().show();
    }

}