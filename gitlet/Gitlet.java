package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Formatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Main class to manage the gitlet.
 * There is only one instance of gitlet in the program.
 * @author Vinh Bui
 * **/
public class Gitlet implements Serializable {
    /** Get the default location for gitlet.
     * @return default location.
     * **/
    public static String getDefaultFolder() {
        return _defaultFolder;
    }

    /** Initialize the gitlet managed folder.
     * Print out error if it is already managed. **/
    public static void init() {
        File file = new File(getDefaultFolder());
        if (file.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            return;
        }
        file.mkdir();
        file = new File(getDefaultFolder() + "/" + "blobs");
        file.mkdir();
        file = new File(getDefaultFolder() + "/" + "commits");
        file.mkdir();
        file = new File(getDefaultFolder() + "/" + "branches");
        file.mkdir();
        Commit commit = new Commit();
        Branch master = new Branch("master", commit);
        _main = new Gitlet();
        _main._branches.put(master.getName(), master);
        _main._HEAD = master;
        master.toFile();
        _main.toFile();
    }

    /** Load the instance from file.
     * @return the main instance.
     * **/
    public static Gitlet getInstance() {
        return _main;
    }

    /** Change default folder for gitlet.
     * @param defaultFolder set a new location.
     * **/
    public static void setDefaultFolder(String defaultFolder) {
        Gitlet._defaultFolder = defaultFolder;
    }

    /** Add the file into staged zone. Return error if file does not existed.
     * If the file is in staged for remove zone, take it out.
     * @param fileName name of the file to add.
     * **/
    public void add(String fileName) {
        String parentFolder = getWorkingDir();
        File file = new File(parentFolder + "/" + fileName);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        HashMap<String, String> blobs = _main._HEAD.getCommit().getBlobs();
        if (_main._stagedForRemove.contains(fileName)) {
            _main._stagedForRemove.remove(fileName);
        }
        if (!blobs.containsKey(fileName)) {
            _main._stagedForAdd.add(fileName);
        } else {
            String content = Utils.readContentsAsString(file);
            String sha = Utils.sha1(fileName, content);
            if (!sha.equals(blobs.get(fileName))) {
                _main._stagedForAdd.add(fileName);
            }
        }
        _main.toFile();
    }

    /** Add the file into staged for remove zone.
     * If the file is in staged for add, remove it.
     * @param filename name of the file to remove.
     * **/
    public void remove(String filename) {
        HashMap<String, String> blobs = _main._HEAD.getCommit().getBlobs();
        if (blobs.containsKey(filename)) {
            _main._stagedForRemove.add(filename);
            _main._stagedForAdd.remove(filename);
            Utils.restrictedDelete(new File(filename));
        } else if (_main._stagedForAdd.contains(filename)) {
            _main._stagedForAdd.remove(filename);
        } else {
            System.out.println("No reason to remove the file.");
        }
        _main.toFile();
    }

    /** Return the commit history from latest commit to initial commits. **/
    public String log() {
        Commit current = _main._HEAD.getCommit();
        String log = "";
        while (current != null) {
            log = getLog(current, log, _form);
            current = current.getParent();
        }
        System.out.print(log);
        return log;
    }

    /** Helper function for log.
     * @param current the commit to get info.
     * @param log the log to append to.
     * @param form form for date formatting.
     * @return new log after adding content.
     * **/
    private String getLog(Commit current, String log, String form) {
        Formatter formatter = new Formatter();
        log += "===";
        log += "\ncommit " + current.getId() + "\n";
        if (current.getMergedParent() != null) {
            String p1 = current.getParent().getId().substring(0, 7);
            String p2 = current.getMergedParent().getId().substring(0, 7);
            log += "Merge: " + p1 + " " + p2 + "\n";
        }
        log += formatter.format(form, current.getDate());
        log += "\n" + current.getMessage() + "\n\n";
        return log;
    }

