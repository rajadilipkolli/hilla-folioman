import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type MonthlyInvestmentResponseDTO_1 from "./MonthlyInvestmentResponseDTO.js";
class MonthlyInvestmentResponseDTOModel<T extends MonthlyInvestmentResponseDTO_1 = MonthlyInvestmentResponseDTO_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(MonthlyInvestmentResponseDTOModel);
    get year(): NumberModel_1 {
        return this[_getPropertyModel_1]("year", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Integer" } }));
    }
    get monthNumber(): NumberModel_1 {
        return this[_getPropertyModel_1]("monthNumber", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Integer" } }));
    }
    get investmentPerMonth(): NumberModel_1 {
        return this[_getPropertyModel_1]("investmentPerMonth", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get cumulativeInvestment(): NumberModel_1 {
        return this[_getPropertyModel_1]("cumulativeInvestment", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
}
export default MonthlyInvestmentResponseDTOModel;
