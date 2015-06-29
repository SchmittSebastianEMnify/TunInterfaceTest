package com.schmittsebastian.tun;

import com.sun.jna.Native;

public class TunInterface {

  private static long writeMaxTimeout = 1000L;
  private static long writeTimeout = 100L;

  /* if.h */
  public static final int IFNAMSIZ = 16;

  /* if_tun.h */
  public static final int TUNSETIFF = 0x400454ca;

  public static final int IFF_TUN = 0x0001;
  public static final int IFF_NO_PI = 0x1000;

  /* fcntl.h */
  public static final int O_RDWR = 2;
  public static final int O_NONBLOCK = 04000;
  public static final int EWOULDBLOCK = 11;

  private static String getNativeErrorMsg() {
    return getNativeErrorMsg(Native.getLastError());
  }

  private static String getNativeErrorMsg(int errorCode) {
    return NativeTunLibrary.INSTANCE.strerror(errorCode);
  }

  /**
   * Open a Tun interface.
   *
   * @param tunName The name of the interface to create
   *
   * @param nonblocking If the interface should be opened in non-blocking mode.
   *
   * @return The file descriptor of the open socket to the Tun interface.
   * @throws RuntimeException
   */
  public static int open(String tunName, boolean nonblocking) throws RuntimeException {

    // check if the the name is valid
    if (tunName != null && tunName.length() > IFNAMSIZ) {
      throw new RuntimeException("Tun name too long!");
    }

    String tunPath = "/dev/net/tun";
    int flags = nonblocking ? O_NONBLOCK : 0;
    int fd = NativeTunLibrary.INSTANCE.open(tunPath, flags | O_RDWR);

    if (fd < 0) {
      throw new RuntimeException(String.format("Failed to open the Tun Interface with error: %s!",
          getNativeErrorMsg()));
    }

    InterfaceReq interfaceReq = new InterfaceReq();
    interfaceReq.ifr_ifru.setType("ifru_flags");
    interfaceReq.ifr_ifrn.setType("ifrn_name");
    interfaceReq.ifr_ifru.ifru_flags = IFF_TUN | IFF_NO_PI;

    if (tunName != null && !tunName.isEmpty()) {
      interfaceReq.ifr_ifrn.ifrn_name = tunName.getBytes();
    }
    int result = NativeTunLibrary.INSTANCE.ioctl(fd, TUNSETIFF, interfaceReq);

    // check if ioctl has been successful
    if (result < 0) {
      String errorMessage = getNativeErrorMsg();
      close(fd);
      throw new RuntimeException(String.format("ioctl failed on tun '%s'. Error: %s", tunName,
          errorMessage));
    }

    return fd;
  }

  /**
   * Close the file descriptor.
   *
   * @param fd The file descriptor to close.
   * @throws RuntimeException
   *
   */
  public static void close(int fd) throws RuntimeException {
    Native.setLastError(0);
    int res = NativeTunLibrary.INSTANCE.close(fd);
    if (res < 0) {
      throw new RuntimeException(String.format(
          "Failed to close the Tun Interface with error: %s! (Returned %s)", getNativeErrorMsg(),
          res));
    }
  }

  /**
   * Read from the Tun Interface.
   *
   * @param fd The file descriptor to read from
   *
   * @param buffer The buffer to read to (length should be the MTU)
   *
   * @return Number of bytes actually read
   * @throws RuntimeException
   */
  public static int read(int fd, byte[] buffer) throws RuntimeException {
    Native.setLastError(0);
    int res = NativeTunLibrary.INSTANCE.read(fd, buffer, buffer.length);
    int err = Native.getLastError();
    if (res < 0) {
      if (err != EWOULDBLOCK) {
        throw new RuntimeException(String.format(
            "Failed to read from Tun Interface with error: %s!", getNativeErrorMsg(err)));
      }
      return 0;
    }
    return res;
  }

  /**
   * Write to Tun Interface.
   *
   * @param fd The file desricptor for the device to write to.
   *
   * @param buffer The buffer to write from.
   *
   * @param nBytes The number of bytes to write.
   * @throws RuntimeException
   */
  public static void write(int fd, byte[] buffer, int nBytes) throws RuntimeException {
    long timeSlept = 0;
    while (nBytes > 0) {
      Native.setLastError(0);
      int res = NativeTunLibrary.INSTANCE.write(fd, buffer, nBytes);
      int err = Native.getLastError();
      if (nBytes == res) {
        return;
      }
      if (res < 0 && err != EWOULDBLOCK) {
        throw new RuntimeException(String.format(
            "Failed to write to Tun Interface with error: %s!", getNativeErrorMsg(err)));
      }
      if (timeSlept >= writeMaxTimeout) {
        throw new RuntimeException("Writing to Tun Interface timed out");
      }
      try {
        System.out.println(String.format("Sleeping for %d millis.", writeTimeout));
        Thread.sleep(writeTimeout);
      } catch (InterruptedException e) {
        System.out.println("Interrupted while writing to tun device: " + e.getMessage());
      }
      timeSlept += writeTimeout;
    }
  }
}
