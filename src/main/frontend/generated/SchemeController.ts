import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type FundDetailProjection_1 from "./com/app/folioman/mfschemes/rest/dtos/FundDetailProjection.js";
import client_1 from "./connect-client.default.js";
async function fetchSchemes_1(schemeName: string | undefined, init?: EndpointRequestInit_1): Promise<Array<FundDetailProjection_1 | undefined> | undefined> { return client_1.call("SchemeController", "fetchSchemes", { schemeName }, init); }
export { fetchSchemes_1 as fetchSchemes };
