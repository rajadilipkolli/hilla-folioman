import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type HarvestRecommendationDTO_1 from "./HarvestRecommendationDTO.js";
class HarvestRecommendationDTOModel<T extends HarvestRecommendationDTO_1 = HarvestRecommendationDTO_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(HarvestRecommendationDTOModel);
    get schemeName(): StringModel_1 {
        return this[_getPropertyModel_1]("schemeName", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get folioNumber(): StringModel_1 {
        return this[_getPropertyModel_1]("folioNumber", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get unitsToRedeem(): NumberModel_1 {
        return this[_getPropertyModel_1]("unitsToRedeem", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get redemptionAmount(): NumberModel_1 {
        return this[_getPropertyModel_1]("redemptionAmount", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get stcg(): NumberModel_1 {
        return this[_getPropertyModel_1]("stcg", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get ltcg(): NumberModel_1 {
        return this[_getPropertyModel_1]("ltcg", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get estimatedTax(): NumberModel_1 {
        return this[_getPropertyModel_1]("estimatedTax", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get exitLoad(): NumberModel_1 {
        return this[_getPropertyModel_1]("exitLoad", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get remainingUnits(): NumberModel_1 {
        return this[_getPropertyModel_1]("remainingUnits", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get recommendationScore(): NumberModel_1 {
        return this[_getPropertyModel_1]("recommendationScore", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "double" } }));
    }
    get reason(): StringModel_1 {
        return this[_getPropertyModel_1]("reason", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default HarvestRecommendationDTOModel;
