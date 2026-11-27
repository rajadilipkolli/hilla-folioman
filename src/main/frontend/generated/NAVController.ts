import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type MFSchemeDTO_1 from "./com/app/folioman/mfschemes/rest/dtos/MFSchemeDTO.js";
import client_1 from "./connect-client.default.js";
async function getSchemeNavOnDate_1(schemeCode: number | undefined, inputDate: string | undefined, init?: EndpointRequestInit_1): Promise<MFSchemeDTO_1 | undefined> { return client_1.call("NAVController", "getSchemeNavOnDate", { schemeCode, inputDate }, init); }
export { getSchemeNavOnDate_1 as getSchemeNavOnDate };
