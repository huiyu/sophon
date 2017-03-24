package io.github.huiyu.sophon;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;

public class ZookeeperConfigClientTest {

    private TestingServer server;
    private CuratorFramework zkClient;

    @Before
    public void setUp() throws Exception {
        server = new TestingServer(true);
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(200, 10);
        zkClient = CuratorFrameworkFactory.newClient(server.getConnectString(), retryPolicy);
        zkClient.start();
    }

    @After
    public void tearDown() throws Exception {
        zkClient.close();
        server.close();
    }
}
