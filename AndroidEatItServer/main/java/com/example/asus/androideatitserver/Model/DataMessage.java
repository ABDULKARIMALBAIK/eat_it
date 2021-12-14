package com.example.asus.androideatitserver.Model;

import java.util.Map;

public class DataMessage {

    //Form our notification is
    /*
     * {
     *   "to":"<topic> or <user token>"
     *   "data":{
     *           "my_custom_key":"my_custom_value"
     *           "my_custom_key2":true
     *   }
     * }
     * */

    public String to;
    public Map<String , String> data;

    public DataMessage() {
    }

    public DataMessage(String to, Map<String, String> data) {
        this.to = to;
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
