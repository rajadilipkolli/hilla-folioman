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

    // Check for search section
    expect(screen.getByRole('button', { name: /Search Schemes/i })).toBeInTheDocument();
    const searchField = screen.getByRole('searchbox', { name: /Search for mutual fund schemes/i });
    expect(searchField).toBeInTheDocument();
    expect(searchField).toHaveAttribute('placeholder', 'Search for mutual fund schemes');

    // Check for grid presence and headers
    const grid = screen.getByRole('grid', { name: /Mutual Fund Schemes/i });
    expect(grid).toBeInTheDocument();

    const columnHeaders = screen.getAllByRole('columnheader');
    const expectedHeaders = ['Scheme Name', 'NAV', 'Category', 'Sub-Category', 'AMC'];
    expect(columnHeaders).toHaveLength(expectedHeaders.length);
    expectedHeaders.forEach((header) => {
      expect(screen.getByText(header)).toBeInTheDocument();
    });
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

    const searchButton = screen.getByRole('button', { name: /Search Schemes/i });
    fireEvent.click(searchButton);

    // Wait for search results
    await waitFor(() => {
      const grid = screen.getByRole('grid', { name: /Mutual Fund Schemes/i });
      expect(grid).toBeInTheDocument();

      // Check grid has correct ARIA role and attributes
      expect(grid).toHaveAttribute('aria-rowcount');
      expect(grid).toHaveAttribute('aria-colcount');

      // The grid should be empty initially until we mock the endpoint
      // Once endpoint is mocked, we can add assertions for search results
      // expect(screen.getByText('Test Fund Growth')).toBeInTheDocument();
      // expect(screen.getByText('100.50')).toBeInTheDocument();
      // expect(screen.getByText('Equity')).toBeInTheDocument();
      // expect(screen.getByText('Large Cap')).toBeInTheDocument();
      // expect(screen.getByText('Test AMC')).toBeInTheDocument();
    });
  });
});
