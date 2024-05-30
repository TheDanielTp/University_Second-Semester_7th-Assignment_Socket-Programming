import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client
{
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private final String serverAddress;
    private final int serverPort;
    private final BufferedReader consoleInput;

    private String username;

    private static final String DOWNLOAD_DIRECTORY = "D:\\Java\\7th Assignment\\University_Second-Semester_7th-Assignment_Socket-Programming";

    public Client(String serverAddress, int serverPort)
    {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.consoleInput = new BufferedReader(new InputStreamReader(System.in));
    }

    public void start()
    {
        try
        {
            // Prompt for username
            System.out.print("Enter your username: ");
            username = consoleInput.readLine();

            socket = new Socket(serverAddress, serverPort);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connected to the server.");

            // Send username to server
            output.println(username);

            // Choose between chat or file download
            System.out.println("Enter '1' for group chat or '2' for file download:");
            String choice = consoleInput.readLine();

            while (!choice.equals("1") && !choice.equals("2"))
            {
                System.out.println("Invalid choice. Enter '1' for group chat or '2' for file download:");
                choice = consoleInput.readLine();
            }

            if ("1".equals(choice))
            {
                System.out.println("Entering Chat...");
                startGroupChat();
            }
            else
            {
                System.out.println("Requesting file list...");
                requestFileList();
                System.out.println("Enter the index of the file to download:");
                String fileIndex = consoleInput.readLine();
                output.println("DOWNLOAD_FILE:" + fileIndex);
                startFileDownload();
            }
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private void requestFileList() throws IOException
    {
        output.println("REQUEST_FILE_LIST");

        String fileListLine;
        while ((fileListLine = input.readLine()) != null)
        {
            if (fileListLine.equals("END_OF_LIST"))
            {
                break;
            }
            System.out.println(fileListLine);
        }
    }

    private void startGroupChat()
    {
        try
        {
            output.println(username + " joined the chat");
            new Thread(new ServerListener()).start();

            String message;
            while ((message = consoleInput.readLine()) != null)
            {
                if (message.equals("0"))
                {
                    start();
                    return;
                }
                // Print the sent message to the client's console
                System.out.println("You: " + message);
                output.println(message);  // Remove the prefix here, as the server will handle it
            }
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private void startFileDownload() {
        try {
            String serverResponse = input.readLine();
            if (serverResponse.startsWith("START_FILE_TRANSFER:")) {
                String fileName = serverResponse.split(":")[1];
                receiveFile(fileName);
            } else if (serverResponse.equals("ERROR: File not found")) {
                System.out.println("Server: " + serverResponse);
            } else {
                System.out.println("Unexpected server response: " + serverResponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveFile(String fileName) {
        try (FileOutputStream fileOutput = new FileOutputStream(DOWNLOAD_DIRECTORY + File.separator + fileName);
             InputStream socketInput = socket.getInputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = socketInput.read(buffer)) != -1) {
                fileOutput.write(buffer, 0, bytesRead);
            }
            System.out.println("File downloaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to download file.");
        }
    }

    private void close()
    {
        try
        {
            input.close();
            output.close();
            socket.close();
            consoleInput.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private class ServerListener implements Runnable
    {
        public void run()
        {
            try
            {
                String message;
                while ((message = input.readLine()) != null)
                {
                    System.out.println(message);
                }
            }
            catch (IOException e)
            {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        if (args.length == 0)
        {
            System.out.println("No arguments given.");

            System.out.print("Enter server address: ");
            String serverAddress = scanner.nextLine();
            System.out.print("Enter server port: ");
            String serverPortString = scanner.nextLine();

            int serverPort = Integer.parseInt(serverPortString);

            Client client = new Client(serverAddress, serverPort);
            client.start();
        }
        else if (args.length != 2)
        {
            System.out.println("Usage: java Client <server address> <server port>");
        }
        else
        {
            String serverAddress = args[0];
            int serverPort = Integer.parseInt(args[1]);

            Client client = new Client(serverAddress, serverPort);
            client.start();
        }
    }
}
