package com.koudai.dns;

import java.util.List;

/**
 * Created by kendada on 2017/4/12.
 */

public interface BaseIpManager {

    /**
     * 内置IP
     * */
      List<String> readAvailableIPs();

    /**
     * 持久化IP
     * */
     void saveAvailableIPs(List<String> availableIPs);

}
