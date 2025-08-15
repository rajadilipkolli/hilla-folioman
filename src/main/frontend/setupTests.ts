import '@testing-library/jest-dom';
import { cleanup } from '@testing-library/react';
import { afterEach, beforeAll, vi } from 'vitest';
import React from 'react';

// Mock Vaadin components
vi.mock('@vaadin/react-components', () => ({
  Button: ({ children, onClick, theme, slot, disabled }: { children: React.ReactNode; onClick?: () => void; theme?: string; slot?: string, disabled?: boolean }) => 
    React.createElement('vaadin-button', { 
      onClick, 
      'data-theme': theme, 
      'data-slot': slot,
      disabled,
      role: 'button',
      name: typeof children === 'string' ? children : undefined,
      'aria-label': typeof children === 'string' ? children : undefined
    }, children),
  FormLayout: ({ children }: { children: React.ReactNode }) => React.createElement('vaadin-form-layout', { 'data-testid': 'form-layout' }, children),
  FormLayoutResponsiveStep: vi.fn(() => null),
  Notification: { show: vi.fn() },
  Upload: ({ children, id, accept }: { children: React.ReactNode; id?: string, accept?: string }) => 
    React.createElement('vaadin-upload', { 
      id, 
      accept,
      'data-testid': 'upload',
      role: 'group',
      'aria-label': 'File upload'
    }, children),
  PasswordField: ({ label, value, onValueChanged }: { label: string; value: string; onValueChanged: (e: { detail: { value: string }}) => void }) => 
    React.createElement('vaadin-password-field', { 
      'data-label': label,
      'value': value || '',
      'aria-label': label,
      'label': label,
      role: 'textbox',
      name: label,
      'onChange': (e: React.ChangeEvent<HTMLInputElement>) => onValueChanged({ detail: { value: e.target.value } })
    }),
  Grid: ({ children, items }: { children?: React.ReactNode; items?: Record<string, any>[] }) => {
    const gridContent = items?.map(item => 
      React.createElement('div', { key: item.amfiCode, role: 'row' }, item.schemeName)
    ) || children;
    return React.createElement('vaadin-grid', { 
      role: 'grid',
      'data-testid': 'grid',
    }, gridContent);
  },
  GridColumn: vi.fn(() => null),
  Icon: ({ icon, slot }: { icon?: string; slot?: string }) => React.createElement('span', { 'data-icon': icon, 'data-slot': slot }),
  TextField: ({ placeholder, value, onChange, children }: { placeholder: string; value: string; onChange: (e: { target: { value: string }}) => void; children?: React.ReactNode }) => React.createElement('div', {}, [
    React.createElement('input', {
      type: 'text',
      placeholder,
      value: value || '',
      onChange: (e: React.ChangeEvent<HTMLInputElement>) => onChange({ target: { value: e.target.value } }),
      key: 'input'
    }),
    children
  ]),
  Dialog: ({ children }: { children: React.ReactNode }) => React.createElement('div', { 'data-testid': 'dialog' }, children),
  DatePicker: vi.fn(() => null),
  Card: ({ children }: { children: React.ReactNode }) => React.createElement('div', { 'data-testid': 'card' }, children),
  Details: ({ children, summary }: { children: React.ReactNode; summary?: React.ReactNode }) => React.createElement('details', {}, [
    React.createElement('summary', { key: 'summary' }, summary),
    children
  ]),
  VaadinHeadingH3: createHeading(3),
  Scroller: vi.fn(() => null),
}));

// Mock ResizeObserver
beforeAll(() => {
  vi.stubGlobal('ResizeObserver', vi.fn(() => ({
    observe: vi.fn(),
    unobserve: vi.fn(),
    disconnect: vi.fn(),
  })));
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
