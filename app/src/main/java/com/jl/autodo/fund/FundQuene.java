package com.jl.autodo.fund;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by JL on 2016/8/14.
 */
public class FundQuene {
    private int queueSize = 10;
    private ArrayBlockingQueue<Map<String, String>> queue = new ArrayBlockingQueue<Map<String, String>>(queueSize);

    public void init() {
        while(true){

        }
    }

    public void put(Map<String, String> map) {
        try {
            queue.put(map);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
