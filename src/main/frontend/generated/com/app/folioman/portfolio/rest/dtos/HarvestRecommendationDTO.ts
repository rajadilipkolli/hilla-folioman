interface HarvestRecommendationDTO {
    schemeName?: string;
    folioNumber?: string;
    unitsToRedeem?: number;
    redemptionAmount?: number;
    stcg?: number;
    ltcg?: number;
    estimatedTax?: number;
    exitLoad?: number;
    remainingUnits?: number;
    recommendationScore: number;
    reason?: string;
}
export default HarvestRecommendationDTO;
