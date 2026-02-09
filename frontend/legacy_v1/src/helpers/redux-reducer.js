// reducer.js
import { SET_COMPANY_ID, SET_COMPANY_NAME, SET_TOKEN } from './redux-action';

const initialState = {
  selectedCompanyId: null,
  selectedCompanyName: null,
  selectedToken: null,
};

export function appReducer(state = initialState, action) {
  switch (action.type) {
    case SET_COMPANY_ID:
      return { ...state, selectedCompanyId: action.payload };
    case SET_COMPANY_NAME:
      return { ...state, selectedCompanyName: action.payload };
    case SET_TOKEN:
      return { ...state, selectedToken: action.payload };
    default:
      return state;
  }
}