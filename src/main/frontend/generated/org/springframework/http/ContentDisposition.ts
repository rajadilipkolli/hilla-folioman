interface ContentDisposition {
    type?: string;
    name?: string;
    filename?: string;
    charset?: unknown;
    inline: boolean;
    attachment: boolean;
    formData: boolean;
}
export default ContentDisposition;
