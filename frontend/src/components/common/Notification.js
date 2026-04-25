import React, { useEffect } from 'react';

export default function Notification({ message, type, onClose }) {
  useEffect(() => {
    const timer = setTimeout(onClose, 4000);
    return () => clearTimeout(timer);
  }, [onClose]);

  const config = {
    success: { bg: '#059669', icon: '✓' },
    error: { bg: '#ef4444', icon: '✕' },
    info: { bg: '#3b82f6', icon: 'ℹ' }
  }[type || 'success'];

  return (
    <div style={{
      position: 'fixed',
      top: '24px',
      right: '24px',
      zIndex: 2000,
      background: config.bg,
      color: 'white',
      padding: '16px 24px',
      borderRadius: '12px',
      boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)',
      display: 'flex',
      alignItems: 'center',
      gap: '12px',
      animation: 'slideInRight 0.4s cubic-bezier(0.16, 1, 0.3, 1)',
      minWidth: '300px'
    }}>
      <div style={{ background: 'rgba(255,255,255,0.2)', width: '28px', height: '28px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, fontSize: '14px', fontWeight: 'bold' }}>
        {config.icon}
      </div>
      <div style={{ flex: 1 }}>
        <div style={{ fontWeight: 700, fontSize: '0.9rem' }}>System Notification</div>
        <div style={{ fontSize: '0.85rem', opacity: 0.9 }}>{message}</div>
      </div>
      <button
        onClick={onClose}
        style={{ background: 'none', border: 'none', color: 'white', cursor: 'pointer', fontSize: '1.2rem', opacity: 0.7 }}
      >
        ×
      </button>
    </div>
  );
}