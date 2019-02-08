package net.prezz.mpr.model.servers;

import java.io.Serializable;

import net.prezz.mpr.Utils;

public class ServerConfiguration implements Serializable {

    private static final long serialVersionUID = -8757270537389863648L;

    private int id;
    private String name;
    private String host;
    private String port;
    private String password;
    private String output;
    private String streaming;


    public ServerConfiguration(String name, String host, String port, String password, String output, String streaming) {
        this(0, name, host, port, password, output, streaming);
    }

    public ServerConfiguration(int id, String name, String host, String port, String password, String output, String streaming) {
        this.id = id;
        this.name = name;
        this.host = host;
        this.port = port;
        this.password = password;
        this.output = output;
        this.streaming = streaming;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public String getOutput() {
        return output;
    }

    public String getStreaming() {
        return streaming;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof ServerConfiguration) {
            ServerConfiguration other = (ServerConfiguration) obj;

            if (id != other.id) {
                return false;
            }

            if (!Utils.equals(name, other.name)) {
                return false;
            }

            if (!Utils.equals(host, other.host)) {
                return false;
            }

            if (!Utils.equals(port, other.port)) {
                return false;
            }

            if (!Utils.equals(password, other.password)) {
                return false;
            }

            if (!Utils.equals(output, other.output)) {
                return false;
            }

            if (!Utils.equals(streaming, other.streaming)) {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = 31 * hash + id;
        hash = 31 * hash + Utils.hashCode(name);
        hash = 31 * hash + Utils.hashCode(host);
        hash = 31 * hash + Utils.hashCode(port);
        hash = 31 * hash + Utils.hashCode(password);
        hash = 31 * hash + Utils.hashCode(output);
        hash = 31 * hash + Utils.hashCode(streaming);

        return hash;
    }
}
