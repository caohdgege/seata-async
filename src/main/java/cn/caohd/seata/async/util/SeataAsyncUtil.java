package cn.caohd.seata.async.util;

import cn.caohd.seata.async.context.SeataAysncCallContext;
import cn.caohd.seata.async.context.SeataAsyncCallInfo;
import cn.caohd.seata.async.functional.AsyncNoRevFunction;
import cn.caohd.seata.async.functional.AsyncRevFunction;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
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
    private final ExecutorService executor;

    public SeataAsyncUtil(@Value("${seata.async.thread-num:512}") int threadNum) {
        executor = Executors.newFixedThreadPool(threadNum);
    }

    public <T> SeataAsyncCallInfo<T> async(AsyncRevFunction<T> func) {
        String xid = RootContext.getXID();

        Future<T> future = executor.submit(() -> {
            if (StringUtils.isNotEmpty(xid)) {
                logger.debug("xid is : {}", xid);
                RootContext.bind(xid);
            }

            try {
                return func.apply();
            } finally {
                if (null != RootContext.getXID()) {
                    RootContext.unbind();
                }
            }
        });

        SeataAsyncCallInfo<T> callInfo = new SeataAsyncCallInfo<>(future, xid);
        SeataAysncCallContext.addAsyncInfo(callInfo);
        return callInfo;
    }

    @CanIgnoreReturnValue
    public <T> SeataAsyncCallInfo<Boolean> asyncNotReturnVal(AsyncNoRevFunction<T> func) {
        String xid = RootContext.getXID();

        Future<Boolean> future = executor.submit(() -> {
            if (StringUtils.isNotEmpty(xid)) {
                logger.debug("xid is : {}", xid);
                RootContext.bind(xid);
            }
            try {
                func.apply();

                return true;
            } finally {
                if (null != RootContext.getXID()) {
                    RootContext.unbind();
                }
            }
        });

        SeataAsyncCallInfo<Boolean> callInfo = new SeataAsyncCallInfo<>(future, xid);
        SeataAysncCallContext.addAsyncInfo(callInfo);
        return callInfo;
    }
}
