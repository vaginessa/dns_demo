package com.koudai.dns;

import java.util.List;

/**
 * Created by kendada on 2017/4/13.
 */

public class IPTestCase {

    private String newIP;
    private volatile String bestIP;
    private  boolean tested;
    private List<IPTestCaseItem> items;

    public IPTestCase(String newIP, String bestIP, boolean tested, List<IPTestCaseItem> items) {
        this.newIP = newIP;
        this.bestIP = bestIP;
        this.tested = tested;
        this.items = items;
    }

    public IPTestCase(String newIP, List<IPTestCaseItem> items) {
       this(newIP,null,false,items);
    }

    public String getNewIP() {
        return newIP;
    }

    public void setNewIP(String newIP) {
        this.newIP = newIP;
    }

    public String getBestIP() {
        return bestIP;
    }

    public void setBestIP(String bestIP) {
        this.bestIP = bestIP;
    }

    public boolean isTested() {
        return tested;
    }

    public void setTested(boolean tested) {
        this.tested = tested;
    }

    public List<IPTestCaseItem> getItems() {
        return items;
    }

    public void setItems(List<IPTestCaseItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "IPTestCase{" +
                "newIP='" + newIP + '\'' +
                ", bestIP='" + bestIP + '\'' +
                ", tested=" + tested +
                ", items=" + items +
                '}';
    }
}
