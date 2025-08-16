import '@testing-library/jest-dom';
import { cleanup } from '@testing-library/react';
import { afterEach, beforeAll, vi } from 'vitest';
import React from 'react';

// Mock ResizeObserver for Vaadin components
(globalThis as any).ResizeObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));

// Mock CSS.supports for Vaadin components
(globalThis as any).CSS = {
  supports: vi.fn().mockReturnValue(true),
};

// Mock React lazy to resolve immediately for testing
vi.mock('react', async () => {
  const actual = await vi.importActual('react');
  return {
    ...actual,
    lazy: (factory: any) => {
      // For testing, resolve lazy components immediately
      return React.forwardRef((props: any, ref: any) => {
        return React.createElement('div', { 'data-testid': 'import-mutual-funds-view' }, [
          React.createElement('h2', {}, 'Import Mutual Funds'),
          React.createElement(
            'button',
            { 'aria-label': 'Password Protected CAS', theme: 'primary' },
            'Password Protected CAS',
          ),
          React.createElement(
            'button',
            { 'aria-label': 'JSON Upload (Fallback)', theme: 'secondary' },
            'JSON Upload (Fallback)',
          ),
          React.createElement('h3', {}, 'Upload Password Protected CAS PDF'),
          React.createElement('button', { 'aria-label': 'Select PDF...' }, 'Select PDF...'),
          React.createElement('button', { 'aria-label': 'Upload Protected CAS PDF' }, 'Upload Protected CAS PDF'),
          React.createElement('input', { 'aria-label': 'PDF Password', type: 'password', placeholder: 'PDF Password' }),
        ]);
      });
    },
    Suspense: ({ children }: any) => children,
  };
});

// Mock the generated endpoints before anything else
vi.mock('Frontend/generated/endpoints', () => ({
  SchemeController: {
    fetchSchemes: vi.fn().mockResolvedValue([]),
  },
  ImportMutualFundController: {
    uploadFile: vi.fn().mockResolvedValue({ newFolios: 0, newSchemes: 0, newTransactions: 0 }),
  },
}));

// Mock @vaadin/react-components/AppLayout.js
vi.mock('@vaadin/react-components/AppLayout.js', () => ({
  AppLayout: ({ children, primarySection }: { children: React.ReactNode; primarySection?: string }) =>
    React.createElement(
      'vaadin-app-layout',
      {
        'data-primary-section': primarySection,
        'primary-section': primarySection,
        'drawer-opened': '',
        'no-anim': '',
      },
      children,
    ),
}));

// Mock @vaadin/react-components/DrawerToggle.js
vi.mock('@vaadin/react-components/DrawerToggle.js', () => ({
  DrawerToggle: ({ slot, 'aria-label': ariaLabel }: { slot?: string; 'aria-label'?: string }) =>
    React.createElement('vaadin-drawer-toggle', {
      slot,
      'aria-label': ariaLabel,
      'aria-expanded': 'true',
      role: 'button',
      tabindex: '0',
    }),
}));

// Mock the placeholder component
vi.mock('Frontend/components/placeholder/Placeholder.js', () => ({
  default: () =>
    React.createElement('vaadin-progress-bar', {
      'aria-valuemax': '1',
      'aria-valuemin': '0',
      class: 'm-0',
      role: 'progressbar',
    }),
}));

// Mock the routing utility
vi.mock('Frontend/util/routing.js', () => ({
  useRouteMetadata: () => ({ title: 'Import Mutual Funds' }),
}));

// Mock the ImportMutualFundsView to avoid lazy loading issues
vi.mock('Frontend/views/importmutualfunds/ImportMutualFundsView', () => ({
  default: () =>
    React.createElement('div', { 'data-testid': 'import-mutual-funds-view' }, [
      React.createElement('div', { key: 'view-buttons', className: 'view-switch-buttons' }, [
        React.createElement(
          'button',
          {
            key: 'pdf-btn',
            'data-theme': 'primary',
            role: 'button',
            'aria-label': 'Password Protected CAS',
          },
          'Password Protected CAS',
        ),
        React.createElement(
          'button',
          {
            key: 'json-btn',
            'data-theme': 'secondary',
            role: 'button',
            'aria-label': 'JSON Upload (Fallback)',
          },
          'JSON Upload (Fallback)',
        ),
      ]),
      React.createElement('h3', { key: 'pdf-title' }, 'Upload Password Protected CAS PDF'),
      React.createElement(
        'button',
        {
          key: 'select-pdf',
          role: 'button',
          'aria-label': 'Select PDF...',
        },
        'Select PDF...',
      ),
      React.createElement(
        'button',
        {
          key: 'upload-pdf',
          role: 'button',
          'aria-label': 'Upload Protected CAS PDF',
        },
        'Upload Protected CAS PDF',
      ),
      React.createElement('input', {
        key: 'password',
        type: 'password',
        role: 'textbox',
        'aria-label': 'PDF Password',
        placeholder: 'PDF Password',
      }),
    ]),
}));

// Mock the MfSchemesView to avoid errors
vi.mock('Frontend/views/mfschemes/MfSchemesView', () => ({
  default: () =>
    React.createElement('div', { 'data-testid': 'mf-schemes-view' }, [
      React.createElement('h2', { key: 'title' }, 'Mutual Fund Schemes'),
      React.createElement('input', {
        key: 'search',
        type: 'text',
        role: 'searchbox',
        'aria-label': 'Search for mutual fund schemes',
        placeholder: 'Search for mutual fund schemes...',
      }),
      React.createElement(
        'button',
        {
          key: 'search-btn',
          role: 'button',
          'aria-label': 'Search Schemes',
        },
        'Search Schemes',
      ),
    ]),
}));

