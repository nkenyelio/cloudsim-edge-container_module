package org.cloudbus.cloudsim.edgecloudsim.edge_container;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.NumberedFileInputSplit;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.InvocationType;
import org.deeplearning4j.optimize.api.TrainingListener;
import org.deeplearning4j.optimize.listeners.EvaluativeListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.learning.config.Nadam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.common.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EdgeML {

    private static final Logger log = LoggerFactory.getLogger(EdgeML.class);

    //'baseDir: Base directory for the data. Change this if you want to save the data somewhere else

    private static File baseDir = new File("src/main/resources/mhealth");
    private static File baseTrainDir = new File(baseDir, "train");
    private static File featuresDirTrain = new File(baseTrainDir, "features");
    private static File labelsDirTrain = new File(baseTrainDir, "labels");
    private static File baseTestDir = new File(baseDir, "test");
    private static File featuresDirTest = new File(baseTestDir, "features");
    private static File labelsDirTest = new File(baseTestDir, "labels");




    public static void mainHelper(String[] args) throws Exception {

        //downloadUCIData();

        SequenceRecordReader trainFeatures = new CSVSequenceRecordReader();
        trainFeatures.initialize(new NumberedFileInputSplit(featuresDirTrain.getAbsolutePath() + "/%d.csv", 0, 449));
        SequenceRecordReader trainLabels = new CSVSequenceRecordReader();
        trainLabels.initialize(new NumberedFileInputSplit(labelsDirTrain.getAbsolutePath() + "/%d.csv", 0, 449));
        int miniBatchSize = 10;
        int numLabelClasses = 6;
        DataSetIterator trainData = new SequenceRecordReaderDataSetIterator(trainFeatures, trainLabels, miniBatchSize, numLabelClasses, false, SequenceRecordReaderDataSetIterator.AlignmentMode.ALIGN_END);
        DataNormalization normalizer = new NormalizerStandardize();
        normalizer.fit(trainData);
        trainData.reset();
        trainData.setPreProcessor(normalizer);
        SequenceRecordReader testFeatures = new CSVSequenceRecordReader();
        testFeatures.initialize(new NumberedFileInputSplit(featuresDirTest.getAbsolutePath() + "/%d.csv", 0, 149));
        SequenceRecordReader testLabels = new CSVSequenceRecordReader();
        testLabels.initialize(new NumberedFileInputSplit(labelsDirTest.getAbsolutePath() + "/%d.csv", 0, 149));
        DataSetIterator testData = new SequenceRecordReaderDataSetIterator(testFeatures, testLabels, miniBatchSize, numLabelClasses, false, SequenceRecordReaderDataSetIterator.AlignmentMode.ALIGN_END);
        testData.setPreProcessor(normalizer);
        MultiLayerConfiguration conf = (new NeuralNetConfiguration.Builder()).seed(123L).weightInit(WeightInit.XAVIER).updater(new Nadam()).gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(0.5D).list().layer(((org.deeplearning4j.nn.conf.layers.LSTM.Builder) ((org.deeplearning4j.nn.conf.layers.LSTM.Builder) ((org.deeplearning4j.nn.conf.layers.LSTM.Builder) (new org.deeplearning4j.nn.conf.layers.LSTM.Builder()).activation(Activation.TANH)).nIn(1)).nOut(10)).build()).layer(((org.deeplearning4j.nn.conf.layers.RnnOutputLayer.Builder) ((org.deeplearning4j.nn.conf.layers.RnnOutputLayer.Builder) ((org.deeplearning4j.nn.conf.layers.RnnOutputLayer.Builder) (new org.deeplearning4j.nn.conf.layers.RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)).activation(Activation.SOFTMAX)).nIn(10)).nOut(numLabelClasses)).build()).build();
        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        log.info("Starting training...");
        net.setListeners(new TrainingListener[]{new ScoreIterationListener(20), new EvaluativeListener(testData, 1, InvocationType.EPOCH_END)});
        int nEpochs = 40;
        net.fit(trainData, nEpochs);
        log.info("Evaluating...");
        Evaluation eval = net.evaluate(testData);
        log.info(eval.stats());
        log.info("----- Activities categorization Complete -----");

    }
}
