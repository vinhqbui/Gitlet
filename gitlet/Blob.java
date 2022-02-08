package gitlet;

import java.io.File;
import java.io.Serializable;

/** Class the represent the blob that store file metadata.
 * @author Vinh Bui
 */

public class Blob implements Serializable {
    /** Return the default folder for Gitlet. **/
    private static String _defaultFolder = Gitlet.getDefaultFolder()
            + "/blobs";
    /** Create Blob object by file class and content.
     * @param file file class to destination.
     * @param content the content of the file.
     */
    Blob(File file, String content) {
        _filename = file.getName();
        _file = file;
        _content = content;
        _id = Utils.sha1(_filename, _content);
    }

    /** Create Blob instance with file name and content.
     * @param filename name of the file.
     * @param content content of the file.
     * **/
    Blob(String filename, String content) {
        _filename = filename;
        _content = content;
        _id = Utils.sha1(_filename, _content);
        _file = new File(_defaultFolder + "/" + _id + ".blob");
    }

    /** Return id of Blob. **/
    public String getId() {
        return _id;
    }

    /** Return the file name of blob. **/
    public String getFilename() {
        return _filename;
    }

    /** Return the content of blob. **/
    public String getContent() {
        return _content;
    }

    /** Set content of the blob.
     * @param content the content of the file.
     * **/
    public void setContent(String content) {
        this._content = content;
        this._id = Utils.sha1(_filename, content);
    }

    /** Check if two blobs are the same.
     * @param o other blob
     * @return true if equal, false otherwise.
     * **/
    public boolean equals(Blob o) {
        if (this._id.equals(o._id)) {
            return true;
        }
        return false;
    }

    /** Serialize the object into file. **/
    public void toFile() {
        String fileName = _file.getParent() + "/" + _id + ".blob";
        File file = new File(fileName);
        if (!file.exists()) {
            Utils.writeObject(file, this);
        }
    }

    /** Static class function read data from file.
     * @param file file path to ata
     * @return Blob object
     */
    public static Blob fromFile(File file) {
        if (!file.exists()) {
            return null;
        }
        Blob blob = Utils.readObject(file, Blob.class);
        return blob;
    }

    /** Static class function read data from file.
     * @param id id of the file
     * @return Blob object
     */
    public static Blob fromFile(String id) {
        File file = new File(_defaultFolder + "/" + id + ".blob");
        if (!file.exists()) {
            return null;
        }
        Blob blob = Utils.readObject(file, Blob.class);
        return blob;
    }

    /** Mainly use this function for testing purpose.
     * @param path set new path.
     * **/
    public static void setDefaultFolder(String path) {
        _defaultFolder = path;
    }

    /** ID of Blobs. **/
    private String _id;

    /** The file name of the blob. **/
    private String _filename;

    /** The content of the file. **/
    private String _content;

    /** File object connect to file. **/
    private File _file;
}
