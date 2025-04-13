import React, {useRef, useState, useEffect} from 'react';
import {Button, FormLayout, FormLayoutResponsiveStep, Notification, PasswordField, ProgressBar} from "@vaadin/react-components";
import {Upload, UploadMaxFilesReachedChangedEvent, UploadBeforeEvent} from "@vaadin/react-components/Upload";
import {ImportMutualFundController} from "Frontend/generated/endpoints";
import './import-mutual-funds.css';

// Password inactivity timeout in milliseconds (5 minutes)
const PASSWORD_INACTIVITY_TIMEOUT = 5 * 60 * 1000;

const layoutSteps: FormLayoutResponsiveStep[] = [
    {minWidth: 0, columns: 1, labelsPosition: 'top'},
    {minWidth: '520px', columns: 2, labelsPosition: 'top'},
];

// Define the interface for the upload response
interface UploadResponse {
    newFolios: number;
    newSchemes: number;
    newTransactions: number;
    casId?: number;
}

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
    const [newFolios, setNewFolios] = useState<number | null>(null);
    const [newSchemes, setNewSchemes] = useState<number | null>(null);
    const [newTransactions, setNewTransactions] = useState<number | null>(null);
    const inactivityTimerRef = useRef<number | null>(null);

    // Security enhancement: Clear password after period of inactivity
    useEffect(() => {
        // Clear any existing timer when password changes
        if (inactivityTimerRef.current !== null) {
            window.clearTimeout(inactivityTimerRef.current);
            inactivityTimerRef.current = null;
        }
        
        // Only set timer if there is a password
        if (password) {
            inactivityTimerRef.current = window.setTimeout(() => {
                setPassword('');
                Notification.show('Password cleared due to inactivity', { position: 'bottom-end', duration: 3000 });
            }, PASSWORD_INACTIVITY_TIMEOUT);
        }
        
        // Cleanup function
        return () => {
            if (inactivityTimerRef.current !== null) {
                window.clearTimeout(inactivityTimerRef.current);
            }
        };
    }, [password]);

    // Clean up password when component unmounts
    useEffect(() => {
        return () => {
            setPassword('');
            if (inactivityTimerRef.current !== null) {
                window.clearTimeout(inactivityTimerRef.current);
            }
        };
    }, []);

    const fileRejectHandler = (event: any) => {
        Notification.show(`Error: ${event.detail.error} '${event.detail.file.name}'`);
    };

    const maxFilesReachedChangedHandler = (event: UploadMaxFilesReachedChangedEvent) => {
        maxFilesReached.current = event.detail.value;
    };

    const handlePasswordChange = (e: any) => {
        setPassword(e.target.value);
    };
    
    // Generic file selection handler
    const handleFileSelection = (event: UploadBeforeEvent, setFile: (file: File | null) => void) => {
        event.preventDefault();
        const file = event.detail.file;
        if (file) {
            setFile(file);
        }
    };
    
    const handleBeforeUpload = (event: UploadBeforeEvent) => {
        handleFileSelection(event, setPdfFile);
    };
    
    const handleJsonBeforeUpload = (event: UploadBeforeEvent) => {
        handleFileSelection(event, setJsonFile);
    };
    
    // Common function to handle upload success
    const handleUploadSuccess = (response: UploadResponse, fileType: 'pdf' | 'json') => {
        setNewFolios(response.newFolios);
        setNewSchemes(response.newSchemes);
        setNewTransactions(response.newTransactions);
        Notification.show('File uploaded successfully');
        
        if (fileType === 'pdf') {
            setPdfFile(null);
            // Immediately clear password after successful upload for security
            setPassword('');
            if (pdfUploadRef.current?.clear) {
                pdfUploadRef.current.clear();
            }
        } else {
            setJsonFile(null);
            if (jsonUploadRef.current?.clear) {
                jsonUploadRef.current.clear();
            }
        }
    };
    
    // Generic error handler for file uploads
    const handleUploadError = (error: unknown) => {
        Notification.show(`Error uploading file: ${error instanceof Error ? error.message : String(error)}`);
        setIsUploading(false);
    };
    
    // Common upload process with validation
    const processUpload = async (
        fileType: 'pdf' | 'json',
        validationFn: () => boolean,
        uploadFn: () => Promise<UploadResponse | Response | undefined>
    ) => {
        if (!validationFn()) {
            return;
        }
        
        setIsUploading(true);
        
        try {
            const response = await uploadFn();
            
            if (!response) {
                throw new Error('No response received from server');
            }
            
            // Check if response is a Response object (from fetch)
            if (response instanceof Response) {
                if (!response.ok) {
                    throw new Error(`Server responded with status: ${response.status}`);
                }
                // Parse JSON data from the response
                const data = await response.json();
                // Validate that the data has the expected properties
                if (typeof data.newFolios !== 'number' || 
                    typeof data.newSchemes !== 'number' || 
                    typeof data.newTransactions !== 'number') {
                    throw new Error('Invalid response format from server');
                }
                handleUploadSuccess(data as UploadResponse, fileType);
            } else {
                // Direct UploadResponse object (from ImportMutualFundController)
                handleUploadSuccess(response, fileType);
            }
        } catch (error) {
            handleUploadError(error);
        } finally {
            setIsUploading(false);
        }
    };
    
    const handlePdfUpload = async () => {
        await processUpload(
            'pdf',
            () => {
                if (!pdfFile) {
                    Notification.show('Please select a PDF file first');
                    return false;
                }
                
                if (!password) {
                    Notification.show('Please enter the password');
                    return false;
                }
                
                return true;
            },
            async () => {
                return await ImportMutualFundController.uploadPasswordProtectedCasPdf(pdfFile!, password);
            }
        );
    };
    
    const handleJsonUpload = async () => {
        await processUpload(
            'json',
            () => {
                if (!jsonFile) {
                    Notification.show('Please select a JSON file first');
                    return false;
                }
                return true;
            },
            async () => {
                const formData = new FormData();
                formData.append('file', jsonFile!);
                
                return await fetch('/api/upload-handler', {
                    method: 'POST',
                    body: formData
                });
            }
        );
    };

    const resetViews = () => {
        setNewFolios(null);
        setNewSchemes(null);
        setNewTransactions(null);
        setPdfFile(null);
        setJsonFile(null);
        setPassword('');
        
        // Clear the files in both upload components
        pdfUploadRef.current?.clear();
        jsonUploadRef.current?.clear();
        maxFilesReached.current = false;
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
