package com.baidu.dpop.frame.core.filter.session.redis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;

/**
 * 基于Base64算法的对象、字符串序列化逻辑实现。
 * 由于是通过Java默认的序列化方式，并且再将二进制流的数据转化为Base64串，因此被序列化的对象必须实现{@link Serializable}接口。
 *
 * @author jiwenhao
 */
public class Base64RedisValueSerDe implements RedisValueSerDe {

    private static final Charset ASCII = Charset.forName("US-ASCII");

    @Override
    public String serialize(Object object) throws IOException {
        if (object == null) {
            return "";
        }

        if (!(object instanceof Serializable)) {
            throw new IllegalArgumentException("the object is not serializable");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(baos);
        try {
            objOut.writeObject(object);
            objOut.flush();

            byte[] raw = baos.toByteArray();
            byte[] encoded = Base64.encodeBase64(raw);
            return new String(encoded, ASCII);
        } finally {
            objOut.close();
        }
    }

    @Override
    public Object deserialize(String redisStoredValue) throws IOException {
        if (redisStoredValue == null || redisStoredValue.isEmpty()) {
            return null;
        }

        byte[] decodedData = Base64.decodeBase64(redisStoredValue.getBytes(ASCII));
        ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(decodedData));
        try {
            return objIn.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("failed to deserialize the stored value into an object due to class not found", e);
        } finally {
            objIn.close();
        }
    }
}
