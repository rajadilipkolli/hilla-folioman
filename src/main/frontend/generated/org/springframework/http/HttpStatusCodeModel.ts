import { _getPropertyModel as _getPropertyModel_1, BooleanModel as BooleanModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type HttpStatusCode_1 from "./HttpStatusCode.js";
class HttpStatusCodeModel<T extends HttpStatusCode_1 = HttpStatusCode_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(HttpStatusCodeModel);
    get error(): BooleanModel_1 {
        return this[_getPropertyModel_1]("error", (parent, key) => new BooleanModel_1(parent, key, false, { meta: { javaType: "boolean" } }));
    }
}
export default HttpStatusCodeModel;
