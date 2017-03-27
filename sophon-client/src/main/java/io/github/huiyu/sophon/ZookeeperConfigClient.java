package io.github.huiyu.sophon;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ZookeeperConfigClient extends AbstractConfigClient {

    private static final String ZK_ROOT_PATH = "sophon";
    private static final Charset UTF_8 = Charsets.UTF_8;

    private final Map<String, String> configs = new ConcurrentHashMap<>();
    private final CuratorFramework zkClient;
    private final PathChildrenCache watcher;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public ZookeeperConfigClient(String application, String zookeeperAddress) {
        super(application);
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(200, 10);
        zkClient = CuratorFrameworkFactory.newClient(zookeeperAddress, retryPolicy);
        zkClient.start();

        String appPath = ZKPaths.makePath(ZK_ROOT_PATH, application);
        try {
            Stat stat = zkClient.checkExists().forPath(appPath);
            if (stat != null) {
                for (String p : zkClient.getChildren().forPath(appPath)) {
                    String name = parseName(p);
                    String data = new String(zkClient.getData().forPath(p), UTF_8);
                    configs.put(name, data);
                }
            }
        } catch (Exception e) {
            throw new ConfigException(e);
        }

        watcher = new PathChildrenCache(zkClient, appPath, false);
        watcher.getListenable().addListener((client, event) -> {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                switch (event.getType()) {
                    case CHILD_ADDED: {
                        String path = event.getData().getPath();
                        String name = parseName(path);
                        String data = new String(zkClient.getData().forPath(path), UTF_8);
                        configs.put(name, data);
                        condition.signal();
                        subscribers.forEach(s -> s.onConfigAdded(name, data));
                        break;
                    }
                    case CHILD_UPDATED: {
                        String path = event.getData().getPath();
                        String name = parseName(path);
                        String data = new String(zkClient.getData().forPath(path), UTF_8);
                        configs.put(name, data);
                        condition.signal();
                        subscribers.forEach(s -> s.onConfigUpdated(name, data));
                        break;
                    }
                    case CHILD_REMOVED: {
                        String path = event.getData().getPath();
                        String name = parseName(path);
                        configs.remove(name);
                        condition.signal();
                        subscribers.forEach(s -> s.onConfigDeleted(name));
                        break;
                    }
                }
            } finally {
                lock.unlock();
            }
        });
        try {
            watcher.start();
        } catch (Exception e) {
            throw new ConfigException(e);
        }
    }

    @Override
    public String get(String name) {
        return configs.get(checkNotNullOrEmpty(name));
    }

    @Override
    public Map<String, String> getAll() {
        return ImmutableMap.copyOf(configs);
    }

    @Override
    public void set(String name, String value) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            String path = makePath(name);
            Stat stat = zkClient.checkExists().forPath(path);
            if (stat != null) {
                if (value != null)
                    zkClient.setData().forPath(path, value.getBytes(UTF_8));
                else
                    zkClient.setData().forPath(path);
            } else {
                if (value != null)
                    zkClient.create().creatingParentsIfNeeded().forPath(path, value.getBytes(UTF_8));
                else
                    zkClient.create().creatingParentsIfNeeded().forPath(path);
            }
            condition.await();
        } catch (Exception e) {
            throw new ConfigException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void delete(String name) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            String path = makePath(name);
            zkClient.delete().forPath(path);
            condition.await();
        } catch (Exception e) {
            throw new ConfigException(e);
        } finally {
            lock.unlock();
        }
    }

    private String makePath(String name) {
        return ZKPaths.makePath(ZK_ROOT_PATH, application, checkNotNullOrEmpty(name));
    }

    @Override
    public void close() throws IOException {
        watcher.close();
        zkClient.close();
    }

    private String parseName(String path) {
        return path.substring(path.lastIndexOf("/") + 1, path.length());
    }
}
