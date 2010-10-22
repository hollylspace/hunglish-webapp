package hu.mokk.hunglish.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.dod.RooDataOnDemand;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import hu.mokk.hunglish.domain.Upload;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

@Configurable
@Component
@RooDataOnDemand(entity = Upload.class)
public class UploadDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<Upload> data;

	@Autowired
    private AuthorDataOnDemand authorDataOnDemand;

	@Autowired
    private GenreDataOnDemand genreDataOnDemand;

	public Upload getNewTransientUpload(int index) {
        hu.mokk.hunglish.domain.Upload obj = new hu.mokk.hunglish.domain.Upload();
        obj.setAuthor(authorDataOnDemand.getRandomAuthor());
        obj.setEnTitle("enTitle_" + index);
        obj.setGenre(genreDataOnDemand.getRandomGenre());
        obj.setIsProcessed(new String("N"));
        return obj;
    }

	public Upload getSpecificUpload(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size()-1)) index = data.size() - 1;
        Upload obj = data.get(index);
        return Upload.findUpload(obj.getId());
    }

	public Upload getRandomUpload() {
        init();
        Upload obj = data.get(rnd.nextInt(data.size()));
        return Upload.findUpload(obj.getId());
    }

	public boolean modifyUpload(Upload obj) {
        return false;
    }

	@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void init() {
        if (data != null) {
            return;
        }
        
        data = hu.mokk.hunglish.domain.Upload.findUploadEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Upload' illegally returned null");
        if (data.size() > 0) {
            return;
        }
        
        data = new java.util.ArrayList<hu.mokk.hunglish.domain.Upload>();
        for (int i = 0; i < 10; i++) {
            hu.mokk.hunglish.domain.Upload obj = getNewTransientUpload(i);
            obj.persist();
            data.add(obj);
        }
    }
}
