package io.github.huiyu.sophon;

import com.google.common.base.Strings;

import java.io.Closeable;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

public abstract class ConfigClient implements Closeable {

    protected String application;

    public ConfigClient(String application) {
        this.application = checkNotNullOrEmpty(application);
    }

    public abstract String get(String name);

    public abstract Map<String,String> getAll();

    public abstract void set(String name, String value);

    public abstract void delete(String name);

    protected String checkNotNullOrEmpty(String s) {
        checkArgument(!Strings.isNullOrEmpty(s));
        return s;
    }
}
