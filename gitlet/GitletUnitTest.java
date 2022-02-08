package gitlet;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

public class GitletUnitTest {
    @Test
    public void testInit() {
        Gitlet.setDefaultFolder("testing/mytest");
        Gitlet.init();
        File file = new File(Gitlet.getDefaultFolder());
        assertTrue(file.exists());
        file = new File(Gitlet.getDefaultFolder() + "/main");
        assertTrue(file.exists());
        file = new File(Gitlet.getDefaultFolder() + "/master");
        assertTrue(file.exists());
    }

    @Test
    public void testDate() {
        StringBuilder sb = new StringBuilder();
        Date date = new Date(System.currentTimeMillis());
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format("Date: %1$ta %1$tb %1$te"
                + " %1$tH:%1$tM:%1$tS %1$tY %1$tz", date);
        System.out.println(date.toString());
    }
}
