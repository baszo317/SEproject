package logistics.model;

import java.time.LocalDateTime;
import logistics.enums.TrackingEventType;
import logistics.enums.ExceptionType;

public class TrackingEvent {
    private TrackingEventType type;
    private LocalDateTime timestamp;
    private String location;    // 倉儲地點 / 城市 / 地址
    private String truckId;     // 若有貨車載具
    private String warehouseId; // 若有倉儲代碼
    private String description; // 備註
    private ExceptionType exceptionType; // 若為 EXCEPTION 事件可填

    public TrackingEvent(TrackingEventType type, LocalDateTime timestamp,
                         String location, String truckId, String warehouseId,
                         String description, ExceptionType exceptionType) {
        this.type = type;
        this.timestamp = timestamp;
        this.location = location;
        this.truckId = truckId;
        this.warehouseId = warehouseId;
        this.description = description;
        this.exceptionType = exceptionType;
    }

    // ★ 加上這些 getter，讓其他 package 也能讀
    public TrackingEventType getType() {
        return type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getLocation() {
        return location;
    }

    public String getTruckId() {
        return truckId;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public String getDescription() {
        return description;
    }

    public ExceptionType getExceptionType() {
        return exceptionType;
    }

    @Override
    public String toString() {
        return "TrackingEvent{" +
                "type=" + type +
                ", time=" + timestamp +
                ", location='" + location + '\'' +
                ", truckId='" + truckId + '\'' +
                ", warehouseId='" + warehouseId + '\'' +
                ", description='" + description + '\'' +
                ", exceptionType=" + exceptionType +
                '}';
    }
}
