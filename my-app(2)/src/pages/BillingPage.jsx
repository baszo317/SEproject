import { Card, Table, Button, Tag, Space, Typography, message, Input } from "antd";
import { useEffect, useState } from "react";
import { listInvoices, generateInvoice, markInvoicePaid } from "../store/mockDb";

const { Title } = Typography;

export default function BillingPage() {
  const [rows, setRows] = useState([]);
  const [trackingNumber, setTrackingNumber] = useState("");

  const refresh = () => setRows(listInvoices());

  useEffect(() => {
    refresh();
  }, []);

  const onGenerate = () => {
    try {
      generateInvoice({ trackingNumber: trackingNumber.trim() });
      setTrackingNumber("");
      refresh();
    } catch (err) {
      message.error(err?.message || "產生失敗");
    }
  };

  const onPaid = (id) => {
    try {
      markInvoicePaid(id);
      refresh();
    } catch (err) {
      message.error(err?.message || "操作失敗");
    }
  };

  const columns = [
    { title: "帳單編號", dataIndex: "id", width: 160 },
    { title: "追蹤碼", dataIndex: "trackingNumber", width: 180 },
    { title: "客戶", dataIndex: "customer" },
    { title: "金額", dataIndex: "amount", width: 120, render: (v) => `${v} 元` },
    {
      title: "狀態",
      dataIndex: "status",
      width: 120,
      render: (s) =>
        s === "PAID" ? <Tag color="green">已付款</Tag> : <Tag color="orange">待付款</Tag>,
    },
    { title: "建立時間", dataIndex: "createdAt", width: 180 },
    {
      title: "操作",
      width: 140,
      render: (_, row) => (
        <Space>
          <Button type="link" disabled={row.status === "PAID"} onClick={() => onPaid(row.id)}>
            設為已付
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Title level={3} style={{ marginTop: 0 }}>
        帳單管理
      </Title>

      <Card style={{ borderRadius: 16, marginTop: 16 }} title="產生帳單">
        <div style={{ display: "flex", gap: 12, maxWidth: 560 }}>
          <Input
            placeholder="輸入追蹤碼（請先建立寄件產生 TRK-...）"
            value={trackingNumber}
            onChange={(e) => setTrackingNumber(e.target.value)}
            onPressEnter={onGenerate}
          />
          <Button type="primary" onClick={onGenerate}>
            產生
          </Button>
        </div>
      </Card>

      <Card style={{ borderRadius: 16, marginTop: 16 }} title="帳單列表">
        <Table
          columns={columns}
          dataSource={rows.map((x) => ({ ...x, key: x.id }))}
          pagination={{ pageSize: 6 }}
        />
      </Card>
    </div>
  );
}
