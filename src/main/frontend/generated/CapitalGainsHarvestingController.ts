import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type CapitalGainsHarvestingRequestDTO_1 from "./com/app/folioman/portfolio/rest/dtos/CapitalGainsHarvestingRequestDTO.js";
import type CapitalGainsHarvestingResponseDTO_1 from "./com/app/folioman/portfolio/rest/dtos/CapitalGainsHarvestingResponseDTO.js";
import client_1 from "./connect-client.default.js";
async function getCapitalGainsHarvesting_1(pan: string | undefined, request: CapitalGainsHarvestingRequestDTO_1 | undefined, init?: EndpointRequestInit_1): Promise<CapitalGainsHarvestingResponseDTO_1 | undefined> { return client_1.call("CapitalGainsHarvestingController", "getCapitalGainsHarvesting", { pan, request }, init); }
export { getCapitalGainsHarvesting_1 as getCapitalGainsHarvesting };
