package com.baidu.paddle.lite.demo.component;

import android.content.Context;
import android.widget.Toast;

public class ToastShow {
    private Context context;
    private Toast toast = null;
    public ToastShow(Context context) {
        this.context = context;
    }
    public void toastShow(String text) {
        if(toast == null)
        {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        }
        else {
            toast.setText(text);
        }
        toast.show();
    }
}
