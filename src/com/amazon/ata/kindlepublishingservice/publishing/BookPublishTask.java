package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

public class BookPublishTask implements Runnable{
    private static final Logger LOGGER = LogManager.getLogger(BookPublisher.class);
    private final BookPublishRequestManager bookPublishRequestManager;
    private final PublishingStatusDao publishingStatusDao;
    private final CatalogDao catalogDao;

    @Inject
    public BookPublishTask(BookPublishRequestManager bookPublishRequestManager,
                           PublishingStatusDao publishingStatusDao,
                           CatalogDao catalogDao) {
        this.bookPublishRequestManager = bookPublishRequestManager;
        this.publishingStatusDao = publishingStatusDao;
        this.catalogDao = catalogDao;
    }
    @Override
    public void run() {
        LOGGER.info("Book Publish Task executed.");
        BookPublishRequest retrievedRequest = bookPublishRequestManager.getBookPublishRequestToProcess();
        if (retrievedRequest == null) {
            return;
        } else {
            publishingStatusDao.setPublishingStatus(retrievedRequest.getPublishingRecordId(),
                    PublishingRecordStatus.IN_PROGRESS,
                    retrievedRequest.getBookId());

        }
        KindleFormattedBook formattedBook = KindleFormatConverter.format(retrievedRequest);

        try {
            CatalogItemVersion updatedOrCreatedBook = catalogDao.createOrUpdate(formattedBook);

            publishingStatusDao.setPublishingStatus(retrievedRequest.getPublishingRecordId(),
                    PublishingRecordStatus.SUCCESSFUL,
                    updatedOrCreatedBook.getBookId());

        }catch (BookNotFoundException e) {
            publishingStatusDao.setPublishingStatus(retrievedRequest.getPublishingRecordId(),
                    PublishingRecordStatus.FAILED, formattedBook.getBookId()
                    );


        }catch (RuntimeException e) {
            publishingStatusDao.setPublishingStatus(retrievedRequest.getPublishingRecordId(),
                    PublishingRecordStatus.FAILED,
                    retrievedRequest.getBookId());

        }

        //create or update method
        //update publishing status to show successful
    }
}
