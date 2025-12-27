package logistics.api;

import logistics.enums.ExceptionType;
import logistics.enums.TrackingEventType;
import logistics.model.TrackingEvent;

import java.util.List;

public interface TrackingApi {
    void addTrackingEvent(String trackingNo, TrackingEventType type,
                          String location, String truckId, String warehouseId,
                          String description, ExceptionType exceptionTypeOrNull);

    TrackingEvent getCurrentStatus(String trackingNo);
    List<TrackingEvent> getHistory(String trackingNo);
}
