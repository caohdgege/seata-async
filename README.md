## seata-async
    这个项目是在实践过程中，发现seata在处理分布式事务的时候，维护性能的开销相对较大，并且其把调用纳入到全局事务体系是基于xid的传递。
    基于以上需求，把seata的一些调用进行异步处理，可以实现异步+分布式事务的并存。

### 用法
#### 1. 本地打包
```shell script
mvn package install -U -DskipTests
```
#### 2. 在项目中引入依赖包
```xml
    <dependency>
        <groupId>cn.caohd</groupId>
        <artifactId>seata-async</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </dependency>
```
#### 3. SpringBoot启动类中添加ComponentScan
```java
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
// 添加这部分
@ComponentScan({"cn.caohd.seata.async.*"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```
#### 4. 自动注入并且使用
```java
@Resource SeataAsyncUtil seataAsyncUtil;

/**
 * 调用 times 次 userMapper，每次往数据库插入100条数据
 * @param times 调用次数
 * @return 插入成功的条数
 */
@GlobalTransactional
public int batchInsert(int times, int size) throws Exception {
    // 旧写法
    // int counter = 0;
    // for (int i = 0; i < times; i++) {
    //     seataAsyncUtil.async(() -> userMapper.batchInsert(buildRandomUsers(size)));
    // }
    // return counter;

    // 新写法
    int counter = 0;
    for (int i = 0; i < times; i++) {
        seataAsyncUtil.async(() -> userMapper.batchInsert(buildRandomUsers(size)));
    }
    // 如果不关心返回值可以不用在这里get
    for (SeataAsyncCallInfo<Integer> callInfo : SeataAysncCallContext.getAsyncInfos()) {
        counter += callInfo.get();
    }

    return counter;
}
```

### 注意事项
1. 这个依赖只是用来解决部分问题，不是解决全部问题
2. 这个仅用于TM端，不要用来RM端(其实要实现RM端的话，可以仿照SeataAsyncAspect，写一个aspect，很简单的)
3. 不要进行事务嵌套，不支持事务嵌套
4. 确保异步的多个操作之间是没有先后顺序的

### 应用场景
1. 一个全局事务包含多个分支事务
2. 分支事务之间是没有执行的先后顺序的

### 性能测试报告
使用上面的例子测试，传入的times=50，seata-server的mode=db

如果使用同步的方式执行的话，用时约为3s

使用异步的方式测试的话，用时约800ms