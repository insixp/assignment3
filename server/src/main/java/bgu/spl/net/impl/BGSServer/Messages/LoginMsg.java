package bgu.spl.net.impl.BGSServer.Messages;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.BGSServer.Database;
import bgu.spl.net.impl.BGSServer.User;

import java.nio.charset.StandardCharsets;
import java.util.Queue;

public class LoginMsg extends Message{
    private String  username;
    private String  password;
    private byte    captcha;

    public LoginMsg(){
        super(MessageCode.LOGIN.OPCODE);
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public byte getCaptcha() { return captcha; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setCaptcha(byte captcha) { this.captcha = captcha; }

    public void process(){
        User user = db.get(this.username);
        if(user == null || this.captcha == 0 || user.getLogged_in()){
            this.sendError();
        } else {
            if(!user.getPassword().equals(this.password)){
                this.sendError();
            } else {
                user.setLogged_in(true);
                user.setConnectionID(this.connId);
                AckMsg ackMsg = new AckMsg();
                ackMsg.setMsgOpCode(this.opcode);
                this.connections.send(connId, ackMsg);
                this.sendBackup(user);
            }
        }
    }

    @Override
    public byte[] serialize() {
        return this.StringtoByte(shortToString(opcode) + this.username + '\0' + this.password + '\0' + this.captcha);
    }

    @Override
    public void decodeNextByte(byte data) {
        if(this.content_index == 0) {
            this.username = this.bytesToString(data);
            if(data == '\0')
                this.content_index++;
        }
        else if(this.content_index == 1) {
            this.password = this.bytesToString(data);
            if(data == '\0')
                this.content_index++;
        }
        else if(this.content_index == 2) {
            this.captcha = data;
            this.content_index++;
        }
        else {
            this.data.add(data);
        }
    }
    private void sendBackup(User user){
        Queue<NotificationMsg> q = user.getBackupMesseges();
        while(!q.isEmpty()&&user.getLogged_in()){
            this.connections.send(this.connId,q.poll());
        }
    }

}
