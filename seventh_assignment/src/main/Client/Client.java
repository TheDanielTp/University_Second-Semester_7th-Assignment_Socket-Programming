import java.io.*;
import java.net.*;
import java.util.*;

public class Client
{
    private Socket         socket;
    private BufferedReader input;
    private PrintWriter    output;

    private final String         serverAddress;
    private final int            serverPort;
    private final BufferedReader consoleInput;

    public Client (String serverAddress, int serverPort)
    {
        this.serverAddress = serverAddress;
        this.serverPort    = serverPort;
        this.consoleInput  = new BufferedReader (new InputStreamReader (System.in));
    }

    public void start ()
    {
        try
        {
            socket = new Socket (serverAddress, serverPort);
            input  = new BufferedReader (new InputStreamReader (socket.getInputStream ()));
            output = new PrintWriter (socket.getOutputStream (), true);

            System.out.println ("Connected to the server.");

            // Choose between chat or file download
            System.out.println ("Enter '1' for group chat or '2' for file download:");
            String choice = consoleInput.readLine ();

            if ("1".equals (choice))
            {
                startGroupChat ();
            }
            else if ("2".equals (choice))
            {
                startFileDownload ();
            }
            else
            {
                System.out.println ("Invalid choice. Exiting.");
                close ();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace ();
        }
    }

    private void startGroupChat ()
    {
        try
        {
            // Send request to join group chat
            output.println ("JOIN_CHAT");

            // Start a new thread to listen for messages from the server
            new Thread (new ServerListener ()).start ();

            // Read messages from console and send to server
            String message;
            while ((message = consoleInput.readLine ()) != null)
            {
                output.println (message);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace ();
        }
    }

    private void startFileDownload ()
    {
        try
        {
            // Send request to list files
            output.println ("LIST_FILES");

            // Receive file list from server
            String fileList = input.readLine ();
            System.out.println ("Available files:");
            System.out.println (fileList);

            // Request a file to download
            System.out.println ("Enter the filename to download:");
            String filename = consoleInput.readLine ();
            output.println ("DOWNLOAD_FILE:" + filename);

            // Receive the file
            receiveFile (filename);
        }
        catch (IOException e)
        {
            e.printStackTrace ();
        }
    }

    private void receiveFile (String filename)
    {
        try
        {
            File             file        = new File (filename);
            FileOutputStream fileOutput  = new FileOutputStream (file);
            InputStream      socketInput = socket.getInputStream ();

            byte[] buffer = new byte[4096];
            int    bytesRead;
            while ((bytesRead = socketInput.read (buffer, 0, buffer.length)) != - 1)
            {
                fileOutput.write (buffer, 0, bytesRead);
            }

            fileOutput.close ();
            System.out.println ("File downloaded: " + filename);
        }
        catch (IOException e)
        {
            e.printStackTrace ();
        }
    }

    private void close ()
    {
        try
        {
            input.close ();
            output.close ();
            socket.close ();
            consoleInput.close ();
        }
        catch (IOException e)
        {
            e.printStackTrace ();
        }
    }

    private class ServerListener implements Runnable
    {
        public void run ()
        {
            try
            {
                String message;
                while ((message = input.readLine ()) != null)
                {
                    System.out.println (message);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace ();
            }
        }
    }

    public static void main (String[] args)
    {
        Scanner scanner = new Scanner (System.in);
        if (args.length == 0)
        {
            System.out.println ("No arguments given.");

            String serverAddress = scanner.nextLine ();
            String serverPortString = scanner.nextLine ();

            int serverPort = Integer.parseInt (serverPortString);

            Client client = new Client (serverAddress, serverPort);
            client.start ();
        }
        if (args.length != 2)
        {
            System.out.println ("Usage: java Client <server address> <server port>");
        }
        else
        {
            String serverAddress = args[0];
            int    serverPort    = Integer.parseInt (args[1]);

            Client client = new Client (serverAddress, serverPort);
            client.start ();
        }
    }
}
