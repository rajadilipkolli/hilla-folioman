import { IObservableValue } from 'mobx';
import { TemplateResult } from 'lit';
import { IconKeyType } from './icons';
import { CopilotExperimentalFeature } from './copilot-experimental-features';
import { CopilotActiveMode } from './copilot-modes';
/**
 * Plugin API for the dev tools window.
 */
export interface CopilotInterface {
    send(command: string, data: any): void;
    addPanel(panel: PanelConfiguration): void;
}
export interface MessageHandler {
    handleMessage(message: ServerMessage): boolean;
}
export interface ServerMessage {
    /**
     * The command
     */
    command: string;
    /**
     * The data for the command
     */
    data: any;
}
export type Framework = 'flow' | 'hilla-lit' | 'hilla-react';
export interface CopilotPlugin {
    /**
     * Called once to initialize the plugin.
     *
     * @param copilotInterface provides methods to interact with the dev tools
     */
    init(copilotInterface: CopilotInterface): void;
}
export declare enum MessageType {
    INFORMATION = "information",
    WARNING = "warning",
    ERROR = "error"
}
export interface Message {
    id: number;
    type: MessageType;
    message: string;
    timestamp: Date;
    details?: IObservableValue<TemplateResult> | string;
    link?: string;
    persistentId?: string;
    dontShowAgain: boolean;
    deleted: boolean;
}
/**
 * Known Copilot panel tags used throughout the application.
 * This object serves as a registry of all panel identifiers.
 */
export declare const CopilotPanelTags: {
    readonly DEVELOPMENT_SETUP: "copilot-development-setup-user-guide";
    readonly FEATURES: "copilot-features-panel";
    readonly FEEDBACK: "copilot-feedback-panel";
    readonly INFO: "copilot-info-panel";
    readonly LOG: "copilot-log-panel";
    readonly SETTINGS: "copilot-settings-panel";
    readonly IMPERSONATOR: "copilot-impersonator";
    readonly A11Y_CHECKER: "copilot-a11y-checker";
    readonly BACKEND_AND_DATA: "copilot-backend-and-data-panel";
    readonly COMPONENT_PROPERTIES: "copilot-component-properties";
    readonly CONNECT_TO_SERVICE: "copilot-connect-to-service";
    readonly DOCS: "copilot-docs";
    readonly EDIT_COMPONENT: "copilot-edit-component";
    readonly I18N: "copilot-i18n-panel";
    readonly NEW_ROUTE: "copilot-new-route";
    readonly OUTLINE: "copilot-outline-panel";
    readonly PALETTE: "copilot-palette";
    readonly ROUTES: "copilot-routes-panel";
    readonly SPRING_SECURITY: "copilot-spring-security";
    readonly TEST_BENCH_TEST_GENERATOR: "copilot-test-bench-test-generator-panel";
    readonly THEME_EDITOR: "copilot-theme-editor-panel";
    readonly UI_SERVICES: "copilot-ui-services-panel";
    readonly UI_TEST_GENERATOR: "copilot-ui-test-generator-panel";
    readonly VAADIN_VERSIONS: "copilot-vaadin-versions";
    readonly COMMENTS: "copilot-comments-panel";
    readonly NEW_COMMENT: "copilot-new-comment";
    readonly TEST_FOO_PANEL: "foo-panel";
    readonly TEST_LOG_PANEL: "test-log-panel";
    readonly TEST_PLUGIN_PANEL: "test-plugin-panel";
};
/**
 * Type representing all known Copilot panel tags.
 * String literals matching these values are automatically compatible.
 */
export type CopilotPanelTag = (typeof CopilotPanelTags)[keyof typeof CopilotPanelTags];
export type CopilotActiveModeAndCommon = CopilotActiveMode | 'common';
type CopilotActiveModeOrderMap = Partial<Record<CopilotActiveModeAndCommon, number>>;
export interface PanelConfiguration {
    header: string;
    tag: CopilotPanelTag;
    actionsTag?: string;
    footerActionsTag?: string;
    position?: {
        top: number;
        left: number;
        width?: number;
        height?: number;
    };
    showWhileDragging?: boolean;
    helpUrl?: string;
    /**
     * These panels can be visible regardless of copilot activation status
     */
    individual?: boolean;
    /**
     * A panel is rendered the first time when it is expanded unless eager is set to true, which causes it be always be rendered
     */
    eager?: boolean;
    /**
     * Marks the panel as experimental feature, evaluates if enabled before showing the panel
     */
    experimental?: CopilotExperimentalFeature;
    toolbarOptions?: {
        allowedModesWithOrder: CopilotActiveModeOrderMap;
        iconKey: IconKeyType;
    };
}
/**
 * Checks if a given tag belongs to a public (Apache 2.0) panel.
 *
 * @param tag The panel tag to check
 * @returns true if the tag is from a public panel, false otherwise
 */
export declare function isPublicPanel(tag: CopilotPanelTag): boolean;
export {};
