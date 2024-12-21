package com.amazon.ata.kindlepublishingservice.converters;


import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;

import java.util.ArrayList;
import java.util.List;

public class PublishingStatusRecordConverter {
    public PublishingStatusRecordConverter() {}

    public static List<PublishingStatusRecord> toPublishingStatusRecords(List<PublishingStatusItem> publishingStatusItems) {

        List<PublishingStatusRecord> publishingStatusRecords = new ArrayList<>();
        for (PublishingStatusItem item : publishingStatusItems) {
            publishingStatusRecords.add(toPublishingStatusRecord(item));
        }
        return publishingStatusRecords;
    }
    public static PublishingStatusRecord toPublishingStatusRecord(PublishingStatusItem publishingStatusItem) {

        return PublishingStatusRecord.builder()
                .withStatus(publishingStatusItem.getStatus().name())
                .withBookId(publishingStatusItem.getBookId())
                .withStatusMessage(publishingStatusItem.getStatusMessage())
                .build();
    }
}
