package io.github.huiyu.sophon;

public interface ConfigSubscriber {

    void onConfigAdded(String name, String data);

    void onConfigUpdated(String name, String data);

    void onConfigDeleted(String name);
}
