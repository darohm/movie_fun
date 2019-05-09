package org.superbiz.moviefun;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.apigateway.model.Op;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

public class S3Store implements BlobStore {
    String bucketName;
    AmazonS3Client s3Client;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
        this.s3Client = s3Client;
        bucketName = photoStorageBucket;
    }

    @Override
    public void put(Blob blob) throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(blob.contentType);
        this.s3Client.putObject(bucketName, blob.name, blob.inputStream, objectMetadata);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        if(s3Client.doesObjectExist(bucketName, name)){
            S3Object s3Object = this.s3Client.getObject(bucketName, name);
            return Optional.of(new Blob(s3Object.getKey(), s3Object.getObjectContent(), s3Object.getObjectMetadata().getContentType()));
        }else return null;
    }

    @Override
    public void deleteAll() {
        ObjectListing objectListing = s3Client.listObjects(bucketName);
        while(true){
            Iterator<S3ObjectSummary> onjIter = objectListing.getObjectSummaries().iterator();
            while (onjIter.hasNext()){
                s3Client.deleteObject(bucketName, onjIter.next().getKey());
            }
            if (objectListing.isTruncated()) {
                objectListing = s3Client.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }
    }
}
