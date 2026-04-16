package de.muenchen.aigner.adapters.network;

import de.muenchen.aigner.domain.service.PrimeService;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServerAdapter {
    private final PrimeService primeService;
    private final int PORT = 1337;

    enum State {INIT, WAIT, RESPONSE}

    public TCPServerAdapter(PrimeService primeService) {
        this.primeService = primeService;
    }

    public void start(){

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Listening on port " + serverSocket.getLocalPort());

            while (true) {
                try(Socket socket = serverSocket.accept()) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    System.out.println("Connection established");
                    State state = State.INIT;
                    boolean active = true;

                    while (active) {
                        String command = in.readLine();
                        if(command == null) {
                            break;
                        }

                        System.out.println("Waiting for command");

                        String response = "";
                        boolean valid = false;

                        switch (state) {
                            case INIT -> {
                                if(command.equals("?") || command.equals("help")) {
                                    response = "You can use:\n" +
                                            "is [number]\n]" +
                                            "exit";
                                }
                            }
                            
                            case WAIT -> {
                                if(command.equals("?") || command.equals("help")) {
                                    response = "You can use:\n" +
                                            "exit";
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Fehler: " + e.getMessage());
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
