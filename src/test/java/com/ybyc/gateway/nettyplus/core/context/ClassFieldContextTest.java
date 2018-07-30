package com.ybyc.gateway.nettyplus.core.context;

import com.ybyc.gateway.nettyplus.core.bean.ClassFieldContext;
import com.ybyc.gateway.nettyplus.core.bean.TailField;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collection;

public class ClassFieldContextTest {

    @Test
    public void test1(){
        Collection<Field> coll = ClassFieldContext.getDataField(Foo.class);
        coll.stream().forEach(field -> {
            System.out.println(field.getName());
        });
        Collection<Field> coll1 = ClassFieldContext.getDataField(Foo.class);
        coll1.stream().forEach(field -> {
            System.out.println(field.getName());
        });
    }

    public static class Foo{

        private String h;
        @TailField
        private int sum;
        private String conteng;

        public String getH() {
            return h;
        }

        public void setH(String h) {
            this.h = h;
        }

        public int getSum() {
            return sum;
        }

        public void setSum(int sum) {
            this.sum = sum;
        }

        public String getConteng() {
            return conteng;
        }

        public void setConteng(String conteng) {
            this.conteng = conteng;
        }
    }

}
