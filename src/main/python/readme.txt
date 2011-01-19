---
Mit kell itt csinalni?

Igy tesztelem:

# Legyalulom a harness jatszoteret:
rm -rf /big3/Work/HunglishMondattar/harness.data/*/*

# Legyalulom a tablat, es letrehozom a zsanereket:
cat ../../../create.sql demo.sql | mysql -uhunglish -psw6x2the --default-character-set=utf8 hunglishwebapp

# Beleteszek egy teszt-fajlpart az upload tablaba:
python machine_upload.py hunglish sw6x2the hunglishwebapp < uploadtable.txt 

# Maga az alkalmazas:
python control_harness.py hunglish sw6x2the hunglishwebapp /big3/Work/HunglishMondattar/harness.data

---
Mit csinal ez?

Az upload tablabol kiszedi az ujakat.
A fajlokat bemasolja a harness adatkonyvtarba.
Elinditja a harness-t, mindegyik bidokumentumra kulon, kabe igy:
HARNESSDIR=/big1/Work/Pipeline/cvs/tcg/harness
python $HARNESSDIR/harness.py --graph=$HARNESSDIR/hunglishstategraph.txt --commands=$HARNESSDIR/hunglishcommands.txt --root=<ARGUMENTUM> --catalog catalog.tmp
Ha ez eredmenyt adott (azaz nem szallt el, es a minosegszurok nem csonkoltak nulla bajtosra),
akkor letrehoz neki egy uj doc-ot, es a mondatparokat bemasolja a bisen tablaba.
Az upload tablat frissiti a harness altal kibocsatott meret-adatokkal.
A bisen tabla mondataihoz eltarolja a mondat normalizalt valtozatanak hash-et.
Ez alapjan beallitja a duplum flaget az uj bisentence-ekre.
Amikor ez mind lezajlott, akkor egy job queue tablan keresztul uzen a webappnak, hogy indexelheti az uj ES nemduplum mondatparokat.

---
TODO:

Legyen a fileUpload fajlnev kozelebb a harness szabvanyhoz.

Zsolt telepitsen legujabb, sun-os javat, es mavent. A tomcat a sun-os java home-ra mutasson.
De az is lehet, hogy az Aspects JAR problemat megoldja, ha a tomcat es a maven ugyanazzal
a java-val (openjdk, sun jdk) megy.

atirtam az init.d/tomcat6-ot sun-rol openjdk path-ra!!!!!!!!!!!!

- Sajnos van egy kulon doc.id, ahelyett, hogy megorokolne' az upload.id-t.
Ez nem jo igy, nagyon nehezkes debuggolaskor osszenezni a harness logokat
a mysql tablakkal.

- Ne relativ path-on keresse az index es upload konyvtarakat, hanem fixen
/big3/Work/HunglishMondattar alatt, ahol egyebkent a harness.data is lesz.
Persze a laptopokon nem big3, legyen valami lokalis conf,
ami alapjan startup (es nem build) idoben beallithato. Utobbi azert fontos,
hogy mac-rol is tudjam a kozelen deployolni.

- Az alkalmazas nem tudja a recordbol letrehozni az upload objektumot,
ha nem hazudok be egy kitoltott harnessed_timestamp mezot.
Gergo szedje ki ezt a hulye checket, Daniel utana mar kiszedheti
a behazudast a machine_upload-bol.

- GERGO: Indexer jdbc eleres parametereit properties fajlbol venni.

- GERGO: Valami ronda nagy (quartz?) leak, ami miatt ujra kell inditgatni a tomcat-et.

- A quartz finnyas arra, hogy honnan kell inditania a cronjobot.

- Ha nem akarok csak emiatt feltolteni egy kamu dokumentumpart, akkor nem tudom
triggerelni a rendszeren belul a harness elinditasat. Jo, nem nagy gond,
tomcat6 user elinditja a
nohup bash /big3/Work/HunglishMondattar/deployment/harness_cronjob.sh &
progit. De akkor is.

- A loadgame-savegame annyira kozel-specifikusak, hogy Gergonek nincs
sok haszna egyelore beloluk. A savegame addig nem lesz ipari szintu,
amig nem allitja le a service-t.

- DANIEL: Masodpeldany van a deployment konyvtarban a cronjob-bol.

- GERGO: Aramvonalasabba kellene tenni a kozel deploy-t. A mac-emen mar teljesen jo:
mvn jetty:run > cout 2> cerr &
, bar csak parancssorbol, eclipse-bol valamiert nem talalja az eroforrast.

- GERGO: Az exception-ok informacioit a webapp es a control_harness is elnyelegeti.
UPDATE: a control_harnesst mar megjavitottam.

- Layout: ROOT/ alabbiak: fileUpload harnessData hunglishIndex logs mysqlDump

- Vegiggondolni, hogy milyen katasztrofakhoz vezet (ha vezet), ha veletlenul
egyidoben fut ket control_harness peldany. indexelesbol semmikeppen ne fusson.
Van-e valami komolyabb tranzakcionalitasi problema, tehat peldaul ha elfogy
az indexelo memoriaja, akkor ertelmes allapotba hozza-e magat a rendszer?

- egy biztonsagi mentest kellene csinalni az indexrol, mielott meghivjuk az indexert.

- Kornai feature request-je: A lucene tokenizalo legyen olyan okos,
hogy a dog's szot is megtalalja, ha a dog-ra keresek.

- Lehetseges, hogy hosszu tavon a machine_upload-nak masolnia kellene
fileUpload-ba helyben-referalas helyett. Goreny bugokhoz vezethez,
ha csak a felhasznalok feltoltesei vannak ott, a tobbi szerteszejjel.

- Nekem ugy tunt (Wed, Jan 12, 2011 at 10:45 PM level), hogy
a spring nem frissitette egy darabig a doc-ot a webes adminfeluleten,
mikozben a harness nagyban irta. A bisen-nel nem ez tortent.
Baromira megragni, hogy milyen problemakhoz vezet, hogy
a spring (aki cache-el valszeg) olvassa az objektumokat, mikozben
a harness irja az adatbazist. Me'g ennel is jobban megragni
ugyanezt harness vs. UploadController parositasban, hiszen
ok egyszerre irjak az adatbazist.

- Modositottam /etc/mysql/my.cnf konfigot ezzel a sorral:
wait_timeout=604800
Ez kesobb valszeg nem is fog kelleni. Kiszedni, kulonosen
ha esetleg kiderul, hogy rizikoforras.

HARNESS, HIBAKEZELES:

- srt formatum feltoltese, konverzioja. UPDATE: Mar csak tesztelni kellene.

- Maradtak bent html entitasok valamelyik konverter kimeneteben.

- ~/scripts/tcom/nogarbage.sh integralasa, vagy legalabbis a relevansabb reszeinek
atmentese, ha vannak egyaltalan. A sajtofigyelo mennyire jol megy enelkul?

- control_harness.py:decideIfWorthIndexing() lehetne kicsit kifinomultabb.
tcg/scripts/filtersen.py szinte'n.

- Tovezes is legyen a pipeline-ban az align elott, hogy ne legyunk rosszabbak, mint
a regi hunglish.

- Ezt valszeg nem fogjuk megcsinalni, de lenne ertelme:
A pipeline-t szetszedni ket reszre valahol a sen elott, ugy, hogy az eleje
baromi gyorsan lefusson, es reszponzivan vissza tudjon kohogni olyanokat,
hogy "tul hosszu", "nem angol", "tul kulonbozo hosszu" meg ilyesmi.
A lassu reszek tovabbra is cronban futnanak.

LEZART DOLGOK:

- Nem mukodik a duplumszures. Ez peldaul egy olyan mondatpar rogton, amit
ki kellett volna:
http://kozel.mokk.bme.hu:8080/hunglish/search?huSentence=&enSentence=%22come+on+claire%22&doc.genre=-10
Ugyanaz a hashuk:
select en_sentence,id,hu_sentence_hash,en_sentence_hash,is_duplicate
 from bisen order by hu_sentence_hash,en_sentence_hash,is_duplicate,
 CHAR_LENGTH(CONCAT(en_sentence,hu_sentence)) limit 2;

DONE - /srv/tomcat6/conf/server.xml Fajlban a Connector-nak
meg kell mondani, hogy URIEncoding="UTF-8"

DONE - Me'g mindig nem igaz az, hogy az ures hunglishIndex konyvarat
eszlelve megcsinalja a lucene az ures indexet.

DONE - Az indexelot kulon kell kerni, hogy indexeljen, ahelyett, hogy
a quartz hivna azt is.

DONE - Kellene egy save-load script-pa'r, ami a megadott konyvtarba ment
egy mysqldumpot, egy harness.data es fileUpload konyvtarat es egy lucene indexet.

DONE - hunglish2.justlaw uploadtable, teszteles.

DONE - harness kimenet logba. datum-uploadId.log
(megoldva, a control_harness uj opcionalis argja, hogy hova loggoljon)

DONE - maga a control_harness kimenet logba. datum.controller.log 
(ez ma'r a harness_cronjob.sh dolga.)

DONE - Kell valami hivatalosan_jovahagyva flag az upload es doc tablakba. UPDATE: az approved nevet kapta.

DONE - A copyright flag mar az upload tablaig sincs visszagorgetve,
nem hogy a hunglish1.nolaw.uploadtable megalkotasaig.

DONE - old_docid-t kivezetni az uploadtable formatumig. Ha mar az se kezeli, akkor ki kezelje?

DONE - Ha ures a qf, akkor most "E"-t jegyez be "L" helyett.

DONE - Ha ures az indexkonyvtar, akkor epitsen oda egy ures indexet.

DONE - egy qfilter a mondatraszegmentalas utanra, ami eldobja az egeszet,
ha rosszak az aranyok mondatra vagy bajtra. (A helye mar megvan.)

DONE - egy qfilter, ami eldobja az egeszet, ha nagyon keves mondatpar van,
vagy nagyon rossz az aranya a parhuzamositas elotti mondatpar-szamnak.
(A helye mar megvan.)

DONE - A bena felhasznalok nagyon el tudjak csufitani a korpuszt, ha ugyetlenul
felcserelik az angol es magyar upload mezot, vagy mindket oldalra ugyanazt toltik fel.
Kell egy nyelvdetektor filter.
UPDATE: Kesz, integralva. ~/big/experiments/textcat.pezo/LM eroforras
a $ROOT/resources/textcat.models konyvtarba kerult.

DONE - Tortenetesen a hunglish2 txt resze es a sajtofigyelo egyarant latin2, de
nemigen lehet meguszni vagy egy autodetect-et, ami a txt-bol raw-ba alakitaskor fut,
vagy egy kulon utftxt source formatot a txt melle. Az elobbi a kenyelmesebb es
biztonsagosabb, csak meg kellene csinalni.

DONE - Most a bisen.is_indexed -et olyanra hasznalom, ami igazibol nem az.
Ugy kellene csinalni, hogy is_duplicate null jelenti azt, hogy me'g nem tortent duplumszures,
es ha tortent, akkor mar true vagy false.
Ennek megfeleloen a ("from Bisen o where o.isIndexed is null and isDuplicate is null") is modositando Bisen.java-ban.

DONE ~/.m2/settings.xml -ba beleraktam, hogy lehessen mavenbol deployolni.

NOTDONE (Mert a catalina.out-ba is jo lesz) - indexer kimenet logba. datum.indexer.log
DONE - Runga ujabb korpuszat betenni.

DONE - Harness commandfajl a /big1/Work/Pipeline/cvs -t referalja,
ami felesleges NFS elerest jelent a kozelrol. Atcuccolni.

DONE - A metaadatok is utf8-ban legyenek.

NOTDONE - Bug: Hogy a turoba tud 37 masodpercig futni ez a trivialis join:
select count(*) from bisen,doc where bisen.doc=doc.id and doc.author=63;

- WTF Szegyenszemre az alkalmazas konyvtarabol kell inditani a tomcat-et, nem tudom miert:
daniel@kozel:/srv/tomcat-6.0.20/webapps/hunglish-0.1.0-SNAPSHOT$ sudo /etc/init.d/tomcat6 start

NOTDONE - Nagy inkonzisztencia-veszely: Ha a harness adatkonyvtarrendszereben ket fajlnak
is ugyanaz az azonositoja, de mas a kiterjesztese:
harness/en/pdf/2.en.pdf harness/en/txt/2.en.txt
akkor csendben kivalasztja az egyiket, feltehetoleg nem azt, amit igazibol akarsz.
Mondjuk ha ez eloall, az mar reg rossz, az is igaz.

DONE - Atkoltozni a big3-ra.

DONE - megepiteni az uploadtable-t a reszkorpuszokhoz, es vegigzavarni a gepet rajta.

DONE - Most eppen ki van kommentelve bisen.updateIsIndexed(true), mert tul lassu. Valami kotegelt timestampeles kellene ide.

DONE - A hasheles fix stabil percenkent 12.3 darabos sebesseggel tortenik, ez igy 62 nap a hunglish1.nolaw-ra.

DONE - utf8 bug.

---
Adatforrasok

Osszesites:

reszkorpusz        #doc   #bisen   futasido perc
------------------------------------------------
hunglish1.nolaw     606  1143848   112
hunglish1.justlaw 10270   958086   713
hunglish2           185  1088991   113
sajtofigyelo       1475    96557    76
hunglish2.justlaw  4204   839863?    ? (egyelore becsult ertekek TODO)
------------------------------------------------
total - h2.law    12536  3287482  1014 (~17 ora)
t. incl. h2.law   16740  4127345  1300 (~22 ora) (egyelore becsult ertekek TODO)


= Hunglish 1.0 no Law
originally at: ~/big/experiments/hunglish.webservice/importolddata/doclist.nolaw , bar ez (maga is generalt fajl) Voa-t tartalmazott VOA helyett.
now at: hunglish1.nolaw , de ehhez minden raw-t txt-re kellett atnevezni, lasd hunglish1.nolaw/readme.txt
Igy keszult az uploadtable:
cat /big3/Work/HunglishMondattar/datasources/hunglish1.nolaw/doclist.nolaw | awk 'BEGIN{t="\t";FS=t;dir="/big3/Work/HunglishMondattar/datasources/hunglish1.nolaw"}  { print $3 t $4 t $5 t $2 t dir "/hu/" $9 ".hu.txt" t dir "/en/" $9 ".en.txt" }' > /big3/Work/HunglishMondattar/datasources/hunglish1.nolaw/hunglish1.nolaw.uploadtable
TODO A subtitles reszkorpuszban benne maradtak a wiIIing bugok.
Szerintem ezt a harness elott kellene rendberakni,
a tcg/scripts/normalizeSubtitles.py fajlban.

= Hunglish 1.0 Law
originally at: ~/big/experiments/hunglish.webservice/importolddata/doclist.justlaw
now at: hunglish1.justlaw
TODO

= Hunglish 2.0 no Law (Runga)
originally at: ~/big/experiments/runga200 , de azon sokat csiszoltam, mikor idetettem, most mar az itteni a nyero.
now at: hunglish2
Igy keszult az uploadtable:
cat /big3/Work/HunglishMondattar/datasources/hunglish2/master.final.txt | cut -f1,2,4,5 | awk 'BEGIN{FS="\t"} (($2!="odt")&&($4!="odt")&&($2!="lit")&&($4!="lit"))' | python master2uploadtable.py /big3/Work/HunglishMondattar/datasources/hunglish2 > /big3/Work/HunglishMondattar/datasources/hunglish2/hunglish2.uploadtable
TODO A lit-ekbol txt-t kellene csinalni, ne menjenek ka'rba.

= Hunglish 2.0 Law (Farkas Andras)
originally at: hunglish2.justlaw/zips (andras.farkas@yahoo.com, 2011 jan 1 levele nyoman)
now at: hunglish2.justlaw
Igy keszult az uploadtable:
TODO Me'g sehogy. Esetleg beszerezni a 2005-2006 jogszabalyokat is.

= Telekom sajtofigyelo
originally at: /big1/User/nemeth/telekom/EVSZAM/txt/{en,hu}
(Figyelem, van egy nemeth/Telekom konyvtar is, abban ugyanez van kicsit tovabbfeldolgozva, nekunk most nem kell.)
now at: sajtofigyelo

= Telekom belso
originally at: /big1/User/nemeth/telekom/telekom_[12]
Ezt ne zuttyantsuk bele, nem szeretnek a Telekomosok.
TODO Viszont azert rakjuk rendbe datasources alatt.

= Feliratok 2
originally at: /big3/User/zseder/Progs/subs/movies_v3.tar.gz tvshows.tar.gz
TODO Ezzel me'g dokumentum-szintu parositasi munka is van. A tvshows.tar.gz nem is tudom,
hogy ugyanolyan formatumu-e. A v2 es v3 kozti kulonbseg is bizonytalan, de
a v3 talan mar kicsit szukitve van potencialis dokumentum-parositas szempontbol.

---
Archivum

Ket nagy machine upload tortent, az egyikben csak a H1 nemjogi resze volt,
a masikban az osszes tobbi reszkorpusz csak az nem.
Ezek a hozzajuk tartozo harness konyvtarral tekinthetoek teljes, visszaallithato
adathalmaznak.

/big3/Work/HunglishMondattar/datasources/hunglish1.nolaw/hunglish1.nolaw.mysqldump
/big3/Work/HunglishMondattar/harness.data.archive.nolaw

/big3/Work/HunglishMondattar/datasources/allexceptnolaw.mysqldump
/big3/Work/HunglishMondattar/harness.data.archive.allexceptnolaw

TODO Ez elavult, mert megvaltozott a sema. A vegen ujracsinalni.
/big3/Work/HunglishMondattar/hunglish-webapp/src/main/python/nolaw.mysqldump.beforeindex
harness.data.nolaw

---
IGY KELL LEGYALULNI NULLARA AZ OSSZES ADATOT A RENDSZERBOL,
HOGY AZTAN EGY SZUZ ADATBAZIST PISZKOLHASSUNK OSSZE

mvn tomcat:stop # mac

cat /big3/Work/HunglishMondattar/hunglish-webapp/create.sql | mysql -uhunglish -psw6x2the --default-character-set=utf8 hunglishwebapp
sudo su tomcat6
cd /big3/Work/HunglishMondattar/deployment/
rm fileUpload/*
rm logs/*
rm harness.data/*/*/*
rm hunglishIndex/*
rm hunglishIndexTmp/*
cp /big3/Work/HunglishMondattar/hunglish-webapp/src/main/resources/hunglishIndex/* hunglishIndex/
exit # su tomcat6, ujra daniel

cd /big3/Work/HunglishMondattar/hunglish-webapp/src/main/python/
cat /big3/Work/HunglishMondattar/datasources/hunglish1.nolaw/hunglish1.nolaw.uploadtable | python machine_upload.py hunglish sw6x2the hunglishwebapp

mvn tomcat:start # mac

sudo su tomcat6
cd  /big3/Work/HunglishMondattar/deployment/
nohup bash /big3/Work/HunglishMondattar/deployment/harness_cronjob.sh &
