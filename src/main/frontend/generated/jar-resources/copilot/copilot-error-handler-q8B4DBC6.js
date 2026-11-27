import { n as e } from "./chunk-DiqZc92J.js";
import { A as t, L as n, P as r, X as i, Y as a, _ as o, _t as s, dt as c, ft as l, l as u, n as d, o as f, r as p, s as m, t as h, u as g, xt as ee } from "./icons-Bzfd0PEL.js";
import { l as te, o as ne } from "./consts-BXxpRU5f.js";
import { a as _, i as v, n as y, r as b } from "./copilot-ui-state-Dc6l_5DA.js";
import { i as x, n as S, r as re, t as ie } from "./directive-DWLihZIi.js";
import { n as ae, t as C } from "./copilot-stored-machine-state-D6qB_Peh.js";
import { n as w } from "./copilot-notification-DGCJSVaA.js";
import { n as T, t as E } from "./early-project-state-LGwavSyI.js";
//#region node_modules/lit-html/directives/unsafe-html.js
var D, O, oe = e((() => {
	u(), re(), D = class extends S {
		constructor(e) {
			if (super(e), this.it = f, e.type !== x.CHILD) throw Error(this.constructor.directiveName + "() can only be used in child bindings");
		}
		render(e) {
			if (e === f || e == null) return this._t = void 0, this.it = e;
			if (e === m) return e;
			if (typeof e != "string") throw Error(this.constructor.directiveName + "() called with a non-string value");
			if (e === this.it) return this._t;
			this.it = e;
			let t = [e];
			return t.raw = t, this._t = {
				_$litType$: this.constructor.resultType,
				strings: t,
				values: []
			};
		}
	}, D.directiveName = "unsafeHTML", D.resultType = 1, O = ie(D);
})), k = e((() => {
	oe();
}));
//#endregion
//#region frontend/copilot/shared/copilot-userinfo-util.ts
function se() {
	let e = y.userInfo;
	return !e || e.copilotProjectCannotLeaveLocalhost ? !1 : ae.isSendErrorReportsAllowed();
}
var ce = e((() => {
	o(), b(), $(), C(), r(), w(), l();
}));
//#endregion
//#region frontend/copilot/shared/hotswap-utils.ts
function A() {
	return y.idePluginState?.supportedActions?.find((e) => e === "restartApplication");
}
function j() {
	i(`${ne}plugin-restart-application`, {}, () => {}).catch((e) => {
		I("Error restarting server", e);
	});
}
function le() {
	return E.jdkInfo?.jrebel || E.jdkInfo?.runningWitHotswap && E.jdkInfo?.runningWithExtendClassDef;
}
var M = e((() => {
	a(), te(), $(), _(), w(), l(), b(), T();
}));
//#endregion
//#region frontend/copilot/shared/copilot-error-handler.ts
function N(e) {
	if (e === void 0) return !1;
	let t = Object.keys(e);
	return t.length === 1 && t.includes("message") || t.length >= 3 && t.includes("message") && t.includes("exceptionMessage") && t.includes("exceptionStacktrace");
}
function P() {
	let e = "A server restart is required";
	return A() ? t(g`${e}${F()}`) : t(g`${e}`);
}
function F() {
	return A() ? g`<vaadin-button
      class="mt-2"
      theme="primary"
      @click=${(e) => {
		let t = e.target;
		t.disabled = !0, t.innerText = "Restarting...", j();
	}}>
      Restart Now
    </vaadin-button>` : f;
}
function I(e, r) {
	let i = r;
	if (i && Y(i)) {
		X(i);
		return;
	}
	let a = N(r) ? r.exceptionMessage ?? r.message : r, o = {
		type: c.ERROR,
		message: "Copilot internal error",
		details: e + (a ? `\n${a}` : "")
	};
	N(r) && r.suggestRestart && A() && (o.details = t(g`${e}<br />${a} ${F()}`), o.delay = 3e4), n(o);
	let s;
	s = r instanceof Error ? r.stack : N(r) ? r?.exceptionStacktrace?.join("\n") : r?.toString(), v.emit("system-info-with-callback", {
		callback: (t) => v.send("copilot-error", {
			message: `Copilot internal error: ${e}`,
			details: s,
			versions: t
		}),
		notify: !1
	});
}
function L(e) {
	return e?.stack?.includes("cdn.vaadin.com/copilot") || e?.stack?.includes("/copilot/copilot/") || e?.stack?.includes("/copilot/copilot-private/");
}
function R() {
	let e = window.onerror;
	window.onerror = (t, n, r, i, a) => {
		if (L(a)) {
			I(t.toString(), a);
			return;
		}
		e && e(t, n, r, i, a);
	}, ee((e) => {
		L(e) && I("", e);
	});
	let t = window.Vaadin.ConsoleErrors;
	if (Array.isArray(t)) for (let e of t) Array.isArray(e) ? Q.push(...e) : Q.push(e);
	B((e) => Q.push(e));
}
function z(e, t, n, r, i, a) {
	let o = { ...e }, s = window.Vaadin.copilot.tree, c = window.Vaadin.copilot.customComponentHandler;
	o.nodes.forEach((e) => {
		e.node = s.allNodesFlat.find((t) => {
			if (!t.isFlowComponent) return !1;
			let n = t.node;
			return n.uiId === e.uiId && n.nodeId === e.nodeId;
		});
	});
	let l = [];
	n && l.push(`Error Message -> ${n}`), r && l.push(`Error Details -> ${r}`), l.push(`Active Level -> ${c.getActiveDrillDownContext() ? c.getActiveDrillDownContext()?.nameAndIdentifier : "No active level"}`), o.nodes.length > 0 && (l.push("\nRelevant Nodes:"), o.nodes.forEach((e) => {
		l.push(`${e.relevance} -> ${e.node?.nameAndIdentifier ?? "Node not found"}`);
	})), o.relevantPairs.length > 0 && (l.push("\nAdditional Info:"), o.relevantPairs.forEach((e) => {
		l.push(`${e.relevance} -> ${e.value}`);
	})), a && (l.push("Versions"), l.push(a));
	let u = {
		name: "Info",
		content: l.join("\n")
	};
	o.items.unshift(u), i && o.items.push({
		name: "Stacktrace",
		content: i
	}), v.emit("system-info-with-callback", {
		callback: (e) => {
			o.items.push({
				name: "Versions",
				content: e
			}), t(o);
		},
		notify: !1
	});
}
function B(e) {
	let t = window.Vaadin.ConsoleErrors;
	window.Vaadin.ConsoleErrors = { push: (n) => {
		n[0] === null || n[0] === void 0 || (n[0].type !== void 0 && n[0].message !== void 0 ? e({
			type: n[0].type,
			message: n[0].message,
			internal: !!n[0].internal,
			details: n[0].details,
			link: n[0].link
		}) : e({
			type: c.ERROR,
			message: n.map((e) => V(e)).join(" "),
			internal: !1
		}), t.push(n));
	} };
}
function V(e) {
	return e.message ? e.message.toString() : e.toString();
}
var H, U, W, G, K, q, J, Y, X, Z, Q, $ = e((() => {
	p(), k(), s(), _(), b(), ce(), M(), r(), l(), d(), H = (e, t) => e.error ? (Z(e.error, t), !0) : !1, U = (e, r, i) => {
		n({
			type: c.ERROR,
			message: e,
			details: t(g`${W(r)} ${K(i)}`),
			delay: 3e4
		});
	}, W = (e) => e.length === 0 ? f : e.length < 80 ? G(e) : g`<vaadin-details class="flex flex-col peer w-full" theme="no-padding reverse">
    <vaadin-details-summary class="font-medium -ms-3 self-start text-secondary text-xs" slot="summary"
      >Details</vaadin-details-summary
    >
    ${G(e)}
  </vaadin-details>`, G = (e) => g`<code
    class="bg-gray-3 dark:bg-gray-6 box-border inline-block mb-3 mt-2 pe-8 ps-3 py-1.75 relative rounded-md text-xs truncate w-full"
    >${O(e)}<copilot-copy></copilot-copy
  ></code>`, K = (e) => e ? g`
    <vaadin-button
      class="peer-has-[[opened]]:mt-2"
      @click="${() => {
		e && v.emit("submit-exception-report-clicked", e);
	}}"
      id="report-issue">
      <vaadin-icon slot="prefix" .svg="${h.bugReport}"></vaadin-icon>
      Report Issue</vaadin-button
    >
  ` : f, q = (e, t, n, r, i) => {
		i ? z(i, (n) => {
			U(e, t, n);
		}, e, t, n) : U(e, t), se() && (r?.templateData && typeof r.templateData == "string" && r.templateData.startsWith("data") && (r.templateData = "<IMAGE_DATA>"), v.emit("system-info-with-callback", {
			callback: (t) => v.send("copilot-error", {
				message: e,
				details: String(n).replace("	", "\n") + (r ? `\n \nRequest: \n${JSON.stringify(r)}\n` : ""),
				versions: t
			}),
			notify: !1
		})), y.clearOperationWaitsHmrUpdate();
	}, J = ["unsupported-source-language", "mixed-language-not-supported"], Y = (e) => !!e?.code && J.includes(e.code), X = (e) => {
		n({
			type: c.WARNING,
			message: e.exceptionMessage?.trim() || e.message
		}), y.clearOperationWaitsHmrUpdate();
	}, Z = (e, t) => {
		if (Y(e)) {
			X(e);
			return;
		}
		q(e.message, e.exceptionMessage ?? "", e.exceptionStacktrace?.join("\n") ?? "", t, e.exceptionReport);
	}, Q = [];
}));
//#endregion
export { H as a, M as c, le as d, k as f, I as i, j as l, Q as n, $ as o, O as p, P as r, R as s, B as t, A as u };
