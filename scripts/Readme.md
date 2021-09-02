# DCC Rules Upload Script

This Batch script allows to upload multiple DCC-Validation Rules with one CMD command.

## Preparation

Install DGC-CLI on your computer. Follow all the steps described in Readme file.
https://github.com/eu-digital-green-certificates/dgc-cli

Copy your DCC-Validation Rules in a directory next to the Batch-File.
The Rules can be placed within a directory structure.
A rule file MUST have the filename ```rule.json```. All other files will be ignored.

Copy you Upload- and MTLS-Certificate into the directory.

Open the Batch-File with a Text-Editor of your choice and set the following Values

| Variable | Value |
| --- | --- |
| DGCG_ENDPOINT | URL of rules upload endpoint (should end with /rules) |
| SIGNING_KEY | Path to PrivateKeyFile of your Upload Certificate |
| SIGNING_CERT | Path to PEM-File of your Upload Certificate |
| TLS_KEY | Path to PrivateKeyFile of your TLS Certificate |
| TLS_CERT | Path to PEM-File of your TLS Certificate |

## Upload Rules

Just execute the Batch Script and all Rules will be uploaded.