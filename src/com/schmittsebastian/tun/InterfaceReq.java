package com.schmittsebastian.tun;

import com.sun.jna.Structure;
import com.sun.jna.Union;

import java.util.Arrays;
import java.util.List;

public class InterfaceReq extends Structure {

    public InterfaceReqName ifr_ifrn;
    public InterfaceReqParam ifr_ifru;

    @Override
    protected List getFieldOrder() {
      return Arrays.asList("ifr_ifrn", "ifr_ifru");
    }

    public static class InterfaceReqByRef extends InterfaceReq implements Structure.ByReference {
    }

    public static class InterfaceReqName extends Union {
    public byte[] ifrn_name = new byte[TunInterface.IFNAMSIZ];

      public static class ByReference extends Union implements Structure.ByReference {
      }
    }

    public static class InterfaceReqParam extends Union {
      public SockAddr ifru_hwaddr;
      public SockAddr ifru_addr;
      public SockAddr ifru_dstaddr;
      public SockAddr ifru_broadaddr;
      public SockAddr ifru_netmask;
      public short ifru_flags;
      public int ifru_ivalue;
      public int ifru_mtu;
    public byte[] ifru_slave = new byte[TunInterface.IFNAMSIZ];
    public byte[] ifru_newname = new byte[TunInterface.IFNAMSIZ];

      public static class ByReference extends Union implements Structure.ByReference {
      }
    }

    public static class SockAddr extends Structure {
      public short sa_family_t;
      public byte[] sa_data = new byte[14];

      @Override
      protected List getFieldOrder() {
        return Arrays.asList("sa_family_t", "sa_data");
      }
    }
  }