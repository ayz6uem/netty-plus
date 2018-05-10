package com.ybyc.gateway.nettyplus.core.util;

import com.ybyc.gateway.nettyplus.core.Heart;
import com.ybyc.gateway.nettyplus.core.Message;
import org.junit.Test;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public class OptionHelperTest {

    public void test1(){
        Message message = new Message<Heart>(){};
        Heart heart = new Heart();
        heart.setId(11231);
        message.setFlag((byte)1);
        message.setPayload(heart);
        boolean c = OptionHelper.containClass(message,Heart.class);
        System.out.println(c);
    }

    @Test
    public void test2(){
        Tuple2<String,String> tuple2 = Tuples.of("111","222");
        Tuple3<String,String,String> tuple1 = Tuples.of("111","222","111");
        System.out.println(tuple1.equals(tuple2));
        System.out.println(tuple2.equals(tuple1));
    }

}
