package io.github.huiyu.sophon;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import redis.embedded.RedisServer;

public class RedisConfigClientTest {
    
    private static RedisServer redisServer;

    @BeforeClass
    public static void setUp() throws Exception {
        redisServer = new RedisServer(6379);
        redisServer.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        redisServer.stop();
    }

    @Test
    public void testBasic() throws Exception {
        RedisConfigClient configClient = new RedisConfigClient("test_app", "localhost", 6379);
        ConfigClientTests.testBasic(configClient);
        configClient.close();
    }
    
    @Test
    public void testSubscriber() throws Exception {
        RedisConfigClient configClient = new RedisConfigClient("test_app", "localhost", 6379);
        ConfigClientTests.testSubscriber(configClient);
        configClient.close();
    }
    
    @Test
    public void testMessage() throws Exception {
        RedisConfigClient.Message original = 
                new RedisConfigClient.Message(RedisConfigClient.Message.TYPE_ADD, "name", "value");
        assertEquals(original, RedisConfigClient.Message.decode(original.encode()));
        
        original = new RedisConfigClient.Message(RedisConfigClient.Message.TYPE_DELETE, "name");
        assertEquals(original, RedisConfigClient.Message.decode(original.encode()));
    }
}
