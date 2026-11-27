import { n as e } from "./chunk-DiqZc92J.js";
import { l as t, o as n } from "./consts-BXxpRU5f.js";
//#region frontend/copilot/shared/dom-utils.ts
function r(e) {
	return e.parentElement ?? e.parentNode?.host;
}
function i(e) {
	if (e.assignedSlot) return i(e.assignedSlot);
	if (e.parentElement) return e.parentElement;
	if (e.parentNode instanceof ShadowRoot) return e.parentNode.host;
}
function a(e) {
	if (e instanceof Node) {
		let t = w(e);
		return e instanceof HTMLElement && t.push(e), t.map((e) => e.localName).some((e) => e.startsWith(n));
	}
	return !1;
}
function o(e) {
	return e instanceof Element;
}
function s(e) {
	return e.startsWith("vaadin-") ? e.substring(7).split("-").map((e) => e.charAt(0).toUpperCase() + e.slice(1)).join(" ") : e;
}
function c(e) {
	if (!e) return;
	if (e.id) return `#${e.id}`;
	if (!e.children) return;
	let t = Array.from(e.children).find((e) => e.localName === "label");
	if (t) return t.outerText.trim();
	let n = Array.from(e.childNodes).find((e) => e.nodeType === Node.TEXT_NODE && e.textContent && e.textContent.trim().length > 0);
	if (n && n.textContent) return n.textContent.trim();
}
function l(e) {
	return e instanceof Element && typeof e.getBoundingClientRect == "function" ? e.getBoundingClientRect() : u(e);
}
function u(e) {
	let t = document.createRange();
	t.selectNode(e);
	let n = t.getBoundingClientRect();
	return t.detach(), n;
}
function d() {
	let e = document.activeElement;
	for (; e?.shadowRoot && e.shadowRoot.activeElement;) e = e.shadowRoot.activeElement;
	return e;
}
function f(e) {
	let t = i(e);
	for (; t && t !== document.body;) {
		let e = window.getComputedStyle(t), n = e.overflowY, r = e.overflowX, a = /(auto|scroll)/.test(n) && t.scrollHeight > t.clientHeight, o = /(auto|scroll)/.test(r) && t.scrollWidth > t.clientWidth;
		if (a || o) return t;
		t = i(t);
	}
	return document.documentElement;
}
function p(e, t) {
	return m(e, t) && h(t);
}
function m(e, t) {
	let n = f(e), r = n.getBoundingClientRect();
	if (n === document.documentElement || n === document.body) {
		let e = window.innerWidth || document.documentElement.clientWidth, n = window.innerHeight || document.documentElement.clientHeight;
		return t.top < n && t.bottom > 0 && t.left < e && t.right > 0;
	}
	return t.bottom > r.top && t.top < r.bottom && t.right > r.left && t.left < r.right;
}
function h(e) {
	return e.bottom > 0 && e.right > 0 && e.top < window.innerHeight && e.left < window.innerWidth;
}
function g(e) {
	let t = _(e), n = T(t);
	!t.every((e) => p(e, n)) && t.length > 0 && t[0].scrollIntoView();
}
function _(e) {
	let t = e;
	if (!t) return [];
	let { element: n } = t;
	if (n) {
		let e = t.element;
		if (n.localName === "vaadin-popover" || n.localName === "vaadin-dialog") {
			let t = e._overlayElement.shadowRoot.querySelector("[part=\"overlay\"]");
			if (t) return [t];
		}
		if (n.localName === "vaadin-login-overlay") {
			let t = e.shadowRoot?.querySelector("vaadin-login-overlay-wrapper")?.shadowRoot?.querySelector("[part=\"card\"]");
			if (t) return [t];
		}
		return [n];
	}
	return t.children.flatMap((e) => _(e));
}
function v(e, t) {
	function n(e) {
		if (e instanceof ShadowRoot) for (let t of e.children) {
			let e = n(t);
			if (e) return e;
		}
		else if (e instanceof Element) {
			if (e.tagName.toLowerCase() === t.toLowerCase()) return e;
			for (let t of e.children) {
				let e = n(t);
				if (e) return e;
			}
		}
	}
	return n(e);
}
function y(e) {
	let { clientX: t, clientY: n } = e;
	return t === 0 && n === 0 || !C(document.documentElement.getBoundingClientRect(), t, n);
}
function b(e) {
	if (e.localName === "vaadin-login-overlay" || e.localName === "vaadin-dialog") return !1;
	if (e.localName === "vaadin-form-row") return !0;
	let t = l(e);
	return t.width === 0 || t.height === 0;
}
function x(e) {
	return typeof e.close == "function";
}
function S(e) {
	if (!e) return "visible";
	let t = e.getBoundingClientRect();
	if (t.width > 0 && t.height > 0) return "visible";
	let n = e, r = "visible";
	for (; n;) {
		let t = window.getComputedStyle(n);
		if (n.hidden || t.display === "none") return n === e ? "self" : "parent";
		if (n.parentNode === null) return r;
		if (n.assignedSlot instanceof HTMLElement) n = n.assignedSlot;
		else if (n.parentNode instanceof HTMLElement) n = n.parentNode;
		else if (n.parentNode instanceof ShadowRoot) {
			let e = n.parentNode.host;
			n = e instanceof HTMLElement ? e : null;
		} else n = null;
	}
	return r;
}
var C, w, T, E, D = e((() => {
	t(), C = (e, t, n) => t >= e.left && t <= e.right && n >= e.top && n <= e.bottom, w = (e) => {
		let t = [], n = r(e);
		for (; n;) t.push(n), n = r(n);
		return t;
	}, T = (e) => {
		if (e.length === 0) return new DOMRect();
		let t = Number.MAX_VALUE, n = Number.MAX_VALUE, r = Number.MIN_VALUE, i = Number.MIN_VALUE, a = new DOMRect();
		return e.map((e) => e.getBoundingClientRect()).filter((e) => !(e.height === 0 && e.width === 0)).forEach((e) => {
			e.x < t && (t = e.x), e.y < n && (n = e.y), e.right > r && (r = e.right), e.bottom > i && (i = e.bottom);
		}), a.x = t, a.y = n, a.width = r - t, a.height = i - n, a;
	}, E = (e, t) => {
		let n = e;
		for (; !(n instanceof HTMLElement && n.localName === "copilot-main");) {
			if (!n.isConnected) return null;
			if (n.parentNode) n = n.parentNode;
			else if (n.host) n = n.host;
			else return null;
			if (n instanceof HTMLElement && n.localName === t) return n;
		}
		return null;
	};
}));
//#endregion
export { w as a, x as c, a as d, y as f, g, s as h, d as i, o as l, b as m, v as n, c as o, C as p, T as r, D as s, E as t, S as u };
