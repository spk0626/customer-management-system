import { HashRouter, Routes, Route } from 'react-router-dom';
import { ToastContainer, toast } from 'react-toastify';
import './App.css';
import 'react-toastify/dist/ReactToastify.css';
import AppNavbar from './components/layout/AppNavbar';
import CustomerListPageContent from './pages/customer/CustomerListPageContent';
import CustomerFormPage from './pages/customer/CustomerFormPage';
import CustomerViewPage from './pages/customer/CustomerViewPage';
import BulkUploadPage from './pages/customer/BulkUploadPage';

function App() {
  const notify = (message, type = 'success') => {
    const options = { toastId: `${type}:${message}` };

    if (type === 'error') {
      toast.error(message, options);
      return;
    }
    if (type === 'info') {
      toast.info(message, options);
      return;
    }
    toast.success(message, options);
  };

  return (
    <HashRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <AppNavbar />

      <Routes>
        <Route path="/" element={<CustomerListPageContent notify={notify} />} />
        <Route path="/customers/:id" element={<CustomerViewPage />} />
        <Route path="/customers/new" element={<CustomerFormPage notify={notify} />} />
        <Route path="/customers/:id/edit" element={<CustomerFormPage notify={notify} />} />
        <Route path="/bulk-upload" element={<BulkUploadPage notify={notify} />} />
      </Routes>

      <ToastContainer position="top-right" autoClose={4000} newestOnTop closeOnClick pauseOnHover />
    </HashRouter>
  );
}

export default App;
