package com.koudai.dns;

/**
 * Created by kendada on 2017/4/12.
 */

public interface BaseHostTester {

    /**
     * 测试IP是否可用
     * */
    boolean testIpAvailable(String ip);

}
