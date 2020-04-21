package com.nanjunxing.voice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.effects.DelayEffect;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.AudioPlayer;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.resample.RateTransposer;

public class Main {
	public static void main(String[] args) throws Exception {
		//		SoundEnum s = SoundEnum.ZHNGHANG;
		SoundEnum s = SoundEnum.LUOLI;
		//		SoundEnum s = SoundEnum.DASHU;
		//		SoundEnum s = SoundEnum.GAOGUAI;
		//		SoundEnum s = SoundEnum.WANGHONGNV;
		byte[] pcmBytes = s.run("F://voiceDemo.mp3");
		//		byte[] pcmBytes = speechPitchShiftMp3("F://voiceDemo.mp3", 0.6,0.7);
		InputStream wavOutPut2  = new ByteArrayInputStream(pcmBytes);
		TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(16000,16,1,true,false);
		AudioInputStream inputStream = new AudioInputStream(wavOutPut2, JVMAudioInputStream.toAudioFormat(format),AudioSystem.NOT_SPECIFIED);
		JVMAudioInputStream stream = new JVMAudioInputStream(inputStream);
		AudioDispatcher dispatcher = new AudioDispatcher(stream, 1024 ,0);
		dispatcher.addAudioProcessor(new AudioPlayer(format,1024));
		dispatcher.run();
	}

	/**
	 * ����
	 * @param speedFactor ������ (0,2) ����1Ϊ�ӿ����٣�С��1Ϊ��������
	 * @param rateFactor �����仯�� (0,2) ����1Ϊ�����������������С��1Ϊ��������������
	 * @return �������MP3����������
	 */
	public static byte[] speechPitchShiftMp3(String fileUrl, double rateFactor, double speedFactor) throws IOException, UnsupportedAudioFileException {

		WaveformSimilarityBasedOverlapAdd w = new WaveformSimilarityBasedOverlapAdd(WaveformSimilarityBasedOverlapAdd.Parameters.speechDefaults(rateFactor, 16000));
		int inputBufferSize = w.getInputBufferSize();
		int overlap = w.getOverlap();

		AudioDispatcher dispatcher = AudioDispatcherFactory.fromPipe(fileUrl,16000,inputBufferSize,overlap);
		w.setDispatcher(dispatcher);
		dispatcher.addAudioProcessor(w);

		/** ������ת������ ʹ�ò�ֵ���Ĳ�����, ��ʱ��������һ�����������ת���� **/
		dispatcher.addAudioProcessor(new RateTransposer(speedFactor));
		AudioOutputToByteArray out = new AudioOutputToByteArray();


		/** ��������ת���� -- ʧ�� **/
		/*SoundTouchRateTransposer soundTouchRateTransposer = new SoundTouchRateTransposer(2);
	        soundTouchRateTransposer.setDispatcher(dispatcher);
	        dispatcher.addAudioProcessor(soundTouchRateTransposer);*/

		/** ���Ҳ������� -- �޷�Ӧ **/
		/*SineGenerator sineGenerator = new SineGenerator(0.5, 0.5);
	        dispatcher.addAudioProcessor(sineGenerator);*/

		/** ����ת���� -- ��Ч�� **/
		//	        dispatcher.addAudioProcessor(new PitchShifter(0.1,16000,448,overlap));

		/** ������ʹ�ÿ����ϳɻط����������������ڿ��Ʋ������ʣ����ߣ�������С�� -- ��Ч�� **/
		//	        dispatcher.addAudioProcessor(new OptimizedGranulator(16000, 448));

		/** ���������� -- ��Ч�� **/
		//	        dispatcher.addAudioProcessor(new NoiseGenerator(0.2   ));

		/** ���洦����  ����Ϊ1�������κη�Ӧ�� �������1��ʾ��������a -- �з�Ӧ **/
		//	        dispatcher.addAudioProcessor(new GainProcessor(10));

		/**���Ч�� -- �з�Ӧ **/
		//	        dispatcher.addAudioProcessor(new FlangerEffect(64, 0.3, 16000, 16000));// ����Ч��
		//	        dispatcher.addAudioProcessor(new FlangerEffect(1 << 4, 0.8, 8000, 2000));// ��ð
		//	        dispatcher.addAudioProcessor(new ZeroCrossingRateProcessor());//��ð

		/** ���� --����������С **/
		//	        dispatcher.addAudioProcessor(new FadeOut(5));

		/** ����-- ����������� **/
		//	        dispatcher.addAudioProcessor(new FadeIn(5));

		/** ���ź�����ӻ���Ч����echoLength����Ϊ��λ  elay������˥��������0��1֮���ֵ��1��ʾ��˥����0��ʾ����˥�� **/
		dispatcher.addAudioProcessor(new DelayEffect(0.2, 0.24, 12000) );

		/** �������� -- ������ת��Ϊ����**/
		//	        dispatcher.addAudioProcessor(new AmplitudeModulatedNoise());

		/** ���LFO -- �������� **/
		//	        dispatcher.addAudioProcessor(new AmplitudeLFO());

		dispatcher.addAudioProcessor(out);

		dispatcher.run();



		//	        return new ByteArrayInputStream(out.getData());
		return out.getData();
	}


	public static byte[] pcm2wav(byte[] bytes) throws IOException {
		//��������������ʵȵȡ������õ���16λ������ 8000 hz
		WaveHeader header = new WaveHeader();

		//�����ֶ� = ���ݵĴ�С��PCMSize) + ͷ���ֶεĴ�С(������ǰ��4�ֽڵı�ʶ��RIFF�Լ�fileLength�����4�ֽ�)
		header.fileLength = bytes.length + (44 - 8);
		header.FmtHdrLeth = 16;
		header.BitsPerSample = 16;
		header.Channels = 1;
		header.FormatTag = 0x0001;
		header.SamplesPerSec = 16000;
		header.BlockAlign = (short)(header.Channels * header.BitsPerSample / 8);
		header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec;
		header.DataHdrLeth = bytes.length;

		byte[] h = header.getHeader();
		assert h.length == 44; //WAV��׼��ͷ��Ӧ����44�ֽ�
		return h;
	}


}
