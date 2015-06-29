package com.schmittsebastian.main;

import com.schmittsebastian.tun.TunInterface;

public class Tunmain {

  /**
   * Small sample program to use a Tun Interface from Java. Opens the Tun Interface and writes a
   * sample icmp ping request to tun0 in one Thread and retrieves the responses in another Thread.
   * 
   * After 10 pings the interface is closed in the writing Thread to see the reading Thread fail and
   * stop the program
   * 
   * @param args
   */
  public static void main(String[] args) {
    // open Tun Interface tun0
    int fd = TunInterface.open("tun0", true);

    // Ping packet from 15.15.15.15 to 10.88.0.158
    byte[] ping =
        {(byte) 0x45, (byte) 0x00, (byte) 0x00, (byte) 0x54, (byte) 0x6c, (byte) 0xee, (byte) 0x00,
            (byte) 0x00, (byte) 0xff, (byte) 0x01, (byte) 0x25, (byte) 0xa7, (byte) 0x0f,
            (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0a, (byte) 0x58, (byte) 0x00,
            (byte) 0x9e, (byte) 0x08, (byte) 0x00, (byte) 0xc0, (byte) 0xb8, (byte) 0x21,
            (byte) 0xd1, (byte) 0x00, (byte) 0x01, (byte) 0x0e, (byte) 0x42, (byte) 0x8d,
            (byte) 0x55, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xae,
            (byte) 0x0a, (byte) 0x0d, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14,
            (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a,
            (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f, (byte) 0x20,
            (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26,
            (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c,
            (byte) 0x2d, (byte) 0x2e, (byte) 0x2f, (byte) 0x30, (byte) 0x31, (byte) 0x32,
            (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37};
    System.out.println("Ping IPs:");
    printIps(ping);

    // Read from tun0 in one Thread ...
    new Thread("ReadTun") {
      public void run() {
        byte buf[] = new byte[1500];
        int count = 1;
        int j = 0;

        while (true) {

          try {
            j = TunInterface.read(fd, buf);
          } catch (Exception e) {
            System.out.println(this.getName() + ": " + e.getMessage());
            System.out.println(this.getName() + ": Try to close again");

            try {
              TunInterface.close(fd);
            } catch (Exception c) {
              System.out.println(this.getName() + ": " + c.getMessage());
            }

            System.out.println(this.getName() + ": " + "Shutdown application!");
            System.exit(0);
          }

          if (j > 0) {
            System.out.println();
            System.out.println("Length: " + j + ", Packet : " + Tunmain.buftoString(buf, j));
            printIps(buf);
            System.out.println(this.getName() + ": Received: " + count++);
          }
        }
      }
    }.start();


    // ... Write to tun0 in another Thread
    new Thread("WriteTun") {
      public void run() {

        for (int i = 0; i < 10; i++) {
          TunInterface.write(fd, ping, ping.length);
          try {
            Thread.sleep(250);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }

        System.out.println();
        System.out.println(this.getName() + ": Close the Interface");

        TunInterface.close(fd);
      }
    }.start();
  }

  /**
   * Print all the bytes of the packet on the console
   * 
   * @param buf The byte array to get the IPs from
   * @param length The length of the packet
   * @return
   */
  public static String buftoString(byte[] buf, int length) {
    String result = "";
    for (int i = 0; i < length; i++) {
      result += String.format("%02x", buf[i]);
    }
    return result;
  }

  /**
   * Print the IPs of a packet on the console
   * 
   * @param buf The byte array to get the IPs from
   */
  public static void printIps(byte[] buf) {
    try {
      System.out.println("FROM: " + (buf[12] & 0xff) + "." + (buf[13] & 0xff) + "."
          + (buf[14] & 0xff) + "." + (buf[15] & 0xff));
      System.out.println("TO: " + (buf[16] & 0xff) + "." + (buf[17] & 0xff) + "."
          + (buf[18] & 0xff) + "." + (buf[19] & 0xff));
    } catch (Exception e) {
      System.out.println("Failed to get Ips");
    }
  }
}
