import { n as e } from "./chunk-DiqZc92J.js";
import { Ct as t, F as n, P as r, _t as i, dt as a, ft as o, n as s, r as c, t as l, u, vt as d, z as f } from "./icons-Bzfd0PEL.js";
import { a as p, d as m, i as h, l as g, n as _, o as v, r as y, s as b, t as x } from "./section-panel-ui-state-COWn2ulI.js";
import { a as S, i as C, n as w, r as T } from "./copilot-ui-state-Dc6l_5DA.js";
import { n as E, t as D } from "./copilot-message-box-8CA3gK_h.js";
import { n as O, o as k, t as A } from "./copilot-error-handler-q8B4DBC6.js";
import { n as j, t as M } from "./base-panel-3DcY1Auw.js";
//#region frontend/copilot/copilot-time-formatter.ts
var N, P, F = e((() => {
	N = () => {
		let e = {
			hour: "numeric",
			minute: "numeric",
			second: "numeric",
			fractionalSecondDigits: 3
		}, t, n = navigator.language ?? "", r = n.indexOf("@"), i = r === -1 ? n : n.slice(0, r);
		try {
			t = new Intl.DateTimeFormat(Intl.getCanonicalLocales(i), e);
		} catch (n) {
			console.error("Failed to create date time formatter for ", i, n), t = new Intl.DateTimeFormat("en-US", e);
		}
		return t;
	}, P = N();
}));
//#endregion
//#region frontend/copilot/plugins/copilot-log/copilot-log-plugin.ts
function I(e) {
	return P.format(e);
}
var L, R, z, B, V, H, U;
//#endregion
e((() => {
	c(), b(), o(), j(), S(), T(), i(), r(), h(), s(), k(), D(), x(), F(), v(), z = class {
		constructor() {
			this.showTimestamps = !1, d(this);
		}
		toggleShowTimestamps() {
			this.showTimestamps = !this.showTimestamps;
		}
	}, B = new z(), V = (L = class extends M {
		constructor(...e) {
			super(...e), this.unreadErrors = !1, this.messages = [], this.nextMessageId = 1, this.transitionDuration = 0, this.errorHandlersAdded = !1;
		}
		connectedCallback() {
			if (super.connectedCallback(), this.classList.add("contents"), this.onCommand("log", (e) => {
				this.handleLogEventData({
					type: e.data.type,
					message: e.data.message
				});
			}), this.onEventBus("log", (e) => this.handleLogEvent(e)), this.onEventBus("update-log", (e) => this.updateLog(e.detail)), this.onEventBus("notification-shown", (e) => this.handleNotification(e)), this.onEventBus("clear-log", () => this.clear()), this.reaction(() => w.sectionPanelResizing, () => {
				this.requestUpdate();
			}), this.transitionDuration = parseInt(window.getComputedStyle(this).getPropertyValue("--dev-tools-transition-duration"), 10), !this.errorHandlersAdded) {
				let e = (e) => {
					t(() => {
						_.attentionRequiredPanelTag = "copilot-log-panel";
					}), this.log(a.ERROR, e.message, !!e.internal, e.details, e.link);
				};
				A((t) => {
					e(t);
				}), O.forEach((t) => {
					e(t);
				}), O.length = 0, this.errorHandlersAdded = !0;
			}
		}
		clear() {
			this.messages = [];
		}
		handleNotification(e) {
			this.log(e.detail.type, e.detail.message, !0, e.detail.details, e.detail.link);
		}
		handleLogEvent(e) {
			this.handleLogEventData(e.detail);
		}
		handleLogEventData(e) {
			this.log(e.type, e.message, !!e.internal, e.details, e.link, f(e.expandedMessage), f(e.expandedDetails), e.id);
		}
		activate() {
			this.unreadErrors = !1, this.updateComplete.then(() => {
				let e = this.renderRoot.querySelector(".message:last-child");
				e && e.scrollIntoView();
			});
		}
		render() {
			return u`
      ${this.messages.length === 0 ? E("info", "Communication between application and backend services, errors, and all notifications will appear here.", "mb-3 mt-0 mx-3") : u`<ul class="border-dashed divide-y list-none m-0 pb-4 px-4">
            ${this.messages.map((e) => this.renderMessage(e))}
          </ul>`}
    `;
		}
		renderMessage(e) {
			let t, n, r;
			e.type === a.ERROR ? (n = l.warning, t = "Error", r = "text-ruby-11") : e.type === a.WARNING ? (n = l.warning, t = "Warning", r = "text-amber-11") : (n = l.info, t = "Info", r = "text-blue-11");
			let i = e.expanded ? "" : "truncate";
			return u`
      <li class="flex gap-2 py-2" data-id="${e.id}">
        <vaadin-icon
          aria-label="${t}"
          class="${r}"
          id="log-icon-${e.id}"
          .svg="${n}"></vaadin-icon>
        <vaadin-tooltip for="log-icon-${e.id}" text="${t}"></vaadin-tooltip>
        <span class="flex flex-col flex-grow overflow-hidden" @click=${() => this.toggleExpanded(e)}>
          <span class="${i}" ?hidden=${!B.showTimestamps}>${I(e.timestamp)}</span>
          <span class="${i}">
            ${e.expanded && e.expandedMessage ? e.expandedMessage : e.message}
          </span>
          ${e.expanded ? u`<span id="log-details-${e.id}"
                >${e.expandedDetails ?? e.details}</span
              >` : u`<span
                class="${i}"
                id="log-details-${e.id}"
                ?hidden="${!e.details && !e.link}">
                ${f(e.details)}
                ${e.link ? u`<a class="block mb-1" href="${e.link}" target="_blank">Learn more</a>` : ""}
              </span>`}
        </span>
        <vaadin-button
          aria-controls="log-details-${e.id}"
          aria-expanded="${e.expanded}"
          aria-label="${e.expanded ? "Collapse details" : "Expand details"}"
          theme="icon tertiary"
          @click=${() => this.toggleExpanded(e)}
          ?hidden=${!this.canBeExpanded(e)}>
          <vaadin-icon
            class="${e.expanded ? "rotate-90" : ""} transition"
            .svg="${l.chevronRight}"></vaadin-icon>
          <vaadin-tooltip
            slot="tooltip"
            text="${e.expanded ? "Collapse details" : "Expand details"}"></vaadin-tooltip>
        </vaadin-button>
      </li>
    `;
		}
		log(e, t, n, r, i, o, s, c) {
			let l = this.nextMessageId;
			this.nextMessageId += 1, s ||= t;
			let u = {
				id: l,
				type: e,
				message: t,
				details: r,
				link: i,
				dontShowAgain: !1,
				deleted: !1,
				expanded: !1,
				expandedMessage: o,
				expandedDetails: s,
				timestamp: /* @__PURE__ */ new Date(),
				internal: n,
				userId: c
			};
			for (this.messages.push(u); this.messages.length > R.MAX_LOG_ROWS;) this.messages.shift();
			return this.requestUpdate(), this.updateComplete.then(() => {
				let t = this.renderRoot.querySelector(".message:last-child");
				t ? (setTimeout(() => t.scrollIntoView({ behavior: "smooth" }), this.transitionDuration), this.unreadErrors = !1) : e === a.ERROR && (this.unreadErrors = !0);
			}), u;
		}
		updateLog(e) {
			let t = this.messages.find((t) => t.userId === e.id);
			t ||= this.log(a.INFORMATION, "<Log message to update was not found>", !1), Object.assign(t, e), n(t.expandedDetails) && (t.expandedDetails = f(t.expandedDetails)), this.requestUpdate();
		}
		updated() {
			let e = this.querySelector(".row:last-child");
			e && this.isTooLong(e.querySelector(".firstrowmessage")) && e.querySelector("button.expand")?.removeAttribute("hidden");
		}
		toggleExpanded(e) {
			this.canBeExpanded(e) && (e.expanded = !e.expanded, this.requestUpdate()), this.trackPanelEvent("use-log", { source: "toggleExpanded" });
		}
		canBeExpanded(e) {
			if (e.expandedMessage || e.expanded) return !0;
			let t = this.querySelector(`[data\\-id="${e.id}"]`)?.querySelector(".firstrowmessage");
			return this.isTooLong(t);
		}
		isTooLong(e) {
			return e && e.offsetWidth < e.scrollWidth;
		}
	}, R = L, L.MAX_LOG_ROWS = 1e3, L), p([g()], V.prototype, "unreadErrors", void 0), p([g()], V.prototype, "messages", void 0), V = R = p([m("copilot-log-panel")], V), H = class extends y {
		createRenderRoot() {
			return this;
		}
		render() {
			return u`
      <style>
        copilot-log-panel-actions {
          display: contents;
        }
      </style>
      <vaadin-button
        aria-label="Clear log"
        @click=${() => {
				C.emit("clear-log", {});
			}}
        theme="icon tertiary">
        <vaadin-icon .svg="${l.delete}"></vaadin-icon>
        <vaadin-tooltip slot="tooltip" text="Clear log"></vaadin-tooltip>
      </vaadin-button>
      <vaadin-button
        aria-label="Toggle timestamps"
        @click=${() => {
				B.toggleShowTimestamps();
			}}
        theme="icon tertiary">
        <vaadin-icon .svg="${B.showTimestamps ? l.schedule : l.historyToggleOff}"></vaadin-icon>
        <vaadin-tooltip slot="tooltip" text="Toggle timestamps"></vaadin-tooltip>
      </vaadin-button>
    `;
		}
	}, H = p([m("copilot-log-panel-actions")], H), U = {
		header: "Log",
		tag: "copilot-log-panel",
		actionsTag: "copilot-log-panel-actions",
		individual: !0,
		toolbarOptions: {
			allowedModesWithOrder: { common: 0 },
			iconKey: "terminal"
		}
	}, window.Vaadin.copilot.plugins.push({ init(e) {
		e.addPanel(U);
	} }), _.addPanel(U);
}))();
export { H as Actions, V as CopilotLogPanel };
