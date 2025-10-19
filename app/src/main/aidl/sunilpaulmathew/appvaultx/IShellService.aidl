package sunilpaulmathew.appvaultx;

interface IShellService {
    String runCommand(String command) = 0;
    void runCommands(String commands) = 1;
    void writeToFile(in ParcelFileDescriptor pfd, String path) = 2;
    void destroyProcess() = 3;
}