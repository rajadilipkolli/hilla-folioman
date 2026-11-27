import { n as e } from "./chunk-DiqZc92J.js";
import { a as t, d as n, i as r, s as i, t as a } from "./dom-utils-YgRxzPHS.js";
import { a as o, i as s, n as c, r as l } from "./copilot-ui-state-Dc6l_5DA.js";
import { l as u, n as d, r as f, s as p, t as m } from "./copilot-unsaved-operation-coordinator-BszTzv5k.js";
import { n as h, t as g } from "./copilot-stored-machine-state-D6qB_Peh.js";
import { n as _, t as v } from "./track-active-mode-event-BNnPoWDk.js";
//#region frontend/copilot/shared/os-utils.ts
function y() {
	let e = window.navigator.userAgent;
	return e.indexOf("Windows") === -1 ? e.indexOf("Mac") === -1 ? e.indexOf("Linux") === -1 ? null : "Linux" : "Mac" : "Windows";
}
function b() {
	return y() === "Mac";
}
function x() {
	return b() ? "⌘" : "Ctrl";
}
var S = e((() => {}));
//#endregion
//#region frontend/copilot/copilot-shortcuts.ts
function C(e) {
	if ((e.ctrlKey || e.metaKey) && e.key === "c" && !e.shiftKey) {
		let e = document.querySelector("copilot-main")?.shadowRoot, t;
		if (t = typeof e?.getSelection == "function" ? e?.getSelection() : document.getSelection() ?? void 0, t && t.rangeCount === 1) {
			let e = t.getRangeAt(0).commonAncestorContainer;
			if (e.nodeType === Node.TEXT_NODE) return n(e);
		}
	}
	return !1;
}
function w(e) {
	let t = a(e, "vaadin-context-menu-overlay");
	if (!t) return !1;
	let n = t.owner;
	return n ? !!a(n, "copilot-component-overlay") : !1;
}
function T() {
	return c.idePluginState?.supportedActions?.find((e) => e === "undo");
}
function E(e) {
	let n = e;
	if (m(e)) return !0;
	let r = t(n);
	for (let e of r) if (m(e)) return !0;
	return !1;
}
var D, O, k, A, j, M, N, P, F, I, L = e((() => {
	o(), l(), S(), g(), i(), u(), v(), f(), D = !1, O = 0, k = (e) => {
		if (h.isActivationShortcut() && h.getToolbarExpandMode() !== "never") if (e.key === "Shift" && !e.ctrlKey && !e.altKey && !e.metaKey) D = !0;
		else if (D && e.shiftKey && (e.key === "Control" || e.key === "Meta")) {
			if (O++, O === 2) return d(() => {
				c.activeMode === "play" ? c.lastNonPlayMode === void 0 ? c.setActiveMode("edit", !0) : c.setActiveMode(c.lastNonPlayMode, !0) : c.setActiveMode("play", !0), _();
			}, "mode-change"), O = 0, !0;
			setTimeout(() => {
				O = 0;
			}, 500);
		} else O = 0;
		return !1;
	}, A = (e) => {
		if (k(e)) {
			e.stopPropagation();
			return;
		}
		if (p()?.appInteractable) return;
		let t = r();
		if (!t) return;
		let n = w(t), i = a(t, "vaadin-dialog") ?? (t.localName === "vaadin-dialog" ? t : null), o = i !== null && i.hasAttribute("panel-container"), l = t.localName === "copilot-main", u = a(t, "copilot-outline-panel") !== null, d = a(t, "copilot-toolbar") !== null;
		if (!l && !n && e.key !== "Escape" && !u && !d) {
			e.stopPropagation();
			return;
		}
		let f = !0, m = !1;
		if (C(e)) f = !1;
		else if (e.key === "Escape") {
			if (c.loginCheckActive && c.setLoginCheckActive(!1), E(t)) {
				e.stopPropagation();
				return;
			}
			s.emit("escape-key-pressed", { event: e });
		} else N(e) && c.activeMode === "edit" && (!o || u) ? (s.emit("delete-selected", {}), m = !0) : (e.ctrlKey || e.metaKey) && e.key === "d" && c.activeMode === "edit" && (!o || u) ? (s.emit("duplicate-selected", {}), m = !0) : (e.ctrlKey || e.metaKey) && e.key === "b" && (!o || u) ? (s.emit("show-selected-in-ide", { attach: e.shiftKey }), m = !0) : (e.ctrlKey || e.metaKey) && e.key === "z" && T() && (!o || u) ? (s.emit("undoRedo", { undo: !e.shiftKey }), m = !0) : C(e) || s.emit("keyboard-event", { event: e });
		c.setMultiSelectionOn(M(e)), f && e.stopPropagation(), m && e.preventDefault();
	}, j = (e) => {
		p()?.appInteractable || r() && M(e) && c.setMultiSelectionOn(!1);
	}, M = (e) => (e.key === "Control" || e.key === "Meta") && !e.shiftKey && !e.altKey, N = (e) => (e.key === "Backspace" || e.key === "Delete") && !e.shiftKey && !e.ctrlKey && !e.altKey && !e.metaKey, P = x(), F = "⇧", I = {
		toggleCopilot: `<kbd>${F} + ${P} ${P}</kbd>`,
		openAiPopover: `<kbd>${F} + Space</kbd>`,
		undo: `<kbd>${P} + Z</kbd>`,
		redo: `<kbd>${P} + ${F} + Z</kbd>`,
		duplicate: `<kbd>${P} + D</kbd>`,
		goToSource: `<kbd>${P} + B</kbd>`,
		goToAttachSource: `<kbd>${P} + ${F} + B</kbd>`,
		selectParent: "<kbd>←</kbd>",
		selectPreviousSibling: "<kbd>↑</kbd>",
		selectNextSibling: "<kbd>↓</kbd>",
		delete: "<kbd>DEL</kbd>",
		copy: `<kbd>${P} + C</kbd>`,
		paste: `<kbd>${P} + V</kbd>`
	};
}));
//#endregion
export { I as a, L as i, j as n, A as r, k as t };
