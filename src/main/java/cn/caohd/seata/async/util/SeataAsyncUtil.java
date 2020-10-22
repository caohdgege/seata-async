package cn.caohd.seata.async.util;

import cn.caohd.seata.async.context.SeataAysncCallContext;
import cn.caohd.seata.async.context.SeataAsyncCallInfo;
import cn.caohd.seata.async.functional.AsyncNoRevFunction;
import cn.caohd.seata.async.functional.AsyncRevFunction;
import io.seata.core.context.RootContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class SeataAsyncUtil {
    private static final Logger logger = LoggerFactory.getLogger(SeataAsyncUtil.class);

    @Value("${seata.async.thread-num:512}")
    private int THREAD_NUM;

    private ExecutorService executor = Executors.newFixedThreadPool(THREAD_NUM);

    public <T> SeataAsyncCallInfo<T> async(AsyncRevFunction<T> func) {
        String xid = RootContext.getXID();

        Future<T> future = executor.submit(() -> {
            if (StringUtils.isNotEmpty(xid)) {
                logger.debug("xid is : {}", xid);
                RootContext.bind(xid);
            }

            return func.apply();
        });

        SeataAsyncCallInfo<T> callInfo = new SeataAsyncCallInfo<>(future, xid);
        SeataAysncCallContext.addAsyncInfo(callInfo);
        return callInfo;
    }

    public <T> SeataAsyncCallInfo asyncNotReturnVal(AsyncNoRevFunction<T> func) {
        String xid = RootContext.getXID();

        Future<Boolean> future = executor.submit(() -> {
            if (StringUtils.isNotEmpty(xid)) {
                logger.debug("xid is : {}", xid);
                RootContext.bind(xid);
            }

            func.apply();

            return true;
        });

        SeataAsyncCallInfo<Boolean> callInfo = new SeataAsyncCallInfo<>(future, xid);
        SeataAysncCallContext.addAsyncInfo(callInfo);
        return callInfo;
    }
}
