import { n as e, t } from "./chunk-DiqZc92J.js";
import { l as n, s as r } from "./icons-Bzfd0PEL.js";
import { i, n as a, r as o, t as s } from "./directive-DWLihZIi.js";
import { a as c, i as l, o as u, r as d, s as f, t as p } from "./directive-helpers-4Zf_8o2L.js";
//#region node_modules/lit-html/directives/repeat.js
var m, h, g = e((() => {
	n(), o(), d(), m = (e, t, n) => {
		let r = /* @__PURE__ */ new Map();
		for (let i = t; i <= n; i++) r.set(e[i], i);
		return r;
	}, h = s(class extends a {
		constructor(e) {
			if (super(e), e.type !== i.CHILD) throw Error("repeat() can only be used in text expressions");
		}
		dt(e, t, n) {
			let r;
			n === void 0 ? n = t : t !== void 0 && (r = t);
			let i = [], a = [], o = 0;
			for (let t of e) i[o] = r ? r(t, o) : o, a[o] = n(t, o), o++;
			return {
				values: a,
				keys: i
			};
		}
		render(e, t, n) {
			return this.dt(e, t, n).values;
		}
		update(e, [t, n, i]) {
			let a = c(e), { values: o, keys: s } = this.dt(t, n, i);
			if (!Array.isArray(a)) return this.ut = s, o;
			let d = this.ut ??= [], h = [], g, _, v = 0, y = a.length - 1, b = 0, x = o.length - 1;
			for (; v <= y && b <= x;) if (a[v] === null) v++;
			else if (a[y] === null) y--;
			else if (d[v] === s[b]) h[b] = f(a[v], o[b]), v++, b++;
			else if (d[y] === s[x]) h[x] = f(a[y], o[x]), y--, x--;
			else if (d[v] === s[x]) h[x] = f(a[v], o[x]), u(e, h[x + 1], a[v]), v++, x--;
			else if (d[y] === s[b]) h[b] = f(a[y], o[b]), u(e, a[v], a[y]), y--, b++;
			else if (g === void 0 && (g = m(s, b, x), _ = m(d, v, y)), g.has(d[v])) if (g.has(d[y])) {
				let t = _.get(s[b]), n = t === void 0 ? null : a[t];
				if (n === null) {
					let t = u(e, a[v]);
					f(t, o[b]), h[b] = t;
				} else h[b] = f(n, o[b]), u(e, a[v], n), a[t] = null;
				b++;
			} else p(a[y]), y--;
			else p(a[v]), v++;
			for (; b <= x;) {
				let t = u(e, h[x + 1]);
				f(t, o[b]), h[b++] = t;
			}
			for (; v <= y;) {
				let e = a[v++];
				e !== null && p(e);
			}
			return this.ut = s, l(e, h), r;
		}
	});
})), _ = e((() => {
	g();
})), v = /* @__PURE__ */ t(((e, t) => {
	t.exports = function() {
		var e = document.getSelection();
		if (!e.rangeCount) return function() {};
		for (var t = document.activeElement, n = [], r = 0; r < e.rangeCount; r++) n.push(e.getRangeAt(r));
		switch (t.tagName.toUpperCase()) {
			case "INPUT":
			case "TEXTAREA":
				t.blur();
				break;
			default:
				t = null;
				break;
		}
		return e.removeAllRanges(), function() {
			e.type === "Caret" && e.removeAllRanges(), e.rangeCount || n.forEach(function(t) {
				e.addRange(t);
			}), t && t.focus();
		};
	};
})), y = /* @__PURE__ */ t(((e, t) => {
	var n = v(), r = {
		"text/plain": "Text",
		"text/html": "Url",
		default: "Text"
	}, i = "Copy to clipboard: #{key}, Enter";
	function a(e) {
		var t = (/mac os x/i.test(navigator.userAgent) ? "⌘" : "Ctrl") + "+C";
		return e.replace(/#{\s*key\s*}/g, t);
	}
	function o(e, t) {
		var o, s, c, l, u, d, f = !1;
		t ||= {}, o = t.debug || !1;
		try {
			if (c = n(), l = document.createRange(), u = document.getSelection(), d = document.createElement("span"), d.textContent = e, d.ariaHidden = "true", d.style.all = "unset", d.style.position = "fixed", d.style.top = 0, d.style.clip = "rect(0, 0, 0, 0)", d.style.whiteSpace = "pre", d.style.webkitUserSelect = "text", d.style.MozUserSelect = "text", d.style.msUserSelect = "text", d.style.userSelect = "text", d.addEventListener("copy", function(n) {
				if (n.stopPropagation(), t.format) if (n.preventDefault(), n.clipboardData === void 0) {
					o && console.warn("unable to use e.clipboardData"), o && console.warn("trying IE specific stuff"), window.clipboardData.clearData();
					var i = r[t.format] || r.default;
					window.clipboardData.setData(i, e);
				} else n.clipboardData.clearData(), n.clipboardData.setData(t.format, e);
				t.onCopy && (n.preventDefault(), t.onCopy(n.clipboardData));
			}), document.body.appendChild(d), l.selectNodeContents(d), u.addRange(l), !document.execCommand("copy")) throw Error("copy command was unsuccessful");
			f = !0;
		} catch (n) {
			o && console.error("unable to copy using execCommand: ", n), o && console.warn("trying IE specific stuff");
			try {
				window.clipboardData.setData(t.format || "text", e), t.onCopy && t.onCopy(window.clipboardData), f = !0;
			} catch (n) {
				o && console.error("unable to copy using clipboardData: ", n), o && console.error("falling back to prompt"), s = a("message" in t ? t.message : i), window.prompt(s, e);
			}
		} finally {
			u && (typeof u.removeRange == "function" ? u.removeRange(l) : u.removeAllRanges()), d && document.body.removeChild(d), c();
		}
		return f;
	}
	t.exports = o;
}));
//#endregion
export { _ as n, h as r, y as t };
