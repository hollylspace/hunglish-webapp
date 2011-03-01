cd /workspace/hunglish/hunglish-webapp/src/main/python

cat ../../../create.sql demo.sql | mysql -uhunglish -psw6x2the --default-character-set=utf8 hunglishwebapp

python machine_upload.py hunglish sw6x2the hunglishwebapp < uploadtable.txt

python control_harness.py hunglish sw6x2the hunglishwebapp /home/demo/workspace/hunglish/harness/harness.data.mini