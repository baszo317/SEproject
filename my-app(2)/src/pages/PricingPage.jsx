import { Card, Form, InputNumber, Button, Space, Typography, message } from "antd";
import { quote } from "../store/mockDb";

const { Title } = Typography;

export default function PricingPage() {
  const onFinish = (v) => {
    const { amount } = quote(v);
    message.success(`預估運費：${amount} 元`);
  };

  return (
    <div>
      <Title level={3} style={{ marginTop: 0 }}>
        運費試算
      </Title>

      <Card style={{ borderRadius: 16, marginTop: 16 }} title="輸入參數">
        <Form layout="vertical" onFinish={onFinish} style={{ maxWidth: 520 }}>
          <Form.Item label="重量 (kg)" name="weight" rules={[{ required: true }]}>
            <InputNumber min={0.1} style={{ width: "100%" }} />
          </Form.Item>

          <Form.Item label="距離 (公里)" name="distance" rules={[{ required: true }]}>
            <InputNumber min={1} style={{ width: "100%" }} />
          </Form.Item>

          <div style={{ display: "flex", justifyContent: "center" }}>
            <Space>
              <Button htmlType="reset">清除</Button>
              <Button type="primary" htmlType="submit">
                計算
              </Button>
            </Space>
          </div>
        </Form>
      </Card>
    </div>
  );
}
