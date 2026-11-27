import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type ChangeDTO_1 from "./ChangeDTO.js";
class ChangeDTOModel<T extends ChangeDTO_1 = ChangeDTO_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(ChangeDTOModel);
    get D(): NumberModel_1 {
        return this[_getPropertyModel_1]("D", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Double" } }));
    }
    get A(): NumberModel_1 {
        return this[_getPropertyModel_1]("A", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Double" } }));
    }
}
export default ChangeDTOModel;
