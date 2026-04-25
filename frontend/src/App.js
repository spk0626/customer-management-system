import React, { useState } from 'react';
import { HashRouter, Routes, Route } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import './App.css';
import 'react-toastify/dist/ReactToastify.css';
import Notification from './components/common/Notification';
import CustomerListPageContent from './pages/customer/CustomerListPageContent';
import CustomerFormPage from './pages/customer/CustomerFormPage';
import CustomerViewPage from './pages/customer/CustomerViewPage';
import BulkUploadPage from './pages/customer/BulkUploadPage';

function App() {
  const [notification, setNotification] = useState(null);

  const notify = (message, type = 'success') => {
    setNotification({ message, type });
  };

  return (
    <HashRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <header style={{
        position: 'sticky',
        top: 0,
        zIndex: 900,
        background: 'linear-gradient(135deg, #064e3b 0%, #059669 55%, #10b981 100%)',
        color: 'white',
        boxShadow: '0 12px 32px rgba(6, 95, 70, 0.18)'
      }}>
        <div style={{ maxWidth: '1180px', margin: '0 auto', padding: '1rem 2rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '1rem' }}>
          <div>
            <div style={{ fontSize: '0.8rem', textTransform: 'uppercase', letterSpacing: '0.18em', opacity: 0.8, fontWeight: 800 }}>Customer Management</div>
            <div style={{ fontSize: '1.15rem', fontWeight: 900, marginTop: '0.15rem' }}>Emerald Directory</div>
          </div>
          <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', justifyContent: 'flex-end' }}>
            <a href="#/" style={{ color: 'white', textDecoration: 'none', padding: '0.65rem 1rem', borderRadius: '999px', background: 'rgba(255,255,255,0.12)', fontWeight: 700 }}>Customers</a>
            <a href="#/customers/new" style={{ color: '#064e3b', textDecoration: 'none', padding: '0.65rem 1rem', borderRadius: '999px', background: 'white', fontWeight: 800 }}>New Record</a>
            <a href="#/bulk-upload" style={{ color: 'white', textDecoration: 'none', padding: '0.65rem 1rem', borderRadius: '999px', border: '1px solid rgba(255,255,255,0.3)', fontWeight: 700 }}>Bulk Upload</a>
          </div>
        </div>
      </header>

      <Routes>
        <Route path="/" element={<CustomerListPageContent notify={notify} />} />
        <Route path="/customers/:id" element={<CustomerViewPage notify={notify} />} />
        <Route path="/customers/new" element={<CustomerFormPage notify={notify} />} />
        <Route path="/customers/:id/edit" element={<CustomerFormPage notify={notify} />} />
        <Route path="/bulk-upload" element={<BulkUploadPage notify={notify} />} />
      </Routes>

      {notification && (
        <Notification
          message={notification.message}
          type={notification.type}
          onClose={() => setNotification(null)}
        />
      )}

      <ToastContainer position="top-right" autoClose={4000} newestOnTop closeOnClick pauseOnHover />
    </HashRouter>
  );
}

export default App;
