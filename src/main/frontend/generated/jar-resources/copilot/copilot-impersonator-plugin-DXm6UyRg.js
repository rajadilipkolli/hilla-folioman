import { n as e } from "./chunk-DiqZc92J.js";
import { X as t, Y as n, ft as r, n as i, r as a, t as o, u as s, ut as c } from "./icons-Bzfd0PEL.js";
import { a as l, d as u, l as d, n as f, o as p, s as m, t as h } from "./section-panel-ui-state-COWn2ulI.js";
import { n as g, t as _ } from "./copilot-stored-machine-state-D6qB_Peh.js";
import { n as v, t as y } from "./early-project-state-LGwavSyI.js";
import { n as b, t as x } from "./base-panel-3DcY1Auw.js";
//#region frontend/copilot/application-user-switcher.ts
function S(e) {
	return t("copilot-switch-user", { username: e }, (e) => e.data.error ? {
		success: !1,
		errorMessage: e.data.error.message
	} : { success: !0 });
}
var C = e((() => {
	n();
})), w, T;
//#endregion
e((() => {
	a(), m(), r(), b(), i(), C(), _(), v(), h(), p(), w = class extends x {
		constructor(...e) {
			super(...e), this.username = "", this.errorMessage = "", this.isLoading = !1, this.handleKeyDown = async (e) => {
				e.key === "Enter" && this.username && !this.isLoading && await this.handleSwitchUser();
			}, this.handleSwitchUser = async () => {
				if (!(!this.username || this.isLoading)) {
					this.isLoading = !0, this.errorMessage = "";
					try {
						let e = await S(this.username);
						e.success ? (this.trackPanelEvent("use-impersonator", { source: "switchUser" }), g.addRecentSwitchedUsername(this.username), globalThis.location.reload()) : (this.errorMessage = e.errorMessage, this.isLoading = !1);
					} catch {
						this.errorMessage = "An unexpected error occurred", this.isLoading = !1;
					}
				}
			}, this.switchToRecentUser = async (e) => {
				this.username = e, await this.handleSwitchUser();
			}, this.removeRecentUser = (e, t) => {
				t.stopPropagation(), g.removeRecentSwitchedUsername(e), this.requestUpdate();
			};
		}
		connectedCallback() {
			super.connectedCallback(), this.classList.add("contents"), this.reaction(() => g.getRecentSwitchedUsernames(), () => {
				this.requestUpdate();
			});
		}
		render() {
			if (!y.springSecurityEnabled) return s`
        <div class="flex flex-col items-center pb-4 px-4">
          <vaadin-icon class="icon-lg mb-2" .svg="${o.accountCircle}"></vaadin-icon>
          <h3 class="mb-0.5 mt-0 text-semibold text-sm">Spring Security Disabled</h3>
          <p class="m-0 text-balance text-center text-secondary text-xs">
            User impersonation requires Spring Security to be configured in your application
          </p>
        </div>
      `;
			let e = g.getRecentSwitchedUsernames();
			return s`
      <div class="flex flex-col gap-4 pb-4 px-4">
        <div class="flex gap-4 items-baseline">
          <vaadin-text-field
            class="flex-1"
            label="Username"
            .value="${this.username}"
            .errorMessage="${this.errorMessage}"
            .invalid="${this.errorMessage !== ""}"
            ?disabled="${this.isLoading}"
            @value-changed="${(e) => {
				this.username = e.detail.value, this.errorMessage = "";
			}}"
            @keydown="${this.handleKeyDown}">
            <vaadin-icon slot="prefix" .svg="${o.accountCircle}"></vaadin-icon>
          </vaadin-text-field>
          <vaadin-button
            theme="primary"
            ?disabled="${!this.username || this.isLoading}"
            @click="${this.handleSwitchUser}">
            <vaadin-icon slot="prefix" .svg="${o.swapHoriz}"></vaadin-icon>
            ${this.isLoading ? "Switching..." : "Switch User"}
          </vaadin-button>
        </div>

        ${e.length > 0 ? s`
              <div class="flex flex-col gap-2 mt-1">
                <h3 class="m-0 text-semibold text-sm">Recent Usernames</h3>
                <ul
                  class="bg-gray-2 dark:bg-gray-6 border border-gray-3 dark:border-gray-7 divide-y list-none m-0 p-0 rounded-md">
                  ${e.map((e) => s`
                      <li class="flex gap-1 items-center pe-1 ps-3 py-1">
                        <span class="flex-1">${e}</span>
                        <vaadin-button theme="icon tertiary" @click="${() => this.switchToRecentUser(e)}">
                          <vaadin-icon .svg="${o.swapHoriz}"></vaadin-icon>
                          <vaadin-tooltip slot="tooltip" text="Switch to ${e}"></vaadin-tooltip>
                        </vaadin-button>
                        <vaadin-button
                          aria-label="Remove ${e}"
                          class="text-ruby-11"
                          theme="icon tertiary"
                          @click="${(t) => this.removeRecentUser(e, t)}">
                          <vaadin-icon .svg="${o.delete}"></vaadin-icon>
                          <vaadin-tooltip slot="tooltip" text="Remove ${e}"></vaadin-tooltip>
                        </vaadin-button>
                      </li>
                    `)}
                </ul>
              </div>
            ` : ""}
      </div>
    `;
		}
	}, l([d()], w.prototype, "username", void 0), l([d()], w.prototype, "errorMessage", void 0), l([d()], w.prototype, "isLoading", void 0), w = l([u("copilot-impersonator")], w), T = {
		header: "Impersonate User",
		tag: c.IMPERSONATOR,
		individual: !0,
		toolbarOptions: {
			allowedModesWithOrder: { common: 0 },
			iconKey: "accountCircle"
		}
	}, globalThis.Vaadin.copilot.plugins.push({ init(e) {
		e.addPanel(T);
	} }), f.addPanel(T);
}))();
export { w as CopilotImpersonatorPanel };
