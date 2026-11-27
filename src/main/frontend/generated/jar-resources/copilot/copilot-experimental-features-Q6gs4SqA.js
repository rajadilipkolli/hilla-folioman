import { n as e } from "./chunk-DiqZc92J.js";
import { n as t, r as n } from "./copilot-ui-state-Dc6l_5DA.js";
import { n as r, t as i } from "./copilot-stored-machine-state-D6qB_Peh.js";
//#region frontend/copilot/shared/copilot-experimental-features.ts
var a, o, s, c, l, u, d, f, p, m = e((() => {
	i(), n(), a = (e) => t.userInfo?.copilotExperimentalFeatureFlag === !0 && r.isExperimentalFeatureEnabled(e), o = {
		id: "theme-from-image",
		name: "Theme from Image",
		description: "Generate a custom theme based on an image you provide.",
		enabled: () => a(o),
		available: () => t.appTheme === "lumo",
		requiresReload: !1
	}, s = {
		id: "ai-docs-assistant",
		name: "AI Docs Assistant",
		description: "AI-powered Vaadin documentation assistant.",
		enabled: () => a(s),
		available: () => !0,
		requiresReload: !1
	}, c = {
		id: "testbench-test-recorder",
		name: "TestBench Test Recorder",
		description: "Record user interactions to generate end-to-end Vaadin TestBench tests automatically.",
		enabled: () => a(c),
		available: () => !0,
		requiresReload: !0
	}, l = {
		id: "i18n",
		name: "Internationalization",
		description: "Edit and manage translations for your application.",
		enabled: () => a(l),
		available: () => !0,
		requiresReload: !0
	}, u = {
		id: "annotations",
		name: "Annotations",
		description: "Add and manage comments and annotations on your application views.",
		enabled: () => a(u),
		available: () => !0,
		requiresReload: !0
	}, d = {
		id: "ui-test-generator",
		name: "UI Test Generator",
		description: "Generate Playwright UI Test for your application views.",
		enabled: () => a(d),
		available: () => !0,
		requiresReload: !0
	}, f = {
		id: "all-components",
		name: "All Components",
		description: "Create and browse interactive component examples for your application.",
		enabled: () => a(f),
		available: () => !0,
		requiresReload: !0
	}, p = [
		o,
		s,
		c,
		l,
		u,
		d,
		f
	];
}));
//#endregion
export { f as n, m as r, p as t };
