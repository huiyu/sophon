package io.github.huiyu.sophon;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.List;

import redis.clients.jedis.Jedis;

public class Test {

    public static void main(String[] args) throws Exception {
        Jedis jedis = new Jedis("localhost");
        
        jedis.close();
    }
}
