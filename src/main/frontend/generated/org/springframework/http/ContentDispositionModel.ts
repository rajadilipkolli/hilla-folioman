import { _getPropertyModel as _getPropertyModel_1, BooleanModel as BooleanModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type ContentDisposition_1 from "./ContentDisposition.js";
class ContentDispositionModel<T extends ContentDisposition_1 = ContentDisposition_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(ContentDispositionModel);
    get type(): StringModel_1 {
        return this[_getPropertyModel_1]("type", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get name(): StringModel_1 {
        return this[_getPropertyModel_1]("name", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get filename(): StringModel_1 {
        return this[_getPropertyModel_1]("filename", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get charset(): ObjectModel_1 {
        return this[_getPropertyModel_1]("charset", (parent, key) => new ObjectModel_1(parent, key, true, { meta: { javaType: "java.nio.charset.Charset" } }));
    }
    get inline(): BooleanModel_1 {
        return this[_getPropertyModel_1]("inline", (parent, key) => new BooleanModel_1(parent, key, false, { meta: { javaType: "boolean" } }));
    }
    get attachment(): BooleanModel_1 {
        return this[_getPropertyModel_1]("attachment", (parent, key) => new BooleanModel_1(parent, key, false, { meta: { javaType: "boolean" } }));
    }
    get formData(): BooleanModel_1 {
        return this[_getPropertyModel_1]("formData", (parent, key) => new BooleanModel_1(parent, key, false, { meta: { javaType: "boolean" } }));
    }
}
export default ContentDispositionModel;
