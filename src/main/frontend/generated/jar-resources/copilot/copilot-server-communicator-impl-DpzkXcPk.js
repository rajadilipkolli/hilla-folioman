import { n as e } from "./chunk-DiqZc92J.js";
import { C as t, w as n } from "./icons-Bzfd0PEL.js";
import { i as r, r as i } from "./copilot-tree-impl-B3m3v0hp.js";
import { a, i as o } from "./copilot-ui-state-Dc6l_5DA.js";
//#region frontend/copilot/copilot-server-communicator-impl.ts
function s(e) {
	for (let t of d) if (t.handleMessage(e)) return d.splice(d.indexOf(t), 1), !0;
	if (o.emitUnsafe({
		type: e.command,
		data: e.data
	})) return !0;
	for (let t of u()) if (c(t, e)) return !0;
	return f.push(e), !1;
}
function c(e, t) {
	return e.handleMessage?.call(e, t);
}
function l() {
	if (f.length) for (let e of u()) for (let t = 0; t < f.length; t++) c(e, f[t]) && (f.splice(t, 1), t--);
}
function u() {
	let e = document.querySelector("copilot-main");
	if (!e) return [];
	let t = [];
	return Array.from(e.shadowRoot.querySelectorAll("copilot-panel-manager vaadin-dialog[panel-container]")).forEach((e) => {
		let n = e.dataset.panelTag;
		if (n) {
			let r = e.querySelector(n);
			r && t.push(r);
		}
	}), t;
}
var d, f, p, m = e((() => {
	i(), a(), t(), d = [], f = [], p = async (e, t, i) => {
		let a, o;
		t.reqId = r();
		let s = new Promise((e, t) => {
			a = e, o = t;
		});
		return d.push({ handleMessage(e) {
			if (e?.data?.reqId !== t.reqId) return !1;
			try {
				a(i(e));
			} catch (e) {
				o(e);
			}
			return !0;
		} }), n(e, t), s;
	};
}));
//#endregion
export { p as i, m as n, l as r, s as t };
