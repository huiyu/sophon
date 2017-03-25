package io.github.huiyu.sophon;

import java.io.Closeable;
import java.util.Map;

public interface ConfigClient extends Closeable {

    String get(String name);

    Map<String, String> getAll();

    void set(String name, String value);

    void delete(String name);

    void addSubscriber(ConfigSubscriber subscriber);

    void removeSubscriber(ConfigSubscriber subscriber);
}
