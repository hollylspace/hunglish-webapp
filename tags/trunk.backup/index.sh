#create index dir if not exists
if [ -d index ]; then
	echo "index dir already exits"
else
	mkdir index
fi

#create index_copy dir of not exists; otherwise remove its content
if [ -d index_copy ]; then
	rm -rf index_copy/*
else
	mkdir index_copy
fi
#copy old index into index_copy
cp index/* index_copy/
#remove old index ?
if [ -d index ]; then
	rm -rf index/*
fi 


CLASSPATH="output/hunglishdict-1.0.jar"

for jar in lib/*.jar 
do
	CLASSPATH=$CLASSPATH:$jar
done
echo $CLASSPATH
export CLASSPATH
java -Xmx512m  -classpath $CLASSPATH mokk.nlp.irutil.IndexerConsole $*

