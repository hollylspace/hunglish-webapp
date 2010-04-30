/**
 * 
 */
package hu.mokk.hunglish.jmorph;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.lucene.analysis.StemmerAnalyzer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import net.sf.jhunlang.jmorph.analysis.Analyser;
import net.sf.jhunlang.jmorph.analysis.AnalyserContext;
import net.sf.jhunlang.jmorph.analysis.AnalyserControl;
import net.sf.jhunlang.jmorph.factory.Definition;
import net.sf.jhunlang.jmorph.factory.JMorphFactory;
import net.sf.jhunlang.jmorph.lemma.Lemmatizer;
import net.sf.jhunlang.jmorph.parser.ParseException;
import net.sf.jhunlang.jmorph.sample.AnalyserConfig;
import net.sf.jhunlang.jmorph.sword.parser.EnglishAffixReader;
import net.sf.jhunlang.jmorph.sword.parser.EnglishReader;
import net.sf.jhunlang.jmorph.sword.parser.SwordAffixReader;
import net.sf.jhunlang.jmorph.sword.parser.SwordReader;

/**
 * @author bpgergo
 * 
 */
@Configurable
public class AnalyzerProvider {

	@Autowired
	private String resouceEncodingEn = "ISO-8859-1";
	@Autowired
	private String resouceEncodingHu = "ISO-8859-2";
	@Autowired
	private String huAff = "C:\\workspaces\\hunglish-webapp\\trunk\\data\\jmorph\\mispellRC1.aff";
	@Autowired
	private String huDic = "C:\\workspaces\\hunglish-webapp\\trunk\\data\\jmorph\\mispellRC1.dic";
	@Autowired
	private String huDerivatives = "C:\\workspaces\\hunglish-webapp\\trunk\\data\\jmorph\\derivatives.lst";
	@Autowired
	private String huCompounds = "C:\\workspaces\\hunglish-webapp\\trunk\\data\\jmorph\\compound.lst";
	@Autowired
	private int huRecursionDepth = 4;
	@Autowired
	private String enAff = "C:\\workspaces\\hunglish-webapp\\trunk\\data\\jmorph\\en.aff";
	@Autowired
	private String enDic = "C:\\workspaces\\hunglish-webapp\\trunk\\data\\jmorph\\en.dic";
	@Autowired
	private int enRecursionDepth = 2;


	/*******************************************/
	private Analyser huAnalyser;
	private Analyser enAnalyser;

	/*******************************************/
	Map<String, LemmatizerWrapper> lemmatizerMap;
	Analyzer analyzer;

	/*******************************************/
	private void initHuAnalyser() throws IOException, IllegalAccessException,
			InstantiationException,
			net.sf.jhunlang.jmorph.parser.ParseException {
		AnalyserConfig acHu = new AnalyserConfig();
		Definition affixDef = acHu.createDefinition(huAff, resouceEncodingHu,
				SwordAffixReader.class);
		Definition dicDef = acHu.createDefinition(huDic, resouceEncodingHu,
				SwordReader.class);
		acHu.setDerivativeFile(affixDef, huDerivatives);
		acHu.setRecursionDepth(affixDef, huRecursionDepth);
		acHu.setCompoundFile(affixDef, huCompounds);
		JMorphFactory jf = new JMorphFactory();
		huAnalyser = jf.build(new Definition[] { affixDef, dicDef });
	}

	private void initEnAnalyser() throws IOException, IllegalAccessException,
			InstantiationException, ParseException {
		AnalyserConfig acEn = new AnalyserConfig();
		Definition affixDef = acEn.createDefinition(enAff, resouceEncodingEn,
				EnglishAffixReader.class);
		Definition dicDef = acEn.createDefinition(enDic, resouceEncodingEn,
				EnglishReader.class);
		acEn.setRecursionDepth(affixDef, enRecursionDepth);
		JMorphFactory jf = new JMorphFactory();
		enAnalyser = jf.build(new Definition[] { affixDef, dicDef });
	}

