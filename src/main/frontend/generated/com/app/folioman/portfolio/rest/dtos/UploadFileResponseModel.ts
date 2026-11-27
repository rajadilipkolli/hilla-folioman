import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type UploadFileResponse_1 from "./UploadFileResponse.js";
class UploadFileResponseModel<T extends UploadFileResponse_1 = UploadFileResponse_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(UploadFileResponseModel);
    get newFolios(): NumberModel_1 {
        return this[_getPropertyModel_1]("newFolios", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
    }
    get newSchemes(): NumberModel_1 {
        return this[_getPropertyModel_1]("newSchemes", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
    }
    get newTransactions(): NumberModel_1 {
        return this[_getPropertyModel_1]("newTransactions", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
    }
    get userCASDetailsId(): NumberModel_1 {
        return this[_getPropertyModel_1]("userCASDetailsId", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Long" } }));
    }
}
export default UploadFileResponseModel;
