echo "CREATE USER 'hunglish'@'localhost' IDENTIFIED BY 'sw6x2the';" | mysql -uroot -psw6x2the 

echo "create database hunglishwebapp default character set = UTF8;" | mysql -uroot -psw6x2the 

echo "GRANT ALL PRIVILEGES ON *.* TO 'hunglish'@'localhost' WITH GRANT OPTION;" | mysql -uroot -psw6x2the 

cat create.sql | mysql -uhunglish -psw6x2the --default-character-set=utf8 hunglishwebapp
