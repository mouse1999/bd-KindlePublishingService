package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.requests.RemoveBookFromCatalogRequest;
import com.amazon.ata.kindlepublishingservice.models.response.RemoveBookFromCatalogResponse;
import com.amazonaws.services.lambda.runtime.Context;

import javax.inject.Inject;

public class RemoveBookFromCatalogActivity {


    private final CatalogDao catalogDao;
    @Inject
    RemoveBookFromCatalogActivity(CatalogDao catalogDao) {
        this.catalogDao = catalogDao;
    }
    public RemoveBookFromCatalogResponse execute(RemoveBookFromCatalogRequest removeBookFromCatalogRequest) {

        if (removeBookFromCatalogRequest == null || removeBookFromCatalogRequest.getBookId()
                == null || removeBookFromCatalogRequest.getBookId().isEmpty()) {
            throw new IllegalArgumentException("Book ID must not be null or empty.");
        }
        String bookId = removeBookFromCatalogRequest.getBookId();

        CatalogItemVersion catalogItem = catalogDao.getBookFromCatalog(bookId);


// Mark the book as inactive
        catalogItem.setInactive(true);

        catalogDao.saveBookToCatalog(catalogItem);

        return  new RemoveBookFromCatalogResponse();
    }
}
