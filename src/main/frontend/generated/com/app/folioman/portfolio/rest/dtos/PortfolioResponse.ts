import type PortfolioDetailsDTO_1 from "./PortfolioDetailsDTO.js";
interface PortfolioResponse {
    totalPortfolioValue?: number;
    portfolioDetailsDTOS?: Array<PortfolioDetailsDTO_1 | undefined>;
}
export default PortfolioResponse;
