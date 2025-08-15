import { describe, expect, test, vi } from 'vitest';
import { screen, fireEvent, waitFor } from '@testing-library/react';
import { renderWithRouter } from '../../../test-utils';
import { Notification } from '@vaadin/react-components';

// Mock the endpoints module
vi.mock('Frontend/generated/endpoints', () => ({
  ImportMutualFundController: {
    // Add any methods that need to be mocked
  }
}));

describe('ImportMutualFundsView', () => {
  test('renders PDF upload view by default', () => {
    renderWithRouter('/importmutualfunds');
    
    // Check for Import Mutual Funds heading
    const headings = screen.getAllByText('Import Mutual Funds');
    expect(headings).toHaveLength(2);
    
    // Check for view switch buttons
    const pdfButton = screen.getByRole('button', { name: /Password Protected CAS/i });
    const jsonButton = screen.getByRole('button', { name: /JSON Upload \(Fallback\)/i });
    expect(pdfButton).toHaveAttribute('data-theme', 'primary');
    expect(jsonButton).toHaveAttribute('data-theme', 'secondary');
    
    // Check for the PDF upload form
    expect(screen.getByText('Upload Password Protected CAS PDF')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Select PDF/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Upload Protected CAS PDF/i })).toBeInTheDocument();
    
    // Check the password field
    const passwordField = screen.getByRole('textbox', { name: /PDF Password/i });
    expect(passwordField).toBeInTheDocument();
    expect(passwordField).toHaveAttribute('label', 'PDF Password');
  });

  test('can switch between PDF and JSON views', () => {
    renderWithRouter('/importmutualfunds');

    // Find and click the JSON view button
    const jsonButton = screen.getByRole('button', { name: /JSON Upload \(Fallback\)/i });
    fireEvent.click(jsonButton);

    // In JSON view
    expect(screen.getByText('Upload CAS JSON File (Fallback Method)')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Select JSON File/i })).toBeInTheDocument();
    expect(screen.getByRole('group', { name: /File upload/i })).toBeInTheDocument();

    // Switch back to PDF view
    const pdfButton = screen.getByRole('button', { name: /Password Protected CAS/i });
    fireEvent.click(pdfButton);

    // Back in PDF view
    expect(screen.getByText('Upload Password Protected CAS PDF')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Select PDF/i })).toBeInTheDocument();
    expect(screen.getByRole('group', { name: /File upload/i })).toBeInTheDocument();
  });

  test('clears password after inactivity', async () => {
    vi.useFakeTimers();
    
    renderWithRouter('/importmutualfunds');

    // Find the password field by its label
    const passwordField = screen.getByRole('textbox', { name: /PDF Password/i }) as HTMLInputElement;
    expect(passwordField).toHaveAttribute('label', 'PDF Password');

    // Set the password
    fireEvent.change(passwordField, { target: { value: 'testpassword' } });
    expect(passwordField.value).toBe('testpassword');

    // Advance time by 5 minutes
    vi.advanceTimersByTime(5 * 60 * 1000);

    // Verify notification was shown and password was cleared
    expect(Notification.show).toHaveBeenCalledWith(
      'Password cleared due to inactivity', 
      expect.objectContaining({ position: 'bottom-end', duration: 3000 })
    );
    
    // Wait for password field to be cleared
    await waitFor(() => {
      const updatedPasswordField = screen.getByRole('textbox', { name: /PDF Password/i }) as HTMLInputElement;
      expect(updatedPasswordField.value).toBe('');
    });

    vi.useRealTimers();
  });
});
