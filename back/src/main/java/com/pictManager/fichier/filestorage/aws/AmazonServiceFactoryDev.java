package com.pictManager.fichier.filestorage.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Component
@Profile({"dev", "default"})
public class AmazonServiceFactoryDev implements IAmazonServiceFactory{
    private final Logger log = LoggerFactory.getLogger(AmazonServiceFactoryDev.class);

    @Override
    public AmazonS3 creatS3Client(String endpoint, String region, String access, String secret) {
        log.debug("Creating S3 client for dev");

        final AwsClientBuilder.EndpointConfiguration endpointConf = new AwsClientBuilder.EndpointConfiguration(endpoint, region);
        return AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(endpointConf)
            .withPathStyleAccessEnabled(true) // for localstack
            .build();
    }
}
