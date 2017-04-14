package com.koudai.dns;

import android.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by kendada on 2017/4/12.
 * DNS防劫持
 */

public class DnsHelper {

    private final String TAG = DnsHelper.class.getSimpleName();

    private final ReentrantLock queueLock = new ReentrantLock();

    private volatile String defaultAvailableIP = null;

    private BaseIpManager mIpManager;
    private BaseHostTester mHostTester;

    private List<String> availableIPs; // 可用IP

    private Thread worker;

    private final ConcurrentHashMap<String, IPTestCase> testCaseMap = new ConcurrentHashMap<>();

    public DnsHelper(BaseIpManager ipManager, BaseHostTester hostTester){
        if(ipManager==null|| hostTester == null){
            throw  new RuntimeException("hostTester, ipManager can not be null");
        }
        mIpManager = ipManager;
        mHostTester = hostTester;
        init();
    }

    private void init(){
        availableIPs = mIpManager.readAvailableIPs();
        if(availableIPs==null||availableIPs.isEmpty()){
            throw  new RuntimeException("AvailableIPs can not be null");
        }
        defaultAvailableIP = availableIPs.get(0);
    }

    public String hostname2IP(String hostname){
        String result = null;
        try {
            InetAddress mInetAddress = InetAddress.getByName(hostname);
            String realIPByDNS = mInetAddress.getHostAddress();
            if(availableIPs.contains(realIPByDNS)){
                result = realIPByDNS;
            }else{
                result = getBestIPByNewIP(realIPByDNS);
                addNewHostTestCase(realIPByDNS);
            }
        } catch (Throwable e){
            Log.e(TAG,e.getMessage(),e);
        }
        if(result!=null){
            return result;
        }
        return defaultAvailableIP;
    }

    private String getBestIPByNewIP(String newIP){
        IPTestCase testCase = testCaseMap.get(newIP);
        if (testCase!=null) {
            return testCase.getBestIP();
        }
        Log.d(TAG, " *** " + testCase);
        return null;
    }

    private IPTestCase createCase(String newIP){
        List<IPTestCaseItem> newCaseItems = new ArrayList<>();
        for (String ip : availableIPs){
            newCaseItems.add(new IPTestCaseItem(ip));
        }
        newCaseItems.add(new IPTestCaseItem(newIP));
        return new IPTestCase(newIP,newCaseItems);
    }

    private void notifyWorker(){
        synchronized (this){
            if(worker==null){
                worker = new Thread(){
                    public void run(){
                        work();
                    }
                };
                worker.setDaemon(true);
                worker.start();
            }else{
                this.notify();
            }
        }
    }

    private void addNewHostTestCase(final String newIP){
        queueLock.lock();
        try {
            if (!testCaseMap.containsKey(newIP)) {
                testCaseMap.put(newIP, createCase(newIP));
                notifyWorker();
            }

        }catch (Exception e){
            Log.e(TAG,e.getMessage(),e);
        }finally {
            queueLock.unlock();
        }

    }

     private List<IPTestCase> getWaittingCases(){
         List<IPTestCase> waittingCases = null;
         queueLock.lock();
         try {
            for(Map.Entry<String,IPTestCase> e:testCaseMap.entrySet()){
                if(!e.getValue().isTested()){
                    if(waittingCases==null){
                        waittingCases = new ArrayList<>();
                    }
                    waittingCases.add(e.getValue());
                }
            }
         }catch (Exception e){
             Log.e(TAG,e.getMessage(),e);
         }finally {
             queueLock.unlock();
         }
         return waittingCases;
     }

    private void work(){
        while(true){
            List<IPTestCase> waittingCases = getWaittingCases();
            if(waittingCases==null||waittingCases.isEmpty()){
                synchronized (this){
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        Log.e(TAG,e.getMessage(),e);
                        break;
                    }
                }
                continue;
            }

            for(IPTestCase testCase:waittingCases){
                doTest(testCase);
            }

            for(IPTestCase testCase:waittingCases){
                testCase.setBestIP(getBestIP(testCase));
                if(newIPIsAvailable(testCase)){
                    if(!availableIPs.contains(testCase.getNewIP())){
                        availableIPs.add(testCase.getNewIP());
                        mIpManager.saveAvailableIPs(availableIPs);
                    }
                }
            }
        }

    }

    private void doTest(IPTestCase testCase){
        for(IPTestCaseItem caseItem:testCase.getItems()){
            long beginTime = System.currentTimeMillis();
            boolean available = false;
            try {
                available = mHostTester.testIpAvailable(caseItem.getIp());
            }catch (Exception e){
                Log.e(TAG,e.getMessage(),e);
            }
            caseItem.setTime(System.currentTimeMillis() - beginTime);
            caseItem.setAvailable(available);
            Log.d(TAG, " *** caseItem = " + caseItem);
        }
        testCase.setTested(true);
    }

    private String getBestIP(IPTestCase testCase){
        long minTime= Long.MAX_VALUE;
        String bestIP = null;
        for(IPTestCaseItem caseItem:testCase.getItems()){
            if(caseItem.isAvailable()&&caseItem.getTime()<minTime){
                minTime = caseItem.getTime();
                bestIP = caseItem.getIp();
            }
        }
        return bestIP;
    }

    private boolean newIPIsAvailable(IPTestCase testCase){
        List<IPTestCaseItem> caseItemList = testCase.getItems();
        return testCase.getItems().get(caseItemList.size()-1).isAvailable();
    }
}
