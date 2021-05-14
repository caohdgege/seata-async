package cn.caohd.seata.async.aspect;

import cn.caohd.seata.async.context.SeataAsyncCallInfo;
import cn.caohd.seata.async.context.SeataAsyncCallContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * @author chd
 **/
@Aspect
@Component
@SuppressWarnings("rawtypes")
public class SeataAsyncAspect {
    @Around("@annotation(io.seata.spring.annotation.GlobalTransactional) " +
            "   || @annotation(org.springframework.web.bind.annotation.GetMapping) " +
            "   || @annotation(org.springframework.web.bind.annotation.PostMapping)" +
            "   || @annotation(org.springframework.web.bind.annotation.PutMapping)" +
            "   || @annotation(org.springframework.web.bind.annotation.RequestMapping)" +
            "   || @annotation(org.springframework.web.bind.annotation.DeleteMapping)" +
            "   || @annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public Object aroundLogCalls(ProceedingJoinPoint pjp) throws Throwable {
        try {
            Object o = pjp.proceed();

            // 在业务逻辑执行完了之后，在这里需要堵塞等待远端执行完成
            List<SeataAsyncCallInfo> callInfos = new ArrayList<>(SeataAsyncCallContext.getAsyncInfos());

            for (SeataAsyncCallInfo callInfo : callInfos) {
                callInfo.get();
            }

            return o;
        } catch (Throwable e){
            // 把异常抛出去，触发回滚
            // 如果是ExecutionException
            // 代表着这是在future里面抛出的异常，应该把原始异常抛出去
            if (e instanceof ExecutionException) {
                throw e.getCause();
            }
            throw e;
        } finally {
            // 无论滚没滚，都要clear一下
            SeataAsyncCallContext.clear();
        }
    }
}
