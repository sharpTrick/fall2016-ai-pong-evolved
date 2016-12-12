package me.patrick.ai.pong.ai;

import me.patrick.ai.pong.model.GameModel;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.graph.ElementWiseVertex;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

/**
 * Created by patrick on 12/7/16.
 */
public class NeuralNetwork {
    private Map<String, INDArray> state = Collections.emptyMap();
    private static final ConcurrentHashMap<MultiLayerNetwork, ReadWriteLock> lockMap = new ConcurrentHashMap<>();

//    private static final ConcurrentHashMap<MultiLayerNetwork, LinkedBlockingQueue<DataSet>> trainingMap = new ConcurrentHashMap<>();
    private static final int PARAMETERS = 1;
    private static final int SIZE = 9;
    private static final int INPUT_SIZE = SIZE+SIZE*SIZE* PARAMETERS;
    //    private int paddleCount;
    private final MultiLayerNetwork network;

    private LinkedList<TrainingDatum> trainingQueue = new LinkedList<>();
    private final int paddle;


    private final int paddleCount;

    public NeuralNetwork(MultiLayerNetwork neuralNetwork, int paddleCount, int paddle) {
        network = neuralNetwork;
        lockMap.computeIfAbsent(network, k->new ReentrantReadWriteLock());
//        trainingMap.computeIfAbsent(network, k->new LinkedBlockingQueue<>());
        this.paddleCount = paddleCount;
        this.paddle = paddle;
    }

    public void logModel(GameModel model){
        GameModel clone = model.clone();
//        trainingQueue.clear();
        trainingQueue.addFirst(new TrainingDatum(clone));
    }

    private void addModelToInput(GameModel model, INDArray input, int row){
        double location = model.getPaddleLocation(paddle);
        int index = location<1?(int)(location*SIZE):SIZE-1;
        input.getRow(row).getColumn(index).addi(1);

        int ballCount = model.getBallCount();
        double ratio = 1d/ballCount;
        IntStream.range(0, ballCount).forEach(ball->{
            double x = model.getBallValue(ball, GameModel.BALL_X);
            double y = model.getBallValue(ball, GameModel.BALL_Y);

//            double xVel = model.getBallValue(ball, GameModel.BALL_VEL_X);
//            double yVel = model.getBallValue(ball, GameModel.BALL_VEL_Y);

            double distanceFromPaddle = paddle<2?x:y;
            double distanceAlongPaddleAxis = paddle<2?y:x;

//            double velocityTowardsPaddle = paddle<2?xVel:yVel;
//            double velocityAlongPaddleAxis = paddle<2?yVel:xVel;

            if(paddle%2==1){
                distanceFromPaddle=1-distanceFromPaddle;
//                velocityTowardsPaddle*=-1;
            }

            int i = distanceFromPaddle<1?(int)(distanceFromPaddle*SIZE):SIZE-1;
            int j = distanceAlongPaddleAxis<1?(int)(distanceAlongPaddleAxis*SIZE):SIZE-1;
            int column = (i*SIZE* PARAMETERS)+(j* PARAMETERS);

//            input.getRow(row).getColumn(SIZE+column+(velocityTowardsPaddle<0?0:1)).addi(ratio);
//            input.getRow(row).getColumn(SIZE+column+(velocityAlongPaddleAxis<0?2:3)).addi(ratio);
            input.getRow(row).getColumn(SIZE+column).addi(ratio);

        });
    }

