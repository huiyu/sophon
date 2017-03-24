package io.github.huiyu.sophon;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.embedded.RedisServer;

public class RedisConfigClientTest {
    
    private RedisServer redisServer;

    @Before
    public void setUp() throws Exception {
        redisServer = new RedisServer(6379);
        redisServer.start();
    }

    @After
    public void tearDown() throws Exception {
        redisServer.stop();
    }

    @Test
    public void testBasic() throws Exception {
        RedisConfigClient configClient = new RedisConfigClient("test_app", "localhost", 6379);

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
}
