#!/bin/bash

name="$1"
url="$2"
if [ -z "$name" ] || [ -z "$url" ]; then
  echo "Usage: $0 <company_name> <company_url>"
  exit 1
fi

curl --include --request POST \
  --data "{\"name\":\"$name\",\"url\":\"$url\"}" \
  --header "Content-type: application/json" \
  "http://localhost:8080/api/companies" \
  && echo
