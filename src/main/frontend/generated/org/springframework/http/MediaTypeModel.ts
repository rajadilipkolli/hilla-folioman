import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1 } from "@vaadin/hilla-lit-form";
import MimeTypeModel_1 from "../util/MimeTypeModel.js";
import type MediaType_1 from "./MediaType.js";
class MediaTypeModel<T extends MediaType_1 = MediaType_1> extends MimeTypeModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(MediaTypeModel);
    get qualityValue(): NumberModel_1 {
        return this[_getPropertyModel_1]("qualityValue", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "double" } }));
    }
}
export default MediaTypeModel;
