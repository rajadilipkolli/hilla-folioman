import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type HarvestSummaryDTO_1 from "./HarvestSummaryDTO.js";
class HarvestSummaryDTOModel<T extends HarvestSummaryDTO_1 = HarvestSummaryDTO_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(HarvestSummaryDTOModel);
    get totalStcg(): NumberModel_1 {
        return this[_getPropertyModel_1]("totalStcg", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get totalLtcg(): NumberModel_1 {
        return this[_getPropertyModel_1]("totalLtcg", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get totalEstimatedTax(): NumberModel_1 {
        return this[_getPropertyModel_1]("totalEstimatedTax", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get totalExitLoad(): NumberModel_1 {
        return this[_getPropertyModel_1]("totalExitLoad", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get totalRedemptionAmount(): NumberModel_1 {
        return this[_getPropertyModel_1]("totalRedemptionAmount", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get totalRemainingValue(): NumberModel_1 {
        return this[_getPropertyModel_1]("totalRemainingValue", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
}
export default HarvestSummaryDTOModel;
