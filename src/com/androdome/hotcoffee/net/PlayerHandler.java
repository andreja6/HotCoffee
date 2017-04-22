package com.androdome.hotcoffee.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.androdome.hotcoffee.server.HeartSaltSend;
import com.androdome.hotcoffee.server.Main;

public class PlayerHandler 
{
	Socket socket;
	String username;
	Main main;
	int playerid;
	DataInputStream in;
	DataOutputStream out;
	PacketParser pPacket;

	public PlayerHandler(Socket sock, Main m) 
	{
		socket = sock;
		main = m;
	}

	public void Player() throws IOException 
	{
		boolean failed = false;
		main.gui.write("Socket connected from " + socket.getInetAddress());
		pPacket = new PacketParser();
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());

		// PacketType.
		String[] info = pPacket.recieve(PacketType.INDENTIFICATION, in);
		pPacket.send(PacketType.INDENTIFICATION, new Object[] { 7, "Test",
				"tast", 0 }, out);

		String mppass = info[2].replace(" ", "");
		String name = info[1].replace(" ", "");
		int packetversion = Integer.parseInt(info[0]);
		
		String verify = HeartSaltSend.generate(Main.salt + name);

		if (mppass.equalsIgnoreCase(verify)) 
		{
			main.gui.write("Verified");
		} 
		
		else 
		{
			if (Main.verify)
				failed = true;
			main.gui.write("Not verified");
		}
		
		if (packetversion != 7 || failed || Main.users >= Main.max) 
		{
			String error = "";
			
			if (packetversion != 7)
				error = "Wrong packet version";
			else if (failed)
				error = "You could not be verified by classicube.net!";
			else if (Main.users >= Main.max)
				error = "This server is full";
			
			pPacket.send(PacketType.DISCONNECT, new Object[] { error }, out);
			main.gui.write(name + " disconnected: " + error);
			socket.close();
		} 
		
		else 
		{
			this.username = name;
			this.playerid = socket.getTrafficClass();
			System.out.print("Player successfully connected: " + name + ":" + playerid);
			Main.playerHandler[playerid] = this;
		}
	}
	public void disconnect(String reason) throws IOException
	{
		if(reason == null)
			reason = "Kicked";
		else
			reason = "Kicked: " + reason;
		if(out != null)
		{
			pPacket.send(PacketType.DISCONNECT, new Object[] {reason}, out);
			socket.close();
		}	
		Main.playerHandler[playerid] = null;
	}
}