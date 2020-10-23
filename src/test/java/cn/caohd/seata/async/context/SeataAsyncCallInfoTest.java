package cn.caohd.seata.async.context;

import cn.caohd.seata.async.util.SeataAsyncUtil;
import io.seata.core.context.RootContext;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

@SpringBootTest
public class SeataAsyncCallInfoTest {

    SeataAsyncUtil seataAsyncUtil = new SeataAsyncUtil(128);

    @Test
    public void testGet() throws Exception {
        RootContext.bind("test-xid");
        SeataAsyncCallInfo<String> callInfo = seataAsyncUtil.async(RootContext::getXID);
        assertEquals(callInfo.get(), "test-xid");
    }

    @Test
    public void getIgnoreTimeout() throws Exception {
        RootContext.bind("test-xid");
        SeataAsyncCallInfo<Boolean> callInfo = seataAsyncUtil.async(() -> {
            Thread.sleep(1000L);
            return true;
        });
        try {
            callInfo.get(500L);
        } catch (TimeoutException ex) {
            assertEquals(callInfo.getIgnoreTimeout(1000L), true);
        }
    }
}