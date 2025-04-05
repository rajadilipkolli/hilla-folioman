[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/rajadilipkolli/hilla-folioman)

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
