BACKUPDIR=/home/bpgergo/workspaces/hunglish2/hunglish/backup
WEBAPPDIR=/var/lib/tomcat6/webapps

sudo rm -rf $BACKUPDIR/*
sudo cp $WEBAPPDIR/ROOT.war $BACKUPDIR

sudo /etc/init.d/tomcat6 stop
mvn clean package
sudo rm -rf $WEBAPPDIR/ROOT
sudo rm -rf $WEBAPPDIR/ROOT.war
sudo mv target/hunglish*.war $WEBAPPDIR/ROOT.war
sudo /etc/init.d/tomcat6 start
