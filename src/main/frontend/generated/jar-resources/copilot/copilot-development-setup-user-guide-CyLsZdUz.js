import { n as e } from "./chunk-DiqZc92J.js";
import { Ct as t, X as n, Y as r, _t as i, n as a, o, r as s, t as c, u as l } from "./icons-Bzfd0PEL.js";
import { a as u, c as d, i as f, l as p, o as m } from "./consts-BXxpRU5f.js";
import { a as h, d as g, i as _, l as v, n as y, o as b, r as x, s as S, t as C } from "./section-panel-ui-state-COWn2ulI.js";
import { a as w, i as T, n as E, r as D } from "./copilot-ui-state-Dc6l_5DA.js";
import { l as O, s as k } from "./copilot-unsaved-operation-coordinator-BszTzv5k.js";
import { i as A, o as j } from "./copilot-error-handler-q8B4DBC6.js";
import { n as M, t as N } from "./early-project-state-LGwavSyI.js";
import { i as P, o as F, s as I, t as L } from "./copilot-development-setup-user-guide-utils-Ctr4ib5Q.js";
import { n as R, t as z } from "./base-panel-3DcY1Auw.js";
//#region frontend/copilot/copilot-development-setup-user-guide.ts
function B(e, t) {
	if (!t) return !0;
	let [n, r, i] = t.split(".").map((e) => Number.parseInt(e)), [a, o, s] = e.split(".").map((e) => Number.parseInt(e));
	if (n < a) return !0;
	if (n === a) {
		if (r < o) return !0;
		if (r === o) return i < s;
	}
	return !1;
}
var V, H, U, W, G, K;
//#endregion
e((() => {
	a(), S(), C(), s(), i(), p(), D(), I(), w(), j(), r(), M(), O(), R(), _(), b(), H = "https://github.com/JetBrains/JetBrainsRuntime/releases", U = "Download complete", W = (V = class extends z {
		createRenderRoot() {
			return this;
		}
		constructor() {
			super(), this.javaPluginSectionOpened = !1, this.hotswapSectionOpened = !1, this.hotswapTab = "hotswapagent", this.downloadStatusMessages = [], this.downloadProgress = 0, this.onDownloadStatusUpdate = this.downloadStatusUpdate.bind(this), this.handleESC = (e) => {
				k().appInteractable || e.key === "Escape" && y.openPanel(K.tag);
			}, this.reaction(() => [N.jdkInfo, E.idePluginState], () => {
				E.idePluginState && (!E.idePluginState.ide || !E.idePluginState.active ? this.javaPluginSectionOpened = !0 : (!new Set(["vscode", "intellij"]).has(E.idePluginState.ide) || !E.idePluginState.active) && (this.javaPluginSectionOpened = !1)), N.jdkInfo && P() !== "success" && (this.hotswapSectionOpened = !0);
			}, { fireImmediately: !0 });
		}
		connectedCallback() {
			super.connectedCallback(), this.classList.add("contents"), T.on("set-up-vs-code-hotswap-status", this.onDownloadStatusUpdate);
		}
		disconnectedCallback() {
			super.disconnectedCallback(), T.off("set-up-vs-code-hotswap-status", this.onDownloadStatusUpdate);
		}
		render() {
			let e = {
				intellij: E.idePluginState?.ide === "intellij",
				vscode: E.idePluginState?.ide === "vscode",
				eclipse: E.idePluginState?.ide === "eclipse",
				idePluginInstalled: !!E.idePluginState?.active
			};
			return l`
      ${this.renderPluginSection(e)}
      <hr class="border-b border-e-0 border-s-0 border-t-0 mx-4 my-0" />
      ${this.renderHotswapSection(e)}
    `;
		}
		renderPluginSection(e) {
			let n = "";
			e.intellij ? n = "IntelliJ" : e.vscode ? n = "VS Code" : e.eclipse && (n = "Eclipse");
			let r, i;
			e.vscode || e.intellij ? e.idePluginInstalled ? (r = `Plugin for ${n} installed`, i = this.renderPluginInstalledContent()) : (r = `Plugin for ${n} not installed`, i = this.renderPluginIsNotInstalledContent(e)) : e.eclipse ? (r = e.idePluginInstalled ? "Eclipse plugin installed" : "Eclipse plugin not installed", i = e.idePluginInstalled ? this.renderPluginInstalledContent() : this.renderEclipsePluginContent()) : (r = "No IDE found", i = this.renderNoIdePluginContent());
			let a = e.idePluginInstalled ? c.checkCircle : c.warning;
			return l`
      <vaadin-details
        theme="reverse"
        .opened=${this.javaPluginSectionOpened}
        @opened-changed=${(e) => {
				t(() => {
					this.javaPluginSectionOpened = e.detail.value;
				}), this.requestLayoutUpdate();
			}}>
        <vaadin-details-summary class="px-4 py-3.5" slot="summary">
          <div class="flex gap-1.5">
            <vaadin-icon
              class="${e.idePluginInstalled ? "text-teal-11" : "text-ruby-11"}"
              .svg=${a}></vaadin-icon>
            <span>${r}</span>
          </div>
        </vaadin-details-summary>
        <div>${i}</div>
      </vaadin-details>
    `;
		}
		renderNoIdePluginContent() {
			return l`
      <div class="flex flex-col gap-2 pb-4 px-4">
        <p class="m-0 text-secondary">
          For the best development experience, use
          <a class="gap-1 inline-flex items-center" href="https://code.visualstudio.com"
            ><vaadin-icon class="icon-sm" .svg=${c.vsCode}></vaadin-icon>Visual Studio Code</a
          >
          or
          <a class="gap-1 inline-flex items-center" href="https://www.jetbrains.com/idea"
            ><vaadin-icon class="icon-sm" .svg=${c.intelliJ}></vaadin-icon>IntelliJ IDEA</a
          >.
        </p>
      </div>
    `;
		}
		renderEclipsePluginContent() {
			return l`
      <div class="flex flex-col gap-2 items-start pb-4 px-4">
        <p class="m-0 text-secondary">Install the Vaadin Eclipse Plugin to ensure a smooth development workflow</p>
        <p class="m-0 text-secondary">
          Installing the plugin is not required, but strongly recommended. Some Vaadin Copilot functionality, such as
          undo, will not function optimally without the plugin.
        </p>
        <vaadin-button
          class="mt-2"
          @click="${() => {
				window.open(f, "_blank");
			}}"
          >Install from Eclipse Marketplace
          <vaadin-icon slot="suffix" .svg="${c.arrowOutward}"></vaadin-icon>
        </vaadin-button>
      </div>
    `;
		}
		renderPluginInstalledContent() {
			return l`
      <p class="m-0 pb-4 px-4 text-secondary">You have a running plugin. Enjoy your awesome development workflow!</p>
    `;
		}
		renderPluginIsNotInstalledContent(e) {
			let t = null, n = "Install from Marketplace";
			return e.intellij ? (t = u, n = "Install from JetBrains Marketplace") : e.vscode ? (t = d, n = "Install from VSCode Marketplace") : e.eclipse && (t = f, n = "Install from Eclipse Marketplace"), l`
      <div class="flex flex-col gap-2 items-start pb-4 px-4">
        <p class="m-0 text-secondary">Install the Vaadin IDE Plugin to ensure a smooth development workflow</p>
        <p class="m-0 text-secondary">
          Installing the plugin is not required, but strongly recommended. Some Vaadin Copilot functionality, such as
          undo, will not function optimally without the plugin.
        </p>
        ${t ? l` <vaadin-button
              class="mt-2"
              @click="${() => {
				window.open(t, "_blank");
			}}"
              >${n}
              <vaadin-icon slot="suffix" .svg="${c.arrowOutward}"></vaadin-icon>
            </vaadin-button>` : o}
      </div>
    `;
		}
		getActiveTabContent(e, t) {
			return this.hotswapTab === "jrebel" ? t.jrebel ? this.renderJRebelInstalledContent() : this.renderJRebelNotInstalledContent() : e.intellij ? this.renderIntelliJHotswapHint() : e.vscode ? this.renderVSCodeHotswapHint() : this.renderHotswapAgentNotInstalledContent(e);
		}
		renderHotswapSection(e) {
			let { jdkInfo: n } = N;
			if (!n) return o;
			let r = P(), i = F(), a, s;
			r === "success" ? (a = c.checkCircle, s = "Java Hotswap is enabled") : r === "warning" ? (a = c.warning, s = "Java Hotswap is not enabled") : r === "error" && (a = c.warning, s = "Java Hotswap is partially enabled");
			let u = this.getActiveTabContent(e, n), d = i === "jrebel" ? this.renderJRebelInstalledContent() : this.renderHotswapAgentInstalledContent(), f = this.hotswapTab === "hotswapagent" ? 0 : 1;
			return l` <vaadin-details
      theme="reverse"
      .opened=${this.hotswapSectionOpened}
      @opened-changed=${(e) => {
				t(() => {
					this.hotswapSectionOpened = e.detail.value;
				}), this.requestLayoutUpdate();
			}}>
      <vaadin-details-summary class="px-4 py-3.5" slot="summary">
        <div class="flex gap-1.5">
          <vaadin-icon
            class="${r === "success" ? "text-teal-11" : "text-ruby-11"}"
            .svg=${a}></vaadin-icon>
          <span>${s}</span>
        </div>
      </vaadin-details-summary>
      <div>
        ${i === "none" ? l`
              <vaadin-tabs
                .selected=${f}
                @selected-changed=${(e) => {
				this.hotswapTab = e.detail.value === 0 ? "hotswapagent" : "jrebel";
			}}>
                <vaadin-tab>Hotswap Agent</vaadin-tab>
                <vaadin-tab>JRebel</vaadin-tab>
              </vaadin-tabs>
              ${u}
            ` : l`${d}`}
      </div>
    </vaadin-details>`;
		}
		renderJRebelNotInstalledContent() {
			return l`
      <div class="flex flex-col gap-2 p-4">
        <p class="m-0 text-secondary">
          <a class="inline-flex items-center" href="https://www.jrebel.com"
            >JRebel <vaadin-icon class="icon-sm" .svg=${c.arrowOutward}></vaadin-icon
          ></a>
          is a commercial hotswap solution. Vaadin detects the JRebel Agent and automatically reloads the application in
          the browser after the Java changes have been hotpatched.
        </p>
        <p class="m-0 text-secondary">
          Go to
          <a
            class="inline-flex items-center"
            href="https://www.jrebel.com/products/jrebel/learn"
            target="_blank"
            rel="noopener noreferrer">
            https://www.jrebel.com/products/jrebel/learn
            <vaadin-icon class="icon-sm" .svg=${c.arrowOutward}></vaadin-icon
          ></a>
          to get started.
        </p>
      </div>
    `;
		}
		renderHotswapAgentNotInstalledContent(e) {
			return l` <div class="p-2">${[
				this.renderJavaRunningInDebugModeSection(),
				this.renderHotswapAgentJdkSection(e),
				this.renderInstallHotswapAgentJdkSection(e),
				this.renderHotswapAgentVersionSection(),
				this.renderHotswapAgentMissingArgParam(e)
			]}</div> `;
		}
		renderIntelliJHotswapHint() {
			return l` <div class="flex flex-col gap-2 p-4">
      <h3 class="font-semibold my-0 text-sm">Use 'Debug using Hotswap Agent' launch configuration</h3>
      <p class="m-0 text-secondary">
        Vaadin IntelliJ plugin offers launch mode that does not require any manual configuration!
      </p>
      <p class="m-0 text-secondary">
        In order to run recommended launch configuration, you should click three dots right next to Debug button and
        select
        <code class="bg-gray-3 dark:bg-gray-7 font-mono inline-flex px-1.5 py-px rounded-md text-body text-xs"
          >Debug using Hotswap Agent</code
        >
        option.
      </p>
    </div>`;
		}
		renderVSCodeHotswapHint() {
			return l` <div>
      <h3 class="font-semibold my-0 text-sm">Use 'Debug (hotswap)'</h3>
      With Vaadin Visual Studio Code extension you can run Hotswap Agent without any manual configuration required!
      <p class="m-0">
        Click
        <code class="bg-gray-3 dark:bg-gray-7 font-mono inline-flex px-1.5 py-px rounded-md text-body text-xs"
          >Debug (hotswap)</code
        >
        within your main class to debug application using Hotswap Agent.
      </p>
    </div>`;
		}
		renderJavaRunningInDebugModeSection() {
			return l`
      <vaadin-details theme="reverse" .opened="${!N.jdkInfo?.runningInJavaDebugMode}">
        <vaadin-details-summary class="p-2" slot="summary">Run Java in debug mode</vaadin-details-summary>
        <p class="m-0 pb-2 px-2 text-secondary">Start the application in debug mode in the IDE.</p>
      </vaadin-details>
    `;
		}
		renderHotswapAgentMissingArgParam(e) {
			return l`
      <vaadin-details theme="reverse" .opened="${!(N.jdkInfo?.runningWitHotswap && N.jdkInfo?.runningWithExtendClassDef)}">
        <vaadin-details-summary class="p-2" slot="summary">Enable HotswapAgent</vaadin-details-summary>
        <div class="flex flex-col gap-2 pb-2 px-2 text-secondary">
          <ul class="m-0 ps-4">
            ${e.intellij ? l`<li>Launch as mentioned in the previous step</li>` : o}
            ${e.intellij ? l`<li>
                  To manually configure IntelliJ, add the
                  <code
                    class="bg-gray-3 dark:bg-gray-7 break-all font-mono inline-flex px-1.5 py-px rounded-md text-body text-xs"
                    >-XX:HotswapAgent=fatjar -XX:+AllowEnhancedClassRedefinition -XX:+UpdateClasses</code
                  >
                  JVM arguments when launching the application.
                </li>` : l`<li>
                  Add the
                  <code
                    class="bg-gray-3 dark:bg-gray-7 break-all font-mono inline-flex px-1.5 py-px rounded-md text-body text-xs"
                    >-XX:HotswapAgent=fatjar -XX:+AllowEnhancedClassRedefinition -XX:+UpdateClasses</code
                  >
                  JVM arguments when launching the application.
                </li>`}
          </ul>
        </div>
      </vaadin-details>
    `;
		}
		renderHotswapAgentJdkSection(e) {
			let t = N.jdkInfo?.extendedClassDefCapable, n = this.downloadStatusMessages?.[this.downloadStatusMessages.length - 1] === U, r = this.downloadProgress > 0 ? l`<vaadin-progress-bar .value="${this.downloadProgress}" min="0" max="1"></vaadin-progress-bar>` : o, i = n ? l`<h3 class="font-semibold my-0 text-sm">
          Go to VS Code and launch the 'Debug using Hotswap Agent' configuration
        </h3>` : o;
			return l`
      <vaadin-details theme="reverse" .opened="${!t}">
        <vaadin-details-summary class="p-2" slot="summary">Run using JetBrains Runtime JDK</vaadin-details-summary>
        <div class="flex flex-col gap-2 pb-2 px-2 text-secondary">
          <p class="m-0">JetBrains Runtime provides much better hotswapping compared to other JDKs.</p>
          <ul class="m-0 ps-4">
            ${e.intellij && B("1.3.0", E.idePluginState?.version) ? l` <li>Upgrade to the latest IntelliJ plugin</li>` : o}
            ${e.intellij ? l` <li>Launch the application in IntelliJ using "Debug using Hotswap Agent"</li>` : o}
            ${e.vscode ? l` <li>
                  <a href @click="${(e) => this.downloadJetbrainsRuntime(e)}"
                    >Let Copilot download and set up JetBrains Runtime for VS Code</a
                  >
                  ${r}
                  <ul>
                    ${this.downloadStatusMessages.map((e) => l`<li>${e}</li>`)} ${i}
                  </ul>
                </li>` : o}
            <li>
              ${e.intellij || e.vscode ? l`If there is a problem, you can manually
                    <a target="_blank" href="${H}">download JetBrains Runtime JDK</a> and set up your
                    debug configuration to use it.` : l`<a target="_blank" href="${H}">Download JetBrains Runtime JDK</a> and set up
                    your debug configuration to use it.`}
            </li>
          </ul>
        </div>
      </vaadin-details>
    `;
		}
		renderInstallHotswapAgentJdkSection(e) {
			let t = N.jdkInfo?.hotswapAgentFound, n = N.jdkInfo?.extendedClassDefCapable;
			return l`
      <vaadin-details theme="reverse" .opened="${!t}">
        <vaadin-details-summary class="p-2" slot="summary"> Install HotswapAgent </vaadin-details-summary>
        <div class="flex flex-col gap-2 pb-2 px-2 text-secondary">
          <p class="m-0">
            Hotswap Agent provides application level support for hot reloading, such as reinitalizing Vaadin @Route or
            @BrowserCallable classes when they are updated.
          </p>
          <ul class="m-0 ps-4">
            ${e.intellij ? l`<li>Launch as mentioned in the previous step</li>` : o}
            ${!e.intellij && !n ? l`<li>First install JetBrains Runtime as mentioned in the step above.</li>` : o}
            ${e.intellij ? l`<li>
                  To manually configure IntelliJ, download HotswapAgent and install the jar file as
                  <code class="bg-gray-3 dark:bg-gray-7 font-mono inline-flex px-1.5 py-px rounded-md text-body text-xs"
                    >[JAVA_HOME]/lib/hotswap/hotswap-agent.jar</code
                  >
                  in the JetBrains Runtime JDK. Note that the file must be renamed to exactly match this path.
                </li>` : l`<li>
                  Download HotswapAgent and install the jar file as
                  <code class="bg-gray-3 dark:bg-gray-7 font-mono inline-flex px-1.5 py-px rounded-md text-body text-xs"
                    >[JAVA_HOME]/lib/hotswap/hotswap-agent.jar</code
                  >
                  in the JetBrains Runtime JDK. Note that the file must be renamed to exactly match this path.
                </li>`}
          </ul>
        </div>
      </vaadin-details>
    `;
		}
		renderHotswapAgentVersionSection() {
			if (!N.jdkInfo?.hotswapAgentFound) return o;
			let e = N.jdkInfo?.hotswapVersionOk, t = N.jdkInfo?.hotswapVersion, n = N.jdkInfo?.hotswapAgentLocation;
			return l`
      <vaadin-details theme="reverse" .opened="${!e}">
        <vaadin-details-summary class="p-2" slot="summary">Hotswap version requires update</vaadin-details-summary>
        <div>
          HotswapAgent version ${t} is in use
          <a target="_blank" href="https://github.com/HotswapProjects/HotswapAgent/releases"
            >Download the latest HotswapAgent</a
          >
          and place it in
          <code class="bg-gray-3 dark:bg-gray-7 font-mono inline-flex px-1.5 py-px rounded-md text-body text-xs"
            >${n}</code
          >
        </div>
      </vaadin-details>
    `;
		}
		renderJRebelInstalledContent() {
			return l` <p class="m-0 pb-2 px-2">JRebel is in use. Enjoy your awesome development workflow!</p> `;
		}
		renderHotswapAgentInstalledContent() {
			return l`
      <p class="m-0 pb-4 px-4 text-secondary">Hotswap agent is in use. Enjoy your awesome development workflow!</p>
    `;
		}
		async downloadJetbrainsRuntime(e) {
			return e.target.disabled = !0, e.preventDefault(), this.downloadStatusMessages = [], n(`${m}set-up-vs-code-hotswap`, {}, (e) => {
				e.data.error ? (A("Error downloading JetBrains runtime", e.data.error), this.downloadStatusMessages = [...this.downloadStatusMessages, "Download failed"]) : this.downloadStatusMessages = [...this.downloadStatusMessages, U];
			});
		}
		downloadStatusUpdate(e) {
			let t = e.detail.progress;
			t ? this.downloadProgress = t : this.downloadStatusMessages = [...this.downloadStatusMessages, e.detail.message];
		}
	}, V.NAME = "copilot-development-setup-user-guide", V), h([v()], W.prototype, "javaPluginSectionOpened", void 0), h([v()], W.prototype, "hotswapSectionOpened", void 0), h([v()], W.prototype, "hotswapTab", void 0), h([v()], W.prototype, "downloadStatusMessages", void 0), h([v()], W.prototype, "downloadProgress", void 0), W = h([g(W.NAME)], W), G = class extends x {
		createRenderRoot() {
			return this;
		}
		connectedCallback() {
			super.connectedCallback(), this.classList.add("contents");
		}
		render() {
			return l`<vaadin-button
      id="close"
      @click="${() => y.closePanel(K.tag)}"
      >Close
    </vaadin-button>`;
		}
	}, G = h([g("copilot-development-setup-footer-actions")], G), K = {
		header: "Development Workflow",
		tag: L,
		footerActionsTag: "copilot-development-setup-footer-actions",
		individual: !0
	}, globalThis.Vaadin.copilot.plugins.push({ init(e) {
		e.addPanel(K);
	} }), y.addPanel(K);
}))();
export { G as CopilotDevelopmentSetupFooterActions, W as CopilotDevelopmentSetupUserGuide, K as copilotDevelopmentSetupPanelConfig };
