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

    private BookPublishTask bookPublishTask;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bookPublishTask = new BookPublishTask(mockRequestManager, mockPublishingStatusDao, mockCatalogDao);
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
        // Arrange
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

        // Act
        bookPublishTask.run();

        // Assert
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

//    @Test
//    void testRun_BookNotFoundException() {
//        // Arrange
//        BookPublishRequest mockRequest = mock(BookPublishRequest.class);
//        KindleFormattedBook mockFormattedBook = mock(KindleFormattedBook.class);
//
//        when(mockRequestManager.getBookPublishRequestToProcess()).thenReturn(mockRequest);
//        when(mockRequest.getPublishingRecordId()).thenReturn("record123");
//        when(mockRequest.getBookId()).thenReturn("book123");
//
//        when(mockCatalogDao.createOrUpdate(any(KindleFormattedBook.class))).thenThrow(BookNotFoundException.class);
//
//        // Act
//        assertThrows(BookNotFoundException.class, () -> bookPublishTask.run());
//
////        // Assert
////        verify(mockPublishingStatusDao).setPublishingStatus(
////                "record123",
////                PublishingRecordStatus.IN_PROGRESS,
//////                "book123"
//////        );
//////        verify(mockCatalogDao).createOrUpdate(any(KindleFormattedBook.class));
//////        verify(mockPublishingStatusDao).setPublishingStatus(
//////                "record123",
//////                PublishingRecordStatus.FAILED,
//////                null // or the formatted book ID if it’s not null
//////        );
//    }
//
//    @Test
//    void testRun_RuntimeException() {
//        // Arrange
//        BookPublishRequest mockRequest = mock(BookPublishRequest.class);
//
//        when(mockRequestManager.getBookPublishRequestToProcess()).thenReturn(mockRequest);
//        when(mockRequest.getPublishingRecordId()).thenReturn("record123");
//        when(mockRequest.getBookId()).thenReturn("book123");
//
//        when(mockCatalogDao.createOrUpdate(any(KindleFormattedBook.class))).thenThrow(new RuntimeException());
//
//        // Act
//        bookPublishTask.run();
//
//        // Assert
//        verify(mockPublishingStatusDao).setPublishingStatus(
//                "record123",
//                PublishingRecordStatus.IN_PROGRESS,
//                "book123"
//        );
//        verify(mockCatalogDao).createOrUpdate(any(KindleFormattedBook.class));
//        verify(mockPublishingStatusDao).setPublishingStatus(
//                "record123",
//                PublishingRecordStatus.FAILED,
//                "book123"
//        );
//    }
}

