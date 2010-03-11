package hu.mokk.hunglish.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.dod.RooDataOnDemand;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import hu.mokk.hunglish.domain.Bisen;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

@Configurable
@Component
@RooDataOnDemand(entity = Bisen.class)
public class BisenDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<Bisen> data;

	@Autowired
    private DocDataOnDemand docDataOnDemand;

	public Bisen getNewTransientBisen(int index) {
        hu.mokk.hunglish.domain.Bisen obj = new hu.mokk.hunglish.domain.Bisen();
        obj.setDoc(docDataOnDemand.getRandomDoc());
        obj.setDownvotes(new Integer(index).longValue());
        obj.setIsIndexed(new Boolean(true));
        obj.setLineNumber(new Integer(index));
        obj.setUpvotes(new Integer(index).longValue());
        return obj;
    }

	public Bisen getSpecificBisen(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size()-1)) index = data.size() - 1;
        Bisen obj = data.get(index);
        return Bisen.findBisen(obj.getId());
    }

	public Bisen getRandomBisen() {
        init();
        Bisen obj = data.get(rnd.nextInt(data.size()));
        return Bisen.findBisen(obj.getId());
    }

	public boolean modifyBisen(Bisen obj) {
        return false;
    }

	@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void init() {
        if (data != null) {
            return;
        }
        
        data = hu.mokk.hunglish.domain.Bisen.findBisenEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Bisen' illegally returned null");
        if (data.size() > 0) {
            return;
        }
        
        data = new java.util.ArrayList<hu.mokk.hunglish.domain.Bisen>();
        for (int i = 0; i < 10; i++) {
            hu.mokk.hunglish.domain.Bisen obj = getNewTransientBisen(i);
            obj.persist();
            data.add(obj);
        }
    }
}
