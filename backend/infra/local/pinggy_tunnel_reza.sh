#!/bin/bash
# Mani you should buy one of these https://pinggy.io/#prices then we move this file to gitignore
# then we have to adjust the auth0 test tenant webhooks to point to both of us
# but try it, it might actually work for both of us.

# The cleanup in start_all.sh will kill any existing tunnels before starting
ssh -p 443 -R0:localhost:8080 -L4300:localhost:4300 -o StrictHostKeyChecking=no -o ServerAliveInterval=30 Mc2F0uGiumF@pro.pinggy.io