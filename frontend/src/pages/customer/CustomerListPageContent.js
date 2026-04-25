import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { customerApi } from '../../services/api';
import ConfirmModal from '../../components/common/ConfirmModal';

const formatDate = (value) => {
  if (!value) return 'Not set';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
};

export default function CustomerListPageContent({ notify }) {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState({ isOpen: false, id: null, name: '' });
  const navigate = useNavigate();

  const loadData = useCallback(() => {
    setLoading(true);
    customerApi.getAll().then(res => {
      setCustomers(res.data.data.content);
    }).catch(() => {
      notify('Unable to load customers from the backend.', 'error');
      setCustomers([]);
    }).finally(() => {
      setLoading(false);
    });
  }, [notify]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const confirmDelete = async () => {
    try {
      await customerApi.delete(modal.id);
      setModal({ ...modal, isOpen: false });
      loadData();
      notify(`The record for ${modal.name} has been permanently deleted.`, 'success');
    } catch {
      notify('Delete failed. Please try again.', 'error');
    }
  };

  return (
    <div className="container" style={{ padding: '2rem 2rem 3rem', maxWidth: '1180px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '2rem', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
        <div>
          <h1 style={{ fontSize: '2.2rem', fontWeight: 900, color: '#0f172a', letterSpacing: '-0.025em' }}>Member Directory</h1>
          <p style={{ fontSize: '1rem', color: '#64748b', marginTop: '0.25rem' }}>Overview of all registered organization members.</p>
        </div>
        <div style={{ display: 'flex', gap: '1rem' }}>
          <button onClick={() => navigate('/bulk-upload')} style={{ padding: '0.75rem 1.5rem', borderRadius: '10px', border: '1px solid #e2e8f0', background: 'white', fontWeight: 700, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span>📥</span> Batch Import
          </button>
          <button onClick={() => navigate('/customers/new')} style={{ background: '#059669', color: 'white', padding: '0.75rem 1.5rem', borderRadius: '10px', border: 'none', fontWeight: 700, cursor: 'pointer', boxShadow: '0 4px 6px -1px rgba(5, 150, 105, 0.2)' }}>
            + New Registration
          </button>
        </div>
      </div>

      <div style={{ background: 'white', borderRadius: '18px', border: '1px solid #dbe7e3', overflow: 'hidden', boxShadow: '0 16px 40px -18px rgba(4, 120, 87, 0.35)' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ background: 'linear-gradient(180deg, #ecfdf5 0%, #f8fafc 100%)', borderBottom: '1px solid #dbe7e3' }}>
              <th style={{ textAlign: 'left', padding: '1.15rem 1.5rem', fontSize: '0.75rem', color: '#047857', textTransform: 'uppercase', letterSpacing: '0.08em' }}>Member Profile</th>
              <th style={{ textAlign: 'left', padding: '1.15rem 1.5rem', fontSize: '0.75rem', color: '#047857', textTransform: 'uppercase', letterSpacing: '0.08em' }}>NIC / ID</th>
              <th style={{ textAlign: 'left', padding: '1.15rem 1.5rem', fontSize: '0.75rem', color: '#047857', textTransform: 'uppercase', letterSpacing: '0.08em' }}>Date of Birth</th>
              <th style={{ textAlign: 'left', padding: '1.15rem 1.5rem', fontSize: '0.75rem', color: '#047857', textTransform: 'uppercase', letterSpacing: '0.08em' }}>Details</th>
              <th style={{ textAlign: 'right', padding: '1.15rem 1.5rem', fontSize: '0.75rem', color: '#047857', textTransform: 'uppercase', letterSpacing: '0.08em' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan="5" style={{ textAlign: 'center', padding: '5rem', color: '#94a3b8' }}>Synchronizing data...</td>
              </tr>
            ) : customers.map(customer => (
              <tr key={customer.id} style={{ borderBottom: '1px solid #f1f5f9', transition: '0.2s' }} className="table-row">
                <td style={{ padding: '1.25rem 1.5rem' }}>
                  <div style={{ fontWeight: 800, color: '#0f172a', fontSize: '1rem' }}>{customer.name}</div>
                  <div style={{ fontSize: '0.85rem', color: '#64748b' }}>{customer.email}</div>
                </td>
                <td style={{ padding: '1.25rem 1.5rem' }}>
                  <code style={{ fontSize: '0.9rem', color: '#065f46', background: '#ecfdf5', padding: '2px 8px', borderRadius: '999px', fontWeight: 700 }}>{customer.nicNumber}</code>
                </td>
                <td style={{ padding: '1.25rem 1.5rem' }}>
                  <span style={{ color: '#334155', fontWeight: 600 }}>{formatDate(customer.dateOfBirth)}</span>
                </td>
                <td style={{ padding: '1.25rem 1.5rem' }}>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem', color: '#475569', fontSize: '0.85rem' }}>
                    <span>{customer.mobileNumbers?.length ? `${customer.mobileNumbers.length} mobile${customer.mobileNumbers.length > 1 ? 's' : ''}` : 'No mobiles listed'}</span>
                    <span>{customer.addresses?.length ? `${customer.addresses.length} address${customer.addresses.length > 1 ? 'es' : ''}` : 'No addresses listed'}</span>
                    <span>{customer.familyMembers?.length ? `${customer.familyMembers.length} family member${customer.familyMembers.length > 1 ? 's' : ''}` : 'No family members listed'}</span>
                  </div>
                </td>
                <td style={{ padding: '1.25rem 1.5rem', textAlign: 'right' }}>
                  <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end' }}>
                    <button onClick={() => navigate(`/customers/${customer.id}`)} style={{ padding: '0.5rem 1rem', borderRadius: '999px', border: '1px solid #cbd5e1', background: 'white', cursor: 'pointer', fontWeight: 700, color: '#334155', fontSize: '0.85rem' }}>View</button>
                    <button onClick={() => navigate(`/customers/${customer.id}/edit`)} style={{ padding: '0.5rem 1rem', borderRadius: '999px', border: '1px solid #a7f3d0', background: '#f0fdf4', cursor: 'pointer', fontWeight: 700, color: '#047857', fontSize: '0.85rem' }}>Edit</button>
                    <button
                      style={{ padding: '0.5rem 1rem', borderRadius: '999px', border: 'none', background: '#fff1f2', color: '#e11d48', cursor: 'pointer', fontWeight: 700, fontSize: '0.85rem' }}
                      onClick={() => setModal({ isOpen: true, id: customer.id, name: customer.name })}
                    >
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ConfirmModal
        isOpen={modal.isOpen}
        title="Delete Member Record"
        message={`Warning: You are about to delete ${modal.name}. This will remove the customer record from the system directory.`}
        type="danger"
        onCancel={() => setModal({ ...modal, isOpen: false })}
        onConfirm={confirmDelete}
      />
    </div>
  );
}