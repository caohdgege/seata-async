package cn.caohd.seata.async.context;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.assertEquals;


@SpringBootTest
public class SeataAsyncCallContextTest {
    @Test
    public void test() {
        SeataAsyncCallContext.addAsyncInfo(new SeataAsyncCallInfo(null, null));
        assertEquals(SeataAsyncCallContext.getAsyncInfos().size(), 1);
        SeataAsyncCallContext.clear();
        assertEquals(SeataAsyncCallContext.getAsyncInfos().size(), 0);
    }
}