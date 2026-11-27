import FundDetailProjection from 'Frontend/generated/com/app/folioman/mfschemes/rest/dtos/FundDetailProjection';
import MFSchemeDTO from 'Frontend/generated/com/app/folioman/mfschemes/rest/dtos/MFSchemeDTO';
import React, { useEffect, useState } from 'react';
import { SchemeController } from 'Frontend/generated/endpoints';
import { authenticatedFetch } from 'Frontend/auth';
import {
  Grid,
  GridColumn,
  Icon,
  TextField,
  Dialog,
  Button,
  DatePicker,
  Card,
  Notification,
  Details,
  Scroller,
  FormLayoutResponsiveStep,
} from '@vaadin/react-components';

const layoutSteps: FormLayoutResponsiveStep[] = [
  { minWidth: 0, columns: 1, labelsPosition: 'top' },
  { minWidth: '520px', columns: 2, labelsPosition: 'top' },
];

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
      setIsLoading(true);
      SchemeController.fetchSchemes(query)
        .then((fetchedResults) => {
          const filteredResults = (fetchedResults ?? []).filter(
            (item): item is FundDetailProjection => item !== undefined,
          );
          setSchemeList(filteredResults);
          setIsLoading(false);
        })
        .catch((error) => {
          console.error('Error fetching schemes:', error);
          setSchemeList([]); // Clear the list in case of an error
          setIsLoading(false);

          Notification.show('Failed to fetch schemes. Please try again.', {
            position: 'middle',
            theme: 'error',
            duration: 3000,
          });
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

    authenticatedFetch(fetchUrl, { method: 'GET' })
      .then((response) => {
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
      .catch((error) => {
        console.error('Error fetching scheme details:', error);
        setIsLoading(false);
        Notification.show('Failed to load scheme details. Please try again.', {
          position: 'middle',
          theme: 'error',
          duration: 3000,
        });
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
    <div className="p-m">
      <Card theme="outlined">
        <div className="flex flex-col md:flex-row justify-between items-start mb-m">
          <div>
            <h2 className="text-xl m-0">Mutual Fund Schemes</h2>
            <p className="text-secondary m-0 mt-s">
              Search for mutual funds and view their details
            </p>
          </div>
        </div>

        <Details summary="Search Tips" opened={false}>
          <div className="p-s text-secondary">
            <p>
              Enter at least 4 characters to search for mutual fund schemes.
            </p>
            <p>
              You can search by fund name, AMC (Asset Management Company), or
              scheme code.
            </p>
            <p>
              Click on a scheme name to view detailed information and historical
              NAV data.
            </p>
          </div>
        </Details>

        <div className="mt-m">
          <TextField
            clearButtonVisible
            placeholder="Search mutual fund schemes..."
            className="w-full"
            helperText={
              query.length <= 3
                ? 'Enter at least 4 characters to search'
                : `Found ${schemeList.length} matching schemes`
            }
            value={query}
            onChange={(e) => setQuery(e.target.value)}>
            <Icon slot="prefix" icon="vaadin:search" />
          </TextField>
        </div>

        <div className="mt-m">
          {isLoading && query.length > 3 ? (
            <div className="flex items-center justify-center p-m">
              <span className="text-secondary">Loading schemes...</span>
            </div>
          ) : query.length > 3 && schemeList.length === 0 ? (
            <div className="flex items-center justify-center p-m">
              <span className="text-secondary">
                No schemes found matching your search
              </span>
            </div>
          ) : (
            <Scroller className="max-h-96">
              <Grid items={schemeList} className="w-full" theme="row-stripes">
                <GridColumn
                  path="schemeName"
                  header="Scheme Name"
                  renderer={({ item }) => (
                    <Button
                      theme="tertiary"
                      className="text-left p-0"
                      onClick={() => {
                        setDialogOpened(true);
                        fetchSchemeDetails(item.amfiCode);
                      }}>
                      <div className="line-clamp-2 overflow-hidden">
                        {item.schemeName}
                      </div>
                    </Button>
                  )}
                />
                <GridColumn
                  path="fundHouse"
                  header="AMC"
                  renderer={({ item }) => (
                    <div className="overflow-hidden text-ellipsis">
                      {item.amcName}
                    </div>
                  )}
                />
              </Grid>
            </Scroller>
          )}
        </div>
      </Card>

      <Dialog
        opened={dialogOpened}
        onOpenedChanged={(e) => setDialogOpened(e.detail.value)}>
        <div
          slot="header"
          className="w-full flex justify-between items-center pr-m">
          <h2 className="m-0 text-l flex-1">
            {schemeDetails?.schemeName || 'Fund Scheme Details'}
          </h2>
          <Button
            theme="tertiary-inline"
            onClick={() => setDialogOpened(false)}
            aria-label="Close"
            className="m-0 p-0">
            <Icon icon="vaadin:close" />
          </Button>
        </div>
        <div
          className="flex flex-col gap-m mt-s"
          style={{ maxWidth: '800px', width: '100%' }}>
          {isLoading ? (
            <div className="flex items-center justify-center p-m">
              <span className="text-secondary">Loading scheme details...</span>
            </div>
          ) : schemeDetails ? (
            <div className="flex flex-col gap-m">
              <div className="flex justify-between items-center mb-s">
                <DatePicker
                  label="Select Date for NAV"
                  value={selectedDate || ''}
                  onValueChanged={handleDateChange}
                  max={today}
                  theme="small"
                  helper-text="View NAV for specific date"
                />
              </div>

              {/* NAV Value and Date displayed prominently */}
              <div className="bg-success-10 p-m rounded flex flex-col md:flex-row justify-between items-center box-border">
                <div className="text-center md:text-left mb-s md:mb-0">
                  <div className="text-secondary text-sm">NAV Value</div>
                  <div className="text-xl font-bold text-success">
                    {schemeDetails.nav || 'N/A'}
                  </div>
                </div>
                <div className="text-center md:text-right">
                  <div className="text-secondary text-sm">NAV Date</div>
                  <div className="text-lg">{schemeDetails.date || 'N/A'}</div>
                </div>
              </div>

              {/* Fixed grid layout for consistent sizing across all rows */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-m">
                <div className="bg-contrast-5 p-m rounded">
                  <div className="text-secondary text-sm mb-xs">
                    AMFI Scheme Code
                  </div>
                  <div>{schemeDetails.schemeCode || 'N/A'}</div>
                </div>

                <div className="bg-contrast-5 p-m rounded">
                  <div className="text-secondary text-sm mb-xs">ISIN</div>
                  <div>{schemeDetails.isin || 'N/A'}</div>
                </div>

                <div className="bg-contrast-5 p-m rounded">
                  <div className="text-secondary text-sm mb-xs">
                    Asset Management Company
                  </div>
                  <div>{schemeDetails.amc || 'N/A'}</div>
                </div>

                <div className="bg-contrast-5 p-m rounded">
                  <div className="text-secondary text-sm mb-xs">
                    Scheme Type
                  </div>
                  <div>{schemeDetails.schemeType || 'N/A'}</div>
                </div>
              </div>
            </div>
          ) : (
            <div className="p-m text-center text-error">
              No details available for this scheme.
            </div>
          )}
        </div>
      </Dialog>
    </div>
  );
}
