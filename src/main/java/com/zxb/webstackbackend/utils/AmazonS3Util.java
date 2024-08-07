package com.zxb.webstackbackend.utils;

/**
 * Author: Administrator
 * CreateTime: 2024/7/6
 * Project: webstack-backend
 */
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteBucketRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListBucketsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.zxb.webstackbackend.config.AmazonS3Config;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * aws s3 文件工具类
 *
 * @author Administartor
 */
@Slf4j
@Component
public class AmazonS3Util {

    @Resource
    private AmazonS3 amazonS3;

    private static AmazonS3 s3Client;

    @PostConstruct
    public void init() {
        s3Client = amazonS3;
    }


    /**
     * 上传文件
     * @author Administartor
     * @param multipartFile 文件
     * @param bucketName 桶名
     */
    public static String uploadFile(MultipartFile multipartFile, String bucketName) {
        InputStream inputStream = null;
        try {
            createBucket(bucketName);
            String fileKey = getFileKey(multipartFile.getOriginalFilename());
            inputStream = multipartFile.getInputStream();
            Date expireDate = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30));
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setHttpExpiresDate(expireDate);
            metadata.setContentType(multipartFile.getContentType());
            metadata.setContentLength(multipartFile.getSize());
            PutObjectRequest request = new PutObjectRequest(bucketName, fileKey, inputStream, metadata);
            //配置文件访问权限
            request.withCannedAcl(CannedAccessControlList.BucketOwnerFullControl);
            s3Client.putObject(request);
            return bucketName + "/" + fileKey;
        } catch (Exception e) {
            log.error("uploadFile error {} ", e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    log.error("inputStream close error {} ", e.getMessage());
                }
            }
        }
        return null;
    }


    /**
     * 上传文件
     * @author Administartor
     * @param multipartFile 文件
     * @param bucketName 桶名
     */
    public static String uploadFile(File multipartFile, String bucketName) {
        InputStream inputStream = null;
        try {
            createBucket(bucketName);
            String fileKey = getFileKey(multipartFile.getName());
            Date expireDate = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30));
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setHttpExpiresDate(expireDate);
            PutObjectRequest request = new PutObjectRequest(bucketName, fileKey, multipartFile);
            //配置文件访问权限
            request.withCannedAcl(CannedAccessControlList.BucketOwnerFullControl);
            s3Client.putObject(request);
            return bucketName + "/" + fileKey;
        } catch (Exception e) {
            log.error("uploadFile error {} ", e.getMessage());
        }
        return null;
    }

    /**
     * 分片上传文件
     *
     * @author Administartor
     * @param multipartFile 文件
     * @param bucketName 桶名
     * @return java.lang.String
     */
    public static String multipartUpload(MultipartFile multipartFile, String bucketName) {
        log.info("开始上传");
        InputStream inputStream = null;
        // Set part size to 5 MB.
        long partSize = 5 * 1024 * 1024;
        try {
            String keyName = getFileKey(multipartFile.getOriginalFilename());
            long contentLength =multipartFile.getSize();
            createBucket(bucketName);
            List<PartETag> partETags = new ArrayList<PartETag>();
            // Initiate the multipart upload.
            ObjectMetadata metadata = new ObjectMetadata();
            Date expireDate = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30));
            metadata.setHttpExpiresDate(expireDate);
            metadata.setContentType(multipartFile.getContentType());
            metadata.setContentLength(multipartFile.getSize());
            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, keyName, metadata);
            InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);
            // Upload the file parts.
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // Because the last part could be less than 5 MB, adjust the part size as needed.
                inputStream = multipartFile.getInputStream();
                partSize = Math.min(partSize, (contentLength - filePosition));
                // Create the request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(bucketName)
                        .withKey(keyName)
                        .withUploadId(initResponse.getUploadId())
                        .withPartNumber(i)
                        .withFileOffset(filePosition)
                        .withInputStream(inputStream)
                        .withPartSize(partSize);
                // Upload the part and add the response's ETag to our list.
                UploadPartResult uploadResult = s3Client.uploadPart(uploadRequest);
                partETags.add(uploadResult.getPartETag());

                filePosition += partSize;
            }
            // Complete the multipart upload.
            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucketName, keyName, initResponse.getUploadId(), partETags);
            s3Client.completeMultipartUpload(compRequest);
            return bucketName + "/" + keyName;
        } catch (Exception e) {
            log.error("uploadFile error {} ", e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    log.error("inputStream close error {} ", e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * 分片上传文件
     *
     * @author Administartor
     * @param multipartFile 文件
     * @param bucketName 桶名
     * @return java.lang.String
     */
    public static String heightMultipartUpload(MultipartFile multipartFile, String bucketName) {
        log.info("开始上传");
        String keyName = getFileKey(multipartFile.getOriginalFilename());
        try {
            createBucket(bucketName);
            TransferManager tm = TransferManagerBuilder.standard()
                    .withS3Client(s3Client)
                    .build();
            // TransferManager processes all transfers asynchronously,
            // so this call returns immediately.
            ObjectMetadata metadata = new ObjectMetadata();
            Date expireDate = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30));
            metadata.setHttpExpiresDate(expireDate);
            metadata.setContentType(multipartFile.getContentType());
            metadata.setContentLength(multipartFile.getSize());
            Upload upload = tm.upload(bucketName, keyName, multipartFile.getInputStream(), metadata);
            log.info("Object upload started");
            // Optionally, wait for the upload to finish before continuing.
            upload.waitForCompletion();

            return  bucketName + "/" + keyName;
        } catch (Exception e) {
            log.error("uploadFile error {} ", e.getMessage());
        }
        return null;
    }


    /**
     * 下载
     *
     * @param bucketName 桶名
     * @param fileKey key
     */
    public static InputStream downloadFile(String bucketName, String fileKey) {
        GetObjectRequest request = new GetObjectRequest(bucketName, fileKey);
        assert s3Client != null;
        S3Object response = s3Client.getObject(request);
        return response.getObjectContent();
    }

    /**
     * 删除文件
     *
     * @param bucketName 桶名
     * @param fileKey key
     */
    public static void deleteFile(String bucketName, String fileKey) {
        try {
            DeleteObjectRequest request = new DeleteObjectRequest(bucketName, fileKey);
            s3Client.deleteObject(request);
        } catch (Exception e) {
            log.error("s3Client error {} ", e.getMessage());
        }
    }

    /**
     * Bucket列表
     */
    public static List<Bucket> listFile() {
        ListBucketsRequest request = new ListBucketsRequest();
        assert s3Client != null;
        return s3Client.listBuckets(request);
    }

    /**
     * 是否存在Bucket
     *
     * @param bucketName 桶名
     * @return boolean
     */
    public static boolean isExistBucket(String bucketName) {
        try {
            HeadBucketRequest request = new HeadBucketRequest(bucketName);
            s3Client.headBucket(request);
        } catch (Exception e) {
            log.error("s3Client error {} ", e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 创建Bucket
     *
     * @param bucketName 桶名
     */
    public static void createBucket(String bucketName) {
        boolean isBucketExists = isExistBucket(bucketName);
        if (!isBucketExists) {
            try {
                CreateBucketRequest request = new CreateBucketRequest(bucketName);
                s3Client.createBucket(request);
            } catch (Exception e) {
                log.error("s3Client error {} ", e.getMessage());
            }
        }
    }

    /**
     * 删除Bucket
     *
     * @param bucketName 桶名
     */
    public static void deleteBucket(String bucketName) {
        try {
            DeleteBucketRequest request = new DeleteBucketRequest(bucketName);
            s3Client.deleteBucket(request);
        } catch (Exception e) {
            log.error("s3Client error {} ", e.getMessage());
        }
    }

    /**
     * fileKey是否存在
     *
     * @param bucketName 桶名
     * @param fileKey key
     * @return boolean
     */
    public static boolean isExistFileKey(String bucketName, String fileKey) {
        GetObjectRequest request = new GetObjectRequest(bucketName,fileKey);
        assert s3Client != null;
        S3Object response = s3Client.getObject(request);
        return response != null &&  fileKey.equals(response.getKey());
    }

    /**
     * 获取文件key
     *
     * @param fileName key
     * @return String
     */
    private static String getFileKey(String fileName) {
        String[] names = fileName.split("\\.");
        String fileTypeName = names[names.length - 1];
        return UUID.randomUUID().toString().replaceAll("-","") + "." + fileTypeName;
    }

}
