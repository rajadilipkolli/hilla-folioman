import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1 } from "@vaadin/hilla-lit-form";
import HttpEntityModel_1 from "./HttpEntityModel.js";
import HttpStatusCodeModel_1 from "./HttpStatusCodeModel.js";
import type ResponseEntity_1 from "./ResponseEntity.js";
class ResponseEntityModel<T extends ResponseEntity_1 = ResponseEntity_1> extends HttpEntityModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(ResponseEntityModel);
    get statusCode(): HttpStatusCodeModel_1 {
        return this[_getPropertyModel_1]("statusCode", (parent, key) => new HttpStatusCodeModel_1(parent, key, true));
    }
}
export default ResponseEntityModel;
