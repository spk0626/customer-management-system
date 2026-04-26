import { render, screen } from '@testing-library/react';
import React from 'react';

jest.mock('./pages/customer/CustomerListPageContent', () => () => (
  <div>
    <h1>Customer Directory</h1>
    <button type="button">New Registration</button>
  </div>
));

jest.mock('./pages/customer/CustomerFormPage', () => () => <div>Customer Form</div>);
jest.mock('./pages/customer/CustomerViewPage', () => () => <div>Customer View</div>);
jest.mock('./pages/customer/BulkUploadPage', () => () => <div>Bulk Upload</div>);

const App = require('./App').default;

test('renders customer directory landing page', async () => {
  render(<App />);
  expect(await screen.findByText(/customer directory/i)).toBeInTheDocument();
  expect(screen.getByRole('button', { name: /new registration/i })).toBeInTheDocument();
});
