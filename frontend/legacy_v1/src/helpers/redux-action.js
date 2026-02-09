export const SET_COMPANY_ID = 'SET_COMPANY_ID';
export const SET_COMPANY_NAME = 'SET_COMPANY_NAME';
export const SET_TOKEN = 'SET_TOKEN';

export function setCompanyId(id) {
  return { type: SET_COMPANY_ID, payload: id };
} 

export function setCompanyName(name) {
  return { type: SET_COMPANY_NAME, payload: name };
} 

export function setToken(token) {
  return { type: SET_TOKEN, payload: token };
} 