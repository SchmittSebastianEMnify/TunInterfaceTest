package com.schmittsebastian.tun;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface NativeTunLibrary extends Library {

  NativeTunLibrary INSTANCE = (NativeTunLibrary) Native.loadLibrary("c", NativeTunLibrary.class);

  int ioctl(int fd, int code, InterfaceReq req);

  int ioctl(int fd, int code, int owner);

  int open(String name, int flags);

  int close(int fd);

  int fcntl(int fd, int cmd);

  int read(int handle, byte[] buffer, int nbyte);

  int write(int handle, byte[] buffer, int nbyte);

  String strerror(int errorCode);
}
