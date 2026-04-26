import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { bulkApi } from '../../services/api';

const sleep = (ms) => new Promise(resolve => window.setTimeout(resolve, ms));

export default function BulkUploadPage({ notify }) {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [jobStatus, setJobStatus] = useState(null);
  const navigate = useNavigate();

  const handleFileChange = (event) => {
    const nextFile = event.target.files[0];
    if (!nextFile) {
      setFile(null);
      return;
    }

    const lowerName = nextFile.name.toLowerCase();
    if (!lowerName.endsWith('.xlsx')) {
      notify('Please choose an Excel file in .xlsx format.', 'error');
      event.target.value = '';
      setFile(null);
      return;
    }

    setFile(nextFile);
    setJobStatus(null);
  };

  const waitForCompletion = async (jobId) => {
    for (let attempt = 0; attempt < 120; attempt += 1) {
      const response = await bulkApi.getJobStatus(jobId);
      const nextStatus = response.data.data;
      setJobStatus(nextStatus);

      if (nextStatus.status === 'COMPLETED') {
        return nextStatus;
      }

      if (nextStatus.status === 'FAILED') {
        throw new Error(nextStatus.errorMessage || 'Bulk upload failed during processing.');
      }

      await sleep(1500);
    }

    throw new Error('Bulk upload is taking longer than expected. Please check the job status again shortly.');
  };

  const handleUpload = async () => {
    setUploading(true);
    setJobStatus(null);

    try {
      const response = await bulkApi.upload(file);
      const acceptedJob = response.data.data;
      setJobStatus(acceptedJob);
      notify(`File accepted. Job ${acceptedJob.jobId} is now processing.`, 'info');

      const completedJob = await waitForCompletion(acceptedJob.jobId);
      notify(
        `Bulk upload completed. ${completedJob.processedRecords || 0} record(s) processed with ${completedJob.failedRecords || 0} failure(s).`,
        'success'
      );
      navigate('/');
    } catch (error) {
      notify(error.message || 'Bulk upload failed. Please verify the backend and file format.', 'error');
    } finally {
      setUploading(false);
    }
  };

  const progressLabel = jobStatus
    ? `${jobStatus.status}${jobStatus.processedRecords != null ? ` - ${jobStatus.processedRecords} processed` : ''}${jobStatus.failedRecords ? `, ${jobStatus.failedRecords} failed` : ''}`
    : null;

  const progressPercentage = jobStatus?.progressPercentage != null
    ? Math.max(0, Math.min(100, jobStatus.progressPercentage))
    : null;

  return (
    <div className="container" style={{ padding: '3rem 2rem', maxWidth: '650px', margin: '0 auto' }}>
      <button onClick={() => navigate('/')} style={{ background: 'none', border: 'none', color: '#64748b', cursor: 'pointer', marginBottom: '1.5rem', fontWeight: 700 }}>&larr; Return to Directory</button>
      <h1 style={{ fontSize: '2rem', fontWeight: 900, marginBottom: '0.75rem', color: '#0f172a' }}>Batch Data Import</h1>
      <p style={{ color: '#64748b', marginBottom: '2.5rem', fontSize: '1.05rem' }}>Upload an Excel .xlsx file with the mandatory customer fields: name, date of birth, and NIC.</p>

      <div style={{
        background: 'white',
        padding: '4rem 2rem',
        borderRadius: '24px',
        border: '2px dashed #cbd5e1',
        textAlign: 'center',
        transition: '0.3s',
        borderColor: file ? '#059669' : '#cbd5e1'
      }}>
        <div style={{ fontSize: '2rem', marginBottom: '1.5rem', fontWeight: 800, color: '#0f172a' }}>
          {uploading ? 'Import In Progress' : file ? 'File Ready' : 'Select File'}
        </div>
        <p style={{ fontWeight: 800, fontSize: '1.1rem', color: '#1e293b', marginBottom: '0.5rem' }}>
          {file ? file.name : 'Select Import File'}
        </p>
        <p style={{ fontSize: '0.9rem', color: '#94a3b8', marginBottom: '2rem' }}>
          {file ? `${(file.size / 1024).toFixed(2)} KB` : 'Excel (.xlsx)'}
        </p>

        {progressLabel && (
          <div style={{ marginBottom: '1.5rem', textAlign: 'left' }}>
            <div style={{ color: '#334155', fontWeight: 700, marginBottom: '0.5rem' }}>{progressLabel}</div>
            <div style={{ height: '10px', background: '#e2e8f0', borderRadius: '999px', overflow: 'hidden' }}>
              <div
                style={{
                  width: `${progressPercentage ?? 15}%`,
                  height: '100%',
                  background: 'linear-gradient(90deg, #059669 0%, #10b981 100%)',
                  transition: 'width 0.3s ease'
                }}
              />
            </div>
          </div>
        )}

        <input type="file" accept=".xlsx" style={{ display: 'none' }} id="file-upload" onChange={handleFileChange} disabled={uploading} />
        {!file ? (
          <label htmlFor="file-upload" style={{ display: 'inline-block', padding: '1rem 2rem', borderRadius: '12px', background: '#f1f5f9', color: '#0f172a', fontWeight: 800, cursor: uploading ? 'default' : 'pointer', transition: '0.2s', opacity: uploading ? 0.6 : 1 }}>
            Choose File From Device
          </label>
        ) : (
          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
            <button onClick={() => { setFile(null); setJobStatus(null); }} disabled={uploading} style={{ padding: '1rem 1.5rem', borderRadius: '12px', background: 'white', border: '1px solid #e2e8f0', color: '#e11d48', fontWeight: 700, cursor: uploading ? 'default' : 'pointer', opacity: uploading ? 0.6 : 1 }}>Remove</button>
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
                cursor: uploading ? 'default' : 'pointer',
                opacity: uploading ? 0.7 : 1
              }}
            >
              {uploading ? 'Waiting For Completion...' : 'Execute Import'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
