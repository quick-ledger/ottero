import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
    url: "https://auth.ottero.com.au",
    realm: "Ottero",
    clientId: "Ottero"
});


//  keycloak.init({ onLoad: 'login-required' }).then(authenticated => {
//     if (authenticated) {
//       keycloak.login({ scope: 'email profile' }); // add your scopes here
//     }
//   }).catch(() => {
//     console.error("Authenticated Failed");
//   });


export default keycloak;

