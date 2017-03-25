package io.github.huiyu.sophon;

public abstract class AbstractConfigSubscriber implements ConfigSubscriber {

    @Override
    public void onConfigAdded(String name, String data) {
    }

    @Override
    public void onConfigUpdated(String name, String data) {
    }

    @Override
    public void onConfigDeleted(String name) {
    }
}
