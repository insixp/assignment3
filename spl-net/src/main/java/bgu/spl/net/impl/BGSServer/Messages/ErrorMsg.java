package bgu.spl.net.impl.BGSServer.Messages;

import bgu.spl.net.impl.BGSServer.Database;

public class ErrorMsg extends Message{
    public short    msgOpCode;

    private byte[] buff;

    public ErrorMsg(){
        super(MessageCode.ERROR.OPCODE);
    }

    public Message process(Database db){
        return null;
    }

    @Override
    public byte[] serialize() {
        return this.StringtoByte(shortToString(opcode) + shortToString(this.msgOpCode));
    }

    @Override
    public void decodeNextByte(byte data) {
        if(this.content_index == 0) {
            this.buff = new byte[2];
            this.buff[0] = data;
            this.content_index++;
        }
        else if(this.content_index == 1){
            this.buff[1] = data;
            this.msgOpCode = this.bytesToShort(this.buff);
        }
        else {
            this.data.add(data);
        }
    }


}
