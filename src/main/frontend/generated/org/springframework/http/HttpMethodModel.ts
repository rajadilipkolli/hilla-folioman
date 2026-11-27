import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type HttpMethod_1 from "./HttpMethod.js";
class HttpMethodModel<T extends HttpMethod_1 = HttpMethod_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(HttpMethodModel);
}
export default HttpMethodModel;
