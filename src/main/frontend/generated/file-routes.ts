import { createRoute as createRoute_1 } from "@vaadin/hilla-file-router/runtime.js";
import type { AgnosticRoute as AgnosticRoute_1 } from "@vaadin/hilla-file-router/types.js";
import { lazy as lazy_1 } from "react";
const routes: readonly AgnosticRoute_1[] = [
    createRoute_1("importmutualfunds", [
        createRoute_1("ImportMutualFundsView", lazy_1(() => import("../views/importmutualfunds/ImportMutualFundsView.js")), { "route": "ImportMutualFundsView", "flowLayout": false, "title": "Import Mutual Funds" })
    ]),
    createRoute_1("login", [
        createRoute_1("LoginView", lazy_1(() => import("../views/login/LoginView.js")), { "route": "LoginView", "flowLayout": false, "title": "Login" })
    ]),
    createRoute_1("MainLayout", lazy_1(() => import("../views/MainLayout.js")), { "route": "MainLayout", "flowLayout": false, "title": "Main Layout" }),
    createRoute_1("mfschemes", [
        createRoute_1("MfSchemesView", lazy_1(() => import("../views/mfschemes/MfSchemesView.js")), { "route": "MfSchemesView", "flowLayout": false, "title": "Mf Schemes" })
    ]),
    createRoute_1("rebalance", [
        createRoute_1("ReBalanceView", lazy_1(() => import("../views/rebalance/ReBalanceView.js")), { "route": "ReBalanceView", "flowLayout": false, "title": "Re Balance" })
    ]),
    createRoute_1("userPortfolio", [
        createRoute_1("UserPortfolioView", lazy_1(() => import("../views/userPortfolio/UserPortfolioView.js")), { "route": "UserPortfolioView", "flowLayout": false, "title": "User Portfolio" })
    ]),
    createRoute_1("userprofile", [
        createRoute_1("UserProfileView", lazy_1(() => import("../views/userprofile/UserProfileView.js")), { "route": "UserProfileView", "flowLayout": false, "title": "User Profile" })
    ])
];
export default routes;
