package hu.mokk.hunglish.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.dod.RooDataOnDemand;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import hu.mokk.hunglish.domain.Doc;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

@Component
@Configurable
@RooDataOnDemand(entity = Doc.class)
public class DocDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<Doc> data;

	@Autowired
    private AuthorDataOnDemand authorDataOnDemand;

	@Autowired
    private GenreDataOnDemand genreDataOnDemand;

	public Doc getNewTransientDoc(int index) {
        hu.mokk.hunglish.domain.Doc obj = new hu.mokk.hunglish.domain.Doc();
        obj.setAlignedFilePath("alignedFilePath_" + index);
        obj.setAuthor(authorDataOnDemand.getRandomAuthor());
        obj.setEnTitle("enTitle_" + index);
        obj.setGenre(genreDataOnDemand.getRandomGenre());
        obj.setIsOpenContent(new Boolean(true));
        return obj;
    }

	public Doc getSpecificDoc(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size()-1)) index = data.size() - 1;
        Doc obj = data.get(index);
        return Doc.findDoc(obj.getId());
    }

	public Doc getRandomDoc() {
        init();
        Doc obj = data.get(rnd.nextInt(data.size()));
        return Doc.findDoc(obj.getId());
    }

	public boolean modifyDoc(Doc obj) {
        return false;
    }

	@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void init() {
        if (data != null) {
            return;
        }
        
        data = hu.mokk.hunglish.domain.Doc.findDocEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Doc' illegally returned null");
        if (data.size() > 0) {
            return;
        }
        
        data = new java.util.ArrayList<hu.mokk.hunglish.domain.Doc>();
        for (int i = 0; i < 10; i++) {
            hu.mokk.hunglish.domain.Doc obj = getNewTransientDoc(i);
            obj.persist();
            data.add(obj);
        }
    }
}
