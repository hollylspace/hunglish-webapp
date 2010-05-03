/**
 * 
 */
package hu.mokk.hunglish.jmorph;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.lucene.analysis.StemmerAnalyzer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author bpgergo
 * 
 */
@Configurable
public class AnalyzerProvider {

	private String resourceEncodingEn;
	private String resourceEncodingHu;
	private String huAff;
	private String huDic;
	private String huDerivatives;
	private String huCompounds;
	private int huRecursionDepth;
	private String enAff;
	private String enDic;
	private int enRecursionDepth;

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
		Definition affixDef = acHu.createDefinition(huAff, resourceEncodingHu,
				SwordAffixReader.class);
		Definition dicDef = acHu.createDefinition(huDic, resourceEncodingHu,
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
		Definition affixDef = acEn.createDefinition(enAff, resourceEncodingEn,
				EnglishAffixReader.class);
		Definition dicDef = acEn.createDefinition(enDic, resourceEncodingEn,
				EnglishReader.class);
		acEn.setRecursionDepth(affixDef, enRecursionDepth);
		JMorphFactory jf = new JMorphFactory();
		enAnalyser = jf.build(new Definition[] { affixDef, dicDef });
	}

	//init-method
	public void initAnalyzerProvider() throws IOException, IllegalAccessException,
			InstantiationException, ParseException {

		try {
			lemmatizerMap = new HashMap<String, LemmatizerWrapper>();

			initHuAnalyser();
			initEnAnalyser();
			AnalyserControl acHu = new AnalyserControl(
					AnalyserControl.ALL_COMPOUNDS);
			AnalyserContext analyserContextHu = new AnalyserContext(acHu);
			boolean huStripDerivates = true;
			Lemmatizer huLemmatizer = new net.sf.jhunlang.jmorph.lemma.LemmatizerImpl(
					huAnalyser, huStripDerivates, analyserContextHu);

			AnalyserControl acEn = new AnalyserControl(
					AnalyserControl.ALL_COMPOUNDS);
			AnalyserContext analyserContextEn = new AnalyserContext(acEn);
			boolean enStripDerivates = true;
			Lemmatizer enLemmatizer = new net.sf.jhunlang.jmorph.lemma.LemmatizerImpl(
					enAnalyser, enStripDerivates, analyserContextEn);

			LemmatizerWrapper huLemmatizerWrapper = new LemmatizerWrapper(huLemmatizer,false/*returnOOVOrig*/,true/*returnOrig*/,false/*returnPOS*/);
			lemmatizerMap.put(Bisen.huSentenceFieldName, huLemmatizerWrapper);

			LemmatizerWrapper enLemmatizerWrapper = new LemmatizerWrapper(enLemmatizer,false/*returnOOVOrig*/,true/*returnOrig*/,false/*returnPOS*/);
			lemmatizerMap.put(Bisen.enSentenceFieldName, enLemmatizerWrapper);

			analyzer = new StemmerAnalyzer(Version.LUCENE_30, lemmatizerMap);
		} catch (IOException e) {
			throw new RuntimeException("Cannot initialize jmorph stemmer.", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot initialize jmorph stemmer.", e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Cannot initialize jmorph stemmer.", e);
		} catch (ParseException e) {
			throw new RuntimeException("Cannot initialize jmorph stemmer.", e);
		}

	}

	/*******************************************/
	public Analyzer getAnalyzer() {
		return analyzer;
	}
	public Map<String, LemmatizerWrapper> getLemmatizerMap() {
		return lemmatizerMap;
	}

	/*******************************************/
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

	public void setResourceEncodingEn(String resourceEncodingEn) {
		this.resourceEncodingEn = resourceEncodingEn;
	}

	public void setResourceEncodingHu(String resourceEncodingHu) {
		this.resourceEncodingHu = resourceEncodingHu;
	}

	
	
}
