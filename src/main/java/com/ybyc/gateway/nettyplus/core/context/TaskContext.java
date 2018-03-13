package com.ybyc.gateway.nettyplus.core.context;

import com.ybyc.gateway.nettyplus.core.exception.ChannelNotFoundException;
import com.ybyc.gateway.nettyplus.core.exception.TaskExecutingException;
import com.ybyc.gateway.nettyplus.core.exception.TimeoutException;
import com.ybyc.gateway.nettyplus.core.util.TimeIdHelper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 请求的上下文，提供正在进行的请求，请求过期等
 *
 * @author wangzhe
 */
public class TaskContext {

    private static TaskContext instance;
    /**
     * 任务时间轮
     */
    private HashedWheelTimer wheelTimer = new HashedWheelTimer();
    /**
     * 任务的超时时间，单位秒
     */
    public static int timeout;

    public static TaskContext getInstance() {
        if (instance == null) {
            newInstance();
        }
        return instance;
    }

    private static synchronized TaskContext newInstance() {
        if (instance == null) {
            instance = new TaskContext();
        }
        return instance;
    }

    public void start(){
        wheelTimer.start();
    }

    public void stop(){
        wheelTimer.stop();
    }


    /**
     * key为复合id，使用Tuple2及子类，将id，指令，msgId组合在一起
     */
    private Map<Object, Task> taskPool = new ConcurrentHashMap<>();

    /**
     * 等待任务结果
     *
     * @param resultId
     * @param resultId
     * @param task
     */
    private void await(Object resultId, Task task) {
        if (!taskPool.containsKey(resultId)) {
            task.timeout =  wheelTimer.newTimeout(task,timeout, TimeUnit.SECONDS);
            taskPool.put(resultId, task);
            return;
        }
        throw new TaskExecutingException("task exist, please wait it");
    }

    /**
     * 唤醒等待方
     *
     * @param channelHandlerContext
     * @param id
     * @param result
     */
    public void wakeup(ChannelHandlerContext channelHandlerContext, Object id, Object result) {
        Task task = taskPool.remove(id);
        if (task != null) {
            task.timeout.cancel();
            task.success(channelHandlerContext, result);
        }
    }

    public void wakeup(ChannelHandlerContext channelHandlerContext, Object result) {
        wakeup(channelHandlerContext, ChannelContext.getId(channelHandlerContext.channel()), result);
    }

    public void wakeup(ChannelHandlerContext channelHandlerContext, Object id, Object directive, Object result) {
        wakeup(channelHandlerContext, Tuples.of(id, directive), result);
    }

    public void wakeup(ChannelHandlerContext channelHandlerContext, Object id, Object directive, Object msgId, Object result) {
        wakeup(channelHandlerContext, Tuples.of(id, directive, msgId), result);
    }

    /**
     * 任务取消
     *
     * @param resultId
     */
    public Task cancel(Object resultId) {
        Task task = taskPool.remove(resultId);
        if(Objects.nonNull(task) && !task.timeout.isCancelled()){
            task.timeout.cancel();
        }
        return task;
    }

    /**
     * 获取任务池中的任务
     * @return
     */
    public Map<Object,Task> getTaskPool(){
        return taskPool;
    }

    public int size(){
        return taskPool.size();
    }


    public static <T> Task.TaskBuilder<T> task(Object id) {
        return new Task.TaskBuilder<>(id);
    }

    public static <T> Task.TaskBuilder<T> task(Object id, Object directive) {
        return new Task.TaskBuilder<T>(id).result(directive);
    }

    public static <T> Task.TaskBuilder<T> task(Object id, Object directive, Object msgId) {
        return new Task.TaskBuilder<T>(id).result(directive, msgId);
    }

    public void taskTimeout(int timeout) {
        this.timeout = timeout;
    }

    public static class Task<T> implements TimerTask {

        private LocalDateTime start;
        private String uid;
        private Object id;
        private Object resultId;
        private Consumer<Channel> preTask;
        private Supplier<Object> postTask;
        private BiConsumer<ChannelHandlerContext, T> successTask;
        private MonoSink<T> sink;
        private Timeout timeout;

        public Task() {
            uid = TimeIdHelper.get();
        }

        public void success(ChannelHandlerContext channelHandlerContext, T data) {
            if (successTask != null) {
                successTask.accept(channelHandlerContext, data);
            }
            sink.success(data);
        }

        /**
         * 任务执行，返回Mono<T>
         *
         * @return
         */
        public Mono<T> execute() {
            return Mono
                    .<T>create(sink -> {

                        TaskContext.getInstance().await(resultId, Task.this);

                        Task.this.start = LocalDateTime.now();
                        Task.this.sink = sink;

                        if (Objects.nonNull(preTask)) {
                            preTask.accept(ChannelContext.getInstance().find(id));
                        } else if (Objects.nonNull(postTask)) {
                            ChannelContext.getInstance().find(id).writeAndFlush(postTask.get());
                        }
                    })
                    .doOnError(error -> {
                        if(error instanceof TaskExecutingException){
                            throw (TaskExecutingException)error;
                        }
                        TaskContext.getInstance().cancel(resultId);
                        if(error instanceof ChannelNotFoundException){
                            throw (ChannelNotFoundException)error;
                        }
                        if(error instanceof TimeoutException){
                            throw (TimeoutException)error;
                        }
                        throw new RuntimeException(error.getMessage(), error);
                    });
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            if(!timeout.isCancelled()){
                sink.error(new TimeoutException("task timeout"));
            }
        }


        public static class TaskBuilder<T> {

            Task<T> task;

            public TaskBuilder(Object id) {
                task = new Task<T>() {
                };
                task.id = id;
                task.resultId = id;
            }

            public TaskBuilder<T> result(Object directive) {
                task.resultId = Tuples.of(task.id, directive);
                return this;
            }

            public TaskBuilder<T> result(Object directive, Object msgId) {
                task.resultId = Tuples.of(task.id, directive, msgId);
                return this;
            }

            /**
             * pre优先于post
             * @param preTask
             * @return
             */
            public TaskBuilder<T> pre(Consumer<Channel> preTask) {
                task.preTask = preTask;
                return this;
            }

            public TaskBuilder<T> post(Supplier<Object> postTask) {
                task.postTask = postTask;
                return this;
            }

            public TaskBuilder<T> success(BiConsumer<ChannelHandlerContext, T> successTask) {
                task.successTask = successTask;
                return this;
            }

            public Task<T> build() {
                return task;
            }

            public Mono<T> execute() {
                return build().execute();
            }

        }

        public Object getId() {
            return id;
        }

        public Object getResultId() {
            return resultId;
        }

        public String getUid() {
            return uid;
        }

        public LocalDateTime getStart() {
            return start;
        }
    }

}
