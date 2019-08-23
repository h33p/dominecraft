package com.heep042.mcslauncher;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.lang.InterruptedException;
import java.math.BigInteger;
import java.lang.String;

import com.myjeeva.digitalocean.*;
import com.myjeeva.digitalocean.pojo.*;
import com.myjeeva.digitalocean.impl.*;
import com.myjeeva.digitalocean.exception.*;
import com.myjeeva.digitalocean.common.*;

import me.dilley.MineStat;

public class App 
{
	static final String dropletSize = "s-2vcpu-2gb";//"c-2";
	static final String dropletTag = "mcserver";
	static final String dropletName = "mcdroplet";
	static final String volumeID = "8fc64c7d-bc4b-11e9-b755-0a58ac14d050";
	static final String volumeName = "volume-fra1-01";
	static final String targetRegion = "fra1";
	static final String targetImage = "coreos-stable";
	static final String floatingIP = "165.227.246.106";
	static final int sshKey = 25169833;

	private static String getFileLines(String fileName)
	{
		App app = new App();
		ClassLoader classLoader = app.getClass().getClassLoader();

		String lines = "";

		try (InputStream inputStream = classLoader.getResourceAsStream(fileName)) {
			lines = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		} catch (Exception e) {
		}

		return lines;
	}

	public static String xorHexstr(String str, String key)
	{
		BigInteger bigInt = new BigInteger(str, 16);
		byte[] keyCharsExt = new byte[str.length() / 2];
		byte[] keyChars = key.getBytes();
		for (int i = 0; i < str.length() / 2; i++) {
			keyCharsExt[i] = keyChars[i % key.length()];
		}
		BigInteger charInt = new BigInteger(keyCharsExt);
		return bigInt.xor(charInt).toString(16);
	}

	public static void main(String[] args)
	{
		System.out.println("3bMt launcher");

		if (!serverStatus()) {
			boolean droplet = doRun();

			if (droplet) {
				System.out.println("Waiting for the server to come online...");
				while (!serverStatus()) {
					try {
						TimeUnit.SECONDS.sleep(5);
					} catch (InterruptedException e) {
					}
				}
			}
		}

		System.out.println("Quit");
	}

	public static boolean serverStatus()
	{
		MineStat ms = new MineStat(floatingIP, 25565);
		int count = 0;

		if (!ms.isServerUp())
			return false;

		System.out.println("Server is online running version " + ms.getVersion());
		System.out.println("Message of the day: " + ms.getMotd());
		System.out.println("\n");
		while (count++ < 3) {
			if (ms.refresh()) {
				count = 0;
				System.out.print("\033[A\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
				System.out.print("\033[A\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
				System.out.println("Latency: " + ms.getLatency() + "ms");
				System.out.println(ms.getCurrentPlayers() + " out of " + ms.getMaximumPlayers() + " players");
			}
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
			}
		}

		System.out.println("Server is no longer online!");

		return true;
	}

	public static boolean doRun()
	{
		String apiToken = getFileLines("token.txt").replace("\n", "").replace("\r", "");
		Scanner in = new Scanner(System.in);

		System.out.println("Enter password:");
		String xorKey = in.nextLine();
		apiToken = xorHexstr(apiToken, xorKey);
		
		System.out.println("Connecting to DigitalOcean...");
		DigitalOcean client = new DigitalOceanClient("v2", apiToken);

		Droplet d = getServerDroplet(client, apiToken);

		if (d != null) {
			System.out.println("Received server droplet!");
			return true;
		} else {
			System.out.println("Failed to create droplet! (invalid password?)"); 
			return false;
		}
	}

	public static Droplet getServerDroplet(DigitalOcean client, String token)
	{
		String userData = String.format(getFileLines("initstub.sh"), volumeName, token);

		try {
			List<Droplet> droplets = client.getAvailableDroplets(0, 20).getDroplets();

			for (Droplet d : droplets) {
				for (String tag : d.getTags()) {
					if (tag.equals(dropletTag)) {
						System.out.println("Server already running...");
						return d;
					}
				}
			}

			System.out.println("Creating a new droplet...");
			System.out.println(dropletSize);

			Droplet newDroplet = new Droplet();
			newDroplet.setName(dropletName);
			newDroplet.setSize(dropletSize);
			newDroplet.setRegion(new Region(targetRegion));
			newDroplet.setImage(new Image(targetImage));
			newDroplet.setVolumeIds(Arrays.asList(volumeID));
			newDroplet.setTags(Arrays.asList(dropletTag));

			List<Key> keys = new ArrayList<Key>();
			keys.add(new Key(sshKey));
			newDroplet.setKeys(keys);

			newDroplet.setUserData(userData);

			System.out.println("Sending out data...");

			Droplet droplet = client.createDroplet(newDroplet);

			boolean fine = false;

			System.out.println("Waiting for setup to finish...");

			while (!fine) {
				fine = true;
				
				try {
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException e) {

				}
			
				List<Action> actions = client.getAvailableDropletActions(droplet.getId(), 0, 5).getActions();

				for (Action act : actions) {
					if (act.getStatus() == ActionStatus.IN_PROGRESS) {
						fine = false;
						break;
					}
				}
			}

			System.out.println("Assigning floating IP...");

			client.assignFloatingIP(droplet.getId(), floatingIP);
			return droplet;

		} catch (DigitalOceanException e) {
		} catch (RequestUnsuccessfulException e) {
		}

		return null;
	}
}
