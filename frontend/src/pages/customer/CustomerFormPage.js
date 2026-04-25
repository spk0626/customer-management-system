import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { customerApi } from '../../services/api';
import ConfirmModal from '../../components/common/ConfirmModal';

const createEmptyMobile = () => ({ mobileNumber: '', primary: false });
const createEmptyAddress = () => ({
  type: '',
  addressLine1: '',
  addressLine2: '',
  postCode: '',
  cityId: '',
  primary: false,
});

export default function CustomerFormPage({ notify }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: '',
    nicNumber: '',
    dateOfBirth: '',
    email: '',
    mobileNumbers: [createEmptyMobile()],
    addresses: [createEmptyAddress()],
    familyMemberIds: [],
  });
  const [confirmSave, setConfirmSave] = useState(false);
  const [familyQuery, setFamilyQuery] = useState('');
  const [familySearchResults, setFamilySearchResults] = useState([]);
  const isEdit = !!id;

  const title = useMemo(() => (isEdit ? 'Update Member Profile' : 'Member Registration'), [isEdit]);

  useEffect(() => {
    if (isEdit) {
      customerApi.getById(id).then(res => {
        if (res.data.data) {
          const customer = res.data.data;
          setForm({
            name: customer.name || '',
            nicNumber: customer.nicNumber || '',
            dateOfBirth: customer.dateOfBirth || '',
            email: customer.email || '',
            mobileNumbers: customer.mobileNumbers?.length
              ? customer.mobileNumbers.map(mobile => ({ mobileNumber: mobile.mobileNumber || '', primary: !!mobile.primary }))
              : [createEmptyMobile()],
            addresses: customer.addresses?.length
              ? customer.addresses.map(address => ({
                type: address.type || '',
                addressLine1: address.addressLine1 || '',
                addressLine2: address.addressLine2 || '',
                postCode: address.postCode || '',
                cityId: address.cityId || '',
                primary: !!address.primary,
              }))
              : [createEmptyAddress()],
            familyMemberIds: customer.familyMembers?.map(member => member.id) || [],
          });
        }
      });
    }
  }, [id, isEdit]);

  useEffect(() => {
    const timer = setTimeout(() => {
      const query = familyQuery.trim();
      if (!query) {
        setFamilySearchResults([]);
        return;
      }
      customerApi.search(query).then(res => {
        const results = (res.data.data || []).filter(customer => String(customer.id) !== id);
        setFamilySearchResults(results);
      }).catch(() => setFamilySearchResults([]));
    }, 250);

    return () => clearTimeout(timer);
  }, [familyQuery, id]);

  const handleSave = async () => {
    try {
      const payload = {
        name: form.name.trim(),
        nicNumber: form.nicNumber.trim(),
        dateOfBirth: form.dateOfBirth,
        email: form.email.trim(),
        mobileNumbers: form.mobileNumbers
          .filter(item => item.mobileNumber.trim())
          .map((item, index) => ({ mobileNumber: item.mobileNumber.trim(), primary: item.primary || index === 0 })),
        addresses: form.addresses
          .filter(item => item.addressLine1.trim())
          .map((item, index) => ({
            type: item.type.trim(),
            addressLine1: item.addressLine1.trim(),
            addressLine2: item.addressLine2.trim(),
            postCode: item.postCode.trim(),
            cityId: item.cityId ? Number(item.cityId) : null,
            primary: item.primary || index === 0,
          })),
        familyMemberIds: form.familyMemberIds,
      };

      if (isEdit) {
        await customerApi.update(id, payload);
        notify(`Changes to ${form.name} have been successfully updated in the database.`, 'success');
      } else {
        await customerApi.create(payload);
        notify(`New member ${form.name} has been successfully registered.`, 'success');
      }
      navigate('/');
    } catch {
      notify('Save failed. Please check the backend response.', 'error');
    }
  };

  const inputStyle = { padding: '0.85rem', borderRadius: '10px', border: '1px solid #cbd5e1', width: '100%', marginTop: '0.5rem', fontSize: '1rem', transition: '0.2s', boxSizing: 'border-box' };
  const labelStyle = { fontSize: '0.85rem', fontWeight: 800, color: '#1e293b', textTransform: 'uppercase', letterSpacing: '0.025em' };

  const updateMobile = (index, field, value) => {
    setForm(current => ({
      ...current,
      mobileNumbers: current.mobileNumbers.map((mobile, mobileIndex) => (
        mobileIndex === index ? { ...mobile, [field]: value } : mobile
      )),
    }));
  };

  const updateAddress = (index, field, value) => {
    setForm(current => ({
      ...current,
      addresses: current.addresses.map((address, addressIndex) => (
        addressIndex === index ? { ...address, [field]: value } : address
      )),
    }));
  };

  const toggleFamilyMember = (memberId) => {
    setForm(current => {
      const exists = current.familyMemberIds.includes(memberId);
      return {
        ...current,
        familyMemberIds: exists
          ? current.familyMemberIds.filter(existingId => existingId !== memberId)
          : [...current.familyMemberIds, memberId],
      };
    });
  };

  return (
    <div className="container" style={{ padding: '3rem 2rem', maxWidth: '650px', margin: '0 auto' }}>
      <button onClick={() => navigate('/')} style={{ background: 'none', border: 'none', color: '#64748b', cursor: 'pointer', marginBottom: '1.5rem', fontWeight: 700, fontSize: '0.95rem' }}>← Return to Directory</button>
      <h1 style={{ fontSize: '2rem', fontWeight: 900, marginBottom: '2rem', color: '#0f172a' }}>{title}</h1>

      <div style={{ background: 'white', padding: '2.5rem', borderRadius: '20px', border: '1px solid #e2e8f0', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.05)' }}>
        <div style={{ marginBottom: '1.5rem' }}>
          <label style={labelStyle}>Legal Full Name</label>
          <input style={inputStyle} placeholder="Enter full name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} />
        </div>
        <div style={{ marginBottom: '1.5rem' }}>
          <label style={labelStyle}>Email Address</label>
          <input required style={inputStyle} type="email" placeholder="email@example.com" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} />
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem', marginBottom: '2.5rem' }}>
          <div>
            <label style={labelStyle}>NIC / Passport ID</label>
            <input style={inputStyle} placeholder="ID Number" value={form.nicNumber} onChange={e => setForm({ ...form, nicNumber: e.target.value })} />
          </div>
          <div>
            <label style={labelStyle}>Date of Birth</label>
            <input style={inputStyle} type="date" value={form.dateOfBirth} onChange={e => setForm({ ...form, dateOfBirth: e.target.value })} />
          </div>
        </div>

        <div style={{ marginBottom: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '1rem', marginBottom: '0.75rem' }}>
            <label style={labelStyle}>Mobile Numbers</label>
            <button
              type="button"
              onClick={() => setForm(current => ({ ...current, mobileNumbers: [...current.mobileNumbers, createEmptyMobile()] }))}
              style={{ border: 'none', background: 'none', color: '#059669', fontWeight: 800, cursor: 'pointer' }}
            >
              + Add mobile
            </button>
          </div>
          {form.mobileNumbers.map((mobile, index) => (
            <div key={`mobile-${index}`} style={{ display: 'grid', gridTemplateColumns: '1fr auto', gap: '0.75rem', marginBottom: '0.75rem' }}>
              <input style={inputStyle} placeholder="0771234567" value={mobile.mobileNumber} onChange={e => updateMobile(index, 'mobileNumber', e.target.value)} />
              <button
                type="button"
                onClick={() => setForm(current => ({ ...current, mobileNumbers: current.mobileNumbers.length > 1 ? current.mobileNumbers.filter((_, itemIndex) => itemIndex !== index) : [createEmptyMobile()] }))}
                style={{ border: '1px solid #fecaca', background: '#fff1f2', color: '#e11d48', borderRadius: '10px', padding: '0 0.9rem', fontWeight: 800, cursor: 'pointer' }}
              >
                Remove
              </button>
            </div>
          ))}
        </div>

        <div style={{ marginBottom: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '1rem', marginBottom: '0.75rem' }}>
            <label style={labelStyle}>Addresses</label>
            <button
              type="button"
              onClick={() => setForm(current => ({ ...current, addresses: [...current.addresses, createEmptyAddress()] }))}
              style={{ border: 'none', background: 'none', color: '#059669', fontWeight: 800, cursor: 'pointer' }}
            >
              + Add address
            </button>
          </div>

          {form.addresses.map((address, index) => (
            <div key={`address-${index}`} style={{ padding: '1rem', border: '1px solid #dbe7e3', borderRadius: '14px', marginBottom: '1rem', background: '#f8fffc' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label style={labelStyle}>Type</label>
                  <input style={inputStyle} placeholder="Home / Work" value={address.type} onChange={e => updateAddress(index, 'type', e.target.value)} />
                </div>
                <div>
                  <label style={labelStyle}>Post Code</label>
                  <input style={inputStyle} placeholder="10100" value={address.postCode} onChange={e => updateAddress(index, 'postCode', e.target.value)} />
                </div>
              </div>
              <div style={{ marginTop: '1rem' }}>
                <label style={labelStyle}>Address Line 1</label>
                <input style={inputStyle} placeholder="Street, building or house" value={address.addressLine1} onChange={e => updateAddress(index, 'addressLine1', e.target.value)} />
              </div>
              <div style={{ marginTop: '1rem' }}>
                <label style={labelStyle}>Address Line 2</label>
                <input style={inputStyle} placeholder="Apartment, area, landmark" value={address.addressLine2} onChange={e => updateAddress(index, 'addressLine2', e.target.value)} />
              </div>
              <div style={{ marginTop: '1rem' }}>
                <label style={labelStyle}>City ID</label>
                <input style={inputStyle} type="number" placeholder="Internal city master ID" value={address.cityId} onChange={e => updateAddress(index, 'cityId', e.target.value)} />
                <div style={{ marginTop: '0.4rem', color: '#64748b', fontSize: '0.8rem' }}>City and country are resolved in the backend master data.</div>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '1rem', marginTop: '1rem' }}>
                <label style={{ ...labelStyle, textTransform: 'none' }}>
                  <input type="checkbox" checked={address.primary} onChange={e => updateAddress(index, 'primary', e.target.checked)} style={{ marginRight: '0.5rem' }} />
                  Primary address
                </label>
                <button
                  type="button"
                  onClick={() => setForm(current => ({ ...current, addresses: current.addresses.length > 1 ? current.addresses.filter((_, itemIndex) => itemIndex !== index) : [createEmptyAddress()] }))}
                  style={{ border: '1px solid #fecaca', background: '#fff1f2', color: '#e11d48', borderRadius: '10px', padding: '0.7rem 0.9rem', fontWeight: 800, cursor: 'pointer' }}
                >
                  Remove address
                </button>
              </div>
            </div>
          ))}
        </div>

        <div style={{ marginBottom: '1.5rem' }}>
          <label style={labelStyle}>Family Members</label>
          <div style={{ marginTop: '0.5rem', position: 'relative' }}>
            <input
              style={inputStyle}
              placeholder="Search customers by name or NIC"
              value={familyQuery}
              onChange={e => setFamilyQuery(e.target.value)}
            />
            {familySearchResults.length > 0 && (
              <div style={{ position: 'absolute', top: 'calc(100% + 0.4rem)', left: 0, right: 0, background: 'white', border: '1px solid #dbe7e3', borderRadius: '14px', boxShadow: '0 10px 30px rgba(15, 23, 42, 0.12)', maxHeight: '220px', overflow: 'auto', zIndex: 5 }}>
                {familySearchResults.map(result => (
                  <button
                    key={result.id}
                    type="button"
                    onClick={() => {
                      toggleFamilyMember(result.id);
                      setFamilyQuery('');
                      setFamilySearchResults([]);
                    }}
                    style={{ width: '100%', textAlign: 'left', padding: '0.9rem 1rem', border: 'none', background: 'white', borderBottom: '1px solid #eff6f3', cursor: 'pointer' }}
                  >
                    <div style={{ fontWeight: 800, color: '#0f172a' }}>{result.name}</div>
                    <div style={{ fontSize: '0.85rem', color: '#64748b' }}>{result.nicNumber}</div>
                  </button>
                ))}
              </div>
            )}
          </div>

          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.75rem', marginTop: '1rem' }}>
            {form.familyMemberIds.length ? form.familyMemberIds.map(memberId => (
              <span key={memberId} style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', padding: '0.55rem 0.85rem', borderRadius: '999px', background: '#ecfdf5', color: '#047857', fontWeight: 800 }}>
                Family member #{memberId}
                <button
                  type="button"
                  onClick={() => toggleFamilyMember(memberId)}
                  style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: '#047857', fontWeight: 900 }}
                >
                  ×
                </button>
              </span>
            )) : (
              <div style={{ color: '#64748b', fontSize: '0.9rem' }}>No family members selected.</div>
            )}
          </div>
        </div>

        <div style={{ display: 'flex', gap: '1.25rem' }}>
          <button onClick={() => navigate('/')} style={{ flex: 1, padding: '1rem', borderRadius: '12px', border: '1px solid #e2e8f0', background: '#f8fafc', color: '#475569', fontWeight: 700, cursor: 'pointer' }}>Cancel</button>
          <button onClick={() => setConfirmSave(true)} style={{ flex: 1, padding: '1rem', borderRadius: '12px', border: 'none', background: '#059669', color: 'white', fontWeight: 800, cursor: 'pointer', boxShadow: '0 4px 6px -1px rgba(5, 150, 105, 0.3)' }}>Process Record</button>
        </div>
      </div>

      <ConfirmModal
        isOpen={confirmSave}
        title="Commit Changes"
        message="Please verify all fields before saving. This will update the primary member database."
        onCancel={() => setConfirmSave(false)}
        onConfirm={handleSave}
      />
    </div>
  );
}