import FundDetailProjection
    from "Frontend/generated/com/app/folioman/mfschemes/FundDetailProjection";
import MFSchemeDTO from "Frontend/generated/com/app/folioman/mfschemes/MFSchemeDTO";
import React, { useEffect, useState } from "react";
import { SchemeController } from "Frontend/generated/endpoints";
import { Grid, GridColumn, Icon, TextField, Dialog, Button, DatePicker } from "@vaadin/react-components";

export default function MfSchemesView() {
    const [query, setQuery] = useState('');
    const [schemeList, setSchemeList] = useState<FundDetailProjection[]>([]);
    const [dialogOpened, setDialogOpened] = useState(false);
    const [schemeDetails, setSchemeDetails] = useState<MFSchemeDTO | null>(null);
    const [selectedDate, setSelectedDate] = useState<string | null>(null);
    const [currentAmfiCode, setCurrentAmfiCode] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const today = new Date().toISOString().split('T')[0]; // Gets current date in YYYY-MM-DD format

    useEffect(() => {
        if (query.length > 3) {
            SchemeController.fetchSchemes(query)
                .then((fetchedResults) => {
                    const filteredResults = (fetchedResults ?? []).filter(
                        (item): item is FundDetailProjection => item !== undefined
                    );
                    setSchemeList(filteredResults);
                })
                .catch((error) => {
                    console.error('Error fetching schemes:', error);
                    setSchemeList([]); // Clear the list in case of an error
                });
        } else {
            setSchemeList([]); // Clear the list if the query is too short
        }
    }, [query]);

    const fetchSchemeDetails = (amfiCode: string, date?: string) => {
        setIsLoading(true);
        const fetchUrl = date
            ? `/api/nav/${amfiCode}/${date}`
            : `/api/nav/${amfiCode}`;

        fetch(fetchUrl, { method: 'GET' })
            .then(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
            })
            .then((data: MFSchemeDTO) => {
                setSchemeDetails(data);
                setCurrentAmfiCode(amfiCode);
                if (data.date) {
                    setSelectedDate(data.date);
                }
                setIsLoading(false);
            })
            .catch(error => {
                console.error('Error fetching scheme details:', error);
                setIsLoading(false);
                // Optionally show an error message to the user
            });
    };

    const handleDateChange = (e: CustomEvent) => {
        if (!currentAmfiCode) return;

        const newDate = e.detail.value;
        if (newDate) {
            // Avoid redundant fetches if the date hasn't changed
            if (newDate === selectedDate) return;
            // Format date as YYYY-MM-DD for API call
            fetchSchemeDetails(currentAmfiCode, newDate);
        }
    };

    return (
        <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '20px' }}>
            <TextField
                theme="align-center small helper-above-field"
                helperText="Search Schemes"
                clearButtonVisible
                placeholder="Search schemes..."
                style={{
                    width: '100%',
                    marginBottom: '20px', // Add spacing below the TextField
                } as React.CSSProperties}
                value={query}
                onChange={(e) => setQuery(e.target.value)}
            >
                <Icon slot="prefix" icon="vaadin:search" />
            </TextField>
            <Grid
                items={schemeList}
                style={{ width: '100%' }}
                theme="no-border"
            >
                <GridColumn path="schemeName" header="Scheme Name"
                            renderer={({ item }) => (
                                <a
                                    href="#"
                                    style={{
                                        whiteSpace: 'normal',
                                        overflow: 'visible',
                                        color: 'blue',
                                        textDecoration: 'underline'
                                    }}
                                    onClick={(e) => {
                                        e.preventDefault(); // Prevent the default anchor behavior
                                        setDialogOpened(true); // Open dialog
                                        fetchSchemeDetails(item.amfiCode);
                                    }}
                                >
                                    {item.schemeName}
                                </a>
                            )}
                />
                <GridColumn path="fundHouse" header="AMC"
                            renderer={({ item }) => (
                                <span style={{ whiteSpace: 'normal', overflow: 'visible' }}>
                                    {item.amcName}
                                </span>
                            )}
                />
            </Grid>

            <Dialog
                opened={dialogOpened}
                onOpenedChanged={(e) => setDialogOpened(e.detail.value)}
                headerTitle="Scheme Details"
            >
                <div>
                    {isLoading ? (
                        <p>Loading data...</p>
                    ) : schemeDetails ? (
                        <>
                            <div style={{ marginBottom: '20px' }}>
                                <DatePicker
                                    label="Select Date"
                                    value={selectedDate || ''}
                                    onValueChanged={handleDateChange}
                                    max={today} // Prevent selecting future dates
                                />
                            </div>

                            <p><strong>AMC:</strong> {schemeDetails.amc}</p>
                            <p><strong>Scheme Code:</strong> {schemeDetails.schemeCode}</p>
                            <p><strong>ISIN:</strong> {schemeDetails.isin}</p>
                            <p><strong>Scheme Name:</strong> {schemeDetails.schemeName}</p>
                            <p><strong>NAV:</strong> {schemeDetails.nav}</p>
                            <p><strong>Date:</strong> {schemeDetails.date}</p>
                            <p><strong>Scheme Type:</strong> {schemeDetails.schemeType}</p>
                        </>
                    ) : (
                        <p>No details available.</p>
                    )}
                </div>
                <div slot="footer">
                    <Button onClick={() => setDialogOpened(false)}>Close</Button>
                </div>
            </Dialog>
        </div>
    );
}
