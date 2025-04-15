import React, { useState, useRef } from 'react';
import {
  Button,
  DatePicker,
  Grid,
  GridColumn,
  GridSortColumn,
  GridSorterDirection,
  TextField,
  HorizontalLayout,
  VerticalLayout,
  FormLayout,
  Card,
  Notification,
} from '@vaadin/react-components';
import PortfolioResponse from 'Frontend/generated/com/app/folioman/portfolio/models/response/PortfolioResponse';
import MonthlyInvestmentResponseDTO from 'Frontend/generated/com/app/folioman/portfolio/models/dto/MonthlyInvestmentResponseDTO';
import YearlyInvestmentResponseDTO from 'Frontend/generated/com/app/folioman/portfolio/models/dto/YearlyInvestmentResponseDTO';
import { getPortfolio } from 'Frontend/generated/ImportMutualFundController';
import {
  getTotalInvestmentsByPanPerMonth,
  getTotalInvestmentsByPanPerYear,
} from 'Frontend/generated/UserTransactionsController';
import './user-portfolio.css';

// Helper function to validate PAN is provided
function ensurePanIsProvided(pan: string, setErrorFn: (msg: string | null) => void): boolean {
  if (!pan) {
    setErrorFn('PAN is required');
    return false;
  }
  setErrorFn(null);
  return true;
}

