package com.pictManager.fichier.filestorage.aws;

import com.amazonaws.services.s3.AmazonS3;

public interface IAmazonServiceFactory {

    AmazonS3 creatS3Client(String endpoint, String region, String access, String secret);
}
