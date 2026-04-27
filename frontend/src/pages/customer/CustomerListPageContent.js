import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { customerApi } from '../../services/api';
import ConfirmModal from '../../components/common/ConfirmModal';

const DEFAULT_PAGE_SIZE = 10;

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
  const [pageState, setPageState] = useState({
    page: 0,
    size: DEFAULT_PAGE_SIZE,
    totalElements: 0,
    totalPages: 0,
    last: true,
  });
  const navigate = useNavigate();

  const loadData = useCallback((requestedPage = pageState.page) => {
    setLoading(true);
    customerApi.getAll(requestedPage, pageState.size).then(res => {
      const payload = res.data.data;
      const nextPage = payload.totalPages > 0 && requestedPage >= payload.totalPages
        ? payload.totalPages - 1
        : payload.page;

      if (payload.totalPages > 0 && requestedPage >= payload.totalPages && nextPage !== requestedPage) {
        loadData(nextPage);
        return;
      }

      setCustomers(payload.content);
      setPageState({
        page: payload.page,
        size: payload.size,
        totalElements: payload.totalElements,
        totalPages: payload.totalPages,
        last: payload.last,
      });
    }).catch(() => {
      setCustomers([]);
      setPageState(current => ({
        ...current,
        totalElements: 0,
        totalPages: 0,
        last: true,
      }));
    }).finally(() => {
      setLoading(false);
    });
  }, [pageState.page, pageState.size]);

  useEffect(() => {
    loadData(pageState.page);
  }, [loadData, pageState.page]);

  const confirmDelete = async () => {
    try {
      await customerApi.delete(modal.id);
      setModal({ ...modal, isOpen: false });
      notify(`The record for ${modal.name} has been permanently deleted.`, 'success');
      loadData(pageState.page);
    } catch {}
  };

  const detailCountLabel = (count, singular, plural) => (
    count ? `${count} ${count === 1 ? singular : plural}` : `No ${plural} listed`
  );

  const goToPage = (nextPage) => {
    if (nextPage === pageState.page || nextPage < 0 || (pageState.totalPages > 0 && nextPage >= pageState.totalPages)) {
      return;
    }
    setPageState(current => ({ ...current, page: nextPage }));
  };

  const pageStart = pageState.totalElements === 0 ? 0 : (pageState.page * pageState.size) + 1;
  const pageEnd = pageState.totalElements === 0
    ? 0
    : Math.min((pageState.page + 1) * pageState.size, pageState.totalElements);

  const pagerButtonStyle = (disabled) => ({
    padding: '0.65rem 0.95rem',
    borderRadius: '10px',
    border: '1px solid #dbe7e3',
    background: disabled ? '#f8fafc' : 'white',
    color: disabled ? '#94a3b8' : '#334155',
    fontWeight: 700,
    cursor: disabled ? 'default' : 'pointer',
    minWidth: '52px',
  });

  return (
    <div className="container" style={{ padding: '2rem 2rem 3rem', maxWidth: '1180px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '2rem', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
        <div>
          <h1 style={{ fontSize: '2.2rem', fontWeight: 900, color: '#0f172a', letterSpacing: '-0.025em' }}>Customer Directory</h1>
          <p style={{ fontSize: '1rem', color: '#64748b', marginTop: '0.25rem' }}>Overview of all registered customers.</p>
        </div>
        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
          <button onClick={() => navigate('/bulk-upload')} style={{ padding: '0.75rem 1.5rem', borderRadius: '10px', border: '1px solid #e2e8f0', background: 'white', fontWeight: 700, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span>Excel</span> Batch Import
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
            ) : customers.length === 0 ? (
              <tr>
                <td colSpan="5" style={{ textAlign: 'center', padding: '4rem', color: '#94a3b8' }}>No customers found on this page.</td>
              </tr>
            ) : customers.map(customer => (
              <tr key={customer.id} style={{ borderBottom: '1px solid #f1f5f9', transition: '0.2s' }} className="table-row">
                <td style={{ padding: '1.25rem 1.5rem' }}>
                  <div style={{ fontWeight: 800, color: '#0f172a', fontSize: '1rem' }}>{customer.name}</div>
                  <div style={{ fontSize: '0.85rem', color: '#64748b' }}>{customer.active ? 'Active customer' : 'Inactive customer'}</div>
                </td>
                <td style={{ padding: '1.25rem 1.5rem' }}>
                  <code style={{ fontSize: '0.9rem', color: '#065f46', background: '#ecfdf5', padding: '2px 8px', borderRadius: '999px', fontWeight: 700 }}>{customer.nicNumber}</code>
                </td>
                <td style={{ padding: '1.25rem 1.5rem' }}>
                  <span style={{ color: '#334155', fontWeight: 600 }}>{formatDate(customer.dateOfBirth)}</span>
                </td>
                <td style={{ padding: '1.25rem 1.5rem' }}>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem', color: '#475569', fontSize: '0.85rem' }}>
                    <span>{detailCountLabel(customer.mobileCount ?? customer.mobileNumbers?.length ?? 0, 'mobile', 'mobiles')}</span>
                    <span>{detailCountLabel(customer.addressCount ?? customer.addresses?.length ?? 0, 'address', 'addresses')}</span>
                    <span>{detailCountLabel(customer.familyMemberCount ?? customer.familyMembers?.length ?? 0, 'family member', 'family members')}</span>
                  </div>
                </td>
                <td style={{ padding: '1.25rem 1.5rem', textAlign: 'right' }}>
                  <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end', flexWrap: 'wrap' }}>
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

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '1rem', padding: '1rem 1.5rem', borderTop: '1px solid #e2e8f0', background: '#fcfefd', flexWrap: 'wrap' }}>
          <div style={{ color: '#64748b', fontSize: '0.9rem', fontWeight: 600 }}>
            Showing {pageStart}-{pageEnd} of {pageState.totalElements}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', flexWrap: 'wrap', justifyContent: 'flex-end' }}>
            <span style={{ color: '#334155', fontSize: '0.9rem', fontWeight: 700 }}>
              Page {pageState.totalPages === 0 ? 0 : pageState.page + 1} of {pageState.totalPages}
            </span>
            <button type="button" onClick={() => goToPage(0)} disabled={loading || pageState.page === 0 || pageState.totalPages === 0} style={pagerButtonStyle(loading || pageState.page === 0 || pageState.totalPages === 0)}>First</button>
            <button type="button" onClick={() => goToPage(pageState.page - 1)} disabled={loading || pageState.page === 0} style={pagerButtonStyle(loading || pageState.page === 0)}>Prev</button>
            <button type="button" onClick={() => goToPage(pageState.page + 1)} disabled={loading || pageState.last || pageState.totalPages === 0} style={pagerButtonStyle(loading || pageState.last || pageState.totalPages === 0)}>Next</button>
            <button type="button" onClick={() => goToPage(pageState.totalPages - 1)} disabled={loading || pageState.last || pageState.totalPages === 0} style={pagerButtonStyle(loading || pageState.last || pageState.totalPages === 0)}>Last</button>
          </div>
        </div>
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
