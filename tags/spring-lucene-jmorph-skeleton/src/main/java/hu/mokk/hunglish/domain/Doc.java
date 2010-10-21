package hu.mokk.hunglish.domain;

import java.util.List;
import javax.persistence.Entity;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.transaction.annotation.Transactional;
import javax.validation.constraints.Size;
import hu.mokk.hunglish.domain.Genre;
import javax.validation.constraints.NotNull;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;
import hu.mokk.hunglish.domain.Author;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooEntity
public class Doc {

    @Size(max = 255)
    private String oldDocid;

    @ManyToOne(targetEntity = Upload.class)
    @JoinColumn
    private Upload upload;

    @NotNull
    @ManyToOne(targetEntity = Genre.class)
    @JoinColumn
    private Genre genre;

    @NotNull
    @ManyToOne(targetEntity = Author.class)
    @JoinColumn
    private Author author;

    @NotNull
    @Size(max = 255)
    private String enTitle;

    @Size(max = 255)
    private String huTitle;

    private Boolean isOpenContent;

    @Size(max = 255)
    private String huRawFilePath;

    @Size(max = 255)
    private String enRawFilePath;

    @NotNull
    @Size(max = 255)
    private String alignedFilePath;

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
            Doc attached = this.entityManager.find(Doc.class, this.id);
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
        Doc merged = this.entityManager.merge(this);
        this.entityManager.flush();
        this.id = merged.getId();
    }

	public static final EntityManager entityManager() {
        EntityManager em = new Doc().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countDocs() {
        return (Long) entityManager().createQuery("select count(o) from Doc o").getSingleResult();
    }

	public static List<Doc> findAllDocs() {
        return entityManager().createQuery("select o from Doc o").getResultList();
    }

	public static Doc findDoc(Long id) {
        if (id == null) return null;
        return entityManager().find(Doc.class, id);
    }

	public static List<Doc> findDocEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("select o from Doc o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public String toString() {
        //StringBuilder sb = new StringBuilder();
        //sb.append("Id: ").append(getId()).append(", ");
        //sb.append("Version: ").append(getVersion()).append(", ");
        //sb.append("OldDocid: ").append(getOldDocid()).append(", ");
        //sb.append("Genre: ").append(getGenre()).append(", ");
        //sb.append("Author: ").append(getAuthor()).append(", ");
        //sb.append("EnTitle: ").append(getEnTitle()).append(", ");
        //sb.append("HuTitle: ").append(getHuTitle()).append(", ");
        //sb.append("IsOpenContent: ").append(getIsOpenContent()).append(", ");
        //sb.append("HuRawFilePath: ").append(getHuRawFilePath()).append(", ");
        //sb.append("EnRawFilePath: ").append(getEnRawFilePath()).append(", ");
        //sb.append("AlignedFilePath: ").append(getAlignedFilePath());
        return getEnTitle(); //sb.toString();
    }

	public String getOldDocid() {
        return this.oldDocid;
    }

	public void setOldDocid(String oldDocid) {
        this.oldDocid = oldDocid;
    }

	public Upload getUpload() {
		return upload;
	}

	public void setUpload(Upload upload) {
		this.upload = upload;
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

	public Boolean getIsOpenContent() {
        return this.isOpenContent;
    }

	public void setIsOpenContent(Boolean isOpenContent) {
        this.isOpenContent = isOpenContent;
    }

	public String getHuRawFilePath() {
        return this.huRawFilePath;
    }

	public void setHuRawFilePath(String huRawFilePath) {
        this.huRawFilePath = huRawFilePath;
    }

	public String getEnRawFilePath() {
        return this.enRawFilePath;
    }

	public void setEnRawFilePath(String enRawFilePath) {
        this.enRawFilePath = enRawFilePath;
    }

	public String getAlignedFilePath() {
        return this.alignedFilePath;
    }

	public void setAlignedFilePath(String alignedFilePath) {
        this.alignedFilePath = alignedFilePath;
    }
}
