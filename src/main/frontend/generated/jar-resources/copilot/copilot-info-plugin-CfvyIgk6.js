import { i as e, n as t } from "./chunk-DiqZc92J.js";
import { _ as n, b as r, n as i, o as a, r as o, t as s, u as c } from "./icons-Bzfd0PEL.js";
import { a as l, d as u, i as d, l as f, n as p, o as m, r as h, s as g, t as _ } from "./section-panel-ui-state-COWn2ulI.js";
import { a as v, i as y, n as b, r as x } from "./copilot-ui-state-Dc6l_5DA.js";
import { c as S, r as C, s as w } from "./copilot-development-setup-user-guide-utils-Ctr4ib5Q.js";
import { n as T, t as E } from "./base-panel-3DcY1Auw.js";
import { n as D, r as O, t as k } from "./copy-to-clipboard-Cma2E2HP.js";
//#region frontend/copilot/plugins/copilot-info/copilot-info-plugin.ts
function A(e, t) {
	let n;
	return n = e === !0 ? "text-teal-11" : e === "partial" ? "text-amber-11" : "text-ruby-11", c`<span class="${n}">${t}</span>`;
}
var j, M, N, P;
//#endregion
t((() => {
	g(), o(), T(), x(), v(), d(), i(), j = /* @__PURE__ */ e(k(), 1), w(), _(), D(), n(), m(), M = class extends E {
		constructor(...e) {
			super(...e), this.sortedEntries = [];
		}
		connectedCallback() {
			super.connectedCallback(), this.classList.add("contents"), this.reaction(() => b.projectInfoEntries, () => {
				if (!b.projectInfoEntries) return;
				let e = [...b.projectInfoEntries, {
					name: "Development Workflow",
					value: ""
				}];
				e = e.filter((e) => e.name !== "Java Hotswap"), this.sortedEntries = e.sort((e, t) => e.name.localeCompare(t.name));
			}, { fireImmediately: !0 });
		}
		render() {
			return c` <div class="flex flex-col py-2 px-4">
      <dl class="border-dashed divide-y m-0">
        ${O(this.sortedEntries.filter((e) => e.name !== "Java Hotswap"), (e) => e.name, (e) => this.renderRow(e))}
      </dl>
    </div>`;
		}
		renderRow(e) {
			if (e.name === "Development Workflow") return this.renderDevelopmentWorkflowButton();
			let t = e.name === "IDE Plugin" && e.value === !0 && b.idePluginState?.ide ? b.idePluginState.ide : e.value, n = this.getIcon(e.name, t), r = this.getIconColor(e.name), i = this.getTextColor(e);
			return c`
      <div class="flex gap-2 py-2">
        <dt class="flex gap-2">
          ${n ? c`<vaadin-icon class="${r}" .svg="${n}"></vaadin-icon>` : a} ${e.name}
        </dt>
        <dd class="flex gap-2 m-0 ${i}">${this.renderRowValue(e)}</dd>
      </div>
    `;
		}
		renderRowValue(e) {
			return e.name === "Vaadin Employee" && e.value === !0 ? c`
        <vaadin-icon id="vaadin-employee" class="text-teal-11" .svg="${s.check}"></vaadin-icon>
        <vaadin-tooltip for="vaadin-employee" text="Yes"></vaadin-tooltip>
      ` : c` ${!e.booleanInfo && typeof e.value == "string" ? e.value : a}
    ${e.booleanInfo && typeof e.value == "boolean" ? A(e.value, e.booleanInfo.ariaLabel) : a}
    ${e.booleanInfo?.text ? e.booleanInfo.text : a}
    ${e.name === "Vaadin" ? this.renderVaadinRowMore() : a}`;
		}
		renderVaadinRowMore() {
			let e = b.newVaadinVersionState?.versions !== void 0 && b.newVaadinVersionState.versions.length > 0;
			return c`
      ${b.projectVersionReleaseNoteInfo && b.projectVersionReleaseNoteInfo.url ? c`<a
            class="flex gap-0.5 items-center"
            href="${b.projectVersionReleaseNoteInfo.url}"
            id="release-notes-link"
            target="_blank"
            >Release notes <vaadin-icon class="icon-sm" .svg="${s.arrowOutward}"></vaadin-icon
          ></a>` : a}
      <vaadin-button
        aria-label="Edit Vaadin version"
        class="-my-1.5 relative"
        @click="${(e) => {
				e.stopPropagation(), r("use-info", { source: "editVaadinVersion" }), p.openPanel("copilot-vaadin-versions");
			}}"
        id="new-vaadin-version-btn"
        theme="icon tertiary">
        <vaadin-icon .svg="${s.editSquare}"></vaadin-icon>
        <vaadin-tooltip slot="tooltip" text="Edit Vaadin version"></vaadin-tooltip>
        ${e ? c`<span aria-hidden="true" class="absolute bg-amber-11 end-0.5 rounded-full size-1 top-0.5"></span>` : ""}
      </vaadin-button>
    `;
		}
		renderDevelopmentWorkflowButton() {
			let e = C(), t = "", n = s.doneAll, i = "";
			return e.status === "success" ? (t = "text-teal-11", i = "IDE Plugin & Java Hotswap") : e.status === "warning" ? (t = "text-amber-11", n = s.arrowUploadReady, i = "Improve") : e.status === "error" && (t = "text-ruby-11", n = s.handyman, i = "Fix"), c`
      <div class="flex gap-2 py-2">
        <dt class="flex gap-2">
          <vaadin-icon class="text-amber-11" .svg="${s.bolt}"></vaadin-icon>
          Development Workflow
        </dt>
        <dd class="m-0">
          <vaadin-button
            class="-my-1.5 ${t}"
            id="development-workflow-status-detail"
            theme="tertiary"
            @click=${() => {
				r("use-info", { source: "developmentWorkflow" }), S();
			}}>
            <vaadin-icon slot="prefix" .svg="${n}"></vaadin-icon>
            ${i}
          </vaadin-button>
        </dd>
      </div>
    `;
		}
		getIconColor(e) {
			return e.includes("Vaadin") || e === "Copilot" ? "text-vaadin-blue" : "";
		}
		getTextColor(e) {
			if (typeof e.value == "string") {
				if (e.value.startsWith("Enabled")) return "text-teal-11";
				if (e.value.startsWith("Disabled")) return "text-ruby-11";
			}
			return "text-secondary";
		}
		getIcon(e, t) {
			switch (e) {
				case "Browser": {
					let e = typeof t == "string" ? t.toLowerCase() : "";
					return e.includes("chrome") && !e.includes("edg") ? s.chrome : e.includes("firefox") ? s.firefox : e.includes("safari") && !e.includes("chrome") ? s.safari : e.includes("edg") ? s.edge : s.webAsset;
				}
				case "Copilot": return s.vaadin;
				case "Flow": return s.flow;
				case "Frontend Hotswap": return s.swapHoriz;
				case "Hilla": return s.hilla;
				case "Java": return s.java;
				case "OS": {
					let e = typeof t == "string" ? t.toLowerCase() : "";
					return e.includes("mac") ? s.apple : e.includes("win") ? s.windows : s.computer;
				}
				case "Spring": return s.spring;
				case "Spring Boot": return s.springBoot;
				case "Spring Data JPA": return s.springData;
				case "Spring Security": return s.springSecurity;
				case "Vaadin":
				case "Vaadin Employee": return s.vaadin;
				case "Java Hotswap": return s.swapHoriz;
				case "IDE Plugin": return typeof t == "string" ? t.toLowerCase() === "intellij" ? s.intelliJ : t.toLowerCase() === "vscode" ? s.vsCode : t.toLowerCase() === "eclipse" ? s.eclipse : s.developerModeTv : s.developerModeTv;
				default: return null;
			}
		}
	}, l([f()], M.prototype, "sortedEntries", void 0), M = l([u("copilot-info-panel")], M), N = class extends h {
		createRenderRoot() {
			return this;
		}
		connectedCallback() {
			super.connectedCallback(), this.style.display = "flex";
		}
		render() {
			return c` <vaadin-button
      aria-label="Copy to clipboard"
      @click=${() => {
				r("use-info", { source: "copySystemInfo" }), y.emit("system-info-with-callback", {
					callback: j.default,
					notify: !0
				});
			}}
      theme="icon tertiary">
      <vaadin-icon .svg="${s.fileCopy}"></vaadin-icon>
      <vaadin-tooltip slot="tooltip" text="Copy to clipboard"></vaadin-tooltip>
    </vaadin-button>`;
		}
	}, N = l([u("copilot-info-actions")], N), P = {
		header: "Info",
		tag: "copilot-info-panel",
		actionsTag: "copilot-info-actions",
		eager: !0,
		toolbarOptions: {
			iconKey: "info",
			allowedModesWithOrder: { common: 0 }
		}
	}, window.Vaadin.copilot.plugins.push({ init(e) {
		e.addPanel(P);
	} });
}))();
export { N as Actions, M as CopilotInfoPanel };
