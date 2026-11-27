import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type HttpEntity_1 from "./HttpEntity.js";
import HttpHeadersModel_1 from "./HttpHeadersModel.js";
class HttpEntityModel<T extends HttpEntity_1 = HttpEntity_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(HttpEntityModel);
    get headers(): HttpHeadersModel_1 {
        return this[_getPropertyModel_1]("headers", (parent, key) => new HttpHeadersModel_1(parent, key, true));
    }
    get body(): ObjectModel_1 {
        return this[_getPropertyModel_1]("body", (parent, key) => new ObjectModel_1(parent, key, true));
    }
}
export default HttpEntityModel;
