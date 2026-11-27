import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type PortfolioDetailsDTO_1 from "./PortfolioDetailsDTO.js";
class PortfolioDetailsDTOModel<T extends PortfolioDetailsDTO_1 = PortfolioDetailsDTO_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(PortfolioDetailsDTOModel);
    get totalValue(): NumberModel_1 {
        return this[_getPropertyModel_1]("totalValue", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get schemeName(): StringModel_1 {
        return this[_getPropertyModel_1]("schemeName", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get folioNumber(): StringModel_1 {
        return this[_getPropertyModel_1]("folioNumber", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get date(): StringModel_1 {
        return this[_getPropertyModel_1]("date", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get xirr(): NumberModel_1 {
        return this[_getPropertyModel_1]("xirr", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "double" } }));
    }
}
export default PortfolioDetailsDTOModel;
