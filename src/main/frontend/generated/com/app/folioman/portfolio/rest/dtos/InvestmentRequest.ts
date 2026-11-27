import type Fund_1 from "./Fund.js";
interface InvestmentRequest {
    funds?: Array<Fund_1 | undefined>;
    amountToInvest?: number;
}
export default InvestmentRequest;
