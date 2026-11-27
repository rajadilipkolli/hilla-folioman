import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import CategoryDTOModel_1 from "./CategoryDTOModel.js";
import ChangeDTOModel_1 from "./ChangeDTOModel.js";
import FolioSummaryDTOModel_1 from "./FolioSummaryDTOModel.js";
import type SchemeSummaryDTO_1 from "./SchemeSummaryDTO.js";
class SchemeSummaryDTOModel<T extends SchemeSummaryDTO_1 = SchemeSummaryDTO_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(SchemeSummaryDTOModel);
    get id(): StringModel_1 {
        return this[_getPropertyModel_1]("id", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get name(): StringModel_1 {
        return this[_getPropertyModel_1]("name", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get xirr(): NumberModel_1 {
        return this[_getPropertyModel_1]("xirr", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Double" } }));
    }
    get category(): CategoryDTOModel_1 {
        return this[_getPropertyModel_1]("category", (parent, key) => new CategoryDTOModel_1(parent, key, true));
    }
    get rta(): StringModel_1 {
        return this[_getPropertyModel_1]("rta", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get plan(): StringModel_1 {
        return this[_getPropertyModel_1]("plan", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get nav0(): NumberModel_1 {
        return this[_getPropertyModel_1]("nav0", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Double" } }));
    }
    get nav1(): NumberModel_1 {
        return this[_getPropertyModel_1]("nav1", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Double" } }));
    }
    get nav_date(): StringModel_1 {
        return this[_getPropertyModel_1]("nav_date", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.time.LocalDate" } }));
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
    get change(): ChangeDTOModel_1 {
        return this[_getPropertyModel_1]("change", (parent, key) => new ChangeDTOModel_1(parent, key, true));
    }
    get change_pct(): ChangeDTOModel_1 {
        return this[_getPropertyModel_1]("change_pct", (parent, key) => new ChangeDTOModel_1(parent, key, true));
    }
    get folios(): ArrayModel_1<FolioSummaryDTOModel_1> {
        return this[_getPropertyModel_1]("folios", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new FolioSummaryDTOModel_1(parent, key, true), { meta: { javaType: "java.util.List" } }));
    }
}
export default SchemeSummaryDTOModel;
