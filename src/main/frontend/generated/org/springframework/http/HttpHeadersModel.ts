import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, BooleanModel as BooleanModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import ContentDispositionModel_1 from "./ContentDispositionModel.js";
import type HttpHeaders_1 from "./HttpHeaders.js";
import HttpMethodModel_1 from "./HttpMethodModel.js";
import HttpRangeModel_1 from "./HttpRangeModel.js";
import MediaTypeModel_1 from "./MediaTypeModel.js";
class HttpHeadersModel<T extends HttpHeaders_1 = HttpHeaders_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(HttpHeadersModel);
    get empty(): BooleanModel_1 {
        return this[_getPropertyModel_1]("empty", (parent, key) => new BooleanModel_1(parent, key, false, { meta: { javaType: "boolean" } }));
    }
    get location(): ObjectModel_1 {
        return this[_getPropertyModel_1]("location", (parent, key) => new ObjectModel_1(parent, key, true, { meta: { javaType: "java.net.URI" } }));
    }
    get host(): ObjectModel_1 {
        return this[_getPropertyModel_1]("host", (parent, key) => new ObjectModel_1(parent, key, true, { meta: { javaType: "java.net.InetSocketAddress" } }));
    }
    get all(): ObjectModel_1<Record<string, string | undefined>> {
        return this[_getPropertyModel_1]("all", (parent, key) => new ObjectModel_1(parent, key, true, { meta: { javaType: "java.util.Map" } }));
    }
    get lastModified(): NumberModel_1 {
        return this[_getPropertyModel_1]("lastModified", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "long" } }));
    }
    get date(): NumberModel_1 {
        return this[_getPropertyModel_1]("date", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "long" } }));
    }
    get contentLength(): NumberModel_1 {
        return this[_getPropertyModel_1]("contentLength", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "long" } }));
    }
    get connection(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("connection", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.util.List" } }));
    }
    get ifModifiedSince(): NumberModel_1 {
        return this[_getPropertyModel_1]("ifModifiedSince", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "long" } }));
    }
    get contentType(): MediaTypeModel_1 {
        return this[_getPropertyModel_1]("contentType", (parent, key) => new MediaTypeModel_1(parent, key, true));
    }
    get origin(): StringModel_1 {
        return this[_getPropertyModel_1]("origin", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get range(): ArrayModel_1<HttpRangeModel_1> {
        return this[_getPropertyModel_1]("range", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new HttpRangeModel_1(parent, key, true), { meta: { javaType: "java.util.List" } }));
    }
    get accessControlRequestMethod(): HttpMethodModel_1 {
        return this[_getPropertyModel_1]("accessControlRequestMethod", (parent, key) => new HttpMethodModel_1(parent, key, true));
    }
    get accessControlExposeHeaders(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("accessControlExposeHeaders", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.util.List" } }));
    }
    get accessControlRequestHeaders(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("accessControlRequestHeaders", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.util.List" } }));
    }
    get accessControlAllowCredentials(): BooleanModel_1 {
        return this[_getPropertyModel_1]("accessControlAllowCredentials", (parent, key) => new BooleanModel_1(parent, key, false, { meta: { javaType: "boolean" } }));
    }
    get accessControlAllowMethods(): ArrayModel_1<HttpMethodModel_1> {
        return this[_getPropertyModel_1]("accessControlAllowMethods", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new HttpMethodModel_1(parent, key, true), { meta: { javaType: "java.util.List" } }));
    }
    get accessControlAllowHeaders(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("accessControlAllowHeaders", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.util.List" } }));
    }
    get accessControlAllowOrigin(): StringModel_1 {
        return this[_getPropertyModel_1]("accessControlAllowOrigin", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get acceptLanguage(): ArrayModel_1<ObjectModel_1> {
        return this[_getPropertyModel_1]("acceptLanguage", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new ObjectModel_1(parent, key, false, { meta: { javaType: "java.util.Locale$LanguageRange" } }), { meta: { javaType: "java.util.List" } }));
    }
    get basicAuth(): StringModel_1 {
        return this[_getPropertyModel_1]("basicAuth", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get contentDisposition(): ContentDispositionModel_1 {
        return this[_getPropertyModel_1]("contentDisposition", (parent, key) => new ContentDispositionModel_1(parent, key, true));
    }
    get acceptLanguageAsLocales(): ArrayModel_1<ObjectModel_1> {
        return this[_getPropertyModel_1]("acceptLanguageAsLocales", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new ObjectModel_1(parent, key, false, { meta: { javaType: "java.util.Locale" } }), { meta: { javaType: "java.util.List" } }));
    }
    get accessControlMaxAge(): NumberModel_1 {
        return this[_getPropertyModel_1]("accessControlMaxAge", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "long" } }));
    }
    get acceptCharset(): ArrayModel_1<ObjectModel_1> {
        return this[_getPropertyModel_1]("acceptCharset", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new ObjectModel_1(parent, key, false, { meta: { javaType: "java.nio.charset.Charset" } }), { meta: { javaType: "java.util.List" } }));
    }
    get acceptQuery(): ArrayModel_1<MediaTypeModel_1> {
        return this[_getPropertyModel_1]("acceptQuery", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new MediaTypeModel_1(parent, key, true), { meta: { javaType: "java.util.List" } }));
    }
    get bearerAuth(): StringModel_1 {
        return this[_getPropertyModel_1]("bearerAuth", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get cacheControl(): StringModel_1 {
        return this[_getPropertyModel_1]("cacheControl", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get acceptPatch(): ArrayModel_1<MediaTypeModel_1> {
        return this[_getPropertyModel_1]("acceptPatch", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new MediaTypeModel_1(parent, key, true), { meta: { javaType: "java.util.List" } }));
    }
    get contentLanguage(): ObjectModel_1 {
        return this[_getPropertyModel_1]("contentLanguage", (parent, key) => new ObjectModel_1(parent, key, true, { meta: { javaType: "java.util.Locale" } }));
    }
    get ifNoneMatch(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("ifNoneMatch", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.util.List" } }));
    }
    get ifUnmodifiedSince(): NumberModel_1 {
        return this[_getPropertyModel_1]("ifUnmodifiedSince", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "long" } }));
    }
    get vary(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("vary", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.util.List" } }));
    }
    get pragma(): StringModel_1 {
        return this[_getPropertyModel_1]("pragma", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get ETag(): StringModel_1 {
        return this[_getPropertyModel_1]("ETag", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get accept(): ArrayModel_1<MediaTypeModel_1> {
        return this[_getPropertyModel_1]("accept", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new MediaTypeModel_1(parent, key, true), { meta: { javaType: "java.util.List" } }));
    }
    get allow(): ArrayModel_1<HttpMethodModel_1> {
        return this[_getPropertyModel_1]("allow", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new HttpMethodModel_1(parent, key, true), { meta: { javaType: "java.util.Set" } }));
    }
    get upgrade(): StringModel_1 {
        return this[_getPropertyModel_1]("upgrade", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get ifMatch(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("ifMatch", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.util.List" } }));
    }
    get expires(): NumberModel_1 {
        return this[_getPropertyModel_1]("expires", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "long" } }));
    }
}
export default HttpHeadersModel;
