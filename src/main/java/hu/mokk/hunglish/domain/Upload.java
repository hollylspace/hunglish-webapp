package hu.mokk.hunglish.domain;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	private static String[] validExtensionElements = {"doc","htm","html","pdf", "rtf", "txt", "srt"};
	private static Set<String> validExtensions = new HashSet<String>(Arrays.asList(validExtensionElements));
	private static char extensionSeparator = '.'; 

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

    /**
     * Y = processed, N = not processed, E = processed with error, L = processed without error but the result is of bad quality
     */
    // TODO quick hack for the upload view @NotNull
    @Size(max = 1)
    private String isProcessed;

    
    // TODO quick hack for the upload view @NotNull
    private Timestamp createdTimestamp;

    private Timestamp harnessedTimestamp;

    
    @Size(max = 255)
    private String huUploadedFilePath;

    @Size(max = 255)
    private String enUploadedFilePath;

    // TODO quick hack for the upload view @NotNull
    @Size(max = 255)
    private String huOriginalFileName;
        
    // TODO quick hack for the upload view @NotNull
    @Size(max = 255)
    private String enOriginalFileName;

    private Long huOriginalFileSize;
    private Long enOriginalFileSize;
    
    private Long huRawFileSize;
    private Long enRawFileSize;
    
    private Long huSentenceCount;
    private Long enSentenceCount;

    private Long alignBisentenceCount;

	// @NotNull
	@Size(max = 1)
	private String approved;
	// @NotNull
	@Size(max = 1)
	private String copyright;
    
	//`old_docid` varchar(255) not NULL
	////not null for hunglish1, empty string for hunglish2
	@Size(max = 255)
	private String oldDocid; 
	
    @Transient
    private String enExtension;
    
    @Transient
    private String huExtension;
    
	/**
	 * validate the uploaded files (no missing files, only allowed extensions)
	 */
    public void validate(){
		if (this.getHuFileData() == null){
			throw new IllegalArgumentException("The Hungarian file is null");
		} else if (this.getHuFileData().getOriginalFilename() == null){
			throw new IllegalArgumentException("The Hungarian filename is null");
		}
		if (this.getEnFileData() == null){
			throw new IllegalArgumentException("The English file is null");
		}else if (this.getEnFileData().getOriginalFilename() == null){
			throw new IllegalArgumentException("The English filename is null");
		}
		if (!validExtensions.contains(this.enExtension)){
			throw new IllegalArgumentException("Invalid English file extension:"+this.enExtension);
		}
		if (!validExtensions.contains(this.huExtension)){
			throw new IllegalArgumentException("Invalid Hungarian file extension:"+this.huExtension);
		}
		if (this.authorName == null && (this.author.getId() == null || Author.dummyAuthorId.equals(this.author.getId()))){
			throw new IllegalArgumentException("Choose an existing Author or provide a name for a new one");			
		}
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

	public String getHuUploadedFilePath() {
        return this.huUploadedFilePath;
    }

	public void setHuUploadedFilePath(String huUploadedFilePath) {
        this.huUploadedFilePath = huUploadedFilePath;
    }

	public String getEnUploadedFilePath() {
        return this.enUploadedFilePath;
    }

	public void setEnUploadedFilePath(String enUploadedFilePath) {
        this.enUploadedFilePath = enUploadedFilePath;
    }

	public String getHuOriginalFileName() {
		return huOriginalFileName;
	}

	private static String getExtension(String fileName){
		int dot = fileName.lastIndexOf(extensionSeparator);
		return fileName.substring(dot + 1).toLowerCase();
	}
	
	public void setHuOriginalFileName(String huOriginalFileName) {
		this.huOriginalFileName = huOriginalFileName;
		this.huExtension = getExtension(huOriginalFileName);
	}

	public String getEnOriginalFileName() {
		return enOriginalFileName;
	}

	public void setEnOriginalFileName(String enOriginalFileName) {
		this.enOriginalFileName = enOriginalFileName;
		this.enExtension = getExtension(enOriginalFileName);
	}

	public String getEnExtension() {
		return enExtension;
	}


	public String getHuExtension() {
		return huExtension;
	}

	
	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}


	public void setCreatedTimestamp(Timestamp createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}


	public Timestamp getHarnessedTimestamp() {
		return harnessedTimestamp;
	}


	public void setHarnessedTimestamp(Timestamp harnessedTimestamp) {
		this.harnessedTimestamp = harnessedTimestamp;
	}


	public Long getHuOriginalFileSize() {
		return huOriginalFileSize;
	}


	public void setHuOriginalFileSize(Long huOriginalFileSize) {
		this.huOriginalFileSize = huOriginalFileSize;
	}


	public Long getEnOriginalFileSize() {
		return enOriginalFileSize;
	}


	public void setEnOriginalFileSize(Long enOriginalFileSize) {
		this.enOriginalFileSize = enOriginalFileSize;
	}


	public Long getHuRawFileSize() {
		return huRawFileSize;
	}


	public void setHuRawFileSize(Long huRawFileSize) {
		this.huRawFileSize = huRawFileSize;
	}


	public Long getEnRawFileSize() {
		return enRawFileSize;
	}


	public void setEnRawFileSize(Long enRawFileSize) {
		this.enRawFileSize = enRawFileSize;
	}


	public Long getHuSentenceCount() {
		return huSentenceCount;
	}


	public void setHuSentenceCount(Long huSentenceCount) {
		this.huSentenceCount = huSentenceCount;
	}


	public Long getEnSentenceCount() {
		return enSentenceCount;
	}


	public void setEnSentenceCount(Long enSentenceCount) {
		this.enSentenceCount = enSentenceCount;
	}


	public Long getAlignBisentenceCount() {
		return alignBisentenceCount;
	}


	public void setAlignBisentenceCount(Long alignBisentenceCount) {
		this.alignBisentenceCount = alignBisentenceCount;
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

	public static long countUnprocessedUploads() {
        return (Long) entityManager().createQuery("select count(o) from Upload o where o.isProcessed = 'N'").getSingleResult();
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
	public String getOldDocid() {
		return oldDocid;
	}

	public void setOldDocid(String oldDocid) {
		this.oldDocid = oldDocid;
	}


	@Override
	public String toString() {
		return "Upload [huFileData=" + huFileData + ", enFileData="
				+ enFileData + ", genre=" + genre + ", author=" + author
				+ ", authorName=" + authorName + ", enTitle=" + enTitle
				+ ", huTitle=" + huTitle + ", isProcessed=" + isProcessed
				+ ", createdTimestamp=" + createdTimestamp
				+ ", harnessedTimestamp=" + harnessedTimestamp
				+ ", huUploadedFilePath=" + huUploadedFilePath
				+ ", enUploadedFilePath=" + enUploadedFilePath
				+ ", huOriginalFileName=" + huOriginalFileName
				+ ", enOriginalFileName=" + enOriginalFileName
				+ ", huOriginalFileSize=" + huOriginalFileSize
				+ ", enOriginalFileSize=" + enOriginalFileSize
				+ ", huRawFileSize=" + huRawFileSize + ", enRawFileSize="
				+ enRawFileSize + ", huSentenceCount=" + huSentenceCount
				+ ", enSentenceCount=" + enSentenceCount
				+ ", alignBisentenceCount=" + alignBisentenceCount
				+ ", approved=" + approved + ", copyright=" + copyright
				+ ", oldDocid=" + oldDocid + ", enExtension=" + enExtension
				+ ", huExtension=" + huExtension + ", id=" + id + ", version="
				+ version + "]";
	}

}
