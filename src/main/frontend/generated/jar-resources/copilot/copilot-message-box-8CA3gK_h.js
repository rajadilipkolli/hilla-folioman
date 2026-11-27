import { n as e } from "./chunk-DiqZc92J.js";
import { n as t, r as n, t as r, u as i } from "./icons-Bzfd0PEL.js";
//#region frontend/copilot/shared/copilot-message-box.ts
function a(e, t, n, r) {
	let { bg: a, iconClass: s, icon: c } = o[e], l = r?.icon ?? c, u = r?.iconExtraClass ? `${s} ${r.iconExtraClass}` : s;
	return i`
    <div class="${`${a} flex gap-2 pe-3 ps-2 py-2 rounded-md text-sm${n ? ` ${n}` : ""}`}">
      <vaadin-icon class="${u}" .svg="${l}"></vaadin-icon>
      ${typeof t == "string" ? i`<span class="message-box-content">${t}</span>` : t}
    </div>
  `;
}
var o, s = e((() => {
	n(), t(), o = {
		info: {
			bg: "bg-blue-3 dark:bg-blue-6",
			iconClass: "text-blue-11",
			icon: r.info
		},
		warning: {
			bg: "bg-amber-3 dark:bg-amber-6",
			iconClass: "text-amber-11",
			icon: r.warning
		},
		error: {
			bg: "bg-ruby-3 dark:bg-ruby-6",
			iconClass: "text-ruby-11",
			icon: r.error
		},
		success: {
			bg: "bg-teal-3 dark:bg-teal-6",
			iconClass: "text-teal-11",
			icon: r.check
		},
		loading: {
			bg: "bg-gray-3 dark:bg-gray-6",
			iconClass: "animate-spin",
			icon: r.progressActivity
		}
	};
}));
//#endregion
export { a as n, s as t };
