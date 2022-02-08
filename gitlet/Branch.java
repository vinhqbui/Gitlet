package gitlet;

import java.io.File;
import java.io.Serializable;

/** This class represent the branches of the gitlet.
 * @author Vinh Bui
 * **/
class Branch implements Serializable {
    /** The latest commit of the branch. **/
    private Commit _commit;
    /** The name of the branch. **/
    private String _name;

    /** Can only construct with branch name and commit id.
     * @param name name of the branch.
     * @param commit commit instance.
     * **/
    Branch(String name, Commit commit) {
        _name = name;
        _commit = commit;
    }

    /** Return the commit. **/
    public Commit getCommit() {
        return _commit;
    }

    /** Change to a new commit.
     * @param commit the new commit.
     * **/
    public void setCommit(Commit commit) {
        this._commit = commit;
    }

    /** Return the branch name. **/
    public String getName() {
        return _name;
    }

    /** Used when want to change the branch name.
     * @param name the new name of the branch
     * **/
    public void setName(String name) {
        this._name = name;
    }

    /** Save branch into name. **/
    public void toFile() {
        File file = new File(_defaultFolder + _name);
        _commit.toFile();
        Utils.writeObject(file, this);
    }

    /** Load the branch from file.
     * @param name name of branch.
     * @return Branch instance.
     * **/
    public static Branch fromFile(String name) {
        File file = new File(_defaultFolder + name);
        return Utils.readObject(file, Branch.class);
    }

    /** Remove a file stored Branch info.
     * @param name name of the branch.
     * **/
    public static void removeFile(String name) {
        File file = new File(_defaultFolder + name);
        file.delete();
    }

    /** Default location for branches. **/
    private static String _defaultFolder = Gitlet.getDefaultFolder()
            + "/branches/";
}