export default function UserPortfolioView() {
  const [pan, setPan] = useState<string>('');
  const [asOfDate, setAsOfDate] = useState<Date | null>(null);
  const [portfolio, setPortfolio] = useState<PortfolioResponse | null>(null);
  const [monthlyInvestments, setMonthlyInvestments] = useState<MonthlyInvestmentResponseDTO[] | null>(null);
  const [yearlyInvestments, setYearlyInvestments] = useState<{ year: number; amount: number }[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [showYearlyChart, setShowYearlyChart] = useState<boolean>(false);
  const [showMonthlyData, setShowMonthlyData] = useState<boolean>(false);
  const chartRef = useRef<HTMLDivElement>(null);

  const fetchPortfolio = async () => {
    if (!ensurePanIsProvided(pan, setError)) return;
    setMonthlyInvestments(null); // Clear monthly investments when fetching portfolio
    setShowYearlyChart(false); // Hide yearly chart when fetching new portfolio
    setShowMonthlyData(false); // Hide monthly data
    try {
      // Convert Date to string format expected by backend API if it exists
      const dateStr = asOfDate ? asOfDate.toISOString().split('T')[0] : undefined;
      const response = await getPortfolio(pan, dateStr);
      setPortfolio(response || null);
      setError(null);
    } catch (e) {
      setError(`Failed to fetch portfolio: ${(e as Error).message}`);
    }
  };

  const fetchMonthlyInvestments = async () => {
    if (!ensurePanIsProvided(pan, setError)) return;
    setPortfolio(null); // Clear portfolio when fetching monthly investments
    setShowYearlyChart(false); // Hide yearly chart when fetching new monthly data
    try {
      const response = await getTotalInvestmentsByPanPerMonth(pan);
      setMonthlyInvestments(
        (response || []).filter((item): item is MonthlyInvestmentResponseDTO => item !== undefined),
      );
      setShowMonthlyData(true); // Show monthly data
      setError(null);
    } catch (e) {
      setError(`Failed to fetch monthly investments: ${(e as Error).message}`);
    }
  };

  const fetchYearlyInvestments = async () => {
    if (!ensurePanIsProvided(pan, setError)) return;

    setPortfolio(null); // Clear portfolio when fetching yearly investments
    setShowMonthlyData(false); // Hide monthly data grid

    try {
      // Direct call to backend for yearly investment data
      const response = await getTotalInvestmentsByPanPerYear(pan);
      const yearlyData = (response || [])
        .filter((item): item is YearlyInvestmentResponseDTO => item !== undefined)
        .map((item) => ({
          year: item.year ?? DEFAULT_YEAR,
          amount: item.yearlyInvestment ?? 0,
        }))
        .sort((a, b) => a.year - b.year);

      setYearlyInvestments(yearlyData);
      setShowYearlyChart(true);
      setError(null);
    } catch (e) {
      setError(`Failed to fetch yearly investments: ${(e as Error).message}`);
    }
  };

  const DEFAULT_YEAR = 0;
  const DEFAULT_MONTH = 0;

  // Custom sort handler for client-side sorting by Year and Month
  const sortMonthlyInvestments = (direction: GridSorterDirection) => {
    const sortedData =
      monthlyInvestments
        ?.map((item) => ({
          ...item,
          monthNumber: item.monthNumber ?? DEFAULT_MONTH,
          year: item.year ?? DEFAULT_YEAR,
        }))
        .sort((a, b) => {
          if (a.year === b.year) {
            return direction === 'asc' ? a.monthNumber - b.monthNumber : b.monthNumber - a.monthNumber;
          }
          return direction === 'asc' ? a.year - b.year : b.year - a.year;
        }) ?? [];
    setMonthlyInvestments(sortedData);
  };

  // Function to render the yearly investment bar chart
  const renderYearlyChart = () => {
    // Handle no data or minimal data scenario
    if (yearlyInvestments.length === 0) {
      return (
        <Card className="fade-in">
          <div className="empty-chart-message" aria-live="polite">
            <h3>No yearly investment data available</h3>
            <p>There is no investment data to display for the selected PAN.</p>
          </div>
        </Card>
      );
    }

    const maxValue = Math.max(...yearlyInvestments.map((item) => item.amount), 1); // Ensure non-zero for division
    const barHeight = 300; // Maximum height for the tallest bar

    // Calculate Y-axis tick values (from bottom to top)
    const yAxisValues = [0, 0.25, 0.5, 0.75, 1].map((ratio) => maxValue * ratio);

    return (
      <Card className="fade-in">
        <h3 style={{ textAlign: 'center', margin: 'var(--lumo-space-m) 0' }}>Yearly Investment Summary</h3>
        <div
          className="chart-inner"
          ref={chartRef}
          role="img"
          aria-label="Bar chart showing yearly investment amounts from years ${yearlyInvestments[0]?.year} to ${yearlyInvestments[yearlyInvestments.length-1]?.year}">
          <div className="chart-y-axis" aria-hidden="true">
            {yAxisValues.reverse().map((value, index) => (
              <div key={index} className="chart-y-axis-label">
                ₹{value.toLocaleString()}
              </div>
            ))}
          </div>
          {yearlyInvestments.map((item, index) => {
            const heightPercentage = (item.amount / maxValue) * 100;
            const barHeightPx = Math.max((heightPercentage / 100) * barHeight, 1); // Ensure at least 1px height for visibility

            return (
              <div key={index} className="chart-bar-group">
                <div
                  className="chart-bar"
                  style={{ height: `${barHeightPx}px` }}
                  role="graphics-symbol"
                  aria-label={`Year ${item.year}: ₹${item.amount.toLocaleString()}`}
                  aria-roledescription="bar"
                  onMouseOver={(e) => {
                    const barElement = e.currentTarget;
                    const valueElement = barElement.querySelector('div');
                    if (valueElement) {
                      valueElement.style.opacity = '1';
                    }
                  }}
                  onMouseOut={(e) => {
                    const barElement = e.currentTarget;
                    const valueElement = barElement.querySelector('div');
                    if (valueElement) {
                      valueElement.style.opacity = '0';
                    }
                  }}>
                  <div className="chart-bar-value">₹{item.amount.toLocaleString()}</div>
                </div>
                <div className="chart-bar-label">{item.year}</div>
              </div>
            );
          })}
          <div className="chart-x-axis" aria-hidden="true"></div>
        </div>
      </Card>
    );
  };

  return (
    <VerticalLayout className="fade-in" style={{ margin: '0 auto', maxWidth: '1200px' }}>
      <h2
        style={{
          fontSize: 'var(--lumo-font-size-xxl)',
          fontWeight: '600',
          margin: 'var(--lumo-space-m) 0',
          color: 'var(--lumo-primary-text-color)',
        }}>
        Portfolio Lookup
      </h2>

      <Card>
        <FormLayout
          responsiveSteps={[
            { minWidth: '0', columns: 1 },
            { minWidth: '500px', columns: 3 },
          ]}>
          <TextField
            label="PAN"
            value={pan}
            onValueChanged={(e: CustomEvent) => setPan((e.target as HTMLInputElement).value)}
            required
          />
          <DatePicker
            label="As of Date (optional)"
            value={asOfDate ? asOfDate.toISOString().split('T')[0] : ''}
            onValueChanged={(e: CustomEvent) =>
              setAsOfDate((e.target as HTMLInputElement).value ? new Date((e.target as HTMLInputElement).value) : null)
            }
            placeholder="YYYY-MM-DD"
          />
          <HorizontalLayout
            theme="spacing"
            style={{
              alignSelf: 'flex-end',
              flexWrap: 'wrap',
            }}>
            <Button onClick={fetchPortfolio} theme="primary">
              Get Portfolio
            </Button>
            <Button onClick={fetchMonthlyInvestments} theme="secondary">
              Get Monthly Investments
            </Button>
            <Button onClick={fetchYearlyInvestments} theme="tertiary">
              Get Yearly Investments
            </Button>
          </HorizontalLayout>
        </FormLayout>
      </Card>

      {error && (
        <div
          style={{
            marginTop: 'var(--lumo-space-m)',
          }}>
          {/* Use Notification component for error message */}
          <Notification theme="error" duration={0} opened={!!error} position="middle">
            {error}
          </Notification>
        </div>
      )}

      {portfolio && (
        <Card className="fade-in" style={{ marginTop: 'var(--lumo-space-m)', width: '100%' }}>
          <HorizontalLayout
            style={{
              padding: 'var(--lumo-space-m)',
              backgroundColor: 'var(--lumo-contrast-5pct)',
              justifyContent: 'space-between',
              width: '100%',
            }}>
            <span>Total Portfolio Value:</span>
            <span
              style={{
                fontWeight: 'bold',
                color: 'var(--lumo-primary-text-color)',
              }}>
              ₹{portfolio.totalPortfolioValue?.toLocaleString() || '0'}
            </span>
          </HorizontalLayout>
          <Grid
            items={portfolio.portfolioDetailsDTOS}
            style={{
              width: '100%',
              minHeight: '300px',
            }}
            theme="no-border row-stripes"
            allRowsVisible
            columnReorderingAllowed>
            <GridSortColumn
              path="schemeName"
              header="Scheme Name"
              flexGrow={2}
              autoWidth={false}
              renderer={({ item }) => (
                <span style={{ whiteSpace: 'normal', overflow: 'visible' }}>{item.schemeName}</span>
              )}
            />
            <GridSortColumn path="folioNumber" header="Folio Number" flexGrow={1} />
            <GridSortColumn
              path="totalValue"
              header="Total Value"
              flexGrow={1}
              textAlign="end"
              renderer={({ item }) => `₹${item.totalValue?.toLocaleString() || '0'}`}
            />
            <GridSortColumn path="date" header="As of Date" flexGrow={1} />
          </Grid>
        </Card>
      )}

      {showMonthlyData && monthlyInvestments && (
        <Card className="fade-in" style={{ marginTop: 'var(--lumo-space-m)', width: '100%' }}>
          <h3
            style={{
              fontSize: 'var(--lumo-font-size-xl)',
              fontWeight: '500',
              margin: 'var(--lumo-space-m)',
              color: 'var(--lumo-secondary-text-color)',
            }}>
            Monthly Investments
          </h3>
          <Grid
            items={monthlyInvestments}
            style={{
              width: '100%',
              minHeight: '300px',
            }}
            theme="no-border row-stripes"
            allRowsVisible
            columnReorderingAllowed>
            <GridSortColumn
              path="year"
              header="Year"
              flexGrow={1}
              autoWidth={false}
              onDirectionChanged={(e) => sortMonthlyInvestments(e.detail.value)}
            />
            <GridColumn path="monthNumber" header="Month" flexGrow={1} />
            <GridSortColumn
              path="investmentPerMonth"
              header="Amount Invested In Current Month"
              flexGrow={2}
              textAlign="end"
              renderer={({ item }) => `₹${item.investmentPerMonth?.toLocaleString() || '0'}`}
            />
            <GridColumn
              path="cumulativeInvestment"
              header="Total Cumulative Investment"
              flexGrow={2}
              textAlign="end"
              renderer={({ item }) => `₹${item.cumulativeInvestment?.toLocaleString() || '0'}`}
            />
          </Grid>
        </Card>
      )}

      {showYearlyChart && yearlyInvestments.length > 0 && renderYearlyChart()}
    </VerticalLayout>
  );
}
