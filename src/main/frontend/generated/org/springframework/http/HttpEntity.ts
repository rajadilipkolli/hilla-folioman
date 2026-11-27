import type HttpHeaders_1 from "./HttpHeaders.js";
interface HttpEntity<T = unknown> {
    headers?: HttpHeaders_1;
    body?: T;
}
export default HttpEntity;
