package hu.mokk.hunglish.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.test.RooIntegrationTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import hu.mokk.hunglish.domain.Upload;
import org.junit.Test;
import org.junit.runner.RunWith;

@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext.xml")
@RooIntegrationTest(entity = Upload.class)
public class UploadIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private UploadDataOnDemand dod;

	@Test
    public void testCountUploads() {
        org.junit.Assert.assertNotNull("Data on demand for 'Upload' failed to initialize correctly", dod.getRandomUpload());
        long count = hu.mokk.hunglish.domain.Upload.countUploads();
        org.junit.Assert.assertTrue("Counter for 'Upload' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindUpload() {
        org.junit.Assert.assertNotNull("Data on demand for 'Upload' failed to initialize correctly", dod.getRandomUpload());
        java.lang.Long id = dod.getRandomUpload().getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Upload' failed to provide an identifier", id);
        hu.mokk.hunglish.domain.Upload obj = hu.mokk.hunglish.domain.Upload.findUpload(id);
        org.junit.Assert.assertNotNull("Find method for 'Upload' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Upload' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllUploads() {
        org.junit.Assert.assertNotNull("Data on demand for 'Upload' failed to initialize correctly", dod.getRandomUpload());
        long count = hu.mokk.hunglish.domain.Upload.countUploads();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Upload', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<hu.mokk.hunglish.domain.Upload> result = hu.mokk.hunglish.domain.Upload.findAllUploads();
        org.junit.Assert.assertNotNull("Find all method for 'Upload' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Upload' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindUploadEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Upload' failed to initialize correctly", dod.getRandomUpload());
        long count = hu.mokk.hunglish.domain.Upload.countUploads();
        if (count > 20) count = 20;
        java.util.List<hu.mokk.hunglish.domain.Upload> result = hu.mokk.hunglish.domain.Upload.findUploadEntries(0, (int)count);
        org.junit.Assert.assertNotNull("Find entries method for 'Upload' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Upload' returned an incorrect number of entries", count, result.size());
    }

	@Test
    @Transactional
    public void testFlush() {
		/*
        org.junit.Assert.assertNotNull("Data on demand for 'Upload' failed to initialize correctly", dod.getRandomUpload());
        java.lang.Long id = dod.getRandomUpload().getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Upload' failed to provide an identifier", id);
        hu.mokk.hunglish.domain.Upload obj = hu.mokk.hunglish.domain.Upload.findUpload(id);
        org.junit.Assert.assertNotNull("Find method for 'Upload' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyUpload(obj);
        java.lang.Integer currentVersion = obj.getVersion();
        obj.flush();
        org.junit.Assert.assertTrue("Version for 'Upload' failed to increment on flush directive", obj.getVersion() > currentVersion || !modified);
        */
    }

	@Test
    @Transactional
    public void testMerge() {
		/*
        org.junit.Assert.assertNotNull("Data on demand for 'Upload' failed to initialize correctly", dod.getRandomUpload());
        java.lang.Long id = dod.getRandomUpload().getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Upload' failed to provide an identifier", id);
        hu.mokk.hunglish.domain.Upload obj = hu.mokk.hunglish.domain.Upload.findUpload(id);
        org.junit.Assert.assertNotNull("Find method for 'Upload' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyUpload(obj);
        java.lang.Integer currentVersion = obj.getVersion();
        obj.merge();
        obj.flush();
        org.junit.Assert.assertTrue("Version for 'Upload' failed to increment on merge and flush directive", obj.getVersion() > currentVersion || !modified);
        */
    }

	@Test
    @Transactional
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Upload' failed to initialize correctly", dod.getRandomUpload());
        hu.mokk.hunglish.domain.Upload obj = dod.getNewTransientUpload(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Upload' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Upload' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Upload' identifier to no longer be null", obj.getId());
    }

	@Test
    @Transactional
    public void testRemove() {
        org.junit.Assert.assertNotNull("Data on demand for 'Upload' failed to initialize correctly", dod.getRandomUpload());
        java.lang.Long id = dod.getRandomUpload().getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Upload' failed to provide an identifier", id);
        hu.mokk.hunglish.domain.Upload obj = hu.mokk.hunglish.domain.Upload.findUpload(id);
        org.junit.Assert.assertNotNull("Find method for 'Upload' illegally returned null for id '" + id + "'", obj);
        obj.remove();
        org.junit.Assert.assertNull("Failed to remove 'Upload' with identifier '" + id + "'", hu.mokk.hunglish.domain.Upload.findUpload(id));
    }
}
