import { n as e } from "./chunk-DiqZc92J.js";
//#region frontend/copilot/shared/copilot-eventbus.ts
var t, n = e((() => {
	if (t = window.Vaadin.copilot.eventbus, !t) throw Error("Tried to access copilot eventbus before it was initialized.");
})), r, i, a = e((() => {
	if (r = {
		AddEventListener: "Add Event Listener",
		AI: "AI",
		Delete: "Delete",
		DragAndDrop: "Drag and Drop",
		Duplicate: "Duplicate",
		SetLabel: "Set label",
		SetText: "Set text",
		SetHelper: "Set helper text",
		SetTitle: "Set title text",
		WrapWithTag: "Wrapping with tag",
		Alignment: "Alignment",
		Padding: "Padding",
		ModifyComponentSource: "Modify component source",
		Gap: "Gap",
		RedoUndo: "Redo/Undo",
		Sizing: "Sizing",
		ConnectToService: "ConnectToService",
		SetStaticData: "SetStaticData",
		ExtractComponent: "ExtractComponent",
		SetViewAccessRequirement: "SetViewAccessRequirement"
	}, i = window.Vaadin.copilot._uiState, !i) throw Error("Tried to access copilot ui state before it was initialized.");
}));
//#endregion
export { n as a, t as i, i as n, a as r, r as t };
