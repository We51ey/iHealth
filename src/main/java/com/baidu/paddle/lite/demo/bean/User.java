package com.baidu.paddle.lite.demo.bean;

public class User {
    private static User user;
    private User() {
    }

    public static User getInstance() {
        if(user == null) {
            user = new User();
        }
        return user;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    private String account;
    private String password;
    private String username;
    private boolean isLogin;

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }
}
