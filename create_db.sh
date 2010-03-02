#usage: create_db.sh [username] [passw] [dbname]

mysql -u$1 -p$2 --default-character-set=utf8 $3 < src/sql/create.sql