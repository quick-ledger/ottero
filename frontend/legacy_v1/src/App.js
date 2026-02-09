import React from 'react';
import './App.css';
import { BrowserRouter, Route, Routes, Outlet } from 'react-router-dom';
import { Provider } from 'react-redux';
import { store, persistor } from './helpers/config-store';
import { ReactKeycloakProvider } from '@react-keycloak/web';
import keycloak from './Keycloak';

// Import your components and pages
import AppNavbar from './helpers/AppNavbar';
import PrivateRoute from './helpers/PrivateRoute';
import SignupWizardPage from './pages/SignupWizardPage';
import QuoteEdit from './components/quote/QuoteEdit';
import QuoteList from './components/quote/QuoteList';
import InvoiceEdit from './components/invoice/InvoiceEdit';
import InvoiceList from './components/invoice/InvoiceList';
import CustomerList from './components/customer/CustomerList';
import CustomerEdit from './components/customer/CustomerEdit';
import AssetList from './components/asset/AssetList';
import AssetEdit from './components/asset/AssetEdit';
import AssetDefEdit from './components/asset/AssetDefEdit';
import AssetDefList from './components/asset/AssetDefList';
import CompanyEdit from './components/company/CompanyEdit';
import CompanyList from './components/company/CompanyList';
import QuoteNumberBuilder from './components/quote/QuoteNumberBuilder';
import QuoteViewer from './public-components/QuoteViewer';
import LoggedInLanding from './components/LoggedInLandng';
import NotLoggedInLanding from './public-components/NotLoggedInLanding';
import ProductList from './components/product/ProductList';
import ProductEdit from './components/product/ProductEdit';
import ServiceList from './components/service/ServiceList';
import ServiceEdit from './components/service/ServiceEdit';
import Footer from "./helpers/Footer";
import ErrorBoundary from "./helpers/ErrorBoundary";
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';


const PublicLayout = () => (
  <Routes>
    {/* Public routes */}
    <Route path="/in-quote" element={<QuoteViewer />} />
  </Routes>
);

const ProtectedLayout = () => (
<>
    <AppNavbar />
    <ToastContainer position="top-right" autoClose={5000} hideProgressBar={false} />
    <Outlet />
    <Footer />
</>
);

const App = () => {
  return (
    <Provider store={store} persistor={persistor}>
      <BrowserRouter>
        <PublicLayout />
        <ReactKeycloakProvider authClient={keycloak}>
          <Routes>
            <Route path="" element={<ProtectedLayout />}>
              <Route path="landing" element={<PrivateRoute><LoggedInLanding /></PrivateRoute>} />
              <Route path="signup-wizard" element={<PrivateRoute><SignupWizardPage /></PrivateRoute>} />
              <Route path="quotes/:id" element={<PrivateRoute><QuoteEdit /></PrivateRoute>} />
              <Route path="quotes" element={<PrivateRoute><QuoteList /></PrivateRoute>} />
              <Route path="quotes/number" element={<PrivateRoute><QuoteNumberBuilder /></PrivateRoute>} />
              <Route path="invoices/:id" element={<PrivateRoute><InvoiceEdit /></PrivateRoute>} />
              <Route path="invoices" element={<PrivateRoute><InvoiceList /></PrivateRoute>} />
              <Route path="companies" element={<PrivateRoute><CompanyList /></PrivateRoute>} />
              <Route path="companies/:id" element={<PrivateRoute><CompanyEdit /></PrivateRoute>} />
              <Route path="products" element={<PrivateRoute><ProductList /></PrivateRoute>} />
              <Route path="products/:id" element={<PrivateRoute><ProductEdit /></PrivateRoute>} />
              <Route path="services" element={<PrivateRoute><ServiceList /></PrivateRoute>} />
              <Route path="services/:id" element={<PrivateRoute><ServiceEdit /></PrivateRoute>} />
              {/* not for mvp <Route path="employees" element={<PrivateRoute><EmployeeList /></PrivateRoute>} />
              <Route path="employees/:id" element={<PrivateRoute><EmployeeEdit /></PrivateRoute>} /> */}
              <Route path="customers" element={<PrivateRoute><CustomerList /></PrivateRoute>} />
              <Route path="customers/:id" element={<PrivateRoute><CustomerEdit /></PrivateRoute>} />
              <Route path="assets/def/new" element={<PrivateRoute><AssetDefEdit /></PrivateRoute>} />
              <Route path="assets/def/:id" element={<PrivateRoute><AssetDefEdit /></PrivateRoute>} />
              <Route path="assets/def" element={<PrivateRoute><AssetDefList /></PrivateRoute>} />
              <Route path="assets" element={<PrivateRoute><AssetList /></PrivateRoute>} />
              <Route path="assets/:id" element={<PrivateRoute><AssetEdit /></PrivateRoute>} />
              <Route path="/logout" element={<NotLoggedInLanding />} />
            </Route>
          </Routes>
        </ReactKeycloakProvider>
      </BrowserRouter>
    </Provider>
  );
}; 

export default App;
