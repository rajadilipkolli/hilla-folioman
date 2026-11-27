import { n as e } from "./chunk-DiqZc92J.js";
import { $ as t, G as n, J as r, Q as i, U as a, V as o, X as s, Y as c, ct as l, dt as u, et as d, ft as f, lt as p, nt as m, ot as h, q as g, rt as _, st as v, tt as y } from "./icons-Bzfd0PEL.js";
import { h as b, l as x, m as S, o as C, s as ee, u as w } from "./dom-utils-YgRxzPHS.js";
import { a as T, i as E } from "./copilot-ui-state-Dc6l_5DA.js";
import { i as D, o as O, r as k } from "./copilot-error-handler-q8B4DBC6.js";
import { n as A, r as j } from "./copilot-notification-DGCJSVaA.js";
//#region frontend/copilot/shared/copilot-storage.ts
var M, N, P = e((() => {
	M = "copilot-conf", N = class {
		static get sessionConfiguration() {
			let e = sessionStorage.getItem(M);
			return e ? JSON.parse(e) : {};
		}
		static saveCopilotActiveMode(e, t) {
			let n = this.sessionConfiguration;
			n.activeMode = e, n.lastNonPlayMode = t, this.persist(n);
		}
		static getCopilotActiveMode() {
			return this.sessionConfiguration.activeMode;
		}
		static getCopilotLastNonPlayMode() {
			return this.sessionConfiguration.lastNonPlayMode;
		}
		static savePanelConfigurations(e) {
			let t = this.sessionConfiguration;
			t.sectionPanelState = e, this.persist(t);
		}
		static getPanelConfigurations() {
			return this.sessionConfiguration.sectionPanelState;
		}
		static persist(e) {
			sessionStorage.setItem(M, JSON.stringify(e));
		}
		static savePrompts(e) {
			let t = this.sessionConfiguration;
			t.prompts = e, this.persist(t);
		}
		static getPrompts() {
			return this.sessionConfiguration.prompts || [];
		}
		static saveCurrentSelection(e) {
			let t = this.sessionConfiguration;
			t.selection = t.selection ?? {}, t.selection && (t.selection.current = e, t.selection.location = window.location.pathname, this.persist(t));
		}
		static savePendingSelection(e) {
			let t = this.sessionConfiguration;
			t.selection = t.selection ?? {}, t.selection && (t.selection.pending = e, t.selection.location = window.location.pathname, this.persist(t));
		}
		static getCurrentSelection() {
			let e = this.sessionConfiguration.selection;
			if (e?.location === window.location.pathname) return e.current;
		}
		static getPendingSelection() {
			let e = this.sessionConfiguration.selection;
			if (e?.location === window.location.pathname) return e.pending;
		}
		static saveDrillDownContextReference(e) {
			let t = this.sessionConfiguration;
			t.drillDownContext = t.drillDownContext ?? {}, t.drillDownContext && (t.drillDownContext.location = window.location.pathname, t.drillDownContext.stack = e, this.persist(t));
		}
		static getDrillDownContextReference() {
			let e = this.sessionConfiguration;
			if (e?.drillDownContext?.location === window.location.pathname) return e.drillDownContext?.stack;
		}
		static savePanelTagsState(e, t) {
			let n = this.sessionConfiguration;
			n.openPanelTags = Array.from(e), n.switchModeClosedPanelTags = Array.from(t), this.persist(n);
		}
		static getOpenPanelTags() {
			let e = this.sessionConfiguration.openPanelTags;
			return e ? new Set(e) : /* @__PURE__ */ new Set();
		}
		static getSwitchModeClosedPanelTags() {
			let e = this.sessionConfiguration.switchModeClosedPanelTags;
			return e ? new Set(e) : /* @__PURE__ */ new Set();
		}
		static saveCustomPanelTags(e) {
			let t = this.sessionConfiguration;
			t.customPanelTags = Object.fromEntries(e), this.persist(t);
		}
		static getCustomPanelTags() {
			let e = this.sessionConfiguration.customPanelTags;
			return e ? new Map(Object.entries(e)) : /* @__PURE__ */ new Map();
		}
		static savePositionUpdatedManuallyPanelTags(e) {
			let t = this.sessionConfiguration;
			t.positionUpdatedManuallyPanelTags = Array.from(e), this.persist(t);
		}
		static getPositionUpdatedManuallyPanelTags() {
			let e = this.sessionConfiguration.positionUpdatedManuallyPanelTags;
			return e ? new Set(e) : /* @__PURE__ */ new Set();
		}
		static savePanelStackingOrder(e) {
			let t = this.sessionConfiguration;
			t.panelStackingOrder = e, this.persist(t);
		}
		static getPanelStackingOrder() {
			return this.sessionConfiguration.panelStackingOrder ?? [];
		}
		static saveToolbarPosition(e, t) {
			let n = this.sessionConfiguration;
			n.toolbarPosition = {
				right: e,
				top: t
			}, this.persist(n);
		}
		static getToolbarPosition() {
			return this.sessionConfiguration.toolbarPosition;
		}
	};
}));
//#endregion
//#region node_modules/uuid/dist/stringify.js
function F(e, t = 0) {
	return (I[e[t + 0]] + I[e[t + 1]] + I[e[t + 2]] + I[e[t + 3]] + "-" + I[e[t + 4]] + I[e[t + 5]] + "-" + I[e[t + 6]] + I[e[t + 7]] + "-" + I[e[t + 8]] + I[e[t + 9]] + "-" + I[e[t + 10]] + I[e[t + 11]] + I[e[t + 12]] + I[e[t + 13]] + I[e[t + 14]] + I[e[t + 15]]).toLowerCase();
}
var I, L = e((() => {
	I = [];
	for (let e = 0; e < 256; ++e) I.push((e + 256).toString(16).slice(1));
}));
//#endregion
//#region node_modules/uuid/dist/rng.js
function R() {
	if (!z) {
		if (typeof crypto > "u" || !crypto.getRandomValues) throw Error("crypto.getRandomValues() not supported. See https://github.com/uuidjs/uuid#getrandomvalues-not-supported");
		z = crypto.getRandomValues.bind(crypto);
	}
	return z(B);
}
var z, B, V = e((() => {
	B = new Uint8Array(16);
})), H, U, W = e((() => {
	H = typeof crypto < "u" && crypto.randomUUID && crypto.randomUUID.bind(crypto), U = { randomUUID: H };
}));
//#endregion
//#region node_modules/uuid/dist/v4.js
function G(e, t, n) {
	e ||= {};
	let r = e.random ?? e.rng?.() ?? R();
	if (r.length < 16) throw Error("Random bytes length must be >= 16");
	if (r[6] = r[6] & 15 | 64, r[8] = r[8] & 63 | 128, t) {
		if (n ||= 0, n < 0 || n + 16 > t.length) throw RangeError(`UUID byte range ${n}:${n + 15} is out of buffer bounds`);
		for (let e = 0; e < 16; ++e) t[n + e] = r[e];
		return t;
	}
	return F(r);
}
function K(e, t, n) {
	return U.randomUUID && !t && !e ? U.randomUUID() : G(e, t, n);
}
var q = e((() => {
	W(), V(), L();
})), J = e((() => {
	L(), V(), q();
}));
//#endregion
//#region frontend/copilot/copilot-tree-impl.ts
function Y(e) {
	return `${e.uiId}#${e.nodeId}`;
}
function X(e) {
	if (!e.parent) return e.name;
	let t = 0;
	for (let n = 0; n < e.siblingIndex + 1; n++) e.parent.children[n].name === e.name && t++;
	return `${e.parent.path} > ${e.name}[${t}]`;
}
function Z(e, t) {
	return t ? `${e} "${t}"` : e;
}
var Q, $, te = e((() => {
	h(), J(), n(), ee(), O(), c(), A(), f(), T(), Q = /* @__PURE__ */ new WeakMap(), $ = class {
		constructor() {
			this.root = null, this.nodeUuidNodeMapFlat = /* @__PURE__ */ new Map(), this.aborted = !1, this._hasFlowComponent = !1, this.flowNodesInSource = {}, this.flowNodeLanguages = {}, this.flowCustomComponentData = {}, this.hillaCustomComponentData = {}, this.componentDragDropApiInfosMap = {}, this.waitForHillaCustomComponentResponseData = () => new Promise((e) => {
				let t = setTimeout(() => {
					console.warn("Timed out waiting for hilla custom component data; continuing without it"), e({});
				}, 1e4);
				E.emit("request-hilla-custom-component-data-with-callback", {
					tree: this,
					callback: (n) => {
						clearTimeout(t), e(n);
					}
				});
			});
		}
		async init() {
			let e = t();
			if (e) {
				let t = await this.addToTree(e);
				t && this.root?.abstractRootNode && this.root.children.length === 1 && (this.root = this.root.children[0]), t && (await this.addOverlayContentToTreeIfExists("vaadin-popover[opened]"), await this.addOverlayContentToTreeIfExists("vaadin-dialog[opened]")), this.hillaCustomComponentData = await this.waitForHillaCustomComponentResponseData();
			}
		}
		getChildren(e) {
			return this.nodeUuidNodeMapFlat.get(e)?.children ?? [];
		}
		get allNodesFlat() {
			return Array.from(this.nodeUuidNodeMapFlat.values());
		}
		getNodeOfElement(e) {
			if (e) return this.allNodesFlat.find((t) => t.element === e);
		}
		async handleRouteContainers(e, t) {
			let n = x(e);
			if (!n && g(e)) {
				let n = m(e);
				if (n && n.nextElementSibling) return await this.addToTree(n.nextElementSibling, t), !0;
			}
			if (n && e.localName === "react-router-outlet") {
				for (let n of Array.from(e.children)) {
					let e = _(n);
					e && await this.addToTree(e, t);
				}
				return !0;
			}
			return !1;
		}
		includeReactNode(e) {
			return y(e) === "PreconfiguredAuthProvider" || y(e) === "RouterProvider" ? !1 : p(e) || l(e);
		}
		async includeFlowNode(e) {
			if (r(e)) return !1;
			let t = o(e);
			return t && this.nodeUuidNodeMapFlat.has(Y(t)) ? !1 : this.isInitializedInProjectSources(e);
		}
		async isInitializedInProjectSources(e) {
			let t = o(e);
			if (!t) return !1;
			let { nodeId: n, uiId: r } = t;
			if (!this.flowNodesInSource[r]) {
				let e = await s("copilot-get-component-source-info", { uiId: r }, (e) => e.data);
				e.error && D("Failed to get component source info", e.error), e.suggestRestart && j({
					type: u.WARNING,
					message: "Route view deleted",
					details: k(),
					dismissId: "view-deleted-restart-required",
					delay: 3e4
				}), this.flowCustomComponentData[r] = e.customComponentResponse, this.flowNodesInSource[r] = new Set(e.nodeIdsInProject), this.flowNodeLanguages[r] = e.nodeSourceLanguages ?? {}, this.componentDragDropApiInfosMap[r] = e.dragDropApiInfos;
			}
			return this.flowNodesInSource[r].has(n);
		}
		async addToTree(e, t) {
			if (this.isAborted()) return !1;
			let n = await this.handleRouteContainers(e, t);
			if (n) return n;
			let r = x(e), i;
			if (!r) this.includeReactNode(e) && (i = this.generateNodeFromFiber(e, t));
			else if (await this.includeFlowNode(e)) {
				let n = this.generateNodeFromFlow(e, t);
				if (!n) return !1;
				this._hasFlowComponent = !0, i = n;
			}
			if (t) i && (i.parent = t, t.children ||= [], t.children.push(i));
			else {
				if (!i) {
					if (!(e instanceof Element) && v(e)) return j({
						type: u.WARNING,
						message: "Copilot is partly usable",
						details: `${y(e)} should be a function component to make Copilot work properly`,
						dismissId: "react_route_component_is_class"
					}), !1;
					if (i = r ? this.generateNodeFromFlow(e) : this.generateNodeFromFiber(e), !i) return D("Unable to add node", /* @__PURE__ */ Error("Tree root node is undefined")), !1;
					i.abstractRootNode = !0;
				}
				this.root = i;
			}
			i && this.nodeUuidNodeMapFlat.set(i.uuid, i);
			let a = i ?? t, o = r ? Array.from(e.children) : d(e);
			for (let e of o) await this.addToTree(e, a);
			return i !== void 0;
		}
		generateNodeFromFiber(e, t) {
			let n = p(e) ? m(e) : void 0, r = t?.children.length ?? 0, i = this, a;
			if (!(n && (a = w(n), a === "parent"))) return {
				node: e,
				parent: t,
				element: n,
				depth: t && t.depth + 1 || 0,
				children: [],
				siblingIndex: r,
				isFlowComponent: !1,
				isReactComponent: !0,
				isLitTemplate: !1,
				zeroSize: n ? S(n) : void 0,
				get uuid() {
					if (Q.has(e)) return Q.get(e);
					if (e.alternate && Q.has(e.alternate)) return Q.get(e.alternate);
					let t = K();
					return Q.set(e, t), t;
				},
				get name() {
					return b(y(e));
				},
				get identifier() {
					return C(n);
				},
				get nameAndIdentifier() {
					return Z(this.name, this.identifier);
				},
				get previousSibling() {
					if (r !== 0) return t?.children[r - 1];
				},
				get nextSibling() {
					if (!(t === void 0 || r === t.children.length - 1)) return t.children[r + 1];
				},
				get path() {
					return X(this);
				},
				get customComponentData() {
					if (i.hillaCustomComponentData[this.uuid]) return i.hillaCustomComponentData[this.uuid];
				},
				get selfHiddenElement() {
					return a === "self";
				}
			};
		}
		generateNodeFromFlow(e, t) {
			let n = o(e);
			if (!n) return;
			let r = w(e);
			if (!n.hiddenByServer && r === "parent") return;
			let i = t?.children.length ?? 0, s = this.flowCustomComponentData, c = this.componentDragDropApiInfosMap, l = this.flowNodeLanguages;
			return {
				node: n,
				get sourceLanguage() {
					return l[n.uiId]?.[n.nodeId];
				},
				parent: t,
				element: e,
				depth: t && t.depth + 1 || 0,
				children: [],
				siblingIndex: i,
				get uuid() {
					return Y(n);
				},
				isFlowComponent: !0,
				isReactComponent: !1,
				get isLitTemplate() {
					return !!this.customComponentData?.litTemplate;
				},
				zeroSize: e ? S(e) : void 0,
				get name() {
					return a(n) ?? b(n.element.localName);
				},
				get identifier() {
					return C(e);
				},
				get nameAndIdentifier() {
					return Z(this.name, this.identifier);
				},
				get previousSibling() {
					if (i !== 0) return t?.children[i - 1];
				},
				get nextSibling() {
					if (!(t === void 0 || i === t.children.length - 1)) return t.children[i + 1];
				},
				get path() {
					return X(this);
				},
				get customComponentData() {
					if (s[n.uiId]) return s[n.uiId].allComponentsInfoForCustomComponentSupport[n.nodeId];
				},
				get componentDragDropApiInfo() {
					if (!c[n.uiId]) return;
					let e = c[n.uiId];
					if (e[n.nodeId]) return e[n.nodeId];
				},
				get selfHiddenElement() {
					return r === "self";
				}
			};
		}
		async addOverlayContentToTreeIfExists(e) {
			let t = document.body.querySelector(e);
			if (!t) return;
			let n = !0;
			if (!this.getNodeOfElement(t)) {
				let e = i(_(t));
				n = await this.addToTree(e ?? t, this.root);
			}
			if (n) for (let e of Array.from(t.children)) await this.addToTree(e, this.getNodeOfElement(t));
		}
		hasFlowComponents() {
			return this._hasFlowComponent;
		}
		findNodeByUuid(e) {
			if (e) return this.nodeUuidNodeMapFlat.get(e);
		}
		getElementByNodeUuid(e) {
			return this.findNodeByUuid(e)?.element;
		}
		findByTreePath(e) {
			if (e) return this.allNodesFlat.find((t) => t.path === e);
		}
		isAborted() {
			return this.aborted;
		}
		abort() {
			this.aborted = !0;
		}
		get customComponentDataLoaded() {
			return Object.keys(this.hillaCustomComponentData).length !== 0 || Object.keys(this.flowCustomComponentData).length !== 0;
		}
	};
}));
//#endregion
export { N as a, K as i, te as n, P as o, J as r, $ as t };
