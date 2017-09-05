package com.glassdoor.planout4j.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.typesafe.config.Config;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

import static java.util.Objects.requireNonNull;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

/**
 * Manages reading and writing of Planout4j configuration from / to <a href="http://redis.io">Redis</a> NoSQL store.
 */
public class Planout4jConfigRedisBackend implements Planout4jConfigBackend {

    private JedisCluster jedisCluster;
    private String redisKey;

    public Planout4jConfigRedisBackend() {
    }

    public Planout4jConfigRedisBackend(final JedisCluster jedisCluster, final String redisKey) {
        this.jedisCluster = requireNonNull(jedisCluster);
        this.redisKey = defaultIfEmpty(redisKey, "planout4j");
    }

    @Override
    public void configure(final Config config) {
        String hostAndPort = config.getString("hostAndPort");
        String[] split = hostAndPort.split(";");

        Set<HostAndPort> nodes = new HashSet<>();
        for (String tmp : split) {
            String[] split1 = tmp.split(":");
            String host = split1[0];
            int port = Integer.parseInt(split1[1]);
            HostAndPort node = new HostAndPort(host, port);
            nodes.add(node);
        }
        jedisCluster = new JedisCluster(nodes);
        redisKey = config.getString("key");
    }

    @Override
    public Map<String, String> loadAll() {
        return jedisCluster.hgetAll(redisKey);
    }

    @Override
    public void persist(final Map<String, String> configData) {
        jedisCluster.del(redisKey);
        if (configData != null && !configData.isEmpty()) {
            jedisCluster.hmset(redisKey, configData);
        }
    }

    @Override
    public String persistenceLayer() {
        return "REDIS";
    }

    @Override
    public String persistenceDestination() {
        //noinspection resource
        Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
        return String.format("%s @ %s:%s, key = %s", persistenceLayer(),
                clusterNodes.keySet(), clusterNodes.values(), redisKey);
    }

}
