package com.baidu.dpop.frame.core.filter.session.redis;

import java.io.IOException;

/**
 * Redis存储对象时的序列化与反序列化逻辑。
 *
 * @author jiwenhao
 */
public interface RedisValueSerDe {

    /**
     * 将一个对象序列化为一个Redis的String值。
     *
     * @param objectToBeSerialized 需要存储至redis的对象
     * @return 序列化后的值
     * @throws IOException 当序列化的过程中发生异常时
     */
    String serialize(Object objectToBeSerialized) throws IOException;

    /**
     * 将一个对象从Redis的String值中反序列化读出。
     *
     * @param redisStoredValue redis中存储的值
     * @return 反序列化后的对象
     * @throws IOException 当序列化的过程中发生异常时
     */
    Object deserialize(String redisStoredValue) throws IOException;
}
