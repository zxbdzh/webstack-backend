package com.zxb.webstackbackend.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zxb.webstackbackend.mp.pojo.TLabel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
    /**
     * 缓存标签
     * @return
     */
    @Bean
    public Cache<Integer, TLabel> tLabelCache() {
       return Caffeine.newBuilder()
               .initialCapacity(100)
               .maximumSize(1_000)
               .build();
    }
}
