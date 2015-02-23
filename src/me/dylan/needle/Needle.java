package me.dylan.needle;

import me.dylan.needle.logging.LogLevel;
import me.dylan.needle.logging.Logger;
import me.dylan.needle.networking.Prod;
import me.dylan.needle.networking.Syringe;
import me.dylan.needle.scraper.Scraper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Needle {
    private String ip = "";
    private List<Integer> openPorts = new ArrayList<>();
    private static final int TIMEOUT = 5000;
    public static int currentPort = 0;
    private int delay = 0;

    public Needle() {
        String scrapeStr = Input.prompt("Would you like to scrape for servers or specify one yourself? ");

        while(true) {
            if(scrapeStr.toLowerCase().contains("scrape")) {
                try {
                    for (String address : Scraper.getIpsFromMCSL()) {
                        hostStatus(address);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                hostStatus(Input.prompt("Enter a server address: "));
            }

            if(!openPorts.isEmpty()) {
                Logger.info("_.-=======~Needle scan results~========-._");
                System.out.println();
                System.out.println();
                Logger.info("_.-=========~Open ports~==============-._");
                for(Integer integer : openPorts) {
                    Logger.info("\n             " + integer.toString());
                }
            } else {
                Logger.log("_.-========~No Ports found~===========-._", LogLevel.INFO);
            }


            String more = Input.prompt("Proccess completed. Would you like to perform another scan?");

            if(!more.toLowerCase().startsWith("y")) {
                break;
            }

        }

    }

    public void hostStatus(String ip) {
        this.ip = ip;
//        ip = Input.prompt("Please enter an IP for prodding: ");
        int threads = Integer.parseInt(Input.prompt("Enter an amount of threads to use: "));
        Logger.log("Prodding IP address " + ip, LogLevel.INFO);
        try {
            if(!Prod.prodIp(ip, TIMEOUT)) {
                Logger.log("Could not reach host " + ip + ".", LogLevel.ERROR);
                //return;
            }
        } catch (IOException e) {
            Logger.log("Couldn't reach " + ip + ".", LogLevel.ERROR);
            Logger.log("Reason: " + e.toString(), LogLevel.DEBUG);
            return;
        }
        try {
            prodPorts(threads);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized int getCurrentPort() {
        return currentPort;
    }

    public synchronized void incrementCurrentPort() {
        currentPort++;
    }

     public void prodPorts(int threads) throws Exception {
            boolean success = false;
            while(!success) {
                String port = Input.prompt("Please enter a port or a port range to check(1-65535): ");
                if(port.contains("-")) {
                    try {
                        final int start = Integer.parseInt(port.split("-")[0]);
                        final int end = Integer.parseInt(port.split("-")[1]);
                        delay = Integer.parseInt(Input.prompt("Please enter a delay value(ms): "));
                        currentPort = start;
                        Input.prompt("Beginning port scan, press enter to confirm");

                        List<Thread> threadsList = new ArrayList<>();

                        for(int j = 0; j < threads; j++) {

                            Thread thread = new Thread(new Runnable() {

                                @Override
                                public void run() {

                                    while(true) {
                                        int portSynced = getCurrentPort();
                                        if (portSynced < end) {
                                            Logger.log("Prodding port " + portSynced, LogLevel.INFO);
                                            try {
                                                prodPort(portSynced);
                                                Thread.sleep(delay);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            incrementCurrentPort();
                                        }

                                    }

                                }

                            });

                            threadsList.add(thread);
                            thread.start();
                        }
                        for(Thread thread : threadsList) {
                            thread.join();
                        }
                        success = true;

                    } catch (NumberFormatException e) {
                        Logger.log("Invalid port range or delay specified." +
                                "Please enter only valid numbers.", LogLevel.ERROR);

                    }

                } else {
                    try {
                        int portVal = Integer.parseInt(port);
                        prodPort(portVal);
                        success = true;

                    } catch(NumberFormatException e) {
                        Logger.log("Invalid port specified. please enter a valid number.", LogLevel.ERROR);

                    }

                }

            }
    }


    public void prodPort(int port) throws InterruptedException {
        try {
            if (Prod.prodPort(ip, port, TIMEOUT)) {
                Logger.info(Syringe.mcPing(ip, port));   //throws ioexception
                Logger.info("Found Minecraft listener on port " + port);
                openPorts.add(port);
            }
            Thread.sleep(delay);
        } catch(IOException e) {}
    }

    public static void main(String[] args) {
        new Needle();
    }
}
