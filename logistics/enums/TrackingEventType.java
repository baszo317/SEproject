package logistics.enums;
public enum TrackingEventType {
    PICKED_UP,          // 起運地收件
    LOADED_TO_TRUCK,    // 進貨車
    UNLOADED_FROM_TRUCK,// 出貨車
    ENTER_WAREHOUSE,    // 入倉
    EXIT_WAREHOUSE,     // 出倉
    SORTED,             // 分揀
    IN_TRANSIT,         // 運送中
    OUT_FOR_DELIVERY,   // 外送中
    DELIVERED,          // 已投遞
    SIGNED,             // 已簽收
    EXCEPTION           // 異常（遺失 / 延誤 / 損毀）
}
