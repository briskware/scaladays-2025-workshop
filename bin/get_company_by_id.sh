#!/bin/bash

id="$1"
if [ -z "$id" ]; then
  echo "Usage: $0 <company_id>"
  exit 1
fi

curl --include --request GET \
  --header "Accept: application/json" \
  "http://localhost:8080/api/companies/$id" \
  && echo
