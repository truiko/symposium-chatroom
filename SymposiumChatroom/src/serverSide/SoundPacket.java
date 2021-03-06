package serverSide;

import java.io.Serializable;
import javax.sound.sampled.AudioFormat;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * some sound
 * @author dosse
 */
public class SoundPacket implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 4459912361165140477L;
	public static AudioFormat defaultFormat=new AudioFormat(11025f, 8, 1, true, true); //11.025khz, 8bit, mono, signed, big endian (changes nothing in 8 bit) ~8kb/s
    public static int defaultDataLength=1200; //send 1200 samples/packet by default
    private byte[] data; //actual data. if null, comfort noise will be played

    public SoundPacket(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
    
}