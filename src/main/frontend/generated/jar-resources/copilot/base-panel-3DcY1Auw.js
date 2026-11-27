import { n as e } from "./chunk-DiqZc92J.js";
import { _ as t, b as n } from "./icons-Bzfd0PEL.js";
import { s as r, t as i } from "./dom-utils-YgRxzPHS.js";
import { i as a, n as o, r as s, t as c } from "./section-panel-ui-state-COWn2ulI.js";
import { a as l, i as u } from "./copilot-ui-state-Dc6l_5DA.js";
import { l as d, r as f, s as p, t as m } from "./copilot-unsaved-operation-coordinator-BszTzv5k.js";
//#region frontend/copilot/shared/section-panels/base-panel.ts
var h, g = e((() => {
	a(), l(), c(), r(), d(), f(), t(), h = class extends s {
		constructor(...e) {
			super(...e), this.eventBusRemovers = [], this.messageHandlers = {}, this.handleESC = (e) => {
				let t = o.getPanelByTag(this.tagName);
				p().appInteractable && t && !t.individual || e.key === "Escape" && m(this);
			};
		}
		getPreferredWidth() {
			return 400;
		}
		getPreferredHeight() {
			return 400;
		}
		getPreferredMaxWidth() {
			return 500;
		}
		createRenderRoot() {
			return this;
		}
		trackPanelEvent(e, t) {
			n(e, {
				...t,
				panel: this.localName
			});
		}
		onEventBus(e, t) {
			this.eventBusRemovers.push(u.on(e, t));
		}
		connectedCallback() {
			super.connectedCallback(), this.addESCListener();
		}
		disconnectedCallback() {
			super.disconnectedCallback(), this.eventBusRemovers.forEach((e) => e()), this.removeESCListener();
		}
		addESCListener() {
			document.addEventListener("keydown", this.handleESC);
		}
		removeESCListener() {
			document.removeEventListener("keydown", this.handleESC);
		}
		onCommand(e, t) {
			this.messageHandlers[e] = t;
		}
		handleMessage(e) {
			return this.messageHandlers[e.command] ? (this.messageHandlers[e.command].call(this, e), !0) : !1;
		}
		repositionInPopover(e) {
			let t = Math.max(this.scrollHeight, this.offsetHeight);
			if (t === 0) return;
			let n = e.getAttribute("for");
			if (!n) return;
			let r = e.parentElement?.querySelector(`#${n}`);
			if (!r) return;
			let i = r.getBoundingClientRect(), a = i.top - 16, o = window.innerHeight - 16 - i.bottom, s = (e.position ?? e.getAttribute("position") ?? "bottom").startsWith("top"), c = s ? a : o;
			c >= t || (s ? o : a) <= c || (e.position = s ? "bottom" : "top", e._overlayElement?._updatePosition?.());
		}
		requestLayoutUpdate() {
			let e = this.localName;
			if (o.positionUpdatedManually(e)) return Promise.resolve();
			let t = o.getPanelByTag(e);
			return t ? new Promise((n) => {
				requestAnimationFrame(() => {
					let r = i(this, "vaadin-dialog");
					if (!r) {
						let e = i(this, "vaadin-popover");
						e && this.repositionInPopover(e), n();
						return;
					}
					let a = this.parentElement?.getBoundingClientRect();
					if (!a || a.width === 0 && a.height === 0) {
						n();
						return;
					}
					let s = r._overlayElement, c = s?.shadowRoot?.querySelector("[part=\"overlay\"]"), l = s?.shadowRoot?.querySelector("[part=\"content\"]"), u = s?.shadowRoot?.querySelector("[part=\"footer\"]");
					if (!c || !l) {
						n();
						return;
					}
					let d = Math.max(this.scrollWidth, this.offsetWidth, a.width), f = Math.max(this.scrollHeight, this.offsetHeight, a.height), p = c.getBoundingClientRect(), m = l.getBoundingClientRect(), h = Math.max(0, p.width - m.width), g = Math.max(0, p.height - m.height), _ = u?.getBoundingClientRect(), v = !!_ && _.width > 0 && _.height > 0, y = v ? Math.max(0, p.left - _.left) + Math.max(0, _.right - p.right) : 0, b = v ? Math.max(0, p.top - _.top) + Math.max(0, _.bottom - p.bottom) : 0, x = this.getPreferredMaxWidth(), S = Math.floor(window.innerHeight * 2 / 3), C = Math.max(0, this.getPreferredWidth()), w = Math.max(0, this.getPreferredHeight()), T = Math.min(x, Math.max(120, window.innerWidth - 32)), E = Math.min(S, Math.max(120, window.innerHeight - 32)), D = Math.max(120, Math.min(T, Math.max(C, Math.ceil(d + h + y)))), O = Math.max(120, Math.min(E, Math.max(w, Math.ceil(f + g + b)))), k = `${D}px`, A = `${O}px`;
					r.setAttribute("width", k), r.setAttribute("height", A), r.width = k, r.height = A;
					let j = t.position, M = Number.parseFloat(r.getAttribute("top") ?? ""), N = Number.parseFloat(r.getAttribute("left") ?? ""), P = j?.top ?? (Number.isNaN(M) ? 0 : M), F = j?.left ?? (Number.isNaN(N) ? 0 : N), I = document.querySelector("copilot-main")?.shadowRoot?.querySelector(`copilot-toolbar #${e}-toolbar-btn`), L = P, R = F;
					if (I) {
						let e = I.getBoundingClientRect(), t = e.top - 16 - 16, n = window.innerHeight - 16 - e.bottom - 16;
						L = t >= O || t >= n ? e.top - O - 16 : e.bottom + 16, R = e.left + e.width / 2 - D / 2;
					}
					L = Math.max(16, Math.min(L, window.innerHeight - 16 - O)), R = Math.max(16, Math.min(R, window.innerWidth - 16 - D)), o.updatePanel(e, { position: {
						top: L,
						left: R,
						width: D,
						height: O
					} }, !1), n();
				});
			}) : Promise.resolve();
		}
	};
}));
//#endregion
export { g as n, h as t };
