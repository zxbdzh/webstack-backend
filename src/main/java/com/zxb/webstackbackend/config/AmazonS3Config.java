package com.zxb.webstackbackend.config;

/*
  Author: Administrator
  CreateTime: 2024/7/6
  Project: webstack-backend
 */

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AmazonS3Config {

    /**
     * S3服务器地址
     */
    @Value("${aws.s3.s3Url}")
    @Getter
    private String s3Url;

    /**
     * 账号key
     */
    @Value("${aws.s3.accessKey}")
    private String accessKey;

    /**
     * 秘钥
     */
    @Value("${aws.s3.secretKey}")
    private String secretKey;


    /**
     * 初始化生成AmazonS3 客户端配置
     */
    @Bean(name = "amazonS3")
    public AmazonS3 getAmazonS3() {
        log.info("start create s3Client");
        ClientConfiguration config = new ClientConfiguration();
        // HTTPS or HTTP
        config.withProtocol(Protocol.HTTPS);
        // 设置AmazonS3使用的最大连接数
        config.setMaxConnections(200);
        // 设置socket超时时间
        config.setSocketTimeout(10000);
        // 设置失败请求重试次数
        config.setMaxErrorRetry(2);
        log.info("create s3Client success");
        return AmazonS3ClientBuilder.standard()
                // US_EAST_2 根据自己亚马逊服务器所在区域
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s3Url, Regions.US_EAST_2.getName()))
                .withClientConfiguration(config)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withPathStyleAccessEnabled(true)
                .disableChunkedEncoding()
                .withForceGlobalBucketAccessEnabled(true)
                .build();
    }


    @Bean(name = "transferManager")
    public TransferManager getTransferManager() {
        return TransferManagerBuilder.standard().withS3Client(getAmazonS3()).build();
    }
}
