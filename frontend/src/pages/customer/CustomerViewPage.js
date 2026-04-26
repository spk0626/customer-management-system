import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { customerApi } from '../../services/api';

const formatDate = (value) => {
  if (!value) return 'Not set';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' });
};

export default function CustomerViewPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [customer, setCustomer] = useState(null);
  const [loadFailed, setLoadFailed] = useState(false);

  useEffect(() => {
    customerApi.getById(id)
      .then(res => {
        setCustomer(res.data.data);
        setLoadFailed(false);
      })
      .catch(() => setLoadFailed(true));
  }, [id]);

  if (!customer && !loadFailed) {
    return (
      <div className="container" style={{ padding: '3rem 2rem', maxWidth: '900px', margin: '0 auto' }}>
        <button onClick={() => navigate('/')} style={{ background: 'none', border: 'none', color: '#64748b', cursor: 'pointer', marginBottom: '1.5rem', fontWeight: 700 }}>&larr; Return to Directory</button>
        <div style={{ background: 'white', borderRadius: '18px', padding: '3rem', border: '1px solid #dbe7e3', textAlign: 'center', color: '#64748b' }}>Loading customer details...</div>
      </div>
    );
  }

  if (loadFailed) {
    return (
      <div className="container" style={{ padding: '3rem 2rem', maxWidth: '900px', margin: '0 auto' }}>
        <button onClick={() => navigate('/')} style={{ background: 'none', border: 'none', color: '#64748b', cursor: 'pointer', marginBottom: '1.5rem', fontWeight: 700 }}>&larr; Return to Directory</button>
        <div style={{ background: 'white', borderRadius: '18px', padding: '3rem', border: '1px solid #dbe7e3', textAlign: 'center', color: '#64748b' }}>Customer details could not be loaded.</div>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '3rem 2rem', maxWidth: '980px', margin: '0 auto' }}>
      <button onClick={() => navigate('/')} style={{ background: 'none', border: 'none', color: '#64748b', cursor: 'pointer', marginBottom: '1.5rem', fontWeight: 700 }}>&larr; Return to Directory</button>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem', flexWrap: 'wrap', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '2.2rem', fontWeight: 900, color: '#0f172a', margin: 0 }}>{customer.name}</h1>
          <p style={{ color: '#64748b', marginTop: '0.35rem' }}>{customer.nicNumber}</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button onClick={() => navigate(`/customers/${customer.id}/edit`)} style={{ padding: '0.75rem 1.15rem', borderRadius: '999px', border: 'none', background: '#059669', color: 'white', fontWeight: 800, cursor: 'pointer' }}>Edit Customer</button>
          <button onClick={() => navigate('/')} style={{ padding: '0.75rem 1.15rem', borderRadius: '999px', border: '1px solid #cbd5e1', background: 'white', color: '#334155', fontWeight: 800, cursor: 'pointer' }}>Back</button>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1rem' }}>
        <section style={{ background: 'white', borderRadius: '18px', padding: '1.5rem', border: '1px solid #dbe7e3', boxShadow: '0 14px 40px -24px rgba(4, 120, 87, 0.35)' }}>
          <h2 style={{ marginTop: 0, color: '#047857' }}>Profile</h2>
          <div style={{ display: 'grid', gap: '0.85rem', color: '#334155' }}>
            <div><strong>Date of birth:</strong> {formatDate(customer.dateOfBirth)}</div>
            <div><strong>Active:</strong> {customer.active ? 'Yes' : 'No'}</div>
          </div>
        </section>

        <section style={{ background: 'white', borderRadius: '18px', padding: '1.5rem', border: '1px solid #dbe7e3', boxShadow: '0 14px 40px -24px rgba(4, 120, 87, 0.35)' }}>
          <h2 style={{ marginTop: 0, color: '#047857' }}>Mobile Numbers</h2>
          <div style={{ display: 'grid', gap: '0.65rem' }}>
            {customer.mobileNumbers?.length ? customer.mobileNumbers.map(mobile => (
              <div key={mobile.id || mobile.mobileNumber} style={{ padding: '0.75rem 0.9rem', borderRadius: '12px', background: '#f0fdf4', color: '#166534', fontWeight: 700 }}>
                {mobile.mobileNumber} {mobile.primary ? '(Primary)' : ''}
              </div>
            )) : <div style={{ color: '#64748b' }}>No mobile numbers.</div>}
          </div>
        </section>

        <section style={{ background: 'white', borderRadius: '18px', padding: '1.5rem', border: '1px solid #dbe7e3', boxShadow: '0 14px 40px -24px rgba(4, 120, 87, 0.35)' }}>
          <h2 style={{ marginTop: 0, color: '#047857' }}>Addresses</h2>
          <div style={{ display: 'grid', gap: '0.75rem' }}>
            {customer.addresses?.length ? customer.addresses.map(address => (
              <div key={address.id || `${address.addressLine1}-${address.cityName}`} style={{ padding: '0.9rem', borderRadius: '12px', border: '1px solid #e2e8f0' }}>
                <div style={{ fontWeight: 800, color: '#0f172a' }}>Address</div>
                <div style={{ color: '#334155', marginTop: '0.35rem' }}>{address.addressLine1}</div>
                {address.addressLine2 && <div style={{ color: '#475569' }}>{address.addressLine2}</div>}
                <div style={{ color: '#64748b', marginTop: '0.35rem' }}>
                  {address.cityName ? `${address.cityName}` : ''}{address.countryName ? `, ${address.countryName}` : ''}
                </div>
              </div>
            )) : <div style={{ color: '#64748b' }}>No addresses.</div>}
          </div>
        </section>

        <section style={{ background: 'white', borderRadius: '18px', padding: '1.5rem', border: '1px solid #dbe7e3', boxShadow: '0 14px 40px -24px rgba(4, 120, 87, 0.35)' }}>
          <h2 style={{ marginTop: 0, color: '#047857' }}>Family Members</h2>
          <div style={{ display: 'grid', gap: '0.75rem' }}>
            {customer.familyMembers?.length ? customer.familyMembers.map(member => (
              <div key={member.id} style={{ padding: '0.75rem 0.9rem', borderRadius: '12px', background: '#ecfdf5', color: '#065f46' }}>
                <div style={{ fontWeight: 800 }}>{member.name}</div>
                <div style={{ fontSize: '0.9rem' }}>{member.nicNumber}</div>
              </div>
            )) : <div style={{ color: '#64748b' }}>No family members.</div>}
          </div>
        </section>
      </div>
    </div>
  );
}
