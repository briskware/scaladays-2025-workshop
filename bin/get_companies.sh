#!/bin/bash

curl --include --request GET \
  --header "Content-type: application/json" \
  "http://localhost:8080/api/companies" \
  && echo
