import React, { useState } from 'react';
import { reBalance } from 'Frontend/generated/ReBalanceController';
import Fund from 'Frontend/generated/com/app/folioman/portfolio/models/request/Fund';
import InvestmentResponse from 'Frontend/generated/com/app/folioman/portfolio/models/response/InvestmentResponse';
import {
  Button,
  FormLayout,
  FormLayoutResponsiveStep,
  TextField,
  Card,
  Notification,
  Icon,
  Scroller,
  Details,
} from '@vaadin/react-components';

const layoutSteps: FormLayoutResponsiveStep[] = [
  { minWidth: 0, columns: 1, labelsPosition: 'top' },
  { minWidth: '520px', columns: 2, labelsPosition: 'top' },
];

export default function ReBalanceView() {
  const [funds, setFunds] = useState<Fund[]>([
    { value: 0, ratio: 0 },
    { value: 0, ratio: 0 },
  ]);
  const [amountToInvest, setAmountToInvest] = useState('');
  const [result, setResult] = useState<InvestmentResponse | null>(null);
  const [isCalculating, setIsCalculating] = useState(false);
  const [totalRatio, setTotalRatio] = useState(0);
  const [hasInitializedRatio, setHasInitializedRatio] = useState(false);

  const isValidForm: boolean = totalRatio === 100 && Number(amountToInvest) > 0;

  // Initialize equal distribution of ratios
  const initializeEqualRatios = () => {
    if (funds.length > 0 && !hasInitializedRatio) {
      const equalRatio = 100 / funds.length;
      const updatedFunds = funds.map((fund) => ({
        ...fund,
        ratio: equalRatio,
      }));
      setFunds(updatedFunds);
      setTotalRatio(100);
      setHasInitializedRatio(true);
    }
  };

  // Call this once after component mounts
  React.useEffect(() => {
    initializeEqualRatios();
  }, []);

  const handleAddFund = () => {
    const newFunds = [...funds, { value: 0, ratio: 0 }];
    setFunds(newFunds);

    // Recalculate ratios for equal distribution
    const equalRatio = 100 / newFunds.length;
    const updatedFunds = newFunds.map((fund) => ({
      ...fund,
      ratio: equalRatio,
    }));
    setFunds(updatedFunds);
    setTotalRatio(100);
  };

  const handleRemoveFund = (index: number) => {
    if (funds.length <= 2) {
      Notification.show('You need at least two funds for rebalancing', {
        position: 'middle',
        theme: 'error',
        duration: 3000,
      });
      return;
    }

    const updatedFunds = funds.filter((_, i) => i !== index);

    // Recalculate ratios for equal distribution
    const equalRatio = 100 / updatedFunds.length;
    const balancedFunds = updatedFunds.map((fund) => ({
      ...fund,
      ratio: equalRatio,
    }));

    setFunds(balancedFunds);
    setTotalRatio(100);
  };

  // Handle fund value or ratio changes
  const handleFundChange = (index: number, field: keyof Fund, value: number) => {
    const updatedFunds = [...funds];
    updatedFunds[index][field] = value;
    setFunds(updatedFunds);

    // Recalculate total ratio when ratios change
    if (field === 'ratio') {
      const newTotalRatio = updatedFunds.reduce((sum, fund) => sum + fund.ratio, 0);
      setTotalRatio(newTotalRatio);
    }
  };

  // Validate the entered number in TextField
  const validateNumber = (value: string): boolean => {
    return /^[0-9]*\.?[0-9]*$/.test(value);
  };

  const handleReBalance = async () => {
    if (funds.some((fund) => fund.value < 0 || fund.ratio < 0 || fund.ratio > 100)) {
      Notification.show('Invalid fund values or ratios. Please check your inputs.', {
        position: 'middle',
        theme: 'error',
        duration: 3000,
      });
      return;
    }

    if (Number(amountToInvest) <= 0) {
      Notification.show('Amount to invest must be greater than zero.', {
        position: 'middle',
        theme: 'error',
        duration: 3000,
      });
      return;
    }

    try {
      setIsCalculating(true);
      const fundsData = funds.map((fund) => ({
        value: fund.value,
        ratio: fund.ratio / 100, // Convert ratio to fraction
      }));

      // Call server-side method and get the InvestmentResponse
      const response: InvestmentResponse | undefined = await reBalance({
        funds: fundsData,
        amountToInvest: Number(amountToInvest),
      });

      // Store the full response object for display
      if (response) {
        setResult(response);
        Notification.show('Rebalance calculation completed successfully!', {
          position: 'bottom-end',
          theme: 'success',
          duration: 3000,
        });
      } else {
        Notification.show('No investment data available', {
          position: 'middle',
          theme: 'error',
          duration: 3000,
        });
      }
    } catch (error: unknown) {
      console.error('Error calculating rebalance:', error);
      let errorMessage = 'An unknown error occurred during rebalance calculation';

      if (error instanceof TypeError) {
        errorMessage = 'There was a problem with the data types in the calculation.';
      } else if (error instanceof Error) {
        errorMessage = error.message;
      }

      Notification.show(errorMessage, {
        position: 'middle',
        theme: 'error',
        duration: 3000,
      });
    } finally {
      setIsCalculating(false);
    }
  };

  return (
    <div className="p-m">
      <Card theme="outlined">
        <div className="flex flex-col md:flex-row justify-between items-start mb-m">
          <div>
            <h2 className="text-xl m-0">Investment ReBalancing Calculator</h2>
            <p className="text-secondary m-0 mt-s">Calculate optimal fund allocation for your investment strategy</p>
          </div>
          <div
            className={`mt-s md:mt-0 px-s py-xs rounded-full font-medium ${
              totalRatio === 100 ? 'bg-success-10 text-success' : 'bg-error-10 text-error'
            }`}>
            {totalRatio}% Allocated
          </div>
        </div>

        <Details summary="About Rebalancing" opened={false}>
          <div className="p-s text-secondary">
            <p>Rebalancing is the process of realigning portfolio assets to maintain your desired asset allocation.</p>
            <p>
              Enter your current fund values, desired ratios, and additional investment amount to calculate the optimal
              way to invest.
            </p>
          </div>
        </Details>

        <Scroller className="mt-m" style={{ maxHeight: '70vh' }}>
          {funds.map((fund, index) => (
            <Card className="mb-m p-s" key={index} theme="tertiary">
              <div className="flex items-center justify-between mb-s">
                <h3 className="m-0">Fund {index + 1}</h3>
                {funds.length > 2 && (
                  <Button
                    theme="error tertiary small"
                    onClick={() => handleRemoveFund(index)}
                    aria-label={`Remove Fund ${index + 1}`}>
                    <Icon icon="vaadin:trash" />
                  </Button>
                )}
              </div>

              <FormLayout responsiveSteps={layoutSteps}>
                <TextField
                  label={`Current Value (₹)`}
                  value={fund.value.toString()}
                  onChange={(e) => {
                    const value = e.target.value;
                    if (value === '' || validateNumber(value)) {
                      handleFundChange(index, 'value', Number(value) || 0);
                    }
                  }}
                  placeholder={`Enter current value`}
                  aria-label={`Current value of Fund ${index + 1}`}
                  helperText="Current market value of your investment"
                />
                <TextField
                  label={`Desired Ratio (%)`}
                  value={fund.ratio.toString()}
                  onChange={(e) => {
                    const value = e.target.value;
                    if (value === '' || validateNumber(value)) {
                      const numValue = Number(value) || 0;
                      // Ensure ratio doesn't exceed 100%
                      if (numValue <= 100) {
                        handleFundChange(index, 'ratio', numValue);
                      }
                    }
                  }}
                  placeholder={`Enter desired ratio`}
                  aria-label={`Current desired ratio of Fund ${index + 1}`}
                  helperText="Target percentage in your portfolio"
                />
              </FormLayout>
            </Card>
          ))}

          <div className="mb-m">
            <Button theme="tertiary" onClick={handleAddFund} aria-label="Add Another Fund">
              <Icon icon="vaadin:plus" slot="prefix" /> Add Another Fund
            </Button>
          </div>

          <Card theme="primary" className="mb-m p-s">
            <FormLayout responsiveSteps={layoutSteps}>
              <TextField
                label="Amount to Invest (₹)"
                value={amountToInvest}
                onChange={(e) => {
                  const value = e.target.value;
                  if (value === '' || validateNumber(value)) {
                    setAmountToInvest(value);
                  }
                }}
                placeholder="Enter amount to invest"
                aria-label="Amount to invest"
                className="mb-m"
              />
            </FormLayout>

            {totalRatio !== 100 && (
              <div className="text-error mb-m">
                <Icon icon="vaadin:exclamation-circle" style={{ marginRight: '8px' }} />
                Total allocation: {totalRatio}% (must equal 100%)
              </div>
            )}

            <Button
              theme="primary"
              onClick={handleReBalance}
              disabled={!isValidForm || isCalculating}
              aria-label="Calculate Rebalance"
              className="w-full">
              {isCalculating ? 'Calculating...' : 'Calculate Rebalance'}
            </Button>
          </Card>

          {result && result.investments && (
            <Card theme="success" className="p-m">
              <h3>Recommended Investment</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-s">
                {result.investments.map((investment: number | undefined, index: number) => (
                  <Card key={index} theme="tertiary" className="p-s">
                    <div className="font-medium">Fund {index + 1}</div>
                    {investment !== undefined ? (
                      <div className="text-xl">₹{investment.toFixed(2)}</div>
                    ) : (
                      <div className="text-secondary">No investment needed</div>
                    )}
                  </Card>
                ))}
              </div>

              <div className="mt-m">
                <Details summary="Rebalancing Details" opened={false}>
                  <div className="p-s">
                    <p>Total Current Value: ₹{funds.reduce((sum, fund) => sum + fund.value, 0).toFixed(2)}</p>
                    <p>Additional Investment: ₹{Number(amountToInvest).toFixed(2)}</p>
                    <p>
                      Total Portfolio Value After Rebalance: ₹
                      {(funds.reduce((sum, fund) => sum + fund.value, 0) + Number(amountToInvest)).toFixed(2)}
                    </p>
                  </div>
                </Details>
              </div>
            </Card>
          )}
        </Scroller>
      </Card>
    </div>
  );
}
