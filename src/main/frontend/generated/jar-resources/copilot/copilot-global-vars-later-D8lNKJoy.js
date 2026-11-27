import { n as e } from "./chunk-DiqZc92J.js";
import { C as t, G as n, H as r, K as i, S as a, X as o, Y as s, _ as c, _t as l, a as u, at as d, it as f, n as p, o as m, ot as h, r as g, t as _, u as v, vt as y, w as b, x, yt as S } from "./icons-Bzfd0PEL.js";
import { l as C, o as w } from "./consts-BXxpRU5f.js";
import { g as T, s as E } from "./dom-utils-YgRxzPHS.js";
import { a as D, n as ee, o as te, t as O } from "./copilot-tree-impl-B3m3v0hp.js";
import { a as k, i as A } from "./copilot-ui-state-Dc6l_5DA.js";
import { i as j, n as M } from "./copilot-server-communicator-impl-DpzkXcPk.js";
import { i as N, o as P } from "./copilot-error-handler-q8B4DBC6.js";
//#region frontend/copilot/show-in-ide.ts
function F(e, t) {
	I(e) ? (x("show-in-ide", {
		attach: t ?? !1,
		goToCustomComponentFile: !0
	}), b(`${w}show-in-ide`, {
		javaClassName: e.className,
		fileName: e.absoluteFilePath
	})) : i(e) ? (x("show-in-ide", { attach: t ?? !1 }), b(`${w}show-in-ide`, {
		...r(e),
		attach: t ?? !1
	})) : (a("show-in-ide"), b(`${w}show-in-ide`, e));
}
function I(e) {
	return e === void 0 ? !1 : e.className === void 0 ? e.absoluteFilePath !== void 0 : !0;
}
function L(e) {
	if (!e.isReactComponent) return;
	let t = d(e.node);
	if (t) return t;
	let n = f(e.node);
	if (n) return n;
	let r = e.children.sort((e, t) => e.siblingIndex - t.siblingIndex).find((e) => e.isReactComponent && L(e) !== void 0);
	if (!r) throw Error(`Could not find the source of ${e.nameAndIdentifier}`);
	return d(r.node);
}
var R = e((() => {
	n(), h(), C(), k(), t(), c(), A.on("show-in-ide", (e) => {
		let t = e.detail.node;
		if (e.detail.source) {
			F(e.detail.source);
			return;
		}
		if (e.detail.javaSource) {
			F(e.detail.javaSource);
			return;
		}
		if (!t) return;
		if (t.isFlowComponent) {
			F(t.node, e.detail.attach);
			return;
		}
		let n = L(t);
		n && F(n);
	});
}));
//#endregion
//#region frontend/copilot/empty-app-initializer.ts
function z(e) {
	let t = document.createElement("div");
	document.body.innerHTML = "", document.body.appendChild(t), u(e, t);
}
function B() {
	z(v`<div class="flex flex-col gap-4 h-screen items-center justify-center">
      <vaadin-icon class="animate-spin" .svg=${_.progressActivity}></vaadin-icon>
      <h3 class="m-0">The files have been created</h3>
      <p class="m-0">Restart the server to load the new view</p>
      <p class="m-0"><small>The page will refresh automatically when the server is ready.</small></p>
    </div>`);
}
async function V() {
	let e = 1e3, t = 12e4, n = Date.now(), r = async () => {
		try {
			return (await fetch(globalThis.location.href, { method: "HEAD" })).ok;
		} catch {
			return !1;
		}
	}, i = !1;
	for (; Date.now() - n < t;) {
		if (!await r()) {
			i = !0;
			break;
		}
		await new Promise((t) => {
			setTimeout(t, e);
		});
	}
	for (; Date.now() - n < t;) {
		if (await r() && i) {
			sessionStorage.removeItem(G), globalThis.location.reload();
			return;
		}
		await new Promise((t) => {
			setTimeout(t, e);
		});
	}
}
function H(e) {
	z(v`<div class="flex flex-col gap-4 h-screen items-center justify-center">
      <vaadin-icon class="animate-spin" .svg=${_.progressActivity}></vaadin-icon>
      <h3 class="m-0">Creating your ${e === "flow" ? "Flow" : "Hilla"} view...</h3>
    </div>`), o("copilot-init-app", { framework: e }, async (e) => {
		if (e.data.success) sessionStorage.setItem(G, "true"), B(), V();
		else {
			let t = e.data.reason;
			N(t);
		}
	});
}
function U() {
	z(v`<div class="m-8">
      <h3>No views found</h3>
      <p>To get started, you can</p>
      <ul>
        <li>
          <a
            href="#"
            @click=${(e) => {
		e.preventDefault(), H("flow");
	}}
            >Create a Flow view using Copilot</a
          >
        </li>
        <li>
          Create a view manually in your IDE, see
          <a target="_blank" href="https://vaadin.com/docs/latest/tutorial">the tutorial</a>
        </li>
      </ul>
      <p>Learn more at <a target="_blank" href="https://vaadin.com/docs">https://vaadin.com/docs</a>.</p>
    </div>`);
}
function W() {
	sessionStorage.getItem(G) ? (B(), V()) : U();
}
var G, K = e((() => {
	s(), P(), p(), g(), G = "vaadin.copilot.viewCreated";
})), q, J = e((() => {
	k(), q = class {
		constructor(e) {
			this._currentTree = e;
		}
		get root() {
			return this.currentTree.root;
		}
		get allNodesFlat() {
			return this.currentTree.allNodesFlat;
		}
		getNodeOfElement(e) {
			return this.currentTree.getNodeOfElement(e);
		}
		getChildren(e) {
			return this.currentTree.getChildren(e);
		}
		hasFlowComponents() {
			return this.currentTree.hasFlowComponents();
		}
		findNodeByUuid(e) {
			return this.currentTree.findNodeByUuid(e);
		}
		getElementByNodeUuid(e) {
			return this.currentTree.getElementByNodeUuid(e);
		}
		findByTreePath(e) {
			return this.currentTree.findByTreePath(e);
		}
		get currentTree() {
			return this._currentTree;
		}
		set currentTree(e) {
			let t = this._currentTree;
			this._currentTree = e, A.emit("copilot-tree-created", {
				prev: t,
				curr: e
			});
		}
		get customComponentDataLoaded() {
			return this._currentTree.customComponentDataLoaded;
		}
	};
})), Y = e((() => {
	k(), A.on("navigate", (e) => {
		let t = new URL(e.detail.path, document.baseURI), n = new CustomEvent("vaadin-router-go", {
			detail: t,
			cancelable: !0
		});
		if (!window.dispatchEvent(n)) return;
		let r = { ...window.history.state ?? {} };
		typeof r.idx == "number" && (r.idx += 1);
		let i = `${t.pathname}${t.search}${t.hash}`;
		window.history.pushState(r, "", i), window.dispatchEvent(new PopStateEvent("popstate", { state: r }));
	});
}));
//#endregion
//#region frontend/copilot/shared/copilot-storage-utils.ts
function X(e) {
	let t = window.Vaadin.copilot.tree;
	return e.map((e) => {
		let n = null, { nodeUuid: r, treePath: i, childIndex: a } = e;
		if (r) {
			let e = t.findNodeByUuid(r);
			e && (n = e);
		}
		return n ||= t.findByTreePath(i) ?? null, n && a !== void 0 && n.children.length > a ? n.children[a] : n;
	}).filter((e) => e !== null);
}
var Z = e((() => {})), Q, ne = e((() => {
	g(), p(), l(), te(), Z(), E(), Q = class e {
		constructor() {
			this.drillDownComponentStack = [], y(this, { drillDownComponentStack: S.shallow });
		}
		getCustomComponentIcon(e) {
			let t = this.getIconTag(e);
			return t === void 0 ? m : _[t];
		}
		getIconTag(e) {
			let t = this.getCustomComponentInfo(e)?.type;
			if (t === "IN_PROJECT") return "thermostatCarbon";
			if (t === "EXTERNAL") return "deployedCube";
		}
		getCustomComponentInfo(t) {
			if (t.customComponentData && e.isCustomComponentInstanceInfo(t.customComponentData)) return t.customComponentData;
		}
		isCustomComponent(e) {
			return this.getCustomComponentInfo(e) !== void 0;
		}
		isVisibleAndSelectable(e) {
			if (!this.getTree().customComponentDataLoaded) return !0;
			let t = this.getActiveDrillDownContext();
			if (!e.customComponentData) return e.isReactComponent && !e.parent && e.name === "App" && !t;
			if (e.uuid === t?.uuid) return !0;
			let n = this.getActiveDrillDownData(), r = e.customComponentData;
			if (!n?.filePath) {
				if (r) return !r.childOfCustomComponent;
			} else if (e.customComponentData) return this.checkNodeIsInDrillDownContext(r, n);
			else return !1;
			return !0;
		}
		pushDrillDownContext(e) {
			this.drillDownComponentStack.length > 0 && this.drillDownComponentStack[this.drillDownComponentStack.length - 1].uuid === e.uuid || (this.drillDownComponentStack.push(e), this.persistIntoStorage(), T(e));
		}
		isDrillDownContext(e) {
			return this.getActiveDrillDownContext()?.uuid === e.uuid;
		}
		getActiveDrillDownContext() {
			if (this.drillDownComponentStack.length !== 0) return this.resolveCurrentTreeNode(this.drillDownComponentStack[this.drillDownComponentStack.length - 1]);
		}
		clearDrillDownContext() {
			this.drillDownComponentStack = [], this.persistIntoStorage();
		}
		popDrillDownContext() {
			this.filterOutNonConnectedElementsFromDrillDownContextStack(), this.drillDownComponentStack.pop(), this.persistIntoStorage();
		}
		hasParentDrillDownContext() {
			return this.drillDownComponentStack.length > 1;
		}
		getParentDrillDownContext() {
			if (this.hasParentDrillDownContext()) return this.resolveCurrentTreeNode(this.drillDownComponentStack[this.drillDownComponentStack.length - 2]);
		}
		isChildInDrillContext(e) {
			let t = e.customComponentData;
			if (!t) return !0;
			let n = this.getActiveDrillDownData();
			return n ? this.checkNodeIsInDrillDownContext(t, n) : !1;
		}
		getActiveDrillDownData() {
			let e = this.getActiveDrillDownContext();
			if (e === void 0) return;
			let t = this.getCustomComponentInfo(e);
			if (!t?.javaClassName && !t?.reactMethodName) return;
			let n = e.node;
			return {
				className: t.javaClassName,
				methodName: t.reactMethodName,
				nodeId: n.nodeId,
				uiId: n.uiId,
				filePath: t.customComponentFilePath ?? void 0
			};
		}
		checkNodeIsInDrillDownContext(e, t) {
			return e.createLocationMethodName && t.methodName ? e.createLocationMethodName === t.methodName && t.filePath === e.createLocationPath : t.filePath === e.createLocationPath && t.className === e.createdClassName;
		}
		persistIntoStorage() {
			let e = this.drillDownComponentStack.map((e) => ({
				treePath: e.path,
				nodeUuid: e.uuid
			}));
			D.saveDrillDownContextReference(e);
		}
		restoreDrillDownFromStorage() {
			let t = D.getDrillDownContextReference(), n = [];
			if (t === void 0) {
				let t = this.getTree().allNodesFlat.find((e) => e.customComponentData?.routeView);
				t?.customComponentData && e.isCustomComponentInstanceInfo(t.customComponentData) && (n = [t]);
			} else n = X(t);
			n.forEach((e) => {
				let t = this.drillDownComponentStack.findIndex((t) => t.uuid === e.uuid);
				t !== -1 && this.drillDownComponentStack.splice(t, 1), this.drillDownComponentStack.push(e);
			});
			let r = this.drillDownComponentStack.filter((e) => !!this.getTree().findNodeByUuid(e.uuid));
			r.length !== this.drillDownComponentStack.length && (this.drillDownComponentStack = r, this.persistIntoStorage()), this.filterOutNonConnectedElementsFromDrillDownContextStack();
			let i = this.getActiveDrillDownContext();
			i && T(i);
		}
		areInternalsVisible(e) {
			if (!this.getCustomComponentInfo(e)) return !0;
			let t = this.getActiveDrillDownData(), n;
			return t && t.filePath && (n = t.filePath), n ? this.checkChildrenCreateLocationToDisplayInternals(e.children, n) : !1;
		}
		checkChildrenCreateLocationToDisplayInternals(e, t) {
			for (let n of e) {
				let e = n.customComponentData;
				if (e && e.createLocationPath === t || this.checkChildrenCreateLocationToDisplayInternals(n.children, t)) return !0;
			}
			return !1;
		}
		getDescendantsCreatedInActiveDrillDownContextFlatten(t) {
			if (t.customComponentData && e.isCustomComponentInstanceInfo(t.customComponentData)) {
				let e = this.getActiveDrillDownData(), n;
				if (e && e.filePath ? n = e.filePath : this.getRouteViewPath() && (n = this.getRouteViewPath()), n) return this.getChildrenInPathFlattenRecursively(t, n);
			}
			return [];
		}
		getChildrenInPathFlattenRecursively(e, t) {
			let n = e.children, r = [];
			for (let e of n) {
				let n = e.customComponentData;
				n && n.createLocationPath === t && r.push(e), r.push(...this.getChildrenInPathFlattenRecursively(e, t));
			}
			return r;
		}
		getTree() {
			return window.Vaadin.copilot.tree;
		}
		getRouteViewPath() {
			let e = this.getTree().allNodesFlat.find((e) => e.customComponentData?.routeView === !0);
			if (e) return e.customComponentData?.createLocationPath ?? void 0;
		}
		resolveCurrentTreeNode(e) {
			return this.getTree().findNodeByUuid(e.uuid) ?? this.getTree().findByTreePath(e.path) ?? e;
		}
		filterOutNonConnectedElementsFromDrillDownContextStack() {
			this.drillDownComponentStack = this.drillDownComponentStack.filter((e) => e.element === void 0 ? !0 : e.element.isConnected);
		}
		static isCustomComponentInstanceInfo(e) {
			return "type" in e && "activeLevel" in e;
		}
	};
})), $;
//#endregion
e((() => {
	R(), M(), K(), J(), Y(), ee(), ne(), window.Vaadin.copilot.comm = j, $ = new O(), window.Vaadin.copilot.tree = new q($), window.Vaadin.copilot.customComponentHandler = new Q(), window.Vaadin.copilot.initEmptyApp = H, window.Vaadin.copilot.noRoutesInProject = W;
}))();
