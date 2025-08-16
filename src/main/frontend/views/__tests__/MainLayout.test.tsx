import { describe, expect, test } from 'vitest';
import { screen } from '@testing-library/react';
import { renderWithRouter } from '../../test-utils';

describe('MainLayout', () => {
  test('renders navigation items', () => {
    renderWithRouter('/');

    // Check for navigation links
    const importMutualFundElements = screen.getAllByText(/Import Mutual Funds/i);
    expect(importMutualFundElements.length).toBeGreaterThan(0);
    const mutualFundElements = screen.getAllByText(/Mutual Fund Schemes/i);
    expect(mutualFundElements.length).toBeGreaterThan(0);
    expect(screen.getByText(/UserPortfolio/i)).toBeInTheDocument();
    expect(screen.getByText(/ReBalance Calculator/i)).toBeInTheDocument();
  });
});
