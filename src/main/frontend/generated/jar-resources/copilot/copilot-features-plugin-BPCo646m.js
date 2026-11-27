import { n as e } from "./chunk-DiqZc92J.js";
import { dt as t, ft as n, n as r, o as i, r as a, t as o, u as s } from "./icons-Bzfd0PEL.js";
import { a as c, d as l, i as u, l as d, o as f, r as p, s as m } from "./section-panel-ui-state-COWn2ulI.js";
import { n as h, r as g } from "./copilot-ui-state-Dc6l_5DA.js";
import { c as _, l as v, o as y, r as b, u as x } from "./copilot-error-handler-q8B4DBC6.js";
import { n as S, t as C } from "./copilot-stored-machine-state-D6qB_Peh.js";
import { n as w, r as T } from "./copilot-notification-DGCJSVaA.js";
import { n as E, t as D } from "./base-panel-3DcY1Auw.js";
import { r as O, t as k } from "./copilot-experimental-features-Q6gs4SqA.js";
//#region frontend/copilot/plugins/copilot-features/copilot-features-plugin.ts
var A, j, M, N, P;
//#endregion
e((() => {
	y(), a(), m(), O(), w(), n(), C(), g(), _(), r(), u(), E(), f(), A = window.Vaadin.devTools, j = "copilotExperimentalFeatures", M = class extends D {
		constructor(...e) {
			super(...e), this.toggledFeaturesThatAreRequiresServerRestart = [];
		}
		connectedCallback() {
			super.connectedCallback(), this.classList.add("contents");
		}
		render() {
			let e = h.featureFlags.find((e) => e.id === j);
			return s`
      <vaadin-tabsheet class="border-0" theme="no-padding">
        <vaadin-tabs slot="tabs">
          <vaadin-tab id="application-features-tab">Application</vaadin-tab>
          <vaadin-tab id="copilot-features-tab">Copilot</vaadin-tab>
        </vaadin-tabs>
        <div tab="application-features-tab" class="px-4 py-0.5">
          <div class="border-dashed flex flex-col divide-y">
            ${h.featureFlags.filter((e) => e.id !== j).sort((e, t) => e.title.localeCompare(t.title)).map((e) => this.renderFeatureFlag(e))}
          </div>
        </div>
        <div tab="copilot-features-tab" class="px-4 py-0.5">
          <div class="border-dashed flex flex-col divide-y">
            ${e ? this.renderFeatureFlag(e) : i}
            ${this.renderCopilotExperimentalFeatures()}
          </div>
        </div>
      </vaadin-tabsheet>
    `;
		}
		renderFeatureFlag(e) {
			return this.renderFeatureRow({
				id: e.id,
				label: e.title,
				description: s`
        <a
          class="flex gap-0.5 text-xs"
          href="${e.moreInfoLink}"
          id="${e.id}-desc"
          target="_blank"
          rel="noopener noreferrer"
          >More info<vaadin-icon class="icon-sm" .svg="${o.arrowOutward}"></vaadin-icon
        ></a>
      `,
				checked: e.enabled,
				onChange: (t) => this.toggleFeatureFlag(t, e)
			});
		}
		renderCopilotExperimentalFeatures() {
			let e = h.userInfo?.copilotExperimentalFeatureFlag === !0;
			return k.filter((e) => e.available()).sort((e, t) => e.name.localeCompare(t.name)).map((t) => this.renderFeatureRow({
				id: t.id,
				label: t.name,
				description: s`
            <span class="text-xs text-secondary" id="${t.id}-desc"
              >${t.description}</span
            >
          `,
				checked: S.isExperimentalFeatureEnabled(t),
				disabled: !e,
				onChange: (e) => this.toggleExperimentalFeatureFlag(e, t)
			}));
		}
		renderFeatureRow(e) {
			return s`
      <div class="flex gap-2 justify-between py-3.5">
        <div class="flex flex-col ${e.disabled ? "opacity-50" : ""}">
          <label id="${e.id}-label">${e.label}</label>
          ${e.description}
        </div>
        <copilot-toggle-button
          accessible-name-ref="${e.id}-label"
          accessible-desc-ref="${e.id}-desc"
          ?checked=${e.checked}
          ?disabled=${e.disabled ?? !1}
          @on-change=${e.onChange}>
        </copilot-toggle-button>
      </div>
    `;
		}
		toggleFeatureFlag(e, n) {
			let r = e.target.checked;
			this.trackPanelEvent("use-feature", {
				source: "toggle",
				enabled: r,
				id: n.id
			}), A.frontendConnection ? (A.frontendConnection.send("setFeature", {
				featureId: n.id,
				enabled: r
			}), n.requiresServerRestart && h.toggleServerRequiringFeatureFlag(n), T({
				type: t.INFORMATION,
				message: `“${n.title}” ${r ? "enabled" : "disabled"}`,
				details: n.requiresServerRestart ? b() : void 0,
				dismissId: `feature${n.id}${r ? "Enabled" : "Disabled"}`
			}), n.id === j && h.userInfo && h.setUserInfo({
				...h.userInfo,
				copilotExperimentalFeatureFlag: r
			})) : A.log("error", `Unable to toggle feature ${n.title}: No server connection available`);
		}
		toggleExperimentalFeatureFlag(e, t) {
			let n = e.target.checked;
			this.trackPanelEvent("use-experimental-feature", {
				source: "toggle",
				enabled: n,
				id: t.id
			});
			let r = S.isExperimentalFeatureEnabled(t);
			S.setExperimentalFeatureEnabled(t, n), t.requiresReload && n && !r && window.location.reload();
		}
	}, c([d()], M.prototype, "toggledFeaturesThatAreRequiresServerRestart", void 0), M = c([l("copilot-features-panel")], M), N = class extends p {
		constructor(...e) {
			super(...e), this.serverRestarting = !1;
		}
		createRenderRoot() {
			return this;
		}
		render() {
			if (h.serverRestartRequiringToggledFeatureFlags.length === 0 || !x()) return i;
			let e = this.serverRestarting ? "Restarting..." : "Click to restart server";
			return s`
      <vaadin-button
        aria-label="Restart server"
        ?disabled="${this.serverRestarting}"
        theme="icon tertiary"
        @click=${() => {
				this.serverRestarting = !0, v();
			}}>
        <vaadin-icon .svg="${o.refresh}"></vaadin-icon>
        <vaadin-tooltip slot="tooltip" text=${e}></vaadin-tooltip>
      </vaadin-button>
    `;
		}
	}, c([d()], N.prototype, "serverRestarting", void 0), N = c([l("copilot-features-actions")], N), P = {
		header: "Features",
		tag: "copilot-features-panel",
		helpUrl: "https://vaadin.com/docs/latest/flow/configuration/feature-flags",
		actionsTag: "copilot-features-actions",
		toolbarOptions: {
			allowedModesWithOrder: { common: 0 },
			iconKey: "listAlt"
		}
	}, window.Vaadin.copilot.plugins.push({ init(e) {
		e.addPanel(P);
	} });
}))();
export { N as CopilotFeaturesActions, M as CopilotFeaturesPanel };
