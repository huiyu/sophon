package io.github.huiyu.sophon;

import org.apache.curator.shaded.com.google.common.base.Charsets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisConfigClient extends AbstractConfigClient {

    private static final Charset UTF_8 = Charsets.UTF_8;
    private static final String CHANNEL_NAME = "sophon";

    private final Jedis jedis;
    private final String key;

    private final String host;
    private final int port;
    
    private final Subscriber subscriber;

    public RedisConfigClient(String application, String host, int port) {
        super(application);
        this.host = host;
        this.port = port;
        this.key = "sophon_" + application;
        this.jedis = new Jedis(host, port);
        this.subscriber = new Subscriber();
        this.subscriber.start();
    }

    @Override
    public String get(String name) {
        return jedis.hget(key, checkNotNullOrEmpty(name));
    }

    @Override
    public Map<String, String> getAll() {
        return jedis.hgetAll(key);
    }

    @Override
    public void set(String name, String value) {
        checkNotNullOrEmpty(name);
        boolean isAdd = this.get(name) == null;

        jedis.hset(key, name, value);

        String msg = new Message(
                isAdd ? Message.TYPE_ADD : Message.TYPE_UPDATE, name, value).encode();
        jedis.publish(CHANNEL_NAME, msg);
    }

    @Override
    public void delete(String name) {
        jedis.hdel(key, name);
        String msg = new Message(Message.TYPE_DELETE, name).encode();
        jedis.publish(CHANNEL_NAME, msg);
    }

    @Override
    public void close() throws IOException {
        subscriber.shutdown();
        jedis.close();
    }

    protected static final class Message {

        static final byte TYPE_ADD = 1;
        static final byte TYPE_UPDATE = 2;
        static final byte TYPE_DELETE = 3;

        byte type;
        String name;
        String data;

        Message(byte type, String name) {
            this(type, name, "");
        }

        Message(byte type, String name, String data) {
            this.type = type;
            this.name = name;
            this.data = data;
        }

        static Message decode(String s) {
            ByteBuffer buf = ByteBuffer.wrap(s.getBytes(UTF_8));
            byte type = buf.get();
            byte[] name = new byte[buf.getInt()];
            byte[] data = new byte[buf.getInt()];
            buf.get(name);
            buf.get(data);
            return new Message(type, new String(name, UTF_8), new String(data));
        }

        String encode() {
            ByteBuffer buf = ByteBuffer.allocate(name.length() + data.length() + 9);
            buf.put(type);
            buf.putInt(name.length());
            buf.putInt(data.length());
            buf.put(name.getBytes(UTF_8));
            buf.put(data.getBytes(UTF_8));
            return new String(buf.array(), UTF_8);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Message message = (Message) o;
            return type == message.type &&
                    Objects.equals(name, message.name) &&
                    Objects.equals(data, message.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, name, data);
        }
    }

    private class Subscriber extends Thread {

        private final Jedis jedis;
        private final JedisPubSub jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                Message decode = Message.decode(message);
                switch (decode.type) {
                    case Message.TYPE_ADD: {
                        subscribers.forEach(s -> s.onConfigAdded(decode.name, decode.data));
                        break;
                    }
                    case Message.TYPE_UPDATE: {
                        subscribers.forEach(s -> s.onConfigUpdated(decode.name, decode.data));
                        break;
                    }
                    case Message.TYPE_DELETE: {
                        subscribers.forEach(s -> s.onConfigDeleted(decode.name));
                        break;
                    }
                }
            }
        };

        public Subscriber() {
            super("thread-config-subscriber:" + application);
            this.jedis = new Jedis(host, port);
        }

        @Override
        public void run() {
            jedis.subscribe(jedisPubSub, CHANNEL_NAME);
        }

        public void shutdown() {
            jedisPubSub.unsubscribe();
        }
    }
}
