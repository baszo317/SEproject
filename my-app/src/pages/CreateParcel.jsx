import { Form, Input, InputNumber, Button, Space, Typography, Card, message } from "antd";
import { useState } from "react";
import { createParcel } from "../store/mockDb";

const { Title } = Typography;

export default function CreateParcel() {
  const [form] = Form.useForm();
  const [result, setResult] = useState(null);

  const onFinish = (values) => {
    const parcel = createParcel(values);
    setResult(parcel);
  };

  return (
    <div>
      <Title level={3} style={{ marginTop: 0 }}>
        建立寄件
      </Title>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16, marginTop: 16 }}>
        <Card title="輸入資料" style={{ borderRadius: 16 }}>
          <Form form={form} layout="vertical" onFinish={onFinish}>
            <Form.Item label="寄件人姓名" name="sender" rules={[{ required: true }]}>
              <Input />
            </Form.Item>

            <Form.Item label="收件人姓名" name="receiver" rules={[{ required: true }]}>
              <Input />
            </Form.Item>

            <Form.Item label="重量 (kg)" name="weight" rules={[{ required: true }]}>
              <InputNumber min={0.1} style={{ width: "100%" }} />
            </Form.Item>

            <Form.Item label="距離 (公里)" name="distance" rules={[{ required: true }]}>
              <InputNumber min={1} style={{ width: "100%" }} />
            </Form.Item>

            <div style={{ display: "flex", justifyContent: "center" }}>
              <Space>
                <Button onClick={() => form.resetFields()}>清除</Button>
                <Button type="primary" htmlType="submit">
                  建立寄件
                </Button>
              </Space>
            </div>
          </Form>
        </Card>

        <Card title="建立結果" style={{ borderRadius: 16 }}>
          {!result ? null : (
            <div style={{ lineHeight: 2 }}>
              <div>
                <b>追蹤碼：</b> {result.trackingNumber}
              </div>
              <div>
                <b>運費：</b> {result.chargeAmount} 元
              </div>
              <div>
                <b>狀態：</b> {result.status}
              </div>
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}
