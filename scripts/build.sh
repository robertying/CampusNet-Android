#!/bin/bash

pushd app/src/main/cpp/openssl-curl-android
chmod +x travis-build.sh
./travis-build.sh
popd
bundle exec fastlane beta
