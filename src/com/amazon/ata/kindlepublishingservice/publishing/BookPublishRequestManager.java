package com.amazon.ata.kindlepublishingservice.publishing;

import javax.inject.Inject;
import java.util.Queue;

public class BookPublishRequestManager {

    private Queue<BookPublishRequest> requestQueue;

    /**
     * Constructs a new {@code BookPublishRequestManager} with the specified queue for managing
     * book publish requests.
     *
     * @param requestQueue the {@link Queue} to store and manage {@code BookPublishRequest} instances
     */
    @Inject
    public BookPublishRequestManager(Queue<BookPublishRequest> requestQueue) {
        this.requestQueue = requestQueue;

    }

    /**
     * Adds a new {@code BookPublishRequest} to the queue for processing.
     *
     * @param bookPublishRequest the {@link BookPublishRequest} to be added to the queue
     */

    public void addBookPublishRequest(BookPublishRequest bookPublishRequest) {
        requestQueue.add(bookPublishRequest);

    }

    /**
     * Retrieves and removes the next {@code BookPublishRequest} in line for processing.
     * If there are no requests in the queue, this method returns {@code null}.
     *
     * @return the next {@link BookPublishRequest} to process, or {@code null} if the queue is empty
     */

    public BookPublishRequest getBookPublishRequestToProcess() {
        return requestQueue.poll();
    }
}
