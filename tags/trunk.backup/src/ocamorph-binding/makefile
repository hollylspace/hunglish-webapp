all: output/libocamorph.so

output/ocamorph.o:  ../../lib/analysis.ml
	cd ../../lib ; ocamlopt -output-obj -o ocamorph.o utils.ml bv.ml zip_list.ml  trie.ml  stringTrie.ml         tag.ml         parser_common.ml         constraint.ml minimize.ml        dic_parser.ml   dic_lexer.ml        aff_parser.ml  aff_lexer.ml analysis.ml ; cd ../bindings/java ; mv ../../lib/ocamorph.o output/

## see http://www.mindspring.com/~david.dagon/jni/Native.txt
OCAMORPH_HOME=../../lib
OCMLINCLUDE=/usr/local/lib/ocaml/
JAVAINCLUDE=/usr/lib/j2sdk1.5-sun/include/
#note that you'll have to configure this manually
HULEXICON=/home/bpgergo/hunglish_tools/ocamorph/lexicons/lexicons/morphdb.hu/out/morphdb_hu.bin

output/mokk_nlp_ocamorph_OcamorphWrapper.h: output/mokk/nlp/ocamorph/OcamorphWrapper.class
	javah -d output  -classpath output/ -jni mokk.nlp.ocamorph.OcamorphWrapper

#dependencies: -I /usr/lib/ocaml/3.10.2/ -I../c/ -L/usr/lib/ocaml/3.10.2/  -I /usr/lib/jvm/java-6-openjdk/include/ -I /usr/lib/jvm/java-6-openjdk/include/linux/
output/libocamorph.so: src/c/hunmorph_jnistub.c output/mokk_nlp_ocamorph_OcamorphWrapper.h ../c/ocamorph_stubs.c output/ocamorph.o
	cc -shared -o output/libocamorph.so -Ioutput/ -I../ -I $(OCMLINCLUDE) -I../c/ -L$(OCMLINCLUDE)  -I $(JAVAINCLUDE) -I $(JAVAINCLUDE)/linux/  output/ocamorph.o src/c/hunmorph_jnistub.c ../c/ocamorph_stubs.c -lunix -lasmrun


output/mokk/nlp/ocamorph/OcamorphWrapper.class: src/java/mokk/nlp/ocamorph/OcamorphWrapper.java
	javac -d output src/java/mokk/nlp/ocamorph/*.java

clean:
	rm -rf output/*  

#javaclean2:
#	rm -rf output/bme

javaclean:
	rm -rf output/mokk

#javacomp2:
#	javac -d output src/java/bme/mokk/hunmorph/HunmorphStub.java

javacomp:
	javac -d output src/java/mokk/nlp/ocamorph/*.java

#test2:
#	java -Djava.library.path=./output/ -cp output bme.mokk.hunmorph.HunmorphStub /home/bpgergo/ocamorph/morphdb/lexicons/morphdb.hu/ready2go/morphdb_hu.bin

test:
	rm -f src/java/mokk/nlp/ocamorph/cache.txt
	java -Djava.library.path=./output/ -cp output mokk.nlp.ocamorph.Tester $(HULEXICON) /home/bpgergo/hunglish_tools/ocamorph/ocamorph/src/bindings/java/src/java/mokk/nlp/ocamorph/

testfilestemmer:
	rm -f src/java/mokk/nlp/ocamorph/cache.txt
	rm -f src/java/mokk/nlp/ocamorph/Tolkien_1.hu.cache
	java -Djava.library.path=./output/ -cp output mokk.nlp.ocamorph.FileStemmer $(HULEXICON) src/java/mokk/nlp/ocamorph/cache.txt > src/java/mokk/nlp/ocamorph/Tolkien_1.hu.cache < src/java/mokk/nlp/ocamorph/Tolkien_1.hu.tok

testfilestemmersmall:
	rm -f src/java/mokk/nlp/ocamorph/subtitles_136.hu.cache
	java -Djava.library.path=./output/ -cp output mokk.nlp.ocamorph.FileStemmer $(HULEXICON) src/java/mokk/nlp/ocamorph/cache.txt > src/java/mokk/nlp/ocamorph/subtitles_136.hu.cache < src/java/mokk/nlp/ocamorph/subtitles_136.hu.tok


#old stuff
# output/bme_mokk_hunmorph_HunmorphStub.h: output/bme/mokk/hunmorph/HunmorphStub.class
#	 javah -d output  -classpath output/ -jni bme.mokk.hunmorph.HunmorphStub

# output/libocamorph.so: src/c/hunmorph_jnistub.c output/bme_mokk_hunmorph_HunmorphStub.h ../c/ocamorph_stubs.c output/ocamorph.o
#	 cc -shared -o output/libocamorph.so -Ioutput/ -I../ -I /usr/local/lib/ocaml/ -I../c/ -L/usr/local/lib/ocaml/  -I /usr/lib/j2sdk1.5-sun/include -I /usr/lib/j2sdk1.5-sun/include/linux/  output/ocamorph.o src/c/hunmorph_jnistub.c ../c/ocamorph_stubs.c -lunix -lasmrun


# output/bme/mokk/hunmorph/HunmorphStub.class: src/java/bme/mokk/hunmorph/HunmorphStub.java
#	 javac -d output src/java/bme/mokk/hunmorph/HunmorphStub.java

# clean:
#	 rm -rf output/*  
# test:
#	 java -Djava.library.path=./output/ -cp output bme.mokk.hunmorph.HunmorphStub morphdb_hu.bin
