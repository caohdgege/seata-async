package cn.caohd.seata.async.functional;

/**
 * 对应有返回值的function
 * @param <T> 返回值类型
 */
@FunctionalInterface
public interface AsyncRevFunction<T> {
    T apply() throws Exception;
}
