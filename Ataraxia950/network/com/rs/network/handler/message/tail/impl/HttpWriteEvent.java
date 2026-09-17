package com.rs.network.handler.message.tail.impl;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.network.codec.ProtocolSet;
import com.rs.network.codec.ResultMessage;
import com.rs.network.codec.handshake.HandshakeType;
import com.rs.network.codec.update.JS5UpdateHelper;
import com.rs.utils.NXTClientsManager;
import com.rs.utils.NXTClientsManager.NXTClient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class HttpWriteEvent extends HandshakeWriteEvent {
    private String location, host;
    private String[] data;

    public HttpWriteEvent(String location, String host, String[] data) {
        super(HandshakeType.UPDATE_CONNECTION_WEB, ResultMessage.SUCCESS);
        this.location = location;
        this.host = host;
        this.data = data;
    }

    public ByteBuf getOutPutData() {
//        System.out.println("location="+location);
        if (location.startsWith("ms")) {
            int m = Integer.parseInt(getVar(data, "m"));
            int indexId = Integer.parseInt(getVar(data, "a"));
            int archiveId = Integer.parseInt(getVar(data, "g"));

            if (m != 0 || archiveId < 0) {
                return null;
            }
            if (indexId != 255 && (Cache.STORE.getIndexes().length <= indexId || Cache.STORE.getIndexes()[indexId] == null || !Cache.STORE.getIndexes()[indexId].archiveExists(archiveId))) {
                return null;
            }
            if (indexId == 255 && archiveId == 255) {
                Long.parseLong(getVar(data, "cb"));
            } else {
                int crc = Integer.parseInt(getVar(data, "c"));
                int revision = Integer.parseInt(getVar(data, "v"));
                if (crc != Cache.STORE.getIndexes()[indexId].getTable().getArchives()[archiveId].getCRC() || revision != Cache.STORE.getIndexes()[indexId].getTable().getArchives()[archiveId].getRevision()) {
                    return null;
                }
            }
            byte[] archive = JS5UpdateHelper.get(indexId, archiveId, true);
            if (archive == null) {
//                System.out.println("null web grab: indexId="+indexId+", archiveId="+archiveId+"");
                return null;
            }
            int length = ((archive[1] & 0xff) << 24) + ((archive[2] & 0xff) << 16) + ((archive[3] & 0xff) << 8) + (archive[4] & 0xff) + 5;
            if (archive[0] != 0)
                length += 4;
            ByteBuf out = Unpooled.buffer(5 + length);
            // header
            out.writeBytes(("HTTP/1.1 200 OK" + "\n" + "Content-Type: application/octet-stream" + "\n" + "Content-Length: " + length + "\r\n\r\n").getBytes());
            // content
            out.writeBytes(archive, 0, length);

            return out;
        }
        if (location.startsWith("jav_config.ws")) {
            int binaryType = Integer.parseInt(getVar(data, "binaryType", "2"));
            NXTClient client = NXTClientsManager.getClientByType(binaryType);
            String body = client == null || client.getJavConfig() == null ? "Couldnt find client config for binary type " + binaryType : new String(client.getJavConfig());
            body = body.replace("title=", "title=" + Settings.SERVER_NAME + " " + ProtocolSet.REVISION + " | NXT " + (host.contains("127.0.0.1") ? "BETA" : "LIVE"));
            body = body.replace("codebase=", "codebase=http://" + host + "/");
            body = body.replace("binary_name=", "binary_name=" + client.getClientNames()[1]);
            body = body.replace("binary_count=", "binary_count=" + client.getBinaryCount());
            for (int i = 0; i < client.getBinaryCount(); i++) {
                body = body.replace("download_name_" + i + "=", "download_name_" + i + "=" + client.getDownloadName(i));
                body = body.replace("download_crc_" + i + "=", "download_crc_" + i + "=" + client.getCrcs()[i]);
                body = body.replace("download_hash_" + i + "=", "download_hash_" + i + "=" + client.getHashs()[i]);
            }
            body = body.replaceAll("RuneScape", Settings.SERVER_NAME);
            body = body.replaceAll("=43594", "=" + Settings.SERVER_PORT);
            body = body.replaceAll("=443", "=" + Settings.SERVER_PORT);
            body = body.replaceAll("=80", "=" + Settings.SERVER_PORT);
            body = body.replaceAll("=http://world1.runescape.com", "=http://" + host.replace(":" + Settings.SERVER_PORT + "", ""));
            body = body.replaceAll("content.runescape.com", host.replace(":" + Settings.SERVER_PORT + "", ""));
            body = body.replace("param=1=", "param=1=" + ProtocolSet.WORLD_TOKEN);
            body = body.replace("param=35=", "param=35=" + ProtocolSet.SETTINGS);
            body = body.replace("param=11=", "param=11=" + ProtocolSet.CLIENT_LOGIN_ID);
            body = body.replace("param=6=", "param=6=" + ProtocolSet.TOKEN);
            body = body.replace("param=43=", "param=43=" + host.replace(":" + Settings.SERVER_PORT + "", ""));
            body = body.replace("localhost", "127.0.0.1");
            ByteBuf out = Unpooled.buffer();
            out.writeBytes(("HTTP/1.1 200 OK" + "\n" + "Content-Type: text/plain; charset=ISO-8859-1" + "\n" + "Server: JAGeX/3.1" + "\n" + "Content-Length: " + body.getBytes().length + "\r\n\r\n").getBytes());
            out.writeBytes(body.getBytes());
            return out;
        }
        if (location.startsWith("client")) {
            int binaryType = Integer.parseInt(getVar(data, "binaryType", "0"));
            String fileName = getVar(data, "fileName", "");
            long crc = Long.parseLong(getVar(data, "crc", ""));
            NXTClient client = NXTClientsManager.getClientByType(binaryType);
            if (client == null) {
                String body = "Couldnt find " + fileName + " for binary type " + binaryType + "";
                ByteBuf out = Unpooled.buffer();
                out.writeBytes(("HTTP/1.1 200 OK" + "\n" + "Content-Type: text/plain; charset=ISO-8859-1" + "\n" + "Server: JAGeX/3.1" + "\n" + "Content-Length: " + body.getBytes().length + "\r\n\r\n").getBytes());
                out.writeBytes(body.getBytes());
                return out;
            }
            int downloadIndex = client.getDownloadIndex(fileName);
            if (downloadIndex == -1 || client.getBinary(downloadIndex) == null || client.getCrcs()[downloadIndex] != crc) {
                String body = "Couldnt find " + fileName + " for binary type " + binaryType + "";
                ByteBuf out = Unpooled.buffer();
                out.writeBytes(("HTTP/1.1 200 OK" + "\n" + "Content-Type: text/plain; charset=ISO-8859-1" + "\n" + "Server: JAGeX/3.1" + "\n" + "Content-Length: " + body.getBytes().length + "\r\n\r\n").getBytes());
                out.writeBytes(body.getBytes());
                return out;
            }
            ByteBuf out = Unpooled.buffer();
            out.writeBytes(("HTTP/1.1 200 OK" + "\n" + "Content-Type: application/octet-stream" + "\n" + "Server: JAGeX/3.1" + "\n" + "Content-Disposition: filename=" + fileName + "\n" + "Content-Length: " + client.getBinary(downloadIndex).length + "\r\n\r\n").getBytes());
            out.writeBytes(client.getBinary(downloadIndex));
            return out;
        }
        String body = "hello World";
        ByteBuf out = Unpooled.buffer();
        out.writeBytes(("HTTP/1.1 200 OK" + "\n" + "Content-Type: text/plain; charset=ISO-8859-1" + "\n" + "Server: JAGeX/3.1" + "\n" + "Content-Length: " + body.getBytes().length + "\r\n\r\n").getBytes());
        out.writeBytes(body.getBytes());
        return out;
    }

    private String getVar(String[] data, String var) {
        return getVar(data, var, "-1");
    }

    private String getVar(String[] data, String var, String defaultValue) {
        for (String param : data) {
            String[] v = param.split("=");
            if (v[0].equals(var))
                return v[1];
        }
        return defaultValue;
    }

}
