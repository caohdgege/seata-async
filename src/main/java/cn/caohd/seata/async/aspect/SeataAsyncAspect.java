package cn.caohd.seata.async.aspect;

import cn.caohd.seata.async.context.SeataAsyncCallInfo;
import cn.caohd.seata.async.context.SeataAysncCallContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * @author chd
 **/
@Aspect
@Component
public class SeataAsyncAspect {
    private static final Logger logger = LoggerFactory.getLogger(SeataAsyncAspect.class);

    @Around("@annotation(io.seata.spring.annotation.GlobalTransactional)")
    public Object aroundLogCalls(ProceedingJoinPoint pjp) throws Throwable {
        try {
            Object o = pjp.proceed();

            // 在业务逻辑执行完了之后，在这里需要堵塞等待远端执行完成
            List<SeataAsyncCallInfo> callInfos = new ArrayList<>(SeataAysncCallContext.getAsyncInfos());

            for (SeataAsyncCallInfo callInfo : callInfos) {
                callInfo.get();
            }

            return o;
        } catch (Throwable e){
            // 如果业务逻辑上有异常，或者get的时候有异常，
            // 需要二次进行get，确保执行完成，并且这里在get的时候有异常，打个日志就忽略
            List<SeataAsyncCallInfo> callInfos = new ArrayList<>(SeataAysncCallContext.getAsyncInfos());
            for (SeataAsyncCallInfo callInfo : callInfos) {
                try {
                    callInfo.get();
                } catch (Exception logEx) {
                    // 在catch里面捕捉到的异常可以直接消费掉
                    logger.error("call exception ", logEx);
                }
            }

            // 把异常抛出去，触发回滚
            // 如果是ExecutionException
            // 代表着这是在future里面抛出的异常，应该把原始异常抛出去
            if (e instanceof ExecutionException) {
                throw e.getCause();
            }
            throw e;
        } finally {
            // 无论滚没滚，都要clear一下
            SeataAysncCallContext.clear();
        }
    }
}
