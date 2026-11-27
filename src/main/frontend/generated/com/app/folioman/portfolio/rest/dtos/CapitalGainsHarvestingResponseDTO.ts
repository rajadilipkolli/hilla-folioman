import type HarvestRecommendationDTO_1 from "./HarvestRecommendationDTO.js";
import type HarvestSummaryDTO_1 from "./HarvestSummaryDTO.js";
interface CapitalGainsHarvestingResponseDTO {
    recommendations?: Array<HarvestRecommendationDTO_1 | undefined>;
    summary?: HarvestSummaryDTO_1;
}
export default CapitalGainsHarvestingResponseDTO;
