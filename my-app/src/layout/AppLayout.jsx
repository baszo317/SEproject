import { Layout, Menu, Button } from "antd";
import { Outlet, useLocation, useNavigate } from "react-router-dom";

const { Header, Sider, Content } = Layout;

export default function AppLayout() {
  const nav = useNavigate();
  const loc = useLocation();

  const items = [
    { key: "/", label: "客戶管理" },
    { key: "/customer/create", label: "新增客戶" },
    { key: "/parcel/create", label: "建立包裹" },
    { key: "/parcel/track", label: "查詢包裹" },
    { key: "/parcel/update", label: "更新狀態" },
    { key: "/pricing", label: "運費試算" },
    { key: "/billing", label: "帳單管理" },
  ];

  const selectedKey = items.some((x) => x.key === loc.pathname) ? loc.pathname : "/";

  return (
    <Layout style={{ minHeight: "100vh", background: "#f2f3f5" }}>
      <Sider
        theme="light"
        width={220}
        style={{
          background: "#ffffff",
          borderRight: "1px solid #e5e7eb",
        }}
      >
        <Menu
          theme="light"
          mode="inline"
          items={items}
          selectedKeys={[selectedKey]}
          onClick={(e) => nav(e.key)}
          style={{ borderRight: 0, marginTop: 8 }}
        />
      </Sider>

      <Layout style={{ background: "transparent" }}>
        <Header
          style={{
            background: "#ffffff",
            borderBottom: "1px solid #e5e7eb",
            display: "flex",
            justifyContent: "flex-end",
            alignItems: "center",
            padding: "0 16px",
          }}
        >
          <Button onClick={() => nav("/login")}>登出</Button>
        </Header>

        <Content style={{ padding: 24, background: "transparent" }}>
          <div
            style={{
              maxWidth: 980,
              margin: "0 auto",
              background: "#ffffff",
              border: "1px solid #e5e7eb",
              borderRadius: 16,
              padding: 24,
              boxShadow: "0 10px 24px rgba(0,0,0,0.08)",
              minHeight: 520,
            }}
          >
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
}
