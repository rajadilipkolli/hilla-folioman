import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import PortfolioDetailsDTOModel_1 from "./PortfolioDetailsDTOModel.js";
import type PortfolioResponse_1 from "./PortfolioResponse.js";
class PortfolioResponseModel<T extends PortfolioResponse_1 = PortfolioResponse_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(PortfolioResponseModel);
    get totalPortfolioValue(): NumberModel_1 {
        return this[_getPropertyModel_1]("totalPortfolioValue", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.math.BigDecimal" } }));
    }
    get portfolioDetailsDTOS(): ArrayModel_1<PortfolioDetailsDTOModel_1> {
        return this[_getPropertyModel_1]("portfolioDetailsDTOS", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new PortfolioDetailsDTOModel_1(parent, key, true), { meta: { javaType: "java.util.List" } }));
    }
}
export default PortfolioResponseModel;
