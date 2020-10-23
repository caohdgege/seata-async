package cn.caohd.seata.async.context;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.assertEquals;


@SpringBootTest
public class SeataAsyncCallContextTest {
    @Test
    public void test() {
        SeataAysncCallContext.addAsyncInfo(new SeataAsyncCallInfo(null, null));
        assertEquals(SeataAysncCallContext.getAsyncInfos().size(), 1);
        SeataAysncCallContext.clear();
        assertEquals(SeataAysncCallContext.getAsyncInfos().size(), 0);
    }
}