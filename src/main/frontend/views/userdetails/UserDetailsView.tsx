import React, { useRef } from 'react';
import { Button, FormLayout, FormLayoutResponsiveStep, Notification } from "@vaadin/react-components";
import { Upload, UploadMaxFilesReachedChangedEvent } from "@vaadin/react-components/Upload";

const layoutSteps: FormLayoutResponsiveStep[] = [
  { minWidth: 0, columns: 1, labelsPosition: 'top' },
  { minWidth: '520px', columns: 2, labelsPosition: 'top' },
];

export default function UserDetailsView() {
  
  const maxFilesReached = useRef(false);

  const fileRejectHandler = (event: any) => {
    Notification.show(`Error: ${event.detail.error} '${event.detail.file.name}'`);
  };

  const maxFilesReachedChangedHandler = (event: UploadMaxFilesReachedChangedEvent) => {
    maxFilesReached.current = event.detail.value;
  };

  return (
    <FormLayout responsiveSteps={layoutSteps}>
      <div className="p-m">
        <h2>Upload CAS Json File</h2>
        <p>Accepted file formats: JSON (.json)</p>
        <label htmlFor="upload-drop-enabled">Drag and drop enabled</label>
          <Upload id="upload-drop-enabled"
            maxFiles={1}
            accept="application/json,.json"
            method="POST"
            target="/api/upload-handler"
            onFileReject={fileRejectHandler}
            onMaxFilesReachedChanged={maxFilesReachedChangedHandler}
          >
            <Button slot="add-button" theme="primary" disabled={maxFilesReached.current}>
              Upload CAS Json...
            </Button>
          </Upload>
      </div>
    </FormLayout>
  );
}
