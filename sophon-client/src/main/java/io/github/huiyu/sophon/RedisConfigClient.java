package io.github.huiyu.sophon;

import java.io.IOException;
import java.util.Map;

public class RedisConfigClient extends ConfigClient {

    public RedisConfigClient(String application) {
        super(application);
    }

    @Override
    public String get(String name) {
        return null;
    }

    @Override
    public Map<String, String> getAll() {
        return null;
    }

    @Override
    public void set(String name, String value) {

    }

    @Override
    public void delete(String name) {

    }

    @Override
    public void close() throws IOException {

    }
}
