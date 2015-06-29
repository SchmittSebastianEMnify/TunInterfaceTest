# TunInterfaceTest

Small sample program to use a Tun Interface from Java. Opens the Tun Interface and writes a sample icmp ping request to tun0 in one Thread and retrieves the responses in another Thread.

After 10 pings the interface is closed in the writing Thread to see the reading Thread fail and stop the program

Before running the application create the interface, allow ip forwarding, use masquerade and add the route:

    //create tun0
    sudo ip tuntap add dev tun0 mode tun
    sudo ip link set tun0 up
    sudo ip addr add 10.199.0.0/16  dev tun0
    
    //allow ip forwarding
    sudo sysctl -w net.ipv4.ip_forward=1
    
    // set MASQUERADE
    sudo iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
    
    //add Route (15.15.15.15 is the sender of the ping)
    sudo ip route add 15.15.15.0/24 dev tun0
