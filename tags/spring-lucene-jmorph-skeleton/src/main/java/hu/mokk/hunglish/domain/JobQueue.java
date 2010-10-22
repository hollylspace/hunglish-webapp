package hu.mokk.hunglish.domain;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.transaction.annotation.Transactional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooEntity
public class JobQueue {

	/**
	 * N not processed; S started; F finished
	 */
    @NotNull
    @Size(max = 1)
    private String status;

    @NotNull
    private Date requestTimestamp;
	
    private Date startTimestamp;
    
    private Date endTimestamp;
    
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
            Genre attached = this.entityManager.find(Genre.class, this.id);
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
        JobQueue merged = this.entityManager.merge(this);
        this.entityManager.flush();
        this.id = merged.getId();
    }

	public static final EntityManager entityManager() {
        EntityManager em = new Genre().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countJobQueues() {
        return (Long) entityManager().createQuery("select count(o) from JobQueue o").getSingleResult();
    }

	public static List<Genre> findAllJobQueues() {
        return entityManager().createQuery("select o from JobQueue o").getResultList();
    }

	public static JobQueue findJobQueue(Long id) {
        if (id == null) return null;
        return entityManager().find(JobQueue.class, id);
    }

	public static List<Genre> findJobQueueEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("select o from JobQueue o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getRequestTimestamp() {
		return requestTimestamp;
	}

	public void setRequestTimestamp(Date requestTimestamp) {
		this.requestTimestamp = requestTimestamp;
	}

	public Date getStartTimestamp() {
		return startTimestamp;
	}

	public void setStartTimestamp(Date startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	public Date getEndTimestamp() {
		return endTimestamp;
	}

	public void setEndTimestamp(Date endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Request time: ").append(getRequestTimestamp().toString()).append(", ");
        sb.append("Status: ").append(getStatus());
        return sb.toString();
    }
}

