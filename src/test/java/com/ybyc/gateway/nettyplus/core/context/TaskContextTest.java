package com.ybyc.gateway.nettyplus.core.context;

import com.ybyc.gateway.nettyplus.core.Message;
import com.ybyc.gateway.nettyplus.core.util.TimeIdHelper;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class TaskContextTest {

//    @Test
    public void test1(){

        TaskContext.getInstance().start();
        TaskContext.timeout = 3;

        Mono.just("123123").delayElement(Duration.ofSeconds(3)).doOnEach(it->{
            TaskContext.getInstance().wakeup("1",null,1,1,new Message<>());
        }).subscribe();


        TaskContext.<Message>task("1", 1,1)
                .execute()
                .doOnEach(message->{
                    System.out.println(message);
                }).block(Duration.ofSeconds(3));



    }

    public void test2(){
        System.out.println(TimeIdHelper.get());
        System.out.println(TimeIdHelper.get());
        System.out.println(TimeIdHelper.get());
        System.out.println(TimeIdHelper.get());
        System.out.println(TimeIdHelper.get());
        System.out.println(TimeIdHelper.get());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(TimeIdHelper.get());
        System.out.println(TimeIdHelper.get());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(TimeIdHelper.get());
        System.out.println(TimeIdHelper.get());
        System.out.println(TimeIdHelper.get());
        System.out.println(TimeIdHelper.get());
    }


}
