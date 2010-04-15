package hu.mokk.hunglish.domain;

import java.util.List;
import javax.persistence.Entity;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.transaction.annotation.Transactional;
import hu.mokk.hunglish.domain.Doc;
import javax.validation.constraints.NotNull;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Version;
import javax.validation.constraints.Size;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooEntity
public class Bisen {

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

	@PersistenceContext
	transient EntityManager entityManager;

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

	@Transactional
	public void persist() {
		if (this.entityManager == null)
			this.entityManager = entityManager();
		this.entityManager.persist(this);
	}

	@Transactional
	public void remove() {
		if (this.entityManager == null)
			this.entityManager = entityManager();
		if (this.entityManager.contains(this)) {
			this.entityManager.remove(this);
		} else {
			Bisen attached = this.entityManager.find(Bisen.class, this.id);
			this.entityManager.remove(attached);
		}
	}

	@Transactional
	public void flush() {
		if (this.entityManager == null)
			this.entityManager = entityManager();
		this.entityManager.flush();
	}

	@Transactional
	public void merge() {
		// merge(this);
		if (this.entityManager == null)
			this.entityManager = entityManager();
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

	public static List<Bisen> findBisenEntriesByDoc(int firstResult,
			int maxResults, Doc doc) {
		return entityManager().createNativeQuery(
				"select * from bisen where doc = " + doc.getId().toString(),
				Bisen.class).setFirstResult(firstResult).setMaxResults(
				maxResults).getResultList();
		// return
		// entityManager().createQuery("select o from Bisen o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
	}

	public static List<Bisen> findBisenEntries(int firstResult, int maxResults) {
		return entityManager().createQuery("select o from Bisen o")
				.setFirstResult(firstResult).setMaxResults(maxResults)
				.getResultList();
	}

	@Transactional
	public void updateHashCode() {
		Long huHash = new Long(stripPunctuation(this.getHuSentence()).hashCode());
		Long enHash = new Long(stripPunctuation(this.getEnSentence()).hashCode());
		Query update = entityManager().createQuery("update Bisen set enSentenceHash = ?, huSentenceHash = ? where id  = ?");
		update.setParameter(1, enHash);
		update.setParameter(2, huHash);
		update.setParameter(3, this.getId());
		update.executeUpdate();
	}

	
	
	public static void updateHashCodes() {
		// List<Bisen> bisens =
		// entityManager().createQuery("select o from Bisen o").getResultList();
		List<Bisen> bisens = entityManager().createQuery(
				"from Bisen o") // where o.doc.id = 2
				.getResultList();
		for (Bisen bisen : bisens) {
			bisen.updateHashCode();
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		// sb.append("Id: ").append(getId()).append(", ");
		// sb.append("Version: ").append(getVersion()).append(", ");
		sb.append("Doc: ").append(getDoc()).append(", ");
		// sb.append("IsIndexed: ").append(getIsIndexed()).append(", ");
		sb.append("LineNumber: ").append(getLineNumber()).append(", ");
		sb.append("Upvotes: ").append(getUpvotes()).append(", ");
		sb.append("Downvotes: ").append(getDownvotes()).append(", ");
		// sb.append("HuSentence: ").append(getHuSentence()).append(", ");
		// sb.append("EnSentence: ").append(getEnSentence());
		return sb.toString();
	}

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
		// reduce spaces
		return sb.toString().replaceAll(" +", " ").trim();
	}

}
