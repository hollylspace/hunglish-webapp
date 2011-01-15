cat /big3/Work/HunglishMondattar/hunglish-webapp/create.sql | mysql -uhunglish -psw6x2the --default-character-set=utf8 hunglishwebapp
cd /big3/Work/HunglishMondattar/deployment/
rm fileUpload/*
rm logs/*
rm -rf harness.data/*/*
rm hunglishIndex/*
rm hunglishIndexTmp/*
