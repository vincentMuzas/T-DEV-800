package com.pictManager.fichier.filestorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.pictManager.fichier.Fichier;
import com.pictManager.fichier.filestorage.aws.IAmazonServiceFactory;
import com.pictManager.fichier.filestorage.exception.CannotFindFileException;
import com.pictManager.fichier.filestorage.exception.FichierMustHaveIdBeforeStoringException;
import com.pictManager.fichier.filestorage.exception.FileAlreadyExistException;

@Service
public class FileStorageOnAwsS3 implements FileStorage {

    // private static final String S3_FILE_DELIMITER = "/";
    private static final String TMP_FILE_PREFIX = "tmp-";
    private final Logger log = LoggerFactory.getLogger(FileStorageOnAwsS3.class);
    private final IAmazonServiceFactory amazonServiceFactory;
    private final int DEFAULT_NUM_OF_DAYS_TO_EXPIRE = 6;

    @Value("${aws.s3.bucket}")
    private String s3BucketName;

    @Value("${aws.s3.region}")
    private String s3Region;

    @Value("${aws.s3.endpoint}")
    private String s3endpoint;

    @Value("${aws.s3.credentials.access}")
    private String credentialsAccess;

    @Value("${aws.s3.credentials.secret}")
    private String credentialsSecret;

    @Value("${aws.s3.readlimit}")
    private Integer s3ReadLimit;

    public FileStorageOnAwsS3(IAmazonServiceFactory amazonServiceFactory) {
        this.amazonServiceFactory = amazonServiceFactory;
    }

    @Override
    public void store(Fichier fichier, File file) throws Exception {
        if (fichier.getId() == null) {
            throw new FichierMustHaveIdBeforeStoringException();
        }

        this.pushFileOnBucket(fichier, file);
    }

    @Override
    public void store(Fichier fichier, InputStream inputStream) throws Exception {
        if (fichier.getId() == null) {
            throw new FichierMustHaveIdBeforeStoringException();
        }

        this.pushFileOnBucketWithInputsstream(fichier, inputStream);
    }

    @Override
    public File retriever(Fichier fichier) throws Exception {
        return this.pullFileFromBucket(fichier);
    }

    @Override
    public void delete(Fichier fichier) {
        this.deleteFileFromBucket(fichier.getFullPath());
    }

    @Override
    public boolean exists(Fichier fichier) {
        return this.exists(fichier.getFullPath());
    }

    public boolean exists(String path) {
        log.debug("exitst: {}", path);
        try {
            return this.createS3Client().doesObjectExist(s3BucketName, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getPublicUrl(Fichier fichier, Date expiration) {
        expiration = expiration != null ? expiration : Date.from(Instant.now().plus(DEFAULT_NUM_OF_DAYS_TO_EXPIRE, ChronoUnit.DAYS));
        return this.generateS3PublicUrl(fichier, expiration);
    }

    public String getPublicUrl(Fichier fichier) {
        return this.getPublicUrl(fichier, null);
    }

    public File pullFileFromBucket(Fichier fichier) throws Exception {
        if (!this.exists(fichier)) {
            throw new CannotFindFileException(fichier.getFullPath());
        }
        File tmpFile = File.createTempFile(TMP_FILE_PREFIX, fichier.getNomUnique());
        tmpFile.deleteOnExit();
        S3Object s3Object = this.createS3Client().getObject(new GetObjectRequest(s3BucketName, fichier.getFullPath()));
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        FileUtils.copyInputStreamToFile(inputStream, tmpFile);
        return tmpFile;
    }

    private void deleteFileFromBucket(String fullPath) {
        DeleteObjectRequest request = new DeleteObjectRequest(s3BucketName, fullPath);
        this.createS3Client().deleteObject(request);
    }

    private void pushFileOnBucket(Fichier fichier, File file) throws Exception {
        InputStream inputStream = new FileInputStream(file);
        this.pushFileOnBucketWithInputsstream(fichier, inputStream);
    }

    private void pushFileOnBucketWithInputsstream(Fichier fichier, InputStream inputStream) throws Exception {
        String fullPath;

        try {
            fullPath = this.buildS3FileName(fichier);
            if (this.exists(fullPath)) {
                throw new FileAlreadyExistException(fichier.getFullPath());
            }
            PutObjectRequest request = new PutObjectRequest(s3BucketName, fullPath, inputStream, new ObjectMetadata());
            if (s3ReadLimit != null && s3ReadLimit > 0) { // this is for prod
                request.getRequestClientOptions().setReadLimit(s3ReadLimit);
            }
            AmazonS3 client = this.createS3Client();
            client.putObject(request);
        } catch (AmazonServiceException e) {
            log.error("Exception occured while pushing inputstream on bucket: " +
                "Exception: {} : {} -> {}", e.getClass().getName(), e.getCause(), e.getMessage());
            throw e;
        }
    }

    private AmazonS3 createS3Client() {
        return amazonServiceFactory.creatS3Client(s3endpoint, s3Region, credentialsAccess, credentialsSecret);
    }

    private String buildS3FileName(Fichier fichier) {
        return fichier.getNomUnique();
    }

    private String generateS3PublicUrl(Fichier fichier, Date expiration) {
        log.debug("Create public url from {} until {}", fichier.getNomUnique(), expiration);
        String key = this.buildS3FileName(fichier);
        try {
            // generate public url, pre-signed
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(s3BucketName, key)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);
            URL url = this.createS3Client().generatePresignedUrl(generatePresignedUrlRequest);

            log.debug("Public url generated: {}", url.toString());

            return url.toString();
        } catch (AmazonServiceException e) {
            log.error("Amazon couldn't execute S3publicUrl creation order. Exception: {} : {} -> {}",
                e.getClass().getName(), e.getCause(), e.getMessage());
            throw e;
        } catch (SdkClientException e) {
            log.error("Couldn't reach amazon or couldn't parse response. Exception: {} : {} -> {}",
                e.getClass().getName(), e.getCause(), e.getMessage());
            throw e;
        }
    }
}
