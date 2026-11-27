import type CategoryDTO_1 from "./CategoryDTO.js";
import type ChangeDTO_1 from "./ChangeDTO.js";
import type FolioSummaryDTO_1 from "./FolioSummaryDTO.js";
interface SchemeSummaryDTO {
    id?: string;
    name?: string;
    xirr?: number;
    category?: CategoryDTO_1;
    rta?: string;
    plan?: string;
    nav0?: number;
    nav1?: number;
    nav_date?: string;
    invested?: number;
    units?: number;
    value?: number;
    avg_nav?: number;
    change?: ChangeDTO_1;
    change_pct?: ChangeDTO_1;
    folios?: Array<FolioSummaryDTO_1 | undefined>;
}
export default SchemeSummaryDTO;
