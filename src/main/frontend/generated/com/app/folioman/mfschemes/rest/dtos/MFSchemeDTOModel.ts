import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NotBlank as NotBlank_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, Positive as Positive_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type MFSchemeDTO_1 from "./MFSchemeDTO.js";
class MFSchemeDTOModel<T extends MFSchemeDTO_1 = MFSchemeDTO_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(MFSchemeDTOModel);
    get amc(): StringModel_1 {
        return this[_getPropertyModel_1]("amc", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get schemeCode(): NumberModel_1 {
        return this[_getPropertyModel_1]("schemeCode", (parent, key) => new NumberModel_1(parent, key, true, { validators: [new Positive_1()], meta: { javaType: "java.lang.Long" } }));
    }
    get isin(): StringModel_1 {
        return this[_getPropertyModel_1]("isin", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get schemeName(): StringModel_1 {
        return this[_getPropertyModel_1]("schemeName", (parent, key) => new StringModel_1(parent, key, true, { validators: [new NotBlank_1()], meta: { javaType: "java.lang.String" } }));
    }
    get nav(): StringModel_1 {
        return this[_getPropertyModel_1]("nav", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get date(): StringModel_1 {
        return this[_getPropertyModel_1]("date", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get schemeType(): StringModel_1 {
        return this[_getPropertyModel_1]("schemeType", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default MFSchemeDTOModel;
