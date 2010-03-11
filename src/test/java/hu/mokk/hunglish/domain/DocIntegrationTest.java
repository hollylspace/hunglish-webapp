package hu.mokk.hunglish.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.test.RooIntegrationTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import hu.mokk.hunglish.domain.Doc;
import org.junit.Test;
import org.junit.runner.RunWith;

@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext.xml")
@RooIntegrationTest(entity = Doc.class)
public class DocIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private DocDataOnDemand dod;

	@Test
    public void testCountDocs() {
        org.junit.Assert.assertNotNull("Data on demand for 'Doc' failed to initialize correctly", dod.getRandomDoc());
        long count = hu.mokk.hunglish.domain.Doc.countDocs();
        org.junit.Assert.assertTrue("Counter for 'Doc' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindDoc() {
        org.junit.Assert.assertNotNull("Data on demand for 'Doc' failed to initialize correctly", dod.getRandomDoc());
        java.lang.Long id = dod.getRandomDoc().getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Doc' failed to provide an identifier", id);
        hu.mokk.hunglish.domain.Doc obj = hu.mokk.hunglish.domain.Doc.findDoc(id);
        org.junit.Assert.assertNotNull("Find method for 'Doc' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Doc' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllDocs() {
		/*
        org.junit.Assert.assertNotNull("Data on demand for 'Doc' failed to initialize correctly", dod.getRandomDoc());
        long count = hu.mokk.hunglish.domain.Doc.countDocs();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Doc', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<hu.mokk.hunglish.domain.Doc> result = hu.mokk.hunglish.domain.Doc.findAllDocs();
        org.junit.Assert.assertNotNull("Find all method for 'Doc' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Doc' failed to return any data", result.size() > 0);
        */
    }

	@Test
    public void testFindDocEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Doc' failed to initialize correctly", dod.getRandomDoc());
        long count = hu.mokk.hunglish.domain.Doc.countDocs();
        if (count > 20) count = 20;
        java.util.List<hu.mokk.hunglish.domain.Doc> result = hu.mokk.hunglish.domain.Doc.findDocEntries(0, (int)count);
        org.junit.Assert.assertNotNull("Find entries method for 'Doc' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Doc' returned an incorrect number of entries", count, result.size());
    }

	@Test
    @Transactional
    public void testFlush() {
		/*
        org.junit.Assert.assertNotNull("Data on demand for 'Doc' failed to initialize correctly", dod.getRandomDoc());
        java.lang.Long id = dod.getRandomDoc().getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Doc' failed to provide an identifier", id);
        hu.mokk.hunglish.domain.Doc obj = hu.mokk.hunglish.domain.Doc.findDoc(id);
        org.junit.Assert.assertNotNull("Find method for 'Doc' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyDoc(obj);
        java.lang.Integer currentVersion = obj.getVersion();
        obj.flush();
        org.junit.Assert.assertTrue("Version for 'Doc' failed to increment on flush directive", obj.getVersion() > currentVersion || !modified);
        */
    }

	@Test
    @Transactional
    public void testMerge() {
		/*
        org.junit.Assert.assertNotNull("Data on demand for 'Doc' failed to initialize correctly", dod.getRandomDoc());
        java.lang.Long id = dod.getRandomDoc().getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Doc' failed to provide an identifier", id);
        hu.mokk.hunglish.domain.Doc obj = hu.mokk.hunglish.domain.Doc.findDoc(id);
        org.junit.Assert.assertNotNull("Find method for 'Doc' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyDoc(obj);
        java.lang.Integer currentVersion = obj.getVersion();
        obj.merge();
        obj.flush();
        org.junit.Assert.assertTrue("Version for 'Doc' failed to increment on merge and flush directive", obj.getVersion() > currentVersion || !modified);
        */
    }

	@Test
    @Transactional
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Doc' failed to initialize correctly", dod.getRandomDoc());
        hu.mokk.hunglish.domain.Doc obj = dod.getNewTransientDoc(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Doc' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Doc' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Doc' identifier to no longer be null", obj.getId());
    }

	@Test
    @Transactional
    public void testRemove() {
        org.junit.Assert.assertNotNull("Data on demand for 'Doc' failed to initialize correctly", dod.getRandomDoc());
        java.lang.Long id = dod.getRandomDoc().getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Doc' failed to provide an identifier", id);
        hu.mokk.hunglish.domain.Doc obj = hu.mokk.hunglish.domain.Doc.findDoc(id);
        org.junit.Assert.assertNotNull("Find method for 'Doc' illegally returned null for id '" + id + "'", obj);
        obj.remove();
        org.junit.Assert.assertNull("Failed to remove 'Doc' with identifier '" + id + "'", hu.mokk.hunglish.domain.Doc.findDoc(id));
    }
}
