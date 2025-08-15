import { describe, expect, test } from 'vitest';
import { screen } from '@testing-library/react';
import { renderWithRouter } from '../../test-utils';

describe('MainLayout', () => {
  test('renders navigation items', () => {
    renderWithRouter('/');
    
    // Check for navigation links
    expect(screen.getByText(/Import Mutual Funds/i)).toBeInTheDocument();
    const mutualFundElements = screen.getAllByText(/Mutual Fund Schemes/i);
    expect(mutualFundElements.length).toBeGreaterThan(0);
    expect(screen.getByText(/UserPortfolio/i)).toBeInTheDocument();
    expect(screen.getByText(/ReBalance Calculator/i)).toBeInTheDocument();
  });
});
