package io.github.huiyu.sophon;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ConfigClientTests {


    public static void testBasic(ConfigClient configClient) throws Exception {
        assertNull(configClient.get("key"));

        // create
        configClient.set("key", "value");
        assertEquals("value", configClient.get("key"));

        // update
        configClient.set("key", "new_value");
        assertEquals("new_value", configClient.get("key"));

        // delete
        configClient.delete("key");
        System.out.println("test delete");
        assertNull(configClient.get("key"));
    }

    public static void testSubscriber(ConfigClient configClient) throws Exception {
        TestSubscriber subscriber = new TestSubscriber();

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

        configClient.removeSubscriber(subscriber);
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

    private static synchronized void await() {
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
        }
    }
}
