import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type PortfolioHistoryDTO_1 from "./PortfolioHistoryDTO.js";
class PortfolioHistoryDTOModel<T extends PortfolioHistoryDTO_1 = PortfolioHistoryDTO_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(PortfolioHistoryDTOModel);
    get invested(): ArrayModel_1<ArrayModel_1<ObjectModel_1>> {
        return this[_getPropertyModel_1]("invested", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new ObjectModel_1(parent, key, false, { meta: { javaType: "java.lang.Number" } }), { meta: { javaType: "java.lang.Number[]" } }), { meta: { javaType: "java.util.List" } }));
    }
    get value(): ArrayModel_1<ArrayModel_1<ObjectModel_1>> {
        return this[_getPropertyModel_1]("value", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new ObjectModel_1(parent, key, false, { meta: { javaType: "java.lang.Number" } }), { meta: { javaType: "java.lang.Number[]" } }), { meta: { javaType: "java.util.List" } }));
    }
}
export default PortfolioHistoryDTOModel;
