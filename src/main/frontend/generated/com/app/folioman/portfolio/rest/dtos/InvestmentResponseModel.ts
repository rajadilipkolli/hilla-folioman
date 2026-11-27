import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type InvestmentResponse_1 from "./InvestmentResponse.js";
class InvestmentResponseModel<T extends InvestmentResponse_1 = InvestmentResponse_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(InvestmentResponseModel);
    get investments(): ArrayModel_1<NumberModel_1> {
        return this[_getPropertyModel_1]("investments", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Double" } }), { meta: { javaType: "java.util.List" } }));
    }
}
export default InvestmentResponseModel;
