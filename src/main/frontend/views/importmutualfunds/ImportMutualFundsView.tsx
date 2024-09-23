import React, {useRef, useState} from 'react';
import {Button, FormLayout, FormLayoutResponsiveStep, Notification, UploadSuccessEvent} from "@vaadin/react-components";
import {Upload, UploadMaxFilesReachedChangedEvent} from "@vaadin/react-components/Upload";

const layoutSteps: FormLayoutResponsiveStep[] = [
    {minWidth: 0, columns: 1, labelsPosition: 'top'},
    {minWidth: '520px', columns: 2, labelsPosition: 'top'},
];

export default function ImportMutualFundsView() {

    const maxFilesReached = useRef(false);

    const fileRejectHandler = (event: any) => {
        Notification.show(`Error: ${event.detail.error} '${event.detail.file.name}'`);
    };

    const maxFilesReachedChangedHandler = (event: UploadMaxFilesReachedChangedEvent) => {
        maxFilesReached.current = event.detail.value;
    };

    const [newFolios, setNewFolios] = useState<number | null>(null);
    const [newSchemes, setNewSchemes] = useState<number | null>(null);
    const [newTransactions, setNewTransactions] = useState<number | null>(null);

    const handleUploadSuccess = (event: UploadSuccessEvent) => {
        const response = event.detail.xhr.responseText;
        const parsedResponse = JSON.parse(response);

        // Update the state with values from the response
        setNewFolios(parsedResponse.newFolios);
        setNewSchemes(parsedResponse.newSchemes);
        setNewTransactions(parsedResponse.newTransactions);
    };

    return (
        <FormLayout responsiveSteps={layoutSteps}>
            <div className="p-m">
                <h2>Upload CAS Json File</h2>
                <p>Accepted file formats: JSON (.json)</p>
                <label htmlFor="upload-drop-enabled">Drag and drop enabled</label>
                <Upload
                    id="upload-drop-enabled"
                    maxFiles={1}
                    accept="application/json,.json"
                    method="POST"
                    target="/api/upload-handler"
                    onFileReject={fileRejectHandler}
                    onMaxFilesReachedChanged={maxFilesReachedChangedHandler}
                    onUploadSuccess={handleUploadSuccess}
                >
                    <Button slot="add-button" theme="primary" disabled={maxFilesReached.current}>
                        Upload CAS Json...
                    </Button>
                </Upload>

                {/* Displaying response data */}
                {newFolios !== null && (
                    <div className="response-data">
                        <h3> Summary of upload </h3>
                        <p>Number of New Folios: {newFolios}</p>
                        <p>Number of New Schemes: {newSchemes}</p>
                        <p>Number of New Transactions: {newTransactions}</p>
                    </div>
                )}
            </div>
        </FormLayout>
    );
}
