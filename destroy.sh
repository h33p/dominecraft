#!/bin/bash

AUTHKEY=$(cat /tmp/authkey)

curl -X DELETE -H "Content-Type: application/json" -H "Authorization: Bearer $AUTHKEY" "https://api.digitalocean.com/v2/droplets?tag_name=mcserver"

