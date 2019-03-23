#!/bin/bash

pushd app/src/main/cpp/openssl-curl-android
curl -sOL "$(curl -s https://api.github.com/repos/robertying/openssl-curl-android/releases/latest | grep browser_download_url | cut -d '"' -f 4)"
tar zxf build.tar.gz
popd
bundle exec fastlane beta
