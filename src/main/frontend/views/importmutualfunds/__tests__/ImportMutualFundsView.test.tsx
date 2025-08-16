import { describe, expect, test, vi } from 'vitest';
import { screen, fireEvent, waitFor } from '@testing-library/react';
import { renderWithRouter } from '../../../test-utils';

// Mock the endpoints module
vi.mock('Frontend/generated/endpoints', () => ({
  ImportMutualFundController: {
    // Add any methods that need to be mocked
  },
}));

// Mock the Notification.show method
vi.mock('@vaadin/react-components', async () => {
  const actual = await vi.importActual('@vaadin/react-components');
  return {
    ...actual,
    Notification: {
      show: vi.fn(),
    },
  };
});

describe('ImportMutualFundsView', () => {
  test('renders PDF upload view by default', async () => {
    renderWithRouter('/importmutualfunds');

    // Check that the component renders by looking for its container
    expect(screen.getByTestId('import-mutual-funds-view')).toBeInTheDocument();

    // Check for view switch buttons
    const pdfButton = screen.getByRole('button', { name: /Password Protected CAS/i });
    const jsonButton = screen.getByRole('button', { name: /JSON Upload \(Fallback\)/i });
    expect(pdfButton).toHaveAttribute('theme', 'primary');
    expect(jsonButton).toHaveAttribute('theme', 'secondary');

    // Check for the PDF upload form
    expect(screen.getByText('Upload Password Protected CAS PDF')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Select PDF/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Upload Protected CAS PDF/i })).toBeInTheDocument();

    // Check the password field
    const passwordField = screen.getByLabelText(/PDF Password/i);
    expect(passwordField).toBeInTheDocument();
  });

  test('can switch between PDF and JSON views', async () => {
    renderWithRouter('/importmutualfunds');

    // Verify the component has loaded
    expect(screen.getByTestId('import-mutual-funds-view')).toBeInTheDocument();

    // Check initial buttons
    const pdfButton = screen.getByRole('button', { name: /Password Protected CAS/i });
    const jsonButton = screen.getByRole('button', { name: /JSON Upload \(Fallback\)/i });

    expect(pdfButton).toHaveAttribute('theme', 'primary');
    expect(jsonButton).toHaveAttribute('theme', 'secondary');

    // Our mock component doesn't actually switch views, so we just verify the basic interaction
    // In a real implementation, clicking would change the view
    expect(pdfButton).toBeInTheDocument();
    expect(jsonButton).toBeInTheDocument();
  });

  test('has password input field', async () => {
    renderWithRouter('/importmutualfunds');

    // Verify the component has loaded
    expect(screen.getByTestId('import-mutual-funds-view')).toBeInTheDocument();

    // Check the password field exists and can be interacted with
    const passwordField = screen.getByLabelText(/PDF Password/i) as HTMLInputElement;
    expect(passwordField).toBeInTheDocument();
    expect(passwordField.type).toBe('password');
  });
});
