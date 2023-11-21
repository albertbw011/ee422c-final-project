import java.io.*;
import java.net.Socket;

public class ClientThread extends Server implements Runnable{
    private Socket socket;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;

    public ClientThread(Socket socket) {
        this.socket = socket;
    }
    public void run() {
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(socket.getOutputStream());

            while (!socket.isClosed()) {
                String input = bufferedReader.readLine();
                if (input != null) {
                    for (ClientThread client : clients) {
                        client.getWriter().write(input);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PrintWriter getWriter() {
        return printWriter;
    }
}
