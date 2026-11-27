import type HttpEntity_1 from "./HttpEntity.js";
import type HttpStatusCode_1 from "./HttpStatusCode.js";
interface ResponseEntity<T = unknown> extends HttpEntity_1 {
    statusCode?: HttpStatusCode_1;
}
export default ResponseEntity;
