interface MimeType {
    type?: string;
    subtype?: string;
    parameters?: Record<string, string | undefined>;
    charset?: unknown;
    concrete: boolean;
    wildcardSubtype: boolean;
    wildcardType: boolean;
    subtypeSuffix?: string;
}
export default MimeType;