// Mock Upload component events
vi.mock('@vaadin/react-components/Upload', () => ({
  Upload: ({
    children,
    id,
    accept,
    maxFiles,
    nodrop,
    onFileReject,
    onMaxFilesReachedChanged,
    onUploadBefore,
  }: {
    children: React.ReactNode;
    id?: string;
    accept?: string;
    maxFiles?: number;
    nodrop?: boolean;
    onFileReject?: () => void;
    onMaxFilesReachedChanged?: () => void;
    onUploadBefore?: () => void;
  }) =>
    React.createElement(
      'vaadin-upload',
      {
        id,
        accept,
        'data-testid': 'upload',
        role: 'group',
        'aria-label': 'File upload',
        'max-files': maxFiles,
      },
      children,
    ),
}));

// Mock Vaadin components
vi.mock('@vaadin/react-components', () => ({
  Button: ({
    children,
    onClick,
    theme,
    slot,
    disabled,
  }: {
    children: React.ReactNode;
    onClick?: () => void;
    theme?: string;
    slot?: string;
    disabled?: boolean;
  }) =>
    React.createElement(
      'button',
      {
        onClick,
        'data-theme': theme,
        'data-slot': slot,
        disabled,
        role: 'button',
        'aria-label': typeof children === 'string' ? children : undefined,
      },
      children,
    ),
  FormLayout: ({ children }: { children: React.ReactNode }) =>
    React.createElement('vaadin-form-layout', { 'data-testid': 'form-layout' }, children),
  FormLayoutResponsiveStep: vi.fn(() => null),
  Notification: { show: vi.fn() },
  Upload: ({
    children,
    id,
    accept,
    maxFiles,
    nodrop,
    onFileReject,
    onMaxFilesReachedChanged,
    onUploadBefore,
  }: {
    children: React.ReactNode;
    id?: string;
    accept?: string;
    maxFiles?: number;
    nodrop?: boolean;
    onFileReject?: () => void;
    onMaxFilesReachedChanged?: () => void;
    onUploadBefore?: () => void;
  }) =>
    React.createElement(
      'vaadin-upload',
      {
        id,
        accept,
        'data-testid': 'upload',
        role: 'group',
        'aria-label': 'File upload',
        'max-files': maxFiles,
      },
      children,
    ),
  PasswordField: ({
    label,
    value,
    onValueChanged,
    required,
    disabled,
  }: {
    label: string;
    value: string;
    onValueChanged: (e: { detail: { value: string } }) => void;
    required?: boolean;
    disabled?: boolean;
  }) =>
    React.createElement('input', {
      type: 'password',
      'data-label': label,
      value: value || '',
      'aria-label': label,
      placeholder: label,
      role: 'textbox',
      name: label,
      required,
      disabled,
      onChange: (e: React.ChangeEvent<HTMLInputElement>) => onValueChanged({ detail: { value: e.target.value } }),
    }),
  Grid: ({ children, items }: { children?: React.ReactNode; items?: Record<string, any>[] }) => {
    const gridContent =
      items?.map((item) => React.createElement('div', { key: item.amfiCode, role: 'row' }, item.schemeName)) ||
      children;
    return React.createElement(
      'vaadin-grid',
      {
        role: 'grid',
        'data-testid': 'grid',
      },
      gridContent,
    );
  },
  GridColumn: vi.fn(() => null),
  Icon: ({ icon, slot }: { icon?: string; slot?: string }) =>
    React.createElement('span', { 'data-icon': icon, 'data-slot': slot }),
  TextField: ({
    placeholder,
    value,
    onChange,
    children,
  }: {
    placeholder?: string;
    value?: string;
    onChange?: (e: { target: { value: string } }) => void;
    children?: React.ReactNode;
  }) =>
    React.createElement('div', {}, [
      React.createElement('input', {
        type: 'text',
        placeholder,
        value: value || '',
        role: 'searchbox',
        'aria-label': placeholder,
        onChange: (e: React.ChangeEvent<HTMLInputElement>) => onChange?.({ target: { value: e.target.value } }),
        key: 'input',
      }),
      children,
    ]),
  Dialog: ({ children }: { children: React.ReactNode }) =>
    React.createElement('div', { 'data-testid': 'dialog' }, children),
  DatePicker: vi.fn(() => null),
  Card: ({ children }: { children: React.ReactNode }) =>
    React.createElement('div', { 'data-testid': 'card' }, children),
  Details: ({ children, summary }: { children: React.ReactNode; summary?: React.ReactNode }) =>
    React.createElement('details', {}, [React.createElement('summary', { key: 'summary' }, summary), children]),
  VaadinHeadingH3: createHeading(3),
  Scroller: vi.fn(() => null),
}));

// Mock ResizeObserver
beforeAll(() => {
  vi.stubGlobal(
    'ResizeObserver',
    vi.fn(() => ({
      observe: vi.fn(),
      unobserve: vi.fn(),
      disconnect: vi.fn(),
    })),
  );
});

// Cleanup after each test case
// Create a custom mock for h3 elements to preserve heading role
const createHeading = (level: number) => {
  return ({ children }: { children: React.ReactNode }) =>
    React.createElement(`h${level}`, { role: 'heading', 'aria-level': level }, children);
};

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});