    public void logDesiredPosition(int ball, int outputIndex, double desiredLocation){
        if(trainingQueue.isEmpty()) return;

        Iterator<TrainingDatum> iterator = trainingQueue.iterator();
        boolean done = false;
        while(iterator.hasNext() && !done){
            TrainingDatum td = iterator.next();
            if(td.contains(outputIndex)){
                done = true;
            }else{
                GameModel model = td.getModel();
                double paddleLocation = model.getPaddleLocation(paddle);
                double diff = desiredLocation - paddleLocation;
                if(Math.abs(diff)<1d/SIZE) diff=0;
                else diff=diff<0?-1:1;
                td.setDesiredPosition(outputIndex, diff);
            }
        }

        train();
        state = Collections.emptyMap();
    }
    private void train(){
        ArrayList<TrainingDatum> trainingData = new ArrayList<>();
        while (!trainingQueue.isEmpty() && trainingQueue.peekLast().isFull()){
            trainingData.add(trainingQueue.pollLast());
        }
        if(trainingData.isEmpty()) return;

        INDArray input = Nd4j.zeros(trainingData.size(), INPUT_SIZE);
        IntStream.range(0,trainingData.size()).forEach(i->
                addModelToInput(trainingData.get(i).getModel(), input, i)
        );

        INDArray labels = Nd4j.zeros(trainingData.size(), paddleCount);
        IntStream.range(0,trainingData.size()).forEach(i->
                trainingData.get(i).addDesiredPositionToLables(labels, i)
        );

        DataSet ds = new DataSet(input,labels);

//        LinkedBlockingQueue<DataSet> dataSetQueue = trainingMap.get(network);
//        dataSetQueue.add(ds);

        Lock lock = lockMap.get(network).writeLock();
//        try {
//            if(lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                try {
                    lock.lock();
//                    List<DataSet> dataSetList = new ArrayList<>();
//                    dataSetQueue.drainTo(dataSetList);

//                    if(!dataSetList.isEmpty()) {
//                        DataSet masterDS = DataSet.merge(dataSetList);
                    network.rnnSetPreviousState(0, state);
                    network.fit(ds);
                } finally {
                    lock.unlock();
                }
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        if (100000 < trainingQueue.size()) {
            System.out.println("cleared training cache");
            trainingQueue.clear();
        }
    }

    public double[] getValues(GameModel model){

        INDArray input = Nd4j.zeros(1, INPUT_SIZE);
        addModelToInput(model, input, 0);
        INDArray output;

        Lock lock = lockMap.get(network).writeLock();
        try {
            lock.lock();
//            network.rnnSetPreviousState(0, state);
            output = network.rnnTimeStep(input);
//            output = network.output(input);
            state=network.rnnGetPreviousState(0);
        }finally {
            lock.unlock();
        }

        return IntStream.range(0,paddleCount)
                .mapToDouble(paddle->output.getRow(0).getColumn(paddle).getDouble(0))
                .toArray();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////


    private class TrainingDatum {
        private final GameModel model;
        private Map<Integer, Double> desiredPosition = new TreeMap<>();

        public TrainingDatum(GameModel model) {
            this.model = model;
        }

        public GameModel getModel() {
            return model;
        }

        public boolean contains(int paddle) {
            return desiredPosition.containsKey(paddle);
        }

        public void setDesiredPosition(int paddle, double value) {
            desiredPosition.put(paddle, value);
        }

        public boolean isFull(){
            return desiredPosition.size() == paddleCount;
        }

        public void addDesiredPositionToLables(INDArray labels, int row){
            desiredPosition.forEach((paddle,position) -> labels.getRow(row).getColumn(paddle).addi(position));
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////

    public static MultiLayerNetwork buildNetwork(int outputs){
        int hiddenLayerSize = outputs*(SIZE*2);

        AtomicInteger layer = new AtomicInteger(0);

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(456)
                .iterations(1)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(0.1)
                .regularization(true).l2(1e-4)
//                .updater(Updater.NESTEROVS).momentum(0.9)
//                .miniBatch(false)
                .list()
                .layer(layer.getAndIncrement(), new  GravesLSTM.Builder()
                        .nIn(INPUT_SIZE)
                        .nOut(hiddenLayerSize)
                        .weightInit(WeightInit.XAVIER)
                        .activation("tanh")
                        .build()
//                ).layer(layer.getAndIncrement(), new  GravesLSTM.Builder()
//                        .nIn(hiddenLayerSize)
//                        .nOut(hiddenLayerSize)
//                        .weightInit(WeightInit.XAVIER)
//                        .activation("tanh")
//                        .build()
                ).layer(layer.getAndIncrement(), new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(hiddenLayerSize)
                        .nOut(outputs)
                        .weightInit(WeightInit.XAVIER)
                        .activation("identity")
                        .build()
                ).pretrain(false).backprop(true)
                .build();

        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();
        return network;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
}
