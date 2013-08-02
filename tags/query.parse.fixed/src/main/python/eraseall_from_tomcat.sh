#!/bin/bash

set -e

cat /big3/Work/HunglishMondattar/hunglish-webapp/create.sql | mysql -uhunglish -psw6x2the --default-character-set=utf8 hunglishwebapp

cd /big3/Work/HunglishMondattar/deployment/

rm -rf fileUpload
mkdir fileUpload

rm -rf logs
mkdir logs
mkdir logs/harness
mkdir logs/cout
mkdir logs/harness/cout

rm -rf harness.data
mkdir harness.data
mkdir harness.data/hu
mkdir harness.data/en
mkdir harness.data/align

rm -rf hunglishIndex
mkdir hunglishIndex
rm -rf hunglishIndexTmp
mkdir hunglishIndexTmp
