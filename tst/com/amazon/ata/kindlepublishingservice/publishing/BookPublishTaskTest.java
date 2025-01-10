package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.recommendationsservice.types.BookGenre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookPublishTaskTest {

    @Mock
    private BookPublishRequestManager mockRequestManager;

    @Mock
    private PublishingStatusDao mockPublishingStatusDao;

    @Mock
    private CatalogDao mockCatalogDao;

    @InjectMocks
    private BookPublishTask bookPublishTask;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void testRun_NoRequest() {
        // GIVEN
        when(mockRequestManager.getBookPublishRequestToProcess()).thenReturn(null);

        // WHEN
        bookPublishTask.run();

        // THEN
        verify(mockRequestManager).getBookPublishRequestToProcess();
        verifyNoInteractions(mockPublishingStatusDao, mockCatalogDao);
    }

    @Test
    void testRun_SuccessfulPublish() {
        // GIVEN
        BookPublishRequest bookPublishRequest = BookPublishRequest.builder()
                .withPublishingRecordId("record123")
                .withBookId("book123")
                .withTitle("The Art of Programming")
                .withAuthor("John Doe")
                .withText("This is a comprehensive guide to programming.")
                .withGenre(BookGenre.SCIENCE_FICTION)
                .build();

        KindleFormattedBook mockFormattedBook = KindleFormatConverter.format(bookPublishRequest);

        CatalogItemVersion catalogItemVersion = new CatalogItemVersion();
        catalogItemVersion.setBookId(mockFormattedBook.getBookId());
        catalogItemVersion.setVersion(2);
        catalogItemVersion.setInactive(false);
        catalogItemVersion.setTitle(mockFormattedBook.getTitle());
        catalogItemVersion.setAuthor(mockFormattedBook.getAuthor());
        catalogItemVersion.setText(mockFormattedBook.getText());
        catalogItemVersion.setGenre(BookGenre.SCIENCE_FICTION);

        when(mockRequestManager.getBookPublishRequestToProcess()).thenReturn(bookPublishRequest);
        when(mockCatalogDao.createOrUpdate(mockFormattedBook)).thenReturn(catalogItemVersion);

        // Create an ArgumentCaptor for KindleFormattedBook
        ArgumentCaptor<KindleFormattedBook> captor = ArgumentCaptor.forClass(KindleFormattedBook.class);

        // WHEN
        bookPublishTask.run();

        // THEN
        verify(mockCatalogDao).createOrUpdate(captor.capture());
        KindleFormattedBook capturedBook = captor.getValue();

        // Verify that the captured book matches the expected properties
        assertNotNull(capturedBook);
        assertEquals("book123", capturedBook.getBookId());
        assertEquals("The Art of Programming", capturedBook.getTitle());
        assertEquals("John Doe", capturedBook.getAuthor());
        assertEquals("This is a comprehensive guide to programming.", capturedBook.getText());
        assertEquals(BookGenre.SCIENCE_FICTION, capturedBook.getGenre());
    }

}