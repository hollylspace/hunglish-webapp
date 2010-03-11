package hu.mokk.hunglish.domain;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.dod.RooDataOnDemand;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import hu.mokk.hunglish.domain.Author;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

@Component
@Configurable
@RooDataOnDemand(entity = Author.class)
public class AuthorDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<Author> data;

	public Author getNewTransientAuthor(int index) {
        hu.mokk.hunglish.domain.Author obj = new hu.mokk.hunglish.domain.Author();
        obj.setName("name_" + index);
        return obj;
    }

	public Author getSpecificAuthor(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size()-1)) index = data.size() - 1;
        Author obj = data.get(index);
        return Author.findAuthor(obj.getId());
    }

	public Author getRandomAuthor() {
        init();
        Author obj = data.get(rnd.nextInt(data.size()));
        return Author.findAuthor(obj.getId());
    }

	public boolean modifyAuthor(Author obj) {
        return false;
    }

	@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void init() {
        if (data != null) {
            return;
        }
        
        data = hu.mokk.hunglish.domain.Author.findAuthorEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Author' illegally returned null");
        if (data.size() > 0) {
            return;
        }
        
        data = new java.util.ArrayList<hu.mokk.hunglish.domain.Author>();
        for (int i = 0; i < 10; i++) {
            hu.mokk.hunglish.domain.Author obj = getNewTransientAuthor(i);
            obj.persist();
            data.add(obj);
        }
    }
}
