import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type InvestmentRequest_1 from "./com/app/folioman/portfolio/rest/dtos/InvestmentRequest.js";
import type InvestmentResponse_1 from "./com/app/folioman/portfolio/rest/dtos/InvestmentResponse.js";
import client_1 from "./connect-client.default.js";
async function reBalance_1(investmentRequest: InvestmentRequest_1 | undefined, init?: EndpointRequestInit_1): Promise<InvestmentResponse_1 | undefined> { return client_1.call("ReBalanceController", "reBalance", { investmentRequest }, init); }
export { reBalance_1 as reBalance };
