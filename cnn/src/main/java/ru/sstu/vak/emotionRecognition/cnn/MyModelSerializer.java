package ru.sstu.vak.emotionRecognition.cnn;

import lombok.NonNull;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.api.Updater;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.serializer.NormalizerSerializer;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class MyModelSerializer {
    private static final Logger log = LogManager.getLogger(MyModelSerializer.class.getName());

    private MyModelSerializer() {
    }


    public static void writeModel(@NonNull Model model, @NonNull String path, boolean saveUpdater) throws IOException {
        if (model == null) {
            throw new NullPointerException("model is marked @NonNull but is null");
        } else if (path == null) {
            throw new NullPointerException("path is marked @NonNull but is null");
        } else {
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(path));
            Throwable var4 = null;

            try {
                writeModel(model, (OutputStream) stream, saveUpdater);
            } catch (Throwable var13) {
                var4 = var13;
                throw var13;
            } finally {
                if (stream != null) {
                    if (var4 != null) {
                        try {
                            stream.close();
                        } catch (Throwable var12) {
                            var4.addSuppressed(var12);
                        }
                    } else {
                        stream.close();
                    }
                }

            }

        }
    }

    public static void writeModel(@NonNull Model model, @NonNull OutputStream stream, boolean saveUpdater) throws IOException {
        if (model == null) {
            throw new NullPointerException("model is marked @NonNull but is null");
        } else if (stream == null) {
            throw new NullPointerException("stream is marked @NonNull but is null");
        } else {
            writeModel(model, (OutputStream) stream, saveUpdater, (DataNormalization) null);
        }
    }

    public static void writeModel(@NonNull Model model, @NonNull OutputStream stream, boolean saveUpdater, DataNormalization dataNormalization) throws IOException {
        if (model == null) {
            throw new NullPointerException("model is marked @NonNull but is null");
        } else if (stream == null) {
            throw new NullPointerException("stream is marked @NonNull but is null");
        } else {
            ZipOutputStream zipfile = new ZipOutputStream(new CloseShieldOutputStream(stream));
            String json = "";
            if (model instanceof MultiLayerNetwork) {
                json = ((MultiLayerNetwork) model).getLayerWiseConfigurations().toJson();
            } else if (model instanceof ComputationGraph) {
                json = ((ComputationGraph) model).getConfiguration().toJson();
            }

            ZipEntry config = new ZipEntry("configuration.json");
            zipfile.putNextEntry(config);
            zipfile.write(json.getBytes());
            ZipEntry coefficients = new ZipEntry("coefficients.bin");
            zipfile.putNextEntry(coefficients);
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(zipfile));
            INDArray params = model.params();
            ZipEntry nEntry;
            if (params != null) {
                try {
                    Nd4j.write(model.params(), dos);
                } finally {
                    dos.flush();
                }
            } else {
                nEntry = new ZipEntry("noParams.marker");
                zipfile.putNextEntry(nEntry);
            }

            if (saveUpdater) {
                INDArray updaterState = null;
                if (model instanceof MultiLayerNetwork) {
                    updaterState = ((MultiLayerNetwork) model).getUpdater().getStateViewArray();
                } else if (model instanceof ComputationGraph) {
                    updaterState = ((ComputationGraph) model).getUpdater().getStateViewArray();
                }

                if (updaterState != null && updaterState.length() > 0L) {
                    ZipEntry updater = new ZipEntry("updaterState.bin");
                    zipfile.putNextEntry(updater);

                    try {
                        Nd4j.write(updaterState, dos);
                    } finally {
                        dos.flush();
                    }
                }
            }

            if (dataNormalization != null) {
                nEntry = new ZipEntry("normalizer.bin");
                zipfile.putNextEntry(nEntry);
                NormalizerSerializer.getDefault().write(dataNormalization, zipfile);
            }

            dos.close();
            zipfile.close();
        }
    }


    public static MultiLayerNetwork restoreMultiLayerNetwork(@NonNull File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("file is marked @NonNull but is null");
        } else {
            return restoreMultiLayerNetwork(file, true);
        }
    }

    public static MultiLayerNetwork restoreMultiLayerNetwork(@NonNull File file, boolean loadUpdater) throws IOException {
        if (file == null) {
            throw new NullPointerException("file is marked @NonNull but is null");
        } else {
            ZipFile zipFile = new ZipFile(file);
            boolean gotConfig = false;
            boolean gotCoefficients = false;
            boolean gotUpdaterState = false;
            boolean gotPreProcessor = false;
            String json = "";
            INDArray params = null;
            Updater updater = null;
            INDArray updaterState = null;
            DataSetPreProcessor preProcessor = null;
            ZipEntry config = zipFile.getEntry("configuration.json");
            if (config != null) {
                InputStream stream = zipFile.getInputStream(config);
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line = "";
                StringBuilder js = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    js.append(line).append("\n");
                }

                json = js.toString();
                reader.close();
                stream.close();
                gotConfig = true;
            }

            ZipEntry coefficients = zipFile.getEntry("coefficients.bin");
            ZipEntry prep;
            if (coefficients != null) {
                if (coefficients.getSize() > 0L) {
                    InputStream stream = zipFile.getInputStream(coefficients);
                    DataInputStream dis = new DataInputStream(new BufferedInputStream(stream));
                    params = Nd4j.read(dis);
                    dis.close();
                    gotCoefficients = true;
                } else {
                    prep = zipFile.getEntry("noParams.marker");
                    gotCoefficients = prep != null;
                }
            }

            InputStream stream;
            if (loadUpdater) {
                prep = zipFile.getEntry("updaterState.bin");
                if (prep != null) {
                    stream = zipFile.getInputStream(prep);
                    DataInputStream dis = new DataInputStream(new BufferedInputStream(stream));
                    updaterState = Nd4j.read(dis);
                    dis.close();
                    gotUpdaterState = true;
                }
            }

            prep = zipFile.getEntry("preprocessor.bin");
            if (prep != null) {
                stream = zipFile.getInputStream(prep);
                ObjectInputStream ois = new ObjectInputStream(stream);

                try {
                    preProcessor = (DataSetPreProcessor) ois.readObject();
                } catch (ClassNotFoundException var21) {
                    throw new RuntimeException(var21);
                }

                gotPreProcessor = true;
            }

            zipFile.close();
            if (gotConfig && gotCoefficients) {
                MultiLayerConfiguration confFromJson;
                try {
                    confFromJson = MultiLayerConfiguration.fromJson(json);
                } catch (Exception var20) {
                    ComputationGraphConfiguration cg;
                    try {
                        cg = ComputationGraphConfiguration.fromJson(json);
                    } catch (Exception var19) {
                        throw new RuntimeException("Error deserializing JSON MultiLayerConfiguration. Saved model JSON is not a valid MultiLayerConfiguration", var20);
                    }

                    if (cg.getNetworkInputs() != null && cg.getVertices() != null) {
                        throw new RuntimeException("Error deserializing JSON MultiLayerConfiguration. Saved model appears to be a ComputationGraph - use ModelSerializer.restoreComputationGraph instead");
                    }

                    throw var20;
                }

                ConvNetwork network = new ConvNetwork(confFromJson);
                network.init(params, false);
                if (gotUpdaterState && updaterState != null) {
                    network.getUpdater().setStateViewArray(network, updaterState, false);
                }

                return network;
            } else {
                throw new IllegalStateException("Model wasnt found within file: gotConfig: [" + gotConfig + "], gotCoefficients: [" + gotCoefficients + "], gotUpdater: [" + gotUpdaterState + "]");
            }
        }
    }

}
