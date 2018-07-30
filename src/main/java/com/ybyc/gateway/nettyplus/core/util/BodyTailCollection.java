package com.ybyc.gateway.nettyplus.core.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 分段式(body tail)集合
 * @param <E>
 */
public class BodyTailCollection<E> extends LinkedList<E> {

    LinkedList<E> tail = new LinkedList<>();

    public LinkedList<E> getTail(){
        return tail;
    }

    /**
     * 将集合和尾部融合，并固化
     * @return
     */
    public List<E> immutable(){
        LinkedList<E> list = new LinkedList();
        list.addAll(this);
        list.addAll(tail);
        return Collections.unmodifiableList(list);
    }



}
