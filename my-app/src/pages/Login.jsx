import { Card, Form, Input, Button, Select, message, Typography } from "antd";
import { useNavigate } from "react-router-dom";
import { mockLogin } from "../store/mockDb";

const { Title } = Typography;

export default function Login() {
  const nav = useNavigate();

  const onFinish = (values) => {
    try {
      mockLogin(values);
      nav("/", { replace: true });
    } catch (err) {
      message.error(err?.message || "登入失敗");
    }
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "grid",
        placeItems: "center",
        background: "#f2f3f5",
        padding: 16,
      }}
    >
      <Card style={{ width: 420, borderRadius: 16 }}>
        <Title level={4} style={{ marginTop: 0 }}>
          登入系統
        </Title>

        <Form layout="vertical" onFinish={onFinish} style={{ marginTop: 12 }}>
          <Form.Item label="帳號" name="username" rules={[{ required: true }]}>
            <Input autoComplete="username" />
          </Form.Item>

          <Form.Item label="密碼" name="password" rules={[{ required: true }]}>
            <Input.Password autoComplete="current-password" />
          </Form.Item>

          <Form.Item label="角色" name="role" rules={[{ required: true }]}>
            <Select
              placeholder="請選擇角色"
              options={[
                { value: "STAFF", label: "櫃台人員" },
                { value: "COURIER", label: "配送員" },
                { value: "CUSTOMER", label: "顧客" },
              ]}
            />
          </Form.Item>

          <Button type="primary" htmlType="submit" block>
            登入
          </Button>
        </Form>
      </Card>
    </div>
  );
}
