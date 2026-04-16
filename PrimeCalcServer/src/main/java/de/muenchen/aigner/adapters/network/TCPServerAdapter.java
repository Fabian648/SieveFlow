package de.muenchen.aigner.adapters.network;

import de.muenchen.aigner.domain.service.PrimeService;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServerAdapter {
    private final PrimeService primeService;
    private final int PORT = 1337;

    enum State {INIT, WAIT}

    public TCPServerAdapter(PrimeService primeService) {
        this.primeService = primeService;
    }

    public void start(){
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("Listening on port " + PORT);

            while(true){
                Socket socket = serverSocket.accept();
                System.out.println("Accepted connection from " + socket.getInetAddress().getHostName());

                Thread.ofVirtual().start(() -> handleClient(socket));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket){
        try (socket){
            BufferedReader in =  new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            State state = State.INIT;
            out.println("Welcome to BitSieve! Type ’help’ for commands.");

            String command;
            while ((command = in.readLine()) != null) {
                command = command.trim().toLowerCase();

                if(command.equals("exit") || command.equals("quit")){
                    out.println("Goodbye!");
                    break;
                }

                switch (state) {
                    case INIT -> {
                        if(command.equals("help") || command.equals("?")) {
                            out.println("Commands: is [number], help, exit");
                        } else if (command.startsWith("is ")){
                            processPrimeCheck(command.substring(3), out);
                        } else {
                            out.println("Unknown command. Type 'help' for commands.");
                        }
                    }
                }
            }
        } catch (IOException e){
            System.out.println("Client disconnected: " + e.getMessage());
        }
    }

    private void processPrimeCheck(String command, PrintWriter out){
        try{
            java.math.BigInteger number = new java.math.BigInteger(command.trim());
            boolean isPrime = primeService.isPrime(number);
            out.println(number + " is " + (isPrime ? "a PRIME" : "NOT a prime") + ".");
        } catch (NumberFormatException e){
            out.println("Error: That’s not a valid number.");
        }
    }
}
