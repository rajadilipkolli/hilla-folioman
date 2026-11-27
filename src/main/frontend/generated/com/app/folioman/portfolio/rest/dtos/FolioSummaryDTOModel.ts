import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type FolioSummaryDTO_1 from "./FolioSummaryDTO.js";
class FolioSummaryDTOModel<T extends FolioSummaryDTO_1 = FolioSummaryDTO_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(FolioSummaryDTOModel);
    get folio(): StringModel_1 {
        return this[_getPropertyModel_1]("folio", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get invested(): NumberModel_1 {
        return this[_getPropertyModel_1]("invested", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Double" } }));
    }
    get units(): NumberModel_1 {
        return this[_getPropertyModel_1]("units", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Double" } }));
    }
    get value(): NumberModel_1 {
        return this[_getPropertyModel_1]("value", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Double" } }));
    }
    get avg_nav(): NumberModel_1 {
        return this[_getPropertyModel_1]("avg_nav", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Double" } }));
    }
}
export default FolioSummaryDTOModel;
