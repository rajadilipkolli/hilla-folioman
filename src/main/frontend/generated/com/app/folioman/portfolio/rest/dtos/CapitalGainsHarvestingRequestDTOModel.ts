import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, BooleanModel as BooleanModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, PositiveOrZero as PositiveOrZero_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type CapitalGainsHarvestingRequestDTO_1 from "./CapitalGainsHarvestingRequestDTO.js";
class CapitalGainsHarvestingRequestDTOModel<T extends CapitalGainsHarvestingRequestDTO_1 = CapitalGainsHarvestingRequestDTO_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(CapitalGainsHarvestingRequestDTOModel);
    get asOfDate(): StringModel_1 {
        return this[_getPropertyModel_1]("asOfDate", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.time.LocalDate" } }));
    }
    get financialYear(): StringModel_1 {
        return this[_getPropertyModel_1]("financialYear", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get targetAmount(): NumberModel_1 {
        return this[_getPropertyModel_1]("targetAmount", (parent, key) => new NumberModel_1(parent, key, true, { validators: [new PositiveOrZero_1({ message: "Target amount cannot be negative" })], meta: { javaType: "java.math.BigDecimal" } }));
    }
    get taxRegime(): StringModel_1 {
        return this[_getPropertyModel_1]("taxRegime", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get minRedemptionAmount(): NumberModel_1 {
        return this[_getPropertyModel_1]("minRedemptionAmount", (parent, key) => new NumberModel_1(parent, key, true, { validators: [new PositiveOrZero_1({ message: "Minimum redemption amount cannot be negative" })], meta: { javaType: "java.math.BigDecimal" } }));
    }
    get includeExitLoad(): BooleanModel_1 {
        return this[_getPropertyModel_1]("includeExitLoad", (parent, key) => new BooleanModel_1(parent, key, false, { meta: { javaType: "boolean" } }));
    }
    get includeStcg(): BooleanModel_1 {
        return this[_getPropertyModel_1]("includeStcg", (parent, key) => new BooleanModel_1(parent, key, false, { meta: { javaType: "boolean" } }));
    }
    get includeLtcg(): BooleanModel_1 {
        return this[_getPropertyModel_1]("includeLtcg", (parent, key) => new BooleanModel_1(parent, key, false, { meta: { javaType: "boolean" } }));
    }
    get schemeFilters(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("schemeFilters", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.util.List" } }));
    }
    get amcFilters(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("amcFilters", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.util.List" } }));
    }
    get existingRealizedGains(): NumberModel_1 {
        return this[_getPropertyModel_1]("existingRealizedGains", (parent, key) => new NumberModel_1(parent, key, true, { validators: [new PositiveOrZero_1({ message: "Existing realized gains cannot be negative" })], meta: { javaType: "java.math.BigDecimal" } }));
    }
    get exemptionOverride(): NumberModel_1 {
        return this[_getPropertyModel_1]("exemptionOverride", (parent, key) => new NumberModel_1(parent, key, true, { validators: [new PositiveOrZero_1({ message: "Exemption override cannot be negative" })], meta: { javaType: "java.math.BigDecimal" } }));
    }
}
export default CapitalGainsHarvestingRequestDTOModel;
