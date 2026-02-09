
# chat tawk.to


https://help.tawk.to/article/react-js

<!--Start of Tawk.to Script-->
<script type="text/javascript">
var Tawk_API=Tawk_API||{}, Tawk_LoadStart=new Date();
(function(){
var s1=document.createElement("script"),s0=document.getElementsByTagName("script")[0];
s1.async=true;
s1.src='https://embed.tawk.to/66eb9aff4cbc4814f7dab4d2/1i845att1';
s1.charset='UTF-8';
s1.setAttribute('crossorigin','*');
s0.parentNode.insertBefore(s1,s0);
})();
</script>
<!--End of Tawk.to Script-->



# KeyCloak

KeyCloak ready clientID is QuickLedger and secret is wSKsQCEewM8pivrXw7EcUZHBxLDndw5r
Admin username: reza/Reza2024
ManiAdmin/ManiIsAwesome

I created the realm called “QuickLedger” and we should create all users under that realm!
https://ec2-52-64-157-99.ap-southeast-2.compute.amazonaws.com:8443/admin/master/console/

https://ec2-52-64-157-99.ap-southeast-2.compute.amazonaws.com:8443/realms/QuickLedger/account/

https://blog.logrocket.com/implement-keycloak-authentication-react/

A realm manages a set of users, credentials, roles, and groups. A user belongs to and logs into a realm. Realms are isolated from one another and can only manage and authenticate the users that they control.

things that needed to be done for auth

remove client auth from client
set redirect url http://localhost:3000/*
set http://localhost:3000 without trailing slash
no root url

thats it

# NPM
use nvm to install particular version of node


# TODO

Auth0: You need to update src/main.tsx with your actual Domain and Client ID.
Running: Run npm run dev in the v2 directory to see the new app.