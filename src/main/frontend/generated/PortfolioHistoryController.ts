import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type PortfolioHistoryDTO_1 from "./com/app/folioman/portfolio/rest/dtos/PortfolioHistoryDTO.js";
import type PortfolioSummaryDTO_1 from "./com/app/folioman/portfolio/rest/dtos/PortfolioSummaryDTO.js";
import client_1 from "./connect-client.default.js";
import type ResponseEntity_1 from "./org/springframework/http/ResponseEntity.js";
async function getPortfolioHistory_1(id: number | undefined, from: string | undefined, to: string | undefined, init?: EndpointRequestInit_1): Promise<ResponseEntity_1<PortfolioHistoryDTO_1 | undefined> | undefined> { return client_1.call("PortfolioHistoryController", "getPortfolioHistory", { id, from, to }, init); }
async function getPortfolioSummary_1(id: number | undefined, init?: EndpointRequestInit_1): Promise<ResponseEntity_1<PortfolioSummaryDTO_1 | undefined> | undefined> { return client_1.call("PortfolioHistoryController", "getPortfolioSummary", { id }, init); }
export { getPortfolioHistory_1 as getPortfolioHistory, getPortfolioSummary_1 as getPortfolioSummary };
