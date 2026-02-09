import { configureStore, combineReducers } from '@reduxjs/toolkit';
import { appReducer } from './redux-reducer';
import { persistStore, persistReducer } from 'redux-persist';
import storage from 'redux-persist/lib/storage'; // defaults to localStorage for web

//state is not saving across page navigation after adding the persistant store.
const rootReducer = combineReducers({
  app: appReducer,
});

const persistConfig = {
  key: 'root',
  storage,
};


const persistedReducer = persistReducer(persistConfig, rootReducer);


const store = configureStore({
  reducer: persistedReducer,
});

const persistor = persistStore(store);

export  {store,persistor};