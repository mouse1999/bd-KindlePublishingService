package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.requests.SubmitBookForPublishingRequest;
import com.amazon.ata.kindlepublishingservice.models.response.SubmitBookForPublishingResponse;
import com.amazon.ata.kindlepublishingservice.converters.BookPublishRequestConverter;
import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequest;

import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequestManager;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

/**
 * Implementation of the SubmitBookForPublishingActivity for ATACurriculumKindlePublishingService's
 * SubmitBookForPublishing API.
 *
 * This API allows the client to submit a new book to be published in the catalog or update an existing book.
 */
public class SubmitBookForPublishingActivity {

    private PublishingStatusDao publishingStatusDao;
    private CatalogDao catalogDao;
    private BookPublishRequestManager requestManager;

    /**
     * Instantiates a new SubmitBookForPublishingActivity object.
     *
     * @param publishingStatusDao PublishingStatusDao to access the publishing status table.
     */
    @Inject
    public SubmitBookForPublishingActivity(PublishingStatusDao publishingStatusDao, CatalogDao catalogDao,
                                           BookPublishRequestManager requestManager) {
        this.publishingStatusDao = publishingStatusDao;
        this.catalogDao = catalogDao;
        this.requestManager = requestManager;
    }

    /**
     * Submits the book in the request for publishing.
     *
     * @param request Request object containing the book data to be published. If the request is updating an existing
     *                book, then the corresponding book id should be provided. Otherwise, the request will be treated
     *                as a new book.
     * @return SubmitBookForPublishingResponse Response object that includes the publishing status id, which can be used
     * to check the publishing state of the book.
     */
    public SubmitBookForPublishingResponse execute(SubmitBookForPublishingRequest request) {
        final BookPublishRequest bookPublishRequest = BookPublishRequestConverter.toBookPublishRequest(request);

        if(bookPublishRequest.getBookId() != null) {
            catalogDao.validateBookExists(bookPublishRequest.getBookId());

        }
        requestManager.addBookPublishRequest(bookPublishRequest);


        PublishingStatusItem item =  publishingStatusDao.setPublishingStatus(bookPublishRequest.getPublishingRecordId(),
                PublishingRecordStatus.QUEUED,
                bookPublishRequest.getBookId());

        return SubmitBookForPublishingResponse.builder()
                .withPublishingRecordId(item.getPublishingRecordId())
                .build();
    }
}
