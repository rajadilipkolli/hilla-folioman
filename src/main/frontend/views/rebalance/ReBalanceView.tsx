import React, {useState} from 'react';
import './ReBalance.css';
import {reBalance} from "Frontend/generated/ReBalanceController";
import Fund from 'Frontend/generated/com/app/folioman/portfolio/models/request/Fund';
import InvestmentResponse from 'Frontend/generated/com/app/folioman/portfolio/models/response/InvestmentResponse';

export default function ReBalanceView() {
    const [funds, setFunds] = useState<Fund[]>([
        {value: 0, ratio: 0},
        {value: 0, ratio: 0}
    ]);
    const [amountToInvest, setAmountToInvest] = useState('');
    const [result, setResult] = useState('');

    const handleAddFund = () => {
        setFunds([...funds, {value: 0, ratio: 0}]);
    };

    // Modified handleFundChange to accept value as a number
    const handleFundChange = (index: number, field: keyof Fund, value: number) => {
        const updatedFunds = [...funds];
        updatedFunds[index][field] = value;
        setFunds(updatedFunds);
    };

    const handleReBalance = async () => {
        if (funds.some(fund => fund.value < 0 || fund.ratio < 0 || fund.ratio > 100)) {
            setResult('Invalid fund values or ratios. Please check your inputs.');
            return;
        }

        if (Number(amountToInvest) <= 0) {
            setResult('Amount to invest must be greater than zero.');
            return;
        }

        try {
            const fundsData = funds.map(fund => ({
                value: fund.value,
                ratio: fund.ratio / 100,  // Convert ratio to fraction
            }));

            // Call server-side method and get the InvestmentResponse
            const response: InvestmentResponse | undefined = await reBalance({
                funds: fundsData,
                amountToInvest: Number(amountToInvest),
            });

            // Check if response and investments exist before processing
            if (response && response.investments) {
                const resultString = response.investments
                    .map((investment: number | undefined, index: number) => {
                        if (investment !== undefined) {
                            return `Fund ${index + 1}: Invest ₹${investment.toFixed(2)}`;
                        } else {
                            return `Fund ${index + 1}: No investment needed`;
                        }
                    })
                    .join(', ');

                setResult(resultString);
            } else {
                setResult('No investments data available');
            }
        } catch (error) {
            console.error('Error calculating rebalance:', error);
            setResult(error instanceof Error ? error.message : 'An unknown error occurred during rebalance calculation');
        }
    };


    return (
        <div className="App">
            <h1>Investment ReBalancing Calculator</h1>

            {funds.map((fund, index) => (
                <div key={index}>
                    <label>
                        Fund {index + 1} Current Value (₹):
                        <input
                            type="number"
                            value={fund.value}
                            onChange={(e) => handleFundChange(index, 'value', Number(e.target.value))}
                            placeholder={`Enter current value of Fund ${index + 1}`}
                            aria-label={`Current value of Fund ${index + 1}`}
                        />
                    </label>
                    <label>
                        Fund {index + 1} Desired Ratio (%):
                        <input
                            type="number"
                            value={fund.ratio}
                            onChange={(e) => handleFundChange(index, 'ratio', Number(e.target.value))}
                            placeholder={`Enter desired ratio of Fund ${index + 1}`}
                            aria-label={`Current desired ratio of Fund ${index + 1}`}
                        />
                    </label>
                </div>
            ))}

            <button onClick={handleAddFund} aria-label="Add Another Fund">Add Another Fund</button>

            <div>
                <label>
                    Amount to Invest (₹):
                    <input
                        type="number"
                        value={amountToInvest}
                        onChange={(e) => setAmountToInvest(e.target.value)}
                        placeholder="Enter amount to invest"
                        aria-label="Amount to invest"
                    />
                </label>
            </div>

            <button onClick={handleReBalance} aria-label="Calculate Rebalance">Calculate Rebalance</button>

            {result && (
                <div id="result">
                    <h3>Result:</h3>
                    <p>{result}</p>
                </div>
            )}
        </div>
    );
}
