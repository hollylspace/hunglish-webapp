rem  java -Xmx1500M -classpath lib/*:hunglish-0.1.0.jar:. hu.mokk.hunglish.lucene.Launcher
java -Xmx1500M -classpath lib\*;hunglish-0.1.0.jar;. hu.mokk.hunglish.lucene.Launcher

rem ***************************
rem TODO ezt bele kell rakni apomba
rem 1) a class-okat egy jar-ba
rem 2) jar mell� lib konyvt�rba a libeket
rem 3) jar mell� META-INF k�nynt�rba a persitance xml
rem 4) jar mell� resources-lang k�nyvt�rba a resource-okat
rem 5) a jar mell� az appContext-et �s a *.properties-t
rem 6) az Appcontextban jav�tani
rem 6.1) 	<context:property-placeholder location="classpath*:*.properties" />
rem 6.2) 	<bean id="multipartResolver" ---> ezt ki kell kommentezni
