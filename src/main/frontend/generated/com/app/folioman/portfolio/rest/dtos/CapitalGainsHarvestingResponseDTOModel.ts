import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type CapitalGainsHarvestingResponseDTO_1 from "./CapitalGainsHarvestingResponseDTO.js";
import HarvestRecommendationDTOModel_1 from "./HarvestRecommendationDTOModel.js";
import HarvestSummaryDTOModel_1 from "./HarvestSummaryDTOModel.js";
class CapitalGainsHarvestingResponseDTOModel<T extends CapitalGainsHarvestingResponseDTO_1 = CapitalGainsHarvestingResponseDTO_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(CapitalGainsHarvestingResponseDTOModel);
    get recommendations(): ArrayModel_1<HarvestRecommendationDTOModel_1> {
        return this[_getPropertyModel_1]("recommendations", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new HarvestRecommendationDTOModel_1(parent, key, true), { meta: { javaType: "java.util.List" } }));
    }
    get summary(): HarvestSummaryDTOModel_1 {
        return this[_getPropertyModel_1]("summary", (parent, key) => new HarvestSummaryDTOModel_1(parent, key, true));
    }
}
export default CapitalGainsHarvestingResponseDTOModel;
