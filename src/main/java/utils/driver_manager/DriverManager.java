package utils.driver_manager;



import bind.load_test.LoadTest;
import utils.process.OpenDriverProcess;
import utils.socket.ClientSocket;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class DriverManager {

    private ClientSocket clientSocket;
    private static OpenDriverProcess openDriverProcess;
    private String host;
    private int port;
    public String driverPath;
    public LoadTest loadTest = new LoadTest(this);


    private String setDriver(String host, int port, String platform){
        switch (platform) {
            case "windows":
                if (this.driverPath == null)
                    this.driverPath = Path.of("").toAbsolutePath() + "/generate_load_density_driver_win.exe";
                break;
            case "linux":
                if (this.driverPath == null)
                    this.driverPath = Path.of("").toAbsolutePath() + "/generate_load_density_driver_liinux";
                break;
            case "macos":
                if (driverPath == null)
                    this.driverPath = Path.of("").toAbsolutePath() + "/generate_load_density_driver_macos";
                break;
        }
        this.host = host;
        this.port = port;
        this.driverPath = this.driverPath.replace("\\", "/");
        return this.driverPath;
    }


    public DriverManager(String host, int port, String driverPath, String platform) throws IOException {
        this.driverPath = driverPath;
        setDriver(host, port, platform);
        if (openDriverProcess == null) {
            openDriverProcess = new OpenDriverProcess(this.driverPath);
            openDriverProcess.start();
            this.clientSocket = new ClientSocket(this.host, this.port);
            while (!openDriverProcess.isAlive()) {
            }
        } else {
            throw new IOException("Can't init DriverManager");
        }
    }

    public DriverManager(String host, int port, List<String> processCommandList, String platform) throws IOException {
        this.driverPath = processCommandList.get(0);
        setDriver(host, port, platform);
        if (openDriverProcess == null) {
            openDriverProcess = new OpenDriverProcess(processCommandList);
            openDriverProcess.start();
            this.clientSocket = new ClientSocket(this.host, this.port);
            while (!openDriverProcess.isAlive()) {
            }
        } else {
            throw new IOException("Can't init DriverManager");
        }
    }


    public String sendCommand(String commandToSend) {
        int retryCount = 5;
        while (retryCount >= 0) {
            if (openDriverProcess.isAlive() && this.clientSocket != null) {
                return this.clientSocket.sendData(commandToSend);
            } else {
                System.err.printf("Driver not ready %s%n", commandToSend);
                retryCount -= 1;
            }
        }
        return "";
    }


    public void quit() {
        try {
            this.clientSocket.sendData("quit_server");
            this.clientSocket.closeClient();
            openDriverProcess.close();
            openDriverProcess = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
