// This file is a wrapper for the AppNavbar component that provides the AppContext to it. but it didnt seem to work as expected so not using it for now.

import React from 'react';
import { AppProvider, useAppContext } from './AppContext';
// other imports

const AppNavbar = () => {
  const { appVariable, setAppVariable } = useAppContext();

  // rest of your component code
}

const WrappedNavbar = () => (
  <AppProvider>
    <AppNavbar />
  </AppProvider>
);

export default WrappedNavbar;