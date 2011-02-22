---
Mit kell itt csinalni?

Igy tesztelem:

# Legyalulom a harness jatszoteret:
rm -rf /big3/Work/HunglishMondattar/harness.data/*/*

# Legyalulom a tablat, es letrehozom a zsanereket:
cat ../../../create.sql | mysql -uhunglish -psw6x2the --default-character-set=utf8 hunglishwebapp

# Beleteszek ket teszt-fajlpart az upload tablaba:
cat /big3/Work/HunglishMondattar/datasources/hunglish1.nolaw/hunglish1.nolaw.uploadtable | head -2 | python machine_upload.py hunglish sw6x2the hunglishwebapp

# Maga az alkalmazas:
python control_harness.py hunglish sw6x2the hunglishwebapp harness.data logprefix

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
Futasidok

3m rekord, konkretan a ket irodalmi es az elso jogi.

a ket irodalmi korpusz harness-elese : 5 ora 30 perc
a jogi korpusz harness-elese : 13 ora
duplumszures : 8 ora (a duplumok megtalalasa 8 perc, a tobbi az sql update)
indexeles : 2 ora 30 perc

az indexeles 20000-es kotegekbol all, egy koteg futasideje:
15 sec  - get the next batch from database done resultList size:20000
20 sec  - call indexBisen for all Bisens and store corresponding database updates in a batch jdbcStatement done
104 sec - indexreader addIndexes done
14 sec  - Indexing batch updates commited

Ezt most el is mentettem /big3/Work/HunglishMondattar/hunglish-webapp/src/main/python/lit12law1.sav ala.

---
TODO

- A timestamp fogalmat rosszul hasznaljuk. Egyes helyeken, mint bisen.indexed_timestamp kiirtando,
mas helyeken, mint job_queue es upload, lecserelendo egy rendes datumra.
Ez is emiatt van:
- Az alkalmazas nem tudja a recordbol letrehozni az upload objektumot,
ha nem hazudok be egy kitoltott harnessed_timestamp mezot.
Gergo szedje ki ezt a hulye checket, Daniel utana mar kiszedheti
a behazudast a machine_upload-bol.

- Az adatbazissema sulyos hibaja, hogy ha megserul a lucene index, akkor nagyon nehez
kimazsolazni, hogy melyik bisen-eket kell ujraepiteskor belerakni. Legyen egy state, ami ezt fejezi ki.

- Highlight search results.

- Nem latom, hogy a fileUpload tenyleg lekezelne azt, amikor ma'r ismeri az authort,
es csak az id-re kellene referenciat raknia.

- SystemCall.java defaultCommand vagy quartz.properties:uploadjob.path adja a cronjob helyet?
Csak az egyik adja!

- http://kozel.mokk.bme.hu:8080/hunglish/doc URL (a size attributum nelkul meghivva)
kilistazza mind a sokszazat. Ki se merem probalni, hogy a hunglish/bisen mit csinal.
UPDATE: Ugy tunik most mar ez default 10-re lett atallitva, de &size=100000 attributummal
azert me'g mindig hagyja megszivatni magat.

- Use case: Utolag rajovok, hogy egy kis bash scriptet bele kellene tenni a qualityfilterbe.
Megteszem, de visszamenoleg is meg kellene tenni. Eszkoz: dumpoljuk ki id-vel az osszes
bisen-t, es ha fennakad a scripten, akkor tiltsuk le.

- A duplumszures eleg lassu. Amikor mar jo nagy az adatbazis, akkor egy rovid doksi
feltoltesetol kezdve igy jonnek a fazisok:
1. par masodperc harness.
2. 2 min 30 sec duplumfilter (Lehet, hogy jobb, ha az adatbazis helyett a python vegzi a rendezest?)
3. 3 min 20 sec indexeles (Tulindexeles bug: futott egy teljesen felesleges kort egy
masodik 20000-es batch-csel, aminek nulla eleme volt, de igy is 50 sec-et szotymorgott vele.)
UPDATE: Ezt ujra kellene merni az uj create.sql-en, valszeg javult a helyzet valamennyit.

- Session management bug: HP Sat, Jan 22, 2011 at 2:35 AM levele.

- Vegre kigondoltam, hogy hogyan nem lesz szerzoi jogi balhe abbol, hogy
hozzaferheto az alignment. Az UploadController hasheli az id es a szerzo
konkatenaciojat (tesztben eleg ehelyett az unobfuscated id), es ebbol ad egy url-t.
Amig upload.is_processed=N, addig csak egy "bocs, turelmet kerek" uzenetet ad.
Ha processed, akkor megnezi, hogy now()-harnessed_timestamp tobb-e, mint egy nap.
Ha igen, akkor azt mondja, hogy "eroforras elavult".
Ha nem, akkor egyetlen weboldalra kidumpolja a kerdeses alignment
metaadatait, es a bisen-eket. (Ha nem fogadtuk el a doksit, akkor
persze csak a metaadatokat.)

- Feltolteskor ha mar fut egy quartz job, akkor a quartz nem moge-schedule-olja
az ujat, hanem bejelenti, hogy most nem tud inditani.

- Ne relativ path-on keresse az index es upload konyvtarakat, hanem fixen
/big3/Work/HunglishMondattar/deployment alatt.
Persze a laptopokon nem big3, legyen valami lokalis conf,
ami alapjan startup (es nem build) idoben beallithato. Utobbi azert fontos,
hogy mac-rol is tudjam a kozelen deployolni.

- Valami ronda nagy (quartz?) leak, ami miatt ujra kell inditgatni
a tomcat-et nehany tomcat:deploy utan.

- A quartz finnyas arra, hogy honnan kell inditania a cronjobot.

- Ha nem akarok csak emiatt feltolteni egy kamu dokumentumpart, akkor nem tudom
triggerelni a rendszeren belul a harness elinditasat. Jo, nem nagy gond,
tomcat6 user elinditja a
nohup bash /big3/Work/HunglishMondattar/deployment/harness_cronjob.sh &
progit. De akkor is.

- A loadgame-savegame annyira kozel-specifikusak, hogy Gergonek nincs
sok haszna egyelore beloluk. A savegame addig nem lesz ipari szintu,
amig nem allitja le a service-t.

- Masodpeldany van a deployment konyvtarban a cronjob-bol.

- Az exception-ok informacioit a webapp es a control_harness is elnyelegeti.
UPDATE: a control_harnesst mar megjavitottam, de nem teljesen, mert nem derul ki
a legalso szint.

- Legyen kovetkezetesen hasznalva a control_harness logolasaban az INFO,WARN,ERROR.

- control_harness-bol es indexelesbol semmikeppen ne fusson ketto egyszerre,
az nagyon durva inkonzisztenciakhoz vezet. Lock-oljuk valami munkanaplo tablaban,
arra az esetre, ha a quartz elszurna, es megis megengedne kettot.

- Van-e valami komolyabb tranzakcionalitasi problema, tehat peldaul ha elfogy
az indexelo memoriaja, akkor ertelmes allapotba hozza-e magat a rendszer?

- Kornai feature request-je: A lucene tokenizalo legyen olyan okos,
hogy a dog's szot is megtalalja, ha a dog-ra keresek.

- SearchRequest legyen Bisen helyett az, amiben halad a searchreuquest. A Bisen raeroltetett. (Low priority.)

HARNESS, HIBAKEZELES:

- A barom html2text meghagyja utf8-nak a tenylegesen utf8 szoveget,
viszont a html entitasokat lelkesen atkonvertalja latin-1-re. Az
eredmeny a jogi szovegek eseteben egy olyan keverek, ahol a fejlec
latin-1, a test utf8. A CELEX-nel me'g be lehetne drotozni, hogy utf8
es kesz, de a nagyvilagban persze vannak latin-2 html-ek.
UPDATE: A CELEX-re kezzel megcsinaltam egy elo-konverziot latin2-re.

- A control_harness legalabb a fontosabb fordulopontokon jegyezze fel
a datumot. Az kulonosen bosszanto, hogy nem tudom mikor kezdte el
a duplumszures harom munkafazisat.

- Tul szigoru a minosegszures, rettenetes mennyisegu hunglish1.nolaw
anyag bukott meg rajta. Beloni pontosabban.

- Maradtak bent html entitasok valamelyik konverter kimeneteben.

- control_harness.py:decideIfWorthIndexing() lehetne kicsit kifinomultabb.
tcg/scripts/filtersen.py szinte'n.

- Tovezes is legyen a pipeline-ban az align elott, hogy ne legyunk rosszabbak, mint
a regi hunglish.

- Kicsit hianyzik a pipeline-bol, hogy parhuzamositott mondatraszegmentalt
adatot (forditomemoria) is bele lehessen tolni.


ADMIN:

- mac:~/drafts/hunglish.install.rtf becsekkolasa.

- Brutalis hibalehetoseg: az ujrainditas utan ugy latom nem tomcat6 userkent fut
a service, hanem root-kent. Ez nem csak azert brutal, mert security hole, hanem
azert is, mert nem lesz joga a tomcat-nek bolygatni a root altal letrehozott
fajlokat.

- Tomcat access logoljon a virtualis gepen is.

- Zsolt telepitsen legujabb, sun-os javat, es mavent. A tomcat a sun-os java home-ra mutasson.
De az is lehet, hogy az Aspects JAR problemat megoldja, ha a tomcat es a maven ugyanazzal
a java-val (openjdk, sun jdk) megy.

- atirtam az init.d/tomcat6-ot sun-rol openjdk path-ra!!!!!!!!!!!!

- Modositottam /etc/mysql/my.cnf konfigot ezzel a sorral:
wait_timeout=604800
Ez kesobb valszeg nem is fog kelleni. Kiszedni, kulonosen
ha esetleg kiderul, hogy rizikoforras.

- Ez nem bugreport, csak egy megjegyzes:
Ha leallitom control_harness kozben a tomcat-et a feje folul, akkor szeme se
rebben, csinalgatja tovabb a fajlokat. Ha tenyleg le akarom loni azt is,
akkor a control_harness-t (es nem a harness_cronjob.sh-t) kell leloni.
Semmilyen problemat nem latszik okozni, mert a felbeszakadtat elorol kezdi,
az adatbazisba nem kerul be hogy E, hanem marad N, az pedig nem gond,
hogy irt mar par fajlt a harness.data-ba, majd felulirja oket sajat magukkal.


FUTURE FEJLESZTESI IGENYEK:

- Autocomplete az author-ra. Ha ez van, akkor nem is kell dropdown menu.

- Menjem vegig UTF8 alatt is. Nagyon sok kis gonosz minosegellenorzesi lepes
tamaszkodik az egybajtos kodolasra, de semmi olyan, ami lecserelhetetlen lenne.

- Clusteresites. harnessbol mar most is futhatna tobb parhuzamosan
(bar a catalog.tmp-t fel kellene szamolni) a duplumszures eredendoen
egy szalra korlatozando, de az indexelest megintcsak van ertelme
szetosztani, mert a tmp megepitese a fo futasido, a mergeles gyorsabb.

- Nem lenne olyan nagy ugy a duplumszureskor felvenni a vezerpeldanyhoz,
hogy hany tarsa veszett el a duplum-harcmezon, es aztan ezzel indexeleskor
felpontozni.

- Szivesen felpontoznam aszerint is, hogy mi a korpusz, mi a zsaner, vagy
aka'r ki a felvivo.

- Most mar lassan ideje elgondolkozni azon, hogy hogyan fog tortenni egy
dokumentum utolagos letiltasa.

- Vegiggondoltuk, hogy nem kell teljesen feldulni a rendszert akkor sem,
ha megengedjuk a bisen-ek modositasat. Felvenni egy flaget, hogy modositott,
atirni a szovegmezot, torolni az indexbol es feljegyezni, hogy duplumszuretlen.
Persze az eredeti peldany duplumat elvileg ekkor fel kellene szabaditani.
Ez csak akkor gond, ha nagyon fontos szotari tetelnek (igen-yes) eppen a nemduplum
tetelet irja at valaki, es nem marad reprezentans.

- Ezt valszeg nem fogjuk megcsinalni, de lenne ertelme:
A pipeline-t szetszedni ket reszre valahol a sen elott, ugy, hogy az eleje
baromi gyorsan lefusson, es reszponzivan vissza tudjon kohogni olyanokat,
hogy "tul hosszu", "nem angol", "tul kulonbozo hosszu" meg ilyesmi.
A lassu reszek tovabbra is cronban futnanak.

- A kisbetusites miatt nem rakja feljebb azokat a talalatokat, amiknek
azonos a kapitalizacioja a keresokulcsszoval.

- Szegyen hogy menet kozben mennyire nem voltunk unit-test based-ek, de
me'g a vegen sem lenne haszontalan par tesztet megcsinalni legalabb a Java
reszehez.


LEZART DOLGOK:

NOTDONE - egy biztonsagi mentest kellene csinalni az indexrol, mielott meghivjuk az indexert.

NOTDONE - Aramvonalasabba kellene tenni a kozel deploy-t. A mac-emen mar teljesen jo:
mvn jetty:run > cout 2> cerr &
, bar csak parancssorbol, eclipse-bol valamiert nem talalja az eroforrast.

NOTDONE - Marhasag: a mysql interpreter miert nem jeleniti meg a bool ertekueket, mint az is_duplicate?
UPDATE: select cast(is_duplicate as signed),bisen.* from bisen limit 2 ; megoldja.

DONE - Legyen a fileUpload fajlnev kozelebb a harness szabvanyhoz.

DONE - Indexer jdbc eleres parametereit properties fajlbol venni.

DONE - FileUpload-nal ne lehessen kivalasztani az All-t zsanerkent. (Ne csak a frontend tiltsa le.)

DONE - Nagybetus keresokulcsszavakat nem kezel le. ('James bug')

NOTDONE (Ez most maradjon rejtely) - Hogy kerul oda egy regesregi fileUpload/824_HU.srt , amikor ott elvileg me'g soha nem
tartott az upload.id kurzor? A logokban sincs nyoma, hogy harnesselte volna.
-rw-r--r-- 1 tomcat6 tomcat6 19029 2011-01-24 11:46 /big3/Work/HunglishMondattar/deployment/fileUpload/825_EN.txt
-rw-r--r-- 1 tomcat6 tomcat6 34279 2011-01-24 11:46 /big3/Work/HunglishMondattar/deployment/fileUpload/825_HU.srt

DONE - qualityfilter: tablazatok, szamoszlopok, nullbajtos mondatok.

NOTDONE (Sokkal erosebbeket csinaltam, amik kivaltjak.)
- ~/scripts/tcom/nogarbage.sh integralasa, vagy legalabbis a relevansabb reszeinek
atmentese, ha vannak egyaltalan. A sajtofigyelo mennyire jol megy enelkul?

DONE - A hunglish2 celex szovegek utf8 kodolasuak, meglepodik rajtuk a harness.
(Raadasul a fejlecuk latin1.)

DONE - Integralni a hunglish2.justlaw-ba Nemeth Andras legujabb gyujteset
a koztes idobol:
/big3/Work/HunglishMondattar/datasources/hunglish2.justlaw/zips
2005_L_R_HTML.rar 2006_L_R_HTML.rar 2007_L_R_HTML.zip
EU_acquis_4reszben_tabsep.rar

NOTDONE - A duplumszures harom munkafazisa nem olyan koltsegu, mint szeretnem,
bar valoszinuleg ebbe bele kell nyugodnom. 8 perc a komplex select.
Aztan van a gigantikus batch update 3m rekordon, es 400k dup bisen rekord
egyenkenti bejegyezgetese. A ketto egyutt eppen 8 ora. Sokallom. Sajnos nem
jegyezzuk fel, hogy mennyi belole az egyik meg a masik.

NOTDONE (Valszeg vaklarma volt)
- Nekem ugy tunt (Wed, Jan 12, 2011 at 10:45 PM level), hogy
a spring nem frissitette egy darabig a doc-ot a webes adminfeluleten,
mikozben a harness nagyban irta. A bisen-nel nem ez tortent.
Baromira megragni, hogy milyen problemakhoz vezet, hogy
a spring (aki cache-el valszeg) olvassa az objektumokat, mikozben
a harness irja az adatbazist. Me'g ennel is jobban megragni
ugyanezt harness vs. UploadController parositasban, hiszen
ok egyszerre irjak az adatbazist.

DONE - Sajnos a txt2raw idonkent IBM855-et detektal, es hazavagja a kodolast.
Talan bele kellene valahogy drotozni, hogy csak utf-et vagy
latin-1-2-t szabad talalnia. cat /big3/Work/HunglishMondattar/deployment/harness.data/hu/txt/290.hu.txt | python /big3/Work/HunglishMondattar/tcg/scripts/txt2raw.py hu
UPDATE: Mon, Jan 24, 2011 at 1:02 AM levelemben leirtam, hogy ez tenyleg nagyon gyakori a filmes korpuszon.
./txt2raw.bugs -ban gyulik, hogy mibol mit csinalt a magyar txt-k kozul.
UPDATE2: Javitva, egy eleg kifinomulatlan modon:
ha nem utf-8-at detektal, akkor feltetelezi, hogy a
kimeno latin kodolas az ervenyes, tehat nem csinal semmit.

DONE - Rossz volt a normalizalo, ezert gondolatjeles mondatokat nem duplumszurt.

DONE - Tunjenek el azok a menupontok, amik semmi masra nem jok, csak hogy inkonzisztensse
tegyek az adatbazist.

DONE - Sajnos van egy kulon doc.id, ahelyett, hogy megorokolne' az upload.id-t.
Ez nem jo igy, nagyon nehezkes debuggolaskor osszenezni a harness logokat
a mysql tablakkal.

NOTDONE (Maradjon ez rejtely, tobbszor nem fordult elo.) - Miert ragadt
bent ket darab 100%-on futo mysqld_safe processz?

DONE - Az ismeretlen szavakat a jmorph nem veszi fel sajat tovukkent.
Ergo minden ismeretlen szora automatice nulla talalatot kapunk.
http://kozel.mokk.bme.hu:8080/hunglish/search?huSentence=keletkezett&enSentence=happened&doc.genre=-10
http://kozel.mokk.bme.hu:8080/hunglish/search?huSentence=Daala&enSentence=&doc.genre=-10

DONE - control_harness nyugodtan logoljon tobbmindent, ne kelljen innen-onnan
osszeneznie, hogy mi a fajlpa'r vagy hogy hany bimondat kerult be.
A redundencianak semmi hatranya itt.

DONE - srt formatum feltoltese, konverzioja. Mar csak tesztelni kellene.

DONE - Nem mukodik a duplumszures. Ez peldaul egy olyan mondatpar rogton, amit
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

DONE - Layout: ROOT/ alabbiak: fileUpload harnessData hunglishIndex logs mysqlDump

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
