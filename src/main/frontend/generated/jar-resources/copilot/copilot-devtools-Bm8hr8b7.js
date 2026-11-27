import { n as e } from "./chunk-DiqZc92J.js";
import { A as t, P as n, _ as r, b as i, dt as a, ft as o, n as s, o as c, r as l, t as u, u as d, ut as f } from "./icons-Bzfd0PEL.js";
import { a as p, d as m, i as h, l as g, n as _, o as v, r as y, s as b, t as x } from "./section-panel-ui-state-COWn2ulI.js";
import { a as S, i as C, n as w, r as T } from "./copilot-ui-state-Dc6l_5DA.js";
import { n as E, t as D } from "./copilot-stored-machine-state-D6qB_Peh.js";
import { n as O, r as k } from "./copilot-notification-DGCJSVaA.js";
import { n as A, t as j } from "./early-project-state-LGwavSyI.js";
import { a as M, i as N, s as P, t as F } from "./copilot-development-setup-user-guide-utils-Ctr4ib5Q.js";
import { n as I, r as L } from "./copilot-experimental-features-Q6gs4SqA.js";
import { n as R, t as z } from "./copilot-all-components-state-bkFTxkle.js";
//#region frontend/copilot/copilot-devtools/copilot-devtools.ts
var B, V, H, U, W;
//#endregion
e((() => {
	b(), l(), h(), T(), s(), P(), o(), x(), r(), A(), D(), O(), n(), L(), S(), R(), v(), B = "bg-[linear-gradient(to_right,var(--amber-3),var(--amber-5),var(--amber-3),var(--amber-6))] dark:bg-[linear-gradient(to_right,var(--amber-5),var(--amber-7),var(--amber-5),var(--amber-8))]", V = "bg-[linear-gradient(to_right,var(--blue-3),var(--blue-5),var(--blue-3),var(--blue-6))] dark:bg-[linear-gradient(to_right,var(--blue-4),var(--blue-6),var(--blue-4),var(--blue-7))]", H = "bg-[linear-gradient(to_right,var(--ruby-3),var(--ruby-5),var(--ruby-3),var(--ruby-6))] dark:bg-[linear-gradient(to_right,var(--ruby-4),var(--ruby-6),var(--ruby-4),var(--ruby-7))]", U = "bg-[linear-gradient(to_right,var(--teal-3),var(--teal-5),var(--teal-3),var(--teal-6))] dark:bg-[linear-gradient(to_right,var(--teal-4),var(--teal-6),var(--teal-4),var(--teal-7))]", W = class extends y {
		constructor(...e) {
			super(...e), this._helpExpanded = !1;
		}
		createRenderRoot() {
			return this;
		}
		connectedCallback() {
			super.connectedCallback(), this.classList.add("flex", "flex-col");
		}
		render() {
			return d`
      <header class="flex items-center pe-2 ps-4 py-2">
        <h2 class="font-bold gap-1 me-auto my-0 text-xs uppercase">Vaadin Copilot</h2>
        <vaadin-button
          aria-label="Close"
          theme="icon tertiary"
          @click=${() => {
				this.closePopover();
			}}>
          <vaadin-icon .svg="${u.close}"></vaadin-icon>
          <vaadin-tooltip slot="tooltip" text="Close"></vaadin-tooltip>
        </vaadin-button>
      </header>
      <div class="flex flex-col gap-4 pb-4 px-4">
        ${this.renderCopilotServerWarning()} ${this.renderUserButton()} ${this.renderDevelopmentWorkflow()}
        ${this.renderWelcomeToVersion()}
        <div class="bg-gray-3 dark:bg-gray-6 flex flex-col rounded-md">
          <vaadin-button
            @click="${this.handleAppInfoClick}"
            class="border-0 h-auto justify-start py-2"
            theme="tertiary">
            <vaadin-icon slot="prefix" .svg="${u.info}"></vaadin-icon>
            App Info
          </vaadin-button>
          <vaadin-button @click="${this.handleAppLogClick}" class="border-0 h-auto justify-start py-2" theme="tertiary">
            <vaadin-icon slot="prefix" .svg="${u.terminal}"></vaadin-icon>
            App Log
          </vaadin-button>
          <vaadin-button
            @click="${this.handleFeaturesClick}"
            class="border-0 h-auto justify-start py-2"
            theme="tertiary">
            <vaadin-icon slot="prefix" .svg="${u.listAlt}"></vaadin-icon>
            Features
          </vaadin-button>
          ${j.springSecurityEnabled ? d`
                <vaadin-button
                  @click="${this.handleImpersonateAppUserClick}"
                  class="border-0 h-auto justify-start py-2"
                  theme="tertiary">
                  <vaadin-icon slot="prefix" .svg="${u.accountCircle}"></vaadin-icon>
                  Impersonate App User
                </vaadin-button>
              ` : c}
          ${this.renderAllComponentsButton()}
        </div>
        <div class="bg-gray-3 dark:bg-gray-6 flex flex-col rounded-md">
          <vaadin-button
            @click="${this.handleFeedbackClick}"
            class="border-0 h-auto justify-start py-2"
            theme="tertiary">
            <vaadin-icon slot="prefix" .svg="${u.feedback}"></vaadin-icon>
            Feedback
          </vaadin-button>
          <vaadin-button
            @click="${this.toggleHelpAndSupport}"
            class="border-0 h-auto justify-start py-2"
            theme="tertiary">
            <vaadin-icon slot="prefix" .svg="${u.help}"></vaadin-icon>
            Help & Support
            <vaadin-icon
              slot="suffix"
              .svg="${this._helpExpanded ? u.keyboardArrowUp : u.keyboardArrowDown}"></vaadin-icon>
          </vaadin-button>
          ${this._helpExpanded ? this.renderHelpLinks() : c}
          <vaadin-button
            @click="${this.handleSettingsClick}"
            class="border-0 h-auto justify-start py-2"
            theme="tertiary">
            <vaadin-icon slot="prefix" .svg="${u.settings}"></vaadin-icon>
            Settings
          </vaadin-button>
        </div>
      </div>
    `;
		}
		renderUserButton() {
			let e = w.userInfo?.validLicense, t = e ? B : V, n = e ? "text-amber-12 dark:text-amber-11" : "text-blue-12 dark:text-blue-11", r = this.getUserName() !== "Log in";
			return d`
      <vaadin-button
        id="user-button"
        @click=${this.handleUserLoginClick}
        class="animate-gradient ${t} border-0 h-auto justify-start py-2 text-start ${r ? "gap-3 px-3" : "items-start"}">
        ${r ? this.renderUserImage() : d`<vaadin-icon
              class="text-blue-12 dark:text-blue-11"
              slot="prefix"
              .svg="${u.login}"></vaadin-icon>`}
        <span class="flex flex-col">
          <span>${this.getUserName()}</span>
          <span class="${n} text-xs">${this.getLicenseType()}</span>
        </span>
      </vaadin-button>
    `;
		}
		renderCopilotServerWarning() {
			return w.userInfo?.copilotServerReached === !1 ? d`
      <vaadin-button
        @click=${this.showCopilotServerTroubleshooting}
        class="animate-gradient ${H} border-0 h-auto items-start justify-start py-2 text-start"
        data-test-id="copilot-server-unreachable">
        <vaadin-tooltip slot="tooltip" text="Click here for troubleshooting"></vaadin-tooltip>
        <vaadin-icon class="text-ruby-12 dark:text-ruby-11" slot="prefix" .svg="${u.warning}"></vaadin-icon>
        <span class="flex flex-col">
          <span>Copilot server is unreachable</span>
          <span class="text-ruby-12 dark:text-ruby-11 text-xs">Check your proxy or firewall settings.</span>
        </span>
      </vaadin-button>
    ` : c;
		}
		showCopilotServerTroubleshooting() {
			k({
				type: a.WARNING,
				message: "Copilot server is unreachable",
				details: t(d`
        <p class="m-0">Copilot could not connect to the Copilot server.</p>
        <p class="mb-0 mt-2">To troubleshoot:</p>
        <ol class="mb-0 mt-1 ps-4">
          <li>Verify that this machine can reach the Copilot server and complete its TLS handshake:</li>
          <li class="list-none mt-1">
            <code
              class="bg-gray-3 dark:bg-gray-6 box-border inline-block pe-8 ps-3 py-1.75 relative rounded-md text-xs w-full"
              ><copilot-copy></copilot-copy>curl -Iv https://copilot.vaadin.com</code
            >
          </li>
          <li>Check that your firewall or network policy allows access to <code>copilot.vaadin.com</code>.</li>
          <li>If you use a proxy, verify its settings and that it trusts the server's SSL certificate.</li>
        </ol>
      `),
				delay: 3e4
			});
		}
		renderWelcomeToVersion() {
			let e = w.projectVersionReleaseNoteInfo;
			return e === null || E.getMostRecentReleaseNoteDismissed() || !e.mostRecentVersion || !e.url ? c : d`
      <div class="flex relative">
      <vaadin-button
        id="release-note-btn"
        data-test-id="release-note-btn"
        class="border-0 h-auto items-start justify-start px-3 py-2 text-start w-full"
        @click="${(t) => {
				window.open(e.url, "_blank");
			}}">
        <vaadin-icon class="text-blue-11" slot="prefix" .svg="${u.info}"></vaadin-icon>
        <span class="flex flex-col">
          <span>Welcome to Vaadin ${e.vaadinVersion}</span>
          <span class="text-blue-11 text-xs">Click for release notes</span>
        </span>
      </vaadin-button>
      <vaadin-button
        class="absolute end-0 top-0"
        id="dismiss-release-note-item"
        theme="icon tertiary"
        @click="${(e) => {
				e.stopPropagation(), E.setMostRecentReleaseNoteDismissed(!0);
			}}"
        ><vaadin-icon .svg="${u.close}"></vaadin-icon
        <vaadin-tooltip slot="tooltip" text="Dismiss"></vaadin-tooltip>
      </vaadin-button>
      </div>
    `;
		}
		renderUserImage() {
			return w.userInfo?.portraitUrl ? d`<img
        alt="${this.getUserName()}"
        class="rounded-full size-8 object-cover"
        slot="prefix"
        src="https://vaadin.com${w.userInfo.portraitUrl}" />` : c;
		}
		renderDevelopmentWorkflow() {
			let e = N(), t = M(), n = this.getDevelopmentWorkflowConfig(e, t), r = n?.bgClass ?? "", i = n?.colorClass ?? "", a = this.resolveIcon(n), o = n?.rotateIcon ? `rotate-180 ${i}` : i, s = this.resolveTitle(n), c = n?.displayMessage ?? "";
			return d`
      <vaadin-button
        data-test-id="development-workflow-btn"
        @click="${this.handleDevelopmentWorkflowClick}"
        class="animation-delay-4000 animate-gradient ${r} border-0 h-auto items-start justify-start py-2 text-start">
        <vaadin-icon class="${o}" slot="prefix" .svg="${a}"></vaadin-icon>
        <span class="flex flex-col">
          <span>${s}</span>
          <span class="text-xs ${i}">${c}</span>
        </span>
      </vaadin-button>
    `;
		}
		renderAllComponentsButton() {
			return w.userInfo?.copilotExperimentalFeatureFlag && E.isExperimentalFeatureEnabled(I) ? d`
        <vaadin-button
          ?disabled=${this.isAllComponentsOpening() || z.error}
          @click="${this.handleAllComponentsClick}"
          class="border-0 h-auto justify-start py-2"
          theme="tertiary">
          <vaadin-icon
            class="${this.getAllComponentsIconClass()}"
            slot="prefix"
            .svg="${this.getAllComponentsIcon()}"></vaadin-icon>
          ${this.getAllComponentsLabel()}
          <vaadin-icon slot="suffix" .svg="${u.experiment}"></vaadin-icon>
          ${z.allComponentsViewExists && !z.error ? c : d`<vaadin-tooltip slot="tooltip" text="${this.getAllComponentsTooltip()}"></vaadin-tooltip>`}
        </vaadin-button>
      ` : c;
		}
		getDevelopmentWorkflowConfig(e, t) {
			let n = {
				bgClass: U,
				colorClass: "text-teal-11"
			};
			if (e === "warning" && t === "warning") return {
				...n,
				icon: u.wbIncandescent,
				rotateIcon: !0,
				title: "IDE plugin & Hotswap recommended",
				combinedTitle: !0,
				displayMessage: "Enable both for optimal development workflow"
			};
			if (e === "warning") return {
				...n,
				icon: u.wbIncandescent,
				rotateIcon: !0,
				title: "Hotswap recommended",
				displayMessage: "Applies changes without restarting"
			};
			if (t === "warning") return {
				...n,
				icon: u.code,
				getIcon: !0,
				title: "IDE plugin recommended",
				getTitle: !0,
				displayMessage: "Simplifies Hotswap setup & config"
			};
			if (e === "error") return {
				bgClass: H,
				colorClass: "text-ruby-11",
				icon: u.error,
				title: "Hotswap partially enabled",
				displayMessage: "View details"
			};
		}
		resolveIcon(e) {
			return e ? e.getIcon ? this.getIdeIcon() : e.icon : u.bolt;
		}
		resolveTitle(e) {
			return e ? e.combinedTitle ? this.getCombinedTitle() : e.getTitle ? this.getIdePluginName() : e.title : "Development Workflow";
		}
		getUserName() {
			return [w.userInfo?.firstName, w.userInfo?.lastName].filter(Boolean).join(" ") || "Log in";
		}
		getLicenseType() {
			return w.userInfo?.validLicense ? "" : "Unlock all Copilot features, including AI";
		}
		getIdeIcon() {
			switch (w.idePluginState?.ide) {
				case "intellij": return u.intelliJ;
				case "vscode": return u.vsCode;
				case "eclipse": return u.eclipse;
				default: return u.code;
			}
		}
		getIdePluginName() {
			switch (w.idePluginState?.ide) {
				case "intellij": return "Vaadin plugin for IntelliJ";
				case "vscode": return "Vaadin extension for VS Code";
				case "eclipse": return "Vaadin plugin for Eclipse";
				default: return "IDE plugin";
			}
		}
		getCombinedTitle() {
			switch (w.idePluginState?.ide) {
				case "intellij": return "IntelliJ plugin & Hotswap recommended";
				case "vscode": return "VS Code extension & Hotswap recommended";
				case "eclipse": return "Eclipse plugin & Hotswap recommended";
				default: return "IDE plugin & Hotswap recommended";
			}
		}
		closePopover() {
			let e = this.closest("vaadin-popover");
			e && (e.opened = !1);
		}
		handleUserLoginClick() {
			if (w.userInfo?.validLicense) {
				window.open("https://vaadin.com/myaccount", "_blank", "noopener");
				return;
			}
			w.setLoginCheckActive(!0);
		}
		handleDevelopmentWorkflowClick() {
			i("use-development-setup"), _.openPanel(F), this.closePopover();
		}
		handleAppInfoClick() {
			_.openPanel(f.INFO), this.closePopover();
		}
		handleAppLogClick() {
			_.openPanel(f.LOG), this.closePopover();
		}
		handleAllComponentsClick() {
			C.emit("open-all-components", {});
		}
		isAllComponentsOpening() {
			return [
				"creating",
				"waiting-for-route",
				"navigating"
			].includes(z.status);
		}
		getAllComponentsIconClass() {
			return z.error ? "text-ruby-11" : this.isAllComponentsOpening() ? "animate-spin" : "";
		}
		getAllComponentsIcon() {
			return z.error ? u.error : this.isAllComponentsOpening() ? u.progressActivity : u.terminal;
		}
		getAllComponentsTooltip() {
			if (z.error) return z.errorMessage;
			switch (z.status) {
				case "creating": return "Creating the component gallery…";
				case "waiting-for-route": return "Waiting for the generated gallery route…";
				case "navigating": return "Opening the component gallery…";
				case "restart-required": return "The gallery was created. Restart the server before opening it.";
				case "timed-out": return "The gallery route is not available yet. Restart the server and try again.";
				default: return "Generate and open a gallery of all components in this application.";
			}
		}
		getAllComponentsLabel() {
			if (z.allComponentsViewExists) return "Open Component Gallery";
			switch (z.status) {
				case "creating": return "Creating Component Gallery…";
				case "waiting-for-route": return "Waiting for Component Gallery…";
				case "navigating": return "Opening Component Gallery…";
				case "restart-required": return "Restart to Open Component Gallery";
				case "timed-out": return "Component Gallery Unavailable";
				default: return "Create Component Gallery";
			}
		}
		handleFeaturesClick() {
			_.openPanel(f.FEATURES), this.closePopover();
		}
		handleImpersonateAppUserClick() {
			_.openPanel(f.IMPERSONATOR), this.closePopover();
		}
		handleSettingsClick() {
			_.openPanel(f.SETTINGS), this.closePopover();
		}
		handleFeedbackClick() {
			_.openPanel(f.FEEDBACK), this.closePopover();
		}
		toggleHelpAndSupport() {
			this._helpExpanded = !this._helpExpanded;
		}
		renderHelpLinks() {
			return d`
      <div class="flex flex-col ps-4">
        ${[
				{
					label: "Forum",
					icon: "forum",
					url: "https://vaadin.com/forum"
				},
				{
					label: "Docs",
					icon: "article",
					url: "https://vaadin.com/docs/latest/tools/copilot"
				},
				{
					label: "GitHub Issues",
					icon: "github",
					url: "https://github.com/vaadin/copilot/issues"
				}
			].map(({ label: e, icon: t, url: n }) => d`
            <vaadin-button
              @click="${() => window.open(n, "_blank", "noopener")}"
              class="border-0 h-auto justify-start py-2"
              theme="tertiary">
              <vaadin-icon slot="prefix" .svg="${u[t]}"></vaadin-icon>
              ${e}
            </vaadin-button>
          `)}
      </div>
    `;
		}
	}, p([g()], W.prototype, "_helpExpanded", void 0), W = p([m("copilot-devtools")], W);
}))();
