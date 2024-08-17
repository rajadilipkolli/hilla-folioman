import FundDetailProjection
    from "Frontend/generated/com/example/application/mfschemes/models/projection/FundDetailProjection";
import React, { useEffect, useState } from "react";
import { SchemeController } from "Frontend/generated/endpoints";
import { Grid, GridColumn } from "@vaadin/react-components";
import { TextField } from "@vaadin/react-components";
import { Icon } from "@vaadin/react-components";

export default function MfSchemesView() {
    const [query, setQuery] = useState('');
    const [schemeList, setSchemeList] = useState<FundDetailProjection[]>([]);

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
                <GridColumn path="fundHouse" header="AMC" autoWidth />
                <GridColumn path="schemeName" header="Scheme Name" autoWidth />
            </Grid>
        </div>
    );
}
