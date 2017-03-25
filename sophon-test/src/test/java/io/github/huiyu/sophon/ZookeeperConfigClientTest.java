package io.github.huiyu.sophon;

import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ZookeeperConfigClientTest {

    private TestingServer server;

    @Before
    public void setUp() throws Exception {
        server = new TestingServer(true);
    }

    @After
    public void tearDown() throws Exception {
        server.close();
    }

    @Test
    public void testBasic() throws Exception {
        ZookeeperConfigClient configClient =
                new ZookeeperConfigClient("test_app", server.getConnectString());
        assertNull(configClient.get("key"));

        // create
        configClient.set("key", "value");
        assertEquals("value", configClient.get("key"));

        // update
        configClient.set("key", "new_value");
        assertEquals("new_value", configClient.get("key"));

        // delete
        configClient.delete("key");
        assertNull(configClient.get("key"));


        configClient.close();
    }

    @Test
    public void testSubscriber() throws Exception {
        TestSubscriber subscriber = new TestSubscriber();

        ZookeeperConfigClient configClient =
                new ZookeeperConfigClient("test_app", server.getConnectString());
        configClient.addSubscriber(subscriber);

        assertNull(subscriber.configsAdded.get("key"));
        configClient.set("key", "value");
        await();
        assertEquals("value", subscriber.configsAdded.get("key"));

        configClient.set("key", "new_value");
        await();
        assertEquals("new_value", subscriber.configsUpdated.get("key"));
        
        configClient.delete("key");
        await();
        assertTrue(subscriber.configsDeleted.contains("key"));
    }
    
    private synchronized void await() {
        try {
            wait(500L);
        } catch (InterruptedException e) {
        }
    }

    private static class TestSubscriber implements ConfigSubscriber {
        Map<String, String> configsAdded = new HashMap<>();
        Map<String, String> configsUpdated = new HashMap<>();
        Set<String> configsDeleted = new HashSet<>();

        @Override
        public void onConfigAdded(String name, String data) {
            configsAdded.put(name, data);
        }

        @Override
        public void onConfigUpdated(String name, String data) {
            configsUpdated.put(name, data);
        }

        @Override
        public void onConfigDeleted(String name) {
            configsDeleted.add(name);
        }
    }
}
