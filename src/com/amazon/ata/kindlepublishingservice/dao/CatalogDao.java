package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

public class CatalogDao {

    private final DynamoDBMapper dynamoDbMapper;

    /**
     * Instantiates a new CatalogDao object.
     *
     * @param dynamoDbMapper The {@link DynamoDBMapper} used to interact with the catalog table.
     */
    @Inject
    public CatalogDao(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDbMapper = dynamoDbMapper;
    }

    /**
     * Returns the latest version of the book from the catalog corresponding to the specified book id.
     * Throws a BookNotFoundException if the latest version is not active or no version is found.
     * @param bookId Id associated with the book.
     * @return The corresponding CatalogItem from the catalog table.
     */
    public CatalogItemVersion getBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }

        return book;


    }

    /**
     * Returns the latest version of the book from the catalog corresponding to the specified book id.
     * return null f the latest version is not active or no version is found.
     * param bookId Id associated with the book.
     * @return The corresponding CatalogItem from the catalog table.
     */

    public CatalogItemVersion getBook(String bookId) {
        return getLatestVersionOfBook(bookId);
    }

    public void saveBookToCatalog(CatalogItemVersion catalogItemVersion) {
        dynamoDbMapper.save(catalogItemVersion);
    }

    // Returns null if no version exists for the provided bookId
    private CatalogItemVersion getLatestVersionOfBook(String bookId) {
        CatalogItemVersion book = new CatalogItemVersion();
        book.setBookId(bookId);

        DynamoDBQueryExpression<CatalogItemVersion> queryExpression = new DynamoDBQueryExpression<CatalogItemVersion>()
            .withHashKeyValues(book)
                .withFilterExpression("inactive = :inactiveVal")
                .withExpressionAttributeValues(Map.of(
                        ":inactiveVal", new AttributeValue().withN("0")
                ))
            .withScanIndexForward(false)
            .withLimit(1);
        List<CatalogItemVersion> results = dynamoDbMapper.query(CatalogItemVersion.class, queryExpression);


        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    /**
     * if no active book is found in the catalog,throw BookNotFoundException
     * else the method validates the existing bookId
     *
     * i want to query the database to return objects whether active or inactive in a descending manner using query
     * @param bookId
     */

    public void validateBookExists(String bookId) {
        CatalogItemVersion book = new CatalogItemVersion();
        book.setBookId(bookId);


        DynamoDBQueryExpression<CatalogItemVersion> queryExpression = new DynamoDBQueryExpression<CatalogItemVersion>()
                .withHashKeyValues(book)
                .withScanIndexForward(false)
                .withLimit(1);


        List<CatalogItemVersion> result = dynamoDbMapper.query(CatalogItemVersion.class, queryExpression);
        if(result.isEmpty()) {
            throw new BookNotFoundException("Book not found for this Id " + bookId);
        }

    }
    public CatalogItemVersion createOrUpdate(KindleFormattedBook formattedBook) {
        CatalogItemVersion catalogItem;

        if (formattedBook.getBookId() == null) {
            // Create a new book entry
            catalogItem = createNewBookEntry(formattedBook);
        } else {
            // Update an existing book entry
            catalogItem = updateExistingBookEntry(formattedBook);
        }

        return catalogItem;
    }

    private CatalogItemVersion createNewBookEntry(KindleFormattedBook formattedBook) {
        CatalogItemVersion newBook = new CatalogItemVersion();
        newBook.setInactive(false);
        newBook.setVersion(1);
        newBook.setBookId(KindlePublishingUtils.generateBookId());
        populateBookDetails(newBook, formattedBook);
        saveBookToCatalog(newBook);
        return newBook;
    }

    private CatalogItemVersion updateExistingBookEntry(KindleFormattedBook formattedBook) {
        CatalogItemVersion existingBook = getBookFromCatalog(formattedBook.getBookId());

        // Mark the current version as inactive
        existingBook.setInactive(true);
        saveBookToCatalog(existingBook);

        // Create a new version of the book
        CatalogItemVersion updatedBook = new CatalogItemVersion();
        updatedBook.setBookId(existingBook.getBookId());
        updatedBook.setVersion(existingBook.getVersion() + 1);
        updatedBook.setInactive(false);
        populateBookDetails(updatedBook, formattedBook);
        saveBookToCatalog(updatedBook);

        return updatedBook;
    }

    private void populateBookDetails(CatalogItemVersion book, KindleFormattedBook formattedBook) {
        book.setAuthor(formattedBook.getAuthor());
        book.setText(formattedBook.getText());
        book.setGenre(formattedBook.getGenre());
        book.setTitle(formattedBook.getTitle());
    }

}