	public void initLemmatizer() throws IOException, IllegalAccessException,
			InstantiationException, ParseException {
		
		lemmatizerMap = new HashMap<String, LemmatizerWrapper>();
		
		initHuAnalyser();
		initEnAnalyser();
		AnalyserControl acHu = new AnalyserControl(
				AnalyserControl.ALL_COMPOUNDS);
		AnalyserContext analyserContextHu = new AnalyserContext(acHu);
		boolean huStripDerivates = true;
		Lemmatizer huLemmatizer = new net.sf.jhunlang.jmorph.lemma.LemmatizerImpl(
				huAnalyser, huStripDerivates, analyserContextHu);

		AnalyserControl acRight = new AnalyserControl(
				AnalyserControl.ALL_COMPOUNDS);
		AnalyserContext analyserContextRight = new AnalyserContext(acRight);
		boolean enStripDerivates = true;
		Lemmatizer enLemmatizer = new net.sf.jhunlang.jmorph.lemma.LemmatizerImpl(
				enAnalyser, enStripDerivates, analyserContextRight);

		LemmatizerWrapper huLemmatizerWrapper = new LemmatizerWrapper();
		huLemmatizerWrapper.setLemmatizer(huLemmatizer);
		huLemmatizerWrapper.setReturnOOVOrig(false);
		huLemmatizerWrapper.setReturnOrig(true);
		huLemmatizerWrapper.setReturnPOS(false);
		lemmatizerMap.put(Bisen.huSentenceFieldName, huLemmatizerWrapper);

		LemmatizerWrapper enLemmatizerWrapper = new LemmatizerWrapper();
		enLemmatizerWrapper.setLemmatizer(enLemmatizer);
		enLemmatizerWrapper.setReturnOOVOrig(false);
		enLemmatizerWrapper.setReturnOrig(true);
		enLemmatizerWrapper.setReturnPOS(false);
		lemmatizerMap.put(Bisen.enSentenceFieldName, enLemmatizerWrapper);
	}

	/*******************************************/
	private void getLemmatizerMap()
			throws IOException, IllegalAccessException, InstantiationException,
			ParseException {
		if (lemmatizerMap== null || lemmatizerMap.isEmpty()) {
			initLemmatizer();
		}
	}

	synchronized public Analyzer getAnalyzer() {
		try {
			getLemmatizerMap();
		} catch (IOException e) {
			throw new RuntimeException("Cannot initialize jmorph stemmer.", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot initialize jmorph stemmer.", e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Cannot initialize jmorph stemmer.", e);
		} catch (ParseException e) {
			throw new RuntimeException("Cannot initialize jmorph stemmer.", e);
		}
		if (analyzer == null) {
			analyzer = new StemmerAnalyzer(Version.LUCENE_30, lemmatizerMap);
		}
		return analyzer;
	}

	/*******************************************/
	public void setResouceEncodingEn(String resouceEncodingEn) {
		this.resouceEncodingEn = resouceEncodingEn;
	}

	public void setResouceEncodingHu(String resouceEncodingHu) {
		this.resouceEncodingHu = resouceEncodingHu;
	}

	public void setHuAff(String huAff) {
		this.huAff = huAff;
	}

	public void setHuDic(String huDic) {
		this.huDic = huDic;
	}

	public void setHuDerivatives(String huDerivatives) {
		this.huDerivatives = huDerivatives;
	}

	public void setHuCompounds(String huCompounds) {
		this.huCompounds = huCompounds;
	}

	public void setHuRecursionDepth(int huRecursionDepth) {
		this.huRecursionDepth = huRecursionDepth;
	}

	public void setEnAff(String enAff) {
		this.enAff = enAff;
	}

	public void setEnDic(String enDic) {
		this.enDic = enDic;
	}

	public void setEnRecursionDepth(int enRecursionDepth) {
		this.enRecursionDepth = enRecursionDepth;
	}

}
