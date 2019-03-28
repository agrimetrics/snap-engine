package org.esa.snap.vfs.remote.http;

import org.esa.snap.vfs.NioPaths;
import org.esa.snap.vfs.VFS;
import org.esa.snap.vfs.preferences.model.VFSRemoteFileRepository;
import org.esa.snap.vfs.remote.AbstractRemoteFileSystem;
import org.esa.snap.vfs.remote.AbstractVFSTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.EOFException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

/**
 * Test: File System for HTTP Object Storage VFS.
 *
 * @author Adrian Drăghici
 */
public class HttpFileSystemTest extends AbstractVFSTest {

    private static Logger logger = Logger.getLogger(HttpFileSystemTest.class.getName());

    private static AbstractRemoteFileSystem httpFileSystem;
    private static VFSRemoteFileRepository httpRepo;
    private static HttpMockService mockService;

    private static String getAddress() {
        return httpRepo.getAddress();
    }

    private static String getUser() {
        return httpRepo.getProperties().get(0).getValue();
    }

    private static String getPassword() {
        return httpRepo.getProperties().get(1).getValue();
    }

    @Before
    public void setUpHttpFileSystemTest() {
        try {
            httpRepo = vfsRepositories.get(0);
            assumeNotNull(httpRepo);
            FileSystemProvider fileSystemProvider = VFS.getInstance().getFileSystemProviderByScheme(httpRepo.getScheme());
            assumeNotNull(fileSystemProvider);
            URI uri = new URI(httpRepo.getScheme() + ":" + httpRepo.getAddress());
            FileSystem fs = fileSystemProvider.newFileSystem(uri, null);
            assumeNotNull(fs);
            assumeTrue(fs instanceof AbstractRemoteFileSystem);
            httpFileSystem = (AbstractRemoteFileSystem) fs;
            Path serviceRootPath = vfsTestsFolderPath.resolve("http/mock-api");
            assumeTrue(Files.exists(serviceRootPath));
            mockService = new HttpMockService(new URL(httpRepo.getAddress()), serviceRootPath);
            assumeNotNull(mockService);
            mockService.start();
        } catch (Exception e) {
            fail("Test requirements not meets.");
        }
    }

    @After
    public void tearDown() throws Exception {
        if (httpFileSystem != null) {
            httpFileSystem.close();
        }
        if (mockService != null) {
            mockService.stop();
        }
    }

    @Test
    public void testScanner() throws Exception {
        List<BasicFileAttributes> items;

        items = new HttpWalker(getAddress(), getUser(), getPassword(), "/", "").walk(NioPaths.get(""));
        assertEquals(2, items.size());

        items = new HttpWalker(getAddress(), getUser(), getPassword(), "/", "").walk(NioPaths.get("rootDir1/"));
        assertEquals(10, items.size());

        items = new HttpWalker(getAddress(), getUser(), getPassword(), "/", "").walk(NioPaths.get("rootDir1/dir1/"));
        assertEquals(6, items.size());
    }

    @Test
    public void testGET() throws Exception {
        URL url = new URL(getAddress() + "/rootDir1/dir1/file.png");
        URLConnection connection = url.openConnection();
        ((HttpURLConnection) connection).setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.connect();

        int responseCode = ((HttpURLConnection) connection).getResponseCode();
        System.out.println("responseCode = " + responseCode);
        String responseMessage = ((HttpURLConnection) connection).getResponseMessage();
        System.out.println("responseMessage = " + responseMessage);

        InputStream stream = connection.getInputStream();
        byte[] b = new byte[1024 * 1024];
        int read = stream.read(b);
        assertTrue(read > 0);
        ReadableByteChannel channel = Channels.newChannel(stream);
        channel.close();
        ((HttpURLConnection) connection).disconnect();
    }

    @Test
    public void testSeparator() {
        assertEquals("/", httpFileSystem.getSeparator());
    }

