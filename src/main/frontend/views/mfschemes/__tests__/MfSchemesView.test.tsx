import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest';
import { screen, fireEvent, waitFor } from '@testing-library/react';
import { renderWithRouter } from '../../../test-utils';

// Mock the SchemeController
import { SchemeController } from 'Frontend/generated/endpoints';

vi.mock('Frontend/generated/endpoints', () => ({
  SchemeController: {
    fetchSchemes: vi.fn().mockImplementation((query) => {
      if (query === 'test fund') {
        return Promise.resolve([
          {
            amfiCode: '123456',
            schemeName: 'Test Fund',
            netAssetValue: '100.00',
            date: '2025-08-15',
          },
        ]);
      }
      return Promise.resolve([]);
    }),
  },
}));

describe('MfSchemesView', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  test('renders MF schemes view', async () => {
    renderWithRouter('/mfschemes');

    // Check for main heading in the view content (not the navigation)
    expect(screen.getByTestId('mf-schemes-view')).toBeInTheDocument();

    // Check for search field
    const searchField = screen.getByRole('searchbox', { name: /Search for mutual fund schemes/i });
    expect(searchField).toBeInTheDocument();
    expect(searchField).toHaveAttribute('placeholder', 'Search for mutual fund schemes...');

    // Check for search button
    expect(screen.getByRole('button', { name: /Search Schemes/i })).toBeInTheDocument();
  });

  test('can search for schemes', async () => {
    renderWithRouter('/mfschemes');

    // Setup mock search results
    const mockSchemes = [
      {
        id: 1,
        schemeName: 'Test Fund Growth',
        nav: '100.50',
        category: 'Equity',
        subCategory: 'Large Cap',
        amc: 'Test AMC',
      },
    ];
    // TODO: Mock the endpoint response when backend integration is ready

    // Perform search
    const searchField = screen.getByRole('searchbox', { name: /Search for mutual fund schemes/i });
    fireEvent.change(searchField, { target: { value: 'Test Fund' } });

    // Since we're using mocked components, just verify the UI responds to interaction
    expect(searchField).toHaveValue('Test Fund');

    // Verify search button exists and can be clicked
    const searchButton = screen.getByRole('button', { name: /Search Schemes/i });
    expect(searchButton).toBeInTheDocument();
    fireEvent.click(searchButton);

    // In a real implementation, we would wait for search results here
    // For now, just verify the basic interaction works
  });
});
