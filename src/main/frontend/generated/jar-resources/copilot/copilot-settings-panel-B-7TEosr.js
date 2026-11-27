import { n as e } from "./chunk-DiqZc92J.js";
import { X as t, Y as n, ft as r, n as i, o as a, r as o, t as s, u as c, ut as l } from "./icons-Bzfd0PEL.js";
import { l as u, o as d } from "./consts-BXxpRU5f.js";
import { a as f, d as p, l as m, o as h, s as g } from "./section-panel-ui-state-COWn2ulI.js";
import { a as _, n as v, r as y } from "./copilot-ui-state-Dc6l_5DA.js";
import { t as b } from "./directive-DWLihZIi.js";
import { a as x, f as S, o as C, p as w } from "./copilot-error-handler-q8B4DBC6.js";
import { n as T, t as E } from "./copilot-stored-machine-state-D6qB_Peh.js";
import { a as D, i as O } from "./copilot-shortcuts-D_fKEFcO.js";
import { n as k, t as A } from "./base-panel-3DcY1Auw.js";
import { n as j, r as M, t as N } from "./lit-renderer-DVJxWUIs.js";
//#region node_modules/@vaadin/select/src/lit/renderer-directives.js
var P, F, I = e((() => {
	M(), j(), P = class extends N {
		addRenderer() {
			this.element.renderer = (e, t) => {
				this.renderRenderer(e, t);
			};
		}
		runRenderer() {
			this.element.requestContentUpdate();
		}
		removeRenderer() {
			this.element.renderer = null;
		}
	}, F = b(P);
})), L = e((() => {
	I();
})), R, z = e((() => {
	if (_(), R = window.Vaadin.copilot.tree, !R) throw Error("Tried to access copilot tree before it was initialized.");
})), B, V, H = e((() => {
	B = [
		{
			dismissId: "connection-lost",
			title: "Connection lost",
			description: "Shown when Copilot loses its frontend connection."
		},
		{
			dismissId: "copilot-ide-plugin-missing",
			title: "IDE plugin recommendation",
			description: "Shown when additional IDE integration features are available but the IDE plugin is not active."
		},
		{
			dismissId: "devmode",
			title: "Development mode reminder",
			description: "Shown when the app is running in development mode."
		},
		{
			dismissId: "java-modification-no-hotswap",
			title: "Java hotswap recommendation",
			description: "Shown when Java source changes are made without hotswap support."
		},
		{
			dismissId: "react_route_component_is_class",
			title: "React class component limitation",
			description: "Shown when a route uses a class component and Copilot functionality is reduced."
		},
		{
			dismissId: "variable-renamed",
			title: "Variable renamed",
			description: "Shown when Copilot renames a generated Java variable."
		},
		{
			dismissId: "view-deleted-restart-required",
			title: "Route view deleted",
			description: "Shown when a deleted route requires a restart to fully apply."
		}
	], V = B.slice().sort((e, t) => e.title.localeCompare(t.title));
})), U, W;
//#endregion
e((() => {
	g(), o(), i(), r(), S(), L(), O(), z(), H(), E(), y(), k(), n(), u(), C(), h(), U = class extends A {
		constructor(...e) {
			super(...e), this.selectedTab = 0, this.activationShortcutEnabled = T.isActivationShortcut(), this.aiUsage = T.isAIUsageAllowed(), this.sendErrorReportsAllowed = T.isSendErrorReportsAllowed(), this.hideCopilotRequestOngoing = !1, this.hideCopilotDialogVisible = !1, this.sizeItems = [{
				label: "Default",
				value: "default"
			}, {
				label: "Compact",
				value: "compact"
			}], this.themeItems = [
				{
					label: "System",
					value: "system"
				},
				{
					label: "Light",
					value: "light"
				},
				{
					label: "Dark",
					value: "dark"
				}
			], this.toolbarExpandModeItems = [
				{
					label: "Proximity",
					value: "proximity",
					description: "The toolbar expands and becomes fully visible as the mouse pointer approaches it."
				},
				{
					label: "Click",
					value: "click",
					description: "The toolbar expands and becomes fully visible when Play mode is clicked."
				},
				{
					label: "Hover",
					value: "hover",
					description: "The toolbar expands and becomes fully visible when the mouse hovers over it."
				},
				{
					label: "Always expanded",
					value: "always",
					description: "The toolbar remains fully visible at all times and never collapses or becomes translucent."
				},
				{
					label: "Disabled",
					value: "never",
					description: "Only Play mode is visible. Changing Copilot mode is not available, and keyboard shortcuts are disabled."
				}
			], this.badgePositionItems = [{
				label: "Smart",
				value: "smart",
				description: "Automatically finds the best position by avoiding overlaps with nearby elements."
			}, {
				label: "Static",
				value: "static",
				description: "Keeps the badge in a predefined position regardless of surrounding elements."
			}], this.aiUsageItems = [
				{
					label: "Ask each time",
					value: "ask"
				},
				{
					label: "Allow",
					value: "yes"
				},
				{
					label: "Deny",
					value: "no"
				}
			], this.aiProviderItems = [{
				label: "Any region",
				value: "ANY"
			}, {
				label: "EU only",
				value: "EU_ONLY"
			}], this.toggleActivationShortcut = () => {
				this.trackPanelEvent("use-settings", {
					source: "activationShortcut",
					selected: !this.activationShortcutEnabled,
					previous: this.activationShortcutEnabled
				}), this.activationShortcutEnabled = !this.activationShortcutEnabled, T.setActivationShortcut(this.activationShortcutEnabled);
			}, this.toggleSendErrorReports = () => {
				this.trackPanelEvent("use-settings", {
					source: "sendErrorReports",
					selected: !this.sendErrorReportsAllowed,
					previous: this.sendErrorReportsAllowed
				}), this.sendErrorReportsAllowed = !this.sendErrorReportsAllowed, T.setSendErrorReportsAllowed(this.sendErrorReportsAllowed);
			};
		}
		connectedCallback() {
			super.connectedCallback(), this.classList.add("flex", "flex-col", "h-full");
		}
		updated(e) {
			super.updated(e);
		}
		renderKbd(e) {
			return w(e.replace(/<kbd([^>]*)class="([^"]*)"/, "<kbd$1class=\"$2 font-sans ms-auto\"").replace(/<kbd(?![^>]*class=)/, "<kbd class=\"font-sans ms-auto\""));
		}
		render() {
			return c`
      <vaadin-tabs>
        <vaadin-tab ?selected=${this.selectedTab === 0} @click=${() => this.selectedTab = 0}>General</vaadin-tab>
        <vaadin-tab ?selected=${this.selectedTab === 1} @click=${() => this.selectedTab = 1}>Shortcuts</vaadin-tab>
        <vaadin-tab ?selected=${this.selectedTab === 2} @click=${() => this.selectedTab = 2}>AI</vaadin-tab>
        <vaadin-tab ?selected=${this.selectedTab === 3} @click=${() => this.selectedTab = 3}>
          <span>Notifications</span>
        </vaadin-tab>
      </vaadin-tabs>
      ${this.selectedTab === 0 ? this.renderGeneralTab() : null}
      ${this.selectedTab === 1 ? this.renderShortcutsTab() : null} ${this.selectedTab === 2 ? this.renderAiTab() : null}
      ${this.selectedTab === 3 ? this.renderNotificationTab() : null}
    `;
		}
		renderGeneralTab() {
			let e = T.getSelectedSize(), t = T.getSelectedTheme(), n = T.getToolbarExpandMode(), r = T.getBadgePositionMode();
			return c`
      <div class="border-dashed flex flex-col flex-grow divide-y pb-4 pt-0.5 px-4" part="general-tab-container">
        <div class="flex gap-2 items-start justify-between py-2">
          <label class="py-1.5" id="size">Size</label>
          <vaadin-select
            accessible-name-ref="size"
            class="flex-shrink-0"
            theme="auto-width no-border"
            .items="${this.sizeItems}"
            .value="${e}"
            @change="${(t) => {
				let n = t.target.value;
				this.trackPanelEvent("use-settings", {
					source: "size",
					selected: n,
					previous: e
				}), T.setSelectedSize(n);
			}}"></vaadin-select>
        </div>
        <div class="flex gap-2 items-start justify-between py-2">
          <label class="py-1.5" id="theme">Theme</label>
          <vaadin-select
            accessible-name-ref="theme"
            class="flex-shrink-0"
            theme="auto-width no-border"
            .items="${this.themeItems}"
            .value="${t}"
            @change="${(e) => {
				let n = e.target.value;
				this.trackPanelEvent("use-settings", {
					source: "theme",
					selected: n,
					previous: t
				}), T.setSelectedTheme(n);
			}}"></vaadin-select>
        </div>
        <div class="flex gap-2 items-start justify-between py-2">
          <div class="flex flex-col py-1.5">
            <label id="toolbar-button-expand-mode">Toolbar behavior</label>
            <span class="text-secondary text-xs">How it appears & expands</span>
          </div>
          <vaadin-select
            accessible-name-ref="toolbar-expand-mode"
            class="flex-shrink-0"
            theme="auto-width no-border"
            .value="${n}"
            ${F(() => c`
                <vaadin-list-box class="max-w-xs">
                  ${this.toolbarExpandModeItems.map((e) => c`
                      <vaadin-item class="items-start" label="${e.label}" value="${e.value}">
                        <span class="flex flex-col gap-0.5">
                          <span>${e.label}</span>
                          <span class="text-secondary text-xs">${e.description}</span>
                        </span>
                      </vaadin-item>
                    `)}
                </vaadin-list-box>
              `)}
            @change="${(e) => {
				let t = e.target.value;
				this.trackPanelEvent("use-settings", {
					source: "toolbarExpandMode",
					selected: t,
					previous: T.getToolbarExpandMode()
				}), T.setToolbarExpandMode(t);
			}}"></vaadin-select>
        </div>
        <div class="flex gap-2 items-start justify-between py-2">
          <div class="flex flex-col py-1.5">
            <label id="toolbar-button-expand-mode">Badge positioning</label>
            <span class="text-secondary text-xs">How it is placed</span>
          </div>
          <vaadin-select
            accessible-name-ref="badge-position-mode"
            class="flex-shrink-0"
            theme="auto-width no-border"
            .value="${r}"
            ${F(() => c`
                <vaadin-list-box class="max-w-xs">
                  ${this.badgePositionItems.map((e) => c`
                      <vaadin-item class="items-start" label="${e.label}" value="${e.value}">
                        <span class="flex flex-col gap-0.5">
                          <span>${e.label}</span>
                          <span class="text-secondary text-xs">${e.description}</span>
                        </span>
                      </vaadin-item>
                    `)}
                </vaadin-list-box>
              `)}
            @change="${(e) => {
				let t = e.target.value;
				this.trackPanelEvent("use-settings", {
					source: "badgePosition",
					selected: t,
					previous: T.getBadgePositionMode()
				}), T.setBadgePositionMode(t);
			}}"></vaadin-select>
        </div>
        <div class="flex gap-2 justify-between mb-4 py-3.5">
          <div class="flex flex-col">
            <label id="error-reports-label">Send error reports</label>
            <span id="error-reports-desc" class="text-secondary text-xs">Helps us improve the user experience</span>
          </div>
          <button
            aria-checked="${this.sendErrorReportsAllowed}"
            aria-labelledby="error-reports-label"
            aria-describedby="error-reports-desc"
            class="my-px"
            role="switch"
            type="button"
            @click=${() => this.toggleSendErrorReports()}>
            <span></span>
          </button>
        </div>
        <vaadin-button
          data-test-id="hide-copilot-btn"
          @click="${this.handleHideCopilotButtonClick}"
          class="justify-start mt-auto self-start">
          <vaadin-icon slot="prefix" .svg="${s.close}"></vaadin-icon>
          Hide Copilot until server restart
        </vaadin-button>
      </div>

      <vaadin-confirm-dialog
        id="hideCopilotDialog"
        header="Temporarily Hide Copilot"
        .confirmText=${this.hideCopilotRequestOngoing ? "Hiding…" : "Continue"}
        cancel-text="Cancel"
        cancel-button-visible
        confirm-theme="primary"
        .confirmDisabled=${this.hideCopilotRequestOngoing}
        .cancelDisabled=${this.hideCopilotRequestOngoing}
        .noCloseOnEsc=${this.hideCopilotRequestOngoing}
        .opened="${this.hideCopilotDialogVisible}"
        .noCloseOnOutsideClick=${this.hideCopilotRequestOngoing}
        @cancel=${() => {
				this.hideCopilotDialogVisible = !1;
			}}
        @confirm=${this.onDisableConfirm}>
        This will hide the Copilot until the server restarts. The page will reload to apply the change. Do you want to
        continue?
        ${this.hideCopilotRequestOngoing ? c`
              <div style="display:flex; align-items:center; gap:var(--lumo-space-s); margin-top:var(--lumo-space-m);">
                <vaadin-progress-indicator indeterminate></vaadin-progress-indicator>
                <span>Hiding…</span>
              </div>
            ` : null}
      </vaadin-confirm-dialog>
    `;
		}
		renderShortcutsTab() {
			let e = R.hasFlowComponents();
			return c`<div class="flex flex-col gap-4 pb-2 pt-4 px-4 ">
      <div class="flex justify-between">
        <div class="flex flex-col">
          <label id="enable-shortcuts-label">Enable keyboard shortcut</label>
          <span id="enable-shortcuts-desc" class="text-secondary text-xs"
            >Switch anytime to Play mode with ${this.renderKbd(D.toggleCopilot)}</span
          >
        </div>
        <button
          aria-checked="${this.activationShortcutEnabled}"
          aria-labelledby="enable-shortcuts-label"
          aria-describedby="enable-shortcuts-desc"
          class="my-px"
          role="switch"
          type="button"
          @click=${() => this.toggleActivationShortcut()}>
          <span></span>
        </button>
      </div>
      <div class="flex flex-col gap-1">
        <h3 class="font-semibold my-0 text-sm">Global</h3>
        <ul class="border-dashed divide-y flex flex-col list-none m-0 p-0">
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.vaadin}"></vaadin-icon>
            <span>Switch Play Mode / Last Mode</span>
            ${this.renderKbd(D.toggleCopilot)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.undo}"></vaadin-icon>
            <span>Undo</span>
            ${this.renderKbd(D.undo)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.redo}"></vaadin-icon>
            <span>Redo</span>
            ${this.renderKbd(D.redo)}
          </li>
        </ul>
      </div>
      <div class="flex flex-col gap-1">
        <h3 class="font-semibold my-0 text-sm">Component Selection</h3>
        <ul class="border-dashed divide-y flex flex-col list-none m-0 p-0">
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.sparkles}"></vaadin-icon>
            <span>Open AI prompt</span>
            ${this.renderKbd(D.openAiPopover)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.code}"></vaadin-icon>
            <span>Go to source</span>
            ${this.renderKbd(D.goToSource)}
          </li>
          ${e ? c`<li class="flex gap-2 py-2">
                <vaadin-icon .svg="${s.code}"></vaadin-icon>
                <span>Go to attach source</span>
                ${this.renderKbd(D.goToAttachSource)}
              </li>` : a}
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.contentCopy}"></vaadin-icon>
            <span>Copy</span>
            ${this.renderKbd(D.copy)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.contentPaste}"></vaadin-icon>
            <span>Paste</span>
            ${this.renderKbd(D.paste)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.fileCopy}"></vaadin-icon>
            <span>Duplicate</span>
            ${this.renderKbd(D.duplicate)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.turnLeft}"></vaadin-icon>
            <span>Select parent</span>
            ${this.renderKbd(D.selectParent)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.north}"></vaadin-icon>
            <span>Select previous sibling</span>
            ${this.renderKbd(D.selectPreviousSibling)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.south}"></vaadin-icon>
            <span>Select first child / next sibling</span>
            ${this.renderKbd(D.selectNextSibling)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.delete}"></vaadin-icon>
            <span>Delete</span>
            ${this.renderKbd(D.delete)}
          </li>
          <li class="flex gap-2 py-2">
            <vaadin-icon .svg="${s.dashboardCustomize}"></vaadin-icon>
            <span>Add component</span>
            <kbd class="font-sans ms-auto">A – Z</kbd>
          </li>
        </ul>
      </div>
    </div>`;
		}
		renderAiTab() {
			let e = v.userInfo?.copilotProjectCannotLeaveLocalhost ?? !1, t = e ? "no" : this.aiUsage, n = v.userInfo?.copilotProjectCannotLeaveEU ? "EU_ONLY" : "ANY";
			return c`<div class="border-dashed flex flex-col divide-y px-4 py-0.5">
      <div class="flex gap-2 items-start justify-between py-2">
        <div class="flex flex-col py-1.5">
          <label id="ai-usage">AI usage</label>
          <span class="text-secondary text-xs">All AI features are clearly labelled </span>
          ${e ? c`<span class="text-secondary text-xs"
                >Restricted for your account. Ask your Vaadin account manager to change it.</span
              >` : a}
        </div>
        <vaadin-select
          accessible-name-ref="ai-usage"
          class="flex-shrink-0"
          theme="auto-width no-border"
          .items="${this.aiUsageItems}"
          .value="${t}"
          ?disabled="${e}"
          @value-changed="${(n) => {
				e || (n.detail.value !== t && this.trackPanelEvent("use-settings", {
					source: "aiUsage",
					selected: n.detail.value,
					previous: t
				}), this.aiUsage = n.detail.value, T.setAIUsageAllowed(n.detail.value));
			}}"></vaadin-select>
      </div>
      <div class="flex gap-2 items-start justify-between py-2">
        <div class="flex flex-col py-1.5">
          <label id="ai-provider">AI provider</label>
          <span class="text-secondary text-xs">Restricted at account level, contact Vaadin to modify it.</span>
        </div>
        <vaadin-select
          accessible-name-ref="ai-provider"
          class="flex-shrink-0"
          theme="auto-width no-border"
          .items="${this.aiProviderItems}"
          .value="${n}"
          disabled></vaadin-select>
      </div>
    </div>`;
		}
		renderNotificationTab() {
			return c`
      <div class="border-dashed flex flex-col divide-y px-4 py-0.5">
        ${V.map((e) => {
				let t = !T.isNotificationDismissed(e.dismissId);
				return c`
            <div
              class="flex gap-2 items-start justify-between py-3.5"
              data-test-id="notification-setting-${e.dismissId}">
              <div class="flex flex-col">
                <label id="notification-setting-${e.dismissId}-label">${e.title}</label>
                ${e.description ? c`<span class="text-secondary text-xs" id="notification-setting-${e.dismissId}-desc"
                      >${e.description}</span
                    >` : a}
              </div>
              <copilot-toggle-button
                accessible-name-ref="notification-setting-${e.dismissId}-label"
                accessible-desc-ref="${e.description ? `notification-setting-${e.dismissId}-desc` : ""}"
                ?checked=${t}
                @on-change=${() => this.toggleNotificationSetting(e.dismissId, t)}>
              </copilot-toggle-button>
            </div>
          `;
			})}
      </div>
    `;
		}
		toggleNotificationSetting(e, t) {
			if (!t) {
				T.removeDismissedNotification(e);
				return;
			}
			T.addDismissedNotification(e);
		}
		handleHideCopilotButtonClick() {
			this.hideCopilotDialogVisible = !0;
		}
		onDisableConfirm() {
			this.hideCopilotRequestOngoing = !0, t(`${d}hide-copilot`, {}, (e) => {
				x(e.data, {}) || (this.hideCopilotRequestOngoing = !1, window.location.reload());
			});
		}
	}, f([m()], U.prototype, "selectedTab", void 0), f([m()], U.prototype, "activationShortcutEnabled", void 0), f([m()], U.prototype, "aiUsage", void 0), f([m()], U.prototype, "sendErrorReportsAllowed", void 0), f([m()], U.prototype, "hideCopilotRequestOngoing", void 0), f([m()], U.prototype, "hideCopilotDialogVisible", void 0), U = f([p("copilot-settings-panel")], U), W = {
		header: "Settings",
		tag: l.SETTINGS
	}, window.Vaadin.copilot.plugins.push({ init(e) {
		e.addPanel(W);
	} });
}))();
export { U as CopilotSettingsPanel, W as panelConfig };
