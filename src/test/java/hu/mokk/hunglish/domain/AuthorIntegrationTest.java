package hu.mokk.hunglish.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.test.RooIntegrationTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import hu.mokk.hunglish.domain.Author;
import org.junit.Test;
import org.junit.runner.RunWith;

@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext.xml")
@RooIntegrationTest(entity = Author.class)
public class AuthorIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private AuthorDataOnDemand dod;

	@Test
    public void testCountAuthors() {
        org.junit.Assert.assertNotNull("Data on demand for 'Author' failed to initialize correctly", dod.getRandomAuthor());
        long count = hu.mokk.hunglish.domain.Author.countAuthors();
        org.junit.Assert.assertTrue("Counter for 'Author' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindAuthor() {
        org.junit.Assert.assertNotNull("Data on demand for 'Author' failed to initialize correctly", dod.getRandomAuthor());
        java.lang.Long id = dod.getRandomAuthor().getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Author' failed to provide an identifier", id);
        hu.mokk.hunglish.domain.Author obj = hu.mokk.hunglish.domain.Author.findAuthor(id);
        org.junit.Assert.assertNotNull("Find method for 'Author' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Author' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllAuthors() {
        org.junit.Assert.assertNotNull("Data on demand for 'Author' failed to initialize correctly", dod.getRandomAuthor());
        long count = hu.mokk.hunglish.domain.Author.countAuthors();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Author', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<hu.mokk.hunglish.domain.Author> result = hu.mokk.hunglish.domain.Author.findAllAuthors();
        org.junit.Assert.assertNotNull("Find all method for 'Author' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Author' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindAuthorEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Author' failed to initialize correctly", dod.getRandomAuthor());
        long count = hu.mokk.hunglish.domain.Author.countAuthors();
        if (count > 20) count = 20;
        java.util.List<hu.mokk.hunglish.domain.Author> result = hu.mokk.hunglish.domain.Author.findAuthorEntries(0, (int)count);
        org.junit.Assert.assertNotNull("Find entries method for 'Author' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Author' returned an incorrect number of entries", count, result.size());
    }

	@Test
    @Transactional
    public void testFlush() {
        /*
		org.junit.Assert.assertNotNull("Data on demand for 'Author' failed to initialize correctly", dod.getRandomAuthor());
        java.lang.Long id = dod.getRandomAuthor().getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Author' failed to provide an identifier", id);
        hu.mokk.hunglish.domain.Author obj = hu.mokk.hunglish.domain.Author.findAuthor(id);
        org.junit.Assert.assertNotNull("Find method for 'Author' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyAuthor(obj);
        java.lang.Integer currentVersion = obj.getVersion();
        obj.flush();
        org.junit.Assert.assertTrue("Version for 'Author' failed to increment on flush directive", obj.getVersion() > currentVersion || !modified);
        */
    }

	@Test
    @Transactional
    public void testMerge() {
        org.junit.Assert.assertNotNull("Data on demand for 'Author' failed to initialize correctly", dod.getRandomAuthor());
        java.lang.Long id = new Long(2);//dod.getRandomAuthor().getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Author' failed to provide an identifier", id);
        hu.mokk.hunglish.domain.Author obj = hu.mokk.hunglish.domain.Author.findAuthor(id);
        org.junit.Assert.assertNotNull("Find method for 'Author' illegally returned null for id '" + id + "'", obj);
        //boolean modified =  dod.modifyAuthor(obj);
        obj.setDescription("description hhehe");
        java.lang.Integer currentVersion = obj.getVersion();
        obj.merge();
        obj.flush();
        //org.junit.Assert.assertTrue("Version for 'Author' failed to increment on merge and flush directive", obj.getVersion() > currentVersion || !modified);
    }

	@Test
    @Transactional
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Author' failed to initialize correctly", dod.getRandomAuthor());
        hu.mokk.hunglish.domain.Author obj = dod.getNewTransientAuthor(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Author' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Author' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Author' identifier to no longer be null", obj.getId());
    }

	@Test
    @Transactional
    public void testRemove() {
        org.junit.Assert.assertNotNull("Data on demand for 'Author' failed to initialize correctly", dod.getRandomAuthor());
        java.lang.Long id = dod.getRandomAuthor().getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Author' failed to provide an identifier", id);
        hu.mokk.hunglish.domain.Author obj = hu.mokk.hunglish.domain.Author.findAuthor(id);
        org.junit.Assert.assertNotNull("Find method for 'Author' illegally returned null for id '" + id + "'", obj);
        obj.remove();
        org.junit.Assert.assertNull("Failed to remove 'Author' with identifier '" + id + "'", hu.mokk.hunglish.domain.Author.findAuthor(id));
    }
}
