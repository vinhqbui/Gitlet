package gitlet;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.util.LinkedHashSet;

/** Unit test for commit.
 * @author Vinh Bui
 * **/
public class CommitUnitTest {
    private final String _commitFolder = "testing/commits";
    private final String _blobFolder = "testing/blobs";

    @Test
    public void testInitCommit() {
        File file = new File(_commitFolder);
        file.mkdir();
        Commit initCommit = new Commit();
        Commit.setFolderPath(_commitFolder);
        initCommit.toFile();
        file = new File(_commitFolder + "/"
                + initCommit.getId() + ".commit");
        assertTrue(file.exists());
    }

    @Test
    public void testCustomCommit() {
        File file = new File(_commitFolder);
        file.mkdir();
        Commit initCommit = new Commit();
        Commit.setFolderPath(_commitFolder);
        initCommit.toFile();
        Commit commit = new Commit(initCommit,
                "Unit Test", "This is test commit");
        File a = new File("a.txt");
        Utils.writeContents(a, "Hello World");
        LinkedHashSet<String> stagedForAdd = new LinkedHashSet<>();
        stagedForAdd.add("a.txt");
        Blob.setDefaultFolder(_blobFolder);
        commit.doCommit(stagedForAdd, new LinkedHashSet<>());
        file = new File(_commitFolder + "/" + commit.getId() + ".commit");
        assertTrue(file.exists());
    }
}
