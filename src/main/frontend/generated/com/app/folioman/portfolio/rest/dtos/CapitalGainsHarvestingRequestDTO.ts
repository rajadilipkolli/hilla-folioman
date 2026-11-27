interface CapitalGainsHarvestingRequestDTO {
    asOfDate?: string;
    financialYear?: string;
    targetAmount?: number;
    taxRegime?: string;
    minRedemptionAmount?: number;
    includeExitLoad: boolean;
    includeStcg: boolean;
    includeLtcg: boolean;
    schemeFilters?: Array<string | undefined>;
    amcFilters?: Array<string | undefined>;
    existingRealizedGains?: number;
    exemptionOverride?: number;
}
export default CapitalGainsHarvestingRequestDTO;
