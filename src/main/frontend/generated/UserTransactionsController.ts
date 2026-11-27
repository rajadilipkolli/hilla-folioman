import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type InvestmentReturnsDTO_1 from "./com/app/folioman/portfolio/rest/dtos/InvestmentReturnsDTO.js";
import type MonthlyInvestmentResponseDTO_1 from "./com/app/folioman/portfolio/rest/dtos/MonthlyInvestmentResponseDTO.js";
import type YearlyInvestmentResponseDTO_1 from "./com/app/folioman/portfolio/rest/dtos/YearlyInvestmentResponseDTO.js";
import client_1 from "./connect-client.default.js";
async function getInvestmentReturns_1(pan: string | undefined, init?: EndpointRequestInit_1): Promise<InvestmentReturnsDTO_1 | undefined> { return client_1.call("UserTransactionsController", "getInvestmentReturns", { pan }, init); }
async function getTotalInvestmentsByPanPerMonth_1(pan: string | undefined, init?: EndpointRequestInit_1): Promise<Array<MonthlyInvestmentResponseDTO_1 | undefined> | undefined> { return client_1.call("UserTransactionsController", "getTotalInvestmentsByPanPerMonth", { pan }, init); }
async function getTotalInvestmentsByPanPerYear_1(pan: string | undefined, init?: EndpointRequestInit_1): Promise<Array<YearlyInvestmentResponseDTO_1 | undefined> | undefined> { return client_1.call("UserTransactionsController", "getTotalInvestmentsByPanPerYear", { pan }, init); }
export { getInvestmentReturns_1 as getInvestmentReturns, getTotalInvestmentsByPanPerMonth_1 as getTotalInvestmentsByPanPerMonth, getTotalInvestmentsByPanPerYear_1 as getTotalInvestmentsByPanPerYear };