    @Test
    public void testGetRootDirectories() {
        Iterable<Path> rootDirectories = httpFileSystem.getRootDirectories();
        Iterator<Path> iterator = rootDirectories.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("/rootDir2/", iterator.next().toString());
        assertTrue(iterator.hasNext());
        assertEquals("/rootDir1/", iterator.next().toString());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testClose() throws Exception {
        FileSystemProvider provider = httpFileSystem.provider();
        HashSet<OpenOption> openOptions = new HashSet<>();
        openOptions.add(StandardOpenOption.READ);
        SeekableByteChannel channel1 = provider.newByteChannel(httpFileSystem.getPath("/rootDir1/dir1/file.png"), openOptions);
        SeekableByteChannel channel2 = provider.newByteChannel(httpFileSystem.getPath("/rootDir1/dir1/file.png"), openOptions);
        SeekableByteChannel channel3 = provider.newByteChannel(httpFileSystem.getPath("/rootDir1/dir1/file.png"), openOptions);
        assertTrue(httpFileSystem.isOpen());
        assertTrue(channel1.isOpen());
        assertTrue(channel2.isOpen());
        assertTrue(channel3.isOpen());
        httpFileSystem.close();
        assertFalse(httpFileSystem.isOpen());
        assertFalse(channel1.isOpen());
        assertFalse(channel2.isOpen());
        assertFalse(channel3.isOpen());
    }

    @Test
    public void testByteChannel() throws Exception {
        FileSystemProvider provider = httpFileSystem.provider();
        Path path = httpFileSystem.getPath("/rootDir1/dir1/file.png");
        HashSet<OpenOption> openOptions = new HashSet<>();
        openOptions.add(StandardOpenOption.READ);
        SeekableByteChannel channel = provider.newByteChannel(path, openOptions);

        assertNotNull(channel);
        assertEquals(42377, channel.size());
        assertEquals(0, channel.position());

        ByteBuffer buffer = ByteBuffer.wrap(new byte[42377]);
        int numRead = channel.read(buffer);
        assertEquals(42377, numRead);
        assertEquals(42377, channel.size());
        assertEquals(42377, channel.position());

        channel.position(30000);
        assertEquals(30000, channel.position());
        assertEquals(42377, channel.size());

        buffer = ByteBuffer.wrap(new byte[10000]);
        numRead = channel.read(buffer);
        assertEquals(10000, numRead);
        assertEquals(40000, channel.position());
        assertEquals(42377, channel.size());

        buffer = ByteBuffer.wrap(new byte[2377]);
        numRead = channel.read(buffer);
        assertEquals(2377, numRead);
        assertEquals(42377, channel.position());
        assertEquals(42377, channel.size());

        buffer = ByteBuffer.wrap(new byte[10]);
        try {
            numRead = channel.read(buffer);
            fail("EOFException expected, but read " + numRead + " bytes");
        } catch (EOFException ex) {
            logger.log(Level.SEVERE, "Unable to run test for Byte Channel. Details: " + ex.getMessage());
        }
    }

    @Test
    public void testBasicFileAttributes() throws Exception {
        Path path = httpFileSystem.getPath("/rootDir1/dir1/file.png");
        assertEquals(42377, Files.size(path));
        FileTime lastModifiedTime = Files.getLastModifiedTime(path);
        assertNotNull(lastModifiedTime);
    }

    @Test
    public void testPathsGet() {
        Path path = httpFileSystem.getPath("/rootDir1/dir1/file.png");
        assertNotNull(path);
        assertEquals("/rootDir1/dir1/file.png", path.toString());
    }

    @Test
    public void testFilesWalk() throws Exception {
        Path path = httpFileSystem.getPath("/rootDir1/");
        Iterator<Path> iterator = Files.walk(path).iterator();
        while (iterator.hasNext()) {
            Path next = iterator.next();
            System.out.println("next = " + next + ", abs=" + path.isAbsolute());
        }
    }

}