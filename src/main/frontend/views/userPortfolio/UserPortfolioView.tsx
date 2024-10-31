import React, { useState } from "react";
import { Button, DatePicker, Grid, GridColumn, GridSortColumn, GridSorterDirection, TextField } from "@vaadin/react-components";
import PortfolioResponse from "Frontend/generated/com/app/folioman/portfolio/models/response/PortfolioResponse";
import MonthlyInvestmentResponse from "Frontend/generated/com/app/folioman/portfolio/models/response/MonthlyInvestmentResponse";
import { getPortfolio } from "Frontend/generated/ImportMutualFundController";
import { getTotalInvestmentsByPanPerMonth } from "Frontend/generated/UserTransactionsController";

export default function UserPortfolioView() {
    const [pan, setPan] = useState<string>('');
    const [asOfDate, setAsOfDate] = useState<string | null>(null);
    const [portfolio, setPortfolio] = useState<PortfolioResponse | null>(null);
    const [monthlyInvestments, setMonthlyInvestments] = useState<MonthlyInvestmentResponse[] | null>(null);
    const [error, setError] = useState<string | null>(null);

    const fetchPortfolio = async () => {
        if (!pan) {
            setError('PAN is required');
            return;
        }
        setError(null);
        setMonthlyInvestments(null);  // Clear monthly investments when fetching portfolio
        try {
            const response = await getPortfolio(pan, asOfDate ?? undefined);
            setPortfolio(response || null);
            setError(null);
        } catch (e) {
            setError(`Failed to fetch portfolio: ${(e as Error).message}`);
        }
    };

    const fetchMonthlyInvestments = async () => {
        if (!pan) {
            setError('PAN is required');
            return;
        }
        setError(null);
        setPortfolio(null);  // Clear portfolio when fetching monthly investments
        try {
            const response = await getTotalInvestmentsByPanPerMonth(pan);
            setMonthlyInvestments((response || []).filter((item): item is MonthlyInvestmentResponse => item !== undefined));
            setError(null);
        } catch (e) {
            setError(`Failed to fetch monthly investments: ${(e as Error).message}`);
        }
    };

    const DEFAULT_YEAR = 0;
    const DEFAULT_MONTH = 0;
    // Custom sort handler for client-side sorting by Year and Month
    const sortMonthlyInvestments = (direction: GridSorterDirection) => {
        const sortedData = monthlyInvestments?.map(item => ({
                    ...item,
                    monthNumber: item.monthNumber ?? DEFAULT_MONTH,
                    year: item.year ?? DEFAULT_YEAR
            })).sort((a, b) => {
                    if (a.year === b.year) {
                            return direction === 'asc' ? a.monthNumber - b.monthNumber : b.monthNumber - a.monthNumber;
                        }
                    return direction === 'asc' ? a.year - b.year : b.year - a.year;
                }) ?? [];
        setMonthlyInvestments(sortedData);
    };

    return (
        <div style={{ padding: '20px' }}>
            <h2>Portfolio Lookup</h2>
            <div>
                <TextField
                    label="PAN"
                    value={pan}
                    onValueChanged={(e: any) => setPan(e.target.value)}
                    required
                />
            </div>
            <div>
                <DatePicker
                    label="As of Date (optional)"
                    value={asOfDate ?? ''}
                    onValueChanged={(e: any) => setAsOfDate(e.target.value)}
                    placeholder="YYYY-MM-DD"
                />
            </div>
            <div style={{ display: 'flex', gap: '10px' }}>
                <Button onClick={fetchPortfolio} theme="primary">Get Portfolio</Button>
                <Button onClick={fetchMonthlyInvestments} theme="secondary">Get Monthly Investments</Button>
            </div>

            {error && <div style={{ color: 'red' }}>{error}</div>}

            {portfolio && (
                <div>
                    <p><strong>Total Value:</strong> {portfolio.totalPortfolioValue}</p>

                    <Grid items={portfolio.portfolioDetailsDTOS}>
                        <GridSortColumn path="schemeName" header="Scheme Name"
                            renderer={({ item }) => (
                                <span style={{ whiteSpace: 'normal', overflow: 'visible' }}>
                                    {item.schemeName}
                                </span>
                            )}
                        />
                        <GridSortColumn path="folioNumber" header="Folio Number" />
                        <GridSortColumn path="totalValue" header="Total Value" />
                        <GridSortColumn path="date" header="As of Date" />
                    </Grid>
                </div>
            )}

            {monthlyInvestments && (
                <div>
                    <h3>Monthly Investments</h3>
                    <Grid items={monthlyInvestments} className="header-wrap" aria-label="Monthly Investment Summary">
                        <GridSortColumn
                            path="year"
                            header="Year"
                            onDirectionChanged={(e) => sortMonthlyInvestments(e.detail.value)}
                        />
                        <GridColumn path="monthNumber" header="Month" />
                        <GridSortColumn path="investmentPerMonth" header="Amount Invested In current Month" />
                        <GridColumn path="cumulativeInvestment" header="Total Cumulative Investment" />
                    </Grid>
                </div>
            )}
        </div>
    );
}
