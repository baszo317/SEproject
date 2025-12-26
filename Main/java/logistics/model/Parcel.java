package logistics.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Parcel {
    public String trackingNumber; // 唯一追蹤編號
    public Customer sender;       // 寄件客戶（帳戶）
    public ServiceType serviceType;

    public double weightKg;   // 重量
    public double lengthCm;   // 尺寸
    public double widthCm;
    public double heightCm;
    public double declaredValue; // 申報價值
    public String description;   // 內容物描述

    public boolean dangerousGoods; // 危險物品
    public boolean fragile;        // 易碎品
    public boolean international;  // 國際貨件

    //改成 private，加方法來操作
    private List<TrackingEvent> events = new ArrayList<>();

    public Parcel(String trackingNumber, Customer sender, ServiceType serviceType,
                  double weightKg, double lengthCm, double widthCm, double heightCm,
                  double declaredValue, String description,
                  boolean dangerousGoods, boolean fragile, boolean international) {
        this.trackingNumber = trackingNumber;
        this.sender = sender;
        this.serviceType = serviceType;
        this.weightKg = weightKg;
        this.lengthCm = lengthCm;
        this.widthCm = widthCm;
        this.heightCm = heightCm;
        this.declaredValue = declaredValue;
        this.description = description;
        this.dangerousGoods = dangerousGoods;
        this.fragile = fragile;
        this.international = international;
    }

    public double getVolumeCubicMeter() {
        return (lengthCm / 100.0) * (widthCm / 100.0) * (heightCm / 100.0);
    }

    public TrackingEvent getCurrentStatus() {
        if (events.isEmpty()) return null;
        return events.get(events.size() - 1);
    }

    //只讀用這個
    public List<TrackingEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    //加一個方法讓外部可以新增事件
    public void addEvent(TrackingEvent event) {
        events.add(event);
    }

    public boolean hasEvents() {
        return !events.isEmpty();
    }

    @Override
    public String toString() {
        return "Parcel{" +
                "trackingNumber='" + trackingNumber + '\'' +
                ", sender=" + sender.name +
                ", weightKg=" + weightKg +
                ", dims=" + lengthCm + "x" + widthCm + "x" + heightCm +
                ", dangerous=" + dangerousGoods +
                ", fragile=" + fragile +
                ", international=" + international +
                '}';
    }
}
