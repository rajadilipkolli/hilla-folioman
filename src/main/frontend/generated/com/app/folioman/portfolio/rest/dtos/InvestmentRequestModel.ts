import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NotEmpty as NotEmpty_1, NotNull as NotNull_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, PositiveOrZero as PositiveOrZero_1 } from "@vaadin/hilla-lit-form";
import FundModel_1 from "./FundModel.js";
import type InvestmentRequest_1 from "./InvestmentRequest.js";
class InvestmentRequestModel<T extends InvestmentRequest_1 = InvestmentRequest_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(InvestmentRequestModel);
    get funds(): ArrayModel_1<FundModel_1> {
        return this[_getPropertyModel_1]("funds", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new FundModel_1(parent, key, true), { validators: [new NotEmpty_1({ message: "Investment request and funds list cannot be null or empty" })], meta: { javaType: "java.util.List" } }));
    }
    get amountToInvest(): NumberModel_1 {
        return this[_getPropertyModel_1]("amountToInvest", (parent, key) => new NumberModel_1(parent, key, true, { validators: [new PositiveOrZero_1({ message: "Amount to invest cannot be negative" }), new NotNull_1({ message: "Amount to invest cannot be null" })], meta: { javaType: "java.lang.Double" } }));
    }
}
export default InvestmentRequestModel;
