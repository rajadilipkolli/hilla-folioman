import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import ChangeDTOModel_1 from "./ChangeDTOModel.js";
import type PortfolioSummaryDTO_1 from "./PortfolioSummaryDTO.js";
import SchemeSummaryDTOModel_1 from "./SchemeSummaryDTOModel.js";
import XirrDTOModel_1 from "./XirrDTOModel.js";
class PortfolioSummaryDTOModel<T extends PortfolioSummaryDTO_1 = PortfolioSummaryDTO_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(PortfolioSummaryDTOModel);
    get invested(): NumberModel_1 {
        return this[_getPropertyModel_1]("invested", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Double" } }));
    }
    get value(): NumberModel_1 {
        return this[_getPropertyModel_1]("value", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Double" } }));
    }
    get xirr(): XirrDTOModel_1 {
        return this[_getPropertyModel_1]("xirr", (parent, key) => new XirrDTOModel_1(parent, key, true));
    }
    get change(): ChangeDTOModel_1 {
        return this[_getPropertyModel_1]("change", (parent, key) => new ChangeDTOModel_1(parent, key, true));
    }
    get change_pct(): ChangeDTOModel_1 {
        return this[_getPropertyModel_1]("change_pct", (parent, key) => new ChangeDTOModel_1(parent, key, true));
    }
    get date(): StringModel_1 {
        return this[_getPropertyModel_1]("date", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.time.LocalDate" } }));
    }
    get schemes(): ArrayModel_1<SchemeSummaryDTOModel_1> {
        return this[_getPropertyModel_1]("schemes", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new SchemeSummaryDTOModel_1(parent, key, true), { meta: { javaType: "java.util.List" } }));
    }
}
export default PortfolioSummaryDTOModel;
