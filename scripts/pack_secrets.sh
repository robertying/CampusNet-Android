#!/bin/bash

BASEDIR=$(dirname "$0")
cd $BASEDIR/../

tar cvf secrets.tar.gz local.keystore .env.default app/google-services.json
travis encrypt-file secrets.tar.gz --add
