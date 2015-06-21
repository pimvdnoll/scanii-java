package com.scanii.client;

import com.scanii.client.misc.EICAR;
import com.scanii.client.misc.Systems;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ScaniiClientTest {
  private static final String KEY = System.getenv("TEST_KEY");
  private static final String SECRET = System.getenv("TEST_SECRET");
  private final Path eicarFile;

  public ScaniiClientTest() throws IOException {
    this.eicarFile = Files.write(Files.createTempFile(null, null), EICAR.SIGNATURE.getBytes());
  }

  @Test
  public void testProcess() throws Exception {
    ScaniiClient client = new ScaniiClient(ScaniiTarget.v2_0, KEY, SECRET);

    ScaniiResult result;
    // simple processing clean
    result = client.process(Systems.randomFile(1024));
    assertNotNull(result.getResourceId());
    assertNotNull(result.getChecksum());
    assertNotNull(result.getResourceLocation());
    assertNotNull(result.getRawResponse());
    assertNotNull(result.getRequestId());
    assertNotNull(result.getContentType());
    assertNotNull(result.getHostId());
    assertNotNull(result.getFindings());
    assertTrue(result.getFindings().isEmpty());
    System.out.println(result);

    // with findings
    result = client.process(eicarFile);
    assertNotNull(result.getResourceId());
    assertNotNull(result.getChecksum());
    assertNotNull(result.getResourceLocation());
    assertNotNull(result.getRawResponse());
    assertNotNull(result.getRequestId());
    assertNotNull(result.getContentType());
    assertNotNull(result.getHostId());
    assertNotNull(result.getFindings());
    assertTrue(result.getFindings().size() == 1);
    assertEquals("av.eicar-test-signature", result.getFindings().get(0));
    System.out.println(result);

    // failures:

  }

  @Test(expected = ScaniiException.class)
  public void shoudlThrowErrosIfInvalidPost() throws IOException {
    ScaniiClient client = new ScaniiClient(ScaniiTarget.v2_0, KEY, SECRET);

    // empty file:
    client.process(Files.createTempFile(null, null));

  }

  @Test(expected = ScaniiException.class)
  public void shoudlThrowErrosIfInvalidCredentials() throws IOException {
    ScaniiClient client = new ScaniiClient(ScaniiTarget.v2_0, "foo", "bar");

    // empty file:
    client.process(Files.createTempFile(null, null));

  }

  @Test
  public void testProcessAsync() throws Exception {
    ScaniiClient client = new ScaniiClient(ScaniiTarget.v2_0, KEY, SECRET);

    ScaniiResult result;
    // simple processing clean
    result = client.processAsync(Systems.randomFile(1024));
    assertNotNull(result.getResourceId());
    assertNull(result.getChecksum());
    assertNotNull(result.getResourceLocation());
    assertNotNull(result.getRawResponse());
    assertNotNull(result.getRequestId());
    assertNull(result.getContentType());
    assertNotNull(result.getHostId());
    assertNull(result.getFindings());
    System.out.println(result);

    Thread.sleep(5000);

    // now fetching the retrieve
    ScaniiResult actualResult = client.retrieve(result.getResourceId());
    assertNotNull(actualResult.getResourceId());
    assertNotNull(actualResult.getChecksum());
    assertNull(actualResult.getResourceLocation());
    assertNotNull(actualResult.getRawResponse());
    assertNotNull(actualResult.getRequestId());
    assertNotNull(actualResult.getContentType());
    assertNotNull(actualResult.getHostId());
    assertNotNull(actualResult.getFindings());
    assertTrue(actualResult.getFindings().isEmpty());

  }

  @Test
  public void testFetchWithoutCallback() throws InterruptedException {
    ScaniiClient client = new ScaniiClient(ScaniiTarget.v2_0, KEY, SECRET);

    ScaniiResult result;
    // simple processing clean
    result = client.fetch("https://scanii.s3.amazonaws.com/eicarcom2.zip");
    assertNotNull(result.getResourceId());
    assertNull(result.getChecksum());
    assertNotNull(result.getResourceLocation());
    assertNotNull(result.getRawResponse());
    assertNotNull(result.getRequestId());
    assertNull(result.getContentType());
    assertNotNull(result.getHostId());
    assertNull(result.getFindings());
    System.out.println(result);

    Thread.sleep(1000);

    // fetching retrieve:
    ScaniiResult actualResult = client.retrieve(result.getResourceId());
    assertNotNull(actualResult.getResourceId());
    assertNotNull(actualResult.getChecksum());
    assertNull(actualResult.getResourceLocation());
    assertNotNull(actualResult.getRawResponse());
    assertNotNull(actualResult.getRequestId());
    assertNotNull(actualResult.getContentType());
    assertNotNull(actualResult.getHostId());
    assertNotNull(actualResult.getFindings());
    assertEquals("av.eicar-test-signature", actualResult.getFindings().get(0));
  }

  @Test
  public void testFetchWithCallback() throws InterruptedException {
    ScaniiClient client = new ScaniiClient(ScaniiTarget.v2_0, KEY, SECRET);

    ScaniiResult result;
    // simple processing clean
    result = client.fetch("https://scanii.s3.amazonaws.com/eicarcom2.zip", "http://google.com");
    assertNotNull(result.getResourceId());
    assertNull(result.getChecksum());
    assertNotNull(result.getResourceLocation());
    assertNotNull(result.getRawResponse());
    assertNotNull(result.getRequestId());
    assertNull(result.getContentType());
    assertNotNull(result.getHostId());
    assertNull(result.getFindings());
    System.out.println(result);

    Thread.sleep(1000);

    // fetching retrieve:
    ScaniiResult actualResult = client.retrieve(result.getResourceId());
    assertNotNull(actualResult.getResourceId());
    assertNotNull(actualResult.getChecksum());
    assertNull(actualResult.getResourceLocation());
    assertNotNull(actualResult.getRawResponse());
    assertNotNull(actualResult.getRequestId());
    assertNotNull(actualResult.getContentType());
    assertNotNull(actualResult.getHostId());
    assertNotNull(actualResult.getFindings());
    assertEquals("av.eicar-test-signature", actualResult.getFindings().get(0));
  }

  @Test
  public void testPing() throws Exception {
    ScaniiClient client = new ScaniiClient(ScaniiTarget.v2_0, KEY, SECRET);
    assertTrue(client.ping());
  }

  @Test
  public void testCreateAuthToken() throws Exception {
    ScaniiClient client = new ScaniiClient(ScaniiTarget.v2_0_EU1, KEY, SECRET);
    ScaniiResult result = client.createAuthToken(1, TimeUnit.HOURS);
    assertNotNull(result.getResourceId());
    assertNotNull(result.getExpirationDate());
    assertNotNull(result.getCreationDate());

    Thread.sleep(1000);

    // now using the auth token to create a new client and process content
    ScaniiClient tempClient = new ScaniiClient(ScaniiTarget.v2_0_EU1, result.getResourceId(), null);
    result = tempClient.process(Systems.randomFile(1024));
    assertNotNull(result.getResourceId());
    assertNotNull(result.getChecksum());
    assertNotNull(result.getResourceLocation());
    assertNotNull(result.getRawResponse());
    assertNotNull(result.getRequestId());
    assertNotNull(result.getContentType());
    assertNotNull(result.getHostId());
    assertNotNull(result.getFindings());
    assertTrue(result.getFindings().isEmpty());
    System.out.println(result);


  }

  @Test
  public void testDeleteAuthToken() throws Exception {
    ScaniiClient client = new ScaniiClient(ScaniiTarget.v2_0_EU1, KEY, SECRET);
    ScaniiResult result = client.createAuthToken(1, TimeUnit.HOURS);
    client.deleteAuthToken(result.getResourceId());
  }

  @Test
  public void testRetrieveAuthToken() throws Exception {
    ScaniiClient client = new ScaniiClient(ScaniiTarget.v2_0_EU1, KEY, SECRET);
    ScaniiResult result = client.createAuthToken(1, TimeUnit.HOURS);
    ScaniiResult result2 = client.retrieveAuthToken(result.getResourceId());
    assertEquals(result.getResourceId(), result2.getResourceId());
    assertEquals(result.getCreationDate(), result2.getCreationDate());
    assertEquals(result.getExpirationDate(), result2.getExpirationDate());
  }
}