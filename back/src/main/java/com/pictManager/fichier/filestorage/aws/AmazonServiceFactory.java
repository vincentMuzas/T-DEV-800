package com.pictManager.fichier.filestorage.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Component
@Profile({"prod", "test"})
public class AmazonServiceFactory implements IAmazonServiceFactory {
    private final Logger log = LoggerFactory.getLogger(AmazonServiceFactory.class);

    @Override
    public AmazonS3 creatS3Client(String endpoint, String region, String access, String secret) {
        log.debug("Creating S3 client WITH credentials");

        BasicAWSCredentials credentials = new BasicAWSCredentials(access, secret);
        AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(credentials);

        AwsClientBuilder.EndpointConfiguration conf = new AwsClientBuilder.EndpointConfiguration(endpoint, region);

        return AmazonS3ClientBuilder.standard().withEndpointConfiguration(conf).withCredentials(provider).build();
    }
}
