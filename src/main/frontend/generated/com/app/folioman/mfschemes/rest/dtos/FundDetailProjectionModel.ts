import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type FundDetailProjection_1 from "./FundDetailProjection.js";
class FundDetailProjectionModel<T extends FundDetailProjection_1 = FundDetailProjection_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(FundDetailProjectionModel);
    get schemeName(): StringModel_1 {
        return this[_getPropertyModel_1]("schemeName", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get amfiCode(): NumberModel_1 {
        return this[_getPropertyModel_1]("amfiCode", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Long" } }));
    }
    get amcName(): StringModel_1 {
        return this[_getPropertyModel_1]("amcName", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default FundDetailProjectionModel;
