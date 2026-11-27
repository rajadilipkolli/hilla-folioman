import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type XirrDTO_1 from "./XirrDTO.js";
class XirrDTOModel<T extends XirrDTO_1 = XirrDTO_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(XirrDTOModel);
    get current(): NumberModel_1 {
        return this[_getPropertyModel_1]("current", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Double" } }));
    }
    get overall(): NumberModel_1 {
        return this[_getPropertyModel_1]("overall", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Double" } }));
    }
}
export default XirrDTOModel;
