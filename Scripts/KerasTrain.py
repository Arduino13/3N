from nnutils import *
import os
import numpy as np
import keras
import tensorflow as tf
from numpy.lib.stride_tricks import as_strided
import os
os.environ["TF_KERAS"]='1'
from keras_adamw import AdamW

'''
Script for training NN in Keras, this script is no longer used as PyTorch is now used instead.
Therefore the code isn't written very well, because it was only for internal purposes
but still should be working. 
'''

mel_size = 161 #number of mel cells
batch_size = 2 
num_epochs = 1250 #number of training epochs
lr_rate = 0.001 #learning rate
save = False #see line no. 213

gpus = tf.config.experimental.list_physical_devices('GPU')
for gpu in gpus:
  tf.config.experimental.set_memory_growth(gpu, True) #for better GPU memory usage

def ctc_loss(args):
    '''
    CTC loss function wrapper
    '''
    y_pred, y_true, input_lengths, label_lengths = args
    return keras.backend.ctc_batch_cost(y_true, y_pred, input_lengths, label_lengths)

freq_masking = torchaudio.transforms.FrequencyMasking(freq_mask_param=3)
time_masking = torchaudio.transforms.TimeMasking(time_mask_param=10)
#sampler = torchaudio.transforms.Resample(48000, 16000) #pouze v pripade commonVoice

text_transform = TextTransform()

def data_processing(data):
    '''
    Data preprocessing converting to mel scale, frequency and time masking,
    converting labels to int arrays
    '''
    spectrograms = []
    labels = []
    input_lengths = []
    label_lengths = []

    for (waveform, _, utterance, _, _, _) in data: #v pripade LibriSpeech
    #for(waveform, sample_rate, dictionary) in data: # v pripade commonVoice
        spec = torch.from_numpy(
            librosa.feature.melspectrogram(
                waveform.detach().numpy()[0],
                sr=16000,
                n_fft=512,
                hop_length=256,
                n_mels=161
            )
        )
        spec = freq_masking(spec)
        spec = time_masking(spec)
        spec = spec.squeeze(0).transpose(0,1)

        spectrograms.append(spec)

        label = torch.Tensor(
            text_transform.text_to_int(
                utterance.lower()
            )
        )
        labels.append(label)

        input_lengths.append(spec.shape[0])
        label_lengths.append(len(label))

    spectrograms = nn.utils.rnn.pad_sequence(
        spectrograms, batch_first=True
    ).unsqueeze(1).transpose(2,3).transpose(1,3)
    labels = nn.utils.rnn.pad_sequence(
        labels, batch_first=True
    )

    return spectrograms, labels, input_lengths, label_lengths

class DataGenerator(keras.utils.Sequence):
    '''
    Class that prepares data for training
    '''
    def __init__(self, data, batch_size=batch_size, shuffle=True):
        self.data = data
        self.batch_size = batch_size
        self.shuffle = shuffle
        self.list_IDs = [i for i in range(0,len(data))]
        self.on_epoch_end()

    def __len__(self):
         return int(np.floor(len(self.data)/ self.batch_size))

    def on_epoch_end(self):
        self.indexes = np.arange(len(self.list_IDs))
        if self.shuffle == True:
            np.random.shuffle(self.indexes)

    def __getitem__(self, index):
        indexes = self.indexes[index*self.batch_size:(index+1)*self.batch_size]

        X, y = self.__data_generation(indexes)

        return X, y

    def __data_generation(self, list_IDs_temp):
       specs, labels, in_len, lab_len = data_processing([self.data[i] for i in list_IDs_temp])

       return {'the_input':specs.detach().numpy(),
                'labels':labels.detach().numpy(),
                'input_lengthk': np.array(in_len),
                'label_length': np.array(lab_len)}, {'ctc' : np.zeros([self.batch_size])}


'''
Script saves current training epoch so in case of crash, it could restore correct
dynamic learning rate
'''
with open('epoch.txt', 'r') as f:
    currentEpoch = int(f.read())

def adaptLearningRate(epoch):
    '''
    Dynamic learning rate implementation
    '''
    epoch += currentEpoch

    with open('epoch.txt', 'w') as f:
        f.write(str(epoch))

    if epoch > num_epochs:
        exit()

    if(epoch>(num_epochs-1)):
        return 0

    top = float(num_epochs/3)
    if epoch<top:
        temp = ((epoch+10)/top)*lr_rate
        return temp
    else:
        temp = ((num_epochs-epoch)/(num_epochs-top))*lr_rate
        return temp

