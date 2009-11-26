if [ -d index ]; then
else 
	mkdir index_copy
fi 
if [ -d index_copy ]; then
	rm -rf index_copy/*
else 
	mkdir index_copy
fi
if [ -d index ]; then
	rm -rf index/*
else 
fi 

 
cp index/* index_copy/

CLASSPATH="output/hunglishdict-1.0.jar"

for jar in lib/*.jar 
do
	CLASSPATH=$CLASSPATH:$jar
done
echo $CLASSPATH
export CLASSPATH
java -Xmx512m  -classpath $CLASSPATH mokk.nlp.irutil.IndexerConsole $*