    /** Perform checkout function with latest backup of file.
     * @param fileName the name of the file
     * **/
    public void checkout(String fileName) {
        if (_main._stagedForAdd.contains(fileName)) {
            _main._stagedForAdd.remove(fileName);
        }
        if (_main._stagedForRemove.contains(fileName)) {
            _main._stagedForRemove.remove(fileName);
        }
        Commit latest = _main._HEAD.getCommit();
        HashMap<String, String> blobs = latest.getBlobs();
        if (!blobs.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        } else {
            String path = Gitlet.getDefaultFolder() + "/blobs/";
            String id = blobs.get(fileName) + ".blob";
            path += id;
            File backupFile = new File(path);
            Blob blob = Blob.fromFile(backupFile);
            String filePath = getWorkingDir() + "/" + fileName;
            File toFile = new File(filePath);
            Utils.writeContents(toFile, blob.getContent());
        }
        _main.toFile();
    }

    /** Perform restore backup at a specific commit.
     * @param id the id of the commit.
     * @param fileName the name of the file.
     * **/
    public void checkoutWithCommitID(String id, String fileName) {
        if (_main._stagedForAdd.contains(fileName)) {
            _main._stagedForAdd.remove(fileName);
        }
        if (_main._stagedForRemove.contains(fileName)) {
            _main._stagedForRemove.remove(fileName);
        }
        Commit current = _main._HEAD.getCommit();
        while (current != null) {
            boolean isEqual = true;
            String currentId = current.getId();
            int length = Math.min(currentId.length(), id.length());
            for (int i = 0; i < length; i++) {
                if (id.charAt(i) != current.getId().charAt(i)) {
                    isEqual = false;
                    break;
                }
            }
            if (isEqual) {
                Commit commit = Commit.fromFile(currentId);
                HashMap<String, String> blobs = commit.getBlobs();
                if (!blobs.containsKey(fileName)) {
                    System.out.println("File does not exist in that commit.");
                    return;
                }
                Blob blob = Blob.fromFile(blobs.get(fileName));
                String filePath = getWorkingDir() + "/" + fileName;
                File toFile = new File(filePath);
                Utils.writeContents(toFile, blob.getContent());
                return;
            }
            current = current.getParent();
        }
        System.out.println("No commit with that id exists.");
    }

    /** Switch to another branch.
     * @param branchName the branch name.
     * **/
    public void checkoutBranch(String branchName) {
        if (_main._HEAD.getName().equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
        } else if (!_main._branches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
        } else {
            Commit cCommit = _main._HEAD.getCommit();
            HashMap<String, String> currentBlobs = cCommit.getBlobs();
            Branch branch = Branch.fromFile(branchName);
            HashMap<String, String> switchBlobs = branch.getCommit().getBlobs();
            List<String> fileNames = Utils.plainFilenamesIn(getWorkingDir());
            if (checkForUntracked(currentBlobs, switchBlobs, fileNames)) {
                return;
            }
            for (String fileName : currentBlobs.keySet()) {
                if (!switchBlobs.containsKey(fileName)) {
                    Utils.restrictedDelete(fileName);
                }
            }
            for (String fileName : switchBlobs.keySet()) {
                Utils.restrictedDelete(fileName);
                Blob blob = Blob.fromFile(switchBlobs.get(fileName));
                File file = new File(getWorkingDir() + "/" + fileName);
                Utils.writeContents(file, blob.getContent());
            }
            _main._HEAD = branch;
        }
        _main.toFile();
    }

    /**
     * Check for legal commit.
     * @param currentBlobs the current tracked files.
     * @param switchBlobs the incoming tracked files.
     * @param fileNames list of file names in working the directory.
     * @return true if not legal, false otherwise.
     */
    private boolean checkForUntracked(HashMap<String, String> currentBlobs,
                                      HashMap<String, String> switchBlobs,
                                      List<String> fileNames) {
        for (String name : fileNames) {
            File file = new File(getWorkingDir() + "/" + name);
            String content = Utils.readContentsAsString(file);
            String hashed = new Blob(name, content).getId();
            String trackedHash = currentBlobs.get(name);
            if (hashed.equals(trackedHash)) {
                continue;
            }
            if (switchBlobs.containsKey(name)) {
                if (switchBlobs.get(name).equals(hashed)) {
                    continue;
                }
                System.out.println("There is an untracked "
                        + "file in the way; delete it, "
                        + "or add and commit it first.");
                return true;
            }
        }
        return false;
    }

