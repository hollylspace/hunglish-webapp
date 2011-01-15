package hu.mokk.hunglish.domain;

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
public class Genre {

    @NotNull
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String description;

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
        Genre merged = this.entityManager.merge(this);
        this.entityManager.flush();
        this.id = merged.getId();
    }

	public static final EntityManager entityManager() {
        EntityManager em = new Genre().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countGenres() {
        return (Long) entityManager().createQuery("select count(o) from Genre o").getSingleResult();
    }

	
	private static List<Genre> allGenres;
	public static List<Genre> findAllGenres() {
		if (allGenres == null){
			allGenres = entityManager().createQuery("select o from Genre o").getResultList();
		}
        return allGenres;
    }

	public static Genre findGenre(Long id) {
        if (id == null) return null;
        return entityManager().find(Genre.class, id);
    }

	public static List<Genre> findGenreEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("select o from Genre o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public String getName() {
        return this.name;
    }

	public void setName(String name) {
        this.name = name;
    }

	public String getDescription() {
        return this.description;
    }

	public void setDescription(String description) {
        this.description = description;
    }

	public String toString() {
        //StringBuilder sb = new StringBuilder();
        //sb.append("Id: ").append(getId()).append(", ");
        //sb.append("Version: ").append(getVersion()).append(", ");
        //sb.append("Name: ").append(getName()).append(", ");
        //sb.append("Description: ").append(getDescription());
        return getName(); //sb.toString();
    }
}
