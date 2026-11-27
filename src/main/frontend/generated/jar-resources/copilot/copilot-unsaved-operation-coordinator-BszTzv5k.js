import { i as e, n as t } from "./chunk-DiqZc92J.js";
import { C as n, w as r } from "./icons-Bzfd0PEL.js";
import { l as i, n as a, s as o } from "./consts-BXxpRU5f.js";
import { c as s, s as c } from "./dom-utils-YgRxzPHS.js";
import { n as l, t as u } from "./section-panel-ui-state-COWn2ulI.js";
import { n as d, r as f } from "./copilot-ui-state-Dc6l_5DA.js";
//#region frontend/copilot/plugins/copilot-plugins.ts
function p(e) {
	e.init({
		addPanel: (e) => {
			l.addPanel(e);
		},
		send(e, t) {
			r(e, t);
		}
	});
}
function m() {
	_().publicPluginsState === "NOT_INITIALIZED" && (v.push(import("./copilot-log-plugin-DJevks0r.js")), v.push(import("./copilot-info-plugin-CfvyIgk6.js")), v.push(import("./copilot-features-plugin-BPCo646m.js")), v.push(import("./copilot-feedback-plugin-UwHQMkPJ.js")), v.push(import("./copilot-settings-panel-B-7TEosr.js")), v.push(import("./copilot-impersonator-plugin-DXm6UyRg.js")), v.push(import("./copilot-development-setup-user-guide-CyLsZdUz.js")), v.push(import("./copilot-vaadin-versions-D9zbrnIU.js")), b = !0, _().setPublicPluginsState("IMPORTED"));
}
function h() {
	if (_().privatePluginsState === "NOT_INITIALIZED") {
		let e = window.Vaadin?.copilot?._localPluginsUrl, t = `https://cdn.vaadin.com/copilot/${o}/copilot-plugins${a}.js`, n = e || t;
		console.debug(`Loading private plugins from: ${n}`), import(
			/* @vite-ignore */
			n
).then(() => {
			_().setPrivatePluginsState("INITIALIZED"), d.setPrivatePluginCdnError(null);
		}).catch((e) => {
			d.setPrivatePluginCdnError(e), console.warn(`Unable to load plugins from ${n}. Some Copilot features are unavailable.`, e);
		});
	}
}
function g() {
	Promise.all(v).then(() => {
		let e = window.Vaadin;
		if (e.copilot.plugins) {
			let t = e.copilot.plugins;
			e.copilot.plugins.push = (e) => p(e), Array.from(t).forEach((e) => {
				y.includes(e) || (p(e), y.push(e));
			});
		}
	}), v = [], b && _().setPublicPluginsState("INITIALIZED");
}
function _() {
	return window.Vaadin.copilot._uiState;
}
var v, y, b, x = t((() => {
	u(), i(), n(), f(), v = [], y = [], b = !1;
})), S, C = t((() => {
	S = window.Vaadin.copilot.overlayManager;
}));
//#endregion
//#region frontend/copilot/dynamic-module-loader.ts
async function w() {
	return (await import("./copilot-focus-trap-CFfZAt9l.js")).copilotFocusTrap;
}
function T() {
	return import("./typescript-rXzk1P2l.js").then((t) => /* @__PURE__ */ e(t.default, 1));
}
var E = t((() => {}));
//#endregion
//#region frontend/copilot/shared/copilot-modes.ts
function D() {
	S.addOverlayOutsideClickEvent(), S.activate();
}
function O() {
	S.removeOverlayOutsideClickEvent(), S.deactivate();
}
function k() {
	let e = j[d.activeMode];
	return d.forceSelectionEnabled ? {
		...e,
		appInteractable: !1
	} : e;
}
function A(e) {
	return j[e];
}
var j, M = t((() => {
	f(), x(), E(), C(), j = {
		edit: {
			label: "Edit",
			appInteractable: !1,
			toolbarIcon: "code",
			toolbarOrder: 0,
			onActivation: async () => {
				let e = await w();
				e.active || e.activate(), D(), h();
			}
		},
		test: {
			label: "Test",
			appInteractable: !0,
			toolbarIcon: "bugReport",
			toolbarOrder: 1,
			onActivation: async (e) => {
				(e === "edit" || e === "inspect") && ((await w()).deactivate(), O()), h();
			}
		},
		inspect: {
			label: "Inspect",
			appInteractable: !1,
			toolbarIcon: "visibility",
			toolbarOrder: 2,
			onActivation: async () => {
				let e = await w();
				e.active || e.activate(), D(), h();
			}
		},
		play: {
			label: "Play",
			appInteractable: !0,
			toolbarIcon: "playCircle",
			toolbarOrder: 3,
			default: !0,
			onActivation: async (e) => {
				(e === "edit" || e === "inspect") && ((await w()).deactivate(), O());
			}
		}
	};
}));
//#endregion
//#region frontend/copilot/copilot-unsaved-operation-coordinator.ts
function N(e) {
	if (s(e)) {
		if (e instanceof HTMLElement) {
			let t = () => e.close();
			return F(e, t) || t(), !0;
		}
	} else if (e.localName === "vaadin-popover") {
		let t = () => {
			e.opened = !1;
		};
		return F(e, t) || t(), !0;
	}
	return !1;
}
var P, F, I, L, R = t((() => {
	f(), c(), P = (e, t) => {
		let n = document.body.querySelector("copilot-main")?.shadowRoot;
		if (!n) return e(!0), !1;
		let r = Array.from(n.querySelectorAll("[has-unsaved-changes]"));
		if (r.length === 0) return e(!0), !1;
		let i = r.filter((e) => e.getUnsavedChanges !== void 0).filter((e) => e.getUnsavedChanges() !== null).map((e) => e.getUnsavedChanges()).filter((e) => e.reactsTo === void 0 || e.reactsTo.includes(t));
		if (i.length === 0) return console.warn("Could not find any unsaved changes for components"), e(!0), !1;
		let a = i.reduceRight((e, t) => ({
			...t,
			next: e ?? void 0
		}), null);
		return a ? (d.setCurrentUnsavedChangesConfiguration({
			...a,
			finalCallback: e
		}), !0) : (e(!0), !1);
	}, F = (e, t) => {
		let n = e.hasAttribute("has-unsaved-changes") ? e : e.querySelector("[has-unsaved-changes]");
		if (!n) return !1;
		let r = n;
		if (r.getUnsavedChanges === void 0 || r.getUnsavedChanges() === null) return !1;
		let i = r.getUnsavedChanges();
		return i.reactsTo !== void 0 && !i.reactsTo.includes("close-request") ? !1 : (I(i, t), !0);
	}, I = (e, t) => {
		d.setCurrentUnsavedChangesConfiguration({
			...e,
			next: void 0,
			finalCallback: t
		});
	}, L = () => {
		let e = d.currentUnsavedChangesConfiguration;
		if (e !== null) if (e.next) {
			let t = e.next, n = {
				...t,
				next: t.next,
				finalCallback: e.finalCallback
			};
			d.setCurrentUnsavedChangesConfiguration(n);
		} else e.finalCallback && e.finalCallback(!1), d.setCurrentUnsavedChangesConfiguration(null);
	};
}));
//#endregion
export { F as a, A as c, E as d, S as f, g, x as h, L as i, M as l, m, P as n, j as o, C as p, R as r, k as s, N as t, T as u };
