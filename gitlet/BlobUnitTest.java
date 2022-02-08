package gitlet;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.File;

/** Test suits for Blob
 * @author Vinh Bui
 */
public class BlobUnitTest {
    private String _folder = "testing/blobs";
    private String _initialContent = "Hello World!";
    String _fileName = "Hello.txt";
    String _sha1Name = Utils.sha1(_fileName, _initialContent);

    @Test
    public void testSaveFile() {
        Blob.setDefaultFolder(_folder);
        File folder = new File(_folder);
        folder.mkdir();
        Blob blob = new Blob(_fileName, _initialContent);
        blob.toFile();
        File file = new File(_folder + "/" + blob.getId() + ".blob");
        assertTrue(file.exists());
    }

    @Test
    public void testLoadFile1() {
        File file = new File(_folder + "/" + _sha1Name);
        Blob blob = Blob.fromFile(file);
        assertEquals(_initialContent, blob.getContent());
    }

    @Test
    public void testAppendData() {
        File file = new File(_folder + "/" + _sha1Name);
        Blob blob = Blob.fromFile(file);
        blob.setContent("This is Vinh Bui");
        String id = blob.getId();
        blob.toFile();
        File newFile = new File(_folder + "/" + id);
        assertTrue(newFile.exists());
    }
}
