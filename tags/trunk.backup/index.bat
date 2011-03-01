echo off
set CLASSPATH=output\hunglishdict-1.0.jar;lib\avalon-framework-4.2.0.jar;lib\bcel-5.1.jar;lib\commons-collections-3.1.jar;lib\concurrent-1.3.1.jar;lib\concurrent.jar;lib\excalibur-concurrent-1.0.jar;lib\excalibur-fortress-1.0.jar;lib\excalibur-fortress-container-complete-1.1.jar;lib\excalibur-fortress-tools-1.0.jar;lib\jmorph.jar;lib\lucene-core-3.0-dev.jar;lib\lucene-highlighter-3.0-dev.jar;lib\qdox-1.1.jar;lib\servlet-api.jar;lib\servlet.jar;lib\velocity-dep-1.4.jar
echo %CLASSPATH%
rem %1 %2
java -Xmx512m  -classpath %CLASSPATH% mokk.nlp.irutil.IndexerConsole conf\indexertest.conf conf\logger.xconf

