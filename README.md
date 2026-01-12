# Hilla FolioMan: Mutual Fund Portfolio Management System

## Project Overview

FolioMan is a comprehensive mutual fund portfolio management application built using Hilla and Spring Boot. The application helps investors track, analyze, and optimize their mutual fund investments in one centralized platform.

### What FolioMan Does

- **Portfolio Tracking**: Automatically imports and tracks your mutual fund investments using CAS (Consolidated Account Statement) files
- **Portfolio Analysis**: Analyzes your investment performance including XIRR (Extended Internal Rate of Return)
- **Investment Optimization**: Provides rebalancing tools to optimize your portfolio allocation
- **Transaction History**: Maintains a detailed history of all your mutual fund transactions

### Architecture Overview

FolioMan follows a modern client-server architecture with these key components:

1. **Frontend (Client-side)**
   - Built with React and Hilla
   - Responsive user interface for portfolio management
   - Interactive data visualization components

2. **Backend (Server-side)**
   - Java Spring Boot application
   - RESTful API endpoints
   - Business logic for investment calculations and portfolio analysis

3. **Database**
   - PostgreSQL database for persistent storage
   - Separate schemas for portfolio data and mutual fund information

4. **External Integrations**
   - AMFI (Association of Mutual Funds in India) data integration
   - BSE Star MF integration for fund data


## Prerequisites

Before running the application, ensure you have the following prerequisites installed:

- Java 21 or later
- Maven
- Docker (for local development with PostgreSQL)
- Python with pip (for CAS PDF file processing)
- casparser: A Python CLI tool required for processing password-protected CAS PDF files
  ```shell
  pip install casparser
  ```
This tool must be installed on the server environment where the application will run. The PDF upload functionality for CAS files will not work without this dependency.

## Install Python & pip

This project requires Python for a CLI dependency (see CAS PDF processing notes). Below are short, copyable steps to install Python and pip on common platforms and to verify the installation.

Notes:
- We recommend Python 3.11 or newer.
- pip is included with modern Python installers; the commands below also show how to ensure pip is available and up-to-date.

Windows (PowerShell)

1. Install Python using the official installer from python.org or via Winget (recommended on modern Windows):

```powershell
# Install via winget (runs from an elevated PowerShell if required)
winget install --exact --id Python.Python
```

2. After installation, confirm Python and pip are available:

```powershell
python --version
python -m pip --version
```

3. Ensure pip, setuptools and wheel are up-to-date:

```powershell
python -m pip install --upgrade pip setuptools wheel
```

If `python` isn't found, try `py` (the Python launcher) or add the Python install directory to your PATH. You can also enable the "Add Python to PATH" option in the official Windows installer.

macOS

1. The easiest way is Homebrew (if you have Homebrew installed):

```bash
brew install python
```

2. Alternatively, download the installer from https://python.org and run it.

3. Verify and upgrade pip:

```bash
python3 --version
python3 -m pip install --upgrade pip setuptools wheel
```

Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install -y python3 python3-venv python3-pip
python3 --version
python3 -m pip install --upgrade pip setuptools wheel --user
```

Linux (Fedora/CentOS/RHEL)

```bash
sudo dnf install -y python3 python3-pip
python3 --version
python3 -m pip install --upgrade pip setuptools wheel --user
```

Troubleshooting / tips

- If pip is missing, you can run:

```bash
python3 -m ensurepip --upgrade
```

- Use virtual environments for project work:

```bash
python -m venv .venv
# activate (PowerShell)
.\.venv\Scripts\Activate.ps1
# activate (bash/macOS/Linux)
source .venv/bin/activate
```

After installing Python and pip, re-run any Python-based CLI steps in this README (for example, the CAS PDF processing) and ensure the required Python packages are installed.

## Running the application

The project is a standard Maven project. To run it from the command line,
type `mvnw` (Windows), or `./mvnw` (Mac & Linux), then open
http://localhost:8080 in your browser.

You can also import the project to your IDE of choice as you would with any
Maven project.

### Run tests

```shell
./mvnw clean verify
```

### Run locally

```shell
docker-compose -f docker/docker-compose.yml up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
### Using Testcontainers at Development Time
You can run `TestApplication.java` from your IDE directly.
You can also run the application using Maven as follows:

