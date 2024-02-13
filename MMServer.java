
/** Server program for the MM app
 *
 *  @author Michael Hulbert, Woo Sik Choi, Alex Rodriguez
 *
 *  @version CS 391 - Spring 2024 - A1
 **/

import java.net.*;
import java.io.*;
import java.util.*;

public class MMServer {
    static ServerSocket serverSocket = null; // listening socket
    static int portNumber = 5555; // port on which server listens
    static Socket clientSocket = null; // socket to a client
    static DataInputStream in = null; // input stream from client
    static DataOutputStream out = null; // output stream to client

    /*
     * Start the server then wait for a connection request. After accepting a
     * connection request, use an instance of MM to send to the client an initial
     * request for his or her next guess and then repeatedly:
     * 1. wait for and read in a request
     * 2. compute the reply
     * 3. send the reply to the client
     * until the reply is "Thank you for playing!"
     * The amount and format of the console output (e.g., client request,
     * server replies) are imposed as part of the problem statement in the
     * handout (as shown in the provided session trace).
     */
    public static void main(String[] args) {
        String request, reply = "";

        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Server started: " + serverSocket);
            System.out.println("Waiting for a client...");
            clientSocket = serverSocket.accept();
            System.out.println("Connection established: " + clientSocket);
            openStreams();
            MM game = new MM();
            out.writeUTF(MM.nextGuess);
            while (!reply.equals(MM.thankYou)) {
                request = in.readUTF();
                reply = game.getReply(request.toUpperCase());
                out.writeUTF(reply);
            }
            close();
        } catch (IOException e) {
            System.out.println("Server encountered an error. Shutting down...");
        }

    }// main method

    /*
     * open the necessary I/O streams and initializes the in and out
     * static variables; this method does not catch any exceptions.
     */
    static void openStreams() throws IOException {
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());
    }// openStreams method

    /*
     * close all open I/O streams and sockets
     */
    static void close() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            System.err.println(" Error in close (): " + e.getMessage());
        }

    }// close method

}// MMServer class

/*
 * this class implements the MM FSM
 **/
class MM {
    private static final int PLAY = 0; // P state
    private static final int GAMEOVER = 1; // GO state
    private int state; // current state
    private String answer; // pattern to discover
    private ArrayList<String> trace;
    static String youWin = "    You win!";
    static String nextGuess = "    Type your next guess:";
    static String playAgain = "    Another game? (Y/N)";
    static String thankYou = "    Thank you for playing!";

    /*
     * the FSM starts in the P state and the trace is empty
     */
    MM() {
        answer = pickRandomAnswer();
        state = PLAY;
        trace = new ArrayList<>();
    }// constructor

    /*
     * return a 4-character string in which each character is a uniformly
     * randomly selected character in the range A-F. This string must be printed
     * to the console before it is returned.
     */
    private String pickRandomAnswer() {
        String charSet = "ABCDEF";
        String result = "";
        Random r = new Random();

        for(int i=0; i<4; i++)
            result += charSet.charAt(r.nextInt(charSet.length()));
            
        System.out.println(result);
        return result;
    }// pickRandomAnswer method

    /*
     * return a string that encodes the codemaker's feedback to the given guess.
     * the format of the feedback is fully specified in the handout.
     */
    private String getFeedback(String guess) {
        if (guess.equals(answer)) {
            return "BBBB";
        }
        String reply = "", leftover = "";
        ArrayList<Character> letterBank = new ArrayList<>();
        for (char x : answer.toCharArray()) {
            letterBank.add(x);
        }
        for (int i = 0; i < guess.length(); i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                reply += "B";
                letterBank.remove((Character) answer.charAt(i));
            } else {
                leftover += guess.charAt(i);
            }
        }
        for (char c : leftover.toCharArray()) {
            if (letterBank.contains(c)) {
                reply += "W";
                letterBank.remove((Character) c);
            }
        }
        return reply;
    }// getFeedback method

    /*
     * Implement the transition function of the FSM. In other words, return
     * the server's reply as a function of the client's query. Also update
     * the current state and other variables as needed.
     */
    public String getReply(String query) {
        if (query.equals(answer) && state != GAMEOVER) {
            state = GAMEOVER;
            return youWin + "\n" + playAgain;
        }
        if (state == GAMEOVER) {
            if (query.equals("Y")) {
                trace.clear();
                answer = pickRandomAnswer();
                state = PLAY;
                System.out.println(answer);
                return nextGuess;
            }
            return thankYou;
        }
        trace.add(query + " " + getFeedback(query));
        String reply = "    =====================\n";
        reply += "    Previous guesses:\n";
        for (String x : trace) {
            reply += "    " + x + "\n";
        }
        reply += "    =====================\n";

        return reply;
    }// getReply method

}// MM class
