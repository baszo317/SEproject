import { Button, Card, Descriptions, Input, Space, Table, Typography, message } from "antd";
import { useState } from "react";
import { findParcel } from "../store/mockDb";

const { Title } = Typography;

export default function TrackParcel() {
  const [trackingNumber, setTrackingNumber] = useState("");
  const [parcel, setParcel] = useState(null);

  const search = () => {
    const data = findParcel(trackingNumber.trim());
    if (!data) {
      setParcel(null);
      message.error("查無此追蹤碼");
      return;
    }
    setParcel(data);
  };

  const columns = [
    { title: "時間", dataIndex: "time", width: 180 },
    { title: "事件", dataIndex: "type", width: 180 },
    { title: "備註", dataIndex: "remarks" },
  ];

  return (
    <div>
      <Title level={3} style={{ marginTop: 0 }}>
        查詢包裹
      </Title>

      <div style={{ marginTop: 16, display: "flex", justifyContent: "center" }}>
        <Space>
          <Input
            style={{ width: 360 }}
            placeholder="輸入追蹤碼"
            value={trackingNumber}
            onChange={(e) => setTrackingNumber(e.target.value)}
            onPressEnter={search}
          />
          <Button type="primary" onClick={search}>
            查詢
          </Button>
        </Space>
      </div>

      {parcel && (
        <div style={{ marginTop: 16, display: "grid", gap: 16 }}>
          <Card title="包裹資訊" style={{ borderRadius: 16 }}>
            <Descriptions column={2}>
              <Descriptions.Item label="追蹤碼">{parcel.trackingNumber}</Descriptions.Item>
              <Descriptions.Item label="狀態">{parcel.status}</Descriptions.Item>
              <Descriptions.Item label="寄件人">{parcel.sender}</Descriptions.Item>
              <Descriptions.Item label="收件人">{parcel.receiver}</Descriptions.Item>
              <Descriptions.Item label="重量">{parcel.weight} kg</Descriptions.Item>
              <Descriptions.Item label="距離">{parcel.distance} km</Descriptions.Item>
            </Descriptions>
          </Card>

          <Card title="物流事件" style={{ borderRadius: 16 }}>
            <Table
              columns={columns}
              dataSource={(parcel.events || []).map((x, i) => ({ ...x, key: i }))}
              pagination={false}
            />
          </Card>
        </div>
      )}
    </div>
  );
}
