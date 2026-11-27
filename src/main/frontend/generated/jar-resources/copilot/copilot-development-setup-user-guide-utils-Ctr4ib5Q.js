import { n as e } from "./chunk-DiqZc92J.js";
import { C as t, _ as n, b as r, w as i } from "./icons-Bzfd0PEL.js";
import { l as a, o } from "./consts-BXxpRU5f.js";
import { n as s, t as c } from "./section-panel-ui-state-COWn2ulI.js";
import { n as l, r as u } from "./copilot-ui-state-Dc6l_5DA.js";
import { n as d, t as f } from "./early-project-state-LGwavSyI.js";
//#region frontend/copilot/shared/copilot-development-setup-user-guide-utils.ts
function p() {
	r("use-development-setup"), s.openPanel(y);
}
function m() {
	let e = f.jdkInfo;
	return e ? e.jrebel ? "success" : e.hotswapAgentFound ? !e.hotswapVersionOk || !e.runningWithExtendClassDef || !e.runningWitHotswap || !e.runningInJavaDebugMode ? "error" : "success" : "warning" : null;
}
function h() {
	let e = f.jdkInfo;
	return !e || m() !== "success" ? "none" : e.jrebel ? "jrebel" : e.runningWitHotswap ? "hotswap" : "none";
}
function g() {
	return l.idePluginState !== void 0 && !l.idePluginState.active ? "warning" : "success";
}
function _() {
	if (!f.jdkInfo) return { status: "success" };
	let e = m(), t = g();
	return e === "warning" ? t === "warning" ? {
		status: "warning",
		message: "IDE Plugin, Hotswap"
	} : {
		status: "warning",
		message: "Hotswap is not enabled"
	} : t === "warning" ? {
		status: "warning",
		message: "IDE Plugin is not active"
	} : e === "error" ? {
		status: "error",
		message: "Hotswap is partially enabled"
	} : { status: "success" };
}
function v() {
	i(`${o}get-dev-setup-info`, {}), window.Vaadin.copilot.eventbus.on("copilot-get-dev-setup-info-response", (e) => {
		if (e.detail.content) {
			let t = JSON.parse(e.detail.content);
			l.setIdePluginState(t.ideInfo);
		}
	});
}
var y, b = e((() => {
	u(), t(), a(), c(), n(), d(), y = "copilot-development-setup-user-guide";
}));
//#endregion
export { g as a, p as c, m as i, v as n, h as o, _ as r, b as s, y as t };
