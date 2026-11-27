import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type PortfolioResponse_1 from "./com/app/folioman/portfolio/rest/dtos/PortfolioResponse.js";
import type UploadFileResponse_1 from "./com/app/folioman/portfolio/rest/dtos/UploadFileResponse.js";
import client_1 from "./connect-client.default.js";
async function getPortfolio_1(panNumber: string | undefined, asOfDate: string | undefined, init?: EndpointRequestInit_1): Promise<PortfolioResponse_1 | undefined> { return client_1.call("ImportMutualFundController", "getPortfolio", { panNumber, asOfDate }, init); }
async function upload_1(multipartFile: File | undefined, init?: EndpointRequestInit_1): Promise<UploadFileResponse_1 | undefined> { return client_1.call("ImportMutualFundController", "upload", { multipartFile }, init); }
async function uploadPasswordProtectedCasPdf_1(pdfFile: File | undefined, password: string | undefined, init?: EndpointRequestInit_1): Promise<UploadFileResponse_1 | undefined> { return client_1.call("ImportMutualFundController", "uploadPasswordProtectedCasPdf", { pdfFile, password }, init); }
export { getPortfolio_1 as getPortfolio, upload_1 as upload, uploadPasswordProtectedCasPdf_1 as uploadPasswordProtectedCasPdf };
