import { Button, Card, Descriptions, Form, Input, Select, Space, Typography, message } from "antd";
import { useState } from "react";
import { findParcel, addTrackingEvent } from "../store/mockDb";

const { Title } = Typography;

const trackingEventTypes = [
  { value: "PICKUP", label: "已收件" },
  { value: "INBOUND_SCAN", label: "入庫掃描" },
  { value: "OUTBOUND_SCAN", label: "出庫掃描" },
  { value: "SORTING", label: "分貨處理" },
  { value: "OUT_FOR_DELIVERY", label: "配送中" },
  { value: "DELIVERED", label: "已送達" },
  { value: "EXCEPTION", label: "異常事件" },
];

export default function UpdateParcelStatus() {
  const [trackingNumber, setTrackingNumber] = useState("");
  const [parcel, setParcel] = useState(null);
  const [form] = Form.useForm();

  const search = () => {
    const data = findParcel(trackingNumber.trim());
    if (!data) {
      setParcel(null);
      message.error("查無此追蹤碼");
      return;
    }
    setParcel(data);
  };

  const onFinish = (values) => {
    try {
      const updated = addTrackingEvent(trackingNumber.trim(), values);
      setParcel(updated);
      form.resetFields();
    } catch (err) {
      message.error(err?.message || "更新失敗");
    }
  };

  return (
    <div>
      <Title level={3} style={{ marginTop: 0 }}>
        更新配送狀態
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
              <Descriptions.Item label="目前狀態">{parcel.status}</Descriptions.Item>
              <Descriptions.Item label="寄件人">{parcel.sender}</Descriptions.Item>
              <Descriptions.Item label="收件人">{parcel.receiver}</Descriptions.Item>
            </Descriptions>
          </Card>

          <Card title="新增物流事件" style={{ borderRadius: 16 }}>
            <Form form={form} layout="vertical" onFinish={onFinish}>
              <Form.Item label="事件類型" name="eventType" rules={[{ required: true }]}>
                <Select placeholder="選擇狀態" options={trackingEventTypes} />
              </Form.Item>

              <Form.Item label="備註" name="remarks">
                <Input.TextArea rows={3} />
              </Form.Item>

              <div style={{ display: "flex", justifyContent: "center" }}>
                <Space>
                  <Button onClick={() => form.resetFields()}>清除</Button>
                  <Button type="primary" htmlType="submit">
                    更新狀態
                  </Button>
                </Space>
              </div>
            </Form>
          </Card>
        </div>
      )}
    </div>
  );
}
