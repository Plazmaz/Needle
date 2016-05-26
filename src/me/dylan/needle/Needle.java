package me.dylan.needle;

import me.dylan.needle.logging.LogLevel;
import me.dylan.needle.logging.Logger;
import me.dylan.needle.networking.Prod;
import me.dylan.needle.networking.Syringe;
import me.dylan.needle.scraper.Scraper;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Needle {
    private String ip = "";
    private Map<String, List<Integer>> openPorts = new HashMap<>();
    private static final int TIMEOUT = 5000;
    public static int currentPort = 0;
    private int delay = 0;

    public Needle() {
        Logger.info("_.-=======~Needle Scanning Utility~========-._");
        Logger.info("We're going to begin scanning soon, but first we need some info!");
        Logger.info("");
        Logger.info("");
        String scrapeStr = Input.prompt("Would you like to scrape for servers(otherwise you will need to specify one yourself)? ");
        String port = Input.prompt("Please enter a port or a port range to check(1-65535): ");
        delay = Integer.parseInt(Input.prompt("Please enter a delay value(ms): "));
        int threads = Integer.parseInt(Input.prompt("Enter an amount of threads to use: "));

        while(true) {
            if(scrapeStr.toLowerCase().contains("yes")) {
                try {
                    for (String address : Scraper.getIpsFromMCSL()) {
                        hostStatus(address, port, threads);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                hostStatus(Input.prompt("Enter a server address: "), port, threads);
            }


            String more = Input.prompt("Proccess completed. Would you like to perform another scan?");

            if(!more.toLowerCase().startsWith("y")) {
                break;
            }

        }

    }

    public void hostStatus(String ip, String ports, int threads) {
        this.ip = ip;
//        ip = Input.prompt("Please enter an IP for prodding: ");
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
            prodPorts(threads, ip, ports);
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

     public void prodPorts(int threads, String host, String port) throws Exception {
            boolean success = false;
            while(!success) {
                if(port.contains("-")) {
                    try {
                        final int start = Integer.parseInt(port.split("-")[0]);
                        final int end = Integer.parseInt(port.split("-")[1]);
                        currentPort = start;
                        String allowScan = Input.prompt("Beginning port scan for host " + host + ", press enter to confirm or type 'skip' to skip.");
                        if(allowScan.equals("skip")) {
                            return;
                        }
                        List<Thread> threadsList = new ArrayList<>();

                        for(int j = 0; j < threads; j++) {

                            final Thread thread = new Thread() {

                                @Override
                                public void run() {
                                    while (!this.isInterrupted()) {
                                        int portSynced = getCurrentPort();
                                        if (portSynced < end) {
                                            Logger.log("Prodding port " + portSynced, LogLevel.INFO);
                                            try {
                                                prodPort(portSynced++);
                                                Thread.sleep(delay);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            incrementCurrentPort();
                                        } else {
                                            this.interrupt();
                                        }

                                    }

                                }
                            };

                            threadsList.add(thread);
                            thread.start();
                        }
                        for(Thread thread : threadsList) {
                            thread.join();
                        }

                        if(!openPorts.isEmpty()) {
                            Logger.info("_.-=======~Needle scan results~========-._");
                            System.out.println();
                            System.out.println();
                            Logger.info("_.-=========~Open ports~==============-._");
                            for(String key : openPorts.keySet()) {
                                Logger.info("\n             " + key);
                            }
                        } else {
                            Logger.log("_.-========~No Ports found~===========-._", LogLevel.INFO);
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
                openPorts.get(ip).add(port);
            }
            Thread.sleep(delay);
        } catch(IOException e) {}
    }

    public static void main(String[] args) {
        new Needle();
    }
}
