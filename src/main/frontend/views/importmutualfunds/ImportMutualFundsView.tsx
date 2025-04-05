import React, {useRef, useState} from 'react';
import {Button, FormLayout, FormLayoutResponsiveStep, Notification, PasswordField} from "@vaadin/react-components";
import {Upload, UploadMaxFilesReachedChangedEvent, UploadBeforeEvent} from "@vaadin/react-components/Upload";
import {ImportMutualFundController} from "Frontend/generated/endpoints";

const layoutSteps: FormLayoutResponsiveStep[] = [
    {minWidth: 0, columns: 1, labelsPosition: 'top'},
    {minWidth: '520px', columns: 2, labelsPosition: 'top'},
];

export default function ImportMutualFundsView() {

    const maxFilesReached = useRef(false);
    // Fix the ref types to use element references instead of component references
    const pdfUploadRef = useRef<any>(null);
    const jsonUploadRef = useRef<any>(null);
    const [activeView, setActiveView] = useState<'json' | 'pdf'>('pdf');
    const [password, setPassword] = useState<string>('');
    const [isUploading, setIsUploading] = useState<boolean>(false);
    const [pdfFile, setPdfFile] = useState<File | null>(null);
    const [jsonFile, setJsonFile] = useState<File | null>(null);

    const fileRejectHandler = (event: any) => {
        Notification.show(`Error: ${event.detail.error} '${event.detail.file.name}'`);
    };

    const maxFilesReachedChangedHandler = (event: UploadMaxFilesReachedChangedEvent) => {
        maxFilesReached.current = event.detail.value;
    };

    const [newFolios, setNewFolios] = useState<number | null>(null);
    const [newSchemes, setNewSchemes] = useState<number | null>(null);
    const [newTransactions, setNewTransactions] = useState<number | null>(null);

    const handlePasswordChange = (e: any) => {
        setPassword(e.target.value);
    };
    
    const handleBeforeUpload = (event: UploadBeforeEvent) => {
        event.preventDefault();
        const file = event.detail.file;
        if (file) {
            setPdfFile(file);
        }
    };
    
    const handleJsonBeforeUpload = (event: UploadBeforeEvent) => {
        event.preventDefault();
        const file = event.detail.file;
        if (file) {
            setJsonFile(file);
        }
    };
    
    const handlePdfUpload = async () => {
        if (!pdfFile) {
            Notification.show('Please select a PDF file first');
            return;
        }
        
        if (!password) {
            Notification.show('Please enter the password');
            return;
        }
        
        setIsUploading(true);
        
        try {
            const response = await ImportMutualFundController.uploadPasswordProtectedCasPdf(pdfFile, password);
            
            if (response) {
                setNewFolios(response.newFolios);
                setNewSchemes(response.newSchemes);
                setNewTransactions(response.newTransactions);
                Notification.show('File uploaded successfully');
                setPdfFile(null);
                setPassword('');
                // Clear the files in the upload component
                if (pdfUploadRef.current) {
                    pdfUploadRef.current.clear && pdfUploadRef.current.clear();
                }
            }
        } catch (error) {
            Notification.show(`Error uploading file: ${error instanceof Error ? error.message : String(error)}`);
        } finally {
            setIsUploading(false);
        }
    };
    
    const handleJsonUpload = async () => {
        if (!jsonFile) {
            Notification.show('Please select a JSON file first');
            return;
        }
        
        setIsUploading(true);
        
        try {
            const formData = new FormData();
            formData.append('file', jsonFile);
            
            const response = await fetch('/api/upload-handler', {
                method: 'POST',
                body: formData
            });
            
            if (response.ok) {
                const data = await response.json();
                setNewFolios(data.newFolios);
                setNewSchemes(data.newSchemes);
                setNewTransactions(data.newTransactions);
                Notification.show('File uploaded successfully');
                setJsonFile(null);
                // Clear the files in the upload component
                if (jsonUploadRef.current) {
                    jsonUploadRef.current.clear && jsonUploadRef.current.clear();
                }
            } else {
                throw new Error(`Server responded with status: ${response.status}`);
            }
        } catch (error) {
            Notification.show(`Error uploading file: ${error instanceof Error ? error.message : String(error)}`);
        } finally {
            setIsUploading(false);
        }
    };

    const resetViews = () => {
        setNewFolios(null);
        setNewSchemes(null);
        setNewTransactions(null);
        setPdfFile(null);
        setJsonFile(null);
        setPassword('');
        
        // Clear the files in both upload components
        if (pdfUploadRef.current) {
            // Reset the file input by calling the clear method instead of manipulating files array
            pdfUploadRef.current.clear && pdfUploadRef.current.clear();
        }
        if (jsonUploadRef.current) {
            // Reset the file input by calling the clear method instead of manipulating files array
            jsonUploadRef.current.clear && jsonUploadRef.current.clear();
        }
    };

    return (
        <div className="p-m">
            <h2>Import Mutual Funds</h2>
            
            <div style={{ marginBottom: '20px', display: 'flex', gap: '10px' }}>
                <Button 
                    theme={activeView === 'pdf' ? 'primary' : 'secondary'} 
                    onClick={() => {
                        setActiveView('pdf');
                        resetViews();
                    }}
                >
                    Password Protected CAS
                </Button>
                <Button 
                    theme={activeView === 'json' ? 'primary' : 'secondary'} 
                    onClick={() => {
                        setActiveView('json');
                        resetViews();
                    }}
                >
                    JSON Upload (Fallback)
                </Button>
            </div>
            
            {activeView === 'pdf' ? (
                <FormLayout responsiveSteps={layoutSteps}>
                    <div className="p-m">
                        <h3>Upload Password Protected CAS PDF</h3>
                        <p>Accepted file formats: PDF (.pdf)</p>
                        <label htmlFor="pdf-upload-drop-enabled">Drag and drop enabled</label>
                        
                        <Upload
                            id="pdf-upload-drop-enabled"
                            maxFiles={1}
                            accept="application/pdf,.pdf"
                            nodrop={false}
                            onFileReject={fileRejectHandler}
                            onMaxFilesReachedChanged={maxFilesReachedChangedHandler}
                            onUploadBefore={handleBeforeUpload}
                            ref={pdfUploadRef}
                        >
                            <Button 
                                slot="add-button" 
                                theme="primary" 
                                disabled={maxFilesReached.current || isUploading}
                            >
                                Select PDF...
                            </Button>
                        </Upload>
                        
                        {pdfFile && (
                            <div style={{ marginTop: '10px' }}>
                                <p>Selected file: {pdfFile.name}</p>
                            </div>
                        )}
                        
                        <div className="password-field-container" style={{ position: 'relative', marginTop: '16px' }}>
                            <PasswordField
                                label="PDF Password"
                                value={password}
                                onValueChanged={handlePasswordChange}
                                required
                                disabled={isUploading}
                            />
                        </div>
                        
                        <div style={{ marginTop: '16px' }}>
                            <Button 
                                theme="primary" 
                                onClick={handlePdfUpload}
                                disabled={isUploading || !pdfFile}
                            >
                                {isUploading ? 'Uploading...' : 'Upload Protected CAS PDF'}
                            </Button>
                        </div>
                    </div>
                </FormLayout>
            ) : (
                <FormLayout responsiveSteps={layoutSteps}>
                    <div className="p-m">
                        <h3>Upload CAS JSON File (Fallback Method)</h3>
                        <p>Accepted file formats: JSON (.json)</p>
                        <label htmlFor="upload-drop-enabled">Drag and drop enabled</label>
                        <Upload
                            id="upload-drop-enabled"
                            maxFiles={1}
                            accept="application/json,.json"
                            onFileReject={fileRejectHandler}
                            onMaxFilesReachedChanged={maxFilesReachedChangedHandler}
                            onUploadBefore={handleJsonBeforeUpload}
                            ref={jsonUploadRef}
                        >
                            <Button slot="add-button" theme="primary" disabled={maxFilesReached.current || isUploading}>
                                Select JSON File...
                            </Button>
                        </Upload>
                        
                        {jsonFile && (
                            <div style={{ marginTop: '10px' }}>
                                <p>Selected file: {jsonFile.name}</p>
                                
                                <div style={{ marginTop: '16px' }}>
                                    <Button 
                                        theme="primary" 
                                        onClick={handleJsonUpload}
                                        disabled={isUploading}
                                    >
                                        {isUploading ? 'Uploading...' : 'Upload JSON File'}
                                    </Button>
                                </div>
                            </div>
                        )}
                    </div>
                </FormLayout>
            )}

            {/* Displaying response data */}
            {newFolios !== null && (
                <div className="response-data p-m">
                    <h3>Summary of upload</h3>
                    <p>Number of New Folios: {newFolios}</p>
                    <p>Number of New Schemes: {newSchemes}</p>
                    <p>Number of New Transactions: {newTransactions}</p>
                </div>
            )}
        </div>
    );
}
