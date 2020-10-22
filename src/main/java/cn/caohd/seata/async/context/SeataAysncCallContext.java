package cn.caohd.seata.async.context;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 调用的上下问
 */
public class SeataAysncCallContext {
    /**
     * 异步的消费队列
     * 注意，这里是TheadLocal，所以的话，不要尝试在异步线程里面获取这个东西
     */
    private static ThreadLocal<Queue<SeataAsyncCallInfo>> calls = ThreadLocal.withInitial(LinkedBlockingQueue::new);

    /**
     * 获取异步列表
     * @return 获取异步信息队列
     */
    public static Queue<SeataAsyncCallInfo> getAsyncInfos() {
        return calls.get();
    }

    /**
     * 添加异步执行信息
     * @param callInfo 异步执行信息内容
     */
    public static void addAsyncInfo(SeataAsyncCallInfo callInfo) {
        calls.get().add(callInfo);
    }

    /**
     * 清空一次异步信息
     */
    public static void clear() {
        calls.get().clear();
    }
}