    /** Perform commit, clear out the stage for add, stage for remove zone.
     * Also, update the latest hash commit in HEAD.
     * @param author the author name
     * @param message the message of commit
     * **/
    public void commit(String author, String message) {
        if (message.isBlank()) {
            System.out.println("Please enter a commit message.");
            return;
        }
        if (_main._stagedForAdd.isEmpty() && _main._stagedForRemove.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        Commit commit = new Commit(_HEAD.getCommit(), author, message);
        _main._HEAD.setCommit(commit.doCommit(_main._stagedForAdd,
                _main._stagedForRemove));
        _main._HEAD.toFile();
        _main._branches.put(_main._HEAD.getName(), _main._HEAD);
        _main._stagedForAdd.clear();
        _main._stagedForRemove.clear();
        _main.toFile();
    }

    /** Create a branch from the latest commit.
     * @param branchName branch name
     * **/
    public void createBranch(String branchName) {
        for (String branch : _main._branches.keySet()) {
            if (branchName.equals(branch)) {
                System.out.println("A branch with that name already exists.");
                return;
            }
        }
        Branch branch = new Branch(branchName, _main._HEAD.getCommit());
        _main._branches.put(branchName, branch);
        branch.toFile();
        _main.toFile();
    }

    /** Remove the branch.
     * @param branchName name of branch.
     * **/
    public void removeBranch(String branchName) {
        if (!_main._branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
        } else if (_main._HEAD.getName().equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
        } else {
            _main._branches.remove(branchName);
            Branch.removeFile(branchName);
            _main.toFile();
        }
    }
    /** Reset the current files back to the commit.
     * @param commitHash commit hash to return to.
     * **/
    public void reset(String commitHash) {
        ArrayList<Commit> commits = listAllCommit();
        boolean found = false;
        for (Commit commit : commits) {
            if (commit.getId().equals(commitHash)) {
                found = true;
                Commit current = _main._HEAD.getCommit();
                HashMap<String, String> currentBlobs = current.getBlobs();
                HashMap<String, String> switchToBlobs = commit.getBlobs();
                List<String> names = Utils.plainFilenamesIn(getWorkingDir());
                if (checkForUntracked(currentBlobs, switchToBlobs, names)) {
                    return;
                }
                for (String name : names) {
                    if (switchToBlobs.containsKey(name)) {
                        Blob blob = Blob.fromFile(switchToBlobs.get(name));
                        File file = new File(getWorkingDir()
                                + "/" + blob.getFilename());
                        Utils.writeContents(file, blob.getContent());
                    } else if (currentBlobs.containsKey(name)) {
                        Utils.restrictedDelete(name);
                    }
                }
                for (String fileName : switchToBlobs.keySet()) {
                    if (!currentBlobs.containsKey(fileName)) {
                        Blob blob = Blob.fromFile(switchToBlobs.get(fileName));
                        File file = new File(getWorkingDir()
                                + "/" + blob.getFilename());
                        Utils.writeContents(file, blob.getContent());
                    }
                }
                _main._stagedForAdd.clear();
                _main._stagedForRemove.clear();
                _main._HEAD.setCommit(commit);
                _main._HEAD.toFile();
                _main.toFile();
                break;
            }
        }
        if (!found) {
            System.out.println("No commit with that id exists.");
        }
    }

    /** Get the current status of the gitlet managed directory.
     * **/
    public void getStatus() {
        System.out.println("=== Branches ===");
        System.out.println("*" + _main._HEAD.getName());
        for (String branchName : _main._branches.keySet()) {
            if (!branchName.equals(_main._HEAD.getName())) {
                System.out.println(branchName);
            }
        }

        System.out.println("\n=== Staged Files ===");
        for (String fileName : _main._stagedForAdd) {
            System.out.println(fileName);
        }

        System.out.println("\n=== Removed Files ===");
        for (String fileName : _main._stagedForRemove) {
            System.out.println(fileName);
        }

        System.out.println("\n=== Modifications Not Staged For Commit ===");
        ArrayList<String> deletedFiles = new ArrayList<>();
        ArrayList<String> modifiedFiles = new ArrayList<>();
        ArrayList<String> untrackedFiles = new ArrayList<>();
        buildModifiedUntracked(deletedFiles, modifiedFiles, untrackedFiles);

        for (String fileName : deletedFiles) {
            System.out.println(fileName + " (deleted)");
        }
        for (String fileName : modifiedFiles) {
            System.out.println(fileName + " (modified)");
        }

        System.out.println("\n=== Untracked Files ===");
        for (String fileName : untrackedFiles) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    /** Use to build information for modified and untracked files.
     * @param deletedFiles stored the deleted files without gitlet consent.
     * @param modifiedFiles stored modified files but not added.
     * @param untrackedFiles stored file that not added to gitlet.
     * **/
    private void buildModifiedUntracked(ArrayList<String> deletedFiles,
                                        ArrayList<String> modifiedFiles,
                                        ArrayList<String> untrackedFiles) {
        HashMap<String, String> blobs = _main._HEAD.getCommit().getBlobs();
        File currentDir = new File(getWorkingDir());
        File [] files = currentDir.listFiles();
        List<String> names = Utils.plainFilenamesIn(getWorkingDir());
        for (String name : names) {
            if (blobs.containsKey(name)) {
                File f = new File(getWorkingDir() + "/" + name);
                String content = Utils.readContentsAsString(f);
                String id = blobs.get(name);
                Blob blob = Blob.fromFile(id);
                if (!blob.getContent().equals(content)
                        && !_main._stagedForAdd.contains(name)
                        && !_main._stagedForRemove.contains(name)) {
                    modifiedFiles.add(name);
                }
            } else if (!_main._stagedForAdd.contains(name)
                    && !_main._stagedForRemove.contains(name)) {
                untrackedFiles.add(name);
            }
        }
        for (String fileName : blobs.keySet()) {
            boolean found = false;
            for (File f : files) {
                if (f.getName().equals(fileName)) {
                    found = true;
                }
            }
            if (!found && !_main._stagedForRemove.contains(fileName)) {
                deletedFiles.add(fileName);
            }
        }
    }

    /** List all info of all commmits ever made. **/
    public void globalLog() {
        ArrayList<Commit> commits = listAllCommit();
        String log = "";
        for (Commit commit : commits) {
            log = getLog(commit, log, _form);
        }
        System.out.println(log);
    }

    /** Find a commit with specific message.
     * @param message message to search for.
     * **/
    public void find(String message) {
        ArrayList<Commit> commits = listAllCommit();
        ArrayList<Commit> filtered = new ArrayList<>();
        for (Commit commit : commits) {
            if (commit.getMessage().equals(message)) {
                filtered.add(commit);
            }
        }
        if (filtered.size() == 0) {
            System.out.println("Found no commit with that message.");
        } else {
            for (Commit commit : filtered) {
                System.out.println(commit.getId());
            }
        }
    }

    /** Merge the given branch into the current branch.
     * @param branchName the name of the branch.
     */
    public void merge(String branchName) {
        if (!(_main._stagedForRemove.isEmpty()
                && _main._stagedForAdd.isEmpty())) {
            System.out.println("You have uncommitted changes.");
        } else if (!_main._branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
        } else if (branchName.equals(_main._HEAD.getName())) {
            System.out.println("Cannot merge a branch with itself.");
        } else {
            Branch givenBranch = Branch.fromFile(branchName);
            Commit commitB1 = _main._HEAD.getCommit();
            Commit commitB2 = givenBranch.getCommit();
            Commit commonAncestor = findCommonAncestor(commitB1,
                    commitB2);
            if (commonAncestor.equals(givenBranch.getCommit())) {
                System.out.println("Given branch "
                        + "is an ancestor of the current branch.");
            } else if (_main._HEAD.getCommit().equals(commonAncestor)) {
                checkoutBranch(branchName);
                System.out.println("Current branch fast-forwarded.");
            } else {
                if (handleMerge(givenBranch, commonAncestor)) {
                    return;
                }
                String message = "Merged " + branchName
                        + " into " + _main._HEAD.getName() + ".";
                Commit p = _main._HEAD.getCommit();
                Commit commit = new Commit(p, "Vinh Bui", message);
                commit.doCommit(_main._stagedForAdd, _main._stagedForRemove);
                _main._stagedForAdd.clear();
                _main._stagedForRemove.clear();
                commit.setMergedParent(givenBranch.getCommit());
                _main._HEAD.setCommit(commit);
                _main._HEAD.toFile();
                _main.toFile();
            }
        }
    }

    /**
     * Handle the merge function.
     * @param givenBranch the current branch
     * @param commonAncestor the merging function.
     * @return true if merge successful, false otherwise.
     */
    private boolean handleMerge(Branch givenBranch, Commit commonAncestor) {
        Commit givenCommit = givenBranch.getCommit();
        Commit headCommit = _main._HEAD.getCommit();
        HashMap<String, String> cBlobs = headCommit.getBlobs();
        HashMap<String, String> iBlobs = givenCommit.getBlobs();
        List<String> names = Utils.plainFilenamesIn(getWorkingDir());
        String workingDir = getWorkingDir() + "/";
        if (checkForUntracked(cBlobs, iBlobs, names)) {
            return true;
        }
        HashMap<String, String> flashPoint = commonAncestor.getBlobs();
        flashPointFiles(cBlobs, iBlobs, flashPoint);

        if (headCommit.getMergedParent() != null) {
            Commit secondParent = headCommit.getMergedParent();
            Commit commonP2 = findCommonAncestor(secondParent, givenCommit);
            HashMap<String, String> flashSecond = commonP2.getBlobs();
            flashPointFiles(secondParent.getBlobs(), iBlobs, flashSecond);
        }
        if (givenCommit.getMergedParent() != null) {
            Commit givenMerge = givenCommit.getMergedParent();
            Commit commonP2 = findCommonAncestor(headCommit, givenMerge);
            HashMap<String, String> flashSecond = commonP2.getBlobs();
            flashPointFiles(cBlobs, givenMerge.getBlobs(), flashSecond);
        }
        for (String fileName : iBlobs.keySet()) {
            if (!flashPoint.containsKey(fileName)) {
                if (!cBlobs.containsKey(fileName)) {
                    String id = iBlobs.get(fileName);
                    mergeAdd(workingDir, fileName, id);
                } else {
                    String cId = cBlobs.get(fileName);
                    String iId = iBlobs.get(fileName);
                    if (!cId.equals(iId)) {
                        handleConflict(workingDir, fileName, cId, iId);
                    }
                }

            }
        }
        return false;
    }

    /**
     * Handle the merge conflict.
     * @param workingDir current dir
     * @param fileName name of the file
     * @param cId blob id of current branch
     * @param iId blob id of merging branch
     */
    private void handleConflict(String workingDir,
                                String fileName, String cId, String iId) {
        File file = new File(workingDir + fileName);
        String currentContent = "";
        String incomingContent = "";
        if (!cId.equals("")) {
            currentContent = Blob.fromFile(cId).getContent();
        }
        if (!iId.equals("")) {
            incomingContent = Blob.fromFile(iId).getContent();
        }
        String content = "<<<<<<< HEAD\n"
                + currentContent + "=======\n"
                + incomingContent + ">>>>>>>\n";
        Utils.writeContents(file, content);
        _main._stagedForAdd.add(fileName);
        System.out.println("Encountered a merge conflict.");
    }

    /** Deal with files that are modified in given branch since split point
     * but not current branch.
     * @param currentBlobs blobs of current branch.
     * @param incomeBlobs blobs of merging branch.
     * @param flashPoint point of split.
     */
    private void flashPointFiles(HashMap<String, String> currentBlobs,
                                 HashMap<String, String> incomeBlobs,
                                 HashMap<String, String> flashPoint) {
        String workingDir = getWorkingDir() + "/";
        for (String fileName : flashPoint.keySet()) {
            String flashPointId = flashPoint.get(fileName);
            if (currentBlobs.containsKey(fileName)
                    && incomeBlobs.containsKey(fileName)) {
                String iId = incomeBlobs.get(fileName);
                String cId = currentBlobs.get(fileName);
                if (iId.equals(cId)) {
                    continue;
                } else if (iId.equals(flashPointId)) {
                    mergeAdd(workingDir, fileName, cId);
                } else if (cId.equals(flashPointId)) {
                    mergeAdd(workingDir, fileName, iId);
                } else {
                    handleConflict(workingDir, fileName, cId, iId);
                }
            } else if (currentBlobs.containsKey(fileName)) {
                String cId = currentBlobs.get(fileName);
                if (cId.equals(flashPointId)) {
                    Utils.restrictedDelete(fileName);
                    _main._stagedForRemove.add(fileName);
                } else {
                    handleConflict(workingDir, fileName, cId, "");
                }
            } else if (incomeBlobs.containsKey(fileName)) {
                String iId = incomeBlobs.get(fileName);
                if (!iId.equals(flashPointId)) {
                    handleConflict(workingDir, fileName, "", iId);
                }
            }
        }
    }
    /** Helper for auto write file and add to stage area.
     * @param workingDir current working dir
     * @param fileName name of the file
     * @param chosenOne the blob id
     * **/
    private void mergeAdd(String workingDir,
                          String fileName, String chosenOne) {
        Blob blob = Blob.fromFile(chosenOne);
        File file = new File(workingDir + fileName);
        Utils.writeContents(file, blob.getContent());
        _main._stagedForAdd.add(fileName);
    }

    /**
     * Find the common ancestor from both branches.
     * @param commitB1 commit number 1
     * @param commitB2 commit number 2
     * @return at worst case, the function will return the initial commit.
     * So, null never reaches.
     */
    private Commit findCommonAncestor(Commit commitB1,
                                      Commit commitB2) {
        ArrayList<Commit> commitsList1 = new ArrayList<>();
        ArrayList<Commit> commitsList2 = new ArrayList<>();

        while (commitB1 != null) {
            commitsList1.add(commitB1);
            commitB1 = commitB1.getParent();
        }
        while (commitB2 != null) {
            commitsList2.add(commitB2);
            commitB2 = commitB2.getParent();
        }
        for (int i = 0; i < commitsList2.size(); i++) {
            Commit current = commitsList2.get(i);
            if (commitsList1.contains(current)) {
                return current;
            }
        }
        return null;
    }

    /** List all commits.
     * @return list of commits.
     * **/
    private ArrayList<Commit> listAllCommit() {
        ArrayList<Commit> result = new ArrayList<>();
        File file = new File(Commit.getFolderPath());
        String [] commitIds = file.list();
        for (String id : commitIds) {
            String path = Commit.getFolderPath();
            Commit commit = Commit.fromFile(new File(path + "/" + id));
            result.add(commit);
        }
        return result;
    }

    /** Serialize the _main gitlet instance to file. **/
    public void toFile() {
        String filePath = getDefaultFolder() + "/main";
        File file = new File(filePath);
        Utils.writeObject(file, _main);
    }

    /** Retrieve the _main instance from file.
     * @return the gitlet instance from file.
     * **/
    public static Gitlet fromFile() {
        String filePath = getDefaultFolder() + "/main";
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        _main = Utils.readObject(file, Gitlet.class);
        return _main;
    }

    /** Return the current working directory. **/
    private String getWorkingDir() {
        return System.getProperty("user.dir");
    }

    /** The default location for gitlet. **/
    private static String _defaultFolder = ".gitlet";

    /** HEAD pointer to latest commit. **/
    private Branch _HEAD = null;
    /** HashMap for managing the branches. **/
    private HashMap<String, Branch> _branches = new HashMap<>();
    /** Store add zone. **/
    private LinkedHashSet<String> _stagedForAdd = new LinkedHashSet<>();
    /** Store remove zone. **/
    private LinkedHashSet<String> _stagedForRemove = new LinkedHashSet<>();
    /** Main instance of gitlet. **/
    private static Gitlet _main;
    /** Time format form of the gitlet. **/
    private String _form = "Date: %1$ta %1$tb %1$te"
            + " %1$tH:%1$tM:%1$tS %1$tY %1$tz";
}
