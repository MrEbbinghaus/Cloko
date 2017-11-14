#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

mkdir -p out
lein clean
lein compile
cp -R resources/public/* out/
rm out/devcards.html