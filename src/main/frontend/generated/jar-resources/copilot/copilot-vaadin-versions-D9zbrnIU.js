import { n as e } from "./chunk-DiqZc92J.js";
import { St as t, X as n, Y as r, _ as i, _t as a, b as o, dt as s, ft as c, n as l, o as u, r as d, t as f, u as p } from "./icons-Bzfd0PEL.js";
import { l as m, o as h } from "./consts-BXxpRU5f.js";
import { a as g, d as _, i as v, l as y, o as b, r as x, s as S } from "./section-panel-ui-state-COWn2ulI.js";
import { n as C, r as w } from "./copilot-ui-state-Dc6l_5DA.js";
import { n as T, t as E } from "./copilot-message-box-8CA3gK_h.js";
import { a as D, c as O, i as k, l as A, o as j, u as M } from "./copilot-error-handler-q8B4DBC6.js";
import { n as N, t as P } from "./copilot-stored-machine-state-D6qB_Peh.js";
import { n as F, r as I } from "./copilot-notification-DGCJSVaA.js";
import { n as L, t as R } from "./base-panel-3DcY1Auw.js";
//#region frontend/copilot/plugins/copilot-vaadin-versions/vaadin-version-request.ts
function z() {
	C.setVaadinVersionState({ loading: !0 }), n(`${h}get-new-vaadin-versions`, { includePreReleases: N.getNewVersionPreReleasesVisible() }, (e) => {
		let t = e.data;
		if (t.error) {
			C.setVaadinVersionState({
				loading: !1,
				errorMessage: t.error.message,
				hasError: !0,
				versions: []
			});
			return;
		}
		if (!t.newVersions) return;
		let n = JSON.parse(t.newVersions);
		C.setVaadinVersionState({
			versions: n,
			loading: !1,
			hasError: !1
		});
	});
}
var B = e((() => {
	r(), m(), P(), w(), a(), t(() => N.getNewVersionPreReleasesVisible(), () => {
		z();
	}, { fireImmediately: !0 });
})), V, H, U;
//#endregion
e((() => {
	S(), c(), d(), L(), w(), v(), l(), P(), r(), m(), F(), E(), B(), j(), O(), i(), b(), V = class extends R {
		constructor(...e) {
			super(...e), this.renderIcon = (e) => e.name === "Flow" ? f.flow : e.name === "Hilla" ? f.hilla : e.name === "Web Components" || e.name === "Flow Components" ? f.uiComponents : e.name === "TestBench" ? f.checklist : e.name.indexOf("MPR") === -1 ? e.name.indexOf("AppSec") === -1 ? e.name.indexOf("Collaboration") === -1 ? e.name === "Copilot" || e.name === "CoPilot" ? f.copilot : e.name.indexOf("Kubernetes") === -1 ? e.name.indexOf("Observability") === -1 ? e.name.indexOf("SSO") === -1 ? e.name.indexOf("Modernization") === -1 ? f.code : f.rocketLaunch : f.login : f.visibility : f.kubernetes : f.group : f.verifiedUser : f.playCircle;
		}
		render() {
			return p`
      <ul class="list-none m-0 pb-2.5 px-2">
        ${this.renderContent()}
      </ul>
    `;
		}
		renderContent() {
			return C.newVaadinVersionState === void 0 || C.newVaadinVersionState.loading ? T("loading", "Versions are loading...") : C.newVaadinVersionState.hasError ? T("error", C.newVaadinVersionState.errorMessage ?? "Unable to display new versions") : !C.newVaadinVersionState.versions || C.newVaadinVersionState.versions.length === 0 ? T("success", "Vaadin version is up to date") : p`
      ${C.newVaadinVersionState.versions.map((e, t) => this.renderNewVersionItem(e, t === 0))}
    `;
		}
		renderNewVersionItem(e, t) {
			return p`
      <li>
        <vaadin-details ?opened="${t}">
          <vaadin-details-summary class="relative" slot="summary">
            <div class="flex gap-2 items-center">
              ${e.version}
              ${t ? p`<span
                    class="bg-blue-3 dark:bg-blue-7 font-normal inline-flex px-1.5 py-px rounded-full text-xs text-blue-11 dark:text-blue-12"
                    >Latest</span
                  >` : u}
              <vaadin-button
                aria-label="Update"
                class="absolute end-0 top-0"
                theme="icon tertiary"
                ?disabled="${this.updateClickedVersion !== void 0}"
                @click="${(t) => {
				t.stopPropagation(), this.sendUpdateRequest(e.version, e.preRelease);
			}}">
                <vaadin-icon
                  class="${this.updateClickedVersion === e.version ? "animate-spin" : ""}"
                  .svg="${this.updateClickedVersion === e.version ? f.progressActivity : f.upgrade}"></vaadin-icon>
                <vaadin-tooltip slot="tooltip" text="Update"></vaadin-tooltip>
              </vaadin-button>
            </div>
          </vaadin-details-summary>
          <ul class="border-dashed divide-y list-none m-0 pe-2 ps-8">
            ${e.changelogs.map((e) => p`
                <li class="flex gap-2 py-2">
                  <vaadin-icon .svg="${this.renderIcon(e)}"></vaadin-icon>
                  ${e.name}
                  <a href="${e.url}" target="_blank">${e.version}</a>
                </li>
              `)}
          </ul>
        </vaadin-details>
      </li>
    `;
		}
		sendUpdateRequest(e, t) {
			o("use-vaadin-versions", {
				source: "updateVersion",
				preRelease: t
			}), this.updateClickedVersion = e;
			let r = {
				newVersion: e,
				preRelease: t
			};
			n(`${h}update-vaadin-version`, r, (e) => {
				this.updateClickedVersion = void 0;
				let t = !D(e.data, r);
				if (t) {
					let e = "Please restart the server";
					M() && (e = "Server will be restarted by the IDE plugin", A()), I({
						message: "Version updated",
						type: s.INFORMATION,
						details: e
					});
				}
				return t;
			}).catch((e) => {
				k("Error updating version", e);
			});
		}
	}, g([y()], V.prototype, "updateClickedVersion", void 0), V = g([_("copilot-vaadin-versions")], V), H = class extends x {
		createRenderRoot() {
			return this;
		}
		render() {
			let e = N.getNewVersionPreReleasesVisible();
			return p`<vaadin-button
      aria-pressed="${e}"
      theme="tertiary"
      @click="${(e) => {
				e.stopPropagation(), o("use-vaadin-versions", { source: "togglePrereleases" }), N.setNewVersionPreReleasesVisible(!N.getNewVersionPreReleasesVisible());
			}}">
      <vaadin-icon slot="prefix" .svg="${e ? f.visibility : f.visibilityOff}"></vaadin-icon>
      Prereleases
    </vaadin-button>`;
		}
	}, H = g([_("copilot-vaadin-versions-actions")], H), U = {
		header: "Vaadin Versions",
		tag: "copilot-vaadin-versions",
		actionsTag: "copilot-vaadin-versions-actions"
	}, window.Vaadin.copilot.plugins.push({ init(e) {
		e.addPanel(U);
	} });
}))();
export { V as CopilotVaadinVersions, H as CopilotVersionCheckerActions, U as versionCheckerPanel };
