import { _getPropertyModel as _getPropertyModel_1, BooleanModel as BooleanModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type MimeType_1 from "./MimeType.js";
class MimeTypeModel<T extends MimeType_1 = MimeType_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(MimeTypeModel);
    get type(): StringModel_1 {
        return this[_getPropertyModel_1]("type", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get subtype(): StringModel_1 {
        return this[_getPropertyModel_1]("subtype", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get parameters(): ObjectModel_1<Record<string, string | undefined>> {
        return this[_getPropertyModel_1]("parameters", (parent, key) => new ObjectModel_1(parent, key, true, { meta: { javaType: "java.util.Map" } }));
    }
    get charset(): ObjectModel_1 {
        return this[_getPropertyModel_1]("charset", (parent, key) => new ObjectModel_1(parent, key, true, { meta: { javaType: "java.nio.charset.Charset" } }));
    }
    get concrete(): BooleanModel_1 {
        return this[_getPropertyModel_1]("concrete", (parent, key) => new BooleanModel_1(parent, key, false, { meta: { javaType: "boolean" } }));
    }
    get wildcardSubtype(): BooleanModel_1 {
        return this[_getPropertyModel_1]("wildcardSubtype", (parent, key) => new BooleanModel_1(parent, key, false, { meta: { javaType: "boolean" } }));
    }
    get wildcardType(): BooleanModel_1 {
        return this[_getPropertyModel_1]("wildcardType", (parent, key) => new BooleanModel_1(parent, key, false, { meta: { javaType: "boolean" } }));
    }
    get subtypeSuffix(): StringModel_1 {
        return this[_getPropertyModel_1]("subtypeSuffix", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default MimeTypeModel;
