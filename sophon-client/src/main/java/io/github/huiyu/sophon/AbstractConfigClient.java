package io.github.huiyu.sophon;

import com.google.common.base.Strings;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.*;

public abstract class AbstractConfigClient implements ConfigClient {

    protected String application;
    protected Set<ConfigSubscriber> subscribers;

    public AbstractConfigClient(String application) {
        this.application = checkNotNullOrEmpty(application);
        this.subscribers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    @Override
    public void addSubscriber(ConfigSubscriber subscriber) {
        subscribers.add(checkNotNull(subscriber));
    }

    @Override
    public void removeSubscriber(ConfigSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    protected String checkNotNullOrEmpty(String s) {
        checkArgument(!Strings.isNullOrEmpty(s));
        return s;
    }
}
