package com.ybyc.gateway.nettyplus.core;

import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapTest {

    static ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();

    public static void main(String[] args) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Object value = concurrentHashMap.putIfAbsent("1","1");
                if(value==null){
                    System.out.println("put 1 success");
                }else{
                    System.out.println("put 1 fail");
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Object value = concurrentHashMap.putIfAbsent("1","2");
                if(value==null){
                    System.out.println("put 2 success");
                }else {
                    System.out.println("put 2 fail");
                }
            }
        }).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(concurrentHashMap);
    }



}
