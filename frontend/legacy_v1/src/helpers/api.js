import axios from 'axios';
import {useKeycloak} from '@react-keycloak/web';
import {jwtDecode} from 'jwt-decode';
import ToastNotification         from "./ToastNotification";
import { toast } from 'react-toastify';


export function useApi() {
    const api = axios.create({
        baseURL: '/', // Use relative base URL
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
        },

    });


// Add a request interceptor
    api.interceptors.request.use(request => {
        //console.log('Starting Request', JSON.stringify(request, null, 2));
        return request;
    }, error => {
        console.error('Request Error', error);
        return Promise.reject(error);
    });

    // Add a response interceptor
    // api.interceptors.response.use(response => {
    //   //console.log('Response:', JSON.stringify(response, null, 2));
    //   return response;
    // }, error => {
    //   if (error.response) {
    //     // The request was made and the server responded with a status code
    //     // that falls out of the range of 2xx
    //     console.error('Response Error Data:', error.response.data);
    //     console.error('Response Error Status:', error.response.status);
    //     console.error('Response Error Headers:', error.response.headers);
    //   } else if (error.request) {
    //     // The request was made but no response was received
    //     console.error('Request Error Data:', error.request);
    //   } else {
    //     // Something happened in setting up the request that triggered an Error
    //     console.error('Error Message:', error.message);
    //   }
    //   console.error('Error Config:', error.config);
    //   return Promise.reject(error);
    // });


    api.interceptors.response.use(
        response => {
            // Success response, just return the response
            return response;
        },
        error => {
            if (error.response) {
                // Server responded with a status code that falls out of the range of 2xx
                console.error('Response Error:', {
                    data: error.response.data,
                    status: error.response.status,
                    headers: error.response.headers,
                    config: error.config,
                    message: error.message
                });
            } else if (error.request) {
                // Request was made but no response was received
                console.error('Request Error: No response received from the backend.', {
                    request: error.request,
                    config: error.config,
                    message: error.message
                });
                toast.error(`Error: ${error.response?.status} - ${error.response?.data?.message || 'A server error occurred'}`);
                return Promise.resolve({error: true, message: 'Backend is not available'});
            } else {
                // Something happened in setting up the request that triggered an Error
                console.error('Error setting up request:', {
                    message: error.message,
                    config: error.config
                });
            }


            toast.error(`Error: ${error.response?.status} - ${error.response?.data?.message || 'A server error occurred'}`);
            // return Promise.reject(error); // Propagate the error
            return Promise.resolve({ error: true, message: 'Backend is not available' });

        }
    );


    const {keycloak} = useKeycloak();

// Add a request interceptor to the axios instance
    api.interceptors.request.use(async (config) => {

        const token = keycloak.token; // Get the current token

        if (token) {
            const {exp} = jwtDecode(token); // Decode token to get expiry time
            const now = new Date().getTime() / 1000; // Current time in seconds
            if (exp - now < 60) { // Check if the token expires in less than 60 seconds
                console.log("token is expring soon, refreshing ")
                try {
                    // Attempt to refresh the token
                    const refreshed = await keycloak.updateToken(60); // Refresh if expires in less than x seconds
                    if (refreshed) {
                        console.log("token refreshed", refreshed)
                        config.headers.Authorization = `Bearer ${keycloak.token}`; // Update the token in the header
                    }
                } catch (error) {
                    console.error('Error refreshing token', error);
                    // Handle token refresh error (e.g., redirect to login)
                }
            } else {
                // If the token is still valid, use it
                config.headers.Authorization = `Bearer ${token}`;
            }
        }

        return config; // Return the modified config
    }, (error) => {
        return Promise.reject(error);
    });

    return api;

}


// export async function useApi() {
//   const { keycloak } = useKeycloak();

//   const token = keycloak.token; // Get the current token
//   const refreshToken = keycloak.refreshToken; // Get the current refresh token

//   if (token) {
//     const { exp } = jwtDecode(token); // Decode token to get expiry time
//     const now = new Date().getTime() / 1000; // Current time in seconds

//     if (exp - now < 60) { // Check if the token expires in less than 60 seconds
//       try {
//         // Attempt to refresh the token
//         const refreshed = await keycloak.updateToken(30); // Refresh if expires in less than 30 seconds
//         if (refreshed) {
//           config.headers.Authorization = `Bearer ${keycloak.token}`; // Update the token in the header
//         }
//       } catch (error) {
//         console.error('Error refreshing token', error);
//         // Handle token refresh error (e.g., redirect to login)
//       }
//     } else {
//       // If the token is still valid, use it
//       config.headers.Authorization = `Bearer ${token}`;
//     }
//   }


//   return api;
// }