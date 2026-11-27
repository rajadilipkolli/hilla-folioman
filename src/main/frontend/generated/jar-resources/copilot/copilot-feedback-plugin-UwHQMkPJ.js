import { n as e } from "./chunk-DiqZc92J.js";
import { C as t, Ct as n, _t as r, n as i, r as a, t as o, u as s, w as c, yt as l } from "./icons-Bzfd0PEL.js";
import { l as u, o as d } from "./consts-BXxpRU5f.js";
import { a as f, c as p, d as m, i as h, l as g, n as _, o as v, r as y, s as b, t as x } from "./section-panel-ui-state-COWn2ulI.js";
import { a as S, i as C, n as w, r as T } from "./copilot-ui-state-Dc6l_5DA.js";
import { n as E, t as D } from "./copilot-message-box-8CA3gK_h.js";
import { n as O, t as k } from "./base-panel-3DcY1Auw.js";
//#region frontend/copilot/plugins/copilot-feedback/copilot-feedback-plugin.ts
var A, j, M, N, P, F, I, L, R;
//#endregion
e((() => {
	b(), O(), h(), a(), r(), D(), t(), u(), S(), i(), x(), T(), v(), A = "https://github.com/vaadin", j = "https://github.com/vaadin/copilot/issues/new", M = "?template=feature_request.md&title=%5BFEATURE%5D", N = "A short, concise description of the bug and why you consider it a bug. Any details like exceptions and logs can be helpful as well.", P = "Please provide as many details as possible, this will help us deliver a fix as soon as possible.%0AThank you!%0A%0A%23%23%23 Description of the Bug%0A%0A{description}%0A%0A%23%23%23 Expected Behavior%0A%0AA description of what you would expect to happen. (Sometimes it is clear what the expected outcome is if something does not work, other times, it is not super clear.)%0A%0A%23%23%23 Minimal Reproducible Example%0A%0AWe would appreciate the minimum code with which we can reproduce the issue.%0A%0A%23%23%23 Versions%0A{versionsInfo}", F = l({
		showForm: !0,
		submitDisabled: !1
	}), I = class extends k {
		constructor() {
			super(), this.description = "", this.types = [
				{
					label: "General feedback",
					value: "feedback",
					ghTitle: ""
				},
				{
					label: "Report a bug",
					value: "bug",
					ghTitle: "[BUG]"
				},
				{
					label: "Ask a question",
					value: "question",
					ghTitle: "[QUESTION]"
				},
				{
					label: "Share an idea",
					value: "idea",
					ghTitle: "[FEATURE]"
				}
			], this.type = this.types[0].value, this.topics = [
				{
					label: "Generic",
					value: "platform"
				},
				{
					label: "Flow",
					value: "flow"
				},
				{
					label: "Hilla",
					value: "hilla"
				},
				{
					label: "Copilot",
					value: "copilot"
				}
			], this.topic = this.topics[0].value;
		}
		connectedCallback() {
			super.connectedCallback(), this.classList.add("contents");
		}
		willUpdate(e) {
			super.willUpdate(e), this.syncFooterState();
		}
		syncFooterState() {
			let e = this.message === void 0, t = this.type === "question" && !this.email;
			(F.showForm !== e || F.submitDisabled !== t) && n(() => {
				F.showForm = e, F.submitDisabled = t;
			});
		}
		getPreferredHeight() {
			return 620;
		}
		render() {
			return s`<div class="flex flex-col gap-4 pb-4 px-4">${this.renderContent()}</div>`;
		}
		renderContent() {
			return this.message === void 0 ? s`
          ${E("info", "Your feedback means a lot to us. Whether you've encountered an issue, have a question, or have ideas to improve our platform, we'd love to hear from you. Feel free to leave your email and we'll get back to you — you can also share a code snippet to help us better understand your experience.", void 0, { icon: o.favorite })}
          <vaadin-radio-group
            label="Type"
            theme="toggle"
            .value="${this.type}"
            @value-changed=${(e) => {
				this.type = e.detail.value;
			}}>
            ${this.types.map((e) => s`<vaadin-radio-button .value="${e.value}" label="${e.label}"></vaadin-radio-button>`)}
          </vaadin-radio-group>
          <vaadin-select
            label="Topic"
            overlay-class="alwaysVisible"
            .items=${this.topics}
            .value="${this.topic}"
            .hidden=${this.type !== "feedback"}
            @value-changed=${(e) => {
				this.topic = e.detail.value;
			}}>
          </vaadin-select>
          <vaadin-text-area
            min-rows="3"
            .value="${this.description}"
            @keydown=${this.keyDown}
            @focus=${() => {
				this.descriptionField.invalid = !1, this.descriptionField.placeholder = "";
			}}
            @value-changed=${(e) => {
				this.description = e.detail.value;
			}}
            label="Your Feedback"
            placeholder="What happened, what you expected, or what you'd change..."></vaadin-text-area>
          <vaadin-email-field
            @keydown=${this.keyDown}
            @value-changed=${(e) => {
				this.email = e.detail.value;
			}}
            .required=${this.type === "question"}
            id="email"
            value="${w.userInfo?.email}"
            label="Email${this.type === "question" ? "" : " (optional)"}"></vaadin-email-field>
        ` : s`<p class="m-0">${this.message}</p>`;
		}
		createGithubIssue() {
			C.emit("system-info-with-callback", {
				callback: (e) => this.openGithub(e, this),
				notify: !1
			});
		}
		close() {
			_.closePanel("copilot-feedback-panel");
		}
		submit() {
			if (this.trackPanelEvent("use-feedback", {
				github: !1,
				type: this.type,
				topic: this.topic
			}), this.description.trim() === "") {
				this.descriptionField.invalid = !0, this.descriptionField.placeholder = "Please tell us more before sending", this.descriptionField.value = "";
				return;
			}
			let e = {
				description: this.description,
				email: this.email,
				type: this.type,
				topic: this.topic
			};
			C.emit("system-info-with-callback", {
				callback: (t) => c(`${d}feedback`, {
					...e,
					versions: t
				}),
				notify: !1
			}), this.parentNode?.style.setProperty("--section-height", "150px"), this.message = "Thank you for sharing feedback.";
		}
		keyDown(e) {
			(e.key === "Backspace" || e.key === "Delete") && e.stopPropagation();
		}
		openGithub(e, t) {
			if (this.trackPanelEvent("use-feedback", {
				github: !0,
				type: this.type,
				topic: this.topic
			}), this.type === "idea") {
				window.open(`${j}${M}`);
				return;
			}
			if (this.type === "feedback") {
				window.open(`${A}/${this.topic}/issues/new`);
				return;
			}
			let n = e ? e.replace(/\n/g, "%0A") : "Activate Copilot to include version info.", r = `${t.types.find((e) => e.value === this.type)?.ghTitle}`, i = t.description === "" ? N : t.description, a = P.replace("{description}", i).replace("{versionsInfo}", n);
			window.open(`${j}?title=${r}&body=${a}`, "_blank")?.focus();
		}
	}, f([g()], I.prototype, "description", void 0), f([g()], I.prototype, "type", void 0), f([g()], I.prototype, "topic", void 0), f([g()], I.prototype, "email", void 0), f([g()], I.prototype, "message", void 0), f([g()], I.prototype, "types", void 0), f([g()], I.prototype, "topics", void 0), f([p("vaadin-text-area")], I.prototype, "descriptionField", void 0), I = f([m("copilot-feedback-panel")], I), L = class extends y {
		createRenderRoot() {
			return this;
		}
		connectedCallback() {
			super.connectedCallback(), this.classList.add("contents");
		}
		getPanel() {
			return this.closest("vaadin-dialog")?.querySelector("copilot-feedback-panel") ?? null;
		}
		render() {
			return F.showForm ? s`
      <vaadin-button
        style="margin-inline-end: auto"
        theme="tertiary"
        @click=${() => this.getPanel()?.createGithubIssue()}>
        <vaadin-icon slot="prefix" .svg="${o.github}"></vaadin-icon>
        Create GitHub Issue
      </vaadin-button>
      <vaadin-button theme="tertiary" @click=${() => this.getPanel()?.close()}>Cancel</vaadin-button>
      <vaadin-button
        theme="primary"
        ?disabled=${F.submitDisabled}
        @click=${() => this.getPanel()?.submit()}>
        Submit
      </vaadin-button>
    ` : s`<vaadin-button @click=${() => this.getPanel()?.close()}>Close</vaadin-button>`;
		}
	}, L = f([m("copilot-feedback-footer-actions")], L), R = {
		header: "Help Us Improve!",
		tag: "copilot-feedback-panel",
		footerActionsTag: "copilot-feedback-footer-actions",
		individual: !0
	}, window.Vaadin.copilot.plugins.push({ init(e) {
		e.addPanel(R);
	} }), _.addPanel(R);
}))();
export { L as CopilotFeedbackFooterActions, I as CopilotFeedbackPanel };
