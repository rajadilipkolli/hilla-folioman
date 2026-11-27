import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type InvestmentReturnsDTO_1 from "./InvestmentReturnsDTO.js";
class InvestmentReturnsDTOModel<T extends InvestmentReturnsDTO_1 = InvestmentReturnsDTO_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(InvestmentReturnsDTOModel);
    get xirr(): NumberModel_1 {
        return this[_getPropertyModel_1]("xirr", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get cagr(): NumberModel_1 {
        return this[_getPropertyModel_1]("cagr", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get invested(): NumberModel_1 {
        return this[_getPropertyModel_1]("invested", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get currentValue(): NumberModel_1 {
        return this[_getPropertyModel_1]("currentValue", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get valuationDate(): StringModel_1 {
        return this[_getPropertyModel_1]("valuationDate", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.time.LocalDate" } }));
    }
}
export default InvestmentReturnsDTOModel;
