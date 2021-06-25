@echo off
REM Change this values according to your needs
SET DGCG_ENDPOINT="https://example.org/rules"
SET SIGNING_KEY="upload_key.pem"
SET SIGNING_CERT="upload.pem"
SET TLS_KEY="auth_key.pem"
SET TLS_CERT="auth.pem"


REM DO NOT CHANGE ANYTHING BELOW THIS!

echo Purging temporary directory

echo Search rule files and sign with Upload Certificate and Upload to DGCG

for /f "usebackq delims=|" %%f in (`dir /s/b rule.json`) do (
	@echo off
	echo Processing JSON file %%f
	dgc signing sign-string -c %SIGNING_CERT% -k %SIGNING_KEY% -i %%f -o tmp.cms
	curl -w -s --request POST "%DGCG_ENDPOINT%" --header "Content-Type: application/cms-text" --header "Accept: application/json" --data-binary @tmp.cms --cert %TLS_CERT% --key %TLS_KEY% -o curl.log
)

echo deleting temporary file
rm -f tmp.cms
