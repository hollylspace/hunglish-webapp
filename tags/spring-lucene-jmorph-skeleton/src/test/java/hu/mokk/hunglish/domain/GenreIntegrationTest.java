package hu.mokk.hunglish.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.test.RooIntegrationTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import hu.mokk.hunglish.domain.Genre;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext.xml")
@Configurable
@RooIntegrationTest(entity = Genre.class)
public class GenreIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private GenreDataOnDemand dod;

	@Test
    public void testCountGenres() {
        org.junit.Assert.assertNotNull("Data on demand for 'Genre' failed to initialize correctly", dod.getRandomGenre());
        long count = hu.mokk.hunglish.domain.Genre.countGenres();
        org.junit.Assert.assertTrue("Counter for 'Genre' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindGenre() {
        org.junit.Assert.assertNotNull("Data on demand for 'Genre' failed to initialize correctly", dod.getRandomGenre());
        java.lang.Long id = dod.getRandomGenre().getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Genre' failed to provide an identifier", id);
        hu.mokk.hunglish.domain.Genre obj = hu.mokk.hunglish.domain.Genre.findGenre(id);
        org.junit.Assert.assertNotNull("Find method for 'Genre' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Genre' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllGenres() {
        org.junit.Assert.assertNotNull("Data on demand for 'Genre' failed to initialize correctly", dod.getRandomGenre());
        long count = hu.mokk.hunglish.domain.Genre.countGenres();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Genre', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<hu.mokk.hunglish.domain.Genre> result = hu.mokk.hunglish.domain.Genre.findAllGenres();
        org.junit.Assert.assertNotNull("Find all method for 'Genre' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Genre' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindGenreEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Genre' failed to initialize correctly", dod.getRandomGenre());
        long count = hu.mokk.hunglish.domain.Genre.countGenres();
        if (count > 20) count = 20;
        java.util.List<hu.mokk.hunglish.domain.Genre> result = hu.mokk.hunglish.domain.Genre.findGenreEntries(0, (int)count);
        org.junit.Assert.assertNotNull("Find entries method for 'Genre' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Genre' returned an incorrect number of entries", count, result.size());
    }

	@Test
    @Transactional
    public void testFlush() {
		/*
        org.junit.Assert.assertNotNull("Data on demand for 'Genre' failed to initialize correctly", dod.getRandomGenre());
        java.lang.Long id = dod.getRandomGenre().getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Genre' failed to provide an identifier", id);
        hu.mokk.hunglish.domain.Genre obj = hu.mokk.hunglish.domain.Genre.findGenre(id);
        org.junit.Assert.assertNotNull("Find method for 'Genre' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyGenre(obj);
        java.lang.Integer currentVersion = obj.getVersion();
        obj.flush();
        org.junit.Assert.assertTrue("Version for 'Genre' failed to increment on flush directive", obj.getVersion() > currentVersion || !modified);
        */
    }

	@Test
    @Transactional
    public void testMerge() {
		/*
        org.junit.Assert.assertNotNull("Data on demand for 'Genre' failed to initialize correctly", dod.getRandomGenre());
        java.lang.Long id = dod.getRandomGenre().getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Genre' failed to provide an identifier", id);
        hu.mokk.hunglish.domain.Genre obj = hu.mokk.hunglish.domain.Genre.findGenre(id);
        org.junit.Assert.assertNotNull("Find method for 'Genre' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyGenre(obj);
        java.lang.Integer currentVersion = obj.getVersion();
        obj.merge();
        obj.flush();
        org.junit.Assert.assertTrue("Version for 'Genre' failed to increment on merge and flush directive", obj.getVersion() > currentVersion || !modified);
        */
    }

	@Test
    @Transactional
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Genre' failed to initialize correctly", dod.getRandomGenre());
        hu.mokk.hunglish.domain.Genre obj = dod.getNewTransientGenre(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Genre' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Genre' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Genre' identifier to no longer be null", obj.getId());
    }

	@Test
    @Transactional
    public void testRemove() {
        org.junit.Assert.assertNotNull("Data on demand for 'Genre' failed to initialize correctly", dod.getRandomGenre());
        java.lang.Long id = dod.getRandomGenre().getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Genre' failed to provide an identifier", id);
        hu.mokk.hunglish.domain.Genre obj = hu.mokk.hunglish.domain.Genre.findGenre(id);
        org.junit.Assert.assertNotNull("Find method for 'Genre' illegally returned null for id '" + id + "'", obj);
        obj.remove();
        org.junit.Assert.assertNull("Failed to remove 'Genre' with identifier '" + id + "'", hu.mokk.hunglish.domain.Genre.findGenre(id));
    }
}
