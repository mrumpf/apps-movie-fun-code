package org.superbiz.moviefun.albums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.UUID;
import java.util.concurrent.locks.Lock;

@Configuration
@EnableAsync
@EnableScheduling
public class AlbumsUpdateScheduler {

    private static final long SECONDS = 1000;
    private static final long MINUTES = 60 * SECONDS;

    private final AlbumsUpdater albumsUpdater;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String registryKey = UUID.randomUUID().toString();

    public AlbumsUpdateScheduler(AlbumsUpdater albumsUpdater) {
        this.albumsUpdater = albumsUpdater;
    }

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Scheduled(initialDelay = 15 * SECONDS, fixedRate = 2 * MINUTES)
    public void run() {
        RedisLockRegistry registry = new RedisLockRegistry(redisConnectionFactory, this.registryKey);
        Lock lock = registry.obtain("album-update");
        logger.debug("Obtaining lock...");
        lock.lock();
        logger.debug("... got lock!");
        try {
            logger.debug("Starting albums update");
            albumsUpdater.update();
            logger.debug("Finished albums update");
        } catch (Throwable e) {
            logger.error("Error while updating albums", e);
        }
        finally {
            logger.debug("Releasing lock...");
            lock.unlock();
            logger.debug("... released lock!");
        }
    }
}