```shell
./mvnw spotless:apply spring-boot:test-run
```

## Deploying to Production

To create a production build, call `mvnw clean package -Pproduction` (Windows),
or `./mvnw clean package -Pproduction` (Mac & Linux).
This will build a JAR file with all the dependencies and front-end resources,
ready to be deployed. The file can be found in the `target` folder after the build completes.

Once the JAR file is built, you can run it using
`java -jar target/hilla-folioman-1.0.0-SNAPSHOT.jar`

## Project structure

<table style="width:100%; text-align: left;">
  <tr><th>Directory</th><th>Description</th></tr>
  <tr><td><code>src/main/frontend/</code></td><td>Client-side source directory</td></tr>
  <tr><td>&nbsp;&nbsp;&nbsp;&nbsp;<code>index.html</code></td><td>HTML template</td></tr>
  <tr><td>&nbsp;&nbsp;&nbsp;&nbsp;<code>index.ts</code></td><td>Frontend 
entrypoint, bootstraps a React application</td></tr>
  <tr><td>&nbsp;&nbsp;&nbsp;&nbsp;<code>routes.tsx</code></td><td>React Router routes definition</td></tr>
  <tr><td>&nbsp;&nbsp;&nbsp;&nbsp;<code>views/MainLayout.tsx</code></td><td>Main 
 layout component, contains the navigation menu, uses <a href="https://hilla.dev/docs/react/components/app-layout">
App Layout</a></td></tr>
  <tr><td>&nbsp;&nbsp;&nbsp;&nbsp;<code>views/</code></td><td>UI view 
components</td></tr>
  <tr><td>&nbsp;&nbsp;&nbsp;&nbsp;<code>themes/</code></td><td>Custom  
CSS styles</td></tr>
  <tr><td><code>src/main/java/com/app/folioman/</code></td><td>Server-side 
source directory, contains the server-side Java code</td></tr>
  <tr><td>&nbsp;&nbsp;&nbsp;&nbsp;<code>portfolio/</code></td><td>Portfolio management services and models</td></tr>
  <tr><td>&nbsp;&nbsp;&nbsp;&nbsp;<code>mfschemes/</code></td><td>Mutual fund scheme data and services</td></tr>
  <tr><td>&nbsp;&nbsp;&nbsp;&nbsp;<code>Application.java</code></td><td>Server entry-point</td></tr>
</table>

## Key Features

- **Import Mutual Funds**: Upload CAS files from mutual fund registrars (CAMS, Karvy)
- **Portfolio View**: See your entire mutual fund portfolio in one place
- **NAV Updates**: Automatically fetches latest NAVs (Net Asset Values)
- **Rebalancing Tool**: Calculate optimum allocation for new investments
- **Performance Tracking**: Track your investments over time with graphical reports

## Useful links

- Read the documentation at [hilla.dev/docs](https://hilla.dev/docs/).
- Ask questions on [Stack Overflow](https://stackoverflow.com/questions/tagged/hilla) or join our [Discord channel](https://discord.gg/MYFq5RTbBn).
- Report issues, create pull requests in [GitHub](https://github.com/vaadin/hilla).

## JVM Parameters
java -XX:StartFlightRecording=filename=recording.jfr -Djdk.tracePinnedThreads=short -jar myapp.jar
- Enable Java Flight Recorder to monitor application performance. The recording will be saved to `recording.jfr`.
- Adjust the filename and path as needed.
- Metrics to track:

  * Number of platform (carrier) threads (should be small, ~CPU cores)
  * Number of virtual threads (can be massive)
  * Pinning events (should be rare)
  * Carrier thread utilization
