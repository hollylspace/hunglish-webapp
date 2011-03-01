package hu.mokk.hunglish.domain;

import java.util.ArrayList;
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
public class Author {
	transient public static Long dummyAuthorId = new Long(-666);

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

	/*
	@Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            Author attached = this.entityManager.find(Author.class, this.id);
            this.entityManager.remove(attached);
        }
    } */

	@Transactional
    public void flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }

	@Transactional
    public void merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Author merged = this.entityManager.merge(this);
        this.entityManager.flush();
        this.id = merged.getId();
    }

	public static final EntityManager entityManager() {
        EntityManager em = new Author().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countAuthors() {
        return (Long) entityManager().createQuery("select count(o) from Author o").getSingleResult();
    }

	public static List<Author> findAllAuthorsWithDummy() {
		List<Author> result = new ArrayList<Author>();
		Author dummy = new Author();
		dummy.setId(dummyAuthorId);
		dummy.setName(" pick an author");
		result.add(dummy);
        result.addAll(entityManager().createQuery("select o from Author o order by o.name").getResultList());
        return result;
    }

	public static List<Author> findAllAuthors() {
        return entityManager().createQuery("select o from Author o").getResultList();
    }

	public static Author findAuthor(Long id) {
        if (id == null) return null;
        return entityManager().find(Author.class, id);
    }

	public static List<Author> findAuthorEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("select o from Author o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public String toString() {
        /*StringBuilder sb = new StringBuilder();
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Version: ").append(getVersion()).append(", ");
        sb.append("Name: ").append(getName()).append(", ");
        sb.append("Description: ").append(getDescription());*/
        return getName(); //sb.toString();
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
}
