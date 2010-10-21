rem  java -Xmx1500M -classpath lib:hunglish-0.1.0.jar:. hu.mokk.hunglish.lucene.Launcher
java -Xmx1500M -classpath lib\*;hunglish-0.1.0.jar;. hu.mokk.hunglish.lucene.Launcher

rem ***************************
rem TODO ezt bele kell rakni apomba
rem 1) a class-okat egy jar-ba
rem 2) jar mellé lib konyvtárba a libeket
rem 3) jar mellé META-INF könyntárba a persitance xml
rem 4) jar mellé resources-lang könyvtárba a resource-okat
rem 5) a jar mellé az appContext-et és a *.properties-t
rem 6) az Appcontextban javítani
rem 6.1) 	<context:property-placeholder location="classpath*:*.properties" />
rem 6.2) 	<bean id="multipartResolver" ---> ezt ki kell kommentezni
