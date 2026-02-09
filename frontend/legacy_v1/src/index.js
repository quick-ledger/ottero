import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import 'bootstrap/dist/css/bootstrap.min.css';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(

/*
  StrictMode renders keykeycloak provider twice so i removed it for now
When using React.StrictMode, it's important to ensure that any code that shouldn't be run twice is placed outside of the React.StrictMode wrapper. In your case, you've correctly identified that the Keycloak provider should not be inside React.StrictMode.


  */

  // <React.StrictMode>
  //   <App />
  // </React.StrictMode>
  <App />
  );

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
