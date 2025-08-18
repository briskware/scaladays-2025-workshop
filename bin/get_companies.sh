#!/bin/bash

curl --include --request GET \
  --header "Accept: application/json" \
  "http://localhost:8080/api/companies" \
  | tail -1 | jq .
