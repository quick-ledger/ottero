//import Keycloak from 'keycloak-js';
import keycloak from "./Keycloak"




// Function to login with hardcoded credentials
function loginWithCredentials() {
    const username = 'mani.hosseini@hotmail.com';
    const password = '0tter020@$!';
    
        keycloak.login({
          username: username,
          password: password,
        }).catch((e) => {
          console.error('Authentication Failed: ', e);
        });
      
}

// Call the function to login with credentials
loginWithCredentials();

export { loginWithCredentials };    