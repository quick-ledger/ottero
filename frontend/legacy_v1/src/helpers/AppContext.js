import React, { useState, createContext, useContext } from 'react';


// Create a Context object
const AppContext = createContext();

// Create a provider component
export const AppProvider = ({ children }) => {
  const [appVariable, setAppVariable] = useState('not selected');

  return (
    <AppContext.Provider value={{ appVariable, setAppVariable }}>
      {children}
    </AppContext.Provider>
  );
};

// Create a hook to use the context
export const useAppContext = () => {
  const context = useContext(AppContext);

  if (!context) {
    throw new Error('useAppContext must be used within an AppProvider');
    
  }

  return context;
};