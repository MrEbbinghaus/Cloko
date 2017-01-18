#!/bin/bash

set -e # Exit with nonzero exit code if anything fails

rm -rf out
mkdir out
lein clean
lein compile
sass out/css:out/css
cp -R resources/public/* out/
rm out/devcards.html