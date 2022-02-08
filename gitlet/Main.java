package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Vinh Bui
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        String defaultAuthor = "Vinh Bui";
        if (args.length < 1) {
            System.out.println("Please enter a command.");
            return;
        }
        String command = getCommand(args[0]);
        if (command == null) {
            return;
        }
        Gitlet gitlet = Gitlet.fromFile();
        if (gitlet == null) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        String filename = "";
        switch (command) {
        case "add":
            filename = args[1];
            gitlet.add(filename);
            break;
        case "rm":
            filename = args[1];
            gitlet.remove(filename);
            break;
        case "commit":
            String message = args[1];
            gitlet.commit(defaultAuthor, message);
            break;
        case "checkout":
            checkout(gitlet, args);
            break;
        case "log":
            gitlet.log();
            break;
        case "status":
            gitlet.getStatus();
            break;
        case "global-log":
            gitlet.globalLog();
            break;
        case "find":
            gitlet.find(args[1]);
            break;
        case "branch":
            gitlet.createBranch(args[1]);
            break;
        case "rm-branch":
            gitlet.removeBranch(args[1]);
            break;
        case "reset":
            gitlet.reset(args[1]);
            break;
        case "merge":
            gitlet.merge(args[1]);
            break;
        default:
            System.out.println("No command with that name exists.");
        }
    }

    /**
     * Get the command, perform init if command is init.
     * @param arg incoming command
     * @return a command String
     */
    private static String getCommand(String arg) {
        String command = arg;
        if (command.equals("init")) {
            Gitlet.init();
            return null;
        }
        return command;
    }

    /** Manage checkout function for gitlet.
     * @param gitlet the gitlet instance
     * @param args inputs for checkout
     * **/
    private static void checkout(Gitlet gitlet, String[] args) {
        if (args[1].equals("--")) {
            gitlet.checkout(args[2]);
        } else if (args.length > 3 && args[2].equals("--")) {
            String commitHash = args[1];
            String fileName = args[3];
            gitlet.checkoutWithCommitID(commitHash, fileName);
        } else if (args.length == 2) {
            gitlet.checkoutBranch(args[1]);
        } else {
            System.out.println("Incorrect operands.");
        }
    }
}
