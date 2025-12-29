import { Table, Button, Space, Typography } from "antd";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { listCustomers } from "../store/mockDb";

const { Title } = Typography;

export default function CustomerList() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);

  useEffect(() => {
    setRows(listCustomers());
  }, []);

  const columns = [
    { title: "客戶編號", dataIndex: "id", width: 140 },
    { title: "姓名", dataIndex: "name" },
    { title: "電話", dataIndex: "phone", width: 180 },
    { title: "地址", dataIndex: "address" },
  ];

  return (
    <div>
      <Title level={3} style={{ marginTop: 0 }}>
        客戶管理
      </Title>

      <div style={{ marginTop: 16, display: "flex", justifyContent: "center" }}>
        <Space size="middle" wrap>
          <Button type="primary" onClick={() => navigate("/customer/create")}>
            新增客戶
          </Button>
          <Button onClick={() => navigate("/parcel/create")}>建立寄件</Button>
          <Button onClick={() => navigate("/parcel/track")}>查詢包裹</Button>
          <Button onClick={() => navigate("/parcel/update")}>更新配送狀態</Button>
        </Space>
      </div>

      <Table
        style={{ marginTop: 18 }}
        columns={columns}
        dataSource={rows.map((x) => ({ ...x, key: x.id }))}
        pagination={{ pageSize: 5 }}
      />
    </div>
  );
}

