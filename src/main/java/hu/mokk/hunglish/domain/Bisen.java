package hu.mokk.hunglish.domain;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooEntity
public class Bisen {
	transient private static Log logger = LogFactory.getLog(Bisen.class);

	@NotNull
	@ManyToOne(targetEntity = Doc.class)
	@JoinColumn
	private Doc doc;

	@NotNull
	private Boolean isIndexed;

	private Integer lineNumber;

	private Long upvotes;

	private Long downvotes;

	@Size(max = 4000)
	private String huSentence;

	@Size(max = 4000)
	private String enSentence;

	private Long huSentenceHash;

	private Long enSentenceHash;

	private Boolean isDuplicate;

	private Timestamp indexedTimestamp;

	// @NotNull
	@Size(max = 1)
	private String approved;
	// @NotNull
	@Size(max = 1)
	private String copyright;
	
	
	@PersistenceContext
	transient EntityManager entityManager;


	transient String huSentenceView;
	transient String enSentenceView;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Version
	@Column(name = "version")
	private Integer version;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getVersion() {
		return this.version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	private void getEntityManager() {
		if (this.entityManager == null)
			this.entityManager = entityManager();
	}

	@Transactional
	public void persist() {
		getEntityManager();
		this.entityManager.persist(this);
	}

	@Transactional
	public void remove() {
		getEntityManager();
		if (this.entityManager.contains(this)) {
			this.entityManager.remove(this);
		} else {
			Bisen attached = this.entityManager.find(Bisen.class, this.id);
			this.entityManager.remove(attached);
		}
	}

	@Transactional
	public void flush() {
		getEntityManager();
		this.entityManager.flush();
	}

	@Transactional
	public void merge() {
		getEntityManager();
		Bisen merged = this.entityManager.merge(this);
		this.entityManager.flush();
		this.id = merged.getId(); // */
	}

	public static final EntityManager entityManager() {
		EntityManager em = new Bisen().entityManager;
		if (em == null)
			throw new IllegalStateException(
					"Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
		return em;
	}

	public static long countBisens() {
		return (Long) entityManager().createQuery(
				"select count(o) from Bisen o").getSingleResult();
	}

	/**
	 * 
	 * @return count of Bisentences which are a duplicate of this
	 */
/* commented out for good
	public long countDuplicates() {
		Long result = (Long) entityManager().createQuery(
				"select count(o) from Bisen o where "
						+ "o.isIndexed = true and "
						+ "o.huSentenceHash = :huhash and "
						+ "o.enSentenceHash = :enhash and " + "o.id != :id")
				.setParameter("huhash", this.huSentenceHash).setParameter(
						"enhash", this.enSentenceHash).setParameter("id",
						this.id).getSingleResult();
		return result;
	}
*/
	
	public static List<Bisen> findAllBisens() {
		throw new RuntimeException(new IllegalAccessException());
		// return
		// null;//entityManager().createQuery("select o from Bisen o").getResultList();
	}

	public static Bisen findBisen(Long id) {
		if (id == null)
			return null;
		return entityManager().find(Bisen.class, id);
	}

	@SuppressWarnings("unchecked")
	public static List<Bisen> findBisenEntriesByDoc(int firstResult,
			int maxResults, Doc doc) {
		return entityManager().createQuery(
				"select o from Bisen o where o.doc.id = ?").setParameter(1,
				doc.getId().toString()).setFirstResult(firstResult)
				.setMaxResults(maxResults).getResultList();
	}

	@SuppressWarnings("unchecked")
	public static List<Bisen> findBisenEntries(int firstResult, int maxResults) {
		return entityManager().createQuery(
				"select o from Bisen o order by o.id").setFirstResult(
				firstResult).setMaxResults(maxResults).getResultList();
	}

	@SuppressWarnings("unchecked")
	public static List<Bisen> findBisenEntries(List<Long> ids) {
		return entityManager().createQuery(
				"select o from Bisen o where o.id in (:ids) order by o.id")
				.setParameter("ids", ids).getResultList();
	}

	@Transactional
	public void updateHashCode() {
		if (this.getEnSentenceHash() == null
				|| this.getHuSentenceHash() == null) {
			this.huSentenceHash = new Long(stripPunctuation(
					this.getHuSentence()).hashCode());
			this.enSentenceHash = new Long(stripPunctuation(
					this.getEnSentence()).hashCode());
			this.merge();
		}
	}

	@Transactional
	public void updateIsIndexed(Boolean value) {
		this.setIsIndexed(value);
		// this.setIsDuplicate(!value);
		this.setIndexedTimestamp(new Timestamp((new Date()).getTime()));
		this.merge();
	}

	@Transactional
	public static void batchUpdateIsIndexed(List<Long> ids, Boolean value) {
		EntityManager em = entityManager();
		em.setFlushMode(FlushModeType.COMMIT);
		em.getTransaction().begin();
		entityManager().createNativeQuery(
				"update bisen set is_indexed = ?, indexed_timestamp = now() "
						+ "where id in (?) ").setParameter(1, value)
				.setParameter(2, ids).executeUpdate();
		em.getTransaction().commit();
	}

	/*
	 * @SuppressWarnings("unchecked") public static void updateHashCodes() {
	 * List<Bisen> bisens = entityManager().createQuery("from Bisen o")
	 * .getResultList(); for (Bisen bisen : bisens) { bisen.updateHashCode(); }
	 * }
	 */

	// public static void index(Bisen bisen, IndexWriter iwriter) throws
	// CorruptIndexException, IOException {
	// iwriter.addDocument(bisen.toLucene());
	// //bisen.updateIsIndexed(true);
	// }

	// @SuppressWarnings("unchecked")
	/*
	 * public static void indexSen(IndexWriter iwriter, Long id) { List<Bisen>
	 * bisens = entityManager() .createQuery(
	 * "from Bisen o where o.id = :senid") .setParameter("senid", id)
	 * .getResultList(); for (Bisen bisen : bisens) { try { index(bisen,
	 * iwriter); } catch (CorruptIndexException e) { throw new
	 * RuntimeException("Error while indexing", e); } catch (IOException e) {
	 * throw new RuntimeException("Error while indexing", e); } } } //
	 */

	// @SuppressWarnings("unchecked")
	/*
	 * public static void indexDoc(IndexWriter iwriter, Long docId) {
	 * List<Bisen> bisens = entityManager() .createQuery(
	 * "from Bisen o where o.doc.id = :docid") .setParameter("docid", docId)
	 * .getResultList(); for (Bisen bisen : bisens) { try { index(bisen,
	 * iwriter); } catch (CorruptIndexException e) { throw new
	 * RuntimeException("Error while indexing", e); } catch (IOException e) {
	 * throw new RuntimeException("Error while indexing", e); } } } //
	 */

	/**
	 * This is going to be a full table scan All bisen record not already hashed
	 * will get hashcodes
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	public static boolean updateHashCodeAll() {
		boolean result = false;
		List<Bisen> bisens = entityManager()
				.createQuery(
						"from Bisen o where o.huSentenceHash is null or o.enSentenceHash is null")
				.getResultList();
		for (Bisen bisen : bisens) {
			result = true;
			bisen.updateHashCode();
		}
		return result;
	}


	@SuppressWarnings("unchecked")
	public void indexAll(IndexWriter iwriter) {
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Bisen [doc=");
		builder.append(doc);
		builder.append(", isIndexed=");
		builder.append(isIndexed);
		builder.append(", lineNumber=");
		builder.append(lineNumber);
		builder.append(", upvotes=");
		builder.append(upvotes);
		builder.append(", downvotes=");
		builder.append(downvotes);
		builder.append(", huSentence=");
		builder.append(huSentence);
		builder.append(", enSentence=");
		builder.append(enSentence);
		builder.append(", huSentenceHash=");
		builder.append(huSentenceHash);
		builder.append(", enSentenceHash=");
		builder.append(enSentenceHash);
		builder.append(", isDuplicate=");
		builder.append(isDuplicate);
		builder.append(", indexedTimestamp=");
		builder.append(indexedTimestamp);
		builder.append(", approved=");
		builder.append(approved);
		builder.append(", copyright=");
		builder.append(copyright);
		builder.append(", id=");
		builder.append(id);
		builder.append(", version=");
		builder.append(version);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * only letters, digits and space (0x20) will preserved all other characters
	 * will be deleted
	 * 
	 * @param s
	 * @return
	 */
	private static String stripPunctuation(String s) {
		StringBuffer sb = new StringBuffer();
		if (s.length() > 0) {
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if (c == 0x20 || Character.isLetterOrDigit(c)) {
					sb = sb.append(Character.toLowerCase(s.charAt(i)));
				}
			}
		}
		// and finally reduce spaces
		return sb.toString().replaceAll(" +", " ").trim();
	}

	public static String idFieldName = "id";
	public static String genreFieldName = "doc.genre.id";
	public static String authorFieldName = "doc.author.id";
	public static String huSentenceFieldName = "huSen";
	public static String enSentenceFieldName = "enSen";
	public static String huSentenceStemmedFieldName = "huSenStemmed";
	public static String enSentenceStemmedFieldName = "enSenStemmed";

	public static String fieldName2LemmatizerMapCode(String fieldName) {
		String res = null;
		if (huSentenceStemmedFieldName.equals(fieldName)) {
			res = huSentenceFieldName;
		} else if (enSentenceStemmedFieldName.equals(fieldName)) {
			res = enSentenceFieldName;
		} else {
			res = fieldName;
		}
		return res;
	}

	public static Bisen toBisen(Document document) {
		return findBisen(new Long(document.getField(idFieldName).stringValue()));
	}

	public Document toLucene() {
		Document result = new Document();

		result
				.add(new Field(idFieldName, this.getId().toString(),
						Field.Store.YES, Field.Index.NOT_ANALYZED,
						Field.TermVector.NO));

		result.add(new Field(genreFieldName, this.doc.getGenre().getId()
				.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED,
				Field.TermVector.NO));

		result.add(new Field(authorFieldName, this.doc.getAuthor().getId()
				.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED,
				Field.TermVector.NO));

		result.add(new Field(huSentenceFieldName, this.huSentence,
				Field.Store.YES, Field.Index.ANALYZED,
				Field.TermVector.WITH_POSITIONS_OFFSETS));
		result.add(new Field(huSentenceStemmedFieldName, this.huSentence,
				Field.Store.NO, Field.Index.ANALYZED,
				Field.TermVector.WITH_POSITIONS_OFFSETS));

		result.add(new Field(enSentenceFieldName, this.enSentence,
				Field.Store.YES, Field.Index.ANALYZED,
				Field.TermVector.WITH_POSITIONS_OFFSETS));
		result.add(new Field(enSentenceStemmedFieldName, this.enSentence,
				Field.Store.NO, Field.Index.ANALYZED,
				Field.TermVector.WITH_POSITIONS_OFFSETS));

		return result;

	}

	public Doc getDoc() {
		return this.doc;
	}

	public void setDoc(Doc doc) {
		this.doc = doc;
	}

	public Boolean getIsIndexed() {
		return this.isIndexed;
	}

	public void setIsIndexed(Boolean isIndexed) {
		this.isIndexed = isIndexed;
	}

	public Integer getLineNumber() {
		return this.lineNumber;
	}

	public void setLineNumber(Integer lineNumber) {
		this.lineNumber = lineNumber;
	}

	public Long getUpvotes() {
		return this.upvotes;
	}

	public void setUpvotes(Long upvotes) {
		this.upvotes = upvotes;
	}

	public Long getDownvotes() {
		return this.downvotes;
	}

	public void setDownvotes(Long downvotes) {
		this.downvotes = downvotes;
	}

	public String getHuSentence() {
		return this.huSentence;
	}

	public void setHuSentence(String huSentence) {
		this.huSentence = huSentence;
	}

	public String getEnSentence() {
		return this.enSentence;
	}

	public void setEnSentence(String enSentence) {
		this.enSentence = enSentence;
	}

	public Long getHuSentenceHash() {
		return huSentenceHash;
	}

	public void setHuSentenceHash(Long huSentenceHash) {
		this.huSentenceHash = huSentenceHash;
	}

	public Long getEnSentenceHash() {
		return enSentenceHash;
	}

	public void setEnSentenceHash(Long enSentenceHash) {
		this.enSentenceHash = enSentenceHash;
	}

	public Boolean getIsDuplicate() {
		return isDuplicate;
	}

	public void setIsDuplicate(Boolean isDuplicate) {
		this.isDuplicate = isDuplicate;
	}

	public Timestamp getIndexedTimestamp() {
		return indexedTimestamp;
	}

	public void setIndexedTimestamp(Timestamp indexedTimestamp) {
		this.indexedTimestamp = indexedTimestamp;
	}

	public String getHuSentenceView() {
		return huSentenceView;
	}

	public void setHuSentenceView(String huSentenceView) {
		this.huSentenceView = huSentenceView;
	}

	public String getEnSentenceView() {
		return enSentenceView;
	}

	public void setEnSentenceView(String enSentenceView) {
		this.enSentenceView = enSentenceView;
	}

	public String getApproved() {
		return approved;
	}

	public void setApproved(String approved) {
		this.approved = approved;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

}
