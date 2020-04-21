package com.nanjunxing.voice;



import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.ZeroCrossingRateProcessor;
import be.tarsos.dsp.effects.DelayEffect;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.resample.RateTransposer;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

public enum SoundEnum {
	ZHNGHANG(1, 1, "����", 0, dispatcher -> {}),
    LUOLI(0.6, 0.6, "����", 1, dispatcher -> {}),
    DASHU(1.2, 1.2, "����", 2, dispatcher -> {}),
    FEIZAI(1.5, 1.5, "����", 3, dispatcher -> {}),
    GAOGUAI(1.5, 0.8, "���", 4, dispatcher -> {}),
    XIONGHAIZI(0.73, 0.73, "�ܺ���", 5, dispatcher -> {}),
    MANTUNTUN(0.35,1, "������",6 , dispatcher -> {}),
    WANGHONGNV(1.2,0.7, "����Ů",7 , dispatcher -> {}),

    /**
     * dispatcher.addAudioProcessor(new DelayEffect(0.2, 0.24, 12000) );
     */
    KUNSHOU(1.55,1.55, "����", 8, dispatcher -> dispatcher.addAudioProcessor(new DelayEffect(0.2, 0.24, 12000))),

    /**
     * dispatcher.addAudioProcessor(new DelayEffect(0.2, 0.24, 12000) );
     */
    ZHONGJIXIE(1.50,1.50, "�ػ�е", 9, dispatcher -> dispatcher.addAudioProcessor(new DelayEffect(0.2, 0.24, 12000))),

    /**
     *          dispatcher.addAudioProcessor(new FlangerEffect(1 << 4, 0.8, 8000, 2000));
     *         dispatcher.addAudioProcessor(new ZeroCrossingRateProcessor());
     */
    GANMAO(1.05,1.05, "��ð", 10, dispatcher -> {
        dispatcher.addAudioProcessor(new DelayEffect(0.2, 0.24, 12000));
        dispatcher.addAudioProcessor(new ZeroCrossingRateProcessor());
    }),

    /**
     *          dispatcher.addAudioProcessor(new DelayEffect(0.8, 0.5, 12000) );
     *         dispatcher.addAudioProcessor(new DelayEffect(0.5, 0.3, 8000) );
     */
    KONGLING(1, 1, "����", 11, dispatcher -> {
        dispatcher.addAudioProcessor(new DelayEffect(0.8, 0.5, 12000) );
        dispatcher.addAudioProcessor(new DelayEffect(0.5, 0.3, 8000) );
    });

    /**
     * @param speedFactor ������ (0,2) ����1Ϊ�ӿ����٣�С��1Ϊ��������
     * @param rateFactor �����仯�� (0,2) ����1Ϊ�����������������С��1Ϊ��������������
     */
    SoundEnum(double rateFactor, double speedFactor, String name, int type, Consumer<AudioDispatcher> consumer){
        this.rateFactor = rateFactor;
        this.speedFactor = speedFactor;
        this.name = name;
        this.type = type;
        this.consumer = consumer;
    }
    private double rateFactor;
    private double speedFactor;
    private String name;
    private int type;
    private Consumer consumer;


    public byte[] run(String fileUrl){
        WaveformSimilarityBasedOverlapAdd w = new WaveformSimilarityBasedOverlapAdd(WaveformSimilarityBasedOverlapAdd.Parameters.speechDefaults(rateFactor, 16000));
        int inputBufferSize = w.getInputBufferSize();
        int overlap = w.getOverlap();

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromPipe(fileUrl,16000,inputBufferSize,overlap);
        w.setDispatcher(dispatcher);
        dispatcher.addAudioProcessor(w);

        /** ������ת������ ʹ�ò�ֵ���Ĳ�����, ��ʱ��������һ�����������ת���� **/
        dispatcher.addAudioProcessor(new RateTransposer(speedFactor));
        AudioOutputToByteArray out = new AudioOutputToByteArray();



        consumer.accept(dispatcher);

        dispatcher.addAudioProcessor(out);
        dispatcher.run();


        return out.getData();
    }


    public static byte[] pcm2wav(byte[] bytes) {
        try {
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
        } catch (IOException e) {
            System.out.println("pcm2wav-error");
        }
        return null;
    }


    public static Optional<SoundEnum> getInstance(int type){
        for (int i = 0; i < SoundEnum.values().length; i++) {
            if(SoundEnum.values()[i].type == type)
                return Optional.of(SoundEnum.values()[i]);
        }
        return Optional.empty();
    }
}
