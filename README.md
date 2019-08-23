# A set of scripts to quickly deploy a minecraft server on DigitalOcean

##### Setting up
Run tokenator.sh to generate a (slightly) encrypted token.txt file that gets embedded to the java client app.


Adjust droplet settings inside client/src/main/java/com/heep042/mcslauncher/App.java

This process will be improved soon.


Compile the java client with maven:
```
cd client
mvn compile assembly:single
```

Possibly turn the jar into windows console executable with a tool like Launch4J.

##### Client security notes
The java client and its password should only be shared with trusted individuals as it is possible to retreive your DigitalOcean API token with these 2 peaces of information.

The encrpytion process is very simple, we are simply running XOR through both peaces of data (token and password converted to base16). Thus, a password with a mix of letters, numbers and other characters would be preferred.
