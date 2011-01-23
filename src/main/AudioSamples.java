package main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class AudioSamples {
	private short[] audioData;
	private AudioFormat audioFormat;
	private AudioFileFormat.Type fileType;
	private boolean initializedOk;
	
	/**
	 * audioData max value:  32767
	 * audioData min value: -32768
	 */
	
	public AudioSamples(File file) {
		initializedOk = false;
		AudioInputStream audioInputStream = null;
		AudioFileFormat fileFormat = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(file);
			fileFormat = AudioSystem.getAudioFileFormat(file);
		} catch(Exception e) {
			Util.get().out(e.toString(), Util.VERBOSITY_ERROR);
			return;
		}
		fileType = fileFormat.getType();
		audioFormat = audioInputStream.getFormat();
		if (
				audioFormat.getChannels() == 1 &&
				audioFormat.isBigEndian() == false &&
				audioFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED &&
				audioFormat.getFrameSize() == 2
		) {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int bytesRead;
			try {
				Util.get().out("Reading file " + file.getAbsolutePath() + " into byte stream.", Util.VERBOSITY_DEBUG_INFO);
				while ((bytesRead = audioInputStream.read(buffer)) != -1) {
					byteArrayOutputStream.write(buffer, 0, bytesRead);
					Util.get().out("Read " + bytesRead + " bytes.", Util.VERBOSITY_DETAILED_DEBUG_INFO);
				}
			} catch(Exception e) {
				Util.get().out(e.toString(), Util.VERBOSITY_ERROR);
				return;
			}
			Util.get().out("Getting raw bytes from byte stream.", Util.VERBOSITY_DEBUG_INFO);
			byte[] audioByteArray = byteArrayOutputStream.toByteArray();
			audioData = new short[audioByteArray.length / 2];
			
			Util.get().out("Converting " + audioByteArray.length + " raw bytes to " + audioData.length + " 16-bit-samples.", Util.VERBOSITY_DEBUG_INFO);
			for (int i = 0; i < audioByteArray.length; i += 2) {
				audioData[i / 2] = (short)
						( (audioByteArray[i + 0] & 0xFF)
						| (audioByteArray[i + 1] << 8)
				);
			}
			initializedOk = true;
		} else {
			Util.get().out(
					"Error: Audio format not supported:\n" +
					"Channels: " + audioFormat.getChannels() + ", " +
					(audioFormat.isBigEndian() ? "big" : "little") + " endian, " +
					audioFormat.getEncoding() + ", " +
					"Frame size: " + audioFormat.getFrameSize() +
					"\nSupported:\n" +
					"Channels: 1, little endian, PCM_SIGNED, Frame size: 2\n",
					Util.VERBOSITY_ERROR);
		}
	}
	
	public boolean initializedOK() {
		return initializedOk;
	}
	
	public boolean writeToFile(File file) {
		byte[] audioByteArray = new byte[audioData.length * 2];
		Util.get().out("Converting " + audioData.length + " 16-bit-samples to " + audioByteArray.length + " raw bytes.", Util.VERBOSITY_DEBUG_INFO);
		for (int i = 0; i < audioByteArray.length; i += 2) {
			audioByteArray[i + 0] = (byte)(audioData[i / 2] & 0xFF); // low byte
			audioByteArray[i + 1] = (byte)((audioData[i / 2] >> 8) & 0xFF); // high byte
		}
		
		Util.get().out("Getting byte stream from raw bytes.", Util.VERBOSITY_DEBUG_INFO);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioByteArray);
		AudioInputStream audioInputStream = new AudioInputStream(
				byteArrayInputStream,
				audioFormat,
				audioByteArray.length / audioFormat.getFrameSize()
		);
		int bytesWritten = 0;
		Util.get().out("Writing byte stream to file " + file.getAbsolutePath(), Util.VERBOSITY_DEBUG_INFO);
		try {
			bytesWritten = AudioSystem.write(audioInputStream, fileType, file);
		} catch(Exception e) {
			Util.get().out(e.toString(), Util.VERBOSITY_ERROR);
			return false;
		}
		Util.get().out("Bytes written to file " + file.getName() + ": " + bytesWritten, Util.VERBOSITY_DEBUG_INFO);
		return true;
	}
	
	public void removeDCOffset() {
		int offset = 0;
		
		// Find DC offset
		Util.get().outNoLN("Finding DC offset: ", Util.VERBOSITY_DEBUG_INFO);
		for (int i = 0; i < audioData.length; i++) {
			offset += audioData[i];
		}
		offset /= audioData.length;
		Util.get().out(offset + "", Util.VERBOSITY_DEBUG_INFO);
		
		// Remove DC offset
		Util.get().out("Removing DC offset.", Util.VERBOSITY_DEBUG_INFO);
		for (int i = 0; i < audioData.length; i++) {
			audioData[i] -= offset;
		}
	}
	
	public void makeFadeInFadeOut() {
		int fadeSamples = 1000;
		Util.get().out("Doing fade-in and fade-out on first and last " + fadeSamples + " samples.", Util.VERBOSITY_DEBUG_INFO);
		if (audioData.length > fadeSamples * 2) {
			// Do fade-in
			for (int i = 0; i < fadeSamples; i++) {
				// Fade-in:
				audioData[i] =
					(short)(audioData[i] *
					(i / (double)fadeSamples));
				// Fade-out:
				audioData[audioData.length - 1 - i] =
					(short)(audioData[audioData.length - 1 - i] *
					(i / (double)fadeSamples));
			}
		} else {
			Util.get().out("Audio is too short to do fade in/out. Ignoring.", Util.VERBOSITY_WARNING);
		}
	}
	
	public void normalizeVolume() {
		// Find highest note (absolute)
		short normalizedValue = 16385;
		short max = 0;
		Util.get().outNoLN("Volume normalize: Finding highest note: ", Util.VERBOSITY_DEBUG_INFO);
		for (int i = 0; i < audioData.length; i++) {
			short tmp = (short)Math.abs(audioData[i]);
			if (tmp > max)
				max = tmp;
		}
		Util.get().out(max + "", Util.VERBOSITY_DEBUG_INFO);
		Util.get().out("Normalizing to " + normalizedValue, Util.VERBOSITY_DEBUG_INFO);
		for (int i = 0; i < audioData.length; i++) {
			Util.get().outNoLN("Old value: " + audioData[i], Util.VERBOSITY_DETAILED_DEBUG_INFO);
			audioData[i] = (short)((double)audioData[i] / (double)max * (double)normalizedValue);
			Util.get().out(", new value: " + audioData[i], Util.VERBOSITY_DETAILED_DEBUG_INFO);
		}
	}
	
	public void substractHalf() {
		for (int i = 0; i < audioData.length; i++) {
			audioData[i] -= 32767;
		}
	}
}
