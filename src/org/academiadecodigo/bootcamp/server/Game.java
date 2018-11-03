package org.academiadecodigo.bootcamp.server;

import org.academiadecodigo.bootcamp.enums.Hand;
import org.academiadecodigo.bootcamp.messages.Messages;

import java.util.LinkedList;
import java.util.List;

public class Game {

    private List<ClientHandler> clients;
    private int[] clientScores;
    private int currentRound;
    private int maxRounds;

    public Game(ClientHandler clientHandler1, ClientHandler clientHandler2){
        init();
        clients.add(clientHandler1);
        clients.add(clientHandler2);
    }

    private void init(){
        clients = new LinkedList<>();
        clientScores = new int[2];
        currentRound = 1;
        maxRounds = 5;
    }

    private void roundPlay(Hand client1Hand, Hand client2Hand) {

        if (client1Hand != client2Hand) {
            switch (client1Hand){
                case ROCK:
                    if (client2Hand == Hand.PAPER){
                        addPoint(clients.get(1));
                        return;
                    }
                    break;
                case PAPER:
                    if (client2Hand == Hand.SCISSORS){
                        addPoint(clients.get(1));
                        return;
                    }
                    break;
                case SCISSORS:
                    if (client2Hand == Hand.ROCK){
                        addPoint(clients.get(1));
                        return;
                    }
                    break;
            }

            addPoint(clients.get(0));
            return;
        }

        clients.get(0).send(Messages.ROUND_TIE);
        clients.get(1).send(Messages.ROUND_TIE);
    }

    public void start() {
        clients.get(0).send(Messages.VERSUS_PART1 + clients.get(1).getName() + Messages.VERSUS_PART2);
        clients.get(1).send(Messages.VERSUS_PART1 + clients.get(0).getName() + Messages.VERSUS_PART2);

        while (currentRound <= maxRounds) {
            clients.get(0).send(Messages.ROUND_PART1 + currentRound + Messages.ROUND_PART2);
            clients.get(1).send(Messages.ROUND_PART1 + currentRound + Messages.ROUND_PART2);

            clients.get(0).send(Messages.WAITING);
            clients.get(1).send(Messages.WAITING);

            Hand client1Hand;
            Hand client2Hand;

            while ((client1Hand = clients.get(0).getHand()) != null && (client2Hand = clients.get(0).getHand()) != null) {
                roundPlay(client1Hand, client2Hand);
                break;
            }

            currentRound++;
        }

        endGame();

    }

    private void addPoint(ClientHandler clientHandler){
        if (clientHandler == clients.get(0)){
            clientScores[0]++;
            clients.get(0).send(Messages.ROUND_WIN);
            clients.get(1).send(Messages.ROUND_LOST);
        }else{
            clientScores[1]++;
            clients.get(0).send(Messages.ROUND_LOST);
            clients.get(1).send(Messages.ROUND_WIN);
        }
    }

    private void endGame(){

        String client1status = Messages.WON_LOG;
        String client2stauts = Messages.WON_LOG;

        if (clientScores[0] == clientScores[1]){
            clients.get(0).send(Messages.GAME_TIE);
            clients.get(1).send(Messages.GAME_TIE);
            client1status = Messages.TIE_LOG;
            client2stauts = Messages.TIE_LOG;
        }

        if (clientScores[0] > clientScores[1]){
            clients.get(0).send(Messages.GAME_WON);
            clients.get(1).send(Messages.GAME_LOST);
            client2stauts = Messages.LOST_LOG;
        }else{
            clients.get(0).send(Messages.GAME_LOST);
            clients.get(1).send(Messages.GAME_WON);
            client1status = Messages.LOST_LOG;
        }

        clients.get(0).setInGame(false);
        clients.get(1).setInGame(false);
        Server.saveLog(clients.get(0) + Messages.ESCAPE_TAG + client1status + Messages.ESCAPE_TAG + clients.get(1) + Messages.NEW_LINE);
        Server.saveLog(clients.get(1) + Messages.ESCAPE_TAG + client2stauts + Messages.ESCAPE_TAG + clients.get(2) + Messages.NEW_LINE);
    }
}