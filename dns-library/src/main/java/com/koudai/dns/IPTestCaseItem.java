package com.koudai.dns;

/**
 * Created by kendada on 2017/4/13.
 */

public class IPTestCaseItem {

    public String ip;
    public long time;
    public boolean isAvailable;


    public IPTestCaseItem(String ip, long time, boolean isAvailable){
        this.ip=ip;
        this.time = time;
        this.isAvailable = isAvailable;
    }

    public IPTestCaseItem(String ip) {
        this(ip, Long.MAX_VALUE,false);
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public boolean isTested(){
        return time== Long.MAX_VALUE;
    }

    @Override
    public String toString() {
        return "IPBO{" +
                "ip='" + ip + '\'' +
                ", time=" + time +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
