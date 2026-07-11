declare module '*.css?inline' {
  import type { CSSResultGroup } from 'lit';
  const content: CSSResultGroup;
  export default content;
}

declare module '*.css';

// Allow any CSS Custom Properties
declare module 'csstype' {
  interface Properties {
    [index: `--${string}`]: any;
  }
}
