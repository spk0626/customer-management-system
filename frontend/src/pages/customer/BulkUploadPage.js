import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { bulkApi } from '../../services/api';

export default function BulkUploadPage({ notify }) {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const navigate = useNavigate();

  const handleUpload = async () => {
    setUploading(true);
    try {
      const response = await bulkApi.upload(file);
      const job = response.data.data;
      notify(`File accepted. Job ${job.jobId} started for ${file.name}.`, 'success');
      setUploading(false);
      navigate('/');
    } catch {
      setUploading(false);
      notify('Bulk upload failed. Please verify the backend and file format.', 'error');
    }
  };

  return (
    <div className="container" style={{ padding: '3rem 2rem', maxWidth: '650px', margin: '0 auto' }}>
      <button onClick={() => navigate('/')} style={{ background: 'none', border: 'none', color: '#64748b', cursor: 'pointer', marginBottom: '1.5rem', fontWeight: 700 }}>← Return to Directory</button>
      <h1 style={{ fontSize: '2rem', fontWeight: 900, marginBottom: '0.75rem', color: '#0f172a' }}>Batch Data Import</h1>
      <p style={{ color: '#64748b', marginBottom: '2.5rem', fontSize: '1.05rem' }}>Securely upload member lists in XLSX, XLS, or CSV format.</p>

      <div style={{
        background: 'white',
        padding: '4rem 2rem',
        borderRadius: '24px',
        border: '2px dashed #cbd5e1',
        textAlign: 'center',
        transition: '0.3s',
        borderColor: file ? '#059669' : '#cbd5e1'
      }}>
        <div style={{ fontSize: '3.5rem', marginBottom: '1.5rem' }}>{file ? '📄' : '📁'}</div>
        <p style={{ fontWeight: 800, fontSize: '1.1rem', color: '#1e293b', marginBottom: '0.5rem' }}>
          {file ? file.name : 'Select Import File'}
        </p>
        <p style={{ fontSize: '0.9rem', color: '#94a3b8', marginBottom: '2rem' }}>
          {file ? `${(file.size / 1024).toFixed(2)} KB` : 'XLSX, CSV up to 10MB'}
        </p>

        <input type="file" style={{ display: 'none' }} id="file-upload" onChange={(e) => setFile(e.target.files[0])} />
        {!file ? (
          <label htmlFor="file-upload" style={{ display: 'inline-block', padding: '1rem 2rem', borderRadius: '12px', background: '#f1f5f9', color: '#0f172a', fontWeight: 800, cursor: 'pointer', transition: '0.2s' }}>
            Choose File From Device
          </label>
        ) : (
          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
            <button onClick={() => setFile(null)} style={{ padding: '1rem 1.5rem', borderRadius: '12px', background: 'white', border: '1px solid #e2e8f0', color: '#e11d48', fontWeight: 700, cursor: 'pointer' }}>Remove</button>
            <button
              onClick={handleUpload}
              disabled={uploading}
              style={{
                padding: '1rem 2.5rem',
                borderRadius: '12px',
                border: 'none',
                background: '#059669',
                color: 'white',
                fontWeight: 800,
                cursor: 'pointer',
                opacity: uploading ? 0.7 : 1
              }}
            >
              {uploading ? 'Processing...' : 'Execute Import'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}