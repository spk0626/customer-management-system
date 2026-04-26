import React from 'react';
import { NavLink } from 'react-router-dom';

const linkBaseStyle = {
  textDecoration: 'none',
  padding: '0.65rem 1rem',
  borderRadius: '999px',
  fontWeight: 700,
};

const activeLinkStyle = {
  background: 'rgba(255,255,255,0.18)',
};

export default function AppNavbar() {
  return (
    <header style={{
      position: 'sticky',
      top: 0,
      zIndex: 900,
      background: 'linear-gradient(135deg, #064e3b 0%, #059669 55%, #10b981 100%)',
      color: 'white',
      boxShadow: '0 12px 32px rgba(6, 95, 70, 0.18)'
    }}>
      <div style={{ maxWidth: '1180px', margin: '0 auto', padding: '1rem 2rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '1rem' }}>
        <div style={{ fontSize: '1.15rem', fontWeight: 900 }}>Customer Management System</div>
        <nav style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', justifyContent: 'flex-end' }}>
          <NavLink
            to="/"
            end
            style={({ isActive }) => ({
              ...linkBaseStyle,
              color: 'white',
              background: isActive ? 'rgba(255,255,255,0.12)' : 'transparent',
              ...(isActive ? activeLinkStyle : {}),
            })}
          >
            Customers
          </NavLink>
          <NavLink
            to="/customers/new"
            style={({ isActive }) => ({
              ...linkBaseStyle,
              color: isActive ? '#064e3b' : '#064e3b',
              background: 'white',
            })}
          >
            New Record
          </NavLink>
          <NavLink
            to="/bulk-upload"
            style={({ isActive }) => ({
              ...linkBaseStyle,
              color: 'white',
              border: '1px solid rgba(255,255,255,0.3)',
              background: isActive ? 'rgba(255,255,255,0.12)' : 'transparent',
              ...(isActive ? activeLinkStyle : {}),
            })}
          >
            Bulk Upload
          </NavLink>
        </nav>
      </div>
    </header>
  );
}
