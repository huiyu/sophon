package io.github.huiyu.sophon;

import org.apache.curator.test.TestingServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ZookeeperConfigClientTest {

    private static TestingServer server;

    @BeforeClass
    public static void setUp() throws Exception {
        server = new TestingServer(true);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.close();
    }

    @Test
    public void testBasic() throws Exception {
        ZookeeperConfigClient configClient =
                new ZookeeperConfigClient("app1", server.getConnectString());
        ConfigClientTests.testBasic(configClient);
        configClient.close();
    }

    @Test
    public void testSubscriber() throws Exception {
        ZookeeperConfigClient configClient =
                new ZookeeperConfigClient("app2", server.getConnectString());
        ConfigClientTests.testSubscriber(configClient);
        configClient.close();
    }
}
