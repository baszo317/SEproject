import { Form, Input, Button, Space, Typography, message } from "antd";
import { useNavigate } from "react-router-dom";
import { createCustomer } from "../store/mockDb";

const { Title } = Typography;

export default function CreateCustomer() {
  const navigate = useNavigate();

  const onFinish = (values) => {
    createCustomer(values);
    navigate("/", { replace: true });
  };

  return (
    <div>
      <Title level={3} style={{ marginTop: 0 }}>
        新增客戶
      </Title>

      <Form layout="vertical" onFinish={onFinish} style={{ marginTop: 16, maxWidth: 520 }}>
        <Form.Item label="姓名" name="name" rules={[{ required: true }]}>
          <Input />
        </Form.Item>

        <Form.Item
          label="電話"
          name="phone"
          rules={[
            { required: true },
            { pattern: /^09\d{8}$/, message: "格式：09XXXXXXXX" },
          ]}
        >
          <Input />
        </Form.Item>

        <Form.Item label="地址" name="address" rules={[{ required: true }]}>
          <Input />
        </Form.Item>

        <div style={{ display: "flex", justifyContent: "center", marginTop: 8 }}>
          <Space>
            <Button onClick={() => navigate(-1)}>取消</Button>
            <Button type="primary" htmlType="submit">
              建立
            </Button>
          </Space>
        </div>
      </Form>
    </div>
  );
}
