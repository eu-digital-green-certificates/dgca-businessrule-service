#!/bin/bash
# Change this values according to your needs
DGCG_ENDPOINT="https://example.org/rules"
SIGNING_KEY="upload_key.pem"
SIGNING_CERT="upload.pem"
TLS_KEY="auth_key.pem"
TLS_CERT="auth.pem"

# DO NOT CHANGE ANYTHING BELOW THIS!
function upload()
{
echo " "
echo "Processing JSON file $1"
java -jar ./dgc-cli.jar signing sign-string -c $SIGNING_CERT -k $SIGNING_KEY -i $1 -o ./tmp.cms
curl --no-progress-bar --request POST "$DGCG_ENDPOINT" --header "Content-Type: application/cms-text" --header "Accept: application/json" --data-binary @tmp.cms --cert "$TLS_CERT" --key "$TLS_KEY"
}

echo "Search rule files and sign with Upload Certificate and Upload to DGCG"


for RULEFILES in `find "./" -type f -name "rule.json"`
do
upload $RULEFILES
done

echo ""
echo "deleting temporary file"
rm tmp.cms

exit 1
