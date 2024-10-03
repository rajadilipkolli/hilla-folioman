import React, {useState} from "react";
import {Button, DatePicker, Grid, GridColumn, TextField} from "@vaadin/react-components";
import PortfolioResponse from "Frontend/generated/com/app/folioman/portfolio/models/response/PortfolioResponse";
import {getPortfolio} from "Frontend/generated/ImportMutualFundController";

export default function UserPortfolioView() {
    const [pan, setPan] = useState<string>('');
    const [asOfDate, setAsOfDate] = useState<string | null>(null);
    const [portfolio, setPortfolio] = useState<PortfolioResponse | null>(null);
    const [error, setError] = useState<string | null>(null);

    const fetchPortfolio = async () => {
        if (!pan) {
            setError('PAN is required');
            return;
        }
        setError(null);
        try {
            // Call the getPortfolio function with pan as path parameter and asOfDate as query parameter
            const response = await getPortfolio(pan, asOfDate ?? undefined);
            setPortfolio(response || null);
        } catch (e) {
            setError('Failed to fetch portfolio');
        }
    };

    return (
        <div style={{padding: '20px'}}>
            <h2>Portfolio Lookup</h2>
            <div>
                <TextField
                    label="PAN"
                    value={pan}
                    onInput={(e: any) => setPan(e.target.value)}
                    required
                />
            </div>
            <div>
                <DatePicker
                    label="As of Date (optional)"
                    value={asOfDate ?? ''}
                    onChange={(e: any) => setAsOfDate(e.target.value)}
                    placeholder="YYYY-MM-DD"
                />
            </div>
            <div>
                <Button onClick={fetchPortfolio} theme="primary">Get Portfolio</Button>
            </div>

            {error && <div style={{color: 'red'}}>{error}</div>}

            {portfolio && (
                <Grid items={portfolio.portfolioDetailsDTOS}>
                    <GridColumn path="schemeName" header="Scheme Name"
                                renderer={({ item }) => (
                                    <span style={{ whiteSpace: 'normal', overflow: 'visible' }}>
                                    {item.schemeName}
                                </span>
                                )}
                    />
                    <GridColumn path="folioNumber" header="Folio Number"/>
                    <GridColumn path="totalValue" header="Total Value"/>
                    <GridColumn path="date" header="As of Date"/>
                </Grid>
            )}
        </div>
    );
}
