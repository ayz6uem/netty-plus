package com.ybyc.gateway.nettyplus.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class TimeIdHelper {

    private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("ddHHmmss");
    private static AtomicInteger atomicInteger = new AtomicInteger();

    public static String get(){
        return LocalDateTime.now().format(timeFormatter) + String.format("%04d",atomicInteger.incrementAndGet());
    }

}
