package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.converters.BookPublishRequestConverter;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequestManager;
import com.amazon.ata.recommendationsservice.types.BookGenre;
import com.amazon.ata.kindlepublishingservice.models.requests.SubmitBookForPublishingRequest;
import com.amazon.ata.kindlepublishingservice.models.response.SubmitBookForPublishingResponse;
import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequest;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class SubmitBookForPublishingActivityTest {

    @Mock
    private PublishingStatusDao publishingStatusDao;

    @Mock
    private CatalogDao catalogDao;
    @Mock
    private BookPublishRequestManager bookPublishRequestManager;

    @InjectMocks
    private SubmitBookForPublishingActivity activity;

    @BeforeEach
    public void setup() {
        initMocks(this);
    }

    @Test
    public void execute_bookIdInRequest_bookQueuedForPublishing() {
        // GIVEN
        SubmitBookForPublishingRequest request = SubmitBookForPublishingRequest.builder()
                .withAuthor("Author")
                .withTitle("Title")
                .withBookId("book.123")
                .withGenre(BookGenre.FANTASY.name())
                .build();

        PublishingStatusItem item = new PublishingStatusItem();
        item.setPublishingRecordId("publishing.123");
        // KindlePublishingUtils generates a random publishing status ID for us
        when(publishingStatusDao.setPublishingStatus(anyString(),
                eq(PublishingRecordStatus.QUEUED),
                eq(request.getBookId()))).thenReturn(item);

        // WHEN
        SubmitBookForPublishingResponse response = activity.execute(request);

        // THEN
        assertEquals("publishing.123", response.getPublishingRecordId(), "Expected response to return a publishing" +
                "record id.");
    }

    @Test
    public void execute_noBookIdInRequest_bookQueuedForPublishing() {
        // GIVEN
        SubmitBookForPublishingRequest request = SubmitBookForPublishingRequest.builder()
                .withAuthor("Author")
                .withTitle("Title")
                .withGenre(BookGenre.FANTASY.name())
                .build();

        PublishingStatusItem item = new PublishingStatusItem();
        item.setPublishingRecordId("publishing.123");
        when(publishingStatusDao.setPublishingStatus(anyString(),
                eq(PublishingRecordStatus.QUEUED),
                isNull())).thenReturn(item);

        // WHEN
        SubmitBookForPublishingResponse response = activity.execute(request);

        // THEN
        assertEquals("publishing.123", response.getPublishingRecordId(), "Expected response to return a publishing" +
                "record id.");
    }

    @Test
    public void execute_inValidBookIdInRequest_throwBookNotException() {
        // GIVEN
        SubmitBookForPublishingRequest request = SubmitBookForPublishingRequest.builder()
                .withAuthor("Author")
                .withTitle("Title")
                .withBookId("invalidBookId")
                .withGenre(BookGenre.FANTASY.name())
                .build();

        PublishingStatusItem item = new PublishingStatusItem();
        item.setPublishingRecordId("publishing.123");

        doThrow(BookNotFoundException.class).when(catalogDao).validateBookExists(request.getBookId());

        assertThrows(BookNotFoundException.class, () -> activity.execute(request),
                "Expected BookNotFoundException to be thrown for an invalid bookId.");

    }

    @Test
    public void execute_activeBookInRequest_bookQueuedForPublishing() {

        // GIVEN
        SubmitBookForPublishingRequest request = SubmitBookForPublishingRequest.builder()
                .withAuthor("Author")
                .withTitle("Title")
                .withBookId("book.123")
                .withGenre(BookGenre.FANTASY.name())
                .build();

        BookPublishRequest bookPublishRequest = BookPublishRequestConverter.toBookPublishRequest(request);

        CatalogItemVersion bookItem = new CatalogItemVersion();
        bookItem.setBookId("book.123");
        bookItem.setInactive(false);

        PublishingStatusItem item = new PublishingStatusItem();
        item.setPublishingRecordId("publishing.123");

        when(catalogDao.getBookFromCatalog(request.getBookId())).thenReturn(bookItem);


        when(publishingStatusDao.setPublishingStatus(anyString(),
                eq(PublishingRecordStatus.QUEUED),
                isNull())).thenReturn(item);


        // WHEN
        SubmitBookForPublishingResponse response = activity.execute(request);

        verify(bookPublishRequestManager).addBookPublishRequest(bookPublishRequest);
        verify(bookPublishRequestManager).addBookPublishRequest(bookPublishRequest);



    }

}
