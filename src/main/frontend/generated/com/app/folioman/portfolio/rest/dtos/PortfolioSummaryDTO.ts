import type ChangeDTO_1 from "./ChangeDTO.js";
import type SchemeSummaryDTO_1 from "./SchemeSummaryDTO.js";
import type XirrDTO_1 from "./XirrDTO.js";
interface PortfolioSummaryDTO {
    invested?: number;
    value?: number;
    xirr?: XirrDTO_1;
    change?: ChangeDTO_1;
    change_pct?: ChangeDTO_1;
    date?: string;
    schemes?: Array<SchemeSummaryDTO_1 | undefined>;
}
export default PortfolioSummaryDTO;
