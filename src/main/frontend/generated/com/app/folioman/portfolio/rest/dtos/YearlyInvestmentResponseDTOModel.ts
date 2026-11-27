import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type YearlyInvestmentResponseDTO_1 from "./YearlyInvestmentResponseDTO.js";
class YearlyInvestmentResponseDTOModel<T extends YearlyInvestmentResponseDTO_1 = YearlyInvestmentResponseDTO_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(YearlyInvestmentResponseDTOModel);
    get year(): NumberModel_1 {
        return this[_getPropertyModel_1]("year", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Integer" } }));
    }
    get yearlyInvestment(): NumberModel_1 {
        return this[_getPropertyModel_1]("yearlyInvestment", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
}
export default YearlyInvestmentResponseDTOModel;
