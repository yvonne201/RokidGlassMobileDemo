package com.rokid.alliancedemo.bean;

/**
 * Author: heshun
 * Date: 2020/9/22 2:31 PM
 * gmail: shunhe1991@gmail.com
 */
public class Words {

    private boolean select;

    private String str;

    public String getStr() {
        return str == null ? "" : str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }
}
