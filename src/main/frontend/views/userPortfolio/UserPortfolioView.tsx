import React, { useState } from "react";
import { Button, DatePicker, Grid, GridColumn, GridSortColumn, TextField } from "@vaadin/react-components";
import PortfolioResponse from "Frontend/generated/com/app/folioman/portfolio/models/response/PortfolioResponse";
import MonthlyInvestmentResponse from "Frontend/generated/com/app/folioman/portfolio/models/response/MonthlyInvestmentResponse";
import { getPortfolio } from "Frontend/generated/ImportMutualFundController";
import { getTotalInvestmentsByPanPerMonth } from "Frontend/generated/UserTransactionsController";

// Define the SortDirection type manually as a union of possible values.
type SortDirection = 'asc' | 'desc' | null;

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

    // Custom sort handler for client-side sorting by Year and Month
    const sortMonthlyInvestments = (direction: SortDirection) => {
        const sortedData = [...(monthlyInvestments || [])].sort((a, b) => {
            const aMonth = a.monthNumber ?? 0; // Use 0 or a default value if undefined
            const bMonth = b.monthNumber ?? 0; // Use 0 or a default value if undefined
            const aYear = a.year ?? 0; // Use 0 or a default value if undefined
            const bYear = b.year ?? 0; // Use 0 or a default value if undefined

            if (aYear === bYear) {
                return direction === 'asc' ? aMonth - bMonth : bMonth - aMonth;
            }
            return direction === 'asc' ? aYear - bYear : bYear - aYear;
        });
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
