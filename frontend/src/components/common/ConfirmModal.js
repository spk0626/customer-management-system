import React from 'react';

export default function ConfirmModal({ isOpen, title, message, onConfirm, onCancel, type = 'primary' }) {
  if (!isOpen) return null;

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      width: '100%',
      height: '100%',
      background: 'rgba(15, 23, 42, 0.6)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 1000,
      backdropFilter: 'blur(4px)'
    }}>
      <div style={{ background: 'white', padding: '2rem', borderRadius: '20px', maxWidth: '440px', width: '90%', boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)' }}>
        <h2 style={{ fontSize: '1.25rem', marginBottom: '0.75rem', color: '#1e293b', fontWeight: 800 }}>{title}</h2>
        <p style={{ fontSize: '0.95rem', color: '#64748b', marginBottom: '2rem', lineHeight: '1.5' }}>{message}</p>
        <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
          <button onClick={onCancel} style={{ padding: '0.75rem 1.5rem', borderRadius: '10px', border: '1px solid #e2e8f0', background: '#f8fafc', color: '#475569', fontWeight: 600, cursor: 'pointer', transition: '0.2s' }}>Cancel</button>
          <button
            onClick={onConfirm}
            style={{
              padding: '0.75rem 1.5rem',
              borderRadius: '10px',
              border: 'none',
              cursor: 'pointer',
              backgroundColor: type === 'danger' ? '#ef4444' : '#059669',
              color: 'white',
              fontWeight: 700,
              transition: '0.2s'
            }}
          >
            Confirm Action
          </button>
        </div>
      </div>
    </div>
  );
}