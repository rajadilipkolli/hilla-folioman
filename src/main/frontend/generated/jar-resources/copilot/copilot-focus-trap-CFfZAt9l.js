import { n as e } from "./chunk-DiqZc92J.js";
import { d as t, s as n } from "./dom-utils-YgRxzPHS.js";
//#region frontend/copilot/copilot-focus-trap.ts
function r() {
	return document.body.querySelector("copilot-main");
}
var i, a;
//#endregion
e((() => {
	n(), i = class {
		constructor() {
			this.active = !1, this.activate = () => {
				this.active = !0;
				let e = this.getApplicationRootElement();
				e && e instanceof HTMLElement && e.addEventListener("focusin", this.focusInEventListener), r()?.focus(), r()?.addEventListener("focusout", this.keepFocusInCopilot);
			}, this.deactivate = () => {
				this.active = !1;
				let e = this.getApplicationRootElement();
				e && e instanceof HTMLElement && e.removeEventListener("focusin", this.focusInEventListener), r()?.removeEventListener("focusout", this.keepFocusInCopilot);
			}, this.focusInEventListener = (e) => {
				this.active && (e.preventDefault(), e.stopPropagation(), t(e.target) || requestAnimationFrame(() => {
					e.target.blur && e.target.blur(), r()?.focus();
				}));
			};
		}
		getApplicationRootElement() {
			return document.body.firstElementChild;
		}
		keepFocusInCopilot(e) {
			e.preventDefault(), e.stopPropagation(), r()?.focus();
		}
	}, a = new i();
}))();
export { a as copilotFocusTrap };
