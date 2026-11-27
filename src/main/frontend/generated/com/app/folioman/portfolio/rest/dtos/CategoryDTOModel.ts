import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type CategoryDTO_1 from "./CategoryDTO.js";
class CategoryDTOModel<T extends CategoryDTO_1 = CategoryDTO_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(CategoryDTOModel);
    get main(): StringModel_1 {
        return this[_getPropertyModel_1]("main", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get sub(): StringModel_1 {
        return this[_getPropertyModel_1]("sub", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default CategoryDTOModel;
