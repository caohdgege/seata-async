package cn.caohd.seata.async.context;

import javax.annotation.Nullable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SeataAsyncCallInfo<T> {
    /**
     * 默认的超时等待时间，建议跟feign/okHttp的超时时间一致
     */
    public static final Long DEFAULT_TIMEOUT = 30000L;
    /**
     * future
     */
    private final Future<T> future;
    /**
     * 分布式事务的xid
     */
    @Nullable
    public String xid;
    /**
     * 远程调用的结果(如果有的话)
     */
    private T object;
    /**
     * 异步调用是否完成(抛出异常或正常返回)
     */
    private boolean finish = false;
    /**
     * 是否抛出异常
     */
    private boolean exception = false;
    /**
     * 如果有异常的话，会同步设置这个异常信息
     */
    private Exception ex;

    public SeataAsyncCallInfo(Future<T> future, @Nullable String xid) {
        this.future = future;
        this.xid = xid;
    }

    public SeataAsyncCallInfo(Future<T> future) {
        this(future, null);
    }

    public T get(long timeout) throws Exception {
        try {
            if (isFinish() || isException()) {
                return object;
            }

            object = future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception exx) {
            this.exception = true;
            this.ex = exx;
            throw exx;
        } finally {
            this.finish = true;
        }
        return object;
    }

    public T get() throws Exception {
        return get(DEFAULT_TIMEOUT);
    }

    /**
     * 超时异常之后，如果想要重试，需要用这个
     * @return T
     */
    public T getIgnoreTimeout(long timeout) throws Exception {
        return object = null == object ? future.get(timeout, TimeUnit.MILLISECONDS) : object;
    }

    public boolean isFinish() {
        return finish;
    }

    public boolean isException() {
        return exception;
    }

    public Exception getException() {
        return ex;
    }

    public String getXid() {
        return xid;
    }
}
