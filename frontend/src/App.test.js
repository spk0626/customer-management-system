import { render, screen } from '@testing-library/react';
import App from './App';

test('renders customer directory landing page', async () => {
  render(<App />);
  expect(await screen.findByText(/member directory/i)).toBeInTheDocument();
  expect(screen.getByRole('button', { name: /new registration/i })).toBeInTheDocument();
});
