@echo off
REM Change this values according to your needs
SET DGCG_ENDPOINT="https://example.org/rules"
SET SIGNING_KEY="upload_key.pem"
SET SIGNING_CERT="upload.pem"
SET TLS_KEY="auth_key.pem"
SET TLS_CERT="auth.pem"


REM DO NOT CHANGE ANYTHING BELOW THIS!

echo Search rule files and sign with Upload Certificate and Upload to DGCG

for /f "usebackq delims=|" %%f in (`dir /s/b rule.json`) do (call :upload %%f)

echo deleting temporary file
del -f tmp.cms

goto :eof

:upload
echo Processing JSON file %1
call dgc signing sign-string -c %SIGNING_CERT% -k %SIGNING_KEY% -i "%1" -o tmp.cms
call curl --no-progress-bar --request POST "%DGCG_ENDPOINT%" --header "Content-Type: application/cms-text" --header "Accept: application/json" --data-binary @tmp.cms --cert %TLS_CERT% --key %TLS_KEY%
echo.
echo.
