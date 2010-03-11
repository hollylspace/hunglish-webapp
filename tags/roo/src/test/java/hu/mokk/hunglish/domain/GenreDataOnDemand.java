package hu.mokk.hunglish.domain;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.dod.RooDataOnDemand;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import hu.mokk.hunglish.domain.Genre;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

@Component
@Configurable
@RooDataOnDemand(entity = Genre.class)
public class GenreDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<Genre> data;

	public Genre getNewTransientGenre(int index) {
        hu.mokk.hunglish.domain.Genre obj = new hu.mokk.hunglish.domain.Genre();
        obj.setName("name_" + index);
        return obj;
    }

	public Genre getSpecificGenre(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size()-1)) index = data.size() - 1;
        Genre obj = data.get(index);
        return Genre.findGenre(obj.getId());
    }

	public Genre getRandomGenre() {
        init();
        Genre obj = data.get(rnd.nextInt(data.size()));
        return Genre.findGenre(obj.getId());
    }

	public boolean modifyGenre(Genre obj) {
        return false;
    }

	@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void init() {
        if (data != null) {
            return;
        }
        
        data = hu.mokk.hunglish.domain.Genre.findGenreEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Genre' illegally returned null");
        if (data.size() > 0) {
            return;
        }
        
        data = new java.util.ArrayList<hu.mokk.hunglish.domain.Genre>();
        for (int i = 0; i < 10; i++) {
            hu.mokk.hunglish.domain.Genre obj = getNewTransientGenre(i);
            obj.persist();
            data.add(obj);
        }
    }
}
