import { HashRouter, Routes, Route, Navigate } from "react-router-dom";
import AppLayout from "./layout/AppLayout";

import Login from "./pages/Login";
import CustomerList from "./pages/CustomerList";
import CreateCustomer from "./pages/CreateCustomer";
import CreateParcel from "./pages/CreateParcel";
import TrackParcel from "./pages/TrackParcel";
import UpdateParcelStatus from "./pages/UpdateParcelStatus";
import PricingPage from "./pages/PricingPage";
import BillingPage from "./pages/BillingPage";

export default function App() {
  return (
    <HashRouter>
      <Routes>
        <Route path="/login" element={<Login />} />

        <Route element={<AppLayout />}>
          <Route path="/" element={<CustomerList />} />
          <Route path="/customer/create" element={<CreateCustomer />} />
          <Route path="/parcel/create" element={<CreateParcel />} />
          <Route path="/parcel/track" element={<TrackParcel />} />
          <Route path="/parcel/update" element={<UpdateParcelStatus />} />
          <Route path="/pricing" element={<PricingPage />} />
          <Route path="/billing" element={<BillingPage />} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </HashRouter>
  );
}
