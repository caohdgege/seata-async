package cn.caohd.seata.async.util;

import cn.caohd.seata.async.context.SeataAsyncCallInfo;
import io.seata.core.context.RootContext;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

@SpringBootTest
public class SeataAsyncUtilTest {

    SeataAsyncUtil seataAsyncUtil = new SeataAsyncUtil(128);

    @Test
    public void async() throws Exception {
        RootContext.bind("test-xid");
        SeataAsyncCallInfo<String> callInfo = seataAsyncUtil.async(RootContext::getXID);
        assertEquals(callInfo.get(),"test-xid");

        RootContext.bind("test-xid");
        SeataAsyncCallInfo<Boolean> callInfo1 = seataAsyncUtil.async(() -> {
            Thread.sleep(31000L);
            return true;
        });

        try {
            callInfo1.get();
        } catch (TimeoutException ex) {
            return;
        }
        // will not happen
        assertTrue(false);
    }

    @Test
    public void asyncNotReturnVal() throws Exception {
        RootContext.bind("test-xid");
        seataAsyncUtil.asyncNotReturnVal(() -> assertEquals(RootContext.getXID(), "test-xid") );
    }
}