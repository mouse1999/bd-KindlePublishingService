package com.amazon.ata.kindlepublishingservice.dagger;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.publishing.*;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Singleton;

@Module
public class PublishingModule {

    @Provides
    @Singleton
    public BookPublisher provideBookPublisher(ScheduledExecutorService scheduledExecutorService, BookPublishTask bookPublishTask) {
        return new BookPublisher(scheduledExecutorService, bookPublishTask );
    }

    @Provides
    @Singleton
    public ScheduledExecutorService provideBookPublisherScheduler() {
        return Executors.newScheduledThreadPool(1);
    }

    @Provides
    @Singleton
    public BookPublishRequestManager provideBookPublishRequestManager() {
        Queue<BookPublishRequest> requestQueue = new ConcurrentLinkedQueue<>();
        return new BookPublishRequestManager(requestQueue);
    }

    @Provides
    @Singleton
    public BookPublishTask provideBookPublishTask(BookPublishRequestManager bookPublishRequestManager,
                                                  PublishingStatusDao publishingStatusDao,
                                                  CatalogDao catalogDao) {
        return new BookPublishTask(bookPublishRequestManager, publishingStatusDao, catalogDao);
    }


}
