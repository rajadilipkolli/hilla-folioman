import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type HttpRange_1 from "./HttpRange.js";
class HttpRangeModel<T extends HttpRange_1 = HttpRange_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(HttpRangeModel);
}
export default HttpRangeModel;
