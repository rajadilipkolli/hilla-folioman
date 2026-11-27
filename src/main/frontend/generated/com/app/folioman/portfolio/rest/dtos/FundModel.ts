import { _getPropertyModel as _getPropertyModel_1, DecimalMax as DecimalMax_1, DecimalMin as DecimalMin_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NotNull as NotNull_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, Positive as Positive_1 } from "@vaadin/hilla-lit-form";
import type Fund_1 from "./Fund.js";
class FundModel<T extends Fund_1 = Fund_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(FundModel);
    get value(): NumberModel_1 {
        return this[_getPropertyModel_1]("value", (parent, key) => new NumberModel_1(parent, key, true, { validators: [new NotNull_1({ message: "Fund value is required" }), new Positive_1({ message: "Fund value cannot be negative" })], meta: { javaType: "java.lang.Double" } }));
    }
    get ratio(): NumberModel_1 {
        return this[_getPropertyModel_1]("ratio", (parent, key) => new NumberModel_1(parent, key, true, { validators: [new NotNull_1({ message: "Fund ratio is required" }), new DecimalMin_1({ message: "Fund ratio must be between 0 and 1", value: "0.0" }), new DecimalMax_1({ message: "Fund ratio must be between 0 and 1", value: "1.0" })], meta: { javaType: "java.lang.Double" } }));
    }
}
export default FundModel;
