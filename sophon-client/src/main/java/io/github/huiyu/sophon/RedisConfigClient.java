package io.github.huiyu.sophon;

import java.io.IOException;
import java.util.Map;

import redis.clients.jedis.Jedis;

public class RedisConfigClient extends ConfigClient {

    private final Jedis jedis;
    private final String key;

    public RedisConfigClient(String application, String host, int port) {
        super(application);
        key = "sophon_" + application;
        jedis = new Jedis(host, port);
    }

    @Override
    public String get(String name) {
        return jedis.hget(key, checkNotNullOrEmpty(name));
    }

    @Override
    public Map<String, String> getAll() {
        return jedis.hgetAll(key);
    }

    @Override
    public void set(String name, String value) {
        jedis.hset(key, checkNotNullOrEmpty(name), value);
    }

    @Override
    public void delete(String name) {
        jedis.hdel(key, name);
    }

    @Override
    public void close() throws IOException {
        jedis.close();
    }
}
