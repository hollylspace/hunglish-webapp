package hu.mokk.hunglish.domain;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooEntity
public class Upload {

	@Transient
	private CommonsMultipartFile huFileData;	
	@Transient
	private CommonsMultipartFile enFileData;
	
    @NotNull
    @ManyToOne(targetEntity = Genre.class)
    @JoinColumn
    private Genre genre;

    @ManyToOne(targetEntity = Author.class)
    @JoinColumn
    private Author author;
    
    @Size(max = 255)
    private String authorName;

    @Size(max = 255)
    private String enTitle;

    @Size(max = 255)
    private String huTitle;

    @NotNull
    @Size(max = 1)
    private String isProcessed;

    private Boolean isApproved;

    @Size(max = 255)
    private String huFilePath;

    @Size(max = 255)
    private String enFilePath;

    @Size(max = 4000)
    private String huSentence;

    @Size(max = 4000)
    private String enSentence;

	public String toString() {
        StringBuilder sb = new StringBuilder();
        //sb.append("Id: ").append(getId()).append(", ");
        //sb.append("Version: ").append(getVersion()).append(", ");
        //sb.append("Genre: ").append(getGenre()).append(", ");
        //sb.append("Author: ").append(getAuthor()).append(", ");
        sb.append("EnTitle: ").append(getEnTitle()).append(", ");
        //sb.append("HuTitle: ").append(getHuTitle()).append(", ");
        //sb.append("IsProcessed: ").append(getIsProcessed()).append(", ");
        //sb.append("IsApproved: ").append(getIsApproved()).append(", ");
        //sb.append("HuFilePath: ").append(getHuFilePath()).append(", ");
        //sb.append("EnFilePath: ").append(getEnFilePath()).append(", ");
        //sb.append("HuSentence: ").append(getHuSentence()).append(", ");
        //sb.append("EnSentence: ").append(getEnSentence());
        return sb.toString();
    }

	public CommonsMultipartFile getHuFileData() {
		return huFileData;
	}

	public void setHuFileData(CommonsMultipartFile huFileData) {
		this.huFileData = huFileData;
	}

	public CommonsMultipartFile getEnFileData() {
		return enFileData;
	}

	public void setEnFileData(CommonsMultipartFile enFileData) {
		this.enFileData = enFileData;
	}

	public Genre getGenre() {
        return this.genre;
    }

	public void setGenre(Genre genre) {
        this.genre = genre;
    }

	public Author getAuthor() {
        return this.author;
    }

	public void setAuthor(Author author) {
        this.author = author;
    }	
	
	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getEnTitle() {
        return this.enTitle;
    }

	public void setEnTitle(String enTitle) {
        this.enTitle = enTitle;
    }

	public String getHuTitle() {
        return this.huTitle;
    }

	public void setHuTitle(String huTitle) {
        this.huTitle = huTitle;
    }

	public String getIsProcessed() {
        return this.isProcessed;
    }

	public void setIsProcessed(String isProcessed) {
        this.isProcessed = isProcessed;
    }

	public Boolean getIsApproved() {
        return this.isApproved;
    }

	public void setIsApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }

	public String getHuFilePath() {
        return this.huFilePath;
    }

	public void setHuFilePath(String huFilePath) {
        this.huFilePath = huFilePath;
    }

	public String getEnFilePath() {
        return this.enFilePath;
    }

	public void setEnFilePath(String enFilePath) {
        this.enFilePath = enFilePath;
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
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }

	@Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            Upload attached = this.entityManager.find(Upload.class, this.id);
            this.entityManager.remove(attached);
        }
    }

	@Transactional
    public void flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }

	@Transactional
    public void merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Upload merged = this.entityManager.merge(this);
        this.entityManager.flush();
        this.id = merged.getId();
    }

	public static final EntityManager entityManager() {
        EntityManager em = new Upload().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countUploads() {
        return (Long) entityManager().createQuery("select count(o) from Upload o").getSingleResult();
    }

	public static List<Upload> findAllUploads() {
        return entityManager().createQuery("select o from Upload o").getResultList();
    }

	public static Upload findUpload(Long id) {
        if (id == null) return null;
        return entityManager().find(Upload.class, id);
    }

	public static List<Upload> findUploadEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("select o from Upload o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