#Creating layers used by ctc loss function
labels = keras.layers.Input(name='labels', shape=(None,), dtype='int64')
input_length = keras.layers.Input(name='input_lengthk', shape=(1,), dtype='int64')
label_length = keras.layers.Input(name='label_length', shape=(1,), dtype='int64')

train_dataset = torchaudio.datasets.LIBRISPEECH("/mnt/LibriSpeech/", url="train-other-500", download=False)
train_gen = DataGenerator(train_dataset)

#Main NN architecture
def train():
    input_tensor = keras.layers.Input(name='the_input', shape=(None,mel_size,1))
    '''
    Keras only allows implementation of CNN with schema (time, features, channels)
    while PyTorch has channels at the beginning (channels, features, time)
    that is why there are permutations, maybe thinking about redesign in future ?
    '''
    y = keras.layers.Permute((2,1,3))(input_tensor) 
    y = keras.layers.Conv2D(16,3,(1,1),'same', activation='selu')(y)
    #Best match for gelu funciton in keras was selu 
    y = keras.layers.Dropout(rate=0.1)(y)
    y = keras.layers.Permute((3,2,1))(y)
    y = keras.layers.LayerNormalization()(y)
    y = keras.layers.Permute((3,2,1))(y)

    y = keras.layers.Conv2D(16,3,(1,1),'same', activation='selu')(y)
    y = keras.layers.Dropout(rate=0.1)(y)
    y = keras.layers.Permute((3,2,1))(y)
    y = keras.layers.LayerNormalization()(y)
    y = keras.layers.Permute((3,2,1))(y)

    y = keras.layers.Permute((1,3,2))(y)

    y = keras.layers.Reshape([2576,-1])(y)
    y = keras.layers.Permute((2,1))(y)

    y = keras.layers.Dense(200, activation='selu')(y)

    y = keras.layers.Conv1D(200, 3, 1, 'same', activation='selu')(y)
    y = keras.layers.Dropout(rate=0.1)(y)
    y = keras.layers.LayerNormalization()(y)

    y = keras.layers.Dense(200, activation='selu')(y)

    for i in range(0,4):
        y = keras.layers.LayerNormalization()(y)
        y = keras.layers.Activation('selu')(y)
        y = keras.layers.Bidirectional(keras.layers.GRU(200, return_sequences=True))(y)
        y = keras.layers.Dropout(rate=0.1)(y)

    y = keras.layers.Dense(200, activation='selu')(y)
    y = keras.layers.Dropout(rate=0.1)(y)
    y = keras.layers.Dense(29)(y)
    y = keras.layers.TimeDistributed(keras.layers.Dense(29))(y)
    #y = tf.clip_by_value(y,1e-10, 0.5)
    output = keras.layers.Activation('softmax')(y)
    #y = tf.clip_by_value(y,1e-10,0.5)
    #output = tf.nn.log_softmax(y, axis=2) #log softmax causes gradient explosion

    model = keras.Model(input_tensor,output)
    model.output_length = lambda x: x
    model.load_weights('chck_v3') #loads only weights

    if save:
        model.save('KerasModel') #saves entire network with it's architecture
				#used for converting model to mobile version
        exit()

    output_lengths = keras.layers.Lambda(model.output_length)(input_length)
    loss_out = keras.layers.Lambda(ctc_loss, output_shape=(1,), name='ctc')([model.output, labels, output_lengths, label_length])

    #final model with ctc layer 
    model_final = keras.Model([model.input, labels, input_length, label_length], loss_out)

    model_final.summary()
    model_final.compile(loss={'ctc' : lambda y_true, y_pred: y_pred},
                        optimizer=AdamW(learning_rate=lr_rate))
    scheduler = keras.callbacks.LearningRateScheduler(adaptLearningRate) #for updating learning rate
    checkpoint=keras.callbacks.ModelCheckpoint('chck_v3', save_weights_only=True, verbose=1)
    tensorboard_callback = keras.callbacks.TensorBoard(log_dir="/mnt/LibriSpeech", histogram_freq=1, embeddings_freq=1)

    callbacks_list = [checkpoint, scheduler, tensorboard_callback]

    model_final.fit(train_gen,  shuffle=True ,epochs=num_epochs, steps_per_epoch=200, callbacks=callbacks_list, workers=4)

train()
